package com.numplates.nomera3.presentation.view.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat.getDrawable
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkInfo
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.shape.CornerFamily
import com.meera.core.base.BaseLoadImages
import com.meera.core.base.BaseLoadImagesDelegate
import com.meera.core.base.enums.PermissionState
import com.meera.core.dialogs.ConfirmDialogBuilder
import com.meera.core.extensions.asCountString
import com.meera.core.extensions.click
import com.meera.core.extensions.clickAnimate
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.dp
import com.meera.core.extensions.empty
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.invisible
import com.meera.core.extensions.openSettingsScreen
import com.meera.core.extensions.setDrawableClickListener
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setPaddingBottom
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.vibrate
import com.meera.core.extensions.visible
import com.meera.core.permission.PERMISSION_MEDIA_CODE
import com.meera.core.permission.PermissionDelegate
import com.meera.core.utils.ApprovedIconSize
import com.meera.core.utils.NSnackbar
import com.meera.core.utils.TopAuthorApprovedUserModel
import com.meera.core.utils.checkAppRedesigned
import com.meera.core.utils.files.FileUtilsImpl.Companion.MEDIA_TYPE_IMAGE_GIF
import com.meera.core.utils.getAge
import com.meera.core.utils.graphics.NGraphics
import com.meera.core.utils.tedbottompicker.TedBottomSheetDialogFragment
import com.meera.core.utils.tedbottompicker.TedBottomSheetPermissionActionsListener
import com.meera.core.utils.tedbottompicker.models.MediaViewerCameraTypeEnum
import com.meera.db.models.dialog.UserChat
import com.meera.media_controller_api.model.MediaControllerCallback
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.noomeera.nmravatarssdk.NMR_AVATAR_STATE_JSON_KEY
import com.noomeera.nmravatarssdk.REQUEST_NMR_KEY_AVATAR
import com.noomeera.nmravatarssdk.ui.view.AvatarView
import com.noomeera.nmrmediatools.NMRPhotoAmplitude
import com.numplates.nomera3.AVATAR_QUALITY_HIGH
import com.numplates.nomera3.AVATAR_QUALITY_LOW
import com.numplates.nomera3.Act
import com.numplates.nomera3.AnimatedAvatarUtils
import com.numplates.nomera3.App
import com.numplates.nomera3.BuildConfig
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentUserInfoBinding
import com.numplates.nomera3.modules.auth.ui.IAuthStateObserver
import com.numplates.nomera3.modules.auth.util.AuthRequester
import com.numplates.nomera3.modules.auth.util.AuthStatusObserver
import com.numplates.nomera3.modules.auth.util.needAuth
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.baseCore.createAccountTypeEnum
import com.numplates.nomera3.modules.baseCore.domain.model.Gender
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapsnippet.model.MapSnippetCloseMethod
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudePropertyMomentScreenOpenWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeAlertPostWithNewAvatarValuesActionType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.toFriendRelationshipAmplitude
import com.numplates.nomera3.modules.baseCore.ui.location.LocationDelegate
import com.numplates.nomera3.modules.billing.BillingClientWrapper
import com.numplates.nomera3.modules.chat.KEY_MESSAGES_ALLOWED
import com.numplates.nomera3.modules.chat.KEY_SAID_HELLO
import com.numplates.nomera3.modules.chat.KEY_SUBSCRIBED
import com.numplates.nomera3.modules.common.ActivityToolsProvider
import com.numplates.nomera3.modules.communities.data.states.CommunityListEvents
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.feed.ui.entity.DestinationOriginEnum
import com.numplates.nomera3.modules.feed.ui.fragment.BaseFeedFragment
import com.numplates.nomera3.modules.feed.ui.fragment.NetworkRoadType
import com.numplates.nomera3.modules.maps.domain.model.UserSnippetModel
import com.numplates.nomera3.modules.maps.ui.model.MapUserUiModel
import com.numplates.nomera3.modules.maps.ui.snippet.UserSnippetBottomSheetWidget
import com.numplates.nomera3.modules.maps.ui.snippet.model.SnippetState
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentClickOrigin
import com.numplates.nomera3.modules.moments.user.domain.model.UserMomentsPreviewModel
import com.numplates.nomera3.modules.post_view_statistic.data.PostViewRoadSource
import com.numplates.nomera3.modules.remotestyle.presentation.formatter.AllRemoteStyleFormatter
import com.numplates.nomera3.modules.screenshot.delegate.SAVING_PICTURE_DELAY
import com.numplates.nomera3.modules.screenshot.delegate.ScreenshotPopupController
import com.numplates.nomera3.modules.screenshot.ui.entity.ScreenshotPlace
import com.numplates.nomera3.modules.screenshot.ui.entity.ScreenshotPopupData
import com.numplates.nomera3.modules.screenshot.ui.fragment.ScreenshotTakenListener
import com.numplates.nomera3.modules.tags.data.SuggestionsMenuType
import com.numplates.nomera3.modules.tags.ui.base.SuggestionsMenu
import com.numplates.nomera3.modules.user.ui.event.PhoneCallsViewEffect
import com.numplates.nomera3.modules.user.ui.event.RoadPostViewEffect
import com.numplates.nomera3.modules.user.ui.event.UserFeedViewEvent
import com.numplates.nomera3.modules.user.ui.event.UserInfoViewEffect
import com.numplates.nomera3.modules.user.ui.event.UserProfileDialogNavigation
import com.numplates.nomera3.modules.user.ui.event.UserProfileNavigation
import com.numplates.nomera3.modules.user.ui.event.UserProfileTooltipEffect
import com.numplates.nomera3.modules.userprofile.domain.maper.toChatInitUserProfile
import com.numplates.nomera3.modules.userprofile.domain.maper.toUserChat
import com.numplates.nomera3.modules.userprofile.domain.maper.toUserProfileUIList
import com.numplates.nomera3.modules.userprofile.domain.maper.toUserUpdateModel
import com.numplates.nomera3.modules.userprofile.profilestatistics.ui.fragments.ProfileStatisticsContainerBottomSheetFragment
import com.numplates.nomera3.modules.userprofile.ui.MomentPreviewItem
import com.numplates.nomera3.modules.userprofile.ui.MomentsMiniPreviewModel
import com.numplates.nomera3.modules.userprofile.ui.action.NestedRecyclerAction
import com.numplates.nomera3.modules.userprofile.ui.adapter.UserProfileAdapter
import com.numplates.nomera3.modules.userprofile.ui.controller.ProfileDialogController
import com.numplates.nomera3.modules.userprofile.ui.controller.ProfileThemeController
import com.numplates.nomera3.modules.userprofile.ui.controller.ProfileToolbarController
import com.numplates.nomera3.modules.userprofile.ui.entity.ProfileSuggestionsFloorUiEntity
import com.numplates.nomera3.modules.userprofile.ui.entity.SubscribersFloorUiEntity
import com.numplates.nomera3.modules.userprofile.ui.entity.UserUIEntity
import com.numplates.nomera3.modules.userprofile.ui.model.FriendStatus
import com.numplates.nomera3.modules.userprofile.ui.model.ProfileToolbarModelUIModel
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileStateUIModel
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIAction
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIModel
import com.numplates.nomera3.modules.userprofile.ui.navigation.UserInfoNavigator
import com.numplates.nomera3.modules.userprofile.ui.tooltip.TooltipProfileFragmentViewController
import com.numplates.nomera3.modules.userprofile.ui.viewModel.UserProfileViewModel
import com.numplates.nomera3.modules.userprofile.utils.shareProfileOutside
import com.numplates.nomera3.presentation.birthday.ui.BirthdayBottomDialogFragment
import com.numplates.nomera3.presentation.model.enums.CreateAvatarPostEnum
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_POST_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_TRANSIT_FROM
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_ID
import com.numplates.nomera3.presentation.utils.buttons.FabWrapper
import com.numplates.nomera3.presentation.utils.buttons.FabWrapper.Priority.HIGH
import com.numplates.nomera3.presentation.utils.buttons.FabWrapper.Priority.MEDIUM
import com.numplates.nomera3.presentation.utils.buttons.FloatingButtonsManager
import com.numplates.nomera3.presentation.utils.sendUserToAppSettings
import com.numplates.nomera3.presentation.view.callback.IOnBackPressed
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.callisresrticted.CallIsRestrictedAlertListener
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.callisresrticted.CallIsRestrictedBottomSheetFragment
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.postavatar.PostAvatarAlertListener
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.postavatar.PostAvatarBottomSheetFragment
import com.numplates.nomera3.presentation.view.fragments.profilephoto.ProfilePhotoViewerFragment
import com.numplates.nomera3.presentation.view.utils.CoreTedBottomPickerActDependencyProvider
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.view.utils.sharedialog.MeeraShareSheet
import com.numplates.nomera3.presentation.view.utils.sharedialog.ShareBottomSheetEvent
import com.numplates.nomera3.presentation.view.utils.sharedialog.ShareDialogType
import com.numplates.nomera3.presentation.view.utils.sharedialog.SharePostBottomSheet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import kotlin.math.sqrt

private const val INFO_TOOLTIP_MARGIN_BOTTOM = 120
private const val INFO_TOOLTIP_USER_REMOVED_MARGIN_BOTTOM = 24
const val IS_MALE = 1
const val COLLAPSING_CONTAINER_MARGIN_STATUS_EMPTY = 0
const val COLLAPSING_DESC_DEFAULT_MARGIN = 16

private const val FIRST_POST = 0
private const val PRELOAD_DELTA = 5

class UserInfoFragment : BaseFeedFragment<FragmentUserInfoBinding>(),
    AppBarLayout.OnOffsetChangedListener,
    NestedRecyclerAction,
    IAuthStateObserver,
    UserSnippetBottomSheetWidget.Listener,
    BaseLoadImages by BaseLoadImagesDelegate(),
    IOnBackPressed,
    PostAvatarAlertListener,
    CallIsRestrictedAlertListener,
    TedBottomSheetPermissionActionsListener,
    ScreenshotTakenListener {

    companion object {
        const val RECYCLER_VIEW_EXTRA_PADDING = 110
        const val DELAY_REQUEST = 300

        const val TAG_PROFILE_STATISTICS_FRAGMENT = "profile_statistics_fragment"

        const val ARG_USER_SNIPPET_DATA = "ARG_USER_SNIPPET_DATA"
        const val ARG_USER_PIN_DATA = "ARG_USER_PIN_DATA"
        const val ARG_USER_SNIPPET_FOCUSED = "ARG_USER_SNIPPET_FOCUSED"
        const val COUNTER_TOP_MARGIN = 10
        fun setupAvatarQuality(avatar: AvatarView?) {
            avatar?.quality = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                AVATAR_QUALITY_HIGH
            } else {
                AVATAR_QUALITY_LOW
            }
        }
    }

    @Inject
    lateinit var billingClientWrapper: BillingClientWrapper

    @Inject
    lateinit var vmFactory: UserProfileViewModel.Factory


    /** TODO https://nomera.atlassian.net/browse/BR-18804
     * Переписать через обращение к parentFragment
     */
    var userSnippet: UserSnippetBottomSheetWidget? = null

    private var adapterProfile: UserProfileAdapter? = null

    private val userProfileViewModel by viewModels<UserProfileViewModel> {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return vmFactory.create(getAuthRequester()) as T
            }
        }
    }
    private val profileDialogController = ProfileDialogController(::submitUIAction)
    private val toolbarController = ProfileToolbarController(
        profileProvider = { userProfile },
        setColorStatusBar = ::makeColorStatusBar,
        setLightStatusBar = ::makeLightStatusBar
    )
    private val themeController = ProfileThemeController()
    private var tooltipDelegate: TooltipProfileFragmentViewController? = null

    private var isDeletedProfile = false
    private var userProfile: UserProfileUIModel? = null

    private var userSnippetModel: UserSnippetModel? = null
    private var userSnippetFocused = false
    private var isFullUserProfileSetup = false
    private var screenshotPopupData: ScreenshotPopupData? = null
    private var uniqName: String? = null
    private var isScreenshotPopupShown = false
    var userId: Long? = null

    // Устанавливается id выбранного поста из экрана PeoplesFragment
    var selectedPostId: Long? = null

    private var isLoadedUserRoad = false
    private var forceLoadRoad = false

    private var infoTooltip: NSnackbar? = null
    private val navigator = UserInfoNavigator(
        getFM = ::getFM,
        openFragment = { fragment, statusBar, args ->
            add(fragment, statusBar, *args)
        },
        openMoments = { existUserId, openedFrom, hasNewMoments ->
            val where = if (userProfileViewModel.isMe()) {
                AmplitudePropertyMomentScreenOpenWhere.PROFILE_AVATAR
            } else {
                AmplitudePropertyMomentScreenOpenWhere.USER_PROFILE_AVATAR
            }
            act.openUserMoments(
                userId = existUserId,
                fromView = binding?.mmpMomentsPreview,
                openedFrom = openedFrom,
                openedWhere = where,
                viewedEarly = hasNewMoments?.not()
            )
        }
    )

    private fun getFM(): FragmentManager = childFragmentManager

    private var showingSuggestions = false

    private var userSnippetTopCornersRadiusPx = 0f

    private var deniedAndNoRationaleNeededBeforeRequest = false

    private var mediaPicker: TedBottomSheetDialogFragment? = null

    private var avatarsAdapter: AvatarsAdapter? = null

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentUserInfoBinding
        get() = FragmentUserInfoBinding::inflate

    override fun getFormatter(): AllRemoteStyleFormatter {
        return AllRemoteStyleFormatter(feedViewModel.getSettings())
    }

    override fun getPostViewRoadSource(): PostViewRoadSource {
        return PostViewRoadSource.Profile
    }

    override fun getAmplitudeWhereFromOpened() = if (userProfileViewModel.isMe()) {
        AmplitudePropertyWhere.SELF_FEED
    } else {
        AmplitudePropertyWhere.USER_PROFILE
    }

    override fun getAmplitudeWhereProfileFromOpened() = if (userProfileViewModel.isMe()) {
        AmplitudePropertyWhere.SELF_FEED
    } else {
        AmplitudePropertyWhere.USER_PROFILE
    }

    override fun getAmplitudeWhereMomentOpened() = if (userProfileViewModel.isMe()) {
        AmplitudePropertyMomentScreenOpenWhere.PROFILE_FEED
    } else {
        AmplitudePropertyMomentScreenOpenWhere.USER_PROFILE_FEED
    }

    override fun getAnalyticPostOriginEnum() = if (userProfileViewModel.isMe()) {
        DestinationOriginEnum.OWN_PROFILE
    } else {
        DestinationOriginEnum.OTHER_PROFILE
    }

    override fun initAuthObserver(): AuthStatusObserver = object : AuthStatusObserver(act, this) {
        override fun onAuthState() = Unit
        override fun onNotAuthState() = Unit

        /**
         * Обновляем весь экран Профайла после авторизации/регистрации
         */
        override fun onJustAuthEvent() = onRefresh()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        App.component.inject(this)
    }

    override fun onScreenshotTaken() {
        if (isSavingFeedPhoto) return
        userProfileViewModel.handleUIAction(UserProfileUIAction.GetUserDataForScreenshotPopup)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(billingClientWrapper)
    }

    /**
     * Have to set feedRecycler here to prevent potential memory leak when this fragment is destroyed
     * but initPostsAdapter function of BaseFeedFragment was not called
     *
     *
     * view.post { .. } предотвращает крэш, связанный с отрисовкой вью при переходе в профиль
     * Задача на Техдолг:
     * TODO https://nomera.atlassian.net/browse/BR-28799
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPermissionDelegate(requireActivity(), viewLifecycleOwner)
        userSnippetTopCornersRadiusPx = resources.getDimensionPixelSize(R.dimen.corner_radius_large).toFloat()
        userId = arguments?.getLong(ARG_USER_ID)
        getPostIdArgument()

        feedRecycler = binding?.rvContent
        tooltipDelegate = TooltipProfileFragmentViewController(this, binding)
        setupAvatarQuality(binding?.vAvatarView)
        binding?.appbarProfile?.setExpanded(true, false)
        binding?.srlUserProfile?.setOnRefreshListener {
            isLoadedUserRoad = false
            onRefresh()
        }
        setupToolbar()
        initContentRecycler()

        val pinModel = arguments?.getParcelable<MapUserUiModel>(ARG_USER_PIN_DATA)
        val snippetModel = arguments?.getParcelable<UserSnippetModel>(ARG_USER_SNIPPET_DATA)
        userSnippetFocused = arguments?.getBoolean(ARG_USER_SNIPPET_FOCUSED) ?: false

        when {
            snippetModel != null -> {
                view.post {
                    this.userSnippetModel = snippetModel
                    setupSnippetProfile(snippetModel)
                    setupNavigation(
                        hasNbBar = false, isPreview = true
                    )
                }
            }

            pinModel != null -> {
                setupPinProfile(pinModel)
                setupNavigation(
                    hasNbBar = false, isPreview = true
                )
            }

            else -> {
                setupFullProfile()
                setupNavigation(
                    hasNbBar = userId == null || userId == userProfileViewModel.getUserUid(), isPreview = false
                )
            }
        }

        submitUIAction(
            UserProfileUIAction.FragmentViewCreated(
                isUserSnippet = pinModel != null || snippetModel != null
            )
        )
        act.supportFragmentManager.setFragmentResultListener(
            ProfilePhotoViewerFragment.PHOTO_VIEWER_RESULT,
            viewLifecycleOwner
        ) { _: String, bundle: Bundle ->
            val argUserId = bundle.getLong(ARG_USER_ID, 0L)
            val argCurrentPosition = bundle.getInt(ProfilePhotoViewerFragment.CURRENT_POSITION, 0)
            val argChanged = bundle.getBoolean(ProfilePhotoViewerFragment.AVATARS_CHANGED, true)
            if (argUserId == userId) {
                userProfileViewModel.refreshAvatarsAndSetCurrent(argCurrentPosition, argChanged)
            }
        }
        initClickListeners()

        act.permissionListener.add(listener)
    }

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

    override fun onUserComplainAdditionalSuccess() {
        userProfileViewModel.refreshProfile()
    }

    override fun onBackPressed(): Boolean {
        return if (userSnippetModel != null && isFullUserProfileSetup && userSnippet?.getLastStableState() == SnippetState.Expanded) {
            transformToPreview()
            true
        } else {
            false
        }
    }

    //https://nomera.atlassian.net/browse/BR-25734  проверка состояния скрола, для определения необходимости покрасить статус бар на белый цвет
    fun appbarProfileIsLifted(): Boolean {
        return binding?.appbarProfile?.isLifted ?: false
    }

    fun setUserSnippetModel(model: UserSnippetModel) {
        if (userSnippetModel == null) {
            userSnippetModel = model
            setupSnippetProfile(model)
        }
    }

    @Suppress("LocalVariableName")
    private fun initFloatingButtonsVisibilityListener() {
        val _binding = binding ?: return
        FloatingButtonsManager(
            context = requireContext(),
            lifecycleOwner = viewLifecycleOwner,
        ).trackFloatingButtonsStateChanges(
            parent = _binding.vgUserActions, wrappedButtons = listOf(
                FabWrapper(_binding.fabCall, MEDIUM),
                FabWrapper(_binding.fabChat, HIGH),
            )
        )
    }

    private fun getPostIdArgument() {
        val args = arguments ?: return
        val postId = args.getLong(ARG_POST_ID)
        if (postId == 0L) return
        this.selectedPostId = postId
    }

    private fun makeColorStatusBar() {
        act.setColorStatusBarNavLight()
        act.changeStatusBarState(Act.COLOR_STATUSBAR_LIGHT_NAVBAR)
    }

    private fun makeLightStatusBar() {
        act.setLightStatusBar()
        act.changeStatusBarState(Act.LIGHT_STATUSBAR)
    }

    private fun getAuthRequester(): AuthRequester? {
        val navigator = (activity as? ActivityToolsProvider)?.getAuthenticationNavigator() ?: return null

        return object : AuthRequester {
            override fun requestAuthAndRun(complete: (Boolean) -> Unit) {
                navigator.needAuth(complete)
            }
        }
    }

    private fun setupPinProfile(user: MapUserUiModel) {
        updateBackgroundTopCorners(userSnippetTopCornersRadiusPx)
        binding?.srlUserProfile?.isEnabled = false

        themeController.setupTheme(user.accountType.value, user.accountColor, binding)
        setupUserPinContent(user)
        uniqName = user.uniqueName
        screenshotPopupData = ScreenshotPopupData(
            title = user.name ?: String.empty(),
            description = user.uniqueName?.let { "@$it" } ?: String.empty(),
            buttonTextStringRes = R.string.share_profile,
            imageLink = user.avatar,
            isVipUser = user.accountType != AccountTypeEnum.ACCOUNT_TYPE_REGULAR,
            isApprovedUser = false,
            isInterestingAuthor = false,
            profileId = user.id,
            screenshotPlace = if (userProfileViewModel.isMe()) ScreenshotPlace.OWN_PROFILE else ScreenshotPlace.USER_PROFILE
        )

        val items = user.toUserProfileUIList()
        adapterProfile?.refresh(items)
        checkSpacingForAdapter(
            isMe = false, blacklistedMe = user.blacklistedMe
        )

        binding?.msbcvUserRoot?.interceptTouchEvents = true
    }

    private fun setupSnippetProfile(snippetModel: UserSnippetModel) {
        updateBackgroundTopCorners(userSnippetTopCornersRadiusPx)
        binding?.srlUserProfile?.isEnabled = false
        themeController.setupTheme(snippetModel.accountType.value, snippetModel.accountColor, binding)
        setupUserSnippetContent(snippetModel)
        uniqName = snippetModel.uniqueName
        screenshotPopupData = ScreenshotPopupData(
            title = snippetModel.name ?: String.empty(),
            description = snippetModel.uniqueName?.let { "@$it" } ?: String.empty(),
            buttonTextStringRes = R.string.share_profile,
            additionalInfo = listOf(snippetModel.city, snippetModel.country).filter { !it.isNullOrBlank() }
                .joinToString(", "),
            imageLink = snippetModel.avatar,
            isApprovedUser = snippetModel.approved,
            isInterestingAuthor = false,
            isVipUser = snippetModel.accountType != AccountTypeEnum.ACCOUNT_TYPE_REGULAR,
            profileId = snippetModel.uid,
            screenshotPlace = if (userProfileViewModel.isMe()) ScreenshotPlace.OWN_PROFILE else ScreenshotPlace.USER_PROFILE
        )

        val items = snippetModel.toUserProfileUIList()
        adapterProfile?.refresh(items)
        checkSpacingForAdapter(
            isMe = false, blacklistedMe = snippetModel.blacklistedMe
        )

        binding?.msbcvUserRoot?.interceptTouchEvents = true
        binding?.msbcvUserRoot?.setThrottledClickListener {
            userSnippet?.setState(SnippetState.Expanded)
        }
        setupMoments(snippetModel.moments?.previews, snippetModel.moments?.hasNewMoments)
    }

    private fun setupFullProfile() {
        if (isFullUserProfileSetup) return
        isFullUserProfileSetup = true
        addPostsAdapter()
        binding?.msbcvUserRoot?.interceptTouchEvents = false
        userProfileViewModel.init(
            appVersionName = BuildConfig.VERSION_NAME,
            serverAppVersionName = act?.serverAppVersionName,
            userId = userId ?: userProfileViewModel.getUserUid() //по умолчанию берем свой id
        )
        initObservers()
        initFloatingButtonsVisibilityListener()
    }

    override fun getWhereFromHashTagPressed() = userProfileViewModel.getHashTagEventValue()

    override fun onUserSelected(userSnippetModel: UserSnippetModel) {
        userSnippetFocused = this.userId == userSnippetModel.uid
    }

    override fun onUserSnippetSlide(offset: Float) {
        if (userSnippetFocused) {
            binding?.srlUserProfile?.isEnabled = false
            val cornerSizePx = sqrt(1 - maxOf(offset, 0f)) * userSnippetTopCornersRadiusPx
            updateBackgroundTopCorners(cornerSizePx)
            binding?.ibSnippetClose?.isVisible = offset <= 0
            binding?.toolbar?.isInvisible = offset < 1f
            showHideAvatarsCarousel(user = userProfile, isPreview = offset < 1f)
        }
    }

    override fun onUserSnippetStateChanged(state: SnippetState) {
        if (userSnippetFocused) {
            binding?.msbcvUserRoot?.interceptTouchEvents = state != SnippetState.Expanded
            if (state == SnippetState.Expanded) {
                binding?.ibSnippetClose?.gone()
                binding?.toolbar?.visible()
                setupFullProfile()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupNavigation(hasNbBar: Boolean, isPreview: Boolean) {
        if (hasNbBar) {
            if (act.navigatorAdapter.getFragmentsCount() == 1) {
                binding?.nbBar?.let { onActivityInteraction?.onGetNavigationBar(it) }
            }
            if (act.getFragmentsCount() > 1) {
                binding?.nbBar?.gone()
                binding?.ivBack?.visible()
                binding?.ivBack?.setOnClickListener { act.onBackPressed() }
            } else {
                binding?.nbBar?.visible()
                binding?.ivBack?.gone()
                binding?.rvContent?.setPaddingBottom(105.dp)
            }
        } else {
            binding?.nbBar?.gone()
            if (isPreview) {
                binding?.ibCollapse?.visible()
                binding?.ivBack?.gone()
                binding?.ibCollapse?.setOnClickListener {
                    transformToPreview()
                }
                binding?.ibSnippetClose?.setOnClickListener {
                    userProfileViewModel.setUserSnippetCloseMethod(MapSnippetCloseMethod.CLOSE_BUTTON)
                    userSnippet?.setState(SnippetState.Closed)
                }
                binding?.ibSnippetClose?.visible()
                binding?.toolbar?.invisible()
            } else {
                binding?.ibSnippetClose?.gone()
                binding?.toolbar?.visible()
                binding?.ibCollapse?.gone()
                binding?.ivBack?.visible()
                binding?.ivBack?.setOnClickListener {
                    act.onBackPressed()
                }
            }
        }
    }

    private fun updateBackgroundTopCorners(cornerSizePx: Float) {
        binding?.msbcvUserRoot?.apply {
            shapeAppearanceModel = shapeAppearanceModel
                .toBuilder()
                .setTopLeftCorner(CornerFamily.ROUNDED, cornerSizePx)
                .setTopRightCorner(CornerFamily.ROUNDED, cornerSizePx)
                .setBottomLeftCornerSize(0f)
                .setBottomRightCornerSize(0f)
                .build()
        }
    }

    private fun checkSpacingForAdapter(isMe: Boolean, blacklistedMe: Boolean) {
        if (isMe.not()) {
            val collapsingContainerMarginIfStatusEmpty = COLLAPSING_CONTAINER_MARGIN_STATUS_EMPTY.dp
            val params = binding?.rvContent?.layoutParams as? CoordinatorLayout.LayoutParams
            val behavior = AppBarLayout.ScrollingViewBehavior()
            params?.behavior = behavior
            behavior.overlayTop = collapsingContainerMarginIfStatusEmpty
            binding?.rvContent?.layoutParams = params

            val paramTitle: ConstraintLayout.LayoutParams? =
                binding?.rvDescr?.layoutParams as? ConstraintLayout.LayoutParams?
            if (!blacklistedMe) {
                binding?.rvDescr?.layoutParams = paramTitle
            }
        }
    }

    private fun handleProfile(profile: UserProfileUIModel) {
        userSnippet?.updateUser(profile.toUserUpdateModel())
        val vipStatusChanged = userProfile?.accountDetails?.accountType != AccountTypeEnum.ACCOUNT_TYPE_VIP.value
            && profile.accountDetails.accountType == AccountTypeEnum.ACCOUNT_TYPE_VIP.value
        val blacklistedMeChanged = userProfile?.settingsFlags?.blacklistedMe != profile.settingsFlags.blacklistedMe
        userProfile = profile
        setupUserFullContent(profile)
        if ((profile.postsCount == 0 && isLoadedUserRoad) || vipStatusChanged || blacklistedMeChanged) {
            isLoadedUserRoad = false
            loadPosts()
        }
        uniqName = profile.uniquename
        screenshotPopupData = ScreenshotPopupData(
            title = profile.name,
            description = profile.uniquename.let { "@$it" },
            buttonTextStringRes = R.string.share_profile,
            additionalInfo = listOf(
                profile.locationDetails.cityName,
                profile.locationDetails.countryName
            ).filter { !it.isNullOrBlank() }.joinToString(", "),
            imageLink = profile.avatarDetails.avatarSmall,
            isVipUser = profile.accountDetails.accountType != AccountTypeEnum.ACCOUNT_TYPE_REGULAR.value,
            isApprovedUser = profile.accountDetails.isAccountApproved,
            isInterestingAuthor = profile.accountDetails.isTopContentMaker,
            profileId = profile.userId,
            screenshotPlace = if (userProfileViewModel.isMe()) ScreenshotPlace.OWN_PROFILE else ScreenshotPlace.USER_PROFILE
        )

        if (avatarsAdapter == null) {
            avatarsAdapter = AvatarsAdapter(Gender.fromValue(profile.gender), ::onAvatarClick)
            binding?.vpAvatars?.adapter = avatarsAdapter

            val snapHelper = PagerSnapHelper()
            snapHelper.attachToRecyclerView(binding?.vpAvatars)

            binding?.rvAvatarCounter?.setMargins(top = context.getStatusBarHeight() + COUNTER_TOP_MARGIN.dp)
            binding?.rvAvatarCounter?.setupViewPager(binding?.vpAvatars)
            binding?.vpAvatars?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    val llm = recyclerView.layoutManager as? LinearLayoutManager
                    val position = llm?.findFirstVisibleItemPosition() ?: 0
                    if ((binding?.vpAvatars?.adapter?.itemCount ?: 0) < position + PRELOAD_DELTA) {
                        submitUIAction(UserProfileUIAction.LoadMoreAvatars)
                    }
                }
            })
        }

        showHideAvatarsCarousel(user = profile, isPreview = false)
        setupMoments(userProfile?.moments?.previews, userProfile?.moments?.hasNewMoments)
    }

    private fun showHideAvatarsCarousel(user: UserProfileUIModel?, isPreview: Boolean) {

        if (user?.accountDetails?.isAccountDeleted == true) return

        val notBlacklistedMe: Boolean = user?.blacklistedMe?.not() ?: false
        val notBlacklistedByMe: Boolean = user?.blacklistedByMe?.not() ?: false
        val isClosedProfile =
            (user?.isClosedProfile ?: false)
                && user?.settingsFlags?.friendStatus != FriendStatus.FRIEND_STATUS_CONFIRMED.intStatus

        if (userProfileViewModel.isAvatarCarouselEnabled() && notBlacklistedMe
            && notBlacklistedByMe && isPreview.not() && isClosedProfile.not()
        ) {
            binding?.ivAvatar?.invisible()
            binding?.vAvatarView?.invisible()
            binding?.vpAvatars?.scrollEnabled = true
            binding?.vpAvatars?.visible()
            binding?.rvAvatarCounter?.visible()
        } else {
            binding?.vpAvatars?.scrollEnabled = false
            binding?.vpAvatars?.visible()
            binding?.rvAvatarCounter?.invisible()
        }
    }

    // если список пуст, не нужно ничего отображать и грузить дорогу
    // скорее всего профиль удален
    private fun handleProfileUIList(profileUIList: List<UserUIEntity>) {
        if (profileUIList.isEmpty()) return
        showingSuggestions = profileUIList.any { it is ProfileSuggestionsFloorUiEntity }

        adapterProfile?.refresh(profileUIList) {
            loadPosts()
            checkSpacingForAdapter(
                isMe = userProfileViewModel.isMe(), blacklistedMe = userProfile?.settingsFlags?.blacklistedMe ?: false
            )
            scrollFeedUpIfExpanded()
        }
    }

    private fun setupMoments(momentsPreviews: List<UserMomentsPreviewModel>?, hasNewMoments: Boolean?) {
        if ((activity?.application as? FeatureTogglesContainer)?.momentsFeatureToggle?.isEnabled == false) return
        if (momentsPreviews == null) return
        binding?.mmpMomentsPreview?.visible()
        val momentsPreview = momentsPreviews.map {
            MomentPreviewItem(
                url = it.url, viewed = it.viewed.toBoolean()
            )
        }
        val momentsModel = MomentsMiniPreviewModel(
            isMe = userProfileViewModel.isMe(),
            momentsPreviews = momentsPreview,
        )
        binding?.mmpMomentsPreview?.setMomentsPreview(momentsModel)
        binding?.mmpMomentsPreview?.setOnClickListener {
            val existUserId = userId ?: return@setOnClickListener
            if ((activity?.application as? FeatureTogglesContainer)?.momentsFeatureToggle?.isEnabled == false) {
                return@setOnClickListener
            }

            tooltipDelegate?.canShow = false

            userProfileViewModel.handleUIAction(
                UserProfileUIAction.OnMomentClicked(
                    openedFrom = MomentClickOrigin.fromUserProfile(),
                    existUserId = existUserId,
                    hasNewMoments = hasNewMoments
                )
            )
        }
        binding?.mmpMomentsPreview?.setOnCreateClickListener {
            act.getMomentsViewController().open()
        }
    }

    private fun showScreenshotPopup(userLink: String) {
        if (isScreenshotPopupShown) return
        isScreenshotPopupShown = true
        screenshotPopupData = screenshotPopupData?.apply { link = "$userLink$uniqName" }
        screenshotPopupData?.let { ScreenshotPopupController.show(this, it) }
    }

    private fun initObservers() {

        lifecycleScope.launchWhenStarted {
            userProfileViewModel.state.filterNotNull().collect { state ->
                handleProfile(state.profile)
                handleProfileUIList(state.profileUIList)
                logProfileEntrance(state)
            }
        }
        observeViewEvent()
    }

    private fun observeViewEvent() {
        lifecycleScope.launchWhenStarted {
            userProfileViewModel.effect.collect(::handleViewEventsUserInfo)
        }
        feedViewModel.userFeedProfileViewEvent
            .flowWithLifecycle(lifecycle)
            .onEach(::handleFeedProfileEvent)
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun handleCommunityChanges(event: UserInfoViewEffect.CommunityChanges) {
        if (event.communityListEvents is CommunityListEvents.DeleteSuccess) onRefresh()
    }

    private fun handleFeedProfileEvent(event: UserFeedViewEvent) {
        when (event) {
            is UserFeedViewEvent.ScrollToPostPosition -> {
                val scrollPoint = getScrollPoint(event.selectedPostPosition)
                binding?.rvContent?.scrollToPosition(scrollPoint)
                binding?.appbarProfile?.setExpanded(false, true)
                // TODO: https://nomera.atlassian.net/browse/BR-17752 временное решение
                //  Т.к при вызове scrollToPosition() видео может иногда не воспроизводиться.
                doDelayed(event.scrollDelay) {
                    binding?.rvContent?.playVideo(false)
                }
            }

            is UserFeedViewEvent.ScrollToFirstPostPositionUiEffect -> {
                handleScrollToFirstPostPositionEffect(event.delayPlayVideo)
            }
        }
    }

    private fun getScrollPoint(postPosition: Int) = userProfileViewModel.getUserInfoListSize() + postPosition

    private fun handleScrollToFirstPostPositionEffect(delay: Long) {
        val lm = (binding?.rvContent?.layoutManager as? LinearLayoutManager) ?: return
        val scrollPoint = getScrollPoint(FIRST_POST)
        lm.scrollToPositionWithOffset(
            scrollPoint, 0
        )
        binding?.appbarProfile?.setExpanded(false, true)
        doDelayed(delay) {
            binding?.rvContent?.playVideo(false)
        }
    }

    private fun showBirthdayDialog(isTodayBirthday: Boolean) {
        val actionType = if (isTodayBirthday) BirthdayBottomDialogFragment.ACTION_TODAY_IS_BIRTHDAY
        else BirthdayBottomDialogFragment.ACTION_YESTERDAY_IS_BIRTHDAY
        act.showBirthdayDialog(
            actionType = actionType,
            dismissListener = {
                userProfileViewModel.setBirthdayDialogShown()
            }
        )
    }

    private fun showError(msg: String?) =
        NToast.with(view)
            .typeError()
            .text(msg)
            .show()


    private fun showSuccessMessage(msg: String?) =
        NToast.with(view)
            .typeSuccess()
            .text(msg)
            .show()

    private fun showSuccessMessage(message: String, isSuccess: Boolean) {
        if (isSuccess) showSuccessMessage(message)
        else showError(message)
    }


    private fun scrollFeedUpIfExpanded() {
        if (!toolbarController.isCollapsedToolbar()) {
            binding?.rvContent?.scrollToPosition(0)
        }
    }

    private fun observeAvatarChangeListener() {
        (act?.supportFragmentManager as? FragmentManager)?.setFragmentResultListener(
            REQUEST_NMR_KEY_AVATAR, viewLifecycleOwner
        ) { _, bundle ->
            val avatarState: String = bundle.getString(NMR_AVATAR_STATE_JSON_KEY) ?: return@setFragmentResultListener
            submitUIAction(UserProfileUIAction.OnLiveAvatarChanged(avatarState))
        }
    }

    private fun removeAvatarObserveChangeListener() {
        (act?.supportFragmentManager as? FragmentManager)?.clearFragmentResultListener(
            REQUEST_NMR_KEY_AVATAR
        )
    }

    private fun allowSwipeAndGoBack() {
        act?.navigatorViewPager?.setCurrentItem(act.navigatorViewPager.currentItem - 1, true)
    }

    private fun loadPosts() {
        if (!isLoadedUserRoad || forceLoadRoad) {
            startLoadPosts()
            isLoadedUserRoad = true
            forceLoadRoad = false
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupUserFullContent(user: UserProfileUIModel) {
        themeController.setupTheme(user.accountDetails.accountType, user.accountDetails.accountColor, binding)
        val params = ProfileToolbarModelUIModel(
            topContentParams = TopAuthorApprovedUserModel(
                approved = user.accountDetails.isAccountApproved,
                interestingAuthor = user.accountDetails.isTopContentMaker,
                isVip = createAccountTypeEnum(user.accountDetails.accountType) != AccountTypeEnum.ACCOUNT_TYPE_REGULAR,
                approvedIconSize = ApprovedIconSize.LARGE,
                customIconTopContent = R.drawable.ic_approved_author_gold_18
            ),
            isMe = userProfileViewModel.isMe(),
            profileDeleted = user.accountDetails.isAccountDeleted,
            blacklistedMe = user.settingsFlags.blacklistedMe,
            accountType = createAccountTypeEnum(user.accountDetails.accountType),
            name = user.name
        )
        toolbarController.setupCollapsingNickname(params, binding)
        val gender = if (!user.settingsFlags.hideGender) {
            user.gender.let(Gender::fromValue)
        } else {
            null
        }
        setupUserAvatar(
            avatarUrl = user.avatarDetails.avatarBig,
            avatarAnimation = user.avatarDetails.avatarAnimation,
            gender = gender
        )
        setupGender(gender)
        val birthdayTimestamp = if (!user.settingsFlags.hideBirthday) {
            user.birthday
        } else {
            null
        }
        setupBirthDayGeo(
            birthdayTimestamp = birthdayTimestamp,
            cityName = user.locationDetails.cityName,
            countryName = user.locationDetails.countryName
        )
        binding?.uniqueNameTextView?.text = "@${user.uniquename}"
        binding?.tvSubscribersCountProfile?.text = user.subscribersCount.asCountString()
        setupClickListeners(user)
        updateViewVisibility(user)
        setupSubscriptionNotificationBell(user)
        userProfileViewModel.getMapUserState(user.userId)
        userProfileViewModel.cacheCompanionUser(user.toChatInitUserProfile())
    }


    @SuppressLint("SetTextI18n")
    private fun setupUserPinContent(user: MapUserUiModel) {
        themeController.setupTheme(user.accountType.value, user.accountColor, binding)
        val params = ProfileToolbarModelUIModel(
            topContentParams = TopAuthorApprovedUserModel(
                approved = false,
                interestingAuthor = false,
                isVip = user.accountType != AccountTypeEnum.ACCOUNT_TYPE_REGULAR,
                approvedIconSize = ApprovedIconSize.LARGE,
                customIconTopContent = R.drawable.ic_approved_author_gold_18
            ),
            isMe = false,
            profileDeleted = false,
            blacklistedMe = user.blacklistedMe,
            accountType = user.accountType,
            name = user.name
        )
        toolbarController.setupCollapsingNickname(params, binding)
        setupUserAvatar(
            avatarUrl = user.avatar,
            avatarAnimation = null,
            gender = user.gender
        )
        setupGender(null)
        setupBirthDayGeo(
            birthdayTimestamp = null,
            cityName = null,
            countryName = null
        )
        binding?.uniqueNameTextView?.text = "@${user.uniqueName}"
    }

    @SuppressLint("SetTextI18n")
    private fun setupUserSnippetContent(user: UserSnippetModel) {
        themeController.setupTheme(user.accountType.value, user.accountColor, binding)
        val params = ProfileToolbarModelUIModel(
            topContentParams = TopAuthorApprovedUserModel(
                approved = user.approved,
                interestingAuthor = false,
                isVip = user.accountType != AccountTypeEnum.ACCOUNT_TYPE_REGULAR,
                approvedIconSize = ApprovedIconSize.LARGE,
                customIconTopContent = R.drawable.ic_approved_author_gold_18
            ),
            isMe = false,
            profileDeleted = user.profileDeleted,
            blacklistedMe = user.blacklistedMe,
            accountType = user.accountType,
            name = user.name,
        )
        toolbarController.setupCollapsingNickname(params, binding)
        setupUserAvatar(
            avatarUrl = user.avatarBig,
            avatarAnimation = null,
            gender = user.gender
        )
        setupGender(user.gender)
        setupBirthDayGeo(
            birthdayTimestamp = user.birthday?.time,
            cityName = user.city,
            countryName = user.country
        )
        binding?.uniqueNameTextView?.text = "@${user.uniqueName}"
    }

    private fun updateViewVisibility(user: UserProfileUIModel) {
        if (!user.settingsFlags.blacklistedMe) {
            setupNormalMarginCollapsing()
        }
        if (userProfileViewModel.isMe()) {
            binding?.ivPhoto?.visible()
            binding?.vgUserActions?.gone()
        } else {
            binding?.ivPhoto?.gone()
            // Bottom buttons
            binding?.vgUserActions?.visible()
            if (user.settingsFlags.blacklistedMe) handleUserBlockedMe()
            else handleUserIsNotBlockedMe()


            if (user.accountDetails.isAccountDeleted) {
                handleProfileDeleted()
            } else if (!user.settingsFlags.blacklistedMe) {
                handleProfileIsNotDeleted()
            }

            if (chatIsUnavailable(user)) {
                binding?.fabChat?.gone()
            } else {
                binding?.fabChat?.visible()
            }
            if (friendStatusIsConfirmed(user).not() && callIsUnavailable(user)) {
                binding?.fabCall?.gone()
            } else {
                binding?.fabCall?.visible()
            }
        }
    }

    private fun friendStatusIsConfirmed(user: UserProfileUIModel): Boolean {
        return user.friendStatus == FriendStatus.FRIEND_STATUS_CONFIRMED
    }

    private fun callIsUnavailable(user: UserProfileUIModel): Boolean {
        return !user.settingsFlags.iCanCall || user.accountDetails.isAccountBlocked
    }

    private fun chatIsUnavailable(user: UserProfileUIModel): Boolean {
        return !user.settingsFlags.iCanChat || !user.settingsFlags.userCanChatMe || user.accountDetails.isAccountBlocked
    }

    private fun handleProfileIsNotDeleted() {
        binding?.llBlockedUser?.gone()
        binding?.ivPlaceholder?.visible()
        binding?.rvDescr?.visible()
        binding?.rvContent?.visible()
        binding?.srlUserProfile?.isEnabled = true
        isDeletedProfile = false
    }

    private fun handleProfileDeleted() {
        binding?.vgUserActions?.gone()
        binding?.llBlockedUser?.visible()
        binding?.blockedByUserLabel?.visible()
        binding?.blockedByUserLabel?.text = getString(R.string.user_deleted_txt)
        binding?.ivNotificationBell?.gone()
        binding?.ivPlaceholder?.gone()
        binding?.rvDescr?.gone()
        binding?.tvSubscribersCountProfile?.gone()
        binding?.rvContent?.gone()
        binding?.profileGradientBottom?.setBackgroundResource(R.drawable.avatar_gradient_bottom)
        isDeletedProfile = true
    }

    private fun handleUserBlockedMe() {
        binding?.vgUserActions?.gone()
        binding?.llBlockedUser?.gone()
        binding?.containerUserDescription?.gone()
        setBlockedMeMarginsCollapsing()
    }

    private fun handleUserIsNotBlockedMe() {
        binding?.vgUserActions?.visible()
        binding?.llBlockedUser?.gone()
        binding?.containerUserDescription?.visible()
    }

    private fun setBlockedMeMarginsCollapsing() {
        binding?.rvDescr?.setMargins(bottom = 8.dp)
        binding?.tvSubscribersCountProfile?.setMargins(bottom = 16.dp)
    }


    private fun submitUIAction(action: UserProfileUIAction) {
        userProfileViewModel.handleUIAction(action)
    }

    private fun openAvatarCreator() {
        observeAvatarChangeListener()
        // Такой костыль сделано для того что бы передать пол пользователя, к либе не возможно передать сам пол, ты можешь передать только состояния аватара.
        val avatarState = if (userProfile?.avatarDetails?.avatarAnimation.isNullOrEmpty()) {
            if (userProfile?.gender == IS_MALE) AnimatedAvatarUtils.DEFAULT_MALE_STATE else AnimatedAvatarUtils.DEFAULT_FEMALE_STATE
        } else {
            userProfile?.avatarDetails?.avatarAnimation
        }
        Timber.i("UserInfo AvatarState:${avatarState}")
        userProfileViewModel.logAvatarOpen()
        userProfileViewModel.noteTimeWhenAvatarOpened()
        navigator.openContainerAvatarFragment(avatarState)
    }

    // OnSuccessShareProfile - тут Delay используется для того, что-бы показывать сообщение
    // после того, как закроется меню и клавиатура
    private fun showShareProfileDialog(user: UserProfileUIModel, profileLink: String) {
        SharePostBottomSheet(ShareDialogType.ShareProfile(user.userId)) { shareEvent ->
            when (shareEvent) {
                is ShareBottomSheetEvent.OnSuccessShareProfile -> {
                    doDelayed(DELAY_REQUEST.toLong()) {
                        showInfoTooltip(R.string.share_profile_success, user.accountDetails.isAccountDeleted)
                    }
                    userProfileViewModel.logShareProfileInside(userId)
                }

                is ShareBottomSheetEvent.OnErrorShareProfile -> {
                    showError(getString(R.string.share_profile_error))
                }

                is ShareBottomSheetEvent.OnMoreShareButtonClick -> {
                    shareProfileOutside(context, profileLink, user.uniquename)
                    userProfileViewModel.logShareProfileOutside(userId)
                }

                is ShareBottomSheetEvent.OnErrorUnselectedUser -> {
                    showError(getString(R.string.no_user_selected))
                }

                is ShareBottomSheetEvent.OnClickFindFriendButton -> {
                    navigator.openSearchFragment()
                }

                else -> {}
            }
        }.show(childFragmentManager)
    }

    private fun meeraShowShareProfileDialog(user: UserProfileUIModel, profileLink: String) {
        MeeraShareSheet().showByType(
            fm = childFragmentManager,
            shareType = ShareDialogType.ShareProfile(user.userId),
            event = { shareEvent ->
                when (shareEvent) {
                    is ShareBottomSheetEvent.OnSuccessShareProfile -> {
                        doDelayed(DELAY_REQUEST.toLong()) {
                            showInfoTooltip(R.string.share_profile_success, user.accountDetails.isAccountDeleted)
                        }
                        userProfileViewModel.logShareProfileInside(userId)
                    }

                    is ShareBottomSheetEvent.OnErrorShareProfile -> {
                        showError(getString(R.string.share_profile_error))
                    }

                    is ShareBottomSheetEvent.OnMoreShareButtonClick -> {
                        shareProfileOutside(context, profileLink, user.uniquename)
                        userProfileViewModel.logShareProfileOutside(userId)
                    }

                    is ShareBottomSheetEvent.OnErrorUnselectedUser -> {
                        showError(getString(R.string.no_user_selected))
                    }

                    is ShareBottomSheetEvent.OnClickFindFriendButton -> {
                        navigator.openSearchFragment()
                    }

                    else -> Unit
                }
            }
        )

    }

    private fun showInfoTooltip(@StringRes text: Int, showOnBottom: Boolean = false) {
        infoTooltip = NSnackbar.with(view)
            .typeSuccess()
            .inView(view)
            .marginBottom(
                if (showOnBottom) INFO_TOOLTIP_USER_REMOVED_MARGIN_BOTTOM
                else INFO_TOOLTIP_MARGIN_BOTTOM
            )
            .text(getString(text))
            .durationLong()
            .show()
    }

    /**
     * Configure notification if user is not a friend and is not subscribed on me hide bell
     * */
    private fun setupSubscriptionNotificationBell(user: UserProfileUIModel) {
        if (userProfileViewModel.isMe() || !user.settingsFlags.isSubscriptionOn) {
            binding?.ivNotificationBell?.gone()
            return
        }
        val isBellEnabled = user.settingsFlags.isSubscriptionNotificationEnabled
        val bellIcon = if (isBellEnabled) R.drawable.ic_profile_notification_on
        else R.drawable.ic_profile_notification_off
        binding?.ivNotificationBell?.setImageDrawable(getDrawable(act, bellIcon))
        onClickSubscriptionNotification(!isBellEnabled)
        if (
            (user.friendStatus == FriendStatus.FRIEND_STATUS_CONFIRMED || user.settingsFlags.isSubscriptionOn)
            && !user.accountDetails.isAccountDeleted
        ) {
            binding?.ivNotificationBell?.visible()
        } else {
            binding?.ivNotificationBell?.gone()
        }

    }

    private fun onClickSubscriptionNotification(isEnable: Boolean) {
        binding?.ivNotificationBell?.click {
            submitUIAction(UserProfileUIAction.ClickSubscribeNotification(isEnable))
        }
    }

    private fun setupNormalMarginCollapsing() {
        binding?.rvDescr?.setMargins(bottom = COLLAPSING_DESC_DEFAULT_MARGIN.dp)
        binding?.tvSubscribersCountProfile?.setMargins(bottom = COLLAPSING_DESC_DEFAULT_MARGIN.dp)
    }

    private fun onAvatarClick(position: Int) {
        needAuth {
            userProfile?.let {
                showUserAvatar(it, position = position)
            }
        }
    }

    private fun setupClickListeners(user: UserProfileUIModel) {
        binding?.uniqueNameTextView?.click {
            copyUniqueNameToClipboard()
            submitUIAction(UserProfileUIAction.UniqueNameClick)
        }

        binding?.tvSubscribersCountProfile?.click {
            submitUIAction(UserProfileUIAction.SubscribersCountClick)
        }

        binding?.ivAvatar?.click {
            needAuth {
                showUserAvatar(user)
            }
        }

        binding?.vAvatarView?.click {
            needAuth {
                showUserAvatar(user, true)
            }
        }
        binding?.ivPhoto?.click {
            val llm = binding?.vpAvatars?.layoutManager as? LinearLayoutManager
            val position = llm?.findFirstVisibleItemPosition() ?: 0
            profileDialogController.showPhotoSourceMenu(context, childFragmentManager, userProfile, position)
        }

        binding?.fabChat?.click {
            it.clickAnimate()
            needAuth {
                submitUIAction(UserProfileUIAction.StartChatClick)
            }
        }

        binding?.fabCall?.click {
            needAuth {
                submitUIAction(UserProfileUIAction.OnTryToCall(userId ?: 0))
            }
        }

        binding?.ivDots?.click {
            userProfileViewModel.setHolidayShow(false)
            needAuth { submitUIAction(UserProfileUIAction.ShowDotsMenuAction) }
        }
    }

    override fun onOpenChatClicked() {
        binding?.fabChat?.callOnClick()
    }

    private fun startCall(companion: UserChat) {
        setPermissions(
            object : PermissionDelegate.Listener {
                override fun onGranted() {
                    companion.let {
                        (activity as Act).placeWebRtcCall(it, false, null, null, null)
                    }
                }

                override fun onDenied() {
                    NToast.with(act)
                        .text(getString(R.string.you_must_grant_permissions))
                        .typeAlert()
                        .durationLong()
                        .button(getString(R.string.allow)) {
                            startCall(companion)
                        }
                        .show()
                }

                override fun onError(error: Throwable?) {
                    showError(getString(R.string.access_is_denied))
                }
            },
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    }

    private fun openCameraToChangeAvatar() {
        checkMediaPermissions(
            object : PermissionDelegate.Listener {

                override fun onGranted() {
                    openCameraToChangeAvatarWithPermissionState(PermissionState.GRANTED)
                }

                override fun onDenied() {
                    openCameraToChangeAvatarWithPermissionState(PermissionState.NOT_GRANTED_CAN_BE_REQUESTED)
                }

                override fun needOpenSettings() {
                    openCameraToChangeAvatarWithPermissionState(PermissionState.NOT_GRANTED_OPEN_SETTINGS)
                }
            }
        )
    }

    private fun openCameraToChangeAvatarWithPermissionState(permissionState: PermissionState) {
        mediaPicker = loadSingleImageUri(
            activity = act,
            viewLifecycleOwner = viewLifecycleOwner,
            type = MediaControllerOpenPlace.Avatar,
            cameraType = MediaViewerCameraTypeEnum.CAMERA_ORIENTATION_FRONT,
            suggestionsMenu = SuggestionsMenu(act.getCurrentFragment(), SuggestionsMenuType.ROAD),
            permissionState = permissionState,
            tedBottomSheetPermissionActionsListener = this,
            loadImagesCommonCallback = CoreTedBottomPickerActDependencyProvider(
                act = act,
                onReadyImageUri = { imagePath ->
                    if (userProfileViewModel.getMediaType(imagePath) == MEDIA_TYPE_IMAGE_GIF) {
                        val path = imagePath.path ?: return@CoreTedBottomPickerActDependencyProvider
                        checkAvatarPostSettings(path)
                    } else {
                        userProfileViewModel.handleUIAction(UserProfileUIAction.OnEditorOpen)
                        act.getMediaControllerFeature().open(
                            uri = imagePath,
                            openPlace = MediaControllerOpenPlace.Avatar,
                            callback = object : MediaControllerCallback {
                                override fun onPhotoReady(
                                    resultUri: Uri,
                                    nmrAmplitude: NMRPhotoAmplitude?
                                ) {
                                    val path = resultUri.path ?: return
                                    checkAvatarPostSettings(path)
                                    userProfileViewModel.handleUIAction(
                                        UserProfileUIAction.EditAvatar(nmrAmplitude)
                                    )
                                }

                                override fun onError() {
                                    showError(getString(R.string.error_editing_media))
                                }
                            }
                        )
                    }
                }
            ),
            cameraLensFacing = CameraCharacteristics.LENS_FACING_FRONT
        )
    }

    private fun showPublishPostAlert(imagePath: String, animation: String? = null) {
        PostAvatarBottomSheetFragment.getInstance(photoPath = imagePath, animation = animation)
            .show(childFragmentManager)
    }

    private fun showCallIsRestrictedAlert(userName: String?, canChat: Boolean) {
        CallIsRestrictedBottomSheetFragment.getInstance(userName = userName, canChat = canChat)
            .show(childFragmentManager)
    }

    private fun checkAvatarPostSettings(imagePath: String, animation: String? = null) {
        userProfileViewModel.requestCreateAvatarPostSettings(imagePath = imagePath, animation = animation)
    }

    override fun onPublishOptionsSelected(
        imagePath: String,
        animation: String?,
        createAvatarPost: Int,
        saveSettings: Int,
        amplitudeActionType: AmplitudeAlertPostWithNewAvatarValuesActionType
    ) {
        setupUserAvatar(avatarUrl = imagePath, avatarAnimation = animation, gender = userSnippetModel?.gender)
        submitUIAction(
            UserProfileUIAction.OnPublishOptionsSelected(
                imagePath = imagePath,
                animation = animation,
                createAvatarPost = createAvatarPost,
                saveSettings = saveSettings,
                amplitudeActionType = amplitudeActionType
            )
        )

        val imageUriPath = File(imagePath).toUri().toString()
        (activity as? ActivityToolsProvider)?.getStatusToastViewController()
            ?.showProgress(message = getString(R.string.user_personal_info_setting_main_photo), imageUrl = imageUriPath)
    }

    private fun showUserAvatar(user: UserProfileUIModel, isAnimatedAvatar: Boolean = false, position: Int? = null) {
        if (user.accountDetails.isAccountDeleted || user.settingsFlags.blacklistedMe || user.settingsFlags.blacklistedByMe) return

        val image = user.avatarDetails.avatarBig
        val smallImage = user.avatarDetails.avatarSmall
        if (smallImage.isNotEmpty() || position != null) {
            userId?.let {
                val isMe = it == userProfileViewModel.getUserUid()
                navigator.openPhotoViewer(
                    image = image,
                    isProfilePhoto = true,
                    isOwnProfile = isMe,
                    isAnimatedAvatar = isAnimatedAvatar,
                    animatedAvatar = user.avatarDetails.avatarAnimation,
                    userName = if (isMe) user.uniquename else null,
                    userId = it,
                    position = position ?: 0
                )
            }
        }
    }

    private fun copyUniqueNameToClipboard() {
        val clipboardManager =
            context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        binding?.uniqueNameTextView?.text?.let { uniqueName: CharSequence ->
            val forCopy = ClipData.newPlainText(
                getString(R.string.unique_user_name_clip_data_label),
                uniqueName
            )
            clipboardManager?.setPrimaryClip(forCopy)
        }
    }

    private fun setupBirthDayGeo(birthdayTimestamp: Long?, cityName: String?, countryName: String?) {
        val age = if (userProfileViewModel.isHiddenAgeAndGender()) null else birthdayTimestamp?.let(::getAge)
        val textDescription = listOf(
            age,
            cityName,
            countryName
        )
            .filter { !it.isNullOrBlank() }
            .joinToString(", ")
        binding?.tvDescription?.text = textDescription
        binding?.tvDescription?.isInvisible = textDescription.isBlank()
    }

    private fun setupGender(gender: Gender?) {
        val setupGender = if (userProfileViewModel.isHiddenAgeAndGender()) null else gender
        toolbarController.setupGender(setupGender, binding)
    }

    private fun setupUserAvatar(avatarUrl: String?, avatarAnimation: String?, gender: Gender?) {
        toolbarController.setupUserAvatar(avatarUrl, avatarAnimation, gender, binding, viewLifecycleOwner)
    }

    override fun onScroll(isScrolling: Boolean) {
        binding?.srlUserProfile?.isEnabled = !isScrolling
    }

    private fun initContentRecycler() {
        lateinit var locationDelegate: LocationDelegate
        val permissionListener = object : PermissionDelegate.Listener {
            override fun onGranted() {
                if (!locationDelegate.isLocationEnabled()) {
                    locationDelegate.requestEnableLocation()
                } else {
                    onRefresh()
                }
                userProfileViewModel.identifyUserProperty(true)
            }

            override fun onDenied() {
                userProfileViewModel.identifyUserProperty(false)
                val deniedAndNoRationaleNeededAfterRequest =
                    !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
                if (deniedAndNoRationaleNeededBeforeRequest && deniedAndNoRationaleNeededAfterRequest) {
                    sendUserToAppSettings()
                }
            }

            override fun onError(error: Throwable?) = Unit
        }
        locationDelegate =
            object : LocationDelegate(act, permissionDelegate, permissionListener) {
                override fun requestLocationPermissions() {
                    deniedAndNoRationaleNeededBeforeRequest = !isPermissionGranted()
                        && !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
                    super.requestLocationPermissions()
                }
            }
        adapterProfile = UserProfileAdapter(locationDelegate, this, ::submitUIAction)
        binding?.rvContent?.apply {
            itemAnimator = null
            layoutManager = LinearLayoutManager(context)
            adapter = adapterProfile
            addOnScrollListener(
                object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        adapterProfile?.callIOutStatusFloor()?.changedFocus()
                    }
                }
            )
        }
        val concatAdapter = ConcatAdapter(adapterProfile)
        binding?.rvContent?.adapter = concatAdapter
    }

    private fun addPostsAdapter() {
        initPostsAdapter(
            roadType = NetworkRoadType.USER(
                userId,
                REQUEST_ROAD_TYPE_USER,
                userId == userProfileViewModel.getUserUid(),
                selectedPostId = selectedPostId
            ),
            recyclerView = binding?.rvContent,
            lottieAnimationView = binding?.btnScrollRefreshUsersRoad
        )
        getAdapterPosts()?.let {
            (binding?.rvContent?.adapter as? ConcatAdapter)?.addAdapter(it)
        }

        initPostsLiveObservable()
    }

    override fun onClickScrollUpButton() {
        lifecycleScope.launch(Dispatchers.Main) {
            binding?.appbarProfile?.setExpanded(true, true)
            delay(300)
            binding?.rvContent?.scrollToPosition(0)
            this@UserInfoFragment.isLoadedUserRoad = false
            onRefresh()
        }
    }

    override fun onNewPost() {
        this.isLoadedUserRoad = false
        forceLoadRoad = true
        onRefresh()
    }

    override fun onStart() {
        super.onStart()
        binding?.nbBar?.selectProfile(true)
    }

    private var needToRefreshWhenOnStartCalled = false
    override fun onStartFragment() {
        super.onStartFragment()
        if (needToRefreshWhenOnStartCalled) userProfileViewModel.refreshProfile()
        else needToRefreshWhenOnStartCalled = true
        userProfileViewModel.logScreenForFragment()
        if (isAnimatedAvatar() && !toolbarController.isCollapsedToolbar()) binding?.vAvatarView?.startParallaxEffect()
        userProfileViewModel.checkForStatisticsSlides()
        submitUIAction(UserProfileUIAction.OnFragmentStart)
    }

    override fun onStopFragment() {
        super.onStopFragment()
        binding?.vAvatarView?.stopParallaxEffect()
        infoTooltip?.dismiss()
    }

    fun scrollAvatarsToStart() {
        binding?.rvAvatarCounter?.selectPosition(0)
        binding?.vpAvatars?.scrollToPosition(0)
    }

    override fun onHideHints() {
        super.onHideHints()
        tooltipDelegate?.hideHints()
    }

    private fun setupToolbar() {
        val layoutParams = binding?.toolbar?.layoutParams as CollapsingToolbarLayout.LayoutParams
        layoutParams.height = layoutParams.height + context.getStatusBarHeight()
        binding?.toolbar?.layoutParams = layoutParams
        binding?.appbarProfile?.addOnOffsetChangedListener(this)
        binding?.tvSubscribersCountProfile?.gone()
    }

    private fun handleAnimatedAvatarPath(path: String) {
        NGraphics.saveImageToDeviceFromAppDirectory(act, path) { savedImageUri ->
            userProfileViewModel.deleteFile(path)
            if (savedImageUri != null) {
                showSuccessMessage(getString(R.string.image_saved))
            } else {
                showError(getString(R.string.avatar_save_file_fail))
            }
        }
    }

    private fun openUserStatistics() {
        if (childFragmentManager.findFragmentByTag(TAG_PROFILE_STATISTICS_FRAGMENT) == null) {
            val fragment = ProfileStatisticsContainerBottomSheetFragment()
            fragment.show(childFragmentManager, TAG_PROFILE_STATISTICS_FRAGMENT)
        }
    }

    private fun handleViewEventsUserInfo(event: UserInfoViewEffect) {
        when (event) {
            is UserInfoViewEffect.GoToMarket -> act.sendToMarket()
            is UserInfoViewEffect.OpenAddPhoto -> addPhoto()
            is UserInfoViewEffect.AuthAndOpenFriendsList -> openUserListFriendsOrShowAuth(
                event.userId,
                null,
//                event.actionType,
                event.name
            )

            is UserProfileDialogNavigation.ShowScreenshotPopup -> {
                showScreenshotPopup(event.userLink)
            }

            is UserProfileDialogNavigation -> profileDialogController
                .handleEvent(event, childFragmentManager, context, userSnippetModel != null)

            is UserInfoViewEffect.ShowToastEvent -> event.message?.let {
                showSuccessMessage(
                    event.message,
                    event.isSuccess
                )
            }

            is UserProfileNavigation.GoToAllGroupTab -> act.deeplinkActionMyCommunity()
            is UserProfileNavigation.ShowInternetError -> {
                showError(getString(R.string.no_internet_connection))
            }

            is UserProfileNavigation -> navigator.handleNavigationEvent(event)
            is UserInfoViewEffect.UploadImageInfo -> handleUpload(event.nonNullWork)
            is UserInfoViewEffect.ShowProfileStatistics -> openUserStatistics()
            is UserProfileTooltipEffect -> tooltipDelegate?.handleTooltipEvent(event)
            is UserInfoViewEffect.OpenAvatarCreator -> openAvatarCreator()
            is UserInfoViewEffect.OpenCameraToChangeAvatar -> openCameraToChangeAvatar()
            is UserInfoViewEffect.SaveImageWithPermission -> saveImage(event.image)
            is UserInfoViewEffect.ShowSuccessCopyProfile -> showInfoTooltip(
                R.string.copy_link_success,
                event.isProfileDeleted
            )

            is UserInfoViewEffect.CallToUser -> callToUser(event)
            is UserInfoViewEffect.AllowSwipeAndGoBack -> allowSwipeAndGoBack()
            is UserInfoViewEffect.ShowShareProfileDialog -> {
                checkAppRedesigned(
                    isRedesigned = {
                        meeraShowShareProfileDialog(event.profile, event.profileLink)
                    },
                    isNotRedesigned = {
                        showShareProfileDialog(event.profile, event.profileLink)
                    }
                )
            }

            is UserInfoViewEffect.ShowBirthdayDialog -> showBirthdayDialog(event.isBirthdayToday)
            is UserInfoViewEffect.CommunityChanges -> handleCommunityChanges(event)
            is UserInfoViewEffect.OnAnimatedAvatarSaved -> handleAnimatedAvatarPath(event.path)

            is UserInfoViewEffect.OnCreateAvatarPostSettings -> {
                if (event.privacySettingModel?.value == CreateAvatarPostEnum.PRIVATE_ROAD.state
                    || event.privacySettingModel?.value == CreateAvatarPostEnum.MAIN_ROAD.state
                ) {
                    onPublishOptionsSelected(
                        imagePath = event.imagePath,
                        animation = event.animation,
                        createAvatarPost = event.privacySettingModel.value,
                        saveSettings = 1,
                        amplitudeActionType = AmplitudeAlertPostWithNewAvatarValuesActionType.PUBLISH
                    )
                } else {
                    showPublishPostAlert(imagePath = event.imagePath, animation = event.animation)
                }
            }

            is UserInfoViewEffect.AvatarReadyToUpload ->
                checkAvatarPostSettings(imagePath = event.path, animation = event.avatarState)

            is UserInfoViewEffect.OnGoneProgressUserAvatar -> {
                binding?.pbUserAvatar?.gone()
                act.refreshGallery(event.imagePath)
                val imageUriPath = File(event.imagePath).toUri().toString()

                val message = when (event.createAvatarPost) {
                    CreateAvatarPostEnum.PRIVATE_ROAD.state,
                    CreateAvatarPostEnum.MAIN_ROAD.state -> R.string.profile_avatar_update_success_with_post

                    else -> R.string.profile_avatar_update_success
                }

                (activity as? ActivityToolsProvider)?.getStatusToastViewController()
                    ?.showSuccess(message = getString(message), imageUrl = imageUriPath)
            }

            is UserInfoViewEffect.OnFailureChangeAvatar -> {
                binding?.pbUserAvatar?.gone()
                act.refreshGallery(event.imagePath)
                val imageUriPath = File(event.imagePath).toUri().toString()
                (activity as? ActivityToolsProvider)?.getStatusToastViewController()
                    ?.showSuccess(message = getString(R.string.profile_avatar_update_fail), imageUrl = imageUriPath)
            }
            // Add friend request
            is UserInfoViewEffect.OnSuccessRequestAddFriend -> {
                setFragmentResult(KEY_SUBSCRIBED, bundleOf(KEY_SUBSCRIBED to true))
                showSuccessMessage(event.message)
                submitUIAction(UserProfileUIAction.SetSuggestionsEnabled(enabled = true))
            }

            is UserInfoViewEffect.OnFailureRequestAddFriend -> showError(getString(R.string.friends_add_friends_request_failure))
            // Remove from friends
            is UserInfoViewEffect.OnSuccessRemoveFriend -> {
                val message = event.message.ifEmpty {
                    if (event.unsubsribed) {
                        getString(R.string.friends_remove_friend_and_unsubscribe_success)
                    } else {
                        getString(R.string.friends_remove_friend_success)
                    }
                }
                showSuccessMessage(message)
                onRefresh()
            }

            is UserInfoViewEffect.OnFailureRemoveFriend -> showError(getString(R.string.friends_remove_friend_failure))
            // Block user
            is UserInfoViewEffect.OnSuccessBlockUser -> {
                setFragmentResult(KEY_SUBSCRIBED, bundleOf(KEY_SUBSCRIBED to false))
                event.isBlock?.let { isBlock ->
                    if (isBlock) {
                        showSuccessMessage(getString(R.string.friends_block_user_success))
                    } else {
                        showSuccessMessage(getString(R.string.friends_unblock_user_success))
                    }
                }
                onRefresh()
            }

            is UserInfoViewEffect.OnFailureBlockUser -> showError(getString(R.string.friends_block_user_failure))
            is UserInfoViewEffect.OnSuccessDisableChat -> {
                setFragmentResult(KEY_SAID_HELLO, bundleOf(KEY_SAID_HELLO to true))
                showSuccessMessage(getString(R.string.personal_messages_toast_disallow_messages))
                binding?.fabChat?.gone()
                onRefresh()
            }

            is UserInfoViewEffect.OnSuccessEnableChat -> {
                setFragmentResult(KEY_SAID_HELLO, bundleOf(KEY_SAID_HELLO to false))
                setFragmentResult(KEY_MESSAGES_ALLOWED, bundleOf(KEY_MESSAGES_ALLOWED to true))
                showSuccessMessage(getString(R.string.personal_messages_toast_allow_messages))
                binding?.fabChat?.visible()
                onRefresh()
            }

            is UserInfoViewEffect.OnSuccessEnableChatCompanionBlocked -> {
                NToast.with(view)
                    .text(getString(R.string.personal_messages_toast_allow_messages))
                    .typeSuccess()
                    .dismissListener {
                        showError(getString(R.string.personal_messages_toast_disallow_info_message))
                    }
                    .show()
                onRefresh()
            }

            is UserInfoViewEffect.OnFailureDisableChat ->
                showError(getString(R.string.personal_messages_toast_disallow_message_failed))

            is UserInfoViewEffect.OnFailureEnableChat ->
                showError(getString(R.string.personal_messages_toast_allow_messages_failed))
            // Hide progress upload images
            is UserInfoViewEffect.OnHideProgressUploadImage -> {}
            is UserInfoViewEffect.OnSubscribed -> {
                subscribeSuccess(event.message)
                setFragmentResult(KEY_SUBSCRIBED, bundleOf(KEY_SUBSCRIBED to true))
                userProfileViewModel.handleUIAction(UserProfileUIAction.SetSuggestionsEnabled(enabled = true))
            }

            is UserInfoViewEffect.OnSubscribeFailure ->
                showError(event.message ?: getString(R.string.subscribe_error_message))

            is UserInfoViewEffect.OnUnsubscribed -> {
                unsubscribeSuccess()
                setFragmentResult(KEY_SUBSCRIBED, bundleOf(KEY_SUBSCRIBED to false))
            }

            is UserInfoViewEffect.OnUnsubscribeFailure ->
                showError(event.message ?: getString(R.string.unsubscribe_error_message))

            is UserInfoViewEffect.OnSuccessEnableSubscriptionNotification -> {
                subscribeSuccess(getString(R.string.enabled_new_post_notif))
                binding?.ivNotificationBell?.setImageDrawable(
                    getDrawable(act, R.drawable.ic_profile_notification_on)
                )
                onRefresh()
            }

            is UserInfoViewEffect.OnSuccessDisableSubscriptionNotification -> {
                subscribeSuccess(getString(R.string.disabled_new_post_notif))
                binding?.ivNotificationBell?.setImageDrawable(
                    getDrawable(act, R.drawable.ic_profile_notification_off)
                )
                onRefresh()
            }

            is UserInfoViewEffect.OnFailChangeSubscriptionNotification ->
                showError(getString(R.string.notification_settings_change_error))

            is RoadPostViewEffect.OnSuccessUnhidePosts -> {
                showSuccessMessage(getString(R.string.show_user_posts))
                onRefresh()
            }

            is RoadPostViewEffect.OnSuccessHidePosts -> {
                showSuccessMessage(getString(R.string.user_complain_posts_hided))
                onRefresh()
            }

            is RoadPostViewEffect.OnFailureUnhidePosts ->
                showError(getString(R.string.show_user_posts_error))

            is RoadPostViewEffect.OnFailureHidePosts ->
                showError(getString(R.string.user_complain_posts_hided_error))

            is PhoneCallsViewEffect.OnSuccessEnableCalls -> {
                showSuccessMessage(getString(R.string.personal_messages_toast_allow_calls))
                userProfile?.let { user ->
                    if (callIsUnavailable(user).not())
                        binding?.fabCall?.visible()
                }
                onRefresh()
            }

            is PhoneCallsViewEffect.OnFailureEnableCalls ->
                showError(getString(R.string.personal_messages_toast_disallow_info_call))

            is PhoneCallsViewEffect.OnSuccessDisableCalls -> {
                showSuccessMessage(getString(R.string.personal_messages_toast_disallow_calls))
                onRefresh()
            }

            is PhoneCallsViewEffect.OnFailureDisableCalls ->
                showError(getString(R.string.personal_messages_toast_disallow_info_call))

            is UserInfoViewEffect.OnSuccessCancelFriendRequest -> {
                val message = if (event.unsubscribed) {
                    getString(R.string.friends_cancel_friend_request_and_unsubscribe_success)
                } else {
                    getString(R.string.friends_cancel_friend_request_success)
                }
                showSuccessMessage(message)
                onRefresh()
            }

            UserInfoViewEffect.OnRefreshUserRoad -> onNewPost()
            is UserInfoViewEffect.SetSnippetState -> {
                binding?.rvContent?.scrollToPosition(0)
                binding?.appbarProfile?.setExpanded(true)
                userSnippet?.setState(event.snippetState)
            }

            is UserInfoViewEffect.OnMomentsPreviewUpdated -> {
                setupMoments(event.previews, event.hasNewMoments)
            }

            is UserInfoViewEffect.SubmitAvatars -> {
                event.count?.let {
                    binding?.rvAvatarCounter?.totalCount = it
                }
                avatarsAdapter?.submitList(event.items) {
                    event.currentPosition?.let {
                        binding?.rvAvatarCounter?.selectPosition(it)
                        binding?.vpAvatars?.scrollToPosition(it)
                    }
                }
            }
            else -> Unit
        }
    }

    private fun callToUser(event: UserInfoViewEffect.CallToUser) {
        val existUser = userProfile ?: return

        if (userProfileViewModel.isWebSocketEnabled().not()) {
            showError(getString(R.string.no_internet_connection))
            requireContext().vibrate()
            return
        }

        if (callIsUnavailable(existUser) || event.iCanCall.not()) {
            showCallIsRestrictedAlert(existUser.name, chatIsUnavailable(existUser).not())
            userProfileViewModel.callUnavailable(existUser.userId)
            return
        }

        startCall(existUser.toUserChat())
        requireContext().vibrate()
    }

    private fun unsubscribeSuccess() {
        val messageToShow = getString(R.string.disabled_new_post_notif)
        showSuccessMessage(messageToShow)
        onRefresh()
    }

    /**
     * Called when user successfully subscribed
     * */
    private fun subscribeSuccess(message: String) {
        val messageToShow = if (message.isEmpty())
            getString(R.string.friend_request_send_notiff_on)
        else message
        showSuccessMessage(messageToShow)
    }

    fun onRefresh() {
        binding?.srlUserProfile?.isRefreshing = false
        Timber.e("REFRESH Profile: $userId")
        userProfileViewModel.refreshProfile()
    }

    override fun showEmptyFeedPlaceholder() {
        onRefresh()
    }

    private fun isAnimatedAvatar() = !userProfile?.avatarDetails?.avatarAnimation.isNullOrEmpty()

    override fun onOffsetChanged(appBarLayout: AppBarLayout, verticalOffset: Int) {
        userSnippet?.setVerticalOffset(verticalOffset)
        toolbarController.onOffsetChanged(appBarLayout, verticalOffset, binding)
    }

    private fun handleUpload(workInfo: WorkInfo?) {
        if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
            onRefresh()
            adapterProfile?.hideProgressGallery()
        }
        if (workInfo != null && workInfo.state == WorkInfo.State.RUNNING) {
            adapterProfile?.showProgressGallery()
        }
        if (workInfo != null && workInfo.state == WorkInfo.State.FAILED) {
            showError(getString(R.string.media_upload_error))
            adapterProfile?.hideProgressGallery()
        }
    }


    override fun onStop() {
        super.onStop()
        adapterProfile?.callIOutStatusFloor()?.stopView()
        removeAvatarObserveChangeListener()
        infoTooltip?.dismiss()
    }

    private fun addPhoto() {
        checkMediaPermissions(
            object : PermissionDelegate.Listener {

                override fun onGranted() {
                    addPhotoWithPermissionState(PermissionState.GRANTED)
                }

                override fun onDenied() {
                    addPhotoWithPermissionState(PermissionState.NOT_GRANTED_CAN_BE_REQUESTED)
                }

                override fun needOpenSettings() {
                    addPhotoWithPermissionState(PermissionState.NOT_GRANTED_OPEN_SETTINGS)
                }
            }
        )

    }

    private fun addPhotoWithPermissionState(permissionState: PermissionState) {
        mediaPicker = loadMultiImage(
            activity = act,
            viewLifecycleOwner = viewLifecycleOwner,
            maxCount = 5,
            type = MediaControllerOpenPlace.Gallery,
            message = "",
            suggestionsMenu = SuggestionsMenu(act.getCurrentFragment(), SuggestionsMenuType.ROAD),
            permissionState = permissionState,
            tedBottomSheetPermissionActionsListener = this,
            loadImagesCommonCallback = CoreTedBottomPickerActDependencyProvider(
                act = act,
                onReadyImagesUri = { images -> uploadToGallery(images) },
                onReadyImagesUriWithText = { images, _ -> uploadToGallery(images) }
            ),
            cameraLensFacing = CameraCharacteristics.LENS_FACING_BACK,
        )
    }

    private fun uploadToGallery(images: List<Uri>) {
        submitUIAction(UserProfileUIAction.UploadToGallery(images))
    }

    override fun onDestroyView() {
        act.permissionListener.remove(listener)

        adapterProfile = null
        userSnippet = null
        feedRecycler?.onDestroyView()
        feedRecycler = null
        binding?.rvContent?.adapter = null
        binding?.rvContent?.onDestroyView()
        super.onDestroyView()
    }

    override var needToShowProfile: Boolean = false

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
            },
            Manifest.permission.CAMERA
        )
    }

    override fun onCameraOpenSettings() {
        showCameraSettingsDialog()
    }

    private fun showCameraSettingsDialog() {
        ConfirmDialogBuilder()
            .setHeader(getString(R.string.camera_settings_dialog_title))
            .setDescription(getString(R.string.camera_settings_dialog_description))
            .setLeftBtnText(getString(R.string.camera_settings_dialog_cancel))
            .setRightBtnText(getString(R.string.camera_settings_dialog_action))
            .setCancelable(false)
            .setRightClickListener {
                requireContext().openSettingsScreen()
            }
            .show(childFragmentManager)
    }

    private fun openUserListFriendsOrShowAuth(
        userId: Long,
        actionType: FriendsHostFragmentNew.SelectedPage?,
        name: String
    ) = needAuth { navigator.openFriendsHostFragment(userId, name, actionType) }

    private fun transformToPreview() {
        binding?.msbcvUserRoot?.interceptTouchEvents = true
        binding?.rvContent?.scrollToPosition(0)
        binding?.appbarProfile?.setExpanded(true)
        showHideAvatarsCarousel(
            user = userProfile,
            isPreview = true
        )
        userSnippet?.setState(SnippetState.Preview)
    }

    private fun saveImage(imageUrl: String) {
        isSavingFeedPhoto = true
        saveImageOrVideoFile(
            imageUrl = imageUrl,
            act = act,
            viewLifecycleOwner = viewLifecycleOwner,
            successListener = {
                showSuccessMessage(getString(R.string.image_saved))
                doDelayed(SAVING_PICTURE_DELAY) { isSavingFeedPhoto = false }
            }
        )
    }

    private fun initClickListeners() {
        binding?.nickname?.setDrawableClickListener { _, _ ->
            if (userProfile?.accountDetails?.isTopContentMaker == false || toolbarController.getLastOffset() != 0) {
                return@setDrawableClickListener
            }
            submitUIAction(UserProfileUIAction.TopMarkerClick)
        }
    }

    private fun logProfileEntrance(state: UserProfileStateUIModel) {
        val subscriberFloor = state.profileUIList
            .find { it is SubscribersFloorUiEntity } as? SubscribersFloorUiEntity
        userProfileViewModel.handleUIAction(
            UserProfileUIAction.OnLogProfileEntrance(
                where = arguments?.getString(ARG_TRANSIT_FROM) ?: AmplitudePropertyWhere.OTHER.property,
                relationship = state.profile.settingsFlags.toFriendRelationshipAmplitude(),
                approved = state.profile.approved,
                topContentMaker = state.profile.topContentMaker,
                countMutualAudience = subscriberFloor?.mutualFriendsAndSubscribersCount ?: 0,
                haveVisibilityMutualAudience = state.profile.showFriendsAndSubscribers
            )
        )
    }

}
