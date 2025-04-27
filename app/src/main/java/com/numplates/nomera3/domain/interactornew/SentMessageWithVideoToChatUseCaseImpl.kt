package com.numplates.nomera3.domain.interactornew

import com.google.gson.Gson
import com.meera.core.extensions.fromJson
import com.meera.core.preferences.AppSettings
import com.meera.db.DataStore
import com.meera.db.models.dialog.UserChat
import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.modules.chat.helpers.resendmessage.SendVideoMessageException
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_VIDEO_SEND
import com.numplates.nomera3.presentation.model.enums.ChatEventEnum
import com.numplates.nomera3.presentation.upload.AttachmentData
import com.numplates.nomera3.presentation.utils.parseUniquename
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject

interface SentMessageWithVideoToChatUseCase {
    suspend fun sendMessageByRoomId(roomId: Long, message: String, videoData: AttachmentData,
                                    isFail: Boolean,
                                    messageId: String? = null,
                                    parentId: String? = null)

    suspend fun sendMessageByUserId(userId: Long, message: String, videoData: AttachmentData,
                                    isFail: Boolean,
                                    messageId: String? = null,
                                    parentId: String? = null)

}

class SentMessageWithVideoToChatUseCaseImpl @Inject constructor(
        private val dataStore: DataStore,
        private val gson: Gson,
        private val appSettings: AppSettings,
        private val newMessageUseCase: SendNewMessageUseCase
) : SentMessageWithVideoToChatUseCase {

    override suspend fun sendMessageByRoomId(roomId: Long, message: String,
                                             videoData: AttachmentData,
                                             isFail: Boolean, messageId: String?, parentId: String?) {
        val payload = hashMapOf<String, Any>(
                "content" to message,
                "room_id" to roomId)

        val attachment = hashMapOf<String, Any>()
        attachment["url"] = videoData.mediaList[0]
        attachment["type"] = "video"
        val metadata = hashMapOf<Any, Any>()
        videoData.duration?.let { metadata["duration"] = it }
        videoData.isSilent?.let { metadata["is_silent"] = it }
        videoData.lowQuality?.let { metadata["low_quality"] = it }
        videoData.preview?.let { metadata["preview"] = it }
        videoData.ratio?.let { metadata["ratio"] = it }
        attachment["metadata"] = metadata
        payload["attachment"] = attachment

        sendChatMessage(payload, isFail, messageId, parentId)
    }

    override suspend fun sendMessageByUserId(userId: Long, message: String,
                                             videoData: AttachmentData,
                                             isFail: Boolean, messageId: String?, parentId: String?) {
        val payload = hashMapOf<String, Any>(
                "content" to message,
                "user_id" to userId,
                "type" to "dialog"
        )

        val attachment = hashMapOf<String, Any>()
        attachment["url"] = videoData.mediaList[0]
        attachment["type"] = "video"
        val metadata = hashMapOf<Any, Any>()
        videoData.duration?.let { metadata["duration"] = it }
        videoData.isSilent?.let { metadata["is_silent"] = it }
        videoData.lowQuality?.let { metadata["low_quality"] = it }
        videoData.preview?.let { metadata["preview"] = it }
        videoData.ratio?.let { metadata["ratio"] = it }
        attachment["metadata"] = metadata
        payload["attachment"] = attachment

        sendChatMessage(payload, isFail, messageId, parentId)
    }

    private suspend fun sendChatMessage(payload: HashMap<String, Any>,
                                        isFail: Boolean,
                                        existMessageId: String?, parentId: String? ) {
        val messageId = existMessageId ?: UUID.randomUUID().toString()
        var message = MessageEntity()
        val content = payload["content"] as String

        val myUid = appSettings.readUID()

        // Message for record to Db (if roomId exists)
        payload["room_id"]?.let { roomId ->
            Timber.d("RoomID exists")
            message = MessageEntity(
                msgId = messageId,
                roomId = roomId as Long,
                creator = UserChat(myUid),
                creatorUid = myUid,
                content = content,
                createdAt = System.currentTimeMillis()
            )
        }

        // Message for record to Db (if userId exists)
        payload["user_id"]?.let {
            Timber.d("UserId exists")
            message = MessageEntity(
                msgId = messageId,
                roomId = 0, // TODO: Anon BUG: Message not shown in List
                creator = UserChat(myUid),
                creatorUid = myUid,
                content = content,
                createdAt = System.currentTimeMillis()
            )
        }

        payload["id"] = messageId
        payload["user_type"] = "UserChat"

        // Save attachment data for Resend
        val messagePayload = gson.fromJson<MessageEntity>(gson.toJson(payload))
        message.attachment.type = messagePayload.attachment.type
        message.attachment.url = messagePayload.attachment.url
        message.attachment.metadata = messagePayload.attachment.metadata

        message.attachments = messagePayload.attachments

        message.resendImages = messagePayload.resendImages

        // Set message color scroll animation
        message.isServerMessage = false
        message.itemType = ITEM_TYPE_VIDEO_SEND
        message.eventCode = ChatEventEnum.VIDEO.state

        // Reply message
        parentId?.let { payload["parent_id"] = it }

        Timber.d("Save MESSAGE to Db before send to server: MSG: $message")

        sendMessageCoroutine(message, payload, isFail)
    }

    private suspend fun sendMessageCoroutine(message: MessageEntity,
                                             payload: HashMap<String, Any>, isFail: Boolean) {
        try {
            message.tagSpan = parseUniquename(message.content)
            insertToDb(message)
            if (isFail) throw Exception("Couldn't send message:$message")

            val result = newMessageUseCase.newMessage(payload)
            if (result.data != null) {
                updateMessageSentStatus(message, true)
                Timber.d("Success save message to Db rows: (PAYLOAD): $payload")
            } else {
                updateMessageSentStatus(message, false)
            }
        } catch (e: Exception) {
            updateMessageSentStatus(message, false)
            Timber.d(e)
            throw SendVideoMessageException("Couldn't send video message:$message", message.msgId)
        }
    }

    private fun insertToDb(message: MessageEntity): Long =
            dataStore.messageDao().insert(message)

    private fun updateMessageSentStatus(message: MessageEntity, isSent: Boolean) {
        message.sent = isSent
        dataStore.messageDao().update(message)
    }

}
