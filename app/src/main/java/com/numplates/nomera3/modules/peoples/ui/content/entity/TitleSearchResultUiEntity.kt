package com.numplates.nomera3.modules.peoples.ui.content.entity

import androidx.annotation.StringRes
import com.numplates.nomera3.modules.peoples.ui.content.adapter.PeoplesContentType

data class TitleSearchResultUiEntity(
    @StringRes val titleResource: Int
) : PeoplesContentUiEntity {

    override fun getUserId(): Long? = null

    override fun getPeoplesActionType(): PeoplesContentType = PeoplesContentType.TITLE_SEARCH_RESULT
}
