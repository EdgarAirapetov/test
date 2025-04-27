package com.numplates.nomera3.modules.services.ui.entity

import com.numplates.nomera3.modules.peoples.ui.content.entity.RecommendedPeopleUiEntity

sealed class MeeraServicesUiUpdate {

    data class UpdateBloggersList(
        val recommendationsList: List<RecommendedPeopleUiEntity>
    ) : MeeraServicesUiUpdate()

    data class UpdateCommunitiesList(
        val newModel: MeeraServicesCommunitiesUiModel
    ) : MeeraServicesUiUpdate()

}
