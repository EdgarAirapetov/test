package com.numplates.nomera3.modules.appInfo.data.entity

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.meera.media_controller_common.CropInfo
import com.numplates.nomera3.modules.remotestyle.data.posts.PostRulesRootEntity
import kotlinx.parcelize.Parcelize

const val APP_SETTINGS_KEY_VIDEO_EDITOR = "video_editor"
const val APP_SETTINGS_KEY_PHOTO_EDITOR = "photo_editor"
//Данные для кропа
const val CROP_INFO_ROAD = "road"
const val CROP_INFO_GALLERY = "gallery"
const val CROP_INFO_PREVIEW = "preview"
const val CROP_INFO_CHAT = "chat"

@Parcelize
class Settings(
    @SerializedName("settings")
    var appInfo: List<ApplicationInfoModel>,

    @SerializedName("media_compression_settings")
    val mediaCompressionSettings: MediaCompressionSettings?,

    @SerializedName("current")
    var currentApp: CurrentInfo?,

    @SerializedName("links")
    val links: AppLinks?,

    @SerializedName("show_birthday_congratulation")
    val showBirthdayCongratulation: Int?,

    @SerializedName("support_user_id")
    val supportUserId: Long?,

    @SerializedName("post_rules")
    val postRules: PostRulesRootEntity?,

    @SerializedName("recomended_to_update")
    val updateRecommendations: UpdateRecommendations?,

    @SerializedName("post_backgrounds")
    val postBackgrounds: List<PostBackgroundItemDto>?,

) : Parcelable {
    override fun toString(): String {
        return "appInfoSize: ${appInfo.size}"
    }
}

@Parcelize
data class MediaCompressionSettings(
    @SerializedName("image_quality")
    val imageQuality: Int?,
    @SerializedName("moment_media_height")
    val momentMediaHeight: Int?,
    @SerializedName("post_media_width")
    val postMediaWidth: Int?
) : Parcelable

@Parcelize
data class PostBackgroundItemDto(
    @SerializedName("id")
    val id: Int?,
    @SerializedName("url")
    val url: String?,
    @SerializedName("preview_url")
    var previewUrl: String?,
    @SerializedName("font_color")
    var fontColor: String?
) : Parcelable

@Parcelize
data class UpdateRecommendations(

    @SerializedName("can_be_closed")
    val isForceUpdate: Boolean? = null,

    @SerializedName("subtitle")
    val subtitle: String? = null,

    @SerializedName("title")
    var title: String? = null,

    @SerializedName("button_text")
    var btnText: String? = null

) : Parcelable

@Parcelize
data class CurrentInfo(
    @SerializedName("notes")
    var notes: List<String>?,
    @SerializedName("version")
    var version: String?
) : Parcelable

@Parcelize
class ApplicationInfoModel(
    @SerializedName("name") var name: String?,
    @SerializedName("value") var value: String?,
    @SerializedName("used") var cropSetting: List<CropInfo>? = null
) : Parcelable {
    override fun toString(): String {
        return "ApplicatioInfoModel(name=$name, value=$value used=$cropSetting)"
    }
}

@Parcelize
data class AppLinks(
    @SerializedName("short")
    val short: String? = null,
    @SerializedName("uniqname")
    val uniqname: String? = null
) : Parcelable

@Parcelize
enum class RecSystemType(val value: String) : Parcelable {
    RECOMMENDED("recommended"),
    TIMELINE("timeline");
}
