package com.numplates.nomera3.modules.feed.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.feed.data.repository.PostRepository
import javax.inject.Inject

class UnsubscribePostUseCase @Inject constructor(
        private val repository: PostRepository
) : BaseUseCaseCoroutine<UnsubscribePostParams, Boolean> {

    override suspend fun execute(params: UnsubscribePostParams,
                                 success: (Boolean) -> Unit,
                                 fail: (Throwable) -> Unit) {
        repository.unsubscribePost(params.id, success, fail)
    }
}

data class UnsubscribePostParams(
        val id: Long
) : DefParams()
