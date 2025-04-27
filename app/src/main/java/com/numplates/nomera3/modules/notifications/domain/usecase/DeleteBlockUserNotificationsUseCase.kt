package com.numplates.nomera3.modules.notifications.domain.usecase

import com.numplates.nomera3.modules.notifications.data.repository.NotificationRepository
import javax.inject.Inject

class DeleteBlockUserNotificationsUseCase @Inject constructor(
    private val repository: NotificationRepository
) {

    suspend fun invoke() {
        repository.deleteBlockUserNotifications()
    }

}
