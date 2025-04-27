package com.numplates.nomera3.modules.services.data.mapper

import com.meera.core.extensions.empty
import com.numplates.nomera3.modules.communities.data.entity.Communities
import com.numplates.nomera3.modules.communities.data.entity.CommunityEntity
import com.numplates.nomera3.modules.services.domain.entity.ServicesCommunitiesModel
import com.numplates.nomera3.modules.services.domain.entity.ServicesCommunityModel
import javax.inject.Inject

class ServiceCommunitiesModelMapper @Inject constructor() {

    fun mapCommunitiesModel(src: Communities): ServicesCommunitiesModel {
        return ServicesCommunitiesModel(
            src.totalCount ?: 0,
            src.communityEntities?.mapNotNull(::mapCommunity) ?: listOf()
        )
    }

    private fun mapCommunity(src: CommunityEntity?): ServicesCommunityModel? {
        return if (src == null) null else ServicesCommunityModel(
            src.groupId,
            src.name ?: String.empty(),
            src.avatar ?: String.empty()
        )
    }

}
