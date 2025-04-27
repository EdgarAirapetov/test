package com.numplates.nomera3.modules.registration.data.repository

import com.meera.core.preferences.AppSettings
import com.numplates.nomera3.modules.registration.data.AvatarNotUploadedException
import com.numplates.nomera3.modules.registration.data.RegistrationApi
import com.numplates.nomera3.modules.user.data.entity.UploadAvatarResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class RegistrationAvatarUniqueNameRepositoryImpl(
    private val api: RegistrationApi,
    private val appSettings: AppSettings
): RegistrationAvatarUniqueNameRepository {


    override suspend fun uploadAvatar(
        imagePath: String,
        avatarAnimation: String?,
        success: (result: UploadAvatarResponse) -> Unit,
        fail: (e: Exception) -> Unit
    ) {
        try {
            val imageFile = File(imagePath)
            val imageRequestFile = imageFile.asRequestBody(MEDIA_TYPE.toMediaTypeOrNull())
            val image = MultipartBody.Part.createFormData(FILE, imageFile.name, imageRequestFile)
            val avatarAnimationParam : RequestBody? = avatarAnimation?.toRequestBody(MultipartBody.FORM)

            val upload = api.uploadAvatar(image, avatarAnimationParam).data
            if (upload != null) {
                upload.avatarSmall?.let { avatarSmall -> appSettings.avatar = (avatarSmall) }
                success(upload)
            } else {
                fail(AvatarNotUploadedException())
            }
        } catch (e: Exception) {
            fail(e)
        }
    }

    companion object {
        private const val MEDIA_TYPE = "image/*"
        private const val FILE = "file"
    }
}
