package com.numplates.nomera3

interface RestartCallback {

    fun restartActivity(action: suspend () -> Unit)
}
