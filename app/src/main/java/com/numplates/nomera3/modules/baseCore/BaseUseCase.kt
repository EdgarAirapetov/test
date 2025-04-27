package com.numplates.nomera3.modules.baseCore

import java.lang.Exception

@Deprecated("Use common use case")
interface BaseUseCase<P : DefParams, T> {

    suspend fun execute(params: P): T
}

@Deprecated("Use common use case")
interface BaseUseCaseCoroutine<P : DefParams, T> {

    suspend fun execute(params: P, success: (T) -> Unit, fail: (Throwable) -> Unit)
}

@Deprecated("Use common use case")
interface BaseUseCaseCoroutineWithoutParams<T> {

    suspend fun execute(success: (T) -> Unit, fail: (Exception) -> Unit)
}

@Deprecated("Use common use case")
interface BaseUseCaseNoSuspend<P : DefParams, T> {

    fun execute(params: P): T
}

@Deprecated("Use common use case")
interface BaseUseCaseNoSuspendWithoutParams<T> {

    fun execute(): T
}

open class DefParams
