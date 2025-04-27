package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import com.meera.db.models.moments.UserMomentsDto
import com.numplates.nomera3.modules.baseCore.data.model.CoordinatesDto
import java.io.Serializable

data class MapUserDto(
    @SerializedName("user_id") var uid: Long,
    @SerializedName("account_type") var accountType: Int,
    @SerializedName("avatar_small") var avatar: String?,
    @SerializedName("gender") var gender: Int,
    @SerializedName("account_color") var accountColor: Int?,
    @SerializedName("map_state") var mapState: Int? = null, //RoadStateEnum
    @SerializedName("ny") var ny: Int? = null,
    @SerializedName("blacklisted_by_me") var blacklistedByMe: Int? = null,
    @SerializedName("blacklisted_me") var blacklistedMe: Int? = null,
    @SerializedName("coordinates") var coordinates: CoordinatesDto? = null,
    @SerializedName("is_friend") var isFriend: Int? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("uniqname") var uniqueName: String? = null,
    @SerializedName("complete") var complete: Boolean? = null,
    @SerializedName("moments") val moments: UserMomentsDto? = null
    ) : Serializable
