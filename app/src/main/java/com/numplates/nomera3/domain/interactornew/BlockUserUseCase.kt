package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.data.network.ApiHiWay
import com.numplates.nomera3.data.network.BlockRequest

class BlockUserUseCase(private val repository: ApiHiWay?) {

    fun blockUser(userId: Long, remoteUserId: Long, isBlock: Boolean = true) =
            repository?.setBlockedStatusToUser(userId, BlockRequest(remoteUserId, isBlock))

}