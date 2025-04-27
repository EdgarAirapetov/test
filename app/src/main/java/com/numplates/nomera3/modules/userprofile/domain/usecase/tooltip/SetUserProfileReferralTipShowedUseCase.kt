package com.numplates.nomera3.modules.userprofile.domain.usecase.tooltip

import com.numplates.nomera3.modules.userprofile.data.repository.ProfileTooltipRepository
import com.numplates.nomera3.presentation.view.utils.apphints.TooltipDuration
import javax.inject.Inject

class SetUserProfileReferralTipShowedUseCase  @Inject constructor(
    private val userRepository: ProfileTooltipRepository
){
    fun invoke() {
        val shownTimes = userRepository.getIsCreateUserInfoReferralToolTipShownTimes()
        if (shownTimes > TooltipDuration.DEFAULT_TIMES) return
        userRepository.setUserInfoReferralToolTipShowed(shownTimes)
    }
}
