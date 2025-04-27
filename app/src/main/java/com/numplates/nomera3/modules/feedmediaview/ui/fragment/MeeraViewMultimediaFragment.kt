package com.numplates.nomera3.modules.feedmediaview.ui.fragment

import android.graphics.Point
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.meera.core.base.BasePermission
import com.meera.core.base.BasePermissionDelegate
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.click
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.goneAnimation
import com.meera.core.extensions.returnReadExternalStoragePermissionAfter33
import com.meera.core.extensions.returnWriteExternalStoragePermissionAfter33
import com.meera.core.extensions.safeNavigate
import com.meera.core.extensions.visible
import com.meera.core.extensions.visibleAnimation
import com.meera.core.permission.PermissionDelegate
import com.meera.core.utils.showCommonError
import com.meera.core.utils.showCommonSuccessMessage
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.snackbar.AvatarUiState
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.numplates.nomera3.App
import com.numplates.nomera3.MEDIA_IMAGE
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentViewMultimediaBinding
import com.numplates.nomera3.modules.baseCore.helper.SaveMediaFileDelegate
import com.numplates.nomera3.modules.baseCore.helper.SaveMediaFileDelegateImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.findfriends.AmplitudeFindFriendsWhereProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.createAmplitudeReactionsParams
import com.numplates.nomera3.modules.comments.bottomsheet.presentation.MeeraCommentsBottomSheetFragment
import com.numplates.nomera3.modules.common.ActivityToolsProvider
import com.numplates.nomera3.modules.feed.data.entity.PostMediaViewInfo
import com.numplates.nomera3.modules.feed.domain.mapper.toPost
import com.numplates.nomera3.modules.feed.ui.adapter.MeeraContentActionBar
import com.numplates.nomera3.modules.feed.ui.entity.DestinationOriginEnum
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.util.PostMediaDownloadControllerUtil
import com.numplates.nomera3.modules.feedmediaview.ui.ViewMultimediaMenuItems
import com.numplates.nomera3.modules.feedmediaview.ui.adapter.ViewMultimediaPagerAdapter
import com.numplates.nomera3.modules.feedmediaview.ui.content.action.ViewMultimediaAction
import com.numplates.nomera3.modules.feedmediaview.ui.content.effect.ViewMultimediaUiEffect
import com.numplates.nomera3.modules.feedmediaview.ui.content.state.ViewMultimediaScreenState
import com.numplates.nomera3.modules.feedmediaview.ui.viewmodel.ViewMultimediaViewModel
import com.numplates.nomera3.modules.feedviewcontent.presentation.custom.ViewContentGestures
import com.numplates.nomera3.modules.feedviewcontent.presentation.dialog.ContentBottomSheetDialogListener
import com.numplates.nomera3.modules.reaction.data.ReactionType
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity
import com.numplates.nomera3.modules.reaction.ui.custom.FlyingReaction
import com.numplates.nomera3.modules.reaction.ui.data.MeeraReactionSource
import com.numplates.nomera3.modules.reaction.ui.getDefaultReactionContainer
import com.numplates.nomera3.modules.reaction.ui.mapper.toMeeraContentActionBarParams
import com.numplates.nomera3.modules.reactionStatistics.ui.MeeraReactionsStatisticsBottomDialogFragment
import com.numplates.nomera3.modules.reactionStatistics.ui.ReactionsEntityType
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.modules.redesign.fragments.main.SUBSCRIPTION_ROAD_REQUEST_KEY
import com.numplates.nomera3.modules.redesign.util.needAuthToNavigate
import com.numplates.nomera3.modules.redesign.util.needAuthToNavigateWithResult
import com.numplates.nomera3.modules.viewvideo.presentation.data.ViewVideoInitialData
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.utils.ReactionAnimationHelper
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraMenuBottomSheet
import com.numplates.nomera3.presentation.view.utils.sharedialog.IOnSharePost
import com.numplates.nomera3.presentation.view.utils.sharedialog.MeeraShareBottomSheetData
import com.numplates.nomera3.presentation.view.utils.sharedialog.MeeraShareSheet
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

const val ARG_VIEW_MULTIMEDIA_POST_ID = "ARG_VIEW_MULTIMEDIA_POST_ID"
const val ARG_VIEW_MULTIMEDIA_DATA = "ARG_VIEW_MULTIMEDIA_DATA"
const val ARG_VIEW_MULTIMEDIA_ASSET_ID = "ARG_VIEW_MULTIMEDIA_ASSET_ID"
const val ARG_VIEW_MULTIMEDIA_ASSET_TYPE = "ARG_VIEW_MULTIMEDIA_ASSET_TYPE"
const val ARG_VIEW_MULTIMEDIA_VIDEO_DATA = "ARG_VIEW_MULTIMEDIA_VIDEO_DATA"
const val KEY_VIEW_PAGER_BLOCK_TOUCHES = "KEY_VIEW_PAGER_BLOCK_TOUCHES"
const val KEY_VIEW_PAGER_CONTENT_TAP = "KEY_VIEW_PAGER_CONTENT_TAP"
const val KEY_VIEW_PAGER_LONG_TAP_IS_ACTIVE = "KEY_VIEW_PAGER_TAP_IS_ACTIVE"
const val KEY_VIEW_PAGER_IS_BLOCKED = "KEY_VIEW_PAGER_IS_BLOCKED"

private const val VIEW_MULTIMEDIA_MENU_TAG = "VIEW_MULTIMEDIA_MENU_TAG"

private const val NO_POSITION = -1

class MeeraViewMultimediaFragment :
    MeeraBaseDialogFragment(
        layout = R.layout.meera_fragment_view_multimedia,
        behaviourConfigState = ScreenBehaviourState.FullScreenMoment
    ),
    ContentBottomSheetDialogListener,
    MeeraMenuBottomSheet.Listener,
    SaveMediaFileDelegate by SaveMediaFileDelegateImpl(),
    BasePermission by BasePermissionDelegate(),
    ViewMultimediaActionListener {

    override val containerId: Int
        get() = R.id.fragment_first_container_view

    private val binding by viewBinding(MeeraFragmentViewMultimediaBinding::bind)

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

    private var actionBarListener: ViewMultimediaActionBarListener? = null

    private var mediaAdapter: ViewMultimediaPagerAdapter? = null

    private var onBackPressedCallback: OnBackPressedCallback? = null

    private var actionBarOnLayoutChangeListener: View.OnLayoutChangeListener? = null

    private var reactionAnimationHelper: ReactionAnimationHelper? = null

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
        binding.apply {
            val currentItem = mediaAdapter?.getItemFromPosition(position) ?: return
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
        initReactionAnimationHelper()
        initStatusBar()
        initActionBar()
        initClickListeners()
        initPostDownloadController()
        initViewPagerBlockListener()
        initViewPagerImageTapListener()
        initGestures()
        startObserversJobs()
        initData()
        initBackPressedCallback()
    }

    private fun initStatusBar() {
        binding.vViewMultimediaStatusBar.updateLayoutParams {
            height = context.getStatusBarHeight()
        }
    }

    private fun initReactionAnimationHelper() {
        reactionAnimationHelper = ReactionAnimationHelper()
    }

    override fun onStart() {
        super.onStart()
        startObserversJobs()
        resumeVideoIfNeeded()
    }

    override fun onStop() {
        pauseVideoPlayback(currentItemPosition)
        cancelObserversJobs()
        saveLastMediaViewInfo()
        clearActionBarLayoutListener()
        super.onStop()
    }

    override fun onDestroyView() {
        mediaAdapter = null
        viewModel.clearPostId()
        viewContentGestures?.destroyGesturesInterceptor()
        onBackPressedCallback?.remove()
        actionBarListener = null
        reactionAnimationHelper = null
        binding.cabViewMultimediaVideoActionBar.clearResources()
        binding.vpViewMultimediaContainer.unregisterOnPageChangeCallback(pagerListener)
        binding.vpViewMultimediaContainer.adapter = null
        super.onDestroyView()
    }

    private fun initBackPressedCallback() {
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }
        }.also { backPressCallback ->
            requireActivity().onBackPressedDispatcher.addCallback(this, backPressCallback)
        }
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
        binding.apply {
            postLoaderController = PostMediaDownloadControllerUtil(plvViewMultimediaPostLoader) {
                viewModel.handleViewAction(ViewMultimediaAction.OnCancelDownloadClicked)
            }
        }
    }

    private fun initViews() {
        binding.vpViewMultimediaContainer.apply {
            val thisFragment = this@MeeraViewMultimediaFragment
            thisFragment.mediaAdapter = ViewMultimediaPagerAdapter(
                fragment = thisFragment,
                fragmentManager = childFragmentManager,
                actionListener = thisFragment
            )
            adapter = mediaAdapter
            registerOnPageChangeCallback(pagerListener)
        }
    }

    private fun initGestures() {
        viewContentGestures = ViewContentGestures().apply {
            initGesturesInterceptor(
                extendedGestureOverlayView = binding.govViewMultimediaGestureInterceptor,
                viewPager2 = binding.vpViewMultimediaContainer
            )
            onVerticalSwipe = {
                isFragmentRemoving = true
                findNavController().popBackStack()
            }
        }
    }

    private fun initClickListeners() {
        binding.ivViewMultimediaBack.click { findNavController().popBackStack() }
        binding.ivViewMultimediaVideoMenu.click { handleMenuClick() }
    }

    private fun handleMenuClick() {
        val position = binding.vpViewMultimediaContainer.currentItem ?: return
        val currentAsset = mediaAdapter?.getItemFromPosition(position) ?: return
        viewModel.handleViewAction(ViewMultimediaAction.OnOpenMenuClicked(currentAsset))
    }

    private fun downloadImage(mediaAssetId: String?) {
        val assets = post?.assets ?: return
        val imageUrl = assets.find { it.id == mediaAssetId }?.image ?: return
        saveImageOrVideoFile(
            imageUrl = imageUrl,
            act = requireActivity() as MeeraAct,
            viewLifecycleOwner = viewLifecycleOwner,
            successListener = { showSuccessMessage(R.string.image_saved) }
        )
    }

    private fun initViewPagerBlockListener() {
        childFragmentManager.setFragmentResultListener(
            KEY_VIEW_PAGER_BLOCK_TOUCHES,
            viewLifecycleOwner
        ) { _, bundle ->
            val blocked = bundle.getBoolean(KEY_VIEW_PAGER_IS_BLOCKED)
            viewContentGestures?.isTouchesBlocked = blocked
            if (blocked) hideActionViews() else showActionViews()
        }
    }

    private fun initViewPagerImageTapListener() {
        childFragmentManager.setFragmentResultListener(
            KEY_VIEW_PAGER_CONTENT_TAP,
            viewLifecycleOwner
        ) { _, bundle ->
            val longTapIsActive = bundle.getBoolean(KEY_VIEW_PAGER_LONG_TAP_IS_ACTIVE)
            if (viewContentGestures?.isTouchesBlocked == false)
                if (longTapIsActive) hideActionViews() else showActionViews()
        }
    }

    private fun startObserversJobs() {
        if (screenStateJob != null && effectsJob != null) return
        screenStateJob = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.viewMultimediaScreenState.onEach { handleStateUpdate(it) }.launchIn(this)
        }
        effectsJob = viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.viewMultimediaUiEffect.collect(::handleUiEffect)
        }
    }

    private fun cancelObserversJobs() {
        screenStateJob?.cancel()
        effectsJob?.cancel()
        screenStateJob = null
        effectsJob = null
    }

    private fun setInitialVideoData(fragmentPosition: Int) {
        binding.vpViewMultimediaContainer.post {
            mediaAdapter?.let { adapter ->
                val currentItem = adapter.getItemFromPosition(fragmentPosition) ?: return@post
                if (adapter.isItemVideo(fragmentPosition) && currentItem.id == videoInitialData?.id) {
                    val fragment = adapter.getFragmentByItemId(currentItem.id)
                    (fragment as? ViewMultimediaVideoItemFragment)?.initTimelineViews(videoInitialData)
                    videoInitialData = null
                }
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
        val currentItem = mediaAdapter?.getItemFromPosition(position) ?: return null
        val fragment = mediaAdapter?.getFragmentByItemId(currentItem.id)

        return fragment as? ViewMultimediaVideoItemFragment?
    }

    private fun disableAllGestures() {
        mediaAdapter?.let { adapter ->
            val itemCount = adapter.itemCount ?: 0
            for (i in 0 until itemCount) {
                val currentItem = adapter.getItemFromPosition(i)
                currentItem?.let {
                    val fragment = adapter.getFragmentByItemId(it.id)
                    if (fragment is ViewMultimediaGesturesListener) {
                        fragment.disableGestures()
                    }
                }
            }
        }
    }

    private fun enableAllGestures() {
        mediaAdapter?.let { adapter ->
            val itemCount = adapter.itemCount ?: 0
            for (i in 0 until itemCount) {
                val currentItem = adapter.getItemFromPosition(i)
                currentItem?.let {
                    val fragment = adapter.getFragmentByItemId(it.id)
                    if (fragment is ViewMultimediaGesturesListener) {
                        fragment.enableGestures()
                    }
                }
            }
        }
    }

    private fun updateViewerCounter(position: Int) {
        mediaAdapter?.let { adapter ->
            binding.apply {
                if (adapter.itemCount <= 1) {
                    tvViewMultimediaMediaCounter.gone()
                } else {
                    tvViewMultimediaMediaCounter.text = requireContext().resources
                        .getString(R.string.post_multimedia_view_counter, position + 1, adapter.itemCount)
                }
            }
        }
    }

    private fun showLoading() {
        binding.apply {
            cpiViewMultimediaLoader.visible()

            vpViewMultimediaContainer.gone()
            cabViewMultimediaVideoActionBar.gone()
        }
    }

    private fun showContent() {
        binding.apply {
            cpiViewMultimediaLoader.goneAnimation()

            vpViewMultimediaContainer.visibleAnimation()
        }
    }

    private fun initActionBar(post: PostUIEntity? = null, isCommentsShow: Boolean? = null) {
        val actionBarParams = post?.toMeeraContentActionBarParams()?.copy(
            isVideo = true,
            commentsIsHide = !(isCommentsShow ?: true)
        ) ?: getDefaultActionBarParams()
        actionBarListener = ViewMultimediaActionBarListener()
            .also { listener ->
                binding.cabViewMultimediaVideoActionBar.init(
                    params = actionBarParams,
                    isNeedToShowRepost = needToShowRepost,
                    isNeedCommentVibrate = false,
                    callbackListener = listener
                )
            }
    }

    private fun handleUpdatePostInfo(state: ViewMultimediaScreenState.MultimediaPostInfo) {
        val post = state.post
        val assets = post.assets ?: emptyList()
        mediaAdapter?.submitList(assets)
        setInitialItem()

        initActionBar(state.post, state.isCommentsShow)
        isVolumeEnabled = state.isVolumeEnabled

        this.post = post

        showContent()
    }

    private fun handlePostUnavailableState() {
        binding.apply {
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

    private fun handleUiEffect(effect: ViewMultimediaUiEffect) {
        when (effect) {
            is ViewMultimediaUiEffect.UpdatePostReaction -> {
                updateActionBar(
                    post = effect.post,
                    reactionHolderViewId = effect.reactionUpdate.reactionSource.reactionHolderViewId
                )
            }

            is ViewMultimediaUiEffect.OpenShareMenu -> showShareMenu(effect.post)
            is ViewMultimediaUiEffect.ShowErrorMessage -> showCommonError(getText(effect.messageResId), requireView())
            is ViewMultimediaUiEffect.OpenMenu -> showMenu(effect.menuItems)
            is ViewMultimediaUiEffect.UpdateLoadingState -> onUpdateLoadingState(effect)
            is ViewMultimediaUiEffect.SubscribedToPost -> onSubscribedToPost()
            is ViewMultimediaUiEffect.UnsubscribedFromPost -> onUnsubscribedFromPost()
            is ViewMultimediaUiEffect.HiddenUserRoad -> onHiddenUserRoad()
            is ViewMultimediaUiEffect.AddedPostComplaint -> onAddedPostComplaint()
            is ViewMultimediaUiEffect.OnPostDeleted -> onDeletedPost()
            is ViewMultimediaUiEffect.PostLinkCopied -> onPostLinkCopied()
        }
    }

    private fun onSubscribedToPost() = showSuccessMessage(R.string.subscribe_post)

    private fun onUnsubscribedFromPost() = showSuccessMessage(R.string.unsubscribe_post)

    private fun onHiddenUserRoad() {
        showSuccessMessage(R.string.post_author_hide_success)
        findNavController().popBackStack()
    }

    private fun onAddedPostComplaint() = showSuccessMessage(R.string.road_complaint_send_success)

    private fun onDeletedPost() {
        showSuccessMessage(R.string.post_deleted_success)
        findNavController().popBackStack()
    }

    private fun onPostLinkCopied() {
        showSuccessMessage(R.string.meera_copy_link_success)
    }

    private fun showMenu(menuItems: List<ViewMultimediaMenuItems>) {
        needAuthToNavigate {
            resumeVideoIfNeeded()

            val menu = MeeraMenuBottomSheet(context)

            menuItems.forEach { menuItem ->
                menu.addItem(
                    title = getString(menuItem.titleResId),
                    icon = menuItem.iconResId,
                    iconAndTitleColor = menuItem.iconAndTitleColor
                ) {
                    when (menuItem) {
                        is ViewMultimediaMenuItems.DownloadImage -> {
                            downloadImage(menuItem.mediaAssetId)
                        }

                        is ViewMultimediaMenuItems.DownloadVideo -> {
                            saveVideo(menuItem.postId, menuItem.mediaAssetId)
                        }

                        is ViewMultimediaMenuItems.SubscribeToPost -> {
                            viewModel.handleViewAction(ViewMultimediaAction.OnSubscribeToPost(menuItem.postId))
                        }

                        is ViewMultimediaMenuItems.UnsubscribeFromPost -> {
                            viewModel.handleViewAction(ViewMultimediaAction.OnUnsubscribeFromPost(menuItem.postId))
                        }

                        is ViewMultimediaMenuItems.SubscribeToUser -> {
                            viewModel.handleViewAction(
                                ViewMultimediaAction.OnSubscribeToUserClicked(
                                    userId = menuItem.userId,
                                    fromMenu = true
                                )
                            )
                        }

                        is ViewMultimediaMenuItems.HideUserRoad -> {
                            viewModel.handleViewAction(ViewMultimediaAction.OnHideUserRoad(menuItem.userId))
                        }

                        is ViewMultimediaMenuItems.AddComplaintPost -> {
                            viewModel.handleViewAction(ViewMultimediaAction.AddComplaintPost(menuItem.postId))
                        }

                        is ViewMultimediaMenuItems.DeletePost -> {
                            viewModel.handleViewAction(ViewMultimediaAction.OnDeletePost(menuItem.postId))
                        }

                        is ViewMultimediaMenuItems.SharePost -> {
                            viewModel.handleViewAction(ViewMultimediaAction.OnRepostClick)
                        }

                        is ViewMultimediaMenuItems.CopyPostLink -> {
                            viewModel.handleViewAction(ViewMultimediaAction.OnCopyPostLink(menuItem.postId))
                        }
                    }
                }
            }

            menu.showWithTag(manager = childFragmentManager, tag = VIEW_MULTIMEDIA_MENU_TAG)
        }
    }

    private fun resumeVideoIfNeeded() {
        mediaAdapter?.let { adapter ->
            val position = binding.vpViewMultimediaContainer.currentItem
            val itemId = adapter.getItemIdByPosition(position) ?: return
            val currentFragment = adapter.getFragmentByItemId(itemId)
            if (currentFragment is ViewMultimediaVideoItemFragment) {
                currentFragment.resumeVideo()
            }
        }
    }

    private fun saveVideo(postId: Long, assetId: String?) {
        setPermissions(
            object : PermissionDelegate.Listener {
                override fun onGranted() {
                    viewModel.handleViewAction(ViewMultimediaAction.OnDownloadVideoClicked(postId, assetId))
                }

                override fun onDenied() {
                    showDeniedPermissionSnackBar(postId, assetId)
                }

                override fun onError(error: Throwable?) {
                    Timber.e("ERROR get Permissions: \$error")
                }
            },
            returnReadExternalStoragePermissionAfter33(),
            returnWriteExternalStoragePermissionAfter33()
        )
    }

    private fun showDeniedPermissionSnackBar(postId: Long, assetId: String?) {
        UiKitSnackBar.make(
            view = requireView(),
            params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = getText(R.string.you_must_grant_permissions),
                    buttonActionText = getText(R.string.general_retry),
                    buttonActionListener = {
                        saveVideo(postId, assetId)
                    },
                    avatarUiState = AvatarUiState.ErrorIconState
                ),
                duration = BaseTransientBottomBar.LENGTH_LONG
            )
        ).show()
    }

    private fun onUpdateLoadingState(update: ViewMultimediaUiEffect.UpdateLoadingState) {
        post = update.post
        postLoaderController?.setupLoading(update.post.postId, update.loadingInfo)
    }

    private fun updateActionBar(post: PostUIEntity, reactionHolderViewId: MeeraContentActionBar.ReactionHolderViewId) {
        binding.cabViewMultimediaVideoActionBar.update(
            post.toMeeraContentActionBarParams().copy(isVideo = true),
            reactionHolderViewId
        )
        this.post = post
        resumeVideoIfNeeded()
    }

    private fun showShareMenu(post: PostUIEntity) {
        needAuthToNavigate {
            resumeVideoIfNeeded()
            MeeraShareSheet().show(
                fm = childFragmentManager,
                data = MeeraShareBottomSheetData(
                    postOrigin = postOrigin,
                    post = post.toPost(),
                    event = null,
                    callback = object : IOnSharePost {
                        override fun onShareFindGroup() {
                            findNavController()
                                .safeNavigate(
                                    R.id.action_meeraViewMultimediaFragment_to_meeraCommunitiesListsContainerFragment
                                )
                        }

                        override fun onShareFindFriend() {
                            findNavController().safeNavigate(
                                resId = R.id.action_meeraViewMultimediaFragment_to_meeraSearchFragment,
                                bundle = Bundle().apply {
                                    putSerializable(
                                        IArgContainer.ARG_FIND_FRIENDS_OPENED_FROM_WHERE,
                                        AmplitudeFindFriendsWhereProperty.SHARE
                                    )
                                }
                            )
                        }

                    override fun onShareToGroupSuccess(groupName: String?) {
                        viewModel.handleViewAction(ViewMultimediaAction.OnRepostSuccess(post))
                        showSuccessMessage(getString(R.string.success_repost_to_group, groupName ?: ""))
                    }

                    override fun onShareToRoadSuccess() {
                        viewModel.handleViewAction(ViewMultimediaAction.OnRepostSuccess(post))
                        showSuccessMessage(R.string.success_repost_to_own_road)
                    }

                    override fun onShareToChatSuccess(repostTargetCount: Int) {
                        viewModel.handleViewAction(ViewMultimediaAction.OnRepostSuccess(post, repostTargetCount))
                        showSuccessMessage(R.string.success_repost_to_chat)
                    }

                        override fun onPostItemUniqnameUserClick(userId: Long?) {
                            openUserFragment(userId)
                        }

                        override fun onOpenShareOutside() {
                            viewModel.handleViewAction(ViewMultimediaAction.OnShareOutsideOpened(true))
                        }
                    }
                )
            )
        }
    }

    private fun openUserFragment(userId: Long?) {
        findNavController().safeNavigate(
            resId = R.id.action_global_userInfoFragment,
            bundle = Bundle().apply {
                putSerializable(IArgContainer.ARG_USER_ID, userId)
            }
        )
    }

    private fun setInitialItem() {
        if (currentAssetId == null || mediaAdapter == null) return
        val position = mediaAdapter?.getItemPositionById(currentAssetId) ?: 0
        currentAssetId = null
        binding.vpViewMultimediaContainer.apply {
            if (currentItem == position) return@apply
            endFakeDrag()
            setCurrentItem(position, false)
        }
    }

    private inner class ViewMultimediaActionBarListener : MeeraContentActionBar.Listener {

        private val reactionController by lazy {
            (requireActivity() as ActivityToolsProvider).getMeeraReactionBubbleViewController()
        }

        override fun onReactionBadgeClick() {
            needAuthToNavigate {
                val post = post ?: return@needAuthToNavigate
                MeeraReactionsStatisticsBottomDialogFragment.makeInstance(
                    entityId = post.postId,
                    entityType = ReactionsEntityType.POST
                ) { destination ->
                    when (destination) {
                        is MeeraReactionsStatisticsBottomDialogFragment.DestinationTransition.UserProfileDestination -> {
                            openUserFragment(destination.userEntity.userId)
                        }
                    }
                }.show(childFragmentManager)
            }
        }

        override fun onReactionLongClick(
            showPoint: Point,
            reactionTip: TextView,
            viewsToHide: List<View>,
            reactionHolderViewId: MeeraContentActionBar.ReactionHolderViewId
        ) {
            val needAuth = needAuthToNavigateWithResult(SUBSCRIPTION_ROAD_REQUEST_KEY) { resumeVideoIfNeeded() }
            if (needAuth) return

            val post = post ?: return
            reactionController.showReactionBubble(
                reactionSource = MeeraReactionSource.Post(
                    postId = post.postId,
                    reactionHolderViewId = reactionHolderViewId,
                    originEnum = postOrigin
                ),
                showPoint = showPoint,
                viewsToHide = viewsToHide,
                reactionTip = reactionTip,
                currentReactionsList = post.reactions ?: emptyList(),
                contentActionBarType = MeeraContentActionBar.ContentActionBarType.DARK,
                containerInfo = (requireActivity() as MeeraAct).getDefaultReactionContainer(),
                reactionsParams = post.createAmplitudeReactionsParams(postOrigin)
            )
        }

        override fun onReactionRegularClick(reactionHolderViewId: MeeraContentActionBar.ReactionHolderViewId) {
            setRegularReaction(reactionHolderViewId)
        }

        override fun onCommentsClick() {
            needAuthToNavigate {
                resumeVideoIfNeeded()
                val post = post ?: return@needAuthToNavigate
                MeeraCommentsBottomSheetFragment.showForPost(post, childFragmentManager, postOrigin)
            }
        }

        override fun onRepostClick() {
            viewModel.handleViewAction(ViewMultimediaAction.OnRepostClick)
        }

        override fun onFlyingAnimationInitialized(flyingReaction: FlyingReaction) = Unit

        override fun onReactionButtonDisabledClick() = Unit

        override fun onReactionClickToShowScreenAnimation(
            reactionEntity: ReactionEntity,
            anchorViewLocation: Pair<Int, Int>
        ) {
            val reactionType = ReactionType.getByString(reactionEntity.reactionType) ?: return
            reactionAnimationHelper?.playLottieAtPosition(
                recyclerView = binding.vpViewMultimediaContainer.get(0) as RecyclerView,
                requireContext(),
                parent = binding.clMainContainer,
                reactionType = reactionType,
                x = anchorViewLocation.first.toFloat(),
                y = anchorViewLocation.second.toFloat()
            )
        }

        private fun setRegularReaction(reactionHolderViewId: MeeraContentActionBar.ReactionHolderViewId) {
            binding.cabViewMultimediaVideoActionBar.post {
                val post = post ?: return@post
                reactionController.onSelectDefaultReaction(
                    reactionSource = MeeraReactionSource.Post(
                        reactionHolderViewId = reactionHolderViewId,
                        postId = post.postId,
                        originEnum = null
                    ),
                    currentReactionsList = post.reactions ?: emptyList(),
                    reactionsParams = post.createAmplitudeReactionsParams(postOrigin)
                )
            }
        }
    }

    private fun getDefaultActionBarParams(): MeeraContentActionBar.Params = MeeraContentActionBar.Params(
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
        binding.apply {
            cabViewMultimediaVideoActionBar.gone()
            flViewMultimediaToolbarContainer.gone()
        }
    }

    override fun showActionViews() {
        binding.apply {
            cabViewMultimediaVideoActionBar.visible()
            flViewMultimediaToolbarContainer.visible()
        }
    }

    private fun clearActionBarLayoutListener() {
        actionBarOnLayoutChangeListener?.apply { binding.cabViewMultimediaVideoActionBar.removeOnLayoutChangeListener(this) }
            .also { actionBarOnLayoutChangeListener = null }
    }

    private fun showSuccessMessage(errorTextRes: Int) {
        showSuccessMessage(requireContext().resources.getString(errorTextRes))
    }

    private fun showSuccessMessage(errorText: String) {
        showCommonSuccessMessage(
            content = errorText,
            view = requireView(),
            anchorView = binding.cabViewMultimediaVideoActionBar.takeIf { it.isVisible }
        )
    }
}

