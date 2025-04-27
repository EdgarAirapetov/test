package com.numplates.nomera3.presentation.view.fragments.bottomfragment.openprofile

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
import com.numplates.nomera3.databinding.MeeraBottomSheetOpenProfileBinding

class MeeraOpenProfileBottomSheetFragment : UiKitBottomSheetDialog<MeeraBottomSheetOpenProfileBinding>() {

    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraBottomSheetOpenProfileBinding
        get() = MeeraBottomSheetOpenProfileBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    override fun createDialogState() = UiKitBottomSheetDialogParams(
        needShowToolbar = false,
        needShowGrabberView = true,
        dialogStyle = R.style.BottomSheetDialogTransparentTheme,
    )

    private fun initView() {
        initListeners()
        initBehavior()
    }

    private fun initListeners() {
        contentBinding?.buttonOpenProfile?.setThrottledClickListener {
            setButtonsClickResult(true)
            dismiss()
        }
        contentBinding?.buttonOpenProfileCancel?.setThrottledClickListener {
            setButtonsClickResult(false)
            dismiss()
        }
        contentBinding?.nvOpenProfile?.closeButtonClickListener = {
            setButtonsClickResult(false)
            dismiss()
        }
    }

    private fun setButtonsClickResult(result: Boolean) {
        setFragmentResult(
            requestKey = ARG_OPEN_PROFILE_REQUEST_KEY, result = bundleOf(ARG_OPEN_PROFILE to result)
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
        const val OPEN_PROFILE_BOTTOM_DIALOG_TAG = "openProfileBottomDialog"
        const val ARG_OPEN_PROFILE_REQUEST_KEY = "argOpenProfileRequestKey"
        const val ARG_OPEN_PROFILE = "argOpenProfile"


        @JvmStatic
        fun show(fragmentManager: FragmentManager): MeeraOpenProfileBottomSheetFragment {
            val instance = MeeraOpenProfileBottomSheetFragment()
            instance.show(fragmentManager, OPEN_PROFILE_BOTTOM_DIALOG_TAG)
            return instance
        }
    }
}
