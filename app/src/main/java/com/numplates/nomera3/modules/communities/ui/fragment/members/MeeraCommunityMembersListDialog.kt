package com.numplates.nomera3.modules.communities.ui.fragment.members

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.meera.core.extensions.dp
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogParams
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraCommunityMembersDialogFragmentBinding

private const val BOTTOM_SHEET_MENU_MEMBER_LIST_BASE = "BOTTOM_SHEET_MENU_MEMBER_LIST_BASE"

class MeeraCommunityMembersListDialog : UiKitBottomSheetDialog<MeeraCommunityMembersDialogFragmentBinding>() {
    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraCommunityMembersDialogFragmentBinding
        get() = MeeraCommunityMembersDialogFragmentBinding::inflate

    private var menuItemClickListener: ((action: MeeraCommunityMembersDialogAction) -> Unit)? = null
    private var data = MeeraCommunityMemberDialogData()
    override fun createDialogState(): UiKitBottomSheetDialogParams =
        UiKitBottomSheetDialogParams(labelText = context?.getString(R.string.general_actions))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initClickListener()
    }

    fun setDataSet(data: MeeraCommunityMemberDialogData) {
        this.data = data
    }

    private fun initView() {
        menuItemClickListener = data.menuItemClickListener

        contentBinding?.vFirstItem?.setMarginStartDivider(8.dp)
        contentBinding?.vSecondItem?.setMarginStartDivider(8.dp)
        contentBinding?.vThirdItem?.setMarginStartDivider(8.dp)

        data.firstItemTitle?.let { contentBinding?.vFirstItem?.setTitleValue(it) }
        data.firstItemSubtitle?.let {
            contentBinding?.vFirstItem?.cellCityText = true
            contentBinding?.vFirstItem?.setCityValue(it)
        } ?: {
            contentBinding?.vFirstItem?.cellCityText = false
        }
        data.firstItemColorIcon?.let { contentBinding?.vFirstItem?.setLeftColorIcon(it) }
        data.firstItemIcon?.let { contentBinding?.vFirstItem?.setLeftIcon(it) }

        data.secondItemTitle?.let { contentBinding?.vSecondItem?.setTitleValue(it) }
        data.secondItemSubtitle?.let {
            contentBinding?.vSecondItem?.cellCityText = true
            contentBinding?.vSecondItem?.setCityValue(it)
        } ?: {
            contentBinding?.vSecondItem?.cellCityText = false
        }
        data.secondItemIcon?.let { contentBinding?.vSecondItem?.setLeftIcon(it) }
        data.secondItemColorIcon?.let { contentBinding?.vSecondItem?.setLeftColorIcon(it) }

        data.thirdItemTitle?.let { contentBinding?.vThirdItem?.setTitleValue(it) }
        data.thirdItemSubtitle?.let {
            contentBinding?.vThirdItem?.cellCityText = true
            contentBinding?.vThirdItem?.setCityValue(it)
        } ?: {
            contentBinding?.vThirdItem?.cellCityText = false
        }
        data.thirdItemIcon?.let { contentBinding?.vThirdItem?.setLeftIcon(it) }
        data.thirdItemColorIcon?.let { contentBinding?.vThirdItem?.setLeftColorIcon(it) }
    }

    private fun initClickListener() {
        contentBinding?.vFirstItem?.setThrottledClickListener {
            menuItemClickListener?.invoke(MeeraCommunityMembersDialogAction.FirstMenuItemClick)
            dismiss()
        }
        contentBinding?.vSecondItem?.setThrottledClickListener {
            menuItemClickListener?.invoke(MeeraCommunityMembersDialogAction.SecondMenuItemClick)
            dismiss()
        }
        contentBinding?.vThirdItem?.setThrottledClickListener {
            menuItemClickListener?.invoke(MeeraCommunityMembersDialogAction.ThirdMenuItemClick)
            dismiss()
        }
    }
}

class MeeraCommunityMembersListDialogBuilder {
    private var data = MeeraCommunityMemberDialogData()

    fun setMenuItemClickListener(listener: (action: MeeraCommunityMembersDialogAction) -> Unit): MeeraCommunityMembersListDialogBuilder {
        data = data.copy(menuItemClickListener = listener)
        return this
    }

    fun setFirstItemTitle(title: String): MeeraCommunityMembersListDialogBuilder {
        data = data.copy(firstItemTitle = title)
        return this
    }

    fun setFirstItemSubtitle(subtitle: String): MeeraCommunityMembersListDialogBuilder {
        data = data.copy(firstItemSubtitle = subtitle)
        return this
    }

    fun setFirstItemIcon(icon: Int): MeeraCommunityMembersListDialogBuilder {
        data = data.copy(firstItemIcon = icon)
        return this
    }

    fun setFirstItemColorIcon(color: Int): MeeraCommunityMembersListDialogBuilder {
        data = data.copy(firstItemColorIcon = color)
        return this
    }

    fun setSecondItemTitle(title: String): MeeraCommunityMembersListDialogBuilder {
        data = data.copy(secondItemTitle = title)
        return this
    }

    fun setSecondItemSubtitle(subtitle: String): MeeraCommunityMembersListDialogBuilder {
        data = data.copy(secondItemSubtitle = subtitle)
        return this
    }

    fun setSecondItemIcon(icon: Int): MeeraCommunityMembersListDialogBuilder {
        data = data.copy(secondItemIcon = icon)
        return this
    }

    fun setSecondItemColorIcon(color: Int): MeeraCommunityMembersListDialogBuilder {
        data = data.copy(secondItemColorIcon = color)
        return this
    }

    fun setThirdItemTitle(title: String): MeeraCommunityMembersListDialogBuilder {
        data = data.copy(thirdItemTitle = title)
        return this
    }

    fun setThirdItemSubtitle(subtitle: String): MeeraCommunityMembersListDialogBuilder {
        data = data.copy(thirdItemSubtitle = subtitle)
        return this
    }

    fun setThirdItemIcon(icon: Int): MeeraCommunityMembersListDialogBuilder {
        data = data.copy(thirdItemIcon = icon)
        return this
    }

    fun setThirdItemColorIcon(color: Int): MeeraCommunityMembersListDialogBuilder {
        data = data.copy(thirdItemColorIcon = color)
        return this
    }

    fun show(fm: FragmentManager): MeeraCommunityMembersListDialog {
        val dialog = MeeraCommunityMembersListDialog()
        dialog.setDataSet(data)
        dialog.show(fm, BOTTOM_SHEET_MENU_MEMBER_LIST_BASE)
        return dialog
    }
}
