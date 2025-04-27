package com.numplates.nomera3.modules.uploadpost.ui.view

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.bumptech.glide.request.RequestOptions
import com.meera.core.extensions.click
import com.meera.core.extensions.dp
import com.meera.core.extensions.glideClear
import com.meera.core.extensions.gone
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.loadGlideWithOptions
import com.meera.core.extensions.vibrate
import com.meera.core.extensions.visible
import com.numplates.nomera3.MIN_ASPECT
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ItemAddMediaAttachmentBinding
import com.numplates.nomera3.modules.uploadpost.ui.AttachmentMediaActions
import com.numplates.nomera3.modules.uploadpost.ui.data.AttachmentPostType
import com.numplates.nomera3.modules.uploadpost.ui.data.UIAttachmentMediaModel
import kotlin.math.max

private const val DEFAULT_IMAGE_HEIGHT = 144
private const val ANIMATION_DURATION = 200L
private const val FIRST_FRAME_TIME = 0L

@SuppressLint("ClickableViewAccessibility")
class AddMediaAttachmentItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle) {

    private val binding = LayoutInflater.from(context)
        .inflate(R.layout.item_add_media_attachment, this, false)
        .apply(::addView)
        .let(ItemAddMediaAttachmentBinding::bind)

    private val gestureDetector: GestureDetector
    private val gestureListener = AddPostAttachmentItemGestureDetector()
    private var actions: AttachmentMediaActions? = null
    private var mediaPreviewMaxWidth: Int = 0
    private var mediaPreviewMaxHeight: Int = 0
    private var attachment: UIAttachmentMediaModel? = null
    private var attachmentPreviewHeight: Int = 0
    private var attachmentPreviewWidth: Int = 0
    private var attachmentStrictHeight = 0
    private var attachmentStrictWidth = 0
    private var isVerticalScrollingEnabled: Boolean = false
    private var isHorizontalScrollingEnabled: Boolean = false
    private var imagePositioningInProcessListener: ((imagePositioningInProcess: Boolean) -> Unit)? = null

    init {
        gestureDetector = GestureDetector(context, gestureListener)
    }

    fun bind(
        actions: AttachmentMediaActions?,
        attachment: UIAttachmentMediaModel,
        mediaPreviewMaxWidth: Int,
        mediaPreviewMaxHeight: Int,
        attachmentStrictHeight: Int = 0,
        attachmentStrictWidth: Int = 0
    ) {
        this.actions = actions
        this.attachment = attachment
        this.mediaPreviewMaxWidth = mediaPreviewMaxWidth
        this.mediaPreviewMaxHeight = mediaPreviewMaxHeight
        this.attachmentStrictWidth = attachmentStrictWidth
        this.attachmentStrictHeight = attachmentStrictHeight

        resetView()
        visible()

        setupViews(attachment)
    }

    fun initImagePositioningInProcessListener(listener: (imagePositioningInProcess: Boolean) -> Unit) {
        this.imagePositioningInProcessListener = listener
    }

    private fun setupViews(attachment: UIAttachmentMediaModel) {

        attachmentPreviewWidth = attachmentStrictWidth
        attachmentPreviewHeight = attachmentStrictHeight

        setupHeight(attachmentPreviewHeight)
        initClickListeners(attachment)
        initTouch(attachment, attachmentPreviewWidth, attachmentPreviewHeight)
        initGridView(attachmentPreviewWidth, attachmentPreviewHeight)
        setupAttachmentImageView(attachment)
    }

    private fun setupAttachmentImageView(attachment: UIAttachmentMediaModel) {
        with(binding) {
            ivMediaAttachment.apply {

                bind(
                    isEditMode = true,
                    onImageSet = {
                        setupViewsAfterImageLoaded(attachment.type)
                    }
                )

                initAttachmentPositioning(attachment, this)
                visible()
            }
        }

        when (attachment.type) {
            AttachmentPostType.ATTACHMENT_PHOTO,
            AttachmentPostType.ATTACHMENT_PREVIEW ->
                setupAttachment(attachment)

            AttachmentPostType.ATTACHMENT_GIF ->
                setupGifAttachment(attachment)

            AttachmentPostType.ATTACHMENT_VIDEO ->
                setupVideoAttachment(attachment)
        }
    }

    fun resetView() {
        with(binding) {
            isVerticalScrollingEnabled = false
            isHorizontalScrollingEnabled = false
            ivMediaAttachment.setImageDrawable(null)
            cvPlayBtn.gone()
        }
    }

    private fun initAttachmentPositioning(
        attachment: UIAttachmentMediaModel,
        scrollableImageView: ScrollableImageView
    ) {
        scrollableImageView.apply {
            setMediaPositioning(attachment.mediaPositioning)

            if (isHorizontalScrollingEnabled) {
                setXPositioning()
                return
            }
            setDefaultPositioning()
        }
    }

    private fun setupViewsAfterImageLoaded(type: AttachmentPostType) {
        with(binding) {
            cvPlayBtn.isVisible = type == AttachmentPostType.ATTACHMENT_VIDEO
        }
    }

    private fun setupHeight(height: Int, needUpdate: Boolean = false) {
        if (this.measuredHeight == height && !needUpdate) return

        val minHeight = binding.root.minHeight
        val finalHeight = if (height < minHeight) minHeight else height

        binding.flMediaContainer.updateLayoutParams {
            this.height = height
        }

        if (this.measuredHeight == 0) {
            updateLayoutParams {
                this.height = finalHeight
            }
            return
        }

        with(ValueAnimator.ofInt(this.measuredHeight, finalHeight)) {
            addUpdateListener { valueAnimator ->
                val value = valueAnimator.animatedValue as Int
                updateLayoutParams {
                    this.height = value
                }
            }
            duration = ANIMATION_DURATION
            start()
        }
    }

    fun isNeedShowHint(): Boolean {
        val attachment = this.attachment
        return if (attachment != null && attachmentPreviewHeight != 0 && attachmentPreviewWidth != 0) {
            attachment.type == AttachmentPostType.ATTACHMENT_PHOTO
                && isNeedImageScroll(attachment, attachmentPreviewWidth, attachmentPreviewHeight)
        } else false
    }

    private fun initGridView(width: Int, height: Int) {
        binding.apaigGridView.updateLayoutParams {
            this.width = width
            this.height = height
        }
    }

    private fun initClickListeners(attachment: UIAttachmentMediaModel) {
        with(binding) {
            ivMediaAttachment.click { actions?.onItemClicked(attachment) }
            cvPlayBtn.click { actions?.onItemClicked(attachment) }
        }
    }

    private fun calculateAttachmentHeight(attachment: UIAttachmentMediaModel): Int {
        return if (attachment.attachmentHeight >= attachment.attachmentWidth) {
            if (mediaPreviewMaxHeight > 0) mediaPreviewMaxHeight else DEFAULT_IMAGE_HEIGHT.dp
        } else {
            attachment.attachmentHeight * calculateAttachmentWidth(attachment) / attachment.attachmentWidth
        }
    }

    private fun calculateAttachmentWidth(attachment: UIAttachmentMediaModel): Int {
        return if (attachment.attachmentWidth > attachment.attachmentHeight) {
            mediaPreviewMaxWidth
        } else {
            val aspect: Double =
                max(MIN_ASPECT, attachment.attachmentWidth.toDouble() / attachment.attachmentHeight.toDouble())
            (aspect * calculateAttachmentHeight(attachment)).toInt()
        }
    }

    private fun setupGifAttachment(attachment: UIAttachmentMediaModel) {
        with(binding) {
            ivMediaAttachment.apply {
                setGifMode()
                updateLayoutParams {
                    this.width = attachmentPreviewWidth
                    this.height = attachmentPreviewHeight
                }
                glideClear()
                loadGlide(attachment.getActualResource())
            }
        }
    }

    private fun setupAttachment(attachment: UIAttachmentMediaModel) {
        with(binding) {
            ivMediaAttachment.apply {
                setDefaultMode()
                updateLayoutParams {
                    this.width = attachmentPreviewWidth
                    this.height = attachmentPreviewHeight
                }
                glideClear()
                loadGlide(attachment.getActualResource())
            }
        }
    }

    private fun setupVideoAttachment(attachment: UIAttachmentMediaModel) {
        with(binding) {
            ivMediaAttachment.apply {
                setDefaultMode()
                updateLayoutParams {
                    this.width = attachmentPreviewWidth
                    this.height = attachmentPreviewHeight
                }
                val options = arrayListOf<RequestOptions>()
                options.add(RequestOptions().frame(FIRST_FRAME_TIME))
                loadGlideWithOptions(attachment.getActualResource(), options)
            }
        }
    }

    private fun isNeedImageScroll(attachment: UIAttachmentMediaModel, viewWidth: Int, viewHeight: Int): Boolean {
        val mediaWidth = attachment.attachmentWidth
        val mediaHeight = attachment.attachmentHeight

        if (mediaWidth == mediaHeight) {
            isVerticalScrollingEnabled = viewWidth > viewHeight
            isHorizontalScrollingEnabled = viewWidth < viewHeight
        } else {
            val widthAspect = viewWidth.toDouble() / mediaWidth.toDouble()
            val heightAspect = viewHeight.toDouble() / mediaHeight.toDouble()

            if ((widthAspect > heightAspect && (viewWidth * mediaHeight / mediaWidth) > viewHeight) ||
                (widthAspect < heightAspect && (viewHeight * mediaWidth / mediaHeight) > viewWidth)) {
                isVerticalScrollingEnabled = widthAspect > heightAspect
                isHorizontalScrollingEnabled = widthAspect < heightAspect
            }
        }

        return isVerticalScrollingEnabled || isHorizontalScrollingEnabled
    }

    private fun initTouch(attachment: UIAttachmentMediaModel, width: Int, height: Int) {
        val isNeedImageScroll = isNeedImageScroll(attachment, width, height)
        binding.ivMediaAttachment.setOnTouchListener(
            when (attachment.type) {
                AttachmentPostType.ATTACHMENT_PHOTO -> {
                    if (isNeedImageScroll) onTouchListener else null
                }
                AttachmentPostType.ATTACHMENT_VIDEO,
                AttachmentPostType.ATTACHMENT_GIF,
                AttachmentPostType.ATTACHMENT_PREVIEW -> null
            }
        )
    }

    private var onTouchListener = object : OnTouchListener {
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            if (event == null) return false
            val result = gestureDetector.onTouchEvent(event) || event.action == MotionEvent.ACTION_DOWN
            if (!result) {
                when (event.action) {
                    MotionEvent.ACTION_MOVE -> {
                        if (gestureListener.isLongPressed) {
                            gestureListener.isLongPressed = false
                            val cancel = MotionEvent.obtain(event)
                            cancel.action = MotionEvent.ACTION_CANCEL
                            gestureDetector.onTouchEvent(cancel)
                        }
                    }

                    MotionEvent.ACTION_UP -> {
                        parent?.requestDisallowInterceptTouchEvent(false)
                        imagePositioningInProcessListener?.invoke(false)
                        checkPositioningChanges()
                        binding.apaigGridView.gone()
                        return true
                    }
                }
            }
            return result
        }
    }

    private fun checkPositioningChanges() {
        attachment?.let {
            binding.ivMediaAttachment.apply {
                actions?.onItemPositionChange(
                    uiMediaModel = it,
                    x = getRelativeImageXPosition(),
                    y = getRelativeImageYPosition()
                )
            }
        }
    }

    private inner class AddPostAttachmentItemGestureDetector : GestureDetector.SimpleOnGestureListener() {
        var isLongPressed = false

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            val movingInProcess = binding.apaigGridView.isVisible
            if (isVerticalScrollingEnabled && movingInProcess)
                binding.ivMediaAttachment.moveByY(distance = distanceY)
            if (isHorizontalScrollingEnabled && movingInProcess)
                binding.ivMediaAttachment.moveByX(distance = distanceX)
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            val attachment = attachment ?: return true
            actions?.onItemClicked(attachment)
            return true
        }

        override fun onLongPress(e: MotionEvent) {
            isLongPressed = true
            parent?.requestDisallowInterceptTouchEvent(true)
            context.vibrate()
            binding.apaigGridView.visible()
            imagePositioningInProcessListener?.invoke(true)
            super.onLongPress(e)
        }
    }
}

