package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.modules.user.data.repository.UserRepository
import javax.inject.Inject

class IsShowTooltipSessionUseCase @Inject constructor(
    private val repository: UserRepository
) {
    fun invoke(key: String): Boolean = repository.isShowTooltipSession(key)
}
