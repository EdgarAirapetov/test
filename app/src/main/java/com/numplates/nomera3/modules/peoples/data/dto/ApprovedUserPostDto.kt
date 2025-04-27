package com.numplates.nomera3.modules.peoples.data.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.meera.db.models.message.UniquenameEntity
import com.numplates.nomera3.data.network.Asset
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class ApprovedUserPostDto(
    @SerializedName("adult_content")
    val isAdultContent: Boolean?,
    @SerializedName("asset")
    val asset: Asset?,
    @SerializedName("city_id")
    val cityId: Int,
    @SerializedName("comment_availability")
    val commentAvailability: String?,
    @SerializedName("comments")
    val comments: Int?,
    @SerializedName("country_id")
    val countryId: Int?,
    @SerializedName("created_at")
    val createdAt: Long?,
    @SerializedName("deleted")
    val isDeleted: Boolean?,
    @SerializedName("dislikes")
    val dislikes: Int?,
    @SerializedName("group_id")
    val groupId: Int?,
    @SerializedName("group_name")
    val groupName: String?,
    @SerializedName("id")
    val id: Long?,
    @SerializedName("is_allowed_to_comment")
    val isAllowedToComment: Boolean?,
    @SerializedName("likes")
    val likes: Int?,
    @SerializedName("moderated")
    val moderated: String?,
    @SerializedName("privacy")
    val privacy: String?,
    @SerializedName("reactions")
    val reactions: List<ReactionEntity?>?,
    @SerializedName("reposts")
    val reposts: Int?,
    @SerializedName("subscription")
    val isSubscription: Boolean?,
    @SerializedName("tags")
    val tags: List<UniquenameEntity?>?,
    @SerializedName("user_id")
    val userId: Long?,
    @SerializedName("updated_at")
    val updatedAt: Long?
) : Parcelable
