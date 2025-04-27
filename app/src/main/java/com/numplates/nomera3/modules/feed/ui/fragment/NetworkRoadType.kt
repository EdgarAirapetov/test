package com.numplates.nomera3.modules.feed.ui.fragment


sealed class NetworkRoadType {

    object ALL: NetworkRoadType()

    class USER(
        val userId: Long?,
        val requestParam: Int,
        val isMe: Boolean,
        val selectedPostId: Long? = null
    ): NetworkRoadType()

    class COMMUNITY(
        val groupId: Int?,
        val isPrivateGroup: Boolean,
        val requestParam: Int
    ) : NetworkRoadType()

    object HASHTAG: NetworkRoadType()

    object SUBSCRIPTIONS: NetworkRoadType()
}
