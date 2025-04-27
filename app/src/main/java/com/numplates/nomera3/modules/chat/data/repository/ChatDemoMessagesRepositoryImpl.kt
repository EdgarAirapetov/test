package com.numplates.nomera3.modules.chat.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.modules.chat.domain.ChatMessagesDemoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val DEMO_MESSAGES_FILE_NAME = "demo_messages.json"

class ChatDemoMessagesRepositoryImpl @Inject constructor(
    private val context: Context,
    private val gson: Gson
) : ChatMessagesDemoRepository {

    override suspend fun readMessages(): List<MessageEntity> {
        return readDemoMessagesFromFile()
    }

    override suspend fun getMessageById(messageId: String): MessageEntity? {
        val messages = readDemoMessagesFromFile()
        return messages.find { it.msgId == messageId }
    }

    private suspend fun readDemoMessagesFromFile(): List<MessageEntity> = withContext(Dispatchers.IO) {
        val fileContent: String =
            context.applicationContext.assets.open(DEMO_MESSAGES_FILE_NAME).bufferedReader().use { it.readText() }
        val listType = object : TypeToken<List<MessageEntity>>() {}
        gson.fromJson(fileContent, listType)
    }
}
