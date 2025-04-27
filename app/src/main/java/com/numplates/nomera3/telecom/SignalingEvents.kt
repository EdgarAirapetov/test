package com.numplates.nomera3.telecom

import com.meera.db.models.dialog.UserChat
import com.numplates.nomera3.VIDEO_CALL_KEY

open class SignalingEvent(p: Int) {

    enum class Origin {ORIGIN_UNKNOWN, ORIGIN_LOCAL, ORIGIN_REMOTE, ORIGIN_OUSIDE /*either Pstn or whatup or etc.*/}

    companion object {
        const val THE_HIGHEST_PRIORITY: Int = 0
        const val HIGH_PRIORITY: Int = 1
        const val THE_LOWEST_PRIOTITY: Int = 255
    }

    val priority: Int = p
    var origin : Origin = Origin.ORIGIN_UNKNOWN

    open fun toPayload(): Map<String, Any> {
        return hashMapOf()
    }
}

class SignalingUnknown : SignalingEvent(THE_LOWEST_PRIOTITY)

class SignalingTransmissionError: SignalingEvent(THE_HIGHEST_PRIORITY) {
    init {
        origin = Origin.ORIGIN_LOCAL
    }
}

class SignalingGetIceServers: SignalingEvent(THE_LOWEST_PRIOTITY) {
    init {
        origin = Origin.ORIGIN_LOCAL
    }
}

class SignalingSendMore: SignalingEvent(THE_LOWEST_PRIOTITY) {
    init {
        origin = Origin.ORIGIN_LOCAL
    }
}

class PlaceCall(callUser: UserChat, typeCall: String) : SignalingEvent(HIGH_PRIORITY) {

    val userId: Long = callUser.userId ?: 0
    val type: String = typeCall
    val user = callUser

    init {
        origin = Origin.ORIGIN_LOCAL
    }

    override fun toPayload(): Map<String, Any> {
        return hashMapOf(
                "user_id" to userId,
                "type" to type
        )
    }
}

class SignalingIncomingCall(fromRemote: SignalingMsgPayload, src: Origin) : SignalingEvent(THE_LOWEST_PRIOTITY) {

    val uuid: String = fromRemote.uuid

    var roomId = if (fromRemote.room?.roomId != null && fromRemote.room?.roomId != 0L) {
        fromRemote.room!!.roomId
    } else if (fromRemote.roomId != null && fromRemote.roomId != 0L) {
        fromRemote.roomId!!
    } else {
        0L
    }

    var callUser: UserChat? = fromRemote.room?.companion
    var videoCall: Boolean = fromRemote.extdata == VIDEO_CALL_KEY

    init {
        origin = src
    }
}

// Send & Received
class PlaceAccept: SignalingEvent(HIGH_PRIORITY) {
    init {
        origin = Origin.ORIGIN_LOCAL
    }
}
class SignalingAcceptCall(uid: String, rid: Long): SignalingEvent(HIGH_PRIORITY) {

    val uuid: String = uid
    val roomId: Long = rid

    init {
        origin = Origin.ORIGIN_REMOTE
    }

    override fun toPayload(): Map<String, Any> {
        return hashMapOf(
                "room_id" to roomId,
                "id" to uuid
        )
    }
}

// Receiving
class SignalingLineBusy(uid: String, rid: Long): SignalingEvent(HIGH_PRIORITY) {

    val uuid: String = uid
    val roomId: Long = rid

    init {
        origin = Origin.ORIGIN_REMOTE
    }

    override fun toPayload(): Map<String, Any> {
        return hashMapOf(
                "room_id" to roomId,
                "id" to uuid
        )
    }
}

// Send & Received
class PlaceReject: SignalingEvent(HIGH_PRIORITY) {
    init {
        origin = Origin.ORIGIN_LOCAL
    }
}
class SignalingRejectCall(uid: String, rid: Long): SignalingEvent(HIGH_PRIORITY) {

    val uuid: String = uid
    val roomId: Long = rid

    init {
        origin = Origin.ORIGIN_REMOTE
    }

    override fun toPayload(): Map<String, Any> {
        return hashMapOf(
                "room_id" to roomId,
                "id" to uuid
        )
    }
}

// Send & Received
class PlaceStop: SignalingEvent(HIGH_PRIORITY) {
    init {
        origin = Origin.ORIGIN_LOCAL
    }
}
class SignalingStopCall(uid: String, rid: Long): SignalingEvent(HIGH_PRIORITY) {

    val uuid: String = uid
    val roomId: Long = rid

    init {
        origin = Origin.ORIGIN_REMOTE
    }

    override fun toPayload(): Map<String, Any> {
        return hashMapOf(
                "room_id" to roomId,
                "id" to uuid
        )
    }
}

// Send & Received
class SignalingOffer(uid: String, rid: Long, dataType: String, src: Origin) : SignalingEvent(THE_LOWEST_PRIOTITY) {

    val uuid: String = uid
    val roomId: Long = rid
    val data: String = dataType

    init {
        origin = src
    }

    override fun toPayload(): Map<String, Any> {
        return hashMapOf(
                "room_id" to roomId,
                "id" to uuid,
                MessageType.OFFER.type to data
        )
    }
}

// Send & Received
class SignalingAnswer(uid:String, rid: Long, sdp: String, src: Origin): SignalingEvent(THE_LOWEST_PRIOTITY) {

    val uuid: String = uid
    val roomId: Long = rid
    val data: String = sdp

    init {
        origin = src
    }

    override fun toPayload(): Map<String, Any> {
        return hashMapOf(
                "room_id" to roomId,
                "id" to uuid,
                MessageType.ANSWER.type to data
        )
    }
}

// Send & Received
class SignalingCandidate(uid: String, rid: Long, candidate: String, src: Origin): SignalingEvent(THE_LOWEST_PRIOTITY) {

    val uuid: String = uid
    val roomId: Long = rid
    val data: String = candidate

    init {
        origin = src
    }

    override fun toPayload(): Map<String, Any> {
        return hashMapOf(
                "room_id" to roomId,
                "id" to uuid,
                MessageType.CANDIDATES.type to data
        )
    }
}

class SignalingRemoveCandidates(
    uid: String,
    rid: Long,
    candidates: String,
    src: Origin
): SignalingEvent(THE_LOWEST_PRIOTITY) {

    val uuid: String = uid
    val roomId: Long = rid
    val data: String = candidates

    init {
        origin = src
    }

    override fun toPayload(): Map<String, Any> {
        return hashMapOf(
                "room_id" to roomId,
                "id" to uuid,
                MessageType.CANDIDATES_REMOVE.type to data
        )
    }
}
