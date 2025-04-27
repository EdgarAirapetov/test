package com.numplates.nomera3.modules.feed.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.feed.data.repository.PostRepository
import javax.inject.Inject

class ReactiveUpdateSubscribePostUseCase @Inject constructor(
    private val repository: PostRepository
) : BaseUseCaseCoroutine<UpdateSubscriptionPostParams, Boolean> {

    override suspend fun execute(params: UpdateSubscriptionPostParams,
                                 success: (Boolean) -> Unit,
                                 fail: (Throwable) -> Unit) {
        repository.updateReactivePostSubscription(
            postId = params.postId,
            isSubscribed = params.isSubscribed,
            success = success,
            fail = fail
        )
    }
}

data class UpdateSubscriptionPostParams(
    val postId: Long = 0,
    val isSubscribed: Boolean
) : DefParams()

