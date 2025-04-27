package com.numplates.nomera3.modules.feed.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseNoSuspend
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.feed.data.entity.FeedUpdateEvent
import com.numplates.nomera3.modules.feed.data.repository.PostRepository
import javax.inject.Inject

class ForceUpdatePostUseCase @Inject constructor(
    private val repository: PostRepository
) : BaseUseCaseNoSuspend<UpdatePostParams, Boolean> {

    override fun execute(params: UpdatePostParams): Boolean {
        when (val event = params.updateEvent) {
            is FeedUpdateEvent.FeedUpdateAll -> repository.refreshPostById(
                postId = event.postId,
                success = {},
                fail = {}
            )
            is FeedUpdateEvent.FeedUpdatePayload -> repository.refreshPost(event)
            is FeedUpdateEvent.FeedUpdateMoments -> repository.refreshMoments(event)
            is FeedUpdateEvent.FeedUpdatePostComments -> repository.refreshPostComments(event)
            else -> Unit
        }
        return true
    }
}

class UpdatePostParams(
    var updateEvent: FeedUpdateEvent
): DefParams()
