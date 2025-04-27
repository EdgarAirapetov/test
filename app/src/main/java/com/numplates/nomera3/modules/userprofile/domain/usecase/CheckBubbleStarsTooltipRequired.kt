package com.numplates.nomera3.modules.userprofile.domain.usecase

import com.meera.core.preferences.AppSettings
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CheckBubbleStarsTooltipRequired @Inject constructor(
    private val appSettings: AppSettings
) {
    fun invoke(): Boolean {
        val daysAfterShown = System.currentTimeMillis() - appSettings.isBubbleStarsTooltipShownDate
        return daysAfterShown > TimeUnit.DAYS.toMillis(DAYS_IN_MONTH)
    }

    companion object {
        const val DAYS_IN_MONTH = 30L
    }
}
