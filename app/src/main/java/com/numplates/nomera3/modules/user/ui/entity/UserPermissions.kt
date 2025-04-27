package com.numplates.nomera3.modules.user.ui.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserPermissions (

    val mainRoad: MainRoadPermissions,

    val communities: CommunitiesPermissions,

    val userBlockInfo: UserBlockInfo? = null

): Parcelable

@Parcelize
data class CommunitiesPermissions(

    val canCreatePostInOpenCommunity: Boolean,

    val canCreatePostInPrivateCommunity: Boolean

): Parcelable

@Parcelize
data class MainRoadPermissions(

    val canCreatePostInMainRoad: Boolean

): Parcelable

@Parcelize
data class UserBlockInfo(

    val blockReasonId: Int? = null,

    val type: String? = null,

    val blockedUntil: Long? = null,

    val blockReasonText: String? = null

): Parcelable
