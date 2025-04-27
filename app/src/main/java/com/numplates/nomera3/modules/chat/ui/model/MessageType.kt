package com.numplates.nomera3.modules.chat.ui.model

/**
 * Local class to split different messages types in chat
 */
enum class MessageType(val value: Int) {
    TEXT(1),
    IMAGE(2),
    GIF(3),
    AUDIO(4),
    VIDEO(5),
    REPOST(6),
    LIST(7),
    GIFT(8),
    SHARE_PROFILE(9),
    SHARE_COMMUNITY(10),
    MOMENT(11),
    GREETING(12),
    EVENT(13),
    STICKER(14),
    CALL(15),
    DATE_TIME(16),
    DELETED(17),
    OTHER(18);

    companion object {
        private val map = MessageType.entries.associateBy { it.value }
        operator fun get(value: Int) = map[value]
    }
}
