package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.modules.usersettings.domain.repository.UserPersonalInfoRepository
import javax.inject.Inject

class SetUserDateOfBirthUseCase @Inject constructor(
    private val userPersonalInfoRepository: UserPersonalInfoRepository
) {
    fun invoke(dateOfBirth: Int) =
        userPersonalInfoRepository.setPreferencesBirthdayDate(dateOfBirth)
}
