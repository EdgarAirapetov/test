package com.numplates.nomera3.modules.communities.ui.fragment.blacklist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentCommunityBlacklistBinding
import com.numplates.nomera3.modules.communities.ui.entity.CommunityConstant.UNKNOWN_COMMUNITY_ID
import com.numplates.nomera3.modules.communities.ui.fragment.blacklist.CommunityBlacklistAdapter.Companion.RESERVED_LIST_SIZE
import com.numplates.nomera3.modules.communities.ui.fragment.blacklist.CommunityBlacklistUIModel.BlacklistedMemberUIModel
import com.numplates.nomera3.modules.communities.ui.viewmodel.blacklist.CommunityBlacklistViewModel
import com.numplates.nomera3.modules.communities.ui.viewmodel.blacklist.CommunityBlacklistViewModel.CommunityBlacklistScreenEvent.BlacklistLoadingFailed
import com.numplates.nomera3.modules.communities.ui.viewmodel.blacklist.CommunityBlacklistViewModel.CommunityBlacklistScreenEvent.BlacklistLoadingStarted
import com.numplates.nomera3.modules.communities.ui.viewmodel.blacklist.CommunityBlacklistViewModel.CommunityBlacklistScreenEvent.BlacklistLoadingSuccess
import com.numplates.nomera3.modules.communities.ui.viewmodel.blacklist.CommunityBlacklistViewModel.CommunityBlacklistScreenEvent.UnblockingUserFailed
import com.numplates.nomera3.modules.communities.ui.viewmodel.blacklist.CommunityBlacklistViewModel.CommunityBlacklistScreenEvent.UnblockingUserStarted
import com.numplates.nomera3.modules.communities.ui.viewmodel.blacklist.CommunityBlacklistViewModel.CommunityBlacklistScreenEvent.UnblockingUserSuccess
import com.numplates.nomera3.modules.communities.ui.viewmodel.blacklist.CommunityBlacklistViewModel.CommunityBlacklistScreenEvent.UnblockingAllUserFailed
import com.numplates.nomera3.modules.communities.ui.viewmodel.blacklist.CommunityBlacklistViewModel.CommunityBlacklistScreenEvent.UnblockingAllUserSuccess
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.fragments.UserInfoFragment
import com.numplates.nomera3.presentation.view.fragments.dialogs.ConfirmDialogBuilder
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraMenuBottomSheet
import com.meera.core.extensions.click
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.invisible
import com.meera.core.extensions.gone
import com.meera.core.extensions.tryCatch
import com.meera.core.extensions.visible
import com.numplates.nomera3.Act
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_ID
import com.numplates.nomera3.presentation.view.utils.NToast
import com.skydoves.baserecyclerviewadapter.RecyclerViewPaginator

/**
 * Экран "Черный список"
 *
 * https://overflow.io/s/0GXL4TMC?node=08dd0ae1
 * https://www.figma.com/file/oi39VGnKVAkMekn5CNrQG8/May-2021?node-id=202%3A85621
 * https://nomera.atlassian.net/wiki/spaces/NOM/pages/2288550188/-#Экран-“Чёрный-список“
 * */
class CommunityBlacklistFragment : BaseFragmentNew<FragmentCommunityBlacklistBinding>() {

    private val viewModel by viewModels<CommunityBlacklistViewModel>()

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentCommunityBlacklistBinding
        get() = FragmentCommunityBlacklistBinding::inflate

    private val blacklistRecyclerView: RecyclerView?
        get() = binding?.blacklist

    private val blacklistPlaceholder: View?
        get() = binding?.blacklistPlaceholder

    private val statusBarStubView: View?
        get() = binding?.statusBarStub

    private val backButtonView: View?
        get() = binding?.backButton

    private var adapter: CommunityBlacklistAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.communityId = arguments?.getInt(IArgContainer.ARG_GROUP_ID) ?: UNKNOWN_COMMUNITY_ID
        viewModel.initCommunityBlacklistLoader()
        blacklistPlaceholder?.gone()

        setAppropriateStatusBarHeight()
        setViewClickListeners()
        setBlacklistAdapter()
        setBlacklistRecyclerView()
        setLiveDataListeners()
        loadNextData()
    }

    private fun setBlacklistAdapter() {
        adapter = CommunityBlacklistAdapter()
        adapter?.onDataSetChanged = { onDataSetChanged(it) }
        adapter?.blacklistItemClickListener = { openUserProfileScreen(it?.memberId) }
        adapter?.clearBlacklistClickListener = { showClearBlacklistDialog() }
        adapter?.blacklistContextMenuClickListener = { showUnblockMemberBottomSheetDialog(it?.memberId) }
    }

    private fun onDataSetChanged(newAdapterItemListSize: Int) {
        if (newAdapterItemListSize <= RESERVED_LIST_SIZE) {
            showBlacklistPlaceHolderView()
        } else {
            showBlacklistView()
        }
    }

    private fun showClearBlacklistDialog() {
        ConfirmDialogBuilder()
            .setHeader(getString(R.string.settings_privacy_dialog_unblock_all_title))
            .setDescription(getString(R.string.settings_privacy_dialog_unblock_all_description))
            .setLeftBtnText(getString(R.string.settings_privacy_dialog_unblock_all_confirm))
            .setRightBtnText(getString(R.string.general_cancel))
            .setLeftClickListener { viewModel.clearBlacklist() }
            .setRightClickListener { /* Dismiss */ }
            .show(childFragmentManager)
    }

    private fun showUnblockMemberBottomSheetDialog(memberId: Long?) {
        val menu = MeeraMenuBottomSheet(act)
        menu.addItem(
            title = R.string.settings_remove_from_exclusions,
            icon = R.drawable.block_user_menu_item_v2,
            click = { removeMemberFromBlacklist(memberId) }
        )
        menu.show(childFragmentManager)
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
            add(UserInfoFragment(), Act.LIGHT_STATUSBAR, Arg(ARG_USER_ID, userId))
        } else {
            showAlertNToastAtScreenTop(R.string.community_blacklist_screen_error_cant_open_user_profile)
        }
    }

    private fun setBlacklistRecyclerView() {
        blacklistRecyclerView?.also { recyclerView ->
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
        backButtonView?.click {
            act?.onBackPressed()
        }
    }

    private fun setAppropriateStatusBarHeight() {
        initStatusBarViewHeight<LinearLayoutCompat.LayoutParams>(statusBarStubView)
    }

    private fun setLiveDataListeners() {
        viewModel.eventLiveData.observe(this, Observer { newEvent ->
            when (newEvent) {
                is BlacklistLoadingFailed -> {
                    showAlertNToastAtScreenTop(R.string.community_general_error)
                }
                is BlacklistLoadingStarted -> {
                    // do nothing
                }
                is BlacklistLoadingSuccess -> {
                    onBlacklistLoadingSuccess(newEvent.uiModel)
                }
                is UnblockingUserFailed -> {
                    showAlertNToastAtScreenTop(R.string.community_general_error)
                }
                is UnblockingUserStarted -> {
                    // do nothing
                }
                is UnblockingUserSuccess -> {
                    adapter?.removeItem(newEvent.userId)
                    updateData()
                }
                is UnblockingAllUserFailed -> {
                    showAlertNToastAtScreenTop(R.string.community_general_error)
                }
                is UnblockingAllUserSuccess -> {
                    adapter?.clearItemList()
                    showBlacklistPlaceHolderView()
                }
            }
        })
    }

    private fun updateData() {
        val blacklisted = adapter?.blacklist?.filterIsInstance<BlacklistedMemberUIModel>()
        if (blacklisted?.isEmpty() == true) {
            adapter?.clearItemList()
            showBlacklistPlaceHolderView()
        }
    }

    private fun onBlacklistLoadingSuccess(uiModel: List<BlacklistedMemberUIModel>) {
        if (viewModel.communityBlacklistTotalSize > 0) {
            adapter?.updateHeaderTextIfNeeded(viewModel.communityBlacklistTotalSize)

            uiModel
                .takeIf { it.isNotEmpty() }
                ?.also { newBlacklistedMemberList: List<BlacklistedMemberUIModel> ->
                    adapter?.addItemList(newBlacklistedMemberList)
                }

            showBlacklistView()
        } else {
            if (adapter?.blacklist?.size ?: 0 <= RESERVED_LIST_SIZE) { // header + clear button = 2
                showBlacklistPlaceHolderView()
            }
        }
    }

    private fun showBlacklistPlaceHolderView() {
        blacklistRecyclerView?.invisible()
        blacklistPlaceholder?.visible()
    }

    private fun showBlacklistView() {
        blacklistPlaceholder?.invisible()
        blacklistRecyclerView?.visible()
    }

    private fun <T : ViewGroup.MarginLayoutParams> initStatusBarViewHeight(statusBarViewHeight: View?) {
        if (statusBarViewHeight != null) {
            tryCatch {
                val params = statusBarViewHeight.layoutParams as? T
                if (params != null) {
                    params.height = context.getStatusBarHeight()
                    statusBarViewHeight.layoutParams = params
                }
            }
        }
    }

    private fun showAlertNToastAtScreenTop(@StringRes stringRes: Int) {
        NToast.with(view)
            .text(getString(stringRes))
            .typeAlert()
            .show()

    }
}

