package com.meera.core.dialogs

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentManager
import com.meera.core.R
import com.meera.core.databinding.MeeraDialogConfirmBinding
import com.meera.core.extensions.empty
import com.meera.core.extensions.gone
import com.meera.core.extensions.setBackgroundTint
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogBehDelegate
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogParams
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogState
import com.meera.uikit.widgets.buttons.ButtonType

class MeeraConfirmDialog(
    private var header: String = String.empty(),
    private var headerRes: Int = ResourcesCompat.ID_NULL,
    private var description: String = String.empty(),
    private var descriptionRes: Int = ResourcesCompat.ID_NULL,
    private var topBtnText: String = String.empty(),
    private var topBtnTextRes: Int = ResourcesCompat.ID_NULL,
    private var topBtnType: ButtonType? = null,
    private var bottomBtnType: ButtonType? = null,
    private var bottomBtnText: String = String.empty(),
    private var bottomBtnTextRes: Int = ResourcesCompat.ID_NULL,
    private var topBtnClicked: () -> Unit = {},
    private var bottomBtnClicked: () -> Unit = {},
    private var dialogCancelled: () -> Unit = {},
    @Deprecated(message = "Устарело, заменить на topBtnErrorMode")
    private var bgColor: Int = 0,
    private var hideBottomBtn: Boolean,
) : UiKitBottomSheetDialog<MeeraDialogConfirmBinding>() {

    private var buttonClicked = false

    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraDialogConfirmBinding
        get() = MeeraDialogConfirmBinding::inflate

    override fun getBehaviorDelegate(): UiKitBottomSheetDialogBehDelegate =
        UiKitBottomSheetDialogBehDelegate.Builder()
            .setBottomSheetState(UiKitBottomSheetDialogState.EXPANDED)
            .setDraggable(true)
            .setSkipCollapsed(true)
            .create(dialog)

    override fun createDialogState(): UiKitBottomSheetDialogParams =
        if (headerRes != ResourcesCompat.ID_NULL) {
            UiKitBottomSheetDialogParams(labelText = context?.getString(headerRes))
        } else {
            UiKitBottomSheetDialogParams(labelText = header)
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (buttonClicked.not()) {
            dialogCancelled.invoke()
        }
    }

    private fun initViews() {
        contentBinding?.apply {
            val description = getDescriptionText()
            if (description == String.empty()) tvDescription.gone() else tvDescription.text = description
            btnTop.text = getTopButtonText()

            topBtnType?.let { type -> btnTop.buttonType = type }
            btnTop.setThrottledClickListener {
                topBtnClicked.invoke()
                setButtonClickState()
            }

            when (bgColor) {
                R.color.uiKitColorAccentPrimary -> {
                    btnTop.setBackgroundTint(R.color.uiKitColorAccentPrimary)
                    ResourcesCompat.getColorStateList(
                        requireContext().resources,
                        R.color.uiKitColorForegroundPrimary,
                        null
                    )?.let { btnTop.updateContentColor(it) }
                }
            }

            if (hideBottomBtn) {
                btnBottom.gone()
            } else {
                bottomBtnType?.let { type -> btnBottom.buttonType = type }
                btnBottom.text = getBottomButtonText()
                btnBottom.setThrottledClickListener {
                    bottomBtnClicked.invoke()
                    setButtonClickState()
                }
            }
        }
    }

    private fun getDescriptionText(): String {
        return when {
            descriptionRes != ResourcesCompat.ID_NULL -> getString(descriptionRes)
            description.isNotEmpty() -> description
            else -> String.empty()
        }
    }

    private fun getTopButtonText(): String {
        return when {
            topBtnTextRes != ResourcesCompat.ID_NULL -> getString(topBtnTextRes)
            topBtnText.isNotEmpty() -> topBtnText
            else -> String.empty()
        }
    }

    private fun getBottomButtonText(): String {
        return when {
            bottomBtnTextRes != ResourcesCompat.ID_NULL -> getString(bottomBtnTextRes)
            bottomBtnText.isNotEmpty() -> bottomBtnText
            else -> String.empty()
        }
    }

    private fun setButtonClickState() {
        buttonClicked = true
        dismiss()
    }

}

class MeeraConfirmDialogBuilder {

    private var header = String.empty()
    private var headerRes = ResourcesCompat.ID_NULL
    private var description = String.empty()
    private var descriptionRes = ResourcesCompat.ID_NULL
    private var topBtnText = String.empty()
    private var topBtnTextRes = ResourcesCompat.ID_NULL
    private var topBtnType: ButtonType? = null
    private var bottomBtnType: ButtonType? = null
    private var bottomBtnText = String.empty()
    private var bottomBtnTextRes = ResourcesCompat.ID_NULL
    private var topBtnClicked: () -> Unit = {}
    private var bottomBtnClicked: () -> Unit = {}
    private var hideBottomBtn: Boolean = false
    private var dialogCancelled: () -> Unit = {}
    private var isCancelable = true
    private var bgColor: Int = 0

    fun setCancelable(canCancel: Boolean): MeeraConfirmDialogBuilder {
        isCancelable = canCancel
        return this
    }

    fun setHeader(header: String): MeeraConfirmDialogBuilder {
        this.header = header
        return this
    }

    fun setHeader(@StringRes headerRes: Int): MeeraConfirmDialogBuilder {
        this.headerRes = headerRes
        return this
    }

    fun setDescription(description: String): MeeraConfirmDialogBuilder {
        this.description = description
        return this
    }

    fun setDescription(@StringRes descriptionRes: Int): MeeraConfirmDialogBuilder {
        this.descriptionRes = descriptionRes
        return this
    }

    fun setTopBtnText(topBtnText: String): MeeraConfirmDialogBuilder {
        this.topBtnText = topBtnText
        return this
    }

    fun setTopBtnText(@StringRes topBtnTextRes: Int): MeeraConfirmDialogBuilder {
        this.topBtnTextRes = topBtnTextRes
        return this
    }

    fun setTopBtnType(topBtnType: ButtonType): MeeraConfirmDialogBuilder {
        this.topBtnType = topBtnType
        return this
    }

    fun setBottomBtnText(bottomBtnText: String): MeeraConfirmDialogBuilder {
        this.bottomBtnText = bottomBtnText
        return this
    }

    fun setBottomBtnText(@StringRes bottomBtnTextRes: Int): MeeraConfirmDialogBuilder {
        this.bottomBtnTextRes = bottomBtnTextRes
        return this
    }

    fun setBottomBtnType(bottomBtnType: ButtonType): MeeraConfirmDialogBuilder {
        this.bottomBtnType = bottomBtnType
        return this
    }

    fun setColorBg(@ColorRes bgColor: Int): MeeraConfirmDialogBuilder {
        this.bgColor = bgColor
        return this
    }

    fun setTopClickListener(topBtnClicked: () -> Unit): MeeraConfirmDialogBuilder {
        this.topBtnClicked = topBtnClicked
        return this
    }

    fun setBottomClickListener(bottomBtnClicked: () -> Unit): MeeraConfirmDialogBuilder {
        this.bottomBtnClicked = bottomBtnClicked
        return this
    }

    fun setDialogCancelledListener(dialogCancelled: () -> Unit): MeeraConfirmDialogBuilder {
        this.dialogCancelled = dialogCancelled
        return this
    }

    fun hideBottomBtn(): MeeraConfirmDialogBuilder {
        this.hideBottomBtn = true
        return this
    }

    fun show(fm: FragmentManager): MeeraConfirmDialog {
        val dialog = MeeraConfirmDialog(
            header = header,
            headerRes = headerRes,
            description = description,
            descriptionRes = descriptionRes,
            topBtnText = topBtnText,
            topBtnTextRes = topBtnTextRes,
            topBtnType = topBtnType,
            bottomBtnType = bottomBtnType,
            bottomBtnText = bottomBtnText,
            bottomBtnTextRes = bottomBtnTextRes,
            topBtnClicked = topBtnClicked,
            bottomBtnClicked = bottomBtnClicked,
            dialogCancelled = dialogCancelled,
            hideBottomBtn = hideBottomBtn,
            bgColor = bgColor
        )

        dialog.isCancelable = this.isCancelable
        dialog.show(fm, "MeeraConfirmDialogFragment")
        return dialog
    }

}
