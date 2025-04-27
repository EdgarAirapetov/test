package com.numplates.nomera3.modules.notifications.domain.usecase

import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.notifications.data.repository.NotificationRepository
import com.numplates.nomera3.modules.notifications.domain.BaseUseCase
import com.numplates.nomera3.modules.notifications.domain.DefParams
import io.reactivex.Single
import javax.inject.Inject

class DeleteAllEventsUseCase @Inject constructor(
        private val nRepository: NotificationRepository
) : BaseUseCase<DefParams, Single<ResponseWrapper<Boolean>>> {

    override fun execute(params: DefParams): Single<ResponseWrapper<Boolean>> =
            nRepository.deleteAllNotification()

}
