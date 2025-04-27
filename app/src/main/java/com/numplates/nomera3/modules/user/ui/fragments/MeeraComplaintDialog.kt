package com.numplates.nomera3.modules.user.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.meera.core.databinding.MeeraDialogComplaintBinding
import com.meera.core.extensions.setBackgroundTint
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogBehDelegate
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogParams
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogState
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.complains.ui.ComplainEvents

//TODO Вызов этой шторки завязан на дорогу (BaseFeedFragment),
// когда новая дорога появится в профиле, нужно будет проверить вызов шторки
// https://nomera.atlassian.net/browse/BR-31681
class MeeraComplaintDialog : UiKitBottomSheetDialog<MeeraDialogComplaintBinding>() {

    private var callback: MeeraAdditionalComplainCallback? = null
    private var userId: Long? = null
    private var optionHideMoments: Boolean = false

    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraDialogComplaintBinding
        get() = MeeraDialogComplaintBinding::inflate

    override fun getBehaviorDelegate(): UiKitBottomSheetDialogBehDelegate {
        return UiKitBottomSheetDialogBehDelegate.Builder()
            .setBottomSheetState(UiKitBottomSheetDialogState.EXPANDED)
            .setDraggable(true)
            .setSkipCollapsed(true)
            .create(dialog)
    }


    override fun createDialogState(): UiKitBottomSheetDialogParams =
        UiKitBottomSheetDialogParams(labelText = context?.getString(R.string.meera_complaint_complaint_accepted))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            userId = it.getLong(UserComplainAdditionalBottomSheet.KEY_COMPLAIN_USER_ID)
            optionHideMoments = it.getBoolean(UserComplainAdditionalBottomSheet.KEY_COMPLAIN_OPTION_HIDE_MOMENTS)
        }
        changeStateAcceptButton(false)
        initViews()
    }

    fun setComplaintCallback(callback: MeeraAdditionalComplainCallback) {
        this.callback = callback
    }

    private fun initViews() {
        contentBinding?.rbDialogComplaintHidePosts?.isVisible = !optionHideMoments
        contentBinding?.rbDialogComplaintHideMoment?.isVisible = optionHideMoments

        contentBinding?.rbDialogComplaintHidePosts?.setRightElementContainerClickable(false)
        contentBinding?.rbDialogComplaintHidePosts?.let {
            it.setThrottledClickListener {
                if (!it.isCheckButton) {
                    it.setCellRightElementChecked(true)
                    contentBinding?.rbDialogComplaintBlocked?.setCellRightElementChecked(false)
                }
                changeStateAcceptButton(true)
            }
        }
        contentBinding?.rbDialogComplaintHideMoment?.setRightElementContainerClickable(false)
        contentBinding?.rbDialogComplaintHideMoment?.let {
            it.setThrottledClickListener {
                if (!it.isCheckButton) {
                    it.setCellRightElementChecked(true)
                    contentBinding?.rbDialogComplaintBlocked?.setCellRightElementChecked(false)
                }
                changeStateAcceptButton(true)
            }
        }
        contentBinding?.rbDialogComplaintBlocked?.setRightElementContainerClickable(false)
        contentBinding?.rbDialogComplaintBlocked?.let {
            it.setThrottledClickListener {
                if (!it.isCheckButton) {
                    it.setCellRightElementChecked(true)
                    contentBinding?.rbDialogComplaintHidePosts?.setCellRightElementChecked(false)
                    contentBinding?.rbDialogComplaintHideMoment?.setCellRightElementChecked(false)
                }
                changeStateAcceptButton(true)
            }
        }

        initAcceptBtn()
    }

    private fun initAcceptBtn() {
        contentBinding?.vDialogComplaintBtnAccept?.setThrottledClickListener {
            contentBinding?.let {
                when {
                    it.rbDialogComplaintBlocked.isRadioCheckButton -> {
                        callback?.onSuccess(
                            msg = R.string.meera_user_complain_user_blocked,
                            reason = ComplainEvents.UserBlocked,
                            userId = userId
                        )
                        dismiss()
                    }

                    it.rbDialogComplaintHidePosts.isRadioCheckButton -> {
                        callback?.onSuccess(
                            msg = R.string.meeta_user_complain_posts_hided,
                            reason = ComplainEvents.PostsDisabledEvents,
                            userId = userId
                        )
                        dismiss()
                    }

                    it.rbDialogComplaintHideMoment.isRadioCheckButton -> {
                        userId?.let { userId ->
                            callback?.onSuccess(
                                msg = R.string.meera_user_complain_moments_hidden,
                                reason = ComplainEvents.MomentsHidden(userId),
                                userId = userId
                            )
                        }
                        dismiss()
                    }
                }
            }
        }
        rootBinding?.ivBottomSheetDialogClose?.setThrottledClickListener {
            callback?.onSuccess(
                msg = null,
                reason = ComplainEvents.RequestModerators,
                userId = null
            )
            dismiss()
        }
    }

    private fun changeStateAcceptButton(isEnable: Boolean) {
        if (isEnable) {
            contentBinding?.vDialogComplaintBtnAccept?.isEnabled = true
            contentBinding?.vDialogComplaintBtnAccept?.setBackgroundTint(R.color.uiKitColorAccentPrimary)
        } else {
            contentBinding?.vDialogComplaintBtnAccept?.isEnabled = false
            contentBinding?.vDialogComplaintBtnAccept?.setBackgroundTint(R.color.uiKitColorDisabledTetriary)
        }

    }

    companion object {

        const val KEY_COMPLAIN_USER_ID = "KEY_COMPLAIN_USER_ID"
        const val KEY_COMPLAIN_OPTION_HIDE_MOMENTS = "KEY_COMPLAIN_OPTION_HIDE_MOMENTS"

        fun newInstance(userId: Long, optionHideMoments: Boolean = false): MeeraComplaintDialog {
            val arg = Bundle()
            arg.putLong(KEY_COMPLAIN_USER_ID, userId)
            arg.putBoolean(KEY_COMPLAIN_OPTION_HIDE_MOMENTS, optionHideMoments)
            val fragment = MeeraComplaintDialog()
            fragment.arguments = arg
            return fragment
        }
    }
}
