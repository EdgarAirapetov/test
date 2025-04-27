package com.numplates.nomera3.modules.gift_coffee.domain.usecase

import com.numplates.nomera3.modules.gift_coffee.data.entity.GiftPlaceEntityResponse
import com.numplates.nomera3.modules.gift_coffee.data.repository.GiftCoffeeRepository
import com.numplates.nomera3.modules.gift_coffee.domain.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.gift_coffee.domain.DefParams
import java.lang.Exception
import javax.inject.Inject

class GetCoffeeAddressUseCase @Inject constructor(
        private val repository: GiftCoffeeRepository
): BaseUseCaseCoroutine<GetCoffeeAddressParams, List<GiftPlaceEntityResponse>> {

    override suspend fun execute(params: GetCoffeeAddressParams,
                                 success: (List<GiftPlaceEntityResponse>) -> Unit,
                                 fail: (Exception) -> Unit) {
        repository.getCoffeeAddress(params.query, params.limit, params.offset, success, fail)
    }

}

class GetCoffeeAddressParams(
        val query: String,
        val limit: Int,
        val offset: Int
): DefParams()