package com.numplates.nomera3.modules.music.domain

import com.numplates.nomera3.modules.music.domain.model.MusicSearchEntity

interface MusicRepository {

    suspend fun searchMusicTest(limit: Int, offset: Int, query: String): List<MusicSearchEntity>

    suspend fun requestTopMusic(limit: Int, offset: Int): List<MusicSearchEntity>
}
