package com.numplates.nomera3.modules.search.data.states

sealed class UserState {

    class AddUserToFriendSuccess(
        val userId: Long,
        val friendStatus: Int
    ): UserState()

    class CancelFriendRequest(
        val userId: Long,
        val friendStatus: Int
    ): UserState()

    class BlockStatusUserChanged(
        val userId: Long,
        val isBlocked: Boolean
    ): UserState()

}
