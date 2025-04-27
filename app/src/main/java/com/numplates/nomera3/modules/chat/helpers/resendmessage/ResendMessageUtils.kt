package com.numplates.nomera3.modules.chat.helpers.resendmessage

import com.meera.db.models.message.MessageAttachment

/**
 * Метод сохраняет старые атачменты с удалёнными
 * медиа файлы и заменяет локальные url на серверные url
 * успешно отправленных изображений
 */
fun attachmentsSaver(
    old: List<MessageAttachment>,
    new: List<MessageAttachment>
): List<MessageAttachment> {
    var i = 0
    old.forEach { attach ->
        if(attach.url != MessageAttachment.EMPTY_URL) {
            attach.url = new[i].url
            i++
        }
    }
    return old
}
