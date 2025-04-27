package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.modules.userprofile.data.repository.ProfileTooltipRepository
import javax.inject.Inject

class IsCreateAvatarUserPersonalInfoHintShownUseCase @Inject constructor(
    private val userRepository: ProfileTooltipRepository
) {

    fun invoke() = userRepository.isCreateAvatarUserPersonalInfoHintShown()
}
