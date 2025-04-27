package com.numplates.nomera3.di

import com.meera.core.di.scopes.AppScope
import com.numplates.nomera3.data.repository.SyncContactsRepositoryImpl
import com.numplates.nomera3.domain.repository.SyncContactsRepository
import com.numplates.nomera3.modules.appDialogs.data.DialogPreparationRepository
import com.numplates.nomera3.modules.appDialogs.data.DialogPreparationRepositoryImpl
import com.numplates.nomera3.modules.appInfo.data.repository.AppInfoAsyncRepository
import com.numplates.nomera3.modules.appInfo.data.repository.AppInfoAsyncRepositoryImpl
import com.numplates.nomera3.modules.appInfo.data.repository.AppInfoRepository
import com.numplates.nomera3.modules.appInfo.data.repository.AppInfoRepositoryImpl
import com.numplates.nomera3.modules.auth.data.repository.AuthRepository
import com.numplates.nomera3.modules.auth.data.repository.AuthRepositoryImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.data.repository.mapvisibilitysettings.MapVisibilitySettingsAnalyticsRepository
import com.numplates.nomera3.modules.baseCore.helper.amplitude.data.repository.mapvisibilitysettings.MapVisibilitySettingsAnalyticsRepositoryImpl
import com.numplates.nomera3.modules.baseCore.helper.amplitude.data.repository.usersnippet.MapSnippetAnalyticsRepository
import com.numplates.nomera3.modules.baseCore.helper.amplitude.data.repository.usersnippet.MapSnippetAnalyticsRepositoryImpl
import com.numplates.nomera3.modules.bump.data.repository.ShakeRepositoryImpl
import com.numplates.nomera3.modules.bump.domain.repository.ShakeRepository
import com.numplates.nomera3.modules.calls.data.CallManagerImpl
import com.numplates.nomera3.modules.calls.domain.CallManager
import com.numplates.nomera3.modules.chat.data.repository.ChatDemoMessagesRepositoryImpl
import com.numplates.nomera3.modules.chat.data.repository.ChatMessageRepositoryImpl
import com.numplates.nomera3.modules.chat.data.repository.ChatPersistDbRepositoryImpl
import com.numplates.nomera3.modules.chat.data.repository.ChatPersistRepository
import com.numplates.nomera3.modules.chat.data.repository.ChatPersistRepositoryImpl
import com.numplates.nomera3.modules.chat.data.repository.ChatRepositoryImlp
import com.numplates.nomera3.modules.chat.data.repository.GroupChatRepositoryImpl
import com.numplates.nomera3.modules.chat.domain.ChatMessageRepository
import com.numplates.nomera3.modules.chat.domain.ChatMessagesDemoRepository
import com.numplates.nomera3.modules.chat.domain.ChatPersistDbRepository
import com.numplates.nomera3.modules.chat.domain.ChatRepository
import com.numplates.nomera3.modules.chat.domain.GroupChatRepository
import com.numplates.nomera3.modules.chat.drafts.data.repository.DraftsRepositoryImpl
import com.numplates.nomera3.modules.chat.drafts.domain.DraftsRepository
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.data.MediakeyboardRepositoryImpl
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.domain.MediakeyboardRepository
import com.numplates.nomera3.modules.chat.messages.data.repository.MessagesRepository
import com.numplates.nomera3.modules.chat.messages.data.repository.MessagesRepositoryImpl
import com.numplates.nomera3.modules.chat.requests.data.repository.ChatRequestRepository
import com.numplates.nomera3.modules.chat.requests.data.repository.ChatRequestRepositoryImpl
import com.numplates.nomera3.modules.chat.requests.data.repository.RemoveRoomRepository
import com.numplates.nomera3.modules.chat.requests.data.repository.RemoveRoomRepositoryImpl
import com.numplates.nomera3.modules.chat.sendmessage.data.SendMessageDataRepository
import com.numplates.nomera3.modules.chat.sendmessage.data.SendMessageDataRepositoryImpl
import com.numplates.nomera3.modules.chat.toolbar.data.repository.ChatToolbarRepository
import com.numplates.nomera3.modules.chat.toolbar.data.repository.ChatToolbarRepositoryImpl
import com.numplates.nomera3.modules.chatfriendlist.data.FriendlistRepositoryImpl
import com.numplates.nomera3.modules.chatfriendlist.domain.GetFriendlistUsecase
import com.numplates.nomera3.modules.chatrooms.data.repository.RoomDataRepository
import com.numplates.nomera3.modules.chatrooms.data.repository.RoomDataRepositoryImpl
import com.numplates.nomera3.modules.comments.data.repository.CommentsRepositoryImpl
import com.numplates.nomera3.modules.comments.data.repository.PostCommentsRepository
import com.numplates.nomera3.modules.communities.data.repository.CommunityInformationRepository
import com.numplates.nomera3.modules.communities.data.repository.CommunityInformationRepositoryImpl
import com.numplates.nomera3.modules.communities.data.repository.CommunityRepository
import com.numplates.nomera3.modules.communities.data.repository.CommunityRepositoryImpl
import com.numplates.nomera3.modules.complains.data.repository.ComplaintRepositoryImpl
import com.numplates.nomera3.modules.complains.domain.repository.ComplaintRepository
import com.numplates.nomera3.modules.contentsharing.data.ContentSharingRepositoryImpl
import com.numplates.nomera3.modules.contentsharing.domain.repository.ContentSharingRepository
import com.numplates.nomera3.modules.devtools_bridge.data.DevToolsBridgeRepository
import com.numplates.nomera3.modules.devtools_bridge.data.DevToolsBridgeRepositoryImpl
import com.numplates.nomera3.modules.feed.data.repository.PostRepository
import com.numplates.nomera3.modules.feed.data.repository.PostRepositoryImpl
import com.numplates.nomera3.modules.feed.data.repository.RoadReferralRepository
import com.numplates.nomera3.modules.feed.data.repository.RoadReferralRepositoryImpl
import com.numplates.nomera3.modules.feed.data.repository.RoadSuggestsRepository
import com.numplates.nomera3.modules.feed.data.repository.RoadSuggestsRepositoryImpl
import com.numplates.nomera3.modules.fileuploads.data.repository.FileUploadRepositoryImpl
import com.numplates.nomera3.modules.fileuploads.domain.FileUploadRepository
import com.numplates.nomera3.modules.gifservice.data.repository.GiphyRepository
import com.numplates.nomera3.modules.gifservice.data.repository.GiphyRepositoryImpl
import com.numplates.nomera3.modules.gift_coffee.data.repository.GiftCoffeeRepository
import com.numplates.nomera3.modules.gift_coffee.data.repository.GiftCoffeeRepositoryImpl
import com.numplates.nomera3.modules.holidays.data.repository.DailyVisitsRepository
import com.numplates.nomera3.modules.holidays.data.repository.DailyVisitsRepositoryImpl
import com.numplates.nomera3.modules.holidays.data.repository.HolidayInfoRepository
import com.numplates.nomera3.modules.holidays.data.repository.HolidayInfoRepositoryImpl
import com.numplates.nomera3.modules.maps.data.repository.LocationRepositoryImpl
import com.numplates.nomera3.modules.maps.data.repository.MapAnalyticsRepositoryImpl
import com.numplates.nomera3.modules.maps.data.repository.MapDataRepositoryImpl
import com.numplates.nomera3.modules.maps.data.repository.MapEventsListsRepositoryImpl
import com.numplates.nomera3.modules.maps.data.repository.MapEventsRepositoryImpl
import com.numplates.nomera3.modules.maps.data.repository.MapFriendsRepositoryImpl
import com.numplates.nomera3.modules.maps.data.repository.MapSettingsRepositoryImpl
import com.numplates.nomera3.modules.maps.data.repository.MapTooltipsRepositoryImpl
import com.numplates.nomera3.modules.maps.data.repository.MapWidgetRepositoryImpl
import com.numplates.nomera3.modules.maps.domain.analytics.MapAnalyticsRepository
import com.numplates.nomera3.modules.maps.domain.repository.LocationRepository
import com.numplates.nomera3.modules.maps.domain.repository.MapDataRepository
import com.numplates.nomera3.modules.maps.domain.repository.MapEventsListsRepository
import com.numplates.nomera3.modules.maps.domain.repository.MapEventsRepository
import com.numplates.nomera3.modules.maps.domain.repository.MapFriendsRepository
import com.numplates.nomera3.modules.maps.domain.repository.MapSettingsRepository
import com.numplates.nomera3.modules.maps.domain.repository.MapTooltipsRepository
import com.numplates.nomera3.modules.maps.domain.repository.MapWidgetRepository
import com.numplates.nomera3.modules.moments.show.data.MomentsRepository
import com.numplates.nomera3.modules.moments.show.data.MomentsRepositoryImpl
import com.numplates.nomera3.modules.music.data.repository.MusicRepositoryImpl
import com.numplates.nomera3.modules.music.domain.MusicRepository
import com.numplates.nomera3.modules.notifications.data.repository.NotificationRepository
import com.numplates.nomera3.modules.notifications.data.repository.NotificationRepositoryImpl
import com.numplates.nomera3.modules.onboarding.data.OnboardingRepository
import com.numplates.nomera3.modules.onboarding.data.OnboardingRepositoryImpl
import com.numplates.nomera3.modules.peoples.data.repository.PeopleRepositoryImpl
import com.numplates.nomera3.modules.peoples.domain.repository.PeopleRepository
import com.numplates.nomera3.modules.places.data.PlacesRepositoryImpl
import com.numplates.nomera3.modules.places.domain.PlacesRepository
import com.numplates.nomera3.modules.post_view_statistic.data.PostViewStatisticRepository
import com.numplates.nomera3.modules.post_view_statistic.data.PostViewStatisticRepositoryImpl
import com.numplates.nomera3.modules.privacysettings.data.blacklist.BlacklistRepository
import com.numplates.nomera3.modules.privacysettings.data.blacklist.BlacklistRepositoryImpl
import com.numplates.nomera3.modules.purchase.data.repository.PurchaseRepositoryImpl
import com.numplates.nomera3.modules.purchase.domain.repository.PurchaseRepository
import com.numplates.nomera3.modules.rateus.data.RateUsAnalyticRepository
import com.numplates.nomera3.modules.rateus.data.RateUsAnalyticRepositoryImpl
import com.numplates.nomera3.modules.rateus.data.RateUsRepository
import com.numplates.nomera3.modules.rateus.data.RateUsRepositoryImpl
import com.numplates.nomera3.modules.reaction.domain.repository.ReactionRepository
import com.numplates.nomera3.modules.reaction.domain.repository.ReactionRepositoryImpl
import com.numplates.nomera3.modules.reactionStatistics.data.repository.ReactionsRepository
import com.numplates.nomera3.modules.reactionStatistics.data.repository.ReactionsRepositoryImpl
import com.numplates.nomera3.modules.registration.data.repository.RegistrationCountriesRepository
import com.numplates.nomera3.modules.registration.data.repository.RegistrationCountriesRepositoryImpl
import com.numplates.nomera3.modules.screenshot.data.ShareScreenshotRepository
import com.numplates.nomera3.modules.screenshot.data.ShareScreenshotRepositoryImpl
import com.numplates.nomera3.modules.search.data.repository.SearchRepository
import com.numplates.nomera3.modules.search.data.repository.SearchRepositoryImpl
import com.numplates.nomera3.modules.share.data.repository.RoomsRepository
import com.numplates.nomera3.modules.share.data.repository.RoomsRepositoryImpl
import com.numplates.nomera3.modules.share.data.repository.ShareRepository
import com.numplates.nomera3.modules.share.data.repository.ShareRepositoryImpl
import com.numplates.nomera3.modules.tags.data.repository.TagsRepository
import com.numplates.nomera3.modules.tags.data.repository.TagsRepositoryImpl
import com.numplates.nomera3.modules.upload.domain.repository.UploadRepository
import com.numplates.nomera3.modules.upload.domain.repository.UploadRepositoryImpl
import com.numplates.nomera3.modules.user.data.repository.UserComplainRepository
import com.numplates.nomera3.modules.user.data.repository.UserComplainRepositoryImpl
import com.numplates.nomera3.modules.user.data.repository.UserRepository
import com.numplates.nomera3.modules.user.data.repository.UserRepositoryImpl
import com.numplates.nomera3.modules.userprofile.data.repository.GetProfileRepository
import com.numplates.nomera3.modules.userprofile.data.repository.GetProfileRepositoryImpl
import com.numplates.nomera3.modules.userprofile.data.repository.ProfileRepository
import com.numplates.nomera3.modules.userprofile.data.repository.ProfileRepositoryImpl
import com.numplates.nomera3.modules.userprofile.data.repository.ProfileTooltipRepository
import com.numplates.nomera3.modules.userprofile.data.repository.ProfileTooltipRepositoryImpl
import com.numplates.nomera3.modules.userprofile.profilestatistics.data.repository.ProfileStatisticsRepository
import com.numplates.nomera3.modules.userprofile.profilestatistics.data.repository.ProfileStatisticsRepositoryImpl
import com.numplates.nomera3.modules.usersettings.data.repository.PrivacyUserSettingsRepositoryImpl
import com.numplates.nomera3.modules.usersettings.data.repository.UserPersonalInfoRepositoryImpl
import com.numplates.nomera3.modules.usersettings.domain.repository.PrivacyUserSettingsRepository
import com.numplates.nomera3.modules.usersettings.domain.repository.UserPersonalInfoRepository
import com.numplates.nomera3.modules.vehicle.VehicleRepository
import com.numplates.nomera3.modules.vehicle.VehicleRepositoryImpl
import com.numplates.nomera3.modules.volume.data.VolumeStateRepository
import com.numplates.nomera3.modules.volume.data.VolumeStateRepositoryImpl
import com.numplates.nomera3.presentation.view.fragments.meerasettings.data.MeeraSettingsRepositoryImpl
import com.numplates.nomera3.presentation.view.fragments.meerasettings.domain.MeeraSettingsRepository
import dagger.Binds
import dagger.Module

@Module
interface RepositoryModule {

    @Binds
    fun provideApplicationContext(repository: NotificationRepositoryImpl): NotificationRepository

    @Binds
    fun provideCommentsRepository(repository: CommentsRepositoryImpl): PostCommentsRepository

    @Binds
    fun provideTagsRepository(repository: TagsRepositoryImpl): TagsRepository

    @Binds
    fun provideUserProfileRepository(repository: ProfileRepositoryImpl): ProfileRepository

    @Binds
    fun provideGiftCoffeeRepository(repository: GiftCoffeeRepositoryImpl): GiftCoffeeRepository

    @Binds
    fun provideGroupRepository(repository: CommunityRepositoryImpl): CommunityRepository

    @Binds
    fun providesVehicleRepository(repository: VehicleRepositoryImpl): VehicleRepository

    @Binds
    fun provideCommunityInformRepo(repository: CommunityInformationRepositoryImpl): CommunityInformationRepository

    @Binds
    fun provideSearchRepository(repository: SearchRepositoryImpl): SearchRepository

    @Binds
    fun provideUserRepository(repository: UserRepositoryImpl): UserRepository

    @Binds
    fun provideProfileTooltipRepository(repository: ProfileTooltipRepositoryImpl): ProfileTooltipRepository

    @Binds
    fun provideComplainRepository(repository: ComplaintRepositoryImpl): ComplaintRepository

    @Binds
    fun provideUserComplainRepository(repository: UserComplainRepositoryImpl): UserComplainRepository

    @Binds
    fun provideAuthRepository(repository: AuthRepositoryImpl): AuthRepository

    @Binds
    fun provideShareRepository(repository: ShareRepositoryImpl): ShareRepository

    @Binds
    fun provideReactionRepository(repository: ReactionRepositoryImpl): ReactionRepository

    @Binds
    fun provideReactionsRepository(repository: ReactionsRepositoryImpl): ReactionsRepository

    @Binds
    fun provideMusicRepository(repository: MusicRepositoryImpl): MusicRepository

    @Binds
    fun provideHolidayInfoRepository(repository: HolidayInfoRepositoryImpl): HolidayInfoRepository

    @Binds
    fun provideHolidayDailyVisitsRepository(repository: DailyVisitsRepositoryImpl): DailyVisitsRepository

    @Binds
    fun provideUpdateAppRepository(repository: AppInfoRepositoryImpl): AppInfoRepository

    @Binds
    fun provideAppAsyncRepository(repository: AppInfoAsyncRepositoryImpl): AppInfoAsyncRepository

    @Binds
    fun provideGetRepository(repository: GetProfileRepositoryImpl): GetProfileRepository

    @Binds
    fun provideGiphyRepository(repository: GiphyRepositoryImpl): GiphyRepository

    @Binds
    fun provideUploadQueueRepository(repository: UploadRepositoryImpl): UploadRepository

    @Binds
    fun provideChatToolbarRepository(repository: ChatToolbarRepositoryImpl): ChatToolbarRepository

    @Binds
    fun providePostViewStatisticRepository(repository: PostViewStatisticRepositoryImpl): PostViewStatisticRepository

    @Binds
    fun provideDevToolStateRepository(repository: DevToolsBridgeRepositoryImpl): DevToolsBridgeRepository

    @Binds
    fun provideChatMessagesRepository(repository: MessagesRepositoryImpl): MessagesRepository

    @Binds
    fun provideSettingsRepository(repository: MeeraSettingsRepositoryImpl): MeeraSettingsRepository

    @Binds
    fun provideRoomsRepository(repository: RoomsRepositoryImpl): RoomsRepository

    @Binds
    fun providePostRepository(repository: PostRepositoryImpl): PostRepository

    @Binds
    fun provideMomentsRepository(repository: MomentsRepositoryImpl): MomentsRepository

    @Binds
    fun provideRoadReferralRepository(repository: RoadReferralRepositoryImpl): RoadReferralRepository

    @Binds
    fun provideRoadSuggestsRepository(repository: RoadSuggestsRepositoryImpl): RoadSuggestsRepository

    @Binds
    fun provideMapVisibilitySettingsAnalyticsRepo(repository: MapVisibilitySettingsAnalyticsRepositoryImpl):
        MapVisibilitySettingsAnalyticsRepository

    @Binds
    fun provideMapSettingsRepository(repository: MapSettingsRepositoryImpl): MapSettingsRepository

    @Binds
    fun provideMapDataRepository(repository: MapDataRepositoryImpl): MapDataRepository

    @Binds
    fun provideMapEventsListsRepository(repository: MapEventsListsRepositoryImpl): MapEventsListsRepository

    @Binds
    fun provideMapTooltipsRepository(repository: MapTooltipsRepositoryImpl): MapTooltipsRepository

    @Binds
    fun provideDialogPreparationRepository(repository: DialogPreparationRepositoryImpl): DialogPreparationRepository

    @Binds
    fun provideOnboardingRepository(repositoryImpl: OnboardingRepositoryImpl): OnboardingRepository

    @Binds
    fun provideUserPersonalInfoRepository(repository: UserPersonalInfoRepositoryImpl): UserPersonalInfoRepository

    @Binds
    fun provideChatRequestRepository(repository: ChatRequestRepositoryImpl): ChatRequestRepository

    @Binds
    fun provideProfileStatisticsRepository(repository: ProfileStatisticsRepositoryImpl): ProfileStatisticsRepository

    @Binds
    fun providePrivacyUsersSettingsRepo(repository: PrivacyUserSettingsRepositoryImpl): PrivacyUserSettingsRepository

    @Binds
    fun provideFriendlistRepository(repository: FriendlistRepositoryImpl): GetFriendlistUsecase.FriendlistRepository

    @Binds
    fun provideRoomDataRepository(repository: RoomDataRepositoryImpl): RoomDataRepository

    @Binds
    fun provideChatMessageRepository(repository: ChatMessageRepositoryImpl): ChatMessageRepository

    @Binds
    fun provideChatPersistDbRepository(repository: ChatPersistDbRepositoryImpl): ChatPersistDbRepository

    @Binds
    fun provideChatRepository(repository: ChatRepositoryImlp): ChatRepository

    @Binds
    fun provideChatPersistRepository(repository: ChatPersistRepositoryImpl): ChatPersistRepository

    @Binds
    fun provideRegistrationCountriesRepo(repository: RegistrationCountriesRepositoryImpl):
        RegistrationCountriesRepository

    @Binds
    fun provideRemoveRoomRepository(repository: RemoveRoomRepositoryImpl): RemoveRoomRepository

    @Binds
    fun provideBlacklistRepository(repository: BlacklistRepositoryImpl): BlacklistRepository

    @Binds
    fun provideUserSnippetAnalyticsRepo(repository: MapSnippetAnalyticsRepositoryImpl): MapSnippetAnalyticsRepository

    @Binds
    fun providePeoplesRepository(repository: PeopleRepositoryImpl): PeopleRepository

    @Binds
    fun providePurchaseRepository(repository: PurchaseRepositoryImpl): PurchaseRepository

    @Binds
    fun provideSendMessageDataRepository(repository: SendMessageDataRepositoryImpl): SendMessageDataRepository

    @Binds
    fun provideMediakeyboardFavoritesRepository(repository: MediakeyboardRepositoryImpl): MediakeyboardRepository

    @Binds
    fun provideDraftsRepository(repository: DraftsRepositoryImpl): DraftsRepository

    @AppScope
    @Binds
    fun provideCallManager(repository: CallManagerImpl): CallManager

    @Binds
    fun provideLocationRepository(repository: LocationRepositoryImpl): LocationRepository

    @Binds
    fun providePlacesRepository(repository: PlacesRepositoryImpl): PlacesRepository

    @Binds
    fun provideRateUsRepository(repository: RateUsRepositoryImpl): RateUsRepository

    @Binds
    fun provideRateUsAnalyticRepository(repository: RateUsAnalyticRepositoryImpl): RateUsAnalyticRepository

    @Binds
    fun bindShakeRepository(repository: ShakeRepositoryImpl): ShakeRepository

    @Binds
    fun bindShareScreenshotRepository(repository: ShareScreenshotRepositoryImpl): ShareScreenshotRepository

    @Binds
    fun bindGroupChatRepository(repository: GroupChatRepositoryImpl): GroupChatRepository

    @Binds
    fun bindSyncContactsRepository(repository: SyncContactsRepositoryImpl): SyncContactsRepository

    @Binds
    fun bindFileUploadRepository(repository: FileUploadRepositoryImpl): FileUploadRepository

    @Binds
    fun provideMapEventsRepository(repository: MapEventsRepositoryImpl): MapEventsRepository

    @Binds
    fun provideMapFriendsRepository(repository: MapFriendsRepositoryImpl): MapFriendsRepository

    @Binds
    fun provideMapWidgetRepository(repository: MapWidgetRepositoryImpl): MapWidgetRepository

    @Binds
    fun provideMapAnalyticsRepository(repository: MapAnalyticsRepositoryImpl): MapAnalyticsRepository

    @Binds
    fun bindContentSharingRepository(repository: ContentSharingRepositoryImpl): ContentSharingRepository

    @Binds
    fun bindChatMessagesDemoRepository(repository: ChatDemoMessagesRepositoryImpl): ChatMessagesDemoRepository

    @Binds
    fun bindVolumeStateRepository(repository: VolumeStateRepositoryImpl): VolumeStateRepository
}
