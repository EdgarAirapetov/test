package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.modules.user.data.repository.UserRepository
import javax.inject.Inject

class GetUserBirthdayDialogShownFlowUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend fun invoke() = repository.getIsNeedShowBirthdayDialogFlow()
}
