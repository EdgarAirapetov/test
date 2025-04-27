package com.numplates.nomera3.modules.newroads.featureAnnounce.data

import android.net.Uri
import com.numplates.nomera3.NOOMEERA_SCHEME
import java.util.regex.Pattern

sealed class DeeplinkAction {
    object GoOwnProfileTabAction : DeeplinkAction()
    object OpenChatsAction : DeeplinkAction()
    data class OpenSpecificChatAction(val userId: Long): DeeplinkAction()
    object OpenNotifications : DeeplinkAction()
    object OpenMapAction : DeeplinkAction()
    object SearchUserAction : DeeplinkAction()
    object CreateNewPostAction : DeeplinkAction()
    object CreateNewPostPersonalAction : DeeplinkAction()
    object OpenMyCommunityAction : DeeplinkAction()
    object OpenUserSettingsAction : DeeplinkAction()
    object OpenAboutMeAction : DeeplinkAction()
    object ProfileEditAction : DeeplinkAction()
    object PrivacyAction : DeeplinkAction()
    object UserNotificationSettingsAction : DeeplinkAction()
    object UserReferralAction : DeeplinkAction()
    object MakeProfileVipAction : DeeplinkAction()
    object OpenPeopleAction : DeeplinkAction()
    object CreateNewMoment : DeeplinkAction()
    data class OpenSpecificUserAction(val userId: Long) : DeeplinkAction()
    data class OpenSpecificCommunityAction(val communityId: Long) : DeeplinkAction()
    data class OpenSpecificPostAction(val postId: Long) : DeeplinkAction()
    data class OpenSpecificMomentAction(val momentId: Long) : DeeplinkAction()
}

enum class DeeplinkParameter(val key: String) {
    ORIGIN("origin")
}

enum class DeeplinkOrigin(val value: String) {
    APP_VIEW("appview"),
    NOTIFICATIONS("notifications"),
    PUSH("push"),
    ANNOUNCEMENT("announcement");

    companion object {
        fun fromValue(value: String): DeeplinkOrigin? {
            return values().firstOrNull { it.value == value }
        }
    }
}

object FeatureDeepLink {

    enum class DeeplinkSegment(val value: String) {
        USER("user"),
        ROOMS("rooms"),
        CHAT("chat"),
        EVENTS("events"),
        MAP("map"),
        SEARCH("search"),
        ROAD("road"),
        NEW_POST("new_post"),
        NEW_POST_PERSONAL("personal"),
        COMMUNITY("community"),
        GROUP("group"),
        MY("my"),
        SETTINGS("settings"),
        ABOUT("about"),
        EDIT("edit"),
        PRIVACY("privacy"),
        REFERRAL("refferal"),
        BUY_GOLD("buygold"),
        MOMENT("moment"),
        POST("post"),
        PEOPLE("people"),
        NEW_MOMENT("newMoment")
    }

    private const val userProfilePattern = "/user/\\d+"
    private const val userMomentPattern = "/moment/\\d+"

    fun getAction(deeplink: String): DeeplinkAction? {
        val segs = Uri.parse(deeplink).pathSegments
        return when {
            segs == listOf(DeeplinkSegment.USER.value) -> DeeplinkAction.GoOwnProfileTabAction
            segs == listOf(DeeplinkSegment.ROOMS.value) -> DeeplinkAction.OpenChatsAction
            segs == listOf(DeeplinkSegment.EVENTS.value) -> DeeplinkAction.OpenNotifications
            segs == listOf(DeeplinkSegment.MAP.value) -> DeeplinkAction.OpenMapAction
            segs == listOf(DeeplinkSegment.SEARCH.value, DeeplinkSegment.USER.value) ->
                DeeplinkAction.SearchUserAction
            segs == listOf(DeeplinkSegment.ROAD.value, DeeplinkSegment.NEW_POST.value) ->
                DeeplinkAction.CreateNewPostAction
            segs == listOf(
                DeeplinkSegment.ROAD.value,
                DeeplinkSegment.NEW_POST.value,
                DeeplinkSegment.NEW_POST_PERSONAL.value
            ) -> DeeplinkAction.CreateNewPostPersonalAction
            segs == listOf(DeeplinkSegment.COMMUNITY.value, DeeplinkSegment.MY.value) ->
                DeeplinkAction.OpenMyCommunityAction
            segs == listOf(DeeplinkSegment.USER.value, DeeplinkSegment.SETTINGS.value) ->
                DeeplinkAction.OpenUserSettingsAction
            segs == listOf(DeeplinkSegment.USER.value, DeeplinkSegment.ABOUT.value) ->
                DeeplinkAction.OpenAboutMeAction
            segs == listOf(DeeplinkSegment.USER.value, DeeplinkSegment.EDIT.value) ->
                DeeplinkAction.ProfileEditAction
            segs == listOf(DeeplinkSegment.SETTINGS.value, DeeplinkSegment.PRIVACY.value) ->
                DeeplinkAction.PrivacyAction
            segs == listOf(DeeplinkSegment.SETTINGS.value, DeeplinkSegment.EVENTS.value) ->
                DeeplinkAction.UserNotificationSettingsAction
            segs == listOf(DeeplinkSegment.USER.value, DeeplinkSegment.REFERRAL.value) ->
                DeeplinkAction.UserReferralAction
            segs == listOf(DeeplinkSegment.PEOPLE.value) -> DeeplinkAction.OpenPeopleAction
            segs == listOf(DeeplinkSegment.USER.value, DeeplinkSegment.BUY_GOLD.value) ->
                DeeplinkAction.MakeProfileVipAction
            segs == listOf(DeeplinkSegment.NEW_MOMENT.value) -> DeeplinkAction.CreateNewMoment
            segs.size == 2 && segs[0] == DeeplinkSegment.USER.value -> {
                segs[1].toLongOrNull()?.let(DeeplinkAction::OpenSpecificUserAction)
            }
            segs.size == 2 && (segs[0] == DeeplinkSegment.COMMUNITY.value ||
                segs[0] == DeeplinkSegment.GROUP.value) -> {
                segs[1].toLongOrNull()?.let(DeeplinkAction::OpenSpecificCommunityAction)
            }
            segs.size == 2 && segs[0] == DeeplinkSegment.POST.value -> {
                segs[1].toLongOrNull()?.let(DeeplinkAction::OpenSpecificPostAction)
            }
            segs.size == 2 && segs[0] == DeeplinkSegment.CHAT.value -> {
                segs[1].toLongOrNull()?.let(DeeplinkAction::OpenSpecificChatAction)
            }
            segs.size == 2 && segs[0] == DeeplinkSegment.MOMENT.value -> {
                segs[1].toLongOrNull()?.let(DeeplinkAction::OpenSpecificMomentAction)
            }
            else -> null
        }
    }

    fun isValid(deeplink: String): Boolean {
        return getAction(deeplink) != null
    }

    fun isAppDeeplink(deeplink: String): Boolean {
        return Uri.parse(deeplink).scheme == NOOMEERA_SCHEME
    }

    fun addDeeplinkOrigin(deeplink: String, deeplinkOrigin: DeeplinkOrigin): String {
        return if (isValid(deeplink)) {
            Uri.parse(deeplink)
                .buildUpon()
                .appendQueryParameter(DeeplinkParameter.ORIGIN.key, deeplinkOrigin.value)
                .build()
                .toString()
        } else {
            deeplink
        }
    }

    fun getDeeplinkOrigin(deeplink: String): DeeplinkOrigin? {
        return Uri.parse(deeplink)
            .getQueryParameter(DeeplinkParameter.ORIGIN.key)
            ?.let(DeeplinkOrigin::fromValue)
    }

    fun isNeedOpenWithOutAuth(path: String): Boolean {
        return Pattern.matches(userProfilePattern, path) || Pattern.matches(userMomentPattern, path)
    }
}
