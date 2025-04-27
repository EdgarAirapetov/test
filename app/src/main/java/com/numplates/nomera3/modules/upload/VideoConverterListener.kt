package com.numplates.nomera3.modules.upload

interface VideoConverterListener {

    fun onStarted()

    fun onProgress(progress: Float)

    fun onCompleted()

    fun onCancelled()

    fun onError(cause: Throwable?)

}