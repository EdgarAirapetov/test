package com.numplates.nomera3.modules.chat.helpers.resendmessage

import android.net.Uri
import com.meera.core.extensions.empty
import com.meera.core.utils.files.FileManager
import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.CHAT_VOICE_MESSAGES_PATH
import com.numplates.nomera3.CHAT_VOICE_MESSAGE_EXTENSION
import com.numplates.nomera3.HTTP_SCHEME
import com.numplates.nomera3.MEDIA_VIDEO
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_AUDIO
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_GIF
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_IMAGE
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_VIDEO
import com.numplates.nomera3.modules.chat.helpers.isGiphyUrl
import com.numplates.nomera3.modules.chat.helpers.sendmessage.PAYLOAD_PARAM
import com.numplates.nomera3.modules.fileuploads.domain.model.ChatAttachmentPartialUploadModel
import com.numplates.nomera3.presentation.upload.AttachmentData
import com.numplates.nomera3.presentation.upload.SendMessageWithVideoToChatWorkerData
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException

open class BaseResendTask(
    private val dpnd: TaskDependencies,
    private val resultCallback: IResendResultCallback,
    private val fileManager: FileManager
) {

    /**
     * Входня точка переотправки любого типа сообщений
     */
    suspend fun resendMessageCommon(msg: MessageEntity) {
        resultCallback.onProgressResend(msg)

        val payload = hashMapOf<String, Any>(
            "id" to msg.msgId,
            "room_id" to msg.roomId,
            "content" to msg.content,
            "user_type" to "UserChat"
        )
        msg.parentMessage?.parentId?.let { parentId ->
            payload.put("parent_id", parentId)
        }

        if (msg.attachment.type.isNotEmpty() || msg.resendImages.isNotEmpty()) {
            if (msg.roomId > 0) {
                val resendUri = msg.resendImages
                val fileUri = msg.attachment.url
                when (msg.attachment.type) {
                    TYPING_TYPE_IMAGE -> resendImageMessage(msg, payload, resendUri, fileUri)
                    TYPING_TYPE_AUDIO -> resendVoiceMessage(msg, payload)
                    TYPING_TYPE_VIDEO -> resendVideoMessage(msg, payload)
                    else -> {
                        // проверяем есть ли что-то на отправку из атачмента
                        if (fileUri.isNotEmpty() || !resendUri.isNullOrEmpty()) {
                            resendImageMessage(msg, payload, resendUri, fileUri)
                        }
                    }
                }
            }
        } else {
            // Отправка простого текстового сообщения
            resendMessage(msg, payload)
        }
    }

    @Suppress("detekt:SwallowedException")
    private suspend fun resendVideoMessage(
        message: MessageEntity,
        payload: HashMap<String, Any>
    ) {
        // Prepare data
        val data = SendMessageWithVideoToChatWorkerData(
            roomId = message.roomId,
            userId = message.creator?.userId,
            mediaList = arrayOf(message.attachment.url),
            message = message.content,
            parentId = message.parentId
        )
        try {
            val preparedList = data.mediaList.map {
                Uri.fromFile(File(it))
            }

            val existsFiles = mutableListOf<Uri>()
            getExistFiles(message, payload, preparedList) { exists ->
                existsFiles.addAll(exists)
            }

            if (existsFiles.isNotEmpty()) {
                val attachment = dpnd.partialUploadChatVideoUseCase.invoke(
                    fileToUpload = File(data.mediaList[0])
                )
                val mediaLinksFromServer = getMediaLinks(attachment)
                payload[PAYLOAD_PARAM.ATTACHMENT.key] = prepareVideoMessageBeforeSend(mediaLinksFromServer)
                resendMessage(message, payload)
                fileManager.deleteFile(preparedList[0].path ?: String.empty())
            }
        } catch (e: Exception) {
            dpnd.dataStore.dialogDao().updateBadgeStatus(true, message.roomId)
            Timber.e("ERROR Upload video file:${e.message}")
            e.printStackTrace()
            resultCallback.onFailResend(message)
            throw Exception("ERROR Upload video file:${e.message}")
        }
    }

    private fun prepareVideoMessageBeforeSend(videoData: AttachmentData): HashMap<String, Any> {
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
        return attachment
    }

    private fun getMediaLinks(response: ChatAttachmentPartialUploadModel): AttachmentData {
        try {
            val imageArray = arrayListOf<String>()
            if (!response.url.isNullOrBlank()) imageArray.add(response.url)
            if (imageArray.isEmpty()) return AttachmentData()

            val responseMetadata = response.metadata
            return AttachmentData().apply {
                duration = responseMetadata?.duration
                isSilent = responseMetadata?.isSilent
                lowQuality = responseMetadata?.lowQuality
                preview = responseMetadata?.preview
                ratio = responseMetadata?.ratio
                mediaList = imageArray
                type = MEDIA_VIDEO
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return AttachmentData()
        }
    }

    private suspend fun resendVoiceMessage(
        message: MessageEntity,
        payload: HashMap<String, Any>
    ) {
        val waveForm = message.attachment.metadata["wave_form"] as? List<Int> ?: listOf()
        // Convert Float to Int to Valid request
        val intForm = waveForm.map { it.toInt() }

        if (!message.attachment.url.startsWith(HTTP_SCHEME)) {
            uploadVoiceMessage(
                message,
                intForm,
                payload,
                {
                    // Success
                    resendMessage(message, payload)
                },
                {
                    // Fail
                    resultCallback.onFailResend(message)
                    throw Exception("Resend voice message failed.")
                })
        } else {
            payload["attachment"] = prepareVoiceMessageBeforeSend(message, intForm)
            resendMessage(message, payload)
        }
    }

    /**
     * Загрузка файла голосового сообщения на storage сервер
     */
    private suspend fun uploadVoiceMessage(
        message: MessageEntity,
        listOfAmplitudes: List<Int>,
        payload: HashMap<String, Any>,
        successUpload: suspend () -> Any,
        failureUpload: () -> Any
    ) {
        try {
            val audioPath = message.attachment.url
            dpnd.sendVoiceMessageUseCase.setParams(audioPath, listOfAmplitudes)
            val response = dpnd.sendVoiceMessageUseCase.sendVoiceMessage()

            if (response?.data != null) {
                // Success sent audio message
                message.attachment.url = response.data.link ?: String.empty()
                payload["attachment"] = prepareVoiceMessageBeforeSend(message, listOfAmplitudes)
                successUpload.invoke()
            } else {
                // Error sent audio message
                resultCallback.onFailResend(message)
                throw Exception("Voice message data null")
            }
        } catch (e: FileNotFoundException) {
            updateBadgeStatusAndLog(e, message)
            handleFileNotFoundVoiceMessage(message)
        } catch (e: Exception) {
            updateBadgeStatusAndLog(e, message)
            failureUpload.invoke()
            throw Exception("Upload voice message failed")
        }
    }

    private fun updateBadgeStatusAndLog(e: Exception, message: MessageEntity) {
        Timber.e("Network error UPLOAD:${e.message}")
        dpnd.dataStore.dialogDao().updateBadgeStatus(true, message.roomId)
    }

    private fun handleFileNotFoundVoiceMessage(message: MessageEntity) {
        val dir = dpnd.appContext.getExternalFilesDir(null)
        val fileName = getFileNameFromPath(message.attachment.url)
        val localFile = File(
            dir, "$CHAT_VOICE_MESSAGES_PATH/${message.roomId}/" +
                "$fileName$CHAT_VOICE_MESSAGE_EXTENSION"
        )
        if (localFile.exists()) {
            localFile.delete()
        }
        resultCallback.onDisableResend(message)
    }

    private fun getFileNameFromPath(attachmentUrl: String): String? {
        val uri = Uri.parse(attachmentUrl)
        return uri.lastPathSegment
    }

    private fun prepareVoiceMessageBeforeSend(
        message: MessageEntity,
        intForm: List<Int>
    ): HashMap<String, Any> {
        val attachment = hashMapOf<String, Any>()
        attachment["url"] = message.attachment.url
        attachment["type"] = "audio"
        val metadata = hashMapOf<Any, Any>()
        intForm.let { data -> metadata.put("wave_form", data) }
        attachment["metadata"] = metadata
        return attachment
    }

    private suspend fun resendImageMessage(
        msg: MessageEntity,
        payload: HashMap<String, Any>,
        resendUri: List<String>,
        fileUri: String
    ) {
        Timber.d("resendImageMessage starts RESEND_URI:$resendUri Payload:$payload fileURI:$fileUri MSG:$msg")
        if (fileUri.isGiphyUrl()) {
            payload["attachment"] = prepareGiphyImageBeforeSend(fileUri)
            resendMessage(msg, payload)
        } else {
            val urlToSendString = when {
                resendUri.size == 1 -> mutableListOf(resendUri[0])
                resendUri.size > 1 -> resendUri
                else -> mutableListOf(fileUri)
            }
            val urlToSendUri = urlToSendString.map { Uri.parse(it) }

            val existsFiles = mutableListOf<Uri>()
            getExistFiles(msg, payload, urlToSendUri) { exists ->
                existsFiles.addAll(exists)
            }

            if (!fileUri.startsWith(HTTP_SCHEME) && existsFiles.isNotEmpty()) {
                try {
                    val uploadImagesResult = dpnd.chatUploadHelper.uploadImages(
                        images = existsFiles,
                        payload = payload
                    )
                    if (uploadImagesResult.successResult != null) {
                        val payloadWithAttachments = payload.toTotalAttachmentsPayload(uploadImagesResult.successResult)
                        resendMessage(msg, payloadWithAttachments)
                    } else {
                        resultCallback.onFailResend(msg)
                        throw Exception("Couldn't upload photo")
                    }
                } catch (e: Exception) {
                    Timber.e(e)
                    dpnd.dataStore.dialogDao().updateBadgeStatus(true, msg.roomId)
                }
            }
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

    private suspend fun getExistFiles(
        msg: MessageEntity,
        payload: HashMap<String, Any>,
        urlToSendUri: List<Uri>,
        existFiles: (List<Uri>) -> Unit
    ) {
        checkExistsMediaFileBeforeSend(urlToSendUri) { existUris, notExistPaths ->
            existFiles(existUris)
            if (notExistPaths.isNotEmpty()) {
                resultCallback.onSetMediaPlaceholder(msg, notExistPaths)
            }

            if (msg.content.isNotEmpty() && existUris.isEmpty()) {
                resendMessage(msg, payload)
            } else if (msg.content.isEmpty() && existUris.isEmpty()) {
                resultCallback.onDisableResend(msg)
            }
        }
    }

    private suspend fun checkExistsMediaFileBeforeSend(
        uris: List<Uri>,
        result: suspend (existUris: List<Uri>, notExistPaths: List<String?>) -> Unit
    ) {
        val existsFiles = mutableListOf<Uri>()
        val notExistsFiles = mutableListOf<String?>()
        uris.forEach { uri ->
            val file = File(uri.path ?: String.empty())
            if (file.exists()) {
                existsFiles.add(uri)
            } else {
                notExistsFiles.add(uri.path)
            }
        }
        result(existsFiles, notExistsFiles)
    }

    private fun prepareGiphyImageBeforeSend(gifUrl: String): HashMap<String, Any> {
        val attachment = hashMapOf<String, Any>()
        attachment["url"] = gifUrl
        attachment["type"] = TYPING_TYPE_GIF
        return attachment
    }

    /**
     * REST отправка сообщения в чате по его payload
     */
    @Suppress("detekt:SwallowedException")
    private suspend fun resendMessage(
        message: MessageEntity,
        payload: HashMap<String, Any>
    ) {
        Timber.d("resendMessage called")
        try {
            val response = dpnd.newMessageUseCase.newMessage(payload)
            if (response.data != null) {
                Timber.d("[BASE RESEND] Success resend")
                resultCallback.onSuccessResend(message)
            } else {
                Timber.e("[BASE RESEND] FAIL resend:$response")
                resultCallback.onFailResend(message)
            }
        } catch (e: Exception) {
            Timber.e("[BASE RESEND] FAIL resend:${e.message}")
            resultCallback.onFailResend(message)
            dpnd.dataStore.dialogDao().updateBadgeStatus(true, message.roomId)
            e.printStackTrace()
            throw Exception("[BASE RESEND] FAIL resend:${e.message}")
        }
    }


}
