package com.numplates.nomera3.modules.user.data.repository

import com.numplates.nomera3.modules.user.data.api.UserComplainApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class UserComplainRepositoryImpl @Inject constructor(
        private val userComplainApi: UserComplainApi
) : UserComplainRepository {

    override suspend fun complainOnUser(
            userId: Long,
            reasonId: Int,
            success: (Boolean) -> Unit,
            fail: (Exception) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                val body = hashMapOf<String, Any>(
                    "user_id" to userId,
                    "reason_id" to reasonId
                )
                userComplainApi.complainOnUser(body)
                success(true)
            } catch (e: Exception) {
                Timber.e(e)
                fail(e)
            }
        }
    }

    override suspend fun complainOnUserFromChat(userId: Long, reasonId: Int, roomId: Long) {
        userComplainApi.complainOnUserFromChat(userId = userId, reasonId = reasonId, roomId = roomId)
    }
}
