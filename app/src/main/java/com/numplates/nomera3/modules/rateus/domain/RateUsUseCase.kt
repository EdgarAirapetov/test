package com.numplates.nomera3.modules.rateus.domain

import com.numplates.nomera3.modules.rateus.data.RateUsRepository
import javax.inject.Inject

class RateUsUseCase @Inject constructor(private val repository: RateUsRepository) {
    suspend fun invoke(rating: Int, comment: String) {
        repository.rateUs(rating = rating, comment = comment)
    }
}
