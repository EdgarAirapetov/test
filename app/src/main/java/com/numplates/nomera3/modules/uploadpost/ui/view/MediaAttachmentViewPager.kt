package com.numplates.nomera3.modules.uploadpost.ui.view

import android.animation.LayoutTransition
import android.animation.LayoutTransition.APPEARING
import android.animation.LayoutTransition.DISAPPEARING
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_IDLE
import com.meera.core.extensions.getScreenWidth
import com.meera.core.extensions.gone
import com.meera.core.extensions.newHeight
import com.meera.core.extensions.newSize
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.numplates.nomera3.MAX_ASPECT
import com.numplates.nomera3.MIN_ASPECT
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ViewMediaAttachmentViewPagerBinding
import com.numplates.nomera3.modules.uploadpost.ui.AttachmentMediaActions
import com.numplates.nomera3.modules.uploadpost.ui.AttachmentMediaAdapter
import com.numplates.nomera3.modules.uploadpost.ui.data.AttachmentPostType
import com.numplates.nomera3.modules.uploadpost.ui.data.UIAttachmentMediaModel

class MediaAttachmentViewPager @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private var mediaPreviewMaxHeight = 0
    private var currentPagerPosition = 0
    private var currentMediaCount = 0
    private var previewHeightInitialized = false
    private val binding = LayoutInflater.from(context)
        .inflate(R.layout.view_media_attachment_view_pager, this, false)
        .apply(::addView)
        .let(ViewMediaAttachmentViewPagerBinding::bind)

    private var attachmentMediaActions: AttachmentMediaActions? = null

    private val attachmentMediaAdapter: AttachmentMediaAdapter by lazy(LazyThreadSafetyMode.NONE) {
        AttachmentMediaAdapter(imagePositioningListener = { imagePositioningInProcess ->
            setupImagePositioningProcess(imagePositioningInProcess)
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupImagePositioningProcess(imagePositioningInProcess: Boolean) {
        if (imagePositioningInProcess) {
            binding.mediaAttachmentViewPager.setOnTouchListener(null)
        } else {
            initPagerIndicators()
            binding.mediaAttachmentViewPager.setOnTouchListener { _, _ -> true }
        }
    }

    init {
        initPager()
        initClickListeners()
        initIconAppearingAnimation()
    }

    fun initializeActions(attachmentMediaActions: AttachmentMediaActions) {
        this.attachmentMediaActions = attachmentMediaActions
        this.attachmentMediaAdapter.setAttachmentActions(attachmentMediaActions)
    }

    private fun initPager() {
        binding.mediaAttachmentViewPager.adapter = attachmentMediaAdapter
        binding.mediaAttachmentViewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                when (state) {
                    SCROLL_STATE_IDLE -> initPagerIndicators()
                    else -> Unit
                }
            }

            override fun onPageSelected(position: Int) {
                binding.attachmentViewPagerSpiIndicator.setCurrentPosition(position)
                currentPagerPosition = position
            }
        })
    }

    private fun initClickListeners() {
        binding.ivEditAttachment.setThrottledClickListener {
            attachmentMediaActions?.onItemEditClick(currentPagerPosition)
        }

        binding.ivDeleteAttachment.setOnClickListener {
            attachmentMediaActions?.onItemCloseClick(currentPagerPosition)
        }

        binding.ivOpenEditorStickers.setThrottledClickListener {
            attachmentMediaActions?.onAddStickerClick(currentPagerPosition)
        }
    }

    private fun initIconAppearingAnimation() {
        binding.pagerRootContainer.apply {
            layoutTransition = null
            layoutTransition = LayoutTransition().apply {
                enableTransitionType(APPEARING)
                enableTransitionType(DISAPPEARING)
                disableTransitionType(LayoutTransition.CHANGING)
                disableTransitionType(LayoutTransition.CHANGE_APPEARING)
                disableTransitionType(LayoutTransition.CHANGE_DISAPPEARING)
            }
        }
    }

    fun submitData(
        items: List<UIAttachmentMediaModel>,
        mediaPreviewMaxHeight: Int
    ) {
        previewHeightInitialized = false
        this.mediaPreviewMaxHeight = mediaPreviewMaxHeight
        attachmentMediaAdapter.items = items
        if (items.isEmpty()) {
            binding.attachmentViewPagerViewGradient.gone()
            binding.pagerRootContainer.gone()
            binding.mediaAttachmentViewPager.newHeight(0)
        } else {
            val sizePair = calculateContainerSize(items.first(), mediaPreviewMaxHeight,)
            val pagerTotalWidth = sizePair.first
            val pagerTotalHeight = sizePair.second
            binding.mediaAttachmentViewPager.newSize(width = pagerTotalWidth, height = pagerTotalHeight)
            attachmentMediaAdapter.setStrictMeasures(pagerTotalHeight, pagerTotalWidth)
            binding.attachmentViewPagerSpiIndicator.setDotCount(items.size)
            initCurrentPage(items)
            initPagerIndicators()
            initGradient(items.size)
            binding.pagerRootContainer.visible()
        }
        this.currentMediaCount = items.size
    }

    private fun initGradient(size: Int) {
        binding.attachmentViewPagerViewGradient.isVisible = size > 1
    }

    private fun initCurrentPage(items: List<UIAttachmentMediaModel>) {
        val newCount = items.size
        val selectPosition = when {
            currentMediaCount == newCount -> currentPagerPosition
            currentMediaCount > newCount -> {
                if (currentPagerPosition > (newCount - 1)) {
                    currentPagerPosition - 1
                } else {
                    currentPagerPosition
                }
            }

            else -> {
                items.size - 1
            }
        }
        if (!(currentMediaCount == 0 && newCount > 1))
            if (selectPosition in 0..attachmentMediaAdapter.itemCount) {
                binding.mediaAttachmentViewPager.setCurrentItem(selectPosition, false)
                binding.attachmentViewPagerSpiIndicator.setCurrentPosition(selectPosition)
            }
    }

    private fun calculateContainerSize(
        mediaModel: UIAttachmentMediaModel,
        maxHeight: Int
    ): Pair<Int, Int> {
        val maxWidth = getScreenWidth()
        val imageHeight = mediaModel.attachmentHeight
        val imageWidth = mediaModel.attachmentWidth
        val imageAspect = imageWidth.toDouble() / imageHeight
        var containerWidth : Int
        var containerHeight : Int

        if (imageHeight >= imageWidth) {
            containerHeight = maxHeight
            containerWidth = (containerHeight * imageAspect).toInt()

            val minWidth = (containerHeight * MIN_ASPECT).toInt()
            if (containerWidth < minWidth) {
                containerWidth = minWidth
            }
        } else {
            containerWidth = maxWidth
            containerHeight = (containerWidth / imageAspect).toInt()

            val minHeight = (containerWidth / MAX_ASPECT).toInt()
            if (containerHeight < minHeight) {
                containerHeight = minHeight
            }
        }

        return Pair(containerWidth, containerHeight)
    }

    private fun initPagerIndicators() {
            val position = binding.mediaAttachmentViewPager.currentItem
            initIndicatorCount(position)
            initEditIndicator()
    }

    private fun initEditIndicator() {
        getCurrentAttachmentItem()?.let {
            when (it.type) {
                AttachmentPostType.ATTACHMENT_GIF -> {
                    binding.ivDeleteAttachment.visible()
                    binding.flEditAttachment.gone()
                    binding.ivOpenEditorStickers.gone()
                }

                AttachmentPostType.ATTACHMENT_PREVIEW -> {
                    binding.ivDeleteAttachment.visible()
                    binding.flEditAttachment.visible()
                    binding.ivEditAttachment.gone()
                    binding.ivOpenEditorStickers.gone()
                    binding.pbEditAttachment.visible()
                }

                else -> {
                    binding.ivDeleteAttachment.visible()
                    binding.ivOpenEditorStickers.visible()
                    binding.flEditAttachment.visible()
                    binding.pbEditAttachment.gone()
                    binding.ivEditAttachment.visible()
                }
            }

        }
    }

    private fun initIndicatorCount(position: Int) {
        val itemsCount = attachmentMediaAdapter.itemCount
        if (itemsCount == 1) {
            binding.flCountOrderAttachment.gone()
        } else {
            binding.flCountOrderAttachment.visible()
        }
        val countPosStr = "${position + 1}/${attachmentMediaAdapter.itemCount}"
        binding.tvCountOrderAttachment.text = countPosStr
    }

    private fun getCurrentAttachmentItem(): UIAttachmentMediaModel? {
        val items = attachmentMediaAdapter.items
        if (items.isNotEmpty() && (items.size) > currentPagerPosition) {
            return items[currentPagerPosition]
        }
        return null
    }

    fun setAspectPreviewHeight(mediaAspect: Float, mediaPreviewMaxHeight: Int) {
        val mediaPreviewMaxWidth = getScreenWidth()
        val newHeight = if (mediaAspect <= mediaPreviewMaxWidth.toFloat() / mediaPreviewMaxHeight.toFloat()) {
            mediaPreviewMaxHeight
        } else {
            (mediaPreviewMaxWidth.toFloat() / mediaAspect).toInt()
        }
        binding.pagerRootContainer.visible()
        binding.mediaAttachmentViewPager.newHeight(newHeight)
        previewHeightInitialized = true
    }

    fun hasAttachments(): Boolean = attachmentMediaAdapter.items.isNotEmpty() || previewHeightInitialized

    fun previewHeightInitializing() = previewHeightInitialized

}
