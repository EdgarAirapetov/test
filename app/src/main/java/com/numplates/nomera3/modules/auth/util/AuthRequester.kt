package com.numplates.nomera3.modules.auth.util

interface AuthRequester {
    fun requestAuthAndRun(complete: (Boolean) -> Unit)
}
