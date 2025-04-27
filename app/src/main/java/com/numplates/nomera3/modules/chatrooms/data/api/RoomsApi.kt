package com.numplates.nomera3.modules.chatrooms.data.api

import com.google.gson.annotations.SerializedName
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.meera.db.models.dialog.DialogEntity
import retrofit2.http.GET
import retrofit2.http.Query

interface RoomsApi {

    @GET("/v2/rooms")
    suspend fun getRooms(
        @Query("user_id") userId: Long,
        @Query("user_type") userType: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("top_ts") topTs: Long? = null,
        @Query("updated_at") updatedAt: Long? = null,
    ): ResponseWrapper<GetRoomsResponse?>

}

data class GetRoomsResponse(
    @SerializedName("whoCanChat")
    var whoCanChat: Int?,

    @SerializedName("count_blacklist")
    var countBlackList: Int?,

    @SerializedName("count_whitelist")
    var countWhiteList: Int?,

    @SerializedName("chatRequest")
    var chatRequest: Int?,

    @SerializedName("rooms")
    val dialogList: List<DialogEntity>
)
