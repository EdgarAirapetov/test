package com.numplates.nomera3.modules.newroads.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogParams
import com.noomeera.nmrmediatools.utils.setThrottledClickListener
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraBottomSheetMomentsMenuBinding


class MeeraMomentsMenuBottomDialog(
    val onHideUserMomentsClick: () -> Unit
): UiKitBottomSheetDialog<MeeraBottomSheetMomentsMenuBinding>() {

    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraBottomSheetMomentsMenuBinding
        get() = MeeraBottomSheetMomentsMenuBinding::inflate

    override fun createDialogState(): UiKitBottomSheetDialogParams =
        UiKitBottomSheetDialogParams(labelText = context?.getString(R.string.general_actions))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contentBinding?.cellHideUserMoments?.setThrottledClickListener {
            dismiss()
            onHideUserMomentsClick()
        }
    }
}
