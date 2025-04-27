package com.numplates.nomera3.modules.purchase.domain.usecase

import com.numplates.nomera3.modules.purchase.domain.model.SimplePurchaseModel
import com.numplates.nomera3.modules.purchase.domain.product.Premium
import com.numplates.nomera3.modules.purchase.domain.product.Vip
import com.numplates.nomera3.modules.purchase.domain.repository.PurchaseRepository
import javax.inject.Inject

class GetSubscriptionUseCase @Inject constructor(
    private val repository: PurchaseRepository,
) {

    suspend fun invoke(isPremium: Boolean): List<SimplePurchaseModel> {
        val subscriptions: List<String> = when (isPremium) {
            true -> Premium.values()
            else -> Vip.values()
        }.map { subscription -> subscription.marketId() }
        return repository.getProductData(subscriptions)
    }
}
