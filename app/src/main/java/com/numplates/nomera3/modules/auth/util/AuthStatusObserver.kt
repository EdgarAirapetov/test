package com.numplates.nomera3.modules.auth.util

import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.LifecycleOwner
import com.numplates.nomera3.modules.auth.AuthStatus
import com.numplates.nomera3.modules.auth.ui.AuthViewModel

abstract class AuthStatusObserver(rootActivity: ComponentActivity, lifecycleOwner: LifecycleOwner) {

    private val authViewModel by rootActivity.viewModels<AuthViewModel>()

    private var currentStatus: AuthStatus? = null

    init {
        authViewModel.authStatus.observe(lifecycleOwner) { authStatus ->
            if (currentStatus != authStatus) {
                val isJustAuth = authStatus is AuthStatus.Authorized && currentStatus !is
                    AuthStatus.Authorized && currentStatus != null

                currentStatus = authStatus

                if (isJustAuth) {
                    onJustAuthEvent()
                }

                onAuthStatusChange(authStatus)
            }
        }
    }

    fun forceUpdate() {
        onAuthStatusChange(currentStatus)
    }

    private fun onAuthStatusChange(status: AuthStatus?) {
        if (status is AuthStatus.Authorized) {
            onAuthState()
        } else {
            onNotAuthState()
        }
    }

    abstract fun onAuthState()
    abstract fun onNotAuthState()

    /**
     * Событие означающее, что только что был закончен процесс авторизации/регистрации
     */
    protected open fun onJustAuthEvent() {}
}
