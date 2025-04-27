package com.numplates.nomera3.modules.chat

import com.numplates.nomera3.modules.user.ui.entity.UserComplainEntity

interface ComplaintDialogChainCallback {
    fun onBlockReport(data: UserComplainEntity)
    fun onBlock()
    fun onCancel()
}
