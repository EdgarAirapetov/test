package com.numplates.nomera3.modules.tags.domain.usecase

import com.numplates.nomera3.modules.comments.domain.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.tags.data.entity.UniqueNameTagListResponse
import com.numplates.nomera3.modules.tags.data.repository.TagsRepository
import javax.inject.Inject

class SearchByUniqueNameUseCase @Inject constructor(
        private val repository: TagsRepository
) : BaseUseCaseCoroutine<SearchUniqueNameParams, UniqueNameTagListResponse> {

    override suspend fun execute(
            params: SearchUniqueNameParams,
            success: (UniqueNameTagListResponse) -> Unit,
            fail: (Exception) -> Unit) {

        repository.getTagListByUniqueName(
                uniqueName = params.query,
                roomId = params.roomId,
                success = success,
                fail = fail
        )
    }
}

