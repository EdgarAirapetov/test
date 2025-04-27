package com.numplates.nomera3.modules.feed.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.feed.data.repository.PostRepository
import com.numplates.nomera3.modules.feed.domain.model.PostModelEntity
import javax.inject.Inject

class GetPostUseCase @Inject constructor(
    private val repository: PostRepository
) : BaseUseCaseCoroutine<GetPostParams, PostModelEntity> {

    override suspend fun execute(
        params: GetPostParams,
        success: (PostModelEntity) -> Unit,
        fail: (Throwable) -> Unit
    ) {
        repository.getPost(
            params.id,
            success,
            fail
        )
    }
}

data class GetPostParams(
    val id: Long
) : DefParams()
