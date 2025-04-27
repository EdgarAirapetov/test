package com.numplates.nomera3.modules.chat


import com.meera.db.models.dialog.DialogEntity
import com.numplates.nomera3.modules.user.ui.entity.UserComplainEntity

data class PendingChatBlockReportActionUiModel(
    val userId: Long,
    val companionDialog: DialogEntity,
    val complaintData: UserComplainEntity
)
