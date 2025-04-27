package com.numplates.nomera3.modules.moments.show.presentation.fragment

import android.Manifest
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.meera.core.extensions.click
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.empty
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.visible
import com.meera.core.utils.TopAuthorApprovedUserModel
import com.meera.core.utils.checkAppRedesigned
import com.meera.core.utils.enableTopContentAuthorApprovedUser
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentMomentPositionBinding
import com.numplates.nomera3.modules.auth.util.needAuthAndReturnStatus
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.baseCore.helper.SaveMediaFileDelegate
import com.numplates.nomera3.modules.baseCore.helper.SaveMediaFileDelegateImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhence
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudePropertyMomentHowScreenClosed
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudePropertyMomentMenuActionType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.AmplitudePropertyReactionWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.AmplitudePropertyReactionsContentType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.AmplitudePropertyReactionsPostType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.AmplitudeReactionsParams
import com.numplates.nomera3.modules.baseCore.ui.permission.PermissionDelegate
import com.numplates.nomera3.modules.common.ActivityToolsProvider
import com.numplates.nomera3.modules.communities.utils.copyCommunityLink
import com.numplates.nomera3.modules.complains.ui.ComplainEvents
import com.numplates.nomera3.modules.complains.ui.ComplainsNavigator
import com.numplates.nomera3.modules.complains.ui.confirm.ConfirmComplainDialog
import com.numplates.nomera3.modules.complains.ui.model.UserComplainUiModel
import com.numplates.nomera3.modules.feed.ui.adapter.ContentActionBar
import com.numplates.nomera3.modules.moments.comments.presentation.MomentCommentsBottomSheetFragment
import com.numplates.nomera3.modules.moments.settings.presentation.MomentSettingsFragment
import com.numplates.nomera3.modules.moments.show.data.ARG_MOMENT_ID
import com.numplates.nomera3.modules.moments.show.data.entity.MomentContentType
import com.numplates.nomera3.modules.moments.show.domain.GetMomentDataUseCase
import com.numplates.nomera3.modules.moments.show.presentation.ViewMomentPositionViewModel
import com.numplates.nomera3.modules.moments.show.presentation.custom.MomentTouchHandler
import com.numplates.nomera3.modules.moments.show.presentation.custom.ViewHider
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentGroupPositionType
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentItemUiModel
import com.numplates.nomera3.modules.moments.show.presentation.dialog.MomentConfirmCallback
import com.numplates.nomera3.modules.moments.show.presentation.dialog.MomentsConfirmDialogBuilder
import com.numplates.nomera3.modules.moments.show.presentation.dialog.ViewMomentAllowCommentsDialog
import com.numplates.nomera3.modules.moments.show.presentation.dialog.ViewMomentAllowCommentsDialog.Companion.showDialog
import com.numplates.nomera3.modules.moments.show.presentation.dialog.ViewMomentAuthorDialog
import com.numplates.nomera3.modules.moments.show.presentation.dialog.ViewMomentAuthorDialog.Companion.showDialog
import com.numplates.nomera3.modules.moments.show.presentation.dialog.ViewMomentWatcherDialog
import com.numplates.nomera3.modules.moments.show.presentation.dialog.ViewMomentWatcherDialog.Companion.showDialog
import com.numplates.nomera3.modules.moments.show.presentation.mapper.toContentActionBarParams
import com.numplates.nomera3.modules.moments.show.presentation.player.MomentsExoPlayerManager
import com.numplates.nomera3.modules.moments.show.presentation.player.MomentsImagePlayer
import com.numplates.nomera3.modules.moments.show.presentation.player.MomentsPlayerWrapper
import com.numplates.nomera3.modules.moments.show.presentation.player.MomentsUnavailablePlayer
import com.numplates.nomera3.modules.moments.show.presentation.player.MomentsVideoPlayer
import com.numplates.nomera3.modules.moments.show.presentation.view.SoftAspectRatioFrameLayout
import com.numplates.nomera3.modules.moments.show.presentation.view.SoftSide
import com.numplates.nomera3.modules.moments.show.presentation.view.toast.CardToastView
import com.numplates.nomera3.modules.moments.show.presentation.viewevents.PositionViewMomentEvent
import com.numplates.nomera3.modules.moments.show.presentation.viewstates.MomentMessageState
import com.numplates.nomera3.modules.moments.show.presentation.viewstates.MomentPlaybackListener
import com.numplates.nomera3.modules.moments.show.presentation.viewstates.MomentPlaybackState
import com.numplates.nomera3.modules.moments.show.presentation.viewstates.MomentScreenActionEvent
import com.numplates.nomera3.modules.moments.show.presentation.viewstates.PositionViewMomentState
import com.numplates.nomera3.modules.moments.show.presentation.viewstates.ViewMomentNavigationState
import com.numplates.nomera3.modules.moments.util.getMomentActionBarDefaultParams
import com.numplates.nomera3.modules.moments.util.isSmallScreen
import com.numplates.nomera3.modules.moments.util.setActionBarPosition
import com.numplates.nomera3.modules.reaction.ui.custom.FlyingReaction
import com.numplates.nomera3.modules.reaction.ui.data.ReactionSource
import com.numplates.nomera3.modules.reaction.ui.getDefaultReactionContainer
import com.numplates.nomera3.modules.reaction.ui.util.reactionCount
import com.numplates.nomera3.modules.reactionStatistics.ui.ReactionsEntityType
import com.numplates.nomera3.modules.reactionStatistics.ui.ReactionsStatisticsBottomSheetFragment
import com.numplates.nomera3.modules.registration.ui.RegistrationContainerFragment
import com.numplates.nomera3.modules.screenshot.delegate.SAVING_PICTURE_DELAY
import com.numplates.nomera3.modules.screenshot.delegate.ScreenshotPopupController
import com.numplates.nomera3.modules.screenshot.ui.entity.ScreenshotPlace
import com.numplates.nomera3.modules.screenshot.ui.entity.ScreenshotPopupData
import com.numplates.nomera3.modules.screenshot.ui.fragment.ScreenshotTakenListener
import com.numplates.nomera3.modules.search.ui.fragment.SearchMainFragment
import com.numplates.nomera3.modules.user.ui.fragments.AdditionalComplainCallback
import com.numplates.nomera3.modules.user.ui.fragments.UserComplainAdditionalBottomSheet
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseBottomSheetDialogFragment
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_COMMENT_ID
import com.numplates.nomera3.presentation.view.fragments.UserInfoFragment
import com.numplates.nomera3.presentation.view.ui.BottomSheetDialogEventsListener
import com.numplates.nomera3.presentation.view.ui.CloseTypes
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraMenuBottomSheet
import com.numplates.nomera3.presentation.view.ui.bottomMenu.ReactionsStatisticBottomMenu
import com.numplates.nomera3.presentation.view.utils.NTime
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.view.utils.NToast.Companion.showError
import com.numplates.nomera3.presentation.view.utils.NToast.Companion.showSuccess
import com.numplates.nomera3.presentation.view.utils.sharedialog.MeeraShareSheet
import com.numplates.nomera3.presentation.view.utils.sharedialog.ShareBottomSheetEvent
import com.numplates.nomera3.presentation.view.utils.sharedialog.ShareDialogType
import com.numplates.nomera3.presentation.view.utils.sharedialog.SharePostBottomSheet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

const val ARG_MOMENT_GROUP_ID = "ARG_MOMENT_GROUP_ID"
const val ARG_MOMENT_USER_ID = "ARG_MOMENT_USER_ID"
const val ARG_MOMENTS_SOURCE = "ARG_MOMENTS_SOURCE"
const val ARG_TARGET_MOMENT_ID = "ARG_TARGET_MOMENT_ID"
private const val ARG_MOMENT_DIALOGS_COUNT = "ARG_MOMENT_DIALOGS_COUNT"

class ViewMomentPositionFragment : BaseFragmentNew<FragmentMomentPositionBinding>(),
    SaveMediaFileDelegate by SaveMediaFileDelegateImpl(),
    BottomSheetDialogEventsListener,
    BaseBottomSheetDialogFragment.Listener,
    MomentCommentsBottomSheetFragment.CommentsListener,
    ReactionsStatisticsBottomSheetFragment.ViewsCountListener,
    MeeraMenuBottomSheet.Listener,
    ScreenshotTakenListener {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMomentPositionBinding
        get() = FragmentMomentPositionBinding::inflate

    private var parentViewPagerListener: ViewMomentPagerParent? = null

    private val viewModel by viewModels<ViewMomentPositionViewModel> { App.component.getViewModelFactory() }
    private val complainsNavigator by lazy(LazyThreadSafetyMode.NONE) { ComplainsNavigator(requireActivity()) }
    private val actionBarListener = ActionBarListener()
    private val momentPlaybackListener = PlaybackListener()
    private val viewHider = ViewHider()
    private var currentItem: MomentItemUiModel? = null
    private var momentsPlayer: MomentsPlayerWrapper? = null
    private var dialogsCount = 0
    private var momentsSource: GetMomentDataUseCase.MomentsSource? = null
    private var momentTouchHandler: MomentTouchHandler? = null
    private var playerHandler: MomentsExoPlayerManager? = null
    private var globalFragmentLifecycleImpl: GlobalFragmentsLifecycleImpl? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var lastMomentSharedWithScreenshotId: Long? = null
    private var screenshotPopupData: ScreenshotPopupData? = null
    private var isSavingMomentPhoto = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        parentViewPagerListener = parentFragment as? ViewMomentPagerParent
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dialogsCount = arguments?.getInt(ARG_MOMENT_DIALOGS_COUNT) ?: 0
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initStatusBar()
        initActionBar()
        initContentFrame()
        initTouchHandler()
        initPlayers()
        initClicks()
        showShimmer()
        handleViewUpdates()
        initMomentData()
        registerFragmentsLifecycleChange()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(ARG_MOMENT_DIALOGS_COUNT, dialogsCount)
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        val currentStackFragments = act.getCurrentFragmentFromAdapter()?.childFragmentManager?.fragments
        if (currentStackFragments?.firstOrNull()?.parentFragment is UserInfoFragment) {
            return
        }
        viewModel.onTriggerViewEvent(PositionViewMomentEvent.OnFragmentResumed(isDialogsCreated()))
        registerComplaintListener()
    }

    override fun onPause() {
        super.onPause()
        pauseMoment()
        viewModel.onTriggerViewEvent(
            PositionViewMomentEvent.OnFragmentPaused(
                isNowOffscreenInPager = isFragmentOffscreenInPager(),
                groupPositionType = detectMomentGroupPositionType()
            )
        )
        unregisterComplaintListener()
    }

    override fun onStop() {
        super.onStop()
        viewModel.onTriggerViewEvent(PositionViewMomentEvent.OnAppHidden)
    }

    override fun onStartFragment() {
        if (act.getCurrentFragment() is ViewMomentPositionFragment) {
            if (!isDialogsCreated()) resumeMoment()
            setSwipeState(lockSwipe = true)
            if (isSmallScreen()) hideSystemUi()
        }
        registerComplaintListener()
        super.onStartFragment()
    }

    override fun onStopFragment() {
        preventSwipeForAuth()
        act.getReactionBubbleViewController().hideReactionBubble()
        if (isSmallScreen()) showSystemUi()
        unregisterComplaintListener()
        super.onStopFragment()
    }

    override fun onDestroyView() {
        momentsPlayer?.releasePlayers()
        momentsPlayer = null
        if (parentViewPagerListener == null) {
            playerHandler?.releasePlayer()
        }
        unRegisterFragmentsLifecycleChange()
        binding?.stvViewMomentSelectedTrack?.hide()
        super.onDestroyView()
    }

    override fun onDetach() {
        super.onDetach()
        parentViewPagerListener = null
    }

    override fun onStartAnimationTransitionFragment() {
        super.onStartAnimationTransitionFragment()
        lastMomentSharedWithScreenshotId = null
        pauseMoment()
    }

    override fun onDismissDialog(closeTypes: CloseTypes?) {
        when {
            closeTypes?.isClickedBack == true -> openAuthorDialog()
            closeTypes?.isResultReceived == true -> Unit
        }
        if (isFragmentOffscreenInPager()) {
            parentViewPagerListener?.onDismissDialog()
        }
        if (dialogsCount > 0) dialogsCount--
        if (dialogsCount <= 0) resumeMoment()
    }

    override fun onScreenshotTaken() {
        if (isSavingMomentPhoto) return
        if (currentItem?.id == lastMomentSharedWithScreenshotId) return
        pauseMoment()
        currentItem?.id?.let {
            lastMomentSharedWithScreenshotId = currentItem?.id
            this.screenshotPopupData = ScreenshotPopupData(
                title = currentItem?.userName ?: String.empty(),
                description = getString(R.string.moment_title),
                buttonTextStringRes = R.string.share_moment,
                imageLink = currentItem?.contentPreview,
                isDeleted = currentItem?.isDeleted,
                momentId = currentItem?.id ?: 0,
                screenshotPlace = ScreenshotPlace.MOMENT
            )
            viewModel.onTriggerViewEvent(PositionViewMomentEvent.ScreenshotTaken(it))
        }
    }

    override fun onCommentsCountChange(commentCount: Int) {
        changeCommentCount(commentCount)
    }

    override fun onViewCountChange(viewsCount: Long) {
        changeViewsCount(viewsCount)
    }

    override fun onCreateDialog() {
        super.onCreateDialog()
        if (isFragmentOffscreenInPager()) {
            parentViewPagerListener?.onCreateDialog()
        }
        pauseMoment()
        dialogsCount++
    }

    override fun onCancelByUser(menuTag: String?) {
        super.onCancelByUser(menuTag)
        viewModel.logMomentMenuAction(actionType = AmplitudePropertyMomentMenuActionType.CANCEL)
    }

    private fun changeCommentCount(commentCount: Int) {
        currentItem?.copy(commentsCount = commentCount)?.let { updatedMoment ->
            viewModel.onTriggerViewEvent(PositionViewMomentEvent.OnGetCommentCount(updatedMoment))
        }
    }

    private fun changeViewsCount(viewsCount: Long) {
        currentItem?.copy(viewsCount = viewsCount)?.let { updatedMoment ->
            viewModel.onTriggerViewEvent(PositionViewMomentEvent.OnGetViewsCount(updatedMoment))
        }
    }

    private fun registerFragmentsLifecycleChange() {
        globalFragmentLifecycleImpl = GlobalFragmentsLifecycleImpl()
        childFragmentManager.registerFragmentLifecycleCallbacks(
            globalFragmentLifecycleImpl ?: return,
            true
        )
    }

    private fun unRegisterFragmentsLifecycleChange() {
        val lifecycleListener = globalFragmentLifecycleImpl ?: return
        childFragmentManager.unregisterFragmentLifecycleCallbacks(lifecycleListener)
        globalFragmentLifecycleImpl = null
    }

    fun pauseMoment() {
        viewModel.onTriggerViewEvent(PositionViewMomentEvent.PausePositionMoment)
    }

    fun resumeMoment() {
        if (isResumed)
            viewModel.onTriggerViewEvent(PositionViewMomentEvent.ResumePositionMoment)
    }

    fun isDialogsCreated(): Boolean = dialogsCount > 0

    fun registerComplaintListener() {
        complainsNavigator.registerAdditionalActionListener(viewLifecycleOwner) { result ->
            when {
                result.isSuccess -> {
                    onDismissDialog(CloseTypes(isResultReceived = true))
                    showAdditionalStepsForComplain(result.getOrThrow())
                }

                result.isFailure -> onDismissDialog()
            }
        }
    }

    fun unregisterComplaintListener() {
        complainsNavigator.unregisterAdditionalActionListener()
    }

    fun toggleTouchEvents(enable: Boolean) {
        binding?.vTapHandler?.setOnTouchListener(if (enable) momentTouchHandler else null)
    }

    private fun setSwipeState(lockSwipe: Boolean) {
        act.navigatorViewPager.let { if (lockSwipe) it.lockSwipe() else it.unlockSwipe() }
    }

    private fun initTouchHandler() {
        momentTouchHandler = MomentTouchHandler(
            context = requireContext(),
            onContentTap = { pauseMoment() },
            onContentTapRelease = {
                resumeMoment()
                viewHider.showViews()
                toggleGesturesAvailability(true)
            },
            onClickLeft = {
                viewModel.onTriggerViewEvent(PositionViewMomentEvent.ClickedPrevPositionMoment)
            },
            onClickRight = {
                viewModel.onTriggerViewEvent(PositionViewMomentEvent.ClickedNextPositionMoment)
            },
            onContentLongTap = {
                viewHider.hideViews(views = getControlViewsList(), useAnimation = true)
                toggleGesturesAvailability(false)
                currentItem?.let { viewModel.logMomentStopByUser(momentId = it.id) }
            },
            onSwipeDown = {
                close()
                viewModel.logCloseMomentByUser(closeButton = AmplitudePropertyMomentHowScreenClosed.SWIPE)
            }
        )
    }

    private fun initMomentData() {
        val args = arguments ?: run {
            close()
            return
        }
        val momentId = args.getLong(ARG_MOMENT_ID, -1L)
        val commentToOpenId = args.getLong(ARG_COMMENT_ID, -1L)
        val momentGroupId = args.getLong(ARG_MOMENT_GROUP_ID, -1L)
        val momentUserId = args.getLong(ARG_MOMENT_GROUP_ID, -1L)
        val targetMomentId = args.getLong(ARG_TARGET_MOMENT_ID, -1L)
        val momentsSource = args.getSerializable(ARG_MOMENTS_SOURCE) as? GetMomentDataUseCase.MomentsSource
        val activityLifecycleScope = act.lifecycleScope
        this.momentsSource = momentsSource
        when {
            momentId != -1L -> viewModel.init(
                scope = activityLifecycleScope,
                momentId = momentId,
                commentId = commentToOpenId
            )

            momentGroupId != -1L && momentsSource != null -> {
                viewModel.init(
                    scope = activityLifecycleScope,
                    momentGroupId = momentGroupId,
                    momentUserId = momentUserId,
                    targetMomentId = targetMomentId,
                    targetCommentId = commentToOpenId,
                    momentsSource = momentsSource,
                    groupPositionType = detectMomentGroupPositionType()
                )
            }

            else -> {
                close()
                return
            }
        }
    }

    private fun initPlayers() {
        binding?.apply {
            playerHandler = parentViewPagerListener?.getPlayerHandler() ?: MomentsExoPlayerManager(requireContext())
            val playerManager = playerHandler ?: run {
                close()
                return
            }
            val videoPlayer = MomentsVideoPlayer(
                videoView = pvViewMomentVideo,
                previewView = ivViewMomentVideoPreview,
                loaderView = cpiViewMomentLoader,
                momentPlaybackListener = momentPlaybackListener,
                playerHandler = playerManager
            )
            val imagePlayer = MomentsImagePlayer(
                imageView = ivViewMomentImage,
                loaderView = cpiViewMomentLoader,
                momentPlaybackListener = momentPlaybackListener
            )
            val unavailablePlayer = MomentsUnavailablePlayer(
                layoutBinding = incMomentsUnavailableLayout,
                loaderView = cpiViewMomentLoader,
                momentPlaybackListener = momentPlaybackListener
            )
            momentsPlayer = MomentsPlayerWrapper(
                momentPlayers = listOf(videoPlayer, imagePlayer, unavailablePlayer),
                progressView = binding?.epmvMomentsProgressBar
            )
        }
    }

    private fun initActionBar() {
        val isMomentViewsToggleEnabled = viewModel.getFeatureTogglesContainer().momentViewsFeatureToggle.isEnabled
        binding?.cabViewMomentActionBar?.resetReactionsAnimation()
        binding?.cabViewMomentActionBar?.init(
            params = getMomentActionBarDefaultParams(),
            callbackListener = actionBarListener,
            isNeedToShowRepost = false,
            momentViewsToggleEnabled = isMomentViewsToggleEnabled
        )
    }

    private fun initContentFrame() {
        binding?.vgMomentAspectRatioContainer?.apply {
            setSoftOrientation(SoftSide.HEIGHT)
            setAspectRatio(SoftAspectRatioFrameLayout.RATIO_9_TO_16)
        }
    }

    private fun initStatusBar() {
        if (isSmallScreen()) {
            binding?.vStatusBar?.gone()
            return
        }
        val isParentViewMomentFragment = parentFragment is ViewMomentFragment
        binding?.vStatusBar?.isVisible = !isParentViewMomentFragment
        if (!isParentViewMomentFragment) {
            binding?.vStatusBar?.updateLayoutParams { height = context.getStatusBarHeight() }
        }
    }

    private fun hideUserInfoElements() {
        binding?.apply {
            vvViewMomentAvatar.gone()
            vgViewMomentNameContainer.gone()
            tvViewMomentName.gone()
            tvViewMomentGeoLocation.gone()
            vTopGradient.gone()
        }
    }

    private fun showUserInfoElements() {
        binding?.apply {
            vvViewMomentAvatar.visible()
            vgViewMomentNameContainer.visible()
            tvViewMomentName.visible()
            tvViewMomentGeoLocation.visible()
            vTopGradient.visible()
        }
    }

    private fun initViews(state: PositionViewMomentState.UpdateMoment) {
        setupUserAvatar()
        setupUserInfo()
        setupMenu(state)
        setMaxWidthName()
        setupMomentTime()
        setupMomentContent(state)
        setupActionBar()
        setupMusicView()
    }

    private fun setupMenu(state: PositionViewMomentState.UpdateMoment) {
        binding?.ivViewMomentMenu?.isEnabled = state.momentItemModel?.isInteractionAllowed() ?: false
    }

    private fun setErrorState(errorState: PositionViewMomentState.UpdateMoment) {
        momentsPlayer?.setupErrorContent(errorState)
    }

    private fun setupUserAvatar() {
        val ctx = context ?: return
        val data = currentItem ?: return
        binding?.vvViewMomentAvatar?.setUp(
            context = ctx,
            avatarLink = data.userAvatarSmall,
            accountType = data.userAccountType,
            frameColor = data.userAccountColor
        )
    }

    private fun setupUserInfo() {
        val data = currentItem ?: return
        binding?.tvViewMomentName?.text = data.userName
        binding?.tvViewMomentName?.enableTopContentAuthorApprovedUser(
            params = TopAuthorApprovedUserModel(
                approved = data.userApproved,
                interestingAuthor = data.userTopContentMaker,
                isVip = data.userAccountType == AccountTypeEnum.ACCOUNT_TYPE_VIP.value
                    || data.userAccountType == AccountTypeEnum.ACCOUNT_TYPE_PREMIUM.value
            )
        )
        binding?.tvViewMomentGeoLocation?.text = data.place
    }

    private fun setMaxWidthName() {
        val nameContainer = binding?.vgViewMomentNameContainer ?: return
        nameContainer.post {
            val containerWidth = nameContainer.width
            binding?.tvViewMomentName?.maxWidth = containerWidth
        }
    }

    private fun setupMomentTime() {
        val data = currentItem ?: return
        val dateText = NTime.timeAgo(
            date = data.createdAt,
            shouldTrimAgo = true
        )
        binding?.tvViewMomentDate?.text = dateText
    }

    private fun setupMomentContent(state: PositionViewMomentState.UpdateMoment) {
        val isAdultContent = state.momentItemModel?.adultContent.toBoolean()
        if (isAdultContent) {
            setupAdultContent(state)
        } else {
            val isPopupShowing = ScreenshotPopupController.isPopupShowing
            if (isPopupShowing) {
                val newState = state.copy(playbackState = MomentPlaybackState.PAUSED)
                momentsPlayer?.setupMomentContent(newState)
            } else {
                momentsPlayer?.setupMomentContent(state)
            }
        }
    }

    private fun setupAdultContent(state: PositionViewMomentState.UpdateMoment) {
        val pausedState = state.copy(playbackState = MomentPlaybackState.PAUSED)
        momentsPlayer?.setupMomentContent(pausedState)
        toggleTouchEvents(enable = false)
        binding?.cabViewMomentActionBar?.gone()

        with(requireNotNull(binding?.incMomentsAgeRestrictions)) {
            root.visible()
            ivMomentPreview.loadGlide(state.momentItemModel?.contentPreview)
            btnShow.click {
                root.gone()
                binding?.cabViewMomentActionBar?.visible()
                momentsPlayer?.updatePlaybackState(MomentPlaybackState.RESUMED)
            }
        }
    }

    private fun setupActionBar() {
        val data = currentItem ?: return
        val isMomentViewsToggleEnabled = viewModel.getFeatureTogglesContainer().momentViewsFeatureToggle.isEnabled
        binding?.cabViewMomentActionBar?.resetReactionsAnimation()
        binding?.cabViewMomentActionBar?.init(
            params = data.toContentActionBarParams(
                viewModel.isUserAuthorized(),
                viewModel.isMomentAuthor(data.userId)
            ),
            callbackListener = actionBarListener,
            isNeedToShowRepost = true,
            momentViewsToggleEnabled = isMomentViewsToggleEnabled
        )
    }

    private fun setupMusicView() {
        val data = currentItem ?: return
        val mediaData = data.media
        if (mediaData == null) {
            binding?.stvViewMomentSelectedTrack?.gone()
            return
        }

        binding?.stvViewMomentSelectedTrack?.setContentInfo(
            media = mediaData,
            onClickAction = { openMusicUrl() }
        )
        binding?.stvViewMomentSelectedTrack?.visible()
    }

    private fun openMusicUrl() {
        val data = currentItem ?: return
        val mediaData = data.media ?: return
        act.openLink(mediaData.trackUrl)
    }

    private fun hideShimmer(hasError: Boolean) {
        binding?.vgShimmerLayout?.root?.gone()
        if (hasError.not()) {
            showUserInfoElements()
        }
        binding?.cpiViewMomentLoader?.hide()
    }

    private fun showShimmer() {
        binding?.vgShimmerLayout?.root?.visible()
        hideUserInfoElements()
    }

    private fun handleViewUpdates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.liveMomentState.collect { state ->
                when (state) {
                    is PositionViewMomentState.UpdateMoment -> {
                        if (screenshotPopupData?.momentId != state.momentItemModel?.id) {
                            lastMomentSharedWithScreenshotId = null
                        }
                        currentItem = state.momentItemModel
                        if (currentItem != null || state.hasError()) hideShimmer(state.hasError())
                        when {
                            state.hasError() -> setErrorState(state)
                            state.reactionSource != null -> updateActionBar(state.reactionSource)
                            else -> initViews(state)
                        }
                    }

                    is PositionViewMomentState.LinkCopied -> {
                        copyCommunityLink(context, state.copyLink) {
                            (requireActivity() as? ActivityToolsProvider)
                                ?.getTooltipController()
                                ?.showSuccessTooltip(R.string.copy_link_success)
                        }
                    }

                    else -> Unit
                }
            }
        }
        viewModel.liveMomentMessageState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is MomentMessageState.ShowError -> {
                    showError(view = view, text = getString(state.error))
                }

                is MomentMessageState.ShowSuccess -> {
                    showSuccess(view = view, text = getString(state.message))
                }
            }
        }
        viewModel.momentNavigation.observe(viewLifecycleOwner) { navigation ->
            when (navigation) {
                is ViewMomentNavigationState.GroupNavigation -> {
                    setFragmentResult(
                        requestKey = KEY_MOMENT_GROUP_CHANGE,
                        result = bundleOf(
                            KEY_MOMENT_GROUP_CHANGE_ID to navigation.currentGroupId,
                            KEY_MOMENT_GROUP_CHANGE_DIRECTION to navigation.navigationType.direction,
                            KEY_MOMENT_GROUP_CURRENT_INVALIDATE to navigation.invalidateCurrentGroup,
                            KEY_MOMENT_GROUP_THE_WAY_HOW_USER_FLIP to navigation.howUserFlipMoment
                        )
                    )
                    act.getReactionBubbleViewController().hideReactionBubble()
                    binding?.cabViewMomentActionBar?.resetReactionBubbleAppearance()
                }

                is ViewMomentNavigationState.CloseScreenRequest -> close()
                is ViewMomentNavigationState.InvalidateCurrentGroup -> {
                    setFragmentResult(
                        requestKey = KEY_MOMENT_GROUP_INVALIDATE_ONLY,
                        result = bundleOf()
                    )
                }
                is ViewMomentNavigationState.ShowScreenshotPopup -> {
                    showScreenshotPopup(navigation.momentLink)
                }
            }
        }
        viewModel.momentScreenAction.observe(viewLifecycleOwner) { event ->
            when (event) {
                is MomentScreenActionEvent.OpenCommentBottomSheet -> {
                    openComments(commentToOpenId = event.commentToOpenId)
                }

                is MomentScreenActionEvent.ShowCommonError -> showCommonError(event.messageResId)
                is MomentScreenActionEvent.OpenComplaintMenu -> openComplainDialog()
            }
        }
    }

    private fun showScreenshotPopup(momentLink: String) {
        val screenshotPopupData = screenshotPopupData?.apply { link = momentLink } ?: return
        ScreenshotPopupController.show(this, screenshotPopupData, this)
    }

    private fun updateActionBar(reactionSource: ReactionSource) {
        val data = currentItem ?: return
        binding?.cabViewMomentActionBar?.update(
            params = data.toContentActionBarParams(
                isAuthed = viewModel.isUserAuthorized(),
                isAuthor = viewModel.isMomentAuthor(data.userId)
            ),
            reactionHolderViewId = reactionSource.reactionHolderViewId
        )
    }

    private fun initClicks() {
        binding?.apply {
            ivViewMomentClose.click {
                close()
                viewModel.logCloseMomentByUser(AmplitudePropertyMomentHowScreenClosed.CLOSE_BUTTON)
            }
            vvViewMomentAvatar.setThrottledClickListener { gotoUserProfileFragment() }
            tvViewMomentName.setThrottledClickListener { gotoUserProfileFragment() }
            tvViewMomentDate.setThrottledClickListener { gotoUserProfileFragment() }
            ivViewMomentMenu.setThrottledClickListener { openMomentsMenu() }
            toggleTouchEvents(enable = true)
        }
    }

    private fun toggleGesturesAvailability(available: Boolean) {
        setFragmentResult(
            requestKey = KEY_MOMENT_GESTURES,
            result = bundleOf(
                KEY_MOMENT_GESTURES_AVAILABILITY to available
            )
        )
    }

    private fun gotoUserProfileFragment() {
        val data = currentItem ?: return
        viewModel.onTriggerViewEvent(PositionViewMomentEvent.OnUserProfileOpened)
        act.addFragmentIgnoringAuthCheck(
            UserInfoFragment(), Act.COLOR_STATUSBAR_LIGHT_NAVBAR,
            Arg(IArgContainer.ARG_USER_ID, data.userId),
            Arg(IArgContainer.ARG_TRANSIT_FROM, AmplitudePropertyWhere.MOMENT.property)
        )
    }

    private fun openMomentsMenu() {
        val data = currentItem ?: return
        needAuth {
            if (viewModel.isMomentAuthor(data.userId)) {
                openAuthorDialog()
            } else if (data.isActive) {
                openWatcherDialog()
            }
        }
    }

    private fun openAuthorDialog() {
        ViewMomentAuthorDialog(context).also { dialog ->
            with(dialog) {
                createAuthorDialog { openAllowCommentsDialog() }
                setListener(this@ViewMomentPositionFragment)
                onPressDownloadContent = { saveMomentMedia() }
                onPressDeleteMoment = { showConfirmDialogDelete() }
                onPressMomentSettings = { openMomentSettings() }
                onPressShareMomentSettings = {
                    checkAppRedesigned(
                        isRedesigned = ::meeraOpenRepost,
                        isNotRedesigned = ::openRepost
                    )
                    viewModel.logMomentMenuAction(actionType = AmplitudePropertyMomentMenuActionType.SHARE)
                }
                onPressCopyMomentSettings = {
                    viewModel.onTriggerViewEvent(PositionViewMomentEvent.CopyLink)
                }
            }
            dialog.showDialog(childFragmentManager)

            pauseMoment()
        }
    }

    private fun openMomentSettings() {
        viewModel.onTriggerViewEvent(PositionViewMomentEvent.OnSettingsOpened)
        checkAppRedesigned(
            isRedesigned = {},
            isNotRedesigned = {
                add(MomentSettingsFragment(), Act.COLOR_STATUSBAR_LIGHT_NAVBAR)
            }
        )
    }

    private fun openAllowCommentsDialog() {
        val commentAvailability = currentItem?.commentAvailability ?: return
        ViewMomentAllowCommentsDialog(context).also { dialog ->
            dialog.createAllowCommentsDialog(commentAvailability)
            dialog.onSelectAllowType = {
                dialog.setCommentsAvailabilityType(it)
                viewModel.onTriggerViewEvent(
                    PositionViewMomentEvent.SetMomentCommentAvailability(it)
                )
            }
            dialog.onBackClick = {
                openAuthorDialog()
            }
            dialog.showDialog(childFragmentManager)
            pauseMoment()
        }
        viewModel.logMomentMenuAction(actionType = AmplitudePropertyMomentMenuActionType.ALLOW_COMMENTS)
    }

    private fun openWatcherDialog() {
        val userId = currentItem?.userId ?: return
        ViewMomentWatcherDialog(context).also { dialog ->
            with(dialog) {
                createWatcherDialog(currentItem?.doNotShowUser == false)
                setListener(this@ViewMomentPositionFragment)
                onPressShareMomentSettings = {
                    checkAppRedesigned(
                        isRedesigned = ::meeraOpenRepost,
                        isNotRedesigned = ::openRepost
                    )
                    viewModel.logMomentMenuAction(actionType = AmplitudePropertyMomentMenuActionType.SHARE)
                }
                onPressCopyMomentSettings = {
                    viewModel.onTriggerViewEvent(PositionViewMomentEvent.CopyLink)
                }
                complainOnMoment = {
                    openConfirmComplainDialog()
                }
                hideUserMoments = {
                    viewModel.onTriggerViewEvent(PositionViewMomentEvent.HideUserMoments(userId))
                }
                showUserMoments = {
                    viewModel.onTriggerViewEvent(PositionViewMomentEvent.ShowUserMoments(userId))
                }
            }
            dialog.showDialog(childFragmentManager)

            pauseMoment()
        }
    }

    private fun openConfirmComplainDialog() {
        pauseMoment()

        val complainData = UserComplainUiModel(
            dialogHeaderTitle = R.string.user_complain_moment_question_title
        )
        val dialog = ConfirmComplainDialog.showDialogInstance(
            fragmentManager = childFragmentManager,
            complain = complainData,
        )

        onCreateDialog()

        dialog.setListener(object : ConfirmComplainDialog.Listener {
            override fun onDismissed() {
                onDismissDialog()
            }

            override fun onConfirmed() {
                dialog.dismiss()
                viewModel.onTriggerViewEvent(PositionViewMomentEvent.ComplainToMoment)
            }
        })
    }

    private fun openComplainDialog() {
        val momentData = currentItem ?: return
        showAdditionalStepsForComplain(momentData.userId)
        pauseMoment()
    }

    private fun showAdditionalStepsForComplain(remoteUserId: Long) {
        UserComplainAdditionalBottomSheet.newInstance(
            userId = remoteUserId,
            optionHideMoments = true
        ).also { dialog ->
            dialog.callback = MomentComplainCallback()
            dialog.show(childFragmentManager, UserComplainAdditionalBottomSheet::class.simpleName)
            pauseMoment()
        }
    }

    private fun getMomentSavePermission(momentExternalUrl: String, permissionGranted: () -> Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            PermissionDelegate(
                act = act,
                viewLifecycleOwner = viewLifecycleOwner
            ).setPermissions(
                listener = object : PermissionDelegate.Listener {
                    override fun onGranted() {
                        permissionGranted()
                    }

                    override fun onDenied() {
                        NToast.with(act)
                            .text(act.getString(R.string.you_must_grant_permissions))
                            .durationLong()
                            .button(act.getString(R.string.general_retry)) {
                                isSavingMomentPhoto = true
                                saveImageOrVideoFile(
                                    imageUrl = momentExternalUrl,
                                    act = act,
                                    viewLifecycleOwner = viewLifecycleOwner,
                                    successListener = {
                                        doDelayed(SAVING_PICTURE_DELAY) { isSavingMomentPhoto = false }
                                    }
                                )
                            }
                            .show()
                    }

                    override fun onError(error: Throwable?) {
                        Timber.e(error)

                        onSaveMomentUnknownError()
                    }
                },
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        } else {
            permissionGranted()
        }
    }

    private fun onSaveMomentUnknownError() {
        NToast.with(act)
            .text(act.getString(R.string.moment_save_unknown_error))
            .durationLong()
            .show()
    }

    private fun saveMomentMedia() {
        val data = currentItem ?: return
        val momentExternalUrl = data.contentUrl ?: return

        getMomentSavePermission(momentExternalUrl) {
            trySaveMomentAndProcessResult(momentExternalUrl)
        }
        viewModel.logMomentMenuAction(actionType = AmplitudePropertyMomentMenuActionType.ALLOW_COMMENTS)
    }

    private fun trySaveMomentAndProcessResult(momentExternalUrl: String) = lifecycleScope.launch(Dispatchers.IO) {
        kotlin.runCatching {
            isSavingMomentPhoto = true
            viewModel.downloadMomentAndAddToGallery(momentExternalUrl)
        }.onSuccess {
            withContext(Dispatchers.Main) {
                val container = act?.getRootView() as? ViewGroup ?: return@withContext

                CardToastView.show(
                    container = container,
                    text = R.string.moment_download_success
                )
                doDelayed(SAVING_PICTURE_DELAY) { isSavingMomentPhoto = false }
            }
        }.onFailure { throwable ->
            Timber.e(throwable)

            withContext(Dispatchers.Main) {
                onSaveMomentUnknownError()
            }
        }
    }

    private fun showConfirmDialogDelete() {
        pauseMoment()
        MomentsConfirmDialogBuilder()
            .setHeader(getString(R.string.moment_dialog_delete_moment_title))
            .setDescription(getString(R.string.moment_dialog_delete_moment_description))
            .setConfirmButtonText(getString(R.string.moment_dialog_delete_moment_confirm))
            .setCancelButtonText(getString(R.string.moment_dialog_delete_moment_cancel))
            .setMomentConfirmCallback(object : MomentConfirmCallback {

                override fun onConfirmButtonClicked() {
                    viewModel.onTriggerViewEvent(PositionViewMomentEvent.DeletePositionMoment)
                }

                override fun onCancelButtonClicked() {
                    resumeMoment()
                }
            })
            .show(childFragmentManager)
    }

    private fun getControlViewsList(): List<View> {
        val viewGroupChildren = binding?.root?.children?.toList() ?: return emptyList()
        return viewGroupChildren.filter { it.tag == "view_may_be_hidden" }
    }

    private fun openComments(commentToOpenId: Long? = null) {
        val momentItem = currentItem ?: return
        MomentCommentsBottomSheetFragment.show(
            model = momentItem,
            fragmentManager = childFragmentManager,
            commentToOpenId = commentToOpenId,
            listener = this,
            commentsListener = this
        )
        pauseMoment()
    }

    private fun openRepost() {
        val moment = currentItem ?: return
        val fragment = SharePostBottomSheet(ShareDialogType.ShareMoment(moment)) { shareEvent ->
            when (shareEvent) {
                is ShareBottomSheetEvent.OnSuccessShareMoment -> {
                    viewModel.onTriggerViewEvent(
                        event = PositionViewMomentEvent.OnRepostMoment(shareEvent.momentItemUiModel)
                    )
                    showSuccess(view = view, text = getString(R.string.moment_share_success))
                }

                is ShareBottomSheetEvent.OnErrorUnselectedUser -> {
                    showError(view = view, text = getString(R.string.no_user_selected))
                }

                is ShareBottomSheetEvent.OnClickFindFriendButton -> {
                    add(SearchMainFragment(), Act.LIGHT_STATUSBAR)
                }

                else -> Unit
            }
        }
        fragment.setListener(this)
        fragment.show(childFragmentManager)

        pauseMoment()
    }

    private fun meeraOpenRepost() {
        val moment = currentItem ?: return
        MeeraShareSheet().showByType(
            fm = childFragmentManager,
            shareType = ShareDialogType.ShareMoment(moment),
            event = { shareEvent ->
                when (shareEvent) {
                    is ShareBottomSheetEvent.OnSuccessShareMoment -> {
                        viewModel.onTriggerViewEvent(
                            event = PositionViewMomentEvent.OnRepostMoment(shareEvent.momentItemUiModel)
                        )
                        showSuccess(view = view, text = getString(R.string.moment_share_success))
                    }

                    is ShareBottomSheetEvent.OnErrorUnselectedUser -> {
                        showError(view = view, text = getString(R.string.no_user_selected))
                    }

                    is ShareBottomSheetEvent.OnClickFindFriendButton -> {
                        add(SearchMainFragment(), Act.LIGHT_STATUSBAR)
                    }

                    else -> Unit
                }
            }
        )

        pauseMoment()
    }

    private fun close() {
        val parentFragment = parentFragment as? ViewMomentFragment?
        if (parentFragment != null) {
            parentFragment.close()
        } else {
            act.onBackPressed()
        }
    }

    private fun isFragmentOffscreenInPager(): Boolean {
        val groupId = arguments?.getLong(ARG_MOMENT_GROUP_ID, -1L)
            .takeIf { it != null && it != -1L } ?: return false
        val pagerParent = parentViewPagerListener ?: return false
        return pagerParent.isCurrentItem(groupId).not()
    }

    private fun detectMomentGroupPositionType(): MomentGroupPositionType? {
        val groupId = arguments?.getLong(ARG_MOMENT_GROUP_ID, -1L)
            .takeIf { it != null && it != -1L } ?: return null
        return parentViewPagerListener?.detectPositionType(groupId)
    }

    private fun showSystemUi() {
        val insetsController = ViewCompat.getWindowInsetsController(act.window.decorView) ?: return
        insetsController.show(WindowInsetsCompat.Type.systemBars())
        act.window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    private fun hideSystemUi() {
        val insetsController = ViewCompat.getWindowInsetsController(act.window.decorView) ?: return
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        act.window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

    /**
     * Stops the swipe from being explicitly enabled for [RegistrationContainerFragment] when leaving this fragment.
     *
     * [RegistrationContainerFragment] itself disables swipes, and behaves incorrectly with them enabled.
     *
     * **Problematic behavior:** If we leave [RegistrationContainerFragment] with a swipe, the auth flow will not complete properly.
     * This causes the inability to open Auth Screen again from this fragment instance.
     *
     * @see [ViewMomentFragment.preventSwipeForAuth]
     */
    private fun preventSwipeForAuth() {
        if (act?.getCurrentFragment() !is RegistrationContainerFragment) {
            setSwipeState(lockSwipe = false)
        }
    }

    private inner class ActionBarListener : ContentActionBar.Listener {

        override fun onCommentsClick() = needAuth {
            openComments()
            currentItem?.let { viewModel.logOnCommentsClicked(it) }
        }

        override fun onRepostClick() = needAuth {
            checkAppRedesigned(
                isRedesigned = ::meeraOpenRepost,
                isNotRedesigned = ::openRepost
            )
        }

        override fun onReactionBadgeClick() {
            showReactionsAndViewsStatisticsBottomSheet(clickFromReactionsBadge = true)
            pauseMoment()
            viewModel.logStatisticReactionsTap()
        }

        override fun onViewsCountClick() {
            showReactionsAndViewsStatisticsBottomSheet()
            pauseMoment()
        }

        override fun onReactionLongClick(
            showPoint: Point,
            reactionTip: TextView,
            viewsToHide: List<View>,
            reactionHolderViewId: ContentActionBar.ReactionHolderViewId,
        ) {
            val data = currentItem ?: return
            val params = data.toContentActionBarParams(
                viewModel.isUserAuthorized(),
                viewModel.isMomentAuthor(data.userId)
            )
            val controller = act.getReactionBubbleViewController()
            controller.onReactionBubbleShow = { pauseMoment() }
            controller.onReactionBubbleHide = {
                //TODO: костыль для https://nomera.atlassian.net/browse/BR-26324
                mainHandler.post {
                    resumeMoment()
                }
                Unit
            }
            val reactionParams = AmplitudeReactionsParams(
                whence = AmplitudePropertyWhence.OTHER,
                momentId = data.id,
                authorId = data.userId,
                postType = AmplitudePropertyReactionsPostType.NONE,
                postContentType = AmplitudePropertyReactionsContentType.SINGLE,
                haveText = false,
                havePic = data.contentType == MomentContentType.IMAGE.value,
                haveVideo = data.contentType == MomentContentType.VIDEO.value,
                haveMusic = data.media != null,
                isEvent = false,
                where = AmplitudePropertyReactionWhere.MOMENT
            )
            controller.showReactionBubble(
                reactionSource = ReactionSource.Moment(
                    momentId = data.id,
                    reactionHolderViewId = reactionHolderViewId
                ),
                showPoint = showPoint,
                viewsToHide = viewsToHide,
                reactionTip = reactionTip,
                currentReactionsList = data.reactions,
                contentActionBarType = ContentActionBar.ContentActionBarType.getType(params),
                containerInfo = act.getDefaultReactionContainer(),
                showMorningEvening = false,
                reactionsParams = reactionParams
            )
        }

        override fun onFlyingAnimationInitialized(flyingReaction: FlyingReaction) = Unit

        override fun onReactionRegularClick(reactionHolderViewId: ContentActionBar.ReactionHolderViewId) {
            val data = currentItem ?: return
            val controller = act.getReactionBubbleViewController()
            resumeMoment()
            val reactionParams = AmplitudeReactionsParams(
                whence = AmplitudePropertyWhence.OTHER,
                momentId = data.id,
                authorId = data.userId,
                postType = AmplitudePropertyReactionsPostType.NONE,
                postContentType = AmplitudePropertyReactionsContentType.SINGLE,
                haveText = false,
                havePic = data.contentType == MomentContentType.IMAGE.value,
                haveVideo = data.contentType == MomentContentType.VIDEO.value,
                haveMusic = data.media != null,
                isEvent = false,
                where = AmplitudePropertyReactionWhere.MOMENT
            )
            controller.onSelectDefaultReaction(
                reactionSource = ReactionSource.Moment(
                    momentId = data.id,
                    reactionHolderViewId = reactionHolderViewId
                ),
                currentReactionsList = data.reactions,
                reactionsParams = reactionParams
            )
        }

        override fun onReactionButtonDisabledClick() {
            TODO("Not yet implemented")
        }
    }

    private fun showReactionsAndViewsStatisticsBottomSheet(clickFromReactionsBadge: Boolean = false) = needAuth {
        val momentItem = currentItem ?: return@needAuth
        if (viewModel.getFeatureTogglesContainer().detailedReactionsForCommentsFeatureToggle.isEnabled) {
            val isMomentViewsToggleEnabled = viewModel.getFeatureTogglesContainer().momentViewsFeatureToggle.isEnabled
            val entityType = if (viewModel.isMomentAuthor(momentItem.userId) && isMomentViewsToggleEnabled) {
                ReactionsEntityType.MOMENT_WITH_VIEWS
            } else {
                ReactionsEntityType.MOMENT
            }
            checkAppRedesigned(
//                isRedesigned = {
//                    MeeraReactionsStatisticsBottomSheetFragment.getInstance(
//                        entityId = momentItem.id,
//                        entityType = entityType,
//                        viewCountListener = this@ViewMomentPositionFragment,
//                        startWithReactionTab = clickFromReactionsBadge,
//                        dialogEventsListener = this
//                    ).show(childFragmentManager)
//                },
                isNotRedesigned = {
                    ReactionsStatisticsBottomSheetFragment.getInstance(
                        entityId = momentItem.id,
                        entityType = entityType,
                        viewCountListener = this@ViewMomentPositionFragment,
                        startWithReactionTab = clickFromReactionsBadge,
                        dialogEventsListener = this
                    ).show(childFragmentManager)
                }
            )

        } else {
            val reactions = momentItem.reactions
            val sortedReactions =
                reactions.sortedByDescending { reactionEntity -> reactionEntity.count }
            val menu = ReactionsStatisticBottomMenu(context)
            menu.addTitle(
                title = R.string.reactions_on_post,
                number = sortedReactions.reactionCount()
            )
            sortedReactions.forEachIndexed { index, value ->
                menu.addReaction(
                    reaction = value,
                    hasDivider = index != sortedReactions.size - 1
                )
            }
            menu.show(childFragmentManager)
        }
    }

    private inner class PlaybackListener : MomentPlaybackListener {
        override fun onResourceReady(isPreview: Boolean) {
            val contentType = currentItem?.contentType ?: kotlin.run {
                viewModel.onTriggerViewEvent(PositionViewMomentEvent.MomentContentLoaded(isPreview))
                return
            }
            when (contentType) {
                MomentContentType.VIDEO.value -> {
                    val computeFrom =
                        if (isPreview) binding?.ivViewMomentVideoPreview else binding?.pvViewMomentVideo
                    computeFrom?.computeContentTypePositionType { positionType ->
                        binding?.vgMomentAspectRatioContainer?.setActionBarPosition(
                            positionType = positionType,
                            gradient = binding?.vBottomGradient,
                            actionBar = binding?.cabViewMomentActionBar
                        )
                    }
                }

                MomentContentType.IMAGE.value -> {
                    binding?.ivViewMomentImage?.computeContentTypePositionType { positionType ->
                        binding?.vgMomentAspectRatioContainer?.setActionBarPosition(
                            positionType = positionType,
                            gradient = binding?.vBottomGradient,
                            actionBar = binding?.cabViewMomentActionBar
                        )
                    }
                }
            }
            viewModel.onTriggerViewEvent(PositionViewMomentEvent.MomentContentLoaded(isPreview))
        }

        override fun onResumePlayback() =
            viewModel.onTriggerViewEvent(PositionViewMomentEvent.MomentPlaybackResumed)

        override fun onPlaybackEnded() =
            viewModel.onTriggerViewEvent(PositionViewMomentEvent.MomentPlaybackEnded)

        override fun onResourceError() =
            viewModel.onTriggerViewEvent(PositionViewMomentEvent.MomentContentRequested)

        override fun onPausePlayback() = Unit
    }

    private inner class MomentComplainCallback : AdditionalComplainCallback {
        override fun onSuccess(msg: String?, reason: ComplainEvents) {
            when (reason) {
                is ComplainEvents.MomentsHidden -> {
                    viewModel.onTriggerViewEvent(
                        PositionViewMomentEvent.UserMomentsHiddenAfterComplain(reason.userId)
                    )
                }

                else -> Unit
            }
        }

        override fun onError(msg: String?) {
            Timber.d("Error completing additional actions after complain. msg=$msg")
        }
    }

    private inner class GlobalFragmentsLifecycleImpl : FragmentManager.FragmentLifecycleCallbacks() {

        override fun onFragmentPaused(
            fragmentManager: FragmentManager,
            fragment: Fragment
        ) {
            super.onFragmentPaused(fragmentManager, fragment)
            //TODO: удалить после теста https://nomera.atlassian.net/browse/BR-26994
//            mainHandler.postDelayed({
//                pauseMoment()
//            }, 0)
        }
    }
}

private fun ViewMomentPositionFragment.needAuth(complete: (Boolean) -> Unit) {
    pauseMoment()
    needAuthAndReturnStatus(complete)
}
