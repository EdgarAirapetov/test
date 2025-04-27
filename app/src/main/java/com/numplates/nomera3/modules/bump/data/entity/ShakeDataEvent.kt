package com.numplates.nomera3.modules.bump.data.entity

sealed class ShakeDataEvent {

    /**
     * Данный эвент попытается зарегистрировать ShakeEventListener
     * Не будет зарегистрирован в том случае, если экран не входит в данное условие
     * [com.numplates.nomera3.modules.bump.ui.isAllowToRegisterShakeInCurrentScreen]
     */
    data class TryToRegisterShakeEvent(val isNeedToRegister: Boolean) : ShakeDataEvent()

    /**
     * Эвент зарегистрирует ShakeEventListener, но только в том случае, если
     * пользователь зарегистрирован
     */
    data class ForceToRegisterShakeEvent(val isNeedToRegister: Boolean) : ShakeDataEvent()

    object ShakeUserNotFoundEvent : ShakeDataEvent()
}
