package com.numplates.nomera3.modules.newroads.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.airbnb.lottie.LottieAnimationView
import com.meera.core.extensions.doDelayed
import com.numplates.nomera3.FEED_START_VIDEO_DELAY
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentCustomRoadBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudeEventName
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudePropertyMomentScreenOpenWhere
import com.numplates.nomera3.modules.feed.data.entity.PostMediaViewInfo
import com.numplates.nomera3.modules.feed.ui.FeedViewEvent
import com.numplates.nomera3.modules.feed.ui.entity.UIPostUpdate
import com.numplates.nomera3.modules.feed.ui.fragment.NetworkRoadType
import com.numplates.nomera3.modules.feed.ui.viewmodel.FeedViewActions
import com.numplates.nomera3.modules.feed.ui.viewmodel.FeedViewModel
import com.numplates.nomera3.modules.newroads.MainPostRoadsFragment
import com.numplates.nomera3.modules.post_view_statistic.data.PostViewRoadSource
import com.numplates.nomera3.modules.volume.domain.model.VolumeState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class CustomRoadFragment : BaseRoadsFragment<FragmentCustomRoadBinding>(),
    SwipeRefreshLayout.OnRefreshListener {

    private var isZeroItemLoading = false

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentCustomRoadBinding
        get() = FragmentCustomRoadBinding::inflate

    override fun getRoadType() = RoadTypeEnum.CUSTOM_ROAD

    override fun getAmplitudeWhereFromOpened() = AmplitudePropertyWhere.SELF_FEED

    override fun getAmplitudeWhereMomentOpened(isUserAvatar: Boolean) = AmplitudePropertyMomentScreenOpenWhere.SELF_FEED

    override fun getNetworkRoadType(): NetworkRoadType {
        return NetworkRoadType.USER(viewModel.getUserUid(), 1, true)
    }

    override fun getPostViewRoadSource(): PostViewRoadSource {
        return PostViewRoadSource.Disable
    }

    override fun hideRefreshLayoutProgress() {
        binding?.refreshCustomRoad?.isRefreshing = false
    }

    override fun getRootView(): View? = view

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.rvCustomRoad?.let { initRecycler(it) }
        initViews()
        initPostCounter()
        loadPostsAfterShimmerWasShown()
        binding?.btnScrollRefreshMyRoad?.let { initAnimViewClickListener(it) }
        initPostsLiveObservable()
    }

    override fun onDestroyView() {
        binding?.rvCustomRoad?.clear()
        binding?.rvCustomRoad?.onDestroyView()
        super.onDestroyView()
    }

    private fun loadPostsAfterShimmerWasShown() = lifecycleScope.launch {
        delay(FeedViewModel.DELAY_SHIMMER_REQUEST)
        loadPosts()
    }

    private fun observeInternetConnection() {
        viewModel.getNetworkStatusProvider().getNetworkStatusLiveData().observe(viewLifecycleOwner) { networkStatus ->
            if (networkStatus.isConnected) {
                triggerPostsAction(FeedViewActions.RetryLastPostsRequest)
            }
        }
    }

    private fun initViews() {
        binding?.refreshCustomRoad?.setOnRefreshListener(this)
    }

    private fun initPostsLiveObservable() {
        viewModel.liveEvent.observe(viewLifecycleOwner, ::handlePostViewEvents)
        observeInternetConnection()
    }

    override fun onRefresh() {
        loadPosts(true)
    }

    private fun handlePostViewEvents(event: FeedViewEvent) {
        when (event) {
            is FeedViewEvent.PlaceHolderEvent -> {
                if (event.hasPosts) hideEmptyPostsPlaceholder() else showEmptyPostsPlaceholder(RoadTypeEnum.CUSTOM_ROAD)
            }

            is FeedViewEvent.OnFirstPageLoaded -> {
                binding?.refreshCustomRoad?.isRefreshing = false
                isZeroItemLoading = true
            }

            else -> {}
        }
    }

    private var canUpdate = true
    override fun onFeedBtnClicked() {
        if (binding?.rvCustomRoad?.computeVerticalScrollOffset() == 0 && canUpdate) {
            binding?.refreshCustomRoad?.isRefreshing = true
            onRefresh()
            canUpdate = false
            doDelayed(1000) {
                canUpdate = true
            }
        } else {
            scrollToTop()
        }

        // Refresh road after scroll
        doDelayed(200) {
            loadPosts(true)
        }


    }

    override fun getRefreshTopButtonView(): LottieAnimationView? {
        return binding?.btnScrollRefreshMyRoad
    }

    override fun onStartFragment() {
        super.onStartFragment()
        if (currentFragment?.requestCurrentFragment() == RoadTypeEnum.CUSTOM_ROAD.index) {
            if (act?.getCurrentFragment() is MainPostRoadsFragment) {
                startVideoIfExist()
            }
        }
        resetLastPostMediaViewInfo()
    }

    override fun onStop() {
        super.onStop()
        stopVideoIfExist()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding?.rvCustomRoad?.clear()
    }

    override fun onReturnTransitionFragment() {
        startVideoIfExist()
    }

    override fun setVolumeState(volumeState: VolumeState) {
        viewModel.onTriggerAction(FeedViewActions.UpdateVolumeState(volumeState))
    }

    override fun getVolumeState() = viewModel.getVolumeState()

    override fun onStopFragment() {
        super.onStopFragment()
        stopVideoIfExist()
    }

    override fun startVideoIfExist() {
        var lastPostMediaViewInfo: PostMediaViewInfo? = null
        runCatching { lastPostMediaViewInfo = viewModel.getLastPostMediaViewInfo() }

        lastPostMediaViewInfo?.let { viewInfo ->
            val postId = viewInfo.postId ?: return@let
            val mediaPosition = viewInfo.viewedMediaPosition ?: return@let
            val postUpdate = UIPostUpdate.UpdateSelectedMediaPosition(postId, mediaPosition)
            feedAdapter?.updateItem(postUpdate)
        }

        binding?.rvCustomRoad?.apply {
            postDelayed({ onStart(lastPostMediaViewInfo = lastPostMediaViewInfo) }, FEED_START_VIDEO_DELAY)
        }
    }

    override fun forceStartVideo() {
        binding?.rvCustomRoad?.forcePlay()
    }

    override fun stopVideoIfExist() {
        binding?.rvCustomRoad?.onStop()
    }

    override fun controlAlreadyPlayingVideo() {
        binding?.rvCustomRoad?.onStopIfNeeded()
    }

    override fun refreshRoadAfterScrollRefresh() {
        super.refreshRoadAfterScrollRefresh()
        loadPosts(true)
        scrollToTop()
    }

    /**
     * Конфиг для настройки кастомной дороги
     */
    object ConfigConst {
        val AMPLITUDE_SCREEN_OPEN = AmplitudeEventName.OPEN_SELF_FEED
        const val IS_FILTER_ENABLE = false
        const val TITLE_RES_ID = R.string.road_type_my_posts
    }
}
