package com.numplates.nomera3.modules.chat.requests.ui.fragment

import com.meera.db.models.dialog.DialogEntity

interface ChatRequestCallback {
    fun onBlockUser(userId: Long, companionData: DialogEntity)
    fun onBlockReportUser(userId: Long, companionData: DialogEntity, complaintReasonId: Int)
}
