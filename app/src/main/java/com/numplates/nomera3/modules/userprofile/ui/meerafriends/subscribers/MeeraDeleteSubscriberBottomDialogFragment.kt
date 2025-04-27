package com.numplates.nomera3.modules.userprofile.ui.meerafriends.subscribers

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogParams
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.snackbar.AvatarUiState
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentDeleteSubscriberBottomDialogBinding
import com.numplates.nomera3.modules.bump.ui.viewmodel.ShakeBottomDialogViewModel
import com.numplates.nomera3.presentation.router.IArgContainer

class MeeraDeleteSubscriberBottomDialogFragment :
    UiKitBottomSheetDialog<MeeraFragmentDeleteSubscriberBottomDialogBinding>() {


    private val argUserId by lazy { requireArguments().getLong(IArgContainer.ARG_USER_ID) }

    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraFragmentDeleteSubscriberBottomDialogBinding
        get() = MeeraFragmentDeleteSubscriberBottomDialogBinding::inflate

    override fun createDialogState() = UiKitBottomSheetDialogParams(
        needShowToolbar = false, needShowCloseButton = false, dialogStyle = R.style.BottomSheetDialogTransparentTheme
    )

    private var shakeBottomSheetBehavior: BottomSheetBehavior<*>? = null
    private val viewModel by viewModels<ShakeBottomDialogViewModel> {
        App.component.getViewModelFactory()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        viewModel.setDialogDismissed()
    }

    fun showErrorToast(@StringRes errorMessage: Int) {
        UiKitSnackBar.make(
            view = requireView(), params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = getText(errorMessage),
                    avatarUiState = AvatarUiState.ErrorIconState,
                )
            )
        ).show()
    }

    private fun initView() {
        initBehavior()
        initListeners()
    }

    private fun initListeners() {
        rootBinding?.ivBottomSheetDialogClose?.setOnClickListener {
            dismiss()
        }
        contentBinding?.nvDelete?.closeButtonClickListener = {
            dismiss()
        }

        contentBinding?.buttonDelete?.setThrottledClickListener {
            setFragmentResult(
                KEY_DELETE_SUBSCRIBER_RESULT,
                bundleOf(KEY_DELETE_SUBSCRIBER_ONLY to true, IArgContainer.ARG_USER_ID to argUserId)
            )
            dismiss()
        }
        contentBinding?.buttonDeleteAndUnsubscribe?.setThrottledClickListener {
            setFragmentResult(
                KEY_DELETE_SUBSCRIBER_RESULT,
                bundleOf(KEY_DELETE_SUBSCRIBER_ONLY to false, IArgContainer.ARG_USER_ID to argUserId)
            )
            dismiss()
        }
    }

    private fun initBehavior() {
        val dialog = dialog as BottomSheetDialog
        val mainContainer = dialog.findViewById<FrameLayout>(R.id.design_bottom_sheet)
        mainContainer?.let { frameLayout ->
            shakeBottomSheetBehavior = BottomSheetBehavior.from(frameLayout)
            shakeBottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    companion object {
        const val DELETE_SUBSCRIBER_BOTTOM_DIALOG_TAG = "deleteSubscriberBottomDialog"

        const val KEY_DELETE_SUBSCRIBER_RESULT = "deleteSubscriberResult"
        const val KEY_DELETE_SUBSCRIBER_ONLY = "deleteSubscriberOnly"

        @JvmStatic
        fun show(
            fragmentManager: FragmentManager, userId: Long
        ): MeeraDeleteSubscriberBottomDialogFragment {
            val instance = MeeraDeleteSubscriberBottomDialogFragment()
            instance.arguments = bundleOf(IArgContainer.ARG_USER_ID to userId)
            instance.show(fragmentManager, DELETE_SUBSCRIBER_BOTTOM_DIALOG_TAG)
            return instance
        }
    }
}
