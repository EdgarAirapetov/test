package com.numplates.nomera3.modules.purchase.domain.product

import com.android.billingclient.api.BillingClient

interface MarketProduct {

    /**
     * Exclusive market product for purchasing items using [BillingClient]
     */
    fun marketId(): String
}
