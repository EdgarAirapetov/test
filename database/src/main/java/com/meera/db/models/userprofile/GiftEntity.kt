package com.meera.db.models.userprofile

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.Nullable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

const val TYPE_GIFT_COFFEE_LIKE = 4

@Parcelize
data class GiftEntity(
    @SerializedName("id")
    val giftId: Long = 0,

    @SerializedName("image_big")
    val imageBig: String = "",

    @SerializedName("image_small")
    val imageSmall: String = "",

    @SerializedName("type_code")
    val typeCode: String = "",

    @SerializedName("added_at")
    val addedAt: Long = 0,

    @SerializedName("comment")
    val comment: String? = "",

    @Nullable
    @SerializedName("play_market_product_id")
    val playMarketProductId: String? = "",

    @Nullable
    @SerializedName("apple_product_id")
    val appleProductId: String? = "",

    @Nullable
    @SerializedName("custom_title")
    val customTitle: String? = "",

    @Nullable
    @SerializedName("holiday_title")
    val holidayTitle: String? = "",

    @Nullable
    @SerializedName("itunes_product_id")
    val iTunesProductId: String? = "",

    @SerializedName("sender")
    var senderUser: GiftSenderUser? = null,

    @SerializedName("metadata")
    val metadata: Metadata? = null,

    @SerializedName("type_id")
    val typeId: Int = 0,

    // Recycler list item (zero item or not)
    var listItemType: Int = 0
) : Serializable, Parcelable {
    fun hasCustomCoffeeDrawable(): Boolean {
        return typeId == TYPE_GIFT_COFFEE_LIKE && metadata?.coffeeCustomDrawable != null
    }
}

data class GiftSenderUser(
    @SerializedName("user_id")
    var userId: Long?,

    @SerializedName("birthday")
    var birthday: Long?,

    @SerializedName("account_color")
    var accountColor: Int?,

    @SerializedName("account_type")
    var accountType: Int?,

    @SerializedName("avatar")
    var avatar: AvatarModel?,

    @SerializedName("city")
    var city: String?,

    @SerializedName("gender")
    var gender: Int?,

    @SerializedName("name")
    var name: String?,

    @SerializedName("uniqname")
    var uniqueName: String?,
) : Serializable

data class AvatarModel(
    @SerializedName("big")
    var avatarBig: String?,

    @SerializedName("small")
    var avatarSmall: String?
) : Serializable

@Parcelize
data class Metadata(
    @SerializedName("coffee_code")
    val coffeeCode: String? = null,

    @SerializedName("coffee_type")
    val coffeeType: Int? = null,

    @SerializedName("is_viewed")
    val isViewed: Boolean = false,

    @SerializedName("is_received")
    val isReceived: Boolean = false,

    @Transient
    @DrawableRes
    val coffeeCustomDrawable: Int? = null
) : Parcelable
