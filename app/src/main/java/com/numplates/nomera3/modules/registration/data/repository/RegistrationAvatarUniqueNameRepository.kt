package com.numplates.nomera3.modules.registration.data.repository

import com.numplates.nomera3.modules.user.data.entity.UploadAvatarResponse

interface RegistrationAvatarUniqueNameRepository {

    suspend fun uploadAvatar(
        imagePath: String,
        avatarAnimation: String? = null,
        success: (result: UploadAvatarResponse) -> Unit,
        fail: (e: Exception) -> Unit
    )
}