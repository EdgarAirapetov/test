package com.numplates.nomera3.modules.newroads.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.gone
import com.meera.core.extensions.safeNavigate
import com.meera.core.extensions.visible
import com.meera.core.utils.showCommonError
import com.numplates.nomera3.FEED_START_VIDEO_DELAY
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentMainRoadItemBinding
import com.numplates.nomera3.modules.auth.ui.IAuthStateObserver
import com.numplates.nomera3.modules.auth.util.AuthStatusObserver
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudePropertyMomentScreenOpenWhere
import com.numplates.nomera3.modules.feed.data.entity.PostMediaViewInfo
import com.numplates.nomera3.modules.feed.ui.FeedViewEvent
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.entity.UIPostUpdate
import com.numplates.nomera3.modules.feed.ui.fragment.NetworkRoadType
import com.numplates.nomera3.modules.feed.ui.viewmodel.FeedViewActions
import com.numplates.nomera3.modules.feed.ui.viewmodel.RoadTypesEnum
import com.numplates.nomera3.modules.post_view_statistic.data.PostViewRoadSource
import com.numplates.nomera3.modules.redesign.util.NavigationManager
import com.numplates.nomera3.modules.upload.util.UPLOAD_BUNDLE_KEY
import com.numplates.nomera3.modules.volume.domain.model.VolumeState
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.ui.MeeraPullToRefreshLayout
import com.numplates.nomera3.presentation.view.utils.apphints.createTooltip

private const val DELAY_TO_ENABLE_REFRESH = 400L
private const val DELAY_AFTER_CLICKED_FEED_BUTTON = 1000L
private const val LOAD_POST_DELAY = 200L

class MeeraMainRoadItemFragment :
    MeeraBaseRoadsFragment<MeeraFragmentMainRoadItemBinding>(R.layout.meera_fragment_main_road_item),
    MeeraPullToRefreshLayout.OnRefreshListener, IAuthStateObserver {

    private var isZeroItemLoading = false

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> MeeraFragmentMainRoadItemBinding
        get() = MeeraFragmentMainRoadItemBinding::inflate

    override fun getRoadType() = RoadTypeEnum.MAIN_ROAD

    override fun getNetworkRoadType() = NetworkRoadType.ALL

    override fun getPostViewRoadSource(): PostViewRoadSource =
        PostViewRoadSource.Main

    override fun getAmplitudeWhereFromOpened() = AmplitudePropertyWhere.MAIN_FEED

    override fun getAmplitudeWhereMomentOpened(fromUser: Boolean): AmplitudePropertyMomentScreenOpenWhere {
        return if (fromUser) {
            AmplitudePropertyMomentScreenOpenWhere.MAIN_FEED_AVATAR
        } else {
            AmplitudePropertyMomentScreenOpenWhere.MAIN_FEED_BLOCK
        }
    }

    private val addNewPostTooltip: PopupWindow? by lazy {
        createTooltip(context, R.layout.tooltip_add_new_post)
    }

    override fun getRootView(): View? = view

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.rvMainRoad?.let { initRecycler(it) }
        initViews()
        initPostsLoadScrollListener()
        initPostCounter()
        binding?.btnScrollRefreshMainRoad?.let { initRefreshViewClickListener(it) }
        observeInternetConnection()
        initPostsLiveObservable()
        initAuthObserver()
        viewModel.onTriggerAction(FeedViewActions.CheckIfInitialOpen)
        enableSwipeRefresh(true)
    }

    fun enableNestedScroll(enable: Boolean) {
        binding?.rvMainRoad?.isNestedScrollingEnabled = enable
    }

    fun enableSwipeRefresh(enable: Boolean) {
        binding?.ptrlMainRoad?.setRefreshEnable(enable)
    }

    override fun initAuthObserver() = object : AuthStatusObserver(act, this) {

        override fun onAuthState() = Unit

        override fun onNotAuthState() {
            resetCacheToUpdateRecSystemSettings()

            triggerPostsAction(
                FeedViewActions.UpdateMainRoad(
                    startPostId = 0
                )
            )
        }

        override fun onJustAuthEvent() {
            resetCacheToUpdateRecSystemSettings()

            triggerPostsAction(FeedViewActions.UpdateMainRoad(startPostId = 0))
            viewModel.getMomentDelegate().initialLoadMoments()
        }
    }

    override fun isMomentsScroll(value: Boolean) {
        if (currentFragment?.getParentBehaviorState() == BottomSheetBehavior.STATE_EXPANDED) return
        enableSwipeRefresh(value.not())
    }

    override fun startVideoIfExist() {
        if (currentFragment?.requestCurrentFragment() != RoadTypeEnum.MAIN_ROAD.index
            || NavigationManager.getManager().isMapMode
        ) return

        var lastPostMediaViewInfo: PostMediaViewInfo? = null
        runCatching { lastPostMediaViewInfo = viewModel.getLastPostMediaViewInfo() }

        lastPostMediaViewInfo?.let { viewInfo ->
            val postId = viewInfo.postId ?: return@let
            val mediaPosition = viewInfo.viewedMediaPosition ?: return@let
            val postUpdate = UIPostUpdate.UpdateSelectedMediaPosition(postId, mediaPosition)
            feedAdapter.updateItem(postUpdate)
        }

        binding?.rvMainRoad?.apply {
            postDelayed({ onStart(lastPostMediaViewInfo = lastPostMediaViewInfo) }, FEED_START_VIDEO_DELAY)
        }
    }

    override fun forceStartVideo() {
        binding?.rvMainRoad?.forcePlay()
    }

    override fun forcePlayVideoFromStart() {
        binding?.rvMainRoad?.forcePlayFromStart()
    }

    override fun stopVideoIfExist(isFromMultimedia: Boolean) {
        binding?.rvMainRoad?.onStop(isFromMultimedia)
    }

    override fun controlAlreadyPlayingVideo() {
        binding?.rvMainRoad?.onStopIfNeeded()
    }

    override fun getParentContainer(): ViewGroup? {
        return binding?.flContainer
    }

    private fun resetCacheToUpdateRecSystemSettings() = triggerPostsAction(
        FeedViewActions.ResetAppInfoCache
    )

    private fun observeInternetConnection() {
        viewModel.getNetworkStatusProvider().getNetworkStatusLiveData().observe(viewLifecycleOwner) { networkStatus ->
            if (networkStatus.isConnected) {
                triggerPostsAction(FeedViewActions.RetryLastPostsRequest)
            }
        }
    }

    private fun initViews() {
        binding?.ptrlMainRoad?.apply {
            setShouldShowLoader(false)
            setOnRefreshListener(this@MeeraMainRoadItemFragment)
            setOnPullOffsetListener(object : MeeraPullToRefreshLayout.OnPullOffsetListener {
                override fun onOffsetChanged(offset: Int) {
                    updateTopMarginEmptyPostsView(offset)
                }
            })
        }
    }

    private fun initPostsLiveObservable() {
        viewModel.livePosts.observe(viewLifecycleOwner) {
            if (isZeroItemLoading) {
                isZeroItemLoading = false
                binding?.ptrlMainRoad?.setRefreshEnable(false)
                doDelayed(DELAY_TO_ENABLE_REFRESH) { // if not to do this feeds will be jumping after several refresh
                    enableSwipeRefresh(true)
                }
            }
        }
        viewModel.liveEvent.observe(viewLifecycleOwner) {
            handlePostViewEvents(it)
        }
    }

    override fun onRefresh(showAdditionalLoader: Boolean) {
        if (showAdditionalLoader) {
            binding?.lvMainRoad?.visible()
            binding?.lvMainRoad?.show()
        }
        onRefreshActions()
    }

    override fun onRefresh() {
        binding?.lvMainRoad?.visible()
        binding?.lvMainRoad?.show()
        onRefreshActions()
    }

    private fun onRefreshActions() {
        loadPosts()
        scrollToTop()
    }

    private fun handlePostViewEvents(event: FeedViewEvent?) {
        when (event) {
            is FeedViewEvent.PlaceHolderEvent -> {
                if (event.hasPosts) hideEmptyPostsPlaceholder()
                else showEmptyPostsPlaceholder(RoadTypeEnum.MAIN_ROAD)
            }

            is FeedViewEvent.OnFirstPageLoaded -> viewModel.getMomentDelegate().initialLoadMoments()
            is FeedViewEvent.OnMomentsFirstLoaded -> if (event.roadTypesEnum == RoadTypesEnum.MAIN) scrollToTop()
            is FeedViewEvent.LoadInitialPosts -> loadPosts()
            is FeedViewEvent.ShowCommonError -> {
                showCommonError(getString(event.messageResId), requireView())
            }
            else -> Unit
        }
    }

    private var canUpdate = true

    override fun onFeedBtnClicked() {
        if (binding?.rvMainRoad?.computeVerticalScrollOffset() == 0 && canUpdate) {
            binding?.ptrlMainRoad?.setRefreshing(true)
            binding?.lvMainRoad?.visible()
            binding?.lvMainRoad?.show()
            onRefresh(false)
            canUpdate = false
            doDelayed(DELAY_AFTER_CLICKED_FEED_BUTTON) { canUpdate = true }
        } else {
            scrollToTop()
            // Refresh road after scroll
            doDelayed(LOAD_POST_DELAY) {
                initPostsLoadScrollListener(true)
            }
        }
    }

    override fun hideRefreshLayoutProgress() {
        binding?.ptrlMainRoad?.setRefreshing(false)
        binding?.lvMainRoad?.hide(onFinished = {
            binding?.lvMainRoad?.gone()
        })
    }

    override fun getRefreshTopButtonView() = binding?.btnScrollRefreshMainRoad

    override fun refreshRoadAfterScrollRefresh() {
        super.refreshRoadAfterScrollRefresh()
        initPostsLoadScrollListener(true)
        onUpdateMomentsClicked()
        scrollToTop()
    }

    override fun onResume() {
        super.onResume()
        startVideoIfExist()
        resetLastPostMediaViewInfo()
    }

    override fun onStop() {
        super.onStop()
        stopVideoIfExist()
    }

    override fun onDestroyView() {
        binding?.rvMainRoad?.release()
        binding?.ptrlMainRoad?.release()
        super.onDestroyView()
    }

    override fun setVolumeState(volumeState: VolumeState) {
        viewModel.onTriggerAction(FeedViewActions.UpdateVolumeState(volumeState))
    }

    override fun getVolumeState() = viewModel.getVolumeState()

    override fun onPause() {
        super.onPause()
        addNewPostTooltip?.dismiss()
        stopVideoIfExist()
    }

    override fun navigateEditPostFragment(post: PostUIEntity?, postStringEntity: String?) {
        findNavController().safeNavigate(
            resId = R.id.action_mainRoadFragment_to_meeraCreatePostFragment,
            bundle = bundleOf(
                IArgContainer.ARG_GROUP_ID to (post?.groupId?.toInt()),
                IArgContainer.ARG_SHOW_MEDIA_GALLERY to false,
                IArgContainer.ARG_POST to post,
                UPLOAD_BUNDLE_KEY to postStringEntity
            )
        )
    }
}
