package com.numplates.nomera3.modules.communities.ui.viewmodel

import com.numplates.nomera3.modules.communities.data.entity.Communities
import com.numplates.nomera3.modules.communities.data.entity.CommunityEntity
import com.numplates.nomera3.modules.communities.ui.entity.CommunityListItemUIModel
import com.numplates.nomera3.modules.communities.ui.entity.CommunityMemberRole

class CommunityModelMapper {

    fun map(communities: Communities): List<CommunityListItemUIModel> {
        return communities.communityEntities
            ?.filterNotNull()
            ?.map {
                CommunityListItemUIModel(
                    id = it.groupId,
                    coverImage = it.avatar ?: "",
                    isPrivate = it.private == 1,
                    isMember = it.isSubscribed == 1,
                    isCreator = it.isAuthor == 1,
                    isModerator = it.isModerator == 1,
                    name = it.name ?: "",
                    memberCount = it.users,
                    isUserApproved = it.userStatus != CommunityEntity.USER_STATUS_NOT_YET_APPROVED,
                    memberRole = getMemberRole(it),
                    userStatus = it.userStatus
                )
            }
            ?: emptyList()
    }

    companion object {

        fun getMemberRole(it: CommunityEntity?): CommunityMemberRole {
            if (it?.isAuthor == 1) return CommunityMemberRole.CREATOR
            if (it?.isModerator == 1) return CommunityMemberRole.MODERATOR
            return CommunityMemberRole.MEMBER
        }
    }
}
