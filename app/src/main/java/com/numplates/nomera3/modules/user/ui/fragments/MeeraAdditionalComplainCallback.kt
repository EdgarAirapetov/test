package com.numplates.nomera3.modules.user.ui.fragments

import com.numplates.nomera3.modules.complains.ui.ComplainEvents

interface MeeraAdditionalComplainCallback {
    fun onSuccess(msg: Int?, reason: ComplainEvents, userId: Long?)
    fun onError(msg: Int?)
}
