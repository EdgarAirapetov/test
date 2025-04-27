package com.numplates.nomera3.modules.gifservice.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.gifservice.data.entity.GiphyItemResponse
import com.numplates.nomera3.modules.gifservice.data.repository.GiphyRepositoryImpl
import javax.inject.Inject

class GetGiphyTrendingUseCase @Inject constructor(
        private val repository: GiphyRepositoryImpl
) : BaseUseCaseCoroutine<GetGiphyTrendingParams, List<GiphyItemResponse?>> {

    override suspend fun execute(params: GetGiphyTrendingParams,
                                 success: (List<GiphyItemResponse?>) -> Unit,
                                 fail: (Throwable) -> Unit) {
        repository.getTrending(params.limit, params.offset, success, fail)
    }

}

class GetGiphyTrendingParams(
        val limit: Int,
        val offset: Int
): DefParams()
