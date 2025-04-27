package com.numplates.nomera3.modules.communities.ui.viewevent

import com.numplates.nomera3.modules.communities.data.entity.CommunityEntity
import com.numplates.nomera3.modules.user.ui.entity.UserPermissions

sealed class CommunityViewEvent {

    data class CommunityData(
        val community: CommunityEntity,
        val permission: UserPermissions?
    ) : CommunityViewEvent()

    class CommunityDataProgress(val inProgress: Boolean): CommunityViewEvent()

    class SubscribeCommunityProgress(val inProgress: Boolean): CommunityViewEvent()

    class SuccessSubscribeCommunity(val groupId: Int? = null) : CommunityViewEvent()

    class SuccessUnsubscribeCommunity(val groupId: Int? = null) : CommunityViewEvent()

    class SuccessUnsubscribePrivateCommunity(val groupId: Int? = null) : CommunityViewEvent()

    class SuccessSubscribePrivateCommunity(val groupId: Int? = null) : CommunityViewEvent()

    class SuccessSubscribedToNotifications(val groupId: Int?) : CommunityViewEvent()

    class SuccessUnsubscribedFromNotifications(val groupId: Int?) : CommunityViewEvent()

    class CommunityNotificationsProgress(val inProgress: Boolean): CommunityViewEvent()

    class FailedSubscribeToNotifications(val groupId: Int?) : CommunityViewEvent()

    class FailedUnsubscribeFromNotifications(val groupId: Int?) : CommunityViewEvent()

    object FailureGetCommunityInfo : CommunityViewEvent()

    object FailureCommunityNotFound: CommunityViewEvent()

    object FailureSubscribeCommunity : CommunityViewEvent()

    object FailureUnsubscribeCommunity : CommunityViewEvent()

    var position: Int? = null

    class SuccessGetCommunityLink(
        val link: String,
        val action: GetCommunityLinkAction
    ): CommunityViewEvent()

    class OpenSupportAdminChat(val adminId: Long) : CommunityViewEvent()

    object FailGetCommunityLink: CommunityViewEvent()

    object RefreshCommunityRoad: CommunityViewEvent()

    object BaseLoadPosts: CommunityViewEvent()
}

enum class GetCommunityLinkAction {
    SHARE_LOCAL, SHARE_OUTSIDE, SCREENSHOT_POPUP
}
