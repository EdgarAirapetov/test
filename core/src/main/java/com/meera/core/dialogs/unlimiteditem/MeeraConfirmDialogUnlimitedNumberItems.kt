package com.meera.core.dialogs.unlimiteditem

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentManager
import com.meera.core.databinding.MeeraConfirmDialogUnlimitedListBinding
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogBehDelegate
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogParams
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogState

private const val FRAGMENT_NAME = "MeeraConfirmDialogUnlimitedNumberItems"

class MeeraConfirmDialogUnlimitedNumberItems : UiKitBottomSheetDialog<MeeraConfirmDialogUnlimitedListBinding>() {

    private var data: MeeraConfirmDialogUnlimitedNumberData? = null
    private var adapter: MeeraConfirmDialogUnlimitedNumberAdapter? = null
    private val closeListener: () -> Unit = {
        dismiss()
    }
    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraConfirmDialogUnlimitedListBinding
        get() = MeeraConfirmDialogUnlimitedListBinding::inflate

    override fun getBehaviorDelegate(): UiKitBottomSheetDialogBehDelegate {
        return UiKitBottomSheetDialogBehDelegate.Builder()
            .setBottomSheetState(UiKitBottomSheetDialogState.EXPANDED)
            .setDraggable(true)
            .setSkipCollapsed(true)
            .create(dialog)
    }

    override fun createDialogState(): UiKitBottomSheetDialogParams {
        return UiKitBottomSheetDialogParams(labelText = data?.header?.let { context?.getString(it) })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        data?.dismiss?.invoke()
    }

    fun setData(data: MeeraConfirmDialogUnlimitedNumberData) {
        this.data = data
    }

    private fun initViews() {
        data?.listener?.let {
            adapter = MeeraConfirmDialogUnlimitedNumberAdapter(
                listener = it,
                closeListener = closeListener,
                itemWithMargins = data?.itemsWithMargins == true
            )
        }
        contentBinding?.rvDialogConfirmList?.adapter = adapter
        adapter?.submitList(data?.items)
    }
}

class MeeraConfirmDialogUnlimitedListBuilder {
    private var data = MeeraConfirmDialogUnlimitedNumberData()
    private var isCancelable = true

    fun setCancelable(canCancel: Boolean): MeeraConfirmDialogUnlimitedListBuilder {
        isCancelable = canCancel
        return this
    }

    fun setHeader(@StringRes headerRes: Int): MeeraConfirmDialogUnlimitedListBuilder {
        data = data.copy(header = headerRes)
        return this
    }

    fun setListItems(list: List<MeeraConfirmDialogUnlimitedNumberItemsData>): MeeraConfirmDialogUnlimitedListBuilder {
        data = data.copy(items = list)
        return this
    }

    fun setItemsWithMargins(itemsWithMargins: Boolean): MeeraConfirmDialogUnlimitedListBuilder {
        data = data.copy(itemsWithMargins = itemsWithMargins)
        return this
    }

    fun setItemListener(listener: (action: MeeraConfirmDialogUnlimitedNumberItemsAction) -> Unit): MeeraConfirmDialogUnlimitedListBuilder {
        data = data.copy(listener = listener)
        return this
    }

    fun setDismissListener(dismiss: () -> Unit): MeeraConfirmDialogUnlimitedListBuilder{
        data = data.copy(dismiss = dismiss)
        return this
    }

    fun show(fm: FragmentManager): MeeraConfirmDialogUnlimitedNumberItems {
        val dialog = MeeraConfirmDialogUnlimitedNumberItems()
        dialog.setData(data)
        dialog.isCancelable = this.isCancelable
        dialog.show(fm, FRAGMENT_NAME)
        return dialog
    }
}

/**
 * Маркер для классов с экшенами для MeeraConfirmDialogUnlimitedNumberItems
 */
interface MeeraConfirmDialogUnlimitedNumberItemsAction
