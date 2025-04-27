package com.numplates.nomera3.modules.userprofile.domain.usecase

import com.meera.core.preferences.AppSettings
import javax.inject.Inject

class ConfirmBubbleStarsTooltipShown @Inject constructor(
    private val appSettings: AppSettings
) {
    fun invoke() {
        appSettings.isBubbleStarsTooltipShownDate = System.currentTimeMillis()
    }
}
