package com.numplates.nomera3.modules.newroads.data.entities

import com.google.gson.Gson
import com.meera.core.network.websocket.WebSocketMainChannel
import com.numplates.nomera3.modules.auth.AuthStatus
import com.numplates.nomera3.modules.auth.domain.AuthUserStateObserverUseCase
import com.numplates.nomera3.presentation.utils.makeEntity
import io.reactivex.Observable

open class GetSubscriptionMomentListenerUseCase(
    webSocket: WebSocketMainChannel,
    private val gson: Gson,
    private val authObserver: AuthUserStateObserverUseCase
) {
    private val subject: Observable<SubscriptionNewPostEntity> = webSocket.getSubscriptionNewMomentMessages()
        .map { it.payload.makeEntity<SubscriptionNewPostEntity>(gson) }

    fun execute(): Observable<SubscriptionNewPostEntity> {
        return authObserver.getObserver().flatMap { domainAuthUser ->
            val newStatus = domainAuthUser.getReadyAuthStatus()

            if (newStatus is AuthStatus.Authorized) {
                subject
            } else {
                Observable.just(SubscriptionNewPostEntity(false))
            }
        }
    }
}
