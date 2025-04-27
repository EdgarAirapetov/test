package com.numplates.nomera3.telecom

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.meera.db.models.message.MessageAttachment
import com.meera.db.models.dialog.DialogEntity
import com.meera.core.extensions.empty
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SignalingMsgPayload(

    @SerializedName("id")
    var uuid: String,

    @SerializedName("room_id")
    var roomId: Long?,

    @SerializedName("room")
    var room: DialogEntity?,

    @SerializedName("type")
    var type: String,

    @SerializedName("data")
    var extdata: String = String()
) : Parcelable {
    constructor() : this(String.empty(), 0L, DialogEntity(), "", "")
}


data class Acceptor(

    @SerializedName("answer")
    var answer: String,

    @SerializedName("candidates")
    var candidates: List<String>,

    @SerializedName("id")
    var id: Long
)

data class Caller(

    @SerializedName("candidates")
    var candidates: List<String>,

    @SerializedName("id")
    var id: Long,

    @SerializedName("offer")
    var offer: String
)

data class Metadata(

    @SerializedName("acceptor")
    var acceptor: Acceptor,

    @SerializedName("at")
    var at: Long,

    @SerializedName("caller")
    var caller: Caller,

    @SerializedName("status")
    var status: String,

    @SerializedName("type")
    var type: String?
)

data class SignalingMsgResponsePayload(

    @SerializedName("updated_at")
    var updated_at: Long,

    @SerializedName("metadata")
    var metadata: Metadata
)

data class SignalingMsgResponse(

    @SerializedName("response")
    var response: SignalingMsgResponsePayload
)

data class SignalingStartCallResponsePayload(

    @SerializedName("attachment")
    var attachment: MessageAttachment = MessageAttachment(),

    @SerializedName("code")
    var code: Long = 10,

    @SerializedName("content")
    var content: String = String.empty(),

    @SerializedName("created_at")
    var created_at: Long,

    @SerializedName("deleted")
    var deleted: Boolean = false,

    @SerializedName("delivered")
    var delivered: Boolean = true,

    @SerializedName("id")
    var id: String,

    @SerializedName("metadata")
    var metadata: Metadata,

    @SerializedName("readed")
    var readed: Boolean = true,

    @SerializedName("room_id")
    var room_id: Long = 0L,
//
//        @SerializedName("room")
//        var room: DialogEntity,

    @SerializedName("type")
    var type: String,

    @SerializedName("updated_at")
    var updated_at: Long
)

data class SignalingStartCallResponse(

    @SerializedName("response")
    var response: SignalingStartCallResponsePayload
)

data class SignalingGetIceResponse(

    @SerializedName("response")
    var response: SignalingGetIceResponsePayload
)

data class SignalingGetIceResponsePayload(

    @SerializedName("stun_servers")
    val stunServers: List<String>?,

    @SerializedName("turn_servers")
    val turnServers: List<Turn>?,

    @SerializedName("rtcp_mux_policy")
    val rtcpMuxPolicy: String?,

    @SerializedName("tcp_candidate_policy")
    val tcpCandidatePolicy: String?

)

data class Turn(
    @SerializedName("url")
    var url: String? = null,
    @SerializedName("credential")
    var credential: String? = "",
    @SerializedName("username")
    var username: String? = ""
)
