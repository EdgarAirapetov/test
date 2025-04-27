package com.numplates.nomera3.modules.userprofile.domain.usecase.tooltip

import com.numplates.nomera3.modules.userprofile.data.repository.ProfileTooltipRepository
import com.numplates.nomera3.presentation.view.utils.apphints.TooltipDuration
import javax.inject.Inject

class IncrementUniqueNameShownTooltipUseCase @Inject constructor(
    private val repository: ProfileTooltipRepository
) {
    fun invoke() {
        val shownTimes = repository.getUniqueNameHintShownTimes()
        if (shownTimes > TooltipDuration.DEFAULT_TIMES) return
        repository.setUniqueNameShownTimes(shownTimes + 1)
        repository.setShownTooltipSession()

    }
}
