package com.numplates.nomera3.domain.interactornew

import android.net.Uri
import com.numplates.nomera3.data.network.ApiMain
import com.numplates.nomera3.data.network.core.ResponseWrapper
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

class UploadMediaToGalleryUseCase @Inject constructor(
    private val api: ApiMain
) {

    suspend fun uploadMediaToGallery(uriList: List<Uri>): ResponseWrapper<Any> {
        val mediaList = uriList
            .mapNotNull { it.path }
            .mapIndexed { i, path ->
                val imageFile = File(path)
                val imageRequestFile = imageFile.asRequestBody(IMAGE_MEDIA_TYPE.toMediaTypeOrNull())
                MultipartBody.Part.createFormData(
                    "$ITEM_NAME${i + 1}",
                    imageFile.name,
                    imageRequestFile,
                )
            }

        return api.uploadMediaToGallery(
            item_1 = mediaList.takeIf { it.isNotEmpty() }?.get(0),
            item_2 = mediaList.takeIf { it.size > 1 }?.get(1),
            item_3 = mediaList.takeIf { it.size > 2 }?.get(2),
            item_4 = mediaList.takeIf { it.size > 3 }?.get(3),
            item_5 = mediaList.takeIf { it.size > 4 }?.get(4),
        )
    }

    companion object {
        private const val IMAGE_MEDIA_TYPE = "image/jpeg"
        private const val ITEM_NAME = "item_"
    }
}
