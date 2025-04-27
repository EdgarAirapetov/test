package com.numplates.nomera3.modules.notifications.domain.usecase

import com.numplates.nomera3.modules.notifications.data.repository.NotificationRepository
import com.numplates.nomera3.modules.notifications.domain.BaseUseCase
import com.numplates.nomera3.modules.notifications.domain.DefParams
import io.reactivex.Single
import javax.inject.Inject

class MarkAsReadNotificationByUserIdUseCase @Inject constructor(
        private val repository: NotificationRepository
): BaseUseCase<MarkAsReadNotificationByUserIdParams, Single<Boolean>> {

    override fun execute(params: MarkAsReadNotificationByUserIdParams): Single<Boolean> {
        return repository.setNotificationAsReadByUserId(params.userId, params.notificationType)
    }
}

class MarkAsReadNotificationByUserIdParams(
        val userId: Long,
        val notificationType: String
): DefParams()