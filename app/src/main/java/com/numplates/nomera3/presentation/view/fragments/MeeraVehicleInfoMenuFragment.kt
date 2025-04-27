package com.numplates.nomera3.presentation.view.fragments

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
import com.numplates.nomera3.databinding.MeeraFragmentVehicleInfoMenuBinding

class MeeraVehicleInfoMenuFragment : UiKitBottomSheetDialog<MeeraFragmentVehicleInfoMenuBinding>() {

    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraFragmentVehicleInfoMenuBinding
        get() = MeeraFragmentVehicleInfoMenuBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initBehavior()
        initViews()
    }

    override fun createDialogState() = UiKitBottomSheetDialogParams(
        needShowToolbar = true,
        needShowCloseButton = true,
        needShowGrabberView = true,
        labelText = getString(R.string.meera_garage_vehicle_menu_title),
        dialogStyle = R.style.BottomSheetDialogTransparentTheme
    )

    private fun initViews() {
        contentBinding?.cellVehicleInfoMenuEdit?.setThrottledClickListener {
            setFragmentResult(
                ARG_VEHICLE_INFO_MENU_REQUEST_KEY, bundleOf(ARG_VEHICLE_INFO_MENU to ARG_VEHICLE_INFO_MENU_ACTION_EDIT)
            )
            dismiss()
        }
        contentBinding?.cellVehicleInfoMenuDelete?.setThrottledClickListener {
            setFragmentResult(
                ARG_VEHICLE_INFO_MENU_REQUEST_KEY,
                bundleOf(ARG_VEHICLE_INFO_MENU to ARG_VEHICLE_INFO_MENU_ACTION_DELETE)
            )
            dismiss()
        }
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
        const val VEHICLE_INFO_MENU_BOTTOM_DIALOG_TAG = "vehicleInfoMenuBottomDialog"
        const val ARG_VEHICLE_INFO_MENU_REQUEST_KEY = "argVehicleInfoMenuRequestKey"
        const val ARG_VEHICLE_INFO_MENU = "argVehicleInfoMenu"
        const val ARG_VEHICLE_INFO_MENU_ACTION_DELETE = "vehicleDelete"
        const val ARG_VEHICLE_INFO_MENU_ACTION_EDIT = "vehicleEdit"

        @JvmStatic
        fun show(fragmentManager: FragmentManager): MeeraVehicleInfoMenuFragment {
            val instance = MeeraVehicleInfoMenuFragment()
            instance.show(fragmentManager, VEHICLE_INFO_MENU_BOTTOM_DIALOG_TAG)
            return instance
        }
    }
}
