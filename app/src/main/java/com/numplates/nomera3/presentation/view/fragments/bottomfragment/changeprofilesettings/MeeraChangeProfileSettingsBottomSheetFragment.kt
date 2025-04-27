package com.numplates.nomera3.presentation.view.fragments.bottomfragment.changeprofilesettings

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogParams
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraBottomSheetChangeProfileSettingsBinding

class MeeraChangeProfileSettingsBottomSheetFragment :
    UiKitBottomSheetDialog<MeeraBottomSheetChangeProfileSettingsBinding>() {

    private var isConfirmed = false

    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraBottomSheetChangeProfileSettingsBinding
        get() = MeeraBottomSheetChangeProfileSettingsBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        setButtonsClickResult()
    }

    override fun createDialogState() = UiKitBottomSheetDialogParams(
        needShowToolbar = false, needShowGrabberView = true, dialogStyle = R.style.BottomSheetDialogTransparentTheme
    )

    private fun initView() {
        initListeners()
        initBehavior()
    }

    private fun initListeners() {
        contentBinding?.buttonChangeProfile?.setThrottledClickListener {
            isConfirmed = true
            dismiss()
        }
        contentBinding?.buttonChangeProfileCancel?.setThrottledClickListener {
            isConfirmed = false
            dismiss()
        }
        contentBinding?.nvChangeProfile?.closeButtonClickListener = {
            isConfirmed = false
            dismiss()
        }
    }

    private fun setButtonsClickResult() {
        setFragmentResult(
            requestKey = ARG_CHANGE_PROFILE_REQUEST_KEY, result = bundleOf(ARG_CHANGE_PROFILE to isConfirmed)
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


    companion object {
        const val CHANGE_PROFILE_BOTTOM_DIALOG_TAG = "changeProfileBottomDialog"
        const val ARG_CHANGE_PROFILE_REQUEST_KEY = "argChangeProfileRequestKey"
        const val ARG_CHANGE_PROFILE = "argChangeProfile"


        @JvmStatic
        fun show(fragmentManager: FragmentManager): MeeraChangeProfileSettingsBottomSheetFragment {
            val instance = MeeraChangeProfileSettingsBottomSheetFragment()
            instance.show(fragmentManager, CHANGE_PROFILE_BOTTOM_DIALOG_TAG)
            return instance
        }
    }
}
