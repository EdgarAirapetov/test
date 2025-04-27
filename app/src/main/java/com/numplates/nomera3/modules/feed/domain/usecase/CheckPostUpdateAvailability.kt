package com.numplates.nomera3.modules.feed.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.feed.data.entity.CheckPostUpdateAvailabilityResponse
import com.numplates.nomera3.modules.feed.data.repository.PostRepository
import javax.inject.Inject

class CheckPostUpdateAvailability @Inject constructor(
    private val repository: PostRepository
) : BaseUseCaseCoroutine<CheckPostPostParams, CheckPostUpdateAvailabilityResponse> {

    override suspend fun execute(
        params: CheckPostPostParams,
        success: (CheckPostUpdateAvailabilityResponse) -> Unit,
        fail: (Throwable) -> Unit
    ) {
        repository.checkPostUpdateAvailability(params.postId, { response ->
            success(response)
        }, fail)
    }
}

data class CheckPostPostParams(
    val postId: Long
) : DefParams()
