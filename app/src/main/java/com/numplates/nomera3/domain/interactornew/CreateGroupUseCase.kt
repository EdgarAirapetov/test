package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.data.network.Api
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.communities.data.entity.CommunityCreatingResult
import io.reactivex.Flowable
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class CreateGroupUseCase(private val repository: Api) {


    fun createGroup(name: String,
                    description: String,
                    privateGroup: Int,
                    royalty: Int,
                    image: String) : Flowable<ResponseWrapper<CommunityCreatingResult>> {

        return if (image.isNotEmpty()) {
            val file = File(image)
            val filePart = MultipartBody.Part.createFormData(
                    "image",
                    file.name,
                    file.asRequestBody("image/*".toMediaTypeOrNull())
//                    RequestBody.create(MediaType.parse("image/*"), file)
            )

            repository.createGroup(name, description, privateGroup, royalty, filePart)
        } else {
            repository.createGroup(name, description, privateGroup, royalty)
        }
    }

}
