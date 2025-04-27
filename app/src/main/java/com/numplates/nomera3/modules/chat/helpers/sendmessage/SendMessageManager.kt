package com.numplates.nomera3.modules.chat.helpers.sendmessage

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.gson.Gson
import com.meera.application_api.media.MediaFileMetaDataDelegate
import com.meera.core.extensions.empty
import com.meera.core.extensions.fromJson
import com.meera.core.preferences.AppSettings
import com.meera.core.utils.graphics.isRotatedFromExif
import com.meera.db.models.dialog.LastMessage
import com.meera.db.models.dialog.UserChat
import com.meera.db.models.message.MessageAttachment
import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.HTTPS_SCHEME
import com.numplates.nomera3.HTTP_SCHEME
import com.numplates.nomera3.MEDIA_VIDEO
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.data.newmessenger.CHAT_ITEM_TYPE_MESSAGE
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_AUDIO
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_GIF
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_IMAGE
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_VIDEO
import com.numplates.nomera3.domain.interactornew.SendNewMessageUseCase
import com.numplates.nomera3.modules.chat.ChatPayloadKeys
import com.numplates.nomera3.modules.chat.ChatViewModel.Companion.USER_TYPE_USER_CHAT
import com.numplates.nomera3.modules.chat.domain.usecases.SaveMessageIntoDbUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.UpdateBadgeStatusUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.UpdateLastMessageUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.UpdateMessageSentStatusUseCase
import com.numplates.nomera3.modules.chat.helpers.UploadChatHelper
import com.numplates.nomera3.modules.chat.helpers.isGifUrl
import com.numplates.nomera3.modules.chat.helpers.resolveMessageType
import com.numplates.nomera3.modules.chat.helpers.sendmessage.models.SendMessageModel
import com.numplates.nomera3.modules.chat.helpers.sendmessage.models.SendMessageType
import com.numplates.nomera3.modules.chat.helpers.toParentMessage
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.entity.MediakeyboardFavoriteRecentUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.ui.entity.MediaPreviewType
import com.numplates.nomera3.modules.chat.messages.data.entity.SendMessageDto
import com.numplates.nomera3.presentation.model.enums.ChatEventEnum
import com.numplates.nomera3.presentation.utils.makeEntity
import com.numplates.nomera3.presentation.utils.parseUniquename
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.math.RoundingMode
import java.util.UUID
import javax.inject.Inject


typealias PAYLOAD_PARAM = ChatPayloadKeys
private const val DELAY_SEND_ACTIONS = 700L

/**
 * Точка входа для отправки всех типов сообщений
 */
class SendMessageManager @Inject constructor(
    private val appSettings: AppSettings,
    private val sendMessageUseCase: SendNewMessageUseCase,
    private val saveMessageDbUseCase: SaveMessageIntoDbUseCase,
    private val updateMessageSentStatusUseCase: UpdateMessageSentStatusUseCase,
    private val updateLastMessageUseCase: UpdateLastMessageUseCase,
    private val updateBadgeStatusUseCase: UpdateBadgeStatusUseCase,
    private val uploadHelper: UploadChatHelper,
    private val gson: Gson,
    val metaDataDelegate: MediaFileMetaDataDelegate,
    val appContext: Context
) {

    private var actionCallback: SendMessageInteractionCallback? = null

    fun addInteractionCallback(callback: SendMessageInteractionCallback) {
        this.actionCallback = callback
    }

    suspend fun sendMessage(sendData: SendMessageModel) = withContext(Dispatchers.IO) {
        when (sendData.sendType) {
            SendMessageType.SIMPLE_MESSAGE_USER_ID -> sendSimpleMessageByUserId(sendData)
            SendMessageType.SIMPLE_MESSAGE_ROOM_ID -> sendSimpleMessageByRoomId(sendData)
            SendMessageType.VOICE_MESSAGE_ROOM_ID -> sendVoiceMessageByRoomId(sendData)
            SendMessageType.VOICE_MESSAGE_USER_ID -> sendVoiceMessageByUserId(sendData)
            SendMessageType.VIDEO_MESSAGE_ROOM_ID,
            SendMessageType.VIDEO_MESSAGE_USER_ID -> sendVideoMessage(sendData)
            else -> Unit
        }
    }

    /**
     * Only for sending test fake messages
     */
    suspend fun sendOnlyNetworkMessage(roomId: Long, messageText: String) = withContext(Dispatchers.IO) {
        val payload = hashMapOf<String, Any>(
            ChatPayloadKeys.CONTENT.key to messageText,
            ChatPayloadKeys.ROOM_ID.key to roomId
        )
        val messageId = UUID.randomUUID().toString()
        payload[ChatPayloadKeys.ID.key] = messageId
        runCatching {
            val response = sendMessageUseCase.newMessage(payload)
            if (response.data != null) {
                Timber.d("SUCCESSFULLY Send message:$messageText")
            } else {
                Timber.e("FAIL Send message:$messageText")
            }
        }.onFailure {
            Timber.e("FAIL Send message:$messageText ERROR:$it")
        }
    }

    private suspend fun sendSimpleMessageByUserId(sendData: SendMessageModel) {
        if (sendData.userId == null) return
        val text = sendData.messageText ?: String.empty()
        val payload = hashMapOf<String, Any>(
            PAYLOAD_PARAM.CONTENT.key to text,
            PAYLOAD_PARAM.USER_ID.key to sendData.userId,
            PAYLOAD_PARAM.ROOM_TYPE.key to sendData.roomType
        )
        val favorite = sendData.favoriteRecent
        val images = sendData.imageData?.images
        when {
            favorite != null -> {
                prepareMessageWithFavorite(sendData, payload)
            }
            images.isNullOrEmpty() -> {
                prepareAndSendSimpleMessage(
                    payloadMap = payload,
                    parentMessage = sendData.parentMessage
                )
            }
            !images.isNullOrEmpty() -> {
                sendMediaMessage(sendData, payload)
            }
        }
    }

    private suspend fun sendSimpleMessageByRoomId(sendData: SendMessageModel) {
        if (sendData.roomId == null) return
        val text = sendData.messageText ?: String.empty()
        val payload = hashMapOf<String, Any>(
            PAYLOAD_PARAM.CONTENT.key to text,
            PAYLOAD_PARAM.ROOM_ID.key to sendData.roomId,
            PAYLOAD_PARAM.ROOM_TYPE.key to sendData.roomType
        )
        val favorite = sendData.favoriteRecent
        val images = sendData.imageData?.images
        when {
            favorite != null -> {
                prepareMessageWithFavorite(sendData, payload)
            }
            images.isNullOrEmpty() -> {
                prepareAndSendSimpleMessage(
                    payloadMap = payload,
                    parentMessage = sendData.parentMessage
                )
            } else -> {
                sendMediaMessage(sendData, payload)
            }
        }
    }

    private suspend fun sendVoiceMessageByUserId(sendData: SendMessageModel) {
        sendData.userId?.let { userId ->
            uploadVoiceMessageBeforeSend(
                sendData = sendData,
                payload = hashMapOf(
                    PAYLOAD_PARAM.USER_ID.key to userId,
                    PAYLOAD_PARAM.ROOM_TYPE.key to sendData.roomType
                )
            )
        }
    }

    private suspend fun sendVoiceMessageByRoomId(sendData: SendMessageModel) {
        sendData.roomId?.let { roomId ->
            uploadVoiceMessageBeforeSend(
                sendData = sendData,
                payload = hashMapOf(
                    PAYLOAD_PARAM.ROOM_ID.key to roomId,
                    PAYLOAD_PARAM.ROOM_TYPE.key to sendData.roomType
                )
            )
        }
    }

    private suspend fun sendMediaMessage(
        sendData: SendMessageModel,
        payload: HashMap<String, Any>
    ) {
        val imagePaths: List<String> = sendData.imageData?.images ?: return
        val images: List<Uri> = imagePaths.map { Uri.parse(it) }

        val networkImages = images.filter { uri -> uri.scheme == HTTPS_SCHEME || uri.scheme == HTTP_SCHEME }
        val localImages = images.filter { uri -> uri.scheme != HTTPS_SCHEME && uri.scheme != HTTP_SCHEME }

        if (networkImages.isNotEmpty()) {
            sendNetworkImages(networkImages, sendData, payload)
        }

        if (localImages.isNotEmpty()) {
            sendLocalImages(localImages, sendData, payload)
        }
    }

    private suspend fun sendNetworkImages(
        networkImages: List<Uri>,
        sendData: SendMessageModel,
        payload: HashMap<String, Any>
    ) {
        val attachments = generateNetworkPayloadForAttachments(networkImages, sendData.imageData?.gifAspectRatio)
        if (attachments.size == 1) {
            payload[PAYLOAD_PARAM.ATTACHMENT.key] = attachments[0]
        } else {
            payload[PAYLOAD_PARAM.ATTACHMENTS.key] = attachments
        }
        prepareAndSendSimpleMessage(
            payloadMap = payload,
            parentMessage = sendData.parentMessage,
            eventType = ChatEventEnum.IMAGE.state
        )
    }

    private suspend fun sendLocalImages(
        localImages: List<Uri>,
        sendData: SendMessageModel,
        payload: HashMap<String, Any>
    ) {
        val (dbMessage, updatedPayload) = sendDbImageMessage(localImages, sendData, payload)
        actionCallback?.onInsertDbMessage(messageId = dbMessage.msgId)

        val uploadImagesResult = uploadHelper.uploadImages(
            images = localImages,
            payload = updatedPayload,
        )

        sendMessageWithUploadedImage(uploadImagesResult, payload, dbMessage)
    }

    private suspend fun sendVideoMessage(sendData: SendMessageModel) {
        val (dbMessage, dbMessagePayload) = sendOnlyDbVideoMessage(sendData)
        actionCallback?.onInsertDbMessage(messageId = dbMessage.msgId)

        val videoPath = requireNotNull(sendData.videoData?.videoPath)
        val uploadVideoResult = uploadHelper.uploadVideo(
            videoUri = Uri.parse(videoPath),
            payload = dbMessagePayload
        )
        sendMessageWithUploadedVideo(
            uploadVideoResult = uploadVideoResult,
            payload = dbMessagePayload,
            currentMessage = dbMessage
        )
    }

    private suspend fun sendOnlyDbVideoMessage(
        sendData: SendMessageModel
    ): Pair<MessageEntity, HashMap<String, Any>> {
        val localVideoAttachmentPayload = localVideoGetPayload(sendData)
        val  (message, payload) = prepareMessageData(
            payloadMap = localVideoAttachmentPayload,
            parentMessage = sendData.parentMessage,
            eventType = ChatEventEnum.VIDEO.state
        )
        val dbMessage = sendMessageDb(message)
        return Pair(first = dbMessage, second = payload)
    }

    private fun localVideoGetPayload(sendData: SendMessageModel): HashMap<String, Any> {
        val message = sendData.messageText ?: String.empty()
        val roomId = sendData.roomId ?: 0L
        val userId = sendData.userId ?: 0L
        val videoPath = requireNotNull(sendData.videoData?.videoPath)
        val videoMetadata = metaDataDelegate.getVideoMetadata(Uri.parse(videoPath))

        val payload = hashMapOf<String, Any>()
        when(sendData.sendType) {
            SendMessageType.VIDEO_MESSAGE_ROOM_ID ->  payload[PAYLOAD_PARAM.ROOM_ID.key] = roomId
            SendMessageType.VIDEO_MESSAGE_USER_ID -> payload[PAYLOAD_PARAM.USER_ID.key] = userId
            else -> Unit
        }

        payload[PAYLOAD_PARAM.CONTENT.key] = message

        val attachment = hashMapOf<String, Any>()
        attachment[PAYLOAD_PARAM.ATTACHMENT_URL.key] = videoPath
        attachment[PAYLOAD_PARAM.TYPING_TYPE.key] = MEDIA_VIDEO
        val metadata = hashMapOf<Any, Any>()
        metadata[PAYLOAD_PARAM.ATTACHMENT_METADATA_DURATION.key] = videoMetadata?.duration?.div(1000) ?: 0
        metadata[PAYLOAD_PARAM.ATTACHMENT_METADATA_PREVIEW.key] = videoPath
        videoMetadata?.let { meta ->
            val aspectRatio = meta.width.toDouble() / meta.height
            val ratio = aspectRatio.toBigDecimal().setScale(2, RoundingMode.UP)
            metadata[PAYLOAD_PARAM.ATTACHMENT_RATIO.key] = ratio
        }
        attachment[PAYLOAD_PARAM.ATTACHMENT_METADATA.key] = metadata
        payload[PAYLOAD_PARAM.ATTACHMENT.key] = attachment
        return payload
    }

    private suspend fun prepareMessageWithFavorite(sendData: SendMessageModel, payload: HashMap<String, Any>) {
        val favorite = sendData.favoriteRecent ?: return
        val attachment = hashMapOf<String, Any>()

        val sendingSticker = favorite.type == MediakeyboardFavoriteRecentUiModel.FavoriteRecentType.STICKER
        val favoriteRecentType = sendData.favoriteRecentType
        if (sendingSticker && favorite.stickerId != null) {
            attachment[PAYLOAD_PARAM.ATTACHMENT_STICKER.key] = favorite.stickerId
        }
        when (favoriteRecentType) {
            MediaPreviewType.FAVORITE -> attachment[PAYLOAD_PARAM.ATTACHMENT_FAVORITE.key] = favorite.id
            MediaPreviewType.RECENT -> attachment[PAYLOAD_PARAM.ATTACHMENT_RECENT.key] = favorite.id
            else -> Unit
        }
        attachment[PAYLOAD_PARAM.ATTACHMENT_URL.key] = favorite.url
        favorite.lottieUrl?.let { attachment[PAYLOAD_PARAM.ATTACHMENT_LOTTIE_URL.key] = it }
        favorite.webpUrl?.let { attachment[PAYLOAD_PARAM.ATTACHMENT_WEBP_URL.key] = it }
        attachment[PAYLOAD_PARAM.ATTACHMENT_TYPE.key] = favorite.type.value
        val metadata = hashMapOf<Any, Any>()
        if (favorite.ratio != null) {
            metadata[PAYLOAD_PARAM.ATTACHMENT_METADATA_RATIO.key] = favorite.ratio
        }
        if (favorite.duration != null) {
            metadata[PAYLOAD_PARAM.ATTACHMENT_METADATA_DURATION.key] = favorite.duration
            metadata[PAYLOAD_PARAM.ATTACHMENT_METADATA_PREVIEW.key] = favorite.preview
        }
        if (favorite.emoji != null) {
            metadata[PAYLOAD_PARAM.ATTACHMENT_EMOJI.key] = favorite.emoji
        }
        if (metadata.isNotEmpty()) {
            attachment[PAYLOAD_PARAM.ATTACHMENT_METADATA.key] = metadata
        }

        payload[PAYLOAD_PARAM.ATTACHMENT.key] = attachment

        prepareAndSendSimpleMessage(
            payloadMap = payload,
            parentMessage = sendData.parentMessage
        )
    }

    private suspend fun sendDbImageMessage(
        localImages: List<Uri>,
        sendData: SendMessageModel,
        payload: HashMap<String, Any>
    ): Pair<MessageEntity, HashMap<String, Any>> {
        val localImagesAttachmentPayload = localImagesGetPayload(localImages)
        val updPayload = payload.toTotalAttachmentsPayload(localImagesAttachmentPayload)
        val (message, _) = prepareMessageData(
            payloadMap = updPayload,
            parentMessage = sendData.parentMessage,
            eventType = ChatEventEnum.IMAGE.state,
        )
        val dbMessage = sendMessageDb(message)
        return Pair(first = dbMessage, second = updPayload)
    }

    @Throws(SendMessageException::class)
    private suspend fun sendMessageWithUploadedImage(
        uploadImagesResult: UploadChatHelper.ResultUploadImages,
        payload: HashMap<String, Any>,
        currentMessage: MessageEntity
    ) {
        if (uploadImagesResult.successResult != null) {
            val payloadWithAttachments = payload.toTotalAttachmentsPayload(uploadImagesResult.successResult)
            handleUploadMediaSendResult(payloadWithAttachments, currentMessage)
        } else {
            handleErrorSendMessage(currentMessage)
            throw SendMessageException("Fail Send image message")
        }
    }

    @Throws(SendMessageException::class)
    private suspend fun sendMessageWithUploadedVideo(
        uploadVideoResult: UploadChatHelper.ResultUploadVideo,
        payload: HashMap<String, Any>,
        currentMessage: MessageEntity
    ) {
        if (uploadVideoResult.successResult != null) {
            handleUploadMediaSendResult(payload, currentMessage)
        } else {
            handleErrorSendMessage(currentMessage)
            throw SendMessageException("Fail Send VIDEO message")
        }
    }

    private suspend fun handleUploadMediaSendResult(
        payload: HashMap<String, Any>,
        currentMessage: MessageEntity
    ) {
        val sendResult = sendMessageNetwork(payload)
        if (sendResult.isSuccess) {
            val updatedAttachmentMessage = gson.fromJson<MessageEntity>(payload)
            val updMessage = currentMessage.copy(
                roomId = sendResult.getOrDefault(0),
                attachment = updatedAttachmentMessage.attachment
            )
            handleSuccessSendMessage(updMessage, payload)
        } else {
            handleErrorSendMessage(currentMessage)
            throw SendMessageException("Fail Send image VIDEO")
        }
    }

    private fun HashMap<String, Any>.toTotalAttachmentsPayload(
        attachments: List<HashMap<String, Any?>>
    ): HashMap<String, Any> {
        if (attachments.size == 1) {
            this[PAYLOAD_PARAM.ATTACHMENT.key] = attachments[0]
        } else {
            this[PAYLOAD_PARAM.ATTACHMENTS.key] = attachments
        }
        return this
    }

    private fun generateNetworkPayloadForAttachments(
        images: List<Uri>,
        aspectRatio: Double?
    ): List<HashMap<String, Any?>> {
        val resAttach = mutableListOf<HashMap<String, Any?>>()
        images.forEach { uri ->
            val attachment = hashMapOf<String, Any?>()
            attachment[PAYLOAD_PARAM.ATTACHMENT_URL.key] = uri.toString()
            attachment[PAYLOAD_PARAM.ATTACHMENT_TYPE.key] =
                if (uri.toString()?.isGifUrl() == true) TYPING_TYPE_GIF
                else TYPING_TYPE_IMAGE
            val metadata = hashMapOf<Any, Any?>()
            metadata[PAYLOAD_PARAM.ATTACHMENT_RATIO.key] = aspectRatio ?: 0.0
            attachment[PAYLOAD_PARAM.ATTACHMENT_METADATA.key] = metadata
            resAttach.add(attachment)
        }
        return resAttach
    }

    private fun localImagesGetPayload(images: List<Uri>): List<HashMap<String, Any?>> {
        val resAttach = mutableListOf<HashMap<String, Any?>>()
        images.forEach { imageUri ->
            val aspect = calculateAspectRatio(imageUri)

            val attachment = hashMapOf<String, Any?>()
            attachment[PAYLOAD_PARAM.ATTACHMENT_URL.key] = imageUri.toString()
            attachment[PAYLOAD_PARAM.ATTACHMENT_TYPE.key] =
                if (imageUri.toString().isGifUrl()) TYPING_TYPE_GIF else TYPING_TYPE_IMAGE
            val metadata = hashMapOf<Any, Any?>()
            metadata[PAYLOAD_PARAM.ATTACHMENT_RATIO.key] = aspect
            attachment[PAYLOAD_PARAM.ATTACHMENT_METADATA.key] = metadata
            resAttach.add(attachment)
        }
        return resAttach
    }

    private fun calculateAspectRatio(uri: Uri): Double {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(File(uri.path).absolutePath, options)
        val height = options.outHeight
        val width = options.outWidth
        val isRotated = isRotatedFromExif(uri.path)
        val aspectRatio = if (isRotated != null && isRotated) {
            height.toDouble() / width
        } else {
            width.toDouble() / height
        }
        return aspectRatio.toBigDecimal().setScale(2, RoundingMode.UP).toDouble()
    }

    private suspend fun uploadVoiceMessageBeforeSend(
        sendData: SendMessageModel,
        payload: HashMap<String, Any>,
    ) {
        val audioPath = sendData.voiceData?.audioPath ?: return
        val amplitudes = sendData.voiceData.amplitudes ?: return
        val duration = sendData.voiceData.durationSec

        uploadHelper.uploadVoiceMessage(
            audioPath = audioPath,
            amplitudes = amplitudes,
            duration = duration,
            payload = payload,
            successUpload = { voicePayload ->
                prepareAndSendSimpleMessage(
                    payloadMap = voicePayload,
                    parentMessage = sendData.parentMessage,
                    eventType = ChatEventEnum.AUDIO.state
                )
            },
            failureUpload = {
                Timber.e("FAIL Upload voice message")
                throw SendMessageException("Fail upload file voice message")
            }
        )
    }



    private suspend fun prepareAndSendSimpleMessage(
        payloadMap: HashMap<String, Any>,
        parentMessage: MessageEntity?,
        eventType: Int = ChatEventEnum.TEXT.state
    ) {
        val  (message, payload) = prepareMessageData(payloadMap, parentMessage, eventType)
        insertDbMessageAndSendServer(
            message = message,
            payload = payload
        )
    }

    private suspend fun prepareMessageData(
        payloadMap: HashMap<String, Any>,
        parentMessage: MessageEntity?,
        eventType: Int = ChatEventEnum.TEXT.state
    ): Pair<MessageEntity, HashMap<String, Any>> {
        val messageId = UUID.randomUUID()
        val ownUid = appSettings.readUID()
        val ownUserName = appSettings.userName.get()
        val message = MessageEntity(
            msgId = messageId.toString(),
            roomId = (payloadMap[PAYLOAD_PARAM.ROOM_ID.key] as? Long) ?: 0L,
            creator = UserChat(
                userId = ownUid,
                name = ownUserName
            ),
            creatorUid = ownUid,
            content = (payloadMap[PAYLOAD_PARAM.CONTENT.key] as? String) ?: String.empty(),
            createdAt = System.currentTimeMillis()
        )

        payloadMap[PAYLOAD_PARAM.ID.key] = messageId
        payloadMap[PAYLOAD_PARAM.USER_TYPE.key] = USER_TYPE_USER_CHAT
        message.type = CHAT_ITEM_TYPE_MESSAGE
        message.eventCode = resolveMessageEventCode(payloadMap, eventType)

        val payload = payloadMap.makeEntity<MessageEntity>(gson)
        message.apply {
            attachment.type = payload.attachment.type
            attachment.url = payload.attachment.url
            attachment.lottieUrl = payload.attachment.lottieUrl
            attachment.webpUrl = payload.attachment.webpUrl
            attachment.metadata = payload.attachment.metadata
            attachments = payload.attachments
            resendImages = payload.resendImages
            parent = parentMessage
            isServerMessage = false
            tagSpan = parseUniquename(message.content)
            isShowLoadingProgress = resolveShouldShowLoadingProgress(eventCode)
        }
        parentMessage?.msgId?.let { payloadMap[PAYLOAD_PARAM.PARENT_ID.key] = it }
        message.parentMessage = parentMessage?.toParentMessage()

        return Pair(first = message, second = payloadMap)
    }

    private fun resolveShouldShowLoadingProgress(eventCode: Int?): Boolean {
        return when(eventCode) {
            ChatEventEnum.IMAGE.state,
            ChatEventEnum.VIDEO.state,
            ChatEventEnum.LIST.state -> true
            else -> false
        }
    }

    private fun resolveMessageEventCode(payload: HashMap<String, Any>, defaultEventType: Int): Int {
        return when {
            payload.contains(PAYLOAD_PARAM.ATTACHMENT.key) -> getAttachmentEventCode(payload)
            payload.contains(PAYLOAD_PARAM.ATTACHMENTS.key) -> ChatEventEnum.LIST.state
            else -> defaultEventType
        }
    }

    private fun getAttachmentEventCode(payload: HashMap<String, Any>): Int {
        val attachmentPayload: HashMap<String, Any> =
            payload[PAYLOAD_PARAM.ATTACHMENT.key] as HashMap<String, Any>
        val attachment = gson.fromJson<MessageAttachment>(attachmentPayload)
        return when(attachment.type) {
            TYPING_TYPE_IMAGE -> ChatEventEnum.IMAGE.state
            TYPING_TYPE_VIDEO -> ChatEventEnum.VIDEO.state
            TYPING_TYPE_GIF -> ChatEventEnum.GIF.state
            TYPING_TYPE_AUDIO -> ChatEventEnum.AUDIO.state
            else -> ChatEventEnum.OTHER.state
        }
    }

    private suspend fun insertDbMessageAndSendServer(
        message: MessageEntity,
        payload: HashMap<String, Any>
    ) {
        val resultRoomId = sendMessageNetwork(payload)
        val updatedMessage = message.copy(roomId = resultRoomId.getOrDefault(message.roomId))
        val dbMessageWithItemType = sendMessageDb(updatedMessage)
        if (resultRoomId.isSuccess) {
            handleSuccessSendMessage(dbMessageWithItemType, payload)
        } else {
            Timber.e("CHAT_LOG Fail send message:${resultRoomId.exceptionOrNull()}")
            handleErrorSendMessage(dbMessageWithItemType)
        }
    }

    private suspend fun sendMessageDb(message: MessageEntity): MessageEntity {
        try {
            val messageWithItemType = message.copy(
                itemType = resolveMessageType(
                    creatorId = message.creator?.userId,
                    attachment = message.attachment,
                    attachments = message.attachments,
                    deleted = message.deleted,
                    eventCode = message.eventCode,
                    type = message.type,
                    myUid = appSettings.readUID()
                )
            )
            val insertedRow = saveMessageDbUseCase.invoke(messageWithItemType)
            return if (insertedRow > 0) {
                messageWithItemType
            } else {
                throw IOException("Can't insert message to Db")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw IOException("Can't insert message to Db:$e")
        }
    }

    private suspend fun sendMessageNetwork(payload: HashMap<String, Any>): Result<Long> {
        return try {
            val response = sendMessageUseCase.newMessage(payload)
            if (response.data != null) {
                val roomId = getRoomIdFromMessageResponse(response)
                Result.success(roomId)
            } else {
                Result.failure(IOException("Can't send message"))
            }
        } catch (e: Exception) {
            Timber.e("Fail send message:${e.message}")
            e.printStackTrace()
            Result.failure(IOException("Can't send message"))
        }
    }

    private fun getRoomIdFromMessageResponse(response: ResponseWrapper<Any>): Long {
        val json = gson.toJson(response.data)
        val result = json.makeEntity<SendMessageDto>(gson)
        return result.roomId
    }

    private suspend fun handleSuccessSendMessage(
        message: MessageEntity,
        payload: HashMap<String, Any>
    ) {
        actionCallback?.onActionSendMessage(
            messageId = message.msgId,
            isSentError = false,
            resultMessage = String.empty()
        )
        delay(DELAY_SEND_ACTIONS)
        actionCallback?.onSuccessSendMessage(
            roomId = message.roomId,
            guestId = (payload[PAYLOAD_PARAM.USER_ID.key] as? Long) ?: 0L,
            chatType = (payload[PAYLOAD_PARAM.ROOM_TYPE.key] as? String).orEmpty()
        )

        updateMessageDbData(message = message, isSent = true)
    }

    private suspend fun handleErrorSendMessage(message: MessageEntity) {
        message.tagSpan = parseUniquename(message.content)
        message.isResendProgress = false
        updateMessageDbData(message = message, isSent = false)
        setBadgeUnreadDialog(roomId = message.roomId, needToShowBadge = true)
        updateLastMessageNotSend(message)
        actionCallback?.onActionSendMessage(
            messageId = message.msgId,
            isSentError = true,
            resultMessage = String.empty()
        )
        delay(DELAY_SEND_ACTIONS)
        throw SendMessageException("Fail send server message")
    }

    private suspend fun setBadgeUnreadDialog(
        roomId: Long?,
        needToShowBadge: Boolean
    ) {
        runCatching {
            updateBadgeStatusUseCase.invoke(
                roomId = roomId,
                needToShowBadge = needToShowBadge
            )
        }.onFailure { Timber.e(it) }
    }

    private fun updateLastMessageNotSend(message: MessageEntity) {
        val lastMessage = LastMessage(
            message.msgId,
            message.content,
            message.type,
            message.attachment,
            message.attachments,
            message.eventCode,
            message.metadata,
            message.creator ?: UserChat(),
            message.createdAt,
            message.updatedAt,
            message.deleted,
            sent = message.sent
        )
        runCatching {
            updateLastMessageUseCase.invoke(
                message = message,
                lastMessage = lastMessage
            )
        }.onFailure { Timber.e(it) }
    }

    private suspend fun updateMessageDbData(
        message: MessageEntity,
        isSent: Boolean
    ) {
        runCatching {
            updateMessageSentStatusUseCase.invoke(
                messageId = message.msgId,
                isSent = isSent
            )
        }.onFailure { Timber.e(it) }
    }

    companion object {
        const val UNREAD_THRESHOLD = 50
    }
}
