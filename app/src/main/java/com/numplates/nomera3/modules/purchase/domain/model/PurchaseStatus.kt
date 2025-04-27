package com.numplates.nomera3.modules.purchase.domain.model

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.Purchase

sealed class PurchaseStatus {

    /**
     * Return params for purchasing product. These params should be used for
     * starting purchase flow using [BillingClient]
     */
    data class ClientPrepared(val params: BillingFlowParams) : PurchaseStatus()

    /**
     * Return list of purchases when product(s) purchased successfully
     */
    data class Purchased(val purchases: List<Purchase>) : PurchaseStatus()
}
