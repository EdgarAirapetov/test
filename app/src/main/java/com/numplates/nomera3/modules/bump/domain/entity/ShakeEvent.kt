package com.numplates.nomera3.modules.bump.domain.entity

sealed class ShakeEvent {

    data class TryToRegisterShakeEvent(val isNeedToRegister: Boolean) : ShakeEvent()

    data class ForceToRegisterShakeEvent(val isNeedToRegister: Boolean) : ShakeEvent()

    object ShakeUsersNotFound : ShakeEvent()
}
