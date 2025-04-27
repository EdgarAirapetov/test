package com.numplates.nomera3.modules.registration

import com.numplates.nomera3.modules.auth.util.AuthRequester

class TestAuthRequester(private val isAuthed: Boolean) : AuthRequester {

    private var completeAction: ((Boolean) -> Unit)? = null

    override fun requestAuthAndRun(complete: (Boolean) -> Unit) {
        completeAction = complete
        if (isAuthed) complete.invoke(false)
    }

    fun getCompleteAction() = completeAction!!
}
