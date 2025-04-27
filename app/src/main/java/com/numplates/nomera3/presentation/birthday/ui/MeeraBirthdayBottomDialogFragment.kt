package com.numplates.nomera3.presentation.birthday.ui

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.airbnb.lottie.LottieDrawable
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogBehDelegate
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogState
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentBirthdayBottomDialogBinding
import timber.log.Timber

class MeeraBirthdayBottomDialogFragment : UiKitBottomSheetDialog<MeeraFragmentBirthdayBottomDialogBinding>() {

    val animationsIds = listOf(
        R.raw.birthday_croc_present,
        R.raw.birthday_bluebunny,
        R.raw.birthday_monster,
        R.raw.birthday_unicorn_celebrate,
        R.raw.birthday_snake,
        R.raw.birthday_marmelade_dino,
        R.raw.birthday_bear,
        R.raw.birthday_ufo,
        R.raw.birthday_cutelamb,
    )

    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraFragmentBirthdayBottomDialogBinding
        get() = MeeraFragmentBirthdayBottomDialogBinding::inflate


    override fun getBehaviorDelegate(): UiKitBottomSheetDialogBehDelegate? {
        return UiKitBottomSheetDialogBehDelegate.Builder().setBottomSheetState(UiKitBottomSheetDialogState.EXPANDED)
            .setDraggable(true).setSkipCollapsed(true).create(dialog)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setScrollingToBottom()
        setButtonListener()
        getActionType()
    }

    private fun setButtonListener() {
        contentBinding?.let { binding ->
            with(binding) {
                btnOk.setThrottledClickListener {
                    dismissDialog()
                }
            }
        }
    }

    private fun getActionType() {
        arguments?.let { args ->
            val typeAction = args.getString(KEY_BIRTHDAY_TYPE)
            Timber.d("Birthday action type: $typeAction")
            typeAction?.let { action ->
                setDialogActionText(action)
            }
        }
    }

    private fun setDialogActionText(typeAction: String) {
        when (typeAction) {
            ACTION_TODAY_IS_BIRTHDAY -> {
                contentBinding?.tvBirthdayTitle?.text = context?.getString(R.string.happy_birthday_dialog)

            }

            ACTION_YESTERDAY_IS_BIRTHDAY -> {
                contentBinding?.tvBirthdayTitle?.text = context?.getString(R.string.past_birthday)
            }
        }
        val stringsArr = context?.resources?.getStringArray(R.array.birthdays)
        stringsArr?.let { array ->
            contentBinding?.tvDesc?.text = array.random()
        }
        contentBinding?.lvBirthdayIcon?.setAnimation(animationsIds.random())
        contentBinding?.lvBirthdayIcon?.repeatCount = LottieDrawable.INFINITE
        contentBinding?.lvBirthdayIcon?.playAnimation()


    }

    private fun dismissDialog() = this@MeeraBirthdayBottomDialogFragment.dismiss()

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        setFragmentResult(TAG,bundleOf())
    }

    private fun setScrollingToBottom() {
        dialog?.window?.decorView?.viewTreeObserver?.addOnGlobalLayoutListener {
            contentBinding?.root?.fullScroll(View.FOCUS_DOWN)
        }
    }

    companion object {
        const val ACTION_TODAY_IS_BIRTHDAY = "ACTION_TODAY_IS_BIRTHDAY"
        const val ACTION_YESTERDAY_IS_BIRTHDAY = "ACTION_YESTERDAY_IS_BIRTHDAY"
        const val ACTION_DEFAULT = "ACTION_DEFAULT"
        const val TAG = "BirthdayBottomDialogFragment"
        private const val KEY_BIRTHDAY_TYPE = "KEY_BIRTHDAY_TYPE"

        @JvmStatic
        fun create(actionType: String = ACTION_DEFAULT): MeeraBirthdayBottomDialogFragment {
            return MeeraBirthdayBottomDialogFragment().apply {
                arguments = bundleOf(KEY_BIRTHDAY_TYPE to actionType)
            }
        }
    }
}
