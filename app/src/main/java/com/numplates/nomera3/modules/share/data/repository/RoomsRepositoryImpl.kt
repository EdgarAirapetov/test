package com.numplates.nomera3.modules.share.data.repository


import com.numplates.nomera3.modules.share.data.api.ShareApi
import com.numplates.nomera3.modules.share.data.entity.ResponseShareItem
import timber.log.Timber
import javax.inject.Inject

private const val DEFAULT_LIMIT = 30

class RoomsRepositoryImpl @Inject constructor(
    private val api: ShareApi
) : RoomsRepository {

    override suspend fun getShareItems(
        lastContactId: String?,
        selectedUserId: Long?,
        success: (users: List<ResponseShareItem>) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            val response = api.getShareItems(
                limit = DEFAULT_LIMIT,
                query = null,
                lastId = lastContactId,
                selectedUserId = selectedUserId
            )
            response.data?.let(success) ?: kotlin.run { fail(error("empty response")) }
        } catch (e: Exception) {
            Timber.e(e)
            fail(e)
        }
    }

    override suspend fun searchShareItems(
        query: String,
        lastContactId: String?,
        success: (users: List<ResponseShareItem>) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            val response = api.getShareItems(
                limit = DEFAULT_LIMIT,
                query = query,
                lastId = lastContactId,
                selectedUserId = null
            )
            response.data?.let(success) ?: kotlin.run { fail(error("empty response")) }
        } catch (e: Exception) {
            Timber.e(e)
            fail(e)
        }
    }
}
