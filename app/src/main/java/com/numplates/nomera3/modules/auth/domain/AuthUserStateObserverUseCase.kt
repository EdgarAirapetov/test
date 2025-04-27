package com.numplates.nomera3.modules.auth.domain

import com.numplates.nomera3.modules.auth.AuthUser
import com.numplates.nomera3.modules.auth.data.repository.AuthRepositoryImpl
import io.reactivex.subjects.ReplaySubject
import javax.inject.Inject

class AuthUserStateObserverUseCase @Inject constructor(
    private val repository: AuthRepositoryImpl
) {
    fun getObserver(): ReplaySubject<AuthUser> {
        return repository.getAuthUserStateObservable()
    }
}