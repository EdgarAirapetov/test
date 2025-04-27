package com.numplates.nomera3.modules.notifications.domain.usecase

import com.meera.db.models.notifications.NotificationEntity
import com.numplates.nomera3.modules.notifications.data.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FlowNotificationsUseCase @Inject constructor(
    private val repository: NotificationRepository
) {

    fun invoke(): Flow<List<NotificationEntity>> {
        return repository.subscribeToNotifications()
    }
}
