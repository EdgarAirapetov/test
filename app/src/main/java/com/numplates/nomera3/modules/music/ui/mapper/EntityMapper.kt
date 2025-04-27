package com.numplates.nomera3.modules.music.ui.mapper

import com.numplates.nomera3.modules.music.domain.model.MusicSearchEntity
import com.numplates.nomera3.modules.music.ui.entity.MusicCellUIEntity
import com.numplates.nomera3.modules.newroads.data.entities.MediaEntity

class EntityMapper {

    fun map(values: List<MusicSearchEntity>, isAddMode: Boolean = true): List<MusicCellUIEntity> =
            values.map { entity ->
                MusicCellUIEntity(
                        mediaEntity = MediaEntity(
                                albumUrl = entity.albumUrl,
                                artist = entity.artist,
                                artistUrl = entity.artistUrl,
                                album = entity.album,
                                trackUrl = entity.trackUrl,
                                track = entity.track,
                                trackPreviewUrl = entity.trackPreviewUrl,
                                track_id = entity.trackId
                        ),
                        needToShowAddBtn = isAddMode
                )
            }
}
