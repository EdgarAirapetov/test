package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.data.network.ApiMain

class GetFriendsUseCase(private val repository: ApiMain) {

    suspend fun getConfirmedFriends(userId: Long, limit: Int, offset: Int) =
        repository.getFriendsList(userId, limit, offset, 2, "UserSimple")

    suspend fun getOutgoingFriends(userId: Long, limit: Int, offset: Int) =
            repository.getFriendsList(userId, limit, offset, 3, "UserSimple")

    suspend fun getIncomingFriends(userId: Long, limit: Int, offset: Int) =
        repository.getFriendsList(userId, limit, offset, 1, "UserSimple")

}