package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.modules.feed.data.entity.PostMediaViewInfo
import com.numplates.nomera3.modules.newroads.data.PostsRepository
import javax.inject.Inject

class SetLastPostMediaViewInfoUseCase @Inject constructor(
    private val postsRepository: PostsRepository
) {
    fun invoke(lastPostMediaViewInfo: PostMediaViewInfo?) = postsRepository.setLastPostMediaViewInfo(lastPostMediaViewInfo)
}
