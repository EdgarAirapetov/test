package com.numplates.nomera3.data.fcm.data

import com.google.gson.annotations.SerializedName

@Deprecated("Used in OLD chat")
data class DialogId(
        @SerializedName("dialog_id") var id: Long?
)