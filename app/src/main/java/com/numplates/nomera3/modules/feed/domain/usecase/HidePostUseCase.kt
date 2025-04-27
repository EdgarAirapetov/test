package com.numplates.nomera3.modules.feed.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.feed.data.repository.PostRepository
import javax.inject.Inject

class HidePostUseCase @Inject constructor(
        private val repository: PostRepository
) : BaseUseCaseCoroutine<HidePostParams, Boolean> {

    override suspend fun execute(params: HidePostParams,
                                 success: (Boolean) -> Unit,
                                 fail: (Throwable) -> Unit) {
        repository.hidePost(params.id, success, fail)
    }
}

data class HidePostParams(
        val id: Long
) : DefParams()
