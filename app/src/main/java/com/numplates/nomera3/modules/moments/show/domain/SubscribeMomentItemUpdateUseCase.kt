package com.numplates.nomera3.modules.moments.show.domain

import com.numplates.nomera3.modules.moments.show.data.MomentsRepository
import com.numplates.nomera3.modules.moments.show.domain.model.MomentRepositoryEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class SubscribeMomentItemUpdateUseCase @Inject constructor(
    private val momentsRepository: MomentsRepository
) {

    fun invoke(
        lifecycleScope: CoroutineScope,
        onSuccess: (MomentRepositoryEvent) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        lifecycleScope.launch {
            runCatching {
                momentsRepository.getEventStream()
            }.onSuccess {
                it.collect { event -> onSuccess(event) }
            }.onFailure {
                onError(it)
            }
        }
    }
}
