package com.numplates.nomera3.modules.moments.show.domain

import com.numplates.nomera3.modules.moments.show.data.MomentsRepository
import javax.inject.Inject

class UpdateUserMomentsStateUseCase @Inject constructor(
    private val momentsRepository: MomentsRepository
) {
    suspend fun invoke(action: MomentsAction, userMomentsStateUpdate: UserMomentsStateUpdateModel) {
        momentsRepository.updateUserMomentsState(action = action, userMomentsStateUpdate = userMomentsStateUpdate)
    }
}
