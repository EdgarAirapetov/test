package com.meera.core.dialogs

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentManager
import com.meera.core.R
import com.meera.core.databinding.MeeraDialogVariantConfirmBinding
import com.meera.core.extensions.empty
import com.meera.core.extensions.gone
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.bottomsheetdialog.LabelIconUiState
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogBehDelegate
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogParams
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogState

private const val FRAGMENT_NAME = "MeeraConfirmVariantDialogFragment"

class MeeraConfirmVariantDialog : UiKitBottomSheetDialog<MeeraDialogVariantConfirmBinding>() {

    private var data = MeeraConfirmVariantDialogData()

    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraDialogVariantConfirmBinding
        get() = MeeraDialogVariantConfirmBinding::inflate

    override fun getBehaviorDelegate(): UiKitBottomSheetDialogBehDelegate {
        return UiKitBottomSheetDialogBehDelegate.Builder()
            .setBottomSheetState(UiKitBottomSheetDialogState.EXPANDED)
            .setDraggable(true)
            .setSkipCollapsed(true)
            .create(dialog)
    }

    override fun createDialogState(): UiKitBottomSheetDialogParams {
        val labelIconUiState = LabelIconUiState(
            labelIcon = R.drawable.ic_outlined_arrow_left_m
        )
        return UiKitBottomSheetDialogParams(
            labelText = context?.getString(data.headerRes),
            labelIconUiState = data.dialogBack?.let { labelIconUiState }
        )
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        data.dialogCancelled.invoke()
    }

    fun setData(data: MeeraConfirmVariantDialogData) {
        this.data = data
    }

    private fun initViews() {
        if (data.header.isNotEmpty()) error("Header type string in setHeader() not implemented. Use only string resources!")

        contentBinding?.apply {
            val description = getDescriptionText()
            if (description.isEmpty()) {
                tvDescription.gone()
            } else {
                tvDescription.text = description
            }

            vDialogConfirmFirst.cellCityText = false
            vDialogConfirmFirst.setTitleValue(getFirstCellText())
            vDialogConfirmFirst.setLeftIcon(data.firstCellIconRes)
            vDialogConfirmSecond.cellCityText = false
            vDialogConfirmSecond.setTitleValue(getSecondCellText())
            vDialogConfirmSecond.setLeftIcon(data.secondCellIconRes)
            vDialogConfirmThird.cellCityText = false
            vDialogConfirmThird.setTitleValue(getThirdCellText())
            vDialogConfirmThird.setLeftIcon(data.thirdCellIconRes)
            vDialogConfirmFirst.setRightElementContainerClickable(false)
            vDialogConfirmSecond.setRightElementContainerClickable(false)
            vDialogConfirmThird.setRightElementContainerClickable(false)
            vDialogConfirmFirst.setThrottledClickListener {
                vDialogConfirmFirst.setCellRightElementChecked(true)
                vDialogConfirmSecond.setCellRightElementChecked(false)
                vDialogConfirmThird.setCellRightElementChecked(false)
                setButtonClickState(MeeraConfirmVariantType.FIRST)
            }
            vDialogConfirmSecond.setThrottledClickListener {
                vDialogConfirmFirst.setCellRightElementChecked(false)
                vDialogConfirmSecond.setCellRightElementChecked(true)
                vDialogConfirmThird.setCellRightElementChecked(false)
                setButtonClickState(MeeraConfirmVariantType.SECOND)
            }
            vDialogConfirmThird.setThrottledClickListener {
                vDialogConfirmFirst.setCellRightElementChecked(false)
                vDialogConfirmSecond.setCellRightElementChecked(false)
                vDialogConfirmThird.setCellRightElementChecked(true)
                setButtonClickState(MeeraConfirmVariantType.THIRD)
            }
        }

        data.dialogBack?.let {
            rootBinding?.ibBottomSheetDialogAction?.setThrottledClickListener {
                it.invoke()
            }
        }

        data.selectOption?.let {
            selectVariant(it)
        }
    }

    private fun selectVariant(variant: MeeraConfirmVariantType) {
        when (variant) {
            MeeraConfirmVariantType.FIRST -> contentBinding?.vDialogConfirmFirst?.setCellRightElementChecked(true)
            MeeraConfirmVariantType.SECOND -> contentBinding?.vDialogConfirmSecond?.setCellRightElementChecked(true)
            MeeraConfirmVariantType.THIRD -> contentBinding?.vDialogConfirmThird?.setCellRightElementChecked(true)
        }
    }

    private fun getDescriptionText(): String {
        return when {
            data.descriptionRes != ResourcesCompat.ID_NULL -> getString(data.descriptionRes)
            data.description.isNotEmpty() -> data.description
            else -> String.empty()
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

    private fun setButtonClickState(value: MeeraConfirmVariantType) {
        data.variantCellClicked.invoke(value)
    }

}

class MeeraConfirmVariantDialogBuilder {
    private var data = MeeraConfirmVariantDialogData()
    private var isCancelable = true

    fun setCancelable(canCancel: Boolean): MeeraConfirmVariantDialogBuilder {
        isCancelable = canCancel
        return this
    }

    fun setHeader(header: String): MeeraConfirmVariantDialogBuilder {
        data = data.copy(header = header)
        return this
    }

    fun setHeader(@StringRes headerRes: Int): MeeraConfirmVariantDialogBuilder {
        data = data.copy(headerRes = headerRes)
        return this
    }

    fun setDescription(description: String): MeeraConfirmVariantDialogBuilder {
        data = data.copy(description = description)
        return this
    }

    fun setDescription(@StringRes descriptionRes: Int): MeeraConfirmVariantDialogBuilder {
        data = data.copy(descriptionRes = descriptionRes)
        return this
    }

    fun setFirstCellText(firstCellText: String): MeeraConfirmVariantDialogBuilder {
        data = data.copy(firstCellText = firstCellText)
        return this
    }

    fun setFirstCellText(@StringRes firstCellTextRes: Int): MeeraConfirmVariantDialogBuilder {
        data = data.copy(firstCellTextRes = firstCellTextRes)
        return this
    }

    fun setFirstCellIcon(@DrawableRes firstCellIconRes: Int): MeeraConfirmVariantDialogBuilder {
        data = data.copy(firstCellIconRes = firstCellIconRes)
        return this
    }

    fun setSecondCellText(firstCellText: String): MeeraConfirmVariantDialogBuilder {
        data = data.copy(firstCellText = firstCellText)
        return this
    }

    fun setSecondCellText(@StringRes secondCellTextRes: Int): MeeraConfirmVariantDialogBuilder {
        data = data.copy(secondCellTextRes = secondCellTextRes)
        return this
    }

    fun setSecondCellIcon(@DrawableRes secondCellIconRes: Int): MeeraConfirmVariantDialogBuilder {
        data = data.copy(secondCellIconRes = secondCellIconRes)
        return this
    }

    fun setThirdCellText(thirdCellText: String): MeeraConfirmVariantDialogBuilder {
        data = data.copy(thirdCellText = thirdCellText)
        return this
    }

    fun setThirdCellText(@StringRes thirdCellTextRes: Int): MeeraConfirmVariantDialogBuilder {
        data = data.copy(thirdCellTextRes = thirdCellTextRes)
        return this
    }

    fun setSelectOption(variant: MeeraConfirmVariantType): MeeraConfirmVariantDialogBuilder {
        data = data.copy(selectOption = variant)
        return this
    }

    fun setThirdCellIcon(@DrawableRes thirdCellIconRes: Int): MeeraConfirmVariantDialogBuilder {
        data = data.copy(thirdCellIconRes = thirdCellIconRes)
        return this
    }

    fun setVariantCellListener(variantCellClicked: (value: MeeraConfirmVariantType) -> Unit): MeeraConfirmVariantDialogBuilder {
        data = data.copy(variantCellClicked = variantCellClicked)
        return this
    }

    fun setBackBtnListener(backBtn: () -> Unit): MeeraConfirmVariantDialogBuilder {
        data = data.copy(dialogBack = backBtn)
        return this
    }

    fun show(fm: FragmentManager): MeeraConfirmVariantDialog {
        val dialog = MeeraConfirmVariantDialog()
        dialog.setData(data)
        dialog.isCancelable = this.isCancelable
        dialog.show(fm, FRAGMENT_NAME)
        return dialog
    }
}
