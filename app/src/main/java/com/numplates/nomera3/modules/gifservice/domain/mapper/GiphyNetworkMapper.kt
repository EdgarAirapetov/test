package com.numplates.nomera3.modules.gifservice.domain.mapper

import com.numplates.nomera3.modules.gifservice.data.entity.GiphyItemResponse
import com.numplates.nomera3.modules.gifservice.ui.entity.GiphyEntity
import com.meera.core.extensions.empty

class GiphyNetworkMapper {

    fun map(response: List<GiphyItemResponse?>): List<GiphyEntity> {
        return response.map { _response ->
            val original = _response?.images?.original
            val width = original?.width?.toDouble() ?: 0.0
            val height = original?.height?.toDouble() ?: 1.0
            GiphyEntity(
                id = _response?.id ?: String.empty(),
                smallUrl = _response?.images?.fixedHeightSmall?.url ?: String.empty(),
                originalUrl = original?.url ?: String.empty(),
                originalAspectRatio = width / height
            )
        }
    }
}
