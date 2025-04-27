package com.numplates.nomera3.modules.feed.domain.model

import com.numplates.nomera3.modules.feed.data.entity.FeatureEntityResponse
import com.numplates.nomera3.modules.feed.data.entity.PostAdEntityResponse
import com.numplates.nomera3.modules.tags.data.entity.HashtagModel

data class PostsModelEntity(
    val posts: List<PostModelEntity>? = null,
    val features: List<FeatureEntityResponse>? = null,
    val ads: ArrayList<PostAdEntityResponse>? = null,
    val hashtag: HashtagModel?
)
