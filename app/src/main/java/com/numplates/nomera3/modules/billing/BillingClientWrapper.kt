package com.numplates.nomera3.modules.billing

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.ConsumeResponseListener
import com.android.billingclient.api.ConsumeResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchaseHistoryParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.consumePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchaseHistory
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.math.min
import kotlin.math.pow

private const val MAX_RETRY_ATTEMPT = 3
private const val RECONNECT_TIMER_START_MILLISECONDS = 1L * 1000L
private const val RECONNECT_TIMER_MAX_TIME_MILLISECONDS = 1000L * 60L * 15L

class BillingClientWrapper(
    private val context: Context,
    private val externalScope: CoroutineScope = CoroutineScope(
        SupervisorJob()
            + Dispatchers.Main
            + CoroutineExceptionHandler { _, throwable -> Timber.e(throwable) }
    ),
) : DefaultLifecycleObserver,
    PurchasesUpdatedListener,
    BillingClientStateListener,
    ProductDetailsResponseListener {

    var purchasesUpdatedListener: WrapperPurchasesUpdatedListener?
        get() = _purchasesUpdatedListener
        set(value) {
            _purchasesUpdatedListener = value
        }

    var billingClientStateListener: WrapperBillingClientStateListener?
        get() = _billingClientStateListener
        set(value) {
            _billingClientStateListener = value
        }

    var productDetailsResponseListener: WrapperProductDetailsResponseListener?
        get() = _productDetailsResponseListener
        set(value) {
            _productDetailsResponseListener = value
        }

    private val handler = Handler(Looper.getMainLooper())

    private var reconnectMilliseconds = RECONNECT_TIMER_START_MILLISECONDS
    private var billingClient: BillingClient? = null
    private var _purchasesUpdatedListener: WrapperPurchasesUpdatedListener? = null
    private var _billingClientStateListener: WrapperBillingClientStateListener? = null
    private var _productDetailsResponseListener: WrapperProductDetailsResponseListener? = null

    override fun onCreate(owner: LifecycleOwner) {
        billingClient = BillingClient.newBuilder(context.applicationContext)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        if (billingClient?.isReady == false) {
            Timber.e("Starting connection.")
            billingClient?.startConnection(this)
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        if (billingClient?.isReady == true) {
            Timber.e("Ending connection.")
            billingClient?.endConnection()
        }
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        _billingClientStateListener?.onBillingSetupFinished(billingResult)
        val response = BillingResponse(billingResult.responseCode)
        if (response.isOk) {
            restoreAllPurchases()
        } else {
            retryBillingServiceConnectionWithExponentialBackoff()
        }
    }

    override fun onBillingServiceDisconnected() {
        Timber.d("onBillingServiceDisconnected")
        // Try connecting again with exponential backoff.
        retryBillingServiceConnectionWithExponentialBackoff()
    }

    override fun onProductDetailsResponse(
        billingResult: BillingResult,
        productDetailsList: MutableList<ProductDetails>
    ) {
        val response = BillingResponse(billingResult.responseCode)
        if (response.isOk) {
            _productDetailsResponseListener?.onSuccess(billingResult, productDetailsList)
        } else {
            _productDetailsResponseListener?.onError(billingResult, productDetailsList)
        }
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        val response = BillingResponse(billingResult.responseCode)
        if (response.isOk) {
            _purchasesUpdatedListener?.onSuccess(billingResult, purchases.orEmpty())
        } else {
            _purchasesUpdatedListener?.onError(billingResult, purchases.orEmpty())
        }
    }

    suspend fun consumePurchase(purchase: Purchase): ConsumeResult {
        Timber.d("Billing: consumePurchaseAsync purchase = ${purchase};")
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        val result = billingClient?.consumePurchase(consumeParams)
        if (result?.billingResult?.responseCode == BillingResponseCode.OK) {
            return result
        } else {
            error("Result code: ${result?.billingResult?.responseCode};")
        }
    }

    suspend fun queryProductDetail(productId: String): ProductDetails? {
        val result = billingClient?.queryProductDetails(createProductParamsBySku(listOf(productId), ProductType.INAPP))
        return if (result?.billingResult?.responseCode == BillingResponseCode.OK) {
            result.productDetailsList?.firstOrNull()
        } else {
            Timber.e("Result code: ${result?.billingResult?.responseCode};")
            null
        }
    }

    suspend fun queryProductDetails(productIds: List<String>): List<ProductDetails> {
        val result = billingClient?.queryProductDetails(createProductParamsBySku(productIds, ProductType.INAPP))
        return if (result?.billingResult?.responseCode == BillingResponseCode.OK) {
            result.productDetailsList ?: emptyList()
        } else {
            Timber.e("Result code: ${result?.billingResult?.responseCode};")
            emptyList()
        }
    }

    fun consumePurchaseAsync(
        purchase: Purchase,
        consumeResponseListener: ConsumeResponseListener
    ) {
        Timber.d("Billing: consumePurchaseAsync purchase = ${purchase};")
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        billingClient?.consumeAsync(consumeParams, consumeResponseListener)
    }

    /**
     * Launching the billing flow.
     *
     * Launching the UI to make a purchase requires a reference to the Activity.
     */
    fun launchBillingFlow(activity: Activity, params: BillingFlowParams): Int {
        if (billingClient?.isReady == false) {
            Timber.e("launchBillingFlow: BillingClient is not ready")
        }
        val billingResult = billingClient?.launchBillingFlow(activity, params)
        val responseCode = billingResult?.responseCode
        val debugMessage = billingResult?.debugMessage
        Timber.d("launchBillingFlow: BillingResponse $responseCode $debugMessage")
        return responseCode ?: BillingResponseCode.ERROR
    }

    fun resetListeners() {
        _purchasesUpdatedListener = null
        _billingClientStateListener = null
        _productDetailsResponseListener = null
    }

    /**
     * Acknowledge a purchase.
     *
     * https://developer.android.com/google/play/billing/billing_library_releases_notes#2_0_acknowledge
     *
     * Apps should acknowledge the purchase after confirming that the purchase token
     * has been associated with a user. This app only acknowledges purchases after
     * successfully receiving the subscription data back from the server.
     */
    @SuppressLint("BinaryOperationInTimber")
    suspend fun acknowledgePurchase(purchaseToken: String): Boolean {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()
        for (trial in 1..MAX_RETRY_ATTEMPT) {
            val result = billingClient?.acknowledgePurchase(params)
            val response = BillingResponse(result?.responseCode ?: 500)
            when {
                response.isOk -> {
                    Timber.i("Acknowledge success - token: $purchaseToken")
                    return true
                }
                response.canFailGracefully -> {
                    Timber.i("Token $purchaseToken is already owned.")
                    return true
                }
                response.isRecoverableError -> {
                    // Retry to ack because these errors may be recoverable.
                    val duration = 500L * 2.0.pow(trial).toLong()
                    delay(duration)
                    if (trial < MAX_RETRY_ATTEMPT) {
                        Timber.w("Retrying($trial) to acknowledge for token " +
                            "$purchaseToken - code: ${result?.responseCode}, message: ${result?.debugMessage}")
                    }
                }
                response.isNonrecoverableError || response.isTerribleFailure -> {
                    Timber.e("Failed to acknowledge for token $purchaseToken - code: " +
                        "${result?.responseCode}, message: ${result?.debugMessage}")
                    break
                }
            }
        }
        throw Exception("Failed to acknowledge the purchase!")
    }

    /**
     * Retries the billing service connection with exponential backoff, maxing out at the time
     * specified by RECONNECT_TIMER_MAX_TIME_MILLISECONDS.
     */
    private fun retryBillingServiceConnectionWithExponentialBackoff() {
        handler.postDelayed({ billingClient?.startConnection(this) }, reconnectMilliseconds)
        reconnectMilliseconds = min(reconnectMilliseconds * 2, RECONNECT_TIMER_MAX_TIME_MILLISECONDS)
    }

    //TODO Пересмотреть логику https://nomera.atlassian.net/browse/BR-20044
    private fun restoreAllPurchases() {
        externalScope.launch {
            val params = QueryPurchaseHistoryParams.newBuilder()
                .setProductType(ProductType.INAPP)
                .build()
            runCatching {
                billingClient?.queryPurchaseHistory(params)
            }.onSuccess { result ->
                if (result?.billingResult?.responseCode == BillingResponseCode.OK) {
                    result.purchaseHistoryRecordList?.forEach { record ->
                        Timber.i("Purchase history record: ${record};")
                        val consumeParams = ConsumeParams.newBuilder()
                            .setPurchaseToken(Purchase(record.originalJson, record.signature).purchaseToken)
                            .build()
                        billingClient?.consumePurchase(consumeParams)
                    }
                } else {
                    error("Restore purchases error: ${result?.billingResult?.responseCode}, ${result?.billingResult?.debugMessage};")
                }
            }.onFailure {
                error("Restore purchases error: $it")
            }
        }
    }

    /**
     * Create product params for billing request
     */
    private fun createProductParamsBySku(
        skuList: List<String>,
        @ProductType productType: String,
    ): QueryProductDetailsParams {
        val productList = skuList.map { item ->
            QueryProductDetailsParams.Product
                .newBuilder()
                .setProductType(productType)
                .setProductId(item)
                .build()
        }
        return QueryProductDetailsParams
            .newBuilder()
            .setProductList(productList)
            .build()
    }
}

private class BillingResponse(val code: Int) {
    val isOk: Boolean
        get() = code == BillingResponseCode.OK
    val canFailGracefully: Boolean
        get() = code == BillingResponseCode.ITEM_ALREADY_OWNED
    val isRecoverableError: Boolean
        get() = code in setOf(
            BillingResponseCode.ERROR,
            BillingResponseCode.SERVICE_DISCONNECTED,
        )
    val isNonrecoverableError: Boolean
        get() = code in setOf(
            BillingResponseCode.SERVICE_UNAVAILABLE,
            BillingResponseCode.BILLING_UNAVAILABLE,
            BillingResponseCode.DEVELOPER_ERROR,
        )
    val isTerribleFailure: Boolean
        get() = code in setOf(
            BillingResponseCode.ITEM_UNAVAILABLE,
            BillingResponseCode.FEATURE_NOT_SUPPORTED,
            BillingResponseCode.ITEM_NOT_OWNED,
            BillingResponseCode.USER_CANCELED,
        )
}
