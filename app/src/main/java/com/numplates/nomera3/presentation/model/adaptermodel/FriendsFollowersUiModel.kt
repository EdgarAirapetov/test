package com.numplates.nomera3.presentation.model.adaptermodel

import android.os.Parcelable
import com.meera.db.models.userprofile.UserSimple
import kotlinx.parcelize.Parcelize

@Parcelize
data class FriendsFollowersUiModel(
    val userSimple: UserSimple?,
    var subscriptionType: SubscriptionType,
    val isAccountApproved: Boolean
) : Parcelable
