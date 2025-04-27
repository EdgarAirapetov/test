package com.numplates.nomera3.modules.chat.requests.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.common.COLOR_STATUSBAR_LIGHT_NAVBAR
import com.meera.core.common.LIGHT_STATUSBAR
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.meera.core.utils.NSnackbar
import com.meera.db.models.dialog.DialogEntity
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentChatRequestsBinding
import com.numplates.nomera3.modules.chat.ChatFragmentNew
import com.numplates.nomera3.modules.chat.IOnDialogClickedNew
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitData
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitType
import com.numplates.nomera3.modules.chat.requests.ui.addDividerDecorator
import com.numplates.nomera3.modules.chat.requests.ui.viewevent.ChatRequestViewEvent
import com.numplates.nomera3.modules.chat.requests.ui.viewmodel.ChatRequestActionUiModel
import com.numplates.nomera3.modules.chat.requests.ui.viewmodel.ChatRequestViewModel
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.adapter.newchat.ChatRoomsPagedAdapterV2
import com.numplates.nomera3.presentation.view.fragments.UserInfoFragment
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraMenuBottomSheet
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.viewmodel.RoomsViewModel
import kotlinx.coroutines.launch

class ChatRequestFragment : BaseFragmentNew<FragmentChatRequestsBinding>(),
    IOnDialogClickedNew, ChatRequestCallback {

    private val chatRequestViewModel by viewModels<ChatRequestViewModel> { App.component.getViewModelFactory() }

    private val roomsViewModel by viewModels<RoomsViewModel> { App.component.getViewModelFactory() }

    private val actionSnackbarList = mutableListOf<NSnackbar>()

    private val pagedAdapter: ChatRoomsPagedAdapterV2 by lazy {
        ChatRoomsPagedAdapterV2(
            userId = chatRequestViewModel.getOwnUserId(),
            featureToggles = getFeatureToggles(),
            onDialogClicked = this
        )
    }

    private val roomsPagedObserver = Observer<PagedList<DialogEntity>> { pagedList ->
        val scrollToEdgeCallback = binding?.rvChatRequests?.createScrollToEdgeCallback()
        pagedAdapter.submitList(pagedList) {
            binding?.pbLoadingProgress?.gone()
            scrollToEdgeCallback?.invoke()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar()
        initRecycler()
        binding?.pbLoadingProgress?.visible()
        chatRequestViewModel.getDrafts()
        initLiveObservables()
        updateAllMessagesAsChatRequest()
    }

    override fun onDestroyView() {
        startPendingWork()
        super.onDestroyView()
    }

    override fun onReturnTransitionFragment() {
        updateAllMessagesAsChatRequest()
        chatRequestViewModel.getDrafts()
    }

    override fun onBlockUser(userId: Long, companionData: DialogEntity) {
        chatRequestViewModel.prepareBlockUserJob(companionData)
        requireActivity().onBackPressed()
    }

    override fun onBlockReportUser(userId: Long, companionData: DialogEntity,  complaintReasonId: Int) {
        chatRequestViewModel.prepareBlockReportUserAction(companionData, complaintReasonId)
        requireActivity().onBackPressed()
    }

    private fun startPendingWork() {
        actionSnackbarList.forEach { it.dismissNoCallbacks() }
        chatRequestViewModel.startAllPendingWork()
    }

    private fun updateAllMessagesAsChatRequest() {
        chatRequestViewModel.updateMessagesAsChatRequest(
            roomId = null,
            isShowBlur = true
        )
    }

    private fun initToolbar() {
        binding?.toolbar?.apply {
            setNavigationIcon(R.drawable.arrowback)
            setNavigationOnClickListener { requireActivity().onBackPressed() }
        }
    }

    private fun initRecycler() {
        binding?.rvChatRequests?.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = pagedAdapter
            addDividerDecorator()
        }
    }

    private fun initLiveObservables() {
        chatRequestViewModel.liveChatRequestList.observe(viewLifecycleOwner, roomsPagedObserver)
        lifecycleScope.launch {
            chatRequestViewModel.chatRequestEventsFlow
                .flowWithLifecycle(lifecycle)
                .collect(::handleViewEvents)
        }
    }

    /**
     * Callback that will automatically scroll up/down
     * when items are inserted at the top/bottom of the list
     * and current list position is exactly at the top/bottom
     */
    private fun RecyclerView.createScrollToEdgeCallback(): () -> Unit {
        val currentList = pagedAdapter.currentList
        val layoutManager = this.layoutManager as? LinearLayoutManager
        val isAtListBeginning = currentList?.isNotEmpty() == true
            && layoutManager?.findFirstCompletelyVisibleItemPosition() == 0
        val isAtListEnd = currentList?.isNotEmpty() == true
            && layoutManager?.findLastCompletelyVisibleItemPosition() == currentList.lastIndex
        when {
            isAtListBeginning -> return getScrollToTopCallback(
                previousListFirstItem = currentList?.firstOrNull(),
                previousListSize = currentList?.size ?: 0)
            isAtListEnd -> return getScrollToBottomCallback(
                previousListLastItem = currentList?.lastOrNull(),
                previousListSize = currentList?.size ?: 0)
            else -> return {}
        }
    }

    private fun getCurrentChatRequestListSize() = pagedAdapter.currentList?.size ?: 0

    /**
     * If an item was inserted at the head of the list
     * when the first current visible list position is the utmost top one
     * then scroll up
     */
    private fun RecyclerView.getScrollToTopCallback(
        previousListFirstItem: DialogEntity?, previousListSize: Int
    ): () -> Unit = {
        val listSizeIncreased = getCurrentChatRequestListSize() > previousListSize
        val currentListFirstItem = pagedAdapter.getItemAt(0)
        if (listSizeIncreased &&
            previousListFirstItem != currentListFirstItem
        ) {
            this.smoothScrollToPosition(0)
        }
    }

    /**
     * If an item was inserted at the tail of the list
     * when the last current visible list position is the utmost bottom one
     * then scroll down
     */
    private fun RecyclerView.getScrollToBottomCallback(
        previousListLastItem: DialogEntity?, previousListSize: Int
    ): () -> Unit = {
        val listSizeIncreased = getCurrentChatRequestListSize() > previousListSize
        val currentListLastIndex = pagedAdapter.currentList?.lastIndex
        val currentListLastItem = currentListLastIndex?.let { index ->
            pagedAdapter.getItemAt(index)
        }
        if (listSizeIncreased &&
            previousListLastItem != currentListLastItem &&
            currentListLastIndex != null
        ) {
            this.smoothScrollToPosition(currentListLastIndex)
        }
    }

    private fun handleViewEvents(event: ChatRequestViewEvent) {
        when (event) {
            is ChatRequestViewEvent.BlockUserResult ->
                showBlockUserResult(event.isSuccess)
            is ChatRequestViewEvent.BlockUserJobCreated ->
                showBlockUserPendingAction(event.workData)
            is ChatRequestViewEvent.BlockReportUserJobCreated ->
                showBlockAndReportPendingAction(event.workData)
            is ChatRequestViewEvent.BlockReportUserResult ->
                showBlockReportUserResult(event.isSuccess)
            is ChatRequestViewEvent.OnPagingInitialized -> {
                chatRequestViewModel.liveChatRequestList.removeObserver(roomsPagedObserver)
                chatRequestViewModel.liveChatRequestList.observe(viewLifecycleOwner, roomsPagedObserver)
            }
            else -> {}
        }
    }

    private fun showBlockReportUserResult(isSuccess: Boolean) {
        if (!isSuccess) {
            showFailedToBlockReportUserError()
        }
    }

    private fun showBlockUserResult(isSuccess: Boolean) {
        if (!isSuccess) {
            showFailedToBlockUserError()
        }
    }

    override val bindingInflater: (
        LayoutInflater,
        ViewGroup?,
        Boolean
    ) -> FragmentChatRequestsBinding
        get() = FragmentChatRequestsBinding::inflate


    override fun onRoomClicked(dialog: DialogEntity?) {
        onActivityInteraction?.onAddFragment(
            fragment = ChatFragmentNew(),
            isLightStatusBar = LIGHT_STATUSBAR,
            mapArgs = hashMapOf(
                IArgContainer.ARG_CHAT_INIT_DATA to ChatInitData(
                    initType = ChatInitType.FROM_LIST_ROOMS,
                    roomId = dialog?.roomId
                )
            )
        )
    }

    override fun onRoomLongClicked(dialog: DialogEntity?) {
        MeeraMenuBottomSheet(context).apply {
            addItem(R.string.road_delete, R.drawable.ic_delete_menu_red) {
                handleDeleteRoom(dialog)
            }
        }.show(childFragmentManager)
    }

    private fun handleDeleteRoom(dialog: DialogEntity?) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.rooms_delete_title))
            .setMessage(getString(R.string.rooms_delete_room))
            .setPositiveButton(R.string.yes) { dlg, _ ->
                dialog?.roomId?.let { roomId ->
                    roomsViewModel.removeRoom(roomId = roomId, isBoth = false)
                }
                dlg.dismiss()
            }
            .setNegativeButton(R.string.no) { d, _ -> d.cancel() }
            .show()
    }

    override fun onAvatarClicked(dialog: DialogEntity?) {
        onActivityInteraction?.onAddFragment(
            fragment = UserInfoFragment(),
            isLightStatusBar = COLOR_STATUSBAR_LIGHT_NAVBAR,
            mapArgs = hashMapOf(IArgContainer.ARG_USER_ID to dialog?.companion?.userId)
        )
    }

    private fun showErrorToast(@StringRes stringRes: Int) {
        NToast.with(view)
            .text(getString(stringRes))
            .typeError()
            .show()
    }

    private fun showFailedToBlockReportUserError() = showErrorToast(R.string.user_complain_error)

    private fun showFailedToBlockUserError() =
        showErrorToast(R.string.complaints_failed_to_block_user)

    private fun showBlockUserPendingAction(actionData: ChatRequestActionUiModel.BlockUserWorkData) {
        NSnackbar.with(requireActivity())
            .inView(view)
            .marginBottom(SNACKBAR_BOTTOM_MARGIN_DP)
            .text(getString(R.string.user_complain_user_blocked))
            .description(getString(R.string.touch_to_delete))
            .durationIndefinite()
            .button(getString(R.string.general_cancel))
            .dismissManualListener {
                chatRequestViewModel.cancelPendingAction(actionData)
            }
            .timer(TIME_TO_CANCEL_PENDING_ACTION_SEC) {
                chatRequestViewModel.executePendingAction(actionData)
            }
            .show()
            .also { actionSnackbarList.add(it) }
    }

    private fun showBlockAndReportPendingAction(actionData: ChatRequestActionUiModel.BlockReportUserWorkData) {
        NSnackbar.with(requireActivity())
            .inView(view)
            .marginBottom(SNACKBAR_BOTTOM_MARGIN_DP)
            .text(getString(R.string.complaints_user_blocked_report_send))
            .description(getString(R.string.touch_to_delete))
            .durationIndefinite()
            .button(getString(R.string.general_cancel))
            .dismissManualListener {
                chatRequestViewModel.cancelPendingAction(actionData)
            }
            .timer(TIME_TO_CANCEL_PENDING_ACTION_SEC) {
                chatRequestViewModel.executePendingAction(actionData)
            }
            .show()
            .also { actionSnackbarList.add(it) }
    }

    private fun getFeatureToggles(): FeatureTogglesContainer =
        (activity?.application as FeatureTogglesContainer)

    companion object {
        private const val TIME_TO_CANCEL_PENDING_ACTION_SEC = 6
        private const val SNACKBAR_BOTTOM_MARGIN_DP = 24
    }
}
