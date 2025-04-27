package com.numplates.nomera3.modules.moments.show.presentation.dialog

import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import com.numplates.nomera3.databinding.LayoutMomentsConfirmDialogBinding

private const val CONFIRM_BUTTON_TEXT_ARG = "CONFIRM_BUTTON_TEXT_ARG"
private const val CANCEL_BUTTON_TEXT_ARG = "CANCEL_BUTTON_TEXT_ARG"
private const val DESCRIPTION_ARG = "DESCRIPTION_ARG"
private const val HEADER_ARG = "HEADER_ARG"

private const val MOMENTS_CONFIRM_DIALOG_TAG = "MomentsConfirmDialog"

class MomentsConfirmDialog : AppCompatDialogFragment() {

    private var confirmButtonText = ""
    private var cancelButtonText = ""
    private var description = ""
    private var header = ""
    private var isSizeConfigured = false
    private var momentConfirmCallback: MomentConfirmCallback? = null
    private var binding : LayoutMomentsConfirmDialogBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        confirmButtonText = arguments?.getString(CONFIRM_BUTTON_TEXT_ARG) ?: ""
        cancelButtonText = arguments?.getString(CANCEL_BUTTON_TEXT_ARG) ?: ""
        description = arguments?.getString(DESCRIPTION_ARG) ?: ""
        header = arguments?.getString(HEADER_ARG) ?: ""
        binding = LayoutMomentsConfirmDialogBinding.inflate(inflater)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onResume() {
        super.onResume()
        if (!isSizeConfigured) {
            val displayMetrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            (context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getMetrics(displayMetrics)
            val displayWidth = displayMetrics.widthPixels
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(dialog?.window?.attributes)
            val dialogWindowWidth = (displayWidth * 0.85f).toInt()
            layoutParams.width = dialogWindowWidth
            dialog?.window?.attributes = layoutParams
        }
        isSizeConfigured = true
    }

    override fun show(manager: FragmentManager, tag: String?) {
        if (manager.findFragmentByTag(tag) != null) return
        super.show(manager, tag)
    }

    fun setMomentConfirmCallback(momentConfirmCallback: MomentConfirmCallback?) {
        this.momentConfirmCallback = momentConfirmCallback
    }

    private fun configureView() {
        binding?.tvMomentsConfirmDialogHeader?.text = header
        binding?.tvMomentsConfirmDialogText?.text = description
        binding?.tvMomentsConfirmDialogConfirmButton?.text = confirmButtonText
        binding?.tvMomentsConfirmDialogCancelButton?.text = cancelButtonText
        binding?.tvMomentsConfirmDialogConfirmButton?.setOnClickListener {
            momentConfirmCallback?.onConfirmButtonClicked()
            dismiss()
        }
        binding?.tvMomentsConfirmDialogCancelButton?.setOnClickListener {
            momentConfirmCallback?.onCancelButtonClicked()
            dismiss()
        }
    }

    companion object {

        fun newInstance(
            confirmButtonText: String,
            cancelButtonText: String,
            description: String,
            header: String
        ): MomentsConfirmDialog {
            return MomentsConfirmDialog().apply {
                arguments = bundleOf(
                    CONFIRM_BUTTON_TEXT_ARG to confirmButtonText,
                    CANCEL_BUTTON_TEXT_ARG to cancelButtonText,
                    DESCRIPTION_ARG to description,
                    HEADER_ARG to header
                )
            }
        }
    }
}

class MomentsConfirmDialogBuilder {

    private var confirmButtonText = ""
    private var cancelButtonText = ""
    private var description = ""
    private var header = ""
    private var isCancelable = false
    private var momentConfirmCallback: MomentConfirmCallback? = null

    fun setCancelable(canCancel: Boolean): MomentsConfirmDialogBuilder {
        isCancelable = canCancel
        return this
    }

    fun setHeader(header: String): MomentsConfirmDialogBuilder {
        this.header = header
        return this
    }

    fun setDescription(description: String): MomentsConfirmDialogBuilder {
        this.description = description
        return this
    }

    fun setConfirmButtonText(confirmButtonText: String): MomentsConfirmDialogBuilder {
        this.confirmButtonText = confirmButtonText
        return this
    }

    fun setCancelButtonText(cancelButtonText: String): MomentsConfirmDialogBuilder {
        this.cancelButtonText = cancelButtonText
        return this
    }

    fun setMomentConfirmCallback(momentConfirmCallback: MomentConfirmCallback?): MomentsConfirmDialogBuilder {
        this.momentConfirmCallback = momentConfirmCallback
        return this
    }

    fun show(fragmentManager: FragmentManager): MomentsConfirmDialog {
        val dialog = MomentsConfirmDialog.newInstance(
            confirmButtonText = confirmButtonText,
            cancelButtonText = cancelButtonText,
            description = description,
            header = header
        )
        dialog.setMomentConfirmCallback(momentConfirmCallback)
        dialog.isCancelable = this.isCancelable
        dialog.show(fragmentManager, MOMENTS_CONFIRM_DIALOG_TAG)
        return dialog
    }
}
