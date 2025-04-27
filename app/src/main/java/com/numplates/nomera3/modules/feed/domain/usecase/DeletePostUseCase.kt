package com.numplates.nomera3.modules.feed.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.feed.data.repository.PostRepository
import javax.inject.Inject

class DeletePostUseCase @Inject constructor(
    private val repository: PostRepository
) : BaseUseCaseCoroutine<DeletePostParams, Boolean> {

    override suspend fun execute(params: DeletePostParams,
                                 success: (Boolean) -> Unit,
                                 fail: (Throwable) -> Unit) {
        repository.deletePost(params.id, success, fail)
    }
}

data class DeletePostParams(
    val id: Long
) : DefParams()
