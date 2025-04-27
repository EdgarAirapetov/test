package com.numplates.nomera3.data.fcm

import android.os.Bundle
import com.meera.db.models.dialog.UserChat
import com.numplates.nomera3.data.fcm.data.CommonPushModel
import com.numplates.nomera3.modules.reaction.data.ReactionType
import com.numplates.nomera3.presentation.router.IActionContainer
import com.numplates.nomera3.presentation.router.IArgContainer

fun CommonPushModel.prepareBundle(): Bundle {
    val bundle = Bundle()

    when (action) {
        IActionContainer.ACTION_OPEN_CHAT -> {
            bundle.putLong(IArgContainer.ARG_ROOM_ID, roomId ?: 0L)
            bundle.putString(IArgContainer.ARG_PUSH_EVENT_ID, eventId)
        }

        IActionContainer.ACTION_FRIEND_REQUEST -> {
            val user = UserChat(userId ?: 0)
            bundle.putParcelable(IArgContainer.ARG_USER_MODEL, user)
        }

        IActionContainer.ACTION_OPEN_MOMENT -> {
            bundle.putLong(IArgContainer.ARG_MOMENT_ID, momentId ?: 0)
            bundle.putLong(IArgContainer.ARG_MOMENT_AUTHOR_ID, momentAuthorId ?: 0)
            if (commentId != null) {
                bundle.putLong(IArgContainer.ARG_COMMENT_ID, commentId ?: 0L)
            }
        }

        IActionContainer.ACTION_FRIEND_CONFIRM -> {
            val user = UserChat(userId ?: 0)
            bundle.putParcelable(IArgContainer.ARG_USER_MODEL, user)
        }

        IActionContainer.ACTION_CALL_UNAVAILABLE -> {
            val user = UserChat(userId ?: 0)
            bundle.putParcelable(IArgContainer.ARG_USER_MODEL, user)
        }

        IActionContainer.ACTION_OPEN_GIFTS -> {
            val user = UserChat(userId ?: 0)
            bundle.putParcelable(IArgContainer.ARG_USER_MODEL, user)
        }

        IActionContainer.ACTION_LEAVE_POST_COMMENTS -> {
            bundle.putLong(IArgContainer.ARG_FEED_POST_ID, postId ?: 0L)
            bundle.putLong(IArgContainer.ARG_COMMENT_ID, commentId ?: 0L)
        }

        IActionContainer.ACTION_LEAVE_POST_COMMENT_REACTIONS -> {
            bundle.putLong(IArgContainer.ARG_FEED_POST_ID, postId ?: 0L)
            bundle.putLong(IArgContainer.ARG_COMMENT_ID, commentId ?: 0L)
            bundle.putSerializable(
                IArgContainer.ARG_COMMENT_LAST_REACTION,
                ReactionType.getByString(lastReaction)
            )
        }

        IActionContainer.ACTION_REPLY_POST_COMMENTS ->
            bundle.putLong(IArgContainer.ARG_FEED_POST_ID, postId ?: 0L)

        IActionContainer.ACTION_ADD_TO_GROUP_CHAT ->
            bundle.putLong(IArgContainer.ARG_ROOM_ID, roomId ?: 0L)

        IActionContainer.ACTION_REQUEST_TO_GROUP ->
            bundle.putInt(IArgContainer.ARG_GROUP_ID, groupId ?: -1)

        IActionContainer.ACTION_OPEN_POST ->
            bundle.putLong(IArgContainer.ARG_FEED_POST_ID, postId ?: 0L)

        IActionContainer.ACTION_OPEN_POST_WITH_REACTIONS -> {
            bundle.putLong(IArgContainer.ARG_FEED_POST_ID, postId ?: 0L)
            bundle.putBoolean(IArgContainer.ARG_FEED_POST_HAVE_REACTIONS, true)
            bundle.putSerializable(
                IArgContainer.ARG_POST_LATEST_REACTION_TYPE,
                ReactionType.getByString(lastReaction)
            )
        }

        IActionContainer.ACTION_OPEN_GALLERY_WITH_REACTIONS -> {
            bundle.putLong(IArgContainer.ARG_FEED_POST_ID, postId ?: 0L)
        }

        IActionContainer.ACTION_SYSTEM_EVENT ->
            bundle.putString(IArgContainer.ARG_URL, url ?: "")

        IActionContainer.ACTION_OPEN_BIRTHDAY_GIFTS -> {
            val user = UserChat(userId = userId ?: 0, name = name)
            bundle.putParcelable(IArgContainer.ARG_USER_MODEL, user)
        }

        IActionContainer.ACTION_OPEN_BIRTHDAY_GROUP ->
            bundle.putString(IArgContainer.ARG_EVENT_GROUP_ID, eventGroupId)

        IActionContainer.ACTION_OPEN_EVENT_ON_MAP -> {
            bundle.putLong(IArgContainer.ARG_FEED_POST_ID, postId ?: -1L)
        }

        IActionContainer.ACTION_OPEN_PEOPLE -> {
            bundle.putLong(IArgContainer.ARG_USER_ID, userId ?: -1L)
        }
    }
    return bundle
}

fun PushObjectNew.toCommonPushModel(action: String, channelIdBase: String): CommonPushModel =
    CommonPushModel(
        channelIdBase = channelIdBase,
        action = action,
        contentTitle = title ?: "",
        contentText = description,
        bigImage = attachmentUrl,
        avatar = senderAvatar,
        roomId = roomId,
        userId = userId,
        eventId = eventId,
        postId = postId,
        commentId = commentId,
        groupId = groupId,
        lastReaction = lastReaction,
        url = url,
        name = name,
        eventGroupId = eventGroupId,
        momentId = momentId,
        momentAuthorId = momentAuthorId
    )
