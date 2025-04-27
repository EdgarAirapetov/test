package com.numplates.nomera3.modules.purchase.domain.repository

import com.numplates.nomera3.modules.purchase.domain.model.GiftCategoryModel
import com.numplates.nomera3.modules.purchase.domain.model.PurchaseOptions
import com.numplates.nomera3.modules.purchase.domain.model.PurchaseStatus
import com.numplates.nomera3.modules.purchase.domain.model.SimplePurchaseModel
import kotlinx.coroutines.flow.Flow

interface PurchaseRepository {

    suspend fun getGiftsWithData(): List<GiftCategoryModel>

    suspend fun getProductData(productIds: List<String>): List<SimplePurchaseModel>

    fun purchaseProduct(purchaseOptions: PurchaseOptions): Flow<PurchaseStatus>
}
