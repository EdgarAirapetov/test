package com.numplates.nomera3.modules.uploadpost.ui.entity

import com.numplates.nomera3.modules.feed.data.entity.NotAvailableReasonResponse

enum class NotAvailableReasonUiEntity {
    POST_NOT_FOUND,
    USER_NOT_CREATOR,
    POST_DELETED,
    EVENT_POST_UNABLE_TO_UPDATE,
    UPDATE_TIME_IS_OVER
}

fun NotAvailableReasonResponse.toUiEntity() =
    when (this) {
        NotAvailableReasonResponse.POST_NOT_FOUND -> NotAvailableReasonUiEntity.POST_NOT_FOUND
        NotAvailableReasonResponse.USER_NOT_CREATOR -> NotAvailableReasonUiEntity.USER_NOT_CREATOR
        NotAvailableReasonResponse.POST_DELETED -> NotAvailableReasonUiEntity.POST_DELETED
        NotAvailableReasonResponse.EVENT_POST_UNABLE_TO_UPDATE -> NotAvailableReasonUiEntity.EVENT_POST_UNABLE_TO_UPDATE
        NotAvailableReasonResponse.UPDATE_TIME_IS_OVER -> NotAvailableReasonUiEntity.UPDATE_TIME_IS_OVER
    }
