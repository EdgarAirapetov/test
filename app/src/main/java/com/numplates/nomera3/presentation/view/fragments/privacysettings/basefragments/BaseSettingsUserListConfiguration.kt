package com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments

import com.numplates.nomera3.R

data class MeeraBaseSettingsUserListConfiguration(
    val screenTitle: String,
    val isShowAddUserItem: Boolean,
    val addUserItemTitle: String?,
    var removeListMenuIcon: Int = R.drawable.block_user_menu_item_v2,
    var actionListMenuText: Int = R.string.settings_remove_from_exclusions,
    val dialogListTitleRes: Int,
    val dialogListSubtitleRes: Int,
    val dialogItemTitleRes: Int,
    val dialogItemSubtitleRes: Int,
    val confirmationButtonTextRes: Int,
    var isShowDeleteAllItem: Boolean = true,
)
