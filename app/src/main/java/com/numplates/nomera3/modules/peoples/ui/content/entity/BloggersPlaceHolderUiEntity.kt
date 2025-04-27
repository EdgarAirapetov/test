package com.numplates.nomera3.modules.peoples.ui.content.entity

import com.numplates.nomera3.modules.peoples.ui.content.adapter.PeoplesContentType

object BloggersPlaceHolderUiEntity : PeoplesContentUiEntity {

    override fun getUserId(): Long? = null

    override fun getPeoplesActionType(): PeoplesContentType = PeoplesContentType.BLOGGERS_PLACEHOLDER
}
