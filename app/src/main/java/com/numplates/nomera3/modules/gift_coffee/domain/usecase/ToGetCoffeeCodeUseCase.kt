package com.numplates.nomera3.modules.gift_coffee.domain.usecase

import com.numplates.nomera3.modules.comments.domain.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.comments.domain.DefParams
import com.numplates.nomera3.modules.gift_coffee.data.entity.CoffeeType
import com.numplates.nomera3.modules.gift_coffee.data.entity.PromoCodeEntity
import com.numplates.nomera3.modules.gift_coffee.data.repository.GiftCoffeeRepository
import javax.inject.Inject

class ToGetCoffeeCodeUseCase @Inject constructor(
        private val repository: GiftCoffeeRepository
) : BaseUseCaseCoroutine<ToGetCoffeeCodeParams, PromoCodeEntity> {

    override suspend fun execute(params: ToGetCoffeeCodeParams, success: (PromoCodeEntity) -> Unit, fail: (Exception) -> Unit) {
        repository.getCoffeeCode(params.id, params.type, success, fail)
    }
}

class ToGetCoffeeCodeParams(
        val id: Long,
        val type: CoffeeType
) : DefParams()
