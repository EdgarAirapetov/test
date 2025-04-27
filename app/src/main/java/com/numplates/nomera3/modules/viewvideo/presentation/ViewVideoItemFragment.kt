package com.numplates.nomera3.modules.viewvideo.presentation

import android.animation.ObjectAnimator
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.goneAnimation
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.keepScreenOnDisable
import com.meera.core.extensions.keepScreenOnEnable
import com.meera.core.extensions.returnReadExternalStoragePermissionAfter33
import com.meera.core.extensions.returnWriteExternalStoragePermissionAfter33
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.core.extensions.visibleAnimation
import com.meera.core.permission.PermissionDelegate
import com.meera.core.utils.checkAppRedesigned
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentViewVideoItemBinding
import com.numplates.nomera3.modules.auth.util.needAuth
import com.numplates.nomera3.modules.auth.util.needAuthAndReturnStatus
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.createAmplitudeReactionsParams
import com.numplates.nomera3.modules.comments.bottomsheet.presentation.MeeraCommentsBottomSheetFragment
import com.numplates.nomera3.modules.common.ActivityToolsProvider
import com.numplates.nomera3.modules.exoplayer.presentation.ExoPlayerPlaybackStateListener
import com.numplates.nomera3.modules.feed.ui.adapter.ContentActionBar
import com.numplates.nomera3.modules.feed.ui.entity.DestinationOriginEnum
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.util.PostMediaDownloadControllerUtil
import com.numplates.nomera3.modules.hashtag.ui.fragment.HashtagFragment
import com.numplates.nomera3.modules.reaction.ui.custom.FlyingReaction
import com.numplates.nomera3.modules.reaction.ui.data.ReactionSource
import com.numplates.nomera3.modules.reaction.ui.getDefaultReactionContainer
import com.numplates.nomera3.modules.reaction.ui.mapper.toContentActionBarParams
import com.numplates.nomera3.modules.reaction.ui.util.reactionCount
import com.numplates.nomera3.modules.reactionStatistics.ui.MeeraReactionsStatisticsBottomSheetFragment
import com.numplates.nomera3.modules.reactionStatistics.ui.ReactionsEntityType
import com.numplates.nomera3.modules.reactionStatistics.ui.ReactionsStatisticsBottomSheetFragment
import com.numplates.nomera3.modules.tags.data.entity.SpanDataClickType
import com.numplates.nomera3.modules.viewvideo.presentation.data.ViewVideoHeaderEvent
import com.numplates.nomera3.modules.viewvideo.presentation.data.ViewVideoInitialData
import com.numplates.nomera3.modules.viewvideo.presentation.events.ViewVideoItemUserEvent
import com.numplates.nomera3.modules.viewvideo.presentation.exoplayer.ViewVideoExoPlayerManager
import com.numplates.nomera3.modules.viewvideo.presentation.mapper.toViewVideoHeader
import com.numplates.nomera3.modules.viewvideo.presentation.viewcontroller.ViewVideoHideUiSwipeController
import com.numplates.nomera3.modules.viewvideo.presentation.viewcontroller.ViewVideoPlaybackController
import com.numplates.nomera3.modules.viewvideo.presentation.viewcontroller.ViewVideoPlayerInfoModel
import com.numplates.nomera3.modules.viewvideo.presentation.viewcontroller.ViewVideoSeekController
import com.numplates.nomera3.modules.viewvideo.presentation.viewcontroller.ViewVideoZoomController
import com.numplates.nomera3.modules.viewvideo.presentation.viewcontroller.toPlayerInfo
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.utils.spanTagsTextInVideoPosts
import com.numplates.nomera3.presentation.view.fragments.UserInfoFragment
import com.numplates.nomera3.presentation.view.fragments.dialogs.ConfirmDialogBuilder
import com.numplates.nomera3.presentation.view.ui.bottomMenu.ReactionsStatisticBottomMenu
import com.numplates.nomera3.presentation.view.utils.NToast
import timber.log.Timber

//private const val VIEW_VIDEO_MENU_TAG = "VIEW_VIDEO_MENU_TAG"
private const val LOADER_SHOW_DELAY = 1_000L
private const val UPDATE_PLAYBACK_POSITION_PERIOD = 50L
private const val MINIMUM_VIDEO_DURATION_FOR_TIMELINE_VISIBILITY = 3_000L

class ViewVideoItemFragment : BaseFragmentNew<FragmentViewVideoItemBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentViewVideoItemBinding
        get() = FragmentViewVideoItemBinding::inflate

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            val postId = it.getLong(ARG_VIEW_VIDEO_POST_ID)
            val post: PostUIEntity? = it.getParcelable(ARG_VIEW_VIDEO_POST)
            postOrigin = it.getSerializable(IArgContainer.ARG_POST_ORIGIN) as? DestinationOriginEnum
            needToShowRepost = it.getBoolean(IArgContainer.ARG_NEED_TO_REPOST, true)
            viewModel.setVideoPostData(postId = postId, post = post, postOrigin = postOrigin, isVolumeEnabled = isVolumeEnabled)
        } ?: run {
            requireActivity().onBackPressed()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            videoInitialData = it.getSerializable(ARG_VIEW_VIDEO_DATA) as? ViewVideoInitialData
        }
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
        initStatusBarViewHeight<ConstraintLayout.LayoutParams>(binding?.vStatusBar)
        initObservers()
        enableDisplayAlwaysOn()
    }

    override fun onStartFragment() {
        super.onStartFragment()
        onFragmentResumed()
    }

    override fun onResume() {
        super.onResume()
        if (act.isCurrentFragmentOnTop(this)) {
            onFragmentResumed()
        }
    }

    override fun onReturnTransitionFragment() {
        super.onReturnTransitionFragment()
        viewVideoPlaybackController?.resume()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onUserEvent(ViewVideoItemUserEvent.OnPauseFragment)
    }

    override fun onStartAnimationTransitionFragment() {
        super.onStartAnimationTransitionFragment()
        viewVideoPlaybackController?.pause()
    }

    override fun onStopFragment() {
        super.onStopFragment()
        onFragmentPaused()
    }

    override fun onStop() {
        super.onStop()
        disableDisplayAlwaysOnDisable()
        saveVideoPlaybackPosition()
        viewVideoPlaybackController?.onFragmentStopped()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disableDisplayAlwaysOnDisable()
        loaderHandler?.removeCallbacksAndMessages(null)
        stopPositionUpdate()
        stopDoubleTapHandler()
        exoPlayerManager?.releasePlayer()
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
            timeBar = binding?.dtbViewVideoTimeBarSmall,
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
                        binding?.apply {
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
                        binding?.vgViewVideoToolbarContainer?.gone()
                        binding?.vgBottomContainer?.gone()
                    })
                }

                override fun onDoubleTap() = Unit

                override fun seekTo(positionX: Int) {
                    checkVideoAvailableAction(action = {
                        if (isCommonLongTapActive) return@checkVideoAvailableAction
                        viewVideoPlaybackController?.seekToX(positionX)
                    })
                }

                override fun onLongTapReleased() {
                    checkVideoAvailableAction(action = {
                        isCommonLongTapActive = false
                        seekVideoController?.hide()
                        binding?.apply {
                            vgViewVideoToolbarContainer.visible()
                            vgBottomContainer.visible()
                            vBottomGradient.visible()
                        }
                        showPlayIfNeeded()
                        val currentVideoPosition = viewVideoPlaybackController?.getCurrentPosition()
                        resumePlayback(currentVideoPosition)
                    })
                }

                override fun onScaleBegin(focusX: Float, focusY: Float) {
                    checkVideoAvailableAction(action = {
                        resumePlayback()
                        zoomVideoController?.onScaleStart(focusX, focusY)
                        binding?.apply {
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
                        binding?.apply {
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
                        binding?.apply {
                            vgViewVideoToolbarContainer.visible()
                            vgBottomContainer.visible()
                            vBottomGradient.visible()
                        }
                        val currentVideoPosition = binding?.dtbViewVideoTimeBar?.getPosition()
                        resumePlayback(currentVideoPosition)
                    })
                }

                override fun onHorizontalFling(velocityX: Float) {
                    checkVideoAvailableAction(action = {
                        seekVideoController?.hide()
                        binding?.apply {
                            vgViewVideoToolbarContainer.visible()
                            vgBottomContainer.visible()
                            vBottomGradient.visible()
                        }
                        val currentVideoPosition = binding?.dtbViewVideoTimeBar?.getPosition()
                        resumePlayback(currentVideoPosition)
                    })
                }

                override fun onHorizontalSwipeTimeBar(tapPoint: Point) {
                    checkVideoAvailableAction(action = {
                        pausePlayback()
                        seekVideoController?.show(tapPoint)
                        binding?.vgViewVideoToolbarContainer?.gone()
                        binding?.vgBottomContainer?.gone()
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
                onCloseAction = { act.onBackPressed() }
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
                override fun onLoading() { showLoaderForPlaybackState() }
                override fun onPause() { stopPositionUpdate() }
                override fun onPlaying() { onPlayingActions() }
                override fun onError() { viewVideoPlaybackController?.tryResume() }
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
                act.onBackPressed()
            }
            vvhvViewVideoHeader.setEventListener { event: ViewVideoHeaderEvent ->
                val userId = post?.user?.userId ?: return@setEventListener
                when (event) {
                    ViewVideoHeaderEvent.FollowClicked -> {
                        needAuth { viewModel.onUserEvent(ViewVideoItemUserEvent.OnSubscribeToUserClicked(userId)) }
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
        val actionBarParams = post?.toContentActionBarParams()?.copy(
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

    private fun enableDisplayAlwaysOn() = act.keepScreenOnEnable()
    private fun disableDisplayAlwaysOnDisable() = act.keepScreenOnDisable()

    private fun updateTimelineVisibility(duration: Long?) {
        if (duration == null) return
        val isPostVideoAvailable = post?.isPostVideoAvailable() ?: true
        binding?.dtbViewVideoTimeBarSmall?.isVisible =
            duration >= MINIMUM_VIDEO_DURATION_FOR_TIMELINE_VISIBILITY && isPostVideoAvailable
    }

//    private fun updateActionBar(post: PostUIEntity, reactionHolderViewId: ContentActionBar.ReactionHolderViewId) {
//        binding?.cabViewVideoActionBar?.update(
//            post.toContentActionBarParams().copy(isVideo = true),
//            reactionHolderViewId
//        )
//        this.post = post
//        viewVideoPlaybackController?.resume()
//    }

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
        ConfirmDialogBuilder()
            .setHeader(getString(R.string.user_info_unsub_dialog_header))
            .setDescription(getString(R.string.user_info_unsub_dialog_description))
            .setLeftBtnText(getString(R.string.user_info_unsub_dialog_close))
            .setRightBtnText(getString(R.string.user_info_unsub_dialog_action))
            .setRightClickListener {
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
//            when (event) {
//                is ViewVideoItemUIEvent.UpdateVideoReaction -> {
//                    updateActionBar(
//                        post = event.post,
//                        reactionHolderViewId = event.reactionUpdate.reactionSource.reactionHolderViewId
//                    )
//                }
//                is ViewVideoItemUIEvent.OpenMenu -> showMenu(event.menuItems)
//                is ViewVideoItemUIEvent.OpenShareMenu -> {
//                    checkAppRedesigned(
//                        isRedesigned = {
//                            meeraShowShareMenu(event.post)
//                        },
//                        isNotRedesigned = {
//                            showShareMenu(event.post)
//                        }
//                    )
//                }
//                ViewVideoItemUIEvent.AddedPostComplaint -> onAddedPostComplaint()
//                ViewVideoItemUIEvent.HiddenUserRoad -> onHiddenUserRoad()
//                ViewVideoItemUIEvent.OnPostDeleted -> onDeletedPost()
//                ViewVideoItemUIEvent.SubscribedToPost -> onSubscribedToPost()
//                ViewVideoItemUIEvent.UnsubscribedFromPost -> onUnsubscribedFromPost()
//                is ViewVideoItemUIEvent.ShowErrorMessage -> showCommonError(event.messageResId)
//                is ViewVideoItemUIEvent.UpdateLoadingState -> onUpdateLoadingState(event)
//                ViewVideoItemUIEvent.PostLinkCopied -> onPostLinkCopied()
//                is ViewVideoItemUIEvent.UpdatePostInfo -> updatePostInfo(event.post)
//                ViewVideoItemUIEvent.OnFragmentPaused -> onFragmentPaused()
//            }
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

//    private fun updatePostInfo(post: PostUIEntity) {
//        this.post = post
//        setViewVideoHeader(post)
//    }

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

//    private fun showMenu(menuItems: List<ViewVideoMenuItems>) {
//        needAuth {
//            viewVideoPlaybackController?.resume()
//
//            val menu = MeeraMenuBottomSheet(context)
//
//            menuItems.forEach { menuItem ->
//                menu.addItem(getString(menuItem.titleResId), menuItem.iconResId) {
//                    when (menuItem) {
//                        is ViewVideoMenuItems.DownloadVideo -> {
//                            saveVideo(menuItem.postId, null)
//                        }
//                        is ViewVideoMenuItems.SubscribeToPost -> {
//                            viewModel.onUserEvent(ViewVideoItemUserEvent.OnSubscribeToPost(menuItem.postId))
//                        }
//                        is ViewVideoMenuItems.UnsubscribeFromPost -> {
//                            viewModel.onUserEvent(ViewVideoItemUserEvent.OnUnsubscribeFromPost(menuItem.postId))
//                        }
//                        is ViewVideoMenuItems.SubscribeToUser -> {
//                            viewModel.onUserEvent(ViewVideoItemUserEvent.OnSubscribeToUserClicked(userId = menuItem.userId, fromMenu = true))
//                        }
//                        is ViewVideoMenuItems.HideUserRoad -> {
//                            viewModel.onUserEvent(ViewVideoItemUserEvent.OnHideUserRoad(menuItem.userId))
//                        }
//                        is ViewVideoMenuItems.AddComplaintPost -> {
//                            viewModel.onUserEvent(ViewVideoItemUserEvent.AddComplaintPost(menuItem.postId))
//                        }
//                        is ViewVideoMenuItems.DeletePost -> {
//                            viewModel.onUserEvent(ViewVideoItemUserEvent.OnDeletePost(menuItem.postId))
//                        }
//                        is ViewVideoMenuItems.SharePost -> {
//                            viewModel.onUserEvent(ViewVideoItemUserEvent.OnRepostClick)
//                        }
//                        is ViewVideoMenuItems.CopyPostLink -> {
//                            viewModel.onUserEvent(ViewVideoItemUserEvent.OnCopyPostLink(menuItem.postId))
//                        }
//                    }
//                }
//            }
//
//            menu.showWithTag(manager = childFragmentManager, tag = VIEW_VIDEO_MENU_TAG)
//        }
//    }

//    private fun showShareMenu(post: PostUIEntity) {
//        needAuth {
//            viewVideoPlaybackController?.resume()
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
//                        viewModel.onUserEvent(ViewVideoItemUserEvent.OnRepostSuccess(post))
//                        NToast.with(act)
//                            .durationLong()
//                            .text(getString(R.string.success_repost_to_group, groupName ?: ""))
//                            .typeSuccess()
//                            .show()
//                    }
//
//                    override fun onShareToRoadSuccess() {
//                        viewModel.onUserEvent(ViewVideoItemUserEvent.OnRepostSuccess(post))
//                        showCommonSuccessMessage(R.string.success_repost_to_own_road)
//                    }
//
//                    override fun onShareToChatSuccess(repostTargetCount: Int) {
//                        viewModel.onUserEvent(ViewVideoItemUserEvent.OnRepostSuccess(post, repostTargetCount))
//                        showCommonSuccessMessage(R.string.success_repost_to_chat)
//                    }
//
//                    override fun onPostItemUniqnameUserClick(userId: Long?) = openUserFragment(userId)
//
//                    override fun onOpenShareOutside() {
//                        viewModel.onUserEvent(ViewVideoItemUserEvent.OnShareOutsideOpened(true))
//                    }
//
//                }).show(childFragmentManager)
//        }
//    }

//    private fun meeraShowShareMenu(post: PostUIEntity) {
//        needAuth {
//            viewVideoPlaybackController?.resume()
//
//            MeeraShareBottomSheet(
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
//                        viewModel.onUserEvent(ViewVideoItemUserEvent.OnRepostSuccess(post))
//                        NToast.with(act)
//                            .durationLong()
//                            .text(getString(R.string.success_repost_to_group, groupName ?: ""))
//                            .typeSuccess()
//                            .show()
//                    }
//
//                    override fun onShareToRoadSuccess() {
//                        viewModel.onUserEvent(ViewVideoItemUserEvent.OnRepostSuccess(post))
//                        showCommonSuccessMessage(R.string.success_repost_to_own_road)
//                    }
//
//                    override fun onShareToChatSuccess(repostTargetCount: Int) {
//                        viewModel.onUserEvent(ViewVideoItemUserEvent.OnRepostSuccess(post, repostTargetCount))
//                        showCommonSuccessMessage(R.string.success_repost_to_chat)
//                    }
//
//                    override fun onPostItemUniqnameUserClick(userId: Long?) = openUserFragment(userId)
//
//                    override fun onOpenShareOutside() {
//                        viewModel.onUserEvent(ViewVideoItemUserEvent.OnShareOutsideOpened(true))
//                    }
//
//                }).show(childFragmentManager)
//        }
//    }

//    private fun onUpdateLoadingState(update: ViewVideoItemUIEvent.UpdateLoadingState) {
//        post = update.post
//        postLoaderController?.setupLoading(update.post.postId, update.loadingInfo)
//    }
//
//    private fun onDeletedPost() {
//        showCommonSuccessMessage(R.string.post_deleted_success)
//        act.onBackPressed()
//    }

    private fun showLoaderForPlaybackState() {
        loaderHandler?.removeCallbacksAndMessages(null)
        if (seekVideoController?.isVisible().isTrue() || !isVideoNeedToPlay) return
        loaderHandler?.postDelayed({ binding?.cpiViewVideoLoader?.visible() }, LOADER_SHOW_DELAY)
    }

    private fun hideLoaderForPlaybackState() {
        loaderHandler?.removeCallbacksAndMessages(null)
        binding?.cpiViewVideoLoader?.hide()
    }

//    private fun onHiddenUserRoad() {
//        showCommonSuccessMessage(R.string.post_author_hide_success)
//        act.onBackPressed()
//    }
//
//    private fun onAddedPostComplaint() = showCommonSuccessMessage(R.string.road_complaint_send_success)
//
//    private fun onSubscribedToPost() = showCommonSuccessMessage(R.string.subscribe_post)
//
//    private fun onUnsubscribedFromPost() = showCommonSuccessMessage(R.string.unsubscribe_post)
//
//    private fun onPostLinkCopied() {
//        (requireActivity() as? ActivityToolsProvider)?.getTooltipController()
//            ?.showSuccessTooltip(R.string.copy_link_success)
//    }

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
            is SpanDataClickType.ClickBadWord -> { viewModel.onUserEvent(ViewVideoItemUserEvent.OnBadWordClicked(clickType)) }
            is SpanDataClickType.ClickHashtag -> openHashtagFragment(clickType)
            is SpanDataClickType.ClickUserId -> openUserFragment(clickType.userId)
            is SpanDataClickType.ClickLink -> { act.openLink(clickType.link) }
            else -> Unit
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

    private fun saveVideo(postId: Long, assetId: String?) {
        setPermissions(
            object : PermissionDelegate.Listener {
                override fun onGranted() {
                    viewModel.onUserEvent(ViewVideoItemUserEvent.OnDownloadVideoClicked(postId, assetId))
                }

                override fun onDenied() {
                    NToast.with(act)
                        .text(act.getString(R.string.you_must_grant_permissions))
                        .durationLong()
                        .button(act.getString(R.string.general_retry)) {
                            saveVideo(postId, assetId)
                        }.show()
                }

                override fun onError(error: Throwable?) {
                    Timber.e("ERROR get Permissions: \$error")
                }
            },
            returnReadExternalStoragePermissionAfter33(),
            returnWriteExternalStoragePermissionAfter33()
        )
    }

    private fun openUserFragment(userId: Long?) {
        act.addFragmentIgnoringAuthCheck(
            UserInfoFragment(), Act.COLOR_STATUSBAR_LIGHT_NAVBAR,
            Arg(IArgContainer.ARG_USER_ID, userId)
        )
    }

    private fun openHashtagFragment(clickType: SpanDataClickType.ClickHashtag) {
        act.addFragment(
            HashtagFragment(),
            Act.LIGHT_STATUSBAR,
            Arg(IArgContainer.ARG_HASHTAG, clickType.hashtag)
        )
    }

    private inner class ViewVideoActionBarListener : ContentActionBar.Listener {

        private val reactionController by lazy {
            (requireActivity() as ActivityToolsProvider).getReactionBubbleViewController()
        }

        override fun onReactionBadgeClick() {
            val needAuth = needAuthAndReturnStatus { viewVideoPlaybackController?.resume() }
            if (needAuth) return

            val post = post ?: return
            if (viewModel.getFeatureTogglesContainer().detailedReactionsForPostFeatureToggle.isEnabled) {
                checkAppRedesigned(
                    isRedesigned = {
                        MeeraReactionsStatisticsBottomSheetFragment.getInstance(
                            entityId = post.postId,
                            entityType = ReactionsEntityType.POST
                        ).show(childFragmentManager)
                    },
                    isNotRedesigned = {
                        ReactionsStatisticsBottomSheetFragment.getInstance(
                            entityId = post.postId,
                            entityType = ReactionsEntityType.POST
                        ).show(childFragmentManager)
                    }
                )

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
            val needAuth = needAuthAndReturnStatus { viewVideoPlaybackController?.resume() }
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
                viewVideoPlaybackController?.resume()
                val post = post ?: return@needAuth
                MeeraCommentsBottomSheetFragment.showForPost(post, childFragmentManager, postOrigin)
            }
        }

        override fun onRepostClick() {
            viewModel.onUserEvent(ViewVideoItemUserEvent.OnRepostClick)
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
}
