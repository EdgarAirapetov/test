package com.meera.db.models.notifications

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.meera.db.models.MomentAsset
import com.meera.db.models.PostAsset
import com.meera.db.models.message.ParsedUniquename
import com.meera.db.models.message.UniquenameEntity

const val META_NOTIFICATION_ENTITY_POST_ID_TABLE_NAME = "MetaNotificationEntity"
const val META_NOTIFICATION_ENTITY_POST_ID = "post_id"
const val META_NOTIFICATION_ENTITY_COMMENT_ID = "comment_id"
const val META_NOTIFICATION_ENTITY_COMMENT = "comment"
const val META_NOTIFICATION_ENTITY_REPLY_COMMENT = "reply_comment"
const val META_NOTIFICATION_ENTITY_GIFT_ID = "gift_id"
const val META_NOTIFICATION_ENTITY_IMAGE = "image"
const val META_NOTIFICATION_ENTITY_TITLE = "title"
const val META_NOTIFICATION_ENTITY_GROUP_ID = "group_id"
const val META_NOTIFICATION_ENTITY_GROUP_NAME = "group_name"
const val META_NOTIFICATION_ENTITY_ROOM_ID = "room_id"
const val META_NOTIFICATION_ENTITY_TEXT = "meta_text"
const val META_NOTIFICATION_ENTITY_AVATAR = "meta_avatar"
const val META_NOTIFICATION_ENTITY_LINK = "meta_link"
const val META_NOTIFICATION_ENTITY_SPAN_DATA = "tags_span_data"
const val META_NOTIFICATION_ENTITY_TAGS = "tags"
const val META_NOTIFICATION_ENTITY_POST_TAGS = "post_tags"
const val META_NOTIFICATION_ENTITY_COMMENT_TAGS = "comment_tags"
const val META_NOTIFICATION_ENTITY_COMMUNITY_AVATAR = "community_avatar"
const val META_NOTIFICATION_ENTITY_COMMUNITY_ID = "community_id"
const val META_NOTIFICATION_ENTITY_COMMUNITY_NAME = "community_name"
const val META_NOTIFICATION_ENTITY_IS_ANONYM__NAME = "is_anonym"
const val META_NOTIFICATION_ENTITY_FROM_USER_ID = "from_user_id"
const val META_NOTIFICATION_MEDIA_ENTITY = "meta_media"
const val META_NOTIFICATION_REACTION = "meta_reaction"
const val META_NOTIFICATION_USER_BLOCKED_REASON = "meta_user_blocked_reason"
const val META_NOTIFICATION_USER_BLOCKED_TO = "meta_user_blocked_to"
const val META_NOTIFICATION_EVENT_TITLE = "meta_event_title"
const val META_NOTIFICATION_EVENT_IMAGE_URL = "meta_event_image_url"
const val META_NOTIFICATION_HAS_EVENT_ON_MAP = "meta_has_event_on_map"
const val META_NOTIFICATION_ENTITY_MOMENT_ID = "moment_id"
const val META_NOTIFICATION_ENTITY_MOMENT_AUTHOR_ID = "moment_author_id"

@Entity(tableName = META_NOTIFICATION_ENTITY_POST_ID_TABLE_NAME)
data class MetaNotificationEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = META_NOTIFICATION_ENTITY_POST_ID)
    var postId: Long? = null,

    @ColumnInfo(name = "post_text")
    var postText: String? = null,

    @ColumnInfo(name = "post_asset")
    var postAsset: PostAsset? = null,

    @ColumnInfo(name = "moment_asset")
    var momentAsset: MomentAsset? = null,

    @ColumnInfo(name = META_NOTIFICATION_ENTITY_COMMENT_ID)
    var commentId: Long? = null,

    @ColumnInfo(name = META_NOTIFICATION_ENTITY_COMMENT)
    var comment: String? = null,

    @ColumnInfo(name = META_NOTIFICATION_ENTITY_REPLY_COMMENT)
    var replyComment: String? = null,

    @ColumnInfo(name = META_NOTIFICATION_ENTITY_GIFT_ID)
    var giftId: Long? = null,

    @ColumnInfo(name = META_NOTIFICATION_ENTITY_IMAGE)
    var image: String? = null,

    @ColumnInfo(name = META_NOTIFICATION_ENTITY_TITLE)
    var title: String? = null,

    @ColumnInfo(name = META_NOTIFICATION_ENTITY_GROUP_ID)
    var groupId: Long? = null,

    @ColumnInfo(name = META_NOTIFICATION_ENTITY_GROUP_NAME)
    val groupName: String?,

    @ColumnInfo(name = META_NOTIFICATION_ENTITY_ROOM_ID)
    var roomId: Long? = null,

    @ColumnInfo(name = META_NOTIFICATION_ENTITY_TEXT)
    var text: String? = null,

    @ColumnInfo(name = META_NOTIFICATION_ENTITY_AVATAR)
    var avatar: String? = null,

    @ColumnInfo(name = META_NOTIFICATION_ENTITY_LINK)
    var link: String? = null,

    @ColumnInfo(name = META_NOTIFICATION_ENTITY_SPAN_DATA)
    var tagSpan: ParsedUniquename? = null,

    @ColumnInfo(name = META_NOTIFICATION_ENTITY_TAGS)
    var tags: List<UniquenameEntity>? = null,

    @ColumnInfo(name = META_NOTIFICATION_ENTITY_POST_TAGS)
    var postTags: List<UniquenameEntity>? = null,

    @ColumnInfo(name = META_NOTIFICATION_ENTITY_COMMENT_TAGS)
    var commentTags: List<UniquenameEntity>? = null,

    @ColumnInfo(name = META_NOTIFICATION_ENTITY_COMMUNITY_AVATAR)
    var communityAvatar: String? = null,

    @ColumnInfo(name = META_NOTIFICATION_ENTITY_COMMUNITY_ID)
    var communityId: Int? = null,

    @ColumnInfo(name = META_NOTIFICATION_ENTITY_COMMUNITY_NAME)
    var communityName: String? = null,

    @ColumnInfo(name = META_NOTIFICATION_ENTITY_IS_ANONYM__NAME)
    var isAnonym: Boolean? = null,

    @ColumnInfo(name = META_NOTIFICATION_ENTITY_FROM_USER_ID)
    var fromUserId: Long? = null,

    @ColumnInfo(name = META_NOTIFICATION_MEDIA_ENTITY)
    var media: MediaEntity? = null,

    @ColumnInfo(name = META_NOTIFICATION_REACTION)
    var reaction: String? = null,

    @ColumnInfo(name = META_NOTIFICATION_USER_BLOCKED_REASON)
    var userBlocReason: String?,

    @ColumnInfo(name = META_NOTIFICATION_USER_BLOCKED_TO)
    var userBlockedTo: Long?,

    @ColumnInfo(name = META_NOTIFICATION_EVENT_TITLE)
    var eventTitle: String?,

    @ColumnInfo(name = META_NOTIFICATION_EVENT_IMAGE_URL)
    var eventImageUrl: String?,

    @ColumnInfo(name = META_NOTIFICATION_HAS_EVENT_ON_MAP)
    var hasEventOnMap: Boolean?,

    @ColumnInfo(name = META_NOTIFICATION_ENTITY_MOMENT_ID)
    var momentId: Long?,

    @ColumnInfo(name = META_NOTIFICATION_ENTITY_MOMENT_AUTHOR_ID)
    var momentAuthorId: Long?
)
