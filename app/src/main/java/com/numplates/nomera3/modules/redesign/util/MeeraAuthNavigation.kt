package com.numplates.nomera3.modules.redesign.util

import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.safeNavigate
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.auth.AuthStatus
import com.numplates.nomera3.modules.auth.ui.AuthViewModel
import com.numplates.nomera3.modules.redesign.MeeraAct

class MeeraAuthNavigation(rootActivity: AppCompatActivity) {

    private val authViewModel by rootActivity.viewModels<AuthViewModel>()

    var isAuthorized: Boolean = false
        private set
        get() = authViewModel.authStatus.value is AuthStatus.Authorized

    val authStatusLive: LiveData<AuthStatus>
        get() = authViewModel.authStatus

}

fun NavController.needAuthToNavigate(act: MeeraAct, action: (NavController) -> Unit) {
    val navigator = act.getMeeraAuthNavigation()

    if (navigator.isAuthorized) {
        action(this)
    } else {
        NavigationManager.getManager().topNavController.safeNavigate(R.id.meeraRegistrationContainer)
    }
}

fun Fragment.needAuthToNavigateWithResult(action: () -> Unit): Boolean {
    needAuthToNavigate(action)
    return isAuthorized()
}

fun Fragment.needAuthToNavigate(action: () -> Unit) {
    if (isAuthorized()) {
        action()
    } else {
        NavigationManager.getManager().topNavController.safeNavigate(R.id.meeraRegistrationContainer)
    }
}

private const val DELAY_PERFORM_ACTION = 400L

fun Fragment.needAuthToNavigateWithResult(requestKey: String, action: () -> Unit): Boolean {
    val needAuth = isAuthorized().not()
    if (needAuth) {
        val act = activity as? MeeraAct ?: error("Activity is not MeeraAct")
        act.supportFragmentManager.setFragmentResultListener(requestKey, this) { _requestKey, _ ->
            if (requestKey == _requestKey) {
                doDelayed(DELAY_PERFORM_ACTION) { action() }
                act.supportFragmentManager.clearFragmentResult(requestKey)
            }
        }
    }

    needAuthToNavigate(action)
    return needAuth
}

fun Fragment.isAuthorized(): Boolean {
    val act = activity as? MeeraAct ?: error("Activity is not MeeraAct")
    val navigator = act.getMeeraAuthNavigation()
    return navigator.isAuthorized
}
