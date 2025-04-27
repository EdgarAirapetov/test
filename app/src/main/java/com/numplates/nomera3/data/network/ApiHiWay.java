package com.numplates.nomera3.data.network;


import com.meera.db.models.userprofile.UserSimple;
import com.numplates.nomera3.data.network.core.ResponseWrapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.Single;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * @deprecated Следует перенести методы по мере необходимости в {@link ApiHiWayKt}.kt
 */
@Deprecated
public interface ApiHiWay {

    @FormUrlEncoded
    @POST("/v2/authentication/refresh_token")
    Flowable<Response<GetToken>> refreshToken(
            @Field("refresh_token") String refreshToken
    );

    /**
     * Migrate old tokens to new tokens with expired and refresh
     *
     * @param oldToken
     * @return
     */
    @FormUrlEncoded
    @POST("/v2/authentication/old_token")
    Flowable<Response<GetToken>> oldToken(
            @Field("old_token") String oldToken
    );

    /**
     * Список производителей авто
     */
    @GET("/v1/info/car_brands")
    Flowable<ResponseWrapper<CarsMakes>> carMakersList(
            @Query("query") String query);

    /**
     * Список стран
     */
    @GET("/v1/info/countries")
    Flowable<ResponseWrapper<Countries>> getCountries();

    /**
     * Инвормация о приложении
     */
    @GET("/v1/info/application?type=android")
    Flowable<ResponseWrapper<Settings>> getApplicationInfo();


    /**
     * Список типов ТС
     */
    @GET("/v1/info/vehicle_types")
    Flowable<ResponseWrapper<VehicleTypes>> getVehicleTypes();

    /**
     * Модели авто по производителю
     */
    @GET("/v1/info/car_models")
    Flowable<ResponseWrapper<CarsModels>> carModelsList(
            @Query("brand_id") String makeId,
            @Query("query") String query);

    /**
     * добавление ТС
     */
    @Multipart
    @POST("/v1/vehicles")
    Flowable<ResponseWrapper<VehicleResponse>> addVehicle(
            @PartMap() Map<String, RequestBody> vehicleMap,
            @Part MultipartBody.Part image);

    @POST("/v1/vehicles")
    Flowable<ResponseWrapper<VehicleResponse>> addVehicle(
            @Body VehicleRequest vehicle);

    /**
     * список ТС
     */
    @GET("/v1/users/{user_id}/vehicles")
    Flowable<ResponseWrapper<Vehicles>> vehicleList(
            @Path("user_id") Long userId);

    /**
     * редактирование ТС
     */
    @Multipart
    @PATCH("/v1/vehicles/{vehicle_id}")
    Flowable<ResponseWrapper<Vehicle>> updateVehicle(
            @Path("vehicle_id") int vehicleId,
            @PartMap() Map<String, RequestBody> vehicleMap,
            @Part MultipartBody.Part image);

    @PATCH("/v1/vehicles/{vehicle_id}")
    Flowable<ResponseWrapper<Vehicle>> updateVehicle(
            @Path("vehicle_id") int vehicleId,
            @Body VehicleRequest vehicle);

    /**
     * инфо ТС
     */
    @GET("/v1/vehicles/{vehicle_id}")
    Flowable<ResponseWrapper<VehicleResponce>> getVehicle(
            @Path("vehicle_id") String vehicleId);

    /**
     * удаление ТС
     */
    @DELETE("/v1/vehicles/{vehicle_id}")
    Flowable<ResponseWrapper<EmptyModel>> deleteVehicle(
            @Path("vehicle_id") String vehicleId);

    /**
     * установка основного ТС
     */
    @PATCH("/v1/vehicles/{vehicle_id}/set_main")
    Flowable<ResponseWrapper<EmptyModel>> setMainVehicle(
            @Path("vehicle_id") String vehicleId,
            @Query("is_main") int isMain);

    @DELETE("/v1/albums/{id}")
    Flowable<ResponseWrapper<Object>> removePhoto(@Path("id") long photoId);

    /**
     * Список друзьяшек пользователя
     */
    @GET("/v1/users/{id}/friends")
    Flowable<ResponseWrapper<List<UserModel>>> getFriendList(@Path("id") long userId, @Query("page") int page);

    /**
     * Список неподтвержденных входящих заявок в друзья
     */
    @GET("/v1/users/{id}/friends/in")
    Flowable<ResponseWrapper<List<UserModel>>> getFriendIncomingList(@Path("id") long userId, @Query("page") int page);

    /**
     * Чёрный список пользователя
     */
    @GET("/v1/users/{id}/blacklist")
    Flowable<ResponseWrapper<List<UserModel>>> getFriendBlockedList(@Path("id") long userId);

    @GET("/v1/users/{id}/friends/out")
    Flowable<ResponseWrapper<List<UserModel>>> getFriendOutcomingList(@Path("id") long userId, @Query("page") int page);

    @POST("/v1/users/{id}/blacklist")
    Single<ResponseWrapper<Boolean>> setBlockedStatusToUser(@Path("id") long currentUserId, @Body BlockRequest blockRequest);

    @DELETE("/v1/users/{id}/remove_friend")
    Single<ResponseWrapper<Boolean>> removeUser(@Path("id") long remoteUserId);

    @DELETE("/v1/users/{id}/remove_friend")
    Single<ResponseWrapper<Boolean>> removeUser(@Path("id") long remoteUserId, @Query("type") String type); // only_friend

    @GET("v1/users/search")
    Flowable<ResponseWrapper<List<UserSimple>>> searchUserSimple(
            @Query("query") String query,
            @Query("friends") int friendsType,
            @Query("limit") int limit,
            @Query("offset") int offset,
            @Query("user_type") String userType);

    @GET("v1/users/search")
    Flowable<ResponseWrapper<List<UserSearchByNameModel>>> searchUserExt(
            @Query("query") String query,
            @Query("friends") Object friendsType,
            @Query("limit") int limit,
            @Query("offset") int offset);

    @Headers("X-Skip-Cache: 1")
    @POST("/v1/iap/android")
    Single<ResponseWrapper<Boolean>> purchasePremium(@Body IapRequest request);

    @GET("/v1/users/{id}/events")
    Flowable<ResponseWrapper<List<NotificationResponse>>> getAllEvents(@Path("id") Long userId, @Query("limit") int limit, @Query("offset") int offset);

    @POST("/v1/users/{id}/hide_posts")
    Flowable<ResponseWrapper<Object>> hidePosts(
            @Path("id") long userId,
            @Body HashMap<String, Integer> hideBody);

    //обновление настроек пушей
    @PATCH("/v1/users/{user_id}/settings")
    Flowable<ResponseWrapper<PushSettingsResponse>> updatePushSettings(
            @Body PushSettingsResponse body,
            @Path("user_id") long userID
    );

    //получение настроек пушей
    @GET("/v1/users/{user_id}/settings")
    Flowable<ResponseWrapper<PushSettingsResponse>> getPushSettings(
            @Path("user_id") long userID
    );

    @Multipart
    @POST("/v2/uploads/images/users/albums")
    Flowable<ResponseWrapper<Object>> uploadImageToAlbum(
            @Part MultipartBody.Part imageFile
    );

}
