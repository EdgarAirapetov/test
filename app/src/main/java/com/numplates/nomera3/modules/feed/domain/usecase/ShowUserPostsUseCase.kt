package com.numplates.nomera3.modules.feed.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.feed.data.repository.PostRepository
import javax.inject.Inject

class ShowUserPostsUseCase @Inject constructor(
        private val repository: PostRepository
) : BaseUseCaseCoroutine<ShowUserPostsParams, Boolean> {

    override suspend fun execute(params: ShowUserPostsParams,
                                 success: (Boolean) -> Unit,
                                 fail: (Throwable) -> Unit) {
        repository.showUserPosts(params.userId, success, fail)
    }
}

data class ShowUserPostsParams(
        val userId: Long
) : DefParams()
