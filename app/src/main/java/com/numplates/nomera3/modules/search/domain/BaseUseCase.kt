package com.numplates.nomera3.modules.search.domain

import java.lang.Exception

interface BaseUseCase<P : DefParams, T> {

    suspend fun execute(params: P): T
}

interface BaseUseCaseCoroutine<P : DefParams, T> {

    suspend fun execute(params: P, success: (T) -> Unit, fail: (Exception) -> Unit)
}

open class DefParams