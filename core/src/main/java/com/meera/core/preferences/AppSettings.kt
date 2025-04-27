package com.meera.core.preferences

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.f2prateek.rx.preferences2.BuildConfig
import com.f2prateek.rx.preferences2.Preference
import com.google.gson.Gson
import com.meera.core.common.DEFAULT_KEYBOARD_HEIGHT_PX
import com.meera.core.common.PREF_KEY_RTCP_MUX_POLICY
import com.meera.core.common.PREF_KEY_TCP_CANDIDATE_POLICY
import com.meera.core.extensions.empty
import com.meera.core.preferences.datastore.PreferenceDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Locale

/**
 *
 * TODO: Возможны потенциальные проблемы с DataStore preferences https://nomera.atlassian.net/browse/BR-17697
 */

class AppSettings(
    private val context: Context,
    private val gson: Gson,
    private val dataStore: PreferenceDataStore,
    private val prefManager: PrefManager
) {

    // TODO: https://nomera.atlassian.net/browse/BR-26059 Рефакторинг
    // TODO: remove https://nomera.atlassian.net/browse/BR-22488
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    init {
        // TODO: remove https://nomera.atlassian.net/browse/BR-22488
        if (prefManager.getBoolean(IS_NEED_TO_MIGRATE_TO_PREF, true)) {
            migratePrefs()
            prefManager.putValue(IS_NEED_TO_MIGRATE_TO_PREF, false)
        }
    }

    val firstLogin by lazy {
        prefManager.getBoolean(KEY_FIRST_LOGIN, true)
    }

    data class UserCallCounters(
        val counterBlacklist: Int,
        val counterWhitelist: Int
    )

    /////

    private val editor: SharedPreferences.Editor
        get() = PreferenceManager.getDefaultSharedPreferences(context).edit()

    var accessTokenChangeListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    private fun migratePrefs() {
        prefs.all.entries.forEach {
            prefManager.putValue(it.key, it.value)
        }
    }

    suspend fun clearPreferences() {
        val isNeedMigrate = prefManager.getBoolean(IS_NEED_TO_MIGRATE_TO_PREF, true)
        val isShownProfanityTemp = readIsShownProfanity()
        val isShownCallsTemp = readIsShownCalls()
        val appVerNameTemp = readAppVerName()
        val isNeedToShowOnBoardingTemp = readNeedOnBoarding()
        val isFirstTimeOpenAppTemp = readIsFirstTimeOpenApp()
        val holidayCalendarShownToUserWithIdTemp = holidayCalendarShownToUserWithId
        val holidayCalendarShowDateTemp = holidayCalendarShowDate
        val lastRecodedAppCodeTemp = lastRecordedAppCode
        val onBoardingWelcomeTemp = readOnBoardingWelcomeShowed()
        val peopleOnboardingTemp = isPeopleOnboardingShown
        val peopleSavedBadge = needShowPeopleBadge

        val beagleLang = readLastBeagleUrl()
        val baseUrl = readLastBaseUrl()
        val socketUrl = readLastBaseUrlSocket()

        val isOpenRoadFilterTooltipWasShownTimesTemp = isOpenRoadFilterTooltipWasShownTimes
        val isAutoRecSysChangedTemp = isAutoRecSystemChangedSingle
        val isMapEventsOnboardingShownTemp = readMapEventsOnboardingShown()

        editor.clear().commit()
        prefManager.clearSuspend()
        dataStore.clearAll()

        prefManager.putValue(IS_NEED_TO_MIGRATE_TO_PREF, isNeedMigrate)
        writeIsShownCalls(isShownCallsTemp)

        writeLastBeagleUrl(beagleLang)
        writeLastBaseUrl(baseUrl)
        writeLastBaseUrlSocket(socketUrl)

        writeIsShownProfanity(isShownProfanityTemp)
        writeAppVerName(appVerNameTemp)
        writeNeedOnBoarding(isNeedToShowOnBoardingTemp)
        writeIsFirstTimeOpenApp(isFirstTimeOpenAppTemp)
        writeOnBoardingWelcomeShowed(onBoardingWelcomeTemp)
        holidayCalendarShownToUserWithId = holidayCalendarShownToUserWithIdTemp
        holidayCalendarShowDate = holidayCalendarShowDateTemp
        lastRecordedAppCode = lastRecodedAppCodeTemp
        isPeopleOnboardingShown = peopleOnboardingTemp
        needShowPeopleBadge = peopleSavedBadge
        isOpenRoadFilterTooltipWasShownTimes = isOpenRoadFilterTooltipWasShownTimesTemp
        isAutoRecSystemChangedSingle = isAutoRecSysChangedTemp
        if (isMapEventsOnboardingShownTemp) writeMapEventsOnboardingShown()
    }

    var supportUserId: Long?
        get() {
            return prefManager.getLong(KEY_SUPPORT_USER_ID)
        }
        set(value) {
            prefManager.putValue(KEY_SUPPORT_USER_ID, value)
        }

    var holidayId: Long
        get() {
            return prefManager.getLong(KEY_HOLIDAY_ID, -1L)
        }
        set(value) {
            prefManager.putValue(KEY_HOLIDAY_ID, value)
        }

    var userExpiresToken: Long
        get() {
            return prefs.getLong(KEY_USER_TOKEN_EXPIRES, -1L)
        }
        set(value) {
            prefManager.putValue(KEY_USER_TOKEN_EXPIRES, value)
        }

    var birthdayFlag: Long
        get() {
            return prefs.getLong(KEY_USER_BIRTHDAY, 0)
        }
        set(value) {
            prefManager.putValue(KEY_USER_BIRTHDAY, value)
        }

    var holidayCode: String
        get() {
            return prefManager.getString(KEY_HOLIDAY_CODE, "") ?: ""
        }
        set(value) {
            prefManager.putValue(KEY_HOLIDAY_CODE, value)
        }

    var userRefreshToken: String
        get() {
            return prefs.getString(KEY_USER_REFRESH_TOKEN_HI_WAY, "") ?: ""
        }
        set(value) {
            prefManager.putValue(KEY_USER_REFRESH_TOKEN_HI_WAY, value)
        }

    /**
     * It used in many different screens.
     * */
    var userAvatarState: String
        get() {
            return prefManager.getString(KEY_USER_AVATAR_STATE, String.empty()) ?: ""
        }
        set(value) {
            prefManager.putValue(KEY_USER_AVATAR_STATE, value)
        }

    var avatar: String?
        get() {
            return prefManager.getString(KEY_USER_AVATAR, String.empty())
        }
        set(value) {
            prefManager.putValue(KEY_USER_AVATAR, value)
        }

    var holidayTitle: String?
        get() {
            return prefManager.getString(KEY_HOLIDAY_TITLE, "") ?: ""
        }
        set(value) {
            prefManager.putValue(KEY_HOLIDAY_TITLE, value)
        }

    var holidayMainButtonActive: String?
        get() {
            return prefManager.getString(KEY_HOLIDAY_MAIN_BUTTON_ACTIVE, "") ?: ""
        }
        set(value) {
            prefManager.putValue(KEY_HOLIDAY_MAIN_BUTTON_ACTIVE, value)
        }

    var holidayMainButtonDefault: String
        get() {
            return prefManager.getString(KEY_HOLIDAY_MAIN_BUTTON_DEFAULT, "") ?: ""
        }
        set(value) {
            prefManager.putValue(KEY_HOLIDAY_MAIN_BUTTON_DEFAULT, value)
        }

    var holidayStartTime: Long
        get() {
            return prefManager.getLong(KEY_HOLIDAY_START, -1L)
        }
        set(value) {
            prefManager.putValue(KEY_HOLIDAY_START, value)
        }

    var preventReceivingAnonymousChat: Boolean
        get() {
            return prefs.getBoolean(KEY_PREVENT_RECEIVING_ANONYMOUS_MESSAGES, false)
        }
        set(value) {
            prefManager.putValue(KEY_PREVENT_RECEIVING_ANONYMOUS_MESSAGES, value)
        }

    var holidayFinishTime: Long
        get() {
            return prefManager.getLong(KEY_HOLIDAY_FINISH, -1L)
        }
        set(value) {
            prefManager.putValue(KEY_HOLIDAY_FINISH, value)
        }

    var holidayOnboardingTitle: String
        get() {
            return prefManager.getString(KEY_HOLIDAY_ONBOARDING_TITLE, "") ?: ""
        }
        set(value) {
            prefManager.putValue(KEY_HOLIDAY_ONBOARDING_TITLE, value)
        }

    var holidayOnboardingDesc: String
        get() {
            return prefManager.getString(KEY_HOLIDAY_ONBOARDING_DESC, "") ?: ""
        }
        set(value) {
            prefManager.putValue(KEY_HOLIDAY_ONBOARDING_DESC, value)
        }

    var holidayOnboardingIcon: String
        get() {
            return prefManager.getString(KEY_HOLIDAY_ONBOARDING_ICON, "") ?: ""
        }
        set(value) {
            prefManager.putValue(KEY_HOLIDAY_ONBOARDING_ICON, value)
        }

    var holidayOnboardingBtn: String
        get() {
            return prefManager.getString(KEY_HOLIDAY_ONBOARDING_BTN_TEXT, "") ?: ""
        }
        set(value) {
            prefManager.putValue(KEY_HOLIDAY_ONBOARDING_BTN_TEXT, value)
        }

    var holidayHatPremium: String
        get() {
            return prefManager.getString(KEY_HOLIDAY_HAT_PREMIUM, "") ?: ""
        }
        set(value) {
            prefManager.putValue(KEY_HOLIDAY_HAT_PREMIUM, value)
        }

    var gender: Int
        get() {
            return prefManager.getInt(KEY_USER_GENDER, 0)
        }
        set(value) {
            prefManager.putValue(KEY_USER_GENDER, value)
        }

    var holidayHatRegular: String
        get() {
            return prefManager.getString(KEY_HOLIDAY_HAT_REGULAR, "") ?: ""
        }
        set(value) {
            prefManager.putValue(KEY_HOLIDAY_HAT_REGULAR, value)
        }

    var holidayHatVip: String
        get() {
            return prefManager.getString(KEY_HOLIDAY_HAT_VIP, "") ?: ""
        }
        set(value) {
            prefManager.putValue(KEY_HOLIDAY_HAT_VIP, value)
        }

    var holidayRoomType: String
        get() {
            return prefManager.getString(KEY_HOLIDAY_ROOM_TYPE, "") ?: ""
        }
        set(value) {
            prefManager.putValue(KEY_HOLIDAY_ROOM_TYPE, value)
        }

    var holidayRoomBgDialog: String
        get() {
            return prefManager.getString(KEY_HOLIDAY_ROOM_DIALOG, "") ?: ""
        }
        set(value) {
            prefManager.putValue(KEY_HOLIDAY_ROOM_DIALOG, value)
        }

    var holidayRoomBgAnon: String
        get() {
            return prefManager.getString(KEY_HOLIDAY_ROOM_ANON, "") ?: ""
        }
        set(value) {
            prefManager.putValue(KEY_HOLIDAY_ROOM_ANON, value)
        }

    var holidayRoomBgGroup: String
        get() {
            return prefManager.getString(KEY_HOLIDAY_ROOM_GROUP, "") ?: ""
        }
        set(value) {
            prefManager.putValue(KEY_HOLIDAY_ROOM_GROUP, value)
        }

    var holidayPrdId: Long
        get() {
            return prefManager.getLong(KEY_HOLIDAY_PRD_ID, -1L)
        }
        set(value) {
            prefManager.putValue(KEY_HOLIDAY_PRD_ID, value)
        }

    var holidayPrdAppleId: String
        get() {
            return prefManager.getString(KEY_HOLIDAY_PRD_APPLE_ID, "") ?: ""
        }
        set(value) {
            prefManager.putValue(KEY_HOLIDAY_PRD_APPLE_ID, value)
        }

    var holidayPrdCustomTitle: String
        get() {
            return prefManager.getString(KEY_HOLIDAY_PRD_CUSTOM_TITLE, "") ?: ""
        }
        set(value) {
            prefManager.putValue(KEY_HOLIDAY_PRD_CUSTOM_TITLE, value)
        }

    var accountColor: Int
        get() {
            return prefs.getInt(KEY_USER_ACCOUNT_COLOR, 0)
        }
        set(value) {
            prefManager.putValue(KEY_USER_ACCOUNT_COLOR, value)
        }

    var userAuthStatus: Int
        get() {
            return prefs.getInt(KEY_USER_AUTH_STATUS, 0)
        }
        set(value) {
            prefManager.putValue(KEY_USER_AUTH_STATUS, value)
        }

    var holidayPrdDesc: String
        get() {
            return prefManager.getString(KEY_HOLIDAY_PRD_DESC, "") ?: ""
        }
        set(value) {
            prefManager.putValue(KEY_HOLIDAY_PRD_DESC, value)
        }

    var holidayPrdImgLink: String
        get() {
            return prefManager.getString(KEY_HOLIDAY_PRD_IMG_LINK, "") ?: ""
        }
        set(value) {
            prefManager.putValue(KEY_HOLIDAY_PRD_IMG_LINK, value)
        }

    var keyboardHeight: Int
        get() {
            return prefs.getInt(KEY_KEYBOARD_HEIGHT, DEFAULT_KEYBOARD_HEIGHT_PX)
        }
        set(value) {
            prefManager.putValue(KEY_KEYBOARD_HEIGHT, value)
        }

    var holidayPrdImgLinkSmall: String
        get() {
            return prefManager.getString(KEY_HOLIDAY_PRD_IMG_LINK_SMALL, "") ?: ""
        }
        set(value) {
            prefManager.putValue(KEY_HOLIDAY_PRD_IMG_LINK_SMALL, value)
        }

    var holidayPrdItunesId: String
        get() {
            return prefManager.getString(KEY_HOLIDAY_PRD_ITUNES_ID, "") ?: ""
        }
        set(value) {
            prefManager.putValue(KEY_HOLIDAY_PRD_ITUNES_ID, value)
        }

    var isNeedShowFriendsFollowersPrivacy: com.meera.core.preferences.datastore.Preference<Boolean>
        get() {
            return dataStore.boolean(KEY_IS_NEED_SHOW_FRIENDS_FOLLOWERS_PRIVACY, false)
        }
        set(value) {
            prefManager.putValue(KEY_IS_NEED_SHOW_FRIENDS_FOLLOWERS_PRIVACY, value)
        }

    var newEvent: com.meera.core.preferences.datastore.Preference<Boolean>
        get() {
            return dataStore.boolean(KEY_EVENT, false)
        }
        set(value) {
            prefManager.putValue(KEY_EVENT, value)
        }

    var holidayPrdPlayId: String
        get() {
            return prefManager.getString(KEY_HOLIDAY_PRD_PLAYMARKER_ID, "") ?: ""
        }
        set(value) {
            prefManager.putValue(KEY_HOLIDAY_PRD_PLAYMARKER_ID, value)
        }

    var holidayPrdType: Long
        get() {
            return prefManager.getLong(KEY_HOLIDAY_PRD_TYPE, -1L)
        }
        set(value) {
            prefManager.putValue(KEY_HOLIDAY_PRD_TYPE, value)
        }

    var isHolidayShowNeeded: Boolean
        get() {
            return prefManager.getBoolean(KEY_IS_HOLIDAY_SHOW_NEEDED, true)
        }
        set(value) {
            prefManager.putValue(KEY_IS_HOLIDAY_SHOW_NEEDED, value)
        }

    var isHolidayIntroduced: Boolean
        get() {
            return prefManager.getBoolean(KEY_IS_HOLIDAY_INTRODUCED, false)
        }
        set(value) {
            prefManager.putValue(KEY_IS_HOLIDAY_INTRODUCED, value)
        }

    var getReferralVip: Boolean
        get() {
            return prefs.getBoolean(GET_REFERAL_VIP, false)
        }
        set(value) {
            prefManager.putValue(GET_REFERAL_VIP, value)
        }

    var showFriendsAndSubscribers: Int
        get() = prefManager.getInt(SHOW_FRIENDS_AND_SUBSCRIBERS, 1)
        set(value) {
            prefManager.putValue(SHOW_FRIENDS_AND_SUBSCRIBERS, value)
        }

    var holidayIntroducedVersion: String?
        get() {
            return prefManager.getString(KEY_IS_HOLIDAY_INTRODUCED_VERSION, null)
        }
        set(value) {
            prefManager.putValue(KEY_IS_HOLIDAY_INTRODUCED_VERSION, value)
        }

    var holidayCalendarShowDate: String?
        get() {
            return prefManager.getString(KEY_HOLIDAY_CALENDAR_SHOW_DATE, null)
        }
        set(value) {
            prefManager.putValue(KEY_HOLIDAY_CALENDAR_SHOW_DATE, value)
        }

    var holidayCalendarStatus: String?
        get() {
            return prefManager.getString(KEY_HOLIDAY_CALENDAR_STATUS, "in_progress") ?: "in_progress"
        }
        set(value) {
            prefManager.putValue(KEY_HOLIDAY_CALENDAR_STATUS, value)
        }

    var holidayCalendarShownToUserWithId: Long
        get() {
            return prefManager.getLong(KEY_HOLIDAY_CALENDAR_SHOWN_TO_USER_ID, -1)
        }
        set(value) {
            prefManager.putValue(KEY_HOLIDAY_CALENDAR_SHOWN_TO_USER_ID, value)
        }

    var holidayCalendarDaysCount: String?
        get() {
            return prefManager.getString(KEY_HOLIDAY_CALENDAR_DAYS_COUNT, null)
        }
        set(value) {
            prefManager.putValue(KEY_HOLIDAY_CALENDAR_DAYS_COUNT, value)
        }

    var isCreateAvatarUserInfoHintShown: Boolean  // тоже самое, что и readAboutUniqueNameHintShown
        get() {
            return prefManager.getBoolean(CREATE_AVATAR_USER_INFO_HINT_SHOWN, false)
        }
        set(value) {
            prefManager.putValue(CREATE_AVATAR_USER_INFO_HINT_SHOWN, value)
        }

    var isCreateAvatarUserPersonalInfoHintShown: Boolean  // тоже самое, что и readAboutUniqueNameHintShown
        get() {
            return prefManager.getBoolean(CREATE_AVATAR_USER_PERSONAL_INFO_HINT_SHOWN, false)
        }
        set(value) {
            prefManager.putValue(CREATE_AVATAR_USER_PERSONAL_INFO_HINT_SHOWN, value)
        }

    var isCreateAvatarRegisterUserHintShown: Boolean  // тоже самое, что и readAboutUniqueNameHintShown
        get() {
            return prefManager.getBoolean(CREATE_AVATAR_REGISTER_USER_HINT_SHOWN, false)
        }
        set(value) {
            prefManager.putValue(CREATE_AVATAR_REGISTER_USER_HINT_SHOWN, value)
        }

    var isRegistrationCompleted: Boolean
        get() {
            return prefManager.getBoolean(KEY_IS_REGISTRATION_COMPLETED, false)
        }
        set(value) {
            prefManager.putValue(KEY_IS_REGISTRATION_COMPLETED, value)
        }

    var isLeakCanaryEnabled: Boolean
        get() {
            return prefManager.getBoolean(KEY_IS_LEAK_CANARY_ENABLED, false)
        }
        set(value) {
            prefManager.putValue(KEY_IS_LEAK_CANARY_ENABLED, value)
        }

    var sessionCounter: Int
        get() {
            return prefManager.getInt(KEY_SESSION_COUNTER, 0)
        }
        set(value) {
            prefManager.putValue(KEY_SESSION_COUNTER, value)
        }

    var isNeedShowBirthdayDialog: com.meera.core.preferences.datastore.Preference<Boolean>
        get() {
            return dataStore.boolean(KEY_DIALOG_BIRTHDAY_SHOWN, false)
        }
        set(value) {
            prefManager.putValue(KEY_DIALOG_BIRTHDAY_SHOWN, value)
        }

    var profileNotification: com.meera.core.preferences.datastore.Preference<Boolean>
        get() {
            return dataStore.boolean(PROFILE_NOTIFICATIONS, false)
        }
        set(value) {
            prefManager.putValue(PROFILE_NOTIFICATIONS, value)
        }

    var profileNotificationAppUpdate: com.meera.core.preferences.datastore.Preference<Boolean>
        get() {
            return dataStore.boolean(PROFILE_NOTIFICATIONS_APP_UPDATE, false)
        }
        set(value) {
            prefManager.putValue(PROFILE_NOTIFICATIONS_APP_UPDATE, value)
        }

    var isPeopleOnboardingShown: Boolean
        get() {
            return prefManager.getBoolean(KEY_PEOPLE_ONBOARDING_SHOWN, false)
        }
        set(value) {
            prefManager.putValue(KEY_PEOPLE_ONBOARDING_SHOWN, value)
        }

    var adminSupportId: Long
        get() {
            return prefManager.getLong(KEY_ADMIN_SUPPORT_ID, 0)
        }
        set(value) {
            prefManager.putValue(KEY_ADMIN_SUPPORT_ID, value)
        }

    var userRegistered: Boolean
        get() {
            return prefs.getBoolean(KEY_USER_REGISTERED, false)
        }
        set(value) {
            prefManager.putValue(KEY_USER_REGISTERED, value)
        }

    var isNewUserRegistered: Boolean
        get() {
            return prefManager.getBoolean(KEY_USER_REGISTERED_NEW, false)
        }
        set(value) {
            prefManager.putValue(KEY_USER_REGISTERED_NEW, value)
        }

    var isOpenRoadFilterTooltipWasShown: Boolean
        get() {
            return prefManager.getBoolean(KEY_IS_OPEN_ROAD_FILTER_WAS_SHOWN, false)
        }
        set(value) {
            prefManager.putValue(KEY_IS_OPEN_ROAD_FILTER_WAS_SHOWN, value)
        }

    var isOpenRoadFilterTooltipWasShownTimes: Int // тоже самое, что и KEY_IS_OPEN_ROAD_FILTER_WAS_SHOWN
        get() {
            return prefManager.getInt(KEY_IS_OPEN_ROAD_FILTER_WAS_SHOWN_TIMES, 0)
        }
        set(value) {
            prefManager.putValue(KEY_IS_OPEN_ROAD_FILTER_WAS_SHOWN_TIMES, value)
        }

    var isAutoRecSystemChangedSingle: Int
        get() {
            return prefManager.getInt(KEY_IS_AUTO_REC_SYSTEM_CHANGED_SINGLE, AppSettingsValue.NOT_SET.value)
        }
        set(value) {
            prefManager.putValue(KEY_IS_AUTO_REC_SYSTEM_CHANGED_SINGLE, value)
        }

    var isAutoRecSystemChanged: Int
        get() {
            return prefManager.getInt(KEY_IS_AUTO_REC_SYSTEM_CHANGED, AppSettingsValue.NOT_SET.value)
        }
        set(value) {
            prefManager.putValue(KEY_IS_AUTO_REC_SYSTEM_CHANGED, value)
        }

    var isRecSystemAvailable: Boolean
        get() {
            return prefManager.getBoolean(KEY_IS_REC_SYSTEM_AVAILABLE, false)
        }
        set(value) {
            prefManager.putValue(KEY_IS_REC_SYSTEM_AVAILABLE, value)
        }

    var isAddNewPostTooltipWasShown: Boolean
        get() {
            return prefManager.getBoolean(KEY_IS_ADD_NEW_POST_TOOLTIP_WAS_SHOWN, false)
        }
        set(value) {
            prefManager.putValue(KEY_IS_ADD_NEW_POST_TOOLTIP_WAS_SHOWN, value)
        }

    var isAddNewPostTooltipWasShownTimes: Int // тоже самое, что и isAddNewPostTooltipWasShown
        get() {
            return prefManager.getInt(KEY_IS_ADD_NEW_POST_TOOLTIP_WAS_SHOWN_TIMES, 0)
        }
        set(value) {
            prefManager.putValue(KEY_IS_ADD_NEW_POST_TOOLTIP_WAS_SHOWN_TIMES, value)
        }

    /**
     * Рефералка пригласить друга на экране списка друзей
     * */
    var isCreateFriendsReferralToolTipWasShownTimes: Int
        get() {
            return prefManager.getInt(KEY_IS_CREATE_FRIENDS_REFERRAL_TOOLTIP_WAS_SHOWN_TIMES, 0)
        }
        set(value) {
            prefManager.putValue(KEY_IS_CREATE_FRIENDS_REFERRAL_TOOLTIP_WAS_SHOWN_TIMES, value)
        }

    fun readFilterEventDate(): Int = prefManager.getInt(KEY_FILTER_EVENT_DATE, 0)

    /**
     * Рефералка пригласить друга на экране подписчиков
     * */
    var isCreateSubscribersReferralToolTipShownTimes: Int
        get() {
            return prefManager.getInt(KEY_IS_CREATE_SUBSCRIBERS_REFERRAL_TOOLTIP_WAS_SHOWN_TIMES, 0)
        }
        set(value) {
            prefManager.putValue(KEY_IS_CREATE_SUBSCRIBERS_REFERRAL_TOOLTIP_WAS_SHOWN_TIMES, value)
        }

    /**
     * Рефералка пригласить друга на экране профиля. Кнопка - друзья
     * */
    var isCreateUserInfoReferralToolTipShownTimes: Int
        get() {
            return prefManager.getInt(KEY_IS_CREATE_USER_INFO_REFERRAL_TOOLTIP_WAS_SHOWN_TIMES, 0)
        }
        set(value) {
            prefManager.putValue(KEY_IS_CREATE_USER_INFO_REFERRAL_TOOLTIP_WAS_SHOWN_TIMES, value)
        }

    var isSelectCommunityTooltipShownTimes: Int
        get() {
            return prefManager.getInt(KEY_IS_SELECT_COMMUNITY_TOOL_TIP_SHOWN_TIMES, 0)
        }
        set(value) {
            prefManager.putValue(KEY_IS_SELECT_COMMUNITY_TOOL_TIP_SHOWN_TIMES, value)
        }

    var isSwipeDownToShowTooltipRequired: Boolean
        get() {
            return prefManager.getBoolean(KEY_IS_SWIPE_DOWN_TO_SHOW_TOOLTIP_REQUIRED, true)
        }
        set(value) {
            prefManager.putValue(KEY_IS_SWIPE_DOWN_TO_SHOW_TOOLTIP_REQUIRED, value)
        }

    var isPhotosCounterTooltipRequired: Boolean
        get() {
            return prefManager.getBoolean(KEY_IS_PHOTOS_COUNTER_TOOLTIP_REQUIRED, true)
        }
        set(value) {
            prefManager.putValue(KEY_IS_PHOTOS_COUNTER_TOOLTIP_REQUIRED, value)
        }

    var isBubbleStarsTooltipShownDate: Long
        get() {
            return prefManager.getLong(KEY_IS_BUBBLE_STARS_TOOLTIP_SHOWN_DATE, 0)
        }
        set(value) {
            prefManager.putValue(KEY_IS_BUBBLE_STARS_TOOLTIP_SHOWN_DATE, value)
        }

    var roadFilterAppHint: Boolean
        get() {
            return prefs.getBoolean(APP_HINT_ROAD_FILTER, true)
        }
        set(value) {
            prefManager.putValue(APP_HINT_ROAD_FILTER, value)
        }

    var roadNewPostAppHint: Boolean
        get() {
            return prefs.getBoolean(APP_HINT_ROAD_NEW_POST, true)
        }
        set(value) {
            prefManager.putValue(APP_HINT_ROAD_NEW_POST, value)
        }

    var createGroupChatAppHint: Boolean
        get() {
            return prefs.getBoolean(APP_HINT_CREATE_GROUP_CHAT, true)
        }
        set(value) {
            prefManager.putValue(APP_HINT_CREATE_GROUP_CHAT, value)
        }

    var mapFilterAppHint: Boolean
        get() {
            return prefs.getBoolean(MAP_FILTER_APP_HINT, true)
        }
        set(value) {
            prefManager.putValue(MAP_FILTER_APP_HINT, value)
        }

    var recordAudioMessageAppHint: Boolean
        get() {
            return prefs.getBoolean(RECORD_AUDIO_MESSAGE_APP_HINT, true)
        }
        set(value) {
            prefManager.putValue(RECORD_AUDIO_MESSAGE_APP_HINT, value)
        }

    var sendAudioMessageAppHint: Boolean
        get() {
            return prefs.getBoolean(SEND_AUDIO_MESSAGE_APP_HINT, true)
        }
        set(value) {
            prefManager.putValue(SEND_AUDIO_MESSAGE_APP_HINT, value)
        }

    var accountNewFunctionAppHint: Boolean
        get() {
            return prefs.getBoolean(ACCOUNT_NEW_FUNCTION_APP_HINT, true)
        }
        set(value) {
            prefManager.putValue(ACCOUNT_NEW_FUNCTION_APP_HINT, value)
        }

    var shakeEnabledPrivacy: com.meera.core.preferences.datastore.Preference<Boolean>
        get() {
            return dataStore.boolean(KEY_IS_NEED_TO_REGISTER_SHAKE_EVENT, true)
        }
        set(value) {
            prefManager.putValue(KEY_IS_NEED_TO_REGISTER_SHAKE_EVENT, value)
            prefManager.putValue(KEY_IS_SWIPE_DOWN_TO_SHOW_TOOLTIP_REQUIRED, value)
        }

    var allowSyncContacts: com.meera.core.preferences.datastore.Preference<Boolean>
        get() {
            return dataStore.boolean(KEY_ALLOW_SYNC_CONTACTS, false)
        }
        set(value) {
            prefManager.putValue(KEY_ALLOW_SYNC_CONTACTS, value)
        }

    var allowShareScreenshot: Boolean
        get() {
            return prefManager.getBoolean(KEY_ALLOW_SHARE_SCREENSHOT, true)
        }
        set(value) {
            prefManager.putValue(KEY_ALLOW_SHARE_SCREENSHOT, value)
        }

    var isPostRoadSwitchHintShownTimes: Int  // подсказка в экране создания поста (выбор дороги)
        get() {
            return prefManager.getInt(APP_HINT_POST_ROAD_SWITCH_TIMES, 0)
        }
        set(value) {
            prefManager.putValue(APP_HINT_POST_ROAD_SWITCH_TIMES, value)
        }

    var isCommentPolicyHintShownTimes: Int  // подсказка в экране создания поста (настройки комментариев)
        get() {
            return prefManager.getInt(APP_HINT_COMMENT_POLICY_TIMES, 0)
        }
        set(value) {
            prefManager.putValue(APP_HINT_COMMENT_POLICY_TIMES, value)
        }

    var isMusicHintShownTimes: Int  // подсказка в экране создания поста (Музыка)
        get() {
            return prefManager.getInt(KEY_IS_MUSIC_TOOLTIP_WAS_SHOWN, 0)
        }
        set(value) {
            prefManager.putValue(KEY_IS_MUSIC_TOOLTIP_WAS_SHOWN, value)
        }

    var isMediaPositioningShownTimes: Int  // подсказка в экране создания поста (Музыка)
        get() {
            return prefManager.getInt(KEY_IS_MEDIA_POSITIONING_TOOLTIP_WAS_SHOWN, 0)
        }
        set(value) {
            prefManager.putValue(KEY_IS_MEDIA_POSITIONING_TOOLTIP_WAS_SHOWN, value)
        }

    var lastNotificationTs: Long
        get() = prefManager.getLong(KEY_NOTIFICATION_LAST_TS, 0)
        set(value) {
            prefManager.putValue(KEY_NOTIFICATION_LAST_TS, value)
        }


    val lastRecordedAppCodeDefaultValue = -1
    var lastRecordedAppCode: Int
        get() = prefManager.getInt(KEY_LAST_RECORDED_APP_CODE, lastRecordedAppCodeDefaultValue)
        set(value) {
            prefManager.putValue(KEY_LAST_RECORDED_APP_CODE, value)
        }

    //new
    var recordAudioMessageTooltipWasShownTimes: Int //тоже самое что и isLetRecordAudioMessageTooltipWasShown только считает кол-во нажатий
        get() {
            return prefManager.getInt(KEY_IS_LET_RECORD_AUDIO_MESSAGE_TOOLTIP_WAS_SHOWN_TIMES, 0)
        }
        set(value) {
            prefManager.putValue(KEY_IS_LET_RECORD_AUDIO_MESSAGE_TOOLTIP_WAS_SHOWN_TIMES, value)
        }

    /**
     * тоже самое что и isReleaseButtonSentAudioMessageTooltipWasShown только считает кол-во нажатий
     */
    var releaseButtonSentAudioMessageTooltipWasShownTimes: Int
        get() {
            return prefManager.getInt(KEY_IS_RELEASE_BUTTON_SENT_AUDIO_MESSAGE_TOOLTIP_WAS_SHOWN_TIMES, 0)
        }
        set(value) {
            prefManager.putValue(KEY_IS_RELEASE_BUTTON_SENT_AUDIO_MESSAGE_TOOLTIP_WAS_SHOWN_TIMES, value)
        }

    var isPhoneAbilityWasShowed: Int
        get() {
            return prefManager.getInt(KEY_IS_PHONE_ABILITY_WAS_SHOWN, 0)
        }
        set(value) {
            prefManager.putValue(KEY_IS_PHONE_ABILITY_WAS_SHOWN, value)
        }

    var isAccountNewFeaturesTooltipWasShown: Boolean
        get() {
            return prefManager.getBoolean(KEY_IS_ACCOUNT_NEW_FEATURES_TOOLTIP_WAS_SHOWN, false)
        }
        set(value) {
            prefManager.putValue(KEY_IS_ACCOUNT_NEW_FEATURES_TOOLTIP_WAS_SHOWN, value)
        }

    var readAboutUniqueNameHintShownTimes: Int  // тоже самое, что и readAboutUniqueNameHintShown
        get() {
            return prefManager.getInt(IS_ABOUT_UNIQUE_NAME_HINT_SHOWN_TIMES, 0)
        }
        set(value) {
            prefManager.putValue(IS_ABOUT_UNIQUE_NAME_HINT_SHOWN_TIMES, value)
        }

    var locale: String
        get() {
            return if (BuildConfig.DEBUG)
                prefManager.getString(KEY_LOCALE, "ru") ?: "ru"
            else
                Locale.getDefault().language
        }
        set(value) {
            prefManager.putValue(KEY_LOCALE, value)
        }

    var isNotificationEnabled: Boolean
        get() {
            return prefManager.getBoolean(KEY_IS_NOTIFICATION_ENABLED, false)
        }
        set(value) {
            prefManager.putValue(KEY_IS_NOTIFICATION_ENABLED, value)
        }

    var isChuckerEnabled: Boolean
        get() {
            return prefManager.getBoolean(KEY_IS_CHUCKER_ENABLED, false)
        }
        set(value) {
            prefManager.putValue(KEY_IS_CHUCKER_ENABLED, value)
        }

    var isNeedToRegisterShakeEvent: Boolean
        get() {
            return prefManager.getBoolean(KEY_IS_NEED_TO_REGISTER_SHAKE_EVENT, true)
        }
        set(value) {
            prefManager.putValue(KEY_IS_NEED_TO_REGISTER_SHAKE_EVENT, value)
        }

    var needShowPeopleSyncContactsDialog: Boolean
        get() {
            return prefManager.getBoolean(KEY_PEOPLE_SYNC_CONTACTS_DIALOG, true)
        }
        set(value) {
            prefManager.putValue(KEY_PEOPLE_SYNC_CONTACTS_DIALOG, value)
        }

    var needShowPeopleBadge: Boolean
        get() {
            return prefManager.getBoolean(KEY_SHOW_PEOPLE_BADGE, true)
        }
        set(value) {
            prefManager.putValue(KEY_SHOW_PEOPLE_BADGE, value)
        }

    var accountType: Int
        get() {
            return prefs.getInt(KEY_USER_ACCOUNT_TYPE, 0)
        }
        set(value) {
            prefManager.putValue(KEY_USER_ACCOUNT_TYPE, value)
        }

    var shownTooltipsMap = hashSetOf<String>() //inMemory setting какие подсказки были показаны в текущую сессию

    fun readMapEventsOnboardingShown(): Boolean =
        prefManager.getBoolean(KEY_MAP_EVENTS_ONBOARDING_SHOWN, false)

    var subscriptionDismissedUserIdSet: com.meera.core.preferences.datastore.Preference<Set<String>> =
        dataStore.stringSet(KEY_SUBSCRIPTION_IN_CHAT_DISMISSED_LIST, emptySet())

    val callUserCounter by lazy {
        dataStore.string(KEY_COUNTER_CALL, String.empty())
    }

    fun writeCallUserCounter(src: UserCallCounters) {
        val jsonString = gson.toJson(src)
        prefManager.putValue(KEY_COUNTER_CALL, jsonString)
    }

    fun <T> readCallUserCounter(clazz: Class<UserCallCounters>): UserCallCounters? {
        val jsonString = prefManager.getString(KEY_COUNTER_CALL, null)
        return if (jsonString == null) {
            UserCallCounters(0, 0)
        } else {
            gson.fromJson(jsonString, clazz)
        }
    }

    fun writeChatInitCompanionUser(src: Any?) {
        val jsonString = gson.toJson(src)
        prefManager.putValue(KEY_CHAT_INIT_COMPANION_USER, jsonString)
    }

    fun <T> readChatInitCompanionUser(clazz: Class<T>): T? {
        val jsonString = prefManager.getString(KEY_CHAT_INIT_COMPANION_USER, null)
        return gson.fromJson(jsonString, clazz)
    }

    val userName by lazy {
        dataStore.string(KEY_USER_NAME, String.empty())
    }

    fun writeFilterEventDate(eventDateFilterValue: Int) {
        prefManager
            .putValue(KEY_FILTER_EVENT_DATE, eventDateFilterValue)
    }

    fun writeAccessToken(accessToken: String) {
        prefManager.putValue(KEY_USER_ACCESS_TOKEN, accessToken)
    }

    fun readAccessToken(): String {
        return prefManager.getString(KEY_USER_ACCESS_TOKEN, String.empty()) ?: String.empty()
    }

    fun writeMapEventsOnboardingShown() {
        prefManager
            .putValue(KEY_MAP_EVENTS_ONBOARDING_SHOWN, true)
    }

    fun registerAccessTokenChangeListener(onPrefChanged: (accessToken: String) -> Unit) {
        accessTokenChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == KEY_USER_ACCESS_TOKEN) {
                val accessToken = sharedPreferences.getString(KEY_USER_ACCESS_TOKEN, String.empty()) ?: String.empty()
                onPrefChanged(accessToken)
            }
        }
        prefManager.registerOnSharedPreferenceChangeListener(accessTokenChangeListener)
    }

    fun unregisterAccessTokenChangeListener() {
        prefManager.unregisterOnSharedPreferenceChangeListener(accessTokenChangeListener)
    }

    suspend fun clearDataStorePreferences() {
        dataStore.clearAll()
    }

    fun writeFirstMomentSettingsOpen(value: Boolean) {
        editor.putBoolean(KEY_FIRST_MOMENT_SETTINGS_OPEN, value)
    }

    fun readFirstMomentSettingsOpen(): Boolean {
        return prefs.getBoolean(KEY_FIRST_MOMENT_SETTINGS_OPEN, true)
    }

    fun writeUID(userId: Long) {
        prefManager.putValue(KEY_USER_UID, userId)
    }

    fun writeUniquieName(uniqueName: String) {
        prefManager.putValue(KEY_UNIQUE_NAME, uniqueName)
    }

    fun readUniquieName() =
        prefManager.getString(KEY_UNIQUE_NAME, "")

    fun readUID(): Long {
        return prefManager.getLong(KEY_USER_UID, 0L)
    }

    fun readIsUserAuthorized(): Int {
        return prefManager.getInt(KEY_USER_AUTH_STATUS, 0)
    }

    fun readOnBoardingWelcomeShowed(): Boolean =
        prefManager.getBoolean(KEY_ON_BOARDING_WELCOME, false)

    fun readNeedOnBoarding(): Boolean =
        prefManager.getBoolean(KEY_ON_BOARDING, true)

    fun readIsFirstTimeOpenApp(): Boolean =
        prefManager.getBoolean(KEY_IS_FIRST_TIME_OPEN_APP, true)

    fun readIsFirstLogin(): Boolean =
        prefManager.getBoolean(KEY_FIRST_LOGIN, true)

    fun readIsWorthToShow(): Boolean {
        return prefManager.getBoolean(KEY_IS_WORTH_TO_SHOW_NEW, false)
    }

    @Deprecated("See https://nomera.atlassian.net/browse/BR-22979")
    fun readShowMen(): Boolean {
        return prefManager.getBoolean(KEY_SHOW_MEN, true)
    }

    @Deprecated("See https://nomera.atlassian.net/browse/BR-22979")
    fun readShowWomen(): Boolean {
        return prefManager.getBoolean(KEY_SHOW_WOMEN, true)
    }

    fun readLastSmsCodeTime(): Long {
        return prefManager.getLong(KEY_LAST_SMS_CODE_TIME, 0)
    }

    fun readAccountType(): Int {
        return prefManager.getInt(KEY_USER_ACCOUNT_TYPE, 0)
    }

    fun readAccountColor(): Int {
        return prefManager.getInt(KEY_USER_ACCOUNT_COLOR, 0)
    }

    fun readShowPeople(): Boolean = prefManager.getBoolean(KEY_SHOW_PEOPLE, true)

    fun readShowEvents(): Boolean = prefManager.getBoolean(KEY_SHOW_EVENTS, true)

    fun readShowFriends(): Boolean = prefManager.getBoolean(KEY_SHOW_FRIENDS, true)

    fun readLayersTooltipPeopleDisabledShownTimes(): Int =
        prefManager.getInt(KEY_LAYERS_TOOLTIP_PEOPLE_DISABLED_SHOWN_TIMES, 0)

    fun readLayersTooltipEventsDisabledShownTimes(): Int =
        prefManager.getInt(KEY_LAYERS_TOOLTIP_EVENTS_DISABLED_SHOWN_TIMES, 0)

    fun readLayersTooltipFriendsDisabledShownTimes(): Int =
        prefManager.getInt(KEY_LAYERS_TOOLTIP_FRIENDS_DISABLED_SHOWN_TIMES, 0)

    fun readLayersTooltipTwoLayersDisabledShownTimes(): Int =
        prefManager.getInt(KEY_LAYERS_TOOLTIP_TWO_LAYERS_DISABLED_SHOWN_TIMES, 0)

    fun readLayersTooltipAllLayersDisabledShownTimes(): Int =
        prefManager.getInt(KEY_LAYERS_TOOLTIP_ALL_LAYERS_DISABLED_SHOWN_TIMES, 0)

    fun readLayersTooltipOnboardingShownTimes(): Int =
        prefManager.getInt(KEY_LAYERS_TOOLTIP_ONBOARDING_SHOWN_TIMES, 0)

    fun readShowFriendsOnly(): Boolean {
        return prefManager.getBoolean(KEY_SHOW_FRIENDS_ONLY, false)
    }

    fun containsRoadFilterAppHint() = contains("APP_HINT_ROAD_FILTER")

    fun containsRoadNewPostAppHint() = contains("APP_HINT_ROAD_NEW_POST")

    fun containsCreateGroupChatAppHint() = contains("RECORD_AUDIO_MESSAGE_APP_HINT")

    fun containsMapFilterAppHint() = contains("MAP_FILTER_APP_HINT")

    fun containsSendAudioMessageAppHint() = contains("SEND_AUDIO_MESSAGE_APP_HINT")

    fun containsAccountNewFunctionAppHint() = contains("ACCOUNT_NEW_FUNCTION_APP_HINT")

    fun containsRecordAudioMessageAppHint() = contains("APP_HINT_CREATE_GROUP_CHAT")

    fun contains(key: String) = prefManager.contains(key)

    /**
     * @return Pair 1-latitude, 2-longitude
     */
    fun readLastLocation(): Pair<Double, Double>? {
        return if (prefManager.contains(KEY_LAST_LAT) && prefManager.contains(KEY_LAST_LAT)) {
            val lat = getDouble(KEY_LAST_LAT, LOCATION_DEFAULT_LATITUDE)
            val lng = getDouble(KEY_LAST_LNG, LOCATION_DEFAULT_LONGITUDE)
            Pair(lat, lng)
        } else {
            null
        }
    }

    fun <T> readFilterSettings(key: String, clazz: Class<T>): T? {
        val jsonString = prefManager.getString(key, null)
        return gson.fromJson(jsonString, clazz)
    }

    fun <T : Any> prefCallIceServers(clazz: Class<T>): T {
        val jsonString = prefs.getString(KEY_CALL_ICE_SERVERS, null)
        return gson.fromJson(jsonString, clazz)
    }

    fun savePrefCallIceServers(src: Any?) {
        val jsonString = gson.toJson(src)
        prefManager.putValue(KEY_CALL_ICE_SERVERS, jsonString)
    }

    fun <T : Any> prefAppLinks(clazz: Class<T>): T? {
        val jsonString = prefs.getString(KEY_APP_LINKS, null)
        return gson.fromJson(jsonString, clazz)
    }

    fun saveAppLinks(src: Any?) {
        val jsonString = gson.toJson(src)
        prefManager.putValue(KEY_APP_LINKS, jsonString)
    }

    fun writeFilterSettings(key: String, src: Any?) {
        val jsonString = gson.toJson(src)
        prefManager.putValue(key, jsonString)
    }

    fun clearFilterSettings(key: String) {
        prefManager.putValue(key, null)
    }

    fun writeRoomsSettings(src: Any?) {
        val jsonString = gson.toJson(src)
        prefManager.putValue(KEY_ROOMS_SETTINGS, jsonString)
    }

    fun readLastBaseUrl(): String =
        dataStore.string(KEY_BASE_URL).getSync() ?: String.empty()

    fun readLastBeagleUrl(): String =
        dataStore.string(KEY_BASE_URL_BEAGLE).getSync() ?: String.empty()

    fun readLastBaseUrlSocket(): String =
        dataStore.string(KEY_BASE_URL_SOCKET).getSync() ?: String.empty()

    suspend fun writeLastBaseUrl(baseUrl: String) {
        dataStore.string(KEY_BASE_URL).set(baseUrl)
    }

    suspend fun writeLastBaseUrlSocket(baseUrlSocket: String) {
        dataStore.string(KEY_BASE_URL_SOCKET).set(baseUrlSocket)
    }

    suspend fun writeLastBeagleUrl(baseUrlSocket: String) {
        dataStore.string(KEY_BASE_URL_BEAGLE).set(baseUrlSocket)
    }


    fun <T> readRoomsSettingsSettings(clazz: Class<T>): T? {
        val jsonString = prefManager.getString(KEY_ROOMS_SETTINGS, null)
        return gson.fromJson(jsonString, clazz)
    }

    fun readUserBirthday(): Long {
        return prefManager.getLong(KEY_USER_BIRTHDAY, 0)
    }

    fun readDarkMapStyle(): Boolean {
        return prefManager.getBoolean(KEY_DARK_MAP_STYLE, false)
    }

    fun readIsShownCalls(): Boolean {
        return prefManager.getBoolean(KEY_IS_SHOWN_CALLS, false)
    }

    fun readIsShownProfanity(): Boolean {
        return prefManager.getBoolean(KEY_IS_SHOWN_PROFANITY, false)
    }

    fun readIsRated(): Boolean {
        return prefManager.getBoolean(KEY_IS_RATED, false)
    }

    fun readLastRatedTime(): Long {
        return prefManager.getLong(KEY_LAST_TIME_SHOWN_RATE_US, 0L)
    }

    fun readPushId(): Int {
        return prefManager.getInt(KEY_PUSH_ID_NEW, 0)
    }

    fun readAppVerName(): String {
        return prefManager.getString(KEY_APP_VER_NAME, "") ?: ""
    }

    fun readShownUpdatedDalog(): Boolean {
        return prefManager.getBoolean(KEY_SHOWN_UPDATED_DIALOG, true)
    }

    fun readMyGroupsFilter(): Boolean {
        return prefManager.getBoolean(KEY_ROAD_MY_GROUPS_SUBSCRIPTION, true)
    }

    fun readSnippetOnboardingShown(): Boolean =
        prefManager.getBoolean(KEY_SNIPPET_ONBOARDING_SHOWN, false)

    fun readEventsModerationDialogShownCount(): Int =
        prefManager.getInt(KEY_EVENTS_MODERATION_DIALOG_SHOWN_COUNT, 0)

    fun readGeoPopupShownCount(): Int =
        prefManager.getInt(KEY_GEO_POPUP_SHOWN_COUNT, 0)

    fun writeNeedToShowPushNewMessage(needToShowPush: Boolean) {
        prefManager.putValue(KEY_NEED_TO_SHOW_PUSH_NEW_MESSAGE, needToShowPush)

    }

    fun writeShownUpdatedDialog(isShown: Boolean) {
        prefManager.putValue(KEY_SHOWN_UPDATED_DIALOG, isShown)

    }

    fun writeAppVerName(verName: String) {
        prefManager
            .putValue(KEY_APP_VER_NAME, verName)
    }

    fun writePushIdNew(id: Int) {
        var value = id
        if (value == Int.MAX_VALUE)
            value = 0
        prefManager
            .putValue(KEY_PUSH_ID_NEW, value)
    }

    fun writeLastRatedTime(time: Long) {
        prefManager
            .putValue(KEY_LAST_TIME_SHOWN_RATE_US, time)
    }

    fun writeIsRated(isRated: Boolean) {
        prefManager.putValue(KEY_IS_RATED, isRated)
    }

    fun writeIsShownCalls(isShown: Boolean) {
        prefManager.putValue(KEY_IS_SHOWN_CALLS, isShown)
    }

    fun writeIsShownProfanity(isShown: Boolean) {
        prefManager.putValue(KEY_IS_SHOWN_PROFANITY, isShown)

    }

    fun writeRefreshToken(refreshToken: String) {
        prefManager.putValue(KEY_USER_REFRESH_TOKEN_HI_WAY, refreshToken)
    }

    fun writeTokenExpiresIn(tokenExpires: Long) {
        prefManager.putValue(KEY_USER_TOKEN_EXPIRES, tokenExpires)
    }

    fun writeIsWorthToShow(isWorth: Boolean) {
        prefManager.putValue(KEY_IS_WORTH_TO_SHOW_NEW, isWorth)
    }

    fun writeUserPhoneNumber(phoneNumber: String) {
        prefManager
            .putValue(KEY_USER_PHONE_NUMBER, phoneNumber)

    }

    fun writeFCMToken(fcmToken: String) {
        prefManager
            .putValue(KEY_FCM_TOKEN, fcmToken)

    }

    fun writeShowPeople(show: Boolean) = prefManager.putValue(KEY_SHOW_PEOPLE, show)

    fun writeShowEvents(show: Boolean) = prefManager.putValue(KEY_SHOW_EVENTS, show)

    fun writeShowFriends(show: Boolean) = prefManager.putValue(KEY_SHOW_FRIENDS, show)

    fun writeLayersTooltipPeopleDisabledShown() =
        prefManager.putValue(
            KEY_LAYERS_TOOLTIP_PEOPLE_DISABLED_SHOWN_TIMES,
            readLayersTooltipPeopleDisabledShownTimes() + 1
        )

    fun writeLayersTooltipEventsDisabledShown() =
        prefManager.putValue(
            KEY_LAYERS_TOOLTIP_EVENTS_DISABLED_SHOWN_TIMES,
            readLayersTooltipEventsDisabledShownTimes() + 1
        )

    fun writeLayersTooltipFriendsDisabledShown() =
        prefManager.putValue(
            KEY_LAYERS_TOOLTIP_FRIENDS_DISABLED_SHOWN_TIMES,
            readLayersTooltipFriendsDisabledShownTimes() + 1
        )

    fun writeLayersTooltipTwoLayersDisabledShown() =
        prefManager.putValue(
            KEY_LAYERS_TOOLTIP_TWO_LAYERS_DISABLED_SHOWN_TIMES,
            readLayersTooltipTwoLayersDisabledShownTimes() + 1
        )

    fun writeLayersTooltipAllLayersDisabledShown() =
        prefManager.putValue(
            KEY_LAYERS_TOOLTIP_ALL_LAYERS_DISABLED_SHOWN_TIMES,
            readLayersTooltipAllLayersDisabledShownTimes() + 1
        )

    fun writeLayersTooltipOnboardingShown() =
        prefManager.putValue(
            KEY_LAYERS_TOOLTIP_ONBOARDING_SHOWN_TIMES,
            readLayersTooltipOnboardingShownTimes() + 1
        )

    fun writeShowFriendsOnly(show: Boolean) {
        prefManager.putValue(KEY_SHOW_FRIENDS_ONLY, show)

    }

    fun writeOnBoardingWelcomeShowed(isShowed: Boolean = true) {
        prefManager.putValue(KEY_ON_BOARDING_WELCOME, isShowed)

    }

    fun writeNeedOnBoarding(show: Boolean) {
        prefManager.putValue(KEY_ON_BOARDING, show)

    }

    fun writeIsFirstTimeOpenApp(isFirstTime: Boolean) {
        prefManager.putValue(KEY_IS_FIRST_TIME_OPEN_APP, isFirstTime)

    }

    fun writeFirstLogin(isFirstLogin: Boolean) {
        prefManager.putValue(KEY_FIRST_LOGIN, isFirstLogin)
    }

    @Deprecated("See https://nomera.atlassian.net/browse/BR-22979")
    fun writeShowMen(show: Boolean) {
        prefManager.putValue(KEY_SHOW_MEN, show)
    }

    @Deprecated("See https://nomera.atlassian.net/browse/BR-22979")
    fun writeShowWomen(show: Boolean) {
        prefManager.putValue(KEY_SHOW_WOMEN, show)
    }

    fun writeLastSmsCodeTime(time: Long) {
        prefManager
            .putValue(KEY_LAST_SMS_CODE_TIME, time)
    }

    fun writeLastLocation(latitude: Double, longitude: Double) {
        putDouble(KEY_LAST_LAT, latitude)
        putDouble(KEY_LAST_LNG, longitude)
    }

    // Выставляем фильтр "Мои сообщества" в дороге подписок
    fun writeMyGroupsFilter(isEnabled: Boolean) {
        prefManager.putValue(KEY_ROAD_MY_GROUPS_SUBSCRIPTION, isEnabled)
    }

    fun writeCallsRtcpMuxPolicy(value: String) {
        prefManager.putValue(PREF_KEY_RTCP_MUX_POLICY, value)
    }

    fun writeCallsTcpCandidatePolicy(value: String) {
        prefManager.putValue(PREF_KEY_TCP_CANDIDATE_POLICY, value)
    }


    internal fun putDouble(key: String, value: Double) {
        prefManager.putValue(key, java.lang.Double.doubleToRawLongBits(value))
    }

    internal fun getDouble(key: String, defaultValue: Double): Double {
        return java.lang.Double.longBitsToDouble(
            prefManager.getLong(
                key,
                java.lang.Double.doubleToLongBits(defaultValue)
            )
        )
    }

    fun writeAboutUniqueNameHintShown(isShown: Boolean = true) {
        prefManager.putValue(IS_ABOUT_UNIQUE_NAME_HINT_SHOWN, isShown)
    }

    fun readAboutUniqueNameHintShown(): Boolean {
        return prefManager.getBoolean(IS_ABOUT_UNIQUE_NAME_HINT_SHOWN, false)
    }

    fun writeSnippetOnboardingShown() {
        prefManager.putValue(KEY_SNIPPET_ONBOARDING_SHOWN, true)
    }

    fun writeGeoPopupShownCount(value: Int) {
        prefManager
            .putValue(KEY_GEO_POPUP_SHOWN_COUNT, value)

    }

    fun writeEventsModerationDialogShownCount(value: Int) {
        prefManager
            .putValue(KEY_EVENTS_MODERATION_DIALOG_SHOWN_COUNT, value)
    }

    fun markTooltipAsShownSession(tooltip: String) = shownTooltipsMap.add(tooltip)

    @Deprecated("Such logic shold be in a repository")
    fun isShownTooltipSession(tooltip: String) = !shownTooltipsMap.contains(tooltip)

    fun registerPreferencesChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefManager.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unRegisterPreferencesChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefManager.unregisterOnSharedPreferenceChangeListener(listener)
    }


    companion object {

        const val KEY_BASE_URL = "KEY_BASE_URL"
        const val KEY_BASE_URL_SOCKET = "KEY_BASE_URL_SOCKET"
        const val KEY_BASE_URL_BEAGLE = "KEY_BASE_URL_BEAGLE"

        // ключи для хранения состояния подсказки ( показана / не показана )
        const val KEY_IS_MUSIC_TOOLTIP_WAS_SHOWN = "KEY_IS_MUSIC_TOOLTIP_WAS_SHOWN"
        const val KEY_IS_MEDIA_POSITIONING_TOOLTIP_WAS_SHOWN = "KEY_IS_MEDIA_POSITIONING_TOOLTIP_WAS_SHOWN"
        const val KEY_IS_ACCOUNT_NEW_FEATURES_TOOLTIP_WAS_SHOWN = "KEY_IS_ACCOUNT_NEW_FEATURES_TOOLTIP_WAS_SHOWN"
        const val KEY_IS_OPEN_ROAD_FILTER_WAS_SHOWN = "KEY_IS_OPEN_ROAD_FILTER_WAS_SHOWN"
        const val KEY_IS_OPEN_ROAD_FILTER_WAS_SHOWN_TIMES = "KEY_IS_OPEN_ROAD_FILTER_WAS_SHOWN_TIMES"
        const val KEY_IS_REC_SYSTEM_AVAILABLE = "KEY_IS_REC_SYSTEM_AVAILABLE"
        const val KEY_IS_AUTO_REC_SYSTEM_CHANGED_SINGLE = "KEY_IS_AUTO_REC_SYSTEM_CHANGED_SINGLE"
        const val KEY_IS_AUTO_REC_SYSTEM_CHANGED = "KEY_IS_AUTO_REC_SYSTEM_CHANGED"

        const val KEY_IS_ADD_NEW_POST_TOOLTIP_WAS_SHOWN = "KEY_IS_OPEN_ADD_NEW_POST_TOOLTIP_WAS_SHOWN"
        const val KEY_IS_ADD_NEW_POST_TOOLTIP_WAS_SHOWN_TIMES = "KEY_IS_ADD_NEW_POST_TOOLTIP_WAS_SHOWN_TIMES"
        const val KEY_IS_SWIPE_DOWN_TO_SHOW_TOOLTIP_REQUIRED = "KEY_IS_SWIPE_DOWN_TO_SHOW_TOOLTIP_REQUIRED"
        const val KEY_IS_PHOTOS_COUNTER_TOOLTIP_REQUIRED = "KEY_IS_PHOTOS_COUNTER_TOOLTIP_REQUIRED"
        const val KEY_IS_BUBBLE_STARS_TOOLTIP_SHOWN_DATE = "KEY_IS_BUBBLE_STARS_TOOLTIP_SHOWN_DATE"

        const val KEY_IS_CREATE_FRIENDS_REFERRAL_TOOLTIP_WAS_SHOWN_TIMES =
            "KEY_IS_CREATE_FRIENDS_TOOLTIP_WAS_SHOWN_TIMES"
        const val KEY_IS_CREATE_SUBSCRIBERS_REFERRAL_TOOLTIP_WAS_SHOWN_TIMES =
            "KEY_IS_CREATE_SUBSCRIBERS_REFERRAL_TOOLTIP_WAS_SHOWN_TIMES"
        const val KEY_IS_CREATE_USER_INFO_REFERRAL_TOOLTIP_WAS_SHOWN_TIMES =
            "KEY_IS_CREATE_USER_INFO_REFERRAL_TOOLTIP_WAS_SHOWN_TIMES"
        const val KEY_IS_SELECT_COMMUNITY_TOOL_TIP_SHOWN_TIMES =
            "KEY_IS_SELECT_COMMUNITY_TOOL_TIP_SHOWN_TIMES"

        const val KEY_IS_LET_RECORD_AUDIO_MESSAGE_TOOLTIP_WAS_SHOWN =
            "KEY_IS_LET_RECORD_AUDIO_MESSAGE_TOOLTIP_WAS_SHOWN"
        const val KEY_IS_LET_RECORD_AUDIO_MESSAGE_TOOLTIP_WAS_SHOWN_TIMES =
            "KEY_IS_LET_RECORD_AUDIO_MESSAGE_TOOLTIP_WAS_SHOWN_TIMES"

        const val KEY_IS_RELEASE_BUTTON_SENT_AUDIO_MESSAGE_TOOLTIP_WAS_SHOWN =
            "KEY_IS_RELEASE_BUTTON_SENT_AUDIO_MESSAGE_TOOLTIP_WAS_SHOWN"
        const val KEY_IS_RELEASE_BUTTON_SENT_AUDIO_MESSAGE_TOOLTIP_WAS_SHOWN_TIMES =
            "KEY_IS_RELEASE_BUTTON_SENT_AUDIO_MESSAGE_TOOLTIP_WAS_SHOWN_TIMES"
        const val KEY_IS_PHONE_ABILITY_WAS_SHOWN = "KEY_IS_PHONE_ABILITY_WAS_SHOWN"

        const val APP_HINT_POST_ROAD_SWITCH_TIMES = "APP_HINT_POST_ROAD_SWITCH_TIMES"
        const val APP_HINT_COMMENT_POLICY_TIMES = "APP_HINT_COMMENT_POLICY_TIMES"

        const val CREATE_AVATAR_REGISTER_USER_HINT_SHOWN = "CREATE_AVATAR_REGISTER_USER_HINT_SHOWN"
        const val CREATE_AVATAR_USER_INFO_HINT_SHOWN = "CREATE_AVATAR_USER_INFO_HINT_SHOWN"
        const val CREATE_AVATAR_USER_PERSONAL_INFO_HINT_SHOWN = "CREATE_AVATAR_USER_PERSONAL_INFO_HINT_SHOWN"

        // для подсказок на экране ленты новостей
        const val KEY_USER_REGISTERED_NEW = "KEY_USER_REGISTERED_NEW"

        /** показывалось ли окно с включением звонков */
        const val KEY_IS_SHOWN_CALLS = "KEY_IS_SHOWN_CALLS"

        /** показывалось ли окно с отключением мата */
        const val KEY_IS_SHOWN_PROFANITY = "KEY_IS_SHOWN_PROFANITY"

        /** нажал ли пользователь на кнопку оценить*/
        const val KEY_IS_RATED = "KEY_IS_RATED"

        /** дата последнего показа rate us dialog*/
        const val KEY_LAST_TIME_SHOWN_RATE_US = "KEY_LAST_TIME_SHOWN_RATE_US"
        const val KEY_IS_WORTH_TO_SHOW_NEW = "KEY_IS_WORTH_TO_SHOW_NEW"

        /** User authorization token  */
        const val KEY_USER_ACCESS_TOKEN = "KEY_ACCESS_TOKEN"

        const val KEY_USER_REFRESH_TOKEN_HI_WAY = "KEY_USER_REFRESH_TOKEN_HI_WAY"

        const val KEY_USER_TOKEN_EXPIRES = "KEY_USER_TOKEN_EXPIRES"

        const val KEY_USER_AUTH_STATUS = "KEY_USER_AUTH_STATUS"

        /** User locale  */
        const val KEY_LOCALE = "KEY_LOCALE"

        /** User login  */
        const val KEY_USER_LOGIN = "KEY_USER_LOGIN"

        /** User Password  */
        const val KEY_USER_PASS = "KEY_USER_PASS"

        /** User Id  */
        const val KEY_USER_UID = "KEY_USER_UID"
        const val KEY_UNIQUE_NAME = "KEY_UNIQUE_NAME"

        const val KEY_SUBSCRIPTION_IN_CHAT_DISMISSED_LIST = "KEY_SUBSCRIPTION_IN_CHAT_DISMISSED_LIST"

        /** User Phone number */
        const val KEY_USER_PHONE_NUMBER = "KEY_USER_PHONE_NUMBER"

        /** Firebase Cloud Messaging Serivce token  */
        const val KEY_FCM_TOKEN = "KEY_FCM_TOKEN"

        const val KEY_USER_NAME = "KEY_USER_NAME"
        const val KEY_USER_AVATAR = "KEY_USER_AVATAR"
        const val KEY_USER_AVATAR_BIG = "KEY_USER_AVATAR_BIG"
        const val KEY_USER_ACCOUNT_TYPE = "KEY_USER_ACCOUNT_TYPE"
        const val KEY_USER_ACCOUNT_COLOR = "KEY_USER_ACCOUNT_COLOR"
        const val KEY_USER_GENDER = "KEY_USER_GENDER"
        const val KEY_USER_BIRTHDAY = "KEY_USER_BIRTHDAY"
        const val IS_ABOUT_UNIQUE_NAME_HINT_SHOWN = "IS_ABOUT_UNIQUE_NAME_HINT_SHOWN"

        const val IS_ABOUT_UNIQUE_NAME_HINT_SHOWN_TIMES = "IS_ABOUT_UNIQUE_NAME_HINT_SHOWN_TIMES"

        const val KEY_IS_NOTIFICATION_ENABLED = "KEY_IS_NOTIFICATION_ENABLED"

        const val KEY_IS_CHUCKER_ENABLED = "KEY_IS_CHUCKER_ENABLED"

        /** When the last sms request code was sent  */
        const val KEY_LAST_SMS_CODE_TIME = "KEY_LAST_SMS_CODE_TIME"

        /** to show users on map  */
        const val KEY_SHOW_FRIENDS_ONLY = "KEY_SHOW_FRIENDS_ONLY"

        private const val KEY_SHOW_MEN = "KEY_SHOW_MEN"
        private const val KEY_SHOW_WOMEN = "KEY_SHOW_WOMEN"

        private const val KEY_SHOW_PEOPLE = "KEY_SHOW_PEOPLE"
        private const val KEY_SHOW_EVENTS = "KEY_SHOW_EVENTS"
        private const val KEY_SHOW_FRIENDS = "KEY_SHOW_FRIENDS"
        private const val KEY_LAYERS_TOOLTIP_PEOPLE_DISABLED_SHOWN_TIMES =
            "KEY_LAYERS_TOOLTIP_PEOPLE_DISABLED_SHOWN_TIMES"
        private const val KEY_LAYERS_TOOLTIP_EVENTS_DISABLED_SHOWN_TIMES =
            "KEY_LAYERS_TOOLTIP_EVENTS_DISABLED_SHOWN_TIMES"
        private const val KEY_LAYERS_TOOLTIP_FRIENDS_DISABLED_SHOWN_TIMES =
            "KEY_LAYERS_TOOLTIP_FRIENDS_DISABLED_SHOWN_TIMES"
        private const val KEY_LAYERS_TOOLTIP_TWO_LAYERS_DISABLED_SHOWN_TIMES =
            "KEY_LAYERS_TOOLTIP_TWO_LAYERS_DISABLED_SHOWN_TIMES"
        private const val KEY_LAYERS_TOOLTIP_ALL_LAYERS_DISABLED_SHOWN_TIMES =
            "KEY_LAYERS_TOOLTIP_ALL_LAYERS_DISABLED_SHOWN_TIMES"
        private const val KEY_LAYERS_TOOLTIP_ONBOARDING_SHOWN_TIMES =
            "KEY_LAYERS_TOOLTIP_ONBOARDING_SHOWN_TIMES"

        const val KEY_FILTER_EVENT_TYPES = "KEY_FILTER_EVENT_TYPES"
        const val KEY_ROOMS_SETTINGS = "KEY_ROOMS_SETTINGS"
        private const val KEY_FILTER_EVENT_DATE = "KEY_FILTER_EVENT_DATE"

        const val KEY_LAST_LAT = "KEY_LAST_LAT"
        const val KEY_LAST_LNG = "KEY_LAST_LNG"

        /** Push notifications settings  */
        const val KEY_PUSH_ON = "KEY_PUSH_ON"
        const val KEY_PUSH_ON_MESSAGE = "KEY_PUSH_ON_MESSAGE"
        const val KEY_PUSH_ON_FRIEND_REQUEST = "KEY_PUSH_ON_FRIEND_REQUEST"
        const val KEY_PUSH_ON_GROUP_INVITE = "KEY_PUSH_ON_GROUP_INVITE"
        const val KEY_PUSH_ON_NEW_GIFT = "KEY_PUSH_ON_NEW_GIFT"
        const val KEY_PUSH_ON_POST_COMMENT = "KEY_PUSH_ON_POST_COMMENT"
        const val KEY_PUSH_ON_POST_GROUP = "KEY_PUSH_ON_POST_GROUP"
        const val KEY_PUSH_ON_ANSWER_COMMENT = "KEY_PUSH_ON_ANSWER_COMMENT"

        /** Privacy preferences  */
        const val KEY_PREVENT_RECEIVING_ANONYMOUS_MESSAGES = "KEY_PREVENT_RECEIVING_ANONYMOUS_MESSAGES"

        const val KEY_DARK_MAP_STYLE = "KEY_DARK_MAP_STYLE"

        const val KEY_PUSH_ID_NEW = "KEY_PUSH_ID_NEW"
        const val KEY_APP_VER_NAME = "KEY_APP_VER_NAME"

        const val KEY_SHOWN_UPDATED_DIALOG = "KEY_SHOWN_UPDATED_DIALOG"
        const val KEY_NEED_TO_SHOW_PUSH_NEW_MESSAGE = "KEY_NEED_TO_SHOW_PUSH_NEW_MESSAGE"

        const val KEY_EVENT = "KEY_EVENT"
        const val PROFILE_NOTIFICATIONS = "PROFILE_NOTIFICATION"
        const val PROFILE_NOTIFICATIONS_APP_UPDATE = "PROFILE_NOTIFICATIONS_APP_UPDATE"

        const val GET_REFERAL_VIP = "GET_REFERAL_VIP"

        const val KEY_USER_REGISTERED = "KEY_USER_REGISTERED"

        const val KEY_USER_AVATAR_STATE = "KEY_USER_AVATAR_STATE"
        const val KEY_ROAD_MY_GROUPS_SUBSCRIPTION = "KEY_ROAD_MY_GROUPS_SUBSCRIPTION"

        const val KEY_ON_BOARDING_WELCOME = "KEY_ON_BOARDING_WELCOME"
        const val KEY_ON_BOARDING = "KEY_ON_BOARDING"
        const val KEY_IS_FIRST_TIME_OPEN_APP = "KEY_IS_FIRST_TIME_OPEN_APP"

        const val KEY_NOTIFICATION_LAST_TS = "KEY_NOTIFICATION_LAST_TS"
        const val KEY_FIRST_LOGIN = "KEY_FIRST_LOGIN"
        const val KEY_FIRST_MOMENT_SETTINGS_OPEN = "KEY_FIRST_SAVE_MOMENT_TO_GALLERY_SETTING"
        const val KEY_HOLIDAY_TITLE = "KEY_HOLIDAY_TITLE"
        const val KEY_HOLIDAY_ID = "KEY_HOLIDAY_ID"
        const val KEY_HOLIDAY_MAIN_BUTTON_DEFAULT = "KEY_HOLIDAY_MAIN_BUTTON_DEFAULT"
        const val KEY_HOLIDAY_MAIN_BUTTON_ACTIVE = "KEY_HOLIDAY_MAIN_BUTTON_ACTIVE"
        const val KEY_HOLIDAY_START = "KEY_HOLIDAY_START"
        const val KEY_HOLIDAY_FINISH = "KEY_HOLIDAY_FINISH"
        const val KEY_HOLIDAY_ONBOARDING_TITLE = "KEY_HOLIDAY_ONBOARDING_TITLE"
        const val KEY_HOLIDAY_ONBOARDING_DESC = "KEY_HOLIDAY_ONBOARDING_DESC"
        const val KEY_HOLIDAY_ONBOARDING_ICON = "KEY_HOLIDAY_ONBOARDING_ICON"
        const val KEY_HOLIDAY_ONBOARDING_BTN_TEXT = "KEY_HOLIDAY_ONBOARDING_BTN_TEXT"
        const val KEY_HOLIDAY_HAT_REGULAR = "KEY_HOLIDAY_HAT_REGULAR"
        const val KEY_HOLIDAY_HAT_PREMIUM = "KEY_HOLIDAY_HAT_PREMIUM"
        const val KEY_HOLIDAY_HAT_VIP = "KEY_HOLIDAY_HAT_VIP"
        const val KEY_HOLIDAY_ROOM_TYPE = "KEY_HOLIDAY_ROOM_TYPE"
        const val KEY_HOLIDAY_ROOM_DIALOG = "KEY_HOLIDAY_ROOM_DIALOG"
        const val KEY_HOLIDAY_ROOM_ANON = "KEY_HOLIDAY_ROOM_ANON"
        const val KEY_HOLIDAY_ROOM_GROUP = "KEY_HOLIDAY_ROOM_GROUP"
        const val KEY_HOLIDAY_PRD_ID = "KEY_HOLIDAY_PRODUCT_ID"
        const val KEY_HOLIDAY_PRD_APPLE_ID = "KEY_HOLIDAY_APPLE_ID"
        const val KEY_HOLIDAY_PRD_CUSTOM_TITLE = "KEY_HOLIDAY_PRD_CUSTOM_TITLE"
        const val KEY_HOLIDAY_PRD_DESC = "KEY_HOLIDAY_PRD_DESC"
        const val KEY_HOLIDAY_PRD_IMG_LINK = "KEY_HOLIDAY_PRD_IMG_LINK"
        const val KEY_HOLIDAY_PRD_IMG_LINK_SMALL = "KEY_HOLIDAY_PRD_IMG_LINK_SMALL"
        const val KEY_HOLIDAY_PRD_ITUNES_ID = "KEY_HOLIDAY_PRD_ITUNES_ID"
        const val KEY_HOLIDAY_PRD_PLAYMARKER_ID = "KEY_HOLIDAY_PRD_PLAYMARKER_ID"
        const val KEY_HOLIDAY_PRD_TYPE = "KEY_HOLIDAY_PRD_TYPE"
        const val KEY_IS_HOLIDAY_INTRODUCED = "KEY_IS_HOLIDAY_INTRODUCED"
        const val KEY_IS_HOLIDAY_INTRODUCED_VERSION = "KEY_IS_HOLIDAY_INTRODUCED_VERSION"
        const val KEY_IS_HOLIDAY_SHOW_NEEDED = "KEY_IS_HOLIDAY_SHOW_NEEDED"
        const val KEY_HOLIDAY_CALENDAR_SHOW_DATE = "KEY_HOLIDAY_CALENDAR_SHOW_DATE"
        const val KEY_HOLIDAY_CALENDAR_STATUS = "KEY_HOLIDAY_CALENDAR_STATUS"
        const val KEY_HOLIDAY_CALENDAR_SHOWN_TO_USER_ID = "KEY_HOLIDAY_CALENDAR_SHOWN_TO_USER_ID"
        const val KEY_HOLIDAY_CALENDAR_DAYS_COUNT = "KEY_HOLIDAY_CALENDAR_DAYS_COUNT"
        const val KEY_PEOPLE_ONBOARDING_SHOWN = "KEY_PEOPLE_ONBOARDING_SHOWN"
        const val KEY_IS_NEED_TO_REGISTER_SHAKE_EVENT = "KEY_IS_NEED_TO_REGISTER_SHAKE_EVENT"
        const val KEY_ALLOW_SYNC_CONTACTS = "KEY_ALLOW_SYNC_CONTACTS"
        const val KEY_ALLOW_SHARE_SCREENSHOT = "KEY_ALLOW_SHARE_SCREENSHOT"
        const val KEY_PEOPLE_SYNC_CONTACTS_DIALOG = "KEY_PEOPLE_SYNC_CONTACTS_DIALOG"


        const val KEY_APP_LINKS = "KEY_APP_LINKS"
        const val KEY_KEYBOARD_HEIGHT = "KEY_KEYBOARD_HEIGHT"
        const val KEY_HOLIDAY_CODE = "KEY_HOLIDAY_CODE"
        const val KEY_IS_REMOVE_COUNTRY = "KEY_IS_REMOVE_COUNTRY"
        const val KEY_LAST_RECORDED_APP_CODE = "KEY_LAST_RECORDED_APP_CODE"
        const val KEY_DIALOG_BIRTHDAY_SHOWN = "KEY_DIALOG_BIRTHDAY_SHOW"
        const val KEY_IS_NEED_SHOW_FRIENDS_FOLLOWERS_PRIVACY =
            "KEY_IS_NEED_SHOW_FRIENDS_FOLLOWERS_PRIVACY"
        const val SHOW_FRIENDS_AND_SUBSCRIBERS = "SHOW_FRIENDS_AND_SUBSCRIBERS"
        const val KEY_ADMIN_SUPPORT_ID = "KEY_ADMIN_SUPPORT_ID"

        const val KEY_IS_REGISTRATION_COMPLETED = "KEY_IS_REGISTRATION_COMPLETED"

        const val KEY_IS_LEAK_CANARY_ENABLED = "KEY_IS_LEAK_CANARY_ENABLED"

        const val KEY_SESSION_COUNTER = "KEY_SESSION_COUNTER"

        const val KEY_SNIPPET_ONBOARDING_SHOWN = "KEY_SNIPPET_ONBOARDING_SHOWN"
        const val KEY_MAP_EVENTS_ONBOARDING_SHOWN = "KEY_MAP_EVENTS_ONBOARDING_SHOWN"

        const val KEY_EVENTS_MODERATION_DIALOG_SHOWN_COUNT = "KEY_EVENTS_MODERATION_DIALOG_SHOWN_COUNT"
        const val KEY_GEO_POPUP_SHOWN_COUNT = "KEY_GEO_POPUP_SHOWN_COUNT"

        const val KEY_CALL_ICE_SERVERS = "KEY_CALL_ICE_SERVERS"
        const val KEY_SHOW_PEOPLE_BADGE = "KEY_SHOW_PEOPLE_BADGE"

        const val LOCATION_DEFAULT_LATITUDE = 55.75
        const val LOCATION_DEFAULT_LONGITUDE = 37.62

        const val APP_HINT_ROAD_FILTER = "APP_HINT_ROAD_FILTER"
        const val APP_HINT_ROAD_NEW_POST = "APP_HINT_ROAD_NEW_POST"
        const val APP_HINT_CREATE_GROUP_CHAT = "APP_HINT_CREATE_GROUP_CHAT"
        const val MAP_FILTER_APP_HINT = "MAP_FILTER_APP_HINT"
        const val RECORD_AUDIO_MESSAGE_APP_HINT = "RECORD_AUDIO_MESSAGE_APP_HINT"
        const val SEND_AUDIO_MESSAGE_APP_HINT = "SEND_AUDIO_MESSAGE_APP_HINT"
        const val ACCOUNT_NEW_FUNCTION_APP_HINT = "ACCOUNT_NEW_FUNCTION_APP_HINT"

        const val IS_NEED_TO_MIGRATE_TO_PREF = "IS_NEED_TO_MIGRATE_TO_PREF"

        const val KEY_CHAT_INIT_COMPANION_USER = "KEY_CHAT_INIT_COMPANION_USER"
        const val KEY_COUNTER_CALL = "counter"
        const val KEY_SUPPORT_USER_ID = "KEY_SUPPORT_USER_ID"

    }
}

enum class AppSettingsValue(val value: Int) {
    NOT_SET(-1),
    FALSE(0),
    TRUE(1)
}

fun AppSettings.launchWithScope(
    scope: CoroutineScope,
    block: suspend AppSettings.() -> Unit
) {
    scope.launch {
        block()
    }
}

class GsonPreferenceAdapter<T : Any>(val gson: Gson, private val clazz: Class<T>) : Preference.Adapter<T> {

    override fun get(key: String, preferences: SharedPreferences): T {
        return gson.fromJson(preferences.getString(key, ""), clazz)
    }

    override fun set(key: String, value: T, editor: SharedPreferences.Editor) {
        editor.putString(key, gson.toJson(value))
    }
}

fun AppSettings.UserCallCounters.toJson(): String = Gson().toJson(this)

