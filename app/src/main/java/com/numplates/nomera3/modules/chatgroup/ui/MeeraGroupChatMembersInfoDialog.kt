package com.numplates.nomera3.modules.chatgroup.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogBehDelegate
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogParams
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogState
import com.meera.uikit.widgets.cell.CellPosition
import com.meera.uikit.widgets.cell.UiKitCell
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraDialogGroupChatMembersInfoBinding

private const val MEERA_GROUP_CHAT_MEMBERS_INFO_DIALOG_TAG = "MeeraGroupChatMembersInfoDialog"

class MeeraGroupChatMembersInfoDialog: UiKitBottomSheetDialog<MeeraDialogGroupChatMembersInfoBinding>() {

    var dialogType: MeeraGroupChatMembersInfoDialogType = MeeraGroupChatMembersInfoDialogType.None
    var clickAction: (MeeraGroupChatMembersInfoDialogClickAction) -> Unit = {}

    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraDialogGroupChatMembersInfoBinding
        get() = MeeraDialogGroupChatMembersInfoBinding::inflate

    override fun getBehaviorDelegate(): UiKitBottomSheetDialogBehDelegate {
        return UiKitBottomSheetDialogBehDelegate.Builder()
            .setBottomSheetState(UiKitBottomSheetDialogState.EXPANDED)
            .setDraggable(true)
            .setSkipCollapsed(true)
            .create(dialog)
    }

    override fun createDialogState(): UiKitBottomSheetDialogParams =
        UiKitBottomSheetDialogParams(labelText = context?.getString(R.string.general_actions))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClicks()
        resolveDialogType(dialogType)
    }

    fun show(
        fm: FragmentManager,
        dialogType: MeeraGroupChatMembersInfoDialogType,
        clickAction: (MeeraGroupChatMembersInfoDialogClickAction) -> Unit
    ): MeeraGroupChatMembersInfoDialog{
        val dialog = MeeraGroupChatMembersInfoDialog()
        dialog.dialogType = dialogType
        dialog.clickAction = clickAction
        dialog.isCancelable = this.isCancelable
        dialog.show(fm, MEERA_GROUP_CHAT_MEMBERS_INFO_DIALOG_TAG)
        return dialog
    }

    private fun initClicks() {
        contentBinding?.apply {
            cellAddAdminGroupChat.setThrottledClickListener {
                clickAction.invoke(MeeraGroupChatMembersInfoDialogClickAction.AddAdmin)
                dismiss()
            }
            cellRemoveAdminGroupChat.setThrottledClickListener {
                clickAction.invoke(MeeraGroupChatMembersInfoDialogClickAction.RemoveAdmin)
                dismiss()
            }
            cellRemoveFromGroupChat.setThrottledClickListener {
                clickAction.invoke(MeeraGroupChatMembersInfoDialogClickAction.RemoveMember)
                dismiss()
            }
        }
    }

    private fun resolveDialogType(type: MeeraGroupChatMembersInfoDialogType) {
        when(type){
            is MeeraGroupChatMembersInfoDialogType.AdminScreenCreator -> showForCreatorAdminScreen()
            is MeeraGroupChatMembersInfoDialogType.MembersScreenTypeAdminRoleCreator ->
                showForCreatorCompanionScreen(type.isShowAddAdmin)
            is MeeraGroupChatMembersInfoDialogType.MembersScreenTypeMemberRoleCreator ->
                showForCreatorCompanionScreen(type.isShowAddAdmin)
            is MeeraGroupChatMembersInfoDialogType.MembersScreenTypeMemberRoleAdmin ->
                showForCreatorCompanionScreen(type.isShowAddAdmin)
            is MeeraGroupChatMembersInfoDialogType.None -> dismiss()
            else -> Unit
        }
    }

    private fun showForCreatorAdminScreen() {
        contentBinding?.apply {
            showTopCell(cellRemoveAdminGroupChat)
            showBottomCell(cellRemoveFromGroupChat)
        }
    }

    private fun showForCreatorCompanionScreen(isShowAddAdmin: Boolean) {
        contentBinding?.apply {
            if (isShowAddAdmin) {
                showMiddleCell(cellAddAdminGroupChat)
            } else {
                showMiddleCell(cellRemoveAdminGroupChat)
            }
            showBottomCell(cellRemoveFromGroupChat)
        }
    }

    private fun showTopCell(cell: UiKitCell) {
        cell.visible()
        cell.cellPosition = CellPosition.TOP
    }

    private fun showMiddleCell(cell: UiKitCell) {
        cell.visible()
        cell.cellPosition = CellPosition.MIDDLE
    }

    private fun showBottomCell(cell: UiKitCell) {
        cell.visible()
        cell.cellPosition = CellPosition.BOTTOM
    }

}

sealed interface MeeraGroupChatMembersInfoDialogType {
    data object AdminScreenCreator: MeeraGroupChatMembersInfoDialogType
    data object AdminScreenMember: MeeraGroupChatMembersInfoDialogType
    data object MembersScreenTypeCreatorRoleCreator: MeeraGroupChatMembersInfoDialogType
    class MembersScreenTypeCreatorRoleNotCreator(val isBlockedUser: Boolean): MeeraGroupChatMembersInfoDialogType
    class MembersScreenTypeAdminRoleCreator(val isBlockedUser: Boolean, val isShowAddAdmin: Boolean): MeeraGroupChatMembersInfoDialogType
    class MembersScreenTypeAdminRoleAdmin(val isBlockedUser: Boolean): MeeraGroupChatMembersInfoDialogType
    class MembersScreenTypeMemberRoleCreator(val isBlockedUser: Boolean, val isShowAddAdmin: Boolean): MeeraGroupChatMembersInfoDialogType
    class MembersScreenTypeMemberRoleAdmin(val isBlockedUser: Boolean, val isShowAddAdmin: Boolean): MeeraGroupChatMembersInfoDialogType
    data object None: MeeraGroupChatMembersInfoDialogType
}

sealed interface MeeraGroupChatMembersInfoDialogClickAction {
    data object OpenProfile: MeeraGroupChatMembersInfoDialogClickAction
    data object AddAdmin: MeeraGroupChatMembersInfoDialogClickAction
    data object RemoveAdmin: MeeraGroupChatMembersInfoDialogClickAction
    data object BlockMember: MeeraGroupChatMembersInfoDialogClickAction
    data object UnBlockMember: MeeraGroupChatMembersInfoDialogClickAction
    data object RemoveMember: MeeraGroupChatMembersInfoDialogClickAction
}
