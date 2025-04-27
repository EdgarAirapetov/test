package com.numplates.nomera3.modules.chat.requests.data.repository

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import com.meera.core.extensions.combineWith
import com.meera.core.preferences.AppSettings
import com.meera.db.DataStore
import com.meera.db.models.dialog.DialogEntity
import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.modules.chat.data.DialogApproved
import com.numplates.nomera3.modules.chat.requests.data.api.ChatRequestApi
import com.numplates.nomera3.modules.chat.requests.ui.ChatRequestDataUiModel
import com.numplates.nomera3.presentation.view.adapter.newchat.ChatRequestItemData
import com.numplates.nomera3.presentation.view.adapter.newchat.MessageSettingsItemType
import com.numplates.nomera3.presentation.view.adapter.newchat.MessagesSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class ChatRequestRepositoryImpl @Inject constructor(
    private val dataStore: DataStore,
    private val api: ChatRequestApi,
    private val appSettings: AppSettings
) : ChatRequestRepository {

    override fun getChatRequestInfo(): LiveData<List<MessagesSettings?>> {
        val liveRequestCount = dataStore.dialogDao()
            .getChatRequestRoomsCount(
                notDefinedStatus = DialogApproved.NOT_DEFINED.key,
                forbiddenStatus = DialogApproved.FORBIDDEN.key
            )

        val unreadMessageCount = dataStore.dialogDao()
            .getChatRequestUnreadMessageCount(DialogApproved.NOT_DEFINED.key)

        val liveSettings =
            liveRequestCount.combineWith(unreadMessageCount) { roomsCount, unreadCount ->
                if (roomsCount != null && unreadCount != null) {
                    return@combineWith getChatRequestSettings(roomsCount, unreadCount)
                } else {
                    return@combineWith listOf<MessagesSettings?>(null)
                }
            }

        return liveSettings
    }

    override fun getChatRequestData(): LiveData<ChatRequestDataUiModel?> {
        val liveRequestCount = dataStore.dialogDao()
            .getChatRequestRoomsCount(
                notDefinedStatus = DialogApproved.NOT_DEFINED.key,
                forbiddenStatus = DialogApproved.FORBIDDEN.key
            )

        val unreadMessageCount = dataStore.dialogDao()
            .getChatRequestUnreadMessageCount(DialogApproved.NOT_DEFINED.key)
        val liveSettings =
            liveRequestCount.combineWith(unreadMessageCount) { roomsCount, unreadCount ->
                if (roomsCount != null && unreadCount != null) {
                    return@combineWith ChatRequestDataUiModel(roomsCount, unreadCount)
                } else {
                    return@combineWith null
                }
            }

        return liveSettings
    }

    private fun getChatRequestSettings(
        roomsCount: Int,
        unreadCount: Int
    ): List<MessagesSettings> {
        val isShowItem = roomsCount > 0
        return if (isShowItem) {
            listOf(
                MessagesSettings(
                    itemType = MessageSettingsItemType.CHAT_REQUEST,
                    settingState = null,
                    chatRequestData = ChatRequestItemData(unreadCount)
                )
            )
        } else {
            emptyList()
        }
    }

    override fun getChatRequestRooms(): DataSource.Factory<Int, DialogEntity> =
        dataStore.dialogDao().getChatRequestDialogs(
            notDefinedStatus = DialogApproved.NOT_DEFINED.key,
            forbiddenStatus = DialogApproved.FORBIDDEN.key
        )

    override suspend fun changeChatRequestVisibility(roomId: Long, isHidden: Boolean) {
        withContext(Dispatchers.IO) {
            dataStore.dialogDao().changeDialogVisibilityById(
                dialogId = roomId,
                isHidden = isHidden
            )
        }
    }

    override suspend fun getDialogByCompanionId(userId: Long): List<DialogEntity> {
        return withContext(Dispatchers.IO) {
            dataStore.dialogDao().getDialogByCompanionId(userId)
        }
    }

    override suspend fun chatRequestAvailability(
        roomId: Long,
        approved: Int,
        success: (DialogEntity?) -> Unit,
        fail: (Exception) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                val response = api.chatRequestAvailability(
                    postId = roomId,
                    approved = approved
                )
                if (response.data != null) {
                    val room = response.data?.dialog
                    dataStore.dialogDao().updateChatRequestApprovedStatus(
                        roomId = roomId,
                        approvedStatus = room?.approved ?: DialogApproved.NOT_DEFINED.key
                    )
                    success(room)
                } else {
                    fail(IllegalArgumentException("Empty response"))
                }
            } catch (e: Exception) {
                Timber.e(e)
                fail(e)
            }
        }
    }

    override suspend fun updateChatRequestApprovedStatusDb(
        roomId: Long,
        approvedStatus: Int,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            dataStore.dialogDao().updateChatRequestApprovedStatus(
                roomId = roomId,
                approvedStatus = approvedStatus
            )
            success(true)
        } catch (e: Exception) {
            Timber.e(e)
            fail(e)
        }
    }

    override suspend fun updateMessagesAsChatRequest(
        roomId: Long?,
        isShowBlur: Boolean,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            val myUid = appSettings.readUID()
            if (roomId != null) {
                dataStore.messageDao().setBlurChatRequestAllMessages(
                    roomId,
                    creatorUid = myUid,
                    isShowBlur = isShowBlur
                )
            } else {
                val roomsChatRequests = dataStore.dialogDao().getAllNonApprovedDialogs()
                roomsChatRequests.forEach { room ->
                    dataStore.messageDao().setBlurChatRequestAllMessages(
                        roomId = room.roomId,
                        creatorUid = myUid,
                        isShowBlur = isShowBlur
                    )
                }
            }
            success(true)
        } catch (e: Exception) {
            Timber.e(e)
            fail(e)
        }
    }

    override suspend fun disableChatRequestImageBlur(
        message: MessageEntity?,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            dataStore.messageDao().updateChatRequestImageBlur(
                messageId = message?.msgId,
                isShowBlur = false
            )
            success(true)
        } catch (e: Exception) {
            Timber.e(e)
            fail(e)
        }
    }
}
