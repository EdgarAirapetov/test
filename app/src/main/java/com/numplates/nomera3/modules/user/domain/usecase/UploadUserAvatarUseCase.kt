package com.numplates.nomera3.modules.user.domain.usecase

import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.baseCore.BaseUseCaseNoSuspend
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.user.data.entity.UploadAvatarResponse
import com.numplates.nomera3.modules.user.data.repository.UserRepository
import io.reactivex.Single
import javax.inject.Inject

class UploadUserAvatarUseCase @Inject constructor(
    private val repository: UserRepository
): BaseUseCaseNoSuspend<UserUploadAvatarParams, Single<ResponseWrapper<UploadAvatarResponse?>>> {

    override fun execute(params: UserUploadAvatarParams): Single<ResponseWrapper<UploadAvatarResponse?>> =
        repository.uploadAvatar(params.imagePath, params.avatarAnimation)

}


class UserUploadAvatarParams(
    val imagePath: String,
    val avatarAnimation: String?
) : DefParams()
