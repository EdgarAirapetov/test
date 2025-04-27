package com.numplates.nomera3.modules.registration.domain

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.registration.data.repository.RegistrationAvatarUniqueNameRepository
import com.numplates.nomera3.modules.user.data.entity.UploadAvatarResponse
import javax.inject.Inject

class UploadAvatarUseCase @Inject constructor(
    private val repository: RegistrationAvatarUniqueNameRepository
): BaseUseCaseCoroutine<UploadAvatarParams, UploadAvatarResponse> {
    override suspend fun execute(
        params: UploadAvatarParams,
        success: (UploadAvatarResponse) -> Unit,
        fail: (Throwable) -> Unit
    ) {
        repository.uploadAvatar(
            imagePath = params.imagePath,
            avatarAnimation = params.avatarAnimation,
            success = success,
            fail = fail
        )
    }
}

data class UploadAvatarParams(val imagePath: String,
                              val avatarAnimation: String?): DefParams()
