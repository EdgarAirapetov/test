package com.numplates.nomera3.modules.purchase.domain.usecase

import com.numplates.nomera3.modules.purchase.domain.model.PurchaseOptions
import com.numplates.nomera3.modules.purchase.domain.model.PurchaseStatus
import com.numplates.nomera3.modules.purchase.domain.repository.PurchaseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PurchaseProductUseCase @Inject constructor(
    private val repository: PurchaseRepository,
) {

    fun invoke(purchaseOptions: PurchaseOptions): Flow<PurchaseStatus> {
        return repository.purchaseProduct(purchaseOptions)
    }
}
