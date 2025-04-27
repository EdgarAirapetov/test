package com.numplates.nomera3.modules.search.domain.usecase

import com.numplates.nomera3.modules.search.data.entity.RecentUserEntityResponse
import com.numplates.nomera3.modules.search.data.repository.SearchRepositoryImpl
import com.numplates.nomera3.modules.search.domain.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.search.domain.DefParams
import javax.inject.Inject

class SearchRecentUsersUseCase @Inject constructor(
        private val repository: SearchRepositoryImpl
) : BaseUseCaseCoroutine<SearchRecentUserParams, List<RecentUserEntityResponse>> {

    override suspend fun execute(params: SearchRecentUserParams,
                                 success: (List<RecentUserEntityResponse>) -> Unit,
                                 fail: (Exception) -> Unit) {
        repository.requestRecentUsers(success, fail)
    }

}

class SearchRecentUserParams: DefParams()