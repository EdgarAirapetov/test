package com.numplates.nomera3.modules.complains.data.api

import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.complains.data.model.AttachMediaDto
import com.numplates.nomera3.modules.complains.data.model.ChatComplaintDto
import com.numplates.nomera3.modules.complains.data.model.ChatComplaintParams
import com.numplates.nomera3.modules.complains.data.model.MomentComplaintDto
import com.numplates.nomera3.modules.complains.data.model.MomentComplaintParams
import com.numplates.nomera3.modules.complains.data.model.UserComplaintDto
import com.numplates.nomera3.modules.complains.data.model.UserComplaintParams
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface ComplaintApi {

    @POST("/v2/complaints")
    suspend fun complainOnUser(@Body params: UserComplaintParams): ResponseWrapper<UserComplaintDto>

    @POST("/v2/complaints")
    suspend fun complainOnChat(@Body params: ChatComplaintParams): ResponseWrapper<ChatComplaintDto>

    @POST("/v2/complaints")
    suspend fun complainOnMoment(@Body params: MomentComplaintParams): ResponseWrapper<MomentComplaintDto>

    /**
     * @param id complaint id, should be in request url
     * @param uploadId id for partial uploading. check this method as an example [ApiUpload.sendPartialUploadData]
     * @param media media file to upload
     */
    @Multipart
    @POST("/v2/complaints/{id}/attach_file")
    suspend fun attachFile(
        @Path("id") id: Int,
        @Part("upload_id") uploadId: RequestBody? = null,
        @Part media: MultipartBody.Part? = null,
    ): ResponseWrapper<AttachMediaDto>
}
