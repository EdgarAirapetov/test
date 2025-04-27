package com.numplates.nomera3.modules.auth.util

import androidx.fragment.app.Fragment
import com.meera.core.preferences.AppSettings
import com.numplates.nomera3.Act
import com.numplates.nomera3.modules.auth.AuthStatus
import com.numplates.nomera3.modules.auth.data.AuthStatusMapper
import com.numplates.nomera3.modules.common.ActivityToolsProvider
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.presentation.view.fragments.UserInfoFragment


// TODO: Убрать не используемые методы после редизайна https://nomera.atlassian.net/browse/BR-30859


/**
 * Проверку на авторизацию можно выполнить 3-мя способами:
 *
 * 1. Добавив открываемый Фрагмент в FRAGMENTS_NEED_AUTH
 * 2. Обрамив код требующий авторизации в needAuth или needAuthAndReturnStatus
 * 3. Реализовав интерфейс IAuthStateObserver (для реактивной реакции на изменения авторизации)
 *
 * (needAuthAndReturnStatus отличается от needAuth только тем, что возвращает
 * boolean значение – авторизован ли пользователь на данный момент или нет)
 */
val FRAGMENTS_NEED_AUTH = listOf<Class<out Fragment>>(
    UserInfoFragment::class.java
)

fun Fragment.isNeedAuth(): Boolean {
    return FRAGMENTS_NEED_AUTH.any { checkedClass -> this::class.java == checkedClass }
}

fun Fragment.needAuth(complete: (Boolean) -> Unit) {
    this.needAuthAndReturnStatus(complete)
}

fun Fragment.needAuth() {
    this.needAuthAndReturnStatus()
}

fun Fragment.needAuthAndReturnStatus(): Boolean {
    val navigator = (activity as? ActivityToolsProvider)?.getMeeraAuthenticationNavigator() ?: return false

    return navigator.needAuth(null)
}

fun Fragment.needAuthAndReturnStatus(complete: (Boolean) -> Unit): Boolean {
    val navigator = (activity as? ActivityToolsProvider)?.getMeeraAuthenticationNavigator() ?: return false

    return navigator.needAuth(complete)
}

fun Act.needAuth(complete: (Boolean) -> Unit) {
    this.needAuthAndReturnStatus(complete)
}

fun Act.needAuthAndReturnStatus(complete: (Boolean) -> Unit): Boolean {
    val navigator = (this as? ActivityToolsProvider)?.getAuthenticationNavigator() ?: return false

    return navigator.needAuth(complete)
}

fun MeeraAct.needAuth(complete: (Boolean) -> Unit) {
    this.needAuthAndReturnStatus(complete)
}

fun MeeraAct.needAuthAndReturnStatus(complete: (Boolean) -> Unit): Boolean {
    val navigator = (this as? ActivityToolsProvider)?.getMeeraAuthenticationNavigator() ?: return false
    return navigator.needAuth(complete)
}

fun AppSettings.isAuthorizedUser(): Boolean {
    val authStatusPref = this.readIsUserAuthorized()
    return when (AuthStatusMapper().mapFromPref(authStatusPref)) {
        is AuthStatus.Authorized -> true
        else -> false
    }
}
