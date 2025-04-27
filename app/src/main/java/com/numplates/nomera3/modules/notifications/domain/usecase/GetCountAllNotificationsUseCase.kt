package com.numplates.nomera3.modules.notifications.domain.usecase

import com.numplates.nomera3.modules.notifications.data.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCountAllNotificationsUseCase @Inject constructor(
    private val repository: NotificationRepository
) {

    fun invoke(): Flow<Int> {
        return repository.getCountAllNotifications()
    }

}
