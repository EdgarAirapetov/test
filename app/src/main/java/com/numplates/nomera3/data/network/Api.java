package com.numplates.nomera3.data.network;

import com.numplates.nomera3.data.network.core.ResponseWrapper;
import com.numplates.nomera3.modules.communities.data.entity.CommunityCreatingResult;

import java.util.List;

import io.reactivex.Flowable;
import okhttp3.MultipartBody;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;


public interface Api {

    @GET("/main/cities_suggestion")
    Flowable<ResponseWrapper<List<City>>> citiesSuggestion(@Query("like") String query);

    @GET("/main/cities_suggestion")
    Flowable<ResponseWrapper<List<City>>> citiesSuggestion(
        @Query("in_country_ids") long countryId);

    @GET("/main/cities_suggestion")
    Flowable<ResponseWrapper<List<City>>> citiesSuggestion(
        @Query("in_country_ids") long countryI, @Query("like") String query);

    @Multipart
    @POST("/groups/add_group")
    Flowable<ResponseWrapper<CommunityCreatingResult>> createGroup(
        @Query("name") String name,
        @Query("description") String description,
        @Query("private") int privateGroup,
        @Query("royalty") int onlyAuthor,
        @Part MultipartBody.Part image);

    @POST("/groups/add_group")
    Flowable<ResponseWrapper<CommunityCreatingResult>> createGroup(
        @Query("name") String name,
        @Query("description") String description,
        @Query("private") int privateGroup,
        @Query("royalty") int onlyAuthor);


    @Multipart
    @POST("/groups/set_group_info")
    Flowable<ResponseWrapper<List<EmptyModel>>> updateGroupInfo(
        @Query("group_id") int groupId,
        @Query("name") String name,
        @Query("description") String description,
        @Query("private") int privateGroup,
        @Query("royalty") int onlyAuthor,
        @Part MultipartBody.Part image);


    @POST("/groups/set_group_info")
    Flowable<ResponseWrapper<List<EmptyModel>>> updateGroupInfoDeleteImage(
        @Query("group_id") int groupId,
        @Query("name") String name,
        @Query("description") String description,
        @Query("private") int privateGroup,
        @Query("royalty") int onlyAuthor,
        @Query("image") String image);

    @POST("/groups/set_group_info")
    Flowable<ResponseWrapper<List<EmptyModel>>> updateGroupInfoNoImage(
        @Query("group_id") int groupId,
        @Query("name") String name,
        @Query("description") String description,
        @Query("private") int privateGroup,
        @Query("royalty") int onlyAuthor);

    @GET("/groups/remove_group")
    Flowable<ResponseWrapper<List<EmptyModel>>> removeGroup(
        @Query("group_id") int groupId);

    @GET("/posts/add_post_comment")
    Flowable<ResponseWrapper<CommentEntity>> addPostComment(
        @Query("post_id") long postId,
        @Query("text") String comment,
        @Query("cid") long commentId);

}

