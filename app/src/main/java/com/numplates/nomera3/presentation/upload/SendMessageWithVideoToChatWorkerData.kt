package com.numplates.nomera3.presentation.upload

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.core.extensions.empty

data class SendMessageWithVideoToChatWorkerData(
    val mediaList: Array<String> = emptyArray(),

    val message: String = String.empty(),
    val userId: Long? = null,
    val roomId: Long? = null,
    val msgId: String? = null,
    val parentId: String? = null
) {

    companion object {
        fun fromJson(json: String): SendMessageWithVideoToChatWorkerData =
            Gson().fromJson(
                json,
                object : TypeToken<SendMessageWithVideoToChatWorkerData>() {}.type
            )

        fun SendMessageWithVideoToChatWorkerData.toJson(): String = Gson().toJson(this)
    }

}

