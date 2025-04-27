package com.numplates.nomera3.modules.moments.show.presentation.data

import android.os.Parcelable
import com.numplates.nomera3.modules.feed.ui.entity.UiMedia
import com.numplates.nomera3.modules.moments.show.domain.CommentsAvailabilityType
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class MomentItemUiModel(
    val id: Long,
    val isActive: Boolean,
    val isDeleted: Boolean,
    val isAccessDenied: Boolean,
    val createdAt: Long,
    val isViewed: Boolean,
    val place: String,
    val contentUrl: String?,
    val contentType: String?,
    val contentPreview: String?,
    val doNotShowUser: Boolean,
    val reactions: List<ReactionEntity>,
    val commentAvailability: CommentsAvailabilityType,
    val commentsCount: Int,
    val repostsCount: Int,
    val viewsCount: Long,
    val userAvatarSmall: String?,
    val userName: String?,
    val userAccountColor: Int?,
    val userAccountType: Int?,
    val userId: Long,
    val userGender: Int?,
    val userApproved: Boolean,
    val userTopContentMaker: Boolean,
    val isSubscribedToUser: Boolean,
    val isUserBlackListMe: Boolean,
    val isUserBlackListByMe: Boolean,
    val meCanCommentMoment: Boolean,
    val media: UiMedia?,
    val adultContent: Int?
) : Parcelable {

    fun isMomentCommentable(): Boolean {
        return !isUserBlackListMe && meCanCommentMoment
    }

    fun hasComments(): Boolean {
        return commentsCount > 0
    }

    fun isInteractionAllowed(): Boolean {
        return !isUserBlackListByMe &&
            !isUserBlackListMe &&
            !isDeleted &&
            !isAccessDenied &&
            isActive &&
            contentUrl != null
    }
}
