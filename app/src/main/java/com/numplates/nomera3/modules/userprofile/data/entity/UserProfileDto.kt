package com.numplates.nomera3.modules.userprofile.data.entity

import com.google.gson.annotations.SerializedName
import com.meera.db.models.moments.UserMomentsDto
import com.meera.db.models.userprofile.MutualUsersEntity
import com.numplates.nomera3.modules.baseCore.data.model.CityDto
import com.numplates.nomera3.modules.baseCore.data.model.CoordinatesDto
import com.numplates.nomera3.modules.baseCore.data.model.CountryDto

data class UserProfileDto(
    @SerializedName("user_id")
    val userId: Long,

    @SerializedName("birthday")
    val birthday: Long? = 0L,

    @SerializedName("birth_date")
    val birthdayFlag: Long? = 0L,

    @SerializedName("show_birthday_button")
    val showBirthdayButton: Long? = 0L,

    @SerializedName("avatar_big")
    val avatarBig: String? = "",

    @SerializedName("avatar_small")
    val avatarSmall: String? = "",

    @SerializedName("account_color")
    val accountColor: Int? = 0,

    @SerializedName("gender")
    val gender: Int? = -1,

    @SerializedName("name")
    val name: String? = "",

    @SerializedName("account_type")
    val accountType: Int? = 0,

    @SerializedName("city")
    val city: CityDto?,

    @SerializedName("country")
    val country: CountryDto?,

    @SerializedName("profile_rating")
    val profileRating: Int? = 0,

    @SerializedName("blacklisted_by_me")
    val blacklistedByMe: Int?,

    @SerializedName("blacklisted_me")
    val blacklistedMe: Int?,

    @SerializedName("friend_status")
    val friendStatus: Int? = 0,

    @SerializedName("friends_count")
    val friendsCount: Int? = 0,

    @SerializedName("vehicles_count")
    val vehiclesCount: Int? = 0,

    @SerializedName("groups_count")
    val groupsCount: Int? = 0,

    @SerializedName("photos_count")
    val photosCount: Int? = 0,

    @SerializedName("posts_count")
    val postsCount: Int? = 0,

    @SerializedName("vehicles")
    val vehicles: List<VehicleDto>? = mutableListOf(),

    @SerializedName("gifts")
    val gifts: List<GiftDto>? = mutableListOf(),

    @SerializedName("gifts_count")
    val giftsCount: Int? = 0,

    @SerializedName("gifts_new_count")
    val giftsNewCount: Int? = 0,

    @SerializedName("photos")
    val photos: List<PhotoDto>? = mutableListOf(),

    @SerializedName("show_on_map")
    val showOnMap: Int? = 0,

    @SerializedName("closed_profile")
    val closedProfile: Int? = 0,

    @SerializedName("can_write_anonymous_messages")
    val canWriteAnonymousMessages: Int? = 0,

    @SerializedName("groups")
    val groups: List<GroupDto>? = mutableListOf(),

    @SerializedName("account_type_expiration")
    val accountTypeExpiration: Long? = -1,

    @SerializedName("profile_deleted")
    val profileDeleted: Int? = 0,

    @SerializedName("profile_blocked")
    val profileBlocked: Int? = 0,

    @SerializedName("profile_verified")
    val profileVerified: Int? = 0,

    @SerializedName("main_vehicle")
    val mainVehicle: VehicleDto?,

    @SerializedName("show_birthday")
    val showBirthday: Int? = 0,

    @SerializedName("coordinates")
    val coordinates: CoordinatesDto?,

    @SerializedName("map_state")
    val mapState: Int?,

    @SerializedName("is_anonym")
    val isAnonym: Int?,

    @SerializedName("membership_type")
    val membershipType: Int?,

    @SerializedName("membership_status")
    val membershipStatus: Int?,

    @SerializedName("hide_birthday")
    var hideBirthday: Int?,

    @SerializedName("hide_gender")
    var hideGender: Int?,

    @SerializedName("settings_flags")
    var settingsFlags: UserSettingsFlagsDto? = null,

    @SerializedName("subscription_count")
    var subscriptionCount: Long = 0,

    @SerializedName("subscribers_count")
    var subscribersCount: Long = 0,

    @SerializedName("friends_request_count")
    var friendsRequestCount: Int? = 0,

    @SerializedName("recover_at")
    var deletedAt: Long? = null,

    @SerializedName("uniqname")
    var uniquename: String,

    @SerializedName("distance")
    var distance: Int? = null,

    @SerializedName("system_admin")
    val isSystemAdmin: Boolean = false,

    @SerializedName("holiday_product")
    val holidayProduct: ProductDto? = null,

    @SerializedName("avatar_animation")
    var avatarAnimation: String? = "",

    @SerializedName("complete")
    val complete: Int? = 0,

    @SerializedName("phone_number")
    val phoneNumber: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("approved")
    val approved: Int = 0,

    @SerializedName("top_content_maker")
    val topContentMaker: Int,

    @SerializedName("show_friends_and_subscribers")
    val showFriendsAndSubscribers: Int? = 0,

    @SerializedName("mutual_users")
    val mutualUsersEntity: MutualUsersEntity? = null,

    @SerializedName("profile_status")
    val profileStatus: String? = null,

    @SerializedName("registration_at")
    val registrationDate: Long? = null,

    @SerializedName("role")
    val role: String? = null,

    @SerializedName("inviter_id")
    val inviterId: Int? = null,

    @SerializedName("country_by_number")
    val countryByNumber: String? = null,

    @SerializedName("moments")
    val moments: UserMomentsDto? = null
)
