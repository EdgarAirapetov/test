package com.numplates.nomera3.presentation.view.utils.sharedialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.bottomsheetdialog.LabelIconUiState
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogParams
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraShareSettingsContainerFragmentBinding
import com.numplates.nomera3.presentation.model.enums.WhoCanCommentPostEnum
import com.numplates.nomera3.presentation.view.utils.sharedialog.MeeraShareSheet.Companion.LAYOUT_REPOST_TO_GROUP

private const val PADDING_HEADER_SETTINGS_SHARE = 16

class MeeraSharePosSettingsBottomSheet(
    val currentLayout: Int,
    val currentWhoCanComment: WhoCanCommentPostEnum,
    val listener: (action: WhoCanCommentPostEnum) ->Unit
): UiKitBottomSheetDialog<MeeraShareSettingsContainerFragmentBinding>() {

    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraShareSettingsContainerFragmentBinding
        get() = MeeraShareSettingsContainerFragmentBinding::inflate

    override fun createDialogState(): UiKitBottomSheetDialogParams{
        return UiKitBottomSheetDialogParams(
            labelText = context?.getString(R.string.meera_post_settings),
            labelIconUiState = LabelIconUiState(
                labelIcon = R.drawable.ic_outlined_arrow_left_m,
                padding = PADDING_HEADER_SETTINGS_SHARE.dp
            )
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMenuListener()
        initWhoCanComment()

        if (currentLayout == LAYOUT_REPOST_TO_GROUP){
            contentBinding?.vFriendsUserItem?.gone()
        }
    }

    private fun initWhoCanComment(){
        when(currentWhoCanComment){
            WhoCanCommentPostEnum.NOBODY -> contentBinding?.vNobodyUserItem?.setCellRightElementChecked(true)
            WhoCanCommentPostEnum.EVERYONE -> contentBinding?.vAllUserItem?.setCellRightElementChecked(true)
            else -> contentBinding?.vFriendsUserItem?.setCellRightElementChecked(true)
        }
    }

    private fun initMenuListener(){
        contentBinding?.apply {
            vAllUserItem.setRightElementContainerClickable(false)
            vFriendsUserItem.setRightElementContainerClickable(false)
            vNobodyUserItem.setRightElementContainerClickable(false)
            vAllUserItem.setThrottledClickListener {
                if (!vAllUserItem.isCheckButton){
                    vAllUserItem.setCellRightElementChecked(true)
                    vFriendsUserItem.setCellRightElementChecked(false)
                    vNobodyUserItem.setCellRightElementChecked(false)
                }
            }
            vFriendsUserItem.setThrottledClickListener {
                if (!vFriendsUserItem.isCheckButton){
                    vFriendsUserItem.setCellRightElementChecked(true)
                    vAllUserItem.setCellRightElementChecked(false)
                    vNobodyUserItem.setCellRightElementChecked(false)
                }
            }
            vNobodyUserItem.setThrottledClickListener {
                if (!vNobodyUserItem.isCheckButton){
                    vNobodyUserItem.setCellRightElementChecked(true)
                    vAllUserItem.setCellRightElementChecked(false)
                    vFriendsUserItem.setCellRightElementChecked(false)
                }
            }
        }
        rootBinding?.ibBottomSheetDialogAction?.setThrottledClickListener {
            dismiss()
        }
        contentBinding?.vSaveBtn?.setThrottledClickListener {
            contentBinding?.let {
                when {
                    it.vAllUserItem.isCheckButton -> listener.invoke(WhoCanCommentPostEnum.EVERYONE)
                    it.vFriendsUserItem.isCheckButton -> listener.invoke(WhoCanCommentPostEnum.FRIENDS)
                    it.vNobodyUserItem.isCheckButton -> listener.invoke(WhoCanCommentPostEnum.NOBODY)
                }
            }
            dismiss()
        }
    }
}
