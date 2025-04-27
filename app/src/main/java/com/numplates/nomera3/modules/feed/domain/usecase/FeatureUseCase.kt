package com.numplates.nomera3.modules.feed.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.feed.data.repository.PostRepository
import javax.inject.Inject

class FeatureUseCase @Inject constructor(
        private val repository: PostRepository
) : BaseUseCaseCoroutine<FeatureParams, Boolean> {

    override suspend fun execute(params: FeatureParams,
                                 success: (Boolean) -> Unit,
                                 fail: (Throwable) -> Unit) {
        repository.actionOnFeature(params.featureId, params.hide, success, fail)
    }
}

data class FeatureParams(
        val featureId: Long,
        val hide: Boolean = true
) : DefParams()
