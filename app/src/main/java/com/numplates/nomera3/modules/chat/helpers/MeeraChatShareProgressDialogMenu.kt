package com.numplates.nomera3.modules.chat.helpers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.meera.core.R
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogBehDelegate
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogParams
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogState
import com.numplates.nomera3.databinding.MeeraChatShareProgressDialogBinding

private const val MEERA_CHAT_SHARE_DIALOG_MENU_TAG = "MeeraChatShareProgressDialogMenu"

class MeeraChatShareProgressDialogMenu : UiKitBottomSheetDialog<MeeraChatShareProgressDialogBinding>() {

    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraChatShareProgressDialogBinding
        get() = MeeraChatShareProgressDialogBinding::inflate

    override fun getBehaviorDelegate(): UiKitBottomSheetDialogBehDelegate {
        return UiKitBottomSheetDialogBehDelegate.Builder()
            .setBottomSheetState(UiKitBottomSheetDialogState.EXPANDED)
            .setDraggable(false)
            .setSkipCollapsed(true)
            .create(dialog)
    }

    override fun createDialogState(): UiKitBottomSheetDialogParams =
        UiKitBottomSheetDialogParams(
            needShowCloseButton = false,
            needShowToolbar = false
        )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentBinding?.cellMenuCloseShareUpload?.setThrottledClickListener { dismiss() }
    }

    fun show(manager: FragmentManager?) = showWithTag(manager, MEERA_CHAT_SHARE_DIALOG_MENU_TAG)

    fun showWithTag(manager: FragmentManager?, tag: String?) {
        val fragment = manager?.findFragmentByTag(tag)
        if (fragment != null) return
        manager?.let {
            super.show(manager, tag)
        }
    }

    fun setLoadingProgress(progress: Int) {
        contentBinding?.tvShareUploadProgress?.text =
            getString(R.string.general_progress_percent, progress)
    }

}
