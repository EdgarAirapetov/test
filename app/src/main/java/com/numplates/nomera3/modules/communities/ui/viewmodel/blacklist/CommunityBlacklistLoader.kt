package com.numplates.nomera3.modules.communities.ui.viewmodel.blacklist

import com.numplates.nomera3.modules.communities.data.entity.CommunityMemberState
import com.numplates.nomera3.modules.communities.data.entity.CommunityMembersEntity
import com.numplates.nomera3.modules.communities.domain.usecase.GetCommunityUsersUseCase
import com.numplates.nomera3.modules.communities.domain.usecase.GetCommunityUsersUseCaseParams

class CommunityBlacklistLoader(
    private val communityId: Int,
    private val useCase: GetCommunityUsersUseCase
) {
    companion object {
        const val DEFAULT_ITEM_COUNT_PER_REQUEST = 100
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

    var loadingStateListener: ((Result<CommunityMembersEntity>) -> Unit)? = null

    suspend fun loadNext() {
        isLoading = true
        useCase.execute(
            params = getUseCaseParametersForBlockedMembers(),
            success = { entity: CommunityMembersEntity? ->
                isLoading = false
                isListEndReached = entity?.moreItems?.let { it <= 0 } ?: true

                if (entity != null) {
                    startIndex = startIndex + itemCountPerRequest - 1
                    loadingStateListener?.invoke(Result.success(entity))
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

    private fun getUseCaseParametersForBlockedMembers(): GetCommunityUsersUseCaseParams {
        return GetCommunityUsersUseCaseParams(
            groupId = communityId,
            startIndex = startIndex,
            quantity = itemCountPerRequest,
            userState = CommunityMemberState.BLOCKED,
            query = null
        )
    }
}