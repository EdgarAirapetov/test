package com.numplates.nomera3.modules.communities.ui.fragment.members

import com.numplates.nomera3.R
import com.numplates.nomera3.modules.communities.data.entity.CommunityMemberState
import com.numplates.nomera3.modules.communities.ui.viewevent.CommunityMembersViewEvent

class CommunityMembersListNotApprovedFragment :
    CommunityMembersListBaseFragment() {

    override val membersState: Int
        get() = CommunityMemberState.NOT_APPROVED

    override fun handleMembersViewEvent(event: CommunityMembersViewEvent) {
        when (event) {
            is CommunityMembersViewEvent.SuccessGetNotApprovedMembers -> {
                binding?.srlRefreshList?.isRefreshing = false
//                setMembersList(event.members)
            }

            is CommunityMembersViewEvent.SuccessfullyBlockedNotApprovedMember -> {
                adapter?.removeItem(event.position)
                showSuccessMessage(R.string.community_success_block_user)
            }
            is CommunityMembersViewEvent.SuccessfullyAddedNotApprovedMemberToAdmins -> {
                removeMember(event.position)
                viewModel.refreshApprovedMembers(membersState)
            }
            is CommunityMembersViewEvent.SuccessfullyApprovedMembershipRequest -> {
                removeMember(event.position)
                viewModel.refreshApprovedMembers(membersState)
            }
            is CommunityMembersViewEvent.SuccessfullyDeclinedMembershipRequest -> {
                removeMember(event.position)
            }
            is CommunityMembersViewEvent.SuccessfullyRemovedMember -> {
                removeMember(event.position)
            }
            is CommunityMembersViewEvent.ClearNotApprovedMembers -> {
                clearMembers()
            }
            is CommunityMembersViewEvent.FailedBlockingMember -> {
                showErrorMessage(R.string.community_block_member_error)
            }
            is CommunityMembersViewEvent.FailedAddingMemberToAdmins -> {
                showErrorMessage(R.string.community_set_admin_member_error)
            }
            is CommunityMembersViewEvent.FailedRemovingMemberFromAdmins -> {
                showErrorMessage(R.string.community_set_not_admin_member_error)
            }
            is CommunityMembersViewEvent.FailGetMembers -> {
                showErrorMessage(R.string.community_get_members_error_text)
            }
            is CommunityMembersViewEvent.FailedRemovingMember -> {
                showErrorMessage(R.string.community_remove_members_error_text)
            }
            is CommunityMembersViewEvent.ProgressGetNotApprovedMembers -> {
                showMembersProgress(event.inProgress)
            }
            else -> {}
        }
    }
}
