package com.numplates.nomera3.modules.search.domain.usecase

import com.numplates.nomera3.modules.search.data.entity.RecentHashtagEntityResponse
import com.numplates.nomera3.modules.search.data.repository.SearchRepositoryImpl
import com.numplates.nomera3.modules.search.domain.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.search.domain.DefParams
import javax.inject.Inject

class SearchRecentHashTagUseCase @Inject constructor(
        private val repository: SearchRepositoryImpl
) : BaseUseCaseCoroutine<SearchRecentHashTagParams, List<RecentHashtagEntityResponse>> {

    override suspend fun execute(params: SearchRecentHashTagParams,
                                 success: (List<RecentHashtagEntityResponse>) -> Unit,
                                 fail: (Exception) -> Unit) {
        repository.requestRecentHashtags(success, fail)
    }
}

class SearchRecentHashTagParams: DefParams()