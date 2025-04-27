package com.numplates.nomera3.modules.music.domain.usecase

import com.numplates.nomera3.modules.music.domain.MusicRepository
import com.numplates.nomera3.modules.music.domain.model.MusicSearchEntity
import javax.inject.Inject

class SearchMusicUseCase @Inject constructor(
    private val repository: MusicRepository
) {
   suspend fun invoke(limit: Int, offset: Int, query: String) : List<MusicSearchEntity> {
       return repository.searchMusicTest(limit, offset, query)
   }
}