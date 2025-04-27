package com.meera.referrals.domain.model

data class ReferralDataModel(
    val availableVips: Int,
    val code: String,
    val referrals: ReferralModel,
    val text: String,
    val title: String
)

data class ReferralModel(
    val count: Int,
    val limit: Int
)
