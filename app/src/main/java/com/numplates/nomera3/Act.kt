package com.numplates.nomera3

import android.Manifest
import android.Manifest.permission.POST_NOTIFICATIONS
import android.animation.Animator
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import android.view.Display
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.children
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.airbnb.lottie.LottieAnimationView
import com.google.android.exoplayer2.database.DatabaseProvider
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.gun0912.tedonactivityresult.TedOnActivityResult
import com.meera.core.base.BaseFragment
import com.meera.core.base.OnActivityInteractionCallback
import com.meera.core.common.PREF_NAME
import com.meera.core.extensions.doAsync
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.gone
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.isLauncher
import com.meera.core.extensions.lightVibrate
import com.meera.core.extensions.needToUpdateStr
import com.meera.core.extensions.register
import com.meera.core.extensions.vibrate
import com.meera.core.extensions.visible
import com.meera.core.navigation.NavigationRouter
import com.meera.core.navigation.Screens
import com.meera.core.permission.PERMISSION_MEDIA_CODE
import com.meera.core.permission.PermissionDelegate
import com.meera.core.preferences.AppSettings
import com.meera.core.preferences.PrefManagerImpl
import com.meera.core.utils.checkAppRedesigned
import com.meera.core.utils.clearNotifications
import com.meera.core.utils.files.FileManager
import com.meera.core.utils.files.FileUtilsImpl.Companion.MEDIA_TYPE_IMAGE
import com.meera.core.utils.files.FileUtilsImpl.Companion.MEDIA_TYPE_IMAGE_GIF
import com.meera.core.utils.files.FileUtilsImpl.Companion.MEDIA_TYPE_UNKNOWN
import com.meera.core.utils.files.FileUtilsImpl.Companion.MEDIA_TYPE_VIDEO
import com.meera.core.views.NavigationBarViewContract
import com.meera.core.views.TooltipViewController
import com.meera.db.models.dialog.DialogEntity
import com.meera.db.models.dialog.UserChat
import com.meera.media_controller_api.MediaControllerFeatureApi
import com.meera.media_controller_api.model.MediaControllerCallback
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.meera.media_controller_implementation.MediaControllerFeatureBuilder
import com.meera.referrals.ui.ReferralFragment
import com.noomeera.nmrmediatools.NMRPhotoAmplitude
import com.noomeera.nmrmediatools.NMRVideoAmplitude
import com.numplates.nomera3.data.fcm.IPushInfo.CHAT_INCOMING_MESSAGE
import com.numplates.nomera3.data.fcm.NotificationHelper
import com.numplates.nomera3.data.fcm.PushObjectNew
import com.numplates.nomera3.data.network.core.INetworkValues
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_AUDIO
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_EVENT
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_GIF
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_IMAGE
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_POST
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_TEXT
import com.numplates.nomera3.modules.appDialogs.ui.DialogDismissListener
import com.numplates.nomera3.modules.appDialogs.ui.DialogNavigator
import com.numplates.nomera3.modules.appDialogs.ui.DismissDialogType
import com.numplates.nomera3.modules.appInfo.domain.usecase.GetAppInfoAsyncUseCase
import com.numplates.nomera3.modules.appInfo.ui.ForceUpdateDialog
import com.numplates.nomera3.modules.auth.ui.AuthNavigator
import com.numplates.nomera3.modules.auth.ui.IAuthStateObserver
import com.numplates.nomera3.modules.auth.ui.MeeraAuthNavigator
import com.numplates.nomera3.modules.auth.util.AuthStatusObserver
import com.numplates.nomera3.modules.auth.util.isNeedAuth
import com.numplates.nomera3.modules.auth.util.needAuth
import com.numplates.nomera3.modules.avatar.ContainerAvatarFragment
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.editor.AmplitudeEditor
import com.numplates.nomera3.modules.baseCore.helper.amplitude.editor.AmplitudeEditorParams
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudePropertyMomentScreenOpenWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.people.AmplitudePeopleWhereProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.FriendInviteTapProperty
import com.numplates.nomera3.modules.baseCore.helper.notification.NotificationManagerImpl.Companion.MESSENGER_BASE
import com.numplates.nomera3.modules.bump.ui.ShakeEventDelegateUi
import com.numplates.nomera3.modules.bump.ui.ShakeEventUiHandler
import com.numplates.nomera3.modules.bump.ui.ShakeRegisterUiHandler
import com.numplates.nomera3.modules.bump.ui.fragment.MeeraShakeBottomDialogFragment
import com.numplates.nomera3.modules.bump.ui.fragment.ShakeBottomDialogFragment
import com.numplates.nomera3.modules.bump.ui.fragment.ShakeFriendRequestsFragment
import com.numplates.nomera3.modules.bump.ui.isAllowToRegisterShakeInCurrentScreen
import com.numplates.nomera3.modules.chat.ChatFragmentNew
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitData
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitType
import com.numplates.nomera3.modules.chat.ui.ActivityInteractChatActions
import com.numplates.nomera3.modules.chat.ui.ActivityInteractionChatCallback
import com.numplates.nomera3.modules.comments.ui.fragment.PostFragmentV2
import com.numplates.nomera3.modules.common.ActivityToolsProvider
import com.numplates.nomera3.modules.communities.ui.CommunityChangesViewController
import com.numplates.nomera3.modules.communities.ui.fragment.CommunityRoadFragment
import com.numplates.nomera3.modules.communities.ui.fragment.members.CommunityMembersContainerFragment
import com.numplates.nomera3.modules.complains.ui.ComplaintFlowInteraction
import com.numplates.nomera3.modules.complains.ui.ComplaintFlowInteractionDelegate
import com.numplates.nomera3.modules.exoplayer.ExoPlayerCache
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.feed.ui.entity.DestinationOriginEnum
import com.numplates.nomera3.modules.feedmediaview.ui.fragment.ViewMultimediaFragment
import com.numplates.nomera3.modules.maps.ui.geo_popup.GeoPopupDialog
import com.numplates.nomera3.modules.maps.ui.geo_popup.MeeraGeoPopupDialog
import com.numplates.nomera3.modules.moments.show.data.ARG_MOMENT_ID
import com.numplates.nomera3.modules.moments.show.presentation.MomentCreateViewController
import com.numplates.nomera3.modules.moments.show.presentation.MomentCreateViewModel
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentClickOrigin
import com.numplates.nomera3.modules.moments.show.presentation.fragment.KEY_MOMENT_CLICK_ORIGIN
import com.numplates.nomera3.modules.moments.show.presentation.fragment.KEY_MOMENT_COMMENT_ID
import com.numplates.nomera3.modules.moments.show.presentation.fragment.KEY_MOMENT_LAST_REACTION_TYPE
import com.numplates.nomera3.modules.moments.show.presentation.fragment.KEY_MOMENT_PREVENT_ANIMATION
import com.numplates.nomera3.modules.moments.show.presentation.fragment.KEY_MOMENT_PUSH_INFO
import com.numplates.nomera3.modules.moments.show.presentation.fragment.KEY_MOMENT_TARGET_ID
import com.numplates.nomera3.modules.moments.show.presentation.fragment.KEY_OPENED_FROM_VIEW_POSITION
import com.numplates.nomera3.modules.moments.show.presentation.fragment.KEY_START_GROUP_ID
import com.numplates.nomera3.modules.moments.show.presentation.fragment.KEY_USER_ID
import com.numplates.nomera3.modules.moments.show.presentation.fragment.MomentsFragmentClosingAnimationState
import com.numplates.nomera3.modules.moments.show.presentation.fragment.ViewMomentFragment
import com.numplates.nomera3.modules.moments.show.presentation.fragment.ViewMomentPositionFragment
import com.numplates.nomera3.modules.newroads.MainPostRoadsFragment
import com.numplates.nomera3.modules.newroads.featureAnnounce.data.DeeplinkAction
import com.numplates.nomera3.modules.newroads.featureAnnounce.data.DeeplinkOrigin
import com.numplates.nomera3.modules.newroads.featureAnnounce.data.FeatureDeepLink
import com.numplates.nomera3.modules.notifications.ui.fragment.NotificationDetailFragment
import com.numplates.nomera3.modules.notifications.ui.viewmodel.SingleLiveEvent
import com.numplates.nomera3.modules.peoples.ui.fragments.PeoplesFragment
import com.numplates.nomera3.modules.purchase.ui.vip.FragmentUpgradeToVipNew
import com.numplates.nomera3.modules.purchase.ui.vip.UpdateStatusFragment
import com.numplates.nomera3.modules.reaction.data.ReactionType
import com.numplates.nomera3.modules.reaction.ui.MeeraReactionBubbleViewController
import com.numplates.nomera3.modules.reaction.ui.ReactionBubbleViewController
import com.numplates.nomera3.modules.reaction.ui.custom.ReactionBubble
import com.numplates.nomera3.modules.reaction.ui.util.ReactionPreloadUtil
import com.numplates.nomera3.modules.reactionStatistics.ui.MeeraReactionsStatisticsBottomSheetFragment
import com.numplates.nomera3.modules.reactionStatistics.ui.ReactionsEntityType
import com.numplates.nomera3.modules.reactionStatistics.ui.ReactionsStatisticsBottomSheetFragment
import com.numplates.nomera3.modules.redesign.util.MeeraAuthNavigation
import com.numplates.nomera3.modules.registration.ui.RegistrationContainerFragment
import com.numplates.nomera3.modules.screenshot.ui.fragment.ScreenshotTakenListener
import com.numplates.nomera3.modules.search.ui.fragment.SearchMainFragment
import com.numplates.nomera3.modules.upload.ui.MeeraStatusToastViewController
import com.numplates.nomera3.modules.upload.ui.StatusToastViewController
import com.numplates.nomera3.modules.upload.ui.viewmodel.UploadStatusViewModel
import com.numplates.nomera3.modules.uploadpost.ui.ARG_TO_PERSONAL_ROAD
import com.numplates.nomera3.modules.uploadpost.ui.AddMultipleMediaPostFragment
import com.numplates.nomera3.modules.viewvideo.presentation.ViewVideoItemFragment
import com.numplates.nomera3.presentation.birthday.ui.BirthdayBottomDialogFragment
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseAct
import com.numplates.nomera3.presentation.router.IActionContainer
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_CALLED_FROM_PROFILE
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_CALL_ACCEPTED
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_COMMENT_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_COMMENT_LAST_REACTION
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_EVENT_GROUP_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_FEED_POST_HAVE_REACTIONS
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_FEED_POST_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_GIFT_SEND_WHERE
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_GROUP_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_IS_FROM_PUSH
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_IS_GOTO_INCOMING
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_MOMENT_AUTHOR_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_OPEN_FROM_REACTIONS
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_POST_LATEST_REACTION_TYPE
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_POST_ORIGIN
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_PUSH_EVENT_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_ROOM_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_SELECT_ON_MAIN_FRAGMENT
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_SHOW_BIRTHDAY
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_SHOW_MEDIA_GALLERY
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_TRANSIT_FROM
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_UPGRADE_TO_VIP_GOLD
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_URL
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_DATE_OF_BIRTH
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_MODEL
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_NAME
import com.numplates.nomera3.presentation.router.IArgContainer.MAP_FRAGMENT
import com.numplates.nomera3.presentation.router.IArgContainer.PEOPLES_FRAGMENT
import com.numplates.nomera3.presentation.router.IArgContainer.ROAD_FRAGMENT
import com.numplates.nomera3.presentation.router.IArgContainer.ROOMS_FRAGMENT
import com.numplates.nomera3.presentation.router.IArgContainer.USER_INFO_FRAGMENT
import com.numplates.nomera3.presentation.router.NavigationRouterAdapterActImpl
import com.numplates.nomera3.presentation.utils.ScreenArgs
import com.numplates.nomera3.presentation.utils.addTo
import com.numplates.nomera3.presentation.view.callback.IOnBackPressed
import com.numplates.nomera3.presentation.view.callback.IOnKeyDown
import com.numplates.nomera3.presentation.view.fragments.CallsEnabledFragment
import com.numplates.nomera3.presentation.view.fragments.FriendsHostFragmentNew
import com.numplates.nomera3.presentation.view.fragments.MainFragment
import com.numplates.nomera3.presentation.view.fragments.MapFragment
import com.numplates.nomera3.presentation.view.fragments.ProfileSettingsFragmentNew
import com.numplates.nomera3.presentation.view.fragments.PushNotificationsSettingsFragment
import com.numplates.nomera3.presentation.view.fragments.UserGiftsFragment
import com.numplates.nomera3.presentation.view.fragments.UserInfoFragment
import com.numplates.nomera3.presentation.view.fragments.UserPersonalInfoFragment
import com.numplates.nomera3.presentation.view.fragments.privacysettings.FriendsFollowersPrivacyFragment
import com.numplates.nomera3.presentation.view.fragments.privacysettings.PrivacyFragmentNew
import com.numplates.nomera3.presentation.view.fragments.profilephoto.GridProfilePhotoFragment
import com.numplates.nomera3.presentation.view.fragments.profilephoto.ProfilePhotoViewerFragment
import com.numplates.nomera3.presentation.view.navigator.MomentPageTransformer
import com.numplates.nomera3.presentation.view.navigator.NavigatorAdapter
import com.numplates.nomera3.presentation.view.navigator.NavigatorViewPager
import com.numplates.nomera3.presentation.view.ui.ActNavigator
import com.numplates.nomera3.presentation.view.utils.NSupport
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.view.utils.PermissionManager
import com.numplates.nomera3.presentation.view.utils.apphints.Hint
import com.numplates.nomera3.presentation.viewmodel.CommunityChanges
import com.numplates.nomera3.presentation.viewmodel.ConnectShakeSocketUiEffect
import com.numplates.nomera3.presentation.viewmodel.HolidayViewEvent
import com.numplates.nomera3.presentation.viewmodel.Logout
import com.numplates.nomera3.presentation.viewmodel.MainActivityViewModel
import com.numplates.nomera3.presentation.viewmodel.NavigateLocationSettingsUiEffect
import com.numplates.nomera3.presentation.viewmodel.NavigateToAppSettingsUiEffect
import com.numplates.nomera3.presentation.viewmodel.OnAddReaction
import com.numplates.nomera3.presentation.viewmodel.OnBindSignallingService
import com.numplates.nomera3.presentation.viewmodel.OnCheckServiceConnection
import com.numplates.nomera3.presentation.viewmodel.OnHolidayReady
import com.numplates.nomera3.presentation.viewmodel.OnPreferencesReady
import com.numplates.nomera3.presentation.viewmodel.OnSocketError
import com.numplates.nomera3.presentation.viewmodel.OnSupportUserIdReady
import com.numplates.nomera3.presentation.viewmodel.ReadyForRestartAppAfterLogout
import com.numplates.nomera3.presentation.viewmodel.SendScreenshotTakenEventToFragment
import com.numplates.nomera3.presentation.viewmodel.ShowBirthdayDialogEvent
import com.numplates.nomera3.presentation.viewmodel.ShowShakeDialogUiEffect
import com.numplates.nomera3.presentation.viewmodel.TryToRegisterShakeByPrivacySetting
import com.numplates.nomera3.presentation.viewmodel.TryToRegisterShakeEvent
import com.numplates.nomera3.presentation.viewmodel.viewevents.ChatRoomsViewEvent
import com.numplates.nomera3.presentation.viewmodel.viewevents.DeeplinkActionViewEvent
import com.numplates.nomera3.telecom.CallFragment
import com.numplates.nomera3.telecom.CallUiEventDispatcher
import com.numplates.nomera3.telecom.RingerModeBroadcastReceiver
import com.numplates.nomera3.telecom.SignalingService
import com.numplates.nomera3.telecom.SignalingServiceConnectionWrapper
import com.numplates.nomera3.telecom.model.CallUiEvent
import dagger.Lazy
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

open class Act : BaseAct(),
    IActionContainer,
    OnActivityInteractionCallback,
    RingerModeBroadcastReceiver.OnRingerModeChangedListener,
    ActivityToolsProvider,
    ActNavigator,
    IAuthStateObserver,
    RestartCallback,
    ActivityInteractionChatCallback,
    ComplaintFlowInteraction by ComplaintFlowInteractionDelegate() {

    @Inject
    lateinit var signalingServiceConnectionWrapper: Lazy<SignalingServiceConnectionWrapper>

    @Inject
    lateinit var callUiEventDispatcher: CallUiEventDispatcher

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var navRouter: NavigationRouter

    @Inject
    lateinit var amplitudeEditor: AmplitudeEditor

    @Inject
    lateinit var dialogDismissListener: DialogDismissListener

    @Inject
    lateinit var fileManager: FileManager

    @Inject
    lateinit var getAppInfoAsyncUseCase: GetAppInfoAsyncUseCase

    @Inject
    lateinit var featureTogglesContainer: FeatureTogglesContainer

    /**
     * Temporary field for analytic back btn
     * Need to delete after analytic finished
     * */
    var isSubscribeFloorFragment = false
    var serverAppVersionName: String? = null
    var ringerModePublishSubject = PublishSubject.create<Int>()

    private val hintJobs = mutableListOf<Job>()

    private var hintView: ConstraintLayout? = null
    private var simpleHintView: ConstraintLayout? = null
    private var geoPopupDialog: GeoPopupDialog? = null
    private var meeraGeoPopupDialog: MeeraGeoPopupDialog? = null

    private var ringerModeBroadcastReceiver: RingerModeBroadcastReceiver? = null
    private var rootLayoutActivity: FrameLayout? = null

    private var isHolidayIntroDialogShown = false

    // ID комнат, нужны для пушей
    val roomIdsSet = hashSetOf<Long>()

    val activityViewModel by viewModels<MainActivityViewModel>()

    private val momentCreateViewModel by viewModels<MomentCreateViewModel>()
    private val uploadStatusViewModel by viewModels<UploadStatusViewModel> { App.component.getViewModelFactory() }

    private lateinit var currentFragment: BaseFragment
    private var previousFragment: BaseFragment? = null

    // FRAGMENT NAVIGATOR
    lateinit var navigatorAdapter: NavigatorAdapter
    lateinit var navigatorViewPager: NavigatorViewPager
    val statusBarState = ArrayList<Int>()
    var mainFragment: BaseFragment? = null
    private var blockTouchEvent = false

    var wakeLock: PowerManager.WakeLock? = null
    private var authNavigator: AuthNavigator? = null
    private var dialogNavigator: DialogNavigator? = null
    private var communtyChangesController: CommunityChangesViewController? = null

    private var mediaController: MediaControllerFeatureApi? = null

    private var reactionBubbleViewController: ReactionBubbleViewController? = null
    private var statusToastViewController: StatusToastViewController? = null
    private var tooltipViewController: TooltipViewController? = null
    private var momentCreateViewController: MomentCreateViewController? = null
    private var shakeEventDelegateUi: ShakeEventDelegateUi? = null

    private var screenShotDetectorDelegate: ScreenShotDetectorDelegate? = null

    private val shakeEventUiHandler = object : ShakeEventUiHandler {
        override fun showLocationDialog() {
            showLocationEnableDialog()
        }

        override fun showShakeDialog(isShowDialogByShake: Boolean) {
            showBumpDialog(isShowDialogByShake)
        }

        override fun showShakeFriendRequestsDialog() {
            if (shakeFriendRequestsIsAdded()) return
            addFragment(ShakeFriendRequestsFragment(), LIGHT_STATUSBAR)
        }
    }

    override fun getAuthenticationNavigator(): AuthNavigator {
        return authNavigator ?: error("Ошибка аргумента. Невозможно получить authNavigator")
    }

    override fun getMeeraAuthNavigation(): MeeraAuthNavigation {
        error("Ошибка аргумента. Невозможно получить MeeraAuthNavigation")
    }

    override fun getMeeraAuthenticationNavigator(): MeeraAuthNavigator {
        error("Ошибка аргумента. Невозможно получить authNavigator")
    }

    override fun getMediaControllerFeature(): MediaControllerFeatureApi {
        return mediaController ?: error("Ошибка аргумента. Невозможно получить mediaControllerFeature")
    }

    override fun getMomentsViewController(): MomentCreateViewController {
        return momentCreateViewController ?: error("Ошибка. Невозможно получить momentsViewController")
    }

    override fun getReactionBubbleViewController(): ReactionBubbleViewController {
        return reactionBubbleViewController ?: error("Ошибка аргумента. Невозможно получить reactionViewController")
    }

    override fun getMeeraReactionBubbleViewController(): MeeraReactionBubbleViewController {
        error("Ошибка аргумента. Невозможно получить reactionViewController")
    }

    override fun getStatusToastViewController(): StatusToastViewController {
        return statusToastViewController ?: error("Ошибка аргумента. Невозможно получить statusToastViewController")
    }

    override fun getMeeraStatusToastViewController(): MeeraStatusToastViewController {
        error("Ошибка аргумента. Невозможно получить tooltipViewController")
    }

    override fun getTooltipController(): TooltipViewController {
        return tooltipViewController ?: error("Ошибка аргумента. Невозможно получить tooltipViewController")
    }

    override fun initAuthObserver() = object : AuthStatusObserver(
        rootActivity = this,
        lifecycleOwner = this
    ) {
        override fun onAuthState() {
            shakeEventDelegateUi?.registerFragmentsLifecycleChange()
            activityViewModel.handleAction(ActActions.TryToRegisterShakeEvent)
            activityViewModel.handleAction(ActActions.ResetSubscriptionsRoad)
        }

        override fun onNotAuthState() {
            shakeEventDelegateUi?.unRegisterFragmentsLifecycleChange()
            activityViewModel.stopSyncContacts()
            activityViewModel.handleAction(ActActions.UnregisterShakeEventListener)
            hideShakeDialog()
        }
    }

    override fun restartActivity(action: suspend () -> Unit) {
        logOutWithDelegate(
            isCancelledRegistration = activityViewModel.isRegistrationCompleted(),
            changeServer = true
        ) { action() }
    }

    override fun onGetActionFromChat(action: ActivityInteractChatActions) {
        when (action) {
            is ActivityInteractChatActions.EnablePush -> enablePushForRoomId(action.roomId)
            is ActivityInteractChatActions.DisablePush -> diasablePushForRoomId(action.roomId)
            is ActivityInteractChatActions.SetAllowedSwipeDirection ->
                navigatorViewPager.setAllowedSwipeDirection(action.direction)

            is ActivityInteractChatActions.HideHints -> hideHints()
            is ActivityInteractChatActions.ShowFireworkAnimation -> showFireworkAnimation()
            is ActivityInteractChatActions.StartCall -> startCallFromChat(action.companion)
            is ActivityInteractChatActions.ShowAppHint -> showAppHintV2(action.hint)
            is ActivityInteractChatActions.HideAppHints -> hideHints()
            is ActivityInteractChatActions.OpenLink -> openLink(action.url)
            is ActivityInteractChatActions.OpenUserMoments -> action.userId?.let { userId ->
                openUserMoments(
                    userId = userId,
                    fromView = action.view,
                    openedWhere = action.where,
                    viewedEarly = !action.hasNewMoments
                )
            }
        }
    }

    override fun onProvideMediaEditorControllerToChat(): MediaControllerFeatureApi {
        return mediaController ?: error("Ошибка аргумента. Невозможно получить mediaControllerFeature")
    }

    /**
     * КОМЕНТЫ УДАЛЯТСЯ
     * вызывается онбординг
     * сделать вызов не через диалог
     * */
    fun triggerDialogToShow() {
        /**
         * Вызов попап приватности звонков
         * */
        dialogNavigator?.triggerDialogToShow()
    }

    //add and remove notifications for rooms
    fun diasablePushForRoomId(roomId: Long) = roomIdsSet.add(roomId)

    fun enablePushForRoomId(roomId: Long) = roomIdsSet.remove(roomId)

    fun placeWebRtcCall(
        callUser: UserChat,
        isIncoming: Boolean,
        callAccepted: Boolean?,
        roomId: Long?,
        messageId: String?
    ) {
        signalingServiceConnectionWrapper.get().placeWebRtcCall(callUser, isIncoming, callAccepted, roomId, messageId)
    }

    override fun onRingerModeChanged(mode: Int?) {
        Timber.e("onRingerModeChanged: $mode")
        ringerModePublishSubject.onNext(mode ?: 0)
    }

    /*
    * Открыть галерею для выбора фото. Вызывается из экрана Профиль
    * */
    fun openPhotoEditorForProfile(uri: Uri, listener: MediaControllerCallback) {
        when (fileManager.getMediaType(uri)) {
            MEDIA_TYPE_IMAGE -> {
                logEditorOpen(
                    where = AmplitudePropertyWhere.PROFILE,
                    automaticOpen = true,
                    uri = uri
                )
                getMediaControllerFeature().open(uri, MediaControllerOpenPlace.Avatar, listener)
            }

            MEDIA_TYPE_IMAGE_GIF -> listener.onPhotoReady(uri, null)
            MEDIA_TYPE_VIDEO -> listener.onError()
            MEDIA_TYPE_UNKNOWN -> listener.onError()
            else -> listener.onError()
        }
    }

    fun navigateToTechSupport() {
        activityViewModel.navigateToTechSupport()
    }

    fun openPhotoEditorForCommunity(uri: Uri, listener: MediaControllerCallback) {
        when (fileManager.getMediaType(uri)) {
            MEDIA_TYPE_IMAGE -> {
                logEditorOpen(
                    where = AmplitudePropertyWhere.COMMUNITY,
                    automaticOpen = true,
                    uri = uri
                )
                getMediaControllerFeature().open(uri, MediaControllerOpenPlace.Avatar, listener)
            }

            MEDIA_TYPE_IMAGE_GIF -> listener.onPhotoReady(uri, null)
            MEDIA_TYPE_VIDEO -> listener.onError()
            MEDIA_TYPE_UNKNOWN -> listener.onError()
            else -> listener.onError()
        }
    }

    fun deleteTempImageFile(filePath: String?) {
        filePath?.let {
            doAsync({
                try {
                    val extension = filePath.substring(filePath.lastIndexOf("."))
                    if (extension != ".gif")
                        return@doAsync fileManager.deleteFile(it)
                    else
                        return@doAsync false
                } catch (e: Exception) {
                    Timber.e("Failed delete file ${e.message}")
                    return@doAsync false
                }
            }, { isDeleted ->
                Timber.d("Temp image file isDeleted: $isDeleted")
                refreshGallery(filePath)
            })
        }
    }

    fun showFireworkAnimation(animationEnd: () -> Unit = {}) {
        val animationView: LottieAnimationView? = findViewById(R.id.lottieAnimationView)
        if (animationView?.isAnimating != true) {
            animationView?.visible()
            animationView?.addAnimatorListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) = Unit
                override fun onAnimationCancel(animation: Animator) = Unit
                override fun onAnimationRepeat(animation: Animator) = Unit
                override fun onAnimationEnd(animation: Animator) {
                    animationView.gone()
                    animationEnd.invoke()
                }
            })
            animationView?.playAnimation()
            vibrate()
        }
    }

    fun setNavigationMomentsPageTransformer() {
        navigatorViewPager.setCurrentPageTransformer(false, MomentPageTransformer())
    }

    fun setNavigationDefaultPageTransformer() {
        navigatorViewPager.setPreviousBeforeMomentPageTransformer(false)
    }

    fun openUserMoments(
        userId: Long? = null,
        startGroupId: Long? = null,
        targetMomentId: Long? = null,
        fromView: View? = null,
        lastReactionType: ReactionType? = null,
        commentID: Long? = null,
        pushInfo: String? = null,
        openedFrom: MomentClickOrigin = MomentClickOrigin.fromUserAvatar(),
        preventMomentAnimation: Boolean = false,
        openedWhere: AmplitudePropertyMomentScreenOpenWhere = AmplitudePropertyMomentScreenOpenWhere.OTHER,
        viewedEarly: Boolean? = null
    ) {
        if (!preventMomentAnimation)
            setNavigationMomentsPageTransformer()

        val location = IntArray(2)
        fromView?.getLocationInWindow(location)
        logMomentOpen(
            userId,
            openedWhere,
            viewedEarly
        )
        addFragment(
            ViewMomentFragment(),
            COLOR_STATUSBAR_BLACK_NAVBAR,
            Arg(KEY_USER_ID, userId),
            Arg(KEY_START_GROUP_ID, startGroupId),
            Arg(KEY_MOMENT_TARGET_ID, targetMomentId),
            Arg(KEY_MOMENT_CLICK_ORIGIN, openedFrom),
            Arg(KEY_OPENED_FROM_VIEW_POSITION, location),
            Arg(KEY_MOMENT_LAST_REACTION_TYPE, lastReactionType),
            Arg(KEY_MOMENT_PUSH_INFO, pushInfo),
            Arg(KEY_MOMENT_COMMENT_ID, commentID),
            Arg(KEY_MOMENT_PREVENT_ANIMATION, preventMomentAnimation)
        )
    }

    private fun logMomentOpen(
        userId: Long?,
        openedWhere: AmplitudePropertyMomentScreenOpenWhere,
        viewedEarly: Boolean?,
    ) {
        activityViewModel.logMomentOpen(
            userId = userId,
            openedWhere = openedWhere,
            viewedEarly = viewedEarly
        )
    }

    fun logEditorOpen(
        uri: Uri,
        where: AmplitudePropertyWhere = AmplitudePropertyWhere.OTHER,
        automaticOpen: Boolean = false
    ) = amplitudeEditor.editorOpenAction(
        where = where,
        automaticOpen = automaticOpen,
        type = amplitudeEditor.getEditorType(uri)
    )

    fun logPhotoEdits(
        nmrAmplitude: NMRPhotoAmplitude,
        where: AmplitudePropertyWhere = AmplitudePropertyWhere.OTHER,
        automaticOpen: Boolean = false
    ) = amplitudeEditor.photoEditorAction(
        nmrAmplitude = nmrAmplitude,
        editorParams = AmplitudeEditorParams(
            where = where,
            automaticOpen = automaticOpen
        )
    )

    fun logVideoEdits(
        nmrAmplitude: NMRVideoAmplitude,
        where: AmplitudePropertyWhere = AmplitudePropertyWhere.OTHER,
        automaticOpen: Boolean = false
    ) = amplitudeEditor.videoEditorAction(
        nmrAmplitude = nmrAmplitude,
        editorParams = AmplitudeEditorParams(
            where = where,
            automaticOpen = automaticOpen
        )
    )

    override fun attachBaseContext(newBase: Context) {
        val preferences = PrefManagerImpl(newBase.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE), newBase)
        val lang = preferences.getString(AppSettings.KEY_LOCALE, "ru")
        val modifiedContext = newBase
            .apply { LanguageContextWrapper.wrap(this, lang) }
            .apply { FontSizeContextWrapper.wrap(this, 1.0F) }
        super.attachBaseContext(modifiedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_Transparent)
        super.onCreate(null)
        setContentView(R.layout.act)
        rootLayoutActivity = findViewById(R.id.root_layout_activity)
        activityViewModel.handleAction(ActActions.SubscribeEvent)
        activityViewModel.handleAction(ActActions.LoadPrivacySettings)
        app.setRestartCallbackListener(this)
        App.component.inject(this)
        dialogNavigator = DialogNavigator(this)
        getHolidayInfo()
        app.hashSetRooms = roomIdsSet
        if (::navigatorAdapter.isInitialized)
            runOnUiThread {
                navigatorAdapter.removeAllFragments()
            }
        initFragmentNavigator()
        authNavigator = AuthNavigator(
            rootActivity = this,
            navigatorViewPager = navigatorViewPager,
        )
        initFragment(intent = intent)
        initObservers()
        activityViewModel.handleAction(ActActions.ObserveRefreshTokenRestService)
        initVideoCache()
        ringerModeBroadcastReceiver = RingerModeBroadcastReceiver(this)

        // регистрируем слушатель на выключение звука
        ringerModeBroadcastReceiver?.register(
            context = this,
            filter = RingerModeBroadcastReceiver.createIntentFilter()
        )
        requestPostNotificationPermission()
        ReactionPreloadUtil.preloadReactionsResources(this)
        activityViewModel.handleAction(ActActions.InitializeAvatarSDK(assets))
        initCommunityChangesController()
        initStatusToastController()
        initReactionController()
        initTooltipViewController()
        initSignalingServiceConnectionWrapper()
        initAuthObserver()
        initShakeDelegateUi()
        initMediaFeature()
        initMomentsController()
    }

    private fun requestPostNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(this, POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            PermissionDelegate(this, this).setPermissions(null, POST_NOTIFICATIONS)
        }
    }

    fun hideShakeDialog() = shakeEventDelegateUi?.closeShakeDialog()

    fun logOutWithDelegate(
        isCancelledRegistration: Boolean = false,
        unsubscribePush: Boolean = true,
        changeServer: Boolean = false,
        action: (suspend () -> Unit)? = null
    ) {
        activityViewModel.logOutWithDelegate(
            isCancelledRegistration,
            unsubscribePush,
            changeServer
        ) { reg: Boolean, hol: Boolean ->
            if (reg) {
                resetNavigatorToMainRoadScreen()
                createNewAdapter()
            }

            isHolidayIntroDialogShown = hol

            action?.invoke()
        }
    }

    private fun initCommunityChangesController() {
        communtyChangesController = CommunityChangesViewController(
            view = rootLayoutActivity,
            deleteCommunity = activityViewModel::deleteCommunity,
            restoreCommunity = activityViewModel::restoreCommunity
        )
    }

    private fun initMomentsController() {
        momentCreateViewController = MomentCreateViewController(
            activity = this,
            viewModel = momentCreateViewModel,
            mediaController = mediaController
        )
    }

    private fun initMediaFeature() {
        mediaController = MediaControllerFeatureBuilder.build(
            applicationApi = App.component,
            rootActivity = this
        )
    }

    private fun initReactionController() {
        reactionBubbleViewController = ReactionBubbleViewController()
        reactionBubbleViewController?.init(
            act = this@Act,
            handleAction = { action ->
                activityViewModel.handleAction(action)
            },
            timeOfDayReactionsFeatureToggle = featureTogglesContainer.timeOfDayReactionsFeatureToggle
        )
    }

    private fun initSignalingServiceConnectionWrapper() {
        signalingServiceConnectionWrapper.get().onStartCall = { isIncoming, callUser, callAccepted ->
            val callFragment = supportFragmentManager.fragments.find { it is CallFragment }
            if (callFragment == null && !callUser?.name.isNullOrBlank()) {
                callUiEventDispatcher.setEvent(CallUiEvent.CREATED)
                addFragment(
                    CallFragment(),
                    COLOR_STATUSBAR_BLACK_NAVBAR,
                    Arg(TYPE_CALL_KEYS, if (isIncoming) INCOMING_CALL_KEY else OUTGOING_CALL_KEY),
                    Arg(ARG_USER_MODEL, callUser),
                    Arg(ARG_CALL_ACCEPTED, callAccepted),
                )
            }
        }
    }

    private fun initStatusToastController() {
        statusToastViewController = StatusToastViewController(
            act = this,
            statusToast = findViewById(R.id.uploadStatusToastView),
            uploadStatusViewModel = uploadStatusViewModel
        )
    }

    private fun initTooltipViewController() {
        rootLayoutActivity?.let { view ->
            tooltipViewController = TooltipViewController().apply {
                init(view)
            }
        }
    }

    fun getHolidayInfo(isFromAuth: Boolean = false) {
        activityViewModel.getHolidayInfo(isFromAuth)
    }

    fun handleFeatureDeepLink(deeplink: String) {
        try {
            when (val deeplinkAction = FeatureDeepLink.getAction(deeplink)) {
                DeeplinkAction.GoOwnProfileTabAction -> needAuth { deeplinkActionGoOwnProfileTab() }
                DeeplinkAction.OpenChatsAction -> needAuth { deeplinkActionChats() }
                DeeplinkAction.OpenNotifications -> needAuth { deeplinkActionNotifications() }
                DeeplinkAction.OpenMapAction -> needAuth { deeplinkActionOpenMap() }
                DeeplinkAction.SearchUserAction -> needAuth { deeplinkActionSearchUser() }
                DeeplinkAction.CreateNewPostAction -> needAuth { deeplinkActionRoadNewPost() }
                DeeplinkAction.CreateNewPostPersonalAction -> needAuth { deeplinkActionRoadNewPersonalPost() }
                DeeplinkAction.OpenMyCommunityAction -> needAuth { deeplinkActionMyCommunity() }
                DeeplinkAction.OpenUserSettingsAction -> needAuth { deeplinkActionUserSettings() }
                DeeplinkAction.OpenAboutMeAction -> needAuth { deeplinkActionOpenAboutMe() }
                DeeplinkAction.ProfileEditAction -> needAuth { deeplinkActionProfileEdit() }
                DeeplinkAction.PrivacyAction -> needAuth { deeplinkActionPrivacy() }
                DeeplinkAction.UserNotificationSettingsAction -> needAuth { deeplinkActionNotificationSettings() }
                DeeplinkAction.UserReferralAction -> needAuth { deeplinkActionUserReferral() }
                DeeplinkAction.MakeProfileVipAction -> needAuth { deeplinkActionUserMakeVip() }
                DeeplinkAction.OpenPeopleAction -> needAuth { deeplinkActionPeople() }
                DeeplinkAction.CreateNewMoment -> needAuth { createNewMoment() }
                is DeeplinkAction.OpenSpecificUserAction -> deeplinkActionOpenSpecificUser(deeplinkAction.userId)
                is DeeplinkAction.OpenSpecificCommunityAction -> needAuth {
                    deeplinkActionOpenSpecificCommunity(
                        deeplinkAction.communityId
                    )
                }

                is DeeplinkAction.OpenSpecificPostAction -> {
                    needAuth {
                        val deeplinkOrigin = FeatureDeepLink.getDeeplinkOrigin(deeplink)
                        deeplinkActionOpenSpecificPost(deeplinkAction.postId, deeplinkOrigin)
                    }
                }

                is DeeplinkAction.OpenSpecificMomentAction -> {
                    deeplinkActionOpenSpecificMoment(deeplinkAction.momentId)
                }

                is DeeplinkAction.OpenSpecificChatAction -> needAuth {
                    deeplinkActionChat(deeplinkAction.userId)
                }

                else -> {
                    // if deep link is not supported, navigate to play market
                    needAuth { NSupport.openAppGooglePlayMarketPage(this) }
                }
            }
        } catch (e: Exception) {
            Timber.i("Feature Deeplink parse Exception:${e.message}")
            NSupport.openAppGooglePlayMarketPage(this)
        }
    }

    private fun createNewMoment() = getMomentsViewController().open()

    private fun initShakeDelegateUi() {
        shakeEventDelegateUi = ShakeEventDelegateUi(
            manager = supportFragmentManager,
            storeOwner = this,
            lifecycle = lifecycle,
            scope = lifecycleScope,
            shakeEventUiHandler = shakeEventUiHandler
        )
    }

    private fun showBumpDialog(isShowDialogByShake: Boolean) {
        checkAppRedesigned(
            isRedesigned = { showMeeraBumpDialog(isShowDialogByShake) },
            isNotRedesigned = { showOldBumpDialog(isShowDialogByShake) }
        )
    }

    private fun showMeeraBumpDialog(isShowDialogByShake: Boolean) {
        val actionType = if (isShowDialogByShake) {
            MeeraShakeBottomDialogFragment.DIALOG_OPENED_BY_SHAKE
        } else {
            MeeraShakeBottomDialogFragment.DIALOG_OPENED_FROM_SOMEWHERE_ELSE
        }
        MeeraShakeBottomDialogFragment.show(
            fragmentManager = supportFragmentManager,
            openPlace = actionType
        )
    }

    private fun showOldBumpDialog(isShowDialogByShake: Boolean) {
        val actionType = if (isShowDialogByShake) {
            ShakeBottomDialogFragment.DIALOG_OPENED_BY_SHAKE
        } else {
            ShakeBottomDialogFragment.DIALOG_OPENED_FROM_SOMEWHERE_ELSE
        }
        ShakeBottomDialogFragment.show(
            fragmentManager = supportFragmentManager,
            openPlace = actionType
        )
    }

    private fun openLocationPermission() = try {
        TedOnActivityResult.with(this)
            .setIntent(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            .setListener { _, _ ->
                if (activityViewModel.isLocationEnabled()) {
                    showBumpDialog(false)
                }
            }
            .startActivityForResult()
    } catch (e: ActivityNotFoundException) {
        Timber.e(e)
    }

    private fun sendScreenshotTakenEventToFragment() {
        if (!::currentFragment.isInitialized) return
        (currentFragment as? ScreenshotTakenListener)?.onScreenshotTaken()
    }

    private fun openAppSettingsForResult() = try {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        TedOnActivityResult.with(this)
            .setIntent(intent)
            .setListener { _, _ ->
                if (activityViewModel.isLocationEnabled()) {
                    showBumpDialog(false)
                }
            }
            .startActivityForResult()
    } catch (e: Exception) {
        Timber.e(e)
    }

    private fun showLocationEnableDialog() {
        checkAppRedesigned(
            isRedesigned = { showMeeraLocationEnableDialog() },
            isNotRedesigned = { showOldLocationEnableDialog() }
        )
    }

    private fun showMeeraLocationEnableDialog() {
        meeraGeoPopupDialog?.dismiss()
        meeraGeoPopupDialog = MeeraGeoPopupDialog(
            activity = this,
            onEnableGeoClicked = {
                activityViewModel.handleAction(ActActions.HandleLocationEnableClicked)
            },
            onGeoPopupAction = { _, _ ->
            }
        )
        meeraGeoPopupDialog?.show()
    }

    private fun showOldLocationEnableDialog() {
        geoPopupDialog?.dismiss()
        geoPopupDialog = GeoPopupDialog(
            activity = this,
            onEnableGeoClicked = {
                activityViewModel.handleAction(ActActions.HandleLocationEnableClicked)
            },
            onGeoPopupAction = { _, _ ->
            }
        )
        geoPopupDialog?.show()
    }

    private fun deeplinkActionOpenSpecificPost(id: Long, deeplinkOrigin: DeeplinkOrigin?) {
        val postOrigin =
            deeplinkOrigin?.let(DestinationOriginEnum::fromDeeplinkOrigin) ?: DestinationOriginEnum.DEEPLINK
        addFragment(
            PostFragmentV2(null),
            LIGHT_STATUSBAR,
            Arg(ARG_FEED_POST_ID, id),
            Arg(ARG_POST_ORIGIN, postOrigin)
        )
    }

    private fun deeplinkActionOpenSpecificMoment(id: Long) {
        logMomentOpen(
            userId = null,
            openedWhere = AmplitudePropertyMomentScreenOpenWhere.DEEPLINK,
            viewedEarly = null
        )
        addFragment(
            ViewMomentPositionFragment(),
            COLOR_STATUSBAR_BLACK_NAVBAR,
            Arg(ARG_MOMENT_ID, id)
        )
    }

    private fun deeplinkActionPeople() {
        returnToTargetFragment(
            position = 0,
            smoothScroll = false,
            readyCallback = {
                (mainFragment as? MainFragment)?.onClickPeoples()
            }
        )
    }

    private fun deeplinkActionOpenSpecificCommunity(id: Long) {
        addFragment(CommunityRoadFragment(), LIGHT_STATUSBAR, Arg(ARG_GROUP_ID, id.toInt()))
        activityViewModel.handleAction(ActActions.LogOpenCommunityFromDeeplink)
    }

    private fun deeplinkActionOpenSpecificUser(id: Long) {
        addFragmentIgnoringAuthCheck(
            UserInfoFragment(), COLOR_STATUSBAR_LIGHT_NAVBAR,
            Arg(ARG_USER_ID, id),
            Arg(ARG_TRANSIT_FROM, AmplitudePropertyWhere.DEEPLINK.property)
        )
    }

    private fun deeplinkActionUserMakeVip() {
        val accType = activityViewModel.getUserAccountType()
        if (accType == INetworkValues.ACCOUNT_TYPE_PREMIUM
            || accType == INetworkValues.ACCOUNT_TYPE_VIP
        ) {
            addFragment(UpdateStatusFragment(), LIGHT_STATUSBAR)
        } else {
            addFragment(
                FragmentUpgradeToVipNew(),
                LIGHT_STATUSBAR,
                Arg(ARG_UPGRADE_TO_VIP_GOLD, true)
            )
        }
    }

    private fun deeplinkActionUserReferral() {
        activityViewModel.logFriendInviteTap(FriendInviteTapProperty.DEEPLINK)
        addFragment(ReferralFragment(), LIGHT_STATUSBAR)
    }

    private fun deeplinkActionNotificationSettings() {
        addFragment(PushNotificationsSettingsFragment(), LIGHT_STATUSBAR)
    }

    private fun deeplinkActionPrivacy() {
        checkAppRedesigned(
            isRedesigned = {},
            isNotRedesigned = { addFragment(PrivacyFragmentNew(), LIGHT_STATUSBAR) }
        )
    }

    private fun deeplinkActionOpenAboutMe() {
        addFragment(GridProfilePhotoFragment(), LIGHT_STATUSBAR)
    }

    private fun deeplinkActionProfileEdit() {
        activityViewModel.logProfileEditTap()
        val arg = Arg(ARG_CALLED_FROM_PROFILE, true)

        checkAppRedesigned(
            isRedesigned = {},
            isNotRedesigned = {
                addFragment(UserPersonalInfoFragment(), LIGHT_STATUSBAR, arg)
            }
        )
    }

    private fun deeplinkActionUserSettings() {
        checkAppRedesigned(
            isRedesigned = {},
            isNotRedesigned = {
                addFragment(ProfileSettingsFragmentNew(), LIGHT_STATUSBAR)
            }
        )
    }

    private fun deeplinkActionOpenMap() {
        navigatorViewPager.setCurrentItem(0, true)
        (mainFragment as? MainFragment)?.showMainRoadMap()
    }

    fun deeplinkActionMyCommunity() {
        navigatorViewPager.setCurrentItem(0, true)
        (mainFragment as? MainFragment)?.selectGroups()
    }

    private fun deeplinkActionNotifications() {
        navigatorViewPager.setCurrentItem(0, true)
        (mainFragment as? MainFragment)?.onClickEvent()
    }

    private fun deeplinkActionSearchUser() {
        addFragment(SearchMainFragment(), LIGHT_STATUSBAR)
    }

    private fun deeplinkActionRoadNewPersonalPost() {
        addFragment(
            AddMultipleMediaPostFragment(), LIGHT_STATUSBAR,
            Arg(ARG_SHOW_MEDIA_GALLERY, false),
            Arg(ARG_TO_PERSONAL_ROAD, true)
        )
    }

    private fun deeplinkActionRoadNewPost() {
        addFragment(AddMultipleMediaPostFragment(), LIGHT_STATUSBAR, Arg(ARG_SHOW_MEDIA_GALLERY, false))
    }

    private fun deeplinkActionGoOwnProfileTab() {
        navigatorViewPager.setCurrentItem(0, true)
        (mainFragment as? MainFragment)?.onClickProfile()
    }

    private fun deeplinkActionChats() {
        navigatorViewPager.setCurrentItem(0, true)
        (mainFragment as? MainFragment)?.onClickChat()
    }

    private fun deeplinkActionChat(userId: Long) {
        addFragment(
            ChatFragmentNew(),
            LIGHT_STATUSBAR,
            Arg(
                IArgContainer.ARG_CHAT_INIT_DATA, ChatInitData(
                    initType = ChatInitType.FROM_PROFILE,
                    userId = userId
                )
            )
        )
    }

    private fun initVideoCache() {
        leastRecentlyUsedCacheEvictor = ExoPlayerCache.leastRecentlyUsedCacheEvictor
        exoDatabaseProvider = ExoPlayerCache.exoDatabaseProvider
        simpleCache = ExoPlayerCache.simpleCache
    }

    fun getNavigationAdapter(): NavigatorAdapter? {
        return if (!this::navigatorAdapter.isInitialized) {
            null
        } else {
            navigatorAdapter
        }
    }

    fun triggerShakeEventChanged(isFromSensor: Boolean) {
        shakeEventDelegateUi?.triggerShowShakeOrLocationDialog(isFromSensor)
    }

    fun showShakeOrLocationDialogByClick() {
        shakeEventDelegateUi?.showShakeOrLocationDialogByClick()
    }

    /**
     * Handle new push events
     * */
    private fun initObservers() {
        setUpRoomViewEventObserver()
        activityViewModel.liveNewMessageEvent.observe(this, Observer { message ->
            if (roomIdsSet.contains(message.roomId))
                return@Observer
            message?.attachment?.let { messageAttachment ->
                val description: String
                val title = message.creator?.name ?: ""
                when (messageAttachment.type) {
                    TYPING_TYPE_POST, TYPING_TYPE_EVENT -> {
                        Timber.e("Bazaleev TYPING_TYPE_POST $message")
                        description = getString(R.string.push_new_post_message)
                    }

                    TYPING_TYPE_TEXT -> {
                        Timber.e("Bazaleev TYPING_TYPE_TEXT $message")
                        description = message.content
                    }

                    TYPING_TYPE_AUDIO -> {
                        Timber.e("Bazaleev TYPING_TYPE_AUDIO $message")
                        description = getString(R.string.you_have_new_audio_message)
                    }

                    TYPING_TYPE_IMAGE -> {
                        Timber.e("Bazaleev TYPING_TYPE_IMAGE $message")
                        description = if (message.content.isEmpty())
                            getString(R.string.you_have_new_photo)
                        else message.content
                    }

                    TYPING_TYPE_GIF -> {
                        Timber.e("Bazaleev TYPING_TYPE_GIF $message")
                        description = if (message.content.isEmpty())
                            getString(R.string.you_have_new_photo)
                        else message.content
                    }

                    else -> {
                        Timber.e("Bazaleev default $message")
                        description = message.content
                    }
                }
                if (message.creator?.avatarSmall.isNullOrEmpty())
                    message.creator?.avatarSmall = null
                showNewPushMessage(
                    PushObjectNew(
                        title,
                        description,
                        message.roomId,
                        CHAT_INCOMING_MESSAGE,
                        message.creator,
                        null
                    )
                )
            }
        })
        // TODO: ОБРАБОТКА Захода с другог устройства и refresh token
        // Observe ViewEvents
        lifecycleScope.launchWhenStarted {
            activityViewModel.liveViewEvents.collect { viewEvent ->
                when (viewEvent) {
                    Logout -> {
                        logOutWithDelegate()
                    }

                    is ReadyForRestartAppAfterLogout -> {
                        if (viewEvent.isReady.not()) Timber.d("Unsubscribe didn't pass")
                        App.get()?.restartApp()
                    }

                    is OnSupportUserIdReady -> {
                        onAddFragment(
                            fragment = ChatFragmentNew(),
                            isLightStatusBar = com.meera.core.common.LIGHT_STATUSBAR,
                            mapArgs = hashMapOf(
                                IArgContainer.ARG_CHAT_INIT_DATA to ChatInitData(
                                    initType = ChatInitType.FROM_PROFILE,
                                    userId = viewEvent.userId
                                )
                            )
                        )
                    }

                    OnSocketError -> handleSocketError()
                    OnCheckServiceConnection -> checkServiceConnection()
                    OnBindSignallingService -> signalingServiceConnectionWrapper.get().bindService(this@Act)
                    OnPreferencesReady -> {
                        activityViewModel.handleAction(ActActions.GetRooms)
                    }

                    is CommunityChanges -> handleCommunityChanges(viewEvent)
                    is TryToRegisterShakeByPrivacySetting ->
                        handleNeedToRegisterShakeUiAction(viewEvent.shakeRegistered)

                    is NavigateToAppSettingsUiEffect -> openAppSettingsForResult()
                    is NavigateLocationSettingsUiEffect -> openLocationPermission()
                    is OnAddReaction -> if (viewEvent.isFromBubble.not()) lightVibrate()
                    is SendScreenshotTakenEventToFragment -> sendScreenshotTakenEventToFragment()
                    else -> Unit
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            activityViewModel.holidayViewEvent.collect { typeEvent ->
                handleViewEvent(typeEvent)
            }
        }

        observeDeeplinkActions()

        activityViewModel.liveEvent.observe(this) { event ->
            reactionBubbleViewController?.onEvent(event)
        }
    }

    private fun handleCommunityChanges(viewEvent: CommunityChanges) =
        communtyChangesController?.handleCommunityListEvents(viewEvent.communityListEvents)

    private fun setUpRoomViewEventObserver() {
        activityViewModel.liveRoomsViewEvent.flowWithLifecycle(lifecycle).onEach { event ->
            when (event) {
                is ChatRoomsViewEvent.OnNavigateToChatEvent -> if (::currentFragment.isInitialized && currentFragment is ChatFragmentNew) {
                    val chatInitData =
                        currentFragment.arguments?.getParcelable<ChatInitData>(IArgContainer.ARG_CHAT_INIT_DATA)
                    if (chatInitData?.roomId != event.roomData.roomId) {
                        goToChatScreenFromPush(event.roomData)
                    }
                } else {
                    goToChatScreenFromPush(event.roomData)
                }

                else -> {}
            }
        }.launchIn(lifecycleScope)
    }

    private fun handleViewEvent(event: HolidayViewEvent) {
        when {
            event is ShowBirthdayDialogEvent -> {
                val type = if (event.isBirthdayToday) {
                    BirthdayBottomDialogFragment.ACTION_TODAY_IS_BIRTHDAY
                } else {
                    BirthdayBottomDialogFragment.ACTION_YESTERDAY_IS_BIRTHDAY
                }
                showBirthdayDialog(
                    actionType = type,
                    dismissListener = {
                        activityViewModel.handleAction(ActActions.UpdateBirthdayDialogShown)
                    }
                )
            }

            event is OnHolidayReady && event.needToShowHoliday && event.isHolidayIntroduced -> {
                activityViewModel.handleAction(ActActions.ShowCalendarIfNot)
            }

            event is OnHolidayReady && event.needToShowHoliday -> {
                if (!isHolidayIntroDialogShown) {
                    dialogNavigator?.showHolidayDialog()
                    isHolidayIntroDialogShown = true
                }
            }

            event is ShowShakeDialogUiEffect -> {
                triggerShakeEventChanged(true)
            }

            event is TryToRegisterShakeEvent -> {
                val fragment = getNavigationAdapter()?.getListOfFragments()?.lastOrNull() ?: return
                val isAllowToRegisterIfDialogNotShown = shakeEventDelegateUi?.isAllowToRegisterShakeByDialog(fragment)
                    ?: return
                if (event.isNeedToRegisterShake
                    && fragment.isAllowToRegisterShakeInCurrentScreen()
                    && isAllowToRegisterIfDialogNotShown
                ) {
                    activityViewModel.handleAction(ActActions.TryToRegisterShakeEvent)
                } else {
                    activityViewModel.handleAction(ActActions.UnregisterShakeEventListener)
                }
            }

            event is ConnectShakeSocketUiEffect -> {
                shakeEventDelegateUi?.connectWebSocket()
            }
        }
    }

    private fun handleNeedToRegisterShakeUiAction(needToRegisterShake: Boolean) {
        val lastFragment = getNavigationAdapter()?.getListOfFragments()?.lastOrNull() ?: return
        val currentFragment = if (lastFragment is MainFragment) {
            lastFragment.currentFragment
        } else {
            lastFragment
        } ?: return
        val isAllowToRegisterIfDialogNotShown = shakeEventDelegateUi?.isAllowToRegisterShakeByDialog(currentFragment)
            ?: return
        if (currentFragment is ShakeRegisterUiHandler) {
            currentFragment.registerShake()
            return
        }
        if (needToRegisterShake && currentFragment.isAllowToRegisterShakeInCurrentScreen()
            && isAllowToRegisterIfDialogNotShown
        ) {
            activityViewModel.handleAction(ActActions.TryToRegisterShakeEvent)
        } else {
            activityViewModel.handleAction(ActActions.UnregisterShakeEventListener)
        }
    }

    private fun observeDeeplinkActions() {
        activityViewModel.liveDeeplinkActions.observe(this) { event ->
            when (event) {
                is DeeplinkActionViewEvent.HandleDeepLink -> {
                    handleFeatureDeepLink(event.deeplink)
                }

                is DeeplinkActionViewEvent.ParseError -> {
                    anonymousEnter()
                }

                is DeeplinkActionViewEvent.NotAuthorized -> {
                    anonymousEnter()
                }
            }
        }
    }

    private fun anonymousEnter() {
        resetNavigatorToMainRoadScreen()
        createNewAdapter()
    }

    fun showAppHintV2(hint: Hint, onTap: () -> Unit = {}) {
        val childCount = rootLayoutActivity?.childCount ?: return
        if (childCount > 1) {
            return
        }

        lifecycleScope.launchWhenResumed {
            val job = launch {
                hintView = View.inflate(this@Act, hint.layout, null) as ConstraintLayout

                hintView?.findViewById<TextView>(R.id.hint_text)?.text = getString(hint.text)

                hintView?.findViewById<LinearLayout>(R.id.layout_hint)?.setOnClickListener {
                    rootLayoutActivity?.removeView(hintView)
                    onTap.invoke()
                }

                rootLayoutActivity?.addView(hintView)

                Timber.e("SHOW HINT V2:${hint.type}")
                delay(hint.visibleTimeSec * 1000)
                hintView?.let { hint -> rootLayoutActivity?.removeView(hint) }

                if (hint.isShowOneTime) {
                    Timber.e("MARK HINT asShow-V2:${hint.type}")
                    activityViewModel.handleAction(ActActions.MarkHintsAsShown(hint))
                }
            }

            hintJobs.add(job)
        }
    }

    fun hideHints() {
        Timber.e("HIDE Hints NEW")
        cancelHintsJob()

        if (hintView != null) {
            rootLayoutActivity?.removeView(hintView)
        }
        if (simpleHintView != null) {
            rootLayoutActivity?.removeView(simpleHintView)
        }
    }

    private fun cancelHintsJob() {
        hintJobs.forEach { job -> job.cancel() }
    }

    /**
     * Show new Push message
     * */
    private fun showNewPushMessage(pushObject: PushObjectNew) {
        notificationHelper.show(
            IActionContainer.ACTION_OPEN_CHAT,
            MESSENGER_BASE,
            pushObject
        )
    }

    /**
     * Screen orientation portrait
     */
    @SuppressLint("SourceLockedOrientationActivity")
    fun setOrientationPortrait() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    /**
     * Screen orientation Auto (portrait/landscape)
     */
    fun setOrientationAuto() {
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    /**
     * Set unread message + notifications
     */
    override fun onGetNavigationBar(navBar: NavigationBarViewContract) {
        activityViewModel.handleAction(ActActions.GetUnreadBadgeInfo)
        activityViewModel.handleAction(ActActions.RequestCounter)
        activityViewModel.handleAction(ActActions.SubscribeProfileNotification)
        activityViewModel.handleAction(ActActions.UpdatePeopleBadge)
        navBar.updateNavBarBasedOnHoliday()
        activityViewModel.liveUnreadNotificationCounter.observe(this) { count ->
            navBar.showUnreadEventsCounter(count > 0)
        }

        activityViewModel.liveUnsentMessageBadge.observe(this) {
            navBar.updateUnreadBadge(it)
        }

        activityViewModel.getTotalUnreadCounter().observe(this) { count ->
            val counter = if (count < 0) 0 else count
            navBar.updateChatCounter(counter, 0)
        }

        activityViewModel.liveProfileIndicator.observe(this, Observer {
            if (it != null) navBar.updateProfileIndicator(it)
        })
        activityViewModel.livePeopleBadge.observe(this) {
            navBar.updatePeopleBadge(it)
        }

        activityViewModel.liveUnreadNotificationBadge.observe(this) {
            navBar.showUnreadEventsCounter(it)
        }

        activityViewModel.liveNewEvent.observe(this) {
            navBar.showUnreadEventsCounter(true)
        }

        lifecycleScope.launchWhenStarted {
            dialogDismissListener.sharedFlow.collect { type ->
                if (type == DismissDialogType.HOLIDAY) {
                    activityViewModel.handleAction(ActActions.ShowCalendarIfNot)
                }
            }
        }

        (mainFragment as? MainFragment)?.let { mainFragment ->
            navBar.setListener(mainFragment)
        }
    }

    //Данный метод используется для отображения бейджа непрочитаннх уведомлений
    fun updateUnreadNotificationBadge(needToShow: Boolean) =
        activityViewModel.handleAction(ActActions.UpdateUnreadNotificationBadge(needToShow))

    fun handleAppVersion() {
        val isNeedToShowUpdateAppMark = BuildConfig.VERSION_NAME.needToUpdateStr(serverAppVersionName)
        activityViewModel.handleAction(ActActions.SetNeedToShowUpdateAppMark(isNeedToShowUpdateAppMark))
    }

    open fun onCallFinished() {
        shouldShowOnLockScreen(false)
        if (navigatorAdapter.getFragmentsCount() > 1) {
            navigatorViewPager.setCurrentItem(navigatorViewPager.currentItem - 1, true)
        } else {
            if (mainFragment == null) mainFragment = MainFragment()
            replaceFragmentNavigator(0, mainFragment!!, LIGHT_STATUSBAR)
        }
        callUiEventDispatcher.setEvent(CallUiEvent.DESTROYED)
    }

    /**F
     * Проблема с серым экраном при входящем звонке при закрытом приложении:
     * При поступлении пуша о входящем звонке создается [SignalingService], который
     * стартует [Act]. У [Act] запускается жизненный цикл [onCreate] [onStart] [onResume]
     * на выключенном экране. Далее экран включается командой [PowerManager.WakeLock.acquire]
     * и происходит повторное воспроизведение жизненного цикла.
     * Вызывается [onPause] [onStop] [onStart] [onResume]. Для того, чтобы подключение к WebSocket
     * не вызывалось дважды, выполняется проверка [isDisplayOff]. Тогда все работает как надо и подключение
     * к WebSocket выполняется только при включенном экране, но на устройствах Huawei и Xiaomi(некооторых) при таком
     * сценарии жизененный цикл не выполняется повторно. То-есть на Huawei вызывается
     * только [onCreate] [onStart] [onResume] при выключенном экране, сокеты не подключаются, и повторный вызов
     * [onPause] [onStop] [onStart] [onRonsesume] не выполняется, соответственно, экран звонка не появляется
     * и остается только серый экран.
     * */
    override fun onStart() {
        super.onStart()
        activityViewModel.handleAction(ActActions.StartReceivingLocationUpdates)

        isFirstConnection = true
        if (::currentFragment.isInitialized)
            currentFragment.onStartFragment()
        Timber.e("ON_START act ${isDisplayOff()}")
        connectSocket()
        wakeLock?.acquire()
        screenShotDetectorDelegate = ScreenShotDetectorDelegate(this) {
            onScreenshotTaken()
        }
    }

    fun connectSocket() {
        activityViewModel.handleAction(ActActions.ConnectSocket)
    }

    /** Deliberately clear all notifications when app moves to foreground
     * (and therefore clear notifications counter on app icon in launcher)
     * @see <a href="https://nomera.atlassian.net/wiki/spaces/NOM/pages/3087499275">Noomeera Notifications</a>
     * */
    override fun onResume() {
        super.onResume()
        clearNotifications()
        activityViewModel.handleAction(ActActions.TryToRegisterShakeEvent)
        screenShotDetectorDelegate?.startScreenshotDetection()
    }

    override fun onPause() {
        super.onPause()
        activityViewModel.handleAction(ActActions.UnregisterShakeEventListener)
        screenShotDetectorDelegate?.stopScreenshotDetection()
    }

    override fun onStop() {
        super.onStop()
        activityViewModel.handleAction(ActActions.StopReceivingLocationUpdates)

        Timber.d("ON-STOP")

        signalingServiceConnectionWrapper.get().isCallActive = false
        supportFragmentManager.fragments.forEach {
            if (it is CallFragment) {
                signalingServiceConnectionWrapper.get().isCallActive = true
            }
        }
        if (::currentFragment.isInitialized) {
            currentFragment.onStopFragment()
            activityViewModel.handleAction(ActActions.ShowAllPosts)
            currentFragment.onAppHidden()
        }
        if (!signalingServiceConnectionWrapper.get().isCallActive) {
            Timber.d("SOCKET START_DISCONNECT")
            activityViewModel.handleAction(ActActions.DisconnectWebSocket)
            signalingServiceConnectionWrapper.get().unbindService(this)
            shouldShowOnLockScreen(false)
        }

        shakeEventDelegateUi?.removeShakeUserIfShakeAdded()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionManager.PERMISSION_LOCATION_CODE
            && PermissionManager.isPermissionGranted(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ), permissions, grantResults
            )
        ) {
            activityViewModel.handleAction(ActActions.StartReceivingLocationUpdates)
        } else if (requestCode == PERMISSION_MEDIA_CODE) {
            permissionListener.forEach {
                it(requestCode, permissions, grantResults)
            }
        }
    }

    var permissionListener: MutableSet<((requestCode: Int, permissions: Array<String>, grantResults: IntArray) -> Unit)> =
        mutableSetOf()

    fun getRootView(): View? {
        var view = findViewById<View>(android.R.id.content).rootView
        // In some devices, root view is retrieved this way
        if (view == null)
            view = window.decorView.findViewById(android.R.id.content)
        return view
    }

    var isFirstConnection = true // при первом подключении не показываем плашку connected
    private fun checkServiceConnection() {
        lifecycleScope.launch(Dispatchers.IO) {
            signalingServiceConnectionWrapper.get().checkServiceConnection {
                withContext(Dispatchers.Main) {
                    showConnectionAlertIfNeeded()
                }
            }
        }
    }

    private fun showConnectionAlertIfNeeded() {
        if (authNavigator?.isAuthScreenOpen() != false) return
        if (!isFirstConnection) {
            NToast.with(this@Act)
                .text(getString(R.string.internet_connected))
                .typeSuccess()
                .show()
        } else {
            isFirstConnection = false
        }
    }

    private fun handleSocketError() {
        runOnUiThread {
            if (signalingServiceConnectionWrapper.get().connectionEstablished.get()) {
                signalingServiceConnectionWrapper.get().connectionEstablished.set(false)
            }
        }
    }

    private fun isDisplayOff(): Boolean {
        var isDisplayOff = false
        val displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager?
        displayManager?.displays?.forEach {
            if (it.state == Display.STATE_OFF) {
                isDisplayOff = true
            }
        }
        return isDisplayOff
    }

    private fun shouldShowOnLockScreen(shouldShow: Boolean) {
        if (shouldShow) {
            val wakelockFlags = PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.SCREEN_DIM_WAKE_LOCK
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(wakelockFlags, WAKELOCK_TAG)
            wakeLock?.acquire()
        } else {
            wakeLock?.release()
            wakeLock = null
        }
    }

    // Инициализируем навигатор фрагментов
    private fun initFragmentNavigator() {
        navigatorViewPager = findViewById(R.id.navigator_view_pager)
        navigatorAdapter = NavigatorAdapter(supportFragmentManager)
        navigatorViewPager.adapter = navigatorAdapter
        navigatorViewPager.offscreenPageLimit = 100
        var canRemoveFragment = false
        var sumPositionAndPositionOffset = 0.0f
        navigatorViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
                blockTouchEvent = state == ViewPager.SCROLL_STATE_SETTLING
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    if (canRemoveFragment) {
                        while ((navigatorAdapter.getFragmentsCount() - 1) > navigatorViewPager.currentItem) {
                            currentFragment.onStopFragment()
                            navigatorAdapter.removeLastFragment()
                            statusBarState.removeAt(statusBarState.size - 1)
                        }
                        setStatusBar()
                        if (navigatorAdapter.getFragmentsCount() == 1) {
                            if (navigatorAdapter.getListOfFragments().last() is MainFragment
                                && (navigatorAdapter.getListOfFragments()
                                    .last() as MainFragment).currentFragment is UserInfoFragment
                            )
                                ((navigatorAdapter.getListOfFragments()
                                    .last() as MainFragment).userInfoFragment as UserInfoFragment).onRefresh()


                            // после регистрации у MainFragment не вызывается
                            // onReturnTransitionFragment, вызываем принудительно
                            // для этого кейса https://nomera.atlassian.net/browse/BR-4041
                            val lastFragment: BaseFragment =
                                navigatorAdapter.getListOfFragments().last()

                            if (currentFragment is RegistrationContainerFragment &&
                                lastFragment != null && lastFragment is MainFragment
                            ) {

                                // делать каст lastFragment к MainFragment
                                // не нужно потому что, в условии проверка
                                // lastFragment is MainFragment
                                lastFragment.onReturnTransitionFragment()
                            }
                        }
                        setCurrentFragment()
                        setPreviousFragment()
                        currentFragment.onReturnTransitionFragment()
                        if (isSubscribeFloorFragment) {
                            activityViewModel.handleAction(ActActions.LogBack)
                            isSubscribeFloorFragment = false
                        } else {
                            activityViewModel.handleAction(ActActions.LogSwipeBack)
                        }

                    } else {
                        currentFragment.onOpenTransitionFragment()
                    }
                    currentFragment.onStartFragment()
                    previousFragment?.apply { onStopFragment() }
                    signalingServiceConnectionWrapper.get().isCallActive = false
                    supportFragmentManager.fragments.forEach {
                        if (it is CallFragment) {
                            signalingServiceConnectionWrapper.get().isCallActive = true
                        }
                    }
                } else if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                    currentFragment.onStartAnimationTransitionFragment()
                }
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                canRemoveFragment = position + positionOffset < sumPositionAndPositionOffset
                sumPositionAndPositionOffset = position + positionOffset
                getRootView()?.let { hideKeyboard(it) }
            }

            override fun onPageSelected(position: Int) {
                blockTouchEvent = false
            }
        })
    }

    fun setCurrentFragment() {
        if (::currentFragment.isInitialized && currentFragment
                !is AddMultipleMediaPostFragment && currentFragment !is MapFragment
        ) {
            getStatusToastViewController().hideStatusToast()
        }
        if (navigatorAdapter.getFragmentsCount() == 1 && navigatorAdapter.getListOfFragments()
                .last() is MainFragment
        ) {
            (navigatorAdapter.getListOfFragments()
                .last() as MainFragment).getCurrentFragmentIntoMain()?.let {
                currentFragment = it
            } ?: kotlin.run {
                currentFragment = navigatorAdapter.getListOfFragments().last()
            }
        } else {
            currentFragment = navigatorAdapter.getListOfFragments().last()
        }

        Timber.e("Set CURR Frag:$currentFragment")
        // Hack for gide road hints
        if (currentFragment !is MainPostRoadsFragment) {
            (mainFragment as? MainFragment)?.hideRoadHints()
        }
    }

    fun currentFragmentInitiated(): Boolean = ::currentFragment.isInitialized

    fun getCurrentFragment(): BaseFragment {
        return currentFragment
    }

    fun getCurrentFragmentFromAdapter(): BaseFragment? {
        return navigatorAdapter.getCurrentFragment()
    }

    fun getCurrentFragmentNullable(): BaseFragment? {
        return if (!this::currentFragment.isInitialized) {
            null
        } else {
            currentFragment
        }
    }

    fun setPreviousFragment() {
        if (navigatorAdapter.getFragmentsCount() > 1) {
            val fragment =
                navigatorAdapter.getListOfFragments()[navigatorAdapter.getFragmentsCount() - 2]
            previousFragment = if (fragment is MainFragment) {
                fragment.getCurrentFragmentIntoMain()
            } else {
                fragment
            }
        } else {
            previousFragment = null
        }
    }

    fun getPreviousFragment(): BaseFragment? {
        return previousFragment
    }

    fun isTopFragmentViewMedia(): Boolean {
        if (navigatorAdapter.getFragmentsCount() == 0) return false
        val last = (navigatorAdapter.getListOfFragments().last())
        return last is ViewVideoItemFragment || last is ViewMultimediaFragment
    }

    fun isCurrentFragmentOnTop(fragment: BaseFragment): Boolean {
        if (navigatorAdapter.getFragmentsCount() == 0) return false
        val topFragment = navigatorAdapter.getListOfFragments().last()
        return fragment == topFragment
    }

    // Добавляем фрагмент в Навигатор
    private fun addFragmentToNavigator(
        fragment: BaseFragment,
        statusBarState: Int,
        smoothScroll: Boolean = true,
        isNeedSetStatusBar: Boolean = true
    ) {
        if (fragment.isAdded)
            return

        if (navigatorAdapter.getFragmentsCount() > 0) {
            blockTouchEvent = true
        }

        navigatorAdapter.addFragment(fragment)
        activityViewModel.fragmentAdded()
        this.statusBarState.add(statusBarState)
        if (isNeedSetStatusBar) setStatusBar()
        doDelayed(20) {
            navigatorViewPager.setCurrentItem(navigatorViewPager.currentItem + 1, smoothScroll)
            setCurrentFragment()
            setPreviousFragment()
            if (navigatorAdapter.getFragmentsCount() == 1) {
                currentFragment.onOpenTransitionFragment()
                currentFragment.onStartFragment()
            }
        }
    }

    // TODO: isNotification - временное решение для хотфикса
    //  https://nomera.atlassian.net/browse/BR-15388 и https://nomera.atlassian.net/browse/BR-15559.
    //  Полноценное исправление багов требуется выполнить в https://nomera.atlassian.net/browse/BR-15565
    private fun openMainFragment(isNotification: Boolean) {
        if (activityViewModel.readAccessToken().isNotEmpty()) {
            when {
                isNotification -> mainFragment = MainFragment()
                !isNotification && mainFragment == null -> mainFragment = MainFragment()
            }
            if (navigatorAdapter.getListOfFragments().isEmpty())
                addFragmentToNavigator(mainFragment!!, LIGHT_STATUSBAR)
        } else {
            triggerDialogToShow()
        }
    }

    fun createNewAdapter() {
        runOnUiThread {
            navigatorAdapter.removeAllFragments()
            navigatorAdapter = NavigatorAdapter(supportFragmentManager)
            openMainFragment(true)
            navigatorViewPager.adapter = navigatorAdapter
        }
    }

    fun resetNavigatorToMainRoadScreen() {
        runOnUiThread {
            mainFragment = null
            navigatorAdapter.removeAllFragments()
            statusBarState.clear()
            initFragmentNavigator()
        }
    }

    // Реплэйс фрагмента (не затрагивает предыдущие)
    fun replaceFragmentNavigator(position: Int, fragment: BaseFragment, isLightStatusBar: Int) {
        navigatorAdapter.replaceFragment(position, fragment)
        changeStatusBarStateOnTargetScreen(position, isLightStatusBar)
        setStatusBar()
    }

    // Меняем цвет статусбара на текущем фрагменте
    fun changeStatusBarState(isLightStatusBar: Int) {
        statusBarState.set((statusBarState.size - 1), isLightStatusBar)
    }

    // Меняем цвет статусбара на предыдущем фрагменте
    fun changeStatusBarStateOnTargetScreen(fragmentPosition: Int, isLightStatusBar: Int) {
        try {
            statusBarState[fragmentPosition] = isLightStatusBar
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun returnToTargetFragment(
        position: Int,
        smoothScroll: Boolean,
        readyCallback: (() -> Unit)? = null
    ) {
        Handler(Looper.getMainLooper()).postDelayed({
            navigatorViewPager.setCurrentItem(position, smoothScroll)
            while ((navigatorAdapter.getFragmentsCount() - 1) > navigatorViewPager.currentItem) {
                navigatorAdapter.removeLastFragment()
                statusBarState.removeAt(statusBarState.size - 1)
            }
            setStatusBar()

            readyCallback?.invoke()
            setCurrentFragment()
        }, 200)
    }

    fun returnToFirstAndOpen(fragment: BaseFragment, vararg args: Arg<*, *>) {
        returnToTargetFragment(0, true) {
            addFragment(fragment, LIGHT_STATUSBAR, *args)
        }
    }

    fun getFragmentsCount(): Int {
        return navigatorAdapter.getFragmentsCount()
    }

    // Устанавливаем цвет статусбара
    fun setStatusBar() {
        if (statusBarState.isNotEmpty())
            when (statusBarState.last()) {
                LIGHT_STATUSBAR -> setLightStatusBar()
                LIGHT_STATUSBAR_NOT_TRANSPARENT -> setLightStatusBarNotTransparent()
                COLOR_STATUSBAR_BLACK_NAVBAR -> setColorStatusBar()
                COLOR_STATUSBAR_LIGHT_NAVBAR -> setColorStatusBarNavLight()
            }
    }

    //Отключаем Touch Screen во время открытия фрагмента
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return if (blockTouchEvent)
            true
        else {
            try {
                super.dispatchTouchEvent(ev)
            } catch (e: Exception) {
                e.printStackTrace()
                return false
            }

        }
    }

    // TODO: BR-17120 Выпилить в рамках тех.долга
    fun showBirthdayDialogDelayed() {
        activityViewModel.handleAction(ActActions.ShowBirthdayDialogDelayed)
    }

    fun showBirthdayDialog(
        actionType: String,
        dismissListener: (() -> Unit)? = null
    ) {
        val birthdayDialog = dialogNavigator?.showBirthdayDialog(actionType)
        dismissListener?.let { action ->
            birthdayDialog?.setOnDismissListener(action)
        }
    }

    fun setUserProfileTab() {
        (mainFragment as MainFragment).onClickProfile()
    }

    fun dismissAllDialogs(fragmentManager: FragmentManager?) {
        val fragments: List<Fragment> = fragmentManager?.fragments ?: return
        for (fragment in fragments) {
            if (fragment is DialogFragment) {
                val dialogFragment: DialogFragment = fragment
                dialogFragment.dismissAllowingStateLoss()
            }
            val childFragmentManager: FragmentManager = fragment.childFragmentManager
            dismissAllDialogs(childFragmentManager)
        }
    }

    public override fun onDestroy() {
        dialogNavigator = null
        this.unregisterReceiver(ringerModeBroadcastReceiver)
        tooltipViewController?.clear()
        signalingServiceConnectionWrapper.get().unbindService(this)
        app.removeRestartListener()
        super.onDestroy()
    }

    /**
     * Открыть экран требующий регистрации даже будучи неавторизованным
     */
    fun addFragmentIgnoringAuthCheck(
        fragment: BaseFragment,
        isLightStatusBar: Int,
        vararg args: Arg<*, *>
    ) {
        dismissAllDialogsAllowedToDismiss(supportFragmentManager)
        fragment.arguments = getBundle(*args)

        addFragmentToNavigator(fragment, isLightStatusBar)
    }

    override fun addFragment(fragment: BaseFragment, vararg args: Arg<*, *>) {
        addFragment(fragment, LIGHT_STATUSBAR, *args)
    }

    fun addMomentFragment(fragment: BaseFragment, isLightStatusBar: Int, vararg args: Arg<*, *>) {
        dismissAllDialogsAllowedToDismiss(supportFragmentManager)

        fragment.arguments = getBundle(*args)

        if (fragment.isNeedAuth() && authNavigator?.isAuthorized() == false) {
            needAuth {
                addFragmentToNavigator(
                    fragment = fragment,
                    statusBarState = isLightStatusBar,
                    isNeedSetStatusBar = false
                )
            }
        } else {
            addFragmentToNavigator(
                fragment = fragment,
                statusBarState = isLightStatusBar,
                isNeedSetStatusBar = false
            )
        }
    }

    fun addFragment(fragment: BaseFragment, isLightStatusBar: Int, arg: ScreenArgs) {
        dismissAllDialogsAllowedToDismiss(supportFragmentManager)
        fragment.arguments = arg.addTo(Bundle())

        addToNavigator(fragment, isLightStatusBar)
    }

    fun addFragment(fragment: BaseFragment, isLightStatusBar: Int, vararg args: Arg<*, *>) {
        dismissAllDialogsAllowedToDismiss(supportFragmentManager)

        fragment.arguments = getBundle(*args)

        addToNavigator(fragment, isLightStatusBar)
    }

    private fun addToNavigator(fragment: BaseFragment, isLightStatusBar: Int) {
        if (fragment.isNeedAuth() && authNavigator?.isAuthorized() == false) {
            needAuth {
                addFragmentToNavigator(fragment, isLightStatusBar)
            }
        } else {
            addFragmentToNavigator(fragment, isLightStatusBar)
        }
    }

    fun replaceFragment(
        position: Int,
        fragment: BaseFragment,
        isLightStatusBar: Int,
        vararg args: Arg<*, *>
    ) {
        fragment.arguments = getBundle(*args)
        replaceFragmentNavigator(position, fragment, isLightStatusBar)
        setCurrentFragment()
    }

    fun replaceFragment(
        position: Int,
        fragment: BaseFragment,
        isLightStatusBar: Int,
        arg: ScreenArgs
    ) {
        fragment.arguments = arg.addTo(Bundle())
        replaceFragmentNavigator(position, fragment, isLightStatusBar)
        setCurrentFragment()
    }

    override fun onAddFragment(screen: Screens) {
        navRouter.addScreen(
            navigationAdapter = NavigationRouterAdapterActImpl(this),
            screen = screen
        )
    }

    override fun onAddFragment(fragment: BaseFragment, isLightStatusBar: Int, mapArgs: Map<String, Any?>) {
        val args = mutableListOf<Arg<*, *>>()
        mapArgs.forEach { (k, v) -> args.add(Arg(k, v)) }
        addFragment(fragment, isLightStatusBar, *args.toTypedArray())
    }

    override fun onSetStatusBar() {
        setStatusBar()
    }

    override fun openPreviousViewPagerItem() {
        navigatorViewPager.setCurrentItem(navigatorViewPager.currentItem - 1, true)
    }

    override fun onBackPressed() {
        activityViewModel.handleAction(ActActions.LogBack)
        navigateBack(1)
    }

    fun navigateBack(rollOutCount: Int = 1) {
        if (rollOutCount > 1 && navigatorAdapter.getFragmentsCount() <= rollOutCount) {
            return
        }

        if (navigatorAdapter.getListOfFragments().isEmpty() || !::currentFragment.isInitialized) {
            finish()
            return
        }
        if (currentFragment is ContainerAvatarFragment) {
            (currentFragment as? ContainerAvatarFragment)?.onBackPressed()
            return
        }
        if (currentFragment is IOnBackPressed && rollOutCount == 1 &&
            (currentFragment as IOnBackPressed).onBackPressed()
        ) {
            return
        }
        if (currentFragment is ViewMomentFragment) {
            handleMomentsFragmentBackNavigation()
            return
        }
        reactionBubbleViewController?.hideReactionBubble()
        if (currentFragment is CallFragment) {
            (currentFragment as CallFragment).disconnect()
            if (navigatorAdapter.getFragmentsCount() > 1) {
                navigatorViewPager.setCurrentItem(
                    navigatorViewPager.currentItem - rollOutCount,
                    true
                )
            } else {
                if (mainFragment == null)
                    mainFragment = MainFragment()
                replaceFragmentNavigator(0, mainFragment!!, LIGHT_STATUSBAR)
            }
            return
        }
        if (navigatorAdapter.getFragmentsCount() > 1) {
            navigatorViewPager.setCurrentItem(navigatorViewPager.currentItem - rollOutCount, true)
        } else if (navigatorAdapter.getListOfFragments()[0] !is MainFragment
            && activityViewModel.readAccessToken().isNotEmpty()
        ) {
            if (mainFragment == null)
                mainFragment = MainFragment()
            replaceFragmentNavigator(0, mainFragment!!, LIGHT_STATUSBAR)
        } else {
            // https://nomera.atlassian.net/browse/BR-6004 при нажатии на кнопку назад не
            // закрывать приложение
            moveTaskToBack(true)
        }

        if (currentFragment is UserInfoFragment
            && previousFragment !is ViewMomentFragment
        ) {
            currentFragment.arguments?.let {
                if (it.getBoolean(ARG_OPEN_FROM_REACTIONS, false)) {
                    val entityId: Long = it.getLong(ReactionsStatisticsBottomSheetFragment.ARG_ENTITY_ID)
                    val entityType: ReactionsEntityType =
                        it.get(ReactionsStatisticsBottomSheetFragment.ARG_ENTITY_TYPE) as ReactionsEntityType
                    checkAppRedesigned(
                        isRedesigned = {
                            MeeraReactionsStatisticsBottomSheetFragment.getInstance(
                                entityId = entityId,
                                entityType = entityType
                            )
                                .show(supportFragmentManager)
                        },
                        isNotRedesigned = {
                            ReactionsStatisticsBottomSheetFragment.getInstance(
                                entityId = entityId,
                                entityType = entityType
                            )
                                .show(supportFragmentManager)
                        }
                    )

                }
            }
        }

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (::currentFragment.isInitialized && currentFragment is
                IOnKeyDown && (currentFragment as IOnKeyDown).onKeyDown(
                keyCode,
                event
            )
        ) {
            true
        } else
            super.onKeyDown(keyCode, event)
    }

    fun clearHolidayDialogShown() {
        isHolidayIntroDialogShown = false
    }

    /**
     * Вызывется когда тапаем на Уведомдение (срабатывает Pending intent)
     */
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        hideBubbleIfExist()
        if (intent?.isLauncher() == true) return
        initFragment(intent = intent)
    }

    private fun handleMomentsFragmentBackNavigation() {
        when ((currentFragment as ViewMomentFragment).getState()) {
            MomentsFragmentClosingAnimationState.IN_PROGRESS -> return
            MomentsFragmentClosingAnimationState.FINISHED -> removeMomentsFragmentFromNavigator()
            MomentsFragmentClosingAnimationState.NOT_STARTED ->
                (currentFragment as ViewMomentFragment).close(onFinishAction = { removeMomentsFragmentFromNavigator() })
        }
    }

    private fun removeMomentsFragmentFromNavigator() {
        if (navigatorAdapter.getFragmentsCount() > 1) {
            returnToTargetFragment(getFragmentsCount() - 2, true)
        } else {
            if (mainFragment == null)
                mainFragment = MainFragment()
            replaceFragmentNavigator(0, mainFragment!!, LIGHT_STATUSBAR)
        }
        setNavigationDefaultPageTransformer()
    }

    private fun initFragment(intent: Intent?) {
        val action = intent?.action
        val extras = intent?.extras
        if (action == null) return

        dismissAllDialogsAllowedToDismiss(supportFragmentManager)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
        when (action) {
            Intent.ACTION_MAIN -> openMainFragment(true)
            Intent.ACTION_VIEW -> tryHandleDeepLinks(intent)
            IActionContainer.ACTION_OPEN_MOMENT -> actionOpenMoment(extras)
            IActionContainer.ACTION_START_CALL -> actionStartCall(extras)
            IActionContainer.ACTION_OPEN_CHAT -> actionOpenChat(extras)
            IActionContainer.ACTION_OPEN_AUTHORIZATION -> actionOpenAuthorization()
            IActionContainer.ACTION_FRIEND_REQUEST -> {
                actionOpenFriendRequest()
            }

            IActionContainer.ACTION_FRIEND_CONFIRM -> {
                authNavigator?.onAuthStatusInitialized {
                    actionOpenUserProfile(extras)
                }
            }

            IActionContainer.ACTION_OPEN_GIFTS -> {
                actionOpenGifts(extras)
            }

            IActionContainer.ACTION_LEAVE_POST_COMMENTS -> {
                actionOpenPostComments(extras)
            }

            IActionContainer.ACTION_LEAVE_POST_COMMENT_REACTIONS -> {
                actionOpenPostCommentsWithReactions(extras)
            }

            IActionContainer.ACTION_REPLY_POST_COMMENTS -> {
                actionOpenPostComments(extras)
            }

            IActionContainer.ACTION_ADD_TO_GROUP_CHAT -> {
                actionOpenChat(extras)
            }

            IActionContainer.ACTION_OPEN_PEOPLE -> actionOpenPeople(
                extras?.getLong(ARG_USER_ID)
            )

            IActionContainer.ACTION_REQUEST_TO_GROUP -> {
                actionOpenGroupAdminFragment(extras)
            }

            IActionContainer.ACTION_OPEN_APP -> actionOpenApp()
            IActionContainer.ACTION_OPEN_MAP -> actionOpenOwnPagerItem(
                { (mainFragment as MainFragment?)?.onClickMap() },
                MAP_FRAGMENT
            )

            IActionContainer.ACTION_OPEN_POST -> actionOpenPost(extras)
            IActionContainer.ACTION_OPEN_POST_WITH_REACTIONS -> actionOpenPost(extras)
            IActionContainer.ACTION_OPEN_GALLERY_WITH_REACTIONS -> actionOpenGallery(extras)
            IActionContainer.ACTION_OPEN_EVENT -> actionOpenOwnPagerItem(
                { (mainFragment as MainFragment?)?.onClickEvent() },
                MAP_FRAGMENT
            )

            IActionContainer.ACTION_OPEN_OWN_PROFILE -> actionOpenOwnPagerItem(
                { (mainFragment as MainFragment?)?.onClickProfile() },
                USER_INFO_FRAGMENT
            )

            IActionContainer.ACTION_OPEN_OWN_CHAT_LIST -> actionOpenOwnPagerItem(
                { (mainFragment as MainFragment?)?.onClickChat() },
                ROOMS_FRAGMENT
            )

            IActionContainer.ACTION_OPEN_REFERAL -> actionOpenReferral()
            IActionContainer.ACTION_OPEN_BIRTHDAY_GIFTS -> {
                authNavigator?.onAuthStatusInitialized {
                    actionOpenUserProfile(extras)
                }
            }

            IActionContainer.ACTION_OPEN_BIRTHDAY_GROUP -> actionOpenBirthdayNotificationGroup(
                extras
            )

            IActionContainer.ACTION_SYSTEM_EVENT -> {
                actionOpenApp()
                val deeplinkWithOrigin = extras?.getString(ARG_URL, "")?.let {
                    FeatureDeepLink.addDeeplinkOrigin(it, DeeplinkOrigin.PUSH)
                }
                openLink(deeplinkWithOrigin)
            }

            IActionContainer.ACTION_OPEN_SELF_BIRTHDAY -> {
                initActionFromBirthdayPush()
            }

            IActionContainer.ACTION_OPEN_EVENT_ON_MAP -> actionOpenEventOnMap(extras)

            IActionContainer.ACTION_CALL_UNAVAILABLE -> {
                authNavigator?.onAuthStatusInitialized {
                    actionOpenUserProfile(extras)
                }
            }
        }
        handlePopup(action)
        intent.action = Intent.ACTION_MAIN
    }
    // TODO: BR-17120 Выпилить в рамках тех.долга
    /**
     * В данном методе мы хэндлим показ диалогов, которые имееют "наивысший приоритет":
     * 1.Показ диалога "Настройки приватности Друзей/Подписчиков/Подписок"
     * 2.Показ диалога "День рождения".
     */
    private fun handlePopup(action: String) {
        when {
            activityViewModel.isNeedShowFriendsFollowersPrivacyPopup() -> {
                dialogNavigator?.showFriendsSubscribersPrivacyDialog {
                    activityViewModel.handleAction(ActActions.ShowDialogIfBirthday)
                }
            }

            activityViewModel.isUserBirthdayToday()
                || activityViewModel.isUserBirthdayYesterday() -> checkBirthdayAction(action)
        }
    }

    // TODO: BR-17120 Выпилить в рамках тех.долга
    /**
     * Если это обычный вход в приложение, то проверяем флаг
     * [com.numplates.nomera3.presentation.view.utils.AppSettings.isNeedShowBirthdayDialog]
     */
    private fun checkBirthdayAction(actionType: String) {
        if (actionType != IActionContainer.ACTION_OPEN_SELF_BIRTHDAY)
            activityViewModel.handleAction(ActActions.ShowDialogIfBirthday)
    }

    private fun shakeFriendRequestsIsAdded(): Boolean {
        return getNavigationAdapter()?.getListOfFragments()?.lastOrNull() is ShakeFriendRequestsFragment
//            || getNavigationAdapter()?.getListOfFragments()?.lastOrNull() is MeeraShakeFriendRequestsFragment
    }

    private fun tryHandleDeepLinks(intent: Intent?) {
        if (mainFragment != null) {
            activityViewModel.handleAction(ActActions.HandleDeepLinks(intent))
        } else {
            mainFragment = MainFragment()
            if (navigatorAdapter.getListOfFragments().isEmpty()) {
                addFragmentToNavigator(mainFragment!!, LIGHT_STATUSBAR)
            }
            authNavigator?.onAuthStatusInitialized {
                activityViewModel.handleAction(ActActions.HandleDeepLinks(intent))
            }
        }
    }

    fun openLink(url: String?) {
        if (url.isNullOrEmpty()) return
        if (FeatureDeepLink.isAppDeeplink(url)) {
            openMainFragment(false)
            handleFeatureDeepLink(url)
        } else {
            handleUrlBySystem(Uri.parse(url))
        }
    }

    private fun handleUrlBySystem(url: Uri) {
        try {
            val launchBrowser = Intent(Intent.ACTION_VIEW, url)
            startActivity(launchBrowser)
        } catch (exception: Throwable) {
            FirebaseCrashlytics.getInstance().recordException(exception)
        }
    }

    private fun actionOpenReferral() {
        activityViewModel.logFriendInviteTap(FriendInviteTapProperty.PUSH)
        addFragment(ReferralFragment(), LIGHT_STATUSBAR)
    }

    private fun initActionFromBirthdayPush() {
        mainFragment?.let { mainFragment ->
            mainFragment.lifecycleScope.launchWhenCreated {
                navigatorViewPager.setCurrentItem(0, true)
                (mainFragment as MainFragment?)?.onClickRoad()
                activityViewModel.handleAction(ActActions.PushBirthdayState)
            }
        } ?: run {
            val fragment = MainFragment()
            runOnUiThread {
                if (navigatorAdapter.getListOfFragments().isNotEmpty()) {
                    navigatorAdapter.removeAllFragments()
                }
                addFragment(
                    fragment,
                    LIGHT_STATUSBAR,
                    Arg(ARG_SELECT_ON_MAIN_FRAGMENT, ROAD_FRAGMENT),
                    Arg(ARG_SHOW_BIRTHDAY, true)
                )
                mainFragment = fragment
            }
        }
    }

    private fun onScreenshotTaken() {
        activityViewModel.handleAction(ActActions.OnScreenshotTaken)
    }

    private fun actionOpenEventOnMap(extras: Bundle?) {
        addFragment(
            fragment = MapFragment(),
            isLightStatusBar = LIGHT_STATUSBAR,
            args = arrayOf(
                Arg(MapFragment.ARG_EVENT_POST_ID, extras?.get(ARG_FEED_POST_ID)),
                Arg(MapFragment.ARG_LOG_MAP_OPEN_WHERE, AmplitudePropertyWhere.OTHER)
            )
        )
    }

    private fun actionOpenPeople(userId: Long?) {
        if (mainFragment != null) {
            addFragment(
                fragment = PeoplesFragment(),
                isLightStatusBar = LIGHT_STATUSBAR,
                Arg(ARG_USER_ID, userId)
            )
        } else {
            mainFragment = MainFragment()
            runOnUiThread {
                if (navigatorAdapter.getListOfFragments().isNotEmpty()) {
                    runOnUiThread {
                        navigatorAdapter.removeAllFragments()
                    }
                }
                addFragment(
                    mainFragment!!,
                    LIGHT_STATUSBAR,
                    Arg(ARG_SELECT_ON_MAIN_FRAGMENT, PEOPLES_FRAGMENT),
                    Arg(ARG_USER_ID, userId)
                )
            }
        }
    }

    private fun actionOpenBirthdayNotificationGroup(extras: Bundle?) {
        val notificationId = extras?.getString(ARG_EVENT_GROUP_ID)
        addFragment(
            NotificationDetailFragment(), LIGHT_STATUSBAR,
            Arg(NotificationDetailFragment.NOTIFICATION_ID, notificationId)
        )
    }

    private fun actionOpenOwnPagerItem(action: () -> Unit, argValue: String) {
        if (mainFragment != null) {
            navigatorViewPager.setCurrentItem(0, true)
            action()
        } else {
            mainFragment = MainFragment()
            runOnUiThread {
                if (navigatorAdapter.getListOfFragments().isNotEmpty()) {
                    runOnUiThread {
                        navigatorAdapter.removeAllFragments()
                    }
                }
                addFragment(
                    mainFragment!!,
                    LIGHT_STATUSBAR,
                    Arg(ARG_SELECT_ON_MAIN_FRAGMENT, argValue)
                )
            }
        }
    }

    private fun actionOpenApp() {
        Timber.d("actionOpenApp")
        createNewAdapter()
    }

    private fun actionOpenGroupAdminFragment(extras: Bundle?) {
        Timber.d("actionOpenGroupAdminFragment")
        val groupId = extras?.getInt(ARG_GROUP_ID)
        groupId?.let { id ->
            addFragment(
                CommunityMembersContainerFragment(),
                LIGHT_STATUSBAR,
                Arg(ARG_GROUP_ID, id),
                Arg(ARG_IS_FROM_PUSH, true)
            )
        }
    }

    private fun actionStartCall(extras: Bundle?) {
        signalingServiceConnectionWrapper.get().actionStartCall(
            extras
        ) {
            shouldShowOnLockScreen(true)
        }
    }

    /** Current navigation implementation doesn't create backstack entries
     * when handling deeplink-like events (like opening a chat from push-notification).
     * We have to add MainFragment() manually.
     * */
    private fun actionOpenChat(extras: Bundle?) {
        val roomId = extras?.getLong(ARG_ROOM_ID) ?: return
        extras.getString(ARG_PUSH_EVENT_ID)?.let { pushEventId ->
            activityViewModel.handleAction(ActActions.MarkAsRead(pushEventId, false))
        }
        if (mainFragment == null) {
            mainFragment = MainFragment().also { mainFragment ->
                addFragment(mainFragment, LIGHT_STATUSBAR)
            }
        }
        activityViewModel.handleAction(ActActions.TriggerGoToChat(roomId))
    }

    private fun goToChatScreenFromPush(roomData: DialogEntity?) {
        addFragment(
            ChatFragmentNew(),
            LIGHT_STATUSBAR,
            Arg(IArgContainer.ARG_WHERE_CHAT_OPEN, AmplitudePropertyWhere.PUSH),
            Arg(
                IArgContainer.ARG_CHAT_INIT_DATA, ChatInitData(
                    initType = ChatInitType.FROM_LIST_ROOMS,
                    roomId = roomData?.roomId
                )
            )
        )
    }

    private fun actionOpenAuthorization() {
        if (mainFragment == null) {
            mainFragment = MainFragment().also { mainFragment ->
                addFragment(mainFragment, LIGHT_STATUSBAR)
            }
        }
        authNavigator?.navigateToPhone()
    }

    private fun actionOpenMoment(extras: Bundle?) {
        val momentItemId = extras?.getLong(ARG_MOMENT_ID) ?: return
        val momentAuthorId = extras.getLong(ARG_MOMENT_AUTHOR_ID)
        val momentCommentId = extras.getLong(ARG_COMMENT_ID, -1L)

        openUserMoments(
            userId = momentAuthorId,
            targetMomentId = momentItemId,
            commentID = momentCommentId,
            preventMomentAnimation = true
        )
    }

    private fun actionOpenUserProfile(extras: Bundle?) {
        addFragment(
            UserInfoFragment(), COLOR_STATUSBAR_LIGHT_NAVBAR,
            Arg(ARG_USER_ID, getUser(extras)?.userId),
            Arg(ARG_IS_FROM_PUSH, true)
        )
    }

    private fun actionOpenFriendRequest() {
        checkAppRedesigned(
            isRedesigned = {
//                addFragment(
//                    MeeraFriendsHostFragment(), LIGHT_STATUSBAR,
//                    Arg(ARG_IS_GOTO_INCOMING, true)
//                )
            },
            isNotRedesigned = {
                addFragment(
                    FriendsHostFragmentNew(), LIGHT_STATUSBAR,
                    Arg(ARG_IS_GOTO_INCOMING, true)
                )
            }
        )
    }

    private fun actionOpenGifts(extras: Bundle?) {
        val user = getUser(extras)
        val userName = user?.name
        val birthDate = user?.birthDate

        checkAppRedesigned(
            isRedesigned = {
//                addFragment(
//                    MeeraUserGiftsFragment(),
//                    LIGHT_STATUSBAR_NOT_TRANSPARENT,
//                    Arg(ARG_USER_NAME, userName),
//                    Arg(ARG_IS_FROM_PUSH, true),
//                    Arg(ARG_USER_DATE_OF_BIRTH, birthDate),
//                    Arg(ARG_GIFT_SEND_WHERE, AmplitudePropertyWhere.PUSH)
//                )
            },
            isNotRedesigned = {
                addFragment(
                    UserGiftsFragment(),
                    LIGHT_STATUSBAR,
                    Arg(ARG_USER_NAME, userName),
                    Arg(ARG_IS_FROM_PUSH, true),
                    Arg(ARG_USER_DATE_OF_BIRTH, birthDate),
                    Arg(ARG_GIFT_SEND_WHERE, AmplitudePropertyWhere.PUSH)
                )
            }
        )
    }

    private fun actionOpenPostComments(extras: Bundle?) {
        val postId = extras?.getLong(ARG_FEED_POST_ID)
        val commentId = extras?.getLong(ARG_COMMENT_ID)
        Timber.e("GET PUSH PostID: $postId")
        addFragment(
            PostFragmentV2(null), LIGHT_STATUSBAR,
            Arg(ARG_FEED_POST_ID, postId),
            Arg(ARG_COMMENT_ID, commentId ?: 0L),
            Arg(ARG_POST_ORIGIN, DestinationOriginEnum.PUSH)
        )
    }

    private fun actionOpenPostCommentsWithReactions(extras: Bundle?) {
        val postId = extras?.getLong(ARG_FEED_POST_ID)
        val commentId = extras?.getLong(ARG_COMMENT_ID)
        val lastReaction = extras?.getSerializable(ARG_COMMENT_LAST_REACTION) as? ReactionType
        Timber.e("GET PUSH Reaction to commentID: $commentId")
        addFragment(
            PostFragmentV2(null), LIGHT_STATUSBAR,
            Arg(ARG_FEED_POST_ID, postId),
            Arg(ARG_COMMENT_ID, commentId ?: 0L),
            Arg(ARG_COMMENT_LAST_REACTION, lastReaction),
            Arg(ARG_POST_ORIGIN, DestinationOriginEnum.PUSH)
        )
    }

    private fun actionOpenPost(extras: Bundle?) {
        val postId = extras?.getLong(ARG_FEED_POST_ID)
        val haveReactions = extras?.getBoolean(ARG_FEED_POST_HAVE_REACTIONS)
        val latestReactionType = extras?.getSerializable(ARG_POST_LATEST_REACTION_TYPE) as? ReactionType
        Timber.e("GET PUSH PostID: $postId")
        addFragment(
            PostFragmentV2(null), LIGHT_STATUSBAR,
            Arg(ARG_FEED_POST_ID, postId),
            Arg(ARG_FEED_POST_HAVE_REACTIONS, haveReactions),
            Arg(ARG_POST_ORIGIN, DestinationOriginEnum.PUSH),
            Arg(ARG_POST_LATEST_REACTION_TYPE, latestReactionType)
        )
    }

    private fun actionOpenGallery(extras: Bundle?) {
        val postId = extras?.getLong(ARG_FEED_POST_ID)
        Timber.e("GET PUSH PostID: $postId")
        checkAppRedesigned(
            isRedesigned = {
//                addFragment(
//                    MeeraProfilePhotoViewerFragment(), COLOR_STATUSBAR_BLACK_NAVBAR,
//                    Arg(IArgContainer.ARG_IS_PROFILE_PHOTO, false),
//                    Arg(IArgContainer.ARG_IS_OWN_PROFILE, true),
//                    Arg(IArgContainer.ARG_POST_ID, postId),
//                    Arg(IArgContainer.ARG_GALLERY_ORIGIN, DestinationOriginEnum.NOTIFICATIONS_REACTIONS),
//                )
            },
            isNotRedesigned = {
                addFragment(
                    ProfilePhotoViewerFragment(), COLOR_STATUSBAR_BLACK_NAVBAR,
                    Arg(IArgContainer.ARG_IS_PROFILE_PHOTO, false),
                    Arg(IArgContainer.ARG_IS_OWN_PROFILE, true),
                    Arg(IArgContainer.ARG_POST_ID, postId),
                    Arg(IArgContainer.ARG_GALLERY_ORIGIN, DestinationOriginEnum.NOTIFICATIONS_REACTIONS),
                )
            }
        )

    }

    fun resetMap() {
        mainFragment?.let {
            (it as MainFragment).resetMap()
        }
    }

    fun goToGroups(where: AmplitudePeopleWhereProperty = AmplitudePeopleWhereProperty.OTHER) {
        returnToTargetFragment(0, false)
        mainFragment?.let {
            (it as MainFragment).selectGroups(where)
        }
    }

    fun goToPeoples(amplitudePeopleWhereProperty: AmplitudePeopleWhereProperty) {
        returnToTargetFragment(
            position = 0,
            smoothScroll = false
        )
        mainFragment?.let { fragment ->
            (fragment as? MainFragment)?.selectPeople(amplitudePeopleWhereProperty)
        }
    }

    fun goToMainRoad() {
        returnToTargetFragment(0, false)
        mainFragment?.let {
            (it as MainFragment).onClickRoad()
        }
    }

    /**
     * this should be called after deleting images from gallery
     * */
    fun refreshGallery(path: String? = null) {
        activityViewModel.handleAction(ActActions.UpdateGalleryPost(path))
    }

    /*
    * Закрываем только те диалоги, которые можно.
    * CallsEnabledFragment - закрывать нельзя, так как он имеет системное значение
    * */
    private fun dismissAllDialogsAllowedToDismiss(manager: FragmentManager?) {
        val fragments: List<Fragment> = manager?.fragments ?: return
        for (fragment in fragments) {
            if (fragment is DialogFragment && isFragmentAllowedToDismiss(fragment)) {
                val dialogFragment: DialogFragment = fragment
                dialogFragment.dismissAllowingStateLoss()
            }
            if (fragment.isAdded) {
                val childFragmentManager: FragmentManager = fragment.childFragmentManager
                dismissAllDialogsAllowedToDismiss(childFragmentManager)
            }
        }
    }

    private fun isFragmentAllowedToDismiss(fragment: Fragment) = when (fragment) {
        is CallsEnabledFragment -> false
        is FriendsFollowersPrivacyFragment -> false
        is ForceUpdateDialog -> false
        else -> true
    }

    fun convert(number: Int, original: IntRange, target: IntRange): Int {
        val ratio = number.toFloat() / (original.endInclusive - original.start)
        return (ratio * (target.endInclusive - target.start)).toInt()
    }

    fun sendToMarket() {
        val marketIntent = Intent(Intent.ACTION_VIEW)
        marketIntent.data = Uri.parse(App.GOOGLE_PLAY_MARKET_URL)
        startActivity(marketIntent)
    }

    private fun startCallFromChat(companion: UserChat) {
        placeWebRtcCall(
            callUser = companion,
            isIncoming = false,
            callAccepted = null,
            roomId = null,
            messageId = null
        )
    }

    private fun hideBubbleIfExist() {
        ((getRootView() as? ViewGroup)
            ?.children
            ?.find { it is ReactionBubble } as? ReactionBubble)
            ?.hide()
    }

    val onCallDialogFragmentDismissedEvent = SingleLiveEvent<Unit>()

    private fun getUser(extras: Bundle?) = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
        extras?.getParcelable(ARG_USER_MODEL)
    else
        extras?.getParcelable(ARG_USER_MODEL, UserChat::class.java)

    companion object {

        const val TAG = "NOMERA_IAB"
        const val KEYGUARD_LOCK_TAG = "NUMAD:KeyguardLock"

        const val LIGHT_STATUSBAR = 0
        const val COLOR_STATUSBAR_BLACK_NAVBAR = 1
        const val COLOR_STATUSBAR_LIGHT_NAVBAR = 2
        const val LIGHT_STATUSBAR_NOT_TRANSPARENT = 3

        var simpleCache: SimpleCache? = null
        var leastRecentlyUsedCacheEvictor: LeastRecentlyUsedCacheEvictor? = null
        var exoDatabaseProvider: DatabaseProvider? = null

        private const val WAKELOCK_TAG = "NUMAD:CallWakelock"
    }
}
