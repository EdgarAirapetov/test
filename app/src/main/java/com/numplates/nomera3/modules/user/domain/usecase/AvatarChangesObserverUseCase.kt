package com.numplates.nomera3.modules.user.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseNoSuspend
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.user.data.entity.UploadAvatarResponse
import com.numplates.nomera3.modules.user.data.repository.UserRepositoryImpl
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class AvatarChangesObserverUseCase @Inject constructor(
    private val repository: UserRepositoryImpl
) : BaseUseCaseNoSuspend<AvatarObserverParams, PublishSubject<UploadAvatarResponse>> {

    override fun execute(params: AvatarObserverParams): PublishSubject<UploadAvatarResponse> {
        return repository.getUserAvatarObserver()
    }
}

class AvatarObserverParams: DefParams()