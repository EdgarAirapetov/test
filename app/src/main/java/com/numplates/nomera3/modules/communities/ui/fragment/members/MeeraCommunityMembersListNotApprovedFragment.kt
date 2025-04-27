package com.numplates.nomera3.modules.communities.ui.fragment.members

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.meera.core.extensions.gone
import com.meera.core.extensions.pluralString
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.communities.data.entity.CommunityMemberState
import com.numplates.nomera3.modules.communities.ui.viewevent.CommunityMembersViewEvent
import com.numplates.nomera3.presentation.router.IArgContainer

private const val LAST_USER = 1

class MeeraCommunityMembersListNotApprovedFragment :
    MeeraCommunityMembersListBaseFragment() {

    private var groupId: Int? = null
    override var userRole: Int = 0
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

    override val membersState: Int
        get() = CommunityMemberState.NOT_APPROVED

    override fun handleMembersViewEvent(event: CommunityMembersViewEvent) {
        when (event) {
            is CommunityMembersViewEvent.SuccessGetNotApprovedMembers -> {
                binding?.srlRefreshList?.isRefreshing = false
                setMembersList(members = event.members, isLoadMore = event.isLoadMore)
            }

            is CommunityMembersViewEvent.SuccessfullyBlockedNotApprovedMember -> {
                adapter?.itemCount?.let { count ->
                    if (count <= LAST_USER) {
                        binding.tvMembersListTitle.gone()
                        binding.rvMembersList.gone()
                        findNavController().popBackStack()
                    } else {
                        val newCurrentList = adapter?.currentList?.toMutableList()
                        newCurrentList?.removeAt(event.position)
                        adapter?.submitList(newCurrentList)
                        adapter?.itemCount?.let { count ->
                            binding.tvMembersListTitle.text =
                                context?.pluralString(R.plurals.group_members_plural, count - LAST_USER)
                        }
                    }
                    showSuccessMessage(R.string.community_success_block_user)
                }
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
                adapter?.updateUserRole(userRole)
                showMembersProgress(event.inProgress)
            }

            is CommunityMembersViewEvent.SuccessSearchMembers -> {
                if (event.userState == CommunityMemberState.NOT_APPROVED) {
                    setMembersList(event.members, true, event.isLoadMore)
                }
            }

            is CommunityMembersViewEvent.RefreshNotApprovedMembers -> {
                refreshList()
            }

            is CommunityMembersViewEvent.ClearSearchNoApprovedMembers -> {
                clearSearchAndUpdateRecycler()
            }

            else -> Unit
        }
    }
}
