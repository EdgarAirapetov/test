package com.numplates.nomera3.modules.tags.domain.usecase

import com.numplates.nomera3.modules.comments.domain.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.comments.domain.DefParams
import com.numplates.nomera3.modules.tags.data.entity.HashtagTagListModel
import com.numplates.nomera3.modules.tags.data.repository.TagsRepository
import com.numplates.nomera3.modules.tags.domain.usecase.GetSuggestedHashtagListUseCase.Params
import javax.inject.Inject

class GetSuggestedHashtagListUseCase @Inject constructor(
        private val repository: TagsRepository
) : BaseUseCaseCoroutine<Params, HashtagTagListModel> {

    override suspend fun execute(
            params: Params,
            success: (HashtagTagListModel) -> Unit,
            fail: (Exception) -> Unit
    ) {
        repository.getTagListByHashtag(hashtag = params.query, success = success, fail = fail)
    }

    inner class Params(val query: String, val roomId: Int? = null) : DefParams()
}
