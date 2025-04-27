package com.numplates.nomera3.modules.communities.ui.viewevent

import com.numplates.nomera3.modules.communities.data.entity.MeeraCommunityMembersEntity

sealed class CommunityMembersViewEvent {

    data class SuccessGetApprovedMembers(val members: MeeraCommunityMembersEntity, val isLoadMore: Boolean) :
        CommunityMembersViewEvent()

    data class ProgressGetApprovedMembers(val inProgress: Boolean) : CommunityMembersViewEvent()

    data class SuccessGetNotApprovedMembers(val members: MeeraCommunityMembersEntity, val isLoadMore: Boolean) :
        CommunityMembersViewEvent()

    data class ProgressGetNotApprovedMembers(val inProgress: Boolean) : CommunityMembersViewEvent()

    data class SuccessSearchMembers(
        val members: MeeraCommunityMembersEntity,
        val userState: Int,
        val isLoadMore: Boolean
    ) : CommunityMembersViewEvent()

    data class SuccessGetJoinRequestsCount(val count: Int) : CommunityMembersViewEvent()

    data class SuccessfullyBlockedApprovedMember(val position: Int) : CommunityMembersViewEvent()

    data class SuccessfullyBlockedNotApprovedMember(val position: Int) : CommunityMembersViewEvent()

    data class SuccessfullyAddedApprovedMemberToAdmins(val position: Int) : CommunityMembersViewEvent()

    data class SuccessfullyAddedNotApprovedMemberToAdmins(val position: Int) : CommunityMembersViewEvent()

    data class SuccessfullyRemovedMemberFromAdmins(val position: Int) : CommunityMembersViewEvent()

    data class SuccessfullyApprovedMembershipRequest(val position: Int) : CommunityMembersViewEvent()

    data class SuccessfullyDeclinedMembershipRequest(val position: Int) : CommunityMembersViewEvent()

    data class SuccessfullyRemovedMember(val position: Int) : CommunityMembersViewEvent()

    object RefreshApprovedMembers : CommunityMembersViewEvent()

    object RefreshNotApprovedMembers : CommunityMembersViewEvent()

    object ClearSearchApprovedMembers : CommunityMembersViewEvent()

    object ClearSearchNoApprovedMembers : CommunityMembersViewEvent()

    object ClearApprovedMembers : CommunityMembersViewEvent()

    object ClearNotApprovedMembers : CommunityMembersViewEvent()

    object FailedBlockingMember : CommunityMembersViewEvent()

    object FailedAddingMemberToAdmins : CommunityMembersViewEvent()

    object FailedRemovingMemberFromAdmins : CommunityMembersViewEvent()

    object FailedApprovingMembershipRequest : CommunityMembersViewEvent()

    object FailedDecliningMembershipRequest : CommunityMembersViewEvent()

    object FailedRemovingMember : CommunityMembersViewEvent()

    object FailGetMembers : CommunityMembersViewEvent()
}
