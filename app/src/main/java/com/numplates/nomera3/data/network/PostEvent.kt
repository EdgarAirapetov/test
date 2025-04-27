package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable


//TODO: непроверен - старая версия ответа сервера (обнови набор полей при первом тесте)
data class PostEvent(
    @SerializedName("event_id") var eventId: Long,
    @SerializedName("type") var type: Int,
    @SerializedName("read_state") var readState: Byte,
    @SerializedName("comment_text") var commentText: String?,
    @SerializedName("date") var date: Long,
    @SerializedName("comment_author") var commentAuthor: UserInfoModel?,
    @SerializedName("post") var post: Post?
): Serializable
