package com.numplates.nomera3.modules.screenshot.data

interface ShareScreenshotRepository {

    suspend fun setShareScreenshotEnabled(enabled: Boolean)

    fun isShareScreenshotEnabled(): Boolean

}
