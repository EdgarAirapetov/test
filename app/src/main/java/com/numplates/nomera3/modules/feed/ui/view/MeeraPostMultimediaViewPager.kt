package com.numplates.nomera3.modules.feed.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import androidx.viewpager2.widget.ViewPager2
import com.google.android.exoplayer2.ui.PlayerView
import com.meera.core.extensions.gone
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraViewPostMultimediaViewPagerBinding
import com.numplates.nomera3.modules.feed.ui.MeeraPostCallback
import com.numplates.nomera3.modules.feed.ui.entity.MediaAssetEntity
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.newroads.data.ISensitiveContentManager
import com.numplates.nomera3.modules.posts.ui.view.VideoDurationView
import com.numplates.nomera3.modules.volume.domain.model.VolumeState
import com.numplates.nomera3.modules.volume.presentation.VolumeStateCallback
import com.numplates.nomera3.presentation.view.utils.zoomy.CanPerformZoom
import com.numplates.nomera3.presentation.view.utils.zoomy.ZoomListener
import com.numplates.nomera3.presentation.view.utils.zoomy.Zoomy
import timber.log.Timber
import kotlin.math.abs

private const val MULTIMEDIA_PAGER_INDICATOR_DELAY = 50L
class MeeraPostMultimediaViewPager @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private var postId: Long? = null
    private var items: List<MediaAssetEntity> = emptyList()
    private var mediaPreviewMaxWidth = 0
    private var mediaPreviewMaxHeight = 0
    private var currentPagerPosition = -1
    private var currentMediaCount = 0
    private var postCallback: MeeraPostCallback? = null
    private var volumeStateCallback: VolumeStateCallback? = null
    private var adapterItemTouchListener: OnItemTouchListener? = null

    private val binding = LayoutInflater.from(context)
        .inflate(R.layout.meera_view_post_multimedia_view_pager, this, false)
        .apply(::addView)
        .let(MeeraViewPostMultimediaViewPagerBinding::bind)

    private var mediaAdapter: MeeraPostMultimediaPagerAdapter? = null

    private var pagerListener: ViewPager2.OnPageChangeCallback? = null

    fun setCurrentMediaPosition(position: Int) {
        binding.vpPostMultimediaPager.apply {
            if (currentItem == position) return@apply
            pagerListener?.let {
                unregisterOnPageChangeCallback(it)
                setCurrentItem(position, false)
                registerOnPageChangeCallback(it)
                binding.spiPostMultimediaIndicator.setCurrentPosition(position)
                currentPagerPosition = position
                initPagerIndicators()
            }
        }
    }

    fun updateVolume(volumeState: VolumeState) {
        getCurrentHolder()?.updateVolumeState(volumeState)
    }

    fun bind(
        post: PostUIEntity,
        mediaPreviewMaxWidth: Int,
        mediaPreviewMaxHeight: Int,
        postCallback: MeeraPostCallback?,
        volumeStateCallback: VolumeStateCallback?,
        zoomyProvider: Zoomy.ZoomyProvider?,
        parentView: View,
        canPerformZoom: CanPerformZoom,
        contentManager: ISensitiveContentManager?,
        onMediaClicked: (MediaAssetEntity, PostUIEntity?) -> Unit,
        zoomListener: ZoomListener,
        forceBind: Boolean = false
    ) {
        if (!isAvailableToUpdateView()) return
        if (!forceBind && !isNeedToBindView(post)) return

        mediaAdapter = MeeraPostMultimediaPagerAdapter(onItemClicked = onMediaClicked)

        pagerListener = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager2.SCROLL_STATE_IDLE) initPagerIndicators()
            }

            override fun onPageSelected(position: Int) {
                binding.spiPostMultimediaIndicator.setCurrentPosition(position)
                if (currentPagerPosition == position) return
                currentPagerPosition = position
                postCallback?.apply {
                    if (mediaAdapter?.isItemVideo(currentPagerPosition).isTrue()) {
                        onStartPlayingVideoRequested()
                    } else {
                        onStopPlayingVideoRequested()
                    }
                    onMediaExpandCheckRequested()
                    post { postId?.let { onMultimediaPostSwiped(it, position) } }
                }
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                if (positionOffset != 0f) {
                    hideAllDurations()
                } else {
                    showAllDurations()
                }
            }
        }

        this.postId = post.postId
        this.items = post.assets ?: emptyList()
        this.mediaPreviewMaxWidth = mediaPreviewMaxWidth
        this.mediaPreviewMaxHeight = mediaPreviewMaxHeight
        this.postCallback = postCallback
        this.volumeStateCallback = volumeStateCallback

        initPager(parentView)
        initBottomGradient(items.size)

        if (items.isEmpty()) {
            binding.clPostMultimediaPagerRootContainer.gone()
            setNewViewPagerHeight(0)
        } else {
            val pagerMaxWidth = mediaPreviewMaxWidth
            val pagerMaxHeight = mediaPreviewMaxHeight

            setNewViewPagerHeight(pagerMaxHeight)

            mediaAdapter?.setStrictMeasures(
                mediaPreviewStrictHeight = pagerMaxHeight,
                mediaPreviewStrictWidth = pagerMaxWidth
            )
            mediaAdapter?.bind(
                post = post,
                zoomyProvider = zoomyProvider,
                postCallback = postCallback,
                volumeStateCallback = volumeStateCallback,
                canPerformZoom = canPerformZoom,
                contentManager = contentManager,
                zoomListener = zoomListener
            )
            mediaAdapter?.submitList(items)
            binding.spiPostMultimediaIndicator.setDotCount(items.size)
            initCurrentPage(post)
            initPagerIndicators()
            binding.clPostMultimediaPagerRootContainer.visible()
        }
        this.currentMediaCount = items.size
    }

    fun unbind() {
        postCallback = null
        pagerListener?.apply {
            binding.vpPostMultimediaPager.unregisterOnPageChangeCallback(this)
                .also { pagerListener = null }
        }
        adapterItemTouchListener?.let {
            (binding.vpPostMultimediaPager[0] as? RecyclerView?)?.apply {
                removeOnItemTouchListener(it)
            }
            adapterItemTouchListener = null
        }
        binding.vpPostMultimediaPager.adapter = null
        items = listOf()
        mediaAdapter?.submitList(emptyList())
        mediaAdapter?.unbind()
        mediaAdapter = null
    }

    fun getCurrentVideoUrl(): String? = mediaAdapter?.getCurrentVideoUrl(binding.vpPostMultimediaPager.currentItem)

    fun getCurrentMedia() = mediaAdapter?.getItem(binding.vpPostMultimediaPager.currentItem)

    fun getCurrentMediaPosition() = binding.vpPostMultimediaPager.currentItem

    fun getCurrentPlayer(): PlayerView? = getCurrentHolder()?.getVideoPlayer()

    fun getCurrentDurationView(): VideoDurationView? = getCurrentHolder()?.getVideoDuration()

    fun startPlayingVideo(position: Long?) = getCurrentHolder()?.startPlayingVideo(position)

    fun stopPlayingVideo() {
        val pagerList = (binding.vpPostMultimediaPager[0] as? RecyclerView?) ?: return
        val itemCount = mediaAdapter?.itemCount ?: 0
        for (i in 0 until itemCount) {
            runCatching {
                val holder = pagerList.findViewHolderForAdapterPosition(i)
                (holder as? MeeraPostMultimediaPagerAdapter.MeeraPostMultimediaPagerViewHolder?)?.stopPlayingVideo()
            }.onFailure { Timber.e(it) }
        }
    }

    fun enableSwipe() {
        binding.vpPostMultimediaPager.isUserInputEnabled = true
    }

    fun disableSwipe() {
        binding.vpPostMultimediaPager.isUserInputEnabled = false
    }

    fun hideInterface() {
        binding.spiPostMultimediaIndicator.gone()
    }

    fun showInterface() {
        if (currentMediaCount > 1)
            binding.spiPostMultimediaIndicator.visible()
    }

    private fun initBottomGradient(size: Int) {
        binding.vBottomGradient.isVisible = size > 1
    }

    private fun showAllDurations() {
        val pagerList = (binding.vpPostMultimediaPager[0] as? RecyclerView?) ?: return
        val itemCount = mediaAdapter?.itemCount ?: 0
        for (i in 0 until itemCount) {
            runCatching {
                val holder = pagerList.findViewHolderForAdapterPosition(i)
                (holder as? MeeraPostMultimediaPagerAdapter.MeeraPostMultimediaPagerViewHolder?)?.showVideoDuration()
            }.onFailure { Timber.e(it) }
        }
    }

    private fun hideAllDurations() {
        val pagerList = (binding.vpPostMultimediaPager[0] as? RecyclerView?) ?: return
        val itemCount = mediaAdapter?.itemCount ?: 0
        for (i in 0 until itemCount) {
            runCatching {
                val holder = pagerList.findViewHolderForAdapterPosition(i)
                (holder as? MeeraPostMultimediaPagerAdapter.MeeraPostMultimediaPagerViewHolder?)?.hideVideoDuration()
            }.onFailure { Timber.e(it) }
        }
    }

    private fun isAvailableToUpdateView(): Boolean {
        if (binding.vpPostMultimediaPager.childCount < 1) return true
        val multimediaRecyclerView = (binding.vpPostMultimediaPager[0] as? RecyclerView?) ?: return true
        return !multimediaRecyclerView.isComputingLayout && multimediaRecyclerView.scrollState == ViewPager2.SCROLL_STATE_IDLE
    }

    private fun isNeedToBindView(post: PostUIEntity): Boolean {
        return post.postId != postId || post.assets != items
    }

    private fun setNewViewPagerHeight(height: Int) {
        val constraints = ConstraintSet()
        constraints.apply {
            clone(binding.clPostMultimediaPagerRootContainer)
            constrainWidth(binding.vpPostMultimediaPager.id, ConstraintSet.MATCH_CONSTRAINT_SPREAD)
            constrainHeight(binding.vpPostMultimediaPager.id, height)
            applyTo(binding.clPostMultimediaPagerRootContainer)
        }
    }

    private fun initPager(parentView: View) {
        binding.vpPostMultimediaPager.apply {
            adapter = mediaAdapter
            pagerListener?.let { unregisterOnPageChangeCallback(it) }
            (binding.vpPostMultimediaPager[0] as? RecyclerView?)?.isNestedScrollingEnabled = false

            adapterItemTouchListener = object : OnItemTouchListener {
                private var initialX = 0f
                private val VIEW_PAGER_SWIPE_THRESHOLD = 10f
                override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                    when (e.action) {
                        MotionEvent.ACTION_DOWN -> {
                            initialX = e.x
                            parentView.parent.requestDisallowInterceptTouchEvent(true)
                        }

                        MotionEvent.ACTION_MOVE -> {
                            val diffX = e.x - initialX
                            parentView.parent.requestDisallowInterceptTouchEvent(abs(diffX) > VIEW_PAGER_SWIPE_THRESHOLD)
                        }

                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                            parentView.parent.requestDisallowInterceptTouchEvent(false)
                    }
                    return false
                }

                override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) = Unit

                override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) = Unit
            }.also { itemTouchListener ->
                (binding.vpPostMultimediaPager[0] as? RecyclerView?)?.apply {
                    isNestedScrollingEnabled = false
                    addOnItemTouchListener(itemTouchListener)
                }
            }
        }
    }

    private fun initCurrentPage(post: PostUIEntity) {
        binding.apply {
            val itemCount = mediaAdapter?.itemCount ?: 0
                val neededMediaPosition = post.getMediaPosition()
                vpPostMultimediaPager.apply {
                    pagerListener?.let {
                        unregisterOnPageChangeCallback(it)
                        if (neededMediaPosition != vpPostMultimediaPager.currentItem) {
                            setCurrentItem(neededMediaPosition, false)
                        }
                        registerOnPageChangeCallback(it)
                    }
                }
            if (neededMediaPosition in 0..itemCount) {
                spiPostMultimediaIndicator.visible()
                spiPostMultimediaIndicator.setCurrentPosition(neededMediaPosition)
            } else {
                spiPostMultimediaIndicator.gone()
            }
        }
    }

    private fun initPagerIndicators() {
        postDelayed({
            val position = binding.vpPostMultimediaPager.currentItem
            val viewHolder = (binding.vpPostMultimediaPager.getChildAt(0) as? RecyclerView)
                ?.findViewHolderForAdapterPosition(position)
            val mediaView =
                viewHolder?.itemView?.findViewById<MeeraPostMultimediaPagerItemView>(R.id.pmpiv_media_item_view) ?: run {
                    initIndicatorCount(position)
                    return@postDelayed
                }
            val horizontalMargin = calculateHorizontalMargin(mediaView.getMediaPreviewWidth())
            val verticalMargin = calculateVerticalMargin(mediaView.getMediaPreviewHeight())

            binding.flPostMultimediaCountIndicator.setMargins(
                top = verticalMargin,
                end = horizontalMargin
            )

            initIndicatorCount(position)
        }, MULTIMEDIA_PAGER_INDICATOR_DELAY)
    }

    private fun getCurrentHolder(): MeeraPostMultimediaPagerAdapter.MeeraPostMultimediaPagerViewHolder? {
        val pagerList = (binding.vpPostMultimediaPager[0] as? RecyclerView?)
        val currentHolder = pagerList?.findViewHolderForAdapterPosition(binding.vpPostMultimediaPager.currentItem)
        return currentHolder as? MeeraPostMultimediaPagerAdapter.MeeraPostMultimediaPagerViewHolder?
    }

    private fun initIndicatorCount(position: Int) {
        val itemsCount = mediaAdapter?.itemCount ?: 0
        binding.flPostMultimediaCountIndicator.isVisible = itemsCount != 1

        val countPosStr = "${position + 1}/${itemsCount}"
        binding.tvPostMultimediaCountIndicator.text = countPosStr
    }

    private fun calculateHorizontalMargin(mediaPreviewWidth: Int): Int {
        val widthMeasure = (binding.vpPostMultimediaPager.width - mediaPreviewWidth) / 2
        return widthMeasure
    }

    private fun calculateVerticalMargin(mediaPreviewHeight: Int): Int {
        val heightMeasure = (binding.vpPostMultimediaPager.height - mediaPreviewHeight) / 2
        return heightMeasure
    }
}
