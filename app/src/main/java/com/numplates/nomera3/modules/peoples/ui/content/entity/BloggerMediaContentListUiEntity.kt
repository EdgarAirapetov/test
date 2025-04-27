package com.numplates.nomera3.modules.peoples.ui.content.entity

import com.numplates.nomera3.modules.peoples.ui.content.adapter.PeoplesContentType
import com.numplates.nomera3.modules.peoples.ui.content.entity.blogger.BloggerMediaContentUiEntity

data class BloggerMediaContentListUiEntity(
    private val userId: Long,
    val bloggerPostList: List<BloggerMediaContentUiEntity>
) : PeoplesContentUiEntity {
    override fun getUserId(): Long = userId

    override fun getPeoplesActionType(): PeoplesContentType {
        return PeoplesContentType.BLOGGER_MEDIA_CONTENT_TYPE
    }

}
