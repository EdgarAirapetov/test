package com.numplates.nomera3.presentation.view.fragments.profilephoto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogParams
import com.meera.uikit.widgets.cell.CellPosition
import com.meera.uikit.widgets.gone
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraMenuBottomSheetDialogBinding

class MeeraMenuBottomSheetFragment(
    private val isProfilePhoto: Boolean,
    private val isOwnPhotoProfile: Boolean = true,
    private val currentItemPosition: Int, private val handleUIAction: (ProfilePhotoViewerAction) -> Unit
) : UiKitBottomSheetDialog<MeeraMenuBottomSheetDialogBinding>() {
    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraMenuBottomSheetDialogBinding
        get() = MeeraMenuBottomSheetDialogBinding::inflate

    override fun createDialogState(): UiKitBottomSheetDialogParams {
        return UiKitBottomSheetDialogParams(labelText = context?.getString(R.string.actions))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentBinding?.apply {
            vSaveBtn.setThrottledClickListener {
                handleUIAction.invoke(ProfilePhotoViewerAction.Save(currentItemPosition))
                dismiss()
            }

            vMakeAnAvatarBtn.setThrottledClickListener {
                handleUIAction.invoke(ProfilePhotoViewerAction.MakeAvatar(currentItemPosition))
                dismiss()
            }

            vDeleteBtn.setThrottledClickListener {
                handleUIAction.invoke(ProfilePhotoViewerAction.Remove(currentItemPosition))
                dismiss()
            }

            if (isOwnPhotoProfile.not()) {
                vSaveBtn.cellPosition = CellPosition.ALONE
                vSaveBtn.visible()
                vMakeAnAvatarBtn.gone()
                vDeleteBtn.gone()
            } else {
                if (isProfilePhoto && currentItemPosition == 0) {
                    vDeleteBtn.gone()
                    vMakeAnAvatarBtn.cellPosition = CellPosition.BOTTOM
                } else {
                    vDeleteBtn.visible()
                    vMakeAnAvatarBtn.cellPosition = CellPosition.MIDDLE
                    vDeleteBtn.cellPosition = CellPosition.BOTTOM
                }
            }
        }
    }
}
