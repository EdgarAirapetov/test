package com.numplates.nomera3.modules.billing

import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails

interface WrapperProductDetailsResponseListener {

    fun onSuccess(billingResult: BillingResult, productDetailsList: List<ProductDetails>)

    fun onError(billingResult: BillingResult, productDetailsList: List<ProductDetails>)
}
