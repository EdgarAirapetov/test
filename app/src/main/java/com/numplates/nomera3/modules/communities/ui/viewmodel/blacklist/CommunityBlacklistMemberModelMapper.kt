package com.numplates.nomera3.modules.communities.ui.viewmodel.blacklist

import com.meera.core.extensions.empty
import com.numplates.nomera3.modules.communities.data.entity.CommunityMembersEntity
import com.numplates.nomera3.modules.communities.ui.fragment.blacklist.CommunityBlacklistUIModel

class CommunityBlacklistMemberModelMapper {

    fun map(
        blacklistedMembersEntity: CommunityMembersEntity?
    ): List<CommunityBlacklistUIModel.BlacklistedMemberUIModel> {
        return blacklistedMembersEntity
            ?.users
            ?.filterNotNull()
            ?.map {
                CommunityBlacklistUIModel.BlacklistedMemberUIModel(
                    memberId = it.uid,
                    memberPhotoUrl = it.avatarSmall ?: it.avatar ?: it.avatarBig?: String.empty(),
                    memberName = it.name ?: String.empty(),
                    uniqueName = it.uniqname ?: String.empty()
                )
            }
            ?: listOf()
    }
}
