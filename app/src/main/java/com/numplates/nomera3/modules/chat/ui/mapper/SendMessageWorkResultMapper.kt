package com.numplates.nomera3.modules.chat.ui.mapper

import com.google.gson.Gson
import com.numplates.nomera3.modules.chat.helpers.sendmessage.models.ActionInsertDbMessage
import com.numplates.nomera3.modules.chat.helpers.sendmessage.models.ActionSendMessageWorkResult
import com.numplates.nomera3.modules.chat.helpers.sendmessage.models.SuccessSendMessageWorkResult
import javax.inject.Inject

class SendMessageWorkResultMapper @Inject constructor(
    private val gson: Gson
) {

    fun mapSuccessSendMessageWorkResult(jsonResult: String): SuccessSendMessageWorkResult {
        return SuccessSendMessageWorkResult.deserialize(
            gson = gson,
            jsonString = jsonResult
        )
    }

    fun mapActionSendMessageWorkResult(jsonResult: String): ActionSendMessageWorkResult {
        return ActionSendMessageWorkResult.deserialize(
            gson = gson,
            jsonString = jsonResult
        )
    }

    fun mapActionMessageInProgress(jsonResult: String): ActionInsertDbMessage {
        return ActionInsertDbMessage.deserialize(
            gson = gson,
            jsonString = jsonResult
        )
    }

}
