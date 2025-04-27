package com.numplates.nomera3.presentation.view.fragments.bottomfragment.changeprofilesettings

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.simpleName
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.BottomSheetChangeProfileSettingsBinding
import com.numplates.nomera3.presentation.router.BaseBottomSheetDialogFragment


class ChangeProfileSettingsBottomSheetFragment :
    BaseBottomSheetDialogFragment<BottomSheetChangeProfileSettingsBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> BottomSheetChangeProfileSettingsBinding
        get() = BottomSheetChangeProfileSettingsBinding::inflate

    var confirmed = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val onCreateDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        return onCreateDialog.apply {
            behavior.peekHeight = resources.displayMetrics?.heightPixels ?: 0
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogTransparentTheme

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.let { existBinding ->
            existBinding.tvBtnChangeProfile.setThrottledClickListener {
                confirmed = true
                dismiss()
            }

            existBinding.tvBtnCancel.setThrottledClickListener {
                dismiss()
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (confirmed) (parentFragment as? ChangeProfileSettingsListener)?.changeProfileSettingConfirmed()
        else (parentFragment as? ChangeProfileSettingsListener)?.changeProfileSettingCanceled()
    }


    fun show(manager: FragmentManager?) {
        val fragment = manager?.findFragmentByTag(simpleName)
        if (fragment != null) return
        manager?.let {
            super.show(manager, simpleName)
        }
    }

    companion object {
        fun getInstance() = ChangeProfileSettingsBottomSheetFragment()
    }
}
