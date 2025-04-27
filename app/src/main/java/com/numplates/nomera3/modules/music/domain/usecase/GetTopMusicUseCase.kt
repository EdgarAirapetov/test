package com.numplates.nomera3.modules.music.domain.usecase

import com.numplates.nomera3.modules.music.domain.MusicRepository
import com.numplates.nomera3.modules.music.domain.model.MusicSearchEntity
import javax.inject.Inject

class GetTopMusicUseCase  @Inject constructor(
    private val repository: MusicRepository
) {
    suspend fun invoke(limit: Int, offset: Int) : List<MusicSearchEntity>{
        return repository.requestTopMusic(limit, offset)
    }
}
