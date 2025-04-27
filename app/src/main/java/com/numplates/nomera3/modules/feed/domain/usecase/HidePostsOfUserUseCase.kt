package com.numplates.nomera3.modules.feed.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.feed.data.repository.PostRepository
import javax.inject.Inject

class HidePostsOfUserUseCase @Inject constructor(
        private val repository: PostRepository
) : BaseUseCaseCoroutine<HidePostsOfUserParams, Boolean> {

    override suspend fun execute(params: HidePostsOfUserParams,
                                 success: (Boolean) -> Unit,
                                 fail: (Throwable) -> Unit) {
        repository.hideUserPosts(params.userId, success, fail)
    }
}

data class HidePostsOfUserParams(
        val userId: Long
) : DefParams()
