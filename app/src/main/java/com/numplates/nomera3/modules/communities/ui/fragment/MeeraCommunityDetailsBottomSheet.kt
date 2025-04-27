package com.numplates.nomera3.modules.communities.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.meera.core.extensions.isFalse
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogBehDelegate
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogParams
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogState
import com.numplates.nomera3.databinding.MeeraCommunityDetailsBottomSheetBinding
import com.numplates.nomera3.modules.communities.data.entity.CommunityEntity
import java.text.SimpleDateFormat
import java.util.Locale

private const val MEERA_COMMUNITY_DETAILS_BOTTOM_SHEET = "MEERA_COMMUNITY_DETAILS_BOTTOM_SHEET"
private const val COMMUNITY_CREATE_DATE_PATTERN = "d MMMM yyyy"

class MeeraCommunityDetailsBottomSheet : UiKitBottomSheetDialog<MeeraCommunityDetailsBottomSheetBinding>() {

    private var data: MeeraCommunityDetailsBottomSheetData = MeeraCommunityDetailsBottomSheetData()
    private var clickAction: (MeeraCommunityDetailsBottomSheetClick) -> Unit = {}

    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraCommunityDetailsBottomSheetBinding
        get() = MeeraCommunityDetailsBottomSheetBinding::inflate

    override fun getBehaviorDelegate(): UiKitBottomSheetDialogBehDelegate {
        return UiKitBottomSheetDialogBehDelegate.Builder()
            .setBottomSheetState(UiKitBottomSheetDialogState.EXPANDED)
            .setDraggable(true)
            .setSkipCollapsed(true)
            .create(dialog)
    }

    override fun createDialogState(): UiKitBottomSheetDialogParams =
        UiKitBottomSheetDialogParams(
            labelText = data.community?.name,
            needShowCloseButton = true
        )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initClicks()
        handleGetMembersVisibility()
    }

    fun show(
        fm: FragmentManager,
        data: MeeraCommunityDetailsBottomSheetData,
        clickAction: (MeeraCommunityDetailsBottomSheetClick) -> Unit
    ): MeeraCommunityDetailsBottomSheet {
        val dialog = MeeraCommunityDetailsBottomSheet()
        dialog.data = data
        dialog.clickAction = clickAction
        dialog.isCancelable = this.isCancelable
        dialog.show(fm, MEERA_COMMUNITY_DETAILS_BOTTOM_SHEET)
        return dialog
    }

    private fun initViews() {
        rootBinding?.tvBottomSheetDialogLabel?.setSingleLine(false)
        rootBinding?.tvBottomSheetDialogLabel?.maxLines = 2
        contentBinding?.apply {
            val community = data.community
            if (community?.isModerator == 1) btnCommunityEdit.visible()
            val formatter = SimpleDateFormat(COMMUNITY_CREATE_DATE_PATTERN, Locale.getDefault())
            cellInputGroupCreate.setTitleValue(formatter.format(community?.timeCreated))
            cellInputGroupDescription.setText(community?.description)
            cellInputGroupMembers.setTitleValue(community?.joinedUsers.toString())
        }
    }

    private fun initClicks() {
        contentBinding?.apply {
            btnCommunityEdit.setThrottledClickListener {
                clickAction.invoke(MeeraCommunityDetailsBottomSheetClick.OpenEditor)
                dismiss()
            }
            cellInputGroupMembers.setRightElementContainerClickable(false)
            cellInputGroupMembers.setThrottledClickListener {
                clickAction.invoke(MeeraCommunityDetailsBottomSheetClick.OpenUsers)
                dismiss()
            }
        }
    }

    private fun handleGetMembersVisibility() {
        val community = data.community
        val isShowMembers = if (community != null) showMembers(community) else false
        if (isShowMembers) {
            contentBinding?.apply {
                tvCommunityMembersTitle.visible()
                cellInputGroupMembers.visible()
            }
        }
    }

    private fun showMembers(community: CommunityEntity): Boolean {
        return when {
            community.userStatus == CommunityEntity.USER_STATUS_BANNED -> false
            community.private.isTrue() && community.isSubscribed.isFalse() -> false
            else -> true
        }
    }

}

data class MeeraCommunityDetailsBottomSheetData(
    val community: CommunityEntity? = null,
)

sealed interface MeeraCommunityDetailsBottomSheetClick {
    data object OpenEditor : MeeraCommunityDetailsBottomSheetClick

    data object OpenUsers : MeeraCommunityDetailsBottomSheetClick
}
