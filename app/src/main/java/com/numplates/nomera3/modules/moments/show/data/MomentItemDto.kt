package com.numplates.nomera3.modules.moments.show.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.modules.newroads.data.entities.MediaEntity
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class MomentItemDto(
    @SerializedName("user")
    val user: UserSimple?,
    @SerializedName("active")
    val active: Int,
    @SerializedName("deleted")
    val deleted: Int,
    @SerializedName("access_denied")
    val accessDenied: Int,
    @SerializedName("asset")
    val asset: Asset?,
    @SerializedName("created_at")
    val createdAt: Long,
    @SerializedName("do_not_show_user")
    val doNotShowUser: Int,
    @SerializedName("gps_x")
    val gpsX: Float,
    @SerializedName("gps_y")
    val gpsY: Float,
    @SerializedName("id")
    val id: Long,
    @SerializedName("place")
    val place: String,
    @SerializedName("user_id")
    val userId: Long,
    @SerializedName("reactions")
    val reactions: List<ReactionEntity>,
    @SerializedName("comment_availability")
    val commentAvailability: Int,
    @SerializedName("viewed")
    val viewed: Int,
    @SerializedName("views_count")
    val viewsCount: Long,
    @SerializedName("comments_count")
    val commentsCount: Int,
    @SerializedName("reposts_count")
    val repostsCount: Int,
    @SerializedName("i_can_comment")
    val iCanComment: Int,
    @SerializedName("media")
    val mediaEntity: MediaEntity?,
    @SerializedName("adult_content")
    val adultContent: Int?
) : Parcelable {
    @Parcelize
    data class Asset(
        @SerializedName("type")
        val type: String,
        @SerializedName("url")
        val url: String?,
        @SerializedName("preview")
        val preview: String
    ) : Parcelable
}
