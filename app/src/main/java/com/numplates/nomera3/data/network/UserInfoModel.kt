package com.numplates.nomera3.data.network

import android.graphics.Bitmap
import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName
import com.google.maps.android.clustering.ClusterItem
import com.numplates.nomera3.presentation.view.view.ProfileListItem
import java.io.Serializable

data class UserInfoModel(
        @SerializedName("uid") override var uid: Long,
        @SerializedName("number") override var number: String?,
        @SerializedName("name") override var name: String?,
        @SerializedName("avatar") override var avatar: String?,
        @SerializedName("small_avatar") var avatarSmall: String?,
        @SerializedName("middle_avatar") var avatarMiddle: String?,
        @SerializedName("birthday") var birthday: Long?,
        @SerializedName("avatar_date") override var avatarDate: Long,
        @SerializedName("status") var status: String?,
        @SerializedName("phone") var phone: String?,
        @SerializedName("gender") var isMale: Int, //Legacy don't harass females
        @SerializedName("mail") var mail: String?,
        @SerializedName("push_message") var pushMessage: Int,
        @SerializedName("push_event") var pushEvent: Int,
        @SerializedName("show_on_map") var showOnMap: Int,
        @SerializedName("show_birthday") var showBirthday: Int,
        @SerializedName("city_id") var cityId: Int,
        @SerializedName("vehicle") override var vehicle: Int,
        @SerializedName("approved") var approved: Int = 0,
        @SerializedName("top_content_maker") var topContentMaker: Int?,
        @SerializedName("purchases") var purchases: Int,
        @SerializedName("country_id") var countryId: Int,
        @SerializedName("country_name") var countryName: String,
        @SerializedName("filter_city_id") var filterCityId: Int,
        @SerializedName("blocked") var blocked: Int,
        @SerializedName("driver") override var driver: Int,
        @SerializedName("verified") var verified: Int,
        @SerializedName("is_friend") var isFriend: Int,
        @SerializedName("count_posts") var countPosts: Int,
        @SerializedName("user_groups") var userGroups: Int,
        @SerializedName("anonyms_blocked") var anonymsBlocked: Int,
        @SerializedName("state_id") var stateId: Int,
        @SerializedName("groups_approved") var groupsApproved: Int,
        @SerializedName("road_hidden") var roadHidden: Int,
        @SerializedName("account_color")override  var accountColor: Int,
        @SerializedName("account_type") override var accountType: Int,
        @SerializedName("purchase_basket") var purchaseBasket: List<Purchase?>?,
        @SerializedName("avatar_big") var avatarBig: String?,
        @SerializedName("filter_countries") var filterCountries: List<Any?>?,
        @SerializedName("filter_group_countries") var filterGroupCountries: List<Any?>?,
        @SerializedName("city_name") var cityName: String?,
        @SerializedName("photos") var photos: PhotoGalleryModel?,
        @SerializedName("state_time") var stateTime: Long,
        @SerializedName("state_duration") var stateDuration: Int,
        @SerializedName("state_counter") var stateCounter: Int,
        @SerializedName("user_status") var userStatus: Int,
        @SerializedName("sort_id") var sortId: Long,
        @SerializedName("confirmed") var confirmed: Int,
        @SerializedName("map_activity") var mapActivity: Long,
        @SerializedName("user_birthday") var userBirthday: Long,
        @SerializedName("is_author") var isAuthor: Boolean? = false,
        @SerializedName("is_moderator") var isModerator: Boolean? = false,
        @SerializedName("uniqname") var uniqname: String? = "id12345678",
        var bitmap: Bitmap?

) : SimpleUser(),

        ClusterItem, Serializable, ProfileListItem {
    override fun getTitle(): String {
        return name!!
    }

    override val caption: String?
        get() = name
    override val imageUrl: String?
        get() = avatar
    override val num: String?
        get() = number

    override fun getSnippet(): String? {
        return name
    }


    override fun getPosition(): LatLng {
        return LatLng(gpsX, gpsY)
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false
        other as UserInfoModel
        if (uid != other.uid) return false
        return true
    }
}



