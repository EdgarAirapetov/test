package com.numplates.nomera3.di

import com.meera.core.di.modules.DOWNLOAD_RETROFIT
import com.meera.core.di.modules.FILE_STORAGE_API_RETROFIT
import com.meera.core.di.modules.GIPHY_API
import com.meera.core.di.modules.HIWAY_API_RETROFIT
import com.meera.core.di.modules.NULLABLE_API_RETROFIT
import com.meera.core.di.modules.UPLOAD_RETROFIT
import com.meera.core.di.modules.UPLOAD_RETROFIT_STORAGE
import com.numplates.nomera3.data.network.ApiFileStorage
import com.numplates.nomera3.data.network.ApiHiWayKt
import com.numplates.nomera3.data.network.ApiMain
import com.numplates.nomera3.data.network.SyncContactsApi
import com.numplates.nomera3.modules.appInfo.data.api.AppInfoApi
import com.numplates.nomera3.modules.auth.data.api.AuthApi
import com.numplates.nomera3.modules.baseCore.data.api.DownloadApi
import com.numplates.nomera3.modules.baseCore.data.api.UploadRoadApi
import com.numplates.nomera3.modules.bump.data.api.ShakeApi
import com.numplates.nomera3.modules.chat.messages.data.api.MessagesApi
import com.numplates.nomera3.modules.chat.requests.data.api.ChatRequestApi
import com.numplates.nomera3.modules.chat.toolbar.data.api.ChatUserApi
import com.numplates.nomera3.modules.chatrooms.data.api.RoomsApi
import com.numplates.nomera3.modules.comments.data.api.CommentsApi
import com.numplates.nomera3.modules.communities.data.api.CommunitiesApi
import com.numplates.nomera3.modules.complains.data.api.ComplaintApi
import com.numplates.nomera3.modules.feed.data.api.EditPostApi
import com.numplates.nomera3.modules.feed.data.api.RoadApi
import com.numplates.nomera3.modules.fileuploads.data.api.ApiUpload
import com.numplates.nomera3.modules.fileuploads.data.api.ApiUploadStorage
import com.numplates.nomera3.modules.gifservice.data.api.GiphyApi
import com.numplates.nomera3.modules.gift_coffee.data.api.GiftCoffeeApi
import com.numplates.nomera3.modules.moments.core.MomentsApi
import com.numplates.nomera3.modules.music.data.api.MusicApi
import com.numplates.nomera3.modules.notifications.data.api.NotificationApi
import com.numplates.nomera3.modules.peoples.data.api.PeopleApi
import com.numplates.nomera3.modules.purchase.data.api.PurchaseApi
import com.numplates.nomera3.modules.reaction.data.net.ReactionApi
import com.numplates.nomera3.modules.reactionStatistics.data.api.ReactionsApi
import com.numplates.nomera3.modules.search.data.api.SearchApi
import com.numplates.nomera3.modules.share.data.api.ShareApi
import com.numplates.nomera3.modules.tags.data.api.TagApi
import com.numplates.nomera3.modules.user.data.api.UserApi
import com.numplates.nomera3.modules.user.data.api.UserComplainApi
import com.numplates.nomera3.modules.userprofile.data.api.UserProfileApi
import com.numplates.nomera3.presentation.view.fragments.meerasettings.data.SettingsApi
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.create
import javax.inject.Named

@Module
class ApiModule {
    @Provides
    fun momentsApi(retrofit: Retrofit): MomentsApi =
        retrofit.create(MomentsApi::class.java)

    @Provides
    fun notificationApi(retrofit: Retrofit): NotificationApi =
        retrofit.create(NotificationApi::class.java)

    @Provides
    fun commentsApi(retrofit: Retrofit): CommentsApi =
        retrofit.create(CommentsApi::class.java)

    @Provides
    fun tagsApi(retrofit: Retrofit): TagApi =
        retrofit.create(TagApi::class.java)

    @Provides
    fun userProfileApi(retrofit: Retrofit): UserProfileApi =
        retrofit.create(UserProfileApi::class.java)

    @Provides
    fun giftCoffeeApi(retrofit: Retrofit): GiftCoffeeApi =
        retrofit.create(GiftCoffeeApi::class.java)

    @Provides
    fun searchApi(retrofit: Retrofit): SearchApi =
        retrofit.create(SearchApi::class.java)

    @Provides
    fun userApi(retrofit: Retrofit): UserApi =
        retrofit.create(UserApi::class.java)

    @Provides
    fun reactionsApi(retrofit: Retrofit): ReactionsApi =
        retrofit.create(ReactionsApi::class.java)

    @Provides
    fun complainApi(retrofit: Retrofit): ComplaintApi =
        retrofit.create(ComplaintApi::class.java)

    @Provides
    fun userComplainApi(retrofit: Retrofit): UserComplainApi =
        retrofit.create(UserComplainApi::class.java)

    @Provides
    fun groupApi(retrofit: Retrofit): CommunitiesApi =
        retrofit.create(CommunitiesApi::class.java)

    @Provides
    fun authApi(retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    @Provides
    fun shareApi(retrofit: Retrofit): ShareApi =
        retrofit.create(ShareApi::class.java)

    @Provides
    fun baseApi(@Named(UPLOAD_RETROFIT) retrofit: Retrofit): UploadRoadApi =
        retrofit.create(UploadRoadApi::class.java)

    @Provides
    fun downloadApi(@Named(DOWNLOAD_RETROFIT) retrofit: Retrofit): DownloadApi =
        retrofit.create(DownloadApi::class.java)

    @Provides
    fun musicApi(retrofit: Retrofit): MusicApi =
        retrofit.create(MusicApi::class.java)

    @Provides
    fun reactionApi(retrofit: Retrofit): ReactionApi =
        retrofit.create(ReactionApi::class.java)

    @Provides
    fun giphyApi(@Named(GIPHY_API) retrofit: Retrofit): GiphyApi =
        retrofit.create(GiphyApi::class.java)

    @Provides
    fun mainApi(retrofit: Retrofit): ApiMain = retrofit.create(ApiMain::class.java)

    @Provides
    fun uploadApi(@Named(UPLOAD_RETROFIT) retrofit: Retrofit): ApiUpload =
        retrofit.create(ApiUpload::class.java)

    @Provides
    fun uploadApiStorage(@Named(UPLOAD_RETROFIT_STORAGE) retrofit: Retrofit): ApiUploadStorage =
        retrofit.create(ApiUploadStorage::class.java)

    @Provides
    fun appInfoApi(retrofit: Retrofit): AppInfoApi =
        retrofit.create(AppInfoApi::class.java)

    @Provides
    fun messagesApi(retrofit: Retrofit): MessagesApi =
        retrofit.create(MessagesApi::class.java)

    @Provides
    fun roadApi(retrofit: Retrofit): RoadApi =
        retrofit.create(RoadApi::class.java)

    @Provides
    fun settingsApi(retrofit: Retrofit): SettingsApi =
        retrofit.create(SettingsApi::class.java)

    @Provides
    fun editPostApi(@Named(NULLABLE_API_RETROFIT) retrofit: Retrofit): EditPostApi =
        retrofit.create(EditPostApi::class.java)

    @Provides
    fun chatRequestsApi(retrofit: Retrofit): ChatRequestApi =
        retrofit.create(ChatRequestApi::class.java)

    @Provides
    fun purchaseApi(retrofit: Retrofit): PurchaseApi =
        retrofit.create(PurchaseApi::class.java)

    @Provides
    fun roomsApi(retrofit: Retrofit): RoomsApi =
        retrofit.create(RoomsApi::class.java)

    @Provides
    fun chatUserApi(retrofit: Retrofit): ChatUserApi = retrofit.create(ChatUserApi::class.java)

    @Provides
    fun apiHiWayKt(@Named(HIWAY_API_RETROFIT) retrofit: Retrofit): ApiHiWayKt =
        retrofit.create(ApiHiWayKt::class.java)

    @Provides
    fun providePeoplesApi(retrofit: Retrofit): PeopleApi =
        retrofit.create(PeopleApi::class.java)

    @Provides
    fun provideShakeApi(retrofit: Retrofit): ShakeApi =
        retrofit.create(ShakeApi::class.java)

    @Provides
    fun provideFileStorageApi(@Named(FILE_STORAGE_API_RETROFIT) retrofit: Retrofit): ApiFileStorage =
        retrofit.create(ApiFileStorage::class.java)

    @Provides
    fun provideSyncContactsApi(retrofit: Retrofit): SyncContactsApi {
        return retrofit.create(SyncContactsApi::class.java)
    }
}
