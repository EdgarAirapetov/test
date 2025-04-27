package com.numplates.nomera3.modules.registration.data

import com.numplates.nomera3.data.network.City
import com.numplates.nomera3.data.network.Countries
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.meera.db.models.userprofile.UserProfileNew
import com.numplates.nomera3.modules.user.data.entity.UploadAvatarResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query


interface RegistrationApi {

    @GET("/v1/info/countries")
    suspend fun getCountries(): ResponseWrapper<Countries>

    @GET("/main/cities_suggestion")
    suspend fun getCitiesSuggestion(
        @Query("in_country_ids") countryId: Long?,
        @Query("like") query: String?
    ): ResponseWrapper<List<City>>

    @Multipart
    @POST("/v2/uploads/images/users/avatars")
    suspend fun uploadAvatar(
        @Part imageFile: MultipartBody.Part?,
        @Part("avatar_animation") avatarAnimation: RequestBody?
    ): ResponseWrapper<UploadAvatarResponse?>

    @PATCH("/v3/users/signup")
    suspend fun uploadUserData(@Body registration: RegistrationRequest): ResponseWrapper<UserProfileNew>

    @GET("/v2/users/generate_uniqname")
    suspend fun generateUniqueName(@Query("source") source: String): ResponseWrapper<String>
}
