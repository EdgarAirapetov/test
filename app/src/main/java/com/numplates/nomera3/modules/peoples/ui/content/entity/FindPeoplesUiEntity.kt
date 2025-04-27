package com.numplates.nomera3.modules.peoples.ui.content.entity

import androidx.annotation.DrawableRes
import com.numplates.nomera3.modules.peoples.ui.content.adapter.PeoplesContentType

data class FindPeoplesUiEntity(
    val label: String,
    val description: String,
    @DrawableRes val icon: Int,
    val contentType: FriendFindContentType,
    val isNeedToDrawSeparator: Boolean = true
) : PeoplesContentUiEntity {
    override fun getUserId(): Long? = null

    override fun getPeoplesActionType(): PeoplesContentType {
        return PeoplesContentType.FIND_FRIENDS_TYPE
    }
}
