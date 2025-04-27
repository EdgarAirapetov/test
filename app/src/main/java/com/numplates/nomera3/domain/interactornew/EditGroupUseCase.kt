package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.data.network.Api
import com.numplates.nomera3.data.network.EmptyModel
import com.numplates.nomera3.data.network.core.ResponseWrapper
import io.reactivex.Flowable
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class EditGroupUseCase(private val repository: Api) {

    fun editGroup(
        groupId: Int,
        name: String,
        description: String,
        privateGroup: Int,
        royalty: Int,
        image: String,
        isDeleteGroupAvatar: Boolean
    ): Flowable<ResponseWrapper<List<EmptyModel>>> {
        return if (image.isEmpty()) {
            if (isDeleteGroupAvatar) {
                repository.updateGroupInfoDeleteImage(groupId, name, description, privateGroup, royalty, image)
            } else {
                repository.updateGroupInfoNoImage(groupId, name, description, privateGroup, royalty)
            }
        } else {
            val file = File(image)
            val filePart = MultipartBody.Part.createFormData(
                "image",
                file.name,
                file.asRequestBody("image/*".toMediaTypeOrNull())
            )
            repository.updateGroupInfo(groupId, name, description, privateGroup, royalty, filePart)
        }
    }

}
