package com.numplates.nomera3.modules.communities.ui.fragment.members

import android.os.Bundle
import android.view.View
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.communities.data.entity.CommunityMemberState
import com.numplates.nomera3.modules.communities.ui.viewevent.CommunityMembersViewEvent
import com.numplates.nomera3.presentation.router.IArgContainer

class MeeraCommunityMembersListApprovedFragment :
    MeeraCommunityMembersListBaseFragment() {
    private var groupId: Int? = null
    override var userRole: Int = 0
    override val membersState: Int
        get() = CommunityMemberState.APPROVED

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getInt(IArgContainer.ARG_GROUP_ID)?.let {
            groupId = it
        }
        arguments?.getInt(IArgContainer.ARG_COMMUNITY_USER_ROLE)?.let {
            userRole = it
        }
        viewModel.bind(groupId = groupId)
        viewModel.getData(userState = membersState)
    }

    override fun handleMembersViewEvent(event: CommunityMembersViewEvent) {
        when (event) {
            is CommunityMembersViewEvent.SuccessGetApprovedMembers -> {
                binding?.srlRefreshList?.isRefreshing = false
                setMembersList(members = event.members, isLoadMore = event.isLoadMore)
            }

            is CommunityMembersViewEvent.SuccessfullyBlockedApprovedMember -> {
                refreshList()
                showSuccessMessage(R.string.community_success_block_user)
            }

            is CommunityMembersViewEvent.SuccessfullyAddedApprovedMemberToAdmins -> {
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
                adapter?.updateUserRole(userRole)
                showMembersProgress(event.inProgress)
            }

            is CommunityMembersViewEvent.SuccessSearchMembers -> {
                if (event.userState == CommunityMemberState.APPROVED) setMembersList(
                    members = event.members,
                    isSearchResult = true,
                    isLoadMore = event.isLoadMore
                )
            }

            is CommunityMembersViewEvent.ClearSearchApprovedMembers -> {
                clearSearchAndUpdateRecycler()
            }

            else -> Unit
        }
    }
}
