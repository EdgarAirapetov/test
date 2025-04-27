package com.numplates.nomera3.modules.feed.data.entity

import com.google.gson.annotations.SerializedName
import com.meera.core.extensions.empty
import com.meera.db.models.message.UniquenameEntity
import com.numplates.nomera3.data.network.Asset
import com.numplates.nomera3.data.network.MediaAssetDto
import com.numplates.nomera3.data.network.Vehicle
import com.numplates.nomera3.modules.baseCore.PostPrivacy
import com.numplates.nomera3.modules.maps.data.model.EventDto
import com.numplates.nomera3.modules.newroads.data.entities.MediaEntity
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity
import com.numplates.nomera3.data.network.MediaPositioningDto

private const val DEF_ID = -1L

data class PostEntityResponse(
    @SerializedName("id")
    val id: Long = DEF_ID,

    @SerializedName("aspect")
    val aspect: Double = 1.0,

    @SerializedName("comments_count")
    val commentsCount: Int = 0,

    @SerializedName("date")
    val date: Long = Long.MAX_VALUE,

    @SerializedName("edited_at")
    val editedAt: Long? = null,

    @SerializedName("deleted")
    val deleted: Int = 0,

    @SerializedName("group_id")
    val groupId: Long = DEF_ID,

    @SerializedName("info")
    val info: Int = 0,

    @SerializedName("is_subscribed")
    val itsSubscribed: Int = 0,

    @SerializedName("main_vehicle")
    val mainVehicle: Vehicle? = null,

    @SerializedName("reposts_count")
    val repostsCount: Int = 0,

    @SerializedName("text")
    val text: String = String.empty(),

    @SerializedName("image")
    val image: String = String.empty(),

    @SerializedName("small_image")
    var smallImage: String?,

    @SerializedName("small_url")
    var smallUrl: String?,

    @SerializedName("title")
    val title: String = String.empty(),

    @SerializedName("user")
    val user: UserEntityResponse? = null,

    @SerializedName("parent")
    val parentPost: PostEntityResponse? = null,

    @SerializedName("asset")
    val asset: Asset? = null,

    @SerializedName("assets")
    val assets: List<MediaAssetDto>? = mutableListOf(),

    @SerializedName("created_at")
    val createdAt: Long = Long.MAX_VALUE,

    @SerializedName("item_type")
    val itemType: Int = 0,

    @SerializedName("is_allowed_to_comment")
    val isAllowedToComment: Boolean = false,

    @SerializedName("adult_content")
    val isAdultContent: Boolean? = false,

    @SerializedName("refresh_post_item")
    val refreshItem: Int = 0,

    @SerializedName("parent_post_id")
    val parentPostId: Long = DEF_ID,

    @SerializedName("privacy")
    val privacy: String? = PostPrivacy.PUBLIC.status,

    @SerializedName("group_name")
    val groupName: String? = null,

    @SerializedName("group_avatar_image")
    val groupAvatar: String? = null,

    @SerializedName("tags")
    val tags: List<UniquenameEntity?>? = mutableListOf(),

    @SerializedName("country")
    val country: CountryEntityResponse? = null,

    @SerializedName("city")
    val city: CityEntityResponse? = null,

    // Video part
    @SerializedName("duration")
    val videoDurationInSeconds: Int? = null,

    @SerializedName("video")
    val video: String? = null,

    @SerializedName("video_preview")
    val videoPreview: String? = null,

    @SerializedName("media")
    val media: MediaEntity? = null,

    @SerializedName("reactions")
    val reactions: List<ReactionEntity>? = null,

    @SerializedName("group_type")
    var groupType: Int? = null, // group_type: Int(optional) (0-closed, 1-open)

    @SerializedName("source_type")
    var sourceType: String? = String.empty(),

    @SerializedName("type")
    var type: Int = 0,

    @SerializedName("comment_availability")
    var commentAvailability: String,

    @SerializedName("event")
    var event: EventDto? = null,

    @SerializedName("background_url")
    var backgroundUrl: String? = null,

    @SerializedName("background_id")
    var backgroundId: Int? = null,

    @SerializedName("font_color")
    var fontColor: String? = null,

    @SerializedName("font_size")
    var fontSize: Int? = null,

    @SerializedName("media_positioning")
    var mediaPositioning: MediaPositioningDto? = null,

    @SerializedName("is_new_subs_post")
    val isNewSubsPost: Int? = null,

    @SerializedName("is_hidden")
    var postHidden: Int? = 0,
)
