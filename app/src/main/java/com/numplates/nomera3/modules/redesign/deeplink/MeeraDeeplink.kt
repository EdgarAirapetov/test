package com.numplates.nomera3.modules.redesign.deeplink

import android.content.Intent
import android.net.Uri
import com.numplates.nomera3.NOOMEERA_SCHEME
import com.numplates.nomera3.modules.newroads.featureAnnounce.data.DeeplinkOrigin
import com.numplates.nomera3.modules.newroads.featureAnnounce.data.DeeplinkParameter
import com.numplates.nomera3.modules.redesign.util.NavigationManager

sealed class MeeraDeeplinkParam {
    data object None : MeeraDeeplinkParam()

    data class MeeraDeeplinkData(
        val isPush: Boolean = false,
        val intent: Intent?
    ) : MeeraDeeplinkParam()

    data class MeeraDeeplinkActionContainer(val action: MeeraDeeplinkAction) : MeeraDeeplinkParam()
}

fun String?.wrapDeeplink(): MeeraDeeplinkParam? {
    this ?: return MeeraDeeplinkParam.MeeraDeeplinkActionContainer(MeeraDeeplinkAction.None)
    val url = this
    if (MeeraDeeplink.isAppDeeplink(this)) {
        val action = MeeraDeeplink.getAction(this) ?: MeeraDeeplinkAction.None
        return MeeraDeeplinkParam.MeeraDeeplinkActionContainer(action)
    } else {
        val launchBrowser = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        NavigationManager.getManager().act.startActivity(launchBrowser)
        return null
    }
}

sealed class MeeraDeeplinkAction {
    data object None : MeeraDeeplinkAction()
    data object GoOwnProfileTabAction : MeeraDeeplinkAction()
    data object OpenChatsAction : MeeraDeeplinkAction()
    data object OpenNotifications : MeeraDeeplinkAction()
    data object SearchUserAction : MeeraDeeplinkAction()
    data object CreateNewPostAction : MeeraDeeplinkAction()
    data object OpenMyCommunityAction : MeeraDeeplinkAction()
    data object OpenMapAction : MeeraDeeplinkAction()
    data object OpenUserSettingsAction : MeeraDeeplinkAction()
    data object ProfileEditAction : MeeraDeeplinkAction()
    data object PrivacyAction : MeeraDeeplinkAction()
    data object UserNotificationSettingsAction : MeeraDeeplinkAction()
    data class OpenSpecificCommunityAction(val communityId: Long) : MeeraDeeplinkAction()
    data class OpenSpecificPostAction(val postId: Long) : MeeraDeeplinkAction()
    data class OpenSpecificChatAction(val userId: Long) : MeeraDeeplinkAction()
    data object CreateNewPostPersonalAction : MeeraDeeplinkAction()
    data object UserReferralAction : MeeraDeeplinkAction()
    data class OpenSpecificUserAction(val userId: Long) : MeeraDeeplinkAction()
    data object OpenPeopleAction : MeeraDeeplinkAction()
    data object OpenAboutMeAction : MeeraDeeplinkAction()
    data class OpenSpecificMomentAction(val momentId: Long) : MeeraDeeplinkAction()
    data class PushWrapper(val intent: Intent?) : MeeraDeeplinkAction()
}

object MeeraDeeplink {

    enum class MeeraDeeplinkSegment(val value: String) {
        USER("user"),
        ROOMS("rooms"),
        EVENTS("events"),
        CHAT("chat"),
        SEARCH("search"),
        ROAD("road"),
        COMMUNITY("community"),
        MY("my"),
        MAP("map"),
        SETTINGS("settings"),
        EDIT("edit"),
        PRIVACY("privacy"),
        REFERRAL("refferal"),
        POST("post"),
        NEW_POST("new_post"),
        NEW_POST_PERSONAL("personal"),
        PEOPLE("people"),
        MOMENT("moment"),
        GROUP("group"),
        ABOUT("about")
    }

    fun getAction(deeplink: String): MeeraDeeplinkAction? {
        val segs = Uri.parse(deeplink).pathSegments
        return when {
            segs == listOf(MeeraDeeplinkSegment.USER.value) -> MeeraDeeplinkAction.GoOwnProfileTabAction
            segs == listOf(MeeraDeeplinkSegment.ROOMS.value) -> MeeraDeeplinkAction.OpenChatsAction
            segs == listOf(MeeraDeeplinkSegment.EVENTS.value) -> MeeraDeeplinkAction.OpenNotifications
            segs == listOf(MeeraDeeplinkSegment.MAP.value) -> MeeraDeeplinkAction.OpenMapAction
            segs == listOf(MeeraDeeplinkSegment.SEARCH.value, MeeraDeeplinkSegment.USER.value) ->
                MeeraDeeplinkAction.SearchUserAction

            segs == listOf(MeeraDeeplinkSegment.USER.value, MeeraDeeplinkSegment.ABOUT.value) ->
                MeeraDeeplinkAction.OpenAboutMeAction

            segs == listOf(MeeraDeeplinkSegment.ROAD.value, MeeraDeeplinkSegment.NEW_POST.value) ->
                MeeraDeeplinkAction.CreateNewPostAction

            segs == listOf(
                MeeraDeeplinkSegment.ROAD.value,
                MeeraDeeplinkSegment.NEW_POST.value,
                MeeraDeeplinkSegment.NEW_POST_PERSONAL.value
            ) -> MeeraDeeplinkAction.CreateNewPostPersonalAction

            segs == listOf(MeeraDeeplinkSegment.COMMUNITY.value, MeeraDeeplinkSegment.MY.value) ->
                MeeraDeeplinkAction.OpenMyCommunityAction

            segs == listOf(MeeraDeeplinkSegment.USER.value, MeeraDeeplinkSegment.SETTINGS.value) ->
                MeeraDeeplinkAction.OpenUserSettingsAction

            segs == listOf(MeeraDeeplinkSegment.USER.value, MeeraDeeplinkSegment.EDIT.value) ->
                MeeraDeeplinkAction.ProfileEditAction

            segs == listOf(MeeraDeeplinkSegment.SETTINGS.value, MeeraDeeplinkSegment.PRIVACY.value) ->
                MeeraDeeplinkAction.PrivacyAction

            segs == listOf(MeeraDeeplinkSegment.SETTINGS.value, MeeraDeeplinkSegment.EVENTS.value) ->
                MeeraDeeplinkAction.UserNotificationSettingsAction

            segs == listOf(MeeraDeeplinkSegment.USER.value, MeeraDeeplinkSegment.REFERRAL.value) ->
                MeeraDeeplinkAction.UserReferralAction

            segs.size == 2 && segs[0] == MeeraDeeplinkSegment.MOMENT.value ->
                segs[1].toLongOrNull()?.let(MeeraDeeplinkAction::OpenSpecificMomentAction)

            segs.size == 2 && segs[0] == MeeraDeeplinkSegment.USER.value ->
                segs[1].toLongOrNull()?.let(MeeraDeeplinkAction::OpenSpecificUserAction)

            segs.size == 2 && segs[0] == MeeraDeeplinkSegment.CHAT.value ->
                segs[1].toLongOrNull()?.let(MeeraDeeplinkAction::OpenSpecificChatAction)

            segs.size == 2 && segs[0] == MeeraDeeplinkSegment.POST.value ->
                segs[1].toLongOrNull()?.let(MeeraDeeplinkAction::OpenSpecificPostAction)

            segs.size == 2 && (segs[0] == MeeraDeeplinkSegment.COMMUNITY.value ||
                segs[0] == MeeraDeeplinkSegment.GROUP.value) ->
                segs[1].toLongOrNull()?.let(MeeraDeeplinkAction::OpenSpecificCommunityAction)

            segs == listOf(MeeraDeeplinkSegment.PEOPLE.value) -> MeeraDeeplinkAction.OpenPeopleAction

            else -> MeeraDeeplinkAction.None
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

}
