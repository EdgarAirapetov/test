package com.numplates.nomera3.modules.services.domain.usecase

import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.modules.search.data.repository.SearchRepository
import javax.inject.Inject

class GetRecentUsersUseCase @Inject constructor(
    private val repository: SearchRepository
) {

    suspend fun invoke(): List<UserSimple> {
        return repository.getRecentUsers().mapNotNull { it.data }
    }

}
