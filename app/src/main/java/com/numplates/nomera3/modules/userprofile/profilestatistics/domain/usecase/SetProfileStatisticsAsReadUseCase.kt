package com.numplates.nomera3.modules.userprofile.profilestatistics.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutineWithoutParams
import com.numplates.nomera3.modules.user.data.repository.UserRepository
import java.lang.Exception
import javax.inject.Inject

class SetProfileStatisticsAsReadUseCase @Inject constructor(
    private val userRepository: UserRepository
) : BaseUseCaseCoroutineWithoutParams<Boolean> {

    override suspend fun execute(success: (Boolean) -> Unit, fail: (Exception) -> Unit) {
        userRepository.setProfileStatisticsAsRead(success, fail)
    }
}