package com.numplates.nomera3.modules.screenshot.data

import com.meera.core.preferences.AppSettings
import javax.inject.Inject

class ShareScreenshotRepositoryImpl @Inject constructor(
    private val appSettings: AppSettings
) : ShareScreenshotRepository {

    override suspend fun setShareScreenshotEnabled(enabled: Boolean) {
        appSettings.allowShareScreenshot = enabled
    }

    override fun isShareScreenshotEnabled(): Boolean {
        return appSettings.allowShareScreenshot
    }
}
