package com.numplates.nomera3.modules.communities.domain.mapper

import com.numplates.nomera3.modules.communities.data.entity.Community
import com.numplates.nomera3.modules.communities.data.entity.CommunityEntity
import com.numplates.nomera3.modules.communities.ui.entity.CommunityInformationScreenUIModel

class CommunityInfoResponseMapper {

    fun map(response: Community?): CommunityInformationScreenUIModel? {
        return response?.community?.let { community: CommunityEntity ->
            CommunityInformationScreenUIModel(
                communityName = community.name,
                communityCoverImageURL = community.imageUrl,
                communityMembersCount = community.users.toString(),
                communityBlacklistMembersCount = community.blockedUsers.toString()
            )
        }
    }
}
