package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.App.Companion.IS_MOCKED_DATA
import com.numplates.nomera3.modules.user.data.repository.UserRepository
import javax.inject.Inject

private const val USER_CHEL2_ID = 3047220L

class GetUserUidUseCase @Inject constructor(
    private val repository: UserRepository
) {
    fun invoke() = if (IS_MOCKED_DATA) {
        USER_CHEL2_ID
    } else {
        repository.getUserUid()
    }
}
