package com.numplates.nomera3.modules.userprofile.domain.usecase

import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.modules.maps.domain.usecase.ReadLastLocationFromStorageUseCase
import com.numplates.nomera3.modules.userprofile.data.repository.ProfileRepository
import javax.inject.Inject

class UpdateOwnUserProfileUseCase @Inject constructor(
    private val repository: ProfileRepository,
    private val getUserUidUseCase: GetUserUidUseCase,
    private val readLastLocationFromStorageUseCase: ReadLastLocationFromStorageUseCase
) {

    suspend operator fun invoke(withoutSideEffects: Boolean = false) {
        val myId = getUserUidUseCase.invoke()
        val location = readLastLocationFromStorageUseCase.invoke()
        val gpsX = location?.lat?.toFloat()
        val gpsY = location?.lon?.toFloat()
        val profile = repository.requestProfileDbModel(
            userId = myId,
            gpsX = gpsX,
            gpsY = gpsY,
            withoutSideEffects = withoutSideEffects
        )
        repository.updateOwnProfileDb(profile)
    }
}
