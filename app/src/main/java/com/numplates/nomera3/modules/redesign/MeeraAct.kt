package com.numplates.nomera3.modules.redesign

import android.Manifest
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.POST_NOTIFICATIONS
import android.animation.Animator
import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.Display
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.children
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.google.android.exoplayer2.database.DatabaseProvider
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.gun0912.tedonactivityresult.TedOnActivityResult
import com.meera.core.base.BaseFragment
import com.meera.core.base.OnActivityInteractionCallback
import com.meera.core.common.PREF_NAME
import com.meera.core.extensions.doAsync
import com.meera.core.extensions.doOnUIThread
import com.meera.core.extensions.dp
import com.meera.core.extensions.getNavigationBarHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.lightVibrate
import com.meera.core.extensions.naveHost
import com.meera.core.extensions.needToUpdateStr
import com.meera.core.extensions.register
import com.meera.core.extensions.safeNavigate
import com.meera.core.extensions.vibrate
import com.meera.core.extensions.visible
import com.meera.core.navigation.Screens
import com.meera.core.permission.PERMISSION_MEDIA_CODE
import com.meera.core.permission.PermissionDelegate
import com.meera.core.preferences.AppSettings
import com.meera.core.preferences.PrefManagerImpl
import com.meera.core.utils.MeeraNotificationController
import com.meera.core.utils.clearNotifications
import com.meera.core.utils.files.FileManager
import com.meera.core.utils.files.FileUtilsImpl.Companion.MEDIA_TYPE_IMAGE
import com.meera.core.utils.files.FileUtilsImpl.Companion.MEDIA_TYPE_IMAGE_GIF
import com.meera.core.utils.files.FileUtilsImpl.Companion.MEDIA_TYPE_UNKNOWN
import com.meera.core.utils.files.FileUtilsImpl.Companion.MEDIA_TYPE_VIDEO
import com.meera.core.views.NavigationBarViewContract
import com.meera.core.views.TooltipViewController
import com.meera.db.models.dialog.UserChat
import com.meera.media_controller_api.MediaControllerFeatureApi
import com.meera.media_controller_api.model.MediaControllerCallback
import com.meera.media_controller_common.MediaControllerOpenPlace
import com.meera.media_controller_implementation.MediaControllerFeatureBuilder
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.state.PaddingState
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.snackbar.AvatarUiState
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.noomeera.nmrmediatools.NMRPhotoAmplitude
import com.noomeera.nmrmediatools.NMRVideoAmplitude
import com.numplates.nomera3.Act
import com.numplates.nomera3.ActActions
import com.numplates.nomera3.App
import com.numplates.nomera3.BuildConfig
import com.numplates.nomera3.FontSizeContextWrapper
import com.numplates.nomera3.LanguageContextWrapper
import com.numplates.nomera3.R
import com.numplates.nomera3.RestartCallback
import com.numplates.nomera3.ScreenShotDetectorDelegate
import com.numplates.nomera3.data.fcm.IPushInfo.CHAT_INCOMING_MESSAGE
import com.numplates.nomera3.data.fcm.NotificationHelper
import com.numplates.nomera3.data.fcm.PushObjectNew
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_AUDIO
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_EVENT
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_GIF
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_IMAGE
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_POST
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_TEXT
import com.numplates.nomera3.modules.appDialogs.ui.DialogNavigator
import com.numplates.nomera3.modules.appInfo.domain.usecase.GetAppInfoAsyncUseCase
import com.numplates.nomera3.modules.auth.ui.AuthNavigator
import com.numplates.nomera3.modules.auth.ui.IAuthStateObserver
import com.numplates.nomera3.modules.auth.ui.MeeraAuthNavigator
import com.numplates.nomera3.modules.auth.util.AuthStatusObserver
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.editor.AmplitudeEditor
import com.numplates.nomera3.modules.baseCore.helper.amplitude.editor.AmplitudeEditorParams
import com.numplates.nomera3.modules.baseCore.helper.notification.NotificationManagerImpl.Companion.MESSENGER_BASE
import com.numplates.nomera3.modules.bump.ui.ShakeEventDelegateUi
import com.numplates.nomera3.modules.bump.ui.ShakeEventUiHandler
import com.numplates.nomera3.modules.bump.ui.fragment.MeeraShakeBottomDialogFragment
import com.numplates.nomera3.modules.bump.ui.fragment.MeeraShakeFriendRequestsFragment
import com.numplates.nomera3.modules.chat.ui.ActivityInteractChatActions
import com.numplates.nomera3.modules.chat.ui.ActivityInteractionChatCallback
import com.numplates.nomera3.modules.common.ActivityToolsProvider
import com.numplates.nomera3.modules.communities.ui.CommunityChangesViewController
import com.numplates.nomera3.modules.complains.ui.ComplaintFlowInteraction
import com.numplates.nomera3.modules.complains.ui.ComplaintFlowInteractionDelegate
import com.numplates.nomera3.modules.exoplayer.ExoPlayerCache
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.maps.ui.geo_popup.MeeraGeoPopupDialog
import com.numplates.nomera3.modules.moments.show.presentation.MomentCreateViewController
import com.numplates.nomera3.modules.moments.show.presentation.MomentCreateViewModel
import com.numplates.nomera3.modules.reaction.ui.MeeraReactionBubbleViewController
import com.numplates.nomera3.modules.reaction.ui.ReactionBubbleViewController
import com.numplates.nomera3.modules.reaction.ui.custom.MeeraReactionBubble
import com.numplates.nomera3.modules.reaction.ui.util.ReactionPreloadUtil
import com.numplates.nomera3.modules.redesign.deeplink.MeeraDeeplinkParam
import com.numplates.nomera3.modules.redesign.deeplink.wrapDeeplink
import com.numplates.nomera3.modules.redesign.fragments.main.map.MainMapFragment
import com.numplates.nomera3.modules.redesign.util.MeeraAuthNavigation
import com.numplates.nomera3.modules.redesign.util.NavigationManager
import com.numplates.nomera3.modules.screenshot.ui.fragment.ScreenshotTakenListener
import com.numplates.nomera3.modules.upload.ui.MeeraStatusToastViewController
import com.numplates.nomera3.modules.upload.ui.StatusToastViewController
import com.numplates.nomera3.modules.upload.ui.viewmodel.UploadStatusViewModel
import com.numplates.nomera3.presentation.birthday.ui.MeeraBirthdayBottomDialogFragment
import com.numplates.nomera3.presentation.router.IActionContainer
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
import com.numplates.nomera3.presentation.viewmodel.UpdateScreenEvent
import com.numplates.nomera3.telecom.MeeraCallDelegate
import com.numplates.nomera3.telecom.MeeraCallFragment
import com.numplates.nomera3.telecom.MeeraSignalingServiceConnectionWrapper
import com.numplates.nomera3.telecom.RingerModeBroadcastReceiver
import com.numplates.nomera3.telecom.SignalingService
import dagger.Lazy
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class MeeraAct : AppCompatActivity(),

    // TODO: Убрать после редизана
    OnActivityInteractionCallback,

    IActionContainer,
    RingerModeBroadcastReceiver.OnRingerModeChangedListener,
    ActivityToolsProvider,
    IAuthStateObserver,
    RestartCallback,
    ActivityInteractionChatCallback,
    MeeraCallDelegate.OnActivityCallInteraction,
    MeeraCallFragment.CallsOnActivityInteraction,
    ComplaintFlowInteraction by ComplaintFlowInteractionDelegate() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var amplitudeEditor: AmplitudeEditor

    @Inject
    lateinit var fileManager: FileManager

    @Inject
    lateinit var getAppInfoAsyncUseCase: GetAppInfoAsyncUseCase

    @Inject
    lateinit var featureTogglesContainer: FeatureTogglesContainer

    @Inject
    lateinit var signalingServiceConnectionWrapper: Lazy<MeeraSignalingServiceConnectionWrapper>

    val app by lazy { application as App }

    @Deprecated("Use MeeraAuthNavigation")
    private var meeraAuthNavigator: MeeraAuthNavigator? = null

    private var meeraAuthNavigation: MeeraAuthNavigation? = null

    override fun onGetNavigationBar(navBar: NavigationBarViewContract) {
        // TODO: Убрать после редизана
    }

    override fun onAddFragment(screen: Screens) {
        // TODO: Убрать после редизана
    }

    override fun onAddFragment(fragment: BaseFragment, isLightStatusBar: Int, mapArgs: Map<String, Any?>) {
        // TODO: Убрать после редизана
    }

    override fun onSetStatusBar() {
        // TODO: Убрать после редизана
    }

    override fun openPreviousViewPagerItem() {
        // TODO: Убрать после редизана
    }

    var serverAppVersionName: String? = null
    var ringerModePublishSubject = PublishSubject.create<Int>()


    private val hintJobs = mutableListOf<Job>()

    private var hintView: ConstraintLayout? = null
    private var simpleHintView: ConstraintLayout? = null

    private var meeraGeoPopupDialog: MeeraGeoPopupDialog? = null

    private var ringerModeBroadcastReceiver: RingerModeBroadcastReceiver? = null
    private var rootLayoutActivity: ConstraintLayout? = null

    private var isHolidayIntroDialogShown = false

    // ID комнат, нужны для пушей
    val roomIdsSet = hashSetOf<Long>()

    val activityViewModel by viewModels<MainActivityViewModel>()

    private val momentCreateViewModel by viewModels<MomentCreateViewModel>()
    private val uploadStatusViewModel by viewModels<UploadStatusViewModel> { App.component.getViewModelFactory() }

    var wakeLock: PowerManager.WakeLock? = null

    private var dialogNavigator: DialogNavigator? = null
    private var communtyChangesController: CommunityChangesViewController? = null

    private var mediaController: MediaControllerFeatureApi? = null

    private var reactionBubbleViewController: MeeraReactionBubbleViewController? = null

    private var statusToastViewController: MeeraStatusToastViewController? = null
    private var tooltipViewController: TooltipViewController? = null

    private var momentCreateViewController: MomentCreateViewController? = null

    private var shakeEventDelegateUi: ShakeEventDelegateUi? = null

    private var screenShotDetectorDelegate: ScreenShotDetectorDelegate? = null

    private val viewModel by viewModels<MeeraActivityViewModel>()

    private var navHostFragment: NavHostFragment? = null

    private var callDelegate: MeeraCallDelegate? = null
    private var lastAction: String? = null
    private var lastExtras: Bundle? = null

    private var infoSnackbar: UiKitSnackBar? = null
    private val DEFAULT_SNACKBAR_BOTTOM_PADDING = 16.dp

    /**
     * Method call from chat and profile
     */
    override fun onStartCall(
        user: UserChat,
        isIncoming: Boolean,
        callAccepted: Boolean?,
        roomId: Long?,
        messageId: String?
    ) {
        callDelegate?.placeWebRtcCall(user, isIncoming, callAccepted, roomId, messageId)
    }

    private fun initNavigation() {
        navHostFragment = naveHost(R.id.main_fragment_container_view)
        val navController = navHostFragment?.findNavController()

        val navGraph = navController?.navInflater?.inflate(R.navigation.main_nav_graph) ?: return

        navController.graph = navGraph
    }

    @SuppressLint("MissingSuperCall")
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        hideBubbleIfExist()

        intent?.let {
            viewModel.emitDeeplinkCall(it)
        }
    }

    @SuppressLint("MissingSuperCall")
    @Deprecated("Deprecated in Java", ReplaceWith("onBackPressedDispatcher.onBackPressed()"))
    override fun onBackPressed() {
        onBackPressedDispatcher.onBackPressed()
    }

    override fun onCallFragmentAction(actions: MeeraCallFragment.CallFragmentActions) {
        callDelegate?.handleCallFragmentActions(actions)
    }

    private fun initMeeraCallDelegate() {
        callDelegate = MeeraCallDelegate(
            activity = this,
            signalingServiceConnectionWrapper = viewModel.getSignalingServiceConnectionWrapper(),
            socket = viewModel.getSocket()
        )
    }

    fun provideCallDelegate(): MeeraCallDelegate? = callDelegate

    companion object {
        const val TAG = "NOMERA_IAB"

        const val LIGHT_STATUSBAR = 0
        const val COLOR_STATUSBAR_BLACK_NAVBAR = 1
        const val COLOR_STATUSBAR_LIGHT_NAVBAR = 2

        var simpleCache: SimpleCache? = null
        var leastRecentlyUsedCacheEvictor: LeastRecentlyUsedCacheEvictor? = null
        var exoDatabaseProvider: DatabaseProvider? = null
    }

    //// ----

    private val shakeEventUiHandler = object : ShakeEventUiHandler {
        override fun showLocationDialog() {
            showLocationEnableDialog()
        }

        override fun showShakeDialog(isShowDialogByShake: Boolean) {
            showBumpDialog(isShowDialogByShake)
        }

        override fun showShakeFriendRequestsDialog() {
            (supportFragmentManager.fragments[supportFragmentManager.fragments.lastIndex] as? BottomSheetDialogFragment?)?.dismiss()
            findNavController(R.id.fragment_first_container_view).safeNavigate(
                R.id.action_global_meeraShakeFriendRequestsFragment
            )
        }
    }

    override fun getAuthenticationNavigator(): AuthNavigator {
        error("Ошибка аргумента. Невозможно получить authNavigator")
    }

    override fun getMeeraAuthenticationNavigator(): MeeraAuthNavigator {
        return meeraAuthNavigator ?: error("Ошибка аргумента. Невозможно получить meeraAuthNavigator")
    }

    override fun getMeeraAuthNavigation(): MeeraAuthNavigation {
        return meeraAuthNavigation ?: error("Ошибка аргумента. Невозможно получить MeeraAuthNavigation")
    }

    override fun getMediaControllerFeature(): MediaControllerFeatureApi {
        return mediaController ?: error("Ошибка аргумента. Невозможно получить mediaControllerFeature")
    }

    override fun getMomentsViewController(): MomentCreateViewController {
        return momentCreateViewController ?: error("Ошибка. Невозможно получить momentsViewController")
    }

    override fun getMeeraReactionBubbleViewController(): MeeraReactionBubbleViewController {
        return reactionBubbleViewController ?: error("Ошибка аргумента. Невозможно получить reactionViewController")
    }

    override fun getReactionBubbleViewController(): ReactionBubbleViewController {
        error("Ошибка аргумента. Невозможно получить reactionViewController")
    }

    override fun getStatusToastViewController(): StatusToastViewController {
        error("Ошибка аргумента. Невозможно получить statusToastViewController")
    }

    override fun getMeeraStatusToastViewController(): MeeraStatusToastViewController {
        return statusToastViewController ?: error("Ошибка аргумента. Невозможно получить statusToastViewController")
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
            statusToastViewController?.hideStatusToast()
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
            is ActivityInteractChatActions.SetAllowedSwipeDirection -> Unit
            is ActivityInteractChatActions.HideHints -> hideHints()
            is ActivityInteractChatActions.ShowFireworkAnimation -> showFireworkAnimation()
            is ActivityInteractChatActions.StartCall -> Unit
            is ActivityInteractChatActions.ShowAppHint -> showAppHintV2(action.hint)
            is ActivityInteractChatActions.HideAppHints -> hideHints()
            is ActivityInteractChatActions.OpenUserMoments -> Unit
            is ActivityInteractChatActions.OpenLink -> emitDeeplinkCall(action.url)
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

    fun openPhotoEditorForCommunity(uri: Uri, listener: MediaControllerCallback) {
        when (fileManager.getMediaType(uri)) {
            MEDIA_TYPE_IMAGE -> {
                logEditorOpen(
                    where = AmplitudePropertyWhere.COMMUNITY,
                    automaticOpen = true,
                    uri = uri
                )
                getMediaControllerFeature().open(uri, MediaControllerOpenPlace.Community, listener)
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


    // TODO: вынести в отдельный класс для моментов
    fun logEditorOpen(
        uri: Uri,
        where: AmplitudePropertyWhere = AmplitudePropertyWhere.OTHER,
        automaticOpen: Boolean = false
    ) = amplitudeEditor.editorOpenAction(
        where = where,
        automaticOpen = automaticOpen,
        type = amplitudeEditor.getEditorType(uri)
    )

    // TODO: вынести в отдельный класс для моментов
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

    // TODO: вынести в отдельный класс для моментов
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
        lastAction = super.getIntent().action
        lastExtras = super.getIntent().extras

        App.component.inject(this)

        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContentView(R.layout.meera_act)

        onCreateInitialize()

        initNavigation()
        viewModel.connect()

        setStatusBarColor()
        initMeeraCallDelegate()
        activityViewModel.writeFirstLogin()
        handlePopup(intent.action)
        emitDeeplinkCall(intent)

        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancelAll()
    }

    fun emitDeeplinkCall(intent: Intent) {
        viewModel.emitDeeplinkCall(intent)
    }

    fun emitDeeplinkCall(action: MeeraDeeplinkParam) {
        viewModel.emitDeeplinkCall(action)
    }

    fun emitDeeplinkCall(actionString: String?) {
        actionString ?: return
        actionString.wrapDeeplink()?.let {
            viewModel.emitDeeplinkCall(it)
        }
    }

    private fun handlePopup(action: String?) {
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

    fun showFromActionBirthDay() {
        activityViewModel.handleAction(ActActions.PushBirthdayState)
    }

    private fun checkBirthdayAction(actionType: String?) {
        if (actionType != IActionContainer.ACTION_OPEN_SELF_BIRTHDAY)
            activityViewModel.handleAction(ActActions.ShowDialogIfBirthday)
    }

    private fun onCreateInitialize() {
        rootLayoutActivity = findViewById(R.id.meera_root_layout_activity)
        activityViewModel.handleAction(ActActions.SubscribeEvent)
        activityViewModel.handleAction(ActActions.LoadPrivacySettings)
        app.setRestartCallbackListener(this)
        meeraAuthNavigator = MeeraAuthNavigator(this)
        meeraAuthNavigation = MeeraAuthNavigation(this)

        getHolidayInfo()
        app.hashSetRooms = roomIdsSet

        initObservers()
        activityViewModel.handleAction(ActActions.ObserveRefreshTokenRestService)
        initVideoCache()
        ringerModeBroadcastReceiver = RingerModeBroadcastReceiver(this)

        // регистрируем слушатель на выключение звука
        ringerModeBroadcastReceiver?.register(
            context = this,
            filter = RingerModeBroadcastReceiver.createIntentFilter()
        )

        requestLocationAndPostNotificationPermissions()
        ReactionPreloadUtil.preloadReactionsResources(this)
        activityViewModel.handleAction(ActActions.InitializeAvatarSDK(assets))
        initCommunityChangesController()
        initStatusToastController()
        initReactionController()
        initTooltipViewController()
        initAuthObserver()
        initShakeDelegateUi()
        initMediaFeature()
        initMomentsController()
    }

    private fun requestLocationAndPostNotificationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            PermissionDelegate(this, this).setPermissions(object : PermissionDelegate.Listener {
                override fun onGranted() {
                    requestPostNotificationPermission()
                }

                override fun onDenied() {
                    requestPostNotificationPermission()
                }
            }, ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)
        }
    }

    // TODO: FIX
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
        action: (suspend (isCancelledRegistration: Boolean) -> Unit)? = null
    ) {
        activityViewModel.logOutWithDelegate(
            isCancelledRegistration,
            unsubscribePush,
            changeServer
        ) { reg: Boolean, hol: Boolean ->
            isHolidayIntroDialogShown = hol
            activityViewModel.handleAction(ActActions.LoadSignupCountries)
            action?.invoke(reg)
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
        reactionBubbleViewController = MeeraReactionBubbleViewController()
        reactionBubbleViewController?.init(
            act = this as MeeraAct,
            handleAction = { action ->
                activityViewModel.handleAction(action)
            },
            timeOfDayReactionsFeatureToggle = featureTogglesContainer.timeOfDayReactionsFeatureToggle
        )
    }

    private fun initStatusToastController() {
        statusToastViewController = MeeraStatusToastViewController(
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
        showMeeraBumpDialog(isShowDialogByShake)
    }

    private fun showMeeraBumpDialog(isShowDialogByShake: Boolean) {
        val currentFragment = NavigationManager.getManager().topNavHost.childFragmentManager.fragments.lastOrNull()
        if (currentFragment is MeeraShakeFriendRequestsFragment) return
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
        val topNavHostCurrentFragment =
            NavigationManager.getManager().topNavHost.childFragmentManager.fragments.lastOrNull()
        val bottomNavHostCurrentFragment =
            NavigationManager.getManager().navHost.childFragmentManager.fragments.lastOrNull()
        val mainContainerFragment =
            supportFragmentManager.fragments.firstOrNull()?.childFragmentManager?.fragments?.firstOrNull()
        val mainContainerCurrentFragment = mainContainerFragment?.childFragmentManager?.fragments?.firstOrNull()
        if (mainContainerCurrentFragment is MainMapFragment) {
            (mainContainerCurrentFragment as ScreenshotTakenListener).onScreenshotTaken()
        }
        if (topNavHostCurrentFragment is ScreenshotTakenListener) {
            topNavHostCurrentFragment.onScreenshotTaken()
        } else if (bottomNavHostCurrentFragment is ScreenshotTakenListener) {
            bottomNavHostCurrentFragment.onScreenshotTaken()
        }
    }

    // TODO: FIX
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
        showMeeraLocationEnableDialog()
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

    private fun initVideoCache() {
        leastRecentlyUsedCacheEvictor = ExoPlayerCache.leastRecentlyUsedCacheEvictor
        exoDatabaseProvider = ExoPlayerCache.exoDatabaseProvider
        simpleCache = ExoPlayerCache.simpleCache
    }

    fun triggerShakeEventChanged(isFromSensor: Boolean) {
        shakeEventDelegateUi?.triggerShowShakeOrLocationDialog(isFromSensor)
    }

    fun showShakeOrLocationDialogByClick() {
        shakeEventDelegateUi?.showShakeOrLocationDialogByClick()
    }


    // TODO: Провести рефакторинг в методе initObservers()
    /**
     * Handle new push events
     * */
    private fun initObservers() {
//        setUpRoomViewEventObserver()
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
                        logOutWithDelegate {
                            lifecycleScope.launch {
                                NavigationManager.getManager().logOutDoPassAndSetState()
                            }
                        }
                    }

                    is ReadyForRestartAppAfterLogout -> {
                        if (viewEvent.isReady.not()) Timber.d("Unsubscribe didn't pass")
                        App.get()?.restartApp()
                    }

                    is OnSupportUserIdReady -> {}

                    OnSocketError -> {}
                    OnCheckServiceConnection -> {}
                    OnBindSignallingService -> {
                        signalingServiceConnectionWrapper.get().bindService(this@MeeraAct)
                    }

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
                    is UpdateScreenEvent -> checkAppVersions(viewEvent.appVerName)
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            activityViewModel.holidayViewEvent.collect { typeEvent ->
                handleViewEvent(typeEvent)
            }
        }


        activityViewModel.liveEvent.observe(this) { event ->
            reactionBubbleViewController?.onEvent(event)
        }
    }

    private fun checkAppVersions(appVerName: String?) {
        serverAppVersionName = appVerName
    }

    private fun handleCommunityChanges(viewEvent: CommunityChanges) =
        communtyChangesController?.handleCommunityListEvents(viewEvent.communityListEvents)

    // TODO: FIX
    private fun handleViewEvent(event: HolidayViewEvent) {
        when {
            event is ShowBirthdayDialogEvent -> {
                val type = if (event.isBirthdayToday) {
                    MeeraBirthdayBottomDialogFragment.ACTION_TODAY_IS_BIRTHDAY
                } else {
                    MeeraBirthdayBottomDialogFragment.ACTION_YESTERDAY_IS_BIRTHDAY
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

            // TODO: FIX
            event is TryToRegisterShakeEvent -> {}

            event is ConnectShakeSocketUiEffect -> {
                shakeEventDelegateUi?.connectWebSocket()
            }
        }
    }

    private fun handleNeedToRegisterShakeUiAction(needToRegisterShake: Boolean) {
        if (needToRegisterShake) {
            activityViewModel.handleAction(ActActions.TryToRegisterShakeEvent)
        } else {
            activityViewModel.handleAction(ActActions.UnregisterShakeEventListener)
        }
    }

    // TODO: FIX
    fun showAppHintV2(hint: Hint, onTap: () -> Unit = {}) {
        val childCount = rootLayoutActivity?.childCount ?: return
        if (childCount > 1) {
            return
        }

        lifecycleScope.launchWhenResumed {
            val job = launch {
                hintView = View.inflate(this@MeeraAct, hint.layout, null) as ConstraintLayout

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

    //Данный метод используется для отображения бейджа непрочитаннх уведомлений
    fun updateUnreadNotificationBadge(needToShow: Boolean) =
        activityViewModel.handleAction(ActActions.UpdateUnreadNotificationBadge(needToShow))

    fun handleAppVersion() {
        val isNeedToShowUpdateAppMark = BuildConfig.VERSION_NAME.needToUpdateStr(serverAppVersionName)
        activityViewModel.handleAction(ActActions.SetNeedToShowUpdateAppMark(isNeedToShowUpdateAppMark))
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

        MeeraNotificationController.shouldShowNotification.set(true)

        activityViewModel.handleAction(ActActions.StartReceivingLocationUpdates)
        activityViewModel.handleAction(ActActions.StartListeningSyncNotificationService)

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
        activityViewModel.handleAction(ActActions.StopListeningSyncNotificationService)
        activityViewModel.handleAction(ActActions.ShowAllPosts)

        Timber.d("ON-STOP")

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

    private var blockTouchEvent = false

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

    fun showBirthdayDialog(
        actionType: String,
        dismissListener: (() -> Unit)? = null
    ) {

        val birthdayDialog = MeeraBirthdayBottomDialogFragment.create(actionType)

        birthdayDialog.show(supportFragmentManager, MeeraBirthdayBottomDialogFragment.TAG)
        birthdayDialog.setFragmentResultListener(MeeraBirthdayBottomDialogFragment.TAG) { _, _ ->
            dismissListener?.invoke()
        }
    }

    public override fun onDestroy() {
        dialogNavigator = null
        this.unregisterReceiver(ringerModeBroadcastReceiver)
        tooltipViewController?.clear()
        app.removeRestartListener()
        statusToastViewController?.release()
        statusToastViewController = null
        super.onDestroy()
    }

    private fun onScreenshotTaken() {
        activityViewModel.handleAction(ActActions.OnScreenshotTaken)
    }

    fun sendToMarket() {
        val marketIntent = Intent(Intent.ACTION_VIEW)
        marketIntent.data = Uri.parse(App.GOOGLE_PLAY_MARKET_URL)
        startActivity(marketIntent)
    }

    internal fun hideBubbleIfExist() {
        ((getRootView() as? ViewGroup)
            ?.children
            ?.find { it is MeeraReactionBubble } as? MeeraReactionBubble)
            ?.hide()
    }

    fun setStatusBarColor(@ColorRes statusBarColor: Int = R.color.colorTransparent) {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        window.statusBarColor = ContextCompat.getColor(baseContext, statusBarColor)
    }

    fun showToastMessage(@StringRes messageRes: Int, isError: Boolean = false) {
        showToastMessage(getString(messageRes), isError)
    }

    private fun showToastMessage(messageString: String, isError: Boolean = false) = doOnUIThread {
        val avatarUiState = if (isError) {
            AvatarUiState.ErrorIconState
        } else {
            AvatarUiState.SuccessIconState
        }

        val view = this.getRootView() ?: return@doOnUIThread

        infoSnackbar = UiKitSnackBar.make(
            view = view,
            params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = messageString,
                    avatarUiState = avatarUiState,
                ),
                duration = BaseTransientBottomBar.LENGTH_SHORT,
                dismissOnClick = true,
                paddingState = PaddingState(
                    bottom = countMessageInputBottomPadding()
                )
            )
        )
        infoSnackbar?.show()
    }

    private fun countMessageInputBottomPadding(): Int {
        val bottomNavigationBarHeight =
            NavigationManager.getManager().toolbarAndBottomInteraction.getNavigationView().height
        val systemNavBarHeight = getNavigationBarHeight()
        return bottomNavigationBarHeight + systemNavBarHeight + DEFAULT_SNACKBAR_BOTTOM_PADDING
    }

}
