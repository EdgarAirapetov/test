package com.numplates.nomera3.modules.newroads.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.gone
import com.meera.core.extensions.hideScaleDown
import com.meera.core.extensions.isFalse
import com.meera.core.extensions.safeNavigate
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.showScaleUp
import com.meera.core.extensions.visible
import com.meera.core.utils.showCommonError
import com.meera.uikit.widgets.dp
import com.numplates.nomera3.FEED_START_VIDEO_DELAY
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentSubscriptionsRoadItemBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudePropertyMomentScreenOpenWhere
import com.numplates.nomera3.modules.feed.data.entity.PostMediaViewInfo
import com.numplates.nomera3.modules.feed.ui.FeedViewEvent
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.entity.UIPostUpdate
import com.numplates.nomera3.modules.feed.ui.fragment.NetworkRoadType
import com.numplates.nomera3.modules.feed.ui.fragment.SubscriptionRoadViewEvent
import com.numplates.nomera3.modules.feed.ui.viewmodel.FeedViewActions
import com.numplates.nomera3.modules.feed.ui.viewmodel.FeedViewModel
import com.numplates.nomera3.modules.feed.ui.viewmodel.RoadTypesEnum
import com.numplates.nomera3.modules.newroads.SubscriptionRoadViewModel
import com.numplates.nomera3.modules.post_view_statistic.data.PostViewRoadSource
import com.numplates.nomera3.modules.redesign.util.NavigationManager
import com.numplates.nomera3.modules.upload.util.UPLOAD_BUNDLE_KEY
import com.numplates.nomera3.modules.volume.domain.model.VolumeState
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.ui.MeeraPullToRefreshLayout
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.extensions.isVisible
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val UPDATE_ACTION_BUTTON_DELAY = 300L
private const val NEW_POST_BTN_SMALL_MARGIN = 56
private const val NEW_POST_BTN_BIG_MARGIN = 80

class MeeraSubscriptionsRoadItemFragment :
    MeeraBaseRoadsFragment<MeeraFragmentSubscriptionsRoadItemBinding>(R.layout.meera_fragment_subscriptions_road_item),
    SwipeRefreshLayout.OnRefreshListener {

    private var subscriptionViewModel: SubscriptionRoadViewModel? = null
    private val disposables = CompositeDisposable()

    private var isZeroItemLoading = false

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> MeeraFragmentSubscriptionsRoadItemBinding
        get() = MeeraFragmentSubscriptionsRoadItemBinding::inflate

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
        binding?.ptrlSubscriptionRoad?.setRefreshing(false)
        binding?.lvSubscriptionsRoad?.hide(onFinished = {
            binding?.lvSubscriptionsRoad?.gone()
        })
    }

    override fun getPostViewRoadSource(): PostViewRoadSource {
        return PostViewRoadSource.Subscription
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscriptionViewModel = ViewModelProvider(this).get(SubscriptionRoadViewModel::class.java)
        binding?.rvSubscriptionsRoad?.let { initRecycler(it) }

        initViews()
        initPostsLoadScrollListener()
        initPostCounter()
        getSubscriptionsRoad()
        viewModel.onTriggerAction(FeedViewActions.CheckIfInitialOpen)
        binding?.btnScrollRefreshSubscriptionsRoad?.let { initRefreshViewClickListener(it) }

        binding?.subscriptionRefreshButton?.setOnClickListener {
            subscriptionViewModel?.refreshRoad(true)
        }

        observeInternetConnection()
        observeMomentViewFragmentResult()
        enableSwipeRefresh(true)
    }

    override fun navigateEditPostFragment(post: PostUIEntity?, postStringEntitiy: String?) {
        findNavController().safeNavigate(
            resId = R.id.action_mainRoadFragment_to_meeraCreatePostFragment,
            bundle = bundleOf(
                IArgContainer.ARG_GROUP_ID to (post?.groupId?.toInt()),
                IArgContainer.ARG_SHOW_MEDIA_GALLERY to false,
                IArgContainer.ARG_POST to post,
                UPLOAD_BUNDLE_KEY to postStringEntitiy
            )
        )
    }

    override fun getParentContainer(): ViewGroup? {
        return binding?.flContainer
    }

    override fun isMomentsScroll(value: Boolean) {
        if (currentFragment?.getParentBehaviorState() == BottomSheetBehavior.STATE_EXPANDED) return
        enableSwipeRefresh(value.not())
    }

    override fun setNewPostBtnMarginTop(bottomSheetState: Int) {
        val margin = when (bottomSheetState) {
            BottomSheetBehavior.STATE_EXPANDED -> NEW_POST_BTN_SMALL_MARGIN
            else -> NEW_POST_BTN_BIG_MARGIN
        }
        binding?.subscriptionRefreshButton?.setMargins(top = margin.dp)
    }

    private fun loadPostsAfterShimmerWasShown() = lifecycleScope.launch {
        delay(FeedViewModel.DELAY_SHIMMER_REQUEST)
        loadPosts()
        subscriptionViewModel?.resetPostsWereRequestedWithinSession()
    }

    private fun observeInternetConnection() {
        viewModel.getNetworkStatusProvider().getNetworkStatusLiveData().observe(viewLifecycleOwner) { networkStatus ->
            if (networkStatus.isConnected) {
                triggerPostsAction(FeedViewActions.RetryLastPostsRequest)
            }
        }
    }

    private fun initViews() {
        binding?.apply {
            ptrlSubscriptionRoad.apply {
                setShouldShowLoader(false)
                setOnRefreshListener(object : MeeraPullToRefreshLayout.OnRefreshListener {
                    override fun onRefresh() {
                        lvSubscriptionsRoad.visible()
                        lvSubscriptionsRoad.show()
                        subscriptionViewModel?.refreshRoad(true)
                    }
                })
                setOnPullOffsetListener(object : MeeraPullToRefreshLayout.OnPullOffsetListener {
                    override fun onOffsetChanged(offset: Int) {
                        updateTopMarginEmptyPostsView(offset)
                    }
                })
            }
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
                binding?.ptrlSubscriptionRoad?.setRefreshing(false)
                binding?.lvSubscriptionsRoad?.hide(onFinished = {
                    binding?.lvSubscriptionsRoad?.gone()
                })
                isZeroItemLoading = true
                scrollToTop()
            }
            is FeedViewEvent.OnMomentsFirstLoaded -> {
                if (event.roadTypesEnum == RoadTypesEnum.SUBSCRIPTION) scrollToTop()
            }
            is FeedViewEvent.LoadInitialPosts -> {
                loadPostsAfterShimmerWasShown()
            }
            is FeedViewEvent.ShowCommonError -> {
                showCommonError(getString(event.messageResId), requireView())
            }
            else -> {}
        }
    }

    private fun subscribeNewPosts() {
        subscriptionViewModel?.hasNewSubscriptionPost?.observe(viewLifecycleOwner) { subscriptionNewPostEntity ->
            if (subscriptionNewPostEntity.hasNew) {
                binding?.subscriptionRefreshButton?.apply {
                    showScaleUp()
                    postDelayed({ requestLayout() }, UPDATE_ACTION_BUTTON_DELAY)
                }
                hideRefreshTopButton()
            } else {
                binding?.subscriptionRefreshButton?.hideScaleDown()
            }
        }
    }

    private fun subscribeRxListeners() {
        subscriptionViewModel
            ?.getEventStream()
            ?.subscribeOn(AndroidSchedulers.mainThread())
            ?.subscribe { event ->
                handleSubscriptionEvent(event)
            }
            ?.addTo(disposables)
    }

    private fun unsubscribeRxListeners() {
        disposables.clear()
    }

    override fun onRefresh(showAdditionalLoader: Boolean) {
        if (showAdditionalLoader) {
            binding?.lvSubscriptionsRoad?.visible()
            binding?.lvSubscriptionsRoad?.show()
        }

        onRefreshActions()
    }

    override fun onRefresh() {
        binding?.lvSubscriptionsRoad?.visible()
        binding?.lvSubscriptionsRoad?.show()
        onRefreshActions()
    }

    fun enableNestedScroll(enable: Boolean) {
        binding?.rvSubscriptionsRoad?.isNestedScrollingEnabled = enable
    }

    fun enableSwipeRefresh(enable: Boolean) {
        binding?.ptrlSubscriptionRoad?.setRefreshEnable(enable)
    }

    fun refreshIfFirstRun() {
        if (subscriptionViewModel?.getPostsWereRequestedWithinSession().isFalse()) {
            subscriptionViewModel?.refreshRoad(false)
            subscriptionViewModel?.setSubscriptionPostsWereRequestedWithinSession()
        }
    }

    private fun onRefreshActions() {
        binding?.subscriptionRefreshButton?.hideScaleDown()
        initPostsLoadScrollListener(true)
    }

    private fun handleSubscriptionEvent(event: SubscriptionRoadViewEvent) {
        when (event) {
            is SubscriptionRoadViewEvent.NeedRefresh -> {
                binding?.subscriptionRefreshButton?.hideScaleDown()
                initPostsLoadScrollListener(true)
                scrollToTop()
                NavigationManager.getManager().getForceUpdatedTopBehavior()?.state = BottomSheetBehavior.STATE_HALF_EXPANDED
            }
        }
    }

    private var canUpdate = true

    override fun onFeedBtnClicked() {
        val recycler = binding?.rvSubscriptionsRoad
        if (recycler?.computeVerticalScrollOffset() == 0 && canUpdate) {
            binding?.ptrlSubscriptionRoad?.setRefreshing(true)
            binding?.lvSubscriptionsRoad?.visible()
            binding?.lvSubscriptionsRoad?.show()
            canUpdate = false
            doDelayed(1000) {
                canUpdate = true
            }
        } else {
            scrollToTop()
        }

        // Refresh road after scroll
        doDelayed(200) {
            subscriptionViewModel?.refreshRoad(true)
        }
    }

    override fun startVideoIfExist() {
        if (currentFragment?.requestCurrentFragment() != RoadTypeEnum.SUBSCRIPTIONS_ROAD.index
            || NavigationManager.getManager().isMapMode) return

        var lastPostMediaViewInfo: PostMediaViewInfo? = null
        runCatching { lastPostMediaViewInfo = viewModel.getLastPostMediaViewInfo() }

        lastPostMediaViewInfo?.let { viewInfo ->
            val postId = viewInfo.postId ?: return@let
            val mediaPosition = viewInfo.viewedMediaPosition ?: return@let
            val postUpdate = UIPostUpdate.UpdateSelectedMediaPosition(postId, mediaPosition)
            feedAdapter.updateItem(postUpdate)
        }
        binding?.rvSubscriptionsRoad?.apply {
            postDelayed({ onStart(lastPostMediaViewInfo = lastPostMediaViewInfo) }, FEED_START_VIDEO_DELAY)
        }
    }

    override fun forceStartVideo() {
        binding?.rvSubscriptionsRoad?.forcePlay()
    }

    override fun forcePlayVideoFromStart() {
        binding?.rvSubscriptionsRoad?.forcePlayFromStart()
    }

    override fun stopVideoIfExist(isFromMultimedia: Boolean) {
        binding?.rvSubscriptionsRoad?.onStop(isFromMultimedia)
    }

    override fun controlAlreadyPlayingVideo() {
        binding?.rvSubscriptionsRoad?.onStopIfNeeded()
    }

    override fun getRefreshTopButtonView() = binding?.btnScrollRefreshSubscriptionsRoad

    override fun refreshRoadAfterScrollRefresh() {
        super.refreshRoadAfterScrollRefresh()

        subscriptionViewModel?.refreshRoad(true)
        onUpdateMomentsClicked()
    }

    override fun canShowRefreshTopButtonView(): Boolean {
        return binding?.subscriptionRefreshButton.isVisible.not()
    }

    override fun onPostsSubmitted() {
        subscriptionViewModel?.markNewPostsAsRead()
    }

    override fun onPause() {
        super.onPause()

        stopVideoIfExist()
        unsubscribeRxListeners()
    }

    override fun onResume() {
        super.onResume()
        startVideoIfExist()
        resetLastPostMediaViewInfo()
        subscribeRxListeners()
    }

    override fun onStop() {
        super.onStop()
        stopVideoIfExist()
    }

    override fun onDestroyView() {
        binding?.rvSubscriptionsRoad?.release()
        binding?.ptrlSubscriptionRoad?.release()
        super.onDestroyView()
    }

    override fun setVolumeState(volumeState: VolumeState) {
        viewModel.onTriggerAction(FeedViewActions.UpdateVolumeState(volumeState))
    }

    override fun getVolumeState() = viewModel.getVolumeState()

    private fun observeMomentViewFragmentResult() {
        //TODO ROAD_FIX
//        act.navigatorAdapter?.fragmentManager?.setFragmentResultListener(
//            KEY_SUBSCRIPTION_ROAD_WATCHED_MOMENT_GROUP,
//            viewLifecycleOwner
//        ) { _, bundle ->
//            val viewedGroupId = bundle.getLong(KEY_MOMENT_GROUP_ID, -1L)
//            if (currentFragment?.requestCurrentFragment() != RoadTypeEnum.SUBSCRIPTIONS_ROAD.index) {
//                return@setFragmentResultListener
//            }
//            viewModel.getMomentDelegate().updateMomentsAndViewedPosition(viewedGroupId)
//        }
    }
}
