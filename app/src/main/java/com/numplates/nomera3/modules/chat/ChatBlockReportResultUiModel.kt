package com.numplates.nomera3.modules.chat

import com.numplates.nomera3.modules.complains.ui.ComplaintFlowResult

data class ChatBlockReportResultUiModel(
    var isBlockSuccess: Boolean? = null,
    var reportResult: ComplaintFlowResult? = null
)
