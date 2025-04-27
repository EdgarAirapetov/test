package com.numplates.nomera3.data.newmessenger

import com.google.gson.annotations.SerializedName
import com.meera.db.models.userprofile.UserRole

data class FriendEntity(
    @SerializedName("id")
    val id: Long,
    @SerializedName("name")
    val name: String?,
    @SerializedName("birthday")
    val birthday: Long?,
    @SerializedName("avatar_big")
    val avatarBig: String?,
    @SerializedName("avatar_small")
    val avatarSmall: String?,
    @SerializedName("gender")
    val gender: Int?,
    @SerializedName("color")
    val color: Int?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("type")
    val type: Int?,
    @SerializedName("city_name")
    val city: String?,
    @SerializedName("uniqname")
    val uniqueName: String?,
    @SerializedName("role")
    val role: String?,
    @SerializedName("approved")
    val approved: Int?,
    // Local fields
    val isChecked: Boolean = false,
    @SerializedName("top_content_maker")
    var topContentMaker: Int? = null
) {
    val userRole: UserRole
        get() = when (role) {
            UserRole.USER.value -> UserRole.USER
            UserRole.ANNOUNCE_USER.value -> UserRole.ANNOUNCE_USER
            UserRole.SUPPORT_USER.value -> UserRole.SUPPORT_USER
            UserRole.SYSTEM_ADMIN.value -> UserRole.SYSTEM_ADMIN
            else -> UserRole.USER
        }
}
