package com.numplates.nomera3.modules.communities.data.states

sealed class CommunityListEvents {

    class StartDeletion(
        val communityId: Long
    ) : CommunityListEvents()

    class DeleteSuccess(val groupId: Int) : CommunityListEvents()
    class CancelDeletion : CommunityListEvents()
    class CreateSuccess : CommunityListEvents()

}
