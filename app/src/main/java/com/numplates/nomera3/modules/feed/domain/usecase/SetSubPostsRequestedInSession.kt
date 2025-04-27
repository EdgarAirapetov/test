package com.numplates.nomera3.modules.feed.domain.usecase

import com.numplates.nomera3.modules.newroads.data.PostsRepository
import javax.inject.Inject

class SetSubPostsRequestedInSession @Inject constructor(
    private val postsRepository: PostsRepository
) {

    fun invoke(isRequested: Boolean) {
        postsRepository.setSubscriptionPostsWereRequestedWithinSession(isRequested)
    }

}
