package com.numplates.nomera3.modules.communities.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.dialogs.MeeraConfirmDialogBuilder
import com.meera.core.extensions.gone
import com.meera.core.extensions.invisible
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.state.ErrorSnakeState
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.buttons.ButtonType
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentCommunityBlacklistBinding
import com.numplates.nomera3.modules.communities.ui.entity.CommunityConstant
import com.numplates.nomera3.modules.communities.ui.fragment.blacklist.CommunityBlacklistAdapter
import com.numplates.nomera3.modules.communities.ui.fragment.blacklist.CommunityBlacklistUIModel
import com.numplates.nomera3.modules.communities.ui.fragment.blacklist.MeeraCommunityBlacklistAdapter
import com.numplates.nomera3.modules.communities.ui.fragment.blacklist.MeeraCommunityBlacklistAdapter.Companion.RESERVED_LIST_SIZE
import com.numplates.nomera3.modules.communities.ui.viewmodel.blacklist.CommunityBlacklistViewModel
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.presentation.router.IArgContainer
import com.skydoves.baserecyclerviewadapter.RecyclerViewPaginator

private const val QUESTION_MARK = "?"


class MeeraCommunityBlacklistFragment : MeeraBaseDialogFragment(
    layout = R.layout.meera_fragment_community_blacklist,
    behaviourConfigState = ScreenBehaviourState.Full
) {

    private val binding by viewBinding(MeeraFragmentCommunityBlacklistBinding::bind)
    private val viewModel by viewModels<CommunityBlacklistViewModel>()
    private var adapter: MeeraCommunityBlacklistAdapter? = null
    private var errorSnackbar: UiKitSnackBar? = null

    override val containerId: Int
        get() = R.id.fragment_second_container_view

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.communityId = arguments?.getInt(IArgContainer.ARG_GROUP_ID) ?: CommunityConstant.UNKNOWN_COMMUNITY_ID
        viewModel.initCommunityBlacklistLoader()
        binding.vgCommunityBlacklistPlaceholder.gone()
        setViewClickListeners()
        setBlacklistAdapter()
        setBlacklistRecyclerView()
        setLiveDataListeners()
        loadNextData()
    }

    override fun onStop() {
        super.onStop()
        errorSnackbar?.dismiss()
    }

    private fun setBlacklistAdapter() {
        adapter = MeeraCommunityBlacklistAdapter()
        adapter?.onDataSetChanged = { onDataSetChanged(it) }
        adapter?.blacklistItemClickListener = { openUserProfileScreen(it?.memberId) }
        adapter?.blacklistContextMenuClickListener = { showUnblockMemberBottomSheetDialog(it) }
    }

    private fun onDataSetChanged(newAdapterItemListSize: Int) {
        if (newAdapterItemListSize <= CommunityBlacklistAdapter.RESERVED_LIST_SIZE) {
            showBlacklistPlaceHolderView()
        } else {
            showBlacklistView()
        }
    }

    private fun showUnblockMemberBottomSheetDialog(member: CommunityBlacklistUIModel.BlacklistedMemberUIModel?) {
        val memberId = member?.memberId
        val memberName = "${getString(R.string.general_unblock)} ${member?.memberName}$QUESTION_MARK"
        MeeraConfirmDialogBuilder()
            .setHeader(memberName)
            .setDescription(getString(R.string.meera_settings_privacy_dialog_unblock_all_confirm_subtitle))
            .setTopBtnText(R.string.settings_privacy_dialog_unblock_all_confirm)
            .setTopBtnType(ButtonType.FILLED)
            .setBottomBtnText(R.string.cancel)
            .setTopClickListener { removeMemberFromBlacklist(memberId) }
            .show(childFragmentManager)
    }

    private fun showClearBlacklistDialog() {
        MeeraConfirmDialogBuilder()
            .setHeader(getString(R.string.settings_privacy_dialog_unblock_all_title))
            .setDescription(getString(R.string.meera_settings_privacy_dialog_unblock_all_subtitle))
            .setTopBtnText(R.string.settings_privacy_dialog_unblock_all_confirm)
            .setTopBtnType(ButtonType.FILLED)
            .setBottomBtnText(R.string.cancel)
            .setTopClickListener { viewModel.clearBlacklist() }
            .show(childFragmentManager)
    }

    private fun removeMemberFromBlacklist(userId: Long?) {
        if (userId != null) {
            viewModel.removeMemberFromBlacklist(userId)
        } else {
            showAlertNToastAtScreenTop(R.string.error_cant_unblock_user)
        }
    }

    private fun openUserProfileScreen(userId: Long?) {
        if (userId != null) {
            // TODO: GOTO MeeraUserInfoFragment
            // add(UserInfoFragment(), Act.LIGHT_STATUSBAR, Arg(IArgContainer.ARG_USER_ID, userId))
        } else {
            showAlertNToastAtScreenTop(R.string.community_blacklist_screen_error_cant_open_user_profile)
        }
    }

    private fun setBlacklistRecyclerView() {
        binding.rvCommunityBlacklist.also { recyclerView ->
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = adapter
            addRecyclerViewPagingHelper(recyclerView)
        }
    }

    private fun addRecyclerViewPagingHelper(recyclerView: RecyclerView) {
        RecyclerViewPaginator(
            recyclerView = recyclerView,
            onLast = viewModel::isListEndReached,
            isLoading = viewModel::isLoading,
            loadMore = { loadNextData() },
        ).apply {
            endWithAuto = true
        }
    }

    private fun loadNextData() {
        viewModel.loadNextData()
    }

    private fun setViewClickListeners() {
        binding.navCommunityBlacklist.backButtonClickListener = {
            findNavController().popBackStack()
        }
        binding.tvBtnGroupBlacklistDelete.setThrottledClickListener {
            showClearBlacklistDialog()
        }
    }

    private fun setLiveDataListeners() {
        viewModel.eventLiveData.observe(viewLifecycleOwner) { newEvent ->
            when (newEvent) {
                is CommunityBlacklistViewModel.CommunityBlacklistScreenEvent.BlacklistLoadingFailed -> {
                    showAlertNToastAtScreenTop(R.string.community_general_error)
                }

                is CommunityBlacklistViewModel.CommunityBlacklistScreenEvent.BlacklistLoadingStarted -> Unit
                is CommunityBlacklistViewModel.CommunityBlacklistScreenEvent.BlacklistLoadingSuccess -> {
                    onBlacklistLoadingSuccess(newEvent.uiModel)
                }

                is CommunityBlacklistViewModel.CommunityBlacklistScreenEvent.UnblockingUserFailed -> {
                    showAlertNToastAtScreenTop(R.string.community_general_error)
                }

                is CommunityBlacklistViewModel.CommunityBlacklistScreenEvent.UnblockingUserStarted -> Unit
                is CommunityBlacklistViewModel.CommunityBlacklistScreenEvent.UnblockingUserSuccess -> {
                    adapter?.removeItem(newEvent.userId)
                    updateData()
                }

                is CommunityBlacklistViewModel.CommunityBlacklistScreenEvent.UnblockingAllUserFailed -> {
                    showAlertNToastAtScreenTop(R.string.community_general_error)
                }

                is CommunityBlacklistViewModel.CommunityBlacklistScreenEvent.UnblockingAllUserSuccess -> {
                    adapter?.clearItemList()
                    showBlacklistPlaceHolderView()
                }
            }
        }
    }

    private fun updateData() {
        val blacklisted = adapter?.blacklist?.filterIsInstance<CommunityBlacklistUIModel.BlacklistedMemberUIModel>()
        if (blacklisted?.isEmpty() == true) {
            adapter?.clearItemList()
            showBlacklistPlaceHolderView()
        }
    }

    private fun onBlacklistLoadingSuccess(uiModel: List<CommunityBlacklistUIModel.BlacklistedMemberUIModel>) {
        if (viewModel.communityBlacklistTotalSize > 0) {
            adapter?.updateHeaderTextIfNeeded(viewModel.communityBlacklistTotalSize)

            uiModel
                .takeIf { it.isNotEmpty() }
                ?.also { newBlacklistedMemberList: List<CommunityBlacklistUIModel.BlacklistedMemberUIModel> ->
                    adapter?.addItemList(newBlacklistedMemberList)
                }

            showBlacklistView()
        } else {
            if (adapter?.blacklist?.size ?: 0 <= RESERVED_LIST_SIZE) {
                showBlacklistPlaceHolderView()
            }
        }
    }

    private fun showBlacklistPlaceHolderView() {
        binding.apply {
            rvCommunityBlacklist.invisible()
            tvBtnGroupBlacklistDelete.invisible()
            vgCommunityBlacklistPlaceholder.visible()
        }
    }

    private fun showBlacklistView() {
        binding.apply {
            vgCommunityBlacklistPlaceholder.invisible()
            tvBtnGroupBlacklistDelete.visible()
            rvCommunityBlacklist.visible()
        }
    }

    private fun showAlertNToastAtScreenTop(@StringRes stringRes: Int) {
        errorSnackbar = UiKitSnackBar.makeError(
            view = requireView(),
            params = SnackBarParams(
                errorSnakeState = ErrorSnakeState(
                    messageText = getText(stringRes)
                )
            )
        )
        errorSnackbar?.show()
    }

}
