package com.numplates.nomera3.modules.userprofile.domain.interactor

import com.numplates.nomera3.modules.userprofile.domain.usecase.tooltip.GetIsCreateAvatarUserInfoHintShownUseCase
import com.numplates.nomera3.modules.userprofile.domain.usecase.tooltip.IsNeedToShowReferralTooltipUseCase
import com.numplates.nomera3.modules.userprofile.domain.usecase.tooltip.SetUserProfileReferralTipShowedUseCase
import com.numplates.nomera3.modules.userprofile.domain.usecase.tooltip.IncrementUniqueNameShownTooltipUseCase
import com.numplates.nomera3.modules.userprofile.domain.usecase.tooltip.IsUniqueNameTooltipShownUseCase
import com.numplates.nomera3.modules.userprofile.domain.usecase.tooltip.SetAvatarUserInfoTooltipShownUseCase
import com.numplates.nomera3.presentation.view.utils.apphints.TooltipDuration.DEFAULT_TIMES
import javax.inject.Inject


class TooltipProfileInteractor @Inject constructor(
    private val getIsCreateAvatarUserInfoHintShownUseCase: GetIsCreateAvatarUserInfoHintShownUseCase,
    private val isNeedToShowReferralTooltipUseCase: IsNeedToShowReferralTooltipUseCase,
    private val isUniqueNameTooltipShownUseCase: IsUniqueNameTooltipShownUseCase,
    private val setAvatarUserInfoTooltipShownUseCase: SetAvatarUserInfoTooltipShownUseCase,
    private val incrementUniqueNameShownTooltipUseCase: IncrementUniqueNameShownTooltipUseCase,
    private val setUserProfileReferralTipShowedUseCase: SetUserProfileReferralTipShowedUseCase,
) {

    fun isCreateAvatarUserInfoHintShown() = getIsCreateAvatarUserInfoHintShownUseCase.invoke()

    fun createAvatarUserInfoTooltipWasShown() = setAvatarUserInfoTooltipShownUseCase.invoke()

    fun isUniqueNameTooltipWasShownTimes() = isUniqueNameTooltipShownUseCase.invoke()

    fun incUniqueNameTooltipWasShown() = incrementUniqueNameShownTooltipUseCase.invoke()

    fun isNeedToShowReferralTooltip() = isNeedToShowReferralTooltipUseCase.invoke(DEFAULT_TIMES)

    fun referralToolTipShowed() = setUserProfileReferralTipShowedUseCase.invoke()
}
