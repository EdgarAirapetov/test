package com.numplates.nomera3.modules.chat.ui.helper

import com.meera.db.models.message.MessageEntity
import timber.log.Timber
import javax.inject.Inject

/**
 * Класс помошник для кэширования списка сообщений из слоя данных. Используется для того, чтобы соединить
 * старый код с новым кодом UI-кита. ЗАПРЕЩЕНО! использовать для нового кода.
 */
class MediaEntitiesCacheHelper @Inject constructor() {

    private val cachedMessageEntities = mutableListOf<MessageEntity>()

    fun getFromCacheById(messageId: String): MessageEntity? {
        return cachedMessageEntities.find { it.msgId == messageId }
    }

    fun cacheMessageEntities(messages: List<MessageEntity>?): List<MessageEntity> {
        Timber.d("CacheMessageEntities called with source size: ${messages?.size}")
        val messagesToCache = messages.orEmpty()
        cachedMessageEntities.clear()
        cachedMessageEntities.addAll(messagesToCache)
        return messagesToCache
    }
}
