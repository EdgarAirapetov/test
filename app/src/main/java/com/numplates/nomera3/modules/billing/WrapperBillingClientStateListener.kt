package com.numplates.nomera3.modules.billing

import com.android.billingclient.api.BillingResult

interface WrapperBillingClientStateListener {

    fun onBillingSetupFinished(billingResult: BillingResult)

    fun onBillingServiceDisconnected()
}
