package com.numplates.nomera3.modules.notifications.domain.usecase

import com.numplates.nomera3.modules.notifications.data.repository.NotificationRepository
import com.numplates.nomera3.modules.notifications.domain.BaseUseCase
import com.numplates.nomera3.modules.notifications.domain.DefParams
import io.reactivex.Single
import javax.inject.Inject

private const val ZERO_NEW_NOTIFICATIONS = 0

class GetAmountOfNotSeenNotificationUseCase @Inject constructor(
        private val nRepository: NotificationRepository
) : BaseUseCase<DefParams, Single<Int>> {

    override fun execute(params: DefParams): Single<Int> =
            nRepository.getAmountOfNotSeenNotification()
                    .onErrorReturn { ZERO_NEW_NOTIFICATIONS }

}
