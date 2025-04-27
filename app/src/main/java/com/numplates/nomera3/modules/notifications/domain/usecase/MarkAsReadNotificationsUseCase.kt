package com.numplates.nomera3.modules.notifications.domain.usecase

import com.numplates.nomera3.modules.notifications.data.repository.NotificationRepository
import com.numplates.nomera3.modules.notifications.domain.BaseUseCase
import com.numplates.nomera3.modules.notifications.domain.DefParams
import io.reactivex.Single
import javax.inject.Inject

class MarkAsReadNotificationsUseCase @Inject constructor(
        private val nRepository: NotificationRepository
): BaseUseCase<MarkAsReadNotificationParams, Single<Boolean>> {

    override fun execute(params: MarkAsReadNotificationParams): Single<Boolean>  =
        nRepository.setNotificationAsRead(params.notificationId, params.isGroup)
}

class MarkAsReadNotificationParams (
        val notificationId: String,
        val isGroup: Boolean
): DefParams()
