package com.numplates.nomera3.data.newmessenger.response


import androidx.annotation.Nullable
import com.google.gson.annotations.SerializedName
import com.meera.db.models.chatmembers.ChatMember
import com.meera.db.models.userprofile.GiftEntity
import com.meera.db.models.dialog.UserChat
import java.io.Serializable

class ResponseWrapperWebSock<T> {

    @Nullable
    @SerializedName("response")
    var response: T? = null

    @Nullable
    @SerializedName("status")
    var status: String? = null

}

data class WebSocketErrorMessage(
    @SerializedName("error")
    val error: String?
)

// get_user_info
data class ChatUsers(
    @SerializedName("users")
    val users: List<UserChat>
)

// Observe update_user
data class UpdateUserResponse(
        @SerializedName("room_id")
        val roomId: Long?,

        @SerializedName("user_id")
        val userId: Long?
)

/*data class MembersIds(
        @SerializedName("members")
        val members: List<Long>
)*/

data class ResponseRoomId(
        @SerializedName("room_id")
        val roomId: Long
)

// ------- Get members ---------------

data class GroupChatMembers(

        @SerializedName("limit")
        val limit: Int,

        @SerializedName("offset")
        val offset: Int,

        @SerializedName("members")
        val members: List<ChatMember>
)

data class GroupChatAdmins(
        @SerializedName("admins")
        val admins: List<ChatMember>
)


// -------- GIFTS -----------------
data class GiftResponse(
        @SerializedName("gifts")
        val gifts: List<GiftEntity>
): Serializable
