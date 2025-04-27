package com.numplates.nomera3.modules.chat.toolbar.domain.usecase

import com.google.gson.Gson
import com.numplates.nomera3.modules.chat.toolbar.data.entity.UpdatedUserResponse
import com.numplates.nomera3.modules.chat.toolbar.data.repository.ChatToolbarRepositoryImpl
import com.numplates.nomera3.presentation.utils.makeEntity
import io.reactivex.Observable
import javax.inject.Inject

class UpdateUserDataObserverUseCase @Inject constructor(
        private val repository: ChatToolbarRepositoryImpl,
        private val gson: Gson
){

    fun observeUserData(): Observable<UpdatedUserResponse> =
            repository.updateUserDataObserver().map {
                message -> message.payload.makeEntity<UpdatedUserResponse>(gson)
            }
}