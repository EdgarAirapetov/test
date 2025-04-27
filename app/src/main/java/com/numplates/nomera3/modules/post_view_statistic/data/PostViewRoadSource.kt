package com.numplates.nomera3.modules.post_view_statistic.data

import com.meera.core.extensions.empty

sealed class PostViewRoadSource(val stringRepresentation: String) {
    object Disable : PostViewRoadSource(String.empty())

    object Main : PostViewRoadSource("all")

    object Subscription : PostViewRoadSource("subscription")

    data class Community(val groupId: Int) : PostViewRoadSource("community")

    object Hashtag : PostViewRoadSource("hashtag")

    object Post : PostViewRoadSource("post")

    object Profile : PostViewRoadSource("profile")
}