package com.numplates.nomera3.modules.purchase.domain.usecase

import com.numplates.nomera3.modules.purchase.domain.model.GiftCategoryModel
import com.numplates.nomera3.modules.purchase.domain.repository.PurchaseRepository
import javax.inject.Inject

class GetGiftsWithDataUseCase @Inject constructor(
    private val repository: PurchaseRepository
) {

    suspend fun invoke(): List<GiftCategoryModel> = repository.getGiftsWithData()
}
