package com.numplates.nomera3.modules.chat

import android.content.Context
import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraMenuBottomSheet

class MoreMenuBottomSheetBuilder(activityContext: Context) {
    private val menuBottomSheet = MeeraMenuBottomSheet(activityContext)

    fun addForbidMessagesItem(onClick: () -> Unit) {
        menuBottomSheet.addItem(
            title = R.string.profile_dots_menu_disallow_messages,
            icon = R.drawable.ic_disallow_message,
            click = onClick
        )
    }

    fun addAllowMessagesItem(onClick: () -> Unit) {
        menuBottomSheet.addItem(
            title = R.string.profile_dots_menu_allow_messages,
            icon = R.drawable.ic_allow_message,
            click = onClick
        )
    }

    fun addBlockUserItem(onClick: () -> Unit) {
        menuBottomSheet.addItem(
            title = R.string.general_block,
            icon = R.drawable.ic_block_user_red,
            click = onClick
        )
    }

    fun addUnblockUserItem(onClick: () -> Unit) {
        menuBottomSheet.addItem(
            title = R.string.general_unblock,
            icon = R.drawable.ic_user_check_purple,
            click = onClick
        )
    }

    fun addBlockReportItem(onClick: () -> Unit) {
        menuBottomSheet.addItem(
            title = R.string.complaints_block_and_report_user,
            icon = R.drawable.ic_report_profile,
            click = onClick
        )
    }

    fun addReportUserItem(onClick: () -> Unit) {
        menuBottomSheet.addItem(
            title = R.string.user_complain,
            icon = R.drawable.ic_report_profile,
            click = onClick
        )
    }

    fun build() = menuBottomSheet
}
