package com.numplates.nomera3.modules.screenshot.domain.usecase

import com.numplates.nomera3.modules.screenshot.data.ShareScreenshotRepository
import javax.inject.Inject

class GetShareScreenshotEnabledUseCase @Inject constructor(
    private val repository: ShareScreenshotRepository
) {

    fun invoke(): Boolean = repository.isShareScreenshotEnabled()

}
