package com.numplates.nomera3.modules.notifications.ui.entity

import com.meera.core.extensions.empty
import com.numplates.nomera3.presentation.utils.equalsIgnoreOrder
import java.util.Date

const val POST = 1
const val CHAT = 2
const val COMMENT = 3
const val MOMENT_COMMENT = 4

data class NotificationUiModel(
    val id: String,
    val isRead: Boolean,
    val isGroup: Boolean,
    val groupId: String,
    val date: Date,
    val timeAgo: String,
    val count: Int,
    val users: List<User>,
    val type: String,
    val meta: Meta,
    val infoSection: InfoSection? = null,
    val dateGroup: String,
    var changedFlag: Int,
    val commentId: Long?
) {
    companion object {
        fun empty() = NotificationUiModel(
            id = String.empty(),
            isRead = false,
            isGroup = false,
            groupId = String.empty(),
            date = Date(),
            timeAgo = String.empty(),
            count = 0,
            users = emptyList(),
            type = String.empty(),
            meta = Meta(),
            infoSection = null,
            dateGroup = String.empty(),
            changedFlag = 0,
            commentId = 0
        )
    }

    override fun equals(other: Any?): Boolean {
        val newItem = other as? NotificationUiModel ?: return false
        return id == newItem.id
            && isRead == newItem.isRead
            && isGroup == newItem.isGroup
            && groupId == newItem.groupId
            && date == newItem.date
            && timeAgo == newItem.timeAgo
            && count == newItem.count
            && equalsIgnoreOrder(users, newItem.users)
            && type == newItem.type
            && meta == newItem.meta
            && infoSection == newItem.infoSection
            && dateGroup == newItem.dateGroup
            && changedFlag == newItem.changedFlag
            && commentId == newItem.commentId
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + isRead.hashCode()
        result = 31 * result + isGroup.hashCode()
        result = 31 * result + groupId.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + timeAgo.hashCode()
        result = 31 * result + count
        result = 31 * result + users.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + meta.hashCode()
        result = 31 * result + (infoSection?.hashCode() ?: 0)
        result = 31 * result + dateGroup.hashCode()
        result = 31 * result + changedFlag
        result = 31 * result + (commentId?.hashCode() ?: 0)
        return result
    }
}

@Suppress("unused")
enum class MentionNotificationType(val value: String, val subGroup: Int) {
    MENTION_POST("mention-post", POST),
    MENTION_EVENT_POST("mention-map-event", POST),
    MENTION_GROUP_CHAT("mention-group-chat", CHAT),
    MENTION_COMMENT("mention-comment", COMMENT),
    MOMENT_MENTION_COMMENT("moment-mention-comment", MOMENT_COMMENT),
    MENTION_COMMENT_YOUR("mention-comment-your", COMMENT),
    MENTION_COMMENT_GROUP("mention-group-comment", COMMENT),
    MENTION_COMMENT_GROUP_YOUR("mention-group-comment-your", COMMENT),
    COMMUNITY_POST_CREATE("community-post-create", POST);

    companion object {
        fun make(value: String): MentionNotificationType? =
            when (value) {
                MENTION_POST.value -> MENTION_POST
                MENTION_EVENT_POST.value -> MENTION_EVENT_POST
                MENTION_GROUP_CHAT.value -> MENTION_GROUP_CHAT
                MENTION_COMMENT.value -> MENTION_COMMENT
                MOMENT_MENTION_COMMENT.value -> MOMENT_MENTION_COMMENT
                MENTION_COMMENT_YOUR.value -> MENTION_COMMENT_YOUR
                MENTION_COMMENT_GROUP.value -> MENTION_COMMENT_GROUP
                MENTION_COMMENT_GROUP_YOUR.value -> MENTION_COMMENT_GROUP_YOUR
                COMMUNITY_POST_CREATE.value -> COMMUNITY_POST_CREATE
                else -> null
            }
    }
}
