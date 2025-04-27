package com.numplates.nomera3.modules.userprofile.data.repository

import com.meera.core.network.websocket.WebSocketMainChannel
import io.reactivex.Single
import org.phoenixframework.Message
import javax.inject.Inject
private const val PROFILE_USER_TYPE = "user_type"

class GetProfileRepositoryImpl @Inject constructor(
    val webSocketMainChannel: WebSocketMainChannel): GetProfileRepository {

    override fun getProfile(): Single<Message> {
        val payload = mutableMapOf(PROFILE_USER_TYPE to "UserProfile")
        return webSocketMainChannel.pushGetProfile(payload)
    }
}
