package com.numplates.nomera3.modules.user.domain.usecase

import com.numplates.nomera3.modules.user.data.repository.UserRepository
import javax.inject.Inject

class GetShareProfileLinkUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend fun invoke() = repository.getShareProfileLink()
}
