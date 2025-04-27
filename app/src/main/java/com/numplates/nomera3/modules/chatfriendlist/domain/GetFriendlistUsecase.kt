package com.numplates.nomera3.modules.chatfriendlist.domain

import com.meera.db.models.userprofile.UserSimple
import javax.inject.Inject

class GetFriendlistUsecase @Inject constructor(private val repository: FriendlistRepository) {

    interface FriendlistRepository {
        suspend fun getFriendsByName(
            nameQuery: String?,
            startingFrom: Int,
            howMuch: Int
        ): List<UserSimple>?
    }

    suspend fun invoke(
        nameQuery: String? = null,
        startingFrom: Int = DEFAULT_FRIEND_LIST_STARTING_POSITION,
        howMuch: Int = DEFAULT_FRIEND_LIST_PAGE_SIZE
    ) : List<UserSimple>? {
        return repository.getFriendsByName(nameQuery, startingFrom, howMuch)
    }

    companion object {
        private const val DEFAULT_FRIEND_LIST_PAGE_SIZE = 20
        private const val DEFAULT_FRIEND_LIST_STARTING_POSITION = 0
    }
}
