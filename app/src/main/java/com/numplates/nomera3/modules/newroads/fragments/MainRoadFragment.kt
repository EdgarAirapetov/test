package com.numplates.nomera3.modules.newroads.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.airbnb.lottie.LottieAnimationView
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.dp
import com.numplates.nomera3.Act
import com.numplates.nomera3.FEED_START_VIDEO_DELAY
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentMainRoadBinding
import com.numplates.nomera3.modules.auth.ui.IAuthStateObserver
import com.numplates.nomera3.modules.auth.util.AuthStatusObserver
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudePropertyMomentScreenOpenWhere
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.feed.data.entity.PostMediaViewInfo
import com.numplates.nomera3.modules.feed.ui.FeedViewEvent
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.entity.UIPostUpdate
import com.numplates.nomera3.modules.feed.ui.fragment.NetworkRoadType
import com.numplates.nomera3.modules.feed.ui.viewmodel.FeedViewActions
import com.numplates.nomera3.modules.moments.show.presentation.fragment.KEY_MAIN_ROAD_WATCHED_MOMENT_GROUP
import com.numplates.nomera3.modules.moments.show.presentation.fragment.KEY_MOMENT_GROUP_ID
import com.numplates.nomera3.modules.newroads.MainPostRoadsFragment
import com.numplates.nomera3.modules.post_view_statistic.data.PostViewRoadSource
import com.numplates.nomera3.modules.volume.domain.model.VolumeState
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.fragments.UserInfoFragment
import com.numplates.nomera3.presentation.view.utils.apphints.TooltipDuration
import com.numplates.nomera3.presentation.view.utils.apphints.createTooltip
import com.numplates.nomera3.presentation.view.utils.apphints.showAboveView
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainRoadFragment : BaseRoadsFragment<FragmentMainRoadBinding>(),
    SwipeRefreshLayout.OnRefreshListener,
    IAuthStateObserver {

    private var isZeroItemLoading = false

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMainRoadBinding
        get() = FragmentMainRoadBinding::inflate

    override fun getRoadType() = RoadTypeEnum.MAIN_ROAD

    override fun getNetworkRoadType() = NetworkRoadType.ALL

    override fun getPostViewRoadSource(): PostViewRoadSource {
        return PostViewRoadSource.Main
    }

    override fun getAmplitudeWhereFromOpened() = AmplitudePropertyWhere.MAIN_FEED

    override fun getAmplitudeWhereMomentOpened(fromUser: Boolean): AmplitudePropertyMomentScreenOpenWhere {
        return if(fromUser) {
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
        initPostCounter()
        loadPosts()
        binding?.btnScrollRefreshMainRoad?.let { initAnimViewClickListener(it) }
        observeInternetConnection()
        initPostsLiveObservable()
        initAuthObserver()
        observeMomentViewFragmentResult()
    }

    override fun initAuthObserver(): AuthStatusObserver = object : AuthStatusObserver(act, this) {

        override fun onAuthState() = Unit

        override fun onNotAuthState() {
            resetCacheToUpdateRecSystemSettings()

            triggerPostsAction(
                FeedViewActions.UpdateMainRoad(
                    startPostId = 0
                )
            )
        }

        /**
         * Обновить дорогу сразу по окончанию процесса авторизации/регистрации
         */
        override fun onJustAuthEvent() {
            resetCacheToUpdateRecSystemSettings()

            triggerPostsAction(
                FeedViewActions.UpdateMainRoad(
                    startPostId = 0
                )
            )
            viewModel.getMomentDelegate().initialLoadMoments()
        }
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

    var isMapCollapsed = true

    override fun onAvatarClicked(post: PostUIEntity, adapterPosition: Int) {
        post.user?.let { user ->
            act.addFragmentIgnoringAuthCheck(
                UserInfoFragment(), Act.COLOR_STATUSBAR_LIGHT_NAVBAR,
                Arg(IArgContainer.ARG_USER_ID, user.userId),
                Arg(IArgContainer.ARG_TRANSIT_FROM, AmplitudePropertyWhere.FEED.property)
            )
        }
    }

    override fun onShowUserMomentsClicked(
        userId: Long,
        fromView: View?,
        hasNewMoments: Boolean?
    ) {
        if((activity?.application as? FeatureTogglesContainer)?.momentsFeatureToggle?.isEnabled == true) {
            act.openUserMoments(
                userId = userId,
                fromView = fromView,
                openedWhere = AmplitudePropertyMomentScreenOpenWhere.MAIN_FEED_AVATAR,
                viewedEarly = hasNewMoments
            )
        }
    }

    override fun startVideoIfExist() {
        if (isMapCollapsed) {
            var lastPostMediaViewInfo: PostMediaViewInfo? = null
            runCatching { lastPostMediaViewInfo = viewModel.getLastPostMediaViewInfo() }

            lastPostMediaViewInfo?.let { viewInfo ->
                val postId = viewInfo.postId ?: return@let
                val mediaPosition = viewInfo.viewedMediaPosition ?: return@let
                val postUpdate = UIPostUpdate.UpdateSelectedMediaPosition(postId, mediaPosition)
                feedAdapter?.updateItem(postUpdate)
            }

            binding?.rvMainRoad?.apply {
                postDelayed({ onStart(lastPostMediaViewInfo = lastPostMediaViewInfo) }, FEED_START_VIDEO_DELAY)
            }
        }
    }

    override fun forceStartVideo() {
        binding?.rvMainRoad?.forcePlay()
    }

    override fun stopVideoIfExist() {
        binding?.rvMainRoad?.onStop()
    }

    override fun controlAlreadyPlayingVideo() {
        binding?.rvMainRoad?.onStopIfNeeded()
    }

    private fun initViews() {
        binding?.swipeMainRoad?.setOnRefreshListener(this)
    }

    private fun initPostsLiveObservable() {
        viewModel.livePosts.observe(viewLifecycleOwner) {
            if (isZeroItemLoading) {
                isZeroItemLoading = false
                binding?.swipeMainRoad?.isEnabled = false
                doDelayed(400) { // if not to do this feeds will be jumping after several refresh
                    binding?.swipeMainRoad?.isEnabled = true
                }
            }
        }
        viewModel.liveEvent.observe(viewLifecycleOwner) {
            handlePostViewEvents(it)
        }
    }

    override fun onRefresh() {
        loadPosts(true)
        scrollToTop()
    }

    private fun handlePostViewEvents(event: FeedViewEvent) {
        when (event) {
            is FeedViewEvent.OnFirstPageLoaded -> viewModel.getMomentDelegate().initialLoadMoments()
            else -> Unit
        }
    }

    private var canUpdate = true

    override fun onFeedBtnClicked() {
        if (binding?.rvMainRoad?.computeVerticalScrollOffset() == 0 && canUpdate) {
            binding?.swipeMainRoad?.isRefreshing = true
            onRefresh()
            canUpdate = false
            doDelayed(1000) { canUpdate = true }
        } else {
            scrollToTop()
            // Refresh road after scroll
            doDelayed(200) {
                loadPosts(true)
            }
        }
    }

    override fun hideRefreshLayoutProgress() {
        binding?.swipeMainRoad?.isRefreshing = false
    }

    override fun getRefreshTopButtonView(): LottieAnimationView? {
        return binding?.btnScrollRefreshMainRoad
    }

    override fun refreshRoadAfterScrollRefresh() {
        super.refreshRoadAfterScrollRefresh()
        loadPosts(true)
        onUpdateMomentsClicked()
        scrollToTop()
    }

    override fun onStartFragment() {
        super.onStartFragment()
        if (currentFragment?.requestCurrentFragment() == RoadTypeEnum.MAIN_ROAD.index) {
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

    override fun onDestroyView() {
        binding?.rvMainRoad?.onDestroyView()
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding?.rvMainRoad?.clear()
    }

    override fun onReturnTransitionFragment() {
        startVideoIfExist()
    }

    override fun setVolumeState(volumeState: VolumeState) {
        viewModel.onTriggerAction(FeedViewActions.UpdateVolumeState(volumeState))
    }

    override fun getVolumeState() = viewModel.getVolumeState()

    override fun onPause() {
        super.onPause()
        addNewPostTooltip?.dismiss()
    }

    override fun onStopFragment() {
        super.onStopFragment()
        addNewPostTooltip?.dismiss()

        stopVideoIfExist()
    }

    fun showAddNewPostTooltip(): Job {
        return lifecycleScope.launch {
            delay(TooltipDuration.COMMON_START_DELAY)

            addNewPostTooltip?.showAboveView(
                fragment = this@MainRoadFragment,
                view = binding?.rvMainRoad as View,
                offsetX = -(20.dp),
                offsetY = 84.dp
            )

            delay(TooltipDuration.ADD_NEW_POST)

            addNewPostTooltip?.dismiss()
        }
    }

    private fun observeMomentViewFragmentResult() {
        act?.navigatorAdapter?.fragmentManager?.setFragmentResultListener(
            KEY_MAIN_ROAD_WATCHED_MOMENT_GROUP,
            viewLifecycleOwner
        ) { _, bundle ->
            val viewedGroupId = bundle.getLong(KEY_MOMENT_GROUP_ID, -1L)
            if (currentFragment?.requestCurrentFragment() != RoadTypeEnum.MAIN_ROAD.index) {
                return@setFragmentResultListener
            }
            viewModel.getMomentDelegate().updateMomentsAndViewedPosition(viewedGroupId)
        }
    }
}
