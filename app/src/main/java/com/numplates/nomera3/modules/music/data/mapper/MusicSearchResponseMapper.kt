package com.numplates.nomera3.modules.music.data.mapper

import com.numplates.nomera3.modules.music.data.entity.MusicResponseEntity
import com.numplates.nomera3.modules.music.domain.model.MusicSearchEntity
import javax.inject.Inject

class MusicSearchResponseMapper @Inject constructor() {

    fun mapRespToDomainModel(resp: MusicResponseEntity): List<MusicSearchEntity> {
        return resp.response.map {
            MusicSearchEntity(
                it.album,
                it.albumUrl,
                it.artist,
                it.artistUrl,
                it.track,
                it.trackId,
                it.trackPreviewUrl.orEmpty(),
                it.trackUrl
            )
        }
    }
}