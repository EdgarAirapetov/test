package com.numplates.nomera3.modules.feed.domain.usecase

import com.numplates.nomera3.modules.feed.data.repository.PostRepository
import com.numplates.nomera3.presentation.download.DownloadMediaEvent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllDownloadingMediaEventUseCase @Inject constructor(private val repository: PostRepository) {
    fun invoke(): Flow<DownloadMediaEvent> {
        return repository.getDownloadHelperEvent()
    }
}
