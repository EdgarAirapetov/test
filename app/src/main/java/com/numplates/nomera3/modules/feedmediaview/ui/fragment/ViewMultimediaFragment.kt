package com.numplates.nomera3.modules.feedmediaview.ui.fragment

import android.graphics.Point
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.meera.core.extensions.click
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.goneAnimation
import com.meera.core.extensions.visible
import com.meera.core.extensions.visibleAnimation
import com.numplates.nomera3.App
import com.numplates.nomera3.MEDIA_IMAGE
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentViewMultimediaBinding
import com.numplates.nomera3.modules.auth.util.needAuth
import com.numplates.nomera3.modules.auth.util.needAuthAndReturnStatus
import com.numplates.nomera3.modules.baseCore.helper.SaveMediaFileDelegate
import com.numplates.nomera3.modules.baseCore.helper.SaveMediaFileDelegateImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.createAmplitudeReactionsParams
import com.numplates.nomera3.modules.comments.bottomsheet.presentation.MeeraCommentsBottomSheetFragment
import com.numplates.nomera3.modules.common.ActivityToolsProvider
import com.numplates.nomera3.modules.feed.data.entity.PostMediaViewInfo
import com.numplates.nomera3.modules.feed.ui.adapter.ContentActionBar
import com.numplates.nomera3.modules.feed.ui.entity.DestinationOriginEnum
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.util.PostMediaDownloadControllerUtil
import com.numplates.nomera3.modules.feedmediaview.ui.adapter.ViewMultimediaPagerAdapter
import com.numplates.nomera3.modules.feedmediaview.ui.content.action.ViewMultimediaAction
import com.numplates.nomera3.modules.feedmediaview.ui.content.state.ViewMultimediaScreenState
import com.numplates.nomera3.modules.feedmediaview.ui.viewmodel.ViewMultimediaViewModel
import com.numplates.nomera3.modules.feedviewcontent.presentation.custom.ViewContentGestures
import com.numplates.nomera3.modules.feedviewcontent.presentation.dialog.ContentBottomSheetDialogListener
import com.numplates.nomera3.modules.reaction.ui.custom.FlyingReaction
import com.numplates.nomera3.modules.reaction.ui.data.ReactionSource
import com.numplates.nomera3.modules.reaction.ui.getDefaultReactionContainer
import com.numplates.nomera3.modules.reaction.ui.mapper.toContentActionBarParams
import com.numplates.nomera3.modules.reaction.ui.util.reactionCount
import com.numplates.nomera3.modules.reactionStatistics.ui.ReactionsEntityType
import com.numplates.nomera3.modules.reactionStatistics.ui.ReactionsStatisticsBottomSheetFragment
import com.numplates.nomera3.modules.viewvideo.presentation.data.ViewVideoInitialData
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraMenuBottomSheet
import com.numplates.nomera3.presentation.view.ui.bottomMenu.ReactionsStatisticBottomMenu
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

//private const val VIEW_MULTIMEDIA_MENU_TAG = "VIEW_MULTIMEDIA_MENU_TAG"

private const val NO_POSITION = -1

class ViewMultimediaFragment :
    BaseFragmentNew<FragmentViewMultimediaBinding>(),
    ContentBottomSheetDialogListener,
    MeeraMenuBottomSheet.Listener,
    SaveMediaFileDelegate by SaveMediaFileDelegateImpl(),
    ViewMultimediaActionListener {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentViewMultimediaBinding
        get() = FragmentViewMultimediaBinding::inflate

    private val viewModel by viewModels<ViewMultimediaViewModel> { App.component.getViewModelFactory() }
    private var viewContentGestures: ViewContentGestures? = null
    private var post: PostUIEntity? = null
    private var currentAssetId: String? = null
    private var currentAssetType: String? = null
    private var needToShowRepost = true
    private var postOrigin: DestinationOriginEnum? = null
    private var screenStateJob: Job? = null
    private var effectsJob: Job? = null
    private var isVolumeEnabled: Boolean = true
    private var videoInitialData: ViewVideoInitialData? = null
    private var postLoaderController: PostMediaDownloadControllerUtil? = null
    private var currentItemPosition = NO_POSITION
    private var currentPositionOffset: Float = 0f
    private var postIsUnavailable: Boolean = false
    private var isFragmentRemoving = false

    private val actionBarListener = ViewMultimediaActionBarListener()

    private val adapter: ViewMultimediaPagerAdapter by lazy(LazyThreadSafetyMode.NONE) {
        ViewMultimediaPagerAdapter(
            fragment = this,
            fragmentManager = childFragmentManager,
            actionListener = this
        )
    }

    private val pagerListener = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            if (positionOffset != 0f) {
                if (currentPositionOffset == 0f) {
                    pauseVideoPlayback(currentItemPosition)
                    disableAllGestures()
                }
            } else {
                resumeVideoPlayback(currentItemPosition)
                enableAllGestures()
            }

            currentPositionOffset = positionOffset
        }

        override fun onPageSelected(position: Int) {
            if (isFragmentRemoving) return
            initMenuVisibility(position)
            updateViewerCounter(position)
            resetVideoPlayback(currentItemPosition)
            currentItemPosition = position
            if (videoInitialData == null) return
            setInitialVideoData(position)
        }
    }

    private fun initMenuVisibility(position: Int) {
        binding?.apply {
            val currentItem = adapter.getItemFromPosition(position) ?: return
            if (currentItem.isAvailable) {
                ivViewMultimediaVideoMenu.visible()
                cabViewMultimediaVideoActionBar.visibleAnimation()
            } else {
                ivViewMultimediaVideoMenu.gone()
                cabViewMultimediaVideoActionBar.gone()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPermissionDelegate(requireActivity(), viewLifecycleOwner)
        initViews()
        initActionBar()
        initClickListeners()
        initPostDownloadController()
        initStatusBarViewHeight<ConstraintLayout.LayoutParams>(binding?.vViewMultimediaStatusBar)
        initViewPagerBlockListener()
        initGestures()
        startObserversJobs()
        initData()
    }

    override fun onStartFragment() {
        super.onStartFragment()
        startObserversJobs()
        resumeVideoIfNeeded()
    }

    override fun onStopFragment() {
        pauseVideoPlayback(currentItemPosition)
        cancelObserversJobs()
        super.onStopFragment()
    }

    override fun onStop() {
        saveLastMediaViewInfo()
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewContentGestures?.destroyGesturesInterceptor()
    }

    private fun initData() {
        arguments?.let {
            val postId = it.getLong(ARG_VIEW_MULTIMEDIA_POST_ID)
            post = it.getParcelable(ARG_VIEW_MULTIMEDIA_DATA) as? PostUIEntity
            currentAssetId = it.getString(ARG_VIEW_MULTIMEDIA_ASSET_ID)
            currentAssetType = it.getString(ARG_VIEW_MULTIMEDIA_ASSET_TYPE)
            postOrigin = it.getSerializable(IArgContainer.ARG_POST_ORIGIN) as? DestinationOriginEnum
            needToShowRepost = it.getBoolean(IArgContainer.ARG_NEED_TO_REPOST, true)
            videoInitialData = it.getSerializable(ARG_VIEW_MULTIMEDIA_VIDEO_DATA) as? ViewVideoInitialData

            viewModel.handleViewAction(
                ViewMultimediaAction.SetPostData(
                    postId = postId,
                    post = post,
                    postOrigin = postOrigin,
                    isVolumeEnabled = isVolumeEnabled
                )
            )
        }
    }

    private fun initPostDownloadController() {
        binding?.apply {
            postLoaderController = PostMediaDownloadControllerUtil(plvViewMultimediaPostLoader) {
                viewModel.handleViewAction(ViewMultimediaAction.OnCancelDownloadClicked)
            }
        }
    }

    private fun initViews() {
        binding?.vpViewMultimediaContainer?.apply {
            adapter = this@ViewMultimediaFragment.adapter
            registerOnPageChangeCallback(pagerListener)
        }
    }

    private fun <T : ViewGroup.MarginLayoutParams> initStatusBarViewHeight(statusBarViewHeight: View?) {
        if (statusBarViewHeight != null) {
            runCatching {
                val params = statusBarViewHeight.layoutParams as? T
                if (params != null) {
                    params.height = context.getStatusBarHeight()
                    statusBarViewHeight.layoutParams = params
                }
            }
        }
    }

    private fun initGestures() {
        viewContentGestures = ViewContentGestures().apply {
            initGesturesInterceptor(
                extendedGestureOverlayView = binding?.govViewMultimediaGestureInterceptor,
                viewPager2 = binding?.vpViewMultimediaContainer
            )
            onVerticalSwipe = {
                isFragmentRemoving = true
                activity?.onBackPressed()
            }
        }
    }

    private fun initClickListeners() {
        binding?.ivViewMultimediaBack?.click { activity?.onBackPressed() }
        binding?.ivViewMultimediaVideoMenu?.click { handleMenuClick() }
    }

    private fun handleMenuClick() {
        val position = binding?.vpViewMultimediaContainer?.currentItem ?: return
        val currentAsset = adapter.getItemFromPosition(position) ?: return
        viewModel.handleViewAction(ViewMultimediaAction.OnOpenMenuClicked(currentAsset))
    }

//    private fun downloadImage(mediaAssetId: String?) {
//        val assets = post?.assets ?: return
//        val imageUrl = assets.find { it.id == mediaAssetId }?.image ?: return
//        saveImageOrVideoFile(
//            imageUrl = imageUrl,
//            act = act,
//            viewLifecycleOwner = viewLifecycleOwner,
//            successListener = {
//                showCommonSuccessMessage(R.string.image_saved)
//            }
//        )
//    }

    private fun initViewPagerBlockListener() {
        childFragmentManager.setFragmentResultListener(
            KEY_VIEW_PAGER_BLOCK_TOUCHES,
            viewLifecycleOwner
        ) { _, bundle ->
            val blocked = bundle.getBoolean(KEY_VIEW_PAGER_IS_BLOCKED)
            viewContentGestures?.isTouchesBlocked = blocked
        }
    }

    private fun startObserversJobs() {
        if (screenStateJob != null && effectsJob != null) return
        screenStateJob = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.viewMultimediaScreenState.onEach { handleStateUpdate(it) }.launchIn(this)
        }
        effectsJob = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
//            viewModel.viewMultimediaUiEffect.collect(::handleUiEffect)
        }
    }

    private fun cancelObserversJobs() {
        screenStateJob?.cancel()
        effectsJob?.cancel()
        screenStateJob = null
        effectsJob = null
    }

    private fun setInitialVideoData(fragmentPosition: Int) {
        binding?.vpViewMultimediaContainer?.post {
            val currentItem = adapter.getItemFromPosition(fragmentPosition) ?: return@post
            if (adapter.isItemVideo(fragmentPosition) && currentItem.id == videoInitialData?.id) {
                val fragment = adapter.getFragmentByItemId(currentItem.id)
                (fragment as? ViewMultimediaVideoItemFragment)?.initTimelineViews(videoInitialData)
                videoInitialData = null
            }
        }
    }

    private fun handleStateUpdate(state: ViewMultimediaScreenState) {
        when (state) {
            is ViewMultimediaScreenState.Loading -> handleStateLoading()
            is ViewMultimediaScreenState.MultimediaPostInfo -> handleUpdatePostInfo(state)
            is ViewMultimediaScreenState.Unavailable -> handlePostUnavailableState()
        }
    }

    private fun handleStateLoading() {
        showLoading()
        initActionBar()
    }

    private fun resetVideoPlayback(position: Int) {
        getVideoFragmentByPosition(position)?.resetVideoPlayback()
    }

    private fun pauseVideoPlayback(position: Int) {
        getVideoFragmentByPosition(position)?.pausePlayback()
    }

    private fun resumeVideoPlayback(position: Int) {
        getVideoFragmentByPosition(position)?.resumePlayback()
    }

    private fun saveLastMediaViewInfo() {
        val lastMediaViewInfo =
            if (postIsUnavailable) {
                val currentPosition = post?.assets?.indexOfFirst { it.id == currentAssetId } ?: 0
                val lastVideoPlaybackPosition = videoInitialData?.position
                PostMediaViewInfo(
                    postId = post?.postId,
                    viewedMediaPosition = currentPosition,
                    lastVideoPlaybackPosition = lastVideoPlaybackPosition
                )
            } else {
                val currentMediaPosition = currentItemPosition
                val currentMediaFragment = getVideoFragmentByPosition(currentMediaPosition)
                PostMediaViewInfo(
                    postId = post?.postId,
                    viewedMediaPosition = currentMediaPosition,
                    lastVideoPlaybackPosition = currentMediaFragment?.getCurrentVideoPlaybackPosition()
                )
            }

        viewModel.handleViewAction(ViewMultimediaAction.SaveLastMediaViewInfo(lastMediaViewInfo))
    }

    private fun getVideoFragmentByPosition(position: Int): ViewMultimediaVideoItemFragment? {
        if (position == NO_POSITION) return null

        val currentItem = adapter.getItemFromPosition(position) ?: return null
        val fragment = adapter.getFragmentByItemId(currentItem.id)

        return fragment as? ViewMultimediaVideoItemFragment?
    }

    private fun disableAllGestures() {
        for (i in 0 until adapter.itemCount) {
            val currentItem = adapter.getItemFromPosition(i)
            currentItem?.let {
                val fragment = adapter.getFragmentByItemId(it.id)
                if (fragment is ViewMultimediaGesturesListener) {
                    fragment.disableGestures()
                }
            }
        }
    }

    private fun enableAllGestures() {
        for (i in 0 until adapter.itemCount) {
            val currentItem = adapter.getItemFromPosition(i)
            currentItem?.let {
                val fragment = adapter.getFragmentByItemId(it.id)
                if (fragment is ViewMultimediaGesturesListener) {
                    fragment.enableGestures()
                }
            }
        }
    }

    private fun updateViewerCounter(position: Int) {
        binding?.apply {
            if (adapter.itemCount <= 1) {
                tvViewMultimediaMediaCounter.gone()
            } else {
                tvViewMultimediaMediaCounter.text = requireContext().resources
                    .getString(R.string.post_multimedia_view_counter, position + 1, adapter.itemCount)
            }
        }
    }

    private fun showLoading() {
        binding?.apply {
            cpiViewMultimediaLoader.visible()

            vpViewMultimediaContainer.gone()
            cabViewMultimediaVideoActionBar.gone()
        }
    }

    private fun showContent() {
        binding?.apply {
            cpiViewMultimediaLoader.goneAnimation()

            vpViewMultimediaContainer.visibleAnimation()
        }
    }

    private fun initActionBar(post: PostUIEntity? = null, isCommentsShow: Boolean? = null) {
        val actionBarParams = post?.toContentActionBarParams()?.copy(
            isVideo = true,
            commentsIsHide = !(isCommentsShow ?: true)
        ) ?: getDefaultActionBarParams()
        binding?.cabViewMultimediaVideoActionBar?.init(
            params = actionBarParams,
            isNeedToShowRepost = needToShowRepost,
            isNeedCommentVibrate = false,
            callbackListener = actionBarListener
        )
    }

    private fun handleUpdatePostInfo(state: ViewMultimediaScreenState.MultimediaPostInfo) {
        val post = state.post
        val assets = post.assets ?: emptyList()
        adapter.submitList(assets)
        setInitialItem()

        initActionBar(state.post, state.isCommentsShow)
        isVolumeEnabled = state.isVolumeEnabled

        this.post = post

        showContent()
    }

    private fun handlePostUnavailableState() {
        binding?.apply {
            cabViewMultimediaVideoActionBar.gone()
            tvViewMultimediaMediaCounter.gone()
            ivViewMultimediaVideoMenu.gone()
            incViewMultimediaUnavailableLayout.tvMediaUnavailableHeader.text = initPostUnavailableTitle()
            incViewMultimediaUnavailableLayout.root.visible()
            postIsUnavailable = true
        }
    }

    private fun initPostUnavailableTitle(): String {
        return when (currentAssetType) {
            MEDIA_IMAGE -> getString(R.string.image_unavailable_default_title)
            else -> getString(R.string.video_unavailable_default_title)
        }
    }

//    private fun handleUiEffect(effect: ViewMultimediaUiEffect) {
//        when (effect) {
//            is ViewMultimediaUiEffect.UpdatePostReaction -> {
//                updateActionBar(
//                    post = effect.post,
//                    reactionHolderViewId = effect.reactionUpdate.reactionSource.reactionHolderViewId
//                )
//            }
//
//            is ViewMultimediaUiEffect.OpenShareMenu -> showShareMenu(effect.post)
//            is ViewMultimediaUiEffect.ShowErrorMessage -> showCommonError(effect.messageResId)
//            is ViewMultimediaUiEffect.OpenMenu -> showMenu(effect.menuItems)
//            is ViewMultimediaUiEffect.UpdateLoadingState -> onUpdateLoadingState(effect)
//            is ViewMultimediaUiEffect.SubscribedToPost -> onSubscribedToPost()
//            is ViewMultimediaUiEffect.UnsubscribedFromPost -> onUnsubscribedFromPost()
//            is ViewMultimediaUiEffect.HiddenUserRoad -> onHiddenUserRoad()
//            is ViewMultimediaUiEffect.AddedPostComplaint -> onAddedPostComplaint()
//            is ViewMultimediaUiEffect.OnPostDeleted -> onDeletedPost()
//            is ViewMultimediaUiEffect.PostLinkCopied -> onPostLinkCopied()
//        }
//    }
//    private fun onSubscribedToPost() = showCommonSuccessMessage(R.string.subscribe_post)

//    private fun onUnsubscribedFromPost() = showCommonSuccessMessage(R.string.unsubscribe_post)

//    private fun onHiddenUserRoad() {
//        showCommonSuccessMessage(R.string.post_author_hide_success)
//        act.onBackPressed()
//    }
//
//    private fun onAddedPostComplaint() = showCommonSuccessMessage(R.string.road_complaint_send_success)
//
//    private fun onDeletedPost() {
//        showCommonSuccessMessage(R.string.post_deleted_success)
//        act.onBackPressed()
//    }

//    private fun onPostLinkCopied() {
//        (requireActivity() as? ActivityToolsProvider)?.getTooltipController()
//            ?.showSuccessTooltip(R.string.copy_link_success)
//    }

//    private fun showMenu(menuItems: List<ViewMultimediaMenuItems>) {
//        needAuth {
//            resumeVideoIfNeeded()
//
//            val menu = MeeraMenuBottomSheet(context)
//
//            menuItems.forEach { menuItem ->
//                menu.addItem(
//                    title = getString(menuItem.titleResId),
//                    icon = menuItem.iconResId,
//                    iconAndTitleColor = menuItem.iconAndTitleColor
//                ) {
//                    when (menuItem) {
//                        is ViewMultimediaMenuItems.DownloadImage -> {
//                            downloadImage(menuItem.mediaAssetId)
//                        }
//
//                        is ViewMultimediaMenuItems.DownloadVideo -> {
//                            saveVideo(menuItem.postId, menuItem.mediaAssetId)
//                        }
//
//                        is ViewMultimediaMenuItems.SubscribeToPost -> {
//                            viewModel.handleViewAction(ViewMultimediaAction.OnSubscribeToPost(menuItem.postId))
//                        }
//
//                        is ViewMultimediaMenuItems.UnsubscribeFromPost -> {
//                            viewModel.handleViewAction(ViewMultimediaAction.OnUnsubscribeFromPost(menuItem.postId))
//                        }
//
//                        is ViewMultimediaMenuItems.SubscribeToUser -> {
//                            viewModel.handleViewAction(
//                                ViewMultimediaAction.OnSubscribeToUserClicked(
//                                    userId = menuItem.userId,
//                                    fromMenu = true
//                                )
//                            )
//                        }
//
//                        is ViewMultimediaMenuItems.HideUserRoad -> {
//                            viewModel.handleViewAction(ViewMultimediaAction.OnHideUserRoad(menuItem.userId))
//                        }
//
//                        is ViewMultimediaMenuItems.AddComplaintPost -> {
//                            viewModel.handleViewAction(ViewMultimediaAction.AddComplaintPost(menuItem.postId))
//                        }
//
//                        is ViewMultimediaMenuItems.DeletePost -> {
//                            viewModel.handleViewAction(ViewMultimediaAction.OnDeletePost(menuItem.postId))
//                        }
//
//                        is ViewMultimediaMenuItems.SharePost -> {
//                            viewModel.handleViewAction(ViewMultimediaAction.OnRepostClick)
//                        }
//
//                        is ViewMultimediaMenuItems.CopyPostLink -> {
//                            viewModel.handleViewAction(ViewMultimediaAction.OnCopyPostLink(menuItem.postId))
//                        }
//                    }
//                }
//            }
//
//            menu.showWithTag(manager = childFragmentManager, tag = VIEW_MULTIMEDIA_MENU_TAG)
//        }
//    }

    private fun resumeVideoIfNeeded() {
        val position = binding?.vpViewMultimediaContainer?.currentItem ?: return
        val itemId = adapter.getItemIdByPosition(position) ?: return
        val currentFragment = adapter.getFragmentByItemId(itemId)
        if (currentFragment is ViewMultimediaVideoItemFragment) {
            currentFragment.resumeVideo()
        }
    }

//    private fun saveVideo(postId: Long, assetId: String?) {
//        setPermissions(
//            object : PermissionDelegate.Listener {
//                override fun onGranted() {
//                    viewModel.handleViewAction(ViewMultimediaAction.OnDownloadVideoClicked(postId, assetId))
//                }
//
//                override fun onDenied() {
//                    NToast.with(act)
//                        .text(act.getString(R.string.you_must_grant_permissions))
//                        .durationLong()
//                        .button(act.getString(R.string.general_retry)) {
//                            saveVideo(postId, assetId)
//                        }.show()
//                }
//
//                override fun onError(error: Throwable?) {
//                    Timber.e("ERROR get Permissions: \$error")
//                }
//            },
//            returnReadExternalStoragePermissionAfter33(),
//            returnWriteExternalStoragePermissionAfter33()
//        )
//    }

//    private fun onUpdateLoadingState(update: ViewMultimediaUiEffect.UpdateLoadingState) {
//        post = update.post
//        postLoaderController?.setupLoading(update.post.postId, update.loadingInfo)
//    }
//
//    private fun updateActionBar(post: PostUIEntity, reactionHolderViewId: ContentActionBar.ReactionHolderViewId) {
//        binding?.cabViewMultimediaVideoActionBar?.update(
//            post.toContentActionBarParams().copy(isVideo = true),
//            reactionHolderViewId
//        )
//        this.post = post
//        resumeVideoIfNeeded()
//    }

//    private fun showShareMenu(post: PostUIEntity) {
//        needAuth {
//            resumeVideoIfNeeded()
//
//            SharePostBottomSheet(
//                postOrigin = postOrigin,
//                post = post.toPost(),
//                event = null,
//                callback = object : IOnSharePost {
//                    override fun onShareFindGroup() {
//                        act.goToGroups()
//                    }
//
//                    override fun onShareFindFriend() {
//                        add(
//                            SearchMainFragment(),
//                            Act.LIGHT_STATUSBAR,
//                            Arg(
//                                IArgContainer.ARG_FIND_FRIENDS_OPENED_FROM_WHERE,
//                                AmplitudeFindFriendsWhereProperty.SHARE
//                            )
//                        )
//                    }
//
//                    override fun onShareToGroupSuccess(groupName: String?) {
//                        viewModel.handleViewAction(ViewMultimediaAction.OnRepostSuccess(post))
//                        NToast.with(act)
//                            .durationLong()
//                            .text(getString(R.string.success_repost_to_group, groupName ?: ""))
//                            .typeSuccess()
//                            .show()
//                    }
//
//                    override fun onShareToRoadSuccess() {
//                        viewModel.handleViewAction(ViewMultimediaAction.OnRepostSuccess(post))
//                        showCommonSuccessMessage(R.string.success_repost_to_own_road)
//                    }
//
//                    override fun onShareToChatSuccess(repostTargetCount: Int) {
//                        viewModel.handleViewAction(ViewMultimediaAction.OnRepostSuccess(post, repostTargetCount))
//                        showCommonSuccessMessage(R.string.success_repost_to_chat)
//                    }
//
//                    override fun onPostItemUniqnameUserClick(userId: Long?) = openUserFragment(userId)
//
//                    override fun onOpenShareOutside() {
//                        viewModel.handleViewAction(ViewMultimediaAction.OnShareOutsideOpened(true))
//                    }
//
//                }).show(childFragmentManager)
//        }
//    }

//    private fun openUserFragment(userId: Long?) {
//        act.addFragmentIgnoringAuthCheck(
//            UserInfoFragment(), Act.COLOR_STATUSBAR_LIGHT_NAVBAR,
//            Arg(IArgContainer.ARG_USER_ID, userId)
//        )
//    }

    private fun setInitialItem() {
        if (currentAssetId == null) return
        val position = adapter.getItemPositionById(currentAssetId)
        currentAssetId = null
        binding?.vpViewMultimediaContainer?.apply {
            if (currentItem == position) return@apply
            endFakeDrag()
            setCurrentItem(position, false)
        }
    }

    private inner class ViewMultimediaActionBarListener : ContentActionBar.Listener {

        private val reactionController by lazy {
            (requireActivity() as ActivityToolsProvider).getReactionBubbleViewController()
        }

        override fun onReactionBadgeClick() {
            val needAuth = needAuthAndReturnStatus { resumeVideoIfNeeded() }
            if (needAuth) return

            val post = post ?: return
            if (viewModel.getFeatureTogglesContainer().detailedReactionsForPostFeatureToggle.isEnabled) {
                ReactionsStatisticsBottomSheetFragment.getInstance(
                    entityId = post.postId,
                    entityType = ReactionsEntityType.POST
                ).show(childFragmentManager)
            } else {
                val reactions = post.reactions ?: return
                val sortedReactions = reactions.sortedByDescending { reactionEntity -> reactionEntity.count }
                val menu = ReactionsStatisticBottomMenu(context)
                menu.addTitle(R.string.reactions_on_post, sortedReactions.reactionCount())
                sortedReactions.forEachIndexed { index, value ->
                    menu.addReaction(value, index != sortedReactions.size - 1)
                }
                menu.show(childFragmentManager)
            }
        }

        override fun onReactionLongClick(
            showPoint: Point,
            reactionTip: TextView,
            viewsToHide: List<View>,
            reactionHolderViewId: ContentActionBar.ReactionHolderViewId
        ) {
            val needAuth = needAuthAndReturnStatus { resumeVideoIfNeeded() }
            if (needAuth) return

            val post = post ?: return
            reactionController.showReactionBubble(
                reactionSource = ReactionSource.Post(
                    postId = post.postId,
                    reactionHolderViewId = reactionHolderViewId,
                    originEnum = postOrigin
                ),
                showPoint = showPoint,
                viewsToHide = viewsToHide,
                reactionTip = reactionTip,
                currentReactionsList = post.reactions ?: emptyList(),
                contentActionBarType = ContentActionBar.ContentActionBarType.DARK,
                containerInfo = act.getDefaultReactionContainer(),
                reactionsParams = post.createAmplitudeReactionsParams(postOrigin)
            )
        }

        override fun onReactionRegularClick(reactionHolderViewId: ContentActionBar.ReactionHolderViewId) {
            val post = post ?: return
            reactionController.onSelectDefaultReaction(
                reactionSource = ReactionSource.Post(
                    reactionHolderViewId = reactionHolderViewId,
                    postId = post.postId,
                    originEnum = null
                ),
                currentReactionsList = post.reactions ?: emptyList(),
                reactionsParams = post.createAmplitudeReactionsParams(postOrigin)
            )
        }

        override fun onCommentsClick() {
            needAuth {
                resumeVideoIfNeeded()
                val post = post ?: return@needAuth
                MeeraCommentsBottomSheetFragment.showForPost(post, childFragmentManager, postOrigin)
            }
        }

        override fun onRepostClick() {
            viewModel.handleViewAction(ViewMultimediaAction.OnRepostClick)
        }

        override fun onFlyingAnimationInitialized(flyingReaction: FlyingReaction) = Unit

        override fun onReactionButtonDisabledClick() = Unit
    }

    private fun getDefaultActionBarParams(): ContentActionBar.Params = ContentActionBar.Params(
        isEnabled = false,
        reactions = emptyList(),
        userAccountType = null,
        commentCount = 0,
        repostCount = 0,
        isMoment = false,
        isVideo = true,
    )

    override fun disableTouchEvent() {
        viewContentGestures?.isTouchesBlocked = true
    }

    override fun enableTouchEvent() {
        viewContentGestures?.isTouchesBlocked = false
    }

    override fun hideActionViews() {
        binding?.apply {
            cabViewMultimediaVideoActionBar.gone()
            flViewMultimediaToolbarContainer.gone()
        }
    }

    override fun showActionViews() {
        binding?.apply {
            cabViewMultimediaVideoActionBar.visible()
            flViewMultimediaToolbarContainer.visible()
        }
    }
}
