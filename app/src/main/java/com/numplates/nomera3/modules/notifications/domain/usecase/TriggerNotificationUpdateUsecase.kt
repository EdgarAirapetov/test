package com.numplates.nomera3.modules.notifications.domain.usecase

import com.numplates.nomera3.modules.notifications.data.repository.NotificationRepository
import javax.inject.Inject

/** Legacy logic wrapped in a usecase. Proper refactor needed */
class TriggerNotificationUpdateUsecase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(notificationId: String) =
        notificationRepository.triggerNotificationCacheUpdate(notificationId)
}
