package com.numplates.nomera3.modules.feed.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseNoSuspend
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.feed.data.entity.FeedUpdateEvent
import com.numplates.nomera3.modules.feed.data.repository.PostRepository
import io.reactivex.Observable
import javax.inject.Inject

class GetFeedStateUseCase @Inject constructor(
    private val repository: PostRepository
) : BaseUseCaseNoSuspend<DefParams, Observable<FeedUpdateEvent>> {
    override fun execute(params: DefParams) = repository.getFeedStateObserver()
}