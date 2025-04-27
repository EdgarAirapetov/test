package com.numplates.nomera3.modules.search.domain.usecase

import com.numplates.nomera3.modules.search.data.entity.RecentGroupEntityResponse
import com.numplates.nomera3.modules.search.data.repository.SearchRepositoryImpl
import com.numplates.nomera3.modules.search.domain.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.search.domain.DefParams
import javax.inject.Inject

class SearchRecentGroupsUseCase @Inject constructor(
        private val repository: SearchRepositoryImpl
) : BaseUseCaseCoroutine<SearchRecentGroupsParams, List<RecentGroupEntityResponse>> {

    override suspend fun execute(params: SearchRecentGroupsParams,
                                 success: (List<RecentGroupEntityResponse>) -> Unit,
                                 fail: (Exception) -> Unit) {
        repository.requestRecentGroups(success, fail)
    }
}

class SearchRecentGroupsParams: DefParams()