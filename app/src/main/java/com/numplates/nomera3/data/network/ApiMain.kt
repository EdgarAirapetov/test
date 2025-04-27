package com.numplates.nomera3.data.network

import com.meera.db.models.userprofile.UserSimple
import com.meera.db.models.usersettings.PrivacySettingsResponseDto
import com.numplates.nomera3.data.dbmodel.UserWrapperWithCounter
import com.numplates.nomera3.data.dbmodel.UsersWrapper
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.data.entity.AddFavoriteBody
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.data.entity.MediakeyboardFavoriteRecentDto
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.data.entity.MediaKeyboardStickerPackDto
import com.numplates.nomera3.modules.communities.data.entity.Communities
import com.numplates.nomera3.modules.feed.data.entity.PostEntityResponse
import com.numplates.nomera3.modules.holidays.data.entity.GetHolidayResponse
import com.numplates.nomera3.modules.holidays.data.entity.HolidayVisitsEntity
import com.numplates.nomera3.modules.maps.data.model.ActiveEventCountDto
import com.numplates.nomera3.modules.maps.data.model.GetEventResultDto
import com.numplates.nomera3.modules.maps.data.model.JoinEventBodyDto
import com.numplates.nomera3.modules.maps.data.model.MapWidgetPointInfoDto
import com.numplates.nomera3.modules.maps.data.model.UserCardDto
import com.numplates.nomera3.modules.places.data.model.PlaceDto
import com.numplates.nomera3.modules.post_view_statistic.data.net.FeatureViewRestRequest
import com.numplates.nomera3.modules.post_view_statistic.data.net.PostViewRestRequest
import com.numplates.nomera3.modules.registration.data.entity.RegistrationCountryDto
import com.numplates.nomera3.modules.userprofile.data.entity.UserSimpleDto
import io.reactivex.Single
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface ApiMain {

    @PATCH("/v1/posts/{id}/subscribe")
    suspend fun subscribePost(@Path("id") postId: Long): ResponseWrapper<Boolean?>?

    // Отписаться от поста
    @PATCH("/v1/posts/{id}/unsubscribe")
    suspend fun unsubscribePost(@Path("id") postId: Long): ResponseWrapper<Boolean?>?

    @POST("/v2/complaints")
    suspend fun addComplainV2(
        @Body complainBody: HashMap<String, Any>
    ): ResponseWrapper<Any?>?

    @POST("/v1/users/{id}/hide_posts")
    suspend fun hidePosts(
        @Path("id") userId: Long,
        @Body hideBody: HashMap<String, Int>
    ): ResponseWrapper<Any?>?

    @POST("/v1/users/{id}/unhide_posts")
    suspend fun unhidePosts(@Path("id") userId: Long): ResponseWrapper<Any?>?

    @DELETE("/v1/posts/{id}")
    suspend fun deletePost(@Path("id") postId: Long): ResponseWrapper<Boolean?>?

    @GET("/v1/posts/{id}?user_type=UserSimple")
    suspend fun getPost(@Path("id") postId: Long): ResponseWrapper<Post?>?

    @Multipart
    @POST("/v2/uploads/audios/messengers/chats")
    suspend fun sendVoiceMessage(
        @Part audioFile: MultipartBody.Part?,
        @Query("wave_form[]") listOfAmplitudes: List<Int?>?
    ): ResponseWrapper<VoiceMessage>

    @FormUrlEncoded
    @POST("/v2/messages/mark_all_as_readed")
    suspend fun markRoomAsRead(@Field("room_ids[]") roomIds: List<Long>): ResponseWrapper<Any>

    @POST("/v2/messages/new_message")
    suspend fun sendNewMessage(
        @Body params: HashMap<String, Any>
    ): ResponseWrapper<Any>


    /** ----------------------  PRIVACY AND SECURITY SETTINGS -----------------------  */

    @GET("/v2/users/settings")
    suspend fun getPrivacySettings(): ResponseWrapper<PrivacySettingsResponseDto>

    @PATCH("/v2/users/settings")
    suspend fun setPrivacySetting(@Body body: PrivacySettingsResponseDto): ResponseWrapper<Any>

    @POST("/v2/users/settings/revert_to_default")
    suspend fun restoreDefaultSettings(): ResponseWrapper<Any>

    @GET("/v2/users/privacy/online/exclusion/show")
    suspend fun getOnlineWhitelistWithCounter(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): ResponseWrapper<UserWrapperWithCounter<UserSimple>>

    @FormUrlEncoded
    @POST("/v2/users/privacy/online/exclusion/show")
    suspend fun addOnlineWhitelist(@Field("ids[]") userIds: List<Long>): ResponseWrapper<Any>

    @HTTP(method = "DELETE", path = "/v2/users/privacy/online/exclusion/show", hasBody = true)
    suspend fun deleteOnlineWhitelist(@Body params: HashMap<String, List<Long>>): ResponseWrapper<Any>

    @GET("/v2/users/privacy/online/exclusion/show/search")
    suspend fun searchOnlineWhitelist(
        @Query("name") name: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): ResponseWrapper<UsersWrapper<UserSimple>>

    @GET("/v2/users/privacy/online/exclusion/hide")
    suspend fun getOnlineBlacklistWithCounter(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): ResponseWrapper<UserWrapperWithCounter<UserSimple>>

    @FormUrlEncoded
    @POST("/v2/users/privacy/online/exclusion/hide")
    suspend fun addOnlineBlacklist(@Field("ids[]") userIds: List<Long>): ResponseWrapper<Any>

    @HTTP(method = "DELETE", path = "/v2/users/privacy/online/exclusion/hide", hasBody = true)
    suspend fun deleteOnlineBlacklist(@Body params: HashMap<String, List<Long>>): ResponseWrapper<Any>

    @GET("/v2/users/privacy/online/exclusion/hide/search")
    suspend fun searchOnlineBlacklist(
        @Query("name") name: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): ResponseWrapper<UsersWrapper<UserSimple>>

    @GET("/v2/users/privacy/show-posts/exclusion")
    suspend fun getShowPostsExclusionsList(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): ResponseWrapper<UserWrapperWithCounter<UserSimple>>

    @FormUrlEncoded
    @POST("/v2/users/privacy/show-posts/exclusion")
    suspend fun addShowPostsExclusionsList(@Field("ids[]") userIds: List<Long>): ResponseWrapper<Any>

    @HTTP(method = "DELETE", path = "/v2/users/privacy/show-posts/exclusion", hasBody = true)
    suspend fun deleteShowPostsExclusionsList(@Body params: HashMap<String, List<Long>>): ResponseWrapper<Any>

    @GET("/v2/users/privacy/map/exclusion/show")
    suspend fun getMapWhitelistWithCounter(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): ResponseWrapper<UserWrapperWithCounter<UserSimple>>

    @FormUrlEncoded
    @POST("/v2/users/privacy/map/exclusion/show")
    suspend fun addMapWhitelist(@Field("ids[]") userIds: List<Long>): ResponseWrapper<Any>

    @HTTP(method = "DELETE", path = "/v2/users/privacy/map/exclusion/show", hasBody = true)
    suspend fun deleteMapWhitelist(@Body params: HashMap<String, List<Long>>): ResponseWrapper<Any>

    @GET("/v2/users/privacy/map/exclusion/show/search")
    suspend fun searchMapWhitelist(
        @Query("name") name: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): ResponseWrapper<UsersWrapper<UserSimple>>

    @GET(" /v2/users/privacy/map/exclusion/hide")
    suspend fun getMapBlacklist(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): ResponseWrapper<UsersWrapper<UserSimple>>

    @GET(" /v2/users/privacy/map/exclusion/hide")
    suspend fun getMapBlacklistWithCounter(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): ResponseWrapper<UserWrapperWithCounter<UserSimple>>

    @FormUrlEncoded
    @POST("/v2/users/privacy/map/exclusion/hide")
    suspend fun addMapBlacklist(@Field("ids[]") userIds: List<Long>): ResponseWrapper<Any>

    @HTTP(method = "DELETE", path = "/v2/users/privacy/map/exclusion/hide", hasBody = true)
    suspend fun deleteMapBlacklist(@Body params: HashMap<String, List<Long>>): ResponseWrapper<Any>

    @GET("/v2/users/privacy/map/exclusion/hide/search")
    suspend fun searchMapBlacklist(
        @Query("name") name: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): ResponseWrapper<UsersWrapper<UserSimple>>

    @GET("/v2/users/call/blacklist/search")
    suspend fun searchCallUsersBlackList(
        @Query("name") name: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): ResponseWrapper<UsersWrapper<UserSimple>>

    @Deprecated("OLD endpoint")
    @GET("/v2/users/call/blacklist")
    suspend fun getCallUsersBlackList(@QueryMap params: Map<String, Int>): ResponseWrapper<UsersWrapper<UserSimple>>


    @GET("/v2/users/call/blacklist")
    suspend fun getCallBlacklist(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): ResponseWrapper<UsersWrapper<UserSimple>>

    @GET("/v2/users/call/blacklist")
    suspend fun getCallBlacklistWithCounter(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): ResponseWrapper<UserWrapperWithCounter<UserSimple>>

    @FormUrlEncoded
    @POST("/v2/users/call/blacklist")
    suspend fun addCallUserToBlackList(@Field("ids[]") userIds: List<Long>): ResponseWrapper<Any>

    @HTTP(method = "DELETE", path = "/v2/users/call/blacklist", hasBody = true)
    suspend fun deleteCallUserFromBlackList(@Body params: HashMap<String, List<Long>>): ResponseWrapper<Any>

    @GET("/v2/users/call/whitelist/search")
    suspend fun searchCallUsersWhiteList(
        @Query("name") name: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): ResponseWrapper<UsersWrapper<UserSimple>>

    @GET("/v2/users/call/whitelist")
    suspend fun getCallUsersWhiteList(@QueryMap params: Map<String, Int>): ResponseWrapper<UsersWrapper<UserSimple>>

    @GET("/v2/users/call/whitelist")
    suspend fun getCallWhitelist(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): ResponseWrapper<UsersWrapper<UserSimple>>

    @GET("/v2/users/call/whitelist")
    suspend fun getCallWhitelistWithCounter(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): ResponseWrapper<UserWrapperWithCounter<UserSimple>>

    @FormUrlEncoded
    @POST("/v2/users/call/whitelist")
    suspend fun addCallUserToWhiteList(@Field("ids[]") userIds: List<Long>): ResponseWrapper<Any>


    @HTTP(method = "DELETE", path = "/v2/users/call/whitelist", hasBody = true)
    suspend fun deleteCallUserFromWhiteList(@Body params: HashMap<String, List<Long>>): ResponseWrapper<Any>

    @GET("/v2/users/message/blacklist/search")
    suspend fun searchPrivateMessagesUsersBlackList(
        @Query("name") name: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): ResponseWrapper<UsersWrapper<UserSimple>>

    @GET("/v2/users/message/blacklist")
    suspend fun getPrivateMessageBlacklistWithCounter(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): ResponseWrapper<UserWrapperWithCounter<UserSimple>>

    @FormUrlEncoded
    @POST("/v2/users/message/blacklist")
    suspend fun addPrivateMessageUserToBlackList(@Field("ids[]") userIds: List<Long>): ResponseWrapper<Any>

    @HTTP(method = "DELETE", path = "/v2/users/message/blacklist", hasBody = true)
    suspend fun deletePrivateMessageUserFromBlackList(@Body params: HashMap<String, List<Long>>): ResponseWrapper<Any>

    @GET("/v2/users/message/whitelist/search")
    suspend fun searchPrivateMessagesUsersWhiteList(
        @Query("name") name: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): ResponseWrapper<UsersWrapper<UserSimple>>

    @GET("/v2/users/message/whitelist")
    suspend fun getPrivateMessagesWhitelist(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): ResponseWrapper<UsersWrapper<UserSimple>>

    @GET("/v2/users/message/whitelist")
    suspend fun getPrivateMessageWhitelistWithCounter(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): ResponseWrapper<UserWrapperWithCounter<UserSimple>>

    @FormUrlEncoded
    @POST("/v2/users/message/whitelist")
    suspend fun addPrivateMessageUserToWhiteList(@Field("ids[]") userIds: List<Long>): ResponseWrapper<Any>

    @HTTP(method = "DELETE", path = "/v2/users/message/whitelist", hasBody = true)
    suspend fun deletePrivateMessagesUserFromWhiteList(@Body params: HashMap<String, List<Long>>): ResponseWrapper<Any>

    @GET("/v2/users/privacy/blacklist/exclusion")
    suspend fun getBlacklistExclusions(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): ResponseWrapper<UsersWrapper<UserSimple>>


    @GET("/v2/users/privacy/blacklist/exclusion")
    suspend fun getBlacklistExclusionsWithCounter(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): ResponseWrapper<UserWrapperWithCounter<UserSimple>>

    @FormUrlEncoded
    @POST("/v2/users/privacy/blacklist/exclusion")
    suspend fun addBlacklistExclusion(@Field("ids[]") userIds: List<Long>): ResponseWrapper<Unit>

    @HTTP(method = "DELETE", path = "/v2/users/privacy/blacklist/exclusion", hasBody = true)
    suspend fun deleteBlacklistExclusion(@Query("ids[]") userIds: List<Long>): ResponseWrapper<Unit>

    @GET("/v2/users/privacy/blacklist/exclusion/search")
    suspend fun searchBlacklistExclusion(
        @Query("name") name: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): ResponseWrapper<UsersWrapper<UserSimple>>

    @GET("/v2/users/settings/messages/exclusion")
    suspend fun getMessageSettingsExclusion(@QueryMap params: Map<String, Int>):
        ResponseWrapper<UserWrapperWithCounter<UserSimple>>

    @GET("/v2/users/settings/messages/exclusion/search")
    suspend fun searchMessageSettingsExclusion(
        @Query("name") name: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): ResponseWrapper<UsersWrapper<UserSimple>>

    @FormUrlEncoded
    @POST("/v2/users/settings/messages/exclusion")
    suspend fun addMessageSettingsExclusion(@Field("ids[]") userIds: List<Long>): ResponseWrapper<Any>


    @HTTP(method = "DELETE", path = "/v2/users/settings/messages/exclusion", hasBody = true)
    suspend fun deleteMessageSettingsExclusion(@Body params: HashMap<String, List<Long>>): ResponseWrapper<Any>

    @GET("/v3/users/events/unreaded_count")
    suspend fun getUnreadEventsCounter(): ResponseWrapper<NotificationCount>

    @GET("/v3/groups/repost_allowed_list")
    suspend fun getGroupsAllowedToRepost(
        @Query("offset") startIndex: Int,
        @Query("limit") quantity: Int
    ): ResponseWrapper<Communities>

    @POST("/v2/posts/{post_id}/repost/roadtape")
    suspend fun repostRoadtape(
        @Path("post_id") postId: Long,
        @Body params: HashMap<String, Any>
    ): ResponseWrapper<Any>

    @POST("/v2/posts/{post_id}/repost/group")
    suspend fun repostGroup(
        @Path("post_id") postId: Long,
        @Body params: HashMap<String, Any>
    ): ResponseWrapper<Any>

    @POST("/v2/posts/{post_id}/repost/message")
    suspend fun repostMessage(
        @Path("post_id") postId: Long,
        @Body params: HashMap<String, Any>
    ): ResponseWrapper<Any>

    @Multipart
    @POST("/v2/users/galleries")
    suspend fun uploadMediaToGallery(
        @Part item_1: MultipartBody.Part?,
        @Part item_2: MultipartBody.Part?,
        @Part item_3: MultipartBody.Part?,
        @Part item_4: MultipartBody.Part?,
        @Part item_5: MultipartBody.Part?
    ): ResponseWrapper<Any>

    @GET("/v2/users/friends")
    suspend fun getFriendsList(
        @Query("user_id") userId: Long,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("state") state: Int, //default 2 - confirmed
        @Query("user_type") userType: String
    ): ResponseWrapper<ListFriendsResponse>

    /**
     * Получить список подписок юзера
     * */
    @GET("/v2/users/subscription")
    suspend fun getUserSubscriptions(
        @Query("user_id") userId: Long,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("user_type") userType: String
    ): ResponseWrapper<ListSubscriptionResponse>

    /**
     * Добавить подписку на пользователей
     * */
    @POST("/v2/users/subscription")
    suspend fun addSubscriptions(
        @Body params: HashMap<String, Any>
    ): ResponseWrapper<Any>


    /**
     * Удалить юзера из подписок
     * */
    @HTTP(method = "DELETE", path = "/v2/users/subscription", hasBody = true)
    suspend fun deleteUserFromSubscriptions(
        @Body params: HashMap<String, Any>
    ): ResponseWrapper<Any>

    /**
     * Поиск по подписчкам
     * */
    @GET("/v2/users/subscription/search")
    suspend fun subscriptionsSearch(
        @Query("user_id") userId: Long,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("user_type") userType: String,
        @Query("name") name: String
    ): ResponseWrapper<ListSubscriptionResponse>


    /**
     * Получить список подписчиков юзера
     * */
    @GET("/v2/users/subscribers")
    suspend fun getUserSubscribers(
        @Query("user_id") userId: Long,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("user_type") userType: String
    ): ResponseWrapper<ListSubscriptionResponse>

    /**
     * Удалить юзера из подписчиков
     * */
    @HTTP(method = "DELETE", path = "/v2/users/subscribers", hasBody = true)
    suspend fun deleteUserFromSubscribers(
        @Body params: HashMap<String, Any>
    ): ResponseWrapper<Any>


    /**
     * Поиск по подпискам
     * */
    @GET("/v2/users/subscribers/search")
    suspend fun subscriberSearch(
        @Query("user_id") userId: Long,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("user_type") userType: String,
        @Query("name") name: String
    ): ResponseWrapper<ListSubscriptionResponse>

    @GET("/v2/users/subscription/notification")
    suspend fun getSubscriptionsNotificationsUsers(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("user_type") userType: String
    ): ResponseWrapper<UserWrapperWithCounter<UserSimple>>


    @GET("/v2/users/subscription/notification/search")
    suspend fun searchSubscriptionsNotificationsUsers(
        @Query("name") name: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("user_type") userType: String
    ): ResponseWrapper<UsersWrapper<UserSimple>>


    @FormUrlEncoded
    @POST("/v2/users/subscription/notification")
    suspend fun addSubscriptionsNotificationsUser(@Field("ids[]") userIds: List<Long>): ResponseWrapper<Any>


    @HTTP(method = "DELETE", path = "/v2/users/subscription/notification", hasBody = true)
    suspend fun deleteSubscriptionsNotificationsUser(@Body params: HashMap<String, List<Long>>): ResponseWrapper<Any>

    @HTTP(method = "DELETE", path = "/v2/users/profile", hasBody = true)
    suspend fun deleteOwnProfile(@Body params: HashMap<String, Any?>): ResponseWrapper<Any>

    @PATCH("/v2/users/profile/recover")
    suspend fun restoreOwnProfile(): ResponseWrapper<Any>

    @Deprecated("endpoint NOT work")
    @Multipart
    @POST("/v2/uploads")
    suspend fun uploadMediaSimple(
        @Part file: MultipartBody.Part,
        @Query("user_id") userId: Long,
        @Query("source[]") source: List<String>
    ): ResponseWrapper<Any>

    @FormUrlEncoded
    @POST("/v2/users/referal")
    suspend fun registerReferralCode(@Field("code") code: String): ResponseWrapper<Any>

    @POST("/v2/messages/delivered")
    fun markAsDelivered(
        @Body params: HashMap<String, Any>
    ): Single<ResponseWrapper<Any>>

    @POST("/v2/messages/mark_all_as_delivered")
    suspend fun markRoomsAsDelivered(@Body params: HashMap<String, Any>): ResponseWrapper<Any>

    @HTTP(method = "DELETE", path = "/v2/purchases/gift", hasBody = true)
    suspend fun deleteGift(@Body params: HashMap<String, Long>): ResponseWrapper<Any>

    @POST("/v2/announces/feature_user_actions")
    suspend fun actionOnFeature(@Body params: HashMap<String, Any>): ResponseWrapper<FeatureActionResponse>

    @GET("/v1/info/holiday")
    suspend fun getHolidayInfo(): ResponseWrapper<GetHolidayResponse>

    @GET("/v2/purchases/daily_visits")
    suspend fun getHolidayDailyVisits(): ResponseWrapper<HolidayVisitsEntity>

    @GET("/v2/avatars/predefined")
    suspend fun generateRandomAvatar(@Query("gender") gender: Int): ResponseWrapper<RandomAvatarResponse>

    @POST("/v2/feed/subs_posts_read")
    suspend fun markSubscriptionPostRead(): ResponseWrapper<Boolean>

    @POST("/v2/feed/post_views")
    suspend fun uploadPostViews(@Body request: PostViewRestRequest): ResponseWrapper<*>

    @POST("/v2/feed/features")
    suspend fun uploadFeatureViews(@Body request: FeatureViewRestRequest): ResponseWrapper<*>

    @GET("/v1/info/countries/signup")
    suspend fun getSignupCountries(): ResponseWrapper<List<RegistrationCountryDto>>

    @GET("/v1/info/countries")
    suspend fun getCountries(): ResponseWrapper<Countries>

    /**
     * Получение мордоленты карты
     */
    @GET("/v3/users/{uid}/maptape")
    suspend fun getUserCards(
        @Path("uid") uid: Long,
        @Query("user_type") userType: String?,
        @Query("selected_user_id") selectedUserId: Long,
        @Query("excluded_user_ids") excludedUserIds: String?,
        @Query("show_only_friends") showOnlyFriends: Int,
        @Query("show_without_friends") showWithoutFriends: Int,
        @Query("gender") gender: String?,
        @Query("lat") lat: Double?,
        @Query("lon") lon: Double?,
        @Query("limit") limit: Int
    ): ResponseWrapper<List<UserCardDto>>

    @GET("v1/map/friends")
    suspend fun getMapFriends(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("search") search: String?
    ): ResponseWrapper<List<UserSimpleDto>>

    /**
     * Mediakeyboard
     */

    @GET("/v2/media_keyboard/favourite")
    suspend fun getMediakeyboardFavorites(
        @Query("last_id") lastId: Int?,
        @Query("limit") limit: Int
    ): ResponseWrapper<List<MediakeyboardFavoriteRecentDto>>

    @POST("/v2/media_keyboard/favourite")
    suspend fun addMediakeyboardFavorite(
        @Body gifBody: AddFavoriteBody.AddFavoriteByGifBody
        ): ResponseWrapper<Any>

    @POST("/v2/media_keyboard/favourite")
    suspend fun addMediakeyboardFavorite(
        @Body messageBody: AddFavoriteBody.AddFavoriteByMessageBody
    ): ResponseWrapper<Any>

    @POST("/v2/media_keyboard/favourite")
    suspend fun addMediakeyboardFavorite(
        @Body stickerBody: AddFavoriteBody.AddFavoriteByStickerBody
    ): ResponseWrapper<Any>

    @DELETE("/v2/media_keyboard/favourite")
    suspend fun deleteMediakeyboardFavorite(
        @Query("favourite_id") favoriteId: Int
    ): ResponseWrapper<Any>

    @GET("/v2/media_keyboard/recent")
    suspend fun getMediaKeyboardRecents(
        @Query("type") type: String
    ) : ResponseWrapper<List<MediakeyboardFavoriteRecentDto>>

    @DELETE("/v2/media_keyboard/recent")
    suspend fun deleteMediaKeyboardRecent(
        @Query("type") type: String,
        @Query("recent_id") recentId: Int
    ): ResponseWrapper<Any>

    @DELETE("/v2/media_keyboard/recent")
    suspend fun clearMediaKeyboardRecents(
        @Query("type") type: String
    ): ResponseWrapper<Any>

    @GET("/v2/users/moment/media_keyboard/favourite")
    suspend fun getMomentMediakeyboardFavorites(
        @Query("last_id") lastId: Int?,
        @Query("limit") limit: Int
    ): ResponseWrapper<List<MediakeyboardFavoriteRecentDto>>

    @POST("/v2/users/moment/media_keyboard/favourite")
    suspend fun addMomentMediakeyboardFavorite(
        @Body stickerBody: AddFavoriteBody.AddFavoriteByStickerBody
    ): ResponseWrapper<Any>

    @DELETE("/v2/users/moment/media_keyboard/favourite")
    suspend fun deleteMomentMediakeyboardFavorite(
        @Query("favourite_id") favoriteId: Int
    ): ResponseWrapper<Any>

    @GET("/v2/users/moment/media_keyboard/recent")
    suspend fun getMomentMediaKeyboardRecents(
        @Query("type") type: String
    ) : ResponseWrapper<List<MediakeyboardFavoriteRecentDto>>

    @DELETE("/v2/users/moment/media_keyboard/recent")
    suspend fun deleteMomentMediaKeyboardRecent(
        @Query("type") type: String,
        @Query("recent_id") recentId: Int
    ): ResponseWrapper<Any>

    @DELETE("/v2/users/moment/media_keyboard/recent")
    suspend fun clearMomentMediaKeyboardRecents(
        @Query("type") type: String
    ): ResponseWrapper<Any>


    @GET("/v3/addresses/places")
    suspend fun getPlaces(
        @Query("keyword") keyword: String? = null,
        @Query("lat") lat: Double? = null,
        @Query("lon") lon: Double? = null
    ) : ResponseWrapper<List<PlaceDto>>

    @GET("/v3/map_events/search")
    suspend fun getMapEvents(
        @Query("min_lat") minLat: Double,
        @Query("min_lon") minLon: Double,
        @Query("max_lat") maxLat: Double,
        @Query("max_lon") maxLon: Double,
        @Query("event_type") typesString: String,
        @Query("time_filter") timeFilter: Int,
        @Query("limit") limit: Int,
    ): ResponseWrapper<List<PostEntityResponse>>

    @GET("/v3/map_events/find_snippets")
    suspend fun getMapEventSnippets(
        @Query("selected_event_id") selectedEventId: Long,
        @Query("excluded_event_ids") excludedEventIds: String,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("event_type") typesString: String,
        @Query("time_filter") timeFilter: Int,
        @Query("limit") limit: Int,
    ): ResponseWrapper<List<PostEntityResponse>>

    @GET("/v2/media_keyboard/sticker_packs")
    suspend fun getMediaKeyboardStickerPacks(): ResponseWrapper<List<MediaKeyboardStickerPackDto>>

    @POST("/v2/media_keyboard/sticker_packs/{id}/set_viewed")
    suspend fun setMediaKeyboardStickerPackViewed(
        @Path("id") id: Int
    ): ResponseWrapper<Any>

    @GET("/v3/map_events/events_count")
    suspend fun getActiveEventCount(): ResponseWrapper<ActiveEventCountDto>

    @GET("/v3/map_events/members")
    suspend fun getEventParticipants(
        @Query("event_id") eventId: Long,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
    ): ResponseWrapper<List<UserSimpleDto>>

    @GET("/v3/map_events/event")
    suspend fun getEvent(
        @Query("post_id") postId: Long,
    ): ResponseWrapper<GetEventResultDto>

    @POST("/v3/map_events/add_member")
    suspend fun joinEvent(
        @Body body: JoinEventBodyDto
    ): ResponseWrapper<PostEntityResponse>

    @DELETE("/v3/map_events/{eventId}/delete_member")
    suspend fun leaveEvent(
        @Path("eventId") eventId: Long
    ): ResponseWrapper<PostEntityResponse>

    @GET("/v1/posts/{id}?user_type=UserSimple")
    suspend fun getEventPost(@Path("id") postId: Long): ResponseWrapper<PostEntityResponse>

    @DELETE("/v3/map_events/delete_participant")
    suspend fun removeEventParticipant(
        @Query("event_id") eventId: Long,
        @Query("participant_id") userId: Long,
    ): ResponseWrapper<PostEntityResponse>

    @GET("/v3/map_events/nearest_by_place")
    suspend fun getEventsListNearby(
        @Query("user_id") userId: Long,
        @Query("event_type") typesString: String,
        @Query("time_filter") timeFilter: Int,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("lon") lon: Double,
        @Query("lat") lat: Double,
    ): ResponseWrapper<List<PostEntityResponse>>

    @GET("/v3/map_events/my")
    suspend fun getEventsListMy(
        @Query("user_id") userId: Long,
        @Query("event_type") typesString: String,
        @Query("time_filter") timeFilter: Int,
        @Query("category_id") categoryId: Int,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
    ): ResponseWrapper<List<PostEntityResponse>>

    @GET("/v3/map_events/archive")
    suspend fun getEventsListArchive(
        @Query("user_id") userId: Long,
        @Query("event_type") typesString: String,
        @Query("category_id") categoryId: Int,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
    ): ResponseWrapper<List<PostEntityResponse>>

    @GET("/v3/map_widget/point_info")
    suspend fun getMapWidgetPointInfo(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("with_weather") getWeather: Int,
    ): ResponseWrapper<MapWidgetPointInfoDto>
}
