package com.numplates.nomera3.modules.peoples.ui.content.entity

import com.numplates.nomera3.modules.peoples.ui.content.adapter.PeoplesContentType

object PeopleSyncContactsUiModel : PeoplesContentUiEntity {
    override fun getUserId(): Long? {
        return null
    }

    override fun getPeoplesActionType(): PeoplesContentType {
        return PeoplesContentType.CONTACT_SYNC_TYPE
    }
}
