package com.numplates.nomera3.modules.chat.data.mapper

import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.meera.core.extensions.empty
import com.meera.core.extensions.fromJson
import com.meera.core.extensions.isTrue
import com.meera.core.preferences.AppSettings
import com.meera.db.DataStore
import com.meera.db.models.message.MessageAttachment
import com.meera.db.models.message.MessageEntity
import com.meera.db.models.message.MessageEntityNetwork
import com.numplates.nomera3.GIPHY_BRAND_NAME
import com.numplates.nomera3.data.network.Post
import com.numplates.nomera3.modules.chat.helpers.resolveMessageType
import com.numplates.nomera3.modules.chat.helpers.sendmessage.PAYLOAD_PARAM
import com.numplates.nomera3.modules.chat.helpers.toParentMessage
import com.numplates.nomera3.presentation.birthday.ui.BirthdayTextUtil
import com.numplates.nomera3.presentation.utils.parseUniquename
import javax.inject.Inject

class MessengerEntityMapper @Inject constructor(
    private val appSettings: AppSettings,
    private val birthdayTextUtil: BirthdayTextUtil,
    private val dataStore: DataStore,
    private val gson: Gson
) {
    fun mapBeforeInsertToDB(
        message: MessageEntity,
        isRoomChatRequest: Boolean
    ): MessageEntity {
        fixMessageJsonParsing(message)
        val filteredParent = fixMessageJsonParsing(message.parent)

        val attachmentUrl = message.attachment.url
        val ownUid = appSettings.readUID()
        message.apply {
            itemType = resolveMessageType(
                creatorId = message.creator?.userId,
                attachment = message.attachment,
                attachments = message.attachments,
                deleted = message.deleted,
                eventCode = message.eventCode,
                type = message.type,
                myUid = ownUid,
            )
            parent?.itemType = resolveMessageType(
                creatorId = filteredParent?.creator?.userId,
                attachment = filteredParent?.attachment,
                attachments = filteredParent?.attachments.orEmpty(),
                deleted = filteredParent?.deleted.isTrue(),
                eventCode = filteredParent?.eventCode,
                type = filteredParent?.type,
                myUid = ownUid
            )
            tagSpan = parseUniquename(message.content, message.tags)
            parent?.tagSpan = parseUniquename(filteredParent?.content, filteredParent?.tags)
            parentMessage = filteredParent?.toParentMessage()
            isShowGiphyWatermark = attachmentUrl.isNotEmpty() && GIPHY_BRAND_NAME in attachmentUrl
            isShowImageBlurChatRequest = isRoomChatRequest && message.creator?.userId != ownUid
            parentMessage = parent?.toParentMessage()
        }
        setBirthdayWordRangesIfBirthday(message)

        (message.attachment.metadata[PAYLOAD_PARAM.ATTACHMENT_METADATA_POST.key]
            as? LinkedTreeMap<String, Any>)?.let {
            val post = gson.fromJson<Post?>(it)
            post?.tagSpan = parseUniquename(post?.text, post?.tags)
            post?.event?.tagSpan = parseUniquename(post?.event?.title, post?.event?.tags)
            val json = gson.toJson(post)
            message.attachment.metadata[PAYLOAD_PARAM.ATTACHMENT_METADATA_POST.key] =
                gson.fromJson(json, Map::class.java)
        }

        saveParentMessageByNull(message)

        return message

    }

    fun mapToNetworkMessages(messages: List<MessageEntity>): List<MessageEntityNetwork> {
        return messages.map { MessageEntityNetwork(
            msgId = it.msgId,
            parentId = it.parentId,
            parent = it.parent,
            author = it.author,
            roomId = it.roomId,
            type = it.type,
            itemType = it.itemType,
            content = it.content,
            tagSpan = it.tagSpan,
            metadata = it.metadata,
            createdAt = it.createdAt,
            updatedAt = it.updatedAt,
            editedAt = it.editedAt,
            creator = it.creator,
            deleted = it.deleted,
            delivered = it.delivered,
            readed = it.readed,
            attachment = it.attachment,
            attachments = it.attachments,
            parentMessage = it.parentMessage,
            isShowGiphyWatermark = it.isShowGiphyWatermark,
            isShowImageBlurChatRequest = it.isShowImageBlurChatRequest,
            birthdayRangesList = it.birthdayRangesList
        ) }
    }

    /**
     * Метод необходим т.к. json по разному парсится через retrofit и через сокет
     * Различия в обработке null значений
     * Если null приходит через rest на данные поля, то происходит ошибка парсинга
     */
    private fun fixMessageJsonParsing(message: MessageEntity?): MessageEntity? {
        if (message?.attachment == null) message?.attachment = MessageAttachment()
        if (message?.attachments == null) message?.attachments = emptyList()
        if (message?.content == null) message?.content = String.empty()
        return message
    }

    private fun setBirthdayWordRangesIfBirthday(messageEntity: MessageEntity) {
        messageEntity.birthdayRangesList =
            birthdayTextUtil.getBirthdayTextListRanges(
                birthdayText = messageEntity.tagSpan?.text.orEmpty()
            )
    }

    private fun saveParentMessageByNull(message: MessageEntity) {
        if (message.deleted) {
            dataStore.messageDao().setParentNullById(message.msgId)
        }
    }
}
