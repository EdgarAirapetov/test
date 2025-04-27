package com.numplates.nomera3.modules.userprofile.data.entity

import com.google.gson.annotations.SerializedName

data class GiftDto(
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

    @SerializedName("play_market_product_id")
    val playMarketProductId: String? = "",

    @SerializedName("apple_product_id")
    val appleProductId: String? = "",

    @SerializedName("custom_title")
    val customTitle: String? = "",

    @SerializedName("holiday_title")
    val holidayTitle: String? = "",

    @SerializedName("itunes_product_id")
    val iTunesProductId: String? = "",

    @SerializedName("sender")
    var senderUser: GiftSenderUserDto? = null,

    @SerializedName("metadata")
    val metadata: MetadataDto? = null,

    @SerializedName("type_id")
    val typeId: Int = 0
)

data class GiftSenderUserDto(
    @SerializedName("user_id")
    var userId: Long?,

    @SerializedName("birthday")
    var birthday: Long?,

    @SerializedName("account_color")
    var accountColor: Int?,

    @SerializedName("account_type")
    var accountType: Int?,

    @SerializedName("avatar")
    var avatar: GiftSenderAvatarDto?,

    @SerializedName("city")
    var city: String?,

    @SerializedName("gender")
    var gender: Int?,

    @SerializedName("name")
    var name: String?,

    @SerializedName("uniqname")
    var uniqueName: String?,
)

data class GiftSenderAvatarDto(
    @SerializedName("big")
    var avatarBig: String?,

    @SerializedName("small")
    var avatarSmall: String?
)

data class MetadataDto(
    @SerializedName("coffee_code")
    val coffeeCode: String? = null,

    @SerializedName("coffee_type")
    val coffeeType: Int? = null,

    @SerializedName("is_viewed")
    val isViewed: Boolean = false,

    @SerializedName("is_received")
    val isReceived: Boolean = false,
)
