package com.numplates.nomera3.modules.feed.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.SCROLL_STATE_IDLE
import com.google.android.exoplayer2.ui.PlayerView
import com.meera.core.extensions.getScreenWidth
import com.meera.core.extensions.gone
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.newHeight
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ViewPostMultimediaViewPagerBinding
import com.numplates.nomera3.modules.feed.ui.PostCallback
import com.numplates.nomera3.modules.feed.ui.entity.MediaAssetEntity
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.newroads.data.ISensitiveContentManager
import com.numplates.nomera3.modules.posts.ui.view.VideoDurationView
import com.numplates.nomera3.modules.volume.domain.model.VolumeState
import com.numplates.nomera3.modules.volume.presentation.VolumeStateCallback
import com.numplates.nomera3.presentation.view.utils.zoomy.Zoomy
import timber.log.Timber
import kotlin.math.abs


private const val PAGE_VISIBILITY_THRESHOLD = 0.5

class PostMultimediaViewPager @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private var postId: Long? = null
    private var mediaPreviewMaxHeight = 0
    private var currentPagerPosition = -1
    private var currentIndicatorPosition = -1
    private var spiPosition = -1
    private var currentMediaCount = 0
    private var postCallback: PostCallback? = null
    private var volumeStateCallback: VolumeStateCallback? = null
    private var onMediaClicked: (MediaAssetEntity, PostUIEntity?) -> Unit = {_, _ -> }

    private val binding = LayoutInflater.from(context)
        .inflate(R.layout.view_post_multimedia_view_pager, this, false)
        .apply(::addView)
        .let(ViewPostMultimediaViewPagerBinding::bind)

    private var mediaAdapter: PostMultimediaPagerAdapter? = null

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

    fun clearResources() {
        pagerListener?.apply {
        binding.vpPostMultimediaPager.unregisterOnPageChangeCallback(this)
            .also { pagerListener = null }
        }
        mediaAdapter?.submitList(emptyList())
        binding.vpPostMultimediaPager.adapter = null
        mediaAdapter = null
    }

    fun updateVolume(volumeState: VolumeState) {
        getCurrentHolder()?.updateVolumeState(volumeState)
    }

    fun bind(
        post: PostUIEntity,
        mediaPreviewMaxHeight: Int,
        postCallback: PostCallback?,
        volumeStateCallback: VolumeStateCallback?,
        zoomyProvider: Zoomy.ZoomyProvider?,
        canZoom: Boolean,
        parentView: View,
        contentManager: ISensitiveContentManager?,
        onMediaClicked: (MediaAssetEntity, PostUIEntity?) -> Unit,
        onScrollingPagerListener: (Boolean)-> Unit
    ) {
        if (!isAvailableToUpdateView()) return

        mediaAdapter = PostMultimediaPagerAdapter(onItemClicked = onMediaClicked)

        pagerListener = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                val scrollIdle = state == SCROLL_STATE_IDLE
                if (scrollIdle) initPagerIndicators()
                onScrollingPagerListener(!scrollIdle)
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
                countIndicatorPosition(position,positionOffset)
                if (positionOffset != 0f) {
                    hideAllDurations()
                } else {
                    showAllDurations()
                }
            }
        }

        this.postId = post.postId
        val items = post.assets ?: emptyList()
        this.mediaPreviewMaxHeight = mediaPreviewMaxHeight
        this.postCallback = postCallback
        this.volumeStateCallback = volumeStateCallback
        this.onMediaClicked = onMediaClicked

        initPager(parentView)
        initBottomGradient(items.size)

        if (items.isEmpty()) {
            binding.clPostMultimediaPagerRootContainer.gone()
            binding.vpPostMultimediaPager.newHeight(0)
        } else {
            val pagerMaxHeight = mediaPreviewMaxHeight
            val mediaMaxWidth = getScreenWidth()
            binding.vpPostMultimediaPager.newHeight(pagerMaxHeight)
            mediaAdapter?.setStrictMeasures(
                pagerMaxHeight,
                mediaMaxWidth
            )
            mediaAdapter?.bind(
                post = post,
                zoomyProvider = zoomyProvider,
                postCallback = postCallback,
                volumeStateCallback = volumeStateCallback,
                canZoom = canZoom,
                contentManager = contentManager
            )
            mediaAdapter?.submitList(items)
            binding.spiPostMultimediaIndicator.setDotCount(items.size)
            initCurrentPage(post)
            initPagerIndicators()
            binding.clPostMultimediaPagerRootContainer.visible()
        }
        this.currentMediaCount = items.size
    }

    private fun countIndicatorPosition(position: Int, positionOffset: Float) {
        val newPage = if (positionOffset >= PAGE_VISIBILITY_THRESHOLD) {
            position + 1
        } else {
            position
        }

        if (currentPagerPosition != newPage) {
            currentPagerPosition = newPage
            initIndicatorCount(currentPagerPosition)
            initPointsIndicator(currentPagerPosition)
        }
    }

    private fun initBottomGradient(size: Int) {
        binding.vBottomGradient.isVisible = size > 1
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
                (holder as? PostMultimediaPagerAdapter.PostMultimediaPagerViewHolder?)?.stopPlayingVideo()
            }.onFailure { Timber.e(it) }
        }
    }

    private fun showAllDurations() {
        val pagerList = (binding.vpPostMultimediaPager[0] as? RecyclerView?) ?: return
        val itemCount = mediaAdapter?.itemCount ?: 0
        for (i in 0 until itemCount) {
            runCatching {
                val holder = pagerList.findViewHolderForAdapterPosition(i)
                (holder as? PostMultimediaPagerAdapter.PostMultimediaPagerViewHolder?)?.showVideoDuration()
            }.onFailure { Timber.e(it) }
        }
    }

    private fun hideAllDurations() {
        val pagerList = (binding.vpPostMultimediaPager[0] as? RecyclerView?) ?: return
        val itemCount = mediaAdapter?.itemCount ?: 0
        for (i in 0 until itemCount) {
            runCatching {
                val holder = pagerList.findViewHolderForAdapterPosition(i)
                (holder as? PostMultimediaPagerAdapter.PostMultimediaPagerViewHolder?)?.hideVideoDuration()
            }.onFailure { Timber.e(it) }
        }
    }

    private fun isAvailableToUpdateView(): Boolean {
        if (binding.vpPostMultimediaPager.childCount < 1) return true
        val multimediaRecyclerView = (binding.vpPostMultimediaPager[0] as? RecyclerView?) ?: return true
        return !multimediaRecyclerView.isComputingLayout && multimediaRecyclerView.scrollState == SCROLL_STATE_IDLE
    }

    private fun initPager(parentView: View) {
        binding.vpPostMultimediaPager.apply {
            adapter = mediaAdapter
            pagerListener?.let { unregisterOnPageChangeCallback(it) }
            (binding.vpPostMultimediaPager[0] as? RecyclerView?)?.apply {
                isNestedScrollingEnabled = false
                addOnItemTouchListener(object : OnItemTouchListener {
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
                })
            }
        }
    }

    private fun initCurrentPage(post: PostUIEntity) {
        binding.apply {
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
            spiPostMultimediaIndicator.setCurrentPosition(neededMediaPosition)
        }
    }

    private fun initPagerIndicators() {
            val position = binding.vpPostMultimediaPager.currentItem
            initIndicatorCount(position)
    }

    private fun getCurrentHolder(): PostMultimediaPagerAdapter.PostMultimediaPagerViewHolder? {
        val pagerList = (binding.vpPostMultimediaPager[0] as? RecyclerView?)
        val currentHolder = pagerList?.findViewHolderForAdapterPosition(binding.vpPostMultimediaPager.currentItem)
        return currentHolder as? PostMultimediaPagerAdapter.PostMultimediaPagerViewHolder?
    }

    private fun initIndicatorCount(position: Int) {
        val itemsCount = mediaAdapter?.itemCount ?: 0
        val currentPosStr = binding.tvPostMultimediaCountIndicator.text
        val countPosStr = "${position + 1}/${itemsCount}"
        if (currentIndicatorPosition == position && currentPosStr == countPosStr) return
        binding.flPostMultimediaCountIndicator.isVisible = itemsCount != 1
        binding.tvPostMultimediaCountIndicator.text = countPosStr
        currentIndicatorPosition = position
    }

    private fun initPointsIndicator(position: Int) {
        if (position == spiPosition) return
        binding.spiPostMultimediaIndicator.setCurrentPosition(position)
        spiPosition = position
    }

    fun enableSwipe() {
        binding.vpPostMultimediaPager.isUserInputEnabled = true
    }

    fun disableSwipe() {
        binding.vpPostMultimediaPager.isUserInputEnabled = false
    }
}
