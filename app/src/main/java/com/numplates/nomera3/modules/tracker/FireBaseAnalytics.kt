package com.numplates.nomera3.modules.tracker

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import timber.log.Timber
import javax.inject.Inject


class FireBaseAnalytics @Inject constructor(
    private val analytics: FirebaseAnalytics?
) {

    fun logScreenForFragment(screenClass: String?) {
        //if (BuildConfig.DEBUG || App.IS_TEST_SERVER) return
        screenClass?.let {
            val screenName = getScreenNameForClass(it)
            if (screenName.isNotEmpty()) {
                Timber.d("logScreenForFragment class = $it, screen = $screenName")
                logScreen(screenName, it)
            }
        }
    }

    private fun logScreen(screenName: String, screenClass: String) =
        analytics?.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            param(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
        }

    private fun getScreenNameForClass(screenName: String) = when (screenName) {
        "AuthSmsFragment" -> ScreenNamesEnum.CONFIRMATION_CODE.value
        "MyRoadFragment" -> ScreenNamesEnum.ROAD_PERSONAL.value
        "MainRoadFragment" -> ScreenNamesEnum.ROAD_MAIN.value
        "SubscriptionsRoadFragment" -> ScreenNamesEnum.ROAD_SUBSCRIPTIONS.value
        "SearchUserFragmentNew" -> ScreenNamesEnum.SEARCH.value
        "MapFragment" -> ScreenNamesEnum.ROAD_MAP.value
        "RoadFilter" -> ScreenNamesEnum.ROAD_FILTER.value
        "PostFragmentV2" -> ScreenNamesEnum.POST.value
        "MapTab" -> ScreenNamesEnum.MAP_MAIN.value
        "MapFiltersHostFragment" -> ScreenNamesEnum.MAP_FILTER.value
        "UserGroupListFragmentNew" -> ScreenNamesEnum.MY_GROUPS.value
        "TopGroupListFragmentNew" -> ScreenNamesEnum.ALL_GROUPS.value
        "EditGroup" -> ScreenNamesEnum.EDIT_GROUP.value
        "CreateGroup" -> ScreenNamesEnum.CREATE_GROUP.value
        "GroupPostV2" -> ScreenNamesEnum.GROUP_POST.value
        "RoomsFragmentV2" -> ScreenNamesEnum.CHAT_LIST.value
        "NotificationFragment" -> ScreenNamesEnum.NOTIFICATIONS.value
        "GroupChatFragmentNew" -> ScreenNamesEnum.CHAT_GROUP.value
        "P2PChatFragmentNew" -> ScreenNamesEnum.CHAT_P2P.value
        "ProfileMy" -> ScreenNamesEnum.PROFILE_MY.value
        "ProfileNotMe" -> ScreenNamesEnum.PROFILE_NOT_MY.value
        "FriendsHostFragmentNew" -> ScreenNamesEnum.FRIENDS.value
        "GridProfilePhotoFragment" -> ScreenNamesEnum.GALLERY.value
        "VehicleListFragmentNew" -> ScreenNamesEnum.GARAGE.value
        "MapProfile" -> ScreenNamesEnum.MAP_PROFILE.value
        "VipSilver" -> ScreenNamesEnum.VIP_SILVER.value
        "VipGold" -> ScreenNamesEnum.VIP_GOLD.value
        "UpdateStatusFragment" -> ScreenNamesEnum.ENHANCE_TO_VIP.value
        "SendGIftFragmentNew" -> ScreenNamesEnum.SEND_GIFT.value
        else -> ""
    }

}
