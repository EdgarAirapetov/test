package com.numplates.nomera3.modules.feed.data.api

import com.meera.referrals.data.model.ReferralDataDto
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.feed.data.entity.CheckPostUpdateAvailabilityResponse
import com.numplates.nomera3.modules.feed.data.entity.PostEntityResponse
import com.numplates.nomera3.modules.feed.data.entity.PostsEntityResponse
import com.numplates.nomera3.modules.feed.data.entity.RoadSuggestionsDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface RoadApi {

    @GET("/v1/posts?user_type=UserSimple")
    @Headers("Cache-Control: no-cache")
    suspend fun getPosts(
            @Query("start_post_id") startPostId: Long,
            @Query("quantity") quantity: Int,
            @Query("road_type") roadType: Int,
            @Query("cities") cityId: String,
            @Query("user_id") userId: Long,
            @Query("group_id") groupId: Int,
            @Query("countries") countryIds: String,
            @Query("hashtag") hashtag: String,
            @Query("include_groups") includeGroups: Boolean?,
            @Query("recommended") recommended: Boolean?
    ): ResponseWrapper<PostsEntityResponse>

    @GET("/v1/posts/{id}?user_type=UserSimple")
    suspend fun getPost(@Path("id") postId: Long): ResponseWrapper<PostEntityResponse>

    @Multipart
    @POST("/v2/posts/")
    suspend fun addPost(
            @Part("group_id") groupId: RequestBody?,
            @Part("text") text: RequestBody?,
            @Part image: MultipartBody.Part?,
            @Part("privacy") roadType: RequestBody?,
            @Part("comment_availability") commentSetting: RequestBody?
    ): ResponseWrapper<PostsEntityResponse>

    @PATCH("/v1/posts/{id}/subscribe")
    suspend fun subscribePost(@Path("id") postId: Long): ResponseWrapper<Boolean>

    @PATCH("/v1/posts/{id}/unsubscribe")
    suspend fun unsubscribePost(@Path("id") postId: Long): ResponseWrapper<Boolean>

    @POST("/v2/complaints")
    suspend fun addComplain(@Body complainBody: HashMap<String, Any>): ResponseWrapper<Any>

    @POST("/v1/users/{id}/hide_posts")
    suspend fun hidePosts(
            @Path("id") userId: Long,
            @Body hideBody: HashMap<String, Int>
    ): ResponseWrapper<Any>

    @POST("/v1/posts/{id}/hide")
    suspend fun hidePost(@Path("id") postId: Long): ResponseWrapper<Any>

    @DELETE("/v1/posts/{id}")
    suspend fun deletePost(@Path("id") postId: Long): ResponseWrapper<Boolean>

    @POST("/v2/posts/{postId}/like")
    suspend fun likePost(
        @Path("postId") postId: Long,
        @Body likeBody: HashMap<String, Int>
    ): ResponseWrapper<PostEntityResponse>

    @POST("/v2/announces/feature_user_actions")
    suspend fun actionOnFeature(@Body params: HashMap<String, Any>): ResponseWrapper<Any>

    @GET("/v2/users/referal")
    suspend fun getReferral(): ResponseWrapper<ReferralDataDto>

    @PATCH("/v2/users/referal/vip")
    suspend fun getVip(): Response<Any>

    @GET("/v2/users/search/suggested_for_main_road")
    suspend fun getRoadSuggestions() : ResponseWrapper<RoadSuggestionsDto>

    @GET("/v3/posts/{postId}/check_update_availability")
    suspend fun checkPostUpdateAvailability(
        @Path("postId") postId: Long
    ): ResponseWrapper<CheckPostUpdateAvailabilityResponse>

}
