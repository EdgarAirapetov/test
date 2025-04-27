package com.numplates.nomera3.modules.rateus.domain

import com.numplates.nomera3.modules.rateus.data.RateUsRepository
import javax.inject.Inject

class HideRateUsPostUseCase @Inject constructor(private val rateUsRepository: RateUsRepository) {
    suspend fun invoke() {
        rateUsRepository.hideRatePost()
    }
}
