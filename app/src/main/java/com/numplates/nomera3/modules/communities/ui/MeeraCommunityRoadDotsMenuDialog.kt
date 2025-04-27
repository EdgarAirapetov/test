package com.numplates.nomera3.modules.communities.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.meera.core.extensions.gone
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogBehDelegate
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogParams
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogState
import com.meera.uikit.widgets.cell.CellPosition
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraDialogDotsMenuCommunityRoadBinding

private const val MEERA_COMMUNITY_ROAD_DOTS_MENU_DIALOG = "MEERA_COMMUNITY_ROAD_DOTS_MENU_DIALOG"

class MeeraCommunityRoadDotsMenuDialog : UiKitBottomSheetDialog<MeeraDialogDotsMenuCommunityRoadBinding>() {

    var data: MeeraCommunityRoadDotsMenuDialogData = MeeraCommunityRoadDotsMenuDialogData()
    private var clickAction: (MeeraCommunityRoadDotsMenuDialogClick) -> Unit = {}

    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraDialogDotsMenuCommunityRoadBinding
        get() = MeeraDialogDotsMenuCommunityRoadBinding::inflate

    override fun getBehaviorDelegate(): UiKitBottomSheetDialogBehDelegate {
        return UiKitBottomSheetDialogBehDelegate.Builder()
            .setBottomSheetState(UiKitBottomSheetDialogState.EXPANDED)
            .setDraggable(true)
            .setSkipCollapsed(true)
            .create(dialog)
    }

    override fun createDialogState(): UiKitBottomSheetDialogParams =
        UiKitBottomSheetDialogParams(labelText = context?.getString(R.string.general_actions))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initClicks()
    }

    fun show(
        fm: FragmentManager,
        data: MeeraCommunityRoadDotsMenuDialogData,
        clickAction: (MeeraCommunityRoadDotsMenuDialogClick) -> Unit
    ): MeeraCommunityRoadDotsMenuDialog {
        val dialog = MeeraCommunityRoadDotsMenuDialog()
        dialog.data = data
        dialog.clickAction = clickAction
        dialog.isCancelable = this.isCancelable
        dialog.show(fm, MEERA_COMMUNITY_ROAD_DOTS_MENU_DIALOG)
        return dialog
    }

    private fun initViews() {
        if (data.isShowSettingsItem.not()) {
            contentBinding?.cellCommunitySettings?.gone()
            contentBinding?.cellCopyCommunityLinkItem?.cellPosition = CellPosition.BOTTOM
        }
    }

    private fun initClicks() {
        contentBinding?.apply {
            cellShareCommunityItem.setThrottledClickListener {
                clickAction.invoke(MeeraCommunityRoadDotsMenuDialogClick.OnClickShare)
                dismiss()
            }
            cellCopyCommunityLinkItem.setThrottledClickListener {
                clickAction.invoke(MeeraCommunityRoadDotsMenuDialogClick.OnClickCopy)
                dismiss()
            }
            cellCommunitySettings.setThrottledClickListener {
                clickAction.invoke(MeeraCommunityRoadDotsMenuDialogClick.OnClickSettings)
                dismiss()
            }
        }
    }

}

data class MeeraCommunityRoadDotsMenuDialogData(
    val isShowSettingsItem: Boolean = false
)

sealed interface MeeraCommunityRoadDotsMenuDialogClick {
    data object OnClickShare : MeeraCommunityRoadDotsMenuDialogClick

    data object OnClickCopy: MeeraCommunityRoadDotsMenuDialogClick

    data object OnClickSettings: MeeraCommunityRoadDotsMenuDialogClick
}
