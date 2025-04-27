package com.numplates.nomera3.modules.chat.helpers

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.setTint
import com.meera.core.extensions.textColor
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogBehDelegate
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogParams
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogState
import com.meera.uikit.widgets.cell.CellPosition
import com.meera.uikit.widgets.cell.UiKitCell
import com.noomeera.nmrmediatools.utils.setBackgroundTint
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraMediaPreviewBottomSheetDialogBinding
import com.numplates.nomera3.modules.moments.wrapper.MomentsWrapperActivity

private const val MEERA_CHAT_SHARE_DIALOG_MENU_TAG = "MeeraMediaPreviewBottomSheetDialog"

/**
 * https://www.figma.com/design/wyLhqHbHkvWWjLHznv6Wz8/Social-Chat-New?node-id=3979-59132&t=Q5FRz1uibuUuByxY-0
 */
class MeeraMediaPreviewBottomSheetDialog : UiKitBottomSheetDialog<MeeraMediaPreviewBottomSheetDialogBinding>() {

    private var dialogData = MeeraMediaPreviewBottomSheetDialogData()
    private var onClick: (action: MeeraMediaPreviewBottomSheetDialogClickAction) -> Unit = {}
    private var onDismiss: (dialog: DialogInterface) -> Unit = {}

    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraMediaPreviewBottomSheetDialogBinding
        get() = MeeraMediaPreviewBottomSheetDialogBinding::inflate

    override fun getBehaviorDelegate(): UiKitBottomSheetDialogBehDelegate {
        return UiKitBottomSheetDialogBehDelegate.Builder()
            .setBottomSheetState(UiKitBottomSheetDialogState.EXPANDED)
            .setDraggable(true)
            .setSkipCollapsed(true)
            .create(dialog)
    }

    override fun createDialogState(): UiKitBottomSheetDialogParams =
        UiKitBottomSheetDialogParams(labelText = context?.getString(R.string.general_actions))

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupClicks()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismiss.invoke(dialog)
    }

    fun show(
        fm: FragmentManager,
        data: MeeraMediaPreviewBottomSheetDialogData,
        onClick: (action: MeeraMediaPreviewBottomSheetDialogClickAction) -> Unit,
        onDismiss: (dialog: DialogInterface) -> Unit
    ): MeeraMediaPreviewBottomSheetDialog {
        val dialog = MeeraMediaPreviewBottomSheetDialog()
        dialog.dialogData = data
        dialog.onClick = onClick
        dialog.onDismiss = onDismiss
        dialog.isCancelable = this.isCancelable
        dialog.show(fm, MEERA_CHAT_SHARE_DIALOG_MENU_TAG)
        return dialog
    }

    private fun setupViews() {
        contentBinding?.apply {
            cellItemSend.isVisible = dialogData.isShowSend
            cellItemAddToFavorites.isVisible = dialogData.isShowAddToFavorites
            cellItemRemoveFromFavorites.isVisible = dialogData.isShowRemoveFromFavorites
            cellItemRemoveFromRecent.isVisible = dialogData.isShowRemoveFromRecent

            if (dialogData.isFromMoments) {
                dialog?.window?.navigationBarColor = ContextCompat.getColor(
                    requireContext(),
                    MomentsWrapperActivity.STATUS_BAR_COLOR
                )
                rootBinding?.tvBottomSheetDialogLabel?.textColor(R.color.ui_white)
                rootBinding?.ivBottomSheetDialogClose?.setTint(R.color.ui_white)
                rootBinding?.vgBottomSheetContainer?.setBackgroundTint(R.color.editor_widgets_content)
                rootBinding?.vgBottomSheetToolbar?.setBackgroundTint(R.color.editor_widgets_content)
                root.setBackgroundResource(R.color.editor_widgets_content)

                cellItemSend.apply {
                    setTitleValue(getString(R.string.editor_widget_add))
                    setLeftIcon(R.drawable.ic_moment_add_sticker)
                    cellLeftIconAndTitleColor = R.color.ui_white
                    cellBackgroundColor = R.color.uiKitColorForegroundPrimary
                    setDividerBackground(R.color.white_alpha_20)
                }

                cellItemAddToFavorites.apply {
                    setLeftIcon(R.drawable.ic_moment_add_to_favorite_sticker)
                    cellLeftIconAndTitleColor = R.color.ui_white
                    cellBackgroundColor = R.color.uiKitColorForegroundPrimary
                    setDividerBackground(R.color.white_alpha_20)
                }

                cellItemRemoveFromFavorites.apply {
                    setLeftIcon(R.drawable.ic_moment_remove_from_favorites_sticker)
                    cellLeftIconAndTitleColor = R.color.uiKitColorAccentWrong
                    cellBackgroundColor = R.color.uiKitColorForegroundPrimary
                    setDividerBackground(R.color.white_alpha_20)
                }

                cellItemRemoveFromRecent.apply {
                    setLeftIcon(R.drawable.ic_moment_remove_recent_sticker)
                    cellLeftIconAndTitleColor = R.color.uiKitColorAccentWrong
                    cellBackgroundColor = R.color.uiKitColorForegroundPrimary
                }
            }

            val visibleCells = getVisibleCells(root)
            visibleCells.forEachIndexed { index, view ->
                if (view is UiKitCell) {
                    val cellPosition = when {
                        visibleCells.size == 1 -> CellPosition.ALONE
                        index == 0 -> CellPosition.TOP
                        index == visibleCells.size - 1 -> CellPosition.BOTTOM
                        else -> CellPosition.MIDDLE
                    }
                    view.cellPosition = cellPosition
                }
            }
        }
    }

    private fun setupClicks() {
        contentBinding?.apply {
            cellItemSend.setThrottledClickListener {
                onClick(MeeraMediaPreviewBottomSheetDialogClickAction.OnClickSend)
                dismiss()
            }
            cellItemAddToFavorites.setThrottledClickListener {
                onClick(MeeraMediaPreviewBottomSheetDialogClickAction.OnClickAddToFavorites)
                dismiss()
            }
            cellItemRemoveFromFavorites.setThrottledClickListener {
                onClick(MeeraMediaPreviewBottomSheetDialogClickAction.OnClickRemoveFromFavorites)
                dismiss()
            }
            cellItemRemoveFromRecent.setThrottledClickListener {
                onClick(MeeraMediaPreviewBottomSheetDialogClickAction.OnClickRemoveFromRecent)
                dismiss()
            }
        }
    }

    private fun getVisibleCells(root: LinearLayout): List<View> {
        val list = mutableListOf<View>()
        root.children.iterator().forEach {
            if (it.isVisible) list.add(it)
        }
        return list
    }

}

data class MeeraMediaPreviewBottomSheetDialogData(
    val isFromMoments: Boolean = false,
    val isShowSend: Boolean = false,
    val isShowAddToFavorites: Boolean = false,
    val isShowRemoveFromFavorites: Boolean = false,
    val isShowRemoveFromRecent: Boolean = false,
)

sealed interface MeeraMediaPreviewBottomSheetDialogClickAction {
    data object OnClickSend : MeeraMediaPreviewBottomSheetDialogClickAction
    data object OnClickAddToFavorites : MeeraMediaPreviewBottomSheetDialogClickAction
    data object OnClickRemoveFromFavorites : MeeraMediaPreviewBottomSheetDialogClickAction
    data object OnClickRemoveFromRecent : MeeraMediaPreviewBottomSheetDialogClickAction
}
