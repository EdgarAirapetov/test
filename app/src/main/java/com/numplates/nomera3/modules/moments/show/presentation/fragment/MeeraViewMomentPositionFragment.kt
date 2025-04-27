package com.numplates.nomera3.modules.moments.show.presentation.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.meera.core.base.BasePermission
import com.meera.core.base.BasePermissionDelegate
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.dialogs.MeeraConfirmDialogBuilder
import com.meera.core.dialogs.MeeraConfirmVariantDialogBuilder
import com.meera.core.dialogs.MeeraConfirmVariantType
import com.meera.core.dialogs.unlimiteditem.MeeraConfirmDialogUnlimitedListBuilder
import com.meera.core.dialogs.unlimiteditem.MeeraConfirmDialogUnlimitedNumberItemsAction
import com.meera.core.dialogs.unlimiteditem.MeeraConfirmDialogUnlimitedNumberItemsData
import com.meera.core.extensions.click
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.dp
import com.meera.core.extensions.empty
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.safeNavigate
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.visible
import com.meera.core.utils.layouts.intercept.InterceptTouchFrameLayout
import com.meera.core.utils.showCommonError
import com.meera.core.utils.showCommonSuccessMessage
import com.meera.core.utils.timeAgo
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.snackbar.AvatarUiState
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.meera.uikit.widgets.snackbar.SnackLoadingUiState
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraMomentPositionFragmentBinding
import com.numplates.nomera3.modules.baseCore.helper.SaveMediaFileDelegate
import com.numplates.nomera3.modules.baseCore.helper.SaveMediaFileDelegateImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhence
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.ComplainExtraActions
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudePropertyMomentHowScreenClosed
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudePropertyMomentMenuActionType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.AmplitudePropertyReactionWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.AmplitudePropertyReactionsContentType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.AmplitudePropertyReactionsPostType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.AmplitudeReactionsParams
import com.numplates.nomera3.modules.communities.utils.copyCommunityLink
import com.numplates.nomera3.modules.complains.ui.ComplainEvents
import com.numplates.nomera3.modules.complains.ui.ComplainsNavigator
import com.numplates.nomera3.modules.complains.ui.UserComplainViewModel
import com.numplates.nomera3.modules.feed.ui.adapter.MeeraContentActionBar
import com.numplates.nomera3.modules.moments.comments.presentation.MeeraMomentCommentsBottomSheetFragment
import com.numplates.nomera3.modules.moments.show.data.ARG_MOMENT_ID
import com.numplates.nomera3.modules.moments.show.data.entity.MomentContentType
import com.numplates.nomera3.modules.moments.show.domain.CommentsAvailabilityType
import com.numplates.nomera3.modules.moments.show.domain.GetMomentDataUseCase
import com.numplates.nomera3.modules.moments.show.presentation.ViewMomentPositionViewModel
import com.numplates.nomera3.modules.moments.show.presentation.custom.MomentTouchHandler
import com.numplates.nomera3.modules.moments.show.presentation.custom.ViewHider
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentGroupPositionType
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentItemUiModel
import com.numplates.nomera3.modules.moments.show.presentation.dialog.MeeraMomentMenuAction
import com.numplates.nomera3.modules.moments.show.presentation.mapper.toMeeraContentActionBarParams
import com.numplates.nomera3.modules.moments.show.presentation.player.MeeraMomentsImagePlayer
import com.numplates.nomera3.modules.moments.show.presentation.player.MeeraMomentsPlayerWrapper
import com.numplates.nomera3.modules.moments.show.presentation.player.MeeraMomentsUnavailablePlayer
import com.numplates.nomera3.modules.moments.show.presentation.player.MeeraMomentsVideoPlayer
import com.numplates.nomera3.modules.moments.show.presentation.player.MomentsExoPlayerManager
import com.numplates.nomera3.modules.moments.show.presentation.viewevents.PositionViewMomentEvent
import com.numplates.nomera3.modules.moments.show.presentation.viewstates.MeeraPositionViewMomentState
import com.numplates.nomera3.modules.moments.show.presentation.viewstates.MomentMessageState
import com.numplates.nomera3.modules.moments.show.presentation.viewstates.MomentPlaybackListener
import com.numplates.nomera3.modules.moments.show.presentation.viewstates.MomentPlaybackState
import com.numplates.nomera3.modules.moments.show.presentation.viewstates.MomentScreenActionEvent
import com.numplates.nomera3.modules.moments.show.presentation.viewstates.ViewMomentNavigationState
import com.numplates.nomera3.modules.moments.util.getMeeraMomentActionBarDefaultParams
import com.numplates.nomera3.modules.moments.util.isSmallScreen
import com.numplates.nomera3.modules.reaction.data.ReactionType
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity
import com.numplates.nomera3.modules.reaction.ui.MeeraReactionBubbleViewController
import com.numplates.nomera3.modules.reaction.ui.custom.FlyingReaction
import com.numplates.nomera3.modules.reaction.ui.data.MeeraReactionSource
import com.numplates.nomera3.modules.reaction.ui.util.reactionCount
import com.numplates.nomera3.modules.reactionStatistics.ui.MeeraReactionsStatisticsBottomDialogFragment
import com.numplates.nomera3.modules.reactionStatistics.ui.MeeraReactionsStatisticsBottomSheetFragment
import com.numplates.nomera3.modules.reactionStatistics.ui.ReactionsEntityType
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseFragment
import com.numplates.nomera3.modules.redesign.util.needAuthToNavigate
import com.numplates.nomera3.modules.registration.ui.RegistrationContainerFragment
import com.numplates.nomera3.modules.screenshot.delegate.SAVING_PICTURE_DELAY
import com.numplates.nomera3.modules.screenshot.delegate.ScreenshotPopupController
import com.numplates.nomera3.modules.screenshot.ui.entity.ScreenshotPlace
import com.numplates.nomera3.modules.screenshot.ui.entity.ScreenshotPopupData
import com.numplates.nomera3.modules.screenshot.ui.fragment.ScreenshotTakenListener
import com.numplates.nomera3.modules.user.ui.fragments.MeeraAdditionalComplainCallback
import com.numplates.nomera3.modules.user.ui.fragments.MeeraComplaintDialog
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum
import com.numplates.nomera3.presentation.router.BaseBottomSheetDialogFragment
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_COMMENT_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_TRANSIT_FROM
import com.numplates.nomera3.presentation.utils.ReactionAnimationHelper
import com.numplates.nomera3.presentation.view.ui.BottomSheetDialogEventsListener
import com.numplates.nomera3.presentation.view.ui.CloseTypes
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraMenuBottomSheet
import com.numplates.nomera3.presentation.view.ui.bottomMenu.ReactionsStatisticBottomMenu
import com.numplates.nomera3.presentation.view.utils.sharedialog.MeeraShareSheet
import com.numplates.nomera3.presentation.view.utils.sharedialog.ShareBottomSheetEvent
import com.numplates.nomera3.presentation.view.utils.sharedialog.ShareDialogType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

private const val ARG_MOMENT_DIALOGS_COUNT = "ARG_MOMENT_DIALOGS_COUNT"
private const val MEERA_COMPLAINT_DIALOG = "MeeraComplaintDialog"
private const val PADDING_VERIFIED_NAME = 4
private const val CONTENT_TOP_MARGIN = 8
private const val SNACK_MARGIN_BOTTOM = 100
private const val DELAY_ON_RESUME_ACTIONS = 200L

class MeeraViewMomentPositionFragment : MeeraBaseFragment(layout = R.layout.meera_moment_position_fragment),
    SaveMediaFileDelegate by SaveMediaFileDelegateImpl(),
    BasePermission by BasePermissionDelegate(),
    BottomSheetDialogEventsListener,
    BaseBottomSheetDialogFragment.Listener,
    MeeraMomentCommentsBottomSheetFragment.CommentsListener,
    MeeraReactionsStatisticsBottomSheetFragment.ViewsCountListener,
    MeeraMenuBottomSheet.Listener,
    ScreenshotTakenListener {

    private val binding by viewBinding(MeeraMomentPositionFragmentBinding::bind)
    private val userComplainViewModel by viewModels<UserComplainViewModel> { App.component.getViewModelFactory() }

    private var parentViewPagerListener: ViewMomentPagerParent? = null

    private val viewModel by viewModels<ViewMomentPositionViewModel> { App.component.getViewModelFactory() }
    private val complainsNavigator by lazy(LazyThreadSafetyMode.NONE) { ComplainsNavigator(requireActivity()) }
    private val actionBarListener = ActionBarListener()
    private val momentPlaybackListener = PlaybackListener()
    private val viewHider = ViewHider()
    private var currentItem: MomentItemUiModel? = null
    private var momentsPlayer: MeeraMomentsPlayerWrapper? = null
    private var dialogsCount = 0
    private var momentsSource: GetMomentDataUseCase.MomentsSource? = null
    private var momentTouchHandler: MomentTouchHandler? = null
    private var playerHandler: MomentsExoPlayerManager? = null
    private var globalFragmentLifecycleImpl: GlobalFragmentsLifecycleImpl? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var lastMomentSharedWithScreenshotId: Long? = null
    private var screenshotPopupData: ScreenshotPopupData? = null
    private var isSavingMomentPhoto = false
    private var commentAvailabilityLocal: CommentsAvailabilityType? = null
    private var reactionAnimationHelper = ReactionAnimationHelper()

    private val act: MeeraAct by lazy {
        requireActivity() as MeeraAct
    }

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
        initActionBar()
        initTouchHandler()
        initPlayers()
        initClicks()
        showShimmer()
        handleViewUpdates()
        initMomentData()
        registerFragmentsLifecycleChange()
        initPermissionDelegate(requireActivity(), viewLifecycleOwner)
        initComplainEventsObserver()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(ARG_MOMENT_DIALOGS_COUNT, dialogsCount)
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        doDelayed(DELAY_ON_RESUME_ACTIONS, {
            viewModel.onTriggerViewEvent(PositionViewMomentEvent.OnFragmentResumed(isDialogsCreated()))
        })
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

    fun onStartFragment() {
        if (this is MeeraViewMomentPositionFragment) {
            if (!isDialogsCreated()) resumeMoment()
            setSwipeState(lockSwipe = true)
            if (isSmallScreen()) hideSystemUi()
        }
        registerComplaintListener()
//        super.onStartFragment()
    }

    fun onStopFragment() {
        preventSwipeForAuth()
        act.getMeeraReactionBubbleViewController().hideReactionBubble()
        if (isSmallScreen()) showSystemUi()
        unregisterComplaintListener()
//        super.onStopFragment()
    }

    override fun onDestroyView() {
        momentsPlayer?.releasePlayers()
        momentsPlayer = null
        if (parentViewPagerListener == null) {
            playerHandler?.releasePlayer()
        }
        unRegisterFragmentsLifecycleChange()
        binding.stvViewMomentSelectedTrack.hide()
        super.onDestroyView()
    }

    override fun onDetach() {
        super.onDetach()
        parentViewPagerListener = null
    }

    fun onStartAnimationTransitionFragment() {
//        super.onStartAnimationTransitionFragment()
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
//        super.onCreateDialog()
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
        if (isResumed) {
            viewModel.onTriggerViewEvent(PositionViewMomentEvent.ResumePositionMoment)
        }
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
        binding.vTapHandler.setOnTouchListener(if (enable) momentTouchHandler else null)
    }

    fun getCurrentItemId() = currentItem?.id

    private fun setSwipeState(lockSwipe: Boolean) {
        lockSwipe
//        act.navigatorViewPager.let { if (lockSwipe) it.lockSwipe() else it.unlockSwipe() }
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
        val momentUserId = args.getLong(ARG_MOMENT_USER_ID, -1L)
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
        binding.apply {
            playerHandler = parentViewPagerListener?.getPlayerHandler() ?: MomentsExoPlayerManager(requireContext())
            val playerManager = playerHandler ?: run {
                close()
                return
            }
            val videoPlayer = MeeraMomentsVideoPlayer(
                videoView = pvViewMomentVideo,
                previewView = ivViewMomentVideoPreview,
                loaderView = cpiViewMomentLoader,
                momentPlaybackListener = momentPlaybackListener,
                playerHandler = playerManager
            )
            val imagePlayer = MeeraMomentsImagePlayer(
                imageView = ivViewMomentImage,
                loaderView = cpiViewMomentLoader,
                momentPlaybackListener = momentPlaybackListener
            )
            val unavailablePlayer = MeeraMomentsUnavailablePlayer(
                layoutBinding = incMomentsUnavailableLayout,
                loaderView = cpiViewMomentLoader,
                momentPlaybackListener = momentPlaybackListener
            )
            momentsPlayer = MeeraMomentsPlayerWrapper(
                momentPlayers = listOf(videoPlayer, imagePlayer, unavailablePlayer),
                progressView = binding.epmvMomentsProgressBar
            )
        }
    }

    private fun initActionBar() {
        val isMomentViewsToggleEnabled = viewModel.getFeatureTogglesContainer().momentViewsFeatureToggle.isEnabled
        binding.cabViewMomentActionBar.resetReactionsAnimation()
        binding.cabViewMomentActionBar.init(
            params = getMeeraMomentActionBarDefaultParams(),
            callbackListener = actionBarListener,
            isNeedToShowRepost = false,
            momentViewsToggleEnabled = isMomentViewsToggleEnabled
        )
        context.getStatusBarHeight().let {
            binding.vgMomentAspectRatioContainer.setMargins(top = it)
            binding.epmvMomentsProgressBar.setMargins(top = CONTENT_TOP_MARGIN.dp + it)
        }
    }

    private fun hideUserInfoElements() {
        binding.apply {
            vvViewMomentAvatar.gone()
            vgViewMomentNameContainer.gone()
            tvViewMomentName.gone()
            tvViewMomentGeoLocation.gone()
            vTopGradient.gone()
        }
    }

    private fun showUserInfoElements() {
        binding.apply {
            vvViewMomentAvatar.visible()
            vgViewMomentNameContainer.visible()
            tvViewMomentName.visible()
            tvViewMomentGeoLocation.visible()
            vTopGradient.visible()
        }
    }

    private fun initViews(state: MeeraPositionViewMomentState.UpdateMoment) {
        setupUserAvatar()
        setupUserInfo()
        setupMenu(state)
        setMaxWidthName()
        setupMomentTime()
        setupMomentContent(state)
        setupActionBar()
        setupMusicView()
    }

    private fun setupMenu(state: MeeraPositionViewMomentState.UpdateMoment) {
        binding.ivViewMomentMenu.isEnabled = state.momentItemModel?.isInteractionAllowed() ?: false
    }

    private fun setErrorState(errorState: MeeraPositionViewMomentState.UpdateMoment) {
        momentsPlayer?.setupErrorContent(errorState)
        binding.cabViewMomentActionBar.gone()
    }

    private fun setupUserAvatar() {
        val ctx = context ?: return
        val data = currentItem ?: return
        binding.vvViewMomentAvatar.setUp(
            context = ctx,
            avatarLink = data.userAvatarSmall,
            accountType = data.userAccountType,
            frameColor = data.userAccountColor
        )
    }

    private fun setupUserInfo() {
        val data = currentItem ?: return
        binding.tvViewMomentName.text = data.userName
        if (data.userApproved) {
            binding.tvViewMomentName.setCompoundDrawablesRelativeWithIntrinsicBounds(
                0, 0, R.drawable.ic_moment_filled_verified_s, 0
            )
            binding.tvViewMomentName.compoundDrawablePadding = PADDING_VERIFIED_NAME.dp
        }
        if (data.userTopContentMaker) {
            binding.tvViewMomentName.setCompoundDrawablesRelativeWithIntrinsicBounds(
                0, 0, R.drawable.ic_filled_verified_flame_s_colored, 0
            )
            binding.tvViewMomentName.compoundDrawablePadding = PADDING_VERIFIED_NAME.dp
        }
        binding.tvViewMomentGeoLocation.text = data.place
    }

    private fun setMaxWidthName() {
        val nameContainer = binding.vgViewMomentNameContainer ?: return
        nameContainer.post {
            val containerWidth = nameContainer.width
            binding.tvViewMomentName.maxWidth = containerWidth
        }
    }

    private fun setupMomentTime() {
        val data = currentItem ?: return

        val dateText = timeAgo(requireContext(), data.createdAt)
        binding.tvViewMomentDate.text = dateText
    }

    private fun setupMomentContent(state: MeeraPositionViewMomentState.UpdateMoment) {
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

    private fun setupAdultContent(state: MeeraPositionViewMomentState.UpdateMoment) {
        val pausedState = state.copy(playbackState = MomentPlaybackState.PAUSED)
        momentsPlayer?.setupMomentContent(pausedState)
        toggleTouchEvents(enable = false)
        binding.cabViewMomentActionBar.gone()

        with(requireNotNull(binding.incMomentsAgeRestrictions)) {
            root.visible()
            ivMomentPreview.loadGlide(state.momentItemModel?.contentPreview)
            btnShow.click {
                root.gone()
                binding.cabViewMomentActionBar.visible()
                momentsPlayer?.updatePlaybackState(MomentPlaybackState.RESUMED)
            }
        }
    }

    private fun setupActionBar() {
        val data = currentItem ?: return
        val isMomentViewsToggleEnabled = viewModel.getFeatureTogglesContainer().momentViewsFeatureToggle.isEnabled
        binding.cabViewMomentActionBar.resetReactionsAnimation()
        binding.cabViewMomentActionBar.init(
            params = data.toMeeraContentActionBarParams(
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
            binding.stvViewMomentSelectedTrack.hide()
            return
        }

        binding.stvViewMomentSelectedTrack.setContentInfo(
            artistName = mediaData.artist ?: String.empty(),
            trackName = mediaData.track ?: String.empty(),
            clickListener = ::openMusicUrl
        )
        binding.stvViewMomentSelectedTrack.show()
    }

    private fun openMusicUrl() {
        val data = currentItem ?: return
        val mediaData = data.media ?: return
        act.emitDeeplinkCall(mediaData.trackUrl)
    }

    private fun hideShimmer(hasError: Boolean) {
        binding.vgShimmerLayout.root.gone()
        if (hasError.not()) {
            showUserInfoElements()
        }
        binding.cpiViewMomentLoader.hide()
    }

    private fun showShimmer() {
        binding.vgShimmerLayout.root.visible()
        hideUserInfoElements()
    }

    private fun handleViewUpdates() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.liveMomentMeeraState.collect { state ->
                when (state) {
                    is MeeraPositionViewMomentState.UpdateMoment -> {
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

                    is MeeraPositionViewMomentState.LinkCopied -> {
                        copyCommunityLink(context, state.copyLink) {
                            showCommonSuccessMessage(
                                content = getText(R.string.copy_link_success),
                                view = requireView(),
                                paddingBottom = SNACK_MARGIN_BOTTOM.dp
                            )
                        }
                    }

                    else -> Unit
                }
            }
        }
        viewModel.liveMomentMessageState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is MomentMessageState.ShowError -> {
                    showCommonError(
                        content = getText(state.error),
                        view = requireView(),
                        paddingBottom = SNACK_MARGIN_BOTTOM.dp
                    )
                }

                is MomentMessageState.ShowSuccess -> {
                    showCommonSuccessMessage(
                        content = getText(state.message),
                        view = requireView(),
                        paddingBottom = SNACK_MARGIN_BOTTOM.dp
                    )
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
//                    act.getReactionBubbleViewController().hideReactionBubble()
                    binding.cabViewMomentActionBar.resetReactionBubbleAppearance()
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

                is MomentScreenActionEvent.ShowCommonError ->
                    showCommonError(
                        content = getText(event.messageResId),
                        view = requireView(),
                        paddingBottom = SNACK_MARGIN_BOTTOM.dp
                    )

                is MomentScreenActionEvent.OpenComplaintMenu -> openComplainDialog()
            }
        }
    }

    private fun showScreenshotPopup(momentLink: String) {
        val screenshotPopupData = screenshotPopupData?.apply { link = momentLink } ?: return
        ScreenshotPopupController.show(this, screenshotPopupData, this)
    }

    private fun updateActionBar(reactionSource: MeeraReactionSource) {
        val data = currentItem ?: return
        binding.cabViewMomentActionBar.update(
            params = data.toMeeraContentActionBarParams(
                isAuthed = viewModel.isUserAuthorized(),
                isAuthor = viewModel.isMomentAuthor(data.userId)
            ),
            reactionHolderViewId = reactionSource.reactionHolderViewId
        )
    }

    private fun initClicks() {
        binding.apply {
            ivViewMomentClose.setThrottledClickListener {
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
        findNavController().safeNavigate(
            resId = R.id.action_meeraViewMomentFragment_to_userInfoFragment,
            bundle = bundleOf(
                IArgContainer.ARG_USER_ID to data.userId,
                IArgContainer.ARG_TRANSIT_FROM to AmplitudePropertyWhere.MOMENT.property
            )
        )
    }

    @SuppressLint("SuspiciousIndentation")
    private fun openMomentsMenu() {
        val data = currentItem ?: return
//        needAuth {
        if (viewModel.isMomentAuthor(data.userId)) {
            openAuthorDialog()
        } else if (data.isActive) {
            openWatcherDialog()
        }
//        }
    }

    private fun openAuthorDialog() {
        var selectedAction: MeeraConfirmDialogUnlimitedNumberItemsAction? = null
        MeeraConfirmDialogUnlimitedListBuilder()
            .setHeader(R.string.actions)
            .setListItems(initListItemMomentAutorMenu())
            .setItemListener { action ->
                selectedAction = action
                initMomentMenuListener(action as MeeraMomentMenuAction)
            }
            .setDismissListener {
                if (selectedAction == null) {
                    resumeMoment()
                }
            }.show(childFragmentManager)

        pauseMoment()
    }

    private fun initMomentMenuListener(
        action: MeeraMomentMenuAction
    ) {
        val userId = currentItem?.userId ?: return
        when (action) {
            MeeraMomentMenuAction.AllowComments -> {
                openAllowCommentsDialog()
            }

            MeeraMomentMenuAction.CopyLink -> {
                viewModel.onTriggerViewEvent(PositionViewMomentEvent.CopyLink)
                resumeMoment()
            }

            MeeraMomentMenuAction.Delete -> {
                showConfirmDialogDelete()
            }

            MeeraMomentMenuAction.MomentDownload -> {
                saveMomentMedia()
                resumeMoment()
            }

            MeeraMomentMenuAction.Share -> {
                meeraOpenRepost()
                viewModel.logMomentMenuAction(actionType = AmplitudePropertyMomentMenuActionType.SHARE)
            }

            MeeraMomentMenuAction.Settings -> {
                openMomentSettings()
            }

            MeeraMomentMenuAction.ComplainMoment -> {
                viewModel.onTriggerViewEvent(PositionViewMomentEvent.ComplainToMoment)
            }

            MeeraMomentMenuAction.HideMomentUser -> {
                viewModel.onTriggerViewEvent(PositionViewMomentEvent.HideUserMoments(userId))
            }

            MeeraMomentMenuAction.ShowMomentUser -> {
                viewModel.onTriggerViewEvent(PositionViewMomentEvent.ShowUserMoments(userId))
            }
        }
    }

    private fun initListItemMomentAutorMenu(): List<MeeraConfirmDialogUnlimitedNumberItemsData> {
        return listOf(
            MeeraConfirmDialogUnlimitedNumberItemsData(
                name = R.string.general_share,
                icon = R.drawable.ic_outlined_repost_m,
                action = MeeraMomentMenuAction.Share,
            ),
            MeeraConfirmDialogUnlimitedNumberItemsData(
                name = R.string.copy_link,
                icon = R.drawable.ic_outlined_copy_m,
                action = MeeraMomentMenuAction.CopyLink,
            ),
            MeeraConfirmDialogUnlimitedNumberItemsData(
                name = R.string.moment_author_menu_download,
                icon = R.drawable.ic_outlined_download_m,
                action = MeeraMomentMenuAction.MomentDownload,
            ),
            MeeraConfirmDialogUnlimitedNumberItemsData(
                name = R.string.meera_moment_author_menu_allow_comments,
                icon = R.drawable.ic_outlined_message_m,
                action = MeeraMomentMenuAction.AllowComments,
            ),
            MeeraConfirmDialogUnlimitedNumberItemsData(
                name = R.string.moment_author_menu_settings,
                icon = R.drawable.ic_outlined_settings_m,
                contentColor = R.color.ui_black,
                action = MeeraMomentMenuAction.Settings,
            ),
            MeeraConfirmDialogUnlimitedNumberItemsData(
                name = R.string.meera_moment_author_menu_delete,
                icon = R.drawable.ic_outlined_archive_m,
                contentColor = R.color.uiKitColorAccentWrong,
                action = MeeraMomentMenuAction.Delete,
            )
        )
    }

    private fun openMomentSettings() {
        viewModel.onTriggerViewEvent(PositionViewMomentEvent.OnSettingsOpened)
        findNavController().safeNavigate(
            resId = R.id.action_meeraViewMomentFragment_to_meeraMomentSettingsFragment
        )
    }

    private fun openAllowCommentsDialog() {
        val commentAvailability = currentItem?.commentAvailability ?: return
        MeeraConfirmVariantDialogBuilder()
            .setHeader(R.string.moment_settings_allow_comments)
            .setFirstCellText(R.string.everyone)
            .setFirstCellIcon(R.drawable.ic_outlined_planet_m)
            .setSecondCellText(R.string.friends)
            .setSecondCellIcon(R.drawable.ic_outlined_user_m)
            .setThirdCellText(R.string.nobody)
            .setThirdCellIcon(R.drawable.ic_outlined_circle_block_m)
            .setSelectOption(
                getConfirmVariantType(commentAvailabilityLocal?.ordinal ?: commentAvailability.ordinal)
            )
            .setBackBtnListener { openAuthorDialog() }
            .setVariantCellListener {
                commentAvailabilityLocal = getTypeConfirmDialog(it)
                viewModel.onTriggerViewEvent(
                    PositionViewMomentEvent.SetMomentCommentAvailability(getTypeConfirmDialog(it))
                )
            }
            .show(childFragmentManager)
        pauseMoment()
        viewModel.logMomentMenuAction(actionType = AmplitudePropertyMomentMenuActionType.ALLOW_COMMENTS)
    }

    private fun getTypeConfirmDialog(type: MeeraConfirmVariantType): CommentsAvailabilityType {
        return when (type) {
            MeeraConfirmVariantType.THIRD -> CommentsAvailabilityType.NOBODY
            MeeraConfirmVariantType.SECOND -> CommentsAvailabilityType.FRIENDS
            MeeraConfirmVariantType.FIRST -> CommentsAvailabilityType.ALL
        }
    }

    private fun getConfirmVariantType(selectVariant: Int): MeeraConfirmVariantType {
        return when (selectVariant) {
            SettingsUserTypeEnum.NOBODY.ordinal -> MeeraConfirmVariantType.FIRST
            SettingsUserTypeEnum.FRIENDS.ordinal -> MeeraConfirmVariantType.THIRD
            SettingsUserTypeEnum.ALL.ordinal -> MeeraConfirmVariantType.SECOND
            else -> MeeraConfirmVariantType.SECOND
        }
    }

    private fun openWatcherDialog() {
        MeeraConfirmDialogUnlimitedListBuilder()
            .setHeader(R.string.actions)
            .setListItems(initListItemMomentWatcherMenu())
            .setItemListener { action ->
                initMomentMenuListener(action as MeeraMomentMenuAction)
            }
            .setDismissListener {
                resumeMoment()
            }.show(childFragmentManager)

        pauseMoment()
    }

    private fun initListItemMomentWatcherMenu(): List<MeeraConfirmDialogUnlimitedNumberItemsData> {
        return listOf(
            MeeraConfirmDialogUnlimitedNumberItemsData(
                name = R.string.general_share,
                icon = R.drawable.ic_outlined_repost_m,
                action = MeeraMomentMenuAction.Share,
            ),
            MeeraConfirmDialogUnlimitedNumberItemsData(
                name = R.string.copy_link,
                icon = R.drawable.ic_outlined_copy_m,
                action = MeeraMomentMenuAction.CopyLink,
            ),
            allowHideMoments(),
            MeeraConfirmDialogUnlimitedNumberItemsData(
                name = R.string.complain_about_moment,
                icon = R.drawable.ic_outlined_attention_m,
                contentColor = R.color.uiKitColorAccentWrong,
                action = MeeraMomentMenuAction.ComplainMoment,
            )
        )
    }

    private fun allowHideMoments(): MeeraConfirmDialogUnlimitedNumberItemsData {
        return if (currentItem?.doNotShowUser == false) {
            MeeraConfirmDialogUnlimitedNumberItemsData(
                name = R.string.moments_bottom_menu_hide_user,
                icon = R.drawable.ic_outlined_eye_off_m,
                contentColor = R.color.uiKitColorAccentWrong,
                action = MeeraMomentMenuAction.HideMomentUser,
            )
        } else {
            MeeraConfirmDialogUnlimitedNumberItemsData(
                name = R.string.moments_bottom_menu_show_user,
                icon = R.drawable.ic_outlined_eye_off_m,
                action = MeeraMomentMenuAction.ShowMomentUser,
            )
        }
    }

    private fun showComplaintInfoSnackbar(msg: Int) {
        UiKitSnackBar.make(
            view = requireView(),
            params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = getText(msg),
                    avatarUiState = AvatarUiState.SuccessIconState
                ),
                duration = BaseTransientBottomBar.LENGTH_SHORT,
                dismissOnClick = true,
            )
        ).apply {
            setAnchorView(anchorView)
            show()
        }
    }

    private fun openComplainDialog() {
        val momentData = currentItem ?: return
        showAdditionalStepsForComplain(momentData.userId)
        pauseMoment()
    }

    private fun initComplainEventsObserver() {
        userComplainViewModel.complainEvents.onEach { event ->
            when (event) {
                is ComplainEvents.ComplainFailed -> {
                    showComplaintInfoSnackbar(R.string.user_complain_error)
                    userComplainViewModel.logAdditionalEvent(ComplainExtraActions.NONE)
                }

                is ComplainEvents.RequestModerators -> {
                    showComplaintInfoSnackbar(R.string.meera_user_complain_request_moderator)
                }

                else -> Unit
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun showAdditionalStepsForComplain(remoteUserId: Long) {
        MeeraComplaintDialog
            .newInstance(userId = remoteUserId, optionHideMoments = true)
            .also { dialog ->
                dialog.setComplaintCallback(callback = object : MeeraAdditionalComplainCallback {
                    override fun onSuccess(msg: Int?, reason: ComplainEvents, userId: Long?) {
                        complaintAction(msg, reason, userId)
                    }

                    override fun onError(msg: Int?) {
                        msg?.let { showComplaintInfoSnackbar(msg) }
                    }
                })
                dialog.show(childFragmentManager, MEERA_COMPLAINT_DIALOG)
                pauseMoment()
            }
    }

    private fun complaintAction(msg: Int?, reason: ComplainEvents, userId: Long?) {
        when (reason) {
            ComplainEvents.UserBlocked -> {
                msg?.let {
                    showComplaintInfoSnackbar(msg)
                    userComplainViewModel.blockUser(userId, true)
                    userComplainViewModel.logAdditionalEvent(ComplainExtraActions.BLOCK)
                }
            }

            is ComplainEvents.MomentsHidden -> {
                msg?.let {
                    showComplaintInfoSnackbar(msg)
                    userComplainViewModel.hideUserMoments(userId)
                }
            }

            ComplainEvents.PostsDisabledEvents -> {
                msg?.let {
                    showComplaintInfoSnackbar(msg)
                    userComplainViewModel.hideUserRoad(userId)
                    userComplainViewModel.logAdditionalEvent(ComplainExtraActions.HIDE)
                }
            }

            ComplainEvents.RequestModerators -> {
                userComplainViewModel.hideUserRequestModerators()
            }

            else -> Unit
        }
    }

    private fun getMomentSavePermission(momentExternalUrl: String, permissionGranted: () -> Unit) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            setPermissions(
                listener = object : com.meera.core.permission.PermissionDelegate.Listener {
                    override fun onGranted() {
                        permissionGranted()
                    }

                    override fun onDenied() {
                        UiKitSnackBar.make(
                            view = requireView(),
                            params = SnackBarParams(
                                snackBarViewState = SnackBarContainerUiState(
                                    messageText = getText(R.string.you_must_grant_permissions),
                                    loadingUiState = SnackLoadingUiState.DonutProgress(
                                        timerStartSec = SAVING_PICTURE_DELAY
                                    ),
                                    buttonActionText = getText(R.string.general_retry),
                                    buttonActionListener = {
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
                                ),
                                duration = BaseTransientBottomBar.LENGTH_INDEFINITE
                            )
                        ).show()
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
        showCommonError(
            content = getText(R.string.moment_save_unknown_error),
            view = requireView(),
            paddingBottom = SNACK_MARGIN_BOTTOM.dp
        )
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
                showCommonSuccessMessage(
                    content = getText(R.string.moment_download_success),
                    view = requireView(),
                    paddingBottom = SNACK_MARGIN_BOTTOM.dp
                )
//                doDelayed(SAVING_PICTURE_DELAY) { isSavingMomentPhoto = false }
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

        MeeraConfirmDialogBuilder()
            .setHeader(R.string.moment_dialog_delete_moment_title)
            .setDescription(R.string.moment_dialog_delete_moment_description)
            .setTopBtnText(R.string.moment_dialog_delete_moment_confirm)
            .setBottomBtnText(R.string.cancel)
            .setTopClickListener { viewModel.onTriggerViewEvent(PositionViewMomentEvent.DeletePositionMoment) }
            .setBottomClickListener { resumeMoment() }
            .setDialogCancelledListener { resumeMoment() }
            .show(childFragmentManager)
    }

    private fun getControlViewsList(): List<View> {
        val viewGroupChildren = binding.root.children.toList()
        return viewGroupChildren.filter { it.tag == "view_may_be_hidden" }
    }

    private fun openComments(commentToOpenId: Long? = null) {
        val momentItem = currentItem ?: return

        MeeraMomentCommentsBottomSheetFragment.show(
            model = momentItem,
            fragmentManager = parentFragmentManager,
            commentToOpenId = commentToOpenId,
            listener = this,
            commentsListener = this
        )
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
                        showCommonSuccessMessage(getText(R.string.moment_share_success), requireView())
                    }

                    is ShareBottomSheetEvent.OnErrorUnselectedUser -> {
                        showCommonError(getText(R.string.no_user_selected), requireView())
                    }

                    is ShareBottomSheetEvent.OnDismissDialog -> resumeMoment()

                    else -> Unit
                }
            }
        )

        pauseMoment()
    }

    private fun close() {
        val parentFragment = parentFragment as? MeeraViewMomentFragment?
        if (parentFragment != null) {
            parentFragment.close()
        } else {
            findNavController().popBackStack()
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
//        if (act?.getCurrentFragment() !is RegistrationContainerFragment) {
//            setSwipeState(lockSwipe = false)
//        }
    }

    private inner class ActionBarListener : MeeraContentActionBar.Listener {

        override fun onCommentsClick() = needAuthToNavigate {
            openComments()
            currentItem?.let { viewModel.logOnCommentsClicked(it) }
        }

        override fun onRepostClick() = needAuthToNavigate {
            meeraOpenRepost()
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
            reactionHolderViewId: MeeraContentActionBar.ReactionHolderViewId,
        ) {
            val data = currentItem ?: return
            val params = data.toMeeraContentActionBarParams(
                viewModel.isUserAuthorized(),
                viewModel.isMomentAuthor(data.userId)
            )
            val controller = act.getMeeraReactionBubbleViewController()
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
                reactionSource = MeeraReactionSource.Moment(
                    momentId = data.id,
                    reactionHolderViewId = reactionHolderViewId
                ),
                showPoint = showPoint,
                viewsToHide = viewsToHide,
                reactionTip = reactionTip,
                currentReactionsList = data.reactions,
                contentActionBarType = MeeraContentActionBar.ContentActionBarType.getType(params),
                containerInfo = getDefaultReactionContainer(requireActivity()),
                showMorningEvening = false,
                reactionsParams = reactionParams
            )
        }

        override fun onFlyingAnimationInitialized(flyingReaction: FlyingReaction) = Unit

        override fun onReactionRegularClick(reactionHolderViewId: MeeraContentActionBar.ReactionHolderViewId) {
            val data = currentItem ?: return
            val controller = act.getMeeraReactionBubbleViewController()
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
                reactionSource = MeeraReactionSource.Moment(
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

        override fun onReactionClickToShowScreenAnimation(
            reactionEntity: ReactionEntity,
            anchorViewLocation: Pair<Int, Int>
        ) {
            val reactionType = ReactionType.getByString(reactionEntity.reactionType) ?: return
            reactionAnimationHelper.playLottieAtPosition(
                recyclerView = null,
                context = requireContext(),
                parent = binding.root,
                reactionType = reactionType,
                x = anchorViewLocation.first.toFloat(),
                y = anchorViewLocation.second.toFloat()
            )
        }
    }

    private fun getDefaultReactionContainer(activity: Activity): MeeraReactionBubbleViewController.ContainerInfo {
        val interceptLayout = activity.findViewById<InterceptTouchFrameLayout>(R.id.root_intercept_layout_activity)
        return MeeraReactionBubbleViewController.ContainerInfo(
            container = interceptLayout,
            bypassLayouts = listOf(interceptLayout)
        )
    }

    private fun showReactionsAndViewsStatisticsBottomSheet(clickFromReactionsBadge: Boolean = false) {
        val momentItem = currentItem ?: return

        if (viewModel.getFeatureTogglesContainer().detailedReactionsForCommentsFeatureToggle.isEnabled) {
            val isMomentViewsToggleEnabled = viewModel.getFeatureTogglesContainer().momentViewsFeatureToggle.isEnabled

            val entityType = if (viewModel.isMomentAuthor(momentItem.userId) && isMomentViewsToggleEnabled) {
                ReactionsEntityType.MOMENT_WITH_VIEWS
            } else {
                ReactionsEntityType.MOMENT
            }

            Timber.d("$clickFromReactionsBadge")

            MeeraReactionsStatisticsBottomDialogFragment.makeInstance(
                momentItem.id,
                entityType,
                startWithReactionTab = clickFromReactionsBadge
            ) { destination ->
                when (destination) {
                    is MeeraReactionsStatisticsBottomDialogFragment.DestinationTransition.UserProfileDestination -> {
                        openUserFragment(destination.userEntity.userId)
                    }
                }
            }.show(childFragmentManager)
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

    private fun openUserFragment(userId: Long?, where: AmplitudePropertyWhere? = null) {
        findNavController().safeNavigate(
            resId = R.id.action_meeraViewMomentFragment_to_userInfoFragment,
            bundle = bundleOf(
                IArgContainer.ARG_USER_ID to userId,
                ARG_TRANSIT_FROM to where
            )
        )
    }

    private inner class PlaybackListener : MomentPlaybackListener {
        override fun onResourceReady(isPreview: Boolean) {
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
