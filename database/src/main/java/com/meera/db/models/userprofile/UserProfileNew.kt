package com.meera.db.models.userprofile

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import com.meera.db.models.moments.UserMomentsDto


/**
 * @property blacklistedByMe Blocked other user or not (for unblock) => NOT My profile
 * @property blacklistedMe // Guest user blocked or not => NOT My profile
 * @property birthday 0, // Long (Optional) UnixTime
 * @property accountTypeExpiration -1, // Long (Optional) UnixTime в секундах
 * @property membershipType 0, // Int (Optional) [Пользователь, Создатель, Админ]
 * @property membershipStatus 0 // Int (Optional) [подал заявку, одобрен, отклонен, и тд]
 * @property hideBirthday 0, // Int (Optional) // поле birthday для своего пользователя будет приходить всегда, показывать или нет будем решать по этому поле
 * @property hideGender 0, // Int (Optional)// то же самое, что и с полем birthday
 */
@Entity(tableName = "user_profile")
data class UserProfileNew(

    @NonNull
    @PrimaryKey
    @SerializedName("user_id")
    @ColumnInfo(name = "id")
    val userId: Long,

    @SerializedName("birthday")
    @ColumnInfo(name = "birthday")
    val birthday: Long? = 0L,

    @SerializedName("birth_date")
    @ColumnInfo(name = "birth_date")
    val birthdayFlag: Long? = 0L,

    @SerializedName("show_birthday_button")
    @ColumnInfo(name = "show_birthday_button")
    val showBirthdayButton: Long? = 0L,

    @SerializedName("avatar_big")
    @ColumnInfo(name = "avatar_big")
    val avatarBig: String? = "",

    @SerializedName("avatar_small")
    @ColumnInfo(name = "avatar_small")
    val avatarSmall: String? = "",

    @SerializedName("account_color")
    @ColumnInfo(name = "account_color")
    val accountColor: Int? = 0,

    @SerializedName("gender")
    @ColumnInfo(name = "gender")
    val gender: Int? = -1,

    @SerializedName("name")
    @ColumnInfo(name = "name")
    val name: String? = "",

    @SerializedName("account_type")
    @ColumnInfo(name = "account_type")
    val accountType: Int? = 0,

    @SerializedName("city")
    @ColumnInfo(name = "city")
    val city: City?,                        // +

    @SerializedName("country")
    @ColumnInfo(name = "country")
    val country: Country?,              // +

    @SerializedName("profile_rating")
    @ColumnInfo(name = "profile_rating")
    val profileRating: Int? = 0,

    @SerializedName("blacklisted_by_me")
    val blacklistedByMe: Int?,

    @SerializedName("blacklisted_me")
    val blacklistedMe: Int?,

    @SerializedName("friend_status")
    @ColumnInfo(name = "friend_status")
    val friendStatus: Int? = 0,

    @SerializedName("friends_count")
    @ColumnInfo(name = "friends_count")
    val friendsCount: Int? = 0,

    @SerializedName("vehicles_count")
    @ColumnInfo(name = "vehicles_count")
    val vehiclesCount: Int? = 0,

    @SerializedName("groups_count")
    @ColumnInfo(name = "groups_count")
    val groupsCount: Int? = 0,

    @SerializedName("photos_count")
    @ColumnInfo(name = "photos_count")
    val photosCount: Int? = 0,

    @SerializedName("posts_count")
    @ColumnInfo(name = "posts_count")
    val postsCount: Int? = 0,

    @SerializedName("vehicles")
    val vehicles: List<VehicleEntity>? = mutableListOf(),

    @SerializedName("gifts")
    val gifts: List<GiftEntity>? = mutableListOf(),

    @SerializedName("gifts_count")
    val giftsCount: Int? = 0,

    @SerializedName("gifts_new_count")
    val giftsNewCount: Int? = 0,

    @SerializedName("photos")
    val photos: List<PhotoEntity>? = mutableListOf(),

    @SerializedName("show_on_map")
    @ColumnInfo(name = "show_on_map")
    val showOnMap: Int? = 0,

    @SerializedName("can_write_anonymous_messages")
    @ColumnInfo(name = "can_write_anonymous_messages")
    val canWriteAnonymousMessages: Int? = 0,

    @SerializedName("groups")
    val groups: List<GroupEntity>? = mutableListOf(),

    @SerializedName("account_type_expiration")
    val accountTypeExpiration: Long? = -1,

    @SerializedName("profile_deleted")
    val profileDeleted: Int? = 0,

    @SerializedName("profile_blocked")
    val profileBlocked: Int? = 0,

    @SerializedName("profile_verified")
    val profileVerified: Int? = 0,

    @SerializedName("main_vehicle")
    val mainVehicle: VehicleEntity?,

    @SerializedName("show_birthday")
    val showBirthday: Int? = 0,

    @SerializedName("coordinates")
    val coordinates: Coordinates?,

    @SerializedName("map_state")
    val mapState: Int?,

    @SerializedName("is_anonym")
    val isAnonym: Int?,

    @SerializedName("membership_type")
    val membershipType: Int?,

    @SerializedName("membership_status")
    val membershipStatus: Int?,

    @SerializedName("hide_birthday")
    @ColumnInfo(name = "hide_birthday")
    var hideBirthday: Int?,

    @SerializedName("hide_gender")
    @ColumnInfo(name = "hide_gender")
    var hideGender: Int?,

    @SerializedName("settings_flags")
    @ColumnInfo(name = "settings_flags")
    var settingsFlags: UserSettingsFlags? = null,

    @SerializedName("subscription_count")
    @ColumnInfo(name = "subscription_count")
    var subscriptionCount: Long = 0,

    @SerializedName("subscribers_count")
    @ColumnInfo(name = "subscribers_count")
    var subscribersCount: Long = 0,

    @SerializedName("friends_request_count")
    @ColumnInfo(name = "friends_request_count")
    var friendsRequestCount: Int? = 0,

    @SerializedName("recover_at") //- дата окончания возможности восстановить
    @ColumnInfo(name = "deleted_at")
    var deletedAt: Long? = null,

    @SerializedName("uniqname")
    @ColumnInfo(name = "uniqname")
    var uniquename: String,

    @SerializedName("distance")
    @ColumnInfo(name = "distance")
    var distance: Int? = null,

    @SerializedName("system_admin")
    val isSystemAdmin: Boolean = false,

    @SerializedName("holiday_product")
    val holidayProduct: ProductEntity? = null,

    @SerializedName("avatar_animation")
    @ColumnInfo(name = "avatar_animation")
    var avatarAnimation: String? = "",

    @SerializedName("complete")
    @ColumnInfo(name = "complete")
    val complete: Int? = 0,

    @SerializedName("phone_number")
    @ColumnInfo(name = "phone_number")
    val phoneNumber: String? = null,

    @SerializedName("email")
    @ColumnInfo(name = "email")
    val email: String? = null,

    @SerializedName("approved")
    @ColumnInfo(name = "approved")
    val approved: Int = 0,

    @SerializedName("top_content_maker")
    @ColumnInfo(name = "top_content_maker")
    val topContentMaker: Int,

    @SerializedName("show_friends_and_subscribers")
    @ColumnInfo(name = "show_friends_and_subscribers")
    val showFriendsAndSubscribers: Int? = 0,

    @SerializedName("mutual_users")
    @ColumnInfo(name = "mutual_users")
    val mutualUsersEntity: MutualUsersEntity? = null,

    @SerializedName("event_count")
    @ColumnInfo(name = "event_count")
    val eventCount: Int = 0,

    @SerializedName("profile_status")
    @ColumnInfo(name = "profile_status")
    val profileStatus: String? = null,

    @SerializedName("registration_at")
    @ColumnInfo(name = "registration_at")
    val registrationDate: Long? = null,

    @SerializedName("role")
    @ColumnInfo(name = "role")
    val role: String? = null,

    @SerializedName("inviter_id")
    val inviterId: Int? = null,

    @SerializedName("country_by_number")
    val countryByNumber: String? = null,

    @ColumnInfo(name = "moments")
    @SerializedName("moments")
    val moments: UserMomentsDto? = null
)

fun UserProfileNew.isProfileFilled(): Boolean {
    return (name != null
        && name.isNotEmpty()
        && birthday != null
        && country?.id != null
        && country.id != 0L
        && city?.id != null
        && city.id != 0L
        && complete == 1)
}

fun UserProfileNew.isProfileDeleted(): Boolean {
    return this.profileDeleted == 1
}

fun UserProfileNew.isRegistrationCompleted(): Boolean {
    return this.complete == 1
}

val UserProfileNew.userRole: UserRole
    get() = when (role) {
        UserRole.USER.value -> UserRole.USER
        UserRole.ANNOUNCE_USER.value -> UserRole.ANNOUNCE_USER
        UserRole.SUPPORT_USER.value -> UserRole.SUPPORT_USER
        UserRole.SYSTEM_ADMIN.value -> UserRole.SYSTEM_ADMIN
        else -> UserRole.USER
    }
