package com.numplates.nomera3.modules.peoples.ui.content.entity

import com.numplates.nomera3.modules.peoples.ui.content.adapter.PeoplesContentType

data class RecentUsersUiEntity(
    val users: List<RecentUserUiModel>
) : PeoplesContentUiEntity {

    override fun getUserId(): Long? = null

    override fun getPeoplesActionType(): PeoplesContentType = PeoplesContentType.RECENT_USERS
}
