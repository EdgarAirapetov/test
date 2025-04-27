package com.numplates.nomera3.domain.interactornew

import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.FRIEND_STATUS_CONFIRMED
import com.numplates.nomera3.data.network.ApiHiWayKt
import com.numplates.nomera3.data.network.core.ResponseWrapper
import javax.inject.Inject

class SearchFriendsUseCase @Inject constructor(private val repository: ApiHiWayKt) {

    suspend fun invoke(
        query: String, limit: Int, offset: Int = 0
    ): ResponseWrapper<List<UserSimple?>?> {
        return repository.searchUserSimple(
            query, FRIEND_STATUS_CONFIRMED, limit, offset, "UserSimple"
        )
    }
}
