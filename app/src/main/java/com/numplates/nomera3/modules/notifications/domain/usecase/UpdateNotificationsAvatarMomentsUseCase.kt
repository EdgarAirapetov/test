package com.numplates.nomera3.modules.notifications.domain.usecase

import com.meera.db.models.notifications.NotificationEntity
import com.numplates.nomera3.modules.moments.show.domain.UserMomentsStateUpdateModel
import com.numplates.nomera3.modules.notifications.data.repository.NotificationRepository
import javax.inject.Inject

class UpdateNotificationsAvatarMomentsUseCase @Inject constructor(
    private val repository: NotificationRepository
) {

    suspend fun invoke(momentsState: UserMomentsStateUpdateModel): List<NotificationEntity> {
        return repository.updateNotificationsAvatarMomentsState(momentsState)
    }

}
