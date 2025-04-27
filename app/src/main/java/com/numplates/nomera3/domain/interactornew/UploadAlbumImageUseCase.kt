package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.data.network.ApiHiWay
import com.numplates.nomera3.data.network.core.ResponseWrapper
import io.reactivex.Flowable
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class UploadAlbumImageUseCase(private val repository: ApiHiWay?) {

    fun uploadAlbumImage(imagePath: String): Flowable<ResponseWrapper<Any>>? {
        val imageFile = File(imagePath)
        val imageRequestFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val image = MultipartBody.Part.createFormData("file", imageFile.name, imageRequestFile)
        return repository?.uploadImageToAlbum(image)
    }

}
