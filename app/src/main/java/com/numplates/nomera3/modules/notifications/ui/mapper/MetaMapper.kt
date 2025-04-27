package com.numplates.nomera3.modules.notifications.ui.mapper

import com.numplates.nomera3.modules.notifications.domain.entity.MetaNotification
import com.numplates.nomera3.modules.notifications.ui.entity.Media
import com.numplates.nomera3.modules.notifications.ui.entity.Meta
import com.numplates.nomera3.modules.reaction.data.ReactionType
import io.reactivex.functions.Function

class MetaMapper : Function<MetaNotification, Meta> {

    override fun apply(metaNotification: MetaNotification): Meta {
        return Meta(
            postId = metaNotification.postId,
            postText = metaNotification.postText,
            postAsset = metaNotification.postAsset,
            momentAsset = metaNotification.momentAsset,
            commentId = metaNotification.commentId,
            comment = metaNotification.comment,
            replyComment = metaNotification.replyComment,
            giftId = metaNotification.giftId,
            image = metaNotification.image,
            title = metaNotification.title,
            groupId = metaNotification.groupId,
            groupName = metaNotification.groupName,
            roomId = metaNotification.roomId,
            text = metaNotification.text,
            avatar = metaNotification.avatar,
            link = metaNotification.link,
            tagSpan = metaNotification.tagSpan,
            tags = metaNotification.tags,
            postTags = metaNotification.postTags,
            commentTags = metaNotification.commentTags,
            communityAvatar = metaNotification.communityAvatar,
            communityId = metaNotification.communityId,
            communityName = metaNotification.communityName,
            isAnonym = metaNotification.isAnonym ?: false,
            fromUserId = metaNotification.fromUserId,
            media = Media(
                artist = metaNotification.media?.artist,
                track = metaNotification.media?.track
            ),
            reaction = ReactionType.getByString(metaNotification.reaction),
            userBlocReason = metaNotification.userBlocReason,
            userBlockedTo = metaNotification.userBlockedTo,
            momentId = metaNotification.momentId,
            momentAuthorId = metaNotification.momentAuthorId,
            eventTitle = metaNotification.eventTitle,
            eventImageUrl = metaNotification.eventImageUrl,
            hasEventOnMap = metaNotification.hasEventOnMap
        )
    }

}
