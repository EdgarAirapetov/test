package com.numplates.nomera3.modules.notifications.domain.usecase

import com.numplates.nomera3.modules.notifications.data.repository.NotificationRepository
import javax.inject.Inject

class ConvertHeaderActionUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {

    suspend fun invoke() {
        notificationRepository.convertHeaderAction()
    }
}
