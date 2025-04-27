package com.numplates.nomera3.modules.chatrooms.ui

import androidx.annotation.LayoutRes
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.meera.core.dialogs.MeeraConfirmDialogBuilder
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.action.UiKitSnackBarActions
import com.meera.uikit.snackbar.state.DismissListeners
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.roomcell.UiKitRoomCellConfig
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.meera.uikit.widgets.snackbar.SnackLoadingUiState
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseFragment

private const val DELAY_DELETE_NOTIFICATION_SEC = 5L

open class RoomsBaseFragment(@LayoutRes layout: Int = R.layout.empty_layout) : MeeraBaseFragment(layout) {

    interface MeeraRoomsFragmentInteraction {
        fun onSetTabLayoutElevation(isElevated: Boolean, isTop: Boolean)

        fun onScrollState(isTop: Boolean)

        fun onShowSwipeDownToSearchTooltip()

        fun isNeedLockReloadRooms(isLock: Boolean)
    }

    val viewModel by viewModels<MeeraRoomsViewModel> { App.component.getViewModelFactory() }

    var meeraRoomsFragmentInteraction: MeeraRoomsFragmentInteraction? = null

    private var currentDeletingRoomId: Long? = null
    private var activeSnackBar: UiKitSnackBar? = null

    override fun onStop() {
        super.onStop()
        activeSnackBar?.dismiss()
    }

    open fun setScrollState(recyclerView: RecyclerView?) {
        val isTop = (recyclerView?.layoutManager as? LinearLayoutManager)?.findFirstVisibleItemPosition() == 0
        meeraRoomsFragmentInteraction?.onScrollState(isTop = isTop)
    }

    fun setFragmentInteraction(callback: MeeraRoomsFragmentInteraction) {
        this.meeraRoomsFragmentInteraction = callback
    }

    fun onScrolled(recyclerView: RecyclerView) {
        val itemPos = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        if (!recyclerView.canScrollVertically(-1)) {
            meeraRoomsFragmentInteraction?.onSetTabLayoutElevation(
                isElevated = false,
                isTop = itemPos == 0
            )
        } else {
            meeraRoomsFragmentInteraction?.onSetTabLayoutElevation(
                isElevated = true,
                isTop = itemPos == 0
            )
        }
        for (i in (recyclerView.adapter?.itemCount ?: 0) downTo 0) {
            val itemView = recyclerView.findViewHolderForAdapterPosition(i)?.itemView
            if (itemView != null && itemView.scrollX > 0) {
                itemView.scrollTo(0, 0)
            }
        }
    }

    fun handleDeleteRoom(item: UiKitRoomCellConfig, onResetSwipedItems: () -> Unit) {
        MeeraConfirmDialogBuilder()
            .setHeader(R.string.rooms_delete_title)
            .setDescription(R.string.rooms_delete_room)
            .setTopBtnText(R.string.yes)
            .setBottomBtnText(R.string.no)
            .setCancelable(true)
            .setTopClickListener {
                viewModel.markRoomAsDeleted(item.id, isDeleted = true)
                showDeleteSnackbar(item)
                onResetSwipedItems.invoke()
            }
            .show(childFragmentManager)
    }

    fun onItemSwipeCompleted() {
        currentDeletingRoomId?.let { id -> dismissRoomDeleting(id) }
    }

    private fun showDeleteSnackbar(item: UiKitRoomCellConfig) {
        val roomId = item.id
        currentDeletingRoomId = roomId
        activeSnackBar = UiKitSnackBar.make(
            view = requireView(),
            params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = getText(R.string.meera_chat_deleted),
                    loadingUiState = SnackLoadingUiState.DonutProgress(
                        timerStartSec = DELAY_DELETE_NOTIFICATION_SEC,
                        onTimerFinished = {
                            completelyRemoveRoom(item)
                        }
                    ),
                    buttonActionText = getText(R.string.cancel),
                    buttonActionListener = {
                        dismissRoomDeleting(roomId)
                    }
                ),
                duration = BaseTransientBottomBar.LENGTH_INDEFINITE,
                dismissOnClick = true,
                dismissListeners = DismissListeners(
                    dismissListener = {
                        dismissRoomDeleting(roomId)
                    }
                )
            )
        )
        activeSnackBar?.handleSnackBarActions(UiKitSnackBarActions.StartTimerIfNotRunning)
        activeSnackBar?.show()
    }

    private fun dismissRoomDeleting(roomId: Long) {
        if (activeSnackBar == null) return
        activeSnackBar?.dismiss()
        activeSnackBar = null
        currentDeletingRoomId = null
        restoreRoom(roomId)
    }

    private fun completelyRemoveRoom(item: UiKitRoomCellConfig) {
        val roomId = item.id
        when {
            item.isGroup -> viewModel.removeRoomGroupDialog(roomId)
            else -> viewModel.removeRoom(roomId, isBoth = false)
        }
        currentDeletingRoomId = null
    }

    private fun restoreRoom(roomId: Long) {
        viewModel.markRoomAsDeleted(roomId, isDeleted = false)
    }

}
