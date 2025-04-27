package com.numplates.nomera3.telecom

import org.appspot.apprtc.AppRTCClient
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

class EmptyWebrtcSignaling : AppRTCClient.SignalingEvents {
    override fun onConnectedToRoom(params: AppRTCClient.SignalingParameters) = Unit
    override fun onRemoteDescription(sdp: SessionDescription) = Unit
    override fun onRemoteIceCandidate(candidate: IceCandidate) = Unit
    override fun onRemoteIceCandidatesRemoved(candidates: Array<IceCandidate>) = Unit
    override fun onChannelClose() = Unit
    override fun onChannelError(description: String) = Unit
}
