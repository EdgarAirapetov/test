package com.numplates.nomera3.modules.rateus.domain

import com.numplates.nomera3.modules.rateus.data.RateUsRepository
import javax.inject.Inject

class RateUsWriteIsRatedUseCase @Inject constructor(private val repository: RateUsRepository) {
    fun invoke(isRated: Boolean) {
        repository.writeIsRated(isRated)
    }
}
