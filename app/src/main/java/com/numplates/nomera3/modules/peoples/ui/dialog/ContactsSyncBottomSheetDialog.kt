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
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.DialogAllowContactsSyncBinding
import com.numplates.nomera3.modules.peoples.ui.dialog.model.AllowContactsSyncUiState
import com.numplates.nomera3.presentation.router.BaseBottomSheetDialogFragment

class ContactsSyncBottomSheetDialog : BaseBottomSheetDialogFragment<DialogAllowContactsSyncBinding>() {

    private var bottomSheetBehavior: BottomSheetBehavior<*>? = null
    private var backArguments: Bundle? = null

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> DialogAllowContactsSyncBinding
        get() = DialogAllowContactsSyncBinding::inflate

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

    override fun getTheme(): Int = R.style.BottomSheetDialogTransparentTheme

    private fun initView() {
        initListeners()
        initBehavior()
    }

    private fun initListeners() {
        binding?.btnAllowContactsPositive?.setThrottledClickListener {
            setButtonsClickResult(POSITIVE_BUTTON_KEY_RESULT)
            dismiss()
        }
        binding?.tvAllowContactsNegative?.setThrottledClickListener {
            setButtonsClickResult(NEGATIVE_BUTTON_KEY_RESULT)
            dismiss()
        }
        binding?.ivAllowContactsClose?.setThrottledClickListener {
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
            bottomSheetBehavior = BottomSheetBehavior.from(frameLayout)
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
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
        binding?.tvAllowContactsLabel?.setTextOrHide(model.labelRes)
        binding?.tvAllowContactsMessage?.setTextOrHide(model.messageRes)
        binding?.btnAllowContactsPositive?.setTextOrHide(model.positiveButtonText)
        binding?.tvAllowContactsNegative?.setTextOrHide(model.negativeButtonText)
        setIconState(model.iconRes)
    }

    private fun updateButtonsPadding(model: AllowContactsSyncUiState) {
        if (model.negativeButtonText == null) {
            val margin = requireContext().resources.getDimension(R.dimen.margin_horizontal_content_general)
                .toInt()
            binding?.btnAllowContactsPositive?.setMargins(bottom = margin)
        }
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
        val drawable = requireContext().getDrawableCompat(iconRes) ?: return
        val width = drawable.intrinsicWidth
        val height = drawable.intrinsicHeight
        Glide.with(requireContext())
            .load(requireContext().getDrawableCompat(iconRes))
            .transition(DrawableTransitionOptions.withCrossFade())
            .override(width, height)
            .into(binding?.ivAllowContactsIcon ?: return)
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

        fun createAndShow(manager: FragmentManager): ContactsSyncBottomSheetDialog {
            val instance = ContactsSyncBottomSheetDialog()
            instance.arguments = bundleOf(DIALOG_ARGUMENT_KEY to uiState)
            instance.show(manager, ContactsSyncBottomSheetDialog.simpleName)
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
