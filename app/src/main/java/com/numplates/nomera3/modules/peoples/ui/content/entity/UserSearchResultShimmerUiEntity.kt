package com.numplates.nomera3.modules.peoples.ui.content.entity

import com.numplates.nomera3.modules.peoples.ui.content.adapter.PeoplesContentType

object UserSearchResultShimmerUiEntity : PeoplesContentUiEntity {

    override fun getUserId(): Long? = null

    override fun getPeoplesActionType() = PeoplesContentType.SEARCH_RESULT_SHIMMER_TYPE
}
