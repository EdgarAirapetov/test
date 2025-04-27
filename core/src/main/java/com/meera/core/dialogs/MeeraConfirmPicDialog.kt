package com.meera.core.dialogs

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentManager
import com.meera.core.databinding.MeeraDialogPicConfirmBinding
import com.meera.core.extensions.empty
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogBehDelegate
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogParams
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogState
import com.meera.uikit.widgets.buttons.ButtonType

private const val FRAGMENT_NAME = "MeeraConfirmPicDialogFragment"

class MeeraConfirmPicDialog : UiKitBottomSheetDialog<MeeraDialogPicConfirmBinding>() {

    private var buttonClicked = false
    private var data = MeeraConfirmPicDialogData()

    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraDialogPicConfirmBinding
        get() = MeeraDialogPicConfirmBinding::inflate

    override fun getBehaviorDelegate(): UiKitBottomSheetDialogBehDelegate {
        return UiKitBottomSheetDialogBehDelegate.Builder()
            .setBottomSheetState(UiKitBottomSheetDialogState.EXPANDED)
            .setDraggable(true)
            .setSkipCollapsed(true)
            .create(dialog)
    }

    override fun createDialogState(): UiKitBottomSheetDialogParams =
        if (data.headerRes != ResourcesCompat.ID_NULL) {
            UiKitBottomSheetDialogParams(labelText = context?.getString(data.headerRes))
        } else {
            UiKitBottomSheetDialogParams(labelText = data.header)
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (buttonClicked.not()) {
            data.dialogCancelled.invoke()
        }
    }

    fun setData(data: MeeraConfirmPicDialogData) {
        this.data = data
    }

    private fun initViews() {
        contentBinding?.apply {
            tvDialogPicDescriptionFirstLine.text = getDescriptionFirstText()
            tvDialogPicDescriptionSecondLine.text = getDescriptionSecondText()
            btnTopDialogPic.text = getTopButtonText()
            data.topBtnType?.let { type -> btnTopDialogPic.buttonType = type }
            btnBottomDialogPic.text = getBottomButtonText()

            btnTopDialogPic.setThrottledClickListener {
                data.topBtnClicked.invoke()
                setButtonClickState()
            }
            btnBottomDialogPic.setThrottledClickListener {
                data.bottomBtnClicked.invoke()
                setButtonClickState()
            }
        }
        rootBinding?.ivBottomSheetDialogClose?.setThrottledClickListener {
            dismiss()
        }
    }

    private fun getDescriptionFirstText(): String {
        return when {
            data.descriptionFirstRes != ResourcesCompat.ID_NULL -> getString(data.descriptionFirstRes)
            data.descriptionFirst.isNotEmpty() -> data.descriptionFirst
            else -> String.empty()
        }
    }

    private fun getDescriptionSecondText(): String {
        return when {
            data.descriptionSecondRes != ResourcesCompat.ID_NULL -> getString(data.descriptionSecondRes)
            data.descriptionSecond.isNotEmpty() -> data.descriptionSecond
            else -> String.empty()
        }
    }

    private fun getTopButtonText(): String {
        return when {
            data.topBtnTextRes != ResourcesCompat.ID_NULL -> getString(data.topBtnTextRes)
            data.topBtnText.isNotEmpty() -> data.topBtnText
            else -> String.empty()
        }
    }

    private fun getBottomButtonText(): String {
        return when {
            data.bottomBtnTextRes != ResourcesCompat.ID_NULL -> getString(data.bottomBtnTextRes)
            data.bottomBtnText.isNotEmpty() -> data.bottomBtnText
            else -> String.empty()
        }
    }

    private fun setButtonClickState() {
        buttonClicked = true
        dismiss()
    }

}

class MeeraConfirmPicDialogBuilder {

    private var isCancelable = true
    private var data = MeeraConfirmPicDialogData()

    fun setCancelable(canCancel: Boolean): MeeraConfirmPicDialogBuilder {
        isCancelable = canCancel
        return this
    }

    fun setHeader(header: String): MeeraConfirmPicDialogBuilder {
        data = data.copy(header = header)
        return this
    }

    fun setHeader(@StringRes headerRes: Int): MeeraConfirmPicDialogBuilder {
        data = data.copy(headerRes = headerRes)
        return this
    }

    fun setDescriptionFirst(description: String): MeeraConfirmPicDialogBuilder {
        data = data.copy(descriptionFirst = description)
        return this
    }

    fun setDescriptionFirst(@StringRes descriptionRes: Int): MeeraConfirmPicDialogBuilder {
        data = data.copy(descriptionFirstRes = descriptionRes)
        return this
    }

    fun setDescriptionSecond(description: String): MeeraConfirmPicDialogBuilder {
        data = data.copy(descriptionSecond = description)
        return this
    }

    fun setDescriptionSecond(@StringRes descriptionRes: Int): MeeraConfirmPicDialogBuilder {
        data = data.copy(descriptionSecondRes = descriptionRes)
        return this
    }

    fun setImageRes(imageRes: Int): MeeraConfirmPicDialogBuilder {
        data = data.copy(image = imageRes)
        return this
    }

    fun setTopBtnText(topBtnText: String): MeeraConfirmPicDialogBuilder {
        data = data.copy(topBtnText = topBtnText)
        return this
    }

    fun setTopBtnText(@StringRes topBtnTextRes: Int): MeeraConfirmPicDialogBuilder {
        data = data.copy(topBtnTextRes = topBtnTextRes)
        return this
    }

    fun setTopBtnType(topBtnType: ButtonType): MeeraConfirmPicDialogBuilder {
        data = data.copy(topBtnType = topBtnType)
        return this
    }

    fun setBottomBtnText(bottomBtnText: String): MeeraConfirmPicDialogBuilder {
        data = data.copy(bottomBtnText = bottomBtnText)
        return this
    }

    fun setBottomBtnText(@StringRes bottomBtnTextRes: Int): MeeraConfirmPicDialogBuilder {
        data = data.copy(bottomBtnTextRes = bottomBtnTextRes)
        return this
    }

    fun setTopClickListener(topBtnClicked: () -> Unit): MeeraConfirmPicDialogBuilder {
        data = data.copy(topBtnClicked = topBtnClicked)
        return this
    }

    fun setBottomClickListener(bottomBtnClicked: () -> Unit): MeeraConfirmPicDialogBuilder {
        data = data.copy(bottomBtnClicked = bottomBtnClicked)
        return this
    }

    fun setDialogCancelledListener(dialogCancelled: () -> Unit): MeeraConfirmPicDialogBuilder {
        data = data.copy(dialogCancelled = dialogCancelled)
        return this
    }

    fun show(fm: FragmentManager): MeeraConfirmPicDialog {
        val dialog = MeeraConfirmPicDialog()
        dialog.setData(data)
        dialog.isCancelable = this.isCancelable
        dialog.show(fm, FRAGMENT_NAME)
        return dialog
    }
}
