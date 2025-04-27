package com.meera.referrals.ui.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class ReferralDataUIModel(
    val availableVips: Int,
    val code: String,
    val referrals: ReferralUIModel,
    val text: String,
    val title: String
) : Parcelable

@Parcelize
data class ReferralUIModel(
    val count: Int,
    val limit: Int
) : Parcelable


