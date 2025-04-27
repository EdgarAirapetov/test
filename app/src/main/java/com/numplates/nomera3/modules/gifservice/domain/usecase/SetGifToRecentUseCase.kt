package com.numplates.nomera3.modules.gifservice.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.gifservice.data.repository.GiphyRepositoryImpl
import javax.inject.Inject

class SetGifToRecentUseCase @Inject constructor(
        private val repository: GiphyRepositoryImpl
) : BaseUseCaseCoroutine<SetGifToRecentParams, Boolean> {

    override suspend fun execute(params: SetGifToRecentParams,
                                 success: (Boolean) -> Unit,
                                 fail: (Throwable) -> Unit) {
        repository.setGifToRecent(
                params.id,
                params.smallUrl,
                params.originalUrl,
                params.originalAspectRatio,
                success,
                fail
        )
    }

}

class SetGifToRecentParams(
    val id: String,
    val smallUrl: String,
    val originalUrl: String,
    val originalAspectRatio: Double
) : DefParams()
