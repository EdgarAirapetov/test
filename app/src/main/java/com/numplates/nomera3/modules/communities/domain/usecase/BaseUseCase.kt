package com.numplates.nomera3.modules.communities.domain.usecase

interface BaseUseCase<P : DefParams, T> {

    suspend fun execute(params: P): T
}

interface BaseUseCaseCoroutine<P : DefParams, T> {

    suspend fun execute(params: P, success: (T) -> Unit, fail: (Exception) -> Unit)
}

interface BaseUseCaseNoSuspend<P : DefParams, T> {

    suspend fun execute(params: P): T
}

open class DefParams