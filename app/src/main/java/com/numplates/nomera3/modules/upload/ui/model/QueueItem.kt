package com.numplates.nomera3.modules.upload.ui.model

data class QueueItem<T>(
    val payload: T,
    val minDuration: Long? = null,
    val maxDuration: Long? = null
)
