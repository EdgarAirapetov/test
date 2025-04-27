package com.numplates.nomera3.modules.peoples.ui.dialog

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.meera.core.extensions.empty
import com.meera.core.extensions.getDrawableCompat
import com.meera.core.extensions.gone
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.simpleName
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogParams
import com.meera.uikit.widgets.buttons.UiKitButton
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraDialogAllowContactsSyncBinding
import com.numplates.nomera3.modules.peoples.ui.dialog.model.AllowContactsSyncUiState

class MeeraContactsSyncBottomSheetDialog : UiKitBottomSheetDialog<MeeraDialogAllowContactsSyncBinding>() {

    private var backArguments: Bundle? = null

    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraDialogAllowContactsSyncBinding
        get() = MeeraDialogAllowContactsSyncBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initState()
    }

    override fun onDismiss(dialog: DialogInterface) {
        if (backArguments == null) {
            setButtonsClickResult(CLOSE_BUTTON_KEY_RESULT)
        }
        super.onDismiss(dialog)
    }

    override fun createDialogState() = UiKitBottomSheetDialogParams(
        needShowToolbar = false,
        needShowGrabberView = true,
        dialogStyle = R.style.BottomSheetDialogTransparentTheme
    )

    private fun initView() {
        initListeners()
        initBehavior()
    }

    private fun initListeners() {
        contentBinding?.btnAllowContactsPositive?.setThrottledClickListener {
            setButtonsClickResult(POSITIVE_BUTTON_KEY_RESULT)
            dismiss()
        }
        contentBinding?.btnAllowContactsNegative?.setThrottledClickListener {
            setButtonsClickResult(NEGATIVE_BUTTON_KEY_RESULT)
            dismiss()
        }
        contentBinding?.nvContactsSync?.closeButtonClickListener = {
            dismiss()
        }
    }

    private fun setButtonsClickResult(key: String) {
        backArguments = bundleOf(BUTTON_CLICK_RESULT_KEY to key)
        setFragmentResult(
            requestKey = DISMISS_REQUEST_CODE,
            result = backArguments ?: bundleOf()
        )
    }

    private fun initBehavior() {
        val dialog = dialog as BottomSheetDialog
        val mainContainer = dialog.findViewById<FrameLayout>(R.id.design_bottom_sheet)
        mainContainer?.let { frameLayout ->
            val bottomSheetBehavior = BottomSheetBehavior.from(frameLayout)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun initState() {
        if (arguments == null) return
        val argumentModel = arguments?.getParcelable(DIALOG_ARGUMENT_KEY) as? AllowContactsSyncUiState
        argumentModel?.let { model ->
            setViewStateByArgument(model)
            updateButtonsPadding(model)
        }
    }

    private fun setViewStateByArgument(model: AllowContactsSyncUiState) {
        contentBinding?.nvContactsSync?.title = if (model.labelRes != null) getString(model.labelRes) else String.empty()
        contentBinding?.btnAllowContactsPositive?.setTextOrHide(model.positiveButtonText)
        contentBinding?.btnAllowContactsNegative?.setTextOrHide(model.negativeButtonText)
        contentBinding?.tvAllowContactsMessage?.setTextOrHide(model.messageRes)
        setIconState(model.iconRes)
    }

    private fun updateButtonsPadding(model: AllowContactsSyncUiState) {
        if (model.negativeButtonText == null) {
            val margin = requireContext().resources.getDimension(R.dimen.margin_horizontal_content_general)
                .toInt()
            contentBinding?.btnAllowContactsPositive?.setMargins(bottom = margin, end = margin, start = margin, top = margin)
        }
    }

    private fun UiKitButton.setTextOrHide(@StringRes textRes: Int?) {
        if (textRes == null) {
            this.gone()
            return
        }
        this.text = context.getString(textRes)
    }

    private fun TextView.setTextOrHide(@StringRes textRes: Int?) {
        if (textRes == null) {
            this.gone()
            return
        }
        this.text = context.getString(textRes)
    }

    private fun setIconState(@DrawableRes iconRes: Int?) {
        if (iconRes == null) return
        Glide.with(requireContext())
            .load(requireContext().getDrawableCompat(iconRes))
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(contentBinding?.ivAllowContactsIcon ?: return)
    }

    class Builder {
        private var uiState = AllowContactsSyncUiState()

        private var positiveButtonActionListener: (() -> Unit)? = null
        private var negativeButtonActionListener: (() -> Unit)? = null
        private var dismissListener: (() -> Unit)? = null

        fun setLabel(@StringRes labelRes: Int): Builder {
            uiState = uiState.copy(
                labelRes = labelRes
            )
            return this
        }

        fun setDescription(@StringRes descriptionRes: Int): Builder {
            uiState = uiState.copy(
                messageRes = descriptionRes
            )
            return this
        }

        fun setIcon(@DrawableRes iconRes: Int): Builder {
            uiState = uiState.copy(
                iconRes = iconRes
            )
            return this
        }

        fun setPositiveButton(
            @StringRes positiveButtonTextRes: Int,
            clickListener: (() -> Unit)? = null
        ): Builder {
            uiState = uiState.copy(
                positiveButtonText = positiveButtonTextRes
            )
            this.positiveButtonActionListener = clickListener
            return this
        }

        fun setNegativeButton(
            @StringRes negativeButtonTextRes: Int,
            clickListener: (() -> Unit)? = null
        ): Builder {
            uiState = uiState.copy(
                negativeButtonText = negativeButtonTextRes
            )
            this.negativeButtonActionListener = clickListener
            return this
        }

        /**
         * Вызывается, когда юзер закрыл (крестиком, тапом на свободную область)
         */
        fun setOnCloseDialogDismissListener(action: () -> Unit): Builder {
            this.dismissListener = action
            return this
        }

        fun createAndShow(manager: FragmentManager): MeeraContactsSyncBottomSheetDialog {
            val instance = MeeraContactsSyncBottomSheetDialog()
            instance.arguments = bundleOf(DIALOG_ARGUMENT_KEY to uiState)
            instance.show(manager, MeeraContactsSyncBottomSheetDialog.simpleName)
            instance.setFragmentResultListener(DISMISS_REQUEST_CODE) { _, bundle ->
                val key = bundle.getString(BUTTON_CLICK_RESULT_KEY, String.empty())
                handleButtonsClickActionByKey(key)
            }
            return instance
        }

        private fun handleButtonsClickActionByKey(key: String) {
            when (key) {
                NEGATIVE_BUTTON_KEY_RESULT -> negativeButtonActionListener?.invoke()
                POSITIVE_BUTTON_KEY_RESULT -> positiveButtonActionListener?.invoke()
                else -> dismissListener?.invoke()
            }
        }
    }

    companion object {
        private const val DIALOG_ARGUMENT_KEY = "dialogArgument"
        private const val DISMISS_REQUEST_CODE = "dismissCode"

        private const val POSITIVE_BUTTON_KEY_RESULT = "positiveButtonResult"
        private const val NEGATIVE_BUTTON_KEY_RESULT = "negativeButtonResult"
        private const val CLOSE_BUTTON_KEY_RESULT = "closeButtonKeyResult"

        private const val BUTTON_CLICK_RESULT_KEY = "buttonClickResult"
    }
}
