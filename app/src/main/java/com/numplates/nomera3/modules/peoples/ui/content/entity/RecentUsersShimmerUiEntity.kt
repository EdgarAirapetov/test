package com.numplates.nomera3.modules.peoples.ui.content.entity

import com.numplates.nomera3.modules.peoples.ui.content.adapter.PeoplesContentType

object RecentUsersShimmerUiEntity : PeoplesContentUiEntity {
    override fun getUserId(): Long? = null

    override fun getPeoplesActionType(): PeoplesContentType {
        return PeoplesContentType.RECENT_SHIMMER_TYPE
    }
}
