package com.meera.referrals.domain.model

data class UserProfileModel(
    val userId: Long,
    val accountTypeExpiration: Long,
    val uniqueName: String
)
