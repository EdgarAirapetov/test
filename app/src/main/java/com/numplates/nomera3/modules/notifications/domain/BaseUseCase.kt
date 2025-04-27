package com.numplates.nomera3.modules.notifications.domain

interface BaseUseCase<P : DefParams, T> {

    fun execute(params: P): T
}

open class DefParams
