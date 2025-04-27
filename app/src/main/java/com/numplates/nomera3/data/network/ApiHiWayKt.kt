package com.numplates.nomera3.data.network

import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.data.network.core.ResponseWrapper
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Апи для миграции методов из {@link ApiHiWay}
 */
interface ApiHiWayKt {

    @POST("/v1/users/{id}/set_gps")
    suspend fun setGpsPosition(
        @Path("id") id: Long, @Body gpsRequest: SetGPSRequestDto
    ): ResponseWrapper<Any>

    @FormUrlEncoded
    @POST("/v2/users/subscribe")
    suspend fun subscribePushNotifications(
        @Header("authorization") token: String?,
        @Field("token") deviceToken: String?,
        @Field("device") device: String?,
        @Field("locale") locale: String?,
        @Field("timezone") timeZone: Float?
    ): ResponseWrapper<Any>

    @POST("/v2/users/unsubscribe")
    suspend fun unsubscribePushNotifications(
        @Header("authorization") token: String?
    ): ResponseWrapper<Any>

    @GET("/v1/info/car_brands")
    suspend fun carBrandsList(
        @Query("query") query: String?
    ): ResponseWrapper<CarsMakes?>

    /**
     * Модели авто по производителю
     */
    @GET("/v1/info/car_models")
    suspend fun carModelsList(
        @Query("brand_id") makeId: Int?, @Query("query") query: String?
    ): ResponseWrapper<CarsModels?>

    @GET("/v1/map/objects")
    suspend fun getMapObjects(
        @Query("min_x") gpsXMin: Double,
        @Query("max_x") gpsXMax: Double,
        @Query("min_y") gpsYMin: Double,
        @Query("max_y") gpsYMax: Double,
        @Query("zoom") zoom: Double,
        @Query("show_only_friends") showOnlyFriends: Int,
        @Query("show_without_friends") showWithoutFriends: Int,
        @Query("show_only_subscriptions") showOnlySubscription: Int,
        @Query("gender") gender: String?,
        @Query("vehicle_types") vehicleTypes: String?,
        @Query("states") states: String?,
        @Query("age_min") ageMin: Int,
        @Query("age_max") ageMax: Int,
        @Query("show_pedestrian") showPedestrian: Int,
        @Query("count_events") countEvents: Int,
        @Query("max_users_count") maxUsersCount: Int,
        @Query("road_events") roadEvents: String?,
        @Query("user_type") userType: String?
    ): ResponseWrapper<MapObjectsDto?>?


    /**
     * редактирование ТС
     */
    @Multipart
    @PATCH("/v1/vehicles/{vehicle_id}")
    suspend fun updateVehicle(
        @Path("vehicle_id") vehicleId: Int?,
        @PartMap vehicleMap: HashMap<String, RequestBody>?,
        @Part image: MultipartBody.Part?
    ): ResponseWrapper<Vehicle?>

    @PATCH("/v1/vehicles/{vehicle_id}")
    suspend fun updateVehicle(
        @Path("vehicle_id") vehicleId: Int?,
        @Body vehicle: VehicleRequest?
    ): ResponseWrapper<Vehicle?>

    /**
     * добавление ТС
     */
    @Multipart
    @POST("/v1/vehicles")
    suspend fun addVehicle(
        @PartMap vehicleMap: HashMap<String, RequestBody>?,
        @Part image: MultipartBody.Part?
    ): ResponseWrapper<VehicleResponse?>

    @POST("/v1/vehicles")
    suspend fun addVehicle(
        @Body vehicle: VehicleRequest?
    ): ResponseWrapper<VehicleResponse?>

    @GET("v1/users/search")
    suspend fun searchUserSimple(
        @Query("query") query: String?,
        @Query("friends") friendsType: Int,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("user_type") userType: String?
    ): ResponseWrapper<List<UserSimple?>?>

    @DELETE("/v1/vehicles/{vehicle_id}")
    suspend fun deleteVehicle(
        @Path("vehicle_id") vehicleId: String
    ): ResponseWrapper<EmptyModel>
}
