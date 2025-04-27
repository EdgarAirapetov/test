package com.numplates.nomera3.modules.purchase.data.repository

import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.meera.core.di.scopes.AppScope
import com.numplates.nomera3.data.network.IapRequest
import com.numplates.nomera3.modules.billing.BillingClientWrapper
import com.numplates.nomera3.modules.billing.WrapperPurchasesUpdatedListener
import com.numplates.nomera3.modules.purchase.data.api.PurchaseApi
import com.numplates.nomera3.modules.purchase.data.mapper.GiftDtoMapper
import com.numplates.nomera3.modules.purchase.domain.error.PurchaseException
import com.numplates.nomera3.modules.purchase.domain.mapper.GiftModelMapper
import com.numplates.nomera3.modules.purchase.domain.model.GiftCategoryModel
import com.numplates.nomera3.modules.purchase.domain.model.PurchaseOptions
import com.numplates.nomera3.modules.purchase.domain.model.PurchaseStatus
import com.numplates.nomera3.modules.purchase.domain.model.SimplePurchaseModel
import com.numplates.nomera3.modules.purchase.domain.repository.PurchaseRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

@AppScope
class PurchaseRepositoryImpl @Inject constructor(
    private val api: PurchaseApi,
    private val billingClient: BillingClientWrapper,
    private val giftDtoMapper: GiftDtoMapper,
    private val giftModelMapper: GiftModelMapper,
) : PurchaseRepository {

    override suspend fun getGiftsWithData(): List<GiftCategoryModel> {
        Timber.d("getGiftsWithData method called.")
        val result = api.getGifts().data?.categories ?: error("Empty response")
        val productDetails = billingClient.queryProductDetails(
            giftDtoMapper.flatMapProductIds(result.flatMap { category -> category.gifts })
        )
        return giftModelMapper.convertGiftCategory(result, productDetails)
    }

    override suspend fun getProductData(productIds: List<String>): List<SimplePurchaseModel> {
        return billingClient.queryProductDetails(productIds)
            .sortedBy { details -> details.oneTimePurchaseOfferDetails?.priceAmountMicros }
            .map { details ->
                SimplePurchaseModel(
                    marketId = details.productId,
                    description = details.description,
                    price = details.oneTimePurchaseOfferDetails?.formattedPrice ?: ""
                )
            }
    }

    override fun purchaseProduct(purchaseOptions: PurchaseOptions): Flow<PurchaseStatus> {
        return callbackFlow {
            val purchaseParams = initializeProductParams(purchaseOptions)
            trySend(PurchaseStatus.ClientPrepared(purchaseParams))
            billingClient.purchasesUpdatedListener = object : WrapperPurchasesUpdatedListener {
                override fun onSuccess(billingResult: BillingResult, purchases: List<Purchase>) {
                    Timber.d("Billing response code OK: $purchases")
                    trySend(PurchaseStatus.Purchased(purchases))
                    channel.close()
                }

                override fun onError(billingResult: BillingResult, purchases: List<Purchase>) {
                    val exception = CancellationException(
                        message = "Purchase error. Code: ${billingResult.responseCode}; " +
                            "Msg: ${billingResult.debugMessage};",
                        cause = when (billingResult.responseCode) {
                            BillingResponseCode.USER_CANCELED -> PurchaseException.CancelledByUser
                            BillingResponseCode.ERROR -> PurchaseException.InternalError
                            else -> UnknownError()
                        }
                    )
                    cancel(exception)
                }
            }
            awaitClose { billingClient.resetListeners() }
        }.onEach { status ->
            if (status is PurchaseStatus.Purchased) {
                confirmPurchasedProducts(status.purchases, purchaseOptions)
            }
        }
    }

    private suspend fun initializeProductParams(purchaseOptions: PurchaseOptions): BillingFlowParams {
        Timber.d("Perform purchase billing flow request.")
        val details = billingClient.queryProductDetails(listOf(purchaseOptions.productId))
        val productParamList = ProductDetailsParams.newBuilder()
            .setProductDetails(details.first())
            .build()
        return BillingFlowParams
            .newBuilder()
            .setProductDetailsParamsList(listOf(productParamList))
            .build()
    }

    private suspend fun confirmPurchasedProducts(
        purchases: List<Purchase>,
        purchaseOptions: PurchaseOptions
    ) {
        Timber.d("Consume purchase items. Count: ${purchases.size}")
        purchases.forEach { purchase ->
            Timber.d("Purchase detailed information: $purchase")
            val iapRequest = createIapRequest(purchase, purchaseOptions)
            val success = purchaseProduct(iapRequest)
            if (success) {
                billingClient.consumePurchase(purchase)
            } else {
                error("Can not approve a purchase on server side")
            }
        }
    }

    private suspend fun purchaseProduct(request: IapRequest): Boolean {
        Timber.d("Purchase Product method called. request: $request")
        val response = api.purchaseProduct(request)
        if (response.data != null) {
            return response.data
        } else {
            error(response.message)
        }
    }

    private fun createIapRequest(
        purchase: Purchase,
        purchaseOptions: PurchaseOptions,
    ): IapRequest {
        return IapRequest(
            orderId = purchase.orderId.orEmpty(),
            packageName = purchase.packageName,
            productId = purchase.products.first(),
            purchaseTime = purchase.purchaseTime,
            purchaseState = purchase.purchaseState,
            purchaseToken = purchase.purchaseToken,
            userId = purchaseOptions.userId,
            comment = purchaseOptions.comment,
            accountColor = purchaseOptions.accountColor,
            showSender = purchaseOptions.showSender,
        )
    }
}
