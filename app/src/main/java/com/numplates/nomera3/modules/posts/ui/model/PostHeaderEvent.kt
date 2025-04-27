package com.numplates.nomera3.modules.posts.ui.model

import android.view.View

sealed interface PostHeaderEvent {
    object UserClicked : PostHeaderEvent
    data class CommunityClicked(val communityId: Long) : PostHeaderEvent
    object OptionsClicked : PostHeaderEvent
    object BackClicked : PostHeaderEvent
    object CloseClicked : PostHeaderEvent
    object FollowClicked : PostHeaderEvent
    data class UserMomentsClicked(
        val userId: Long,
        val fromView: View,
        val hasNewMoments: Boolean?
    ) : PostHeaderEvent
}
