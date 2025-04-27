package com.numplates.nomera3.modules.complains.ui.confirm

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import com.meera.core.extensions.click
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.DialogConfirmComplainBinding
import com.numplates.nomera3.modules.complains.ui.model.UserComplainUiModel

class ConfirmComplainDialog : AppCompatDialogFragment() {

    interface Listener {
        fun onDismissed()
        fun onConfirmed()
    }

    private var listener: Listener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = AlertDialog.Builder(context)
        val binding = DialogConfirmComplainBinding.inflate(LayoutInflater.from(context), null, false)
        dialog.setView(binding.root)
        val alertDialog = dialog.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setupDialogView(binding)
        return alertDialog
    }

    fun setListener(listener: Listener) {
        this.listener = listener
    }

    private fun setupDialogView(binding: DialogConfirmComplainBinding) {
        val complain = arguments?.get(KEY_EXTRA_USER_COMPLAIN) as UserComplainUiModel
        binding.tvTitle.text = getString(complain.dialogHeaderTitle)
        if (complain.titleRes != -1) {
            binding.tvSubtitle.text =
                getString(R.string.user_complain_reason_dialog_message, getString(complain.titleRes))
        }
        binding.tvCancel.click { dismiss() }
        binding.tvSend.click { listener?.onConfirmed() }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        listener?.onDismissed()
    }

    companion object {
        private const val KEY_EXTRA_USER_COMPLAIN = "key_extra_user_complain"
        private const val TAG_COMPLAIN_DIALOG = "tag_complain_dialog"

        fun showDialogInstance(
            fragmentManager: FragmentManager,
            complain: UserComplainUiModel?,
        ): ConfirmComplainDialog {
            return ConfirmComplainDialog().apply {
                arguments = bundleOf(KEY_EXTRA_USER_COMPLAIN to complain)
            }.also { dialog ->
                dialog.show(fragmentManager, TAG_COMPLAIN_DIALOG)
            }
        }
    }
}
