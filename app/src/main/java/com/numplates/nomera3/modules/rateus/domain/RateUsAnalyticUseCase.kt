package com.numplates.nomera3.modules.rateus.domain

import com.numplates.nomera3.modules.rateus.data.RateUsAnalyticRepository
import com.numplates.nomera3.modules.rateus.data.RateUsAnalyticsRating
import javax.inject.Inject

class RateUsAnalyticUseCase @Inject constructor(private val repository: RateUsAnalyticRepository) {
    suspend fun invoke(ratingAnalytics: RateUsAnalyticsRating) {
        repository.rateUs(ratingAnalytics)
    }
}
