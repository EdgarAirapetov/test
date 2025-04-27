package com.numplates.nomera3.modules.chat.requests.ui.viewevent

import com.meera.db.models.dialog.DialogEntity
import com.numplates.nomera3.modules.chat.requests.ui.viewmodel.ChatRequestActionUiModel

sealed class ChatRequestViewEvent {
    class AllowChat(val room: DialogEntity): ChatRequestViewEvent()
    class ForbidChatRequest(val room: DialogEntity): ChatRequestViewEvent()
    object NetworkErrorChatRequest: ChatRequestViewEvent()
    class BlockUserResult(val dialogToDelete: DialogEntity, val isSuccess: Boolean): ChatRequestViewEvent()
    class BlockUserJobCreated(val workData: ChatRequestActionUiModel.BlockUserWorkData): ChatRequestViewEvent()
    class BlockReportUserJobCreated(val workData: ChatRequestActionUiModel.BlockReportUserWorkData):
        ChatRequestViewEvent()
    class BlockReportUserResult(
        val dialogToDelete: DialogEntity,
        val isSuccess: Boolean
    ): ChatRequestViewEvent()
    object OnPagingInitialized : ChatRequestViewEvent()
}
