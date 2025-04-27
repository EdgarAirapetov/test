package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.data.network.ApiHiWay
import com.numplates.nomera3.data.network.core.ResponseWrapper
import io.reactivex.Single

class RemoveUserUseCase(private val repository: ApiHiWay) {

    fun removeUser(userId: Long): Single<ResponseWrapper<Boolean>> {
        return repository.removeUser(userId)
    }

    fun removeUserAndSaveSubscription(userId: Long): Single<ResponseWrapper<Boolean>> {
        return repository.removeUser(userId, "only_friend")
    }
}