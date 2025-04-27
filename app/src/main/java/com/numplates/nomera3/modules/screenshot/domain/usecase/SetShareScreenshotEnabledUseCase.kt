package com.numplates.nomera3.modules.screenshot.domain.usecase

import com.numplates.nomera3.modules.screenshot.data.ShareScreenshotRepository
import javax.inject.Inject

class SetShareScreenshotEnabledUseCase @Inject constructor(
    private val repository: ShareScreenshotRepository
) {

    suspend fun invoke(isEnabled: Boolean) {
        repository.setShareScreenshotEnabled(isEnabled)
    }

}
