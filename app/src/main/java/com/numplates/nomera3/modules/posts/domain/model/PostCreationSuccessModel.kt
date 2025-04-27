package com.numplates.nomera3.modules.posts.domain.model

import com.numplates.nomera3.modules.feed.data.entity.PostEntityResponse

sealed class PostActionModel {
    data class PostCreationSuccessModel(
        val postId: Long,
        val eventId: Long?
    ) : PostActionModel()

    data class PostEditingStartModel(
        val postId: Long,
        val postText: String,
        val isContainsMedia: Boolean
    ) : PostActionModel()

    data class PostEditingAbortModel(
        val postId: Long
    ) : PostActionModel()

    data class PostEditingCompleteModel(val post: PostEntityResponse): PostActionModel()
}
