package com.numplates.nomera3.modules.userprofile.domain.usecase.tooltip

import com.numplates.nomera3.modules.userprofile.data.repository.ProfileTooltipRepository
import com.numplates.nomera3.presentation.view.utils.apphints.TooltipDuration
import javax.inject.Inject

class IsUniqueNameTooltipShownUseCase @Inject constructor(
    private val profileRepository: ProfileTooltipRepository
) {
    fun invoke() : Boolean {
        val isAppearedMaxTimes =
            profileRepository.getUniqueNameHintShownTimes() < TooltipDuration.DEFAULT_TIMES
        val isShownTooltipSession = profileRepository.isShownTooltipSession()
        return (isAppearedMaxTimes && isShownTooltipSession)
    }
}
