package com.numplates.nomera3.modules.gifservice.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.gifservice.data.entity.GiphyFullResponse
import com.numplates.nomera3.modules.gifservice.data.repository.GiphyRepositoryImpl
import javax.inject.Inject

class GiphySearchUseCase @Inject constructor(
        private val repository: GiphyRepositoryImpl
) : BaseUseCaseCoroutine<GiphySearchParams, GiphyFullResponse> {

    override suspend fun execute(params: GiphySearchParams,
                                 success: (GiphyFullResponse) -> Unit,
                                 fail: (Throwable) -> Unit) {
        repository.search(
                query = params.query,
                limit = params.limit,
                offset = params.offset,
                lang = params.lang,
                success = success,
                fail = fail
        )
    }

}

class GiphySearchParams(
    val query: String,
    val limit: Int,
    val offset: Int,
    val lang: String
) : DefParams()
