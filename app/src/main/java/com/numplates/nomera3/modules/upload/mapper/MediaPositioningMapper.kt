package com.numplates.nomera3.modules.upload.mapper

import com.numplates.nomera3.data.network.MediaPositioningDto
import com.numplates.nomera3.modules.feed.ui.entity.MediaPositioning

object MediaPositioningMapper {
    fun mapMediaPositioning(dto: MediaPositioningDto): MediaPositioning {
        return MediaPositioning(
            x = dto.x ?: 0.0,
            y = dto.y ?: 0.0
        )
    }
}
