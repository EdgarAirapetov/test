package com.numplates.nomera3.modules.viewvideo.presentation

import android.animation.ObjectAnimator
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.meera.core.base.BasePermission
import com.meera.core.base.BasePermissionDelegate
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.dialogs.MeeraConfirmDialogBuilder
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.goneAnimation
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.keepScreenOnDisable
import com.meera.core.extensions.keepScreenOnEnable
import com.meera.core.extensions.returnReadExternalStoragePermissionAfter33
import com.meera.core.extensions.returnWriteExternalStoragePermissionAfter33
import com.meera.core.extensions.safeNavigate
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.core.extensions.visibleAnimation
import com.meera.core.permission.PermissionDelegate
import com.meera.core.utils.checkAppRedesigned
import com.meera.core.utils.showCommonError
import com.meera.core.utils.showCommonSuccessMessage
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.snackbar.AvatarUiState
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentViewVideoBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.findfriends.AmplitudeFindFriendsWhereProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.createAmplitudeReactionsParams
import com.numplates.nomera3.modules.comments.bottomsheet.presentation.MeeraCommentsBottomSheetFragment
import com.numplates.nomera3.modules.common.ActivityToolsProvider
import com.numplates.nomera3.modules.exoplayer.presentation.ExoPlayerPlaybackStateListener
import com.numplates.nomera3.modules.feed.domain.mapper.toPost
import com.numplates.nomera3.modules.feed.ui.adapter.MeeraContentActionBar
import com.numplates.nomera3.modules.feed.ui.entity.DestinationOriginEnum
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.util.PostMediaDownloadControllerUtil
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
import com.numplates.nomera3.modules.tags.data.entity.SpanDataClickType
import com.numplates.nomera3.modules.viewvideo.presentation.data.ViewVideoHeaderEvent
import com.numplates.nomera3.modules.viewvideo.presentation.data.ViewVideoInitialData
import com.numplates.nomera3.modules.viewvideo.presentation.events.ViewVideoItemUIEvent
import com.numplates.nomera3.modules.viewvideo.presentation.events.ViewVideoItemUserEvent
import com.numplates.nomera3.modules.viewvideo.presentation.exoplayer.ViewVideoExoPlayerManager
import com.numplates.nomera3.modules.viewvideo.presentation.mapper.toViewVideoHeader
import com.numplates.nomera3.modules.viewvideo.presentation.viewcontroller.ViewVideoHideUiSwipeController
import com.numplates.nomera3.modules.viewvideo.presentation.viewcontroller.ViewVideoPlaybackController
import com.numplates.nomera3.modules.viewvideo.presentation.viewcontroller.ViewVideoPlayerInfoModel
import com.numplates.nomera3.modules.viewvideo.presentation.viewcontroller.ViewVideoSeekController
import com.numplates.nomera3.modules.viewvideo.presentation.viewcontroller.ViewVideoZoomController
import com.numplates.nomera3.modules.viewvideo.presentation.viewcontroller.toPlayerInfo
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.utils.spanTagsTextInVideoPosts
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraMenuBottomSheet
import com.numplates.nomera3.presentation.view.utils.sharedialog.IOnSharePost
import com.numplates.nomera3.presentation.view.utils.sharedialog.MeeraShareBottomSheetData
import com.numplates.nomera3.presentation.view.utils.sharedialog.MeeraShareSheet
import com.numplates.nomera3.presentation.view.utils.sharedialog.SharePostBottomSheet
import timber.log.Timber

const val ARG_VIEW_VIDEO_POST_ID = "ARG_VIEW_VIDEO_POST_ID"
const val ARG_VIEW_VIDEO_POST = "ARG_VIEW_VIDEO_POST"
const val ARG_VIEW_VIDEO_DATA = "ARG_VIEW_VIDEO_DATA"

private const val VIEW_VIDEO_MENU_TAG = "VIEW_VIDEO_MENU_TAG"
private const val LOADER_SHOW_DELAY = 1_000L
private const val UPDATE_PLAYBACK_POSITION_PERIOD = 50L
private const val MINIMUM_VIDEO_DURATION_FOR_TIMELINE_VISIBILITY = 3_000L

class MeeraViewVideoFragment :
    MeeraBaseDialogFragment(
        layout = R.layout.meera_fragment_view_video,
        behaviourConfigState = ScreenBehaviourState.FullScreenMoment
    ),
    BasePermission by BasePermissionDelegate() {

    override val containerId: Int
        get() = R.id.fragment_first_container_view

    private val binding by viewBinding(MeeraFragmentViewVideoBinding::bind)

    private val viewModel by viewModels<ViewVideoItemViewModel>(
        factoryProducer = { App.component.getViewModelFactory() }
    )

    private var gestureDetector: ViewVideoGestureDetector? = null
    private var viewVideoPlaybackController: ViewVideoPlaybackController? = null
    private var seekVideoController: ViewVideoSeekController? = null
    private var zoomVideoController: ViewVideoZoomController? = null
    private var hideSwipeVideoController: ViewVideoHideUiSwipeController? = null
    private var exoPlayerManager: ViewVideoExoPlayerManager? = null
    private var postLoaderController: PostMediaDownloadControllerUtil? = null

    private val actionBarListener = ViewVideoActionBarListener()

    private var post: PostUIEntity? = null
    private var videoInitialData: ViewVideoInitialData? = null
    private var isVolumeEnabled: Boolean = true
    private var isVideoNeedToPlay: Boolean = true
    private var postOrigin: DestinationOriginEnum? = null
    private var loaderHandler: Handler? = null
    private var isCommonLongTapActive = false
    private var needToShowRepost = true

    private var doubleTapHandler: Handler? = null
    private var playbackPositionHandler: Handler? = null
    private var updatePlaybackPositionRunnable = Runnable { updatePlaybackPositionActions() }

    private var tryResumePlaybackHandler: Handler? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            videoInitialData = it.getSerializable(ARG_VIEW_VIDEO_DATA) as? ViewVideoInitialData
        }
        initData()
        initPermissionDelegate(requireActivity(), viewLifecycleOwner)
        initHandlers()
        initTimelineViews()
        initGestureDetector()
        initSeekVideoController()
        initZoomVideoController()
        initHideUiSwipeVideoController()
        initPostDownloadController()
        initPlayer()
        initVolumeHandler()
        initClickListeners()
        initActionBar()
        initStatusBar()
        initObservers()
        enableDisplayAlwaysOn()
        initBackPressedCallback()
    }

    private fun initData() {
        arguments?.let {
            val postId = it.getLong(ARG_VIEW_VIDEO_POST_ID)
            val post: PostUIEntity? = it.getParcelable(ARG_VIEW_VIDEO_POST)
            postOrigin = it.getSerializable(IArgContainer.ARG_POST_ORIGIN) as? DestinationOriginEnum
            needToShowRepost = it.getBoolean(IArgContainer.ARG_NEED_TO_REPOST, true)
            viewModel.setVideoPostData(
                postId = postId,
                post = post,
                postOrigin = postOrigin,
                isVolumeEnabled = isVolumeEnabled
            )
        } ?: run {
            findNavController().popBackStack()
        }
    }

    override fun onStart() {
        super.onStart()
        onFragmentResumed()
    }

    override fun onResume() {
        super.onResume()
//        if ((requireActivity()).isCurrentFragmentOnTop(this)) { //todo FIX
//            onFragmentResumed()
//        }
    }


//    override fun onReturnTransitionFragment() { todo FIX
//        super.onReturnTransitionFragment()
//        viewVideoPlaybackController?.resume()
//    }

    override fun onPause() {
        super.onPause()
        viewModel.onUserEvent(ViewVideoItemUserEvent.OnPauseFragment)
    }

//    override fun onStartAnimationTransitionFragment() {
//        super.onStartAnimationTransitionFragment()
//        viewVideoPlaybackController?.pause()
//    }

//    override fun onStopFragment() {
//        super.onStopFragment()
//        onFragmentPaused()
//    }

    override fun onStop() {
        super.onStop()
        onFragmentPaused()
        disableDisplayAlwaysOnDisable()
        saveVideoPlaybackPosition()
        viewVideoPlaybackController?.onFragmentStopped()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.clearPostId()
        disableDisplayAlwaysOnDisable()
        loaderHandler?.removeCallbacksAndMessages(null)
        stopPositionUpdate()
        stopDoubleTapHandler()
        exoPlayerManager?.releasePlayer()
        exoPlayerManager = null
    }

    private fun initStatusBar() {
        binding.vStatusBar.updateLayoutParams {
            height = context.getStatusBarHeight()
        }
    }

    private fun initBackPressedCallback() {
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().popBackStack()
        }
    }

    private fun onFragmentResumed() {
        viewModel.onUserEvent(ViewVideoItemUserEvent.OnShareOutsideOpened(false))
        viewVideoPlaybackController?.onFragmentResumed(videoInitialData?.position ?: 0)
        removeSeekListener()
        addSeekListener()
        clearInitialPlayerPosition()
    }

    private fun onFragmentPaused() {
        disableDisplayAlwaysOnDisable()
        stopDoubleTapHandler()
        viewVideoPlaybackController?.onFragmentPaused(/*isPlayerAttached = if we were not scrolled offscreen in viewpager*/)
        removeSeekListener()
    }

    private fun stopPositionUpdate() = playbackPositionHandler?.removeCallbacksAndMessages(null)
    private fun stopDoubleTapHandler() = doubleTapHandler?.removeCallbacksAndMessages(null)

    private fun addSeekListener() {
        val seekListener = seekVideoController?.providePlayerListenerInstance()
        if (seekListener != null) viewVideoPlaybackController?.addPlayerListener(seekListener)
    }

    private fun removeSeekListener() {
        val seekListener = seekVideoController?.providePlayerListenerInstance()
        if (seekListener != null) viewVideoPlaybackController?.removePlayerListener(seekListener)
    }

    private fun saveVideoPlaybackPosition() {
        val videoPlaybackPosition = viewVideoPlaybackController?.getCurrentPosition()
        viewModel.saveLastVideoPlaybackPosition(videoPlaybackPosition)
    }

    private fun initHandlers() {
        loaderHandler = Handler(Looper.getMainLooper())
        playbackPositionHandler = Handler(Looper.getMainLooper())
        tryResumePlaybackHandler = Handler(Looper.getMainLooper())
        doubleTapHandler = Handler(Looper.getMainLooper())
    }

    private fun initGestureDetector() {
        val gestureDetector = ViewVideoGestureDetector(
            context = requireContext(),
            doubleTapHandler = doubleTapHandler,
            timeBar = binding.dtbViewVideoTimeBarSmall,
            listener = object : ViewVideoGestureDetector.Listener {
                override fun onTap() = Unit
                override fun onTapInsideTimeBar() = Unit

                override fun onTapReleased() {
                    checkVideoAvailableAction(action = { resumePlayback() })
                }

                override fun onLongTap() {
                    checkVideoAvailableAction(action = {
                        isCommonLongTapActive = true
                        pausePlayback()
                        binding.apply {
                            vgViewVideoToolbarContainer.gone()
                            vgBottomContainer.gone()
                            vBottomGradient.gone()
                            ivViewVideoPlayIndicator.gone()
                        }
                    })
                }

                override fun onLongTapTimeBar(tapPoint: Point) {
                    checkVideoAvailableAction(action = {
                        pausePlayback()
                        seekVideoController?.show(tapPoint)
                        seekControllerToPosition(tapPoint.x)
                        binding.vgViewVideoToolbarContainer.gone()
                        binding.vgBottomContainer.gone()
                    })
                }

                override fun onDoubleTap() = Unit

                override fun seekTo(positionX: Int) {
                    checkVideoAvailableAction(action = {
                        seekControllerToPosition(positionX)
                    })
                }

                override fun onLongTapReleased() {
                    checkVideoAvailableAction(action = {
                        isCommonLongTapActive = false
                        seekVideoController?.hide()
                        binding.apply {
                            vgViewVideoToolbarContainer.visible()
                            vgBottomContainer.visible()
                            vBottomGradient.visible()
                        }
                        showPlayIfNeeded()
                        val currentVideoPosition = viewVideoPlaybackController?.getLastPosition()
                        resumePlayback(currentVideoPosition)
                    })
                }

                override fun onScaleBegin(focusX: Float, focusY: Float) {
                    checkVideoAvailableAction(action = {
                        resumePlayback()
                        zoomVideoController?.onScaleStart(focusX, focusY)
                        binding.apply {
                            vgViewVideoToolbarContainer.gone()
                            vgBottomContainer.gone()
                            vBottomGradient.gone()
                            ivViewVideoPlayIndicator.gone()
                        }
                    })
                }

                override fun onScale(scale: Float) {
                    checkVideoAvailableAction(action = { zoomVideoController?.onScale(scale) })
                }

                override fun onScaleEnd() {
                    checkVideoAvailableAction(action = {
                        resumePlayback()
                        zoomVideoController?.onScaleEnd()
                        binding.apply {
                            vgViewVideoToolbarContainer.visible()
                            vgBottomContainer.visible()
                            vBottomGradient.visible()
                        }
                        showPlayIfNeeded()
                    })
                }

                override fun onHorizontalSwipe(distanceX: Float) = Unit

                override fun onHorizontalSwipeEnded() {
                    checkVideoAvailableAction(action = {
                        seekVideoController?.hide()
                        binding.apply {
                            vgViewVideoToolbarContainer.visible()
                            vgBottomContainer.visible()
                            vBottomGradient.visible()
                        }
                        val currentVideoPosition = binding.dtbViewVideoTimeBar.getPosition()
                        resumePlayback(currentVideoPosition)
                    })
                }

                override fun onHorizontalFling(velocityX: Float) {
                    checkVideoAvailableAction(action = {
                        seekVideoController?.hide()
                        binding.apply {
                            vgViewVideoToolbarContainer.visible()
                            vgBottomContainer.visible()
                            vBottomGradient.visible()
                        }
                        val currentVideoPosition = binding.dtbViewVideoTimeBar.getPosition()
                        resumePlayback(currentVideoPosition)
                    })
                }

                override fun onHorizontalSwipeTimeBar(tapPoint: Point) {
                    checkVideoAvailableAction(action = {
                        pausePlayback()
                        seekVideoController?.show(tapPoint)
                        binding.vgViewVideoToolbarContainer.gone()
                        binding.vgBottomContainer.gone()
                    })
                }

                override fun onVerticalFling(velocityY: Float) {
                    resumePlayback()
                    hideSwipeVideoController?.onVerticalFling()
                }

                private fun pausePlayback() = viewVideoPlaybackController?.pause()

                private fun resumePlayback(position: Long? = null) = viewVideoPlaybackController?.resume(position)
            })
        this.gestureDetector = gestureDetector
        binding?.apply {
            vgViewVideoGestures.setOnTouchListener { v, event -> gestureDetector.onTouchEvent(v, event) }
        }
    }

    private fun seekControllerToPosition(positionX: Int) {
        if (isCommonLongTapActive) return
        viewVideoPlaybackController?.seekToX(positionX)
    }

    private fun initSeekVideoController() {
        binding?.apply {
            seekVideoController = ViewVideoSeekController(
                timeDisplayView = tvViewVideoTime,
                timeBar = dtbViewVideoTimeBar,
                smallTimeBar = dtbViewVideoTimeBarSmall
            )
        }
    }

    private fun initZoomVideoController() {
        binding?.apply {
            zoomVideoController = ViewVideoZoomController(pvViewVideoPlayer)
        }
    }

    private fun initHideUiSwipeVideoController() {
        binding?.apply {
            hideSwipeVideoController = ViewVideoHideUiSwipeController(
                toolbarContainer = vgViewVideoToolbarContainer,
                swipeContainer = vgBottomContainer,
                bottomShadow = vBottomGradient,
                onCloseAction = { findNavController().popBackStack() }
            )
        }
    }

    private fun initPostDownloadController() {
        binding?.apply {
            postLoaderController = PostMediaDownloadControllerUtil(plvVideoPostLoader) {
                viewModel.onUserEvent(ViewVideoItemUserEvent.OnCancelDownloadClicked)
            }
        }
    }

    private fun initPlayer() {
        binding?.apply {
            val playbackListener = object : ExoPlayerPlaybackStateListener {
                override fun onLoading() {
                    showLoaderForPlaybackState()
                }

                override fun onPause() {
                    stopPositionUpdate()
                }

                override fun onPlaying() {
                    onPlayingActions()
                }

                override fun onError() {
                    viewVideoPlaybackController?.tryResume()
                }
            }

            val manager = exoPlayerManager ?: ViewVideoExoPlayerManager(root.context, playbackListener)
                .also { exoPlayerManager = it }

            viewVideoPlaybackController = ViewVideoPlaybackController(
                playerView = pvViewVideoPlayer,
                playerManager = manager,
                videoTimeBar = dtbViewVideoTimeBar,
                tryResumePlaybackHandler = tryResumePlaybackHandler,
                onSeekToMs = { currentPosition -> seekVideoController?.updateProgress(currentPosition) },
                onResume = { enableDisplayAlwaysOn() }
            )
        }
    }

    private fun initVolumeHandler() {
        binding?.vgViewVideoGestures?.setOnClickListener {
            onPlayingStateChanged(isVideoNeedToPlay.not())
            viewModel.toggleIsVideoStatePlaying()
        }
    }

    private fun initClickListeners() {
        binding?.apply {
            ivViewVideoMenu.setThrottledClickListener {
                viewModel.onUserEvent(ViewVideoItemUserEvent.OnOpenMenuClicked)
            }
            ivBackToolbar.setThrottledClickListener {
                findNavController().popBackStack()
            }
            vvhvViewVideoHeader.setEventListener { event: ViewVideoHeaderEvent ->
                val userId = post?.user?.userId ?: return@setEventListener
                when (event) {
                    ViewVideoHeaderEvent.FollowClicked -> {
                        needAuthToNavigate {
                            viewModel.onUserEvent(
                                ViewVideoItemUserEvent.OnSubscribeToUserClicked(
                                    userId
                                )
                            )
                        }
                    }

                    ViewVideoHeaderEvent.UnfollowClicked -> {
                        showUnsubscribeDialog(userId)
                    }

                    ViewVideoHeaderEvent.UserClicked -> {
                        openUserFragment(userId)
                    }
                }
            }
            val expandedColor = ContextCompat.getColor(vgViewVideoGestures.context, R.color.ui_black_40)
            tvExpandableTextVideo.addExpandedListener { isExpanded ->
                gestureDetector?.setTouchEventsEnabled(isExpanded.not())
                val baseStartColor = if (isExpanded) Color.TRANSPARENT else expandedColor
                val baseEndColor = if (isExpanded) expandedColor else Color.TRANSPARENT
                val currentColor = (vgViewVideoGestures.background as? ColorDrawable)?.color
                vBottomGradient.isVisible = !isExpanded
                ObjectAnimator.ofArgb(
                    vgViewVideoGestures,
                    "backgroundColor",
                    currentColor ?: baseStartColor,
                    baseEndColor
                ).setDuration(150L).start()
            }
        }
    }

    private fun initActionBar(post: PostUIEntity? = null, isCommentsShow: Boolean? = null) {
        val actionBarParams = post?.toMeeraContentActionBarParams()?.copy(
            isVideo = true,
            commentsIsHide = !(isCommentsShow ?: true)
        ) ?: getDefaultActionBarParams()
        binding?.cabViewVideoActionBar?.init(
            params = actionBarParams,
            isNeedToShowRepost = needToShowRepost,
            isNeedCommentVibrate = false,
            callbackListener = actionBarListener
        )
    }

    private fun initTimelineViews() {
        videoInitialData?.let { data ->
            binding?.apply {
                updateTimelineVisibility(data.duration)

                dtbViewVideoTimeBarSmall.setDuration(data.duration)
                dtbViewVideoTimeBarSmall.setPosition(data.position)

                dtbViewVideoTimeBar.setDuration(data.duration)
                dtbViewVideoTimeBar.setPosition(data.position)
            }
        }
    }

    private fun enableDisplayAlwaysOn() = (requireActivity() as MeeraAct).keepScreenOnEnable()
    private fun disableDisplayAlwaysOnDisable() = (requireActivity() as MeeraAct).keepScreenOnDisable()

    private fun updateTimelineVisibility(duration: Long?) {
        if (duration == null) return
        val isPostVideoAvailable = post?.isPostVideoAvailable() ?: true
        binding?.dtbViewVideoTimeBarSmall?.isVisible =
            duration >= MINIMUM_VIDEO_DURATION_FOR_TIMELINE_VISIBILITY && isPostVideoAvailable
    }

    private fun updateActionBar(post: PostUIEntity, reactionHolderViewId: MeeraContentActionBar.ReactionHolderViewId) {
        binding?.cabViewVideoActionBar?.update(
            post.toMeeraContentActionBarParams().copy(isVideo = true),
            reactionHolderViewId
        )
        this.post = post
        viewVideoPlaybackController?.resume()
    }

    private fun showPlayIfNeeded() {
        if (!isVideoNeedToPlay) binding?.ivViewVideoPlayIndicator?.visible()
    }

    private fun onPlayingActions() {
        checkVideoAvailableAction(
            action = { onPlayingConfirmActions() },
            stopAction = { handleStopPlayer() }
        )
    }

    private fun onPlayingConfirmActions() {
        viewVideoPlaybackController?.markAsPlayed()
        hideLoaderForPlaybackState()

        val videoDuration = viewVideoPlaybackController?.getDuration()
        updateTimelineVisibility(duration = videoDuration)

        updatePlaybackPosition()
    }

    private fun checkVideoAvailableAction(action: () -> Unit, stopAction: () -> Unit = {}) {
        val isPostVideoAvailable = post?.isPostVideoAvailable() ?: true
        if (isPostVideoAvailable) {
            action.invoke()
        } else {
            stopAction.invoke()
        }
    }

    private fun showUnsubscribeDialog(userId: Long) {
        MeeraConfirmDialogBuilder()
            .setHeader(getString(R.string.user_info_unsub_dialog_header))
            .setDescription(getString(R.string.unsubscribe_desc))
            .setTopBtnText(getString(R.string.unsubscribe))
            .setBottomBtnText(getString(R.string.general_cancel))
            .setCancelable(true)
            .setTopClickListener {
                viewModel.onUserEvent(ViewVideoItemUserEvent.OnUnsubscribeFromUserClicked(userId))
            }
            .show(childFragmentManager)
    }

    private fun initObservers() {
        viewModel.videoScreenState.observe(viewLifecycleOwner) { state ->
            when (state) {
                VideoScreenState.Loading -> {
                    gestureDetector?.setTouchEventsEnabled(false)
                    showSkeleton()
                    initActionBar(null)
                }

                is VideoScreenState.VideoInfo -> {
                    gestureDetector?.setTouchEventsEnabled(true)
                    showContent()
                    viewVideoPlaybackController?.setVideoData(state.toPlayerInfo())
                    setViewVideoHeader(state.post)
                    setViewVideoText(state.post)
                    initActionBar(state.post, state.isCommentsShow)
                    isVolumeEnabled = state.isVolumeEnabled
                    post = state.post
                    setVideoStatePlaying(state.isVideoNeedToPlay)
                }

                is VideoScreenState.Unavailable -> setContentUnavailable(state.post)
            }
        }
        viewModel.videoScreenUIEvents.observe(viewLifecycleOwner) { event ->
            when (event) {
                is ViewVideoItemUIEvent.UpdateVideoReaction -> {
                    updateActionBar(
                        post = event.post,
                        reactionHolderViewId = event.reactionUpdate.reactionSource.reactionHolderViewId
                    )
                }

                is ViewVideoItemUIEvent.OpenMenu -> showMenu(event.menuItems)
                is ViewVideoItemUIEvent.OpenShareMenu -> {
                    checkAppRedesigned(
                        isRedesigned = {
                            meeraShowShareMenu(event.post)
                        },
                        isNotRedesigned = {
                            showShareMenu(event.post)
                        }
                    )
                }

                ViewVideoItemUIEvent.AddedPostComplaint -> onAddedPostComplaint()
                ViewVideoItemUIEvent.HiddenUserRoad -> onHiddenUserRoad()
                ViewVideoItemUIEvent.OnPostDeleted -> onDeletedPost()
                ViewVideoItemUIEvent.SubscribedToPost -> onSubscribedToPost()
                ViewVideoItemUIEvent.UnsubscribedFromPost -> onUnsubscribedFromPost()
                is ViewVideoItemUIEvent.ShowErrorMessage -> showCommonError(getText(event.messageResId), requireView())
                is ViewVideoItemUIEvent.UpdateLoadingState -> onUpdateLoadingState(event)
                ViewVideoItemUIEvent.PostLinkCopied -> onPostLinkCopied()
                is ViewVideoItemUIEvent.UpdatePostInfo -> updatePostInfo(event.post)
                ViewVideoItemUIEvent.OnFragmentPaused -> onFragmentPaused()
            }
        }
    }

    private fun setVideoStatePlaying(isNeedToPlay: Boolean) {
        if (isVideoNeedToPlay == isNeedToPlay) return
        isVideoNeedToPlay = isNeedToPlay
        if (isNeedToPlay) {
            viewVideoPlaybackController?.resume()
        } else {
            stopPositionUpdate()
            viewVideoPlaybackController?.pause()
        }
    }

    private fun setContentUnavailable(post: PostUIEntity?) {
        this.post = post
        val info = ViewVideoPlayerInfoModel(isVolumeEnabled = false)
        viewVideoPlaybackController?.setVideoData(info)

        seekVideoController = null
        stopDoubleTapHandler()
        stopPositionUpdate()
        playbackPositionHandler = null

        binding?.vgViewVideoGestures?.setOnClickListener(null)

        binding?.apply {
            incVideoUnavailableLayout.root.visible()
            ivViewVideoMenu.gone()

            cpiViewVideoLoader.goneAnimation()
            vgViewVideoSkeletonBackground.goneAnimation()
            vgViewVideoSkeletonBody.root.gone()

            vvhvViewVideoHeader.gone()
            dtbViewVideoTimeBarSmall.gone()
            dtbViewVideoTimeBar.gone()
            tvViewVideoTime.gone()
            pvViewVideoPlayer.gone()
            rmswViewVideoTextWrapper.gone()
            cabViewVideoActionBar.gone()
        }
    }

    private fun handleStopPlayer() {
        val info = ViewVideoPlayerInfoModel(isVolumeEnabled = false)
        viewVideoPlaybackController?.setVideoData(info)
        viewVideoPlaybackController?.onFragmentStopped()
    }

    private fun updatePostInfo(post: PostUIEntity) {
        this.post = post
        setViewVideoHeader(post)
    }

    private fun updatePlaybackPosition() {
        playbackPositionHandler?.apply {
            removeCallbacksAndMessages(null)
            postDelayed(updatePlaybackPositionRunnable, UPDATE_PLAYBACK_POSITION_PERIOD)
        }
    }

    private fun updatePlaybackPositionActions() {
        val isPostVideoAvailable = post?.isPostVideoAvailable() ?: true
        if (!isPostVideoAvailable) {
            binding?.dtbViewVideoTimeBarSmall?.gone()
            binding?.dtbViewVideoTimeBar?.gone()
            return
        }
        val videoPlaybackPosition = viewVideoPlaybackController?.getCurrentPosition() ?: 0
        binding?.dtbViewVideoTimeBarSmall?.setPosition(videoPlaybackPosition)
        binding?.dtbViewVideoTimeBar?.setPosition(videoPlaybackPosition)
        updatePlaybackPosition()
    }

    private fun showSkeleton() {
        binding?.apply {
            cpiViewVideoLoader.visible()
            vgViewVideoSkeletonBackground.visible()

            pvViewVideoPlayer.gone()
            vvhvViewVideoHeader.gone()
            rmswViewVideoTextWrapper.gone()
        }
    }

    private fun showContent() {
        binding?.apply {
            cpiViewVideoLoader.goneAnimation()
            vgViewVideoSkeletonBackground.goneAnimation()
            vgViewVideoSkeletonBody.root.gone()

            pvViewVideoPlayer.visibleAnimation()
            rmswViewVideoTextWrapper.gone()
        }
    }

    private fun onPlayingStateChanged(isNeedToPlay: Boolean) {
        binding?.ivViewVideoPlayIndicator?.isVisible = !isNeedToPlay
    }

    private fun showMenu(menuItems: List<ViewVideoMenuItems>) {
        needAuthToNavigate {
            viewVideoPlaybackController?.resume()

            val menu = MeeraMenuBottomSheet(context)

            menuItems.forEach { menuItem ->
                menu.addItem(
                    title = getString(menuItem.titleResId),
                    icon = menuItem.iconResId,
                    iconAndTitleColor = menuItem.iconAndTitleColor
                ) {
                    when (menuItem) {
                        is ViewVideoMenuItems.DownloadVideo -> {
                            saveVideo(menuItem.postId, null)
                        }

                        is ViewVideoMenuItems.SubscribeToPost -> {
                            viewModel.onUserEvent(ViewVideoItemUserEvent.OnSubscribeToPost(menuItem.postId))
                        }

                        is ViewVideoMenuItems.UnsubscribeFromPost -> {
                            viewModel.onUserEvent(ViewVideoItemUserEvent.OnUnsubscribeFromPost(menuItem.postId))
                        }

                        is ViewVideoMenuItems.SubscribeToUser -> {
                            viewModel.onUserEvent(
                                ViewVideoItemUserEvent.OnSubscribeToUserClicked(
                                    userId = menuItem.userId,
                                    fromMenu = true
                                )
                            )
                        }

                        is ViewVideoMenuItems.HideUserRoad -> {
                            viewModel.onUserEvent(ViewVideoItemUserEvent.OnHideUserRoad(menuItem.userId))
                        }

                        is ViewVideoMenuItems.AddComplaintPost -> {
                            viewModel.onUserEvent(ViewVideoItemUserEvent.AddComplaintPost(menuItem.postId))
                        }

                        is ViewVideoMenuItems.DeletePost -> {
                            viewModel.onUserEvent(ViewVideoItemUserEvent.OnDeletePost(menuItem.postId))
                        }

                        is ViewVideoMenuItems.SharePost -> {
                            viewModel.onUserEvent(ViewVideoItemUserEvent.OnRepostClick)
                        }

                        is ViewVideoMenuItems.CopyPostLink -> {
                            viewModel.onUserEvent(ViewVideoItemUserEvent.OnCopyPostLink(menuItem.postId))
                        }
                    }
                }
            }

            menu.showWithTag(manager = childFragmentManager, tag = VIEW_VIDEO_MENU_TAG)
        }
    }

    private fun showShareMenu(post: PostUIEntity) {
        needAuthToNavigate {
            viewVideoPlaybackController?.resume()

            SharePostBottomSheet(
                postOrigin = postOrigin,
                post = post.toPost(),
                event = null,
                callback = object : IOnSharePost {
                    override fun onShareFindGroup() {
                        openGroups()
                    }

                    override fun onShareFindFriend() {
                        openSearch()
                    }

                    override fun onShareToGroupSuccess(groupName: String?) {
                        viewModel.onUserEvent(ViewVideoItemUserEvent.OnRepostSuccess(post))
                        showCommonSuccessMessage(
                            getString(R.string.success_repost_to_group, groupName ?: ""),
                            requireView()
                        )
                    }

                    override fun onShareToRoadSuccess() {
                        viewModel.onUserEvent(ViewVideoItemUserEvent.OnRepostSuccess(post))
                        showCommonSuccessMessage(getText(R.string.success_repost_to_own_road), requireView())
                    }

                    override fun onShareToChatSuccess(repostTargetCount: Int) {
                        viewModel.onUserEvent(ViewVideoItemUserEvent.OnRepostSuccess(post, repostTargetCount))
                        showCommonSuccessMessage(getText(R.string.success_repost_to_chat), requireView())
                    }

                    override fun onPostItemUniqnameUserClick(userId: Long?) = openUserFragment(userId)

                    override fun onOpenShareOutside() {
                        viewModel.onUserEvent(ViewVideoItemUserEvent.OnShareOutsideOpened(true))
                    }

                }).show(childFragmentManager)
        }
    }

    private fun meeraShowShareMenu(post: PostUIEntity) {
        needAuthToNavigate {

            viewVideoPlaybackController?.resume()

            MeeraShareSheet().show(
                fm = childFragmentManager,
                data = MeeraShareBottomSheetData(
                    postOrigin = postOrigin,
                    post = post.toPost(),
                    event = null,
                    callback = object : IOnSharePost {
                        override fun onShareFindGroup() {
                            openGroups()
                        }

                        override fun onShareFindFriend() {
                            openSearch()
                        }

                        override fun onShareToGroupSuccess(groupName: String?) {
                            viewModel.onUserEvent(ViewVideoItemUserEvent.OnRepostSuccess(post))
                            showCommonSuccessMessage(
                                getString(R.string.success_repost_to_group, groupName ?: ""),
                                requireView()
                            )
                        }

                        override fun onShareToRoadSuccess() {
                            viewModel.onUserEvent(ViewVideoItemUserEvent.OnRepostSuccess(post))
                            showCommonSuccessMessage(getText(R.string.success_repost_to_own_road), requireView())
                        }

                        override fun onShareToChatSuccess(repostTargetCount: Int) {
                            viewModel.onUserEvent(ViewVideoItemUserEvent.OnRepostSuccess(post, repostTargetCount))
                            showCommonSuccessMessage(getText(R.string.success_repost_to_chat), requireView())
                        }

                        override fun onPostItemUniqnameUserClick(userId: Long?) = openUserFragment(userId)

                        override fun onOpenShareOutside() {
                            viewModel.onUserEvent(ViewVideoItemUserEvent.OnShareOutsideOpened(true))
                        }
                    }
                )
            )

        }
    }

    private fun openSearch() {
        findNavController().safeNavigate(
            resId = R.id.action_meeraPostFragmentV2_to_meeraSearchFragment,
            bundle = Bundle().apply {
                putSerializable(
                    IArgContainer.ARG_FIND_FRIENDS_OPENED_FROM_WHERE,
                    AmplitudeFindFriendsWhereProperty.SHARE
                )
            }
        )
    }

    private fun openGroups() {
        findNavController().safeNavigate(R.id.action_meeraPostFragmentV2_to_meeraCommunitiesListsContainerFragment)
    }

    private fun onUpdateLoadingState(update: ViewVideoItemUIEvent.UpdateLoadingState) {
        post = update.post
        postLoaderController?.setupLoading(update.post.postId, update.loadingInfo)
    }

    private fun onDeletedPost() {
        showCommonSuccessMessage(getText(R.string.post_deleted_success), requireView())
        findNavController().popBackStack()
    }

    private fun showLoaderForPlaybackState() {
        loaderHandler?.removeCallbacksAndMessages(null)
        if (seekVideoController?.isVisible().isTrue() || !isVideoNeedToPlay) return
        loaderHandler?.postDelayed({ binding?.cpiViewVideoLoader?.visible() }, LOADER_SHOW_DELAY)
    }

    private fun hideLoaderForPlaybackState() {
        loaderHandler?.removeCallbacksAndMessages(null)
        binding?.cpiViewVideoLoader?.hide()
    }

    private fun onHiddenUserRoad() {
        showCommonSuccessMessage(getText(R.string.post_author_hide_success), requireView())
        findNavController().popBackStack()
    }

    private fun onAddedPostComplaint() = showCommonSuccessMessage(getText(R.string.road_complaint_send_success), requireView())

    private fun onSubscribedToPost() = showCommonSuccessMessage(getText(R.string.subscribe_post), requireView())

    private fun onUnsubscribedFromPost() = showCommonSuccessMessage(getText(R.string.unsubscribe_post), requireView())

    private fun onPostLinkCopied() {
        showCommonSuccessMessage(getText(R.string.meera_copy_link_success), requireView())
    }

    private fun clearInitialPlayerPosition() {
        arguments?.putLong(ARG_VIEW_VIDEO_DATA, 0)
        videoInitialData = videoInitialData?.copy(position = 0)
    }

    private fun setViewVideoHeader(post: PostUIEntity) {
        binding?.vvhvViewVideoHeader?.setHeaderInfo(post.toViewVideoHeader())
    }

    private fun setViewVideoText(post: PostUIEntity) {
        binding?.tvExpandableTextVideo?.apply {
            if (post.tagSpan != null) {
                spanTagsTextInVideoPosts(
                    context = context,
                    tvText = this,
                    post = post.tagSpan,
                    click = { handleTextTagClick(it) }
                )
            } else {
                text = post.postText
            }
        }
    }

    private fun handleTextTagClick(clickType: SpanDataClickType) {
        when (clickType) {
            is SpanDataClickType.ClickBadWord -> {
                viewModel.onUserEvent(ViewVideoItemUserEvent.OnBadWordClicked(clickType))
            }

            is SpanDataClickType.ClickHashtag -> openHashtagFragment(clickType)
            is SpanDataClickType.ClickUserId -> openUserFragment(clickType.userId)
            is SpanDataClickType.ClickLink -> {
                (requireActivity() as MeeraAct).emitDeeplinkCall(clickType.link)
            }

            else -> Unit
        }
    }

    private fun saveVideo(postId: Long, assetId: String?) {
        setPermissions(
            object : PermissionDelegate.Listener {
                override fun onGranted() {
                    viewModel.onUserEvent(ViewVideoItemUserEvent.OnDownloadVideoClicked(postId, assetId))
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

    private fun openUserFragment(userId: Long?) {
        findNavController().safeNavigate(
            resId = R.id.action_global_userInfoFragment,
            bundle = bundleOf(
                IArgContainer.ARG_USER_ID to userId
            )
        )
    }

    private fun openHashtagFragment(clickType: SpanDataClickType.ClickHashtag) {
        Timber.e("$clickType")
        findNavController().safeNavigate(
            resId = R.id.action_meeraViewVideoFragment_to_meeraHashTagFragment,
            bundle = bundleOf(
                IArgContainer.ARG_HASHTAG to clickType.hashtag
            )
        )
    }

    private inner class ViewVideoActionBarListener : MeeraContentActionBar.Listener {

        private val reactionController by lazy {
            (requireActivity() as ActivityToolsProvider).getMeeraReactionBubbleViewController()
        }

        override fun onReactionBadgeClick() {
            needAuthToNavigateWithResult(SUBSCRIPTION_ROAD_REQUEST_KEY) {
                val post = post ?: return@needAuthToNavigateWithResult
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
            val needAuth = needAuthToNavigateWithResult { viewVideoPlaybackController?.resume() }
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
            val post = post ?: return
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

        override fun onCommentsClick() {
            needAuthToNavigate {
                viewVideoPlaybackController?.resume()
                val post = post ?: return@needAuthToNavigate
                MeeraCommentsBottomSheetFragment.showForPost(post, childFragmentManager, postOrigin)
            }
        }

        override fun onRepostClick() {
            viewModel.onUserEvent(ViewVideoItemUserEvent.OnRepostClick)
        }

        override fun onFlyingAnimationInitialized(flyingReaction: FlyingReaction) = Unit

        override fun onReactionButtonDisabledClick() = Unit
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
}



