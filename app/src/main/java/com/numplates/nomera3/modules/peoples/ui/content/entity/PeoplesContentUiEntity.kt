package com.numplates.nomera3.modules.peoples.ui.content.entity

import com.numplates.nomera3.modules.peoples.ui.content.adapter.PeoplesContentType

interface PeoplesContentUiEntity {
    fun getUserId(): Long?

    fun getPeoplesActionType() : PeoplesContentType
}
