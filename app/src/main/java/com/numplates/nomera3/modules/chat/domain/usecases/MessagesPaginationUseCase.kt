package com.numplates.nomera3.modules.chat.domain.usecases

import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.modules.chat.domain.ChatMessageRepository
import com.numplates.nomera3.modules.chat.domain.params.MessagePaginationDirection
import com.numplates.nomera3.modules.chat.domain.params.MessagePaginationUserType
import javax.inject.Inject

private const val DEFAULT_LIMIT = 60

class MessagesPaginationUseCase @Inject constructor(
    private val repository: ChatMessageRepository
) {
    suspend fun invoke(
        roomId: Long,
        timeStamp: Long,
        direction: MessagePaginationDirection,
        isChatRoomRequest: Boolean,
        needToShowUnreadMessagesSeparator: Boolean = false,
        isInitialRequest: Boolean
    ): List<MessageEntity> {
        return repository.getMessagesAndInsertInDb(
            roomId = roomId,
            timeStamp = timeStamp,
            limit = DEFAULT_LIMIT,
            direction = direction,
            isRoomChatRequest = isChatRoomRequest,
            needToShowUnreadMessagesSeparator = needToShowUnreadMessagesSeparator,
            isInitialRequest = isInitialRequest,
            userType = MessagePaginationUserType.USER_CHAT.paramValue
        )
    }
}
