package com.numplates.nomera3.modules.communities.data.states

sealed class CommunityState {

    class OnSubscribe(
        val groupId: Int
    ): CommunityState()

    class OnUnsubscribe(
        val groupId: Int
    ): CommunityState()
}
