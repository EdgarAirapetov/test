package com.numplates.nomera3.modules.notifications.domain.mapper

import com.meera.db.models.notifications.MetaNotificationEntity
import com.numplates.nomera3.modules.notifications.domain.entity.Media
import com.numplates.nomera3.modules.notifications.domain.entity.MetaNotification
import io.reactivex.functions.Function

class MetaNotificationMapper : Function<MetaNotificationEntity, MetaNotification> {

    override fun apply(metaEntity: MetaNotificationEntity): MetaNotification =
        MetaNotification(
            postId = metaEntity.postId,
            postText = metaEntity.postText,
            postAsset = metaEntity.postAsset,
            momentAsset = metaEntity.momentAsset,
            commentId = metaEntity.commentId,
            comment = metaEntity.comment,
            replyComment = metaEntity.replyComment,
            giftId = metaEntity.giftId,
            image = metaEntity.image,
            title = metaEntity.title,
            groupId = metaEntity.groupId,
            groupName = metaEntity.groupName,
            roomId = metaEntity.roomId,
            text = metaEntity.text,
            avatar = metaEntity.avatar,
            link = metaEntity.link,
            tagSpan = metaEntity.tagSpan,
            tags = metaEntity.tags,
            postTags = metaEntity.postTags,
            commentTags = metaEntity.commentTags,
            communityAvatar = metaEntity.communityAvatar,
            communityId = metaEntity.communityId,
            communityName = metaEntity.communityName,
            isAnonym = metaEntity.isAnonym,
            fromUserId = metaEntity.fromUserId,
            media = Media(
                artist = metaEntity.media?.artist,
                track = metaEntity.media?.track
            ),
            reaction = metaEntity.reaction,
            userBlocReason = metaEntity.userBlocReason,
            userBlockedTo = metaEntity.userBlockedTo,
            eventTitle = metaEntity.eventTitle,
            eventImageUrl = metaEntity.eventImageUrl,
            hasEventOnMap = metaEntity.hasEventOnMap,
            momentId = metaEntity.momentId,
            momentAuthorId = metaEntity.momentAuthorId
        )

}
