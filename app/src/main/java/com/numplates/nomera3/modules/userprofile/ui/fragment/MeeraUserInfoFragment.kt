package com.numplates.nomera3.modules.userprofile.ui.fragment

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraCharacteristics
import android.net.Uri
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isInvisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkInfo
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_DRAGGING
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HALF_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_SETTLING
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.meera.core.base.BaseLoadImages
import com.meera.core.base.BaseLoadImagesDelegate
import com.meera.core.base.BasePermission
import com.meera.core.base.BasePermissionDelegate
import com.meera.core.base.enums.PermissionState
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.dialogs.MeeraConfirmDialogBuilder
import com.meera.core.dialogs.unlimiteditem.MeeraConfirmDialogUnlimitedListBuilder
import com.meera.core.dialogs.unlimiteditem.MeeraConfirmDialogUnlimitedNumberItemsData
import com.meera.core.extensions.addAnimationTransitionByDefault
import com.meera.core.extensions.displayWidth
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.dp
import com.meera.core.extensions.getScreenHeight
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.invisible
import com.meera.core.extensions.isFalse
import com.meera.core.extensions.isNotTrue
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.loadGlideFullSizeCircle
import com.meera.core.extensions.openSettingsScreen
import com.meera.core.extensions.safeNavigate
import com.meera.core.extensions.setBackgroundTint
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.toInt
import com.meera.core.extensions.vibrate
import com.meera.core.extensions.visible
import com.meera.core.permission.PERMISSION_MEDIA_CODE
import com.meera.core.permission.PermissionDelegate
import com.meera.core.utils.LocationUtility
import com.meera.core.utils.files.FileUtilsImpl
import com.meera.core.utils.showCommonError
import com.meera.core.utils.showCommonSuccessMessage
import com.meera.core.utils.tedbottompicker.TedBottomSheetDialogFragment
import com.meera.core.utils.tedbottompicker.TedBottomSheetPermissionActionsListener
import com.meera.core.utils.tedbottompicker.models.MediaViewerCameraTypeEnum
import com.meera.db.models.dialog.UserChat
import com.meera.media_controller_api.model.MediaControllerCallback
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.action.UiKitSnackBarActions
import com.meera.uikit.snackbar.state.DismissListeners
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.tooltip.TooltipShowHandler
import com.meera.uikit.tooltip.createTooltip
import com.meera.uikit.widgets.applyRoundedOutline
import com.meera.uikit.widgets.nav.UiKitToolbarViewState
import com.meera.uikit.widgets.people.TopAuthorApprovedUserModel
import com.meera.uikit.widgets.setMargins
import com.meera.uikit.widgets.snackbar.AvatarUiState
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.meera.uikit.widgets.snackbar.SnackLoadingUiState
import com.meera.uikit.widgets.tooltip.TooltipMessage
import com.meera.uikit.widgets.tooltip.UiKitTooltipBubbleMode
import com.meera.uikit.widgets.tooltip.UiKitTooltipViewState
import com.meera.uikit.widgets.userpic.UserpicStoriesStateEnum
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.noomeera.nmravatarssdk.NMR_AVATAR_STATE_JSON_KEY
import com.noomeera.nmravatarssdk.REQUEST_NMR_KEY_AVATAR
import com.noomeera.nmrmediatools.NMRPhotoAmplitude
import com.numplates.nomera3.AnimatedAvatarUtils
import com.numplates.nomera3.App
import com.numplates.nomera3.BuildConfig
import com.numplates.nomera3.FRIEND_STATUS_CONFIRMED
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentUserInfoBinding
import com.numplates.nomera3.domain.interactornew.GetFriendsListUseCase.Companion.SUBSCRIBERS
import com.numplates.nomera3.domain.interactornew.GetFriendsListUseCase.Companion.SUBSCRIPTIONS
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyChatCreatedFromWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeAlertPostWithNewAvatarValuesActionType
import com.numplates.nomera3.modules.chat.KEY_SUBSCRIBED
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitData
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitType
import com.numplates.nomera3.modules.complains.ui.KEY_COMPLAINT_USER_ID
import com.numplates.nomera3.modules.complains.ui.KEY_COMPLAINT_WHERE_VALUE
import com.numplates.nomera3.modules.feed.ui.entity.DestinationOriginEnum
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.fragment.BaseFeedFragment
import com.numplates.nomera3.modules.feed.ui.fragment.MeeraBaseFeedFragment
import com.numplates.nomera3.modules.feed.ui.fragment.NetworkRoadType
import com.numplates.nomera3.modules.feed.ui.viewholder.MeeraBasePostHolder
import com.numplates.nomera3.modules.maps.ui.model.FocusedMapItem
import com.numplates.nomera3.modules.maps.ui.model.MapUserUiModel
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentClickOrigin
import com.numplates.nomera3.modules.moments.show.presentation.fragment.KEY_MOMENT_CLICK_ORIGIN
import com.numplates.nomera3.modules.moments.show.presentation.fragment.KEY_USER_ID
import com.numplates.nomera3.modules.post_view_statistic.data.PostViewRoadSource
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.modules.redesign.fragments.base.DEF_HEIGHT_USERINFO_SNIPPET_CONTAINER
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.modules.redesign.fragments.main.SUBSCRIPTION_ROAD_REQUEST_KEY
import com.numplates.nomera3.modules.redesign.fragments.main.map.NoShowOnMapPlaceholderType
import com.numplates.nomera3.modules.redesign.fragments.main.map.snippet.MeeraOnSwipeTouchListener
import com.numplates.nomera3.modules.redesign.util.NavigationManager
import com.numplates.nomera3.modules.redesign.util.needAuthToNavigate
import com.numplates.nomera3.modules.redesign.util.needAuthToNavigateWithResult
import com.numplates.nomera3.modules.remotestyle.presentation.formatter.AllRemoteStyleFormatter
import com.numplates.nomera3.modules.screenshot.delegate.ScreenshotPopupController
import com.numplates.nomera3.modules.screenshot.ui.entity.ScreenshotPlace
import com.numplates.nomera3.modules.screenshot.ui.entity.ScreenshotPopupData
import com.numplates.nomera3.modules.screenshot.ui.fragment.ScreenshotTakenListener
import com.numplates.nomera3.modules.search.ui.fragment.SearchMainFragment
import com.numplates.nomera3.modules.tags.data.SuggestionsMenuType
import com.numplates.nomera3.modules.tags.ui.base.SuggestionsMenu
import com.numplates.nomera3.modules.upload.util.UPLOAD_BUNDLE_KEY
import com.numplates.nomera3.modules.uploadpost.ui.AddMultipleMediaPostFragment
import com.numplates.nomera3.modules.user.ui.event.PhoneCallsViewEffect
import com.numplates.nomera3.modules.user.ui.event.RoadPostViewEffect
import com.numplates.nomera3.modules.user.ui.event.UserFeedViewEvent
import com.numplates.nomera3.modules.user.ui.event.UserInfoViewEffect
import com.numplates.nomera3.modules.user.ui.event.UserProfileDialogNavigation
import com.numplates.nomera3.modules.user.ui.event.UserProfileNavigation
import com.numplates.nomera3.modules.userprofile.domain.maper.toUserChat
import com.numplates.nomera3.modules.userprofile.profilestatistics.ui.fragments.MeeraProfileStatisticsContainerFragment
import com.numplates.nomera3.modules.userprofile.ui.model.FriendStatus
import com.numplates.nomera3.modules.userprofile.ui.model.PhotoModel
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIAction
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIAction.DeleteVehicle
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIAction.HideVehicle
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIAction.ShowVehicle
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIModel
import com.numplates.nomera3.modules.userprofile.utils.copyProfileLink
import com.numplates.nomera3.modules.userprofile.utils.shareProfileOutside
import com.numplates.nomera3.presentation.birthday.ui.BirthdayBottomDialogFragment
import com.numplates.nomera3.presentation.model.VipStatus
import com.numplates.nomera3.presentation.model.enums.CreateAvatarPostEnum
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_CAR_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_FRIEND_LIST_MODE
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_POST_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_SHOW_MEDIA_GALLERY
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_ID
import com.numplates.nomera3.presentation.view.fragments.FriendsHostOpenedType
import com.numplates.nomera3.presentation.view.fragments.MeeraFriendsHostFragment
import com.numplates.nomera3.presentation.view.fragments.MeeraVehicleInfoFragment.Companion.VEHICLE_INFO_BOTTOM_DIALOG_DELETED
import com.numplates.nomera3.presentation.view.fragments.MeeraVehicleInfoFragment.Companion.VEHICLE_INFO_BOTTOM_DIALOG_KEY
import com.numplates.nomera3.presentation.view.fragments.MeeraVehicleInfoFragment.Companion.VEHICLE_INFO_BOTTOM_DIALOG_UPDATED
import com.numplates.nomera3.presentation.view.fragments.UserInfoFragment
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.callisresrticted.MeeraCallIsRestrictedBottomSheetFragment
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.callisresrticted.MeeraCallIsRestrictedBottomSheetFragment.Companion.ARG_CALL_IS_RESTRICTED_IS_CHAT_CLICKED
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.postavatar.MeeraPostAvatarBottomSheetFragment
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.postavatar.PostAvatarAlertListener
import com.numplates.nomera3.presentation.view.fragments.profilephoto.MeeraProfilePhotoViewerFragment
import com.numplates.nomera3.presentation.view.utils.MeeraCoreTedBottomPickerActDependencyProvider
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.view.utils.apphints.TooltipDuration
import com.numplates.nomera3.presentation.view.utils.sharedialog.MeeraShareSheet
import com.numplates.nomera3.presentation.view.utils.sharedialog.ShareBottomSheetEvent
import com.numplates.nomera3.presentation.view.utils.sharedialog.ShareDialogType
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.math.abs

private const val FIRST_POST = 0
private const val SHOW_SHIMMER_DELAY_MS = 100L
private const val DURATION_CONTACT_ANIMATION = 200L
private const val TRANSITION_Y_CONTACT_ANIMATION = 300f
private const val SHIMMER_MARGIN = 52

class MeeraUserInfoFragment :
    MeeraBaseFeedFragment(R.layout.meera_fragment_user_info, ScreenBehaviourState.ScrollableHalfProfile),
    PostAvatarAlertListener, BaseLoadImages by BaseLoadImagesDelegate(), TedBottomSheetPermissionActionsListener,
    BasePermission by BasePermissionDelegate(), ScreenshotTakenListener {
    override val containerId: Int
        get() = R.id.fragment_first_container_view

    override fun getAmplitudeWhereFromOpened(): AmplitudePropertyWhere = AmplitudePropertyWhere.PROFILE_FEED

    private val binding by viewBinding(MeeraFragmentUserInfoBinding::bind)
    private val viewModel by viewModels<MeeraUserInfoViewModel>() {
        App.component.getViewModelFactory()
    }
    private var isScreenshotPopupShown = false
    private var screenshotPopupData: ScreenshotPopupData? = null
    private var mediaPicker: TedBottomSheetDialogFragment? = null
    private var motionLayoutState: Int? = null
    private var smoothScroller: RecyclerView.SmoothScroller? = null
    private val adapter: UserInfoAdapter by lazy {
        UserInfoAdapter {
            submitUIAction(it)
        }
    }
    private var undoSnackBar: UiKitSnackBar? = null
    var infoSnackbar: UiKitSnackBar? = null

    override fun onClickScrollUpButton() = Unit

    override fun isNotCommunityScreen() = true

    override val needToShowProfile: Boolean
        get() = true

    private val act: MeeraAct by lazy {
        requireActivity() as MeeraAct
    }
    private val checkScrollPositionListener = CheckScrollPositionListenerImpl()
    private var isVisibleConnectionButton = true
    private var numberFloorsProfile = 0
    private val meeraAddMorePhotosTooltip: TooltipShowHandler by lazy {
        createTooltip(
            tooltipState = UiKitTooltipViewState(
                uiKitTooltipBubbleMode = UiKitTooltipBubbleMode.RIGHT_TOP,
                tooltipMessage = TooltipMessage.TooltipMessageString(
                    context?.getString(R.string.meera_profile_tooltip_add_more_photos).orEmpty()
                ),
                showCloseButton = false
            )
        )
    }
    private val meeraPhotosCounterTooltip: TooltipShowHandler by lazy {
        createTooltip(
            tooltipState = UiKitTooltipViewState(
                uiKitTooltipBubbleMode = UiKitTooltipBubbleMode.RIGHT_BOTTOM,
                tooltipMessage = TooltipMessage.TooltipMessageString(
                    context?.getString(R.string.meera_profile_tooltip_photos_counter).orEmpty()
                ),
                showCloseButton = false
            )
        )
    }


    override fun getAnalyticPostOriginEnum(): DestinationOriginEnum = if (viewModel.isMe()) {
        DestinationOriginEnum.OWN_PROFILE
    } else {
        DestinationOriginEnum.OTHER_PROFILE
    }

    override fun getFormatter(): AllRemoteStyleFormatter {
        return AllRemoteStyleFormatter(feedViewModel.getSettings())
    }

    override fun onScreenshotTaken() {
        if (isSavingFeedPhoto) return
        viewModel.handleUIAction(UserProfileUIAction.GetUserDataForScreenshotPopup)
    }

    override fun getPostViewRoadSource(): PostViewRoadSource = PostViewRoadSource.Profile

    private var userProfile: UserProfileUIModel? = null

    private val listener: (requestCode: Int, permissions: Array<String>, grantResults: IntArray) -> Unit =
        { requestCode, permissions, grantResults ->
            if (requestCode == PERMISSION_MEDIA_CODE) {
                if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                    mediaPicker?.updateGalleryPermissionState(PermissionState.GRANTED)
                } else {
                    mediaPicker?.updateGalleryPermissionState(PermissionState.NOT_GRANTED_OPEN_SETTINGS)
                }
            }
        }

    private val sensorManager by lazy { requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    private val accelerometer by lazy { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
    private var sensorListener: SensorEventListener? = null
    private var bottomSheetCallback: BottomSheetBehavior.BottomSheetCallback? = null

    private var snippetOnLayoutChangeListener: OnLayoutChangeListener? = null

    /* Актуальное значение ширины фрагмента, предоставляемое для перерисовывающихся айтемов списка
    при изменении ширины родителя */
    private var currentSnippetLayoutWidth: Int? = null

    private var userId: Long? = null
    private var isSnippet: Boolean? = false
    private var isSnippetCollapsed = false
    private var isUserSnippetDataFull: Boolean? = false
    private var isUserSnippetEvent: Boolean? = false
    private var isOpenFromNotification: Boolean? = false
    override fun ignoreSlide() = !isResumed

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initArguments()
        altSheetBehaviour = when {
            isUserSnippetEvent.isTrue() -> {
                ScreenBehaviourState.BottomScreens(percentHeight = 0.95f, isFullWidth = false, isDraggable = false)
            }

            arguments?.getBoolean(UserInfoFragment.ARG_USER_SNIPPET_DATA).isTrue() -> {
                isApplyNavigationConfig = false
                ScreenBehaviourState.Snippet()
            }

            isUserSnippetDataFull.isTrue() || isOpenFromNotification.isTrue() -> {
                ScreenBehaviourState.BottomScreens(percentHeight = 0.95f)
            }

            else -> ScreenBehaviourState.ScrollableHalfProfile
        }

        setFragmentResultListener(MeeraProfilePhotoViewerFragment.PHOTO_VIEWER_RESULT) { _: String, bundle: Bundle ->
            val argUserId = bundle.getLong(ARG_USER_ID, 0L)
            val argCurrentPosition = bundle.getInt(MeeraProfilePhotoViewerFragment.CURRENT_POSITION, 0)
            val argChanged = bundle.getBoolean(MeeraProfilePhotoViewerFragment.AVATARS_CHANGED, true)
            if (argUserId == userId) {
                viewModel.refreshAvatarsAndSetCurrent(argCurrentPosition, argChanged)
            }
        }

        setFragmentResultListener(VEHICLE_INFO_BOTTOM_DIALOG_KEY) { _, bundle ->
            val shouldDelete = bundle.getBoolean(VEHICLE_INFO_BOTTOM_DIALOG_DELETED, false)
            if (shouldDelete) {
                val vehicleID = bundle.getString(ARG_CAR_ID) ?: return@setFragmentResultListener
                submitUIAction(HideVehicle(vehicleID))
                undoSnackBar?.dismiss()
                undoSnackBar = UiKitSnackBar.make(
                    view = requireView(),
                    params = SnackBarParams(
                        snackBarViewState = SnackBarContainerUiState(
                            messageText = getText(R.string.vehicle_deleted),
                            loadingUiState = SnackLoadingUiState.DonutProgress(
                                timerStartSec = DELAY_DELETE_SNACK_BAR_SEC.toLong(),
                                onTimerFinished = {
                                    submitUIAction(DeleteVehicle(vehicleID))
                                    undoSnackBar?.dismiss()
                                }),
                            buttonActionText = getText(R.string.cancel),
                            buttonActionListener = {
                                submitUIAction(ShowVehicle(vehicleID))
                                undoSnackBar?.dismiss()
                            }),
                        duration = BaseTransientBottomBar.LENGTH_INDEFINITE,
                        dismissOnClick = true,
                        dismissListeners = DismissListeners(dismissListener = {
                            submitUIAction(ShowVehicle(vehicleID))
                            undoSnackBar?.dismiss()
                        })
                    )
                )
                undoSnackBar?.handleSnackBarActions(UiKitSnackBarActions.StartTimerIfNotRunning)
                undoSnackBar?.show()
                return@setFragmentResultListener
            }
            val shouldUpdate = bundle.getBoolean(VEHICLE_INFO_BOTTOM_DIALOG_UPDATED, false)
            if (shouldUpdate) viewModel.refreshProfile()
        }

        initRoadTypeAndViewModel(
            roadType = NetworkRoadType.USER(
                userId,
                BaseFeedFragment.REQUEST_ROAD_TYPE_USER,
                userId == viewModel.getUserUid(),
                selectedPostId = selectedPostId
            )
        )
        doDelayed(SHOW_SHIMMER_DELAY_MS) { showShimmer(true) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getPostIdArgument()
        initRecycler()
        setupFullProfile()
        setupAnimation()
        initButtonsListeners()
        setupLayoutChangeListener()
        NavigationManager.getManager().mainMapFragment.initNavigationButtonsListeners(fromMap = false)
        childFragmentManager.setFragmentResultListener(
            MeeraCallIsRestrictedBottomSheetFragment.ARG_CALL_IS_RESTRICTED_REQUEST_KEY, getViewLifecycleOwner()
        ) { _: String, bundle: Bundle ->
            if (bundle.getBoolean(ARG_CALL_IS_RESTRICTED_IS_CHAT_CLICKED, false)) {
                needAuthToNavigateWithResult(SUBSCRIPTION_ROAD_REQUEST_KEY) {
                    submitUIAction(UserProfileUIAction.StartChatClick)
                }
            }
        }
    }


    override fun onGalleryRequestPermissions() {
        setMediaPermissions()
    }

    override fun onGalleryOpenSettings() {
        requireContext().openSettingsScreen()
    }

    override fun onCameraRequestPermissions(fromMediaPicker: Boolean) {
        setPermissionsWithSettingsOpening(
            listener = object : PermissionDelegate.Listener {
                override fun onGranted() {
                    mediaPicker?.openCamera()
                }

                override fun needOpenSettings() {
                    onCameraOpenSettings()
                }

                override fun onError(error: Throwable?) {
                    onCameraOpenSettings()
                }
            }, Manifest.permission.CAMERA
        )
    }

    override fun onCameraOpenSettings() {
        showCameraSettingsDialog()
    }

    override fun navigateEditPostFragment(post: PostUIEntity?, postStringEntity: String?) {
        findNavController().safeNavigate(
            resId = R.id.action_meeraUserInfoFragment_to_meeraCreatePostFragment, bundle = bundleOf(
                IArgContainer.ARG_GROUP_ID to (post?.groupId?.toInt()),
                IArgContainer.ARG_SHOW_MEDIA_GALLERY to false,
                IArgContainer.ARG_POST to post,
                UPLOAD_BUNDLE_KEY to postStringEntity
            )
        )
    }

    override fun getParentContainer(): ViewGroup = binding.flContainer

    override fun onStateChanged(newState: Int) {
        if (newState == STATE_HALF_EXPANDED) {
            stopVideoIfExists()
        }
        super.onStateChanged(newState)
    }

    fun updateSnippetState(isSnippetCollapsed: Boolean) {
        this.isSnippetCollapsed = isSnippetCollapsed
        viewModel.updateSnippetProfile(isSnippetCollapsed)
    }

    fun setProfileViewed() {
        viewModel.setViewed()
    }

    /* Слушатель необходим для отслеживания изменений ширины родителя (а конкретно сниппета), чтобы отслеживать
    актуальную ширину фрагмента, хранить актуальное значение и перерисовывать элементы списка при необходимости */
    private fun setupLayoutChangeListener() {
        if (isSnippet.isNotTrue() && isUserSnippetDataFull.isNotTrue()) return

        snippetOnLayoutChangeListener = object : OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View?,
                left: Int,
                top: Int,
                right: Int,
                bottom: Int,
                oldLeft: Int,
                oldTop: Int,
                oldRight: Int,
                oldBottom: Int
            ) {
                if (isVisible.not() || userVisibleHint.not()) return
                val newWidth = right - left
                if (currentSnippetLayoutWidth == null) {
                    currentSnippetLayoutWidth = newWidth
                    return
                }
                if (newWidth != currentSnippetLayoutWidth) {
                    currentSnippetLayoutWidth = newWidth
                    updateRecyclerItemsWidth()
                }
            }
        }

        binding.root.addOnLayoutChangeListener(snippetOnLayoutChangeListener)
    }

    private fun clearSnippetLayoutChangeListener() {
        snippetOnLayoutChangeListener?.let { binding.root.addOnLayoutChangeListener(it) }
            .also { snippetOnLayoutChangeListener = null }
    }

    private fun updateViewVisibility(profile: UserProfileUIModel) {
        if (!viewModel.isMe()) {
            binding.btnAddPhoto.gone()
            profile.let { user ->
                initBell(user.settingsFlags.isSubscriptionNotificationEnabled)
                if (user.blacklistedMe || user.blacklistedByMe || user.settingsFlags.isSubscriptionOn.not()) {
                    binding.ivBell.gone()
                } else {
                    binding.ivBell.visible()
                }
                if (chatIsUnavailable(user)) {
                    binding.fabChat.gone()
                } else {
                    binding.fabChat.visible()
                }
                if (friendStatusIsConfirmed(user).not() && callBtnIsGone(user)) {
                    binding.fabCall.gone()
                } else {
                    binding.fabCall.visible()
                }
                if (user.accountDetails.isAccountDeleted) {
                    showDeleteProfileView()
                }
            }
        } else {
            binding.fabChat.gone()
            binding.fabCall.gone()
            binding.flBell.gone()
            binding.btnAddPhoto.visible()
        }
    }

    private fun showDeleteProfileView() {
        binding.mlUserInfo.gone()
        binding.deleteProfile.llDeleteProfile.visible()
        binding.fabChat.gone()
        binding.fabCall.gone()
        binding.deleteProfile.tvUserName.text = userProfile?.name

        when {
            userProfile?.approved == true -> {
                binding.deleteProfile.ivVerifiedProfile.visible()
                binding.deleteProfile.ivTopContentProfile.gone()
            }

            userProfile?.approved == false && userProfile?.topContentMaker == true -> {
                binding.deleteProfile.ivTopContentProfile.visible()
                binding.deleteProfile.ivVerifiedProfile.gone()
            }

            else -> {
                binding.deleteProfile.ivTopContentProfile.gone()
                binding.deleteProfile.ivVerifiedProfile.gone()
            }
        }
        if (userProfile?.settingsFlags?.isSubscriptionOn == false) binding.deleteProfile.ivDotsMenu.gone()
        binding.deleteProfile.ivDotsMenu.setThrottledClickListener {
            submitUIAction(UserProfileUIAction.ShowDotsMenuAction)
        }

    }

    private fun initBell(isSubscriptionNotificationEnabled: Boolean) {
        if (isSubscriptionNotificationEnabled) {
            binding.ivBell.setImageResource(R.drawable.ic_outlined_bell_m)
            binding.ivBell.setThrottledClickListener {
                needAuthToNavigate {
                    submitUIAction(UserProfileUIAction.ClickSubscribeNotification(false))
                }
            }
        } else {
            binding.ivBell.setImageResource(R.drawable.ic_outlined_bell_off_m)
            binding.ivBell.setThrottledClickListener {
                needAuthToNavigate {
                    submitUIAction(UserProfileUIAction.ClickSubscribeNotification(true))
                }
            }
        }
    }

    private fun showBirthdayDialog(isTodayBirthday: Boolean) {
        val actionType = if (isTodayBirthday) BirthdayBottomDialogFragment.ACTION_TODAY_IS_BIRTHDAY
        else BirthdayBottomDialogFragment.ACTION_YESTERDAY_IS_BIRTHDAY
        act.showBirthdayDialog(
            actionType = actionType, dismissListener = {
                viewModel.setBirthdayDialogShown()
            })
    }

    private fun chatIsUnavailable(user: UserProfileUIModel): Boolean {
        return !user.settingsFlags.iCanChat || !user.settingsFlags.userCanChatMe || user.accountDetails.isAccountBlocked
            || user.blacklistedByMe || user.blacklistedMe
    }

    private fun friendStatusIsConfirmed(user: UserProfileUIModel): Boolean {
        return user.friendStatus == FriendStatus.FRIEND_STATUS_CONFIRMED
    }

    private fun callIsUnavailable(user: UserProfileUIModel): Boolean {
        return !user.settingsFlags.iCanCall || user.accountDetails.isAccountBlocked || user.blacklistedMe || user.blacklistedByMe
    }

    private fun callBtnIsGone(user: UserProfileUIModel): Boolean {
        return !user.settingsFlags.iCanCall || user.accountDetails.isAccountBlocked
    }

    private fun initButtonsListeners() {
        binding.apply {
            if (isSnippet.isFalse() && isUserSnippetDataFull.isFalse() && isOpenFromNotification.isFalse()) {
                initProfileSnippetTouchListener()
            } else {
                initProfileMapSnippetTouchListener()
            }

            buttonUserInfoBack.setThrottledClickListener {
                val topContainerView = NavigationManager.getManager().getTopContainer()
                topContainerView?.animate()?.translationY(0f)?.setDuration(USERINFO_HIDE_SHOW_ANIM_DURATION_MS)
                    ?.withEndAction {
                        findNavController().popBackStack()

                        if (isUserSnippetDataFull.isTrue()) {
                            NavigationManager.getManager().mainMapFragment.apply {
                                showFriendsList()
                            }
                        }

                    }?.start()
            }
            tvUsername.setThrottledClickListener {
                copyUniqueNameToClipboard()
                submitUIAction(UserProfileUIAction.UniqueNameClick)
                showCommonSuccessMessage(getText(R.string.meera_text_copied), requireView())
            }
            buttonUserInfoMenu.setThrottledClickListener {
                needAuthToNavigate {
                    submitUIAction(UserProfileUIAction.ShowDotsMenuAction)
                }
            }
            btnAddPhoto.setThrottledClickListener {
                showSelectPhotoMenu(false)
            }
            fabCall.setThrottledClickListener {
                needAuthToNavigateWithResult(SUBSCRIPTION_ROAD_REQUEST_KEY) {
                    submitUIAction(UserProfileUIAction.OnTryToCall(userId ?: 0))
                }
            }
            fabChat.setThrottledClickListener {
                needAuthToNavigateWithResult(SUBSCRIPTION_ROAD_REQUEST_KEY) {
                    submitUIAction(UserProfileUIAction.StartChatClick)
                }
            }
            fabRoadSwope.setThrottledClickListener {
                (binding.rvUserInfo.layoutManager as LinearLayoutManager).scrollToPosition(0)
                mlUserInfo.apply {
                    jumpToState(R.id.scene_user_info_start)
                    setTransition(R.id.transition_user_info_start_to_middle)
                    setProgress(0f)
                }
            }
        }

        initPermissionDelegate(requireActivity(), viewLifecycleOwner)
        act.permissionListener.add(listener)
    }

    private fun initProfileSnippetTouchListener() {
        binding.apply {
            flContainer.setOnTouchListener(object : View.OnTouchListener {
                private val gestureDetector =
                    GestureDetector(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
                        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                            expandIfNeeded()
                            return true
                        }

                        override fun onScroll(
                            e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float
                        ): Boolean {
                            if (abs(distanceX) > abs(distanceY)) {
                                flContainer?.parent?.requestDisallowInterceptTouchEvent(true)
                                return true
                            }
                            return super.onScroll(e1, e2, distanceX, distanceY)
                        }

                        override fun onFling(
                            e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float
                        ): Boolean {
                            if (abs(velocityX) > abs(velocityY)) {
                                flContainer?.parent?.requestDisallowInterceptTouchEvent(true)
                                return true
                            }
                            return super.onFling(e1, e2, velocityX, velocityY)
                        }
                    })

                fun expandIfNeeded() {
                    val topContainerView = NavigationManager.getManager().getTopContainer()
                    val behavior = NavigationManager.getManager().getTopBehaviour()
                    if (topContainerView?.translationY == 0f) {
                        behavior?.state = STATE_HALF_EXPANDED
                    } else {
                        topContainerView?.animate()?.translationY(0f)?.setDuration(USERINFO_HIDE_SHOW_ANIM_DURATION_MS)
                            ?.start()
                    }
                }

                override fun onTouch(v: View?, event: MotionEvent): Boolean {
                    val behavior = NavigationManager.getManager().getTopBehaviour()
                    return when (behavior?.state) {
                        STATE_COLLAPSED -> {
                            gestureDetector.onTouchEvent(event)
                            true
                        }

                        else -> false
                    }
                }
            })
        }
    }

    private fun initProfileMapSnippetTouchListener() {
        binding.apply {
            flContainer.setOnTouchListener(object : View.OnTouchListener {
                private val gestureDetector =
                    GestureDetector(requireContext(), object : GestureDetector.SimpleOnGestureListener() {

                        override fun onScroll(
                            e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float
                        ): Boolean {
                            val behavior = NavigationManager.getManager().getTopBehaviour()
                            if (distanceY < 0) {
                                behavior?.state = STATE_COLLAPSED
                            }
                            return super.onScroll(e1, e2, distanceX, distanceY)
                        }
                    })

                override fun onTouch(v: View?, event: MotionEvent): Boolean {
                    val behavior = NavigationManager.getManager().getTopBehaviour()
                    return when (behavior?.state) {
                        STATE_EXPANDED -> {
                            if (abs(event.x) > abs(event.y) && binding.mlUserInfo.progress == 0f) {
                                gestureDetector.onTouchEvent(event)
                                return true
                            } else {
                                flContainer?.parent?.requestDisallowInterceptTouchEvent(true)
                                binding.rvUserInfo.onTouchEvent(event)
                                return false
                            }
                        }

                        STATE_HALF_EXPANDED -> {
                            behavior.state = STATE_EXPANDED
                            flContainer.parent?.requestDisallowInterceptTouchEvent(true)
                            binding.rvUserInfo.onTouchEvent(event)
                            return false
                        }

                        else -> false
                    }
                }
            })
        }
    }

    private fun copyUniqueNameToClipboard() {
        val clipboardManager = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        binding.tvUsername.text?.let { uniqueName: CharSequence ->
            val forCopy = ClipData.newPlainText(
                getString(R.string.unique_user_name_clip_data_label), uniqueName
            )
            clipboardManager?.setPrimaryClip(forCopy)
        }
    }

    private fun getPostIdArgument() {
        val args = arguments ?: return
        val postId = args.getLong(ARG_POST_ID)
        if (postId == 0L) return
        this.selectedPostId = postId
    }

    private fun submitUIAction(action: UserProfileUIAction) {
        viewModel.handleUIAction(action)
    }

    private fun initArguments() {
        isSnippet = arguments?.getBoolean(UserInfoFragment.ARG_USER_SNIPPET_DATA)
        isUserSnippetDataFull = arguments?.getBoolean(ARG_USER_SNIPPET_DATA_FULL)
        isUserSnippetEvent = arguments?.getBoolean(ARG_USER_SNIPPET_EVENT)
        userId = arguments?.getLong(ARG_USER_ID) ?: viewModel.getUserUid()
        isSnippetCollapsed = arguments?.getBoolean(USERINFO_SNIPPET_COLLAPSED) ?: false
        isOpenFromNotification = arguments?.getBoolean(USERINFO_OPEN_FROM_NOTIFICATION) ?: false
    }

    private fun setupAnimation() {
        binding.apply {

            restoreMotionState()
            binding.avatarView.applyRoundedOutline(requireContext().displayWidth.toFloat())
            val layoutManager = binding.rvUserInfo.layoutManager as LinearLayoutManager
            val bottomBehavior = NavigationManager.getManager().getTopBehaviour()
            val scrollListener = UserInfoRecyclerScrollListener(
                bottomBehavior, layoutManager, mlUserInfo, checkScrollPositionListener
            )
            val gestureListener = UserInfoGestureDetectorListener(
                bottomBehavior = bottomBehavior,
                layoutManager = layoutManager,
                motionLayout = mlUserInfo,
                isSnippet = isSnippet,
                isUserSnippetDataFull = isUserSnippetDataFull
            )
            val gestureDetector = GestureDetector(requireContext(), gestureListener)

            if (isUserSnippetDataFull.isTrue() || isOpenFromNotification.isTrue()) {
                binding.flCall.translationY = 0f
                binding.flChat.translationY = 0f
                root.setBackgroundResource(R.drawable.bg_bottomsheet_header)
            }
            if (bottomBehavior?.state == STATE_HALF_EXPANDED
                && isOpenFromNotification.isFalse()
                && isUserSnippetDataFull.isFalse()
            ) {
                val startHeightConnectionButton = requireContext().getStatusBarHeight().dp
                binding.flCall.translationY = -startHeightConnectionButton.toFloat()
                binding.flChat.translationY = -startHeightConnectionButton.toFloat()
            }
            rvUserInfo.setOnTouchListener { v, event ->
                event?.let {
                    if (isVisible.not()) return@setOnTouchListener false
                    gestureDetector.onTouchEvent(event)
                }
                when (bottomBehavior?.state) {
                    STATE_EXPANDED, STATE_SETTLING -> false
                    else -> true
                }
            }

            binding.flHeaderBottomSheet.setOnTouchListener(object : MeeraOnSwipeTouchListener() {
                override fun onSwipeDown() {
                    if (isUserSnippetDataFull.isTrue() || isOpenFromNotification.isTrue()) {
                        bottomBehavior?.state = STATE_HIDDEN
                    }
                }
            })

            bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (isVisible.not()) return
                    binding.vDrag.isInvisible =
                        newState == STATE_EXPANDED && (isSnippet.isFalse() && isUserSnippetDataFull.isFalse())

                    bottomBehavior?.isDraggable = newState != STATE_EXPANDED

                    if (isUserSnippetDataFull.isTrue() && bottomBehavior?.state == STATE_HIDDEN) {
                        findNavController().popBackStack()
                        NavigationManager.getManager().mainMapFragment.apply {
                            showFriendsList()
                        }

                        return
                    }

                    if (isOpenFromNotification.isTrue() && bottomBehavior?.state == STATE_HIDDEN) {
                        onHidden()
                        return
                    }

                    if (bottomBehavior?.state == STATE_COLLAPSED) {
                        handleUserOnOpenedMap()
                        root.setMargins(
                            USERINFO_SNIPPET_HORIZONTAL_MARGIN.dp, 0, USERINFO_SNIPPET_HORIZONTAL_MARGIN.dp, 0
                        )
                        root.maxHeight = USERINFO_SNIPPET_HEIGHT.dp
                        flCall.gone()
                        flChat.gone()
                        flBell.gone()
                        userProfile?.apply {
                            val location = LatLng(
                                locationDetails.latitude ?: return, locationDetails.longitude ?: return
                            )
                            val yOffset = (DEF_HEIGHT_USERINFO_SNIPPET_CONTAINER.dp - getScreenHeight().div(2))
                            if (isSnippet.isFalse()
                                && isUserSnippetDataFull.isFalse()
                                && isOpenFromNotification.isFalse()
                                && isZeroLocation(
                                    location
                                ).not()
                            ) {
                                NavigationManager.getManager().mainMapFragment.let {
                                    it.updateCameraLocation(location, yOffset = yOffset)
                                    it.focusMapItem(FocusedMapItem.User(userId = userId))
                                }
                            }
                        }


                    } else if (
                        (((isSnippet.isTrue() || isUserSnippetDataFull.isTrue()) &&
                            bottomBehavior?.state == STATE_EXPANDED) ||
                            (isSnippet.isFalse() && isUserSnippetDataFull.isFalse())) &&
                        bottomBehavior?.state != STATE_DRAGGING
                    ) {
                        handleUserOnClosedMap()
                        if (isSnippet.isTrue() || isUserSnippetDataFull.isTrue() || isOpenFromNotification.isTrue()) {
                            binding.flCall.translationY = 0f
                            binding.flChat.translationY = 0f
                        }
                        if (bottomBehavior?.state != STATE_SETTLING) {
                            root.setMargins(0)
                            flCall.visible()
                            flChat.visible()
                            flBell.visible()
                        }
                        root.maxHeight = Integer.MAX_VALUE
                    }

                    if (bottomBehavior?.state == STATE_EXPANDED) {
                        if (isSnippet.isTrue() || isUserSnippetDataFull.isTrue() || isOpenFromNotification.isTrue()) {
                            root.setBackgroundResource(R.drawable.bg_bottomsheet_header)
                        } else {
                            root.setBackgroundColor(
                                ContextCompat.getColor(
                                    requireContext(), R.color.uiKitColorBackgroundPrimary
                                )
                            )
                        }
                    } else if (bottomBehavior?.state != STATE_DRAGGING
                        && isOpenFromNotification.isFalse()
                    ) {
                        root.setBackgroundResource(R.drawable.bg_rectangle_rad_16)
                        root.setBackgroundTint(R.color.uiKitColorBackgroundPrimary)
                    }

                    if (newState == STATE_COLLAPSED || newState == STATE_HIDDEN) {
                        stopVideoIfExists()
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    if (isVisible.not()) return

                    if (isOpenFromNotification.isFalse()) {
                        val startMargin = USERINFO_SNIPPET_HORIZONTAL_MARGIN.dp
                        val interpolatedMargin = (startMargin * (1 - slideOffset)).toInt()
                        root.setMargins(interpolatedMargin, 0, interpolatedMargin, 0)
                    }

                    if (bottomBehavior?.state == STATE_DRAGGING) {
                        val topContainerView = NavigationManager.getManager().getTopContainer()
                        if (slideOffset > 0f) {
                            if (topContainerView?.translationY == 0f) {
                                root.maxHeight = Integer.MAX_VALUE
                            } else {
                                topContainerView?.animate()?.translationY(0f)
                                    ?.setDuration(USERINFO_HIDE_SHOW_ANIM_DURATION_MS)?.start()
                            }
                        } else {
                            root.setBackgroundResource(R.drawable.bg_rectangle_rad_16)
                            root.setBackgroundTint(R.color.uiKitColorBackgroundPrimary)
                            if (isSnippet.isFalse() && isUserSnippetDataFull.isFalse()) {
                                topContainerView?.animate()?.translationY(USERINFO_HIDDEN_HEIGHT.dp)
                                    ?.setDuration(USERINFO_HIDE_SHOW_ANIM_DURATION_MS)?.start()
                            }
                        }
                    }

                    if (isVisibleConnectionButton) {
                        val translationY = -(bottomSheet.top - requireContext().getStatusBarHeight()).toFloat()
                        binding.flCall.translationY = translationY
                        binding.flChat.translationY = translationY
                    }
                }
            }
            bottomSheetCallback?.let { bottomBehavior?.addBottomSheetCallback(it) }
            rvUserInfo.addOnScrollListener(scrollListener)
            mlUserInfo.addTransitionListener(UserInfoMotionLayoutTransitionListener())

            if (isVisibleConnectionButton.not()) {
                binding.flCall.animate().cancel()
                binding.flChat.animate().cancel()
                binding.flCall.animate().translationY(TRANSITION_Y_CONTACT_ANIMATION)
                    .setDuration(DURATION_CONTACT_ANIMATION).start()
                binding.flChat.animate().translationY(TRANSITION_Y_CONTACT_ANIMATION)
                    .setDuration(DURATION_CONTACT_ANIMATION).start()
            }
        }
    }

    private fun isZeroLocation(latLng: LatLng): Boolean = latLng.latitude == 0.0 && latLng.longitude == 0.0

    private fun handleUserOnOpenedMap() {
        if (isSnippet == true || isUserSnippetDataFull.isTrue()) return
        NavigationManager.getManager().isMapMode = true
        userProfile?.let { profile ->
            val mapUser = viewModel.mapProfileToMapUser(profile)

            NavigationManager.getManager().mainMapFragment.apply {

                hideMapControls()
                showWeatherWidger()
            }

            NavigationManager.getManager().mainMapFragment.setMapModeForUserProfile(
                user = mapUser, isMe = viewModel.isMe()
            )
            NavigationManager.getManager().mainMapFragment.handleIsShowNoMapPlaceholder(
                isShowNoMapPlaceholder = handleIsShowPlaceholderOnMap(mapUser),
                placeholderType = if (viewModel.isMe()) NoShowOnMapPlaceholderType.OWN_PROFILE else NoShowOnMapPlaceholderType.OTHER_PROFILE
            )
        }
    }

    private fun handleIsShowPlaceholderOnMap(mapUser: MapUserUiModel): Boolean {
        val isMe = viewModel.isMe()
        return if (isMe) {
            val isLocationPermissionGranted = LocationUtility.checkPermissionLocation(requireContext())
            isLocationPermissionGranted.not()
        } else {
            mapUser.isShowOnMap.not()
        }
    }

    private fun handleUserOnClosedMap() {
        if (isSnippet == true || isUserSnippetDataFull.isTrue()) return
        NavigationManager.getManager().isMapMode = false
        NavigationManager.getManager().mainMapFragment.handleIsShowNoMapPlaceholder(isShowNoMapPlaceholder = false)
        NavigationManager.getManager().mainMapFragment.apply {
            hideWeatherWidget()
        }
    }

    private fun restoreMotionState() = with(binding) {
        motionLayoutState?.let { state ->
            mlUserInfo.apply {
                jumpToState(state)
                when (state) {
                    R.id.scene_user_info_start -> {
                        setTransition(R.id.transition_user_info_start_to_middle)
                        setProgress(0f)
                    }

                    R.id.scene_user_info_end -> {
                        setTransition(R.id.transition_user_info_middle_to_end)
                        setProgress(1f)
                    }
                }
            }
        }
    }

    private fun setupGyroscopeAnimation() {
        binding.apply {
            val animatedViewsListRight = listOf(flRightTop, flPhotosCounter, flRightBottom)
            val animatedViewsListLeft = listOf(flLeftTop, flLeftBottom)
            sensorListener = UserInfoSensorEventListener(
                leftViews = animatedViewsListLeft, rightViews = animatedViewsListRight
            )
            sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_STATUS_ACCURACY_HIGH)
        }
    }

    override fun onStop() {
        motionLayoutState = binding.mlUserInfo.currentState
        super.onStop()
    }

    override fun onDestroyView() {
        bottomSheetCallback?.let { NavigationManager.getManager().getTopBehaviour()?.removeBottomSheetCallback(it) }
        clearSnippetLayoutChangeListener()
        NavigationManager.getManager().mainMapFragment.setMapModeToDefault()
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        NavigationManager.getManager().mainMapFragment.showWeatherWidger()
    }

    override fun onResume() {
        super.onResume()
        setupGyroscopeAnimation()
        smoothScroller = object : LinearSmoothScroller(context) {
            override fun getVerticalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }

        if (isSnippet.isFalse() && isUserSnippetDataFull.isFalse() && isOpenFromNotification.isFalse()) {
            NavigationManager.getManager().toolbarAndBottomInteraction.getToolbar().state =
                UiKitToolbarViewState.EXPANDED
        }

    }

    override fun onPause() {
        super.onPause()
        sensorListener?.let { sensorManager.unregisterListener(it) }
        smoothScroller = null
        handleUserOnClosedMap()
    }

    private fun initRecycler() {
        binding.apply {
            val concatAdapter = ConcatAdapter(adapter)
            rvUserInfo.adapter = concatAdapter
        }
    }

    // Устанавливается id выбранного поста из экрана PeoplesFragment
    var selectedPostId: Long? = null

    private fun setupFullProfile() {
        viewModel.init(
            appVersionName = BuildConfig.VERSION_NAME,
            serverAppVersionName = (requireActivity() as MeeraAct).serverAppVersionName,
            userId = userId ?: viewModel.getUserUid(),//по умолчанию берем свой id
            isSnippetCollapsed = isSnippetCollapsed,
            isSnippet = isSnippet == true
        )
        addPostsAdapter()
        initPostsLoadScrollListener()
        initObservers()
        checkSnippet()
    }

    private fun addPostsAdapter() {
        initPostsAdapter(
            roadType = NetworkRoadType.USER(
                userId,
                BaseFeedFragment.REQUEST_ROAD_TYPE_USER,
                userId == viewModel.getUserUid(),
                selectedPostId = selectedPostId
            ), recyclerView = binding?.rvUserInfo
        )
        getAdapterPosts()?.let {
            (binding?.rvUserInfo?.adapter as? ConcatAdapter)?.addAdapter(it)
        }

        initPostsLiveObservable()
    }

    private fun checkSnippet() {
        if (isSnippet == true) {
            binding.vDrag.visible()
            val state = activity?.findViewById<FrameLayout>(getContainerFragmentId())?.let { containerView ->
                BottomSheetBehavior.from(containerView).state
            }

            binding.buttonUserInfoBack.gone()
            binding.buttonUserInfoSnippetBack.visible()

            if (state == STATE_COLLAPSED) {
                binding.shimmerProfileNoMy.ivUserInfoMenu.gone()
                binding.ivBell.gone()
                binding.shimmerProfileIsMy.ivUserInfoMenu.gone()
                binding.flAddPhoto.gone()
                binding.flChat.gone()
                binding.flCall.gone()
                binding.buttonUserInfoMenu.gone()
                binding.buttonUserInfoSnippetBack.gone()
            } else {
                showTopButtons()
            }

            binding.buttonUserInfoSnippetBack.setThrottledClickListener {
                switchSnippetState()
            }
        }
    }

    private fun initObservers() {
        viewModel.state.onEach { state ->
            userProfile = state.profile
            showShimmer(true)
            handleProfileUi(state.profile)
            handleProfileUIList(state.profileUIList, state.scrollToTop)
            showShimmer(false)
            updateViewVisibility(state.profile)

        }.launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.effect.onEach { effect -> handleViewEventsUserInfo(effect) }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        feedViewModel.userFeedProfileViewEvent.flowWithLifecycle(lifecycle).onEach(::handleFeedProfileEvent)
            .launchIn(viewLifecycleOwner.lifecycleScope)


        viewModel.avatarsLiveData.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            handleAvatarsUi(it.count, it.items)
        }
    }

    private fun handleFeedProfileEvent(event: UserFeedViewEvent) {
        when (event) {
            is UserFeedViewEvent.ScrollToPostPosition -> {
                val scrollPoint = getScrollPoint(event.selectedPostPosition)
                val layoutManager = binding.rvUserInfo.layoutManager as? LinearLayoutManager? ?: return
                smoothScroller?.targetPosition = scrollPoint
                layoutManager.startSmoothScroll(smoothScroller)
                doDelayed(event.scrollDelay) {
                    binding.rvUserInfo.playVideo(false)
                }
                expandBottomSheet()
            }

            is UserFeedViewEvent.ScrollToFirstPostPositionUiEffect -> {
                handleScrollToFirstPostPositionEffect(event.delayPlayVideo)
            }
        }
    }

    private fun getScrollPoint(postPosition: Int) = viewModel.userInfoListSize + postPosition

    private fun handleScrollToFirstPostPositionEffect(delay: Long) {
        val scrollPoint = getScrollPoint(FIRST_POST)
        binding.rvUserInfo.smoothScrollToPosition(scrollPoint)
        doDelayed(delay) {
            binding.rvUserInfo.playVideo(false)
        }
        expandBottomSheet()
    }

    private fun expandBottomSheet() {
        activity?.findViewById<FrameLayout>(getContainerFragmentId())?.let { containerView ->
            BottomSheetBehavior.from(containerView).state = STATE_EXPANDED
        }
    }

    private fun showShimmer(visible: Boolean) {
        when {
            viewModel.isMe() && visible -> {
                binding.shimmerProfileIsMy.llShimmerProfile.visible()
                binding.mlUserInfo.gone()
            }

            viewModel.isMe() && !visible -> {
                binding.shimmerProfileIsMy.llShimmerProfile.gone()
                binding.mlUserInfo.visible()
            }

            !viewModel.isMe() && visible -> {
                binding.shimmerProfileNoMy.llShimmerProfile.visible()
                if (isSnippet == true) {
                    binding.shimmerProfileNoMy.vNavView.gone()
                    binding.shimmerProfileNoMy.shimmerContainer.setMargins(0, SHIMMER_MARGIN.dp, 0, 0)
                }
                binding.mlUserInfo.gone()
            }

            !viewModel.isMe() && !visible -> {
                binding.shimmerProfileNoMy.llShimmerProfile.gone()
                binding.mlUserInfo.visible()
            }
        }
    }

    private fun handleProfileUi(profile: UserProfileUIModel) {
        screenshotPopupData = ScreenshotPopupData(
            title = profile.name,
            description = profile.uniquename.let { "@$it" },
            buttonTextStringRes = R.string.share_profile,
            additionalInfo = listOf(
                profile.locationDetails.cityName, profile.locationDetails.countryName
            ).filter { !it.isNullOrBlank() }.joinToString(", "),
            imageLink = profile.avatarDetails.avatarSmall,
            isVipUser = profile.accountDetails.accountType != AccountTypeEnum.ACCOUNT_TYPE_REGULAR.value,
            isApprovedUser = profile.accountDetails.isAccountApproved,
            isInterestingAuthor = profile.accountDetails.isTopContentMaker,
            profileId = profile.userId,
            isDeleted = profile.accountDetails.isAccountDeleted,
            screenshotPlace = if (viewModel.isMe()) ScreenshotPlace.OWN_PROFILE else ScreenshotPlace.USER_PROFILE
        )
        binding.apply {

            usernameUserInfo.text = profile.name
            usernameUserInfo.enableTopContentAuthorApprovedUser(
                params = TopAuthorApprovedUserModel(
                    approved = profile.approved,
                    customIconTopContent = R.drawable.ic_approved_author_gold_10,
                    interestingAuthor = profile.topContentMaker,
                )
            )


            if (profile.approved) {
                ivVerified.visible()
                ivVerified.imageTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        this@MeeraUserInfoFragment.requireContext(), R.color.uiKitColorForegroundLightGreen
                    )
                )
                ivVerified.setThrottledClickListener {
                    showCommonSuccessMessage(getText(R.string.meera_verified_profile), requireView())
                }
            }
            if (profile.topContentMaker) {
                ivVerified.visible()
                ivVerified.setImageResource(R.drawable.ic_filled_verified_flame_s_colored)
                ivVerified.setThrottledClickListener {
                    showCommonSuccessMessage(getText(R.string.meera_top_content_profile), requireView())
                }
            }
            tvUsername.text = "@${profile.uniquename}"


            val hasMoments = profile.moments?.hasMoments ?: false
            val hasNewMoments = profile.moments?.hasNewMoments ?: false

            val momentState = when {
                hasNewMoments -> UserpicStoriesStateEnum.NEW
                hasMoments -> UserpicStoriesStateEnum.VIEWED
                else -> UserpicStoriesStateEnum.NO_STORIES
            }
            userpicUserPhoto.setConfig(UserpicUiModel(storiesState = momentState))
            if (profile.blacklistedMe == true || profile.blacklistedByMe == true) {
                sivLeftTop.gone()
                sivLeftBottom.gone()
                sivRightTop.gone()
                sivRightBottom.gone()
                sivLeftTop.setImageBitmap(null)
                sivLeftBottom.setImageBitmap(null)
                sivRightTop.setImageBitmap(null)
                sivRightBottom.setImageBitmap(null)
            }
            userpicUserPhoto.setThrottledClickListener {
                if (isSnippet == true) {
                    activity?.findViewById<FrameLayout>(getContainerFragmentId())?.let { containerView ->
                        if (BottomSheetBehavior.from(containerView).state != STATE_EXPANDED) {
                            expandBottomSheet()
                            return@setThrottledClickListener
                        }
                    }
                }
                if (profile.blacklistedMe == true || profile.blacklistedByMe == true) return@setThrottledClickListener
                if (profile.moments?.hasMoments == true || profile.moments?.hasNewMoments == true) {
                    findNavController().safeNavigate(
                        R.id.action_global_meeraViewMomentFragment, bundleOf(
                            KEY_USER_ID to userId,
                            KEY_MOMENT_CLICK_ORIGIN to MomentClickOrigin.fromUserAvatar(),
                        )
                    )
                } else {
                    submitUIAction(
                        UserProfileUIAction.OnShowImage(
                            position = 0, listPhotoEntity = emptyList(), isAvatarPhoto = true
                        )
                    )
                }
            }
        }
    }


    private fun showAddPhotoTooltip() {
        binding.flRightBottom.post {
            meeraAddMorePhotosTooltip.showUniversal(
                view = binding.flRightBottom,
                duration = TooltipDuration.COMMON_END_DELAY,
                offsetX = binding.flRightBottom.width / 2,
            )
        }
    }

    private fun showPhotoCounterTooltip() {
        binding.flPhotosCounter.post {
            meeraPhotosCounterTooltip.showUniversal(
                view = binding.flPhotosCounter,
                duration = TooltipDuration.COMMON_END_DELAY,
                offsetY = binding.flPhotosCounter.height / 2,
                offsetX = -binding.flPhotosCounter.width / 2,
            )
        }
    }

    private fun handleViewEventsUserInfo(effect: UserInfoViewEffect) {
        when (effect) {
            UserInfoViewEffect.ShowBubbleStarsTooltip -> showAddPhotoTooltip()
            UserInfoViewEffect.ShowPhotosCounterTooltip -> showPhotoCounterTooltip()
            UserProfileNavigation.OpenAddVehicle -> {
                needAuthToNavigate {
                    findNavController().safeNavigate(R.id.meeraVehicleSelectTypeFragment)
                }
            }

            is UserProfileNavigation.OpenVehicle -> {
                openVehicleInfoFragment(vehicleId = effect.vehicleId, userId = effect.userId)
            }

            UserInfoViewEffect.OnRefreshUserRoad -> {
                loadBasePosts()
                viewModel.refreshProfile()
            }

            is UserProfileNavigation.OpenVehicleList -> {
                openVehicleListFragmentNew(
                    userId = effect.userId, accountType = effect.accountType, accountColor = effect.accountColor
                )
            }

            is UserInfoViewEffect.AuthAndOpenFriendsList -> openUserListFriendsOrShowAuth(
                userId = effect.userId, actionType = effect.actionType, name = effect.name
            )

            is UserProfileNavigation.OpenSubscribers -> openSubscribersListFragment()
            is UserProfileNavigation.OpenSubscriptions -> openSubscriptionsListFragment()
            is UserProfileNavigation.OpenGridProfile -> openGridProfilePhotoFragment(effect.userId, effect.photoCount)
            is UserInfoViewEffect.OpenAddPhoto -> addPhoto()
            is UserInfoViewEffect.ShowBirthdayDialog -> showBirthdayDialog(effect.isBirthdayToday)
            is UserInfoViewEffect.AddSuggestionFriends -> {
                needAuthToNavigate {
                    submitUIAction(
                        UserProfileUIAction.OnFriendClicked(
                            friendStatus = effect.friendStatus,
                            approved = effect.approved,
                            influenecer = effect.influenecer
                        )
                    )
                }
            }

            is UserProfileNavigation.OpenProfilePhotoViewer -> {
                openProfilePhotoViewerFragment(effect.isMe, effect.position, effect.userId, effect.isAvatarPhoto)
            }

            is UserInfoViewEffect.ShowShareProfileDialog -> {
                meeraShowShareProfileDialog(effect.profile, effect.profileLink)
            }

            is UserInfoViewEffect.ShowSuccessCopyProfile -> {
                showCommonSuccessMessage(getText(R.string.meera_copy_link_success), requireView())
            }

            is UserInfoViewEffect.OpenCameraToChangeAvatar -> openCameraToChangeAvatar()
            is UserInfoViewEffect.OpenAvatarCreator -> openAvatarCreator()
            is PhoneCallsViewEffect.OnSuccessDisableCalls -> {
                showCommonSuccessMessage(getText(R.string.personal_messages_toast_disallow_calls), requireView())
                viewModel.refreshProfile()
            }

            is PhoneCallsViewEffect.OnSuccessEnableCalls -> {
                showCommonSuccessMessage(getText(R.string.personal_messages_toast_allow_calls), requireView())
                userProfile?.let { user ->
                    if (callBtnIsGone(user)) binding.fabCall.visible()
                }
                viewModel.refreshProfile()
            }

            is RoadPostViewEffect.OnSuccessHidePosts -> {
                showCommonSuccessMessage(getText(R.string.user_complain_posts_hided), requireView())
                viewModel.refreshProfile()
            }

            is RoadPostViewEffect.OnSuccessUnhidePosts -> {
                showCommonSuccessMessage(getText(R.string.meera_user_complain_posts_unhided), requireView())
                viewModel.refreshProfile()
            }

            is UserInfoViewEffect.OnSuccessDisableChat -> {
                showCommonSuccessMessage(getText(R.string.personal_messages_toast_disallow_messages), requireView())
                binding.fabChat.gone()
                viewModel.refreshProfile()
            }

            is UserInfoViewEffect.OnSuccessEnableChat -> {
                showCommonSuccessMessage(getText(R.string.personal_messages_toast_allow_messages), requireView())
                binding.fabChat.visible()
                viewModel.refreshProfile()
            }

            is UserProfileNavigation.OpenComplainFragment -> {
                findNavController().safeNavigate(
                    R.id.action_userInfoFragment_to_meeraUserComplaintDetailsFragment, bundleOf(
                        KEY_COMPLAINT_USER_ID to effect.userId, KEY_COMPLAINT_WHERE_VALUE to effect.where
                    )
                )
            }

            is UserInfoViewEffect.OnSuccessRemoveFriend -> {
                val message = effect.message.ifEmpty {
                    if (effect.unsubsribed) {
                        getString(R.string.friends_remove_friend_and_unsubscribe_success)
                    } else {
                        getString(R.string.friends_remove_friend_success)
                    }
                }
                showCommonSuccessMessage(message, requireView())
            }

            is UserInfoViewEffect.OnSuccessBlockUser -> {
                effect.isBlock?.let { isBlock ->
                    if (isBlock) {
                        showCommonSuccessMessage(getText(R.string.you_blocked_user), requireView())
                    } else {
                        showCommonSuccessMessage(getText(R.string.user_unblacklisted), requireView())
                    }
                }
                viewModel.refreshProfile(scrollToTop = true)
            }

            is PhoneCallsViewEffect.OnFailureDisableCalls -> showCommonError(
                getText(R.string.personal_messages_toast_disallow_info_call), requireView()
            )

            is PhoneCallsViewEffect.OnFailureEnableCalls -> showCommonError(
                getText(R.string.personal_messages_toast_disallow_info_call), requireView()
            )

            is UserProfileDialogNavigation.ShowDotsMenu -> {
                if (viewModel.isMe()) {
                    showMyDotsMenu(effect)
                } else {
                    showNotMyDotsMenu(effect)
                }
            }

            is UserProfileDialogNavigation.ShowScreenshotPopup -> {
                showScreenshotPopup(effect.userLink)
            }

            is UserProfileNavigation.OpenPeopleFragment -> {
                needAuthToNavigate {
                    val bundle = bundleOf(
                        IArgContainer.ARG_SHOW_SWITCHER to false,
                        IArgContainer.ARG_SHOW_SYNC_CONTACTS_WELCOME to effect.showSyncContactsWelcome
                    )
                    when {
                        isUserSnippetDataFull == true -> {
                            NavigationManager.getManager().topNavController.safeNavigate(
                                R.id.action_userInfoFragment_to_peoplesFragment, bundle = bundle
                            )
                        }

                        isOpenFromNotification == true -> {
                            findNavController().safeNavigate(R.id.peoplesFragment, bundle = bundle)
                        }

                        else -> {
                            findNavController().safeNavigate(
                                resId = R.id.action_userInfoFragment_to_peoplesFragment, bundle = bundle
                            )
                        }
                    }
                }
            }

            is UserProfileNavigation.OpenProfile -> {
                val bundle = bundleOf(
                    ARG_USER_ID to effect.userId,
                    IArgContainer.ARG_TRANSIT_FROM to AmplitudePropertyWhere.SUGGEST_USER_PROFILE.property
                )
                when {
                    isUserSnippetDataFull == true -> {
                        NavigationManager.getManager().topNavController.safeNavigate(
                            R.id.action_userInfoFragment_to_userInfoFragment, bundle = bundle
                        )
                    }

                    isOpenFromNotification == true -> {
                        findNavController().safeNavigate(R.id.userInfoFragment, bundle = bundle)
                    }

                    else -> {
                        findNavController().safeNavigate(
                            resId = R.id.action_userInfoFragment_to_userInfoFragment, bundle = bundle
                        )
                    }
                }
            }

            is UserProfileNavigation.CloseFloorCongratulation -> {
                needAuthToNavigate {
                    submitUIAction(
                        UserProfileUIAction.SetBirthdayFloorEnabled(
                            enabled = false
                        )
                    )
                }
            }

            is UserProfileNavigation.NavigateToPostFragment -> openAddPost(effect.showMediaGallery)


            is UserInfoViewEffect.OnCreateAvatarPostSettings -> {
                if (effect.privacySettingModel?.value == CreateAvatarPostEnum.PRIVATE_ROAD.state
                    || effect.privacySettingModel?.value == CreateAvatarPostEnum.MAIN_ROAD.state
                ) {
                    onPublishOptionsSelected(
                        imagePath = effect.imagePath,
                        animation = effect.animation,
                        createAvatarPost = effect.privacySettingModel.value,
                        saveSettings = 1,
                        amplitudeActionType = AmplitudeAlertPostWithNewAvatarValuesActionType.PUBLISH
                    )
                } else {
                    showPublishPostAlert(imagePath = effect.imagePath, animation = effect.animation)
                }
            }

            is UserInfoViewEffect.OnGoneProgressUserAvatar -> {
                handleSuccessUpload(effect)
            }

            UserProfileNavigation.OpenSubscribers -> openSubscribersListFragment()
            UserProfileNavigation.OpenSubscriptions -> openSubscriptionsListFragment()
            is UserInfoViewEffect.SubmitAvatars -> handleAvatarsUi(effect.count, effect.items)

            is UserInfoViewEffect.OnSuccessRequestAddFriend -> {
                effect.messageRes?.let { showCommonSuccessMessage(getText(it), requireView()) }
                viewModel.refreshProfile()
            }

            is UserProfileDialogNavigation.ShowUnsubscribeMenuMeera -> {
                needAuthToNavigate {
                    showUnsubscribeMenu(
                        isNotificationsAvailable = effect.isNotificationsAvailable,
                        isNotificationsEnabled = effect.isNotificationsEnabled,
                        friendStatus = effect.friendStatus
                    )
                }
            }

            is UserProfileDialogNavigation.ShowSubscribeMenuMeera -> {
                needAuthToNavigate {
                    showSubscribeMenu(
                        friendStatus = effect.friendStatus
                    )
                }
            }

            is UserProfileDialogNavigation.ShowFriendUnsubscribeMenu -> {
                needAuthToNavigate {
                    showFriendSubscribeMenu(friendStatus = effect.friendStatus)
                }
            }

            is UserProfileDialogNavigation.ShowFriendSubscribeMenu -> {
                needAuthToNavigate {
                    showUnsubscribeMenu(
                        isNotificationsAvailable = effect.isNotificationsAvailable,
                        isNotificationsEnabled = effect.isNotificationsEnabled,
                        friendStatus = effect.friendStatus
                    )
                }
            }

            is UserProfileDialogNavigation.ShowNoFriendSubscribeMenu -> {
                initListItemFriendIncomingMenu(
                    isNotificationsAvailable = effect.isNotificationsAvailable,
                    isNotificationsEnabled = effect.isNotificationsEnabled,
                )
                showUnsubscribeMenu(
                    isNotificationsAvailable = effect.isNotificationsAvailable,
                    isNotificationsEnabled = effect.isNotificationsEnabled,
                    friendStatus = effect.friendStatus
                )
            }

            is UserProfileDialogNavigation.ShowFriendIncomingSubscribeStatusMenuMeera -> {
                needAuthToNavigate {
                    showIncomingSubscribeMenu(
                        approved = effect.approved,
                        influencer = effect.influencer,
                        isNotificationsAvailable = effect.isNotificationsAvailable,
                        isNotificationsEnabled = effect.isNotificationsEnabled,
                        friendStatus = effect.friendStatus
                    )
                }
            }

            is UserProfileDialogNavigation.ShowFriendIncomingUnsubscribeStatusMenuMeera -> {
                needAuthToNavigate {
                    showIncomingUnsubscribeMenu(
                        approved = effect.approved, influencer = effect.influencer, friendStatus = effect.friendStatus
                    )
                }
            }

            is UserProfileDialogNavigation.ShowSuggestion -> {
                needAuthToNavigate {
                    submitUIAction(
                        UserProfileUIAction.SetSuggestionsEnabled(
                            enabled = effect.isSuggestionShow
                        )
                    )
                }
            }

            is UserProfileDialogNavigation.SubscribeRequestAction -> {
                needAuthToNavigate {
                    submitUIAction(
                        UserProfileUIAction.OnSubscribeClicked(
                            isSubscribed = effect.isSubscribed,
                            userId = effect.userId,
                            friendStatus = effect.friendStatus,
                            approved = effect.approved,
                            topContent = effect.topContent,
                            message = effect.message
                        )
                    )
                }
            }

            is UserInfoViewEffect.ShowToastEvent -> effect.messageRes?.let {
                showCommonSuccessMessage(getText(it), requireView())
            }

            is UserProfileDialogNavigation.ShowFriendIncomingStatusMenu -> {
                needAuthToNavigate {
                    showFriendIncomingStatusMenu(
                        isNotificationsAvailable = effect.isNotificationsAvailable,
                        isNotificationsEnabled = effect.isNotificationsEnabled,
                        approved = effect.approved,
                        influencer = effect.influencer,
                        friendStatus = effect.friendStatus
                    )
                }
            }

            is UserInfoViewEffect.GoToMarket -> act.sendToMarket()
            is UserInfoViewEffect.CallToUser -> callToUser(effect)
            is UserProfileNavigation.StartDialog -> openChatFragment(effect.where, effect.userId, effect.fromWhere)

            is UserInfoViewEffect.OnSuccessEnableSubscriptionNotification -> {
                showCommonSuccessMessage(getText(R.string.meera_enable_notifications_new_post), requireView())
                binding.ivBell.setImageResource(R.drawable.ic_outlined_bell_m)
                viewModel.refreshProfile()
            }

            is UserInfoViewEffect.OnSuccessDisableSubscriptionNotification -> {
                showCommonSuccessMessage(getText(R.string.meera_disable_notifications_new_post), requireView())
                binding.ivBell.setImageResource(R.drawable.ic_outlined_bell_off_m)
                viewModel.refreshProfile()
            }

            is UserInfoViewEffect.OnFailChangeSubscriptionNotification -> showCommonError(
                getText(R.string.notification_settings_change_error), requireView()
            )

            is UserInfoViewEffect.OnUnsubscribed -> {
                showCommonSuccessMessage(getText(R.string.meera_search_unsubscribed_notification), requireView())
                viewModel.refreshProfile()
            }

            is UserInfoViewEffect.OnSubscribed -> {
                showCommonSuccessMessage(effect.message, requireView())
                setFragmentResult(KEY_SUBSCRIBED, bundleOf(KEY_SUBSCRIBED to true))
                viewModel.handleUIAction(UserProfileUIAction.SetSuggestionsEnabled(enabled = true))
            }

            is UserInfoViewEffect.UploadImageInfo -> handleUpload(effect.nonNullWork)
            is UserInfoViewEffect.ShowProfileStatistics -> openUserStatistics()

            is UserInfoViewEffect.OnSuccessCancelFriendRequest -> {
                val message = if (effect.unsubscribed) {
                    getString(R.string.friends_cancel_friend_request_and_unsubscribe_success)
                } else {
                    getString(R.string.friends_cancel_friend_request_success)
                }
                showCommonSuccessMessage(message, requireView())
                viewModel.refreshProfile()
            }

            else -> Unit
        }
    }

    private fun openAddPost(isShowGallery: Boolean) {
        findNavController().safeNavigate(
            resId = R.id.meeraCreatePostFragment, bundle = Bundle().apply {
                putBoolean(ARG_SHOW_MEDIA_GALLERY, isShowGallery)
                putString(
                    AddMultipleMediaPostFragment.OpenFrom.EXTRA_KEY,
                    AddMultipleMediaPostFragment.OpenFrom.Profile.toString()
                )
            })
    }

    private fun openUserStatistics() {
        if (childFragmentManager.findFragmentByTag(TAG_PROFILE_STATISTICS_FRAGMENT) == null) {
            val fragment = MeeraProfileStatisticsContainerFragment()
            fragment.show(childFragmentManager, TAG_PROFILE_STATISTICS_FRAGMENT)
        }
    }

    private fun showScreenshotPopup(userLink: String) {
        if (isScreenshotPopupShown) return
        isScreenshotPopupShown = true
        screenshotPopupData =
            screenshotPopupData?.apply { link = "$userLink${screenshotPopupData?.description?.drop(1)}" }
        screenshotPopupData?.let { ScreenshotPopupController.show(this, it) }
    }

    private fun handleUpload(workInfo: WorkInfo?) {
        if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
            viewModel.refreshProfile()
        }
        if (workInfo != null && workInfo.state == WorkInfo.State.RUNNING) {
            viewModel.refreshProfile()
        }
        if (workInfo != null && workInfo.state == WorkInfo.State.FAILED) {
            showCommonError(getText(R.string.media_upload_error), requireView())
        }
    }

    private fun openChatFragment(
        biAmplitudeWhere: AmplitudePropertyWhere, userId: Long, biWhereCreated: AmplitudePropertyChatCreatedFromWhere
    ) {
        val bundle = bundleOf(
            IArgContainer.ARG_WHERE_CHAT_OPEN to biAmplitudeWhere,
            IArgContainer.ARG_FROM_WHERE_CHAT_CREATED to biWhereCreated,
            IArgContainer.ARG_CHAT_INIT_DATA to ChatInitData(
                initType = ChatInitType.FROM_PROFILE, userId = userId
            )
        )
        when {
            isSnippet == true -> {
                NavigationManager.getManager().topNavController.safeNavigate(
                    resId = R.id.meeraChatFragment, bundle = bundle
                )
            }

            isUserSnippetDataFull == true -> {
                NavigationManager.getManager().topNavController.safeNavigate(
                    resId = R.id.meeraChatFragment, bundle = bundle
                )
            }

            else -> {
                findNavController().safeNavigate(
                    resId = R.id.meeraChatFragment, bundle = bundle
                )
            }
        }
    }

    private fun callToUser(event: UserInfoViewEffect.CallToUser) {
        val existUser = userProfile ?: return
        viewModel.isWebSocketEnabled()
        if (viewModel.isWebSocketEnabled().not()) {
            showCommonError(getText(R.string.no_internet_connection), requireView())
            requireContext().vibrate()
            return
        }

        if (callIsUnavailable(existUser) || event.iCanCall.not()) {
            showCallIsRestrictedAlert(existUser.name, chatIsUnavailable(existUser).not())
            viewModel.callUnavailable(existUser.userId)
            return
        }

        startCall(existUser.toUserChat())
        requireContext().vibrate()
    }

    private fun startCall(companion: UserChat) {
        setPermissions(
            object : PermissionDelegate.Listener {
                override fun onGranted() {
                    companion.let {
                        act.onStartCall(it, false, null, null, null)
                    }
                }

                override fun onDenied() {
                    NToast.with(requireView()).text(getString(R.string.you_must_grant_permissions)).typeAlert()
                        .durationLong().button(getString(R.string.allow)) {
                            startCall(companion)
                        }.show()
                }

                override fun onError(error: Throwable?) {
                    showCommonError(getText(R.string.access_is_denied), requireView())
                }
            }, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
        )
    }

    private fun showCallIsRestrictedAlert(userName: String?, canChat: Boolean) {
        MeeraCallIsRestrictedBottomSheetFragment.show(
            fragmentManager = childFragmentManager,
            userName = userName,
            canChat = canChat
        )
    }

    private fun showFriendIncomingStatusMenu(
        isNotificationsAvailable: Boolean,
        isNotificationsEnabled: Boolean,
        approved: Boolean,
        influencer: Boolean,
        friendStatus: Int
    ) {
        MeeraConfirmDialogUnlimitedListBuilder().setHeader(R.string.actions).setListItems(
            initListItemFriendIncomingMenu(
                isNotificationsAvailable = isNotificationsAvailable,
                isNotificationsEnabled = isNotificationsEnabled
            )
        ).setItemListener {
            initFriendIncomingMenuListener(
                action = it as MeeraUnsubscribeMenuAction,
                approved = approved,
                influencer = influencer,
                friendStatus = friendStatus
            )
        }.show(childFragmentManager)
    }

    private fun initFriendIncomingMenuListener(
        action: MeeraUnsubscribeMenuAction, approved: Boolean, influencer: Boolean, friendStatus: Int
    ) {
        when (action) {
            MeeraUnsubscribeMenuAction.DisableNotification -> {
                submitUIAction(UserProfileUIAction.ClickSubscribeNotification(false))
            }

            MeeraUnsubscribeMenuAction.EnableNotification -> {
                submitUIAction(UserProfileUIAction.ClickSubscribeNotification(true))
            }

            MeeraUnsubscribeMenuAction.Subscribe -> {
                userProfile?.let { user ->
                    submitUIAction(
                        UserProfileUIAction.OnSubscribeClicked(
                            isSubscribed = user.settingsFlags.isSubscriptionOn,
                            userId = user.userId,
                            friendStatus = user.friendStatus.intStatus,
                            approved = user.approved,
                            topContent = user.topContentMaker,
                            message = binding.root.resources.getString(R.string.meera_subscribed_on_user_notif_on)
                        )
                    )
                }
            }

            MeeraUnsubscribeMenuAction.Unsubscribe -> {
                submitUIAction(UserProfileUIAction.UnsubscribeFromUserClickedAction)
            }

            MeeraUnsubscribeMenuAction.AddFriend -> {
                submitUIAction(
                    UserProfileUIAction.OnAddFriendClicked(
                        approved = approved, influencer = influencer, friendStatus = friendStatus
                    )
                )
            }

            MeeraUnsubscribeMenuAction.AcceptRequest -> {
                userProfile?.let { user ->
                    submitUIAction(
                        UserProfileUIAction.OnAddFriendClicked(
                            friendStatus = user.friendStatus.intStatus,
                            approved = user.approved,
                            influencer = influencer
                        )
                    )
                }
            }

            MeeraUnsubscribeMenuAction.RejectRequest -> {
                userProfile?.let { user ->
                    submitUIAction(
                        UserProfileUIAction.RemoveFriendClickedAction(
                            cancellingFriendRequest = true
                        )
                    )
                }
            }

            MeeraUnsubscribeMenuAction.RejectRequestUnsubscribe -> {
                userProfile?.let { user ->
                    submitUIAction(
                        UserProfileUIAction.RemoveFriendAndUnsubscribe(
                            cancellingFriendRequest = true
                        )
                    )
                }
            }

            else -> Unit
        }
    }

    private fun initListItemFriendIncomingMenu(
        isNotificationsAvailable: Boolean, isNotificationsEnabled: Boolean
    ): List<MeeraConfirmDialogUnlimitedNumberItemsData> {
        return listOf(
            MeeraConfirmDialogUnlimitedNumberItemsData(
                name = R.string.add_to_friends,
                icon = R.drawable.ic_outlined_user_add_m,
                action = MeeraUnsubscribeMenuAction.Unsubscribe,
            ), getNotificationItemUnsubscribeMenu(
                isNotificationsAvailable = isNotificationsAvailable,
                isNotificationsEnabled = isNotificationsEnabled
            ), MeeraConfirmDialogUnlimitedNumberItemsData(
                name = R.string.unsubscribe,
                icon = R.drawable.ic_outlined_unfollow_m,
                contentColor = R.color.uiKitColorAccentWrong,
                action = MeeraUnsubscribeMenuAction.Unsubscribe,
            )
        )
    }

    private fun showPublishPostAlert(imagePath: String, animation: String? = null) {
        MeeraPostAvatarBottomSheetFragment.getInstance(photoPath = imagePath, animation = animation)
            .show(childFragmentManager)
    }

    override fun onPublishOptionsSelected(
        imagePath: String,
        animation: String?,
        createAvatarPost: Int,
        saveSettings: Int,
        amplitudeActionType: AmplitudeAlertPostWithNewAvatarValuesActionType
    ) {
        showProgress()
        submitUIAction(
            UserProfileUIAction.OnPublishOptionsSelected(
                imagePath = imagePath,
                animation = animation,
                createAvatarPost = createAvatarPost,
                saveSettings = saveSettings,
                amplitudeActionType = amplitudeActionType
            )
        )
    }

    private fun showFriendSubscribeMenu(
        friendStatus: Int = -1,
    ) {
        MeeraConfirmDialogUnlimitedListBuilder().setHeader(R.string.actions).setListItems(
            initListItemFriendsUnsubscribeMenu(
                friendStatus = friendStatus
            )
        ).setItemListener {
            initUnsubscribeMenuListener(it as MeeraUnsubscribeMenuAction, friendStatus)
        }.show(childFragmentManager)
    }

    private fun showIncomingSubscribeMenu(
        approved: Boolean,
        influencer: Boolean,
        isNotificationsAvailable: Boolean,
        isNotificationsEnabled: Boolean,
        friendStatus: Int = -1
    ) {
        MeeraConfirmDialogUnlimitedListBuilder().setHeader(R.string.actions).setListItems(
            initListItemIncomingSubscribeMenu(
                isNotificationsAvailable = isNotificationsAvailable, isNotificationsEnabled = isNotificationsEnabled
            )
        ).setItemListener {
            initFriendIncomingMenuListener(
                action = it as MeeraUnsubscribeMenuAction,
                approved = approved,
                influencer = influencer,
                friendStatus = friendStatus
            )
        }.show(childFragmentManager)
    }

    private fun showIncomingUnsubscribeMenu(
        approved: Boolean,
        influencer: Boolean,
        friendStatus: Int = -1,
    ) {
        MeeraConfirmDialogUnlimitedListBuilder().setHeader(R.string.actions).setListItems(
            initListItemIncomingUnsubscribeMenu()
        ).setItemListener {
            initFriendIncomingMenuListener(
                action = it as MeeraUnsubscribeMenuAction,
                approved = approved,
                influencer = influencer,
                friendStatus = friendStatus
            )
        }.show(childFragmentManager)
    }

    private fun showUnsubscribeMenu(
        isNotificationsAvailable: Boolean,
        isNotificationsEnabled: Boolean,
        friendStatus: Int = -1,
    ) {
        MeeraConfirmDialogUnlimitedListBuilder().setHeader(R.string.actions).setListItems(
            initListItemUnsubscribeMenu(
                isNotificationsAvailable = isNotificationsAvailable,
                isNotificationsEnabled = isNotificationsEnabled,
                friendStatus = friendStatus
            )
        ).setItemListener {
            initUnsubscribeMenuListener(it as MeeraUnsubscribeMenuAction, friendStatus)
        }.show(childFragmentManager)
    }

    private fun showSubscribeMenu(
        friendStatus: Int = -1,
    ) {
        MeeraConfirmDialogUnlimitedListBuilder().setHeader(R.string.actions).setListItems(
            initListItemSubscribeMenu(
                friendStatus = friendStatus
            )
        ).setItemListener {
            initUnsubscribeMenuListener(it as MeeraUnsubscribeMenuAction, friendStatus)
        }.show(childFragmentManager)
    }

    private fun initUnsubscribeMenuListener(action: MeeraUnsubscribeMenuAction, friendStatus: Int) {
        when (action) {
            MeeraUnsubscribeMenuAction.DisableNotification -> {
                submitUIAction(UserProfileUIAction.ClickSubscribeNotification(false))
            }

            MeeraUnsubscribeMenuAction.EnableNotification -> {
                submitUIAction(UserProfileUIAction.ClickSubscribeNotification(true))
            }

            MeeraUnsubscribeMenuAction.RemoveFriend -> {
                if (friendStatus == FRIEND_STATUS_CONFIRMED) {
                    submitUIAction(UserProfileUIAction.RemoveFriendClickedAction(false))
                } else {
                    submitUIAction(UserProfileUIAction.RemoveFriendClickedAction(true))
                }
            }

            MeeraUnsubscribeMenuAction.RemoveUnsubscribeFriend -> {
                if (friendStatus == FRIEND_STATUS_CONFIRMED) {
                    submitUIAction(UserProfileUIAction.RemoveFriendAndUnsubscribe(false))
                } else {
                    submitUIAction(UserProfileUIAction.RemoveFriendAndUnsubscribe(true))
                }
            }

            MeeraUnsubscribeMenuAction.Unsubscribe -> {
                submitUIAction(UserProfileUIAction.UnsubscribeFromUserClickedAction)
            }

            MeeraUnsubscribeMenuAction.Subscribe -> {
                userProfile?.let { user ->
                    submitUIAction(
                        UserProfileUIAction.OnSubscribeClicked(
                            isSubscribed = user.settingsFlags.isSubscriptionOn,
                            userId = user.userId,
                            friendStatus = user.friendStatus.intStatus,
                            approved = user.approved,
                            topContent = user.topContentMaker,
                            message = binding.root.resources.getString(R.string.meera_subscribed_on_user_notif_on)
                        )
                    )
                }
            }

            else -> Unit
        }
    }

    private fun handleAvatarsUi(count: Int?, items: List<PhotoModel>) {
        binding.apply {
            items.firstOrNull()?.let {
                if (it.animation.isNullOrEmpty().not()) {
                    avatarView.updateLayoutParams {
                        width = MATCH_PARENT
                        height = MATCH_PARENT
                    }
                    avatarView.setStateAsync(it.animation ?: "", lifecycleScope)
                    avatarView.avatarIsReadyCallback = {
                        avatarView.startParallaxEffect()
                    }
                    avatarView.visible()
                } else {
                    avatarView.gone()
                    userpicUserPhoto.findViewById<ShapeableImageView>(R.id.iv_avatar)
                        .loadGlideFullSizeCircle(it.imageUrl)
                    userpicUserPhoto.findViewById<TextView>(R.id.tv_name).gone()
                }
                userpicUserPhoto.setOnLongClickListener {
                    if (userProfile?.blacklistedMe == true
                        || userProfile?.blacklistedByMe == true
                    ) return@setOnLongClickListener true
                    viewModel.handleUIAction(
                        UserProfileUIAction.OnShowImage(
                            position = 0, listPhotoEntity = emptyList(), isAvatarPhoto = true
                        )
                    )
                    return@setOnLongClickListener true
                }
            } ?: run {
                val avatarPlaceholder = if (userProfile?.gender != 1) {
                    R.drawable.ic_woman_avatar_placeholder
                } else {
                    R.drawable.ic_man_avatar_placeholder
                }
                userpicUserPhoto.setConfig(UserpicUiModel(userAvatarRes = avatarPlaceholder))
            }
            if (userProfile?.blacklistedMe == true || userProfile?.blacklistedByMe == true) return

            items.getOrNull(PHOTO_POSITION_LEFT_TOP)?.let {
                sivLeftTop.visible()
                sivLeftTop.loadGlideFullSizeCircle(it.imageUrl)
                sivLeftTop.setThrottledClickListener {
                    needAuthToNavigate {
                        viewModel.handleUIAction(
                            UserProfileUIAction.OnShowImage(
                                position = PHOTO_POSITION_LEFT_TOP, listPhotoEntity = emptyList(), isAvatarPhoto = true
                            )
                        )
                    }
                }
                flLeftTop.setOnClickListener(null)
            } ?: run {
                sivLeftTop.gone()
                flLeftTop.setThrottledClickListener {
                    if (viewModel.isMe()) showSelectPhotoMenu(true)
                }
            }
            items.getOrNull(PHOTO_POSITION_RIGHT_TOP)?.let {
                sivRightTop.visible()
                sivRightTop.loadGlideFullSizeCircle(it.imageUrl)
                sivRightTop.setThrottledClickListener {
                    needAuthToNavigate {
                        viewModel.handleUIAction(
                            UserProfileUIAction.OnShowImage(
                                position = PHOTO_POSITION_RIGHT_TOP, listPhotoEntity = emptyList(), isAvatarPhoto = true
                            )
                        )
                    }
                }
                flRightTop.setOnClickListener(null)
            } ?: run {
                sivRightTop.gone()
                flRightTop.setThrottledClickListener {
                    if (viewModel.isMe()) showSelectPhotoMenu(true)
                }
            }
            items.getOrNull(PHOTO_POSITION_LEFT_BOTTOM)?.let {
                sivLeftBottom.visible()
                sivLeftBottom.loadGlideFullSizeCircle(it.imageUrl)
                sivLeftBottom.setThrottledClickListener {
                    needAuthToNavigate {
                        viewModel.handleUIAction(
                            UserProfileUIAction.OnShowImage(
                                position = PHOTO_POSITION_LEFT_BOTTOM,
                                listPhotoEntity = emptyList(),
                                isAvatarPhoto = true
                            )
                        )
                    }
                }
                flLeftBottom.setOnClickListener(null)
            } ?: run {
                sivLeftBottom.gone()
                flLeftBottom.setThrottledClickListener {
                    if (viewModel.isMe()) showSelectPhotoMenu(true)
                }
            }
            items.getOrNull(PHOTO_POSITION_RIGHT_BOTTOM)?.let {
                sivRightBottom.visible()
                sivRightBottom.loadGlideFullSizeCircle(it.imageUrl)
                sivRightBottom.setThrottledClickListener {
                    needAuthToNavigate {
                        viewModel.handleUIAction(
                            UserProfileUIAction.OnShowImage(
                                position = PHOTO_POSITION_RIGHT_BOTTOM,
                                listPhotoEntity = emptyList(),
                                isAvatarPhoto = true
                            )
                        )
                    }
                }
                flRightBottom.setOnClickListener(null)
            } ?: run {
                sivRightBottom.gone()
                flRightBottom.setThrottledClickListener {
                    if (viewModel.isMe()) showSelectPhotoMenu(true)
                }
            }

            val morePhotosCount = (count ?: 0) - PHOTOS_ON_SIDES_COUNT
            if (morePhotosCount > 0) {
                tvPhotosCounter.text = "+$morePhotosCount"
                tvPhotosCounter.visible()
                tvPhotosCounter.setThrottledClickListener {
                    needAuthToNavigate {
                        viewModel.handleUIAction(
                            UserProfileUIAction.OnShowImage(
                                position = PHOTO_POSITION_MORE, listPhotoEntity = emptyList(), isAvatarPhoto = true
                            )
                        )
                    }
                }
            } else {
                tvPhotosCounter.gone()
            }
            submitUIAction(UserProfileUIAction.AvatarsAlreadySet(count ?: items.size))
        }
    }

    private fun initListItemFriendsUnsubscribeMenu(
        friendStatus: Int = -1
    ): List<MeeraConfirmDialogUnlimitedNumberItemsData> {
        return listOf(
            getSubscribeItemUnsubscribeMenu(userProfile?.settingsFlags?.isSubscriptionOn == true),
            MeeraConfirmDialogUnlimitedNumberItemsData(
                name = if (friendStatus == FRIEND_STATUS_CONFIRMED) {
                    R.string.friends_remove_friend
                } else {
                    R.string.meera_cancel_request
                },
                contentColor = R.color.uiKitColorAccentWrong,
                icon = R.drawable.ic_outlined_user_minus_m,
                action = MeeraUnsubscribeMenuAction.RemoveFriend,
            )
        )
    }

    private fun initListItemIncomingSubscribeMenu(
        isNotificationsAvailable: Boolean,
        isNotificationsEnabled: Boolean,
    ): List<MeeraConfirmDialogUnlimitedNumberItemsData> {
        return listOf(
            getNotificationItemUnsubscribeMenu(
                isNotificationsAvailable = isNotificationsAvailable, isNotificationsEnabled = isNotificationsEnabled
            ),
            getSubscribeItemUnsubscribeMenu(userProfile?.settingsFlags?.isSubscriptionOn == true),
            MeeraConfirmDialogUnlimitedNumberItemsData(
                name = R.string.accept_request,
                icon = R.drawable.ic_outlined_following_m,
                action = MeeraUnsubscribeMenuAction.AcceptRequest,
            ),
            MeeraConfirmDialogUnlimitedNumberItemsData(
                name = R.string.reject_friend_request,
                icon = R.drawable.ic_outlined_user_delete_m,
                contentColor = R.color.uiKitColorAccentWrong,
                action = MeeraUnsubscribeMenuAction.RejectRequest,
            ),
            MeeraConfirmDialogUnlimitedNumberItemsData(
                name = R.string.meera_reject_request_unsubscribe,
                icon = R.drawable.ic_outlined_delete_m,
                contentColor = R.color.uiKitColorAccentWrong,
                action = MeeraUnsubscribeMenuAction.RejectRequestUnsubscribe,
            )
        )
    }

    private fun initListItemIncomingUnsubscribeMenu(): List<MeeraConfirmDialogUnlimitedNumberItemsData> {
        return listOf(
            getSubscribeItemUnsubscribeMenu(userProfile?.settingsFlags?.isSubscriptionOn == true),
            MeeraConfirmDialogUnlimitedNumberItemsData(
                name = R.string.accept_request,
                icon = R.drawable.ic_outlined_following_m,
                action = MeeraUnsubscribeMenuAction.AcceptRequest,
            ),
            MeeraConfirmDialogUnlimitedNumberItemsData(
                name = R.string.reject_friend_request,
                icon = R.drawable.ic_outlined_user_delete_m,
                contentColor = R.color.uiKitColorAccentWrong,
                action = MeeraUnsubscribeMenuAction.RejectRequest,
            )
        )
    }

    private fun initListItemUnsubscribeMenu(
        isNotificationsAvailable: Boolean,
        isNotificationsEnabled: Boolean,
        friendStatus: Int = -1,
    ): List<MeeraConfirmDialogUnlimitedNumberItemsData> {

        return listOf(
            getNotificationItemUnsubscribeMenu(
                isNotificationsAvailable = isNotificationsAvailable, isNotificationsEnabled = isNotificationsEnabled
            ),
            getSubscribeItemUnsubscribeMenu(userProfile?.settingsFlags?.isSubscriptionOn == true),
            MeeraConfirmDialogUnlimitedNumberItemsData(
                name = if (friendStatus == FRIEND_STATUS_CONFIRMED) {
                    R.string.friends_remove_friend
                } else {
                    R.string.meera_cancel_request
                },
                contentColor = R.color.uiKitColorAccentWrong,
                icon = R.drawable.ic_outlined_user_minus_m,
                action = MeeraUnsubscribeMenuAction.RemoveFriend,
            ),
            MeeraConfirmDialogUnlimitedNumberItemsData(
                name = if (friendStatus == FRIEND_STATUS_CONFIRMED) {
                    R.string.meera_friends_remove_friend
                } else {
                    R.string.meera_cancel_friendship_request_dialog_cancel_request_and_unsub
                },
                contentColor = R.color.uiKitColorAccentWrong,
                icon = R.drawable.ic_outlined_delete_m,
                action = MeeraUnsubscribeMenuAction.RemoveUnsubscribeFriend,
            )
        )
    }

    private fun initListItemSubscribeMenu(
        friendStatus: Int = -1,
    ): List<MeeraConfirmDialogUnlimitedNumberItemsData> {

        return listOf(
            getSubscribeItemUnsubscribeMenu(userProfile?.settingsFlags?.isSubscriptionOn == true),
            MeeraConfirmDialogUnlimitedNumberItemsData(
                name = if (friendStatus == FRIEND_STATUS_CONFIRMED) {
                    R.string.friends_remove_friend
                } else {
                    R.string.meera_cancel_request
                },
                contentColor = R.color.uiKitColorAccentWrong,
                icon = R.drawable.ic_outlined_user_minus_m,
                action = MeeraUnsubscribeMenuAction.RemoveFriend,
            )
        )
    }

    private fun getNotificationItemUnsubscribeMenu(
        isNotificationsAvailable: Boolean, isNotificationsEnabled: Boolean
    ): MeeraConfirmDialogUnlimitedNumberItemsData {
        return when {
            isNotificationsAvailable && isNotificationsEnabled -> {
                MeeraConfirmDialogUnlimitedNumberItemsData(
                    name = R.string.meera_disable_notifications,
                    icon = R.drawable.ic_outlined_bell_off_m,
                    action = MeeraUnsubscribeMenuAction.DisableNotification,
                )
            }

            isNotificationsAvailable && isNotificationsEnabled.not() -> {
                MeeraConfirmDialogUnlimitedNumberItemsData(
                    name = R.string.meera_enable_notifications,
                    icon = R.drawable.ic_outlined_bell_on_m,
                    action = MeeraUnsubscribeMenuAction.EnableNotification,
                )
            }

            else -> error("No such an item type.")
        }
    }

    private fun getSubscribeItemUnsubscribeMenu(
        isSubscriptionOn: Boolean
    ): MeeraConfirmDialogUnlimitedNumberItemsData {
        return if (isSubscriptionOn) {
            MeeraConfirmDialogUnlimitedNumberItemsData(
                name = R.string.unsubscribe,
                icon = R.drawable.ic_outlined_unfollow_m,
                contentColor = R.color.uiKitColorAccentWrong,
                action = MeeraUnsubscribeMenuAction.Unsubscribe
            )
        } else {
            MeeraConfirmDialogUnlimitedNumberItemsData(
                name = R.string.general_subscribe,
                icon = R.drawable.ic_outlined_follow_m,
                action = MeeraUnsubscribeMenuAction.Subscribe,
            )
        }
    }

    private fun openAvatarCreator() {
        observeAvatarChangeListener()
        val avatarState = if (userProfile?.gender == true.toInt()) {
            AnimatedAvatarUtils.DEFAULT_MALE_STATE
        } else {
            AnimatedAvatarUtils.DEFAULT_FEMALE_STATE
        }
        findNavController().safeNavigate(
            R.id.action_userInfoFragment_to_meeraContainerAvatarFragment, bundle = bundleOf(
                IArgContainer.ARG_AVATAR_STATE to avatarState
            )
        )
    }

    private fun observeAvatarChangeListener() {
        setFragmentResultListener(
            REQUEST_NMR_KEY_AVATAR
        ) { _, bundle ->
            val avatarState: String = bundle.getString(NMR_AVATAR_STATE_JSON_KEY) ?: return@setFragmentResultListener
            submitUIAction(UserProfileUIAction.OnLiveAvatarChanged(avatarState))
        }

    }

    private fun openCameraToChangeAvatar() {
        checkMediaPermissions(object : PermissionDelegate.Listener {

            override fun onGranted() {
                openCameraToChangeAvatarWithPermissionState(PermissionState.GRANTED)
            }

            override fun onDenied() {
                openCameraToChangeAvatarWithPermissionState(PermissionState.NOT_GRANTED_CAN_BE_REQUESTED)
            }

            override fun needOpenSettings() {
                openCameraToChangeAvatarWithPermissionState(PermissionState.NOT_GRANTED_OPEN_SETTINGS)
            }
        })
    }

    private fun openCameraToChangeAvatarWithPermissionState(permissionState: PermissionState) {
        needAuthToNavigate {
            mediaPicker = loadSingleImageUri(
                activity = requireActivity(),
                viewLifecycleOwner = viewLifecycleOwner,
                type = MediaControllerOpenPlace.Avatar,
                cameraType = MediaViewerCameraTypeEnum.CAMERA_ORIENTATION_FRONT,
                suggestionsMenu = SuggestionsMenu(this, SuggestionsMenuType.ROAD),
                permissionState = permissionState,
                tedBottomSheetPermissionActionsListener = this,
                loadImagesCommonCallback = MeeraCoreTedBottomPickerActDependencyProvider(
                    act = act, onReadyImageUri = { imagePath ->
                        if (viewModel.getMediaType(imagePath) == FileUtilsImpl.MEDIA_TYPE_IMAGE_GIF) {
                            val path = imagePath.path ?: return@MeeraCoreTedBottomPickerActDependencyProvider
                            checkAvatarPostSettings(path)
                        } else {
                            viewModel.handleUIAction(UserProfileUIAction.OnEditorOpen)
                            act.getMediaControllerFeature().open(
                                uri = imagePath,
                                openPlace = MediaControllerOpenPlace.Avatar,
                                callback = object : MediaControllerCallback {
                                    override fun onPhotoReady(
                                        resultUri: Uri, nmrAmplitude: NMRPhotoAmplitude?
                                    ) {
                                        val path = resultUri.path ?: return
                                        checkAvatarPostSettings(path)
                                        viewModel.handleUIAction(
                                            UserProfileUIAction.EditAvatar(nmrAmplitude)
                                        )
                                    }

                                    override fun onError() {
                                        showCommonError(getText(R.string.error_editing_media), requireView())
                                    }
                                })
                        }
                    }),
                cameraLensFacing = CameraCharacteristics.LENS_FACING_FRONT
            )
        }
    }

    private fun checkAvatarPostSettings(imagePath: String, animation: String? = null) {
        viewModel.requestCreateAvatarPostSettings(imagePath = imagePath, animation = animation)
    }

    private fun addPhoto() {
        checkMediaPermissions(object : PermissionDelegate.Listener {

            override fun onGranted() {
                addPhotoWithPermissionState(PermissionState.GRANTED)
            }

            override fun onDenied() {
                addPhotoWithPermissionState(PermissionState.NOT_GRANTED_CAN_BE_REQUESTED)
            }

            override fun needOpenSettings() {
                addPhotoWithPermissionState(PermissionState.NOT_GRANTED_OPEN_SETTINGS)
            }
        })
    }

    private fun addPhotoWithPermissionState(permissionState: PermissionState) {
        needAuthToNavigate {
            mediaPicker = loadMultiImage(
                activity = requireActivity(),
                viewLifecycleOwner = viewLifecycleOwner,
                maxCount = 5,
                type = MediaControllerOpenPlace.CreatePost,
                message = "",
                suggestionsMenu = SuggestionsMenu(this, SuggestionsMenuType.ROAD),
                permissionState = permissionState,
                tedBottomSheetPermissionActionsListener = this,
                loadImagesCommonCallback = MeeraCoreTedBottomPickerActDependencyProvider(
                    act = act,
                    onReadyImagesUri = { images -> uploadToGallery(images) },
                    onReadyImagesUriWithText = { images, _ -> uploadToGallery(images) }),
                cameraLensFacing = CameraCharacteristics.LENS_FACING_BACK,
            )
        }
    }

    private fun uploadToGallery(images: List<Uri>) {
        submitUIAction(UserProfileUIAction.UploadToGallery(images))
    }

    fun openProfilePhotoViewerFragment(
        isOwnProfile: Boolean, position: Int, userId: Long, isAvatarPhoto: Boolean
    ) {
        val origin = if (isOwnProfile) {
            DestinationOriginEnum.OWN_PROFILE
        } else {
            DestinationOriginEnum.OTHER_PROFILE
        }
        needAuthToNavigate {
            val bundle = bundleOf(
                IArgContainer.ARG_IS_PROFILE_PHOTO to isAvatarPhoto,
                IArgContainer.ARG_IS_OWN_PROFILE to isOwnProfile,
                ARG_USER_ID to userId,
                IArgContainer.ARG_GALLERY_POSITION to position,
                IArgContainer.ARG_GALLERY_ORIGIN to origin
            )
            when {
                isUserSnippetDataFull == true -> {
                    NavigationManager.getManager().topNavController.safeNavigate(
                        R.id.action_userInfoFragment_to_meeraProfilePhotoViewerFragment2, bundle = bundle
                    )
                }

                isOpenFromNotification == true -> {
                    findNavController().safeNavigate(R.id.meeraProfilePhotoViewerFragment, bundle = bundle)
                }

                else -> {
                    findNavController().safeNavigate(
                        resId = R.id.action_userInfoFragment_to_meeraProfilePhotoViewerFragment2, bundle = bundle
                    )
                }
            }
        }
    }

    private fun openVehicleInfoFragment(vehicleId: String, userId: Long) {
        needAuthToNavigate {
            findNavController().safeNavigate(
                R.id.meeraVehicleInfoFragment, bundleOf(
                    IArgContainer.ARG_CAR_ID to vehicleId, ARG_USER_ID to userId
                )
            )
        }
    }

    private fun openVehicleListFragmentNew(userId: Long, accountType: Int?, accountColor: Int?) {
        needAuthToNavigate {
            findNavController().safeNavigate(
                resId = R.id.meeraVehicleListFragment, bundle = bundleOf(
                    ARG_USER_ID to userId, IArgContainer.ARG_USER_VIP_STATUS to VipStatus(accountType, accountColor)
                ), navBuilder = { builder ->
                    builder.apply { addAnimationTransitionByDefault() }
                })
        }
    }

    private fun openUserListFriendsOrShowAuth(
        userId: Long, actionType: MeeraFriendsHostFragment.SelectedPage?, name: String
    ) {
        needAuthToNavigate {
            val bundle = bundleOf(
                ARG_USER_ID to userId,
                IArgContainer.ARG_TYPE_FOLLOWING to actionType,
                IArgContainer.ARG_USER_NAME to name,
                IArgContainer.ARG_FRIENDS_HOST_OPENED_FROM to FriendsHostOpenedType.FRIENDS_HOST_OPENED_FROM_PROFILE
            )

            when {
                isUserSnippetDataFull == true -> {
                    NavigationManager.getManager().topNavController.safeNavigate(
                        R.id.action_userInfoFragment_to_meeraFriendsHostFragment, bundle = bundle
                    )
                }

                isOpenFromNotification == true -> {
                    findNavController().safeNavigate(R.id.meeraFriendsHostFragment, bundle = bundle)
                }

                else -> {
                    findNavController().safeNavigate(
                        R.id.action_userInfoFragment_to_meeraFriendsHostFragment, bundle = bundle
                    )
                }
            }
        }
    }

    private fun openGridProfilePhotoFragment(userId: Long, photoCount: Int) {
        needAuthToNavigate {
            val bundle = bundleOf(
                ARG_USER_ID to userId, IArgContainer.ARG_GALLERY_IMAGES_COUNT to photoCount
            )
            when {
                isUserSnippetDataFull == true -> {
                    NavigationManager.getManager().topNavController.safeNavigate(
                        R.id.action_userInfoFragment_to_meeraGridProfilePhotoFragment2, bundle = bundle
                    )
                }

                isOpenFromNotification == true -> {
                    findNavController().safeNavigate(R.id.meeraGridProfilePhotoFragment, bundle = bundle)
                }

                else -> {
                    findNavController().safeNavigate(
                        R.id.action_userInfoFragment_to_meeraGridProfilePhotoFragment2, bundle = bundle
                    )
                }
            }
        }
    }

    private fun openSubscribersListFragment() {
        needAuthToNavigate {
            findNavController().safeNavigate(
                R.id.action_userInfoFragment_to_meeraSubscribersListFragment, bundle = bundleOf(
                    ARG_FRIEND_LIST_MODE to SUBSCRIBERS
                )
            )
        }
    }

    private fun openSubscriptionsListFragment() {
        needAuthToNavigate {
            findNavController().safeNavigate(
                R.id.action_userInfoFragment_to_meeraSubscriptionsListFragment, bundle = bundleOf(
                    ARG_FRIEND_LIST_MODE to SUBSCRIPTIONS
                )
            )
        }
    }


    private var isLoadedUserRoad = false
    private var forceLoadRoad = false


    private fun handleProfileUIList(profileUIList: List<UserInfoRecyclerData>, scrollToTop: Boolean) {
        if (profileUIList.isEmpty()) return
        numberFloorsProfile = profileUIList.size
        adapter.submitList(profileUIList) {
            val concatAdapter = (binding.rvUserInfo.adapter as ConcatAdapter)
            if (userProfile?.blacklistedMe == true
                || userProfile?.blacklistedByMe == true
                || userProfile?.accountDetails?.isAccountBlocked == true
            ) {
                concatAdapter.removeAdapter(getAdapterPosts())
            } else {
                concatAdapter.addAdapter(getAdapterPosts())
                loadPosts()
            }
            if (scrollToTop) binding.rvUserInfo.scrollToPosition(0)
        }
    }

    private fun loadPosts() {
        if (!isLoadedUserRoad || forceLoadRoad) {
            loadBasePosts()
            isLoadedUserRoad = true
            forceLoadRoad = false
        }
    }

    private fun showCameraSettingsDialog() {
        MeeraConfirmDialogBuilder().setHeader(R.string.camera_settings_dialog_title)
            .setDescription(R.string.camera_settings_dialog_description)
            .setTopBtnText(R.string.camera_settings_dialog_action)
            .setBottomBtnText(R.string.camera_settings_dialog_cancel).setCancelable(false)
            .setTopClickListener { requireContext().openSettingsScreen() }.show(childFragmentManager)
    }

    private fun showMyDotsMenu(effect: UserInfoViewEffect) {
        effect as UserProfileDialogNavigation.ShowDotsMenu
        MeeraConfirmDialogUnlimitedListBuilder().setHeader(R.string.actions).setItemListener {
            initDotsMenuListener(it as MeeraConfirmDialogAction)
        }.setListItems(initListItemMenuIsMeDotsMenu(effect)).show(childFragmentManager)
    }

    private fun initListItemMenuIsMeDotsMenu(effect: UserProfileDialogNavigation.ShowDotsMenu): List<MeeraConfirmDialogUnlimitedNumberItemsData> {
        return listOf(
            MeeraConfirmDialogUnlimitedNumberItemsData(
                name = R.string.share_profile,
                icon = R.drawable.ic_outlined_repost_m,
                action = MeeraConfirmDialogAction.ShareProfile,
            ),
            MeeraConfirmDialogUnlimitedNumberItemsData(
                name = R.string.copy_link,
                icon = R.drawable.ic_outlined_copy_m,
                action = MeeraConfirmDialogAction.CopyLink(
                    profileLink = effect.profileLink, uniquename = effect.profile.uniquename
                ),
            ),
            MeeraConfirmDialogUnlimitedNumberItemsData(
                name = R.string.profile_settings,
                icon = R.drawable.ic_outlined_settings_m,
                action = MeeraConfirmDialogAction.Settings,
            ),
        )
    }

    private fun showNotMyDotsMenu(effect: UserProfileDialogNavigation.ShowDotsMenu) {
        if (effect.profile.accountDetails.isAccountDeleted) {
            if (effect.profile.settingsFlags.isSubscriptionOn) {
                MeeraConfirmDialogUnlimitedListBuilder().setHeader(R.string.actions).setItemListener {
                    initUnsubscribeMenuListener(it as MeeraUnsubscribeMenuAction, effect.profile.friendStatus.intStatus)
                }.setListItems(initDeleteUserDotsMenu()).show(childFragmentManager)
            }
        } else {
            MeeraConfirmDialogUnlimitedListBuilder().setHeader(R.string.actions).setItemListener {
                initIsNotMeDotsMenuListener(
                    it as MeeraConfirmDialogAction, effect
                )
            }.setListItems(initListItemMenuIsNotMeDotsMenu(effect)).show(childFragmentManager)
        }
    }

    private fun initIsNotMeDotsMenuListener(
        action: MeeraConfirmDialogAction, effect: UserProfileDialogNavigation.ShowDotsMenu
    ) {
        when (action) {
            MeeraConfirmDialogAction.CallBlock -> {
                submitUIAction(UserProfileUIAction.OnCallPrivacyClickedAction)
            }

            is MeeraConfirmDialogAction.CopyLink -> {
                copyProfileLink(context, action.profileLink, action.uniquename) {
                    submitUIAction(UserProfileUIAction.OnCopyProfileClickedAction)
                }
            }

            MeeraConfirmDialogAction.HidePostRoad -> {
                if (effect.profile.settingsFlags.isHideRoadPosts.not()) {
                    submitUIAction(UserProfileUIAction.ChangePostsPrivacyClickedAction(true))
                } else if (effect.profile.settingsFlags.isHideRoadPosts) {
                    submitUIAction(UserProfileUIAction.ChangePostsPrivacyClickedAction(false))
                }
            }

            MeeraConfirmDialogAction.MessageBlock -> {
                submitUIAction(UserProfileUIAction.OnChatPrivacyClickedAction)
            }

            MeeraConfirmDialogAction.ReportProfile -> {
                submitUIAction(
                    UserProfileUIAction.OnComplainClick(
                        userId = effect.profile.userId, where = AmplitudePropertyWhere.PROFILE
                    )
                )
            }

            MeeraConfirmDialogAction.ShareProfile -> {
                needAuthToNavigate {
                    submitUIAction(UserProfileUIAction.OnShareProfileClickAction)
                }
            }

            MeeraConfirmDialogAction.UserBlock -> {
                submitUIAction(UserProfileUIAction.OnBlacklistUserClickedAction)
            }

            else -> Unit
        }
    }

    private fun initDeleteUserDotsMenu(): List<MeeraConfirmDialogUnlimitedNumberItemsData> {
        return listOf(
            MeeraConfirmDialogUnlimitedNumberItemsData(
                name = R.string.unsubscribe_user_txt,
                icon = R.drawable.ic_outlined_unfollow_m,
                contentColor = R.color.uiKitColorAccentWrong,
                action = MeeraUnsubscribeMenuAction.Unsubscribe,
            )
        )
    }

    private fun initListItemMenuIsNotMeDotsMenu(effect: UserInfoViewEffect): List<MeeraConfirmDialogUnlimitedNumberItemsData> {
        val profile = (effect as UserProfileDialogNavigation.ShowDotsMenu).profile
        val settingsFlags = profile.settingsFlags
        return listOf(
            MeeraConfirmDialogUnlimitedNumberItemsData(
                name = R.string.share_profile,
                icon = R.drawable.ic_outlined_repost_m,
                action = MeeraConfirmDialogAction.ShareProfile,
            ),
            MeeraConfirmDialogUnlimitedNumberItemsData(
                name = R.string.copy_link,
                icon = R.drawable.ic_outlined_copy_m,
                action = MeeraConfirmDialogAction.CopyLink(
                    profileLink = effect.profileLink, uniquename = effect.profile.uniquename
                ),
            ),
            MeeraConfirmDialogUnlimitedNumberItemsData(

                name = if (settingsFlags.userCanChatMe) {
                    R.string.profile_dots_menu_disallow_messages
                } else {
                    R.string.profile_dots_menu_allow_messages
                },
                icon = R.drawable.ic_outlined_message_off_m,
                action = MeeraConfirmDialogAction.MessageBlock,
            ),
            MeeraConfirmDialogUnlimitedNumberItemsData(
                name = if (settingsFlags.userCanCallMe) {
                    R.string.profile_dots_menu_disallow_calls
                } else {
                    R.string.profile_dots_menu_allow_calls
                },
                icon = R.drawable.ic_outlined_call_block_m,
                action = MeeraConfirmDialogAction.CallBlock,
            ),
            if (profile.blacklistedByMe) {
                null
            } else {
                MeeraConfirmDialogUnlimitedNumberItemsData(
                    name = if (settingsFlags.isHideRoadPosts.not()) {
                        R.string.road_hide_user_road
                    } else {
                        R.string.show_user_posts
                    },
                    icon = R.drawable.ic_outlined_eye_off_m,
                    action = MeeraConfirmDialogAction.HidePostRoad,
                )
            },
            MeeraConfirmDialogUnlimitedNumberItemsData(
                name = R.string.complain_about_profile,
                icon = R.drawable.ic_outlined_attention_m,
                action = MeeraConfirmDialogAction.ReportProfile,
            ),
            MeeraConfirmDialogUnlimitedNumberItemsData(
                name = if (settingsFlags.blacklistedByMe.not()) {
                    R.string.general_block
                } else {
                    R.string.general_unblock
                },
                icon = R.drawable.ic_outlined_user_block_m,
                contentColor = R.color.uiKitColorAccentWrong,
                action = MeeraConfirmDialogAction.UserBlock,
            ),
        ).filterNotNull()
    }

    private fun initDotsMenuListener(action: MeeraConfirmDialogAction) {
        when (action) {
            is MeeraConfirmDialogAction.ShareProfile -> {
                submitUIAction(UserProfileUIAction.OnShareProfileClickAction)
            }

            is MeeraConfirmDialogAction.CopyLink -> {
                copyProfileLink(context, action.profileLink, action.uniquename) {
                    submitUIAction(UserProfileUIAction.OnCopyProfileClickedAction)
                }
            }

            is MeeraConfirmDialogAction.Settings -> {
                findNavController().safeNavigate(R.id.action_userInfoFragment_to_meeraProfileSettingsFragment)
            }

            else -> Unit
        }
    }

    private fun meeraShowShareProfileDialog(user: UserProfileUIModel, profileLink: String) {
        MeeraShareSheet().showByType(
            fm = childFragmentManager, shareType = ShareDialogType.ShareProfile(user.userId), event = { shareEvent ->
                when (shareEvent) {
                    is ShareBottomSheetEvent.OnSuccessShareProfile -> {
                        showCommonSuccessMessage(getText(R.string.share_profile_success), requireView())
                    }

                    is ShareBottomSheetEvent.OnErrorShareProfile -> {
                        showCommonError(getText(R.string.share_profile_error), requireView())
                    }

                    is ShareBottomSheetEvent.OnMoreShareButtonClick -> {
                        shareProfileOutside(context, profileLink, user.uniquename)
                    }

                    is ShareBottomSheetEvent.OnErrorUnselectedUser -> {
                        showCommonError(getText(R.string.no_user_selected), requireView())
                    }

                    is ShareBottomSheetEvent.OnClickFindFriendButton -> {
                        findNavController().safeNavigate(
                            R.id.action_userInfoFragment_to_meeraSearchMainFragment, bundle = bundleOf(
                                IArgContainer.ARG_SEARCH_OPEN_PAGE to SearchMainFragment.PAGE_SEARCH_PEOPLE
                            )
                        )
                    }

                    else -> Unit
                }
            })
    }

    private fun showSelectPhotoMenu(onlyAvatars: Boolean) {
        val headerTitleRes = if (onlyAvatars) R.string.meera_avatars else R.string.meera_moments_and_photo
        MeeraConfirmDialogUnlimitedListBuilder().setHeader(headerTitleRes)
            .setListItems(initSelectPhotoMenu(onlyAvatars)).setCancelable(true)
            .setItemListener { initSelectPhotoListener(it as MeeraConfirmDialogAction) }.show(childFragmentManager)
    }

    private fun initSelectPhotoMenu(onlyAvatars: Boolean): List<MeeraConfirmDialogUnlimitedNumberItemsData> {
        val listItems = mutableListOf<MeeraConfirmDialogUnlimitedNumberItemsData>()
        if (onlyAvatars.not()) {
            listItems.add(
                MeeraConfirmDialogUnlimitedNumberItemsData(
                    name = R.string.meera_create_moments,
                    icon = R.drawable.ic_outlined_cam_m,
                    action = MeeraConfirmDialogAction.CreateMoment,
                )
            )
        }
        listItems.add(
            MeeraConfirmDialogUnlimitedNumberItemsData(
                name = R.string.add_avatar,
                icon = R.drawable.ic_outlined_girl_m,
                action = MeeraConfirmDialogAction.AddAvatar,
            )
        )
        listItems.add(
            MeeraConfirmDialogUnlimitedNumberItemsData(
                name = R.string.create_avatar_action,
                icon = R.drawable.ic_outlined_robot_m,
                action = MeeraConfirmDialogAction.CreateAvatar,
            )
        )
        return listItems
    }

    private fun initSelectPhotoListener(action: MeeraConfirmDialogAction) {
        when (action) {
            is MeeraConfirmDialogAction.CreateMoment -> act.getMomentsViewController().open()

            is MeeraConfirmDialogAction.AddAvatar -> submitUIAction(UserProfileUIAction.OnAvatarChangeClicked)

            is MeeraConfirmDialogAction.CreateAvatar -> submitUIAction(UserProfileUIAction.CreateAvatar)
            else -> Unit
        }
    }

    fun initStartPositionMotionLayout() {
        binding.mlUserInfo.apply {
            jumpToState(R.id.scene_user_info_start)
            setTransition(R.id.transition_user_info_start_to_middle)
            setProgress(0f)
        }
    }

    fun switchSnippetState() {
        binding.rvUserInfo.stopScroll()
        (binding.rvUserInfo.layoutManager as LinearLayoutManager).scrollToPosition(0)
        initStartPositionMotionLayout()

        activity?.findViewById<FrameLayout>(getContainerFragmentId())?.let { containerView ->
            BottomSheetBehavior.from(containerView).state = STATE_COLLAPSED
        }
    }

    fun hideTopButtons() {
        binding.buttonUserInfoSnippetBack.gone()
        binding.flCall.gone()
        binding.buttonUserInfoMenu.gone()
        binding.flBell.gone()
        binding.flChat.gone()
    }

    fun showTopButtons() {
        binding.flCall.visible()
        binding.flChat.visible()
        binding.buttonUserInfoMenu.visible()
        binding.buttonUserInfoSnippetBack.visible()
        if (!viewModel.isMe()) binding.flBell.visible()
    }

    fun updateSnippetBackground() {
        binding.mlUserInfo.background = ContextCompat.getDrawable(
            requireContext(), R.drawable.bg_rectangle_rad_16
        )
    }

    /* Перерисовка уже созданных холдеров для синхронизации с шириной родителя */
    fun updateRecyclerItemsWidth() {
        val adapter = binding.rvUserInfo.adapter ?: return
        for (position in 0 until adapter.itemCount) {
            val holder = binding.rvUserInfo.findViewHolderForAdapterPosition(position)
            if (holder != null && holder is MeeraBasePostHolder) {
                holder.updateViewsWithPresetWidth()
            }
        }
    }

    private fun showProgress() {
        infoSnackbar = UiKitSnackBar.make(
            view = requireView(), params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = getText(R.string.user_personal_info_setting_main_photo),
                    loadingUiState = SnackLoadingUiState.ProgressState
                ),
            )
        )
        infoSnackbar?.show()
    }

    private fun handleSuccessUpload(event: UserInfoViewEffect.OnGoneProgressUserAvatar) {
        val message = when (event.createAvatarPost) {
            CreateAvatarPostEnum.PRIVATE_ROAD.state,
            CreateAvatarPostEnum.MAIN_ROAD.state -> R.string.profile_avatar_update_success_with_post

            else -> R.string.profile_avatar_update_success
        }

        infoSnackbar = UiKitSnackBar.make(
            view = requireView(), params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = getText(message),
                    avatarUiState = AvatarUiState.SuccessIconState,
                ),
            )
        )
        infoSnackbar?.show()
    }

    override fun getParentWidth() = currentSnippetLayoutWidth

    inner class CheckScrollPositionListenerImpl : CheckScrollPositionListener {
        override fun checkVisibilityConnectionButton(isVisible: Boolean) {

            when {
                isVisible && !isVisibleConnectionButton -> {
                    runCatching {
                        binding.flCall.animate().cancel()
                        binding.flChat.animate().cancel()
                        binding.flCall.animate().translationY(-0f).setDuration(DURATION_CONTACT_ANIMATION).start()
                        binding.flChat.animate().translationY(-0f).setDuration(DURATION_CONTACT_ANIMATION).start()
                    }
                }

                !isVisible && isVisibleConnectionButton -> {
                    runCatching {
                        binding.flCall.animate().cancel()
                        binding.flChat.animate().cancel()
                        binding.flCall.animate().translationY(TRANSITION_Y_CONTACT_ANIMATION)
                            .setDuration(DURATION_CONTACT_ANIMATION).start()
                        binding.flChat.animate().translationY(TRANSITION_Y_CONTACT_ANIMATION)
                            .setDuration(DURATION_CONTACT_ANIMATION).start()
                    }
                }
            }
            isVisibleConnectionButton = isVisible
        }

        override fun checkVisibilityUpButton(findFirstVisibleItemPosition: Int, isSwipeUp: Boolean) {
            if (findFirstVisibleItemPosition >= numberFloorsProfile + 2 && isSwipeUp) {
                binding.fabRoadSwope.visible()
            } else {
                binding.fabRoadSwope.invisible()
            }
        }
    }

    companion object {
        const val TAG_PROFILE_STATISTICS_FRAGMENT = "profile_statistics_fragment"
        const val ARG_USER_SNIPPET_FOCUSED = "ARG_USER_SNIPPET_FOCUSED"
        const val USERINFO_SNIPPET_COLLAPSED = "USERINFO_SNIPPET_COLLAPSED"
        const val USERINFO_OPEN_FROM_NOTIFICATION = "USERINFO_OPEN_FROM_NOTIFICATION"
        const val ARG_USER_SNIPPET_DATA_FULL = "ARG_USER_SNIPPET_DATA_FULL"
        const val ARG_USER_SNIPPET_DATA_FULL_NEW = "ARG_USER_SNIPPET_DATA_FULL_NEW"
        const val ARG_USER_SNIPPET_EVENT = "ARG_USER_SNIPPET_EVENT"
        const val PHOTOS_ON_SIDES_COUNT = 5
        const val PHOTO_POSITION_LEFT_TOP = 1
        const val PHOTO_POSITION_RIGHT_TOP = 2
        const val PHOTO_POSITION_LEFT_BOTTOM = 3
        const val PHOTO_POSITION_RIGHT_BOTTOM = 4
        const val PHOTO_POSITION_MORE = 5
        const val USERINFO_SNIPPET_HEIGHT = 420
        const val USERINFO_SNIPPET_HORIZONTAL_MARGIN = 16
        const val USERINFO_HIDDEN_HEIGHT = 240F
        const val USERINFO_HIDE_SHOW_ANIM_DURATION_MS = 300L
        const val DELAY_DELETE_SNACK_BAR_SEC = 4
    }

}
