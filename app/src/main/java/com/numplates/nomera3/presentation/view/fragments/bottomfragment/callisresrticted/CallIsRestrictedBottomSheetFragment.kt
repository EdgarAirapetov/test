package com.numplates.nomera3.presentation.view.fragments.bottomfragment.callisresrticted

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.simpleName
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.BottomSheetCallsIsRestrictedBinding
import com.numplates.nomera3.presentation.router.BaseBottomSheetDialogFragment

class CallIsRestrictedBottomSheetFragment : BaseBottomSheetDialogFragment<BottomSheetCallsIsRestrictedBinding>() {

    val userName: String by lazy(LazyThreadSafetyMode.NONE) {
        requireArguments().getString(ARG_USER_NAME) ?: ""
    }

    val canChat: Boolean by lazy(LazyThreadSafetyMode.NONE) {
        requireArguments().getBoolean(ARG_CAN_CHAT, false)
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> BottomSheetCallsIsRestrictedBinding
        get() = BottomSheetCallsIsRestrictedBinding::inflate

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val onCreateDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        return onCreateDialog.apply {
            behavior.peekHeight = resources.displayMetrics?.heightPixels ?: 0
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.let { existBinding ->

            val descriptionString = if (canChat) {
                R.string.user_personal_call_is_restricted_description
            } else {
                R.string.user_personal_call_and_message_are_restricted_description
            }
            existBinding.tvDescription.setText(getString(descriptionString, userName))
            existBinding.ivCloseIcon.setThrottledClickListener { dismiss() }
            existBinding.tvBtnCancel.setThrottledClickListener { dismiss() }

            existBinding.tvBtnWrite.isVisible = canChat
            existBinding.tvBtnWrite.setThrottledClickListener {
                (parentFragment as? CallIsRestrictedAlertListener)?.onOpenChatClicked()
                dismiss()
            }
        }
    }

    fun show(manager: FragmentManager?) {
        val fragment = manager?.findFragmentByTag(simpleName)
        if (fragment != null) return
        manager?.let {
            super.show(manager, simpleName)
        }
    }

    companion object {
        private const val ARG_USER_NAME = "argUserName"
        private const val ARG_CAN_CHAT = "argCanChat"

        fun getInstance(userName: String?, canChat: Boolean) =
            CallIsRestrictedBottomSheetFragment().apply {
                arguments = bundleOf(ARG_USER_NAME to userName, ARG_CAN_CHAT to canChat)
            }
    }
}
