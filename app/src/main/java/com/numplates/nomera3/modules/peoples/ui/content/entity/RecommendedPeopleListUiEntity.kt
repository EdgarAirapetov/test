package com.numplates.nomera3.modules.peoples.ui.content.entity

import com.numplates.nomera3.modules.peoples.ui.content.adapter.PeoplesContentType

data class RecommendedPeopleListUiEntity(
    val recommendedPeopleList: List<RecommendedPeopleUiEntity>,
    val showPossibleFriendsText: Boolean
) : PeoplesContentUiEntity {
    override fun getUserId(): Long? = null

    override fun getPeoplesActionType(): PeoplesContentType {
        return PeoplesContentType.RECOMMENDED_PEOPLE
    }

}
