package com.numplates.nomera3.modules.feed.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.feed.data.repository.PostRepository
import javax.inject.Inject

class SubscribePostUseCase @Inject constructor(
        private val repository: PostRepository
) : BaseUseCaseCoroutine<SubscribePostParams, Boolean> {

    override suspend fun execute(params: SubscribePostParams,
                                 success: (Boolean) -> Unit,
                                 fail: (Throwable) -> Unit) {
        repository.subscribePost(params.id, success, fail)
    }
}

data class SubscribePostParams(
        val id: Long = 0
) : DefParams()
