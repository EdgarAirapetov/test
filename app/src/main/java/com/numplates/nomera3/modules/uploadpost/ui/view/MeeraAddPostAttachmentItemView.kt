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
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.loadGlideWithOptions
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.vibrate
import com.meera.core.extensions.visible
import com.numplates.nomera3.MIN_ASPECT
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraAddPostAttachmentItemBinding
import com.numplates.nomera3.modules.uploadpost.ui.AttachmentPostActions
import com.numplates.nomera3.modules.uploadpost.ui.data.AttachmentPostType
import com.numplates.nomera3.modules.uploadpost.ui.data.UIAttachmentPostModel
import kotlin.math.max

private const val DEFAULT_IMAGE_HEIGHT = 144
private const val ANIMATION_DURATION = 200L
private const val FIRST_FRAME_TIME = 0L

@SuppressLint("ClickableViewAccessibility")
class MeeraAddPostAttachmentItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : ConstraintLayout(context, attrs, defStyle) {

    private val binding = LayoutInflater.from(context)
        .inflate(R.layout.meera_add_post_attachment_item, this, false)
        .apply(::addView)
        .let(MeeraAddPostAttachmentItemBinding::bind)

    private val gestureDetector: GestureDetector
    private val gestureListener = AddPostAttachmentItemGestureDetector()
    private var actions: AttachmentPostActions? = null
    private var mediaPreviewMaxWidth: Int = 0
    private var mediaPreviewMaxHeight: Int = 0
    private var isNeedMediaPositioning: Boolean = false
    private var attachment: UIAttachmentPostModel? = null
    private var attachmentPreviewHeight: Int = 0
    private var attachmentPreviewWidth: Int = 0

    init {
        gestureDetector = GestureDetector(context, gestureListener)
    }

    fun bind(
        actions: AttachmentPostActions,
        attachment: UIAttachmentPostModel,
        mediaPreviewMaxWidth: Int,
        mediaPreviewMaxHeight: Int,
        isNeedMediaPositioning: Boolean
    ) {
        this.actions = actions
        this.attachment = attachment
        this.mediaPreviewMaxWidth = mediaPreviewMaxWidth
        this.mediaPreviewMaxHeight = mediaPreviewMaxHeight
        this.isNeedMediaPositioning = isNeedMediaPositioning

        resetView()
        visible()

        setupViews(attachment)
    }

    private fun setupViews(attachment: UIAttachmentPostModel) {
        attachmentPreviewHeight = calculateAttachmentHeight(attachment)
        attachmentPreviewWidth = calculateAttachmentWidth(attachment)

        setupHeight(attachmentPreviewHeight)

        with(binding) {
            ivMediaAttachment.bind(
                isEditMode = true,
                onImageSet = {
                setupViewsAfterImageLoaded(attachment.type)
            })
            ivMediaAttachment.visible()
        }
        when (attachment.type) {
            AttachmentPostType.ATTACHMENT_PHOTO ->
                setupImageAttachment(attachment, attachmentPreviewWidth, attachmentPreviewHeight)

            AttachmentPostType.ATTACHMENT_PREVIEW -> {
                setupImageAttachment(attachment, attachmentPreviewWidth, attachmentPreviewHeight)
            }

            AttachmentPostType.ATTACHMENT_GIF ->
                setupGifAttachment(attachment, attachmentPreviewWidth, attachmentPreviewHeight)

            AttachmentPostType.ATTACHMENT_VIDEO ->
                setupVideoAttachment(attachment, attachmentPreviewWidth, attachmentPreviewHeight)
        }
        initClickListeners(attachment)
        initTouch(attachment, attachmentPreviewWidth, attachmentPreviewHeight, isNeedMediaPositioning)
    }

    fun showLoading() {
        with(binding) {
            ivEditAttachmentComplaint.gone()
            pbAttachment.visible()
        }
    }

    fun resetView() {
        with(binding) {
            ivMediaAttachment.setImageDrawable(null)
            cvPlayBtn.gone()
            ivDeleteAttachmentComplaint.gone()
            ivEditAttachmentComplaint.gone()
            pbAttachment.gone()
        }
    }

    private fun setupViewsAfterImageLoaded(type: AttachmentPostType) {
        with(binding) {
            cvPlayBtn.isVisible =
                type != AttachmentPostType.ATTACHMENT_PHOTO && type != AttachmentPostType.ATTACHMENT_GIF
            ivEditAttachmentComplaint.isVisible =
                type != AttachmentPostType.ATTACHMENT_GIF && type != AttachmentPostType.ATTACHMENT_PREVIEW
            ivDeleteAttachmentComplaint.visible()
            if (type == AttachmentPostType.ATTACHMENT_PREVIEW) {
                showLoading()
            } else {
                pbAttachment.gone()
            }
        }
    }

    private fun setupHeight(height: Int, needUpdate: Boolean = false) {
        if (this.measuredHeight == height && !needUpdate) return

        val minHeight = binding.root.minHeight
        val finalHeight = if (height < minHeight) minHeight else height

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

    private fun initClickListeners(attachment: UIAttachmentPostModel) {
        with(binding) {
            ivMediaAttachment.setThrottledClickListener { actions?.onItemClicked(attachment) }
            cvPlayBtn.setThrottledClickListener { actions?.onItemClicked(attachment) }
            ivEditAttachmentComplaint.setThrottledClickListener { actions?.onItemEditClick(attachment) }
            ivDeleteAttachmentComplaint.setThrottledClickListener { actions?.onItemCloseClick(attachment) }
        }
    }

    private fun calculateAttachmentHeight(attachment: UIAttachmentPostModel): Int {
        return if (attachment.attachmentHeight >= attachment.attachmentWidth) {
            if (mediaPreviewMaxHeight > 0) mediaPreviewMaxHeight else DEFAULT_IMAGE_HEIGHT.dp
        } else {
            attachment.attachmentHeight * calculateAttachmentWidth(attachment) / attachment.attachmentWidth
        }
    }

    private fun calculateAttachmentWidth(attachment: UIAttachmentPostModel): Int {
        return if (attachment.attachmentWidth > attachment.attachmentHeight) {
            mediaPreviewMaxWidth
        } else {
            val aspect: Double = max(
                MIN_ASPECT,
                attachment.attachmentWidth.toDouble() / attachment.attachmentHeight.toDouble()
            )
            (aspect * calculateAttachmentHeight(attachment)).toInt()
        }
    }

    private fun setupGifAttachment(attachment: UIAttachmentPostModel, width: Int, height: Int) {
        with(binding) {
            ivMediaAttachment.apply {
                binding.ivMediaAttachment.setGifMode()
                updateLayoutParams {
                    this.width = width
                    this.height = height
                }
                loadGlide(attachment.attachmentResource)
            }
        }
    }

    private fun setupImageAttachment(attachment: UIAttachmentPostModel, width: Int, height: Int) {
        with(binding) {
            ivMediaAttachment.apply {
                updateLayoutParams {
                    this.width = width
                    this.height = height
                }
                loadGlide(attachment.attachmentResource)
            }
        }
    }

    private fun setupVideoAttachment(attachment: UIAttachmentPostModel, width: Int, height: Int) {
        with(binding) {
            ivMediaAttachment.apply {
                updateLayoutParams {
                    this.width = width
                    this.height = height
                }
                val options = listOf(
                    RequestOptions().frame(FIRST_FRAME_TIME)
                )
                loadGlideWithOptions(attachment.attachmentResource, options)
            }
        }
    }

    private fun isNeedImageScroll(attachment: UIAttachmentPostModel, viewWidth: Int, viewHeight: Int): Boolean {
        return attachment.attachmentHeight > attachment.attachmentWidth &&
            attachment.attachmentWidth * viewHeight < viewWidth * attachment.attachmentHeight
    }

    private fun initTouch(attachment: UIAttachmentPostModel, width: Int, height: Int, isNeedMediaPositioning: Boolean) {
        binding.ivMediaAttachment.setOnTouchListener(
            when (attachment.type) {
                AttachmentPostType.ATTACHMENT_PHOTO -> {
                    if (isNeedImageScroll(
                            attachment,
                            width,
                            height
                        ) && isNeedMediaPositioning
                    ) onTouchListener else null
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
                        binding.ivDeleteAttachmentComplaint.visible()
                        return true
                    }
                }
            }
            return result
        }
    }

    private inner class AddPostAttachmentItemGestureDetector : GestureDetector.SimpleOnGestureListener() {
        var isLongPressed = false

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            binding.ivMediaAttachment.moveByY(distance = distanceY)
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
            binding.ivDeleteAttachmentComplaint.gone()
            super.onLongPress(e)
        }
    }
}
