package com.numplates.nomera3.modules.chat.requests.data.repository

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import com.meera.db.models.dialog.DialogEntity
import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.modules.chat.requests.ui.ChatRequestDataUiModel
import com.numplates.nomera3.presentation.view.adapter.newchat.MessagesSettings

interface ChatRequestRepository {

    fun getChatRequestInfo(): LiveData<List<MessagesSettings?>>

    fun getChatRequestData(): LiveData<ChatRequestDataUiModel?>

    fun getChatRequestRooms(): DataSource.Factory<Int, DialogEntity>

    suspend fun chatRequestAvailability(
        roomId: Long,
        approved: Int,
        success: (DialogEntity?) -> Unit,
        fail: (Exception) -> Unit
    )

    suspend fun updateChatRequestApprovedStatusDb(
        roomId: Long,
        approvedStatus: Int,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    )

    suspend fun updateMessagesAsChatRequest(
        roomId: Long?,
        isShowBlur: Boolean,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    )

    suspend fun disableChatRequestImageBlur(
        message: MessageEntity?,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    )

    suspend fun changeChatRequestVisibility(roomId: Long, isHidden: Boolean)

    suspend fun getDialogByCompanionId(userId: Long): List<DialogEntity>
}
