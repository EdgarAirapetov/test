package com.numplates.nomera3.modules.registration.ui

import com.meera.core.di.scopes.AppScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

/**
 * Интерфейс, когда нужно получить изменения статуса регистрации/авторизации
 */
interface AuthFinishListener {
    /**
     * Должен вызываться тогда, когда юзер должен залогиниться
     */
    suspend fun setAuthStatusChanged()

    /**
     * Должен вызываться тогда, когда юзер успешно зарегистрировался
     */
    suspend fun setRegistrationStatusChanged()

    /**
     * Метод должен слушать изменения Flow, когда юзер зарегистрировался
     */
    fun observeRegistrationFinishListener() : SharedFlow<Unit>

    /**
     * Метод должен слушать изменения Flow, когда юзер авторизовался
     */
    fun observeAuthFinishListener() : SharedFlow<Unit>
}

@AppScope
class AuthFinishListenerImpl @Inject constructor() : AuthFinishListener {

    private val authEvent = MutableSharedFlow<Unit>()
    private val registrationEvent = MutableSharedFlow<Unit>()

    override suspend fun setAuthStatusChanged() {
        authEvent.emit(Unit)
    }

    override suspend fun setRegistrationStatusChanged() {
        registrationEvent.emit(Unit)
    }

    override fun observeRegistrationFinishListener(): SharedFlow<Unit> = registrationEvent

    override fun observeAuthFinishListener(): SharedFlow<Unit> = authEvent
}
