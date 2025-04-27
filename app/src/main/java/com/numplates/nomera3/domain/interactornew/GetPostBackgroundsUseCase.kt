package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.modules.appInfo.data.entity.Settings
import com.numplates.nomera3.modules.appInfo.ui.entity.PostBackgroundItemUiModel
import com.numplates.nomera3.modules.appInfo.ui.mapper.PostBackgroundItemUIMapper
import javax.inject.Inject

class GetPostBackgroundsUseCase @Inject constructor(private val postBackgroundMapper: PostBackgroundItemUIMapper) {

    fun invoke(settings: Settings?): List<PostBackgroundItemUiModel> {
        return settings?.postBackgrounds?.map { postBackgroundItemDto ->
            postBackgroundMapper.map(postBackgroundItemDto)
        } ?: return emptyList()
    }
}
