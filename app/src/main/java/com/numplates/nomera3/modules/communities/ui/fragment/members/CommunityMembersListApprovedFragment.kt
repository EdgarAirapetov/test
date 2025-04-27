package com.numplates.nomera3.modules.communities.ui.fragment.members

import com.numplates.nomera3.R
import com.numplates.nomera3.modules.communities.data.entity.CommunityMemberState
import com.numplates.nomera3.modules.communities.ui.viewevent.CommunityMembersViewEvent

class CommunityMembersListApprovedFragment :
    CommunityMembersListBaseFragment() {

    override val membersState: Int
        get() = CommunityMemberState.APPROVED

    override fun handleMembersViewEvent(event: CommunityMembersViewEvent) {
        when (event) {
            is CommunityMembersViewEvent.SuccessGetApprovedMembers -> {
                binding?.srlRefreshList?.isRefreshing = false
//                setMembersList(event.members)
            }
            is CommunityMembersViewEvent.SuccessfullyBlockedApprovedMember -> {
                adapter?.removeItem(event.position)
                showSuccessMessage(R.string.community_success_block_user)
            }
            is CommunityMembersViewEvent.SuccessfullyAddedApprovedMemberToAdmins-> {
                adapter?.setAdmin(event.position)
            }
            is CommunityMembersViewEvent.SuccessfullyRemovedMemberFromAdmins -> {
                adapter?.setNotAdmin(event.position)
            }
            is CommunityMembersViewEvent.RefreshApprovedMembers -> {
                refreshList()
            }
            is CommunityMembersViewEvent.SuccessfullyRemovedMember -> {
                removeMember(event.position)
            }
            is CommunityMembersViewEvent.ClearApprovedMembers -> {
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
            is CommunityMembersViewEvent.ProgressGetApprovedMembers -> {
                showMembersProgress(event.inProgress)
            }
            else -> {}
        }
    }
}
