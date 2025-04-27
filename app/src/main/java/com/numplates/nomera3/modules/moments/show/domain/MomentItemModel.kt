package com.numplates.nomera3.modules.moments.show.domain

import android.os.Parcelable
import com.numplates.nomera3.modules.feed.ui.entity.UiMedia
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class MomentItemModel(
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
) : Parcelable
