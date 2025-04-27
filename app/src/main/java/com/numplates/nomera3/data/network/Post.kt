package com.numplates.nomera3.data.network

import androidx.room.Ignore
import com.google.gson.annotations.SerializedName
import com.meera.core.extensions.empty
import com.meera.db.models.message.ParsedUniquename
import com.meera.db.models.message.UniquenameEntity
import com.meera.db.models.userprofile.City
import com.meera.db.models.userprofile.Country
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.modules.baseCore.PostPrivacy
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.maps.data.model.EventDto
import com.numplates.nomera3.modules.newroads.data.entities.MediaEntity
import com.numplates.nomera3.modules.newroads.data.entities.PostSourceType
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity
import kotlinx.android.parcel.RawValue
import java.io.Serializable

const val TYPE_REPOST = 10
const val TYPE_FEATURE_ANNOUNCEMENT = 11
const val TYPE_IMAGE_POST = 3

@Deprecated("старые мапперы типов, добавлены для обратной совместимости")
fun getPostListAdapterLegacyType(post: Post): Int {
    return if (post.parentPost != null) {
        TYPE_REPOST
    } else { TYPE_IMAGE_POST }
}

data class Post(

    @SerializedName("id")
    override var id: Long,

    @SerializedName("uid")
    override var uid: Long,

    @SerializedName("title")
    var title: String?,

    @SerializedName("text")
    override var text: String?,

    @SerializedName("date")
    override var date: Long,

    @SerializedName("edited_at")
    var editedAt: Long,

    @SerializedName("group_id")
    var groupId: Long,

    @SerializedName("info")
    var info: Int,

    @SerializedName("user_number")
    override var number: String?,

    @SerializedName("driver")
    override var driver: Int,

    @SerializedName("user_name")
    override var name: String?,

    @SerializedName("user_city_id")
    var userCityId: Int,

    @SerializedName("user_birthday")
    var userBirthday: Long,

    @SerializedName("show_birthday")
    var showBirthday: Int,

    @SerializedName("avatar")
    override var avatar: String?,

    @SerializedName("avatar_date")
    override var avatarDate: Long,

    @SerializedName("show_on_map")
    var showOnMap: Int,

    @SerializedName("map_activity")
    var mapActivity: Long,

    @SerializedName("vehicle")
    override var vehicle: Int,

    @SerializedName("verified")
    var verified: Int,

    @SerializedName("purchases")
    var purchases: Int,

    @SerializedName("country_id")
    var countryId: Int,

    @SerializedName("subscr")
    var subscr: Int,

    @SerializedName("is_subscribed")
    var subscribed: Int?,

    @Ignore
    @SerializedName("purchase_basket")
    var purchaseBasket: List<Purchase?>?,

    @SerializedName("account_color")
    override var accountColor: Int,

    @SerializedName("account_type")
    override var accountType: Int,

    @SerializedName("small_image")
    var smallImage: String?,

    @SerializedName("small_url")
    var smallUrl: String?,

    @Ignore
    @SerializedName("ads")
    var ads: PostAds?,

    @SerializedName("user_city_name")
    var userCityName: String?,

    @SerializedName("post_city_name")
    var postCityName: String?,

    @SerializedName("resp_name")
    override var respName: String?,

    @SerializedName("user")
    var user: UserSimple?,

    @SerializedName("main_vehicle")
    var mainVehicle: Vehicle?,

    @SerializedName("parent")
    var parentPost: Post? = null,

    @SerializedName("reposts_count")
    var repostsCount: Int = 0,

    @SerializedName("deleted")
    var deleted: Int = 0,

    @SerializedName("asset")
    var asset: Asset? = null, // used for chat repost

    @SerializedName("assets")
    var assets: List<MediaAssetDto>? = null, // used for multiple media posts

    @SerializedName("created_at")
    var createdAt: Long? = null, // used for chat repost

    @SerializedName("video")
    var video: String? = null, // used for chat repost

    @SerializedName("source_type")
    var sourceType: String? = String.empty(),

    @SerializedName("type")
    var type: Int = 0,

    @SerializedName("duration")
    var videoDurationInSeconds: Int? = null,

    @SerializedName("video_preview")
    var videoPreview: String? = null,

    @SerializedName("adult_content")
    var isAdultContent: Boolean? = false,

    @SerializedName("is_allowed_to_comment")
    var isAllowedToComment: Boolean? = null,

    @SerializedName("reactions")
    var reactions: List<ReactionEntity>? = mutableListOf(),

    @SerializedName("privacy")
    var privacy: String? = PostPrivacy.PUBLIC.status,

    @SerializedName("group_name")
    var groupName: String? = null,

    @SerializedName("post_type")
    var postType: Int? = null,

    @SerializedName("group_avatar_image")
    var groupAvatar: String? = null,

    @SerializedName("tags")
    var tags: List<UniquenameEntity?>? = mutableListOf(),

    var tagSpan: @RawValue ParsedUniquename? = null,

    @SerializedName("city")
    var city: City?,

    @SerializedName("country")
    var country: Country?,

    @SerializedName("media")
    var mediaEntity: MediaEntity?,

    @SerializedName("group_type")
    var groupType: Int? = null, // group_type: Int(optional) (0-closed, 1-open)

    @SerializedName("comment_availability")
    var commentAvailability: String?,

    @SerializedName("is_hidden")
    var postHidden: Int? = 0,

    @SerializedName("event")
    var event: EventDto? = null

) : Serializable, SimplePost() {

    constructor() : this(0, 0, "", "", 0, 0, 0, 0,
            "", 0, "", 0, 0,
            0, "", 0, 0, 0,
            0, 0, 0, 0, 0, 0,
            null, 0, 0, "",
            "", null, "", "", "",
            null, null, city = null, country = null, mediaEntity = null, commentAvailability = null)

    /**
     * If [PostUIEntity.video] is not null it means we have a GIF or a VIDEO.
     * By checking [PostUIEntity.sourceType] we can understand which exactly content we have.
     */
    fun hasPostVideo(): Boolean {
        return !video.isNullOrBlank() && sourceType != PostSourceType.GIF.key
    }

    /**
     * The logic is the same as [PostUIEntity.hasPostVideo] but GIF type check is used.
     */
    fun hasPostGif(): Boolean {
        return !video.isNullOrBlank() && sourceType == PostSourceType.GIF.key
    }
}
