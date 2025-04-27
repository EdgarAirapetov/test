package com.numplates.nomera3.modules.moments.show.domain

import com.numplates.nomera3.modules.moments.show.data.MomentsRepository
import javax.inject.Inject

class NotifyNewMomentCreatedUseCase @Inject constructor(
    private val momentsRepository: MomentsRepository
) {
    fun invoke() = momentsRepository.newMomentCreated()
}
