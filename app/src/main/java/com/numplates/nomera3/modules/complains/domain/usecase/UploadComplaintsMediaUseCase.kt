package com.numplates.nomera3.modules.complains.domain.usecase

import com.numplates.nomera3.domain.interactornew.TEXT_PLANE_NAME
import com.numplates.nomera3.modules.complains.data.api.ComplaintApi
import com.numplates.nomera3.modules.fileuploads.data.model.PartialUploadSourceType
import com.numplates.nomera3.modules.fileuploads.domain.FileUploadRepository
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

private const val VIDEO_MP4_NAME = "video/mp4"
private const val IMAGE_NAME = "image/*"

class UploadComplaintsMediaUseCase @Inject constructor(
    private val fileUploadRepository: FileUploadRepository,
    private val complaintApi: ComplaintApi,
) {

    suspend fun invoke(complaintId: Int, imagePath: String?, videoPath: String?) {
        val mediaPath = imagePath ?: videoPath ?: return
        val mediaType = if (imagePath != null) {
            IMAGE_NAME.toMediaTypeOrNull()
        } else {
            VIDEO_MP4_NAME.toMediaTypeOrNull()
        }
        complaintApi.attachFile(
            id = complaintId,
            uploadId = getUploadId(mediaPath, mediaType)
        )
    }

    private suspend fun getUploadId(mediaPath: String, mediaType: MediaType?): RequestBody {
        return fileUploadRepository.partialFileUpload(
            fileToUpload = File(mediaPath),
            mediaType = mediaType,
            sourceType = PartialUploadSourceType.CHAT
        ).toRequestBody(TEXT_PLANE_NAME.toMediaTypeOrNull())
    }
}
