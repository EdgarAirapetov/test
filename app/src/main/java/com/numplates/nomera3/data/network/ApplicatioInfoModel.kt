package com.numplates.nomera3.data.network

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.meera.media_controller_common.CropInfo
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Deprecated("use appinfo module")
class Settings(
    @SerializedName("settings")
    var appInfo: List<ApplicationInfoModel>,
    @SerializedName("current")
    var currentApp: CurrentInfo?,
    @SerializedName("links")
    val links: AppLinks?

) : Serializable {
    override fun toString(): String {
        return "appInfoSize: ${appInfo.size}"
    }
}

data class CurrentInfo(
    @SerializedName("notes")
    var notes: List<String>?,
    @SerializedName("version")
    var version: String?
)


class ApplicationInfoModel(
    @SerializedName("name") var name: String?,
    @SerializedName("value") var value: String?,
    @SerializedName("used") var cropSetting: List<CropInfo>? = null
) : Serializable {
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
