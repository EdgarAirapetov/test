package com.numplates.nomera3.modules.communities.domain.usecase.notifications

import com.numplates.nomera3.modules.communities.data.repository.CommunityRepository
import com.numplates.nomera3.modules.communities.domain.usecase.BaseUseCaseCoroutine

class SubscribeCommunityNotificationsUseCase(
    private val repository: CommunityRepository
): BaseUseCaseCoroutine<CommunityNotificationsUseCaseParams, Boolean> {

    override suspend fun execute(
        params: CommunityNotificationsUseCaseParams,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) {
        repository.subscribeNotifications(params.groupId, success, fail)
    }
}