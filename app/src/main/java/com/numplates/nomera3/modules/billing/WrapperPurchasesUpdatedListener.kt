package com.numplates.nomera3.modules.billing

import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase

interface WrapperPurchasesUpdatedListener {

    fun onSuccess(billingResult: BillingResult, purchases: List<Purchase>)

    fun onError(billingResult: BillingResult, purchases: List<Purchase>)
}
