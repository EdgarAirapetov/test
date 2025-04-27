package com.numplates.nomera3.modules.userprofile.domain.usecase.tooltip

import com.numplates.nomera3.modules.userprofile.data.repository.ProfileTooltipRepository
import javax.inject.Inject

class SetAvatarUserInfoTooltipShownUseCase @Inject constructor(
    private val profileRepository: ProfileTooltipRepository
) {
    fun invoke() =
        profileRepository.setCreateAvatarTooltipShown()
}
