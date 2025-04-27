package com.numplates.nomera3.domain.interactornew

import com.meera.core.extensions.empty
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.fileuploads.data.api.ApiUpload
import com.numplates.nomera3.modules.upload.domain.usecase.post.CompressImageUseCase
import com.numplates.nomera3.presentation.upload.UploadPostWorker
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

class UploadGroupChatAvatarUseCase @Inject constructor(
    private val repository: ApiUpload,
    private val compressImageUseCase: CompressImageUseCase
) {

    suspend fun uploadGroupChatAvatar(roomId: Long, imagePath: String): ResponseWrapper<Any> {
        val compressedImage = processImage(imagePath) ?: String.empty()
        val imageFile = File(compressedImage)
        val imageRequestFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val image = MultipartBody.Part.createFormData("file", imageFile.name, imageRequestFile)
        return repository.uploadGroupChatAvatar(roomId, image)
    }

    private suspend fun processImage(image: String?): String? {
        if (image == null || image.endsWith(UploadPostWorker.GIF_EXT)) return image
        return try {
            val compressedFile = compressImageUseCase.invoke(image)
            compressedFile.path ?: image
        } catch (e: Exception) {
            e.printStackTrace()
            image
        }
    }
}
