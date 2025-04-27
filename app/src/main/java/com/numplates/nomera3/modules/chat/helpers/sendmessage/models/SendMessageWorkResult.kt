package com.numplates.nomera3.modules.chat.helpers.sendmessage.models

import com.google.gson.Gson


enum class SendMessageWorkResultKey(val key: String) {
    ACTION_INSERT_DB_MESSAGE("action_insert_db_message"),
    SUCCESS_SEND("success_send"),
    ACTION_SEND_MESSAGE("action_send_message")
}

data class ActionInsertDbMessage(
    val messageId: String,
    val workId: String
) {

    companion object {
        fun deserialize(gson: Gson, jsonString: String): ActionInsertDbMessage =
            gson.fromJson(jsonString, ActionInsertDbMessage::class.java)
    }
}

data class SuccessSendMessageWorkResult(
    val roomId: Long,
    val guestId: Long,
    val chatType: String
) {
    fun serialize(gson: Gson): String = gson.toJson(this)

    companion object {
        fun deserialize(gson: Gson, jsonString: String): SuccessSendMessageWorkResult =
            gson.fromJson(jsonString, SuccessSendMessageWorkResult::class.java)
    }
}

data class ActionSendMessageWorkResult(
    val messageId: String,
    val isSentError: Boolean,
) {
    fun serialize(gson: Gson): String = gson.toJson(this)

    companion object {
        fun deserialize(gson: Gson, jsonString: String): ActionSendMessageWorkResult =
            gson.fromJson(jsonString, ActionSendMessageWorkResult::class.java)
    }

}
