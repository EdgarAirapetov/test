package com.numplates.nomera3.modules.chat.domain.interactors

import com.numplates.nomera3.modules.chat.domain.usecases.tooltips.IncRecordAudioMessageTooltipWasShownUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.tooltips.IncReleaseButtonSentAudioMessageTooltipWasShownUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.tooltips.IsLetRecordAudioMessageTooltipUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.tooltips.IsReleaseButtonSentAudioMessageTooltipWasShownTimesUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.tooltips.ShouldShowPhoneAbilityTooltipUseCase
import javax.inject.Inject

class ToolTipsInteractor @Inject constructor(
    private val isLetRecordAudioMessageTooltipUseCase: IsLetRecordAudioMessageTooltipUseCase,
    private val incRecordAudioMessageTooltipWasShown: IncRecordAudioMessageTooltipWasShownUseCase,
    private val shouldShowPhoneAbilityTooltip: ShouldShowPhoneAbilityTooltipUseCase,
    private val isRelBtnSentAudioMsgTooltipWasShownTimes: IsReleaseButtonSentAudioMessageTooltipWasShownTimesUseCase,
    private val incReleaseButtonSentAudioMessageTooltipWasShown: IncReleaseButtonSentAudioMessageTooltipWasShownUseCase
) {
    fun isNeedShowRecordAudioMessageTip(timesForTips: Int) = isLetRecordAudioMessageTooltipUseCase.invoke(timesForTips)

    fun isNeedShowPhoneAbilityTip(timesForTips: Int) = shouldShowPhoneAbilityTooltip.invoke(timesForTips)

    fun isNeedShowReleaseButtonSentAudioMessageTip(timesForTips: Int) =
        isRelBtnSentAudioMsgTooltipWasShownTimes.invoke(timesForTips)

    fun recordAudioMessageTipWasShowed(timesForTips: Int) = incRecordAudioMessageTooltipWasShown.invoke(timesForTips)

    fun releaseButtonSentAudioMessageTipWasShowed(timesForTips: Int) =
        incReleaseButtonSentAudioMessageTooltipWasShown.invoke(timesForTips)

}
