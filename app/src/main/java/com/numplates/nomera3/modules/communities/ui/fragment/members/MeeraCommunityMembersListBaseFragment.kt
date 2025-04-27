package com.numplates.nomera3.modules.communities.ui.fragment.members

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.dialogs.unlimiteditem.MeeraConfirmDialogUnlimitedListBuilder
import com.meera.core.dialogs.unlimiteditem.MeeraConfirmDialogUnlimitedNumberItemsData
import com.meera.core.extensions.gone
import com.meera.core.extensions.invisible
import com.meera.core.extensions.pluralString
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.visible
import com.meera.core.utils.showCommonError
import com.meera.core.utils.showCommonSuccessMessage
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.MeeraUserInfoModel
import com.numplates.nomera3.databinding.MeeraCommunityMembersListFragmentBinding
import com.numplates.nomera3.modules.communities.data.entity.CommunityMemberState
import com.numplates.nomera3.modules.communities.data.entity.CommunityUserRole
import com.numplates.nomera3.modules.communities.data.entity.MeeraCommunityMembersEntity
import com.numplates.nomera3.modules.communities.ui.adapter.MeeraMembersAdapter
import com.numplates.nomera3.modules.communities.ui.adapter.UserInfoModelRecyclerData
import com.numplates.nomera3.modules.communities.ui.viewevent.CommunityMembersViewEvent
import com.numplates.nomera3.modules.communities.ui.viewmodel.CommunityMembersViewModel
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.skydoves.baserecyclerviewadapter.RecyclerViewPaginator

private const val FIRST_PAGE = 1

abstract class MeeraCommunityMembersListBaseFragment :
    MeeraBaseDialogFragment(
        layout = R.layout.meera_community_members_list_fragment,
        behaviourConfigState = ScreenBehaviourState.Full
    ) {
    override val containerId: Int
        get() = R.id.fragment_first_container_view
    protected val viewModel by viewModels<CommunityMembersViewModel>(
        ownerProducer = { requireParentFragment() }
    ) {
        App.component.getViewModelFactory()
    }
    protected var adapter: MeeraMembersAdapter? = null
    private var paginator: RecyclerViewPaginator? = null
    protected val binding by viewBinding(MeeraCommunityMembersListFragmentBinding::bind)
    abstract val membersState: Int
    abstract var userRole: Int
    private var isLastPage = false
    private var userList = mutableListOf<MeeraUserInfoModel>()

    abstract fun handleMembersViewEvent(event: CommunityMembersViewEvent)

    protected fun refreshList() {
        paginator?.resetCurrentPage()
        viewModel.getData(userState = membersState)
        viewModel.updateGroupInfo()
        binding?.srlRefreshList?.isRefreshing = false
        userList.clear()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initObservers()
    }

    private fun initView() {
        binding?.srlRefreshList?.setOnRefreshListener {
            if (viewModel.getSearchQuery().isNullOrEmpty()) {
                refreshList()
            } else {
                binding?.srlRefreshList?.isRefreshing = false
            }
        }
        adapter = MeeraMembersAdapter(
            userId = viewModel.getUserUid(),
            listType = membersState,
            actionListener = ::initAdapterListener
        )
        binding?.rvMembersList?.also {
            it.layoutManager = LinearLayoutManager(context)
            it.adapter = adapter
            paginator = RecyclerViewPaginator(
                recyclerView = it,
                loadMore = {
                    if (it != FIRST_PAGE) viewModel.loadMore(adapter?.itemCount ?: 0, membersState)
                },
                isLoading = viewModel::isLoading,
                onLast = { isLastPage }
            ).apply {
                endWithAuto = true
            }
            it.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val layoutManager =
                        it.layoutManager as? LinearLayoutManager
                    val firstVisibleItemPosition = layoutManager?.findFirstCompletelyVisibleItemPosition()
                    if (firstVisibleItemPosition != 0) {
                        binding.vDividerElevation.visible()
                    } else {
                        binding.vDividerElevation.invisible()
                    }

                }
            })
        }
    }

    private fun initObservers() {
        viewModel.liveViewEvent.observe(viewLifecycleOwner, ::handleMembersViewEvent)
    }

    private fun initAdapterListener(action: MeeraMembersActionClick) {

        when (action) {
            is MeeraMembersActionClick.MemberActionClicked -> {
                showActionsBottomSheetMenu(action.member, action.position)
            }

            is MeeraMembersActionClick.MemberClicked -> {
//                add(
//                    UserInfoFragment(), Act.COLOR_STATUSBAR_LIGHT_NAVBAR,
//                    Arg(IArgContainer.ARG_USER_ID, action.model.uid),
//                    Arg(IArgContainer.ARG_TRANSIT_FROM, AmplitudePropertyWhere.COMMUNITY.property)
//                )
            }

            is MeeraMembersActionClick.MembershipApproveClicked -> {
                showApprovingBottomSheet(action.member, action.position)
            }
        }
    }

    protected fun clearSearchAndUpdateRecycler() {
        userList.clear()
        adapter?.submitList(listOf())
        viewModel.getData(userState = membersState)
    }

    protected fun setMembersList(
        members: MeeraCommunityMembersEntity,
        isSearchResult: Boolean = false,
        isLoadMore: Boolean
    ) {
        val users = members.users
        if (!isLoadMore) userList.clear()
        when {
            users?.isNotEmpty() == true -> {
                userList.addAll(users)
                if (userList.size == members.totalCount) isLastPage = true
                adapter?.submitList(userList.map { UserInfoModelRecyclerData.UserInfoData(it.user.userId, it) })
                binding.rvMembersList.visible()
                binding.vNoMembershipRequests.gone()
                binding.vPlaceholderEmptyListUsers.gone()
                if (isSearchResult) {
                    binding.tvMembersListTitle.text = context?.getString(R.string.general_search_results)
                } else {
                    members.totalCount?.let { count ->
                        binding?.tvMembersListTitle?.text =
                            context?.pluralString(R.plurals.group_members_plural, count)
                        binding?.tvMembersListTitle?.visible()
                    }
                }
            }

            users.isNullOrEmpty() && membersState == CommunityMemberState.NOT_APPROVED -> {
                showEmptyListPlaceholder()
            }

            users.isNullOrEmpty() && membersState == CommunityMemberState.APPROVED -> {
                showEmptyPlaceholderAllUsersList()
                binding.tvMembersListTitle.text =
                    context?.getString(R.string.general_search_results)
            }
        }
    }

    protected fun clearMembers() {
        adapter?.submitList(mutableListOf())
    }

    protected fun removeMember(position: Int) {
        val newCurrentList = adapter?.currentList?.toMutableList()
        newCurrentList?.removeAt(position)
        adapter?.submitList(newCurrentList)
        adapter?.itemCount?.let { count ->
            binding.tvMembersListTitle.text = context?.pluralString(R.plurals.group_members_plural, count - 1)
            if (count == 1) findNavController().popBackStack()
        }
        if (adapter?.hasItems() != true) showEmptyListPlaceholder()
    }

    protected fun showSuccessMessage(@StringRes messageRes: Int) {
        showCommonSuccessMessage(getText(messageRes), requireView())
    }

    protected fun showErrorMessage(@StringRes messageRes: Int) {
        showCommonError(getText(messageRes), requireView())
    }

    protected fun showMembersProgress(inProgress: Boolean) {
        if (inProgress) binding?.pbMembers?.visible()
        else binding?.pbMembers?.gone()
    }

    private fun showEmptyListPlaceholder() {
        binding?.vNoMembershipRequests?.visible()
        binding?.tvMembersListTitle?.gone()
        binding?.rvMembersList?.gone()
    }

    private fun showEmptyPlaceholderAllUsersList() {
        binding?.vPlaceholderEmptyListUsers?.visible()
        binding?.tvMembersListTitle?.gone()
        binding?.rvMembersList?.gone()
    }

    private fun showActionsBottomSheetMenu(member: MeeraUserInfoModel, position: Int) {
        var firstTitle = getString(R.string.menu_chat_set_administrator)
        var firstSubtitle = getString(R.string.meera_subtitle_menu_chat_set_administrator)
        var firstIcon = R.drawable.ic_outlined_user_s

        if (member.isModerator.toBoolean()) {
            firstTitle = getString(R.string.menu_chat_delete_admin)
            firstSubtitle = getString(R.string.meera_subtitle_menu_chat_delete_admin)
            firstIcon = R.drawable.ic_outlined_user_delete_2_m

        }
        MeeraCommunityMembersListDialogBuilder()
            .setFirstItemTitle(firstTitle)
            .setFirstItemSubtitle(firstSubtitle)
            .setFirstItemIcon(firstIcon)
            .setSecondItemTitle(getString(R.string.general_block))
            .setSecondItemSubtitle(getString(R.string.meera_subtitle_community_blocking_member))
            .setSecondItemIcon(R.drawable.ic_outlined_user_block_m)
            .setSecondItemColorIcon(R.color.uiKitColorLegacyAccentSecondary)
            .setThirdItemTitle(getString(R.string.community_remove_member))
            .setThirdItemSubtitle(getString(R.string.meera_subtitle_community_remove_member))
            .setThirdItemIcon(R.drawable.ic_outlined_user_delete_m)
            .setThirdItemColorIcon(R.color.uiKitColorLegacyAccentSecondary)
            .setMenuItemClickListener {
                initSettingsMenuListener(
                    action = it,
                    member = member,
                    position = position
                )
            }
            .show(childFragmentManager)
    }

    private fun initSettingsMenuListener(
        action: MeeraCommunityMembersDialogAction,
        member: MeeraUserInfoModel,
        position: Int
    ) {
        when (action) {
            MeeraCommunityMembersDialogAction.FirstMenuItemClick -> {
                if (viewModel.getUserRole() == CommunityUserRole.AUTHOR && member.isModerator.toBoolean()) {
                    viewModel.removeMemberAdminStatus(member.user.userId, position)
                } else if (viewModel.getUserRole() == CommunityUserRole.AUTHOR && !member.isModerator.toBoolean()) {
                    viewModel.setMemberAdminStatus(member.user.userId, position, membersState)
                }
            }

            MeeraCommunityMembersDialogAction.SecondMenuItemClick -> {
                viewModel.blockMember(member.user.userId, position, membersState)
            }

            MeeraCommunityMembersDialogAction.ThirdMenuItemClick -> {
                viewModel.removeCommunityMember(member.user.userId, position)
            }
        }
    }

    private fun showApprovingBottomSheet(member: MeeraUserInfoModel, position: Int) {
        MeeraConfirmDialogUnlimitedListBuilder()
            .setHeader(R.string.actions)
            .setListItems(initListItemCommunityMembersMenu())
            .setItemListener { action ->
                initCommunityMembersMenuListener(
                    action = action as MeeraCommunityMembersMenuAction,
                    member = member,
                    position = position
                )
            }.show(childFragmentManager)
    }

    private fun initCommunityMembersMenuListener(
        action: MeeraCommunityMembersMenuAction,
        member: MeeraUserInfoModel,
        position: Int
    ) {
        when (action) {
            MeeraCommunityMembersMenuAction.Accept -> {
                viewModel.approveMembershipRequest(member.user.userId, position, false)
            }

            MeeraCommunityMembersMenuAction.AcceptMakeAdmin -> {
                viewModel.approveMembershipRequest(member.user.userId, position, true)
            }

            MeeraCommunityMembersMenuAction.Decline -> {
                viewModel.declineMembershipRequest(member.user.userId, position, false)
            }

            MeeraCommunityMembersMenuAction.DeclineBlock -> {
                viewModel.declineMembershipRequest(member.user.userId, position, true)
            }
        }
    }

    private fun initListItemCommunityMembersMenu(): List<MeeraConfirmDialogUnlimitedNumberItemsData> {
        return listOf(
            MeeraConfirmDialogUnlimitedNumberItemsData(
                name = R.string.accept_request,
                icon = R.drawable.ic_outlined_user_add_m,
                action = MeeraCommunityMembersMenuAction.Accept,
            ),
            MeeraConfirmDialogUnlimitedNumberItemsData(
                name = R.string.meera_accept_request_make_admin,
                icon = R.drawable.ic_outlined_admin_add_m,
                action = MeeraCommunityMembersMenuAction.AcceptMakeAdmin,
            ),
            MeeraConfirmDialogUnlimitedNumberItemsData(
                name = R.string.decline_request,
                icon = R.drawable.ic_outlined_user_delete_m,
                contentColor = R.color.uiKitColorLegacyAccentSecondary,
                action = MeeraCommunityMembersMenuAction.Decline,
            ),
            MeeraConfirmDialogUnlimitedNumberItemsData(
                name = R.string.decline_request_block,
                icon = R.drawable.ic_outlined_user_block_m,
                contentColor = R.color.uiKitColorLegacyAccentSecondary,
                action = MeeraCommunityMembersMenuAction.DeclineBlock,
            )
        )
    }
}
