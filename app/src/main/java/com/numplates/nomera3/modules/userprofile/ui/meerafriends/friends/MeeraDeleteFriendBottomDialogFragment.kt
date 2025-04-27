package com.numplates.nomera3.modules.userprofile.ui.meerafriends.friends

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
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentDeleteFriendBottomDialogBinding
import com.numplates.nomera3.modules.bump.ui.viewmodel.ShakeBottomDialogViewModel
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.utils.NToast

class MeeraDeleteFriendBottomDialogFragment : UiKitBottomSheetDialog<MeeraFragmentDeleteFriendBottomDialogBinding>() {

    private val argUserId by lazy { requireArguments().getLong(IArgContainer.ARG_USER_ID) }
    private val argUserName by lazy { requireArguments().getString(IArgContainer.ARG_USER_NAME) }

    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraFragmentDeleteFriendBottomDialogBinding
        get() = MeeraFragmentDeleteFriendBottomDialogBinding::inflate

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
        NToast.with(view).text(getString(errorMessage)).typeError().inView(dialog?.window?.decorView).show()
    }

    private fun initView() {
        contentBinding?.tvDescription?.text =
            getString(R.string.user_info_remove_from_friend_dialog_description, argUserName)

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
                KEY_DELETE_RESULT, bundleOf(KEY_DELETE_ONLY to true, IArgContainer.ARG_USER_ID to argUserId)
            )
            dismiss()
        }
        contentBinding?.buttonDeleteAndUnsubscribe?.setThrottledClickListener {
            setFragmentResult(
                KEY_DELETE_RESULT, bundleOf(KEY_DELETE_ONLY to false, IArgContainer.ARG_USER_ID to argUserId)
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
        const val DELETE_BOTTOM_DIALOG_TAG = "deleteBottomDialog"
        const val KEY_DELETE_RESULT = "deleteResult"
        const val KEY_DELETE_ONLY = "deleteOnly"


        @JvmStatic
        fun show(
            fragmentManager: FragmentManager, userId: Long, userName: String
        ): MeeraDeleteFriendBottomDialogFragment {
            val instance = MeeraDeleteFriendBottomDialogFragment()
            instance.arguments = bundleOf(IArgContainer.ARG_USER_ID to userId, IArgContainer.ARG_USER_NAME to userName)
            instance.show(
                fragmentManager, DELETE_BOTTOM_DIALOG_TAG
            )
            return instance
        }
    }
}
