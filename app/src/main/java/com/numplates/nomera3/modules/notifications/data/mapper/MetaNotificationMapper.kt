package com.numplates.nomera3.modules.notifications.data.mapper


import com.meera.db.models.message.ParsedUniquename
import com.meera.db.models.notifications.MediaEntity
import com.meera.db.models.notifications.MetaNotificationEntity
import com.numplates.nomera3.modules.notifications.data.entity.MetaNotificationEntityResponse
import com.numplates.nomera3.modules.notifications.ui.entity.MentionNotificationType
import com.numplates.nomera3.presentation.utils.parseUniquename

class MetaNotificationMapper {

    fun apply(response: MetaNotificationEntityResponse, type: MentionNotificationType?): MetaNotificationEntity {
        return MetaNotificationEntity(
            postId = response.postId,
            postText = response.postText,
            postAsset = response.postAsset,
            momentAsset = response.momentAsset,
            commentId = response.commentId,
            comment = response.comment,
            replyComment = mapReplyCommentTags(response),
            giftId = response.giftId,
            image = response.image,
            title = response.title,
            groupId = response.groupId,
            groupName = response.groupName,
            roomId = response.roomId,
            link = response.link,
            text = response.text,
            avatar = response.avatar,
            tagSpan = mapTags(response, type),
            tags = response.tags,
            postTags = response.postTags,
            commentTags = response.commentTags,
            communityAvatar = response.communityAvatar,
            communityId = response.communityId,
            communityName = response.communityName,
            isAnonym = response.isAnonym,
            fromUserId = response.fromUserId,
            media = MediaEntity(
                artist = response.media?.artist,
                track = response.media?.track
            ),
            reaction = response.reaction,
            userBlocReason = response.userBlocReason,
            userBlockedTo = response.userBlockedTo,
            eventTitle = response.eventTitle,
            eventImageUrl = response.eventImageUrl,
            hasEventOnMap = response.hasEventOnMap,
            momentId = response.momentId,
            momentAuthorId = response.momentAuthorId
        )
    }

    private fun mapReplyCommentTags(response: MetaNotificationEntityResponse): String {
        val comment = response.replyComment
        val commentText = comment?.comment
        return if (comment?.tags.isNullOrEmpty()) {
            commentText.orEmpty()
        } else {
            parseUniquename(commentText, comment?.tags).text.orEmpty()
        }
    }

    private fun mapTags(t0: MetaNotificationEntityResponse, type: MentionNotificationType?): ParsedUniquename? {
        if (t0.tags.isNullOrEmpty()) return null
        return when (type) {
            MentionNotificationType.MENTION_POST -> parseUniquename(t0.postText, t0.tags)
            MentionNotificationType.MENTION_GROUP_CHAT -> parseUniquename(t0.messageText, t0.tags)
            MentionNotificationType.MENTION_COMMENT -> parseUniquename(t0.comment, t0.tags)
            MentionNotificationType.MENTION_COMMENT_YOUR -> parseUniquename(t0.comment, t0.tags)
            MentionNotificationType.MENTION_COMMENT_GROUP -> parseUniquename(t0.comment, t0.tags)
            MentionNotificationType.MENTION_COMMENT_GROUP_YOUR -> parseUniquename(t0.comment, t0.tags)
            MentionNotificationType.COMMUNITY_POST_CREATE -> parseUniquename(t0.postText, t0.tags)
            MentionNotificationType.MENTION_EVENT_POST -> parseUniquename(t0.eventTitle, t0.tags)
            MentionNotificationType.MOMENT_MENTION_COMMENT -> parseUniquename(t0.comment, t0.tags)
            else -> parseUniquename(t0.postText)
        }
    }
}
