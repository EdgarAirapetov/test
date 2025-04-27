package com.numplates.nomera3.modules.peoples.ui.content.entity

import com.numplates.nomera3.modules.peoples.ui.content.adapter.PeoplesContentType

object RecommendedUsersShimmerUiEntity : PeoplesContentUiEntity {

    override fun getUserId(): Long? = null

    override fun getPeoplesActionType() = PeoplesContentType.RECOMMENDED_USERS_SHIMMER_TYPE
}
