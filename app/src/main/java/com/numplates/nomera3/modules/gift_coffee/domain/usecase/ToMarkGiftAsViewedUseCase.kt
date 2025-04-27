package com.numplates.nomera3.modules.gift_coffee.domain.usecase

import com.numplates.nomera3.modules.comments.domain.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.comments.domain.DefParams
import com.numplates.nomera3.modules.gift_coffee.data.repository.GiftCoffeeRepository
import javax.inject.Inject

class ToMarkGiftAsViewedUseCase @Inject constructor(
    private val repository: GiftCoffeeRepository
) : BaseUseCaseCoroutine<ToMarkGiftViewedParams, Boolean> {

    override suspend fun execute(
        params: ToMarkGiftViewedParams,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) {
        repository.markAsViewed(params.id, success, fail)
    }
}

class ToMarkGiftViewedParams(
    val id: Long
) : DefParams()
