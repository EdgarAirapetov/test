package com.numplates.nomera3.modules.rateus.domain

import com.numplates.nomera3.modules.rateus.data.RateUsRepository
import javax.inject.Inject

class RateUsSaveLastShowUseCase @Inject constructor(private val repository: RateUsRepository) {
    fun invoke() {
        repository.saveLastShow()
    }
}
