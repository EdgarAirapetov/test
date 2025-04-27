package com.numplates.nomera3.modules.user.data.entity

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserPermissionResponse(

    @SerializedName("permissions")
    val permissions: Permissions? = null,

    @SerializedName("user_block")
    val userBlockInfo: UserBlockInfo? = null

): Parcelable

@Parcelize
data class Permissions(
    @SerializedName("main_road")
    val mainRoad: List<String>? = null,

    @SerializedName("communities")
    val communities: CommunitiesPermissions? = null,
): Parcelable

@Parcelize
data class UserBlockInfo(

    @SerializedName("block_reason_id")
    val blockReasonId: Int? = null,

    @SerializedName("type")
    val type: String? = null,

    @SerializedName("blocked_to")
    val blockedUntil: Long? = null,

    @SerializedName("block_reason_value")
    val blockReasonValue: String? = null

): Parcelable

@Parcelize
data class CommunitiesPermissions(

    @SerializedName("open")
    val open: List<String>? = null,

    @SerializedName("closed")
    val closed: List<String>? = null

): Parcelable