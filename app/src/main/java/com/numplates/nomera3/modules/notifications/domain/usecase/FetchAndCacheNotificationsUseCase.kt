package com.numplates.nomera3.modules.notifications.domain.usecase

import com.meera.db.models.notifications.NotificationEntity
import com.numplates.nomera3.modules.notifications.data.repository.NotificationRepository
import javax.inject.Inject

class FetchAndCacheNotificationsUseCase @Inject constructor(
    private val repository: NotificationRepository
) {

    suspend fun invoke(milliSecondOffset: Long, limit: Int): List<NotificationEntity> {
        return repository.fetchAndCacheNotifications(milliSecondOffset, limit)
    }
}
