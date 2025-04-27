package com.numplates.nomera3.modules.bump.ui

/**
 * Данный interface должен быть реализован во фрагменте, когда нужна кастомная подписка на ShakeEventListener
 */
interface ShakeRegisterUiHandler {
    /**
     * Вызывается в onResumed() в [ShakeEventDelegateUi.GlobalFragmentsLifecycleImpl], когда
     * необходима кастомная реализация подписки ShakeEventListener
     */
    fun registerShake()
}
