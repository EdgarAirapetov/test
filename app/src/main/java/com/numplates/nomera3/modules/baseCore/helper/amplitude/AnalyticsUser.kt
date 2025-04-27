package com.numplates.nomera3.modules.baseCore.helper.amplitude

import android.os.Parcelable
import com.meera.core.extensions.toBoolean
import com.meera.core.utils.getAge
import com.numplates.nomera3.ZERO_VALUE
import com.numplates.nomera3.data.network.core.INetworkValues
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserProfileModel
import com.yandex.metrica.profile.Attribute
import com.yandex.metrica.profile.GenderAttribute
import com.yandex.metrica.profile.UserProfile
import kotlinx.parcelize.Parcelize
import org.json.JSONObject
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale

const val DEVICE_ID = "device id"
const val GEO_ENABLED = "geo enabled"
const val CITY = "city"
const val COUNTRY = "country"
const val USER_PIC = "user pic"
const val NUM_OF_POSTS = "num of posts"
const val NUM_OF_COMMUNITY = "num of community"
const val NUM_OF_FRIENDS = "num of friends"
const val NUM_OF_FOLLOWERS = "num of followers"
const val NUM_OF_FOLLOWS = "num of follows"
const val DATE_OF_BIRTH = "date of birth"
const val ANONIM = "anonim"
const val VIP = "VIP"
const val NUM_OF_PHOTOS = "num of photos"
const val HAVE_TRANSPORT = "have transport"
const val PRIVACY_ABOUT_ME = "privacy about me"
const val PRIVACY_GARAGE = "privacy garage"
const val PRIVACY_GIFTS = "privacy gifts"
const val PRIVACY_SELF_ROAD = "privacy self road"
const val PRIVACY_MAP = "privacy map visibility"
const val MAP_PEOPLE_VISIBILITY = "map people visibility"
const val MAP_EVENTS_VISIBILITY = "map events visibility"
const val MAP_FRIENDS_VISIBILITY = "map friends visibility"
const val MAP_FILTER_SHOW_FRIENDS_ONLY = "map filter show friends"
const val MAP_FILTER_SHOW_MEN = "map filter men"
const val MAP_FILTER_SHOW_WOMEN = "map filter women"
const val USERNAME = "username"
const val GENDER = "gender"
const val INFLUENCER = "influencer"
const val HAVE_STATUS = "have status"
const val PUSH_PERMISSION = "push permission"
const val PUSH_ENABLED = "push enabled"
const val LAST_ONLINE_DATE = "last online date"
const val REGISTRATION_DATE = "registration date"
const val HOT_AUTHOR = "hot author"
const val CONTACTS_PERMISSION = "contacts permission"
const val PRIVACY_SYNC_CONTACTS = "privacy sync contacts"
const val SHAKE_SIGN = "shake sign"
const val AUTO_REC_SYSTEM_CHANGE = "automatically rec sys change"
const val REC_SYSTEM_AVAILABLE = "rec sys available"
const val REC_SYSTEM_ACTION = "rec sys act"
const val COUNT_MUTUAL_AUDIENCE = "count mutual audience"
const val HAVE_VISIBILITY_MUTUAL_AUDIENCE = "have visibility mutual audience"
const val PRIVACY_SCREENSHOT_SHARE_ACTION = "privacy screenshot share act"

const val UNKNOWN = "unknown"

const val UNIX_MULTIPLAYER = 1000

@Parcelize
data class AnalyticsUser(
    val userId: String,
    val deviceId: String? = null,
    val gender: UserGender,
    val isAnon: Boolean,
    val vipStatus: UserVIPStatus,
    val numOfPhotos: Int? = null,
    val haveTransport: Boolean? = null,
    val userPicture: UserPictureEnum,
    var geoEnabled: Boolean? = null,
    val cityName: String? = null,
    val countryName: String? = null,
    val registrationNumberCountry: String? = null,
    val numberOfPost: Int? = null,
    val numberOfFriends: Int? = null,
    val numberOfFollowers: Long? = null,
    val numberOfFollows: Long? = null,
    val numberOfCommunity: Int? = null,
    val userName: String? = null,
    val dateOfBirth: String? = null,
    val age: Int? = null,
    val userPrivacy: UserPrivacy? = null,
    val mapFilters: MapFilters? = null,
    val isApproved: Boolean,
    val haveStatus: Boolean = false,
    val pushPermitted: Boolean = false,
    val pushEnabled: Boolean = false,
    val userOnlineDate: String? = null,
    val registrationDate: String? = null,
    val contactsPermission: Boolean = false,
    val hotAuthor: Boolean = false,
    val autoRecSystemChange: Boolean?,
    val recSystemAvailable: Boolean?,
    val isRecommendedSystemSelected: Boolean,
    val privacyScreenshotShareAct: Boolean
) : Parcelable

@Parcelize
data class MapFilters(
    val mapFilterShowFriendsOnly: FilterState? = null,
    val mapFilterShowMen: FilterState? = null,
    val mapFilterShowWomen: FilterState? = null,
    val mapShowPeople: Boolean? = null,
    val mapShowEvents: Boolean? = null,
    val mapShowFriends: Boolean? = null,
) : Parcelable

enum class FilterState(val value: String) {
    ON("on"),
    OFF("off");

    companion object {
        fun valueOf(value: Boolean): FilterState {
            return if (value) ON else OFF
        }
    }
}

@Parcelize
data class UserPrivacy(
    val privacyAboutMe: String? = null,
    val privacyGarage: String? = null,
    val privacyGifts: String? = null,
    val privacySelfRoad: String? = null,
    val privacyMap: String? = null,
    val shakeSign: String? = null,
    val privacySyncContacts: String? = null
) : Parcelable

enum class UserPictureEnum(val type: String) {
    IMAGE("img"),
    AVATAR("avatar"),
    UNKNOWN("unknown")
}

enum class UserGender(val type: String) {
    MEN("men"),
    WOMAN("woman"),
    UNKNOWN("unknown")
}

enum class UserVIPStatus(val type: String) {
    SILVER("silver"),
    GOLD("gold"),
    NO("no")
}


fun AnalyticsUser.toAppMetricaUser(): UserProfile {
    return UserProfile.newBuilder()
        .apply(Attribute.name().withValue(userName ?: UNKNOWN))
        .apply(Attribute.birthDate().withAge(age ?: -1))
        .apply(
            Attribute.gender().withValue(
                when (gender) {
                    UserGender.MEN -> GenderAttribute.Gender.MALE
                    UserGender.WOMAN -> GenderAttribute.Gender.FEMALE
                    else -> GenderAttribute.Gender.OTHER
                }
            )
        )
        .apply(Attribute.customString(DEVICE_ID).withValue(deviceId ?: UNKNOWN))
        .apply(Attribute.customString(GEO_ENABLED).withValue(geoEnabled?.toString() ?: UNKNOWN))
        .apply(Attribute.customString(CITY).withValue(cityName ?: UNKNOWN))
        .apply(Attribute.customString(COUNTRY).withValue(countryName ?: UNKNOWN))
        .apply(Attribute.customString(USER_PIC).withValue(userPicture.type))
        .apply(Attribute.customNumber(NUM_OF_POSTS).withValue(numberOfPost?.toDouble() ?: 0.0))
        .apply(Attribute.customNumber(NUM_OF_FRIENDS).withValue(numberOfFriends?.toDouble() ?: 0.0))
        .apply(Attribute.customNumber(NUM_OF_FOLLOWERS).withValue(numberOfFollowers?.toDouble() ?: 0.0))
        .apply(Attribute.customNumber(NUM_OF_FOLLOWS).withValue(numberOfFollows?.toDouble() ?: 0.0))
        .apply(Attribute.customNumber(NUM_OF_COMMUNITY).withValue(numberOfCommunity?.toDouble() ?: 0.0))
        .apply(Attribute.customNumber(NUM_OF_FRIENDS).withValue(numberOfFriends?.toDouble() ?: 0.0))
        .apply(Attribute.customString(ANONIM).withValue(isAnon.toString()))
        .apply(Attribute.customString(VIP).withValue(vipStatus.type))
        .apply(Attribute.customNumber(NUM_OF_PHOTOS).withValue(numOfPhotos?.toDouble() ?: 0.0))
        .apply(Attribute.customBoolean(HAVE_TRANSPORT).withValue(haveTransport ?: false))
        .apply(Attribute.customString(PRIVACY_ABOUT_ME).withValue(userPrivacy?.privacyAboutMe ?: UNKNOWN))
        .apply(Attribute.customString(PRIVACY_GARAGE).withValue(userPrivacy?.privacyGarage ?: UNKNOWN))
        .apply(Attribute.customString(PRIVACY_GIFTS).withValue(userPrivacy?.privacyGifts ?: UNKNOWN))
        .apply(Attribute.customString(PRIVACY_SELF_ROAD).withValue(userPrivacy?.privacySelfRoad ?: UNKNOWN))
        .apply(Attribute.customString(PRIVACY_MAP).withValue(userPrivacy?.privacyMap ?: UNKNOWN))
        .apply(Attribute.customString(MAP_PEOPLE_VISIBILITY).withValue(mapFilters?.mapShowPeople?.toString() ?: UNKNOWN))
        .apply(Attribute.customString(MAP_EVENTS_VISIBILITY).withValue(mapFilters?.mapShowEvents?.toString() ?: UNKNOWN))
        .apply(Attribute.customString(MAP_FRIENDS_VISIBILITY).withValue(mapFilters?.mapShowFriends?.toString() ?: UNKNOWN))
        .apply(Attribute.customString(PRIVACY_SYNC_CONTACTS).withValue(userPrivacy?.privacySyncContacts ?: UNKNOWN))
        .apply(Attribute.customString(SHAKE_SIGN).withValue(userPrivacy?.shakeSign ?: UNKNOWN))
        .apply(
            Attribute.customString(MAP_FILTER_SHOW_FRIENDS_ONLY)
                .withValue(mapFilters?.mapFilterShowFriendsOnly?.value ?: UNKNOWN)
        )
        .apply(Attribute.customString(MAP_FILTER_SHOW_MEN).withValue(mapFilters?.mapFilterShowMen?.value ?: UNKNOWN))
        .apply(
            Attribute.customString(MAP_FILTER_SHOW_WOMEN).withValue(mapFilters?.mapFilterShowWomen?.value ?: UNKNOWN)
        )
        .apply(Attribute.customString(INFLUENCER).withValue(isApproved.toString()))
        .apply(Attribute.customString(HAVE_STATUS).withValue(haveStatus.toString()))
        .apply(Attribute.customBoolean(PUSH_PERMISSION).withValue(pushPermitted))
        .apply(Attribute.customBoolean(PUSH_ENABLED).withValue(pushEnabled))
        .apply(Attribute.customString(LAST_ONLINE_DATE).withValue(userOnlineDate ?: UNKNOWN))
        .apply(Attribute.customString(REGISTRATION_DATE).withValue(registrationDate ?: UNKNOWN))
        .apply(Attribute.customString(HOT_AUTHOR).withValue(hotAuthor.toString()))
        .apply(Attribute.customString(CONTACTS_PERMISSION).withValue(contactsPermission.toString()))
        .apply(Attribute.customString(REC_SYSTEM_AVAILABLE).withValue(recSystemAvailable.toString()))
        .apply(Attribute.customString(AUTO_REC_SYSTEM_CHANGE).withValue((autoRecSystemChange ?: UNKNOWN).toString()))
        .apply(Attribute.customString(REC_SYSTEM_ACTION).withValue((isRecommendedSystemSelected ?: UNKNOWN).toString()))
        .build()
}

fun AnalyticsUser.toAmplitudeUser(): JSONObject {
    return JSONObject().apply {
        put(USERNAME, userName ?: UNKNOWN)
        put(DATE_OF_BIRTH, dateOfBirth)
        put(
            GENDER, when (gender) {
                UserGender.MEN -> GenderAttribute.Gender.MALE
                UserGender.WOMAN -> GenderAttribute.Gender.FEMALE
                else -> GenderAttribute.Gender.OTHER
            }
        )
        put(DEVICE_ID, deviceId ?: UNKNOWN)
        put(GEO_ENABLED, geoEnabled?.toString() ?: UNKNOWN)
        put(CITY, cityName ?: UNKNOWN)
        put(COUNTRY, countryName ?: UNKNOWN)
        put(USER_PIC, userPicture.type)
        put(NUM_OF_POSTS, numberOfPost ?: UNKNOWN)
        put(NUM_OF_FRIENDS, numberOfFriends?.toString() ?: UNKNOWN)
        put(NUM_OF_FOLLOWERS, numberOfFollowers?.toString() ?: UNKNOWN)
        put(NUM_OF_FOLLOWS, numberOfFollows?.toString() ?: UNKNOWN)
        put(NUM_OF_COMMUNITY, numberOfCommunity?.toString() ?: UNKNOWN)
        put(NUM_OF_FRIENDS, numberOfFriends?.toString() ?: UNKNOWN)
        put(ANONIM, isAnon.toString())
        put(VIP, vipStatus.type)
        put(NUM_OF_PHOTOS, numOfPhotos?.toString() ?: UNKNOWN)
        put(HAVE_TRANSPORT, haveTransport?.toString() ?: UNKNOWN)
        put(PRIVACY_ABOUT_ME, userPrivacy?.privacyAboutMe ?: UNKNOWN)
        put(PRIVACY_GARAGE, userPrivacy?.privacyGarage ?: UNKNOWN)
        put(PRIVACY_GIFTS, userPrivacy?.privacyGifts ?: UNKNOWN)
        put(PRIVACY_SELF_ROAD, userPrivacy?.privacySelfRoad ?: UNKNOWN)
        put(PRIVACY_MAP, userPrivacy?.privacyMap ?: UNKNOWN)
        put(MAP_PEOPLE_VISIBILITY, mapFilters?.mapShowPeople?.toString() ?: UNKNOWN)
        put(MAP_EVENTS_VISIBILITY, mapFilters?.mapShowEvents?.toString() ?: UNKNOWN)
        put(MAP_FRIENDS_VISIBILITY, mapFilters?.mapShowFriends?.toString() ?: UNKNOWN)
        put(PRIVACY_SYNC_CONTACTS, userPrivacy?.privacySyncContacts ?: UNKNOWN)
        put(SHAKE_SIGN, userPrivacy?.shakeSign ?: UNKNOWN)
        put(MAP_FILTER_SHOW_FRIENDS_ONLY, mapFilters?.mapFilterShowFriendsOnly?.value ?: UNKNOWN)
        put(MAP_FILTER_SHOW_MEN, mapFilters?.mapFilterShowMen?.value ?: UNKNOWN)
        put(MAP_FILTER_SHOW_WOMEN, mapFilters?.mapFilterShowWomen?.value ?: UNKNOWN)
        put(INFLUENCER, isApproved)
        put(HOT_AUTHOR, hotAuthor)
        put(CONTACTS_PERMISSION, contactsPermission)
        put(REC_SYSTEM_AVAILABLE, recSystemAvailable)
        put(REC_SYSTEM_ACTION, isRecommendedSystemSelected)
        put(PRIVACY_SCREENSHOT_SHARE_ACTION, privacyScreenshotShareAct)
        autoRecSystemChange?.let {
            put(AUTO_REC_SYSTEM_CHANGE, it)
        }
    }
}

fun createAnonUser(
    deviceId: String? = null,
    contactsPermission: Boolean
): AnalyticsUser {
    return AnalyticsUser(
        userId = ZERO_VALUE,
        deviceId = deviceId,
        gender = UserGender.UNKNOWN,
        isAnon = true,
        vipStatus = UserVIPStatus.NO,
        userPicture = UserPictureEnum.UNKNOWN,
        isApproved = false,
        hotAuthor = false,
        contactsPermission = contactsPermission,
        autoRecSystemChange = null,
        recSystemAvailable = false,
        isRecommendedSystemSelected = false,
        privacyScreenshotShareAct = true
    )
}

fun UserProfileModel.toAnalyticsUser(
    isAnon: Boolean,
    deviceId: String?,
    geoEnabled: Boolean,
    userPrivacy: UserPrivacy?,
    mapFilters: MapFilters?,
    pushEnabled: Boolean,
    pushPermitted: Boolean,
    userOnlineDate: String,
    formatter: SimpleDateFormat,
    hasContactsPermission: Boolean,
    recSystemAvailable: Boolean?,
    autoRecSystemChanged: Boolean?,
    isRecommendedSystemSelected: Boolean,
    isShareScreenshotEnabled: Boolean
): AnalyticsUser {
    val age = try {
        Integer.parseInt(getAge(birthday ?: 0))
    } catch (e: Exception) {
        Timber.e(e)
        null
    }
    val dateOfBirth = birthday?.let { birth ->
        val format = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        format.format(birth * UNIX_MULTIPLAYER)
    } ?: kotlin.run { UNKNOWN }
    val userPictureType = when {
        avatarAnimation?.isNotEmpty() == true -> UserPictureEnum.AVATAR
        !avatarBig.isNullOrEmpty() || !avatarSmall.isNullOrEmpty() -> UserPictureEnum.IMAGE
        else -> UserPictureEnum.UNKNOWN
    }
    return AnalyticsUser(
        userId = userId.toString(),
        deviceId = deviceId,
        gender = if (gender == 1) UserGender.MEN else if (gender == 0) UserGender.WOMAN else UserGender.UNKNOWN,
        isAnon = isAnon,
        vipStatus = when (accountType) {
            INetworkValues.ACCOUNT_TYPE_VIP -> {
                UserVIPStatus.GOLD
            }
            INetworkValues.ACCOUNT_TYPE_PREMIUM -> {
                UserVIPStatus.SILVER
            }
            else -> {
                UserVIPStatus.NO
            }
        },
        haveTransport = vehiclesCount?.let { it > 0 } ?: kotlin.run { false },
        userPicture = userPictureType,
        geoEnabled = geoEnabled,
        cityName = coordinates?.cityName,
        countryName = coordinates?.countryName,
        registrationNumberCountry = null,
        numberOfPost = postsCount,
        numberOfFriends = friendsCount,
        numberOfFollowers = subscribersCount,
        numberOfFollows = subscriptionCount,
        numberOfCommunity = groupsCount,
        numOfPhotos = photosCount ?: 0,
        userName = name,
        dateOfBirth = dateOfBirth,
        age = age,
        userPrivacy = userPrivacy,
        mapFilters = mapFilters,
        isApproved = approved == 1,
        haveStatus = !profileStatus.isNullOrEmpty(),
        pushPermitted = pushPermitted,
        pushEnabled = pushEnabled,
        userOnlineDate = userOnlineDate,
        registrationDate = registrationDate?.let {
            formatter.format(it * UNIX_MULTIPLAYER)
        },
        hotAuthor = topContentMaker.toBoolean(),
        contactsPermission = hasContactsPermission,
        recSystemAvailable = recSystemAvailable,
        autoRecSystemChange = autoRecSystemChanged,
        isRecommendedSystemSelected = isRecommendedSystemSelected,
        privacyScreenshotShareAct = isShareScreenshotEnabled
    )
}
