package com.numplates.nomera3.modules.user.data.repository

interface UserComplainRepository {

    suspend fun complainOnUser(
            userId: Long,
            reasonId: Int,
            success: (Boolean) -> Unit,
            fail: (Exception) -> Unit
    )

    suspend fun complainOnUserFromChat(
        userId: Long,
        reasonId: Int,
        roomId: Long
    )
}
