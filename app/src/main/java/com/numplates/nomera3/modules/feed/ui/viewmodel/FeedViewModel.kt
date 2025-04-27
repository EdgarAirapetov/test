package com.numplates.nomera3.modules.feed.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.meera.core.common.ACCOUNT_TYPE_PREMIUM
import com.meera.core.common.ACCOUNT_TYPE_VIP
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.empty
import com.meera.core.extensions.toInt
import com.meera.core.permission.ReadContactsPermissionProvider
import com.meera.referrals.ui.mapper.ReferralDataUIMapper
import com.meera.referrals.ui.model.ReferralDataUIModel
import com.numplates.nomera3.FIRST_POST_ID
import com.numplates.nomera3.R
import com.numplates.nomera3.domain.interactornew.CheckMainFilterRecommendedUseCase
import com.numplates.nomera3.domain.interactornew.CheckPostMarkedAsSensitiveParams
import com.numplates.nomera3.domain.interactornew.CheckPostMarkedAsSensitiveUseCase
import com.numplates.nomera3.domain.interactornew.GetCitiesForMainFilterUseCase
import com.numplates.nomera3.domain.interactornew.GetCountriesForMainFilterUseCase
import com.numplates.nomera3.domain.interactornew.GetLastPostMediaViewInfoUseCase
import com.numplates.nomera3.domain.interactornew.GetSyncContactsPrivacyUseCase
import com.numplates.nomera3.domain.interactornew.GetUserSmallAvatarUseCase
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.domain.interactornew.MarkPostAsNotSensitiveForUserParams
import com.numplates.nomera3.domain.interactornew.MarkPostAsNotSensitiveForUserUseCase
import com.numplates.nomera3.domain.interactornew.ObserveSyncContactsUseCase
import com.numplates.nomera3.domain.interactornew.ReadOnboardingUseCase
import com.numplates.nomera3.domain.interactornew.SetLastPostMediaViewInfoUseCase
import com.numplates.nomera3.domain.interactornew.SetSyncContactsPrivacyUseCase
import com.numplates.nomera3.domain.interactornew.SetupMainRoadFilterUseCase
import com.numplates.nomera3.domain.interactornew.StartSyncContactsUseCase
import com.numplates.nomera3.domain.interactornew.SubscriptionUseCase
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.appInfo.data.entity.Settings
import com.numplates.nomera3.modules.appInfo.domain.usecase.GetAppInfoAsyncUseCase
import com.numplates.nomera3.modules.auth.data.repository.DEFAULT_ANON_UID
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.baseCore.helper.AudioFeedHelper
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyMenuAction
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyOpenType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyPostType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyRoadType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhence
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AnalyticsPost
import com.numplates.nomera3.modules.baseCore.helper.amplitude.FriendAddAction
import com.numplates.nomera3.modules.baseCore.helper.amplitude.add_friend.AmplitudeAddFriendAnalytic
import com.numplates.nomera3.modules.baseCore.helper.amplitude.feed.AmplitudeAnnouncementButtonType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.feed.AmplitudeFeedAnalytics
import com.numplates.nomera3.modules.baseCore.helper.amplitude.followbutton.AmplitudeFollowButton
import com.numplates.nomera3.modules.baseCore.helper.amplitude.followbutton.AmplitudeFollowButtonPropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.followbutton.AmplitudePropertyType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.followbutton.toAmplitudeFollowButtonPropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsDeleteWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsGetThereWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mapevents.AmplitudePropertyMapEventsWantToGoWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudeMoment
import com.numplates.nomera3.modules.baseCore.helper.amplitude.moment.AmplitudePropertyMomentEntryPoint
import com.numplates.nomera3.modules.baseCore.helper.amplitude.people.AmplitudePeopleAnalytics
import com.numplates.nomera3.modules.baseCore.helper.amplitude.people.AmplitudePeopleWhereProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.people.AmplitudePeopleWhich
import com.numplates.nomera3.modules.baseCore.helper.amplitude.post.AmplitudePropertySaveType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.post.AmplitudePropertyWhosePost
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeProfile
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeUserCardHideSectionProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeUserCardHideWhereProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.FriendInviteTapAnalytics
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.FriendInviteTapProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.createInfluencerAmplitudeProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.AmplitudePropertyReactionWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.reactions.AmplitudeReactions
import com.numplates.nomera3.modules.baseCore.helper.amplitude.sync_contacts.AmplitudeSyncContactsWhereProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.sync_contacts.SyncContactsAnalytic
import com.numplates.nomera3.modules.devtools_bridge.domain.GetPostViewCollisionHighlightEnableUseCase
import com.numplates.nomera3.modules.uploadpost.ui.entity.NotAvailableReasonUiEntity.EVENT_POST_UNABLE_TO_UPDATE
import com.numplates.nomera3.modules.uploadpost.ui.entity.toUiEntity
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.feed.data.entity.FeedUpdateEvent
import com.numplates.nomera3.modules.feed.data.entity.PostMediaViewInfo
import com.numplates.nomera3.modules.feed.domain.usecase.GetUserProfileForVipUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.CheckPostPostParams
import com.numplates.nomera3.modules.feed.domain.usecase.CheckPostUpdateAvailability
import com.numplates.nomera3.modules.feed.domain.usecase.ComplainPostParams
import com.numplates.nomera3.modules.feed.domain.usecase.ComplainPostUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.DeletePostParams
import com.numplates.nomera3.modules.feed.domain.usecase.DeletePostUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.DownloadVideoToGalleryUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.FeatureParams
import com.numplates.nomera3.modules.feed.domain.usecase.FeatureUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.ForceUpdatePostUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.GetAllDownloadingMediaEventUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.GetFeedStateUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.GetLastReferralInfoUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.GetNewPostStreamUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.GetPostsParams
import com.numplates.nomera3.modules.feed.domain.usecase.GetPostsUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.GetRoadSuggestsUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.GetVipRoadUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.HidePostsOfUserParams
import com.numplates.nomera3.modules.feed.domain.usecase.HidePostsOfUserUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.LoadAndCacheReferralInfoUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.ReactiveUpdateSubscribeUserUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.SetSubPostsRequestedInSession
import com.numplates.nomera3.modules.feed.domain.usecase.StopDownloadingVideoToGalleryUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.SubscribePostParams
import com.numplates.nomera3.modules.feed.domain.usecase.SubscribePostUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.UnsubscribePostParams
import com.numplates.nomera3.modules.feed.domain.usecase.UnsubscribePostUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.UpdatePostParams
import com.numplates.nomera3.modules.feed.domain.usecase.UpdateSubscriptionUserParams
import com.numplates.nomera3.modules.feed.ui.FeedViewEvent
import com.numplates.nomera3.modules.feed.ui.adapter.FeedType
import com.numplates.nomera3.modules.feed.ui.adapter.MediaLoadingState
import com.numplates.nomera3.modules.feed.ui.data.LoadingPostVideoInfoUIModel
import com.numplates.nomera3.modules.feed.ui.data.LocalPosts
import com.numplates.nomera3.modules.feed.ui.data.RATE_US_POST_ID
import com.numplates.nomera3.modules.feed.ui.entity.DestinationOriginEnum
import com.numplates.nomera3.modules.feed.ui.entity.MediaAssetEntity
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.entity.UIPostUpdate
import com.numplates.nomera3.modules.feed.ui.entity.toAmplitudePropertyWhence
import com.numplates.nomera3.modules.feed.ui.mapper.toFeatureUiEntity
import com.numplates.nomera3.modules.feed.ui.mapper.toUIPostUpdate
import com.numplates.nomera3.modules.feed.ui.mapper.toUiEntity
import com.numplates.nomera3.modules.feed.ui.util.preloader.FeedVideoPreLoader
import com.numplates.nomera3.modules.maps.domain.analytics.MapEventsAnalyticsInteractor
import com.numplates.nomera3.modules.maps.domain.analytics.model.MapEventIdParamsAnalyticsModel
import com.numplates.nomera3.modules.maps.domain.analytics.model.MapEventInvolvementParamsAnalyticsModel
import com.numplates.nomera3.modules.maps.domain.events.usecase.JoinEventUseCase
import com.numplates.nomera3.modules.maps.domain.events.usecase.LeaveEventUseCase
import com.numplates.nomera3.modules.maps.domain.events.usecase.ObserveEventParticipationChangesUseCase
import com.numplates.nomera3.modules.maps.ui.mapper.MapAnalyticsMapperImpl
import com.numplates.nomera3.modules.moments.show.MomentDelegate
import com.numplates.nomera3.modules.moments.show.data.entity.MomentInfoCarouselUiModel
import com.numplates.nomera3.modules.moments.show.domain.MomentsAction
import com.numplates.nomera3.modules.moments.show.domain.SubscribeMomentsEventsUseCase
import com.numplates.nomera3.modules.moments.show.domain.UserMomentsStateUpdateModel
import com.numplates.nomera3.modules.moments.show.domain.model.MomentRepositoryEvent
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentCarouselItem
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentGroupUiModel
import com.numplates.nomera3.modules.peoples.domain.usecase.ClearSavedPeopleContentUseCase
import com.numplates.nomera3.modules.peoples.domain.usecase.RemoveRelatedUserUseCase
import com.numplates.nomera3.modules.post_view_statistic.domain.DetectPostViewUseCase
import com.numplates.nomera3.modules.post_view_statistic.domain.TryUploadPostViewsUseCase
import com.numplates.nomera3.modules.post_view_statistic.presentation.IPostViewsDetectViewModel
import com.numplates.nomera3.modules.post_view_statistic.presentation.PostCollisionDetector
import com.numplates.nomera3.modules.posts.domain.model.PostActionModel
import com.numplates.nomera3.modules.rateus.data.RateUsAnalyticsRating
import com.numplates.nomera3.modules.rateus.domain.HideRateUsPostUseCase
import com.numplates.nomera3.modules.rateus.domain.IsNeedToGetRateUseCase
import com.numplates.nomera3.modules.rateus.domain.RateUsAnalyticUseCase
import com.numplates.nomera3.modules.rateus.domain.RateUsUseCase
import com.numplates.nomera3.modules.reaction.data.ReactionUpdate
import com.numplates.nomera3.modules.reaction.domain.usecase.GetCommandReactionStreamUseCase
import com.numplates.nomera3.modules.registration.ui.AuthFinishListener
import com.numplates.nomera3.modules.share.domain.usecase.GetPostLinkParams
import com.numplates.nomera3.modules.share.domain.usecase.GetPostLinkUseCase
import com.numplates.nomera3.modules.tracker.FireBaseAnalytics
import com.numplates.nomera3.modules.user.domain.effect.UserSettingsEffect
import com.numplates.nomera3.modules.user.domain.usecase.AddUserToFriendParams
import com.numplates.nomera3.modules.user.domain.usecase.AddUserToFriendUseCase
import com.numplates.nomera3.modules.user.domain.usecase.BlockSuggestionUseCase
import com.numplates.nomera3.modules.user.domain.usecase.EmitSuggestionRemovedUseCase
import com.numplates.nomera3.modules.user.domain.usecase.GetUserAccountTypeUseCase
import com.numplates.nomera3.modules.user.domain.usecase.GetUserSettingsStateChangedUseCase
import com.numplates.nomera3.modules.user.domain.usecase.PushFriendStatusChangedUseCase
import com.numplates.nomera3.modules.user.domain.usecase.RemoveUserFromFriendAndUnsubscribeParams
import com.numplates.nomera3.modules.user.domain.usecase.RemoveUserFromFriendAndUnsubscribeUseCase
import com.numplates.nomera3.modules.uploadpost.ui.viewmodel.EDIT_POST_AVAILABLE
import com.numplates.nomera3.modules.user.ui.event.UserFeedViewEvent
import com.numplates.nomera3.modules.userprofile.ui.entity.ProfileSuggestionUiModels
import com.numplates.nomera3.modules.userprofile.ui.mapper.UserSuggestionsUiMapper
import com.numplates.nomera3.modules.usersettings.domain.usecase.SetSettingsUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.SettingsParams
import com.numplates.nomera3.modules.volume.domain.GetVolumeStateUseCase
import com.numplates.nomera3.modules.volume.domain.SetVolumeStateUseCase
import com.numplates.nomera3.modules.volume.domain.SubscribeVolumeEventsUseCase
import com.numplates.nomera3.modules.volume.domain.model.VolumeRepositoryEvent
import com.numplates.nomera3.modules.volume.domain.model.VolumeState
import com.numplates.nomera3.presentation.download.DownloadMediaEvent
import com.numplates.nomera3.presentation.download.DownloadMediaHelper
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum
import com.numplates.nomera3.presentation.utils.networkconn.NetworkStatusProvider
import com.numplates.nomera3.presentation.view.utils.TextProcessorUtil
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.ReplaySubject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Calendar
import java.time.LocalDateTime
import javax.inject.Inject

private const val START_POSITION_WITH_MOMENTS = 1
private const val START_POSITION_WITHOUT_MOMENTS = 0


class FeedViewModel @Inject constructor(
    private val getCommandReactionStreamUseCase: GetCommandReactionStreamUseCase,
    private val getPosts: GetPostsUseCase,
    private val subscribePostUseCase: SubscribePostUseCase,
    private val unsubscribePostUseCase: UnsubscribePostUseCase,
    private val deletePostUseCase: DeletePostUseCase,
    private val postComplainUseCase: ComplainPostUseCase,
    private val hidePostsOfUserUseCase: HidePostsOfUserUseCase,
    private val featureUseCase: FeatureUseCase,
    private val getNewPostStreamUseCase: GetNewPostStreamUseCase,
    private val subscribeMomentsEventsUseCase: SubscribeMomentsEventsUseCase,
    private val checkPostMarkedAsSensitiveUseCase: CheckPostMarkedAsSensitiveUseCase,
    private val markPostAsNotSensitiveForUserUseCase: MarkPostAsNotSensitiveForUserUseCase,
    private val getFeedStateUseCase: GetFeedStateUseCase,
    private val forceUpdatePostUseCase: ForceUpdatePostUseCase,
    private val getCitiesForMainFilterUseCase: GetCitiesForMainFilterUseCase,
    private val getCountriesForMainFilterUseCase: GetCountriesForMainFilterUseCase,
    private val textProcessorUtil: TextProcessorUtil,
    private val subscriptionsUseCase: SubscriptionUseCase,
    private val updateReactiveSubscription: ReactiveUpdateSubscribeUserUseCase,
    private val audioFeedHelper: AudioFeedHelper,
    private val tryUploadPostViewUseCase: TryUploadPostViewsUseCase,
    private val detectPostViewUseCase: DetectPostViewUseCase,
    private val getAppInfoAsyncUseCase: GetAppInfoAsyncUseCase,
    private val readOnBoardingUseCase: ReadOnboardingUseCase,
    private val getPostViewCollisionHighlightUseCaseUseCase: GetPostViewCollisionHighlightEnableUseCase,
    private val analyticsInteractor: AnalyticsInteractor,
    private val amplitudeMoment: AmplitudeMoment,
    private val amplitudeFollowButton: AmplitudeFollowButton,
    private val amplitudeReactions: AmplitudeReactions,
    private val getUidUseCase: GetUserUidUseCase,
    private val downloadVideoToGalleryUseCase: DownloadVideoToGalleryUseCase,
    private val getAllDownloadingVideoWorkInfosUseCase: GetAllDownloadingMediaEventUseCase,
    private val stopDownloadingVideoToGalleryUseCase: StopDownloadingVideoToGalleryUseCase,
    private val fbAnalytic: FireBaseAnalytics,
    private val getUserUidUseCase: GetUserUidUseCase,
    private val getUserSmallAvatarUseCase: GetUserSmallAvatarUseCase,
    private val getPostLinkUseCase: GetPostLinkUseCase,
    private val feedAnalytics: AmplitudeFeedAnalytics,
    private val isNeedToGetRateUseCase: IsNeedToGetRateUseCase,
    private val setupMainRoadFilterUseCase: SetupMainRoadFilterUseCase,
    private val checkMainFilterRecommendedUseCase: CheckMainFilterRecommendedUseCase,
    private val rateUsUseCase: RateUsUseCase,
    private val hideRateUsPostUseCase: HideRateUsPostUseCase,
    private val featureTogglesContainer: FeatureTogglesContainer,
    private val networkStatusProvider: NetworkStatusProvider,
    private val profileAnalytics: AmplitudeProfile,
    private val rateUsAnalyticUseCase: RateUsAnalyticUseCase,
    private val momentDelegate: MomentDelegate,
    private val joinEventUseCase: JoinEventUseCase,
    private val leaveEventUseCase: LeaveEventUseCase,
    private val checkPostUpdateAvailability: CheckPostUpdateAvailability,
    private val observeEventParticipationChangesUseCase: ObserveEventParticipationChangesUseCase,
    private val videoPreloadUtil: FeedVideoPreLoader,
    private val mapEventsAnalyticsInteractor: MapEventsAnalyticsInteractor,
    private val setSubPostsRequestedInSession: SetSubPostsRequestedInSession,
    private val mapAnalyticsMapperImpl: MapAnalyticsMapperImpl,
    private val readContactsPermissionProvider: ReadContactsPermissionProvider,
    private val setSettingsUseCase: SetSettingsUseCase,
    private val startSyncContactsUseCase: StartSyncContactsUseCase,
    private val getSyncContactsPrivacyUseCase: GetSyncContactsPrivacyUseCase,
    private val setSyncContactsPrivacyUseCase: SetSyncContactsPrivacyUseCase,
    private val observeSyncContactsUseCase: ObserveSyncContactsUseCase,
    private val getLastReferralInfoUseCase: GetLastReferralInfoUseCase,
    private val loadAndCacheReferralInfoUseCase: LoadAndCacheReferralInfoUseCase,
    private val referralDataUIMapper: ReferralDataUIMapper,
    private val userSuggestionsUiMapper: UserSuggestionsUiMapper,
    private val getRoadSuggestsUseCase: GetRoadSuggestsUseCase,
    private val blockSuggestionUseCase: BlockSuggestionUseCase,
    private val removeRelatedUserUseCase: RemoveRelatedUserUseCase,
    private val emitSuggestionRemovedUseCase: EmitSuggestionRemovedUseCase,
    private val getUserSettingsStateChangedUseCase: GetUserSettingsStateChangedUseCase,
    private val clearSavedPeopleContentUseCase: ClearSavedPeopleContentUseCase,
    private val reactiveUpdateSubscribeUserUseCase: ReactiveUpdateSubscribeUserUseCase,
    private val pushFriendStatusChanged: PushFriendStatusChangedUseCase,
    private val addUserToFriendUseCase: AddUserToFriendUseCase,
    private val removeUserFromFriendAndUnsubscribeUseCase: RemoveUserFromFriendAndUnsubscribeUseCase,
    private val getVipRoadUseCase: GetVipRoadUseCase,
    private val syncContactsAnalytic: SyncContactsAnalytic,
    private val amplitudeAddFriendAnalytic: AmplitudeAddFriendAnalytic,
    private val amplitudePeopleAnalytics: AmplitudePeopleAnalytics,
    private val friendInviteTapAnalytics: FriendInviteTapAnalytics,
    private val authFinishListener: AuthFinishListener,
    private val getUserAccountTypeUseCase: GetUserAccountTypeUseCase,
    private val getUserProfileForVipUseCase: GetUserProfileForVipUseCase,
    private val getLastPostMediaViewInfoUseCase: GetLastPostMediaViewInfoUseCase,
    private var setLastPostMediaViewInfoUseCase: SetLastPostMediaViewInfoUseCase,
    private var getVolumeStateUseCase: GetVolumeStateUseCase,
    private var setVolumeStateUseCase: SetVolumeStateUseCase,
    private var subscribeVolumeEventsUseCase: SubscribeVolumeEventsUseCase
) : ViewModel(), IPostViewsDetectViewModel {

    private val disposable = CompositeDisposable()

    val isEventsOnMapEnabled: Boolean

    private var roadType: RoadTypesEnum? = null
    private var settings: Settings? = null
    private var originEnum: DestinationOriginEnum? = null

    private val _liveEvent = MutableLiveData<FeedViewEvent>()
    private val _livePosts = MutableLiveData<MutableList<PostUIEntity>>()

    var liveEvent = _liveEvent as LiveData<FeedViewEvent>
    var livePosts = _livePosts as LiveData<MutableList<PostUIEntity>>
    var subjectPosts = ReplaySubject.create<FeedViewEvent>()
    val postsObservable: Observable<FeedViewEvent> = subjectPosts.share()

    private val featureList: MutableList<PostUIEntity> = mutableListOf()
    private val localFeatureList: MutableList<PostUIEntity> = mutableListOf()

    private var observeAuthDisposable: Disposable? = null
    private var roadUserId: Long? = null // when roadtype = user

    var loadingPostsJob: Job? = null

    private val _userFeedProfileViewEvent = MutableSharedFlow<UserFeedViewEvent>()
    val userFeedProfileViewEvent: SharedFlow<UserFeedViewEvent> = _userFeedProfileViewEvent

    private var momentInfoUiModel: MomentInfoCarouselUiModel? = null
    private var referralInfo: ReferralDataUIModel? = null
    private val suggests: MutableList<ProfileSuggestionUiModels> = mutableListOf()

    //время открытия меню поста. Проверяем, доступен ли для редактирования пост, у которого открыли меню
    private var postMenuOpenedDate: LocalDateTime = LocalDateTime.MIN

    init {
        isEventsOnMapEnabled = featureTogglesContainer.mapEventsFeatureToggle.isEnabled
        settings = getAppInfoAsyncUseCase.executeBlocking()
        loadSuggests()
        loadReferralInfo()
        setupRecSystemSettings()
        observeFeedState()
        observeReactions()
        observeNewPostCreate()
        observeMomentsEvents()
        observeVolumeEvents()
        observeDownloadingVideoWorkInfos()
        observeEventParticipationChanges()
        observeSyncContacts()
        observeRemoveSuggestion()
        observeAuth()
    }

    private var lastJoinedEventPostId: Long? = null

    val liveFeedEvents = MutableLiveData<FeedViewEventPost>()

    var isLoading = false
    var isLastPage = false
    private var lastPostId = 0L
    private var isLastRequestSuccess: Boolean = true
    private var isScrolledToPostSuccess = false

    fun preloadVideoPosts(currentVisiblePostPosition: Int) {
        videoPreloadUtil.preLoadVideoPosts(
            currentVisiblePostPosition = currentVisiblePostPosition,
            allPosts = livePosts.value ?: emptyList()
        )
    }

    override fun detectPostView(postViewDetectModel: PostCollisionDetector.PostViewDetectModel) {
        detectPostViewUseCase.execute(postViewDetectModel)
    }

    override fun uploadPostViews() {
        tryUploadPostViewUseCase.execute()
    }

    override fun onCleared() {
        super.onCleared()

        disposeAuthDisposable()
        disposable.clear()
        subjectPosts.onComplete()
    }

    fun getLastPostMediaViewInfo(): PostMediaViewInfo? = getLastPostMediaViewInfoUseCase.invoke()

    fun startActivatingVip(data: ReferralDataUIModel) {
        viewModelScope.launch {
            val accountType = getUserAccountTypeUseCase.invoke()
            val userProfile = getUserProfileForVipUseCase.invoke()
            val vipUntilDate: Long
            val descriptionTextId: Int
            when (accountType) {
                ACCOUNT_TYPE_VIP -> {
                    vipUntilDate = calculateVipUntilDate(
                        userProfile.accountTypeExpiration.times(1000),
                        data.availableVips
                    )
                    descriptionTextId = com.meera.referrals.R.string.referral_get_vip_dialog_description_premium
                }
                ACCOUNT_TYPE_PREMIUM -> {
                    vipUntilDate = calculateVipUntilDate(System.currentTimeMillis(), data.availableVips)
                    descriptionTextId = com.meera.referrals.R.string.referral_get_vip_dialog_description_premium
                }
                else -> {
                    vipUntilDate = calculateVipUntilDate(System.currentTimeMillis(), data.availableVips)
                    descriptionTextId = com.meera.referrals.R.string.referral_get_vip_dialog_description_regular
                }
            }
            _liveEvent.postValue(FeedViewEvent.ShowGetVipDialog(vipUntilDate, descriptionTextId))
        }
    }

    private fun calculateVipUntilDate(startTime: Long, availableVips: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = startTime
        calendar.add(Calendar.MONTH, availableVips)
        return calendar.timeInMillis / 1000
    }

    fun onSyncContactsPositiveButtonClicked() {
        if (readContactsPermissionProvider.hasContactsPermission().not()) {
            _liveEvent.postValue(FeedViewEvent.RequestContactsPermission)
        } else {
            setSyncContactsSettingTrue()
        }
    }

    fun onContactsPermissionGranted() {
        setSyncContactsSettingTrue()
    }

    fun onContactsPermissionDenied(deniedAndNoRationaleNeededAfterRequest: Boolean) {
        if (deniedAndNoRationaleNeededAfterRequest) {
            _liveEvent.postValue(FeedViewEvent.ShowSyncDialogPermissionDenied)
        }
    }

    fun logOpenReferral() {
        friendInviteTapAnalytics.logFiendInviteTap(FriendInviteTapProperty.MAIN_FEED)
    }

    fun logSyncContactsClicked() {
        syncContactsAnalytic.logSyncContactsStart(
            where = AmplitudeSyncContactsWhereProperty.MAIN_FEED,
            userId = getUserUid()
        )
    }

    fun getFeatureTogglesContainer(): FeatureTogglesContainer {
        return featureTogglesContainer
    }

    fun getAudioHelper(): AudioFeedHelper {
        return audioFeedHelper
    }

    fun getAmplitudeReactions(): AmplitudeReactions {
        return amplitudeReactions
    }

    fun getAmplitudeHelper(): AnalyticsInteractor {
        return analyticsInteractor
    }

    fun getNetworkStatusProvider(): NetworkStatusProvider {
        return networkStatusProvider
    }

    fun getVipReferral(vipUntilDate: Long) {
        viewModelScope.launch {
            try {
                val isSuccessResult = getVipRoadUseCase.invoke()
                if (isSuccessResult) {
                    _liveEvent.postValue(FeedViewEvent.OnSuccessGetVip(vipUntilDate))
                    loadNewReferralInfo()
                } else {
                    _liveEvent.postValue(FeedViewEvent.OnFailGetVip)
                }
            } catch (e: Exception) {
                Timber.e(e)
                _liveEvent.postValue(FeedViewEvent.OnFailGetVip)
            }
        }
    }

    fun logOpenPeoplesFromSuggestions() {
        amplitudePeopleAnalytics.setPeopleSelected(
            where = AmplitudePeopleWhereProperty.SUGGEST_MAIN_FEED_MORE,
            which = AmplitudePeopleWhich.PEOPLE
        )
    }

    fun getVolumeState() = getVolumeStateUseCase.invoke()

    fun onTriggerAction(action: FeedViewActions) {
        if (roadType == null) return

        when (action) {
            is FeedViewActions.ResetAppInfoCache -> {
                getAppInfoAsyncUseCase.resetCache()
            }

            is FeedViewActions.GetAllPosts -> getAllPosts(action.startPostId)
            is FeedViewActions.GetUserPosts -> {
                getUserPosts(
                    startPostId = action.startPostId, userId = action.userId, selectedPostId = action.selectedPostId
                )
            }

            is FeedViewActions.GetSubscriptionPosts -> getSubscriptionPosts(action.startPostId)
            is FeedViewActions.GetGroupPosts -> getGroupPosts(action.startPostId, action.groupId)
            is FeedViewActions.GetHashtagPosts -> getHashtagPosts(action.startPostId, action.hashtag)
            is FeedViewActions.RetryLastPostsRequest -> retryGetPosts()
            is FeedViewActions.SubscribeToPost -> {
                subscribeToPost(action.postId, action.titles)
            }
            is FeedViewActions.UnsubscribeFromPost -> unsubscribeFromPost(action.postId, action.titles)
            is FeedViewActions.HideUserRoads -> hideUserRoad(action.userId)
            is FeedViewActions.DeletePost -> deletePost(action.post)
            is FeedViewActions.ComplainToPost -> complainToPost(action.postId)
            is FeedViewActions.FeatureClick -> dismissFeature(action.featureId, action.dismiss, action.deepLink)
            is FeedViewActions.UnsubscribeFromUserAndClear -> unsubscribeFromUserAndClear(action.postId, action.userId)
            is FeedViewActions.SubscribeToUser -> {
                if (action.postId != null && action.userId != null) {
                    subscribeUser(
                        postId = action.postId,
                        userId = action.userId,
                        needToHideFollowButton = action.needToHideFollowButton,
                        fromFollowButton = action.fromFollowButton,
                        isApproved = action.isApproved,
                        topContentMaker = action.topContentMaker
                    )
                }
            }

            is FeedViewActions.UnsubscribeFromUser -> {
                if (action.postId != null && action.userId != null) {
                    unsubscribeFromUser(
                        postId = action.postId,
                        userId = action.userId,
                        fromFollowButton = action.fromFollowButton,
                        isApproved = action.isApproved,
                        topContentMaker = action.topContentMaker
                    )
                }
            }

            is FeedViewActions.OnShowMoreText -> handleShowMoreText(action.post)
            is FeedViewActions.UpdateMainRoad -> handleMainRoadUpdate(action.startPostId)
            is FeedViewActions.CopyPostLink -> handleGetLinkAndCopy(action.postId)
            is FeedViewActions.HideRateUsPost -> handleHideRateUsPost()
            is FeedViewActions.RateUs -> handleRateUs(
                rating = action.rating, comment = action.comment
            )

            is FeedViewActions.RateUsAnalytic -> handleRateUsAnalytic(
                ratingAnalytics = action.rateUsAnalyticsRating
            )

            is FeedViewActions.CheckUpdateAvailability ->
                handleCheckUpdateAvailability(post = action.post, currentMedia = action.currentMedia)
            is FeedViewActions.EditPost -> openEditPost(post = action.post)
            is FeedViewActions.SaveLastPostMediaViewInfo -> saveLastPostMediaViewInfo(action.lastPostMediaViewInfo)
            is FeedViewActions.UpdatePostSelectedMediaPosition ->
                updatePostSelectedMediaPosition(action.postId, action.selectedMediaPosition)
            is FeedViewActions.UpdateVolumeState -> updateVolumeState(action.volumeState)

            else -> Timber.d("Action is not implemented")
        }
    }

    fun getUserUid() = getUidUseCase.invoke()

    fun clearFeed() {
        submitList(listOf())
    }

    fun initRoadType(type: RoadTypesEnum, originEnum: DestinationOriginEnum) {
        this.roadType = type
        this.originEnum = originEnum
        when (roadType) {
            RoadTypesEnum.CUSTOM,
            RoadTypesEnum.SUBSCRIPTION,
            -> showShimmerProgressDelayed(DELAY_SHIMMER_REQUEST)

            RoadTypesEnum.MAIN,
            RoadTypesEnum.HASHTAG,
            RoadTypesEnum.COMMUNITY,
            -> showShimmerProgress()

            else -> Unit
        }

        if (featureTogglesContainer.momentsFeatureToggle.isEnabled) {
            initMomentDelegate()
        }

        getAppInfoAsyncUseCase.resetCache()
    }

    fun logScreenForFragment(screenName: String) = fbAnalytic.logScreenForFragment(screenName)

    fun logStatisticReactionsTap(
        where: AmplitudePropertyReactionWhere,
        whence: AmplitudePropertyWhence,
    ) {
        getAmplitudeReactions().statisticReactionsTap(
            where = where,
            whence = whence,
            recFeed = checkMainFilterRecommendedUseCase.invoke()
        )
    }

    fun stopDownloadingPostVideo(postId: Long) {
        stopDownloadingVideoToGalleryUseCase.invoke(postId)
    }

    fun downloadPostVideo(postId: Long, assetId: String?) {
        downloadVideoToGalleryUseCase.invoke(DownloadMediaHelper.PostMediaDownloadType.PostRoadDownload(postId, assetId))
    }

    fun isMarkedAsSensitivePost(postId: Long?): Boolean {
        return checkPostMarkedAsSensitiveUseCase.invoke(
            params = CheckPostMarkedAsSensitiveParams(
                postId = postId
            )
        )
    }

    fun markPostAsNotSensitiveForUser(postId: Long?, parentPostId: Long?) {
        markPostAsNotSensitiveForUserUseCase.invoke(
            params = MarkPostAsNotSensitiveForUserParams(
                postId = postId,
                parentPostId = parentPostId
            )
        )
    }

    fun refreshPost(postId: Long?) = postId?.let {
        forceUpdatePostUseCase.execute(
            UpdatePostParams(FeedUpdateEvent.FeedUpdateAll(it))
        )
    }

    fun isNeedToShowOnBoarding() = readOnBoardingUseCase.invoke()

    fun getPostViewHighlightLiveData(): LiveData<Boolean> {
        return getPostViewCollisionHighlightUseCaseUseCase.execute()
    }

    fun getSettings() = settings

    fun logAnnouncementButtonEvent(
        dismissed: Boolean,
        haveAction: Boolean,
        announceName: String?,
    ) {
        val buttonType = if (dismissed) {
            AmplitudeAnnouncementButtonType.CLEAR_BUTTON
        } else {
            AmplitudeAnnouncementButtonType.OPTIONAL_BUTTON
        }
        feedAnalytics.logAnnouncementButtonPress(
            type = buttonType,
            haveAction = haveAction,
            announceName = announceName.toString(),
        )
    }

    fun logFeedScroll() {
        val amplitudePropertyRoadType = when (roadType) {
            RoadTypesEnum.MAIN -> AmplitudePropertyRoadType.MAIN_FEED
            RoadTypesEnum.CUSTOM -> AmplitudePropertyRoadType.SELF_FEED
            RoadTypesEnum.SUBSCRIPTION -> AmplitudePropertyRoadType.FOLLOW_FEED
            else -> null
        }
        amplitudePropertyRoadType?.let {
            analyticsInteractor.logFeedScroll(
                roadType = it,
                recFeed = if (it == AmplitudePropertyRoadType.MAIN_FEED) {
                    checkMainFilterRecommendedUseCase.invoke()
                } else {
                    false
                }
            )
        }
    }

    fun logDeletedPost(post: AnalyticsPost, whereFrom: AmplitudePropertyWhere) {
        analyticsInteractor.logPostDeleted(post, whereFrom)
    }

    fun logPressMoreText(
        postId: Long,
        authorId: Long,
        where: AmplitudePropertyWhere,
        postType: AmplitudePropertyPostType,
        isPostDetailOpen: Boolean,
    ) {
        val openType = when (isPostDetailOpen) {
            true -> AmplitudePropertyOpenType.SEPARATE
            else -> AmplitudePropertyOpenType.DEPLOY
        }
        analyticsInteractor.logPressMoreText(
            postId = postId,
            authorId = authorId,
            where = where,
            postType = postType,
            openType = openType,
        )
    }

    fun removePostsViewedBlock() {
        _livePosts.value?.let { postsList ->
            _livePosts.value = postsList.filter { it.feedType != FeedType.POSTS_VIEWED_ROAD }.toMutableList()
        }
    }

    fun logPostMenuAction(
        post: PostUIEntity,
        action: AmplitudePropertyMenuAction,
        authorId: Long?,
        saveType: AmplitudePropertySaveType = AmplitudePropertySaveType.NONE,
    ) {
        authorId ?: return
        val whosePost = if (getUserUidUseCase.invoke() == authorId) {
            AmplitudePropertyWhosePost.MY
        } else {
            AmplitudePropertyWhosePost.USER
        }
        val where = if (post.isEvent()) {
            AmplitudePropertyWhere.MAP_EVENT
        } else {
            AmplitudePropertyWhere.POST
        }
        analyticsInteractor.logPostMenuAction(
            actionType = action,
            authorId = authorId,
            where = where,
            whosePost = whosePost,
            whence = originEnum.toAmplitudePropertyWhence(),
            saveType = saveType,
            recFeed = checkMainFilterRecommendedUseCase.invoke()
        )
    }

    fun unsubscribeSuggestedUser(userId: Long, isApproved: Boolean, topContentMaker: Boolean) {
        viewModelScope.launch {
            val data = subscriptionsUseCase.deleteFromSubscriptions(listOf(userId))
            if (data.err == null) {
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
                changeSuggestionSubscriptionStatus(
                    userId,
                    false
                )
                logUnsubscribeSuggestedUser(userId, isApproved, topContentMaker)
            }
        }
    }

    private fun logUnsubscribeSuggestedUser(
        userId: Long,
        isApproved: Boolean,
        topContentMaker: Boolean
    ) {
        val amplitudeInfluencerProperty = createInfluencerAmplitudeProperty(
            topContentMaker = topContentMaker,
            approved = isApproved
        )
        amplitudeFollowButton.logUnfollowAction(
            fromId = getUserUid(),
            toId = userId,
            where = AmplitudeFollowButtonPropertyWhere.SUGGEST_MAIN_FEED,
            type = AmplitudePropertyType.OTHER,
            amplitudeInfluencerProperty = amplitudeInfluencerProperty
        )
    }

    fun subscribeSuggestedUser(
        userId: Long,
        isApproved: Boolean,
        topContentMaker: Boolean
    ) {
        viewModelScope.launch {
            runCatching {
                val response = subscriptionsUseCase.addSubscription(mutableListOf(userId))
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
                    clearPeopleContent()
                    val amplitudeInfluencer = createInfluencerAmplitudeProperty(
                        topContentMaker = topContentMaker,
                        approved = isApproved
                    )
                    amplitudeFollowButton.followAction(
                        fromId = getUserUid(),
                        toId = userId,
                        where = AmplitudeFollowButtonPropertyWhere.SUGGEST_MAIN_FEED,
                        type = AmplitudePropertyType.OTHER,
                        amplitudeInfluencerProperty = amplitudeInfluencer
                    )
                }
            }.onFailure { Timber.e(it) }
        }
    }

    fun addFriendSuggestedUser(
        userId: Long,
        isApproved: Boolean,
        topContentMaker: Boolean
    ) {
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
                        clearPeopleContent()
                        val influencerProperty = createInfluencerAmplitudeProperty(
                            approved = isApproved,
                            topContentMaker = topContentMaker
                        )
                        amplitudeAddFriendAnalytic.logAddFriend(
                            fromId = getUserUid(),
                            toId = userId,
                            type = FriendAddAction.SUGGEST_MAIN_FEED,
                            influencer = influencerProperty
                        )
                    },
                    fail = {
                        Timber.e(it)
                    }
                )
            }.onFailure { Timber.e(it) }
        }
    }

    fun removeFriendSuggestedUser(userId: Long) {
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

    fun hideSuggestedUser(userId: Long) {
        viewModelScope.launch {
            runCatching {
                blockSuggestionUseCase.invoke(userId)
                removeRelatedUserDbById(userId)
            }.onSuccess {
                emitSuggestionRemovedUseCase.invoke(userId)
                profileAnalytics.logUserCardHide(
                    where = AmplitudeUserCardHideWhereProperty.SUGGEST,
                    fromId = getUserUid(),
                    toId = userId,
                    section = AmplitudeUserCardHideSectionProperty.MAIN_FEED
                )
            }.onFailure { e ->
                Timber.e(e)
            }
        }
    }

    private suspend fun removeRelatedUserDbById(userId: Long) {
        runCatching { removeRelatedUserUseCase.invoke(userId) }
            .onFailure { e -> Timber.e(e) }
    }

    fun logPressHashTag(
        where: AmplitudePropertyWhere,
        postId: Long,
        authorId: Long,
    ) {
        analyticsInteractor.logHashTagPress(where, postId, authorId)
    }

    fun logMomentTapCreate(entryPoint: AmplitudePropertyMomentEntryPoint) {
        amplitudeMoment.onTapCreateMoment(entryPoint)
    }

    fun repostSuccess(post: PostUIEntity, repostTargetCount: Int = 1) {
        forceUpdatePostUseCase.execute(
            UpdatePostParams(
                FeedUpdateEvent.FeedUpdatePayload(
                    postId = post.postId,
                    repostCount = post.repostCount + repostTargetCount
                )
            )
        )
    }

    fun onJoinAnimationFinished(postUIEntity: PostUIEntity, adapterPosition: Int) {
        Timber.d("adapterPosition $adapterPosition")
        if (postUIEntity.postId != lastJoinedEventPostId) return
        _liveEvent.postValue(
            FeedViewEvent.ShowEventSharingSuggestion(
                post = postUIEntity,
            )
        )
    }

    fun joinEvent(postUIEntity: PostUIEntity) {
        logMapEventWantToGo(postUIEntity)
        val eventId = postUIEntity.event?.id ?: return
        viewModelScope.launch {
            runCatching {
                joinEventUseCase.invoke(eventId)
                delay(DELAY_EVENT_SHARING_SUGGESTION)
                _liveEvent.postValue(FeedViewEvent.ShowEventSharingSuggestion(postUIEntity))
                lastJoinedEventPostId = postUIEntity.postId
            }.onFailure {
                Timber.e(it)
                showCommonError()
            }
        }
    }

    fun leaveEvent(postUIEntity: PostUIEntity) {
        val eventId = postUIEntity.event?.id ?: return
        viewModelScope.launch {
            runCatching {
                leaveEventUseCase.invoke(eventId)
                logMapEventMemberDeleteYouself(postUIEntity)
            }.onFailure {
                Timber.e(it)
                showCommonError()
            }
        }
    }

    fun hideUserMoments(userId: Long) {
        getMomentDelegate().hideUserMoments(userId) {
            _liveEvent.postValue(FeedViewEvent.ShowCommonSuccess(R.string.moments_hidden))
        }
    }

    private fun updateEventPost(postUIEntity: PostUIEntity) {
        val postIndex = _livePosts.value?.indexOfFirst { it.postId == postUIEntity.postId } ?: -1
        if (postIndex != -1) {
            _livePosts.value?.set(postIndex, postUIEntity)
            _liveEvent.postValue(FeedViewEvent.UpdateEventPost(postUIEntity))
        }
    }

    fun logMapEventGetTherePress(post: PostUIEntity) {
        val mapEventIdParamsAnalyticsModel = MapEventIdParamsAnalyticsModel(
            eventId = post.event?.id ?: return,
            authorId = post.user?.userId ?: return
        )
        mapEventsAnalyticsInteractor.logMapEventGetTherePress(
            mapEventIdParamsAnalyticsModel = mapEventIdParamsAnalyticsModel,
            where = AmplitudePropertyMapEventsGetThereWhere.FEED
        )
    }

    private fun updateVolumeState(volumeState: VolumeState) {
        setVolumeStateUseCase.invoke(volumeState)
    }

    private fun updatePostSelectedMediaPosition(postId: Long, selectedMediaPosition: Int) {
        val postIndex = _livePosts.value?.indexOfFirst { it.postId == postId } ?: return
        val updatedPost = _livePosts.value?.get(postIndex)?.copy(selectedMediaPosition = selectedMediaPosition) ?: return
        _livePosts.value?.set(postIndex, updatedPost)
        val postUpdate = UIPostUpdate.UpdateSelectedMediaPosition(postId, selectedMediaPosition)
        liveFeedEvents.value = FeedViewEventPost.UpdatePostEvent(postUpdate)
    }

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

    private fun observeSyncContacts() {
        observeSyncContactsUseCase.invoke()
            .distinctUntilChanged()
            .onEach(::handleSyncWork)
            .catch { e ->
                Timber.e(e)
            }
            .launchIn(viewModelScope)
    }

    private fun clearPeopleContent() {
        viewModelScope.launch {
            clearSavedPeopleContentUseCase.invoke()
        }
    }

    private fun observeRemoveSuggestion() {
        getUserSettingsStateChangedUseCase.invoke()
            .onEach { effect ->
                if (effect is UserSettingsEffect.SuggestionRemoved) {
                    removeSuggestion(userId = effect.userId)
                }
            }
            .catch { e -> Timber.e(e) }
            .launchIn(viewModelScope)
    }

    private fun observeAuth() {
        authFinishListener.observeAuthFinishListener()
            .onEach { reloadSuggestionsAndReferral() }
            .launchIn(viewModelScope)
        authFinishListener.observeRegistrationFinishListener()
            .onEach { reloadSuggestionsAndReferral() }
            .launchIn(viewModelScope)
    }

    private fun reloadSuggestionsAndReferral() {
        loadSuggests()
        loadNewReferralInfo()
    }

    private fun handleSyncWork(workInfo: WorkInfo?) {
        val isFinishedSucceed = workInfo?.state == WorkInfo.State.SUCCEEDED
        if (isFinishedSucceed) {
            if (getSyncContactsPrivacyUseCase.invoke().not() && _liveEvent.value !is FeedViewEvent.ShowContactsHasBeenSyncDialog) {
                _liveEvent.postValue(FeedViewEvent.ShowContactsHasBeenSyncDialog)
            }
            viewModelScope.launch {
                setSyncContactsPrivacyUseCase.invoke(true)
            }
        }
        if (workInfo?.state == WorkInfo.State.FAILED) {
            _liveEvent.postValue(FeedViewEvent.ShowCommonError(R.string.error_try_later))
        }
        setProgressBySyncContactsState(workInfo)
    }

    private fun setProgressBySyncContactsState(workInfo: WorkInfo?) {
        val isFinishedSuccess = workInfo?.state == WorkInfo.State.SUCCEEDED
        val currentList = _livePosts.value?.toMutableList() ?: return
        if (isFinishedSuccess) {
            currentList.removeIf { it.feedType == FeedType.SYNC_CONTACTS }
        }
        _livePosts.value = currentList
    }

    private fun setSyncContactsSettingTrue() {
        setSettingsUseCase.invoke(
            params = SettingsParams.CommonSettingsParams(
                key = SettingsKeyEnum.ALLOW_CONTACT_SYNC.key,
                value = true.toInt()
            )
        ).invokeOnCompletion { error ->
            if (error == null) {
                startSyncContacts()
            } else {
                _liveEvent.postValue(FeedViewEvent.ShowCommonError(R.string.error_try_later))
                Timber.e(error)
            }
        }
    }

    private fun startSyncContacts() {
        viewModelScope.launch {
            startSyncContactsUseCase.invoke()
        }
    }

    private fun loadNewReferralInfo() {
        if (getUserUid() == DEFAULT_ANON_UID) return
        viewModelScope.launch {
            runCatching {
                val newReferralInfo = loadAndCacheReferralInfoUseCase.invoke()
                val newReferralInfoUiModel = referralDataUIMapper.mapReferralData(newReferralInfo)
                this@FeedViewModel.referralInfo = newReferralInfoUiModel
                setupNewReferralInfo(newReferralInfoUiModel)
            }.onFailure {
                Timber.e(it)
            }
        }
    }

    private fun setupNewReferralInfo(referralInfo: ReferralDataUIModel) {
        val items = ArrayList(getActivePosts())
        val indexOfReferralPost = items.indexOfFirst { it.feedType == FeedType.REFERRAL }
        if (indexOfReferralPost !in items.indices) return
        items.removeAt(indexOfReferralPost)
        items.add(indexOfReferralPost, LocalPosts.getReferralPost(referralInfo))
        submitList(items)
    }

    private fun loadSuggests() {
        viewModelScope.launch {
            runCatching {
                val suggests = getRoadSuggestsUseCase.invoke()
                val suggestsUiModels = userSuggestionsUiMapper.mapSuggestionToUiModel(
                    suggestions = suggests,
                    allowSyncContacts = true
                )
                this@FeedViewModel.suggests.clear()
                this@FeedViewModel.suggests.addAll(suggestsUiModels)
                replaceSuggestsIfNeeded()
            }.onFailure {
                Timber.e(it)
            }
        }
    }

    private fun replaceSuggestsIfNeeded() {
        val items = ArrayList(getActivePosts())
        val indexOfSuggests = items.indexOfFirst { it.feedType == FeedType.SUGGESTIONS }
        if (indexOfSuggests !in items.indices) return
        items.removeAt(indexOfSuggests)
        items.add(indexOfSuggests, LocalPosts.getSuggestsPost(suggests))
        submitList(items)
    }

    private fun loadReferralInfo() {
        if (getUserUid() == DEFAULT_ANON_UID) return
        val lastReferralInfo = getLastReferralInfoUseCase.invoke()
        lastReferralInfo?.let { referralInfo = referralDataUIMapper.mapReferralData(it) }
        viewModelScope.launch {
            runCatching {
                val latestReferralInfo = loadAndCacheReferralInfoUseCase.invoke()
                referralInfo = referralDataUIMapper.mapReferralData(latestReferralInfo)
            }.onFailure {
                Timber.e(it)
            }
        }
    }

    private fun setupRecSystemSettings(onFinished: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val newSettings = getAppInfoAsyncUseCase.executeAsync().await()
                settings = newSettings
                setupMainRoadFilterUseCase.invoke(newSettings)
            } catch (e: Exception) {
                Timber.e(e)
            }
            onFinished.invoke()
        }
    }

    fun getMomentDelegate() = momentDelegate

    fun initLoadMoments() {
        if(featureTogglesContainer.momentsFeatureToggle.isEnabled){
            initMomentDelegate()
            getMomentDelegate().initialLoadMoments()
        }
    }

    private fun initMomentDelegate() {
        momentDelegate.initCoroutineScope(viewModelScope)
        momentDelegate.initRoadType(roadType)
    }

    private fun observeDownloadingVideoWorkInfos() {
        viewModelScope.launch {
            getAllDownloadingVideoWorkInfosUseCase.invoke().collect { downloadEvent ->
                val downloadStateEvent = downloadEvent as? DownloadMediaEvent.PostDownloadState ?: return@collect
                val loadingInfo = LoadingPostVideoInfoUIModel(
                    loadingState = if (downloadStateEvent.postMediaDownloadType.canShowOnRoad()) {
                        downloadStateEvent.state
                    } else {
                        MediaLoadingState.NONE
                    }, loadingTime = System.currentTimeMillis(),
                    isShowLoadingProgress = true
                )
                handleVideoLoading(downloadStateEvent.postMediaDownloadType.postId, loadingInfo)
            }
        }
    }

    private fun findPostByIdAndScrollToPosition(selectedPostId: Long?) {
        if (selectedPostId == null || isScrolledToPostSuccess) return
        isScrolledToPostSuccess = true
        doDelayed(DELAY_SCROLL_TO_POST) {
            val currentList = _livePosts.value ?: return@doDelayed
            if (selectedPostId == FIRST_POST_ID) {
                scrollToFirstPositionEvent()
                return@doDelayed
            }
            val searchResult = currentList.find { post ->
                Timber.d("Bazaleev d: ${post.postId} $selectedPostId")
                post.postId == selectedPostId
            }
            searchResult?.let { entity ->
                val index = currentList.indexOf(entity)
                emitUserFeedEvent(
                    UserFeedViewEvent.ScrollToPostPosition(
                        selectedPostPosition = index, scrollDelay = DELAY_SCROLL_TO_POST
                    )
                )
            }
        }
    }

    private fun scrollToFirstPositionEvent() {
        emitUserFeedEvent(
            UserFeedViewEvent.ScrollToFirstPostPositionUiEffect(
                delayPlayVideo = DELAY_PLAY_VIDEO
            )
        )
    }

    private fun emitUserFeedEvent(event: UserFeedViewEvent) {
        viewModelScope.launch {
            _userFeedProfileViewEvent.emit(event)
        }
    }

    private fun handleVideoLoading(postId: Long, loadingInfo: LoadingPostVideoInfoUIModel) {
        val postUpdate = UIPostUpdate.UpdateLoadingState(postId = postId, loadingInfo = loadingInfo)
        liveFeedEvents.value = FeedViewEventPost.UpdatePostEvent(postUpdate)

        val posts = getActivePosts()
        val item = posts.findLast { it.postId == postId } ?: return
        val index = posts.indexOf(item)
        if (index != -1) {
            posts[index] = item.copy(loadingInfo = loadingInfo)
            submitList(posts)
        }
    }

    private fun showShimmerProgressDelayed(delay: Long) {
        doDelayed(delay) {
            showShimmerProgress()
        }
    }

    private fun showShimmerProgress() {
        val newPostCreateItem = if (roadType == RoadTypesEnum.MAIN || roadType == RoadTypesEnum.CUSTOM) {
            PostUIEntity(feedType = FeedType.CREATE_POST)
        } else {
            null
        }
        val momentShimmer = if (roadType == RoadTypesEnum.MAIN || roadType == RoadTypesEnum.SUBSCRIPTION) {
            PostUIEntity(feedType = FeedType.SHIMMER_MOMENTS_PLACEHOLDER)
        } else {
            null
        }
        val postShimmer = PostUIEntity(feedType = FeedType.SHIMMER_PLACEHOLDER)
        val shimmerItems = listOfNotNull(newPostCreateItem, momentShimmer, postShimmer)

        submitList(shimmerItems)
    }

    private fun handleShowMoreText(post: PostUIEntity) {
        val posts = getActivePosts()
        val item = posts.findLast { it.postId == post.postId } ?: return
        val index = posts.indexOf(item)
        if (index != -1) {
            posts[index] = item.copy(tagSpan = item.tagSpan?.copy(showFullText = true))
            submitList(posts)
        }
    }

    private fun handleMainRoadUpdate(startPostId: Long) {
        val lastPost = if (lastPostId > 0) lastPostId.toInt() else NETWORK_PAGE_SIZE
        getAllPosts(startPostId, lastPost)
    }

    private fun handleRateUs(rating: Int, comment: String) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                rateUsUseCase.invoke(rating = rating, comment = comment)
            }.onFailure { exception ->
                Timber.e(exception)
            }
        }
    }

    private fun handleRateUsAnalytic(ratingAnalytics: RateUsAnalyticsRating) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                rateUsAnalyticUseCase.invoke(ratingAnalytics = ratingAnalytics)
            }.onFailure { exception ->
                Timber.e(exception)
            }
        }
    }

    private fun handleHideRateUsPost() {
        viewModelScope.launch(Dispatchers.IO) {
            hideRateUsPostUseCase.invoke()

            removePostById(RATE_US_POST_ID)
        }
    }

    private fun handleGetLinkAndCopy(postId: Long?) {
        if (postId == null) return
        viewModelScope.launch(Dispatchers.IO) {
            getPostLinkUseCase.execute(params = GetPostLinkParams(postId), success = { response ->
                _liveEvent.postValue(FeedViewEvent.CopyLinkEvent(response.deeplinkUrl))
            }, fail = { exception ->
                Timber.e(exception)
                showCommonError()
            })
        }
    }

    private fun handleCheckUpdateAvailability(post: PostUIEntity, currentMedia: MediaAssetEntity?) {
        if (featureTogglesContainer.editPostFeatureToggle.isEnabled) {
            viewModelScope.launch(Dispatchers.IO) {
                checkPostUpdateAvailability.execute(
                    params = CheckPostPostParams(postId = post.postId),
                    success = { response ->
                        _liveEvent.postValue(
                            FeedViewEvent.PostEditAvailableEvent(
                                post = post,
                                isAvailable = response.isAvailable == EDIT_POST_AVAILABLE && post.isEditable(),
                                currentMedia = currentMedia
                            )
                        )
                        postMenuOpenedDate = LocalDateTime.now()
                    },
                    fail = { error ->
                        Timber.e(error)
                        _liveEvent.postValue(FeedViewEvent.PostEditAvailableEvent(
                            post = post,
                            isAvailable = false,
                            currentMedia = currentMedia)
                        )
                    }
                )
            }
        } else {
            _liveEvent.postValue(FeedViewEvent.PostEditAvailableEvent(
                post = post,
                isAvailable = false,
                currentMedia = currentMedia)
            )
        }
    }

    private fun observeNewPostCreate() {
        getNewPostStreamUseCase.invoke().subscribe({
            when (it) {
                is PostActionModel.PostCreationSuccessModel -> newPostWasCreated()
                is PostActionModel.PostEditingStartModel -> postEditingStart(it)
                is PostActionModel.PostEditingCompleteModel -> postEditingEnd(it)
                is PostActionModel.PostEditingAbortModel -> postEditingAbort(it.postId)
            }
        }, {
            Timber.e(it)
        })?.addDisposable()
    }

    private fun observeVolumeEvents() {
        subscribeVolumeEventsUseCase.invoke().onEach { event -> handleVolumeEvents(event) }.launchIn(viewModelScope)
    }

    private fun handleVolumeEvents(event: VolumeRepositoryEvent) {
        when (event) {
            is VolumeRepositoryEvent.VolumeStateUpdated -> {
                liveFeedEvents.value = FeedViewEventPost.UpdateVolumeState(event.volumeState)
            }
        }
    }

    private fun observeMomentsEvents() {
        subscribeMomentsEventsUseCase.invoke().onEach { event -> handleMomentsEvents(event) }.launchIn(viewModelScope)
    }

    private fun handleMomentsEvents(event: MomentRepositoryEvent) {
        if (featureTogglesContainer.momentsFeatureToggle.isEnabled) {
            when (event) {
                is MomentRepositoryEvent.UserMomentsStateUpdated -> {
                    when (event.action) {
                        MomentsAction.CREATED -> newMomentCreated()
                        else -> updateUserMomentsState(event.userMomentsStateUpdate)
                    }
                }

                else -> Unit
            }
        }
    }

    private fun updateUserMomentsState(userMomentsStateUpdate: UserMomentsStateUpdateModel) {
        val postUpdate = userMomentsStateUpdate.toUIPostUpdate()
        val posts = _livePosts.value ?: return
        var momentItem: PostUIEntity? = null
        for (i in 0 until posts.size) {
            var post = posts[i]
            if (post.feedType == FeedType.MOMENTS) {
                post = getUpdatedMomentsItem(
                    postUpdate = postUpdate,
                    post = post
                )
                replaceItemInPosts(position = i, post = post)
                momentInfoUiModel = post.moments

                momentItem = post
            } else if (post.getUserId() == postUpdate.userId) {
                val moments = post.user?.moments?.copy(
                    hasMoments = postUpdate.hasMoments,
                    hasNewMoments = postUpdate.hasNewMoments
                )
                val user = post.user?.copy(
                    moments = moments
                )
                post = post.copy(user = user)

                replaceItemInPosts(position = i, post = post)
            }
        }
        liveFeedEvents.value = FeedViewEventPost.UpdatePostEvent(postUpdate.copy(postMomentsBlock = momentItem))
    }

    private fun getUpdatedMomentsItem(postUpdate: UIPostUpdate.UpdateUserMomentsState, post: PostUIEntity): PostUIEntity {
        val momentsList = post.moments?.momentsCarouselList?.toMutableList()
        if (!momentsList.isNullOrEmpty()) {
            for (i in momentsList.indices) {
                if (momentsList[i] is MomentCarouselItem.MomentGroupItem) {
                    val momentGroupItem = (momentsList[i] as MomentCarouselItem.MomentGroupItem)
                    val momentGroup = updateMomentsGroupItem(
                        postUpdate = postUpdate,
                        momentsGroup = momentGroupItem.group
                    )
                    momentGroup?.let { momentsList[i] = momentGroupItem.copy(group = momentGroup) }
                } else if (momentsList[i] is MomentCarouselItem.MomentCreateItem) {
                    val momentGroupItem = (momentsList[i] as MomentCarouselItem.MomentCreateItem)
                    val momentGroup = updateMomentsGroupItem(
                        postUpdate = postUpdate,
                        momentsGroup = momentGroupItem.group
                    )
                    momentGroup?.let { momentsList[i] = momentGroupItem.copy(group = momentGroup) }
                }
            }
        }
        val moments = post.moments?.copy(momentsCarouselList = momentsList)
        return post.copy(moments = moments)
    }

    private fun updateMomentsGroupItem(postUpdate: UIPostUpdate.UpdateUserMomentsState, momentsGroup: MomentGroupUiModel): MomentGroupUiModel? {
        if (momentsGroup.userId != postUpdate.userId) return null
        var updatedMomentGroup = momentsGroup

        if (postUpdate.hasMoments && !postUpdate.hasNewMoments) {
            val viewedMoments = updatedMomentGroup.moments.toMutableList()
            for (i in 0 until viewedMoments.size) {
                if (!viewedMoments[i].isViewed) {
                    viewedMoments[i] = viewedMoments[i].copy(isViewed = true)
                }
            }
            updatedMomentGroup = updatedMomentGroup.copy(moments = viewedMoments)
        }

        return updatedMomentGroup
    }

    private fun replaceItemInPosts(position: Int, post: PostUIEntity) {
        _livePosts.value?.removeAt(position)
        _livePosts.value?.add(position, post)
    }

    private fun newMomentCreated() {
        when (roadType) {
            RoadTypesEnum.MAIN -> {
                momentDelegate.initialLoadMoments()
                getAllPosts(0)
            }

            RoadTypesEnum.CUSTOM -> getUserPosts(0, getUserUidUseCase.invoke())
            else -> Unit
        }
    }

    private fun postEditingStart(postActionModel: PostActionModel.PostEditingStartModel) {

        val loadingInfo = LoadingPostVideoInfoUIModel(
            loadingState = MediaLoadingState.LOADING_NO_CANCEL_BUTTON,
            loadingTime = System.currentTimeMillis()
        )
        handlePostUpdating(postActionModel.postId, loadingInfo)
    }

    private fun postEditingAbort(postId: Long) {
        handlePostUpdating(postId, LoadingPostVideoInfoUIModel())
        liveFeedEvents.value = FeedViewEventPost.ShowMediaExpand
    }

    private fun handlePostUpdating(
        postId: Long,
        loadingInfo: LoadingPostVideoInfoUIModel
    ) {
        val posts = getActivePosts()
        val item = posts.findLast { it.postId == postId } ?: return
        val index = posts.indexOf(item)
        if (index != -1) {
            posts[index] = item.copy(postUpdatingLoadingInfo = loadingInfo)
            submitList(posts)
        }
    }

    private fun postEditingEnd(model: PostActionModel.PostEditingCompleteModel) {
        val post = getActivePosts().findLast { it.postId == model.post.id }
        post?.let { nonNullPost ->
            val index = getActivePosts().indexOf(nonNullPost)
            val postsToSubmit = getActivePosts()
            postsToSubmit[index] = model.post.toUiEntity(
                textProcessorUtil = textProcessorUtil,
                isInSnippet = false
            )
            submitList(postsToSubmit)
            liveFeedEvents.value = FeedViewEventPost.ShowMediaExpand
        }
    }

    private fun newPostWasCreated() {
        when (roadType) {
            RoadTypesEnum.CUSTOM -> getUserPosts(0, getUserUidUseCase.invoke())
            RoadTypesEnum.MAIN -> getAllPosts(0)
            else -> Unit
        }
    }

    private fun retryGetPosts() {
        if (!isLastRequestSuccess) {
            when (roadType) {
                RoadTypesEnum.MAIN -> getAllPosts(lastPostId)
                RoadTypesEnum.CUSTOM -> roadUserId?.let { getUserPosts(lastPostId, it) }
                RoadTypesEnum.SUBSCRIPTION -> getSubscriptionPosts(lastPostId)
                else -> Unit
            }
        }
    }

    private fun hideUserRoad(userId: Long?) {
        viewModelScope.launch(Dispatchers.IO) {
            userId?.let { id ->
                val params = HidePostsOfUserParams(id)
                hidePostsOfUserUseCase.execute(params, {
                    Timber.d("Success hide user $id posts")
                    _liveEvent.postValue(
                        FeedViewEvent.OnSuccessHideUserRoad(
                            userId, R.string.user_complain_posts_hided
                        )
                    )
                }, {
                    Timber.e("Fail hide user $id posts:${it.message}")
                    _liveEvent.postValue(
                        FeedViewEvent.ShowCommonError(R.string.error_try_later)
                    )
                })
            }
        }
    }

    private fun removeSuggestion(userId: Long) {
        val newSuggestionsList = suggests.filter { it.getUserId() != userId }
        val newList = ArrayList(getActivePosts().map {  post ->
            if (post.feedType == FeedType.SUGGESTIONS) {
                post.featureData?.suggestions = newSuggestionsList
            }
            post
        })
        submitList(newList)
        this.suggests.clear()
        this.suggests.addAll(newSuggestionsList)
    }

    private fun changeSuggestionSubscriptionStatus(userId: Long, isSubscribed: Boolean = true) {
        val currentFloorUiEntity = getActivePosts()?.firstOrNull { it.feedType == FeedType.SUGGESTIONS } ?: return
        val currentSuggestions = currentFloorUiEntity.featureData?.suggestions ?: return
        val newSuggestions = ArrayList(currentSuggestions.map {
            if (it.getUserId() == userId && it is ProfileSuggestionUiModels.ProfileSuggestionUiModel) {
                it.copy(isSubscribed = isSubscribed)
            } else {
                it
            }
        })
        val newPost = LocalPosts.getSuggestsPost(newSuggestions)
        val newValue = ArrayList(getActivePosts().map { post ->
            if (post.feedType == FeedType.SUGGESTIONS) {
                newPost
            } else {
                post
            }
        })
        submitList(newValue)
        this.suggests.clear()
        this.suggests.addAll(newSuggestions)
    }

    private fun unsubscribeFromUserAndClear(postId: Long?, userId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = subscriptionsUseCase.deleteFromSubscriptions(mutableListOf(userId))
                if (result.data != null) {
                    _liveEvent.postValue(FeedViewEvent.OnSuccessHideUserRoad(userId, R.string.disabled_new_post_notif))
                    val newList = getActivePosts().filter { it.user?.userId != userId }
                    handlePlaceHolders(newList.filter { it.feedType != FeedType.CREATE_POST }.size)
                    postId?.let { postId ->
                        handleSuccessUnsubscribeUser(
                            postId = postId,
                            userId = userId,
                            needToPushAmplitude = false,
                            isApproved = false,
                            fromFollowButton = false,
                            topContentMaker = false
                        )
                    }
                    submitList(newList)
                } else showCommonError()
            } catch (e: Exception) {
                Timber.e(e)
                showCommonError()
            }
        }
    }

    private fun unsubscribeFromUser(
        postId: Long,
        userId: Long,
        fromFollowButton: Boolean,
        isApproved: Boolean, topContentMaker: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val res = subscriptionsUseCase.deleteFromSubscriptions(listOf(userId))
                if (res.data != null) {
                    handleSuccessUnsubscribeUser(
                        postId = postId,
                        userId = userId,
                        needToPushAmplitude = true,
                        fromFollowButton = fromFollowButton,
                        isApproved = isApproved,
                        topContentMaker = topContentMaker
                    )
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private suspend fun handleSuccessUnsubscribeUser(
        postId: Long,
        userId: Long,
        needToPushAmplitude: Boolean,
        fromFollowButton: Boolean,
        isApproved: Boolean,
        topContentMaker: Boolean
    ) {
        updateReactiveSubscription.execute(params = UpdateSubscriptionUserParams(
            postId = postId,
            userId = userId,
            isSubscribed = false,
            needToHideFollowButton = false,
            isBlocked = false
        ), success = {
            if (needToPushAmplitude) {
                logUnsubscribePost(
                    userId = userId,
                    roadType = roadType ?: return@execute,
                    fromFollowButton = fromFollowButton,
                    isApproved = isApproved,
                    topContentMaker = topContentMaker
                )
            }
        }, fail = {})
    }

    private fun subscribeUser(
        postId: Long,
        userId: Long,
        needToHideFollowButton: Boolean,
        fromFollowButton: Boolean,
        isApproved: Boolean,
        topContentMaker: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val res = subscriptionsUseCase.addSubscription(listOf(userId))
                if (res.data != null) {
                    handleSuccessSubscribeUser(
                        postId = postId,
                        userId = userId,
                        needToHideFollowButton = needToHideFollowButton,
                        fromFollowButton = fromFollowButton,
                        isApproved = isApproved,
                        topContentMaker = topContentMaker
                    )
                }
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private suspend fun handleSuccessSubscribeUser(
        postId: Long,
        userId: Long,
        needToHideFollowButton: Boolean,
        fromFollowButton: Boolean,
        isApproved: Boolean,
        topContentMaker: Boolean
    ) {
        updateReactiveSubscription.execute(params = UpdateSubscriptionUserParams(
            postId = postId,
            userId = userId,
            isSubscribed = true,
            needToHideFollowButton = needToHideFollowButton,
            isBlocked = false
        ), success = {
            logSubscribePost(
                userId = userId,
                roadType = roadType?: return@execute,
                fromFollowButton = fromFollowButton,
                isApproved = isApproved,
                topContentMaker = topContentMaker
            )
        }, fail = {})
    }

    private fun logSubscribePost(
        userId: Long,
        roadType: RoadTypesEnum,
        fromFollowButton: Boolean,
        isApproved: Boolean,
        topContentMaker: Boolean
    ) {
        val propertyType = if (fromFollowButton) AmplitudePropertyType.POST else AmplitudePropertyType.POST_MENU
        val influencerProperty = createInfluencerAmplitudeProperty(
            topContentMaker = topContentMaker, approved = isApproved
        )
        amplitudeFollowButton.followAction(
            fromId = getUserUidUseCase.invoke(),
            toId = userId,
            where = roadType.toAmplitudeFollowButtonPropertyWhere(),
            type = propertyType,
            amplitudeInfluencerProperty = influencerProperty
        )
    }

    private fun logUnsubscribePost(
        userId: Long,
        roadType: RoadTypesEnum,
        fromFollowButton: Boolean,
        isApproved: Boolean,
        topContentMaker: Boolean
    ) {
        val propertyType = if (fromFollowButton) AmplitudePropertyType.POST else AmplitudePropertyType.POST_MENU
        val amplitudeInfluencerProperty = createInfluencerAmplitudeProperty(
            topContentMaker = topContentMaker, approved = isApproved
        )
        amplitudeFollowButton.logUnfollowAction(
            fromId = getUserUid(),
            toId = userId,
            where = roadType.toAmplitudeFollowButtonPropertyWhere(),
            type = propertyType,
            amplitudeInfluencerProperty = amplitudeInfluencerProperty
        )
    }

    private fun showCommonError() = _liveEvent.postValue(FeedViewEvent.ShowCommonError(R.string.error_try_later))

    private fun complainToPost(postId: Long?) {
        viewModelScope.launch(Dispatchers.IO) {
            postId?.let { id ->
                val params = ComplainPostParams(id)
                postComplainUseCase.execute(params, {
                    _liveEvent.postValue(
                        FeedViewEvent.ShowCommonSuccess(R.string.road_complaint_send_success)
                    )
                }, { exception ->
                    Timber.e("Error complain to post:$id EX:${exception.message}")
                    _liveEvent.postValue(
                        FeedViewEvent.ShowCommonError(R.string.error_try_later)
                    )
                })
            }
        }
    }

    private fun deletePost(post: PostUIEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val params = DeletePostParams(post.postId)
            deletePostUseCase.execute(params, {
                Timber.d("Success delete post")
                logMapEventDelete(post)
            }, { exception ->
                Timber.e("Fail delete post:${post.postId} EX:${exception.message}")
                _liveEvent.postValue(FeedViewEvent.ShowCommonError(R.string.error_try_later))
            })
        }
    }

    private fun openEditPost(post: PostUIEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            checkPostUpdateAvailability.execute(
                params = CheckPostPostParams(postId = post.postId),
                success = { response ->
                    if (response.isAvailable == EDIT_POST_AVAILABLE && response.notAvailableReason == null) {
                        _liveEvent.postValue(FeedViewEvent.OpenEditPostEvent(post = post))
                    } else {
                        response.notAvailableReason?.let { reason ->
                            _liveEvent.postValue(FeedViewEvent.ShowAvailabilityError(reason.toUiEntity()))
                        } ?: _liveEvent.postValue(FeedViewEvent.ShowAvailabilityError(EVENT_POST_UNABLE_TO_UPDATE))
                    }
                },
                fail = { error ->
                    Timber.e(error)
                    _liveEvent.postValue(FeedViewEvent.ShowAvailabilityError(EVENT_POST_UNABLE_TO_UPDATE))
                }
            )
        }
    }

    private fun saveLastPostMediaViewInfo(lastPostMediaViewInfo: PostMediaViewInfo?) {
        setLastPostMediaViewInfoUseCase.invoke(lastPostMediaViewInfo)
    }

    private fun subscribeToPost(postId: Long?, titles: PostSubscribeTitle) {
        viewModelScope.launch(Dispatchers.IO) {
            postId?.let { id ->
                val params = SubscribePostParams(id)
                subscribePostUseCase.execute(params, {
                    handleSuccessSubscribePost(titles)
                }, {
                    _liveEvent.postValue(FeedViewEvent.ShowCommonError(R.string.error_try_later))
                })
            }
        }
    }

    private fun getActivePosts(): MutableList<PostUIEntity> {
        val res = mutableListOf<PostUIEntity>()
        _livePosts.value?.let {
            res.addAll(it.filter { feed ->
                feed.feedType != FeedType.SHIMMER_PLACEHOLDER
                    && feed.feedType != FeedType.SHIMMER_MOMENTS_PLACEHOLDER
            })
        }
        return res
    }

    private fun handleSuccessSubscribePost(title: PostSubscribeTitle) {
        val result = when(title) {
            is PostSubscribeTitle.NotificationString -> title.subscribeString
            is PostSubscribeTitle.SubscribeString -> title.subscribeString
        }
        _liveEvent.postValue(result?.let { FeedViewEvent.ShowCommonSuccess(it) })
    }

    private fun unsubscribeFromPost(postId: Long?, titles: PostSubscribeTitle) {
        viewModelScope.launch(Dispatchers.IO) {
            postId?.let { id ->
                val params = UnsubscribePostParams(id)
                unsubscribePostUseCase.execute(params, {
                    handleSuccessUnSubscribePost(titles)
                }, { exception ->
                    Timber.e("Error unsubscribe post:$id Ex:${exception.message}")
                    _liveEvent.postValue(FeedViewEvent.ShowCommonError(R.string.error_try_later))
                })
            }
        }
    }

    private fun handleSuccessUnSubscribePost(title: PostSubscribeTitle) {
        val result = when(title) {
            is PostSubscribeTitle.NotificationString -> title.unsubscribeString
            is PostSubscribeTitle.SubscribeString -> title.unsubscribeString
        }
        _liveEvent.postValue(result?.let { FeedViewEvent.ShowCommonSuccess(it) })
    }

    private fun dismissFeature(featureId: Long, dismiss: Boolean, deepLink: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val params = FeatureParams(featureId = featureId)
            removeFeatureIdAndSubmit(featureId)
            featureUseCase.execute(params, {
                if (dismiss.not() && !deepLink.isNullOrEmpty()) {
                    _liveEvent.postValue(FeedViewEvent.OpenDeepLink(deepLink))
                }
            }, { exception ->
                Timber.e("Error dismiss feature:$featureId Ex:${exception.message}")
                _liveEvent.postValue(FeedViewEvent.ShowCommonError(R.string.error_try_later))
            })
        }
    }

    private fun removeFeatureIdAndSubmit(featureId: Long) {
        val res = getActivePosts()
        res.removeAll { it.featureData != null && it.featureData?.id == featureId }
        featureList.removeAll { it.featureData?.id == featureId }
        submitList(res)
    }

    private fun getAllPosts(startPostId: Long, quantity: Int = NETWORK_PAGE_SIZE) {
        val cities = getCitiesForMainFilterUseCase.invoke()
        val counties = getCountriesForMainFilterUseCase.invoke()
        getPostsCommon(
            startPostId = startPostId,
            roadType = ROAD_TYPE_MAIN,
            userId = 0,
            groupId = 0,
            cityIds = cities,
            countryParams = if (cities.isEmpty() || cities == "0") counties else String.empty(),
            hashtag = String.empty(),
            quantity = quantity,
            recommended = checkMainFilterRecommendedUseCase.invoke()
        )
        reloadSuggestionsAndReferral()
    }

    private fun getUserPosts(
        startPostId: Long,
        userId: Long,
        selectedPostId: Long? = null
    ) {
        if (userId == 0L) return

        this.roadUserId = userId
        getPostsCommon(
            startPostId = startPostId,
            roadType = ROAD_TYPE_ALL,
            userId = userId,
            groupId = 0,
            cityIds = String.empty(),
            countryParams = String.empty(),
            hashtag = String.empty(),
            selectedPostId = selectedPostId
        )
    }

    private fun getSubscriptionPosts(startPostId: Long) {
        getPostsCommon(
            startPostId = startPostId,
            roadType = ROAD_TYPE_SUBSCRIPTION,
            userId = 0,
            groupId = 0,
            cityIds = String.empty(),
            countryParams = String.empty(),
            hashtag = String.empty(),
            includeGroups = false
        )
        setSubPostsRequestedInSession.invoke(true)
    }

    private fun getGroupPosts(startPostId: Long, groupId: Int) {
        getPostsCommon(
            startPostId = startPostId,
            roadType = ROAD_TYPE_ALL,
            userId = 0,
            groupId = groupId,
            cityIds = String.empty(),
            countryParams = String.empty(),
            hashtag = String.empty()
        )
    }

    private fun getHashtagPosts(startPostId: Long, hashtag: String?) {
        Timber.d("getHashtagPosts $startPostId, hash = $hashtag")
        if (hashtag == null) return
        getPostsCommon(
            startPostId = startPostId,
            roadType = ROAD_TYPE_HASHTAG,
            userId = 0,
            groupId = 0,
            cityIds = String.empty(),
            countryParams = String.empty(),
            hashtag = hashtag.replace("#", "")
        )
    }

    private fun getPostsCommon(
        startPostId: Long,
        roadType: Int,
        userId: Long,
        groupId: Int,
        cityIds: String,
        countryParams: String,
        hashtag: String,
        includeGroups: Boolean? = true,
        recommended: Boolean? = false,
        quantity: Int = NETWORK_PAGE_SIZE,
        selectedPostId: Long? = null
    ) {
        lastPostId = startPostId
        isLoading = true
        isLastPage = false

        loadingPostsJob?.cancel()
        loadingPostsJob = viewModelScope.launch(Dispatchers.IO) {
            val momentsBlockAvatar = getUserSmallAvatarUseCase.invoke()
            if (startPostId != 0L) {
                _liveEvent.postValue(FeedViewEvent.OnShowLoader(true))
            }
            val params = GetPostsParams(
                startPostId = startPostId,
                quantity = quantity,
                roadType = roadType,
                cityId = cityIds,
                userId = userId,
                groupId = groupId,
                countryIds = countryParams,
                hashtag = hashtag,
                includeGroups = includeGroups,
                recommended = recommended
            )
            getPosts.execute(params, { response ->
                val posts = response.toUiEntity(textProcessorUtil)

                val features = response.toFeatureUiEntity(textProcessorUtil)
                val type = this@FeedViewModel.roadType
                launch(Dispatchers.Main) {
                    if (startPostId == 0L) {
                        if (type == RoadTypesEnum.MAIN) {
                            featureList.clear()
                            featureList.addAll(features)
                            mergePostsWithFeaturesAndSubmit(
                                posts = posts,
                                featureList = featureList,
                                momentsBlockAvatar = momentsBlockAvatar,
                                requestedQuantity = quantity
                            )
                        } else {
                            mergePostsWithFeaturesAndSubmit(
                                posts = posts,
                                featureList = emptyList(),
                                momentsBlockAvatar = momentsBlockAvatar,
                                requestedQuantity = quantity
                            )
                        }
                        handlePlaceHolders(posts.size)
                        _liveEvent.postValue(FeedViewEvent.OnFirstPageLoaded(posts, features))
                    } else {
                        appendListAndSubmit(
                            posts = posts,
                            momentsBlockAvatar = momentsBlockAvatar,
                            requestedQuantity = quantity
                        )
                        _liveEvent.value = FeedViewEvent.OnShowLoader(false)
                    }
                    response.hashtag?.count?.let {
                        _liveEvent.value = FeedViewEvent.TotalPostCount(it)
                    }
                    if (posts.isEmpty()) {
                        isLastPage = true
                    }
                    isLoading = false
                    findPostByIdAndScrollToPosition(selectedPostId)
                    uploadPostViews()
                }
                isLastRequestSuccess = true
            }, { exception ->
                isLastRequestSuccess = false
                if (exception !is CancellationException) {
                    Timber.e(exception)
                    _liveEvent.postValue(
                        FeedViewEvent.ShowErrorAndHideProgress(R.string.error_load_posts)
                    )
                }
            })
        }
    }

    private fun handlePlaceHolders(size: Int) {
        viewModelScope.launch(Dispatchers.Main) {
            if (size == 0) {
                _liveEvent.value = FeedViewEvent.PlaceHolderEvent(false)
            } else {
                _liveEvent.value = FeedViewEvent.PlaceHolderEvent(true)
            }
        }
    }

    private fun observeReactions() {
        getCommandReactionStreamUseCase.execute().subscribe(::handleReactionUpdate).addDisposable()
    }

    private fun appendListAndSubmit(posts: List<PostUIEntity>, momentsBlockAvatar: String?, requestedQuantity: Int) {
        val resultList = getActivePosts().toMutableList()

        resultList.addAll(filterAndAddFollowButtons(inputPostList = posts))
        addFeatures(
            inputPostList = resultList,
            externalFeatureList = featureList,
            localFeatureList = generateLocalFeatures(momentsBlockAvatar)
        )
        addPostsViewedBlock(
            inputPostList = resultList,
            newPosts = posts,
            requestedQuantity = requestedQuantity
        )

        submitList(resultList)
    }

    private fun addPostsViewedBlock(
        inputPostList: MutableList<PostUIEntity>,
        newPosts: List<PostUIEntity>,
        requestedQuantity: Int
    ) {
        val isVip = inputPostList.firstOrNull()?.user?.accountType == AccountTypeEnum.ACCOUNT_TYPE_VIP
        val postsViewedPost = LocalPosts.getPostsViewedPost(originEnum, isVip) ?: return
        when (originEnum) {
            DestinationOriginEnum.SUBSCRIPTIONS_ROAD -> {
                addPostsViewedBlockSubscriptions(postsViewedPost, inputPostList)
            }
            DestinationOriginEnum.OTHER_PROFILE -> {
                addPostsViewedBlockProfile(postsViewedPost, inputPostList, newPosts, requestedQuantity)
            }
            else -> return
        }
    }

    private fun addPostsViewedBlockSubscriptions(
        postsViewedPost: PostUIEntity,
        inputPostList: MutableList<PostUIEntity>
    ) {
        val position = inputPostList.indexOfFirst { !it.isNewSubsPost && it.feedType != FeedType.MOMENTS }
        val startPosition = if (inputPostList.firstOrNull()?.feedType == FeedType.MOMENTS) {
            START_POSITION_WITH_MOMENTS
        } else {
            START_POSITION_WITHOUT_MOMENTS
        }
        if (position <= startPosition) return
        inputPostList.removeIf { it.postId == postsViewedPost.postId }
        inputPostList.add(position, postsViewedPost)
    }

    private fun addPostsViewedBlockProfile(
        postsViewedPost: PostUIEntity,
        inputPostList: MutableList<PostUIEntity>,
        newPosts: List<PostUIEntity>,
        requestedQuantity: Int
    ) {
        if ((newPosts.isEmpty() || newPosts.size < requestedQuantity) && inputPostList.isNotEmpty()) {
            inputPostList.removeIf { it.postId == postsViewedPost.postId }
            inputPostList.add(postsViewedPost)
        }
    }

    private fun filterAndAddFollowButtons(inputPostList: List<PostUIEntity>) =
        inputPostList.map { post ->
            val isMyPost = post.isMyPost(getUserUidUseCase.invoke())
            val isSubscribed = post.isSubscribedToPostUser()
            val isHashTagOrMainRoad = roadType == RoadTypesEnum.MAIN || roadType == RoadTypesEnum.HASHTAG

            post.copy(
                needToShowFollowButton = isHashTagOrMainRoad && !isMyPost && !isSubscribed
            )
        }.toMutableList()

    private fun submitList(data: List<PostUIEntity>) = viewModelScope.launch(Dispatchers.Main) {
        _livePosts.value = data.toMutableList()
    }

    private fun mergePostsWithFeaturesAndSubmit(
        posts: List<PostUIEntity>,
        featureList: List<PostUIEntity>,
        momentsBlockAvatar: String?,
        requestedQuantity: Int
    ) {
        val localFeatureList = generateLocalFeatures(momentsBlockAvatar)
        val resultList = mutableListOf<PostUIEntity>()

        resultList.addAll(
            filterAndAddFollowButtons(inputPostList = posts)
        )
        addFeatures(
            inputPostList = resultList,
            externalFeatureList = featureList,
            localFeatureList = localFeatureList
        )
        addPostsViewedBlock(
            inputPostList = resultList,
            newPosts = resultList,
            requestedQuantity = requestedQuantity
        )

        submitList(resultList)
    }

    private fun generateLocalFeatures(momentsBlockAvatar: String?): List<PostUIEntity> {
        val moments = LocalPosts.getMoments(
            model = momentInfoUiModel,
            blockAvatar = momentsBlockAvatar,
            roadType = roadType
        )

        return listOfNotNull(
            LocalPosts.getAddNew().takeIf { roadType == RoadTypesEnum.MAIN || roadType == RoadTypesEnum.CUSTOM },
            LocalPosts.getRateUsPost().takeIf { roadType == RoadTypesEnum.MAIN && isNeedToGetRateUseCase.invoke() },
            moments.takeIf {
                val isCorrectRoad = (roadType == RoadTypesEnum.MAIN || roadType == RoadTypesEnum.SUBSCRIPTION)
                val isCorrectData = momentInfoUiModel?.momentsCarouselList.isNullOrEmpty().not()

                isCorrectRoad && isCorrectData
            },
            LocalPosts.getSuggestsPost(suggests).takeIf { roadType == RoadTypesEnum.MAIN &&
                    getUserUid() != DEFAULT_ANON_UID
            },
            LocalPosts.getSyncContactsPost().takeIf { roadType == RoadTypesEnum.MAIN &&
                    getUserUid() != DEFAULT_ANON_UID &&
                    getSyncContactsPrivacyUseCase.invoke().not()
            },
            referralInfo?.let {
                LocalPosts.getReferralPost(it).takeIf { roadType == RoadTypesEnum.MAIN &&
                    getUserUid() != DEFAULT_ANON_UID
                }
            },
        )
    }

    private fun PostUIEntity.setFollowButtonState(
        postId: Long?,
        isSubscribed: Boolean,
        needToHideFollowButton: Boolean
    ): PostUIEntity {
        val isMyPost = isMyPost(getUserUidUseCase.invoke())
        val isHashTagOrMainRoad = roadType == RoadTypesEnum.MAIN || roadType == RoadTypesEnum.HASHTAG
        return this.copy(
            needToShowFollowButton = when {
                !isSubscribed -> !isMyPost && isHashTagOrMainRoad
                !isMyPost && isHashTagOrMainRoad -> postId == this.postId && !needToHideFollowButton
                else -> false
            }
        )
    }

    private fun addFeatures(
        inputPostList: MutableList<PostUIEntity>,
        externalFeatureList: List<PostUIEntity>,
        localFeatureList: List<PostUIEntity>
    ) {
        insertFeaturePosts(inputPostList = inputPostList, featuresToInsert = externalFeatureList)
        insertFeaturePosts(inputPostList = inputPostList, featuresToInsert = localFeatureList)
    }

    private fun insertFeaturePosts(
        inputPostList: MutableList<PostUIEntity>,
        featuresToInsert: List<PostUIEntity>,
    ) {
        featuresToInsert.forEach { featurePost ->
            inputPostList.removeIf { it.postId == featurePost.postId }

            featurePost.featureData?.positions?.forEach { index ->
                if (index < inputPostList.size) {
                    inputPostList.add(index, featurePost)
                }
            }
        }
    }

    private fun handleReactionUpdate(reactionUpdate: ReactionUpdate) {
        val postUpdate = reactionUpdate.toUIPostUpdate() as UIPostUpdate.UpdateReaction

        val postIndex = _livePosts.value?.indexOfFirst { it.postId == postUpdate.postId } ?: -1
        val post = _livePosts.value?.getOrNull(postIndex)?.copy(reactions = reactionUpdate.reactionList) ?: return

        // TODO вынести эту логику в handleFeedUpdate
        _livePosts.value?.removeAt(postIndex)
        _livePosts.value?.add(postIndex, post)

        liveFeedEvents.value = FeedViewEventPost.UpdatePostEvent(postUpdate)
    }

    private fun observeEventParticipationChanges() {
        viewModelScope.launch {
            observeEventParticipationChangesUseCase.invoke()
                .onEach { post ->
                    val updatedPost = post.toUiEntity(textProcessorUtil = textProcessorUtil, isInSnippet = false)
                    updateEventPost(updatedPost)
                }
                .catch { Timber.e(it) }
                .launchIn(viewModelScope)
        }
    }

    private fun disposeAuthDisposable() {
        observeAuthDisposable?.dispose()
        observeAuthDisposable = null
    }

    /**
     * Реактивные события, прилетают из репозитория
     * */
    private fun observeFeedState() {
        getFeedStateUseCase.execute(DefParams()).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(::handleFeedUpdate) { Timber.e(it) }.addDisposable()
    }

    /**
     * Обработка реактивных событий
     * */
    private fun handleFeedUpdate(updatedItem: FeedUpdateEvent?) {
        updatedItem?.let { item ->
            when (item) {
                is FeedUpdateEvent.FeedUpdateMoments -> handleFeedMomentsUpdate(item.toUIPostUpdate())
                is FeedUpdateEvent.FeedUpdatePayload -> handleUpdateReactivePost(item.toUIPostUpdate())
                is FeedUpdateEvent.FeedUpdateAll -> subjectPosts.onNext(FeedViewEvent.UpdatePostById(item.postId))
                is FeedUpdateEvent.FeedPostRemoved -> removePostById(item.postId)
                is FeedUpdateEvent.FeedHideUserRoad -> handleFeedHideUserRoad(item)
                is FeedUpdateEvent.FeedPostSubscriptionChanged -> handleSubscriptionPostChanged(
                    item.postId,
                    item.isSubscribed
                )

                is FeedUpdateEvent.FeedUserSubscriptionChanged -> handleUserSubscriptionChanged(
                    postId = item.postId,
                    userId = item.userId,
                    subscribed = item.isSubscribed,
                    needToHideFollowButton = item.needToHideFollowButton
                )
                else -> Unit
            }
        }
    }

    private fun handleUserSubscriptionChanged(
        postId: Long?,
        userId: Long,
        subscribed: Boolean,
        needToHideFollowButton: Boolean
    ) {
        try {
            val newPosts = getActivePosts()
            val posts = getActivePosts().filter { it.user?.userId == userId }
            posts.forEach {
                val index = newPosts.indexOf(it)
                newPosts[index] = it.copy(
                    user = it.user?.copy(
                        subscriptionOn = subscribed.toInt()
                    )
                )
                newPosts[index] = newPosts[index].setFollowButtonState(
                    postId = postId, isSubscribed = subscribed, needToHideFollowButton = needToHideFollowButton
                )
            }
            changeSuggestionSubscriptionStatus(userId = userId, isSubscribed = subscribed)
            submitList(newPosts)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun handleSubscriptionPostChanged(postId: Long, subscribed: Boolean) {
        val res = getActivePosts()
        val post = res.findLast { it.postId == postId }
        post?.let {
            val index = res.indexOf(it)
            res[index] = post.copy(isPostSubscribed = subscribed)
            submitList(res)
        }
    }

    private fun handleUpdateReactivePost(update: UIPostUpdate) {
        val post = getActivePosts().findLast { it.postId == update.postId }
        post?.let { nonNullPost ->
            val commentCount = update.commentCount ?: nonNullPost.commentCount
            val repostCount = update.repostCount ?: nonNullPost.repostCount
            val reactions = update.reactions ?: nonNullPost.reactions
            val index = getActivePosts().indexOf(nonNullPost)
            val postUpdate = UIPostUpdate(
                postId = nonNullPost.postId,
                commentCount = commentCount,
                repostCount = repostCount,
                reactions = reactions
            )
            val updatedPost = nonNullPost.copy(
                commentCount = commentCount, repostCount = repostCount, reactions = reactions
            )
            _livePosts.value?.set(index, updatedPost)
            liveFeedEvents.value = FeedViewEventPost.UpdatePostValues(postUpdate)
        }
    }

    private fun removePostById(postId: Long) {
        val res = getActivePosts().filter { it.postId != postId }

        handlePlaceHolders(res.filter {
            it.feedType != FeedType.CREATE_POST
                && it.feedType != FeedType.SHIMMER_PLACEHOLDER
                && it.feedType != FeedType.SHIMMER_MOMENTS_PLACEHOLDER
        }.size)

        if (res.isEmpty()) {
            _liveEvent.postValue(FeedViewEvent.EmptyFeed)
        }

        submitList(res)
    }

    private fun handleFeedMomentsUpdate(item: UIPostUpdate.UpdateMoments) {
        if (item.roadType != roadType) return

        momentInfoUiModel = getMomentInfoWithUniqueItems(item.moments)

        mergePostsWithFeaturesAndSubmit(
            posts = getActivePosts(),
            featureList = featureList,
            momentsBlockAvatar = item.momentsBlockAvatar,
            requestedQuantity = getActivePosts().size
        )

        val momentsHolder = getActivePosts().firstOrNull { it.postId == item.postId }
        val isInitHolderUpdate = momentsHolder == null && !item.moments?.momentsCarouselList.isNullOrEmpty()

        if (isInitHolderUpdate) {
            _liveEvent.postValue(FeedViewEvent.OnMomentsFirstLoaded(roadType))
        } else if (item.scrollToStart){
            _liveEvent.postValue(FeedViewEvent.ScrollMomentsToStart)
        }
    }

    private fun getMomentInfoWithUniqueItems(momentsInfoModel: MomentInfoCarouselUiModel?): MomentInfoCarouselUiModel? {
        return momentsInfoModel?.copy(momentsCarouselList = momentsInfoModel.momentsCarouselList?.toSet()?.toList())
    }

    private fun handleFeedHideUserRoad(item: FeedUpdateEvent.FeedHideUserRoad) {
        if (roadType == RoadTypesEnum.MAIN || roadType == RoadTypesEnum.COMMUNITY) {
            val res = getActivePosts().filter { it.user?.userId != item.userId }
            handlePlaceHolders(res.filter {
                it.feedType != FeedType.CREATE_POST
                    && it.feedType != FeedType.SHIMMER_PLACEHOLDER
                    && it.feedType != FeedType.SHIMMER_MOMENTS_PLACEHOLDER
            }.size)
            submitList(res)
        }
    }

    private fun logMapEventWantToGo(post: PostUIEntity) {
        val eventId = post.event?.id ?: return
        val authorId = post.user?.userId ?: return
        val mapEventIdParamsAnalyticsModel = MapEventIdParamsAnalyticsModel(
            eventId = eventId,
            authorId = authorId
        )
        val mapEventInvolvementParamsAnalyticsModel = MapEventInvolvementParamsAnalyticsModel(
            membersCount = post.event.participation.participantsCount,
            reactionCount = post.reactions?.size ?: 0,
            commentCount = post.commentCount
        )
        mapEventsAnalyticsInteractor.logMapEventWantToGo(
            mapEventIdParamsAnalyticsModel = mapEventIdParamsAnalyticsModel,
            where = AmplitudePropertyMapEventsWantToGoWhere.FEED,
            mapEventInvolvementParamsAnalyticsModel = mapEventInvolvementParamsAnalyticsModel
        )
    }

    private fun logMapEventDelete(post: PostUIEntity) {
        post.event ?: return
        val mapEventDeletedEventParamsAnalyticsModel = mapAnalyticsMapperImpl
            .mapMapEventDeletedEventParamsAnalyticsModel(post) ?: return
        mapEventsAnalyticsInteractor.logMapEventDelete(
            mapEventDeletedEventParamsAnalyticsModel = mapEventDeletedEventParamsAnalyticsModel,
            where = AmplitudePropertyMapEventsDeleteWhere.POST
        )
    }

    private fun logMapEventMemberDeleteYouself(post: PostUIEntity) {
        val eventId = post.event?.id ?: return
        val authorId = post.user?.userId ?: return
        mapEventsAnalyticsInteractor.logMapEventMemberDeleteYouself(
            MapEventIdParamsAnalyticsModel(
                eventId = eventId,
                authorId = authorId
            )
        )
    }

    private fun Disposable.addDisposable() {
        disposable.add(this)
    }

    companion object {
        const val NETWORK_PAGE_SIZE = 30
        const val ROAD_TYPE_ALL = 0
        const val ROAD_TYPE_MAIN = 3
        const val ROAD_TYPE_SUBSCRIPTION = 5
        const val ROAD_TYPE_HASHTAG = 6
        const val DELAY_SHIMMER_REQUEST = 300L
        const val DELAY_SCROLL_TO_POST = 150L
        const val DELAY_PLAY_VIDEO = 150L
        const val DELAY_EVENT_SHARING_SUGGESTION = 500L

        const val RECOMMENDED_ROAD_ENABLED_SETTING_NAME = "rec_road_enabled"
        const val DEFAULT_ROAD_TYPE_SETTING_NAME = "default_main_road_type"
    }
}
