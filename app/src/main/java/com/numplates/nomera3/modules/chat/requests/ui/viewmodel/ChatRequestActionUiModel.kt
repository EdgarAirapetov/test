package com.numplates.nomera3.modules.chat.requests.ui.viewmodel

import com.meera.db.models.dialog.DialogEntity

sealed class ChatRequestActionUiModel {

    data class BlockUserWorkData(
        val userId: Long,
        val companionData: DialogEntity
    ): ChatRequestActionUiModel()

    data class BlockReportUserWorkData(
        val userId: Long,
        val companionDialog: DialogEntity,
        val complaintReasonId: Int,
    ): ChatRequestActionUiModel()
}
