package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.newroads.data.PostsRepository
import javax.inject.Inject

class MarkPostAsNotSensitiveForUserUseCase @Inject constructor(
    private val repository: PostsRepository
) {
    fun invoke(params: MarkPostAsNotSensitiveForUserParams) {
        return repository.markPostAsNotSensitiveForUser(
            postId = params.postId,
            parentPostId = params.parentPostId
        )
    }
}

data class MarkPostAsNotSensitiveForUserParams(
    val postId: Long?,
    val parentPostId: Long?
) : DefParams()
