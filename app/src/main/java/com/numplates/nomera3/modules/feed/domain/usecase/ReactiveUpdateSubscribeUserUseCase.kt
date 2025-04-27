package com.numplates.nomera3.modules.feed.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.feed.data.repository.PostRepository
import javax.inject.Inject

class ReactiveUpdateSubscribeUserUseCase @Inject constructor(
    private val repository: PostRepository
) : BaseUseCaseCoroutine<UpdateSubscriptionUserParams, Boolean> {

    override suspend fun execute(params: UpdateSubscriptionUserParams,
                                 success: (Boolean) -> Unit,
                                 fail: (Throwable) -> Unit) {
        repository.updateReactiveUserSubscription(
            postId = params.postId,
            userId = params.userId,
            isSubscribed = params.isSubscribed,
            isBlocked = params.isBlocked,
            needToHideFollowButton = params.needToHideFollowButton,
            success = success,
            fail = fail
        )
    }
}

data class UpdateSubscriptionUserParams(
    val postId: Long? = null,
    val userId: Long = 0,
    val isSubscribed: Boolean,
    val needToHideFollowButton: Boolean,
    val isBlocked: Boolean
) : DefParams()

