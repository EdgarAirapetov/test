package com.numplates.nomera3.modules.communities.ui.fragment.members

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.meera.core.extensions.gone
import com.meera.core.extensions.pluralString
import com.meera.core.extensions.visible
import com.meera.core.utils.NSnackbar
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.UserInfoModel
import com.numplates.nomera3.databinding.FragmentCommunityMembersListBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.communities.data.entity.CommunityMemberState
import com.numplates.nomera3.modules.communities.data.entity.CommunityMembersEntity
import com.numplates.nomera3.modules.communities.data.entity.CommunityUserRole
import com.numplates.nomera3.modules.communities.ui.adapter.MembersAdapter
import com.numplates.nomera3.modules.communities.ui.viewevent.CommunityMembersViewEvent
import com.numplates.nomera3.modules.communities.ui.viewmodel.CommunityMembersViewModel
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_ID
import com.numplates.nomera3.presentation.view.fragments.UserInfoFragment
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraMenuBottomSheet
import com.skydoves.baserecyclerviewadapter.RecyclerViewPaginator

private const val NTOAST_BOTTOM_MARGIN = 52

abstract class CommunityMembersListBaseFragment :
    BaseFragmentNew<FragmentCommunityMembersListBinding>() {

    protected lateinit var viewModel: CommunityMembersViewModel
    protected var adapter: MembersAdapter? = null
    private var paginator: RecyclerViewPaginator? = null

    abstract val membersState: Int

    abstract fun handleMembersViewEvent(event: CommunityMembersViewEvent)

    protected fun refreshList() {
        adapter?.clearMembers()
        paginator?.resetCurrentPage()
        viewModel.getData(userState = membersState)
        viewModel.updateGroupInfo()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(act).get(CommunityMembersViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initAdapterListener()
        initObservers()
        refreshList()
    }

    private fun initView() {
        binding?.srlRefreshList?.setOnRefreshListener {
            refreshList()
        }
        adapter = MembersAdapter(
            userId = viewModel.getUserUid(),
            userRole = viewModel.getUserRole(),
            listType = membersState,
            hideAgeAndGender = viewModel.isHiddenAgeAndGender()
        )
        binding?.rvMembersList?.also {
            it.layoutManager = LinearLayoutManager(context)
            it.adapter = adapter
            paginator = RecyclerViewPaginator(
                recyclerView = it,
                loadMore = { viewModel.loadMore(adapter?.itemCount ?: 0, membersState) },
                isLoading = viewModel::isLoading,
                onLast = viewModel::isLastPage
            ).apply {
                endWithAuto = true
            }
        }
    }

    private fun initObservers() {
        viewModel.liveViewEvent.observe(viewLifecycleOwner, ::handleMembersViewEvent)
    }

    private fun initAdapterListener() {
        adapter?.onMemberClicked = {
            add(UserInfoFragment(), Act.COLOR_STATUSBAR_LIGHT_NAVBAR,
                Arg(ARG_USER_ID, it.uid),
                Arg(IArgContainer.ARG_TRANSIT_FROM, AmplitudePropertyWhere.COMMUNITY.property)
            )
        }
        when (membersState) {
            CommunityMemberState.NOT_APPROVED -> adapter?.onMembershipApproveClicked =
                { member, position ->
                    showApprovingBottomSheet(member, position)
                }
            else -> adapter?.onMemberActionClicked = { member, position ->
                showActionsBottomSheetMenu(member, position)
            }
        }
    }

    protected fun setMembersList(members: CommunityMembersEntity) {
        val users = members.users
        when {
            users?.isNotEmpty() == true -> {
                adapter?.addMembers(users)
                binding?.rvMembersList?.visible()
                binding?.tvNoMembershipRequests?.gone()
                members.totalCount?.let { count ->
                    binding?.tvMembersListTitle?.text =
                        context?.pluralString(R.plurals.group_members_plural, count)
                    binding?.tvMembersListTitle?.visible()
                }
            }
            users.isNullOrEmpty() && membersState == CommunityMemberState.NOT_APPROVED -> {
                showEmptyListPlaceholder()
            }
        }
    }

    protected fun clearMembers() {
        adapter?.clearMembers()
    }

    protected fun removeMember(position: Int) {
        adapter?.removeItem(position)
        if (adapter?.hasItems() != true) showEmptyListPlaceholder()
    }

    protected fun showSuccessMessage(@StringRes messageRes: Int) {
        NSnackbar.with(requireView())
            .typeSuccess()
            .marginBottom(NTOAST_BOTTOM_MARGIN)
            .text(getString(messageRes))
            .show()
    }

    protected fun showErrorMessage(@StringRes messageRes: Int) {
        NSnackbar.with(requireView())
            .typeError()
            .text(getString(messageRes))
            .show()
    }

    protected fun showMembersProgress(inProgress: Boolean) {
        if (inProgress) binding?.pbMembers?.visible()
        else binding?.pbMembers?.gone()
    }

    private fun showEmptyListPlaceholder() {
        binding?.tvNoMembershipRequests?.visible()
        binding?.tvMembersListTitle?.gone()
        binding?.rvMembersList?.gone()
    }

    private fun showActionsBottomSheetMenu(member: UserInfoModel, position: Int) {
        val menu = MeeraMenuBottomSheet(context)

        if (viewModel.getUserRole() == CommunityUserRole.AUTHOR && member.isModerator == true) {
            menu.addItem(getString(R.string.menu_chat_delete_admin), R.drawable.ic_remove_admin) {
                viewModel.removeMemberAdminStatus(member.uid, position)
            }
        } else if (viewModel.getUserRole() == CommunityUserRole.AUTHOR && member.isModerator == false) {
            menu.addItem(getString(R.string.menu_chat_set_administrator), R.drawable.ic_add_admin) {
                viewModel.setMemberAdminStatus(member.uid, position, membersState)
            }
        }

        menu.addItem(getString(R.string.general_block), R.drawable.ic_block_user_red) {
            viewModel.blockMember(member.uid, position, membersState)
        }

        menu.addItem(getString(R.string.community_remove_member), R.drawable.ic_remove_user) {
            viewModel.removeCommunityMember(member.uid, position)
        }

        menu.show(childFragmentManager)
    }

    private fun showApprovingBottomSheet(member: UserInfoModel, position: Int) {
        val menu = MeeraMenuBottomSheet(context)

        menu.addItem(getString(R.string.accept_request), R.drawable.ic_add_friend_purple) {
            viewModel.approveMembershipRequest(member.uid, position, false)
        }

        if (viewModel.getUserRole() == CommunityUserRole.AUTHOR) {
            menu.addItem(getString(R.string.accept_request_make_admin), R.drawable.ic_add_admin) {
                viewModel.approveMembershipRequest(member.uid, position, true)
            }
        }

        menu.addItem(
            getString(R.string.decline_request),
            R.drawable.ic_reject_friend_red
        ) {
            viewModel.declineMembershipRequest(member.uid, position, false)
        }

        menu.addItem(getString(R.string.decline_request_block), R.drawable.ic_block_user_red) {
            viewModel.declineMembershipRequest(member.uid, position, true)
        }

        menu.show(childFragmentManager)
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentCommunityMembersListBinding
        get() = FragmentCommunityMembersListBinding::inflate
}
