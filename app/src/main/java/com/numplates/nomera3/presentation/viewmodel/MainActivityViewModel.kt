package com.numplates.nomera3.presentation.viewmodel

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.meera.core.extensions.combineWith
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.fromJson
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.simpleName
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.toInt
import com.meera.core.network.HTTP_CODE_FORBIDDEN
import com.meera.core.network.HTTP_CODE_NOT_AUTHORIZED
import com.meera.core.network.websocket.WebSocketMainChannel
import com.meera.core.permission.ReadContactsPermissionProvider
import com.meera.core.preferences.AppSettings
import com.meera.core.utils.HardwareIdUtil
import com.meera.db.DataStore
import com.meera.db.models.dialog.LastMessage
import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.ADMIN_SUPPORT_ID_NAME
import com.numplates.nomera3.ActActions
import com.numplates.nomera3.App
import com.numplates.nomera3.BuildConfig
import com.numplates.nomera3.FALSE_INT
import com.numplates.nomera3.HTTPS_SCHEME
import com.numplates.nomera3.HTTP_SCHEME
import com.numplates.nomera3.NEED_SHOW_DIALOG
import com.numplates.nomera3.NOOMEERA_SCHEME
import com.numplates.nomera3.SHOW_FRIENDS_SUBSCRIBERS_POPUP
import com.numplates.nomera3.TRUE_VALUE
import com.numplates.nomera3.data.network.GetToken
import com.numplates.nomera3.data.network.core.INetworkValues.ACCOUNT_TYPE_REGULAR
import com.numplates.nomera3.data.network.core.ResponseError
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_AUDIO
import com.numplates.nomera3.data.newmessenger.response.ChatUsers
import com.numplates.nomera3.data.newmessenger.response.ResponseWrapperWebSock
import com.numplates.nomera3.data.newmessenger.response.UpdateUserResponse
import com.numplates.nomera3.domain.interactornew.AuthRefreshTokenUseCase
import com.numplates.nomera3.domain.interactornew.CheckMainFilterRecommendedUseCase
import com.numplates.nomera3.domain.interactornew.ClearMainRoadFilterRecommendedStateUseCase
import com.numplates.nomera3.domain.interactornew.DeliveredUseCase
import com.numplates.nomera3.domain.interactornew.GetFriendsSubscribersPopupPrivacyUseCase
import com.numplates.nomera3.domain.interactornew.GetMainFilterSettingsUseCase
import com.numplates.nomera3.domain.interactornew.GetRoomDataUseCase
import com.numplates.nomera3.domain.interactornew.GetUserBirthdayDialogShownUseCase
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.domain.interactornew.NotificationCounterUseCase
import com.numplates.nomera3.domain.interactornew.SetAdminSupportIdUseCase
import com.numplates.nomera3.domain.interactornew.StartSyncContactsUseCase
import com.numplates.nomera3.domain.interactornew.StopSyncContactsUseCase
import com.numplates.nomera3.domain.interactornew.UpdateBirthdayShownUseCase
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.appInfo.data.entity.CurrentInfo
import com.numplates.nomera3.modules.appInfo.data.entity.Settings
import com.numplates.nomera3.modules.appInfo.domain.usecase.GetAppInfoAsyncUseCase
import com.numplates.nomera3.modules.auth.domain.AuthAuthenticateAnonymouslyUseCase
import com.numplates.nomera3.modules.auth.domain.AuthInitUseCase
import com.numplates.nomera3.modules.auth.domain.AuthIsAuthorizedUseCase
import com.numplates.nomera3.modules.auth.domain.AuthLogoutUseCase
import com.numplates.nomera3.modules.auth.util.LogoutDelegate
import com.numplates.nomera3.modules.auth.util.isAuthorizedUser
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.baseCore.domain.repository.AmplitudeRepository
import com.numplates.nomera3.modules.baseCore.domain.repository.AmplitudeShakeAnalyticRepository
import com.numplates.nomera3.modules.baseCore.helper.HolidayInfoHelper
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyCandyCount
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyReactionType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhence
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereCommunityOpen
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereReaction
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudeMoment
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudePropertyMomentScreenOpenWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudePropertyMomentWhose
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeProfile
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeProfileEditTapProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.FriendInviteTapAnalytics
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.FriendInviteTapProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.AmplitudePropertyReactionWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.AmplitudeReactions
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.AmplitudeReactionsParams
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.getAmplitudePostReactionName
import com.numplates.nomera3.modules.baseCore.helper.amplitude.shake.AmplitudeShakeHowProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.shake.AmplitudeShakeWhereProperty
import com.numplates.nomera3.modules.baseCore.helper.notification.NotificationManager
import com.numplates.nomera3.modules.baseCore.ui.location.LocationEnableProvider
import com.numplates.nomera3.modules.bump.domain.entity.ShakeEvent
import com.numplates.nomera3.modules.bump.domain.usecase.GetNeedToRegisterShakeUseCase
import com.numplates.nomera3.modules.bump.domain.usecase.ObserveRegisterShakeEventUseCase
import com.numplates.nomera3.modules.bump.domain.usecase.ObserveShakePrivacySetting
import com.numplates.nomera3.modules.bump.hardware.ShakeEventListener
import com.numplates.nomera3.modules.chat.data.DialogApproved
import com.numplates.nomera3.modules.chat.domain.usecases.CacheCompanionUserForChatInitUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.ListenSocketStatusUseCase
import com.numplates.nomera3.modules.chat.drafts.domain.GetAllDraftsUseCase
import com.numplates.nomera3.modules.chat.drafts.ui.DraftsUiMapper
import com.numplates.nomera3.modules.chat.helpers.resendmessage.ResendNotificationUtil
import com.numplates.nomera3.modules.chat.helpers.resolveMessageType
import com.numplates.nomera3.modules.chat.helpers.toRoomLastMessage
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.domain.ClearMediaKeyboardStickerPacksUseCase
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.domain.GetMediaKeyboardStickersUseCase
import com.numplates.nomera3.modules.chatrooms.domain.interactors.RoomsInteractor
import com.numplates.nomera3.modules.chatrooms.domain.usecase.GetRoomsUseCase
import com.numplates.nomera3.modules.communities.data.states.CommunityListEvents
import com.numplates.nomera3.modules.communities.domain.usecase.CommunityListEventsUseCase
import com.numplates.nomera3.modules.communities.domain.usecase.DeleteCommunityUseCase
import com.numplates.nomera3.modules.communities.domain.usecase.DeleteCommunityUseCaseParams
import com.numplates.nomera3.modules.communities.domain.usecase.InitializeAvatarSdkUseCase
import com.numplates.nomera3.modules.communities.ui.entity.CommunityConstant
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.feed.domain.usecase.LoadAndCacheReferralInfoUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.SetSubPostsRequestedInSession
import com.numplates.nomera3.modules.feed.ui.entity.toAmplitudePropertyWhence
import com.numplates.nomera3.modules.holidays.data.entity.HolidayVisitsEntity
import com.numplates.nomera3.modules.holidays.domain.interactor.HolidayDailyInteractorImpl
import com.numplates.nomera3.modules.holidays.domain.usecase.GetHolidayParams
import com.numplates.nomera3.modules.holidays.ui.calendar.HolidayCalendarBottomDialog
import com.numplates.nomera3.modules.holidays.ui.entity.HolidayInfo
import com.numplates.nomera3.modules.holidays.ui.entity.HolidayVisits
import com.numplates.nomera3.modules.maps.domain.usecase.StartReceivingLocationUpdatesUseCase
import com.numplates.nomera3.modules.maps.domain.usecase.StopReceivingLocationUpdatesUseCase
import com.numplates.nomera3.modules.moments.show.data.MomentItemDto
import com.numplates.nomera3.modules.newroads.data.ISensitiveContentManager
import com.numplates.nomera3.modules.newroads.data.PostsRepository
import com.numplates.nomera3.modules.newroads.featureAnnounce.data.DeeplinkOrigin
import com.numplates.nomera3.modules.newroads.featureAnnounce.data.FeatureDeepLink
import com.numplates.nomera3.modules.notifications.domain.usecase.MarkAsReadNotificationParams
import com.numplates.nomera3.modules.notifications.domain.usecase.MarkAsReadNotificationsUseCase
import com.numplates.nomera3.modules.notifications.service.SyncNotificationService
import com.numplates.nomera3.modules.peoples.domain.usecase.NeedShowPeopleBadgeUseCase
import com.numplates.nomera3.modules.peoples.domain.usecase.ObserveNeedShowPeopleBadgeUseCase
import com.numplates.nomera3.modules.reaction.data.ReactionType
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity
import com.numplates.nomera3.modules.reaction.domain.MomentDeletedException
import com.numplates.nomera3.modules.reaction.domain.repository.ReactionRepository
import com.numplates.nomera3.modules.reaction.domain.usecase.AddReactionUseCase
import com.numplates.nomera3.modules.reaction.domain.usecase.RemoveReactionUseCase
import com.numplates.nomera3.modules.reaction.ui.data.MeeraReactionSource
import com.numplates.nomera3.modules.reaction.ui.data.ReactionSource
import com.numplates.nomera3.modules.registration.domain.LoadSignupCountriesUseCase
import com.numplates.nomera3.modules.registration.ui.AuthFinishListener
import com.numplates.nomera3.modules.registration.ui.FirebasePushSubscriberDelegate
import com.numplates.nomera3.modules.screenshot.domain.usecase.GetShareScreenshotEnabledUseCase
import com.numplates.nomera3.modules.upload.domain.usecase.post.UpdateGalleryUseCase
import com.numplates.nomera3.modules.user.domain.usecase.GetUserDateOfBirthUseCase
import com.numplates.nomera3.modules.user.ui.utils.UserBirthdayUtils
import com.numplates.nomera3.modules.userprofile.domain.maper.toChatInitUserProfile
import com.numplates.nomera3.modules.userprofile.domain.usecase.GetProfileUseCase
import com.numplates.nomera3.modules.userprofile.profilestatistics.data.entity.SlidesListResponse
import com.numplates.nomera3.modules.userprofile.profilestatistics.domain.usecase.SetProfileStatisticsParams
import com.numplates.nomera3.modules.userprofile.profilestatistics.domain.usecase.SetProfileStatisticsSlidesUseCase
import com.numplates.nomera3.modules.usersettings.domain.models.PrivacySettingModel
import com.numplates.nomera3.modules.usersettings.domain.usecase.GetSettingsUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.SetSettingsUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.SettingsParams
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum
import com.numplates.nomera3.presentation.utils.makeEntity
import com.numplates.nomera3.presentation.view.navigator.NavigationActionType
import com.numplates.nomera3.presentation.view.navigator.NavigationListener
import com.numplates.nomera3.presentation.view.utils.RemoteConfigs
import com.numplates.nomera3.presentation.view.utils.apphints.Hint
import com.numplates.nomera3.presentation.view.utils.apphints.HintManager
import com.numplates.nomera3.presentation.view.utils.eventbus.busevents.RxEventsJava
import com.numplates.nomera3.presentation.viewmodel.viewevents.ChatRoomsViewEvent
import com.numplates.nomera3.presentation.viewmodel.viewevents.DeeplinkActionViewEvent
import com.numplates.nomera3.presentation.viewmodel.viewevents.SingleLiveEvent
import com.numplates.nomera3.telecom.MessageType
import com.numplates.nomera3.telecom.SignalingGetIceResponse
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Response
import org.phoenixframework.Payload
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainActivityViewModel : BaseViewModel() {

    val disposables = CompositeDisposable()

    private val lastMessages = hashSetOf<String>()

    @Inject
    lateinit var remoteConfigs: RemoteConfigs

    @Inject
    lateinit var dataStore: Lazy<DataStore>

    @Inject
    lateinit var websocketChannel: Lazy<WebSocketMainChannel>

    @Inject
    lateinit var listenSocketStatusUseCase: ListenSocketStatusUseCase

    @Inject
    lateinit var getRoomsUseCase: GetRoomsUseCase

    @Inject
    lateinit var refreshTokenUseCase: Lazy<AuthRefreshTokenUseCase>

    @Inject
    lateinit var gson: Lazy<Gson>

    @Inject
    lateinit var appSettings: Lazy<AppSettings>

    @Inject
    lateinit var hintManager: Lazy<HintManager>

    @Inject
    lateinit var repository: Lazy<PostsRepository>

    @Inject
    lateinit var resendMessagesNotificationUtil: Lazy<ResendNotificationUtil>

    @Inject
    lateinit var authInitUseCase: Lazy<AuthInitUseCase>

    @Inject
    lateinit var roomDataUseCase: Lazy<GetRoomDataUseCase>

    @Inject
    lateinit var isAuthorizedUserUseCase: Lazy<AuthIsAuthorizedUseCase>

    @Inject
    lateinit var authLogoutUseCase: Lazy<AuthLogoutUseCase>

    @Inject
    lateinit var amplitudeHelper: Lazy<AmplitudeRepository>

    @Inject
    lateinit var amplitudeMoment: Lazy<AmplitudeMoment>

    @Inject
    lateinit var setProfileStatisticsSlidesUseCase: Lazy<SetProfileStatisticsSlidesUseCase>

    @Inject
    lateinit var holidayInfoHelper: Lazy<HolidayInfoHelper>

    @Inject
    lateinit var holidayInfoInteractor: Lazy<HolidayDailyInteractorImpl>

    @Inject
    lateinit var getSettingsUseCase: Lazy<GetSettingsUseCase>

    @Inject
    lateinit var notificationManager: Lazy<NotificationManager>

    @Inject
    lateinit var getAppInfoUseCase: Lazy<GetAppInfoAsyncUseCase>

    @Inject
    lateinit var getAllDraftsUseCase: GetAllDraftsUseCase

    @Inject
    lateinit var updateBirthdayShownUseCase: Lazy<UpdateBirthdayShownUseCase>

    @Inject
    lateinit var userBirthdayUtils: Lazy<UserBirthdayUtils>

    @Inject
    lateinit var getUserBirthdayDialogShownUseCase: Lazy<GetUserBirthdayDialogShownUseCase>

    @Inject
    lateinit var setAdminSupportIdUseCase: SetAdminSupportIdUseCase

    @Inject
    lateinit var getUserDateOfBirthUseCase: Lazy<GetUserDateOfBirthUseCase>

    @Inject
    lateinit var getFriendsSubscribersPopupPrivacyUseCase: GetFriendsSubscribersPopupPrivacyUseCase

    @Inject
    lateinit var loadSignupCountriesUseCase: LoadSignupCountriesUseCase

    @Inject
    lateinit var loadAndCacheReferralInfoUseCase: LoadAndCacheReferralInfoUseCase

    @Inject
    lateinit var authenticatorDelegate: FirebasePushSubscriberDelegate

    @Inject
    lateinit var authLoginAnonymousUseCase: AuthAuthenticateAnonymouslyUseCase

    @Inject
    lateinit var startReceivingLocationUpdatesUseCase: StartReceivingLocationUpdatesUseCase

    @Inject
    lateinit var stopReceivingLocationUpdatesUseCase: StopReceivingLocationUpdatesUseCase

    @Inject
    lateinit var deliveredUseCase: DeliveredUseCase

    @Inject
    lateinit var draftsMapper: DraftsUiMapper

    @Inject
    lateinit var markEventAsReadUseCase: MarkAsReadNotificationsUseCase

    @Inject
    lateinit var notificationCounterUseCase: NotificationCounterUseCase

    @Inject
    lateinit var updateGalleryPostUseCase: Lazy<UpdateGalleryUseCase>

    @Inject
    lateinit var communityChangesUseCase: CommunityListEventsUseCase

    @Inject
    lateinit var deleteCommunityUseCase: DeleteCommunityUseCase

    @Inject
    lateinit var shakeEventListener: Lazy<ShakeEventListener>

    @Inject
    lateinit var observeRegisterShakeEventUseCase: Lazy<ObserveRegisterShakeEventUseCase>

    @Inject
    lateinit var initializeAvatarSdkUseCase: InitializeAvatarSdkUseCase

    @Inject
    lateinit var roomsInteractor: RoomsInteractor

    @Inject
    lateinit var locationEnableProvider: Lazy<LocationEnableProvider>

    @Inject
    lateinit var getNeedToRegisterShakeUseCase: Lazy<GetNeedToRegisterShakeUseCase>

    @Inject
    lateinit var hardwareId: Lazy<HardwareIdUtil>

    @Inject
    lateinit var clearMediaKeyboardStickersUseCase: ClearMediaKeyboardStickerPacksUseCase

    @Inject
    lateinit var getMediaKeyboardStickersUseCase: GetMediaKeyboardStickersUseCase

    @Inject
    lateinit var clearMainRoadFilterRecommendedStateUseCase: ClearMainRoadFilterRecommendedStateUseCase

    @Inject
    lateinit var getMainFilterSettingsUseCase: GetMainFilterSettingsUseCase

    @Inject
    lateinit var featureTogglesContainer: Lazy<FeatureTogglesContainer>

    @Inject
    lateinit var observeShakePrivacySetting: Lazy<ObserveShakePrivacySetting>

    @Inject
    lateinit var authFinishListener: AuthFinishListener

    @Inject
    lateinit var amplitudeShakeAnalytic: AmplitudeShakeAnalyticRepository

    @Inject
    lateinit var getUserUidUseCase: GetUserUidUseCase

    @Inject
    lateinit var readContactsPermissionProvider: Lazy<ReadContactsPermissionProvider>

    @Inject
    lateinit var startSyncContactsUseCase: Lazy<StartSyncContactsUseCase>

    @Inject
    lateinit var stopSyncContactsUseCase: Lazy<StopSyncContactsUseCase>

    @Inject
    lateinit var setSettingsUseCase: Lazy<SetSettingsUseCase>

    @Inject
    lateinit var reactionRepository: Lazy<ReactionRepository>

    @Inject
    lateinit var addReactionUseCase: Lazy<AddReactionUseCase>

    @Inject
    lateinit var removeReactionUseCase: Lazy<RemoveReactionUseCase>

    @Inject
    lateinit var amplitudeReactions: Lazy<AmplitudeReactions>

    @Inject
    lateinit var checkMainFilterRecommendedUseCase: CheckMainFilterRecommendedUseCase

    @Inject
    lateinit var needShowPeopleBadgeUseCase: Lazy<NeedShowPeopleBadgeUseCase>

    @Inject
    lateinit var observeNeedShowPeopleBadgeUseCase: Lazy<ObserveNeedShowPeopleBadgeUseCase>

    @Inject
    lateinit var getProfileUserCase: Lazy<GetProfileUseCase>

    @Inject
    lateinit var cacheCompanionUserUseCase: Lazy<CacheCompanionUserForChatInitUseCase>

    @Inject
    lateinit var friendInviteTapAnalytics: FriendInviteTapAnalytics

    @Inject
    lateinit var profileAnalytics: AmplitudeProfile

    @Inject
    lateinit var navigationListener: NavigationListener

    @Inject
    lateinit var setSubPostsRequestedInSession: SetSubPostsRequestedInSession

    @Inject
    lateinit var getShareScreenshotEnabledUseCase: GetShareScreenshotEnabledUseCase

    @Inject
    lateinit var analyticsInteractor: AnalyticsInteractor

    @Inject
    lateinit var logoutDelegate: LogoutDelegate

    @Inject
    lateinit var syncNotificationService: SyncNotificationService

    val liveUnsentMessageBadge = MutableLiveData<Boolean>()
    val liveNewMessageEvent = MutableLiveData<MessageEntity>()
    val liveNewEvent = MutableLiveData<Boolean>()
    val liveUnreadNotificationBadge = MutableLiveData<Boolean>()
    val liveProfileIndicator = MutableLiveData<Boolean>()
    val livePeopleBadge = MutableLiveData<Boolean>()
    val liveDeeplinkActions = MutableLiveData<DeeplinkActionViewEvent>()


    private val _liveViewEvents = MutableSharedFlow<MainActivityViewEvent>()
    val liveViewEvents: SharedFlow<MainActivityViewEvent> = _liveViewEvents

    private val _holidayViewEvent = MutableSharedFlow<HolidayViewEvent>()
    val holidayViewEvent: SharedFlow<HolidayViewEvent> = _holidayViewEvent

    private val _holidaysFlow = MutableSharedFlow<HolidayVisits>()
    val holidaysFlow: SharedFlow<HolidayVisits> = _holidaysFlow

    private val _liveRoomsViewEvent = MutableSharedFlow<ChatRoomsViewEvent>()
    val liveRoomsViewEvent = _liveRoomsViewEvent.asSharedFlow()

    private val _liveUnreadNotificationCounter = MutableLiveData<Int>()
    val liveUnreadNotificationCounter = _liveUnreadNotificationCounter

    private val _onFeatureTogglesLoaded = MutableLiveData<Unit>()
    val onFeatureTogglesLoaded: LiveData<Unit> = _onFeatureTogglesLoaded

    private var isBackPres = false

    val liveEvent = SingleLiveEvent<ReactionEvent>()

    init {
        App.component.inject(this)
        auth()
        initializeAuthentication()
        stopChatResendProgress()
        showUnsentMessagesNotification()
        logFirstTimeOpenApp()
        notificationManager.get().deleteChannelsIfUpdatedApp()
        getAppInfo()
        loadStickers()
        observeAuthFinish()
        loadSignupCountries()
        loadAndCacheReferralInfo()
        subsсribeFbToken()
        observeCommunityEvents()
        initShakeObservers()
        initSessionCounter()
        observePeopleBadgeChanged()
    }

    private fun onShowReactionBubble() = amplitudeHelper.get().reactionPanelOpen()

    fun writeFirstLogin() = appSettings.get().writeFirstLogin(false)

    private fun addReaction(
        reactionSource: ReactionSource,
        currentReactionList: List<ReactionEntity>,
        reaction: ReactionType,
        reactionsParams: AmplitudeReactionsParams?,
        isFromBubble: Boolean,
    ) {
        sendAmplitudeSelect(
            reactionSource = reactionSource,
            selectedReactionType = reaction,
            reactionsParams = reactionsParams
        )
        viewModelScope.launch {
            try {
                addReactionUseCase.get().execute(reactionSource, reaction)
            } catch (exception: Exception) {
                proceedException(exception)
            }
        }
        reactionRepository.get().addReaction(
            reactionSource = reactionSource,
            reactionType = reaction,
            reactionList = currentReactionList
        )

        emitAddReactionEvent(isFromBubble)
    }

    private fun addReactionMeera(
        reactionSource: MeeraReactionSource,
        currentReactionList: List<ReactionEntity>,
        reaction: ReactionType,
        reactionsParams: AmplitudeReactionsParams?,
        isFromBubble: Boolean,
    ) {
        sendAmplitudeSelectMeera(
            reactionSource = reactionSource,
            selectedReactionType = reaction,
            reactionsParams = reactionsParams
        )
        viewModelScope.launch {
            try {
                addReactionUseCase.get().executeMeera(reactionSource, reaction)
            } catch (exception: Exception) {
                proceedException(exception)
            }
        }
        reactionRepository.get().addReactionMeera(
            reactionSource = reactionSource,
            reactionType = reaction,
            reactionList = currentReactionList
        )

        emitAddReactionEvent(isFromBubble)
    }

    private fun emitAddReactionEvent(isFromBubble: Boolean) = viewModelScope.launch {
        _liveViewEvents.emit(OnAddReaction(isFromBubble))
    }

    fun navigateToTechSupport() {
        viewModelScope.launch {
            val supportUserId = appSettings.get().supportUserId ?: return@launch
            val supportUser = runCatching {
                getProfileUserCase.get().invoke(supportUserId)
            }.getOrNull()?.toChatInitUserProfile()
            cacheCompanionUserUseCase.get().invoke(supportUser)
            _liveViewEvents.emit(OnSupportUserIdReady(supportUserId))
        }
    }

    fun removeReaction(
        reactionSource: ReactionSource,
        currentReactionList: List<ReactionEntity>,
        reactionToRemove: ReactionType,
        reactionsParams: AmplitudeReactionsParams?
    ) {
        viewModelScope.launch {
            try {
                removeReactionUseCase.get().execute(reactionSource)
            } catch (exception: Exception) {
                proceedException(exception)
            }
        }
        sendUnlikeAmplitudeSelect(
            reactionSource = reactionSource,
            reactionToRemoveType = reactionToRemove,
            reactionsParams = reactionsParams
        )
        reactionRepository.get().removeReaction(
            reactionSource = reactionSource,
            reactionType = reactionToRemove,
            reactionList = currentReactionList
        )
    }

    fun removeReactionMeera(
        reactionSource: MeeraReactionSource,
        currentReactionList: List<ReactionEntity>,
        reactionToRemove: ReactionType,
        reactionsParams: AmplitudeReactionsParams?
    ) {
        viewModelScope.launch {
            try {
                removeReactionUseCase.get().executeMeera(reactionSource)
            } catch (exception: Exception) {
                proceedException(exception)
            }
        }
        sendUnlikeAmplitudeSelectMeera(
            reactionSource = reactionSource,
            reactionToRemoveType = reactionToRemove,
            reactionsParams = reactionsParams
        )
        reactionRepository.get().removeReactionMeera(
            reactionSource = reactionSource,
            reactionType = reactionToRemove,
            reactionList = currentReactionList
        )
    }

    fun logFriendInviteTap(where: FriendInviteTapProperty) {
        friendInviteTapAnalytics.logFiendInviteTap(where)
    }

    fun logProfileEditTap() {
        profileAnalytics.logProfileEditTap(
            userId = appSettings.get().readUID(),
            where = AmplitudeProfileEditTapProperty.DEEPLINK
        )
    }

    private fun proceedException(exception: Throwable) {
        liveEvent.value = when (exception) {
            is AddReactionUseCase.AlreadyDeletedException -> ReactionEvent.ShowAlert(exception.message)
            is MomentDeletedException -> ReactionEvent.Error(exception.message)
            else -> ReactionEvent.UnknownError
        }
    }

    private fun sendUnlikeAmplitudeSelect(
        reactionSource: ReactionSource,
        reactionToRemoveType: ReactionType,
        reactionsParams: AmplitudeReactionsParams?
    ) {
        when (reactionSource) {
            is ReactionSource.Post -> {
                if (reactionsParams != null) {
                    sendUnlikePostAmplitudeSelect(
                        reactionToRemoveType = reactionToRemoveType,
                        reactionsParams = reactionsParams
                    )
                }
            }

            is ReactionSource.Moment -> {
                if (reactionsParams != null) {
                    sendUnlikeMomentAmplitudeSelect(
                        reactionToRemoveType = reactionToRemoveType,
                        reactionsParams = reactionsParams
                    )
                }
            }

            else -> Unit
        }
    }

    private fun sendUnlikeAmplitudeSelectMeera(
        reactionSource: MeeraReactionSource,
        reactionToRemoveType: ReactionType,
        reactionsParams: AmplitudeReactionsParams?
    ) {
        when (reactionSource) {
            is MeeraReactionSource.Post -> {
                if (reactionsParams != null) {
                    sendUnlikePostAmplitudeSelect(
                        reactionToRemoveType = reactionToRemoveType,
                        reactionsParams = reactionsParams
                    )
                }
            }

            is MeeraReactionSource.Moment -> {
                if (reactionsParams != null) {
                    sendUnlikeMomentAmplitudeSelect(
                        reactionToRemoveType = reactionToRemoveType,
                        reactionsParams = reactionsParams
                    )
                }
            }

            else -> Unit
        }
    }

    private fun sendAmplitudeSelect(
        reactionSource: ReactionSource,
        selectedReactionType: ReactionType,
        reactionsParams: AmplitudeReactionsParams?
    ) {
        when (reactionSource) {
            is ReactionSource.CommentBottomMenu -> {
                val where = if (reactionsParams?.isEvent.isTrue()) {
                    AmplitudePropertyWhereReaction.MAP_EVENT
                } else {
                    AmplitudePropertyWhereReaction.STAT
                }
                sendCommentAmplitudeSelect(
                    postId = reactionSource.postId,
                    selectedReactionType = selectedReactionType,
                    amplitudeWhereType = where,
                    amplitudeWhenceType = reactionSource.originEnum.toAmplitudePropertyWhence(),
                    commentUserId = reactionSource.commentUserId,
                    publicationUserId = reactionSource.postUserId,
                    momentId = 0
                )
            }

            is ReactionSource.PostComment -> {
                val where = if (reactionsParams?.isEvent.isTrue()) {
                    AmplitudePropertyWhereReaction.MAP_EVENT
                } else {
                    AmplitudePropertyWhereReaction.POST
                }
                sendCommentAmplitudeSelect(
                    postId = reactionSource.postId,
                    selectedReactionType = selectedReactionType,
                    amplitudeWhereType = where,
                    amplitudeWhenceType = reactionSource.originEnum.toAmplitudePropertyWhence(),
                    commentUserId = reactionSource.commentUserId,
                    publicationUserId = reactionSource.postUserId,
                    momentId = 0
                )
            }

            is ReactionSource.MomentComment -> {
                sendCommentAmplitudeSelect(
                    postId = 0,
                    selectedReactionType = selectedReactionType,
                    amplitudeWhereType = AmplitudePropertyWhereReaction.MOMENT,
                    amplitudeWhenceType = AmplitudePropertyWhence.OTHER,
                    commentUserId = reactionSource.commentUserId,
                    publicationUserId = reactionSource.momentUserId,
                    momentId = reactionSource.momentId
                )
            }

            is ReactionSource.Moment -> {
                if (reactionsParams != null) {
                    sendLikeMomentAmplitudeSelect(
                        selectedReactionType = selectedReactionType,
                        reactionsParams = reactionsParams
                    )
                }
            }

            is ReactionSource.Post -> {
                if (reactionsParams != null) {
                    sendLikePostAmplitudeSelect(
                        selectedReactionType = selectedReactionType,
                        reactionsParams = reactionsParams
                    )
                }
            }

            is ReactionSource.CommentBottomSheet -> {
                //TODO setup Comment Amplitude
//                sendCommentAmplitudeSelect(
//                    postId = reactionSource.postId,
//                    selectedReactionType = selectedReactionType,
//                    amplitudeWhereType = AmplitudePropertyWhereReaction.COMMENTS
//                )
            }

            else -> {}
        }
    }

    private fun sendAmplitudeSelectMeera(
        reactionSource: MeeraReactionSource,
        selectedReactionType: ReactionType,
        reactionsParams: AmplitudeReactionsParams?
    ) {
        when (reactionSource) {
            is MeeraReactionSource.CommentBottomMenu -> {
                val where = if (reactionsParams?.isEvent.isTrue()) {
                    AmplitudePropertyWhereReaction.MAP_EVENT
                } else {
                    AmplitudePropertyWhereReaction.STAT
                }
                sendCommentAmplitudeSelect(
                    postId = reactionSource.postId,
                    selectedReactionType = selectedReactionType,
                    amplitudeWhereType = where,
                    amplitudeWhenceType = reactionSource.originEnum.toAmplitudePropertyWhence(),
                    commentUserId = reactionSource.commentUserId,
                    publicationUserId = reactionSource.postUserId,
                    momentId = 0
                )
            }

            is MeeraReactionSource.PostComment -> {
                val where = if (reactionsParams?.isEvent.isTrue()) {
                    AmplitudePropertyWhereReaction.MAP_EVENT
                } else {
                    AmplitudePropertyWhereReaction.POST
                }
                sendCommentAmplitudeSelect(
                    postId = reactionSource.postId,
                    selectedReactionType = selectedReactionType,
                    amplitudeWhereType = where,
                    amplitudeWhenceType = reactionSource.originEnum.toAmplitudePropertyWhence(),
                    commentUserId = reactionSource.commentUserId,
                    publicationUserId = reactionSource.postUserId,
                    momentId = 0
                )
            }

            is MeeraReactionSource.MomentComment -> {
                sendCommentAmplitudeSelect(
                    postId = 0,
                    selectedReactionType = selectedReactionType,
                    amplitudeWhereType = AmplitudePropertyWhereReaction.MOMENT,
                    amplitudeWhenceType = AmplitudePropertyWhence.OTHER,
                    commentUserId = reactionSource.commentUserId,
                    publicationUserId = reactionSource.momentUserId,
                    momentId = reactionSource.momentId
                )
            }

            is MeeraReactionSource.Moment -> {
                if (reactionsParams != null) {
                    sendLikeMomentAmplitudeSelect(
                        selectedReactionType = selectedReactionType,
                        reactionsParams = reactionsParams
                    )
                }
            }

            is MeeraReactionSource.Post -> {
                if (reactionsParams != null) {
                    sendLikePostAmplitudeSelect(
                        selectedReactionType = selectedReactionType,
                        reactionsParams = reactionsParams
                    )
                }
            }

            is MeeraReactionSource.CommentBottomSheet -> {
                //TODO setup Comment Amplitude
//                sendCommentAmplitudeSelect(
//                    postId = reactionSource.postId,
//                    selectedReactionType = selectedReactionType,
//                    amplitudeWhereType = AmplitudePropertyWhereReaction.COMMENTS
//                )
            }

            else -> {}
        }
    }

    private fun sendLikePostAmplitudeSelect(
        selectedReactionType: ReactionType,
        reactionsParams: AmplitudeReactionsParams
    ) {
        val reactionAnalytics = getAmplitudePostReactionName(selectedReactionType)

        amplitudeReactions.get().likeAction(
            userId = getUserUidUseCase.invoke(),
            actionType = reactionAnalytics,
            reactionsParams = reactionsParams,
            recFeed = checkMainFilterRecommendedUseCase.invoke(),
            where = reactionsParams.where
        )
    }

    private fun sendLikeMomentAmplitudeSelect(
        selectedReactionType: ReactionType,
        reactionsParams: AmplitudeReactionsParams,
    ) {
        amplitudeReactions.get().likeAction(
            userId = getUserUidUseCase.invoke(),
            actionType = getAmplitudePostReactionName(selectedReactionType),
            reactionsParams = reactionsParams,
            recFeed = false,
            where = AmplitudePropertyReactionWhere.MOMENT
        )
    }

    private fun sendUnlikePostAmplitudeSelect(
        reactionToRemoveType: ReactionType,
        reactionsParams: AmplitudeReactionsParams
    ) {
        val reactionAnalytics = getAmplitudePostReactionName(reactionToRemoveType)

        amplitudeReactions.get().unlikeAction(
            userId = getUserUidUseCase.invoke(),
            actionType = reactionAnalytics,
            reactionsParams = reactionsParams,
            recFeed = checkMainFilterRecommendedUseCase.invoke(),
            where = reactionsParams.where
        )
    }

    private fun sendUnlikeMomentAmplitudeSelect(
        reactionToRemoveType: ReactionType,
        reactionsParams: AmplitudeReactionsParams,
    ) {
        amplitudeReactions.get().unlikeAction(
            userId = getUserUidUseCase.invoke(),
            actionType = getAmplitudePostReactionName(reactionToRemoveType),
            reactionsParams = reactionsParams,
            recFeed = false,
            where = AmplitudePropertyReactionWhere.MOMENT
        )
    }

    private fun sendCommentAmplitudeSelect(
        postId: Long,
        selectedReactionType: ReactionType,
        amplitudeWhereType: AmplitudePropertyWhereReaction,
        amplitudeWhenceType: AmplitudePropertyWhence,
        commentUserId: Long,
        publicationUserId: Long?,
        momentId: Long,
    ) {
        val reactionAnalytics = when (selectedReactionType) {
            ReactionType.Crying -> AmplitudePropertyReactionType.SAD
            ReactionType.Facepalm -> AmplitudePropertyReactionType.OOPS
            ReactionType.Fire -> AmplitudePropertyReactionType.FIRE
            ReactionType.GreenLight -> AmplitudePropertyReactionType.LIKE
            ReactionType.InLove -> AmplitudePropertyReactionType.WOW
            ReactionType.LaughTears -> AmplitudePropertyReactionType.HA
            ReactionType.RedLight -> AmplitudePropertyReactionType.SHIT
            ReactionType.Amazing -> AmplitudePropertyReactionType.OHO
            ReactionType.Morning -> AmplitudePropertyReactionType.MORNING
            ReactionType.Evening -> AmplitudePropertyReactionType.NIGHT
        }
        amplitudeHelper.get().reactionToComment(
            postId = postId,
            type = reactionAnalytics,
            where = amplitudeWhereType,
            whence = amplitudeWhenceType,
            commentUserId = commentUserId,
            publicationUserId = publicationUserId ?: 0L,
            momentId = momentId
        )
    }

    fun logOutWithDelegate(
        isCancelledRegistration: Boolean = false,
        unsubscribePush: Boolean = true,
        changeServer: Boolean = false,
        action: suspend (
            isNotCancelledRegistration: Boolean,
            isHolidayIntroDialogShown: Boolean
        ) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            logoutDelegate.logout(
                isCancelledRegistration,
                unsubscribePush,
                changeServer,
                action
            ) {
                _liveUnreadNotificationCounter.postValue(0)
                if (changeServer) _liveViewEvents.emit(ReadyForRestartAppAfterLogout(true))
            }
        }
    }

    private fun startReceivingLocationUpdates() = startReceivingLocationUpdatesUseCase.invoke()

    private fun setNeedToShowUpdateAppMark(isNeedToShowUpdateAppMark: Boolean) {
        viewModelScope.launch {
            appSettings.get().profileNotificationAppUpdate.set(isNeedToShowUpdateAppMark)
        }
    }

    fun readAccessToken() = appSettings.get().readAccessToken()

    fun isRegistrationCompleted() = appSettings.get().isRegistrationCompleted

    private fun stopReceivingLocationUpdates() = stopReceivingLocationUpdatesUseCase.invoke()

    private fun resetSubscriptionsRoad() {
        setSubPostsRequestedInSession.invoke(false)
    }

    fun tryToRegisterShakeEvent() = viewModelScope.launch {
        if (!isAuthorizedUserUseCase.get().isAuthorizedUser()
            || shakeEventListener.get().isSensorRunning()
            || getNeedToRegisterShakeUseCase.get().invoke().not()
            || featureTogglesContainer.get().shakeFeatureToggle.isEnabled.not()
        ) return@launch
        shakeEventListener.get().registerShakeEventListener()
    }

    private fun unRegisterShakeEventListener() {
        if (shakeEventListener.get().isSensorRunning().not()) return
        shakeEventListener.get().unregisterShakeEventListener()
    }

    private fun auth() {
        viewModelScope.launch(Dispatchers.IO) {
            if (appSettings.get().readIsUserAuthorized() != 0) {
                getHolidayInfo()
                initFirebaseRemoteConfig()
            } else {
                authLoginAnonymousUseCase.execute(
                    success = { isSuccess ->
                        if (isSuccess) getHolidayInfo()
                        initFirebaseRemoteConfig()
                    },
                    fail = {
                        Timber.e(it.simpleName)
                    }
                )
            }
        }
    }

    private fun initFirebaseRemoteConfig() {
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            Timber.e("УПАЛИ ФИЧАТОГЛЫ - $throwable")
        }) {
            remoteConfigs.getFlags()
            _onFeatureTogglesLoaded.postValue(Unit)
        }
    }

    private fun logFirstTimeOpenApp() {
        try {
            if (appSettings.get().readIsFirstTimeOpenApp()) {
                appSettings.get().writeIsFirstTimeOpenApp(false)
                amplitudeHelper.get().logFirstTimeOpen()
            }
        } catch (e: Exception) {
            Timber.d(e)
        }
    }

    fun logMomentOpen(
        userId: Long?,
        openedWhere: AmplitudePropertyMomentScreenOpenWhere,
        viewedEarly: Boolean?
    ) {
        val whoseMomentOf = userId?.let {
            if (it == getUserUidUseCase.invoke()) {
                AmplitudePropertyMomentWhose.MY_MOMENT
            } else {
                AmplitudePropertyMomentWhose.USER_MOMENT
            }
        } ?: AmplitudePropertyMomentWhose.UNKNOWN

        amplitudeMoment.get().onMomentScreenOpen(
            whoseMomentOf = whoseMomentOf,
            whereOpenFrom = openedWhere,
            isViewEarlier = viewedEarly
        )
    }

    private fun initSessionCounter() {
        appSettings.get().sessionCounter += 1
    }

    /**
     * Счётчик считает количество непрочитанных сообщений + запросы на переписку
     * в которых есть хотя бы одно непрочитанное сообщение
     */
    fun getTotalUnreadCounter(): LiveData<Int> {
        val messageCounter = dataStore.get().dialogDao().liveCountUnreadMessages()
        val requestCounter = dataStore.get().dialogDao()
            .getChatRequestUnreadMessageCount(DialogApproved.NOT_DEFINED.key)
        val totalCounter = messageCounter.combineWith(requestCounter) { msg, request ->
            val messageCount = msg ?: 0
            val requestCount = request ?: 0
            return@combineWith messageCount + requestCount
        }
        return totalCounter
    }

    fun observeReloadDialogs() {
        disposables.add(
            websocketChannel.get().observeReloadDialogs()
                .subscribeOn(Schedulers.io())
                .subscribe({
                    Timber.d("Observe RELOAD Room [MainActivityViewModel]")
                    getRooms()
                }, { Timber.e(it) })
        )
    }

    /**
     * Get all rooms with pagination
     * isReloadRooms = true когда тебе событие приходит reload_dialogs. В остальных случаях false
     */
    private fun getRooms() {
        viewModelScope.launch { roomsInteractor.getRooms() }
    }

    /**
     * Method for get room updatedAt before transit to chat fragment
     */
    private fun triggerGoToChat(roomId: Long) {
        viewModelScope.launch {
            val roomData = runCatching {
                roomDataUseCase.get().invoke(roomId)
            }.getOrNull()
            if (roomData != null) {
                val event = ChatRoomsViewEvent.OnNavigateToChatEvent(roomData)
                _liveRoomsViewEvent.emit(event)
            }
        }
    }

    private fun getUnreadBadgeInfo() {
        dataStore.get().dialogDao()
            .getBadgedUnreadDialogs()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ needToShowBadge ->
                liveUnsentMessageBadge.postValue(needToShowBadge > 0)
            }, {
                Timber.e(it)
            })
            .addDisposable()
    }


    fun getUserAccountType(): Int {
        return appSettings.get().accountType ?: ACCOUNT_TYPE_REGULAR
    }

    fun stopSyncContacts() {
        stopSyncContactsUseCase.get().invoke()
    }

    private var isFirstRequest = true

    private fun subscribeEvent() {
        viewModelScope.launch {
            runCatching {
                appSettings.get().newEvent.asFlow().collect {
                    Timber.d("Bazaleev subscribeEvent called value = $it")
                    if (!isFirstRequest)
                        liveNewEvent.value = true
                    isFirstRequest = false
                }
            }.onFailure {
                Timber.e("Error while getting settings $it")
            }
        }
    }

    fun isLocationEnabled(): Boolean {
        return locationEnableProvider.get().hasLocationPermission()
            && locationEnableProvider.get().isLocationEnabled()
    }

    // TODO https://nomera.atlassian.net/browse/BR-22144
    private fun subscribeProfileNotification() {
        viewModelScope.launch {
            runCatching {
                appSettings.get().profileNotification.asFlow().collect {
                    liveProfileIndicator.value =
                        (it ?: false) || appSettings.get().profileNotificationAppUpdate.get() ?: false
                }
            }.onFailure {
                liveProfileIndicator.value = false || appSettings.get().profileNotificationAppUpdate.get() ?: false
            }

            runCatching {
                appSettings.get().profileNotificationAppUpdate.asFlow().collect {
                    liveProfileIndicator.value = (it ?: false) || appSettings.get().profileNotification.get() ?: false
                }
            }.onFailure {
                liveProfileIndicator.value = false || appSettings.get().profileNotification.get() ?: false
            }
        }
    }

    private fun connectSocket() {
        if (websocketChannel.get().isConnected()) return
        val accessToken = appSettings.get().readAccessToken()
        if (accessToken.isNotEmpty()) {
            websocketChannel.get().initSocket(accessToken)
            socketConnectionListener()
            if (!websocketChannel.get().isChannelJoined()) {
                viewModelScope.launch(Dispatchers.Main) {
                    _liveViewEvents.emit(OnBindSignallingService)
                }
                rejoinWebSocket {
                    startSocketEventObservers(accessToken)
                }
            }
        } else {
            disconnectWebSocket()
        }
    }

    private fun handleLocationEnableClicked() {
        when {
            !locationEnableProvider.get().hasLocationPermission() -> {
                viewModelScope.launch { _liveViewEvents.emit(NavigateToAppSettingsUiEffect) }
            }

            !locationEnableProvider.get().isLocationEnabled() -> {
                viewModelScope.launch { _liveViewEvents.emit(NavigateLocationSettingsUiEffect) }
            }
        }
    }

    private fun socketConnectionListener() {
        onOpenSocket {
            viewModelScope.launch(Dispatchers.IO) {
                _liveViewEvents.emit(OnCheckServiceConnection)
            }
        }
        onErrorSocket { _, responseError ->
            handleSocketError(responseError)
        }
    }

    private fun handleSocketError(responseError: Response?) {
        when (responseError?.code) {
            HTTP_CODE_FORBIDDEN, HTTP_CODE_NOT_AUTHORIZED -> tryRefreshToken()
            else -> {
                viewModelScope.launch(Dispatchers.IO) {
                    _liveViewEvents.emit(OnSocketError)
                }
            }
        }
    }

    private fun handleRefreshToken() {
        websocketChannel.get().disconnectMainChannel()
        websocketChannel.get().disconnectSocket()
        viewModelScope.launch(Dispatchers.IO) {
            delay(2000L)
            val accessToken = appSettings.get().readAccessToken()
            if (accessToken.isNotEmpty()) {
                connectSocket()
            } else {
                _liveViewEvents.emit(Logout)
            }
        }
    }

    private fun rejoinWebSocket(successReconnect: () -> Unit) {
        Timber.d("ViewModel Rejoin web socket")
        websocketChannel.get().rejoinChannel({
            successReconnect.invoke()
        })
    }

    private fun disconnectWebSocket() {
        websocketChannel.get().disconnectAll()
    }

    fun onOpenSocket(block: () -> Unit) {
        websocketChannel.get().onOpenSocket {
            block()
        }
    }

    fun onCloseSocket(block: () -> Unit) {
        websocketChannel.get().onCloseSocket {
            block()
        }
    }

    private fun onErrorSocket(block: (Throwable, Response?) -> Unit) {
        websocketChannel.get().onErrorSocket { throwable, response ->
            block(throwable, response)
        }
    }

    private fun startSocketEventObservers(accessToken: String) {
        Timber.d("(MainActivityViewModel) start OBSERVERS Access token PREF: $accessToken")
        if (accessToken.isNotEmpty()) {
            observeDisconnectEventWebSocket()
            observeGetMessages()
            observeUpdateUser()
            observeProfileStatistics()
            getCallSettings()
            observeReloadDialogs()
            emitViewEffect(ConnectShakeSocketUiEffect)
            // Reload getRooms() after reconnect
            viewModelScope.launch(Dispatchers.IO) {
                _liveViewEvents.emit(OnPreferencesReady)
            }
        }
    }

    /**
     * Получение новых сообщений с сервера (Observer) для вычисления каунтеров и т.д.
     */
    private fun observeGetMessages() {
        Timber.d("Start observe Get messages")
        disposables.add(
            websocketChannel.get().observeGetMessages()
                .flatMap { response ->
                    Observable.fromCallable {
                        val json = gson.get().toJson(response.payload)
                        val message = gson.get().fromJson<MessageEntity>(json)
                        message.creatorUid = message.creator?.userId ?: 0
                        val messageExists = dataStore.get().messageDao().getMessageById(message.msgId)

                        // Timber.d("MSG => UPDATE OBSERVER (MESSAGE(${message})")
                        // Только для сообщения, которого нет в базе
                        if (message.creator?.userId != appSettings.get().readUID() && messageExists == null) {
                            // Update field is_show_avatar for trigger onBind view holder for hide previous message user avatar in a group chat
                            val prevMessage = dataStore.get().messageDao()
                                .getPreviousMessage(message.roomId, message.createdAt)
                            prevMessage?.let { preMsg ->
                                if (preMsg.creator?.userId == message.creator?.userId) {
                                    dataStore.get().messageDao()
                                        .setInvisiblePreviousAvatar(message.roomId, preMsg.msgId)
                                }
                            }
                        }

                        // If message deleted update into Db and reload rooms
                        val moment = getMomentChatMetadata(message)
                        if (message.deleted || moment?.deleted == true.toInt()) {
                            message.itemType = resolveMessageType(
                                creatorId = message.creator?.userId,
                                attachment = message.attachment,
                                attachments = message.attachments,
                                deleted = message.deleted,
                                eventCode = message.eventCode,
                                type = message.type,
                                myUid = appSettings.get().readUID()
                            )
                            dataStore.get().messageDao().insert(message)
                            viewModelScope.launch {
                                roomsInteractor.reloadRooms {}
                            }
                        }

                        // Update Rooms for sort by updatedAt last message every time when incoming message
                        dataStore.get().dialogDao().updateDialogTime(message.roomId, message.createdAt)

                        // Get current Room state
                        val currentRoom = dataStore.get().dialogDao().getDialogByRoomIdSuspend(message.roomId)

                        // Если приходит новое сообщение впервые
                        val isChangedCurrentLastMessage = isChangedCurrentLastMessage(message, currentRoom?.lastMessage)
                        if (currentRoom?.lastMessage?.msgId != message.msgId || isChangedCurrentLastMessage) {
                            val lastMessage = message.toRoomLastMessage()
                            val isUpdatedLastMessage =
                                dataStore.get().dialogDao()
                                    .updateLastMessage(message.roomId, lastMessage, lastMessage.sent)

                            if (message.creator?.userId != appSettings.get().readUID() && messageExists == null) {
                                if (!lastMessages.contains(message.msgId)) {
                                    val unreadMessages =
                                        dataStore.get().dialogDao().getUnreadMessageCount(message.roomId)
                                    if (unreadMessages == 0L) {
                                        dataStore.get().dialogDao().updateLastUnreadMessageTs(
                                            message.roomId,
                                            message.createdAt
                                        )
                                    }

                                    if (!message.readed) {
                                        dataStore.get().dialogDao().incrementUnreadMessageCount(message.roomId)
                                    }
                                }
                                lastMessages.add(message.msgId)
                            }
                            viewModelScope.launch(Dispatchers.IO) {
                                roomsInteractor.dirtyHackForUnreadEvent(isUpdatedLastMessage, message)
                            }
                        }

                        if (currentRoom?.lastMessage?.attachment?.type == TYPING_TYPE_AUDIO
                            && message.attachment.audioRecognizedText.isNotEmpty()
                        ) {
                            val lastMessage = message.toRoomLastMessage()
                            dataStore.get().dialogDao().updateLastMessage(message.roomId, lastMessage, lastMessage.sent)
                        }

                        message //.roomId
                    }
                }
                .subscribeOn(Schedulers.io())
                .subscribe({ row ->
                    // Timber.e("Successfully new incoming message(Act viewModel) -row- $row")
                }, { error -> Timber.e("ERROR Observe New response Message: $error") })
        )
    }

    private fun isChangedCurrentLastMessage(
        message: MessageEntity,
        currentLastMessage: LastMessage?
    ): Boolean {
        return message.msgId == currentLastMessage?.msgId
            && (message.delivered != currentLastMessage.delivered || message.readed != currentLastMessage.readed)
    }

    private fun getMomentChatMetadata(message: MessageEntity): MomentItemDto? {
        val momentMap = message.attachment.moment
        return gson.get().fromJson<MomentItemDto?>(momentMap ?: emptyMap())
    }

    private fun observeProfileStatistics() {
        Timber.d("Start observe Profile Statistics")
        disposables.add(
            websocketChannel.get().observeProfileStatistics()
                .subscribeOn(Schedulers.io())
                .subscribe({ message ->
                    val response = gson.get().fromJson<SlidesListResponse>(gson.get().toJson(message.payload))

                    response?.let { handleProfileStatistics(it) }
                }, { Timber.e(it) })
        )
    }

    private fun handleProfileStatistics(slidesResponse: SlidesListResponse) {
        viewModelScope.launch {
            if (slidesResponse.slides.isNotEmpty()) {
                val useCaseParams = SetProfileStatisticsParams(slidesResponse)
                setProfileStatisticsSlidesUseCase.get().execute(useCaseParams)

                appSettings.get().profileNotification.set(true)
            }
        }
    }


    /**
     * Observe update user for Room type dialog
     */
    private fun observeUpdateUser() {
        disposables.add(
            websocketChannel.get().observeUpdateUser()
                .subscribeOn(Schedulers.io())
                .subscribe({ message ->
                    // Timber.e("OBSERVE ===> Update user when (MainActivityVM)")
                    val response = gson.get().fromJson<UpdateUserResponse>(gson.get().toJson(message.payload))
                    val payload = hashMapOf(
                        "user_type" to "UserChat",
                        "user_id" to mutableListOf(response.userId)
                    )
                    response.roomId?.let { id -> updateCompanionUser(id, payload) }
                }, { Timber.e(it) })
        )
    }

    /**
     * Update companion user in Db when get user info request
     */
    private fun updateCompanionUser(roomId: Long, payload: Map<String, Any>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val responsePayload = websocketChannel.get().getUserInfo(payload).payload
                val response =
                    gson.get().fromJson<ResponseWrapperWebSock<ChatUsers>>(gson.get().toJson(responsePayload))
                if (response.response?.users?.isNotEmpty() == true) {
                    val companion = response.response?.users?.get(0)
                    // Update companion user in room IF Room exists
                    dataStore.get().dialogDao().updateCompanionUser(roomId, companion)
                }
            } catch (e: Exception) {
                Timber.e("(ERROR) Network exception - update_companion_user")
            }
        }
    }

    /**
     * Запрашиваем информацию c сервера об IceServers для звонков
     */
    private fun getCallSettings() {
        viewModelScope.launch {
            runCatching {
                val message = websocketChannel.get()
                    .pushSignalingMessageSuspend(MessageType.GET_ICE.type, hashMapOf())
                val result = message.payload.makeEntity<SignalingGetIceResponse>(gson.get())
                appSettings.get().savePrefCallIceServers(result.response)
            }.onFailure { Timber.e("ERROR get call settings from server:$it") }
        }
    }

    /**
     * Observe disconnect event from web socket (for exam. token expires)
     */
    private fun observeDisconnectEventWebSocket() {
        Timber.d("Start observe disconnect")
        disposables.add(
            websocketChannel.get().observeDisconnect()
                .subscribe({ response ->
                    val payload = response.payload
                    val code = payload["code"]         // 401 => refresh token
                    val message = payload["message"]    // String

                    Timber.e("(EVENT) Disconnect socket -code- $code -message- $message")

                    (code as Double?)?.toInt()?.let { httpCode ->
                        Timber.e("LOG_OUT_CALLBACK $httpCode")
                        if (httpCode == HTTP_CODE_NOT_AUTHORIZED) {
                            viewModelScope.launch(Dispatchers.IO) {
                                _liveViewEvents.emit(Logout)
                            }
                        }
                    }
                    tryRefreshToken()
                }, { error ->
                    Timber.e("ERROR: Observe disconnect status: ${error.message}")
                    error.printStackTrace()
                })
        )
    }


    fun handleError(
        errorCallback: (message: String) -> Any,
        logOutCallback: (message: String?) -> Any
    ) {
        websocketChannel.get().errorHandler(object : WebSocketMainChannel.OnActivityInteractionCallback {

            /**
             * Error from Phoenix framework
             */
            override fun onSocketPhxError(response: Response?) {
                logOutCallback.invoke(null)
            }

            /**
             * Errors from common request
             */
            override fun onErrorHandler(responseError: Payload) {
                val error =
                    gson.get().fromJson<ResponseWrapperWebSock<ResponseError>>(responseError).response
                when (error?.code) {
                    401 -> error.message?.let { logOutCallback.invoke(it) }
                    else -> error?.message?.let { errorCallback.invoke(it) }

                }
            }
        })
    }

    private fun updateBirthdayDialogShown() {
        viewModelScope.launch {
            try {
                updateBirthdayShownUseCase.get().invoke()
                repository.get().appSettings.isNeedShowBirthdayDialog.set(false)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    /**
     * Observe refresh token when OkHttp response -> token expired
     */
    private fun observeRefreshTokenRestService() {
        disposables.add(
            App.bus.toObservable()
                .debounce(5, TimeUnit.SECONDS)
                .subscribe({ obj ->
                    Timber.d("RxBus RESULT shouuld token refresh")
                    if (obj is RxEventsJava.MustRefreshToken) {
                        Timber.e("(REST) SHOULD Refresh token =>")
                        tryRefreshToken()
                    }
                }, {
                    Timber.e("ERROR: RxBus ${it.message}")
                })
        )
    }

    /**
     * Initialized local assets(hair,eyes,hat,ears and etc) for animated Avatar
     */
    private fun initializeAvatarSDK() {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                initializeAvatarSdkUseCase.invoke()
            }.onFailure {
                Timber.e(it)
            }
        }
    }

    private var visits: HolidayVisits? = null
    private var isHolidayCalendarShown = false
    private val mutex = Mutex()


    private fun showCalendarIfNot() {
        val visits = this.visits
        when {
            !holidayInfoHelper.get().isHolidayExistAndMatches() ||
                holidayInfoHelper.get().currentHoliday().code != HolidayInfo.HOLIDAY_NEW_YEAR -> return

            !appSettings.get().isAuthorizedUser() && visits != null -> this.visits = null

            appSettings.get().isAuthorizedUser() && visits == null -> getVisits()

            !holidayInfoHelper.get().isHolidayIntroduced() || !appSettings.get().isAuthorizedUser() -> return

            appSettings.get().holidayCalendarStatus == HolidayVisitsEntity.STATUS_ACHIEVED -> return

            !holidayInfoHelper.get()
                .wasShownToday() && appSettings.get().holidayCalendarDaysCount == visits?.visitDays -> getVisits(true)

            !holidayInfoHelper.get().wasShownToday() || !holidayInfoHelper.get()
                .sameUser() && visits?.visitDays != "0" -> {
                if (!holidayInfoHelper.get().wasShownYesterday() && holidayInfoHelper.get().sameUser() &&
                    visits?.status == HolidayVisitsEntity.STATUS_IN_PROGRESS
                ) {
                    visits.status = HolidayVisitsEntity.STATUS_DAY_SKIPPED
                }
                visits?.let { postHolidayVisits(it) }
            }
        }
    }

    fun calendarShown() {
        val count = when (visits?.visitDays?.toInt()) {
            HolidayCalendarBottomDialog.DAY_COUNT_1 -> AmplitudePropertyCandyCount.CANDY_1
            HolidayCalendarBottomDialog.DAY_COUNT_2 -> AmplitudePropertyCandyCount.CANDY_2
            HolidayCalendarBottomDialog.DAY_COUNT_3 -> AmplitudePropertyCandyCount.CANDY_3
            HolidayCalendarBottomDialog.DAY_COUNT_4 -> AmplitudePropertyCandyCount.CANDY_4
            HolidayCalendarBottomDialog.DAY_COUNT_5 -> AmplitudePropertyCandyCount.CANDY_5
            HolidayCalendarBottomDialog.DAY_COUNT_6 -> AmplitudePropertyCandyCount.CANDY_6
            HolidayCalendarBottomDialog.DAY_COUNT_7 -> AmplitudePropertyCandyCount.CANDY_7
            else -> null
        }
        count?.let { amplitudeHelper.get().logNewYearCandyCount(it) }
        visits?.let { updateVisits(it) }
    }

    private fun initializeAuthentication() {
        viewModelScope.launch(Dispatchers.IO) {
            authInitUseCase.get().init()
        }
    }

    private fun postHolidayVisits(visits: HolidayVisits) {
        viewModelScope.launch {
            mutex.withLock {
                if (!isHolidayCalendarShown) {
                    isHolidayCalendarShown = true
                    _holidaysFlow.emit(visits)
                }
            }
        }
    }

    private fun getVisits(showNow: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            holidayInfoInteractor.get().getDailyVisits(
                DefParams(),
                success = {
                    visits = it
                    if (showNow) {
                        postHolidayVisits(it)
                    } else {
                        showCalendarIfNot()
                    }
                }, {})
        }
    }

    private fun updateVisits(visits: HolidayVisits) {
        appSettings.get().apply {
            holidayCalendarShowDate = holidayInfoHelper.get().todayDate()
            holidayCalendarStatus =
                if (visits.status == HolidayVisitsEntity.STATUS_DAY_SKIPPED) {
                    HolidayVisitsEntity.STATUS_IN_PROGRESS
                } else {
                    visits.status
                }
            holidayCalendarShownToUserWithId = appSettings.get().readUID()
            holidayCalendarDaysCount = visits.getAchievedDays()
            this@MainActivityViewModel.visits = visits
        }
    }


    /**
     * Refresh access token request and save to preferences
     */
    fun tryRefreshToken() {
        val refreshToken = appSettings.get().readAccessToken()
        Timber.d("Actual refresh token: $refreshToken")
        if (refreshToken != null) {
            refreshTokenUseCase.get().refreshToken(refreshToken)?.let { refresh ->
                refresh.subscribeOn(Schedulers.io())
                    .subscribe({ response ->
                        //Timber.e("Response REFRESH token code: ${response.code()} New refresh token: ${response.body()?.refreshToken} CleanResponse: $response")
                        Timber.e("RESP Refresh: $response")
                        if (response.code() == 200) {
                            //Timber.e("Should REFRESH")
                            response.body()?.let { token ->
                                Timber.d("WRITE NEW USER TOKEN (New refresh token:$token)")
                                writeTokensAfterRefresh(token)
                                handleRefreshToken()
                            }
                        } else {
                            Timber.e("Invoke ERROR")
                            viewModelScope.launch(Dispatchers.IO) {
                                _liveViewEvents.emit(Logout)
                            }
                        }
                    }, { error ->
                        Timber.e("REST ERROR: Refresh token ${error.message}")
                        //_liveViewEvents.emit(OnFailureRefreshToken)
                        viewModelScope.launch(Dispatchers.IO) {
                            _liveViewEvents.emit(Logout)
                        }
                    })
            }
        } else Timber.e("Refresh token is NULL (app settings)")
    }

    private fun writeTokensAfterRefresh(token: GetToken) {
        appSettings.get().writeAccessToken(token.accessToken)
        appSettings.get().userExpiresToken = token.expiresIn.toLong()
        appSettings.get().userRefreshToken = token.refreshToken
    }

    fun handleAction(action: ActActions) {
        when (action) {
            ActActions.ResetSubscriptionsRoad -> resetSubscriptionsRoad()
            ActActions.TryToRegisterShakeEvent -> tryToRegisterShakeEvent()
            ActActions.UnregisterShakeEventListener -> unRegisterShakeEventListener()
            ActActions.SubscribeEvent -> subscribeEvent()
            ActActions.LoadPrivacySettings -> loadPrivacySettings()
            ActActions.ObserveRefreshTokenRestService -> observeRefreshTokenRestService()
            is ActActions.InitializeAvatarSDK -> initializeAvatarSDK()
            ActActions.GetRooms -> getRooms()
            ActActions.HandleLocationEnableClicked -> handleLocationEnableClicked()
            ActActions.UpdateBirthdayDialogShown -> updateBirthdayDialogShown()
            ActActions.ShowCalendarIfNot -> showCalendarIfNot()
            is ActActions.MarkHintsAsShown -> markHintsAsShown(action.hint)
            ActActions.GetUnreadBadgeInfo -> getUnreadBadgeInfo()
            ActActions.RequestCounter -> requestCounter()
            ActActions.ConnectSocket -> connectSocket()
            ActActions.SubscribeProfileNotification -> subscribeProfileNotification()
            is ActActions.UpdateUnreadNotificationBadge -> updateUnreadNotificationBadge(action.needToShow)
            is ActActions.SetNeedToShowUpdateAppMark -> setNeedToShowUpdateAppMark(action.isNeedToShowUpdateAppMark)
            ActActions.StartReceivingLocationUpdates -> startReceivingLocationUpdates()
            ActActions.StopReceivingLocationUpdates -> stopReceivingLocationUpdates()
            ActActions.ShowAllPosts -> showAllPosts()
            ActActions.DisconnectWebSocket -> disconnectWebSocket()
            ActActions.LogBack -> logBack()
            ActActions.LogSwipeBack -> logSwipeBack()
            ActActions.ShowBirthdayDialogDelayed -> showBirthdayDialogDelayed()
            ActActions.ShowDialogIfBirthday -> showDialogIfBirthday()
            is ActActions.HandleDeepLinks -> handleDeepLinks(action.intent)
            ActActions.PushBirthdayState -> pushBirthdayState()
            is ActActions.MarkAsRead -> markAsRead(action.pushEventId, action.isGroup)
            is ActActions.TriggerGoToChat -> triggerGoToChat(action.roomId)
            is ActActions.UpdateGalleryPost -> updateGalleryPost(action.path)
            ActActions.OnShowReactionBubble -> onShowReactionBubble()
            is ActActions.AddReaction -> addReaction(
                reactionSource = action.reactionSource,
                currentReactionList = action.currentReactionList,
                reaction = action.reaction,
                reactionsParams = action.reactionsParams,
                isFromBubble = action.isFromBubble
            )
            is ActActions.AddReactionMeera -> addReactionMeera(
                reactionSource = action.reactionSource,
                currentReactionList = action.currentReactionList,
                reaction = action.reaction,
                reactionsParams = action.reactionsParams,
                isFromBubble = action.isFromBubble
            )

            is ActActions.RemoveReaction -> removeReaction(
                reactionSource = action.reactionSource,
                currentReactionList = action.currentReactionList,
                reactionToRemove = action.reactionToRemove,
                reactionsParams = action.reactionsParams
            )
            is ActActions.RemoveReactionMeera -> removeReactionMeera(
                reactionSource = action.reactionSource,
                currentReactionList = action.currentReactionList,
                reactionToRemove = action.reactionToRemove,
                reactionsParams = action.reactionsParams
            )

            is ActActions.UpdatePeopleBadge -> updatePeopleBadge()
            is ActActions.OnScreenshotTaken -> checkIfNeedToShowScreenshotPopup()
            is ActActions.LogOpenCommunityFromDeeplink -> logOpenCommunityFromDeeplink()
            is ActActions.LoadSignupCountries -> loadSignupCountries()
            is ActActions.StartListeningSyncNotificationService -> startListeningSyncNotificationService()
            is ActActions.StopListeningSyncNotificationService -> stopListeningSyncNotificationService()
        }
    }

    private fun markHintsAsShown(hint: Hint) {
        hintManager.get().markShownHint(hint)
    }

    override fun onCleared() {
        super.onCleared()
        websocketChannel.get().disconnectMainChannel()
        websocketChannel.get().disconnectSocket()
        disposables.dispose()
    }

    private fun getSensitiveContentManager(): ISensitiveContentManager = repository.get()

    private fun showAllPosts() {
        viewModelScope.launch {
            getSensitiveContentManager().getPosts().forEach { postId ->
                repository.get().updatePostById()
            }
            getSensitiveContentManager().clear()
        }
    }

    private fun updateGalleryPost(path: String?) {
        updateGalleryPostUseCase.get().execute(path)
    }

    private fun stopChatResendProgress() {
        viewModelScope.launch(Dispatchers.IO) {
            dataStore.get().messageDao().updateAllResendProgressStatus(showResendProgress = false)
        }
    }

    /**
     * При входе в приложение показываем уведомление
     * о непрочитанных сообщениях, если они существуют
     */
    private fun showUnsentMessagesNotification() {
        viewModelScope.launch {
            resendMessagesNotificationUtil.get().showNoSentMessagesNotifications()
        }
    }

    fun getHolidayInfo(isFromAuth: Boolean = false) {
        viewModelScope.launch {
            holidayInfoInteractor.get().getHolidayInfo(
                GetHolidayParams,
                { holidayInfo ->
                    holidayInfoHelper.get().setCurrentHoliday(holidayInfo)
                    val holidayMatches = holidayInfoHelper.get().isHolidayExistAndMatches()
                    val holidayIntroduced = appSettings.get().isHolidayIntroduced
                        && appSettings.get().holidayIntroducedVersion == BuildConfig.VERSION_NAME
                    val isAuthorized =
                        appSettings.get().isAuthorizedUser() && (appSettings.get().isNewUserRegistered || isFromAuth)
                    emitViewEffect(
                        OnHolidayReady(
                            needToShowHoliday = holidayMatches && isAuthorized,
                            isHolidayIntroduced = holidayIntroduced
                        )
                    )
                },
                { e ->
                    emitViewEffect(OnHolidayFailed)
                    Timber.i("ERROR getting holiday info:${e.localizedMessage}")
                })
        }
    }

    /**
     * Проверяем День Рождения юзера
     * Приостанавливаем на пол секунды т.к диалог не успевает показаться
     */
    private fun showDialogIfBirthday() {
        doDelayed(GET_BIRTHDAY_STATE_DELAY) {
            viewModelScope.launch {
                if (repository.get().appSettings.isAuthorizedUser()
                    && repository.get().appSettings.isNeedShowBirthdayDialog.get() == true
                ) {
                    pushBirthdayState()
                }
            }
        }
    }

    private fun showBirthdayDialogDelayed() {
        doDelayed(GET_BIRTHDAY_STATE_DELAY) {
            pushBirthdayState()
        }
    }

    private fun pushBirthdayState() {
        if (!isAuthorizedUserUseCase.get().isAuthorizedUser()) return
        when {
            isUserBirthdayToday() -> {
                if (userBirthdayUtils.get().isDateAfter(DEFAULT_SEND_DIALOG_AFTER_TIME)) {
                    emitViewEffect(ShowBirthdayDialogEvent(true))
                }
            }

            isUserBirthdayYesterday() -> {
                if (userBirthdayUtils.get().isDateAfter(DEFAULT_SEND_DIALOG_AFTER_TIME)) {
                    emitViewEffect(ShowBirthdayDialogEvent(false))
                }
            }
        }
    }

    /**
     * Triggers user's privacy settings loading. [GetSettingsUseCase] caches it locally after
     * getting the latest settings from server.
     */
    private fun loadPrivacySettings() = viewModelScope.launch(Dispatchers.IO) {
        try {
            val settings = getSettingsUseCase.get().invoke()
            syncContactsIfAllowed(settings)
        } catch (e: Exception) {
            Timber.i("Failed to load privacy settings:${e.localizedMessage}")
        }
    }

    fun isNeedShowFriendsFollowersPrivacyPopup() =
        getFriendsSubscribersPopupPrivacyUseCase.invoke().getSync() ?: false

    private fun handleDeepLinks(intent: Intent?) {
        intent?.let { _intent ->
            val scheme = _intent.scheme
            if (scheme == HTTP_SCHEME || scheme == HTTPS_SCHEME || scheme == NOOMEERA_SCHEME) {
                _intent.data?.let { uri ->
                    checkDeepLinkAndOpen(uri)
                } ?: kotlin.run {
                    Timber.e("Deeplink URI is NULL")
                    liveDeeplinkActions.value = DeeplinkActionViewEvent.ParseError
                }
            }
        }
    }

    private fun logSwipeBack() {
        if (!isBackPres) {
            amplitudeHelper.get().logBackSwipe()
        } else {
            isBackPres = false
        }
    }

    private fun logBack() {
        isBackPres = true
        amplitudeHelper.get().logBackPressed()
    }

    fun isUserBirthdayToday(): Boolean {
        return userBirthdayUtils.get().isBirthdayToday(getUserDateOfBirthUseCase.get().invoke())
    }

    fun isUserBirthdayYesterday(): Boolean {
        return userBirthdayUtils.get().isBirthdayYesterday(getUserDateOfBirthUseCase.get().invoke())
    }

    //Данный метод используется для отображения бейджа непрочитаннх уведомлений
    private fun updateUnreadNotificationBadge(needToShow: Boolean) {
        liveUnreadNotificationBadge.postValue(needToShow)
    }

    private fun markAsRead(notificationId: String, isGroup: Boolean) {
        markEventAsReadUseCase
            .execute(MarkAsReadNotificationParams(notificationId, isGroup))
            .subscribe({ requestCounter() }, {})
            .addDisposable()
    }

    private fun requestCounter() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val response = notificationCounterUseCase.getCounter()
                if (response.data != null) {
                    val count = response.data.count ?: 0
                    Timber.d("RESP counter:$count")
                    _liveUnreadNotificationCounter.postValue(count)
                } else {
                    Timber.e("ERROR when request counter")
                }
            }.onFailure {
                Timber.e("Network error when request counter:${it.message}")
            }
        }
    }

    fun deleteCommunity(id: Long) {
        viewModelScope.launch {
            val communityId = id.toInt()
            if (communityId != CommunityConstant.UNKNOWN_COMMUNITY_ID) {
                DeleteCommunityUseCaseParams(communityId).also { parameters ->
                    deleteCommunityUseCase.execute(
                        params = parameters,
                        success = { success ->
                            Timber.d("Success delete community $id")
                        },
                        fail = { error: Exception? ->
                            Timber.e(error)
                        }
                    )
                }

            }
        }
    }

    fun restoreCommunity(communityId: Long) {
        viewModelScope.launch {
            deleteCommunityUseCase.deletionCommunityCancel(communityId)
        }
    }

    private fun checkDeepLinkAndOpen(uri: Uri) {
        val deeplinkWithOrigin =
            FeatureDeepLink.addDeeplinkOrigin(uri.toString(), DeeplinkOrigin.APP_VIEW)
        when {
            FeatureDeepLink.isNeedOpenWithOutAuth(uri.path.orEmpty()) -> {
                liveDeeplinkActions.value =
                    DeeplinkActionViewEvent.HandleDeepLink(deeplinkWithOrigin)
            }

            appSettings.get().isAuthorizedUser() -> {
                liveDeeplinkActions.value =
                    DeeplinkActionViewEvent.HandleDeepLink(deeplinkWithOrigin)
            }

            else -> {
                liveDeeplinkActions.value = DeeplinkActionViewEvent.NotAuthorized
            }
        }
    }

    private fun observeCommunityEvents() {
        viewModelScope.launch {
            communityChangesUseCase.invoke().collect {
                _liveViewEvents.emit(CommunityChanges(it))
            }
        }
    }

    private fun getAppInfo() {
        viewModelScope.launch {
            try {
                val appInfo = getAppInfoUseCase.get().executeAsync().await()
                handleAppInfoResponse(appInfo)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private fun loadStickers() {
        viewModelScope.launch {
            runCatching {
                getMediaKeyboardStickersUseCase.invoke()
            }.onFailure { Timber.e(it) }
        }
    }

    private fun observeAuthFinish() {
        authFinishListener.observeAuthFinishListener()
            .onEach {
                getAppInfo()
                loadStickers()
                livePeopleBadge.value = needShowPeopleBadge()
            }
            .launchIn(viewModelScope)
        authFinishListener.observeRegistrationFinishListener()
            .onEach {
                getAppInfo()
                loadStickers()
                livePeopleBadge.value = needShowPeopleBadge()
            }
            .launchIn(viewModelScope)
    }

    private fun loadSignupCountries() {
        viewModelScope.launch {
            runCatching {
                loadSignupCountriesUseCase.invoke()
            }.onFailure {
                Timber.e(it)
            }
        }
    }

    private fun loadAndCacheReferralInfo() {
        viewModelScope.launch {
            runCatching {
                loadAndCacheReferralInfoUseCase.invoke()
            }.onFailure {
                Timber.e(it)
            }
        }
    }

    private suspend fun handleAppInfoResponse(settings: Settings?) {
        getUserBirthdayDialogShownUseCase.get().invoke().set(
            settings?.showBirthdayCongratulation == NEED_SHOW_DIALOG
        )
        handleSubscribersFlag(settings)
        handleSupportUserId(settings?.supportUserId)
        saveAdminSupportIdPref(settings)
        handleBirthdayToken(settings?.showBirthdayCongratulation)
        appSettings.get().saveAppLinks(settings?.links)
        settings?.currentApp?.let { currentInfo -> checkUpdateScreen(currentInfo) }
    }

    private fun checkUpdateScreen(currentInfo: CurrentInfo) {
        currentInfo.version?.let { _ ->
            viewModelScope.launch {
                _liveViewEvents.emit(
                    UpdateScreenEvent(
                        currentInfo.notes, currentInfo.version
                    )
                )
            }
        }
    }

    private suspend fun handleBirthdayToken(showBirthdayDialog: Int?) {
        showBirthdayDialog?.let { isShow ->
            Timber.d("birthday dialog shown: $isShow")
            appSettings.get().isNeedShowBirthdayDialog.set(
                isShow == NEED_SHOW_DIALOG
            )
        }
    }

    private fun saveAdminSupportIdPref(settings: Settings?) {
        settings?.appInfo
            ?.find { it.name == ADMIN_SUPPORT_ID_NAME }
            ?.value
            ?.toLongOrNull()
            ?.let(setAdminSupportIdUseCase::invoke)
    }

    private suspend fun handleSubscribersFlag(settings: Settings?) {
        settings?.appInfo?.forEach { model ->
            if (model.name == SHOW_FRIENDS_SUBSCRIBERS_POPUP) {
                getFriendsSubscribersPopupPrivacyUseCase.invoke().set(
                    model.value == TRUE_VALUE
                )
            }
            return@forEach
        }
    }

    private fun handleSupportUserId(supportUserId: Long?) {
        supportUserId?.let { saveSupportUserId(it) }
    }

    private fun saveSupportUserId(supportUserId: Long?) {
        supportUserId?.let {
            appSettings.get().supportUserId = it
        }
    }

    private fun emitViewEffect(typeEvent: HolidayViewEvent) {
        viewModelScope.launch {
            _holidayViewEvent.emit(typeEvent)
        }
    }

    private fun subsсribeFbToken() {
        if (appSettings.get().isAuthorizedUser()) {
            authenticatorDelegate.subscribePush()
        }
    }

    private fun initShakeObservers() {
        observeShakeChanged()
        observeShakeRequestRegisterChanged()
        observeShakePrivacySettingChanged()
    }

    private fun observeShakeChanged() {
        shakeEventListener.get().observeShakeChanged()
            .onEach { handleShakeEvent() }
            .catch { e -> Timber.d(e) }
            .launchIn(viewModelScope)
    }

    private fun handleShakeEvent() {
        amplitudeShakeAnalytic.logShakeTap(
            howCalled = AmplitudeShakeHowProperty.SHAKE,
            userId = getUserUidUseCase.invoke(),
            where = AmplitudeShakeWhereProperty.ACTION
        )
        emitViewEffect(ShowShakeDialogUiEffect)
    }

    private fun observeShakeRequestRegisterChanged() {
        observeRegisterShakeEventUseCase.get().invoke()
            .onEach(::handleNeedToRegisterShake)
            .catch { e -> Timber.d(e) }
            .launchIn(viewModelScope)
    }

    private fun handleNeedToRegisterShake(shakeEvent: ShakeEvent) {
        when (shakeEvent) {
            is ShakeEvent.TryToRegisterShakeEvent -> {
                emitViewEffect(TryToRegisterShakeEvent(shakeEvent.isNeedToRegister))
            }

            is ShakeEvent.ForceToRegisterShakeEvent -> {
                handleForceIsNeedRegisterEvent(shakeEvent.isNeedToRegister)
            }

            else -> Unit
        }
    }

    private fun handleForceIsNeedRegisterEvent(needToRegister: Boolean) {
        if (needToRegister) {
            shakeEventListener.get().registerShakeEventListener()
        } else {
            shakeEventListener.get().unregisterShakeEventListener()
        }
    }

    private fun observeShakePrivacySettingChanged() {
        viewModelScope.launch {
            runCatching {
                observeShakePrivacySetting.get().invoke().asFlow().drop(1).collect { shakeAvailable ->
                    viewModelScope.launch { _liveViewEvents.emit(TryToRegisterShakeByPrivacySetting(shakeAvailable == true)) }
                }
            }.onFailure { Timber.e(it) }
        }
    }

    private fun syncContactsIfAllowed(settings: List<PrivacySettingModel>) {
        settings.find { it.key == SettingsKeyEnum.ALLOW_CONTACT_SYNC.key }
            ?.value.toBoolean()
            .let { isAllowSyncContactsBySetting ->
                if (isAllowSyncContactsBySetting && readContactsPermissionProvider.get().hasContactsPermission()) {
                    viewModelScope.launch { startSyncContactsUseCase.get().invoke() }
                }
                checkNeedResetSettingsSyncContactsSetting(isAllowSyncContactsBySetting)
            }
    }

    private fun checkNeedResetSettingsSyncContactsSetting(isAllowSyncContactsBySetting: Boolean) {
        if (isAllowSyncContactsBySetting && readContactsPermissionProvider.get().hasContactsPermission().not()) {
            setSettingsUseCase.get().invoke(
                SettingsParams.CommonSettingsParams(
                    key = SettingsKeyEnum.ALLOW_CONTACT_SYNC.key,
                    value = FALSE_INT
                )
            )
        }
    }

    private fun needShowPeopleBadge(): Boolean = needShowPeopleBadgeUseCase.get().invoke()
        && isAuthorizedUserUseCase.get().isAuthorizedUser()

    private fun updatePeopleBadge() {
        livePeopleBadge.value = needShowPeopleBadge()
    }

    private fun checkIfNeedToShowScreenshotPopup() {
        if (getShareScreenshotEnabledUseCase.invoke().not()) return
        viewModelScope.launch {
            _liveViewEvents.emit(SendScreenshotTakenEventToFragment)
        }
    }

    private fun logOpenCommunityFromDeeplink() {
        analyticsInteractor.logCommunityScreenOpened(
            AmplitudePropertyWhereCommunityOpen.DEEPLINK
        )
    }

    private fun observePeopleBadgeChanged() {
        if (needShowPeopleBadgeUseCase.get().invoke().not()) return
        observeNeedShowPeopleBadgeUseCase.get().invoke()
            .catch { e -> Timber.e(e) }
            .onEach { updatePeopleBadge() }
            .launchIn(viewModelScope)
    }

    private fun startListeningSyncNotificationService() {
        syncNotificationService.startListening()
    }

    private fun stopListeningSyncNotificationService() {
        syncNotificationService.stopListening()
    }

    fun fragmentAdded() {
        viewModelScope.launch {
            navigationListener.fragmentChanged(NavigationActionType.FragmentAdded)
        }
    }

    companion object {
        const val YEAR_DAYS_THRESHOLD_MIN = 1
        const val YEAR_DAYS_THRESHOLD_MAX = 364
        private const val GET_BIRTHDAY_STATE_DELAY = 500L
        private const val DEFAULT_SEND_DIALOG_AFTER_TIME = "06:00"
    }
}

sealed class ReactionEvent {
    data class ShowAlert(val message: String) : ReactionEvent()
    data class Error(val message: String) : ReactionEvent()
    object UnknownError : ReactionEvent()
}

sealed class MainActivityViewEvent
object Logout : MainActivityViewEvent()

data class ReadyForRestartAppAfterLogout(
    val isReady: Boolean = true
) : MainActivityViewEvent()

object OnSocketError : MainActivityViewEvent()
object OnPreferencesReady :
    MainActivityViewEvent() // событие получения профиля и обновления данных о токене в шаред префах

object OnCheckServiceConnection : MainActivityViewEvent()
object OnBindSignallingService : MainActivityViewEvent()

data class OnSupportUserIdReady(val userId: Long) : MainActivityViewEvent()

class OnAddReaction(val isFromBubble: Boolean) : MainActivityViewEvent()

sealed class HolidayViewEvent
class OnHolidayReady(val needToShowHoliday: Boolean, val isHolidayIntroduced: Boolean) : HolidayViewEvent()
object OnHolidayFailed : HolidayViewEvent()

class ShowBirthdayDialogEvent(val isBirthdayToday: Boolean) : HolidayViewEvent()
object ShowShakeDialogUiEffect : HolidayViewEvent()
object ConnectShakeSocketUiEffect : HolidayViewEvent()
data class TryToRegisterShakeEvent(val isNeedToRegisterShake: Boolean) : HolidayViewEvent()
data class TryToRegisterShakeByPrivacySetting(val shakeRegistered: Boolean) : MainActivityViewEvent()
class CommunityChanges(val communityListEvents: CommunityListEvents) : MainActivityViewEvent()
object NavigateToAppSettingsUiEffect : MainActivityViewEvent()
object NavigateLocationSettingsUiEffect : MainActivityViewEvent()
object SendScreenshotTakenEventToFragment : MainActivityViewEvent()
data class UpdateScreenEvent(var infos: List<String>?, var appVerName: String?) :MainActivityViewEvent()
