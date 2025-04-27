package com.numplates.nomera3.modules.feed.domain.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.feed.data.repository.PostRepository
import com.numplates.nomera3.modules.feed.domain.model.PostsModelEntity
import javax.inject.Inject

class GetPostsUseCase @Inject constructor(
        private val repository: PostRepository
) : BaseUseCaseCoroutine<GetPostsParams, PostsModelEntity> {

    override suspend fun execute(params: GetPostsParams,
                                 success: (PostsModelEntity) -> Unit,
                                 fail: (Throwable) -> Unit) {
        repository.getPosts(
                params.startPostId,
                params.quantity,
                params.roadType,
                params.cityId,
                params.userId,
                params.groupId,
                params.countryIds,
                params.hashtag,
                params.includeGroups,
                params.recommended,
                success,
                fail)
    }
}

data class GetPostsParams(
        val startPostId: Long = 0,
        val quantity: Int = 0,
        val roadType: Int = 0,
        val cityId: String = "",
        val userId: Long = 0,
        val groupId: Int = 0,
        val countryIds: String = "",
        val hashtag: String = "",
        val includeGroups: Boolean? = null,
        val recommended: Boolean? = null
) : DefParams()
