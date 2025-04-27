package com.numplates.nomera3.telecom

class EmptyCallProgress : CallProgressEvents {
    override fun callIsStopped() = Unit
    override fun callIsRejected() = Unit
    override fun callIsAccepted(roomIdInProc: Long, uuid: String?) = Unit
    override fun lineIsBusy() = Unit
}
