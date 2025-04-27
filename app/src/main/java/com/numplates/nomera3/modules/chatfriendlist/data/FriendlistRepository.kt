package com.numplates.nomera3.modules.chatfriendlist.data

import android.annotation.SuppressLint
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.data.network.ApiHiWay
import com.numplates.nomera3.modules.chatfriendlist.domain.GetFriendlistUsecase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.suspendCoroutine

class FriendlistRepositoryImpl @Inject constructor(
    private val apiHiWay: ApiHiWay
): GetFriendlistUsecase.FriendlistRepository {

    @SuppressLint("CheckResult")
    override suspend fun getFriendsByName(
        nameQuery: String?,
        startingFrom: Int,
        howMuch: Int
    ): List<UserSimple>? {
        return withContext(Dispatchers.IO) {
            suspendCoroutine { continuation ->
                apiHiWay.searchUserSimple(
                    nameQuery,
                    CONFIRMED_FRIENDS_CODE,
                    howMuch,
                    startingFrom,
                    RESPONSE_DATA_TYPE
                ).subscribe(
                    { wrappedResponse ->
                        val data = wrappedResponse.data
                        continuation.resumeWith(Result.success(data))
                    },
                    { continuation.resumeWith(Result.success(null)) }
                )
            }
        }
    }

    companion object {
        private const val CONFIRMED_FRIENDS_CODE = 2
        private const val RESPONSE_DATA_TYPE = "UserSimple"
    }
}
