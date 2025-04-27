package com.numplates.nomera3.modules.notifications.data.api

import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.notifications.data.entity.CountNewNotificationEntityResponse
import com.numplates.nomera3.modules.notifications.data.entity.NotificationCountResponse
import com.numplates.nomera3.modules.notifications.data.entity.NotificationEntityResponse
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.PATCH
import retrofit2.http.Query

interface NotificationApi {

    @Deprecated("Appeared a new version of the api method")
    //Get grouped events
    @GET("/v2/users/events")
    fun fetchGroupedNotification(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): Single<ResponseWrapper<List<NotificationEntityResponse>>>

    //Get grouped events NEW
    @GET("/v2/users/events")
    fun fetchGroupedNotificationNew(
            @Query("limit") limit: Int,
            @Query("created_at") createdAt: Long
    ): Single<ResponseWrapper<List<NotificationEntityResponse>>>

    @GET("/v2/users/events")
    suspend fun fetchGroupedNotificationSuspend(
        @Query("limit") limit: Int,
        @Query("created_at") createdAt: Long
    ): ResponseWrapper<List<NotificationEntityResponse>>

    //Get expanded events
    @GET("/v2/users/events/expand")
    suspend fun fetchExpandedNotification(
            @Query("id") id: String,
            @Query("limit") limit: Int,
            @Query("offset") offset: Int
    ): ResponseWrapper<List<NotificationEntityResponse>>

    //Get new events count
    @GET("/v2/users/events/new_count")
    fun fetchNewNotificationCount(): Single<ResponseWrapper<CountNewNotificationEntityResponse>>

    //Set as read event
    @PATCH("/v2/users/events/as_read")
    fun setNotificationAsRead(@Body params: HashMap<String, Any>): Single<ResponseWrapper<Boolean>>

    @PATCH("/v2/users/events/as_read")
    suspend fun markNotificationAsRead(@Body params: HashMap<String, Any>): ResponseWrapper<Boolean>

    //Set all events as read
    @PATCH("/v2/users/events/as_read")
    fun setAllNotificationsAsRead(): Single<ResponseWrapper<Boolean>>

    @PATCH("/v2/users/events/as_read")
    suspend fun setAllNotificationsAsReadSuspend()

    //Set group of notifications as
    @PATCH("/v2/users/events/as_not_new")
    fun setGroupOfNotificationsAsSeen(
            @Body ids: HashMap<String, Any>
    ): Single<ResponseWrapper<Boolean>>

    //Delete all events
    @DELETE("/v2/users/events/")
    fun deleteAllEvents(): Single<ResponseWrapper<Boolean>>

    //Delete event
    @DELETE("/v2/users/events/")
    fun deleteEvent(@Query("id") id: String): Single<ResponseWrapper<Boolean>>

    //Delete event
    @HTTP(method = "DELETE", path = "/v2/users/events/", hasBody = true)
    fun deleteEventByGroupId(@Body params: HashMap<String, Any>): Single<ResponseWrapper<Boolean>>

    //Delete event
    @HTTP(method = "DELETE", path = "/v2/users/events/", hasBody = true)
    suspend fun deleteEventByGroupIdSuspend(
        @Query("id") notificationId: String,
        @Query("is_group") isNotificationGroup: Boolean
    ): ResponseWrapper<Boolean>

    @GET("/v3/users/events/unreaded_count")
    fun getUnreadEventsCounter(): Single<ResponseWrapper<NotificationCountResponse>>
}
