package com.numplates.nomera3.data.newmessenger.response

import com.google.gson.annotations.SerializedName
import com.meera.db.models.dialog.DialogEntity


/**
 * Json Dialogs response from server
 */
data class ResponseDialogs(

    @SerializedName("response")
    var response: Dialogs,

    @SerializedName("status")
    var status: String
)

data class Dialogs(

    @SerializedName("whoCanChat")
    var whoCanChat: Int,

    @SerializedName("count_blacklist")
    var countBlackList: Int,

    @SerializedName("count_whitelist")
    var countWhiteList: Int,

    @SerializedName("chatRequest")
    var chatRequest: Int?,

    @SerializedName("rooms")
    var dialogs: List<DialogEntity>
)

data class Dialog(
    @SerializedName("room")
    val dialog: DialogEntity
)
