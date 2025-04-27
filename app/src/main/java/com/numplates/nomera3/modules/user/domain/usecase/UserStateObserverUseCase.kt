package com.numplates.nomera3.modules.user.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseNoSuspend
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.search.data.states.UserState
import com.numplates.nomera3.modules.user.data.repository.UserRepositoryImpl
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class UserStateObserverUseCase @Inject constructor(
    private val repository: UserRepositoryImpl
) : BaseUseCaseNoSuspend<AddUserToFriendObserverParams, PublishSubject<UserState>> {

    override fun execute(params: AddUserToFriendObserverParams): PublishSubject<UserState> {
        return repository.getUserStateObserver()
    }

}

class AddUserToFriendObserverParams: DefParams()