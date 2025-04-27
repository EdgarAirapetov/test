package com.numplates.nomera3.presentation.view.fragments.bottomfragment.callisresrticted

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogParams
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraBottomSheetCallsIsRestrictedBinding
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_NAME

class MeeraCallIsRestrictedBottomSheetFragment : UiKitBottomSheetDialog<MeeraBottomSheetCallsIsRestrictedBinding>() {

    private val userName: String by lazy(LazyThreadSafetyMode.NONE) {
        requireArguments().getString(ARG_USER_NAME) ?: ""
    }

    private val canChat: Boolean by lazy(LazyThreadSafetyMode.NONE) {
        requireArguments().getBoolean(ARG_CALL_IS_RESTRICTED_CAN_CHAT, false)
    }

    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraBottomSheetCallsIsRestrictedBinding
        get() = MeeraBottomSheetCallsIsRestrictedBinding::inflate

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
        setupViews()
        initListeners()
        initBehavior()
    }

    private fun setupViews() {
        contentBinding?.apply {

            val descriptionString = if (canChat) {
                R.string.user_personal_call_is_restricted_description
            } else {
                R.string.user_personal_call_and_message_are_restricted_description
            }
            tvDescription.text = getString(descriptionString, userName)

            btnWrite.isVisible = canChat
        }
    }

    private fun initListeners() {
        contentBinding?.apply {
            btnWrite.setThrottledClickListener {
                setButtonsClickResult(true)
                dismiss()
            }
            btnCancel.setThrottledClickListener {
                setButtonsClickResult(false)
                dismiss()
            }
            nvCallIsRestricted.closeButtonClickListener = {
                setButtonsClickResult(false)
                dismiss()
            }
        }
    }

    private fun setButtonsClickResult(isWriteClicked: Boolean) {
        setFragmentResult(
            requestKey = ARG_CALL_IS_RESTRICTED_REQUEST_KEY, result = bundleOf(ARG_CALL_IS_RESTRICTED_IS_CHAT_CLICKED to isWriteClicked)
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
        const val CALL_IS_RESTRICTED_BOTTOM_DIALOG_TAG = "callIsRestrictedBottomDialogTag"
        const val ARG_CALL_IS_RESTRICTED_REQUEST_KEY = "argCallIsRestrictedRequestKey"
        const val ARG_CALL_IS_RESTRICTED_IS_CHAT_CLICKED = "argCallIsRestrictedIsChatClicked"
        private const val ARG_CALL_IS_RESTRICTED_CAN_CHAT = "argCallIsRestrictedCanChat"


        @JvmStatic
        fun show(
            fragmentManager: FragmentManager, userName: String?, canChat: Boolean
        ): MeeraCallIsRestrictedBottomSheetFragment {
            val instance = MeeraCallIsRestrictedBottomSheetFragment()
            instance.arguments = bundleOf(ARG_USER_NAME to userName, ARG_CALL_IS_RESTRICTED_CAN_CHAT to canChat)
            instance.show(fragmentManager, CALL_IS_RESTRICTED_BOTTOM_DIALOG_TAG)
            return instance
        }
    }
}
