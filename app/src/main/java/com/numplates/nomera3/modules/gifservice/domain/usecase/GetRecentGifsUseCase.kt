package com.numplates.nomera3.modules.gifservice.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.meera.db.models.RecentGifEntity
import com.numplates.nomera3.modules.gifservice.data.repository.GiphyRepositoryImpl
import javax.inject.Inject

class GetRecentGifsUseCase @Inject constructor(
        private val repository: GiphyRepositoryImpl
) : BaseUseCaseCoroutine<GetRecentGifsParams, List<RecentGifEntity>> {

    override suspend fun execute(params: GetRecentGifsParams,
                                 success: (List<RecentGifEntity>) -> Unit,
                                 fail: (Throwable) -> Unit) {
        repository.getRecentGifs(success, fail)
    }

}

class GetRecentGifsParams: DefParams()
