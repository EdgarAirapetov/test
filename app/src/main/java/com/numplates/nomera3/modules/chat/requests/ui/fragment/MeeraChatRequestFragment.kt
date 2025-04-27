package com.numplates.nomera3.modules.chat.requests.ui.fragment

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.annotation.StringRes
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.PagedList
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.gone
import com.meera.core.extensions.safeNavigate
import com.meera.core.extensions.visible
import com.meera.db.models.dialog.DialogEntity
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.action.UiKitSnackBarActions
import com.meera.uikit.snackbar.state.ErrorSnakeState
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.roomcell.UiKitRoomCellConfig
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.meera.uikit.widgets.snackbar.SnackLoadingUiState
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentChatRequestBinding
import com.numplates.nomera3.modules.chat.MeeraChatFragment
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitData
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitType
import com.numplates.nomera3.modules.chat.requests.ui.addDividerDecorator
import com.numplates.nomera3.modules.chat.requests.ui.viewevent.ChatRequestViewEvent
import com.numplates.nomera3.modules.chat.requests.ui.viewmodel.ChatRequestActionUiModel
import com.numplates.nomera3.modules.chat.requests.ui.viewmodel.MeeraChatRequestViewModel
import com.numplates.nomera3.modules.chatrooms.ui.MeeraRoomsAdapter
import com.numplates.nomera3.modules.chatrooms.ui.RoomsBaseFragment
import com.numplates.nomera3.modules.chatrooms.ui.gestures.resetSwipedItems
import com.numplates.nomera3.modules.chatrooms.ui.gestures.setItemTouchHelper
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_CHAT_INIT_DATA
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_CHAT_TRANSIT_FROM
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

const val KEY_CHAT_REQUEST_BLOCK_REPORT_USER_RESULT = "KEY_CHAT_REQUEST_BLOCK_REPORT_USER_RESULT"
const val KEY_CHAT_REQUEST_BLOCK_USER_RESULT = "KEY_CHAT_REQUEST_BLOCK_USER_RESULT"
const val KEY_BUNDLE_CHAT_REQUEST_BLOCK_REPORT_USER_DATA = "KEY_BUNDLE_CHAT_REQUEST_BLOCK_REPORT_USER_DATA"
const val KEY_BUNDLE_CHAT_REQUEST_BLOCK_USER_DATA = "KEY_BUNDLE_CHAT_REQUEST_BLOCK_USER_DATA"
private const val BUTTONS_COUNT = 2
private const val SINGLE_ROOM = 1
private const val LIST_ANIMATION_TIME = 100L
private const val TIME_TO_CANCEL_PENDING_ACTION_SEC = 6L
private const val DELAY_INIT_OBSERVABLES = 1000L


class MeeraChatRequestFragment: RoomsBaseFragment(R.layout.meera_fragment_chat_request) {

    private val binding by viewBinding(MeeraFragmentChatRequestBinding::bind)

    private val chatRequestViewModel by viewModels<MeeraChatRequestViewModel> { App.component.getViewModelFactory() }

    private var infoSnackbar: UiKitSnackBar? = null

    private var pagedAdapter: MeeraRoomsAdapter? = null

    private val roomsPagedObserver = Observer<PagedList<UiKitRoomCellConfig>> { pagedList ->
        val scrollToEdgeCallback = binding.rvMeeraChatRequests.createScrollToEdgeCallback()
        pagedAdapter?.submitList(pagedList) {
            binding.rvMeeraChatRequests.itemAnimator = recyclerAnimator
            binding.pbMeeraChatRequestsLoadingProgress.gone()
            scrollToEdgeCallback.invoke()
        }
    }

    private val recyclerAnimator by lazy {
        DefaultItemAnimator().apply {
            changeDuration = LIST_ANIMATION_TIME
            addDuration = LIST_ANIMATION_TIME
            removeDuration = LIST_ANIMATION_TIME
            moveDuration = LIST_ANIMATION_TIME
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecycler()
        binding.pbMeeraChatRequestsLoadingProgress.visible()
        if (savedInstanceState == null) chatRequestViewModel.getDrafts()
        doDelayed(DELAY_INIT_OBSERVABLES) { initLiveObservables() }
        updateAllMessagesAsChatRequest()
    }

    override fun onPause() {
        super.onPause()
        infoSnackbar?.dismiss()
    }

    override fun setScrollState(recyclerView: RecyclerView?) {
        super.setScrollState(binding.rvMeeraChatRequests)
    }

    private fun initRecycler() {
        pagedAdapter = MeeraRoomsAdapter(object : MeeraRoomsAdapter.RoomCellListener {
            override fun onRoomClicked(item: UiKitRoomCellConfig) {
                findNavController().safeNavigate(
                    resId = R.id.action_mainChatFragment_to_meeraChatFragment,
                    bundle = Bundle().apply {
                        putParcelable(ARG_CHAT_INIT_DATA, ChatInitData(
                            initType = ChatInitType.FROM_LIST_ROOMS,
                            roomId = item.id
                        ))

                        val itemCount = pagedAdapter?.itemCount ?: 0
                        val transitFrom = when {
                            itemCount > SINGLE_ROOM -> MeeraChatFragment.TransitFrom.CHAT_REQUEST
                            itemCount == SINGLE_ROOM -> MeeraChatFragment.TransitFrom.CHAT_REQUEST_SINGLE_ROOM
                            else -> MeeraChatFragment.TransitFrom.OTHER
                        }
                        putSerializable(ARG_CHAT_TRANSIT_FROM, transitFrom)
                    }
                )
            }

            override fun onDeleteRoomClicked(item: UiKitRoomCellConfig) {
                handleDeleteRoom(item, onResetSwipedItems = { binding.rvMeeraChatRequests.resetSwipedItems() })
            }

            override fun onChangeMuteClicked(item: UiKitRoomCellConfig) {
                chatRequestViewModel.changeMuteState(item)
                binding.rvMeeraChatRequests.resetSwipedItems()
            }
        })


        binding.rvMeeraChatRequests.apply {
            setHasFixedSize(true)
            addDividerDecorator()
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            itemAnimator = recyclerAnimator
            adapter = pagedAdapter
        }

        binding.rvMeeraChatRequests.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) = Unit

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                onScrolled(recyclerView)
            }
        })

        setItemTouchHelper(
            recyclerView = requireNotNull(binding.rvMeeraChatRequests),
            buttonsLimit = BUTTONS_COUNT,
            adapter = requireNotNull(pagedAdapter),
            onSwipeComplete = { onItemSwipeCompleted() }
        )
    }

    private fun initLiveObservables() {
        lifecycleScope.launch {
            chatRequestViewModel.chatRequestEventsFlow
                .flowWithLifecycle(lifecycle)
                .collect(::handleViewEvents)
        }

        chatRequestViewModel.roomsPagingList
            .distinctUntilChanged()
            .observe(viewLifecycleOwner, roomsPagedObserver)
    }

    private fun updateAllMessagesAsChatRequest() {
        chatRequestViewModel.updateMessagesAsChatRequest(
            roomId = null,
            isShowBlur = true
        )
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
            else -> Unit
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

    private fun showFailedToBlockReportUserError() = showErrorToast(R.string.user_complain_error)

    private fun showFailedToBlockUserError() = showErrorToast(R.string.complaints_failed_to_block_user)

    private fun showBlockUserPendingAction(actionData: ChatRequestActionUiModel.BlockUserWorkData) {
        showTimerSnackbar(
            message = R.string.user_complain_user_blocked,
            onManualDismiss = {
                chatRequestViewModel.cancelPendingAction(actionData)
            },
            onTimerFinished = {
                chatRequestViewModel.executePendingAction(actionData)
            }
        )
    }

    private fun showBlockAndReportPendingAction(actionData: ChatRequestActionUiModel.BlockReportUserWorkData) {
        showTimerSnackbar(
            message = R.string.complaints_user_blocked_report_send,
            onManualDismiss = {
                chatRequestViewModel.cancelPendingAction(actionData)
            },
            onTimerFinished = {
                chatRequestViewModel.executePendingAction(actionData)
            }
        )
    }

    fun blockUser(data: ChatRequestBlockData?) {
        meeraRoomsFragmentInteraction?.isNeedLockReloadRooms(isLock = true)
        data?.let { chatRequestViewModel.prepareBlockUserJob(it.room) }
    }

    fun blockWithReportUser(data: ChatRequestBlockData?) {
        meeraRoomsFragmentInteraction?.isNeedLockReloadRooms(isLock = true)
        data?.let { chatRequestViewModel.prepareBlockReportUserAction(it.room, it.reasonId) }
    }

    /**
     * Callback that will automatically scroll up/down
     * when items are inserted at the top/bottom of the list
     * and current list position is exactly at the top/bottom
     */
    private fun RecyclerView.createScrollToEdgeCallback(): () -> Unit {
        val currentList = pagedAdapter?.currentList
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

    /**
     * If an item was inserted at the head of the list
     * when the first current visible list position is the utmost top one
     * then scroll up
     */
    private fun RecyclerView.getScrollToTopCallback(
        previousListFirstItem: UiKitRoomCellConfig?, previousListSize: Int
    ): () -> Unit = {
        val listSizeIncreased = getCurrentChatRequestListSize() > previousListSize
        val currentListFirstItem = pagedAdapter?.getItemAt(0)
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
        previousListLastItem: UiKitRoomCellConfig?, previousListSize: Int
    ): () -> Unit = {
        val listSizeIncreased = getCurrentChatRequestListSize() > previousListSize
        val currentListLastIndex = pagedAdapter?.currentList?.lastIndex
        val currentListLastItem = currentListLastIndex?.let { index ->
            pagedAdapter?.getItemAt(index)
        }
        if (listSizeIncreased &&
            previousListLastItem != currentListLastItem &&
            currentListLastIndex != null
        ) {
            this.smoothScrollToPosition(currentListLastIndex)
        }
    }

    private fun getCurrentChatRequestListSize() = pagedAdapter?.currentList?.size ?: 0

    private fun showErrorToast(@StringRes stringRes: Int) {
        infoSnackbar = UiKitSnackBar.make(
            view = requireView(),
            params = SnackBarParams(
                errorSnakeState = ErrorSnakeState(
                    messageText = getText(stringRes)
                )
            )
        )
        infoSnackbar?.show()
    }

    private fun showTimerSnackbar(
        @StringRes message: Int,
        onManualDismiss: () -> Unit,
        onTimerFinished: () -> Unit
    ) {
        if (infoSnackbar != null) return
        infoSnackbar = UiKitSnackBar.make(
            view = requireView(),
            params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = getText(message),
                    loadingUiState = SnackLoadingUiState.DonutProgress(
                        timerStartSec = TIME_TO_CANCEL_PENDING_ACTION_SEC,
                        onTimerFinished = {
                            meeraRoomsFragmentInteraction?.isNeedLockReloadRooms(isLock = false)
                            onTimerFinished.invoke()
                            infoSnackbar = null
                        }
                    ),
                    buttonActionText = getText(R.string.general_cancel),
                    buttonActionListener = {
                        meeraRoomsFragmentInteraction?.isNeedLockReloadRooms(isLock = false)
                        onManualDismiss.invoke()
                        infoSnackbar?.dismiss()
                        infoSnackbar = null
                    }
                ),
                duration = BaseTransientBottomBar.LENGTH_INDEFINITE
            )
        )
        infoSnackbar?.handleSnackBarActions(UiKitSnackBarActions.StartTimerIfNotRunning)
        infoSnackbar?.show()
    }

}

@Parcelize
data class ChatRequestBlockData(
    val userId: Long,
    val room: DialogEntity,
    val reasonId: Int
) : Parcelable
