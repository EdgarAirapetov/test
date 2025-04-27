package com.numplates.nomera3.modules.userprofile.domain.usecase.tooltip

import com.numplates.nomera3.modules.userprofile.data.repository.ProfileTooltipRepository
import javax.inject.Inject

class GetIsCreateAvatarUserInfoHintShownUseCase @Inject constructor(
    private val userRepository: ProfileTooltipRepository
) {
    fun invoke() = userRepository.getIsCreateAvatarUserInfoHintShown()
}
