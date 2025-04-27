package com.numplates.nomera3.presentation.view.adapter.newuserprofile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.meera.core.databinding.MeeraDialogConfirmUserDeleteGiftBinding
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogParams
import com.numplates.nomera3.R

class MeeraConfirmDialogUserDeleteGift: UiKitBottomSheetDialog<MeeraDialogConfirmUserDeleteGiftBinding>() {

    private var deleteAction: ()-> Unit = {}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentBinding?.vDeleteGift?.setThrottledClickListener{
            dismiss()
            deleteAction.invoke()
        }
    }

    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraDialogConfirmUserDeleteGiftBinding
        get() = MeeraDialogConfirmUserDeleteGiftBinding::inflate

    override fun createDialogState(): UiKitBottomSheetDialogParams =
        UiKitBottomSheetDialogParams(labelText = context?.getString(R.string.general_actions))

    fun setClickDeleteAction(deleteAction: ()-> Unit){
        this.deleteAction = deleteAction
    }
}
