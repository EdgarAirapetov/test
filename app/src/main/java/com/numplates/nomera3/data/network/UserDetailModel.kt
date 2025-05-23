package com.numplates.nomera3.data.network
import com.google.gson.annotations.SerializedName
import com.numplates.nomera3.modules.communities.data.entity.CommunityEntity
import java.io.Serializable


data class UserDetailModel(
    @SerializedName("account_color") val accountColor: Int,
    @SerializedName("account_type") val accountType: Int,
    @SerializedName("vip_expiration") val vipExpiration: Long?,
    @SerializedName("premium_expiration") val premiumExpiration: Long?,
    @SerializedName("anonyms_blocked") val anonymsBlocked: Int,
    @SerializedName("avatar") var avatar: String,
    @SerializedName("avatar_big") val avatarBig: String,
    @SerializedName("birthday") val birthday: Long,
    @SerializedName("blocked") val blocked: Int,
    @SerializedName("blocked_me") val blockedMe: Int,
    @SerializedName("city_name") val cityName: String?,
    @SerializedName("city_id") val cityId: Int?,
    @SerializedName("country_id") var countryId: Int,
    @SerializedName("country_name") var countryName: String,
    @SerializedName("friends_count") val friendsCount: Int,
    @SerializedName("groups_count") val groupsCount: Int,
    @SerializedName("isAdmin") val isAdmin: Boolean,
    @SerializedName("is_friend") val isFriend: Int,
    @SerializedName("vehicles") val vehicle: List<Vehicle>?,
    @SerializedName("friends") val friends: List<UserModel>?,
    @SerializedName("gender") val isMale: Int,
    @SerializedName("mail") val mail: String,
    @SerializedName("name") val name: String,
    @SerializedName("photos") val photos: List<PhotoModel>?,
    @SerializedName("photos_count") val photosCount: Int,
    @SerializedName("posts_count") val postsCount: Int,
    @SerializedName("groups") val communities: List<CommunityEntity>?,
    @SerializedName("push_event") val pushEvent: Int,
    @SerializedName("push_message") val pushMessage: Int,
    @SerializedName("rating") val rating: Int,
    @SerializedName("show_on_map") val showOnMap: Int,
    @SerializedName("state_id") val stateId: Int,
    @SerializedName("status") val status: String,
    @SerializedName("purchase_basket") val purchaseBasket: List<PurchaseModel>?,
    @SerializedName("uid") val uid: Long,
    @SerializedName("user_groups") val userGroups: Int,
    @SerializedName("show_gender") val showGender: Int,
    @SerializedName("show_birthday") val showBirthday: Int,
    @SerializedName("gps_x") val gpsX: Double?,
    @SerializedName("gps_y") val gpsY: Double?,
    var mapState: Int? = null
) : Serializable {


    constructor(userId: Long, avatar: String, color: Int, type: Int, gender: Int, lat: Double, lon: Double) : this(
            accountColor = color,
            accountType = type,
            vipExpiration = 0L,
            premiumExpiration = 0L,
            anonymsBlocked = 0,
            avatar = avatar,
            avatarBig = "",
            birthday = 0L,
            blocked = 0,
            blockedMe = 0,
            cityName = "",
            cityId = 0,
            countryId = 0,
            countryName = "",
            friendsCount = 0,
            groupsCount = 0,
            isAdmin = false,
            isFriend = 0,
            vehicle = null,
            friends = null,
            isMale = 1,
            mail = "",
            name = "",
            photos = null,
            photosCount = 0,
            postsCount = 0,
            communities = null,
            pushEvent = 0,
            pushMessage = 0,
            rating = 0,
            showOnMap = 0,
            stateId = 0,
            status = "",
            purchaseBasket = null,
            uid = userId,
            userGroups = 0,
            showGender = gender,
            showBirthday = 0,
            gpsX = lat,
            gpsY = lon
    )

    constructor(userId: Long, avatar: String, color: Int, type: Int, gender: Int, lat: Double, lon: Double, showOnMap: Int) : this(
            accountColor = color,
            accountType = type,
            vipExpiration = 0L,
            premiumExpiration = 0L,
            anonymsBlocked = 0,
            avatar = avatar,
            avatarBig = "",
            birthday = 0L,
            blocked = 0,
            blockedMe = 0,
            cityName = "",
            cityId = 0,
            countryId = 0,
            countryName = "",
            friendsCount = 0,
            groupsCount = 0,
            isAdmin = false,
            isFriend = 0,
            vehicle = null,
            friends = null,
            isMale = 1,
            mail = "",
            name = "",
            photos = null,
            photosCount = 0,
            postsCount = 0,
            communities = null,
            pushEvent = 0,
            pushMessage = 0,
            rating = 0,
            showOnMap = showOnMap,
            stateId = 0,
            status = "",
            purchaseBasket = null,
            uid = userId,
            userGroups = 0,
            showGender = gender,
            showBirthday = 0,
            gpsX = lat,
            gpsY = lon
    )
}
