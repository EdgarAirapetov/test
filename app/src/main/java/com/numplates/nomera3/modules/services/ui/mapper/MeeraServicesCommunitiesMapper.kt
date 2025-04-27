package com.numplates.nomera3.modules.services.ui.mapper

import com.numplates.nomera3.modules.services.domain.entity.ServicesCommunitiesModel
import com.numplates.nomera3.modules.services.domain.entity.ServicesCommunityModel
import com.numplates.nomera3.modules.services.ui.entity.MeeraServicesCommunitiesUiModel
import com.numplates.nomera3.modules.services.ui.entity.ServicesCommunityUiModel
import javax.inject.Inject

class MeeraServicesCommunitiesMapper @Inject constructor() {

    fun mapCommunities(src: ServicesCommunitiesModel): MeeraServicesCommunitiesUiModel {
        return MeeraServicesCommunitiesUiModel(
            totalCount = src.totalCount,
            communities = src.communities.map(::mapCommunity)
        )
    }

    private fun mapCommunity(src: ServicesCommunityModel): ServicesCommunityUiModel {
        return ServicesCommunityUiModel(
            id = src.id,
            name = src.name,
            avatarUrl = src.avatarUrl
        )
    }

}
