package com.numplates.nomera3.modules.peoples.ui.content.entity

sealed class UiPeopleUpdate {

    data class UpdateBloggersList(
        val recommendationsList: List<RecommendedPeopleUiEntity>
    ) : UiPeopleUpdate()
}
