package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.newroads.data.PostsRepository
import javax.inject.Inject

class CheckPostMarkedAsSensitiveUseCase @Inject constructor(
        private val repository: PostsRepository
) {
    fun invoke(
        params: CheckPostMarkedAsSensitiveParams
    ): Boolean {
        return repository.isMarkedAsNonSensitivePost(postId = params.postId)
    }
}

data class CheckPostMarkedAsSensitiveParams(
    val postId: Long?
) : DefParams()
