package com.numplates.nomera3.modules.communities.ui.viewmodel.list

import com.numplates.nomera3.modules.communities.data.entity.Communities
import com.numplates.nomera3.modules.communities.domain.usecase.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.communities.domain.usecase.GetCommunitiesTopUseCase
import com.numplates.nomera3.modules.communities.domain.usecase.GetCommunitiesUseCaseParams

class CommunitiesListLoader(private val useCase: BaseUseCaseCoroutine<GetCommunitiesUseCaseParams, Communities>) {
    companion object {
        const val DEFAULT_ITEM_COUNT_PER_REQUEST = 40
    }

    var isLoading: Boolean = false
        private set

    var isListEndReached: Boolean = false
        private set

    var itemCountPerRequest: Int = DEFAULT_ITEM_COUNT_PER_REQUEST
        set(value) {
            if (value < 0) {
                field = 0
            } else {
                field = value
            }
        }

    var startIndex: Int = 0
        set(value) {
            if (value < 0) {
                field = 0
            } else {
                field = value
            }
        }

    var loadingStateListener: ((Result<Communities?>) -> Unit)? = null

    suspend fun loadNext() {
        isLoading = true
        useCase.execute(
            params = GetCommunitiesUseCaseParams(startIndex, itemCountPerRequest),
            success = { communities: Communities ->
                val isResponseListEmpty = communities.getList()?.isEmpty() ?: true

                isLoading = false
                isListEndReached = isResponseListEmpty

                communities.isNewList = startIndex == 0

                when {
                    !isResponseListEmpty -> {
                        startIndex += itemCountPerRequest
                        if (useCase is GetCommunitiesTopUseCase) startIndex += 1
                        loadingStateListener?.invoke(Result.success(communities))
                    }
                    isResponseListEmpty && startIndex == 0 -> {
                        loadingStateListener?.invoke(Result.success(null))
                    }
                    isResponseListEmpty -> {
                        loadingStateListener?.invoke(Result.success(communities))
                    }
                }

            },
            fail = { exception: Exception ->
                isLoading = false
                isListEndReached = true

                loadingStateListener?.invoke(Result.failure(exception))
            }
        )
    }

    fun reset() {
        startIndex = 0

        isLoading = false
        isListEndReached = false
    }
}
