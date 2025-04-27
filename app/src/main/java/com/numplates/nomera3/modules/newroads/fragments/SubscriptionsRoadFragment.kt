package com.numplates.nomera3.modules.newroads.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.airbnb.lottie.LottieAnimationView
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.hideScaleDown
import com.meera.core.extensions.showScaleUp
import com.numplates.nomera3.FEED_START_VIDEO_DELAY
import com.numplates.nomera3.databinding.FragmentSubscriptionsRoadBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudePropertyMomentScreenOpenWhere
import com.numplates.nomera3.modules.feed.data.entity.PostMediaViewInfo
import com.numplates.nomera3.modules.feed.ui.FeedViewEvent
import com.numplates.nomera3.modules.feed.ui.entity.UIPostUpdate
import com.numplates.nomera3.modules.feed.ui.fragment.NetworkRoadType
import com.numplates.nomera3.modules.feed.ui.fragment.SubscriptionRoadViewEvent
import com.numplates.nomera3.modules.feed.ui.viewmodel.FeedViewActions
import com.numplates.nomera3.modules.feed.ui.viewmodel.RoadTypesEnum
import com.numplates.nomera3.modules.moments.show.presentation.fragment.KEY_MOMENT_GROUP_ID
import com.numplates.nomera3.modules.moments.show.presentation.fragment.KEY_SUBSCRIPTION_ROAD_WATCHED_MOMENT_GROUP
import com.numplates.nomera3.modules.feed.ui.viewmodel.FeedViewModel
import com.numplates.nomera3.modules.newroads.MainPostRoadsFragment
import com.numplates.nomera3.modules.newroads.SubscriptionRoadViewModel
import com.numplates.nomera3.modules.post_view_statistic.data.PostViewRoadSource
import com.numplates.nomera3.modules.volume.domain.model.VolumeState
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.extensions.isVisible
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SubscriptionsRoadFragment : BaseRoadsFragment<FragmentSubscriptionsRoadBinding>(),
    SwipeRefreshLayout.OnRefreshListener {

    private val subscriptionViewModel by viewModels<SubscriptionRoadViewModel>()
    private val disposables = CompositeDisposable()

    private var isZeroItemLoading = false

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSubscriptionsRoadBinding
        get() = FragmentSubscriptionsRoadBinding::inflate

    override fun getRoadType() = RoadTypeEnum.SUBSCRIPTIONS_ROAD

    override fun getAmplitudeWhereFromOpened() = AmplitudePropertyWhere.FOLLOW_FEED

    override fun getAmplitudeWhereMomentOpened(fromUser: Boolean): AmplitudePropertyMomentScreenOpenWhere {
        return if(fromUser) {
            AmplitudePropertyMomentScreenOpenWhere.FOLLOW_FEED_AVATAR
        } else {
            AmplitudePropertyMomentScreenOpenWhere.FOLLOW_FEED_BLOCK
        }
    }

    override fun getNetworkRoadType() = NetworkRoadType.SUBSCRIPTIONS

    override fun getRootView(): View? = view

    override fun hideRefreshLayoutProgress() {
        binding?.swipeSubscriptionsRoad?.isRefreshing = false
    }

    override fun getPostViewRoadSource(): PostViewRoadSource {
        return PostViewRoadSource.Subscription
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.rvSubscriptionsRoad?.let { initRecycler(it) }

        initViews()
        initPostCounter()
        getSubscriptionsRoad()
        loadPostsAfterShimmerWasShown()
        binding?.btnScrollRefreshSubscriptionsRoad?.let { initAnimViewClickListener(it) }

        binding?.subscriptionRefreshButton?.setOnClickListener {
            subscriptionViewModel.refreshRoad(true)
        }

        observeInternetConnection()
        observeMomentViewFragmentResult()
    }

    private fun loadPostsAfterShimmerWasShown() = lifecycleScope.launch {
        delay(FeedViewModel.DELAY_SHIMMER_REQUEST)
        loadPosts()
        subscriptionViewModel.resetPostsWereRequestedWithinSession()
    }

    private fun observeInternetConnection() {
        viewModel.getNetworkStatusProvider().getNetworkStatusLiveData().observe(viewLifecycleOwner) { networkStatus ->
            if (networkStatus.isConnected) {
                triggerPostsAction(FeedViewActions.RetryLastPostsRequest)
            }
        }
    }

    private fun initViews() {
        binding?.swipeSubscriptionsRoad?.setOnRefreshListener {
            subscriptionViewModel.refreshRoad(true)
        }
    }

    private fun getSubscriptionsRoad() {
        // Observe view events
        subscribePostViewEvent()

        // Observe network state
        subscribeNetworkState()

        // Observe new posts indicator
        subscribeNewPosts()
    }

    private fun subscribePostViewEvent() {
        viewModel.liveEvent.observe(viewLifecycleOwner, ::handlePostViewEvents)
    }

    private fun subscribeNetworkState() = Unit

    private fun handlePostViewEvents(event: FeedViewEvent?) {
        when (event) {
            is FeedViewEvent.PlaceHolderEvent -> {
                if (event.hasPosts) hideEmptyPostsPlaceholder()
                else showEmptyPostsPlaceholder(RoadTypeEnum.SUBSCRIPTIONS_ROAD)
            }
            is FeedViewEvent.OnFirstPageLoaded -> {
                viewModel.getMomentDelegate().initialLoadMoments()
                binding?.swipeSubscriptionsRoad?.isRefreshing = false
                isZeroItemLoading = true
                scrollToTop(false)
            }
            is FeedViewEvent.OnMomentsFirstLoaded -> {
                if (event.roadTypesEnum == RoadTypesEnum.SUBSCRIPTION) scrollToTop(false)
            }
            else -> {}
        }
    }

    private fun subscribeNewPosts() {
        subscriptionViewModel.hasNewSubscriptionPost.observe(viewLifecycleOwner) { subscriptionNewPostEntity ->
            if (subscriptionNewPostEntity.hasNew) {
                binding?.subscriptionRefreshButton?.showScaleUp()
                hideRefreshTopButton()
            } else {
                binding?.subscriptionRefreshButton?.hideScaleDown()
            }
        }
    }

    private fun subscribeRxListeners() {
        subscriptionViewModel
            .getEventStream()
            .subscribeOn(AndroidSchedulers.mainThread())
            .subscribe { event ->
                handleSubscriptionEvent(event)
            }
            .addTo(disposables)
    }

    private fun unsubscribeRxListeners() {
        disposables.clear()
    }

    override fun onRefresh() {
        loadPosts(true)
    }

    fun refreshIfFirstRun() {
        if (subscriptionViewModel.getPostsWereRequestedWithinSession().not()) {
            subscriptionViewModel.refreshRoad(false)
            subscriptionViewModel.setSubscriptionPostsWereRequestedWithinSession()
        }
    }

    private fun handleSubscriptionEvent(event: SubscriptionRoadViewEvent) {
        when (event) {
            is SubscriptionRoadViewEvent.NeedRefresh -> {
                binding?.subscriptionRefreshButton?.hideScaleDown()
                loadPosts(true)
                scrollToTop()
            }
        }
    }

    private var canUpdate = true

    override fun onFeedBtnClicked() {
        val recycler = binding?.rvSubscriptionsRoad
        if (recycler?.computeVerticalScrollOffset() == 0 && canUpdate) {
            binding?.swipeSubscriptionsRoad?.isRefreshing = true
            canUpdate = false
            doDelayed(1000) {
                canUpdate = true
            }
        } else {
            scrollToTop()
        }

        // Refresh road after scroll
        doDelayed(200) {
            subscriptionViewModel.refreshRoad(true)
        }
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
        binding?.rvSubscriptionsRoad?.apply {
            postDelayed({ onStart(lastPostMediaViewInfo = lastPostMediaViewInfo) }, FEED_START_VIDEO_DELAY)
        }
    }

    override fun forceStartVideo() {
        binding?.rvSubscriptionsRoad?.forcePlay()
    }

    override fun stopVideoIfExist() {
        binding?.rvSubscriptionsRoad?.onStop()
    }

    override fun controlAlreadyPlayingVideo() {
        binding?.rvSubscriptionsRoad?.onStopIfNeeded()
    }

    override fun getRefreshTopButtonView(): LottieAnimationView? {
        return binding?.btnScrollRefreshSubscriptionsRoad
    }

    override fun canShowRefreshTopButtonView(): Boolean {
        return binding?.subscriptionRefreshButton.isVisible.not()
    }

    override fun refreshRoadAfterScrollRefresh() {
        super.refreshRoadAfterScrollRefresh()

        subscriptionViewModel.refreshRoad(true)
        onUpdateMomentsClicked()
    }

    override fun onStartFragment() {
        super.onStartFragment()
        if (currentFragment?.requestCurrentFragment() == RoadTypeEnum.SUBSCRIPTIONS_ROAD.index) {
            if (act.getCurrentFragment() is MainPostRoadsFragment) {
                startVideoIfExist()
            }
        }
        resetLastPostMediaViewInfo()
    }

    override fun onPostsSubmitted() {
        subscriptionViewModel.markNewPostsAsRead()
    }

    override fun onPause() {
        super.onPause()

        unsubscribeRxListeners()
    }

    override fun onResume() {
        super.onResume()

        subscribeRxListeners()
    }

    override fun onStop() {
        super.onStop()
        stopVideoIfExist()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding?.rvSubscriptionsRoad?.clear()
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

    private fun observeMomentViewFragmentResult() {
        act.navigatorAdapter?.fragmentManager?.setFragmentResultListener(
            KEY_SUBSCRIPTION_ROAD_WATCHED_MOMENT_GROUP,
            viewLifecycleOwner
        ) { _, bundle ->
            val viewedGroupId = bundle.getLong(KEY_MOMENT_GROUP_ID, -1L)
            if (currentFragment?.requestCurrentFragment() != RoadTypeEnum.SUBSCRIPTIONS_ROAD.index) {
                return@setFragmentResultListener
            }
            viewModel.getMomentDelegate().updateMomentsAndViewedPosition(viewedGroupId)
        }
    }
}
