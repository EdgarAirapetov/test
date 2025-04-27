package com.numplates.nomera3.modules.notifications.domain.usecase

import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.notifications.data.repository.NotificationRepository
import com.numplates.nomera3.modules.notifications.domain.BaseUseCase
import com.numplates.nomera3.modules.notifications.domain.DefParams
import io.reactivex.Single
import javax.inject.Inject

class SetGroupOfNotificationAsSeen @Inject constructor(
        private val nRepository: NotificationRepository
): BaseUseCase<GroupOfNotificationParams, Single<ResponseWrapper<Boolean>>> {

    override fun execute(params: GroupOfNotificationParams): Single<ResponseWrapper<Boolean>> =
            nRepository.setGroupOfNotificationAsSeen(params.notifications)
}

class GroupOfNotificationParams (
        val notifications: List<NotificationBodyItem>
): DefParams()

data class NotificationBodyItem (
        val id: String,
        val is_group: Boolean
)
