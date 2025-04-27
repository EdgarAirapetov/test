package com.numplates.nomera3.di

import android.content.ClipboardManager
import com.google.gson.Gson
import com.meera.core.di.modules.FILE_STORAGE_API_RETROFIT
import com.meera.core.di.modules.HIWAY_API_RETROFIT
import com.meera.core.di.modules.OLD_API_RETROFIT
import com.meera.core.di.scopes.AppScope
import com.meera.core.network.websocket.WebSocketMainChannel
import com.meera.core.utils.files.FileManager
import com.numplates.nomera3.data.network.Api
import com.numplates.nomera3.data.network.ApiFileStorage
import com.numplates.nomera3.data.network.ApiHiWay
import com.numplates.nomera3.data.network.ApiHiWayKt
import com.numplates.nomera3.data.network.ApiMain
import com.numplates.nomera3.domain.interactornew.AddPostCommentUseCase
import com.numplates.nomera3.domain.interactornew.AddPostComplaintUseCase
import com.numplates.nomera3.domain.interactornew.AuthAuthenticateUseCase
import com.numplates.nomera3.domain.interactornew.AuthOldTokenUseCase
import com.numplates.nomera3.domain.interactornew.AuthRefreshTokenUseCase
import com.numplates.nomera3.domain.interactornew.BlockUserUseCase
import com.numplates.nomera3.domain.interactornew.CallUserSettingsUseCase
import com.numplates.nomera3.domain.interactornew.CitySuggestionUseCase
import com.numplates.nomera3.domain.interactornew.CreateGroupUseCase
import com.numplates.nomera3.domain.interactornew.DeleteGiftUseCase
import com.numplates.nomera3.domain.interactornew.DeletePhotoUseCase
import com.numplates.nomera3.domain.interactornew.DeletePostUseCase
import com.numplates.nomera3.domain.interactornew.DeleteRestoreProfileUseCase
import com.numplates.nomera3.domain.interactornew.DeliveredUseCase
import com.numplates.nomera3.domain.interactornew.DownloadFileUseCase
import com.numplates.nomera3.domain.interactornew.EditGroupUseCase
import com.numplates.nomera3.domain.interactornew.FeatureActionUseCase
import com.numplates.nomera3.domain.interactornew.GenerateRandomAvatarUseCase
import com.numplates.nomera3.domain.interactornew.GetAllEventsUseCase
import com.numplates.nomera3.domain.interactornew.GetCountriesUseCase
import com.numplates.nomera3.domain.interactornew.GetFriendsListUseCase
import com.numplates.nomera3.domain.interactornew.GetFriendsUseCase
import com.numplates.nomera3.domain.interactornew.GetInfoApplicationUseCase
import com.numplates.nomera3.domain.interactornew.GetPostUseCase
import com.numplates.nomera3.domain.interactornew.GetProfileUseCase
import com.numplates.nomera3.domain.interactornew.GetReferralsUseCase
import com.numplates.nomera3.domain.interactornew.HideUserPostsUseCase
import com.numplates.nomera3.domain.interactornew.MarkPostAsNotSensitiveForUserUseCase
import com.numplates.nomera3.domain.interactornew.MarkRoomAsReadUseCase
import com.numplates.nomera3.domain.interactornew.NotificationCounterUseCase
import com.numplates.nomera3.domain.interactornew.RemoveGroupUseCase
import com.numplates.nomera3.domain.interactornew.RemoveUserUseCase
import com.numplates.nomera3.domain.interactornew.RepostUseCase
import com.numplates.nomera3.domain.interactornew.SearchUserUseCase
import com.numplates.nomera3.domain.interactornew.SendNewMessageUseCase
import com.numplates.nomera3.domain.interactornew.SendVoiceMessageUseCase
import com.numplates.nomera3.domain.interactornew.SetPushSettingsUseCase
import com.numplates.nomera3.domain.interactornew.SharePostUseCase
import com.numplates.nomera3.domain.interactornew.SubscribePostUseCase
import com.numplates.nomera3.domain.interactornew.SubscribersUseCase
import com.numplates.nomera3.domain.interactornew.SubscriptionUseCase
import com.numplates.nomera3.domain.interactornew.UnsubscribePostUseCase
import com.numplates.nomera3.domain.interactornew.UploadAlbumImageUseCase
import com.numplates.nomera3.domain.interactornew.VehicleListUseCase
import com.numplates.nomera3.domain.interactornew.VehicleTypesUseCase
import com.numplates.nomera3.modules.auth.domain.AuthUserStateObserverUseCase
import com.numplates.nomera3.modules.chat.drafts.domain.DraftsRepository
import com.numplates.nomera3.modules.chat.toolbar.domain.usecase.UserChatPreferencesListener
import com.numplates.nomera3.modules.communities.data.repository.CommunityInformationRepository
import com.numplates.nomera3.modules.communities.data.repository.CommunityRepository
import com.numplates.nomera3.modules.communities.domain.usecase.GetCommunitiesTopUseCase
import com.numplates.nomera3.modules.communities.domain.usecase.GetCommunitiesUseCase
import com.numplates.nomera3.modules.communities.domain.usecase.GetCommunityInformationUseCase
import com.numplates.nomera3.modules.communities.domain.usecase.SearchGroupsUseCase
import com.numplates.nomera3.modules.communities.domain.usecase.notifications.SubscribeCommunityNotificationsUseCase
import com.numplates.nomera3.modules.communities.domain.usecase.notifications.UnsubscribeCommunityNotificationsUseCase
import com.numplates.nomera3.modules.devtools_bridge.data.DevToolsBridgeRepository
import com.numplates.nomera3.modules.devtools_bridge.domain.GetPostViewCollisionHighlightEnableUseCase
import com.numplates.nomera3.modules.holidays.data.repository.HolidayInfoRepository
import com.numplates.nomera3.modules.moments.show.data.MomentsRepository
import com.numplates.nomera3.modules.holidays.domain.usecase.GetHolidayInfoUseCase
import com.numplates.nomera3.modules.moments.show.domain.GetLastLoadedMomentUserIdUseCase
import com.numplates.nomera3.modules.moments.show.domain.GetMomentsPagingParamsBySourceUseCase
import com.numplates.nomera3.modules.moments.show.domain.SetMomentsPagingParamsBySourceUseCase
import com.numplates.nomera3.modules.newroads.data.PostsRepository
import com.numplates.nomera3.modules.newroads.data.entities.GetSubscriptionMomentListenerUseCase
import com.numplates.nomera3.modules.newroads.data.entities.GetSubscriptionPostListenerUseCase
import com.numplates.nomera3.modules.share.domain.usecase.CopyPostLinkUseCase
import com.numplates.nomera3.modules.share.domain.usecase.GetPostLinkUseCase
import com.numplates.nomera3.modules.user.data.repository.UserRepository
import com.numplates.nomera3.modules.user.domain.usecase.UserPreferencesUseCase
import com.numplates.nomera3.modules.userprofile.data.repository.GetProfileRepository
import com.numplates.nomera3.presentation.view.fragments.notificationsettings.message.MessageNotificationsExclusionsUseCase
import com.numplates.nomera3.presentation.view.fragments.notificationsettings.subscription.SubscriptionNotificationsUseCase
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Named


@Module
class UseCasesModule {

    @Provides
    fun provideGetPostUseCase(retrofit: Retrofit): GetPostUseCase {
        return GetPostUseCase(retrofit.create(ApiMain::class.java))
    }

    @Provides
    fun provideAddPostCommentUseCase(@Named(OLD_API_RETROFIT) retrofit: Retrofit): AddPostCommentUseCase {
        return AddPostCommentUseCase(retrofit.create(Api::class.java))
    }

    @Provides
    fun provideAddPostComplaintUseCase(retrofit: Retrofit): AddPostComplaintUseCase {
        return AddPostComplaintUseCase(retrofit.create(ApiMain::class.java))
    }


    @Provides
    fun provideDeletePostUseCase(retrofit: Retrofit): DeletePostUseCase {
        return DeletePostUseCase(retrofit.create(ApiMain::class.java))
    }

    @Provides
    fun provideHideUserPostsUseCase(@Named(HIWAY_API_RETROFIT) retrofit: Retrofit): HideUserPostsUseCase {
        return HideUserPostsUseCase(
            apiHiWay = retrofit.create(ApiHiWay::class.java),
            apiMain = retrofit.create(ApiMain::class.java)
        )
    }

    @Provides
    fun provideSubscribePostUseCase(retrofit: Retrofit): SubscribePostUseCase {
        return SubscribePostUseCase(retrofit.create(ApiMain::class.java))
    }

    @Provides
    fun provideUnsubscribePostUseCase(retrofit: Retrofit): UnsubscribePostUseCase {
        return UnsubscribePostUseCase(retrofit.create(ApiMain::class.java))
    }

    @Provides
    fun provideDownloadFileUseCase(
        @Named(FILE_STORAGE_API_RETROFIT) retrofit: Retrofit,
        fileManager: FileManager
    ): DownloadFileUseCase {
        return DownloadFileUseCase(retrofit.create(ApiFileStorage::class.java), fileManager)
    }

    @Provides
    fun provideGetFriendsListUseCase(@Named(HIWAY_API_RETROFIT) retrofit: Retrofit): GetFriendsListUseCase {
        return GetFriendsListUseCase(retrofit.create(ApiHiWay::class.java))
    }


    @Provides
    fun provideSendVoiceMessageUseCase(retrofit: Retrofit): SendVoiceMessageUseCase {
        return SendVoiceMessageUseCase(retrofit.create(ApiMain::class.java))
    }

    @Provides
    fun provideMarkRoomAsReadUseCase(retrofit: Retrofit) =
        MarkRoomAsReadUseCase(retrofit.create(ApiMain::class.java))

    @Provides
    fun provideNewMessageUseCase(retrofit: Retrofit) =
        SendNewMessageUseCase(retrofit.create(ApiMain::class.java))

    @Provides
    fun provideUploadAlbumImageUseCase(@Named(HIWAY_API_RETROFIT) retrofit: Retrofit): UploadAlbumImageUseCase {
        return UploadAlbumImageUseCase(retrofit.create(ApiHiWay::class.java))
    }

    @Provides
    fun provideAuthAuthenticateUseCase(@Named(HIWAY_API_RETROFIT) retrofit: Retrofit): AuthAuthenticateUseCase {
        return AuthAuthenticateUseCase(retrofit.create(ApiHiWayKt::class.java))
    }

    @Provides
    fun provideAuthRefreshTokenUseCase(@Named(HIWAY_API_RETROFIT) retrofit: Retrofit): AuthRefreshTokenUseCase {
        return AuthRefreshTokenUseCase(retrofit.create(ApiHiWay::class.java))
    }

    @Provides
    fun provideAuthOldTokenUseCase(@Named(HIWAY_API_RETROFIT) retrofit: Retrofit): AuthOldTokenUseCase {
        return AuthOldTokenUseCase(retrofit.create(ApiHiWay::class.java))
    }

    @Provides
    fun provideGetCountriesUseCase(@Named(HIWAY_API_RETROFIT) retrofit: Retrofit): GetCountriesUseCase {
        return GetCountriesUseCase(retrofit.create(ApiHiWay::class.java))
    }

    @Provides
    fun provideCitySuggestionUseCase(@Named(OLD_API_RETROFIT) retrofit: Retrofit): CitySuggestionUseCase {
        return CitySuggestionUseCase(retrofit.create(Api::class.java))
    }

    @Provides
    fun provideBlockUserUseCase(@Named(HIWAY_API_RETROFIT) retrofit: Retrofit) =
        BlockUserUseCase(retrofit.create(ApiHiWay::class.java))

    @Provides
    fun provideSearchUserUseCase(@Named(HIWAY_API_RETROFIT) retrofit: Retrofit) =
        SearchUserUseCase(retrofit.create(ApiHiWay::class.java))

    @Provides
    fun provideSearchGroupsUseCase(repository: CommunityRepository) = SearchGroupsUseCase(repository)

    @Provides
    fun provideSubscribeCommunityNotificationsUseCase(repository: CommunityRepository) =
        SubscribeCommunityNotificationsUseCase(repository)

    @Provides
    fun provideUnsubscribeCommunityNotificationsUseCase(repository: CommunityRepository) =
        UnsubscribeCommunityNotificationsUseCase(repository)

    @Provides
    fun provideCommunityInformationUseCase(repository: CommunityInformationRepository) =
        GetCommunityInformationUseCase(repository)

    @Provides
    fun provideVehicleTypesUseCase(@Named(HIWAY_API_RETROFIT) retrofit: Retrofit) =
        VehicleTypesUseCase(retrofit.create(ApiHiWay::class.java))

    @Provides
    fun provideCreateGroupUseCase(@Named(OLD_API_RETROFIT) retrofit: Retrofit) =
        CreateGroupUseCase(retrofit.create(Api::class.java))

    @Provides
    fun provideEditGroupUseCase(@Named(OLD_API_RETROFIT) retrofit: Retrofit) =
        EditGroupUseCase(retrofit.create(Api::class.java))

    @Provides
    fun provideRemoveGroupUseCase(@Named(OLD_API_RETROFIT) retrofit: Retrofit) =
        RemoveGroupUseCase(retrofit.create(Api::class.java))

    @Provides
    fun provideGetInfoApplicationUseCase(@Named(HIWAY_API_RETROFIT) retrofit: Retrofit) =
        GetInfoApplicationUseCase(retrofit.create(ApiHiWay::class.java))

    @Provides
    fun provideGetGroupsTopUseCase(repository: CommunityRepository) = GetCommunitiesTopUseCase(repository)

    @Provides
    fun provideGetMyGroupsUseCase(repository: CommunityRepository) = GetCommunitiesUseCase(repository)

    @Provides
    fun provideRemoveUserUseCase(@Named(HIWAY_API_RETROFIT) retrofit: Retrofit) =
        RemoveUserUseCase(retrofit.create(ApiHiWay::class.java))

    @Provides
    fun provideDeletePhotoUseCase(@Named(HIWAY_API_RETROFIT) retrofit: Retrofit) =
        DeletePhotoUseCase(retrofit.create(ApiHiWay::class.java))

    @Provides
    fun provideGetAllEventsUseCase(@Named(HIWAY_API_RETROFIT) retrofit: Retrofit) =
        GetAllEventsUseCase(retrofit.create(ApiHiWay::class.java))

    @Provides
    fun provideVehicleListUseCase(@Named(HIWAY_API_RETROFIT) retrofit: Retrofit) =
        VehicleListUseCase(retrofit.create(ApiHiWay::class.java))

    @Provides
    fun provideSetPushSettingsUseCase(@Named(HIWAY_API_RETROFIT) retrofit: Retrofit) =
        SetPushSettingsUseCase(retrofit.create(ApiHiWay::class.java))


    @Provides
    fun provideCallUserSettingsUseCase(retrofit: Retrofit) =
        CallUserSettingsUseCase(retrofit.create(ApiMain::class.java))

    @Provides
    fun provideMessageNotificationsExclusionsUseCase(retrofit: Retrofit) =
        MessageNotificationsExclusionsUseCase(retrofit.create(ApiMain::class.java))

    @Provides
    fun provideNotificationCountUseCase(retrofit: Retrofit) =
        NotificationCounterUseCase(retrofit.create(ApiMain::class.java))

    @Provides
    fun provideSubscriptionNotificationsUseCase(retrofit: Retrofit) =
        SubscriptionNotificationsUseCase(retrofit.create(ApiMain::class.java))

    @Provides
    fun provideSharePostUseCase(retrofit: Retrofit) =
        SharePostUseCase(retrofit.create(ApiMain::class.java))

    @Provides
    fun provideRepostUseCase(retrofit: Retrofit) =
        RepostUseCase(retrofit.create(ApiMain::class.java))

    @Provides
    fun provideGetFriendsUseCase(retrofit: Retrofit) =
        GetFriendsUseCase(retrofit.create(ApiMain::class.java))

    @AppScope
    @Provides
    fun providePostsRepository(): PostsRepository {
        return PostsRepository()
    }

    @Provides
    fun provideGetSubscriptionNewPostUseCase(
        webSocket: WebSocketMainChannel,
        gson: Gson,
        authObserver: AuthUserStateObserverUseCase
    ): GetSubscriptionPostListenerUseCase {
        return GetSubscriptionPostListenerUseCase(webSocket, gson, authObserver)
    }

    @Provides
    fun provideGetSubscriptionMomentListenerUseCase(
        webSocket: WebSocketMainChannel,
        gson: Gson,
        authObserver: AuthUserStateObserverUseCase
    ): GetSubscriptionMomentListenerUseCase {
        return GetSubscriptionMomentListenerUseCase(webSocket, gson, authObserver)
    }

    @Provides
    fun provideSubscriptionUseCase(retrofit: Retrofit, momentsRepository: MomentsRepository) =
        SubscriptionUseCase(retrofit.create(ApiMain::class.java), momentsRepository)

    @Provides
    fun provideSubscribersUseCase(retrofit: Retrofit) =
        SubscribersUseCase(retrofit.create(ApiMain::class.java))

    @Provides
    fun provideDeleteRestoreProfileUseCase(retrofit: Retrofit, draftsRepository: DraftsRepository) =
        DeleteRestoreProfileUseCase(retrofit.create(ApiMain::class.java), draftsRepository)

    @Provides
    fun provideGetReferralsUseCase(retrofit: Retrofit) =
        GetReferralsUseCase(retrofit.create(ApiMain::class.java))

    @Provides
    fun provideDeliveredUseCase(retrofit: Retrofit) =
        DeliveredUseCase(retrofit.create(ApiMain::class.java))

    @Provides
    fun provideDeleteGiftUseCase(retrofit: Retrofit) =
        DeleteGiftUseCase(retrofit.create(ApiMain::class.java))

    @Provides
    fun provideFeatureActionUseCase(retrofit: Retrofit) =
        FeatureActionUseCase(retrofit.create(ApiMain::class.java))

    @Provides
    fun provideGetProfileUseCase(repository: GetProfileRepository) = GetProfileUseCase(repository)

    @Provides
    fun provideRandomAvatarUseCase(retrofit: Retrofit): GenerateRandomAvatarUseCase {
        return GenerateRandomAvatarUseCase(retrofit.create(ApiMain::class.java))
    }

    @Provides
    fun provideGetHolidayInfoUseCase(repository: HolidayInfoRepository): GetHolidayInfoUseCase {
        return GetHolidayInfoUseCase(repository)
    }

    @Provides
    fun provideGetPostViewCollisionHighlightEnableUseCase(repository: DevToolsBridgeRepository):
        GetPostViewCollisionHighlightEnableUseCase {
        return GetPostViewCollisionHighlightEnableUseCase(repository)
    }

    @Provides
    fun provideUserPreferencesUseCase(repository: UserRepository): UserPreferencesUseCase {
        return UserPreferencesUseCase(repository)
    }

    @Provides
    fun provideChatPreferencesListener(repository: UserRepository): UserChatPreferencesListener {
        return UserChatPreferencesListener(repository)
    }

    @Provides
    fun provideMarkPostAsNotSensitiveForUserUseCase(repository: PostsRepository) =
        MarkPostAsNotSensitiveForUserUseCase(repository)

    @Provides
    fun provideGetLastLoadedMomentUserIdUseCase(momentsRepository: MomentsRepository): GetLastLoadedMomentUserIdUseCase {
        return GetLastLoadedMomentUserIdUseCase(momentsRepository)
    }

    @Provides
    fun provideGetMomentsPagingParamsBySourceUseCase(momentsRepository: MomentsRepository): GetMomentsPagingParamsBySourceUseCase {
        return GetMomentsPagingParamsBySourceUseCase(momentsRepository)
    }

    @Provides
    fun provideSetMomentsPagingParamsBySourceUseCase(momentsRepository: MomentsRepository): SetMomentsPagingParamsBySourceUseCase {
        return SetMomentsPagingParamsBySourceUseCase(momentsRepository)
    }

    @Provides
    fun provideCopyPostLinkUseCase(getPostLinkUseCase: GetPostLinkUseCase, clipManager: ClipboardManager): CopyPostLinkUseCase {
        return CopyPostLinkUseCase(getPostLinkUseCase, clipManager)
    }
}
