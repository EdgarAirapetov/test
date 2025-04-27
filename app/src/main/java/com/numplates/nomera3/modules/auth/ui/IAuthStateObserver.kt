package com.numplates.nomera3.modules.auth.ui

import com.numplates.nomera3.modules.auth.util.AuthStatusObserver

/**
 * Классы состояние которых нужно менять реактивно в зависимости от авторизации
 * должны реализовывать этот интерфейс
 *
 * Класс обязан вызывать свой метод initAuthObserver() как будет готов реагировать на изменение авторизации
 * (обычно вызов initAuthObserver() совершается в onCreate или в onViewCreated)
 */
interface IAuthStateObserver {
    fun initAuthObserver(): AuthStatusObserver
}