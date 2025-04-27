package com.numplates.nomera3.modules.gifservice.domain.mapper

import com.meera.db.models.RecentGifEntity
import com.numplates.nomera3.modules.gifservice.ui.entity.GiphyEntity

class GiphyDbMapper {

    fun map(data: List<RecentGifEntity>): List<GiphyEntity> {
        return data.map { recentGifEntity ->
            GiphyEntity(
                id = recentGifEntity.id,
                smallUrl = recentGifEntity.smallUrl,
                originalUrl = recentGifEntity.originalUrl,
                originalAspectRatio = recentGifEntity.originalAspectRatio
            )
        }
    }

}
