package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import com.numplates.nomera3.data.network.core.ListResponse
import java.io.Serializable


//TODO: непроверен - старая версия ответа сервера (обнови набор полей при первом тесте)
class PostEvents (
    @SerializedName("events") var events: List<PostEvent?>?
): Serializable, ListResponse<PostEvent?>() {
    override fun getList(): List<PostEvent?>? {
        return events
    }
}
