package com.numplates.nomera3.modules.userprofile.domain.usecase

import com.numplates.nomera3.modules.userprofile.data.repository.ProfileRepository
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserProfileModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveLocalOwnUserProfileModelUseCase @Inject constructor(
    private val repository: ProfileRepository
) {

    fun invoke(): Flow<UserProfileModel> = repository.getOwnProfileModelFlow()

}
