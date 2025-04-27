package com.numplates.nomera3.modules.appInfo.ui.mapper

import com.meera.core.extensions.empty
import com.numplates.nomera3.modules.appInfo.data.entity.PostBackgroundItemDto
import com.numplates.nomera3.modules.appInfo.ui.entity.PostBackgroundItemUiModel
import javax.inject.Inject

class PostBackgroundItemUIMapper @Inject constructor() {
    fun map(postBackgroundItemDto: PostBackgroundItemDto) = PostBackgroundItemUiModel(
        id = postBackgroundItemDto.id ?: PostBackgroundItemUiModel.DEFAULT_ID,
        url = postBackgroundItemDto.url ?: String.empty(),
        previewUrl = postBackgroundItemDto.previewUrl ?: String.empty(),
        fontColor = postBackgroundItemDto.fontColor ?: String.empty()
    )
}
