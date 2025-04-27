package com.numplates.nomera3.modules.userprofile.domain.usecase

import com.meera.core.preferences.AppSettings
import javax.inject.Inject

class CheckPhotosCounterTooltipRequired @Inject constructor(
    private val appSettings: AppSettings
) {
    fun invoke(): Boolean {
        return appSettings.isPhotosCounterTooltipRequired
    }
}
