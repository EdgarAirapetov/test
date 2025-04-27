package com.numplates.nomera3.modules.share.data.repository

import com.numplates.nomera3.modules.share.data.entity.ResponseShareItem

interface RoomsRepository {

    suspend fun getShareItems(
        lastContactId: String?,
        selectedUserId: Long?,
        success: (users: List<ResponseShareItem>) -> Unit,
        fail: (Exception) -> Unit
    )

    suspend fun searchShareItems(
        query: String,
        lastContactId: String?,
        success: (users: List<ResponseShareItem>) -> Unit,
        fail: (Exception) -> Unit
    )
}
