package com.numplates.nomera3.modules.feed.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.feed.data.repository.PostRepository
import javax.inject.Inject

class ComplainPostUseCase @Inject constructor(
        private val repository: PostRepository
) : BaseUseCaseCoroutine<ComplainPostParams, Boolean> {

    override suspend fun execute(params: ComplainPostParams,
                                 success: (Boolean) -> Unit,
                                 fail: (Throwable) -> Unit) {
        repository.postComplain(params.postId, success, fail)
    }
}

data class ComplainPostParams(
        val postId: Long
) : DefParams()
