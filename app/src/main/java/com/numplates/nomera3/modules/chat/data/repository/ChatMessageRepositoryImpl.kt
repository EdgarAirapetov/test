package com.numplates.nomera3.modules.chat.data.repository

import android.net.Uri
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.meera.core.di.scopes.AppScope
import com.meera.core.extensions.fromJson
import com.meera.core.network.websocket.WebSocketMainChannel
import com.meera.core.preferences.AppSettings
import com.meera.core.utils.files.FileManager
import com.meera.core.utils.files.FileUtilsImpl
import com.meera.db.DataStore
import com.meera.db.models.dialog.LastMessage
import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.CHAT_VOICE_MESSAGES_PATH
import com.numplates.nomera3.HTTPS_SCHEME
import com.numplates.nomera3.HTTP_SCHEME
import com.numplates.nomera3.data.network.ApiFileStorage
import com.numplates.nomera3.data.network.Post
import com.numplates.nomera3.data.newmessenger.response.ResponseMessages
import com.numplates.nomera3.data.websocket.STATUS_OK
import com.numplates.nomera3.modules.chat.ChatPayloadKeys
import com.numplates.nomera3.modules.chat.data.mapper.MessengerEntityMapper
import com.numplates.nomera3.modules.chat.domain.ChatMessageRepository
import com.numplates.nomera3.modules.chat.domain.params.MessagePaginationDirection
import com.numplates.nomera3.modules.chat.domain.params.MessagePaginationUserType
import com.numplates.nomera3.modules.chat.helpers.sendmessage.models.MessageSendSuccessfullyEvent
import com.numplates.nomera3.modules.chat.helpers.sendmessage.models.SendMessageEvent
import com.numplates.nomera3.modules.chat.messages.data.api.MessagesApi
import com.numplates.nomera3.presentation.utils.makeEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.util.concurrent.CancellationException
import javax.inject.Inject

private const val DOWNLOAD_VOICE_MESSAGE_PROGRESS_COMPLETE = 100

@AppScope
class ChatMessageRepositoryImpl @Inject constructor(
    private val api: MessagesApi,
    private val dataStore: DataStore,
    private val gson: Gson,
    private val webSocketMainChannel: WebSocketMainChannel,
    private val messengerEntityMapper: MessengerEntityMapper,
    private val fileUtils: FileManager,
    private val fileApi: ApiFileStorage,
    private val appSettings: AppSettings,
) : ChatMessageRepository {

    private val _eventsFlow = MutableSharedFlow<SendMessageEvent>()

    override val eventsFlow
        get() = _eventsFlow as SharedFlow<SendMessageEvent>

    private val messageDao = dataStore.messageDao()

    override suspend fun getChatMessageListPosition(message: MessageEntity) =
        messageDao.getMessagePositionDb(roomId = message.roomId, createdAt = message.createdAt)

    override suspend fun getMessageById(messageId: String) =
        messageDao.getMessageByIdSuspend(messageId)

    override suspend fun updateMessageEditingStatus(messageId: String, isEditing: Boolean) {
        messageDao.updateProgressStatusByMessageId(messageId = messageId, showEditingProgress = isEditing)
    }

    override suspend fun updateMessageData(message: MessageEntity) {
        messageDao.updateSuspend(message)
    }

    override suspend fun getFileForContentUri(media: Uri, label: String) =
        fileUtils.getFileForContentUri(label, media)


    override suspend fun saveMessageIntoDb(
        message: MessageEntity,
        shouldEmmitEvent: Boolean
    ): Long {
        Timber.d("sendMessageBackground db")
        if (shouldEmmitEvent) _eventsFlow.emit(MessageSendSuccessfullyEvent(message))
        val dbMessage = dataStore.messageDao().getMessageByIdSuspend(message.msgId)
        return withContext(Dispatchers.IO) {
            dataStore.messageDao()
                .insert(message.copy(createdAt = dbMessage?.createdAt ?: message.createdAt))
        }
    }

    override fun updateLastMessage(
        message: MessageEntity,
        lastMessage: LastMessage
    ) {
        dataStore.dialogDao().apply {
            updateLastMessage(
                roomId = message.roomId,
                lastMessage = lastMessage,
                lastMessageSent = lastMessage.sent
            )
            updateDialogTime(
                roomId = message.roomId,
                updatedAt = message.createdAt
            )
        }
    }

    override fun updateMessageSentStatus(
        messageId: String,
        isSent: Boolean,
        isShowLoadingProgress: Boolean
    ) {
        dataStore.messageDao().updateSentStatus(messageId, isSent, isShowLoadingProgress)
    }

    override suspend fun updateBadgeStatus(
        roomId: Long,
        needToShowBadge: Boolean
    ) = withContext(Dispatchers.IO) {
        dataStore.dialogDao().updateBadgeStatus(
            needToShowUnreadBadge = needToShowBadge,
            roomId = roomId
        )
    }

    override suspend fun updateLastUnreadMessageTs(
        roomId: Long,
        timestamp: Long
    ): Int = withContext(Dispatchers.IO) {
        return@withContext dataStore.dialogDao().updateLastUnreadMessageTs(roomId, timestamp)
    }

    @Throws(RuntimeException::class)
    override suspend fun getMessages(
        roomId: Long,
        lastUpdatedAtMessages: Long,
        direction: MessagePaginationDirection
    ): List<MessageEntity> {
        val payload = hashMapOf(
            ChatPayloadKeys.ROOM_ID.key to roomId,
            ChatPayloadKeys.TS.key to lastUpdatedAtMessages,
            ChatPayloadKeys.DIRECTION.key to direction,
            ChatPayloadKeys.USER_TYPE.key to MessagePaginationUserType.USER_CHAT.paramValue
        )
        val socketResponse = webSocketMainChannel.pushGetMessagesSuspend(payload)
        when (socketResponse.payload[ChatPayloadKeys.SOCKET_STATUS.key]) {
            STATUS_OK -> {
                val response = socketResponse.payload.makeEntity<ResponseMessages>(gson).response
                if (response.error != null) {
                    throw RuntimeException("Server error socket get_messages: ${response.error}")
                } else {
                    return response.messages
                }
            }
            else -> throw RuntimeException("Internal server error socket get_messages")
        }
    }

    @Throws(RuntimeException::class)
    override suspend fun getMessagesAndInsertInDb(
        roomId: Long,
        timeStamp: Long,
        limit: Int,
        direction: MessagePaginationDirection,
        isRoomChatRequest: Boolean,
        needToShowUnreadMessagesSeparator: Boolean,
        isInitialRequest: Boolean,
        userType: String
    ): List<MessageEntity> {
        val response = api.getMessages(
            roomId = roomId,
            direction = direction.paramValue,
            ts = if (isInitialRequest) null else timeStamp,
            limit = limit,
            userType = userType
        )
        Timber.d("getMessagesAndInsertInDb: ${gson.toJson(response)}")
        if (response.data == null || response.err != null) {
            throw RuntimeException("Something went wrong. Server error: ${response.message}")
        } else {
            val messagesResponse = response.data.messages
                .map { msg -> messengerEntityMapper.mapBeforeInsertToDB(msg, isRoomChatRequest) }
                .mapVoiceMessageRecognizedText()
                .apply { firstUnreadOrNull()?.isShowUnreadDivider = needToShowUnreadMessagesSeparator }
            if (messagesResponse.isNotEmpty()) {
                val editedMessages = messagesResponse.filter { messageEntity ->
                    return@filter (messageEntity.attachment.metadata["post"] as? LinkedTreeMap<String, Any>)?.let {
                        val post = gson.fromJson<Post?>(it)
                        return@filter post?.editedAt != null
                    } ?: false
                }
                val notEditedMessages = messagesResponse - editedMessages.toSet()

                if (isContainMyReadMessages(messagesResponse)) {
                    dataStore.messageDao().insert(messagesResponse)
                } else {
                    dataStore.messageDao().insertWithoutReplace(notEditedMessages)
                    if (editedMessages.isNotEmpty()) {
                        dataStore.messageDao().insert(editedMessages)
                    }
                }
            }
            return messagesResponse
        }
    }

    @Throws(IOException::class)
    override fun downloadVoiceMessage(
        message: MessageEntity,
        externalFilesDir: File?
    ): Flow<Int> = callbackFlow {
        try {
            val voiceFileUrl = message.attachment.url
            val fileName = getFileNameFromPath(voiceFileUrl)
            val storageDir = File(externalFilesDir, "$CHAT_VOICE_MESSAGES_PATH/${message.roomId}")
            if (!storageDir.exists()) storageDir.mkdirs()

            val fileForSave = File(storageDir, fileName)
            if (fileForSave.exists()) {
                //Timber.e("Audio file ($fileForSave) already exists")
                cancel(CancellationException("Audio file ($fileForSave) already exists"))
                return@callbackFlow
            }

            if (isWebUrlScheme(voiceFileUrl)) {
                val response = fileApi.downloadFileFromUrl(voiceFileUrl)
                fileUtils.saveToDisk(storageDir, response, fileName, object :
                    FileUtilsImpl.DownloadFileProgressListener {
                    override fun onProgressUpdate(progress: Int) {
                        trySend(progress)
                        if (progress == 100) channel.close()
                    }
                })
            } else {
                fileUtils.copy(File(voiceFileUrl), fileForSave)
            }
        } catch (e: Exception) {
            Timber.e("ERROR download voice message file:$e")
            cancel(CancellationException("Download voice message file ERROR!"))
            throw IOException("Download voice message file ERROR!")
        }

    }.flowOn(Dispatchers.IO)

    @Throws(IOException::class)
    override fun downloadVoiceMessage(
        url: String,
        roomId: Long,
        externalFilesDir: File?
    ): Flow<Int> = callbackFlow {
        try {
            val fileName = getFileNameFromPath(url)
            val storageDir = File(externalFilesDir, "$CHAT_VOICE_MESSAGES_PATH/${roomId}")
            if (!storageDir.exists()) storageDir.mkdirs()

            val fileForSave = File(storageDir, fileName)
            if (fileForSave.exists()) {
                cancel(CancellationException("Audio file ($fileForSave) already exists"))
                return@callbackFlow
            }

            if (isWebUrlScheme(url)) {
                val response = fileApi.downloadFileFromUrl(url)
                fileUtils.saveToDisk(storageDir, response, fileName, object :
                    FileUtilsImpl.DownloadFileProgressListener {
                    override fun onProgressUpdate(progress: Int) {
                        trySend(progress)
                        if (progress == DOWNLOAD_VOICE_MESSAGE_PROGRESS_COMPLETE) channel.close()
                    }
                })
            } else {
                fileUtils.copy(File(url), fileForSave)
            }
        } catch (e: Exception) {
            Timber.e("ERROR download voice message file:$e")
            cancel(CancellationException("Download voice message file ERROR!"))
            throw IOException("Download voice message file ERROR!")
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun readMessageNetwork(roomId: Long, messageIds: List<String>): Boolean {
        val payload = hashMapOf(
            ChatPayloadKeys.ROOM_ID.key to roomId,
            ChatPayloadKeys.IDS.key to messageIds
        )
        val socketResponse = webSocketMainChannel.pushMessageReadCoroutine(payload)
        return when (socketResponse.payload[ChatPayloadKeys.SOCKET_STATUS.key]) {
            STATUS_OK -> true
            else -> false
        }
    }

    override suspend fun sendTyping(roomId: Long, type: String): Boolean = withContext(Dispatchers.IO) {
        val payload = hashMapOf(
            ChatPayloadKeys.ROOM_ID.key to roomId,
            ChatPayloadKeys.TYPING_TYPE.key to type
        )
        val socketResponse = webSocketMainChannel.pushTypingSuspend(payload)
        return@withContext when (socketResponse.payload[ChatPayloadKeys.SOCKET_STATUS.key]) {
            STATUS_OK -> true
            else -> false
        }
    }

    override suspend fun deleteMessageNetwork(
        roomId: Long,
        messageId: String,
        isBoth: Boolean
    ): Boolean = withContext(Dispatchers.IO) {
        val payload = hashMapOf(
            ChatPayloadKeys.ROOM_ID.key to roomId,
            ChatPayloadKeys.ID.key to messageId,
            ChatPayloadKeys.IS_BOTH.key to isBoth
        )
        val socketResponse = webSocketMainChannel.pushRemoveMessageSuspend(payload)
        return@withContext when (socketResponse.payload[ChatPayloadKeys.SOCKET_STATUS.key]) {
            STATUS_OK -> true
            else -> false
        }
    }

    override fun observeIncomingMessage(): Flow<MessageEntity> {
        return webSocketMainChannel.observeIncomingMessage().map { phoenixMessage ->
            phoenixMessage.payload.makeEntity<MessageEntity>(gson)
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun getResendProgressMessages(roomId: Long): List<MessageEntity>? = withContext(Dispatchers.IO) {
        return@withContext dataStore.messageDao().getResendProgressMessages(roomId)
    }

    private fun List<MessageEntity>.mapVoiceMessageRecognizedText(): List<MessageEntity> {
        return map { msg ->
            if (msg.attachment.audioRecognizedText.isNotEmpty()) {
                msg.isExpandedRecognizedText = false
            }
            msg
        }
    }

    private fun isContainMyReadMessages(messages: List<MessageEntity>): Boolean {
        val isContainReadMessages = messages.any { it.readed && it.creator?.userId == appSettings.readUID() }
        return messages.isNotEmpty() && isContainReadMessages
    }

    private fun getFileNameFromPath(attachmentUrl: String): String {
        val uri = Uri.parse(attachmentUrl)
        return uri.lastPathSegment ?: throw IOException("Url parse exception")
    }

    private fun isWebUrlScheme(path: String): Boolean {
        return when (Uri.parse(path).scheme) {
            HTTP_SCHEME, HTTPS_SCHEME -> true
            else -> false
        }
    }

    private fun List<MessageEntity>.firstUnreadOrNull(): MessageEntity? {
        val found = this.find { !it.readed }
        return if (this.isNotEmpty() && found != null) found else null
    }

}
