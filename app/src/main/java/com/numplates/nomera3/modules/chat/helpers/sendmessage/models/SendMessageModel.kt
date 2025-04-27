package com.numplates.nomera3.modules.chat.helpers.sendmessage.models

import com.numplates.nomera3.data.newmessenger.ROOM_TYPE_DIALOG
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.entity.MediakeyboardFavoriteRecentUiModel
import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.modules.chat.mediakeyboard.ui.entity.MediaPreviewType

data class SendMessageModel(
    val sendType: SendMessageType? = null,
    val roomId: Long? = null,
    val userId: Long? = null,
    val roomType: String = ROOM_TYPE_DIALOG,
    val messageText: String? = null,
    val parentMessage: MessageEntity? = null,
    val imageData: ImageMessageDataModel? = null,
    val voiceData: VoiceMessageDataModel? = null,
    val videoData: VideoMessageDataModel? = null,
    val currentScrollPosition: Int = 0,
    val unreadMessagesCount: Int = 0,
    val favoriteRecent: MediakeyboardFavoriteRecentUiModel? = null,
    val favoriteRecentType: MediaPreviewType? = null
)
