package com.numplates.nomera3.modules.chat.helpers

import android.net.Uri
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.meera.core.extensions.empty
import com.meera.core.extensions.fromJson
import com.meera.core.extensions.toBoolean
import com.meera.db.models.dialog.DialogEntity
import com.meera.db.models.dialog.LastMessage
import com.meera.db.models.dialog.UserChat
import com.meera.db.models.message.MessageAttachment
import com.meera.db.models.message.MessageEntity
import com.meera.db.models.message.ParentMessage
import com.meera.db.models.message.isDefault
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.GIPHY_BRAND_NAME
import com.numplates.nomera3.HTTPS_SCHEME
import com.numplates.nomera3.HTTP_SCHEME
import com.numplates.nomera3.MEDIA_EXT_GIF
import com.numplates.nomera3.data.newmessenger.CHAT_ITEM_TYPE_EVENT
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_AUDIO
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_EVENT
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_GIF
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_IMAGE
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_MOMENT
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_POST
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_STICKER
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_VIDEO
import com.numplates.nomera3.modules.chat.ChatPayloadKeys
import com.numplates.nomera3.modules.chat.messages.domain.model.AttachmentType
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_AUDIO_RECEIVE
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_AUDIO_SEND
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_CALLS
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_GIFT_RECEIVE
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_GIFT_SEND
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_GREETING_RECEIVE
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_GREETING_SEND
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_IMAGE_RECEIVE
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_IMAGE_SEND
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_MESSAGE_RECEIVE_DELETED
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_MESSAGE_SEND_DELETED
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_ONLY_TEXT_RECEIVE
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_ONLY_TEXT_SEND
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_RECEIVE
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_REPOST_RECEIVE
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_REPOST_SEND
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_SEND
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_SERVICE_MESSAGE
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_SHARE_COMMUNITY_RECEIVE
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_SHARE_COMMUNITY_SEND
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_SHARE_PROFILE_RECEIVE
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_SHARE_PROFILE_SEND
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_STICKER_RECEIVE
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_STICKER_SEND
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_UNKNOWN
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_VIDEO_RECEIVE
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_VIDEO_SEND
import com.numplates.nomera3.modules.communities.data.entity.CommunityShareEntity
import com.numplates.nomera3.modules.moments.show.data.MomentItemDto
import com.numplates.nomera3.presentation.model.enums.ChatEventEnum
import com.numplates.nomera3.presentation.utils.parseUniquename
import java.io.File
import java.util.concurrent.TimeUnit

private const val META_DATA_MOMENT = "moment"
private const val EDIT_MESSAGE_TIME_WINDOW_HOURS = 48

fun resolveMessageType(
    creatorId: Long?,
    attachment: MessageAttachment?,
    attachments: List<MessageAttachment>,
    deleted: Boolean,
    eventCode: Int?,
    type: String?,
    myUid: Long
): Int {
    return when {
        // Deleted messages -> MUST BE PLACED BEFORE ALL TYPES
        creatorId == myUid && deleted -> ITEM_TYPE_MESSAGE_SEND_DELETED
        creatorId != myUid && deleted -> ITEM_TYPE_MESSAGE_RECEIVE_DELETED

        // Share profile
        creatorId == myUid && eventCode == ChatEventEnum.SHARE_PROFILE.state -> ITEM_TYPE_SHARE_PROFILE_SEND
        creatorId != myUid && eventCode == ChatEventEnum.SHARE_PROFILE.state -> ITEM_TYPE_SHARE_PROFILE_RECEIVE

        // Share community
        creatorId == myUid && eventCode == ChatEventEnum.SHARE_COMMUNITY.state -> ITEM_TYPE_SHARE_COMMUNITY_SEND
        creatorId != myUid && eventCode == ChatEventEnum.SHARE_COMMUNITY.state -> ITEM_TYPE_SHARE_COMMUNITY_RECEIVE

        // Service message (any event)
        type == CHAT_ITEM_TYPE_EVENT -> ITEM_TYPE_SERVICE_MESSAGE

        // Repost Messages
        creatorId == myUid
            && (attachment?.type == TYPING_TYPE_POST || attachment?.type == TYPING_TYPE_EVENT || attachment?.type == TYPING_TYPE_MOMENT)
            -> ITEM_TYPE_REPOST_SEND

        creatorId != myUid
            && (attachment?.type == TYPING_TYPE_POST || attachment?.type == TYPING_TYPE_EVENT || attachment?.type == TYPING_TYPE_MOMENT)
            -> ITEM_TYPE_REPOST_RECEIVE

        // Gifts
        creatorId == myUid && eventCode == ChatEventEnum.GIFT.state -> ITEM_TYPE_GIFT_SEND
        creatorId != myUid && eventCode == ChatEventEnum.GIFT.state -> ITEM_TYPE_GIFT_RECEIVE

        // Audio message
        creatorId == myUid
            && !attachment?.url.isNullOrBlank()
            && attachment?.type == TYPING_TYPE_AUDIO -> ITEM_TYPE_AUDIO_SEND

        creatorId != myUid
            && !attachment?.url.isNullOrBlank()
            && attachment?.type == TYPING_TYPE_AUDIO -> ITEM_TYPE_AUDIO_RECEIVE

        // Video
        creatorId == myUid
            && !attachment?.url.isNullOrBlank()
            && attachment?.type == TYPING_TYPE_VIDEO -> ITEM_TYPE_VIDEO_SEND

        creatorId != myUid
            && !attachment?.url.isNullOrBlank()
            && attachment?.type == TYPING_TYPE_VIDEO -> ITEM_TYPE_VIDEO_RECEIVE

        // Images
        creatorId == myUid
            && !attachment?.url.isNullOrBlank() &&
            (attachment?.type == TYPING_TYPE_IMAGE || attachment?.type == TYPING_TYPE_GIF) -> ITEM_TYPE_IMAGE_SEND

        creatorId != myUid
            && !attachment?.url.isNullOrBlank()
            && (attachment?.type == TYPING_TYPE_IMAGE || attachment?.type == TYPING_TYPE_GIF) -> ITEM_TYPE_IMAGE_RECEIVE

        creatorId == myUid
            && !attachment?.url.isNullOrBlank()
            && attachment?.type == TYPING_TYPE_STICKER -> ITEM_TYPE_STICKER_SEND

        creatorId != myUid
            && !attachment?.url.isNullOrBlank()
            && attachment?.type == TYPING_TYPE_STICKER -> ITEM_TYPE_STICKER_RECEIVE

        // Multi Images
        creatorId == myUid && attachments.isNotEmpty() -> ITEM_TYPE_IMAGE_SEND
        creatorId != myUid && attachments.isNotEmpty() -> ITEM_TYPE_IMAGE_RECEIVE

        // Call incoming/outgoing messages
        eventCode == ChatEventEnum.CALL.state -> ITEM_TYPE_CALLS

        creatorId == myUid && eventCode == ChatEventEnum.GREETING.state -> ITEM_TYPE_GREETING_SEND
        creatorId != myUid && eventCode == ChatEventEnum.GREETING.state -> ITEM_TYPE_GREETING_RECEIVE

        // ONLY TEXT MESSAGES
        // IF user == me AND all attachments == empty -> my text message
        // IF user != me AND all attachments == empty -> other's text message
        creatorId == myUid
            && attachments.isEmpty()
            && attachment?.url.isNullOrBlank() -> ITEM_TYPE_ONLY_TEXT_SEND

        creatorId != myUid
            && attachments.isEmpty()
            && attachment?.url.isNullOrBlank() -> ITEM_TYPE_ONLY_TEXT_RECEIVE

        // ОБЯЗАТЕЛЬНО после аудио сообщений
        creatorId == myUid -> ITEM_TYPE_SEND
        creatorId != myUid -> ITEM_TYPE_RECEIVE

        else -> ITEM_TYPE_UNKNOWN
    }
}

fun MessageEntity.toRoomLastMessage(): LastMessage {
    val tagParsedContent = parseUniquename(this.content, this.tags).text ?: this.content
    return LastMessage(
            this.msgId,
            tagParsedContent,
            this.type,
            this.attachment,
            this.attachments,
            this.eventCode,
            this.metadata,
            this.creator ?: UserChat(),
            this.createdAt,
            this.updatedAt,
            this.deleted,
            this.delivered,
            this.readed,
            this.sent
    )
}

fun MessageEntity.toParentMessage() : ParentMessage {
    val videoPreview = if (itemType == ITEM_TYPE_VIDEO_RECEIVE || itemType == ITEM_TYPE_VIDEO_SEND){
        attachment.makeMetaMessageWithVideo()?.preview ?: ""
    } else ""

    val sharedUser = if(itemType == ITEM_TYPE_SHARE_PROFILE_SEND || itemType == ITEM_TYPE_SHARE_PROFILE_RECEIVE) {
        Gson().fromJson<UserSimple?>(attachment.metadata)
    } else null

    val sharedCommunity = if (itemType == ITEM_TYPE_SHARE_COMMUNITY_SEND
        || itemType == ITEM_TYPE_SHARE_COMMUNITY_RECEIVE) {
            Gson().fromJson<CommunityShareEntity?>(attachment.metadata)
        } else null

    val isMoment = attachment.metadata[META_DATA_MOMENT] != null

    return ParentMessage(
            type = itemType,
            eventCode = eventCode ?: -1,
            creatorName = creator?.name?: String.empty(),
            messageContent = tagSpan?.text?: String.empty(),
            imagePreview = attachments.firstOrNull()?.url ?: attachment.url,
            videoPreview = videoPreview,
            imageCount = attachments.size,
            createdAt = createdAt,
            parentId = msgId,
            sharedProfileUrl = sharedUser?.avatarSmall ?: String.empty(),
            isDeletedSharedProfile = sharedUser?.profileDeleted == 1,
            sharedCommunityUrl = sharedCommunity?.avatar ?: String.empty(),
            isPrivateCommunity = sharedCommunity?.private == 1,
            isDeletedSharedCommunity = sharedCommunity?.deleted == 1,
            isEvent = attachment.type == CHAT_ITEM_TYPE_EVENT,
            isMoment = isMoment,
            metadata = metadata
    )
}

/**
 * Проверка содержит ли сообщение какое-либо
 * изображение или видео для скачивания
 */
fun MessageEntity.isMediaMessage(): Boolean {
    return listOf(attachment)
        .any { item -> item.type in listOf(TYPING_TYPE_IMAGE, TYPING_TYPE_GIF, TYPING_TYPE_VIDEO) }
}

fun MessageEntity.isImageOrGifMessage(): Boolean {
    return listOf(attachment)
        .any { item -> item.type in listOf(TYPING_TYPE_IMAGE, TYPING_TYPE_GIF) }
}

fun MessageEntity.isStickerMessage() = attachment.type == TYPING_TYPE_STICKER

/**
 * Проверка существования локального файла
 * для возможности отправки на сервер
 */
fun isFileByLocalPathExists(url: String?, isNotExists: () -> Unit) {
    val uri = Uri.parse("file://$url")
    val file = File(uri.path ?: String.empty())
    url?.let { path ->
        if (!path.isNetworkPath() && !file.exists()) {
            isNotExists.invoke()
        }
    }
}

fun String.isNetworkPath() = this.startsWith(HTTP_SCHEME) || this.startsWith(HTTPS_SCHEME)

/**
 * Метод определяет это сетевой адрес или локальное
 */
fun List<String>.isNetworkPath() =
    this.isNotEmpty() && (this[0].startsWith(HTTP_SCHEME) || this[0].startsWith(HTTPS_SCHEME))

/**
 * Метод определяет это сетевое URI или локальное
 */
fun List<Uri>.isNetworkUri(): Boolean {
    val scheme = this[0].scheme
    return this.isNotEmpty() && (scheme == HTTP_SCHEME || scheme == HTTPS_SCHEME)
}

fun Uri.isNetworkUri(): Boolean = scheme == HTTP_SCHEME || scheme == HTTPS_SCHEME

fun String.isGifUrl() = GIPHY_BRAND_NAME in this || this.endsWith(MEDIA_EXT_GIF)

fun String.isGiphyUrl() = GIPHY_BRAND_NAME in this

/**
 * Метод определят, можно ли копировать текст этого сообщения
 * в буфер обмена
 */
fun MessageEntity.isValidForCopy() =
    content.isNotEmpty()
            && eventCode != ChatEventEnum.SHARE_PROFILE.state
            && eventCode != ChatEventEnum.SHARE_COMMUNITY.state

fun MessageEntity.isValidForForwarding(): Boolean {
    if (sent.not()) return false
    return when (eventCode) {
        ChatEventEnum.TEXT.state,
        ChatEventEnum.IMAGE.state,
        ChatEventEnum.GIF.state,
        ChatEventEnum.AUDIO.state,
        ChatEventEnum.VIDEO.state,
        ChatEventEnum.LIST.state,
        ChatEventEnum.REPOST.state,
        ChatEventEnum.MOMENT.state,
        ChatEventEnum.SHARE_PROFILE.state,
        ChatEventEnum.GREETING.state,
        ChatEventEnum.SHARE_COMMUNITY.state,
        ChatEventEnum.STICKER.state -> true
        else -> false
    }
}

// TODO check for isSticker when STICKERS are merged (https://nomera.atlassian.net/browse/PO-60)
fun MessageEntity.isValidForEdit(): Boolean {
    val hasAuthor = author != null
    val isMessageSent = sent
    val canBeEdited = when (itemType) {
        ITEM_TYPE_SEND,
        ITEM_TYPE_IMAGE_SEND,
        ITEM_TYPE_VIDEO_SEND,
        ITEM_TYPE_ONLY_TEXT_SEND,
        ITEM_TYPE_REPOST_SEND -> true
        else -> false
    }
    return isMessageSent && !isLateForEdit() && !hasGiphyMedia() && canBeEdited && !hasAuthor
}

fun MessageEntity.isValidForContentSharing(): Boolean {
    if (sent.not()) return false
    return when (eventCode) {
        ChatEventEnum.TEXT.state,
        ChatEventEnum.IMAGE.state,
        ChatEventEnum.GIF.state,
        ChatEventEnum.VIDEO.state,
        ChatEventEnum.LIST.state -> true
        else -> false
    }
}

fun MessageEntity.hasGiphyMedia(): Boolean {
    return attachment.type == AttachmentType.GIF.type &&
        attachment.url.contains("giphy.com")
}

fun MessageEntity.isLateForEdit(): Boolean =
    TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - createdAt) >= EDIT_MESSAGE_TIME_WINDOW_HOURS

fun MessageEntity.isEdited(): Boolean = this.editedAt > 0L

fun MessageEntity.isMomentDeleted(): Boolean {
    if (attachment.type != TYPING_TYPE_MOMENT) return false
    val momentMap = attachment.metadata[ChatPayloadKeys.ATTACHMENT_METADATA_MOMENT.key] as? LinkedTreeMap<String, Any>
        ?: return false
    val moment = Gson().fromJson<MomentItemDto?>(momentMap) ?: return false

    return moment.deleted.toBoolean()
}

fun MessageEntity.isCommunityDeleted(): Boolean {
    if (attachment.type != AttachmentType.COMMUNITY.type) return false
    val group = Gson().fromJson<CommunityShareEntity?>(attachment.metadata) ?: return false
    return group.deleted.toBoolean()
}

fun MessageEntity.isRepost() = this.eventCode == ChatEventEnum.REPOST.state

fun MessageEntity.allMediaAttachments(): List<MessageAttachment> {
    return attachments.orEmpty()
        .plus(attachment.takeIf { !it.isDefault() })
        .filterNot { attachment ->
            attachment?.type == AttachmentType.POST.type || attachment?.type == AttachmentType.EVENT.type
        }
        .filterNotNull()
}

fun UserChat.isNotLockedMessages(): Boolean {
    return settingsFlags?.userCanChatMe?.toBoolean() == true
        && settingsFlags?.iCanChat?.toBoolean() == true
        && blacklistedByMe?.toBoolean() != true
        && blacklistedMe?.toBoolean() != true
}

fun DialogEntity.isNotBlocked(): Boolean = this.blocked == false

fun DialogEntity?.isNotBlocked(block: () -> Unit) {
    if (this != null &&  this.blocked == false) {
        block.invoke()
    }
}
