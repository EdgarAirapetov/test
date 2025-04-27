package com.numplates.nomera3.presentation.view.utils.friendsdialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentManager
import com.meera.core.databinding.MeeraDialogUserFriendBinding
import com.meera.core.extensions.dp
import com.meera.core.extensions.empty
import com.meera.core.extensions.gone
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogBehDelegate
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogParams
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogState
import com.meera.uikit.widgets.cell.CellPosition

private const val FRAGMENT_NAME = "MeeraUserFriendActionMenuBottomSheet"
private const val MARGIN_START_DIVIDER = 16

class MeeraUserFriendActionMenuBottomSheet : UiKitBottomSheetDialog<MeeraDialogUserFriendBinding>() {
    private var data = MeeraFriendsDialogData()

    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraDialogUserFriendBinding
        get() = MeeraDialogUserFriendBinding::inflate

    override fun getBehaviorDelegate(): UiKitBottomSheetDialogBehDelegate {
        return UiKitBottomSheetDialogBehDelegate.Builder()
            .setBottomSheetState(UiKitBottomSheetDialogState.EXPANDED)
            .setDraggable(true)
            .setSkipCollapsed(true)
            .create(dialog)
    }

    override fun createDialogState(): UiKitBottomSheetDialogParams =
        UiKitBottomSheetDialogParams(labelText = context?.getString(data.headerRes))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        data.dialogCancelled.invoke()
    }

    fun setData(data: MeeraFriendsDialogData) {
        this.data = data
    }

    private fun initViews() {
        contentBinding?.apply {
            vDialogConfirmFirst.cellCityText = false
            vDialogConfirmFirst.setTitleValue(getFirstCellText())
            vDialogConfirmFirst.setLeftIcon(data.firstCellIconRes)
            vDialogConfirmSecond.cellCityText = false
            vDialogConfirmSecond.setTitleValue(getSecondCellText())
            vDialogConfirmSecond.setLeftIcon(data.secondCellIconRes)
            vDialogConfirmThird.cellCityText = false
            vDialogConfirmThird.setTitleValue(getThirdCellText())
            vDialogConfirmThird.setLeftIcon(data.thirdCellIconRes)
            vDialogConfirmFirst.setMarginStartDivider(MARGIN_START_DIVIDER.dp)
            vDialogConfirmSecond.setMarginStartDivider(MARGIN_START_DIVIDER.dp)
            vDialogConfirmThird.setMarginStartDivider(MARGIN_START_DIVIDER.dp)
            if (getThirdCellText().isEmpty()) {
                vDialogConfirmThird.gone()
                vDialogConfirmSecond.cellPosition = CellPosition.BOTTOM
            }

            vDialogConfirmFirst.setThrottledClickListener {
                setButtonClickState(MeeraUserFriendsDialogAction.FirstItemClick())
            }
            vDialogConfirmSecond.setThrottledClickListener {
                setButtonClickState(MeeraUserFriendsDialogAction.SecondItemClick())
            }
            vDialogConfirmThird.setThrottledClickListener {
                setButtonClickState(MeeraUserFriendsDialogAction.ThirdItemClick())
            }
        }
    }

    private fun getFirstCellText(): String {
        return when {
            data.firstCellTextRes != ResourcesCompat.ID_NULL -> getString(data.firstCellTextRes)
            data.firstCellText.isNotEmpty() -> data.firstCellText
            else -> String.empty()
        }
    }

    private fun getSecondCellText(): String {
        return when {
            data.secondCellTextRes != ResourcesCompat.ID_NULL -> getString(data.secondCellTextRes)
            data.secondCellText.isNotEmpty() -> data.secondCellText
            else -> String.empty()
        }
    }

    private fun getThirdCellText(): String {
        return when {
            data.thirdCellTextRes != ResourcesCompat.ID_NULL -> getString(data.thirdCellTextRes)
            data.thirdCellText.isNotEmpty() -> data.thirdCellText
            else -> String.empty()
        }
    }

    private fun setButtonClickState(action: MeeraUserFriendsDialogAction) {
        data.clickListener.invoke(action)
        dismiss()
    }

}

class MeeraUserFriendActionMenuBottomSheetBuilder {
    private var data = MeeraFriendsDialogData()
    private var isCancelable = true

    fun setCancelable(canCancel: Boolean): MeeraUserFriendActionMenuBottomSheetBuilder {
        isCancelable = canCancel
        return this
    }

    fun setHeader(header: String): MeeraUserFriendActionMenuBottomSheetBuilder {
        data = data.copy(header = header)
        return this
    }

    fun setHeader(@StringRes headerRes: Int): MeeraUserFriendActionMenuBottomSheetBuilder {
        data = data.copy(headerRes = headerRes)
        return this
    }

    fun setFirstCellText(firstCellText: String): MeeraUserFriendActionMenuBottomSheetBuilder {
        data = data.copy(firstCellText = firstCellText)
        return this
    }

    fun setFirstCellText(@StringRes firstCellTextRes: Int): MeeraUserFriendActionMenuBottomSheetBuilder {
        data = data.copy(firstCellTextRes = firstCellTextRes)
        return this
    }

    fun setFirstCellIcon(@DrawableRes firstCellIconRes: Int): MeeraUserFriendActionMenuBottomSheetBuilder {
        data = data.copy(firstCellIconRes = firstCellIconRes)
        return this
    }

    fun setSecondCellText(firstCellText: String): MeeraUserFriendActionMenuBottomSheetBuilder {
        data = data.copy(firstCellText = firstCellText)
        return this
    }

    fun setSecondCellText(@StringRes secondCellTextRes: Int): MeeraUserFriendActionMenuBottomSheetBuilder {
        data = data.copy(secondCellTextRes = secondCellTextRes)
        return this
    }

    fun setSecondCellIcon(@DrawableRes secondCellIconRes: Int): MeeraUserFriendActionMenuBottomSheetBuilder {
        data = data.copy(secondCellIconRes = secondCellIconRes)
        return this
    }

    fun setThirdCellText(thirdCellText: String): MeeraUserFriendActionMenuBottomSheetBuilder {
        data = data.copy(thirdCellText = thirdCellText)
        return this
    }

    fun setThirdCellText(@StringRes thirdCellTextRes: Int): MeeraUserFriendActionMenuBottomSheetBuilder {
        data = data.copy(thirdCellTextRes = thirdCellTextRes)
        return this
    }

    fun setThirdCellIcon(@DrawableRes thirdCellIconRes: Int): MeeraUserFriendActionMenuBottomSheetBuilder {
        data = data.copy(thirdCellIconRes = thirdCellIconRes)
        return this
    }

    fun setClickListener(clickListener: (action: MeeraUserFriendsDialogAction) -> Unit): MeeraUserFriendActionMenuBottomSheetBuilder {
        data = data.copy(clickListener = clickListener)
        return this
    }

    fun show(fm: FragmentManager): MeeraUserFriendActionMenuBottomSheet {
        val dialog = MeeraUserFriendActionMenuBottomSheet()
        dialog.setData(data)
        dialog.isCancelable = this.isCancelable
        dialog.show(fm, FRAGMENT_NAME)
        return dialog
    }
}
