package com.numplates.nomera3.telecom

interface CallProgressEvents {
    fun callIsStopped()
    fun callIsRejected()
    fun callIsAccepted(roomIdInProc: Long, uuid: String? = null)
    fun lineIsBusy()
}