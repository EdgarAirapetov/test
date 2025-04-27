package com.numplates.nomera3.modules.userprofile.ui.viewModel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.meera.core.base.viewmodel.BaseViewModel
import com.meera.core.extensions.doAsyncViewModel
import com.meera.core.extensions.needToUpdateStr
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.tryCatch
import com.meera.core.preferences.AppSettings
import com.meera.core.utils.files.FileManager
import com.noomeera.nmrmediatools.NMRPhotoAmplitude
import com.numplates.nomera3.FRIEND_STATUS_CONFIRMED
import com.numplates.nomera3.FRIEND_STATUS_INCOMING
import com.numplates.nomera3.FRIEND_STATUS_NONE
import com.numplates.nomera3.FRIEND_STATUS_OUTGOING
import com.numplates.nomera3.R
import com.numplates.nomera3.domain.interactornew.GetSyncContactsPrivacyUseCase
import com.numplates.nomera3.domain.interactornew.GetUserBirthdayDialogShownFlowUseCase
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.domain.interactornew.HideUserPostsUseCase
import com.numplates.nomera3.domain.interactornew.ObserveSyncContactsUseCase
import com.numplates.nomera3.domain.interactornew.ProcessAnimatedAvatar
import com.numplates.nomera3.domain.interactornew.SubscriptionUseCase
import com.numplates.nomera3.domain.interactornew.UpdateBirthdayShownUseCase
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.auth.util.AuthRequester
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.baseCore.ResourceManager
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyAnimatedAvatarFrom
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyAvatarType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyChatCreatedFromWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyProfileShare
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereCommunityOpen
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereMapPrivacy
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereOpenMap
import com.numplates.nomera3.modules.baseCore.helper.amplitude.FriendAddAction
import com.numplates.nomera3.modules.baseCore.helper.amplitude.add_friend.AmplitudeAddFriendAnalytic
import com.numplates.nomera3.modules.baseCore.helper.amplitude.editor.AmplitudeEditor
import com.numplates.nomera3.modules.baseCore.helper.amplitude.editor.AmplitudeEditorParams
import com.numplates.nomera3.modules.baseCore.helper.amplitude.editor.AmplitudeEditorTypeProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.followbutton.AmplitudeFollowButton
import com.numplates.nomera3.modules.baseCore.helper.amplitude.followbutton.AmplitudeFollowButtonPropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.followbutton.AmplitudePropertyType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapsnippet.model.MapSnippetCloseMethod
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mutual_friends.AmplitudeHowSelectedMutualFriendsProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mutual_friends.AmplitudeMutualFriendsAnalytic
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mutual_friends.AmplitudeSelectedMutualFriendsTabProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.people.AmplitudePeopleAnalytics
import com.numplates.nomera3.modules.baseCore.helper.amplitude.people.AmplitudePeopleWhereProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.people.AmplitudePeopleWhich
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeAlertPostWithNewAvatarValuesActionType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeAlertPostWithNewAvatarValuesFeedType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudePhotoActionValuesWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeProfile
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeProfileEditTapProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeUserCardHideSectionProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeUserCardHideWhereProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.createInfluencerAmplitudeProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.sync_contacts.AmplitudeSyncContactsWhereProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.sync_contacts.SyncContactsAnalytic
import com.numplates.nomera3.modules.chat.domain.usecases.CacheCompanionUserForChatInitUseCase
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitProfileData
import com.numplates.nomera3.modules.chat.toolbar.domain.usecase.GetChatUserInfoUseCase
import com.numplates.nomera3.modules.chat.toolbar.domain.usecase.UpdateUserDataObserverUseCase
import com.numplates.nomera3.modules.communities.domain.usecase.CommunityListEventsUseCase
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.feed.data.entity.FeedUpdateEvent
import com.numplates.nomera3.modules.feed.domain.usecase.GetFeedStateUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.ReactiveUpdateSubscribeUserUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.UpdateSubscriptionUserParams
import com.numplates.nomera3.modules.maps.domain.analytics.MapAnalyticsInteractor
import com.numplates.nomera3.modules.maps.ui.snippet.model.SnippetState
import com.numplates.nomera3.modules.moments.show.domain.SubscribeMomentsEventsUseCase
import com.numplates.nomera3.modules.moments.show.domain.model.MomentRepositoryEvent
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentClickOrigin
import com.numplates.nomera3.modules.peoples.domain.usecase.ClearSavedPeopleContentUseCase
import com.numplates.nomera3.modules.peoples.domain.usecase.RemoveRelatedUserUseCase
import com.numplates.nomera3.modules.registration.domain.UserCallUnavailableUseCase
import com.numplates.nomera3.modules.tracker.FireBaseAnalytics
import com.numplates.nomera3.modules.tracker.ITrackerActions
import com.numplates.nomera3.modules.tracker.ScreenNamesEnum
import com.numplates.nomera3.modules.upload.domain.UploadStatus
import com.numplates.nomera3.modules.upload.domain.usecase.post.GetUploadStateUseCase
import com.numplates.nomera3.modules.user.data.mapper.UserCoordinateUIMapper
import com.numplates.nomera3.modules.user.domain.effect.UserSettingsEffect
import com.numplates.nomera3.modules.user.domain.usecase.AddUserToFriendParams
import com.numplates.nomera3.modules.user.domain.usecase.AddUserToFriendUseCase
import com.numplates.nomera3.modules.user.domain.usecase.BlockStatusUseCase
import com.numplates.nomera3.modules.user.domain.usecase.BlockSuggestionUseCase
import com.numplates.nomera3.modules.user.domain.usecase.ChatDisableUseCase
import com.numplates.nomera3.modules.user.domain.usecase.ChatEnableUseCase
import com.numplates.nomera3.modules.user.domain.usecase.DefBlockParams
import com.numplates.nomera3.modules.user.domain.usecase.DisableChatParams
import com.numplates.nomera3.modules.user.domain.usecase.EmitSuggestionRemovedUseCase
import com.numplates.nomera3.modules.user.domain.usecase.EnableChatParams
import com.numplates.nomera3.modules.user.domain.usecase.GetShareProfileLinkUseCase
import com.numplates.nomera3.modules.user.domain.usecase.GetUserMapStateUseCase
import com.numplates.nomera3.modules.user.domain.usecase.GetUserSettingsStateChangedUseCase
import com.numplates.nomera3.modules.user.domain.usecase.GetWebSocketEnabledUseCase
import com.numplates.nomera3.modules.user.domain.usecase.PhoneCallsDisableUseCase
import com.numplates.nomera3.modules.user.domain.usecase.PhoneCallsEnableUseCase
import com.numplates.nomera3.modules.user.domain.usecase.PushBlockStatusChangedUseCase
import com.numplates.nomera3.modules.user.domain.usecase.PushFriendStatusChangedUseCase
import com.numplates.nomera3.modules.user.domain.usecase.RemoveUserFromFriendAndSaveSubscriptionParams
import com.numplates.nomera3.modules.user.domain.usecase.RemoveUserFromFriendAndSaveSubscriptionUseCase
import com.numplates.nomera3.modules.user.domain.usecase.RemoveUserFromFriendAndUnsubscribeParams
import com.numplates.nomera3.modules.user.domain.usecase.RemoveUserFromFriendAndUnsubscribeUseCase
import com.numplates.nomera3.modules.user.domain.usecase.SaveAvatarStateLocallyUseCase
import com.numplates.nomera3.modules.user.domain.usecase.UpdateUserAvatarUseCase
import com.numplates.nomera3.modules.user.ui.entity.UserCoordinatesUIModel
import com.numplates.nomera3.modules.user.ui.event.PhoneCallsViewEffect
import com.numplates.nomera3.modules.user.ui.event.RoadPostViewEffect
import com.numplates.nomera3.modules.user.ui.event.UserInfoViewEffect
import com.numplates.nomera3.modules.user.ui.event.UserProfileDialogNavigation
import com.numplates.nomera3.modules.user.ui.event.UserProfileNavigation
import com.numplates.nomera3.modules.user.ui.event.UserProfileTooltipEffect
import com.numplates.nomera3.modules.user.ui.utils.UserBirthdayUtils
import com.numplates.nomera3.modules.userprofile.domain.interactor.TooltipProfileInteractor
import com.numplates.nomera3.modules.userprofile.domain.maper.PhotoModelMapper
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserProfileModel
import com.numplates.nomera3.modules.userprofile.domain.usecase.GetOwnLocalProfileUseCase
import com.numplates.nomera3.modules.userprofile.domain.usecase.GetProfileSuggestionsUseCase
import com.numplates.nomera3.modules.userprofile.domain.usecase.GetProfileUseCase
import com.numplates.nomera3.modules.userprofile.domain.usecase.GetUserAvatarsUseCase
import com.numplates.nomera3.modules.userprofile.domain.usecase.ObserveLocalOwnUserProfileModelUseCase
import com.numplates.nomera3.modules.userprofile.domain.usecase.UpdateOwnUserProfileUseCase
import com.numplates.nomera3.modules.userprofile.profilestatistics.domain.usecase.GetProfileStatisticsSlidesUseCase
import com.numplates.nomera3.modules.userprofile.ui.entity.GroupUIModel
import com.numplates.nomera3.modules.userprofile.ui.entity.ProfileSuggestionUiModels
import com.numplates.nomera3.modules.userprofile.ui.entity.ProfileSuggestionsFloorUiEntity
import com.numplates.nomera3.modules.userprofile.ui.entity.UserEntityFriendSubscribeFloor
import com.numplates.nomera3.modules.userprofile.ui.entity.UserEntityMapFloor
import com.numplates.nomera3.modules.userprofile.ui.entity.VehicleUIModel
import com.numplates.nomera3.modules.userprofile.ui.mapper.GiftItemUIMapper
import com.numplates.nomera3.modules.userprofile.ui.mapper.ProfileUIListMapper
import com.numplates.nomera3.modules.userprofile.ui.mapper.UserDetailsMapper
import com.numplates.nomera3.modules.userprofile.ui.mapper.UserProfileUIMapper
import com.numplates.nomera3.modules.userprofile.ui.mapper.UserSuggestionsUiMapper
import com.numplates.nomera3.modules.userprofile.ui.model.FriendStatus
import com.numplates.nomera3.modules.userprofile.ui.model.PhotoModel
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileStateUIModel
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIAction
import com.numplates.nomera3.modules.userprofile.ui.model.UserProfileUIModel
import com.numplates.nomera3.modules.usersettings.domain.usecase.GetLocalSettingsUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.GetSettingsUseCase
import com.numplates.nomera3.modules.usersettings.ui.mapper.PrivacySettingUiMapper
import com.numplates.nomera3.presentation.model.enums.CreateAvatarPostEnum
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum
import com.numplates.nomera3.presentation.upload.IUploadContract
import com.numplates.nomera3.presentation.utils.networkconn.NetworkStatusProvider
import com.numplates.nomera3.presentation.view.fragments.FriendsHostFragmentNew
import com.numplates.nomera3.presentation.view.fragments.notificationsettings.subscription.SubscriptionNotificationsUseCase
import com.numplates.nomera3.presentation.view.utils.apphints.TooltipDuration
import com.numplates.nomera3.presentation.view.utils.apphints.TooltipDuration.UNIQUE_NAME_TOOLTIP_DELAY
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class UserProfileViewModel @AssistedInject constructor(
    private val hideUserPosts: HideUserPostsUseCase,
    private val blockStatusUseCase: BlockStatusUseCase,
    private val enableChatUseCase: ChatEnableUseCase,
    private val disableChatUseCase: ChatDisableUseCase,
    private val phoneCallsDisableUseCase: PhoneCallsDisableUseCase,
    private val phoneCallsEnableUseCase: PhoneCallsEnableUseCase,
    private val uploadHelper: IUploadContract,
    private val subscriptions: SubscriptionUseCase,
    private val subscriptionNotificationUseCase: SubscriptionNotificationsUseCase,
    private val myTracker: ITrackerActions,
    private val getSettingsUseCase: GetSettingsUseCase,
    private val addUserToFriendUseCase: AddUserToFriendUseCase,
    private val removeUserFromFriendAndUnsubscribeUseCase: RemoveUserFromFriendAndUnsubscribeUseCase,
    private val removeUserFromFriendAndSaveSubscriptionUseCase: RemoveUserFromFriendAndSaveSubscriptionUseCase,
    private val processAnimatedAvatar: ProcessAnimatedAvatar,
    private val appSettings: AppSettings,
    private val getUserProfileUseCase: GetProfileUseCase,
    private val getOwnProfileUseCase: UpdateOwnUserProfileUseCase,
    private val analyticsInteractor: AnalyticsInteractor,
    private val amplitudeFollowButton: AmplitudeFollowButton,
    private val updateUserDataObserverUseCase: UpdateUserDataObserverUseCase,
    private val networkStatusProvider: NetworkStatusProvider,
    private val getUserUidUseCase: GetUserUidUseCase,
    private val getProfileStatisticsSlidesUseCase: GetProfileStatisticsSlidesUseCase,
    private val pushFriendStatusChanged: PushFriendStatusChangedUseCase,
    private val pushBlockStatusChanged: PushBlockStatusChangedUseCase,
    private val reactiveUpdateSubscribeUserUseCase: ReactiveUpdateSubscribeUserUseCase,
    private val privacySettingsUiMapper: PrivacySettingUiMapper,
    private val getUserBirthdayDialogShownFlowUseCase: GetUserBirthdayDialogShownFlowUseCase,
    private val amplitudeMutualFriends: AmplitudeMutualFriendsAnalytic,
    private val fbAnalytic: FireBaseAnalytics,
    private val followButtonAnalytic: AmplitudeFollowButton,
    private val amplitudeAddFriendAnalytic: AmplitudeAddFriendAnalytic,
    private val communityChangesUseCase: CommunityListEventsUseCase,
    private val userBirthdayUtils: UserBirthdayUtils,
    private val userProfileUseCaseNew: GetOwnLocalProfileUseCase,
    private val updateBirthdayShownUseCase: UpdateBirthdayShownUseCase,
    private val observeOwnProfileFlow: ObserveLocalOwnUserProfileModelUseCase,
    private val getUserMapCoordinateUseCase: GetUserMapStateUseCase,
    private val userMapCoordinateUIMapper: UserCoordinateUIMapper,
    private val getWebSocketEnabledUseCase: GetWebSocketEnabledUseCase,
    private val updateUserAvatarUseCase: UpdateUserAvatarUseCase,
    private val getUserSettingsUseCase: GetSettingsUseCase,
    private val saveAvatarStateLocally: SaveAvatarStateLocallyUseCase,
    private val getShareProfileLinkUseCase: GetShareProfileLinkUseCase,
    private val userDetailsMapper: UserDetailsMapper,
    private val giftItemUIMapper: GiftItemUIMapper,
    private val userProfileMapper: UserProfileUIMapper,
    private val profileUIListMapper: ProfileUIListMapper,
    private val amplitudeProfile: AmplitudeProfile,
    private val getChatUserInfoUseCase: GetChatUserInfoUseCase,
    private val getProfileSuggestionsUseCase: GetProfileSuggestionsUseCase,
    private val getFeedStateUseCase: GetFeedStateUseCase,
    uploadState: GetUploadStateUseCase,
    private val clearSavedPeopleContentUseCase: ClearSavedPeopleContentUseCase,
    private val fileManager: FileManager,
    private val amplitudePeopleAnalytics: AmplitudePeopleAnalytics,
    private val getUserSettingsStateChangedUseCase: GetUserSettingsStateChangedUseCase,
    private val userSuggestionsUiMapper: UserSuggestionsUiMapper,
    private val observeSyncContactsUseCase: ObserveSyncContactsUseCase,
    private val getSyncContactsPrivacyUseCase: GetSyncContactsPrivacyUseCase,
    private val blockSuggestionUseCase: BlockSuggestionUseCase,
    private val emitSuggestionRemovedUseCase: EmitSuggestionRemovedUseCase,
    private val removeRelatedUserUseCase: RemoveRelatedUserUseCase,
    private val subscribeMomentsEventsUseCase: SubscribeMomentsEventsUseCase,
    private val resourceManager: ResourceManager,
    private val profileTooltipInteractor: TooltipProfileInteractor,
    private val syncContactsAnalytic: SyncContactsAnalytic,
    private val cacheCompanionUserUseCase: CacheCompanionUserForChatInitUseCase,
    private val userCallUnavailableUseCase: UserCallUnavailableUseCase,
    private val getLocalSettingsUseCase: GetLocalSettingsUseCase,
    private val mapAnalyticsInteractor: MapAnalyticsInteractor,
    private val featureTogglesContainer: FeatureTogglesContainer,
    private val getUserAvatarUseCase: GetUserAvatarsUseCase,
    private val photoModelMapper: PhotoModelMapper,
    private val amplitudeEditor: AmplitudeEditor,
    @Assisted private val authRequester: AuthRequester?
) : BaseViewModel<UserProfileStateUIModel?, UserInfoViewEffect, UserProfileUIAction>() {

    private val uploadWorkObserver = Observer<WorkInfo> { work ->
        work?.let { nonNullWork ->
            launchEffect(UserInfoViewEffect.UploadImageInfo(nonNullWork))
        }
    }
    private var liveUploadMediaToGallery: LiveData<WorkInfo>? = null

    private var isUserSnippet: Boolean = false

    private val profileSuggestions = mutableListOf<ProfileSuggestionUiModels>()
    private var showSuggestionsIfAvailable = false

    private val amplitudeWhere
        get() = if (isUserSnippet) AmplitudePropertyWhere.MAP_SNIPPET else AmplitudePropertyWhere.USER_PROFILE

    private val disposables = CompositeDisposable()
    private var avatarEditorOpenedTimeStamp: Long = -1L

    /**
     * Было ли затрекано событие показа кнопки обновите приложение
     * Кнопка показывается раз в сессию
     * */
    private var isTrackedUpdateBtnVisibility = false

    private var appVersionName: String? = null
    private var serverAppVersionName: String? = null
    private var userCoordinatesUIModel: UserCoordinatesUIModel? = null

    private var userId: Long? = null
    private var isMyFriend: Boolean = false
    private var onBindInvoked = false
    private var isSendProfileEntranceLog = false

    private var numberOfElements = 0
    private val avatarsList = mutableListOf<PhotoModel>()

    private var gettingAvatarsJob: Job? = null
    private var uploadingMoment = false

    init {
        observeBirthdayDialogNeedToShow()
        uploadState.invoke()
            .onEach {
                when (it.status) {
                    UploadStatus.Processing -> {
                        uploadingMoment = true
                    }
                    UploadStatus.Success -> {
                        uploadingMoment = false
                        emitEffect(UserInfoViewEffect.OnRefreshUserRoad)
                    }
                    else -> Unit
                }
            }
            .launchIn(viewModelScope)
        observeCommunityChangesEvents()
        observeSubscriptionChangesForSuggestions()
        observeSyncContactsWorker()
        observeRemoveSuggestion()
        observeMomentsEvents()
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
        liveUploadMediaToGallery?.removeObserver(uploadWorkObserver)
    }

    fun init(appVersionName: String?, serverAppVersionName: String?, userId: Long) {
        this.appVersionName = appVersionName
        this.serverAppVersionName = serverAppVersionName
        this.userId = userId
        if (userId != getUserUid()) {
            loadProfileSuggestions(userId)
        }
        refreshProfile()
        observeCompanionUserUpdates()
        checkIfNeedToShowProfileStatistics()
        if (isMe()) observeDbProfile()
    }

    override fun handleUIAction(action: UserProfileUIAction) {
        when (action) {
            is UserProfileUIAction.UpdateButtonClicked -> handleUpdateClicked()
            is UserProfileUIAction.AddPhoto -> handleAddPhotoClicked()
            is UserProfileUIAction.OnShowMoreSuggestionsClicked -> handleShowMoreSuggestionsClicked()
            is UserProfileUIAction.OnSubscribeClicked -> handleSubscribeClicked(action)
            is UserProfileUIAction.OnSuggestionUserClicked -> handleUserClicked(action)
            is UserProfileUIAction.UploadToGallery -> handleUploadToGallery(action)
            is UserProfileUIAction.OnCongratulationClick -> handleGiftsListClicked(true)
            is UserProfileUIAction.OnGiftsListClick -> handleGiftsListClicked()
            is UserProfileUIAction.OnGiftClick -> handleGiftClicked()
            is UserProfileUIAction.StartChatClick -> handleStartChatClick()
            is UserProfileUIAction.FragmentViewCreated -> handleFragmentCreated(action)
            is UserProfileUIAction.ShowDotsMenuAction -> handleShowDotsMenu()
            is UserProfileUIAction.OnShareProfileClickAction -> handleShareProfile()
            is UserProfileUIAction.OnLiveAvatarChanged -> handleLiveAvatarChanged(action)
            is UserProfileUIAction.OnPublishOptionsSelected -> uploadUserAvatar(action)
            is UserProfileUIAction.OnCopyProfileClickedAction -> handleCopyProfileClicked()
            is UserProfileUIAction.OnChatPrivacyClickedAction -> handleChatPrivacyClicked()
            is UserProfileUIAction.OnCallPrivacyClickedAction -> handleCallsPrivacyClicked()
            is UserProfileUIAction.OnBlacklistUserClickedAction -> handleBlackListedUserClicked()
            is UserProfileUIAction.ChangePostsPrivacyClickedAction -> handlePostsPrivacyClicked(action)
            is UserProfileUIAction.RemoveFriendClickedAction -> handleRemoveFriendClick(action)
            is UserProfileUIAction.UnsubscribeFromUserClickedAction -> handleUnsubscribe()
            is UserProfileUIAction.SaveUserAvatarToGalleryAction -> handleSaveAvatarToGallery(action.position)
            is UserProfileUIAction.OnAvatarChangeClicked -> handleAvatarChange()
            is UserProfileUIAction.OnOpenProfileClicked -> handleOpenProfileClicked()
            is UserProfileUIAction.CreateAvatar -> handleCreateAvatar()
            is UserProfileUIAction.OnEditorOpen -> logOpenEditor()
            is UserProfileUIAction.EditAvatar -> action.nmrAmplitude?.let(::handleEditAvatar)
            is UserProfileUIAction.OnAddFriendClicked -> handleOnAddFriendClicked(action)
            is UserProfileUIAction.OnMapClicked -> handleMapClicked(action)
            is UserProfileUIAction.OnSendGiftClicked -> handleGiftClicked(action)
            is UserProfileUIAction.ClickSubscribeNotification -> handleSubscribeNotification(action)
            is UserProfileUIAction.RemoveFriendAndUnsubscribe -> removeFriendsAndUnsubscribe(action)
            is UserProfileUIAction.UnsubscribeFromUserDialogClickedAction -> unsubscribeUser(action)
            is UserProfileUIAction.OnTryToCall -> handleOnTryToCall(action)
            is UserProfileUIAction.SetSuggestionsEnabled -> handleEnablingSuggestions(action)
            is UserProfileUIAction.SubscribeSuggestion -> handleSubscribeSuggestion(action)
            is UserProfileUIAction.AddFriendSuggestion -> handleAddFriendSuggestion(action)
            is UserProfileUIAction.UnsubscribeSuggestion -> handleUnsubscribeSuggestion(action)
            is UserProfileUIAction.RemoveFriendSuggestion -> handleRemoveFriendSuggestion(action)
            is UserProfileUIAction.LogPeopleSelectedFromSuggestionUiAction -> logPeopleSelectedFromSuggestion()
            is UserProfileUIAction.BlockSuggestionById -> handleBlockSuggestion(action)
            is UserProfileUIAction.HandleNavigateSyncContactsUiAction -> handleNavigateSyncContactsAction()
            is UserProfileUIAction.OnFragmentStart -> handleFragmentStart()
            is UserProfileUIAction.OnHolderBind -> handleHolderBind(action.position)
            is UserProfileUIAction.TopMarkerClick -> handleTopMarkerClick()
            is UserProfileUIAction.SubscribersCountClick -> handleSubscribersCountClick()
            is UserProfileUIAction.UniqueNameClick -> handleUniqueNameClicked()
            is UserProfileUIAction.OnFriendClicked -> handleOnFriendsClicked()
            is UserProfileUIAction.OnNewPostClicked -> handleOnNewPostClicked(action.isWithImages)
            is UserProfileUIAction.OnVehicleClick -> handleVehicleClicked(action.vehicle)
            is UserProfileUIAction.OnAddVehicleClick -> handleAddVehicleClicked()
            is UserProfileUIAction.OnAllVehiclesClick -> handleOnAllVehicleClicked()
            is UserProfileUIAction.OnPrivacyClick -> handlePrivacyClicked()
            is UserProfileUIAction.RequestMapPrivacySettings -> handleRequestMapPrivacySettings()
            is UserProfileUIAction.OnFindGroup -> handleFindGroup()
            is UserProfileUIAction.OnAllGroupClick -> handleAllGroup()
            is UserProfileUIAction.OnCreateGroupClick -> handleCreateGroup()
            is UserProfileUIAction.OnGroupClicked -> handleGroupClicked(action.group)
            is UserProfileUIAction.OnFriendsListClicked -> handleFriendsListClicked()
            is UserProfileUIAction.OnSubscribersListClicked -> handleSubscribersListClicked()
            is UserProfileUIAction.OnSubscriptionsListClicked -> handleSubscriptionsListClicked()
            is UserProfileUIAction.DisabledSubscriberFloorClicked -> logDisabledShowFriendsListClicked()
            is UserProfileUIAction.OnMutualFriendsClicked -> handleMutualFriendsClicked()
            is UserProfileUIAction.OnShowImage -> handleShowImage(action.position)
            is UserProfileUIAction.OnGridGalleryClicked -> handleGridGalleryClicked()
            is UserProfileUIAction.OnLogProfileEntrance -> handleLogProfileEntrance(action)
            is UserProfileUIAction.OnMomentClicked -> handleMomentsClicked(
                action.openedFrom,
                action.existUserId,
                action.hasNewMoments
            )
            is UserProfileUIAction.GetUserDataForScreenshotPopup -> getUserDataForScreenshotPopup()
            UserProfileUIAction.LoadMoreAvatars -> requestAvatars()
            is UserProfileUIAction.OnComplainClick -> {
                launchEffect(UserProfileNavigation.OpenComplainFragment(
                    userId = action.userId,
                    where = amplitudeWhere
                ))
            }
            else -> throw IllegalArgumentException("Action is not supported")
        }
    }

    private fun handleMomentsClicked(
        openedFrom: MomentClickOrigin,
        existUserId: Long,
        hasNewMoments: Boolean?
    ) {
        if(isInternetConnected() && isWebSocketEnabled()) {
            launchEffect(
                UserProfileNavigation.OpenMoments(
                    openedFrom,
                    existUserId,
                    hasNewMoments
                )
            )
        } else {
            launchEffect(UserProfileNavigation.ShowInternetError)
        }
    }

    private fun getUserDataForScreenshotPopup() {
        if (uploadingMoment) return
        viewModelScope.launch {
            val userLink = getShareProfileLinkUseCase.invoke()
            launchEffect(UserProfileDialogNavigation.ShowScreenshotPopup(userLink))
        }
    }

    fun getMediaType(uri: Uri?) = fileManager.getMediaType(uri)

    private fun handleOnTryToCall(uiAction: UserProfileUIAction.OnTryToCall) {
        viewModelScope.launch {
            val companionInfo = runCatching { getChatUserInfoUseCase.invoke(uiAction.userId) }
                .onFailure { Timber.e(it) }
                .getOrNull()
            emitEffect(UserInfoViewEffect.CallToUser(companionInfo?.settingsFlags?.iCanCall == 1))
        }
    }

    fun getUserUid() = getUserUidUseCase.invoke()

    fun checkForStatisticsSlides() {
        checkIfNeedToShowProfileStatistics()
    }

    /**
     * Listen user got changes on server side
     */
    private fun observeCompanionUserUpdates() {
        if (isWebSocketEnabled()) {
            disposables.add(
                updateUserDataObserverUseCase
                    .observeUserData()
                    .subscribeOn(Schedulers.io())
                    .subscribe({ response ->
                        response.userId?.let { id ->
                            if (userId == id) {
                                requestProfile(id)
                            }
                        }
                    }, { Timber.e(it) })
            )
        } else {
            Timber.e("Web socket is not available!")
        }
    }

    fun isInternetConnected() = networkStatusProvider.isInternetConnected()

    private fun unsubscribeFromUser(
        userId: Long?,
        isApproved: Boolean,
        topContentMaker: Boolean,
        where: AmplitudeFollowButtonPropertyWhere = AmplitudeFollowButtonPropertyWhere.USER_PROFILE,
        success: () -> Unit
    ) {
        if (userId != null) {
            val analyticEventType = when {
                isUserSnippet -> AmplitudePropertyType.MAP_SNIPPET
                where == AmplitudeFollowButtonPropertyWhere.SUGGEST_USER_PROFILE -> AmplitudePropertyType.OTHER
                else -> AmplitudePropertyType.PROFILE
            }
            val whereValue = if (isUserSnippet) {
                AmplitudeFollowButtonPropertyWhere.USER_SNIPPET
            } else {
                where
            }
            viewModelScope.launch {
                unsubscribeUser(userId,
                    {
                        success.invoke()
                        val amplitudeInfluencerProperty = createInfluencerAmplitudeProperty(
                            topContentMaker = topContentMaker,
                            approved = isApproved
                        )
                        followButtonAnalytic.logUnfollowAction(
                            fromId = getUserUid(),
                            toId = userId,
                            where = whereValue,
                            type = analyticEventType,
                            amplitudeInfluencerProperty = amplitudeInfluencerProperty
                        )
                        updateUserSubscription(
                            userId = userId,
                            isSubscribed = false,
                            needToHideFollowButton = false,
                            isBlocked = false
                        )
                        pushFriendStatusChanged(
                            userId = userId,
                            isSubscribe = true
                        )
                        clearPeopleContent()
                    },
                    { Timber.e("Fail unsubscribe from user:$userId") })
            }
        }
    }

    fun logShareProfileInside(userId: Long?) {
        analyticsInteractor.logShareProfile(userId, AmplitudePropertyProfileShare.INSIDE)
    }

    fun logShareProfileOutside(userId: Long?) {
        analyticsInteractor.logShareProfile(userId, AmplitudePropertyProfileShare.OUTSIDE)
    }

    private fun logCopyProfileLink(userId: Long?) {
        analyticsInteractor.logShareProfile(userId, AmplitudePropertyProfileShare.LINK)
    }

    fun setHolidayShow(show: Boolean) {
        appSettings.isHolidayShowNeeded = show
    }

    private fun logBlockedUser(userId: Long, blockedUserId: Long) {
        analyticsInteractor.logBlockUser(userId, blockedUserId)
    }

    private fun logUnBlockUser(userId: Long, unBlockedUserId: Long) {
        analyticsInteractor.logUnblockUser(userId, unBlockedUserId)
    }

    fun isWebSocketEnabled(): Boolean =
        getWebSocketEnabledUseCase.invoke()

    fun callUnavailable(userId: Long) {
        viewModelScope.launch {
            userCallUnavailableUseCase.execute(userId = userId)
        }
    }

    fun identifyUserProperty(geoEnabled: Boolean) {
        analyticsInteractor.identifyUserProperty { identify ->
            identify.set(AmplitudePropertyNameConst.GEO_ENABLED, geoEnabled)
        }
    }

    /**
     * Get user coordinates on map
     */
    fun getMapUserState(userId: Long?) {
        userId ?: return
        if (userCoordinatesUIModel != null) return
        viewModelScope.launch {
            runCatching {
                val result = userMapCoordinateUIMapper
                    .mapDomainToUI(getUserMapCoordinateUseCase.invoke(userId))
                userCoordinatesUIModel = result
            }.onFailure { error ->
                Timber.e("ERROR: Get map user state: $error")
            }
        }
    }

    private fun addFriends(
        friendId: Long?,
        friendStatus: Int,
        successMessage: String,
        approved: Boolean,
        influencer: Boolean
    ) {
        myTracker.trackToFriendRequest()

        friendId?.let { id ->
            viewModelScope.launch(Dispatchers.IO) {
                addUserToFriendUseCase.execute(
                    params = AddUserToFriendParams(id),
                    success = {
                        Timber.d("SUCCESS Add new friend :)")
                        updateUserSubscription(
                            userId = id,
                            isSubscribed = true,
                            needToHideFollowButton = true,
                            isBlocked = false
                        )
                        launchEffect(UserInfoViewEffect.OnSuccessRequestAddFriend(successMessage))
                        analyticsInteractor.identifyUserProperty {
                            it.add(AmplitudePropertyNameConst.NUM_OF_FRIENDS, 1)
                        }
                        val influencerProperty = createInfluencerAmplitudeProperty(
                            topContentMaker = influencer,
                            approved = approved
                        )
                        amplitudeAddFriendAnalytic.logAddFriend(
                            fromId = getUserUid(),
                            toId = id,
                            type = if (isUserSnippet) FriendAddAction.MAP_SNIPPET else FriendAddAction.USER_PROFILE,
                            influencer = influencerProperty
                        )
                        pushFriendStatusChanged(
                            userId = friendId,
                            isSubscribe = friendStatus == FriendStatus.FRIEND_STATUS_INCOMING.intStatus
                        )
                        clearPeopleContent()
                    },
                    fail = { exception ->
                        Timber.e("Error add friend :( [${exception.message}]")
                        launchEffect(UserInfoViewEffect.OnFailureRequestAddFriend)
                    }
                )
            }
        }
    }

    private fun removeFriends(
        friendId: Long?,
        message: String = "",
        cancellingFriendRequest: Boolean = false
    ) {
        friendId?.let { id ->
            viewModelScope.launch(Dispatchers.IO) {
                removeUserFromFriendAndSaveSubscriptionUseCase.execute(
                    params = RemoveUserFromFriendAndSaveSubscriptionParams(id),
                    success = {
                        val event = if (cancellingFriendRequest) {
                            UserInfoViewEffect.OnSuccessCancelFriendRequest(false)
                        } else {
                            UserInfoViewEffect.OnSuccessRemoveFriend(false, message)
                        }
                        launchEffect(event)

                        analyticsInteractor.identifyUserProperty {
                            it.add(AmplitudePropertyNameConst.NUM_OF_FRIENDS, -1)
                        }
                        analyticsInteractor.logDelFriend(getUserUid(), id)
                        pushFriendStatusChanged(friendId)
                        updateUserSubscription(
                            userId = id,
                            isSubscribed = false,
                            needToHideFollowButton = false,
                            isBlocked = false
                        )
                        clearPeopleContent()
                    },
                    fail = {
                        launchEffect(UserInfoViewEffect.OnFailureRemoveFriend)
                        Timber.e(it)
                    }
                )
            }
        }
    }

    private fun removeFriendsAndUnsubscribe(uiAction: UserProfileUIAction.RemoveFriendAndUnsubscribe) {
        val userId = _state.value?.profile?.userId ?: return
        viewModelScope.launch(Dispatchers.IO) {
            removeUserFromFriendAndUnsubscribeUseCase.execute(
                params = RemoveUserFromFriendAndUnsubscribeParams(userId),
                success = {
                    val event = if (uiAction.cancellingFriendRequest) {
                        UserInfoViewEffect.OnSuccessCancelFriendRequest(true)
                    } else {
                        UserInfoViewEffect.OnSuccessRemoveFriend(true)
                    }
                    launchEffect(event)
                    pushFriendStatusChanged(userId)
                    updateUserSubscription(
                        userId = userId,
                        isSubscribed = false,
                        needToHideFollowButton = false,
                        isBlocked = false
                    )
                    clearPeopleContent()
                },
                fail = {
                    launchEffect(UserInfoViewEffect.OnFailureRemoveFriend)
                    Timber.e(it)
                }
            )
        }

    }

    private fun loadProfileSuggestions(userId: Long) {
        viewModelScope.launch {
            val profileSuggestions = runCatching {
                val suggestions = getProfileSuggestionsUseCase.invoke(userId)
                userSuggestionsUiMapper.mapSuggestionToUiModel(
                    suggestions = suggestions,
                    allowSyncContacts = getSyncContactsPrivacyUseCase.invoke()
                )
            }.getOrDefault(emptyList())
            this@UserProfileViewModel.profileSuggestions.clear()
            this@UserProfileViewModel.profileSuggestions += profileSuggestions
        }
    }

    private fun hideUserPosts(userId: Long?, hideStatus: Int = 1) {
        userId?.let { id ->
            disposables.add(
                hideUserPosts.hidePosts(id, hideStatus)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        Timber.d("Successfully hide posts")
                        launchEffect(RoadPostViewEffect.OnSuccessHidePosts)
                    }, { error ->
                        Timber.e("ERROR: Hide user posts: $error")
                        launchEffect(RoadPostViewEffect.OnFailureHidePosts)
                    })
            )
        }
    }

    private fun unhideUserPost(userId: Long?) {
        userId?.let { id ->
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    hideUserPosts.showUserPosts(id)
                    emitEffect(RoadPostViewEffect.OnSuccessUnhidePosts)
                } catch (error: Exception) {
                    Timber.e("ERROR: Unhide user posts: $error")
                    emitEffect(RoadPostViewEffect.OnFailureUnhidePosts)
                }
            }
        }
    }

    private fun disableChat(userId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            disableChatUseCase.execute(
                params = DisableChatParams(userId),
                success = {
                    launchEffect(UserInfoViewEffect.OnSuccessDisableChat)
                },
                fail = {
                    launchEffect(UserInfoViewEffect.OnFailureDisableChat)
                }
            )
        }
    }

    private fun enableChat(user: UserProfileUIModel) {
        viewModelScope.launch(Dispatchers.IO) {
            enableChatUseCase.execute(
                params = EnableChatParams(user.userId),
                success = {
                    if (user.settingsFlags.iCanChat) {
                        launchEffect(UserInfoViewEffect.OnSuccessEnableChat)
                    } else {
                        launchEffect(UserInfoViewEffect.OnSuccessEnableChatCompanionBlocked)
                    }
                },
                fail = {
                    launchEffect(UserInfoViewEffect.OnFailureEnableChat)
                }
            )
        }
    }

    private fun enablePhoneCalls(userId: Long) {
        viewModelScope.launch {
            try {
                phoneCallsEnableUseCase.invoke(userId)
                emitEffect(PhoneCallsViewEffect.OnSuccessEnableCalls)
            } catch (e: Exception) {
                Timber.e(e)
                emitEffect(PhoneCallsViewEffect.OnFailureEnableCalls)
            }
        }
    }

    private fun disablePhoneCalls(userId: Long) {
        viewModelScope.launch {
            try {
                phoneCallsDisableUseCase.invoke(userId)
                emitEffect(PhoneCallsViewEffect.OnSuccessDisableCalls)
            } catch (e: Exception) {
                Timber.e(e)
                emitEffect(PhoneCallsViewEffect.OnFailureDisableCalls)
            }
        }
    }

    private fun blockUser(userId: Long, remoteUserId: Long, isBlock: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val blockParams = DefBlockParams(
                userId = userId,
                remoteUserId = remoteUserId,
                isBlocked = isBlock
            )
            blockStatusUseCase.execute(
                params = blockParams,
                success = {
                    if (isBlock) {
                        updateUserSubscription(
                            userId = remoteUserId,
                            isSubscribed = false,
                            needToHideFollowButton = false,
                            isBlocked = true
                        )
                    }
                    val event = UserInfoViewEffect.OnSuccessBlockUser(isBlock)
                    launchEffect(event)
                    pushUserBlockChanged(remoteUserId, isBlock)
                    clearPeopleContent()
                },
                fail = {
                    launchEffect(UserInfoViewEffect.OnFailureBlockUser)
                }
            )
        }
    }

    private fun handleUploadToGallery(action: UserProfileUIAction.UploadToGallery) {
        if (action.images.isEmpty()) return
        uploadHelper
            .uploadImageToGallery(action.images)
            .apply { liveUploadMediaToGallery = this }
            ?.observeForever(uploadWorkObserver)
    }

    fun deleteFile(path: String) = fileManager.deleteFile(path)

    private fun uploadUserAvatar(uiAction: UserProfileUIAction.OnPublishOptionsSelected) {
        Timber.d("Upload user avatar => path: ${uiAction.imagePath}")
        viewModelScope.launch {
            runCatching {
                updateUserAvatarUseCase.invoke(
                    imagePath = uiAction.imagePath,
                    animation = uiAction.animation,
                    createAvatarPost = uiAction.createAvatarPost,
                    saveSettings = uiAction.saveSettings
                )
            }.onSuccess {
                requestOwnUserProfile()
                refreshAvatarsAndSetCurrent(0, true)
                deleteTempImageFile(uiAction.imagePath)
                emitEffect(
                    UserInfoViewEffect.OnGoneProgressUserAvatar(
                        imagePath = uiAction.imagePath,
                        createAvatarPost = uiAction.createAvatarPost
                    )
                )
            }.onFailure {
                Timber.e(it)
                deleteTempImageFile(uiAction.imagePath)
                emitEffect(UserInfoViewEffect.OnFailureChangeAvatar(uiAction.imagePath))
            }
            logPhotoSelection(true)

            val amplitudeFeedType = when (uiAction.createAvatarPost) {
                CreateAvatarPostEnum.PRIVATE_ROAD.state -> AmplitudeAlertPostWithNewAvatarValuesFeedType.SELF_FEED
                CreateAvatarPostEnum.MAIN_ROAD.state -> AmplitudeAlertPostWithNewAvatarValuesFeedType.MAIN_FEED
                else -> AmplitudeAlertPostWithNewAvatarValuesFeedType.NO_PUBLISH
            }

            logAlertPostWithNewAvatarAction(
                actionType = uiAction.amplitudeActionType,
                feedType = amplitudeFeedType,
                toggle = uiAction.saveSettings.toBoolean()
            )

            if (uiAction.saveSettings.toBoolean()) {
                logPrivacyPostWithNewAvatarChange(uiAction.createAvatarPost)
            }
        }
    }

    private fun logPhotoActionSave() {
        userId?.let { authorId ->
            amplitudeProfile.photoActionSave(
                getUserUid(),
                authorId = authorId,
                where = AmplitudePhotoActionValuesWhere.AVATAR_PROFILE
            )
        }
    }

    private fun logPhotoActionPhotoChange() {
        userId?.let { authorId ->
            amplitudeProfile.photoActionPhotoChange(
                getUserUid(),
                authorId = authorId,
                where = AmplitudePhotoActionValuesWhere.AVATAR_PROFILE
            )
        }
    }

    private fun logPhotoActionAvatarCreate() {
        userId?.let { authorId ->
            amplitudeProfile.photoActionAvatarCreate(
                getUserUid(),
                authorId = authorId,
                where = AmplitudePhotoActionValuesWhere.AVATAR_PROFILE
            )
        }
    }

    private fun logMainPhotoChangeDownloadNewPhoto() {
        amplitudeProfile.mainPhotoChangesDownloadNewPhoto(getUserUid())
    }

    private fun logMainPhotoChangeAvatarCreate() {
        amplitudeProfile.mainPhotoChangesAvatarCreate(getUserUid())
    }

    private fun logAlertPostWithNewAvatarAction(
        actionType: AmplitudeAlertPostWithNewAvatarValuesActionType,
        feedType: AmplitudeAlertPostWithNewAvatarValuesFeedType,
        toggle: Boolean
    ) {
        amplitudeProfile.alertPostWithNewAvatarAction(
            actionType = actionType,
            feedType = feedType,
            toggle = toggle,
            userId = getUserUid()
        )
    }

    private fun logPrivacyPostWithNewAvatarChange(createAvatarPost: Int) =
        amplitudeProfile.privacyPostWithNewAvatarChangeAlert(createAvatarPost)

    /**
     * Subscribe user
     */
    private fun subscribeUser(
        userId: Long,
        message: String = "",
        isAccountApproved: Boolean,
        topContentMaker: Boolean
    ) {
        myTracker.trackSubscribe()
        viewModelScope.launch {
            val event = tryCatch({
                val response = subscriptions.addSubscription(mutableListOf(userId))
                if (response.data != null) {
                    reactiveUpdateSubscribeUserUseCase.execute(
                        params = UpdateSubscriptionUserParams(
                            userId = userId,
                            isSubscribed = true,
                            needToHideFollowButton = true,
                            isBlocked = false
                        ), {}, {}
                    )
                    val influencerProperty = createInfluencerAmplitudeProperty(
                        topContentMaker = topContentMaker,
                        approved = isAccountApproved
                    )
                    val whereValue = if (isUserSnippet) {
                        AmplitudeFollowButtonPropertyWhere.USER_SNIPPET
                    } else {
                        AmplitudeFollowButtonPropertyWhere.USER_PROFILE
                    }
                    amplitudeFollowButton.followAction(
                        fromId = getUserUid(),
                        toId = userId,
                        where = whereValue,
                        type = if (isUserSnippet) AmplitudePropertyType.MAP_SNIPPET else
                            AmplitudePropertyType.PROFILE,
                        amplitudeInfluencerProperty = influencerProperty
                    )
                    pushFriendStatusChanged(
                        userId = userId,
                        isSubscribe = true
                    )
                    clearPeopleContent()
                    return@tryCatch UserInfoViewEffect.OnSubscribed(message)
                } else {
                    return@tryCatch UserInfoViewEffect.OnSubscribeFailure(message = response.err.message)
                }
            }, {
                return@tryCatch UserInfoViewEffect.OnSubscribeFailure()
            })
            emitEffect(event)
        }
    }

    private fun saveAvatarInFile(avatarState: String) {
        disposables.add(Observable.just(avatarState)
            .flatMap {
                Observable.fromCallable {
                    val bitmap = processAnimatedAvatar.createBitmap(avatarState)
                    processAnimatedAvatar.saveInFile(bitmap)
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ path ->
                Timber.i("Avatar saved path:${path}")
                if (path.isNotEmpty()) {
                    launchEffect(
                        UserInfoViewEffect.AvatarReadyToUpload(path = path, avatarState = avatarState)
                    )
                }
            }, { error ->
                Timber.i("Save avatar in file failed:${error}")
            })
        )
    }

    private fun generateBitmapFromAvatarState(avatarState: String) {
        disposables.add(Observable.just(avatarState)
            .flatMap {
                Observable.fromCallable {
                    val bitmap = processAnimatedAvatar.createBitmap(avatarState)
                    processAnimatedAvatar.saveInFileWithWaterMark(bitmap)
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ path ->
                launchEffect(UserInfoViewEffect.OnAnimatedAvatarSaved(path))
            }, { err ->
                Timber.i("Error while generating bitmap from avatar state")
            })
        )
    }

    /**
     * Unsubscribe user
     */
    private fun unsubscribeUser(uiAction: UserProfileUIAction.UnsubscribeFromUserDialogClickedAction) {
        val userId = _state.value?.profile?.userId ?: return
        viewModelScope.launch {
            val response = subscriptions.deleteFromSubscriptions(mutableListOf(userId))
            if (response.data != null) {
                reactiveUpdateSubscribeUserUseCase.execute(
                    params = UpdateSubscriptionUserParams(
                        userId = userId,
                        isSubscribed = false,
                        needToHideFollowButton = false,
                        isBlocked = false
                    ), {}, {}
                )

                emitEffect(UserInfoViewEffect.OnUnsubscribed)
                val influencerProperty = createInfluencerAmplitudeProperty(
                    topContentMaker = uiAction.topContentMaker,
                    approved = uiAction.isApproved
                )
                followButtonAnalytic.logUnfollowAction(
                    fromId = getUserUid(),
                    toId = userId,
                    where = AmplitudeFollowButtonPropertyWhere.USER_PROFILE,
                    type = if (isUserSnippet) AmplitudePropertyType.MAP_SNIPPET else
                        AmplitudePropertyType.PROFILE,
                    amplitudeInfluencerProperty = influencerProperty
                )
            } else {
                emitEffect(UserInfoViewEffect.OnUnsubscribeFailure(message = response.err.message))
            }
            pushFriendStatusChanged(
                userId = userId,
                isSubscribe = true
            )
            clearPeopleContent()
        }
    }

    private fun enableSubscriptionNotification(userId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                subscriptionNotificationUseCase.addUsers(mutableListOf(userId))
                emitEffect(UserInfoViewEffect.OnSuccessEnableSubscriptionNotification)
            } catch (e: Exception) {
                emitEffect(UserInfoViewEffect.OnFailChangeSubscriptionNotification)
                e.printStackTrace()
            }
        }
    }

    private fun disableSubscriptionNotification(userId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                subscriptionNotificationUseCase.deleteUsers(mutableListOf(userId))
                emitEffect(UserInfoViewEffect.OnSuccessDisableSubscriptionNotification)
            } catch (e: Exception) {
                emitEffect(UserInfoViewEffect.OnFailChangeSubscriptionNotification)
                e.printStackTrace()
            }
        }
    }

    fun isMe() = getUserUid() == userId

    private fun isMyFriend() = isMyFriend

    fun getHashTagEventValue(): AmplitudePropertyWhere {
        return if (isMe()) {
            AmplitudePropertyWhere.PROFILE_FEED
        } else {
            AmplitudePropertyWhere.USER_PROFILE_FEED
        }
    }

    fun requestCreateAvatarPostSettings(imagePath: String, animation: String?) {
        viewModelScope.launch {
            kotlin.runCatching {
                getUserSettingsUseCase.invoke().find { it.key == SettingsKeyEnum.CREATE_AVATAR_POST.key }
            }.onSuccess {
                emitEffect(
                    UserInfoViewEffect.OnCreateAvatarPostSettings(
                        privacySettingModel = it,
                        imagePath = imagePath,
                        animation = animation
                    )
                )
            }.onFailure {
                emitEffect(
                    UserInfoViewEffect.OnCreateAvatarPostSettings(
                        privacySettingModel = null,
                        imagePath = imagePath,
                        animation = animation
                    )
                )
                Timber.e(it)
            }
        }
    }

    fun getUserInfoListSize() = _state.value?.profileUIList?.size ?: 0

    fun isAvatarCarouselEnabled() = featureTogglesContainer.avatarCarouselFeatureToggle.isEnabled

    private fun updateUserSubscription(
        userId: Long,
        isSubscribed: Boolean,
        needToHideFollowButton: Boolean,
        isBlocked: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            reactiveUpdateSubscribeUserUseCase.execute(
                params = UpdateSubscriptionUserParams(
                    userId = userId,
                    isSubscribed = isSubscribed,
                    needToHideFollowButton = needToHideFollowButton,
                    isBlocked = isBlocked
                ),
                success = {},
                fail = {}
            )
        }
    }

    private fun logAmplitudeShowMutualFriendsType(
        type: AmplitudeSelectedMutualFriendsTabProperty
    ) {
        amplitudeMutualFriends.logMutualFriendsTabSelected(
            friendTabSelected = type,
            typeSelected = AmplitudeHowSelectedMutualFriendsProperty.USER_PROFILE
        )
    }

    private fun logDisabledShowFriendsListClicked() =
        amplitudeMutualFriends.logDisabledMutualFriendsClicked()

    /**
     * Проверка версии приложения
     * Возвращает true если необходимо показать кнопку Update false иначе
     * */
    private fun isNeedToShowUpdateBtn(): Boolean {
        appVersionName?.let { appVer ->
            serverAppVersionName?.let { serverVer ->
                val needToShow =
                    appVer.needToUpdateStr(serverVer) && userId == getUserUid()
                if (needToShow && !isTrackedUpdateBtnVisibility) {
                    analyticsInteractor.logUpdateBtnShown()
                    isTrackedUpdateBtnVisibility = true
                }
                return needToShow
            }
        }
        return false
    }

    private fun requestProfile(userId: Long) {
        viewModelScope.launch {
            runCatching {
                getUserProfileUseCase(userId)
            }.onSuccess { profile ->
                emitState(
                    UserProfileStateUIModel(
                        profile = userProfileMapper.mapDomainToUIModel(profile),
                        profileUIList = profileUIListMapper.map(
                            isMe(),
                            isNeedToShowUpdateBtn(),
                            profile,
                            if (showSuggestionsIfAvailable) profileSuggestions.toMutableList() else null
                        )
                    )
                )
                isMyFriend = profile.friendStatus == FRIEND_STATUS_CONFIRMED
                requestAvatars()
            }.onFailure { error ->
                Timber.e(error)
            }
        }
    }

    private fun requestAvatars(currentPosition: Int? = null) {
        if (gettingAvatarsJob?.isActive == true) return
        gettingAvatarsJob = viewModelScope.launch {
            val id = userId ?: return@launch
            kotlin.runCatching {
                getUserAvatarUseCase.invoke(
                    userId = id,
                    limit = (currentPosition ?: 0) + AVATARS_LOADING_LIMIT,
                    offset = numberOfElements
                )
            }.onSuccess { avatars ->
                numberOfElements += avatars.avatars.size
                avatarsList.addAll(avatars.avatars.map { photoModelMapper.avatarModelToPhotoModel(it) })
                launchEffect(
                    UserInfoViewEffect.SubmitAvatars(
                        items = avatarsList.toList(),
                        count = avatars.count,
                        currentPosition = currentPosition
                    )
                )
            }.onFailure { error ->
                Timber.e(error)
            }
        }
    }

    private fun observeDbProfile() {
        viewModelScope.launch {
            observeOwnProfileFlow.invoke().collect { userProfile ->
                if (_state.value == null) {
                    emitState(
                        UserProfileStateUIModel(
                            profile = userProfileMapper.mapDomainToUIModel(userProfile),
                            profileUIList = emptyList()
                        )
                    )
                    delay(DELAY_FOR_FAST_RENDERING)
                }
                emitState(
                    UserProfileStateUIModel(
                        profile = userProfileMapper.mapDomainToUIModel(userProfile),
                        profileUIList = profileUIListMapper.map(isMe(), isNeedToShowUpdateBtn(), userProfile)
                    )
                )
            }
        }
    }

    fun refreshProfile() {
        userId?.let {
            if (isMe()) requestOwnUserProfile()
            else requestProfile(it)
        }
    }

    /**
     * Request user profile and refresh in Db
     */
    private fun requestOwnUserProfile() {
        userId ?: return
        viewModelScope.launch {
            runCatching {
                getOwnProfileUseCase()
            }.onSuccess {
                Timber.d("Own profileUpdated")
            }.onFailure { e ->
                Timber.e(e)
            }
            requestAvatars()
        }
    }

    fun setBirthdayDialogShown() {
        viewModelScope.launch {
            try {
                updateBirthdayShownUseCase.invoke()
                appSettings.isNeedShowBirthdayDialog.set(false)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    fun logScreenForFragment() {
        if (isMe()) {
            fbAnalytic.logScreenForFragment(ScreenNamesEnum.PROFILE_MY.value)
        } else {
            fbAnalytic.logScreenForFragment(ScreenNamesEnum.PROFILE_NOT_MY.value)
        }
    }

    private fun logAmplitudeOpenCommunity() {
        if (isMe()) {
            analyticsInteractor.logCommunityScreenOpened(
                AmplitudePropertyWhereCommunityOpen.OWN_PROFILE
            )
        } else {
            if (isMyFriend()) {
                analyticsInteractor.logCommunityScreenOpened(
                    AmplitudePropertyWhereCommunityOpen.FRIEND_PROFILE
                )
            } else {
                analyticsInteractor.logCommunityScreenOpened(
                    AmplitudePropertyWhereCommunityOpen.USER_PROFILE
                )
            }
        }
    }

    private fun logUpdateBtnClicked() {
        analyticsInteractor.logUpdateBtnClicked()
    }

    private fun logPhotoClicked() {
        analyticsInteractor.logAvatarPickerOpen()
    }

    fun logAvatarOpen() {
        val property = AmplitudePropertyAnimatedAvatarFrom.FROM_PROFILE
        analyticsInteractor.logAnimatedAvatarOpen(property)
    }

    fun cacheCompanionUser(user: ChatInitProfileData?) {
        cacheCompanionUserUseCase.invoke(user)
    }


    private fun logAvatarCreated() {
        val property = AmplitudePropertyAnimatedAvatarFrom.FROM_PROFILE
        analyticsInteractor.logAnimatedAvatarCreated(property)
    }

    fun noteTimeWhenAvatarOpened() {
        avatarEditorOpenedTimeStamp = System.currentTimeMillis()
    }

    fun isHiddenAgeAndGender() = featureTogglesContainer.hiddenAgeAndSexFeatureToggle.isEnabled

    /**
     * Получаем состояние Дня Рождения юзера
     */
    private fun getDateOfBirthState() {
        viewModelScope.launch {
            try {
                val userProfile = userProfileUseCaseNew.invoke()
                handleBirthdayState(userProfile)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private fun logPhotoSelection(isPhoto: Boolean) {
        val current = System.currentTimeMillis()
        val diff = current - avatarEditorOpenedTimeStamp
        val type =
            if (isPhoto) AmplitudePropertyAvatarType.PHOTO else AmplitudePropertyAvatarType.ANIMATED_AVATAR
        analyticsInteractor.logPhotoSelection(type, if (isPhoto.not()) formatLogTime(diff) else null)
    }

    fun onPrivacyClicked() {
        analyticsInteractor.logMapPrivacySettingsClicked(AmplitudePropertyWhereMapPrivacy.PROFILE)
    }

    fun setUserSnippetCloseMethod(method: MapSnippetCloseMethod) =
        mapAnalyticsInteractor.setMapSnippetCloseMethod(method)

    private fun observeCommunityChangesEvents() {
        viewModelScope.launch {
            communityChangesUseCase.invoke().collect {
                emitEffect(UserInfoViewEffect.CommunityChanges(it))
            }
        }
    }

    private fun observeSubscriptionChangesForSuggestions() {
        disposables.add(
            getFeedStateUseCase.execute(DefParams())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::handleFeedUpdate) { Timber.e(it) })
    }

    private fun handleFeedUpdate(event: FeedUpdateEvent) {
        when (event) {
            is FeedUpdateEvent.FeedUserSubscriptionChanged -> {
                when {
                    event.isBlocked -> {
                        profileSuggestions.removeAll { it.getUserId() == event.userId }
                        refreshProfile()
                    }

                    else -> {
                        changeSuggestionSubscriptionStatus(event.userId, event.isSubscribed)
                    }
                }
            }

            else -> {}
        }
    }

    private fun formatLogTime(time: Long): String {
        val date = Date(time)
        val timeDate = SimpleDateFormat("HH.mm.ss", Locale.getDefault())
        timeDate.timeZone = TimeZone.getTimeZone("GMT")
        return timeDate.format(date)
    }

    private suspend fun unsubscribeUser(
        userId: Long,
        success: () -> Unit,
        fail: () -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val response = subscriptions.deleteFromSubscriptions(mutableListOf(userId))
            if (response.data != null) {
                success.invoke()
            } else {
                fail.invoke()
            }
        } catch (e: Exception) {
            Timber.e(e)
            fail.invoke()
        }
    }

    private fun deleteTempImageFile(filePath: String?) {
        filePath?.let {
            doAsyncViewModel({
                try {
                    val extension = filePath.substring(filePath.lastIndexOf("."))
                    Timber.d("Temp image file extension: $extension")
                    if (extension != ".gif")
                        return@doAsyncViewModel fileManager.deleteFile(it)
                    else
                        return@doAsyncViewModel false
                } catch (e: Exception) {
                    Timber.e(e)
                    return@doAsyncViewModel false
                }
            }, { isDeleted ->
                Timber.d("Temp image file isDeleted: $isDeleted")
            })
        }
    }

    private fun handleBirthdayState(userProfile: UserProfileModel?) {
        userProfile?.let { profile ->
            when {
                userBirthdayUtils.isBirthdayToday(profile.birthday) -> {
                    if (userBirthdayUtils.isDateAfter(DEFAULT_SEND_DIALOG_AFTER_TIME)) {
                        launchEffect(UserInfoViewEffect.ShowBirthdayDialog(true))
                    }
                }

                userBirthdayUtils.isBirthdayYesterday(profile.birthday) -> {
                    if (userBirthdayUtils.isDateAfter(DEFAULT_SEND_DIALOG_AFTER_TIME)) {
                        launchEffect(UserInfoViewEffect.ShowBirthdayDialog(false))
                    }
                }
            }
        }
    }

    private fun observeBirthdayDialogNeedToShow() {
        viewModelScope.launch {
            runCatching {
                getUserBirthdayDialogShownFlowUseCase.invoke()
                    .collect { isNeedShowBirthdayDialog ->
                        if (isNeedShowBirthdayDialog) {
                            getDateOfBirthState()
                        }
                    }
            }.onFailure {
                Timber.e(it)
            }
        }
    }

    private fun checkIfNeedToShowProfileStatistics() {
        val slidesListResponse = getProfileStatisticsSlidesUseCase.execute()
        if (isMe() && slidesListResponse != null && slidesListResponse.slides.isNotEmpty()) {
            launchEffect(UserInfoViewEffect.ShowProfileStatistics)
        }
    }

    private fun pushFriendStatusChanged(
        userId: Long?,
        isSubscribe: Boolean = false
    ) {
        viewModelScope.launch {
            pushFriendStatusChanged.invoke(
                userId = userId ?: 0,
                isSubscribe = isSubscribe
            )
        }
    }

    private fun pushUserBlockChanged(userId: Long, isBlocked: Boolean) {
        viewModelScope.launch {
            pushBlockStatusChanged.invoke(
                userId = userId,
                isBlocked = isBlocked
            )
        }
    }

    private fun clearPeopleContent() {
        viewModelScope.launch {
            clearSavedPeopleContentUseCase.invoke()
        }
    }

    private fun handleCreateAvatar() {
        launchEffect(UserInfoViewEffect.OpenAvatarCreator)
        logPhotoActionAvatarCreate()
        logMainPhotoChangeAvatarCreate()
    }

    private fun logOpenEditor() = viewModelScope.launch {
        amplitudeEditor.editorOpenAction(
            automaticOpen = true,
            where = AmplitudePropertyWhere.PROFILE,
            type = AmplitudeEditorTypeProperty.PHOTO
        )
    }

    private fun handleEditAvatar(nmrAmplitude: NMRPhotoAmplitude) =
        viewModelScope.launch {
            amplitudeEditor.photoEditorAction(
                editorParams = AmplitudeEditorParams(
                    where = AmplitudePropertyWhere.PROFILE,
                    automaticOpen = true
                ),
                nmrAmplitude = nmrAmplitude
            )
        }

    private fun handleAvatarChange() {
        launchEffect(UserInfoViewEffect.OpenCameraToChangeAvatar)
        logPhotoActionPhotoChange()
        logMainPhotoChangeDownloadNewPhoto()
    }

    private fun handleSaveAvatarToGallery(position: Int) {
        val selectedAvatar = avatarsList.getOrNull(position)
        selectedAvatar?.let {
            if (it.animation.isNullOrEmpty()) {
                val avatar = it.imageUrl
                if (avatar.isNotEmpty()) {
                    launchEffect(UserInfoViewEffect.SaveImageWithPermission(avatar))
                }
            } else {
                generateBitmapFromAvatarState(it.animation)
            }
            logPhotoActionSave()
        }?:run {
            val profile = _state.value?.profile ?: return
            val avatarState = profile.avatarDetails.avatarAnimation
            if (avatarState.isNullOrEmpty()) {
                val avatar = profile.avatarDetails.avatarBig
                if (avatar.isNotEmpty()) {
                    launchEffect(UserInfoViewEffect.SaveImageWithPermission(avatar))
                }
            } else {
                generateBitmapFromAvatarState(avatarState)
            }
            logPhotoActionSave()
        }
    }

    private inline fun runIfNotMe(action: () -> Unit) {
        if (isMe()) {
            Timber.d("Can't perform self action")
        } else {
            action.invoke()
        }
    }

    private fun handleUnsubscribe() {
        runIfNotMe {
            val user = _state.value?.profile ?: return
            unsubscribeFromUser(
                userId = user.userId,
                isApproved = user.accountDetails.isAccountApproved,
                topContentMaker = user.accountDetails.isTopContentMaker
            ) {
                launchEffect(UserInfoViewEffect.OnUnsubscribed)
                refreshProfile()
            }
        }
    }

    private fun handleRemoveFriendClick(uiAction: UserProfileUIAction.RemoveFriendClickedAction) {
        runIfNotMe {
            val userId = _state.value?.profile?.userId ?: return
            removeFriends(
                friendId = userId,
                cancellingFriendRequest = uiAction.cancellingFriendRequest,
                message = uiAction.message
            )
        }
    }

    private fun handlePostsPrivacyClicked(uiAction: UserProfileUIAction.ChangePostsPrivacyClickedAction) {
        runIfNotMe {
            val userId = _state.value?.profile?.userId
            if (uiAction.needToHidePost) {
                hideUserPosts(userId)
            } else {
                unhideUserPost(userId)
            }
        }
    }

    private fun handleBlackListedUserClicked() {
        runIfNotMe {
            val profile = _state.value?.profile ?: return
            val isBlocked = profile.settingsFlags.blacklistedByMe
            if (isBlocked.not()) {
                logBlockedUser(getUserUid(), profile.userId)
                blockUser(getUserUid(), profile.userId, true)
            } else {
                logUnBlockUser(getUserUid(), profile.userId)
                blockUser(getUserUid(), profile.userId, false)
            }
        }
    }

    private fun handleCallsPrivacyClicked() {
        runIfNotMe {
            val profile = _state.value?.profile ?: return
            val userCanCallMe = profile.settingsFlags.userCanCallMe
            if (userCanCallMe) {
                disablePhoneCalls(profile.userId)
            } else {
                enablePhoneCalls(profile.userId)
            }
        }
    }

    private fun handleChatPrivacyClicked() {
        runIfNotMe {
            val profile = _state.value?.profile ?: return
            val userCanChatMe = profile.settingsFlags.userCanChatMe

            if (userCanChatMe) {
                disableChat(profile.userId)
            } else {
                enableChat(profile)
            }
        }
    }

    private fun handleCopyProfileClicked() {
        val userId = _state.value?.profile?.userId ?: return
        val isProfileDeleted = _state.value?.profile?.accountDetails?.isAccountDeleted ?: return
        logCopyProfileLink(userId)
        launchEffect(UserInfoViewEffect.ShowSuccessCopyProfile(isProfileDeleted))
    }

    private fun handleOpenProfileClicked() {
        userId?.let { userId ->
            amplitudeProfile.logProfileEditTap(
                userId = userId,
                where = AmplitudeProfileEditTapProperty.PROFILE
            )
        }
        launchEffect(UserProfileNavigation.OpenProfileEdit)
    }

    private fun handleLiveAvatarChanged(uiAction: UserProfileUIAction.OnLiveAvatarChanged) {
        logAvatarCreated()
        logPhotoSelection(false)
        setAvatarState(uiAction.avatarJson)
        saveAvatarInFile(uiAction.avatarJson)
        launchEffect(UserInfoViewEffect.AllowSwipeAndGoBack)
    }

    private fun handleShareProfile() {
        viewModelScope.launch {
            runCatching {
                val profileLink = getShareProfileLinkUseCase.invoke()
                val profile = _state.value?.profile ?: return@launch
                emitEffect(UserInfoViewEffect.ShowShareProfileDialog(profileLink, profile))
            }
        }
    }

    private fun handleShowDotsMenu() {
        viewModelScope.launch {
            runCatching {
                val profileLink = getShareProfileLinkUseCase.invoke()
                val profile = _state.value?.profile ?: return@launch
                emitEffect(UserProfileDialogNavigation.ShowDotsMenu(profileLink, profile, isMe()))
                setHolidayShow(true)
            }
        }
    }

    private fun setAvatarState(state: String) {
        viewModelScope.launch {
            saveAvatarStateLocally.invoke(state)
        }
    }

    private fun handleOnAddFriendClicked(uiAction: UserProfileUIAction.OnAddFriendClicked) {
        val userId = _state.value?.profile?.userId ?: return
        val model = _state.value
            ?.profileUIList
            ?.filterIsInstance<UserEntityFriendSubscribeFloor>()
            ?.firstOrNull() ?: return
        val message = when {
            model.friendStatus == FRIEND_STATUS_INCOMING && model.isSubscribed ->
                resourceManager.getString(R.string.request_acepted)

            model.friendStatus == FRIEND_STATUS_INCOMING && !model.isSubscribed ->
                resourceManager.getString(R.string.request_accepted_notif_on)

            !model.isSubscribed -> resourceManager.getString(R.string.friend_request_send_notiff_on)
            else -> resourceManager.getString(R.string.friend_request_send)
        }

        addFriends(
            friendId = userId,
            successMessage = message,
            friendStatus = uiAction.friendStatus,
            approved = uiAction.approved,
            influencer = uiAction.influencer
        )
    }

    private fun handleSubscribeNotification(uiAction: UserProfileUIAction.ClickSubscribeNotification) {
        val userId = _state.value?.profile?.userId ?: return
        if (uiAction.isEnabled) {
            enableSubscriptionNotification(userId)
        } else {
            disableSubscriptionNotification(userId)
        }
    }

    private fun handleMapClicked(uiAction: UserProfileUIAction.OnMapClicked) {
        if (isUserSnippet) {
            launchEffect(UserInfoViewEffect.SetSnippetState(SnippetState.Closed))
            return
        }
        val profile = _state.value?.profile ?: return
        val latitude = uiAction.lat ?: userCoordinatesUIModel?.lat?.toDouble() ?: return
        val longitude = uiAction.lng ?: userCoordinatesUIModel?.lng?.toDouble() ?: return
        mapAnalyticsInteractor.logOpenMap(
            if (isMe()) AmplitudePropertyWhereOpenMap.PROFILE else AmplitudePropertyWhereOpenMap.USER_PROFILE
        )
        launchEffect(
            UserProfileNavigation.ShowMap(
                userDetailsMapper.mapUserUiModel(
                    userModel = profile,
                    latitude = latitude,
                    longitude = longitude
                )
            )
        )
    }

    private fun handleGiftClicked(uiAction: UserProfileUIAction.OnSendGiftClicked) = requestAuthAndRun {
        setHolidayShow(false)
        val profile = _state.value?.profile ?: return@requestAuthAndRun
        val gift = giftItemUIMapper.mapToGiftUIModel(uiAction.gift)
        val userId = profile.userId
        launchEffect(
            UserProfileNavigation.ShowGiftScreen(
                gift = gift, userId = userId,
                userName = profile.name, goBackTwice = false,
                birth = profile.birthdayFlag, biAmplitudeWhere = amplitudeWhere
            )
        )
    }

    private fun handleEnablingSuggestions(uiAction: UserProfileUIAction.SetSuggestionsEnabled) {
        showSuggestionsIfAvailable = uiAction.enabled
        refreshProfile()
    }

    private fun handleSubscribeSuggestion(uiAction: UserProfileUIAction.SubscribeSuggestion) {
        val userId = uiAction.userId
        viewModelScope.launch {
            runCatching {
                val response = subscriptions.addSubscription(mutableListOf(userId))
                if (response.data != null) {
                    reactiveUpdateSubscribeUserUseCase.execute(
                        params = UpdateSubscriptionUserParams(
                            userId = userId,
                            isSubscribed = true,
                            needToHideFollowButton = true,
                            isBlocked = false
                        ), {}, {}
                    )
                    pushFriendStatusChanged(
                        userId = userId,
                        isSubscribe = true
                    )
                    changeSuggestionSubscriptionStatus(userId)
                    val amplitudeInfluencer = createInfluencerAmplitudeProperty(
                        topContentMaker = uiAction.topContentMaker,
                        approved = uiAction.isApprovedUser
                    )
                    amplitudeFollowButton.followAction(
                        fromId = getUserUid(),
                        toId = userId,
                        where = AmplitudeFollowButtonPropertyWhere.SUGGEST_USER_PROFILE,
                        type = AmplitudePropertyType.OTHER,
                        amplitudeInfluencerProperty = amplitudeInfluencer
                    )
                    clearPeopleContent()
                }
            }.onFailure { Timber.e(it) }
        }
    }

    private fun handleAddFriendSuggestion(uiAction: UserProfileUIAction.AddFriendSuggestion) {
        val userId = uiAction.userId
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                addUserToFriendUseCase.execute(
                    params = AddUserToFriendParams(userId),
                    success = {
                        updateUserSubscription(
                            userId = userId,
                            isSubscribed = true,
                            needToHideFollowButton = true,
                            isBlocked = false
                        )
                        pushFriendStatusChanged(userId)
                        changeSuggestionSubscriptionStatus(userId)
                        val influencerProperty = createInfluencerAmplitudeProperty(
                            approved = uiAction.isApprovedUser,
                            topContentMaker = uiAction.topContentMaker
                        )
                        amplitudeAddFriendAnalytic.logAddFriend(
                            fromId = getUserUid(),
                            toId = userId,
                            type = FriendAddAction.SUGGEST_USER_PROFILE,
                            influencer = influencerProperty
                        )
                        clearPeopleContent()
                    },
                    fail = {
                        launchEffect(UserInfoViewEffect.OnFailureRequestAddFriend)
                    }
                )
            }.onFailure { Timber.e(it) }
        }
    }

    private fun handleUnsubscribeSuggestion(uiAction: UserProfileUIAction.UnsubscribeSuggestion) {
        runCatching {
            unsubscribeFromUser(
                userId = uiAction.userId,
                isApproved = uiAction.isApprovedUser,
                topContentMaker = uiAction.topContentMaker,
                where = AmplitudeFollowButtonPropertyWhere.SUGGEST_USER_PROFILE
            ) {
                changeSuggestionSubscriptionStatus(
                    userId = uiAction.userId,
                    isSubscribed = false
                )
            }
        }.onFailure { Timber.e(it) }
    }

    private fun handleRemoveFriendSuggestion(uiAction: UserProfileUIAction.RemoveFriendSuggestion) {
        val userId = uiAction.userId
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                removeUserFromFriendAndUnsubscribeUseCase.execute(
                    params = RemoveUserFromFriendAndUnsubscribeParams(userId),
                    success = {
                        pushFriendStatusChanged(userId)
                        changeSuggestionSubscriptionStatus(
                            userId = userId,
                            isSubscribed = false
                        )
                        clearPeopleContent()
                    },
                    fail = {
                        Timber.e(it)
                    }
                )
            }.onFailure { Timber.e(it) }
        }
    }

    private fun changeSuggestionSubscriptionStatus(userId: Long, isSubscribed: Boolean = true) {
        if (this.userId == userId) return
        if (profileSuggestions.isEmpty()) return
        val currentFloorUiEntity = _state.value
            ?.profileUIList
            ?.filterIsInstance<ProfileSuggestionsFloorUiEntity>()
            ?.firstOrNull()
        val currentSuggestions = currentFloorUiEntity?.suggestions ?: emptyList()
        if (currentSuggestions.isEmpty()) return
        val newSuggestions = ArrayList(currentSuggestions.map {
            if (it.getUserId() == userId && it is ProfileSuggestionUiModels.ProfileSuggestionUiModel) {
                it.copy(isSubscribed = isSubscribed)
            } else {
                it
            }
        })
        val newFloorUiEntity = ProfileSuggestionsFloorUiEntity(
            userType = currentFloorUiEntity?.userType ?: AccountTypeEnum.ACCOUNT_TYPE_UNKNOWN,
            suggestions = newSuggestions
        )
        val newValue = _state.value?.copy(
            profileUIList = _state.value?.profileUIList?.map {
                if (it is ProfileSuggestionsFloorUiEntity) newFloorUiEntity else it
            } ?: emptyList())
        launchState(newValue)
        this@UserProfileViewModel.profileSuggestions.clear()
        this@UserProfileViewModel.profileSuggestions += newSuggestions
    }

    private fun observeSyncContactsWorker() {
        if (isMe()) return
        observeSyncContactsUseCase.invoke()
            .distinctUntilChanged()
            .onEach(::handleSyncContactsWork)
            .catch { e -> Timber.e(e) }
            .launchIn(viewModelScope)
    }

    private fun handleSyncContactsWork(workInfo: WorkInfo?) {
        val succeeded = workInfo?.state == WorkInfo.State.SUCCEEDED
        if (succeeded.not()) return
        removeSuggestion(
            userId = null,
            isPhonebook = true
        )
    }

    private fun observeRemoveSuggestion() {
        if (isMe()) return
        getUserSettingsStateChangedUseCase.invoke()
            .onEach { effect ->
                if (effect is UserSettingsEffect.SuggestionRemoved) {
                    removeSuggestion(
                        userId = effect.userId,
                        isPhonebook = false
                    )
                }
            }
            .catch { e -> Timber.e(e) }
            .launchIn(viewModelScope)
    }

    private fun removeSuggestion(userId: Long?, isPhonebook: Boolean) {
        val currentFloorUiEntity = _state.value
            ?.profileUIList
            ?.filterIsInstance<ProfileSuggestionsFloorUiEntity>()
            ?.firstOrNull()
        val currentSuggestions = currentFloorUiEntity?.suggestions ?: emptyList()
        if (currentSuggestions.isEmpty()) return
        val newSuggestions = currentSuggestions.toMutableList()
        newSuggestions.removeAll {
            if (isPhonebook.not()) {
                it is ProfileSuggestionUiModels.ProfileSuggestionUiModel && it.userId == userId
            } else {
                it is ProfileSuggestionUiModels.SuggestionSyncContactUiModel
            }
        }
        val newFloorUiEntity = ProfileSuggestionsFloorUiEntity(
            userType = currentFloorUiEntity?.userType ?: AccountTypeEnum.ACCOUNT_TYPE_UNKNOWN,
            suggestions = newSuggestions
        )
        val newValue = _state.value?.copy(
            profileUIList = _state.value?.profileUIList?.map {
                if (it is ProfileSuggestionsFloorUiEntity) newFloorUiEntity else it
            }?.filterNot {
                it is ProfileSuggestionsFloorUiEntity && it.suggestions.isEmpty()
            } ?: emptyList())
        launchState(newValue)
        this@UserProfileViewModel.profileSuggestions.clear()
        this@UserProfileViewModel.profileSuggestions += newSuggestions
    }

    private fun handleBlockSuggestion(uiAction: UserProfileUIAction.BlockSuggestionById) {
        val userId = uiAction.userId
        amplitudeProfile.logUserCardHide(
            where = AmplitudeUserCardHideWhereProperty.SUGGEST,
            fromId = getUserUid(),
            toId = userId,
            section = AmplitudeUserCardHideSectionProperty.USER_PROFILE
        )
        blockUserSuggestion(userId)
    }

    private fun blockUserSuggestion(userId: Long) {
        viewModelScope.launch {
            runCatching {
                blockSuggestionUseCase.invoke(userId)
                removeRelatedUserDbById(userId)
            }.onSuccess {
                emitSuggestionRemovedUseCase.invoke(userId)
            }.onFailure { e ->
                Timber.e(e)
            }
        }
    }

    private suspend fun removeRelatedUserDbById(userId: Long) {
        runCatching { removeRelatedUserUseCase.invoke(userId) }
            .onFailure { e -> Timber.e(e) }
    }

    private fun logPeopleSelectedFromSuggestion() {
        amplitudePeopleAnalytics.setPeopleSelected(
            where = AmplitudePeopleWhereProperty.SUGGEST_USER_PROFILE,
            which = AmplitudePeopleWhich.PEOPLE
        )
    }

    private fun observeMomentsEvents() {
        subscribeMomentsEventsUseCase.invoke().onEach { event ->
            if (!featureTogglesContainer.momentsFeatureToggle.isEnabled) return@onEach
            when (event) {
                is MomentRepositoryEvent.ProfileUserMomentsStateUpdated ->
                    handleProfileUserMomentsStateUpdate(event)

                else -> Unit
            }
        }.launchIn(viewModelScope)
    }

    private fun handleProfileUserMomentsStateUpdate(event: MomentRepositoryEvent.ProfileUserMomentsStateUpdated) {
        if (userId == event.userId) {
            viewModelScope.launch {
                emitEffect(UserInfoViewEffect.OnMomentsPreviewUpdated(
                    event.userMomentsModel.previews,
                    event.userMomentsModel.hasNewMoments
                    ))
            }
        }
    }

    private fun handleNavigateSyncContactsAction() {
        syncContactsAnalytic.logSyncContactsStart(
            where = AmplitudeSyncContactsWhereProperty.SUGGEST,
            userId = getUserUid()
        )
        launchEffect(
            UserProfileNavigation.NavigateToPeopleFragment(
                showSwitcher = false,
                showSyncContactsWelcome = true
            )
        )
    }

    private fun handleFragmentStart() {
        tryShowUniqueNameTooltip()
        tryShowAvatarTooltip()
    }

    private fun handleHolderBind(position: Int) {
        if (onBindInvoked) return
        onBindInvoked = true
        tryShowReferralTooltip(position)
    }

    private fun tryShowReferralTooltip(position: Int) {
        if (!profileTooltipInteractor.isNeedToShowReferralTooltip() || !isMe()) return
        viewModelScope.launch {
            delay(TooltipDuration.UNIQUE_NAME + UNIQUE_NAME_TOOLTIP_DELAY)
            emitEffect(UserProfileTooltipEffect.ShowReferralTooltip(position))
            profileTooltipInteractor.referralToolTipShowed()
        }
    }

    private fun tryShowUniqueNameTooltip() {
        if (profileTooltipInteractor.isUniqueNameTooltipWasShownTimes() && isMe()) {
            viewModelScope.launch {
                delay(UNIQUE_NAME_TOOLTIP_DELAY)
                profileTooltipInteractor.incUniqueNameTooltipWasShown()
                emitEffect(UserProfileTooltipEffect.ShowUniqueNameTooltip)
            }
        }
    }

    private fun tryShowAvatarTooltip() {
        if (!isMe() || profileTooltipInteractor.isCreateAvatarUserInfoHintShown()) return
        viewModelScope.launch {
            delay(TooltipDuration.COMMON_START_DELAY)
            profileTooltipInteractor.createAvatarUserInfoTooltipWasShown()
            emitEffect(UserProfileTooltipEffect.ShowAvatarCreateTooltip)
        }
    }

    private fun handleTopMarkerClick() =
        launchEffect(UserProfileTooltipEffect.ShowUserTopMarkerTooltip)


    private fun handleUniqueNameClicked() =
        launchEffect(UserProfileTooltipEffect.ShowUniqueNameTooltipCopied)


    private fun handleSubscribersCountClick() {
        if (isMe()) launchEffect(UserProfileTooltipEffect.ShowUserSubscribersTooltip)
    }

    private fun handleStartChatClick() {
        _state.value?.profile?.let {
            val fromWhereChatCreated = if (isUserSnippet) {
                AmplitudePropertyChatCreatedFromWhere.MAP
            } else {
                AmplitudePropertyChatCreatedFromWhere.PROFILE
            }
            launchEffect(
                UserProfileNavigation.StartDialog(
                    userId = it.userId,
                    where = amplitudeWhere,
                    fromWhere = fromWhereChatCreated
                )
            )
        }
    }

    private fun handleFragmentCreated(action: UserProfileUIAction.FragmentViewCreated) {
        isUserSnippet = action.isUserSnippet
    }

    private fun handleGiftClicked() = requestAuthAndRun {
        _state.value?.profile?.let { userModel ->
            launchEffect(
                UserProfileNavigation.OpenUserGiftsFragment(
                    user = userModel,
                    where = amplitudeWhere
                )
            )
        }
    }

    private fun handleGiftsListClicked(scrollToBottom: Boolean = false) = requestAuthAndRun {
        _state.value?.profile?.let { userModel ->
            launchEffect(
                UserProfileNavigation.OpenUserGiftsListFragment(
                    user = userModel,
                    where = amplitudeWhere,
                    scrollToBottom = scrollToBottom
                )
            )
        }
    }

    private fun handleUserClicked(action: UserProfileUIAction.OnSuggestionUserClicked) {
        launchEffect(UserProfileNavigation.OpenProfile(action.toUserId))
    }

    private fun handleSubscribeClicked(action: UserProfileUIAction.OnSubscribeClicked) = requestAuthAndRun {
        if (!action.isSubscribed) {
            subscribeUser(
                userId = action.userId,
                message = action.message,
                isAccountApproved = action.approved,
                topContentMaker = action.topContent
            )
        } else {
            val isProfileSuggestionFloorShown = _state.value?.profileUIList?.any {
                it is ProfileSuggestionsFloorUiEntity
            } ?: false
            val isNotificationsEnabled =
                _state.value?.profile?.settingsFlags?.isSubscriptionNotificationEnabled ?: false
            launchEffect(
                UserProfileDialogNavigation.ShowUnsubscribeMenu(
                    isProfileSuggestionFloorShown = isProfileSuggestionFloorShown,
                    isNotificationsAvailable = true,
                    isNotificationsEnabled = isNotificationsEnabled
                )
            )
        }
    }

    private fun handleShowMoreSuggestionsClicked() {
        logPeopleSelectedFromSuggestion()
        launchEffect(UserProfileNavigation.OpenPeopleFragment())
    }

    private fun handleOnNewPostClicked(isWithImages: Boolean) = requestAuthAndRun {
        launchEffect(UserProfileNavigation.NavigateToPostFragment(isWithImages))
    }

    private fun handleOnAllVehicleClicked() = requestAuthAndRun {
        val profile = _state.value?.profile ?: return@requestAuthAndRun
        launchEffect(
            UserProfileNavigation.OpenVehicleList(
                profile.userId,
                profile.accountDetails.accountType,
                profile.accountDetails.accountColor
            )
        )
    }

    private fun handleAddVehicleClicked() = requestAuthAndRun {
        launchEffect(UserProfileNavigation.OpenAddVehicle)
    }

    private fun handleVehicleClicked(vehicle: VehicleUIModel) = requestAuthAndRun {
        val profile = _state.value?.profile ?: return@requestAuthAndRun
        launchEffect(
            UserProfileNavigation.OpenVehicle(
                profile.userId,
                vehicle.vehicleId.toString()
            )
        )
    }

    private fun handlePrivacyClicked() = viewModelScope.launch {
        val showOnMapSetting = getLocalSettingsUseCase.invoke()
            .find { it.key == SettingsKeyEnum.SHOW_ON_MAP.key }
            ?.let(privacySettingsUiMapper::mapModelToUi)
        showOnMapSetting?.let { mapPrivacySetting ->
            onPrivacyClicked()
            emitEffect(
                UserProfileNavigation.OpenMapSettingsFragment(
                    settingValue = mapPrivacySetting.value,
                    countBlacklist = mapPrivacySetting.countBlacklist,
                    countWhitelist = mapPrivacySetting.countWhitelist
                )
            )
        }
    }

    private fun handleRequestMapPrivacySettings() {
        viewModelScope.launch {
            val mapModel = _state.value
                ?.profileUIList
                ?.filterIsInstance<UserEntityMapFloor>()
                ?.firstOrNull() ?: return@launch
            val profile = _state.value?.profile ?: return@launch
            val list = _state.value?.profileUIList?.toMutableList() ?: return@launch
            runCatching {
                val settings = getSettingsUseCase.invoke()
                    .find { it.key == SettingsKeyEnum.SHOW_ON_MAP.key }
                    ?.let(privacySettingsUiMapper::mapModelToUi) ?: return@launch

                val index = list.indexOf(mapModel)
                val newMap = mapModel.copy(
                    countBlacklist = settings.countBlacklist,
                    countWhitelist = settings.countWhitelist,
                    value = settings.value
                )
                list[index] = newMap
                emitState(
                    UserProfileStateUIModel(
                        profile = profile,
                        profileUIList = list
                    )
                )
            }
        }
    }

    private fun handleGroupClicked(group: GroupUIModel) {
        launchEffect(UserProfileNavigation.OpenCommunityFeed(group.id.toInt()))
        logAmplitudeOpenCommunity()
    }

    private fun handleCreateGroup() =
        launchEffect(UserProfileNavigation.OpenCommunityEditCreate)

    private fun handleFindGroup() =
        launchEffect(UserProfileNavigation.GoToGroupTab(AmplitudePeopleWhereProperty.PROFILE_COMMUNITIES))

    private fun handleAllGroup() {
        launchEffect(UserProfileNavigation.GoToAllGroupTab)
    }

    private fun authAndOpenFriendsList(
        userId: Long,
        page: FriendsHostFragmentNew.SelectedPage?,
        name: String
    ) {
        page
        launchEffect(
            UserInfoViewEffect.AuthAndOpenFriendsList(
                userId = userId,
//                actionType = page,
                name = name
            )
        )
    }

    private fun handleMutualFriendsClicked() = requestAuthAndRun {
        val profile = _state.value?.profile ?: return@requestAuthAndRun
        logAmplitudeShowMutualFriendsType(AmplitudeSelectedMutualFriendsTabProperty.MUTUAL_FOLLOWS)
        authAndOpenFriendsList(
            userId = profile.userId,
            page = FriendsHostFragmentNew.SelectedPage.TAB_USER_MUTUAL,
            name = profile.name
        )
    }

    private fun handleFriendsListClicked() = requestAuthAndRun {
        val profile = _state.value?.profile ?: return@requestAuthAndRun
        if (!isMe()) {
            logAmplitudeShowMutualFriendsType(AmplitudeSelectedMutualFriendsTabProperty.FRIENDS)
        }
        val page = if (isMe()) {
            null
        } else {
            FriendsHostFragmentNew.SelectedPage.TAB_USER_FRIENDS
        }
        authAndOpenFriendsList(profile.userId, page, profile.name)
    }

    private fun handleSubscribersListClicked() = requestAuthAndRun {
        val profile = _state.value?.profile ?: return@requestAuthAndRun
        if (!isMe()) {
            logAmplitudeShowMutualFriendsType(AmplitudeSelectedMutualFriendsTabProperty.FOLLOWERS)
            authAndOpenFriendsList(profile.userId, FriendsHostFragmentNew.SelectedPage.TAB_USER_FOLLOWERS, profile.name)
        } else {
            launchEffect(UserProfileNavigation.OpenSubscribers)
        }
    }

    private fun handleSubscriptionsListClicked() = requestAuthAndRun {
        val profile = _state.value?.profile ?: return@requestAuthAndRun
        if (!isMe()) {
            logAmplitudeShowMutualFriendsType(AmplitudeSelectedMutualFriendsTabProperty.FOLLOWS)
            authAndOpenFriendsList(profile.userId, FriendsHostFragmentNew.SelectedPage.TAB_USER_FOLLOWING, profile.name)
        } else {
            launchEffect(UserProfileNavigation.OpenSubscriptions)
        }
    }

    private fun handleGridGalleryClicked() = requestAuthAndRun {
        val profile = _state.value?.profile ?: return@requestAuthAndRun
        launchEffect(UserProfileNavigation.OpenGridProfile(profile.userId, profile.photoCount))
    }

    private fun handleShowImage(position: Int) = requestAuthAndRun {
        val profile = _state.value?.profile ?: return@requestAuthAndRun
        launchEffect(
            UserProfileNavigation.OpenProfilePhotoViewer(
                isMe = isMe(),
                position = position,
                userId = profile.userId
            )
        )
    }

    private fun handleAddPhotoClicked() = requestAuthAndRun {
        logPhotoClicked()
        launchEffect(UserInfoViewEffect.OpenAddPhoto)
    }

    private fun handleUpdateClicked() {
        logUpdateBtnClicked()
        launchEffect(UserInfoViewEffect.GoToMarket)
    }

    private fun handleOnFriendsClicked() = requestAuthAndRun {
        if (isMe()) return@requestAuthAndRun
        val model = _state.value
            ?.profileUIList
            ?.filterIsInstance<UserEntityFriendSubscribeFloor>()
            ?.firstOrNull() ?: return@requestAuthAndRun
        if (model.isUserBlacklisted) {
            launchEffect(
                UserInfoViewEffect.ShowToastEvent(
                    resourceManager.getString(R.string.user_in_a_black_remove_and_try_again),
                    true
                )
            )
            return@requestAuthAndRun
        }
        when (model.friendStatus) {
            FRIEND_STATUS_NONE ->
                handleOnAddFriendClicked(
                    UserProfileUIAction.OnAddFriendClicked(
                        friendStatus = model.friendStatus,
                        approved = model.approved,
                        influencer = model.topContentMaker
                    )
                )

            FRIEND_STATUS_OUTGOING ->
                launchEffect(UserProfileDialogNavigation.ShowCancelFriendshipRequestDialog(model.isSubscribed))

            FRIEND_STATUS_INCOMING -> {
                launchEffect(
                    UserProfileDialogNavigation.ShowFriendIncomingStatusMenu(
                        isSubscribed = model.isSubscribed,
                        approved = model.approved,
                        influencer = model.topContentMaker,
                        friendStatus = model.friendStatus
                    )
                )
            }

            FRIEND_STATUS_CONFIRMED ->
                launchEffect(UserProfileDialogNavigation.ShowConfirmDialogRemoveFromFriend(model.isSubscribed, model.name))
        }
    }

    private fun requestAuthAndRun(action: (Boolean) -> Unit) {
        authRequester?.requestAuthAndRun(action) ?: FirebaseCrashlytics.getInstance().recordException(
            IllegalArgumentException("authRequester in UserProfileViewModel is null")
        )
    }

    private fun handleLogProfileEntrance(action: UserProfileUIAction.OnLogProfileEntrance) {
        if (isSendProfileEntranceLog) return
        val where = AmplitudePropertyWhere from action.where
        val amplitudeInfluencerProperty = createInfluencerAmplitudeProperty(
            topContentMaker = action.topContentMaker,
            approved = action.approved
        )
        amplitudeProfile.logProfileEntrance(
            where = where ?: AmplitudePropertyWhere.OTHER,
            fromId = getUserUid(),
            toId = this.userId ?: 0,
            relationship = action.relationship,
            amplitudeInfluencerProperty = amplitudeInfluencerProperty,
            countMutualAudience = action.countMutualAudience,
            haveVisibilityMutualAudience = action.haveVisibilityMutualAudience
        )
        isSendProfileEntranceLog = true
    }

    fun refreshAvatarsAndSetCurrent(argCurrentPosition: Int, argChanged: Boolean) {
        if (argChanged.not()) {
            if (argCurrentPosition < avatarsList.size) {
                launchEffect(
                    UserInfoViewEffect.SubmitAvatars(
                        items = avatarsList.toList(),
                        count = null,
                        currentPosition = argCurrentPosition
                    )
                )
                return
            }
        }
        numberOfElements = 0
        avatarsList.clear()
        requestAvatars(argCurrentPosition)
    }

    @AssistedFactory
    interface Factory {
        fun create(authRequester: AuthRequester?): UserProfileViewModel
    }

    companion object {
        const val DEFAULT_SEND_DIALOG_AFTER_TIME = "06:00"
        const val DELAY_FOR_FAST_RENDERING = 150L
        const val AVATARS_LOADING_LIMIT = 10
    }
}
