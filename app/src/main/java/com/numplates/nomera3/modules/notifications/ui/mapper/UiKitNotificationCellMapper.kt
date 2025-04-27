package com.numplates.nomera3.modules.notifications.ui.mapper

import android.content.Context
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import androidx.core.content.ContextCompat
import androidx.core.text.color
import androidx.core.text.toSpannable
import androidx.core.util.Function
import com.meera.core.extensions.dp
import com.meera.core.extensions.empty
import com.meera.core.extensions.isNotTrue
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.pluralString
import com.meera.core.extensions.string
import com.meera.core.extensions.text
import com.meera.core.utils.timeAgoNotification
import com.meera.uikit.widgets.NotificationCellConfig
import com.meera.uikit.widgets.NotificationCellImageConfig
import com.meera.uikit.widgets.multiuserpic.MultiUserpicUiModel
import com.meera.uikit.widgets.userpic.UserpicSizeEnum
import com.meera.uikit.widgets.userpic.UserpicStoriesStateEnum
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.MEDIA_EXT_MP4
import com.numplates.nomera3.OBSCENE_WORDS_MASK
import com.numplates.nomera3.R
import com.numplates.nomera3.REACTION_SYMBOLS_MASK
import com.numplates.nomera3.data.fcm.IPushInfo.ADD_TO_GROUP_CHAT
import com.numplates.nomera3.data.fcm.IPushInfo.ADD_TO_PRIVATE_BY_MODERATOR_NSFW
import com.numplates.nomera3.data.fcm.IPushInfo.BIRTHDAY
import com.numplates.nomera3.data.fcm.IPushInfo.COMMENT_REACTION
import com.numplates.nomera3.data.fcm.IPushInfo.COMMUNITY_NEW_POST
import com.numplates.nomera3.data.fcm.IPushInfo.CREATE_ANIMATED_AVATAR
import com.numplates.nomera3.data.fcm.IPushInfo.EVENT_CALL_UNAVAILABLE
import com.numplates.nomera3.data.fcm.IPushInfo.EVENT_PARTICIPANT
import com.numplates.nomera3.data.fcm.IPushInfo.EVENT_START_SOON
import com.numplates.nomera3.data.fcm.IPushInfo.FRIEND_CONFIRM
import com.numplates.nomera3.data.fcm.IPushInfo.FRIEND_REQUEST
import com.numplates.nomera3.data.fcm.IPushInfo.GALLERY_REACTION
import com.numplates.nomera3.data.fcm.IPushInfo.GIFT_RECEIVED_NOTIFICATION
import com.numplates.nomera3.data.fcm.IPushInfo.GROUP_COMMENT
import com.numplates.nomera3.data.fcm.IPushInfo.GROUP_COMMENT_REPLY
import com.numplates.nomera3.data.fcm.IPushInfo.GROUP_COMMENT_YOUR
import com.numplates.nomera3.data.fcm.IPushInfo.MENTION_COMMENT
import com.numplates.nomera3.data.fcm.IPushInfo.MENTION_MAP_EVENT
import com.numplates.nomera3.data.fcm.IPushInfo.MENTION_POST
import com.numplates.nomera3.data.fcm.IPushInfo.MOMENT
import com.numplates.nomera3.data.fcm.IPushInfo.MOMENT_COMMENT
import com.numplates.nomera3.data.fcm.IPushInfo.MOMENT_COMMENT_REACTION
import com.numplates.nomera3.data.fcm.IPushInfo.MOMENT_COMMENT_REPLY
import com.numplates.nomera3.data.fcm.IPushInfo.MOMENT_MENTION_COMMENT
import com.numplates.nomera3.data.fcm.IPushInfo.MOMENT_REACTION
import com.numplates.nomera3.data.fcm.IPushInfo.NOTIFY_PEOPLE
import com.numplates.nomera3.data.fcm.IPushInfo.POST_COMMENT
import com.numplates.nomera3.data.fcm.IPushInfo.POST_COMMENT_REPLY
import com.numplates.nomera3.data.fcm.IPushInfo.POST_COMMENT_YOUR
import com.numplates.nomera3.data.fcm.IPushInfo.POST_REACTION
import com.numplates.nomera3.data.fcm.IPushInfo.PUSH_GROUP_REQUEST
import com.numplates.nomera3.data.fcm.IPushInfo.SUBSCRIBERS_AVATAR_POST_CREATE
import com.numplates.nomera3.data.fcm.IPushInfo.SUBSCRIBERS_POST_CREATE
import com.numplates.nomera3.modules.common.CharToImageMapper
import com.numplates.nomera3.modules.common.ObsceneWordImageProvider
import com.numplates.nomera3.modules.common.ReactionImageProvider
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.notifications.ui.entity.Media
import com.numplates.nomera3.modules.notifications.ui.entity.MentionNotificationType
import com.numplates.nomera3.modules.notifications.ui.entity.NotificationCellUiModel
import com.numplates.nomera3.modules.notifications.ui.entity.NotificationUiModel
import com.numplates.nomera3.modules.notifications.ui.entity.User
import com.numplates.nomera3.modules.notifications.ui.viewholder.MUSIC_SIGN
import com.numplates.nomera3.modules.notifications.ui.viewholder.NotificationViewHolder
import com.numplates.nomera3.modules.reaction.data.ReactionType
import javax.inject.Inject
import kotlin.math.min

private const val MAX_VISIBLE_USERS_IN_TITLE = 3
private const val DOT = "."
private const val COMMA_SEPARATOR = ", "
private const val SPACE_SYMBOL = " "
private const val TWO_USERS = 2
private const val MAX_COUNT_USERS = 3
private const val DEFAULT_CONTENT_LINE_HEIGHT = 18

/**
 * https://www.figma.com/file/wyLhqHbHkvWWjLHznv6Wz8/Social-Chat-New?type=design&node-id=1-12&mode=design
 */
class UiKitNotificationCellMapper @Inject constructor(
    private val context: Context,
    private val featureToggles: FeatureTogglesContainer,
    private val charToImageMapper: CharToImageMapper,
    private val tagsMapper: TagsMapper,
    private val obsceneWordImageProvider: ObsceneWordImageProvider,
    private val reactionImageProvider: ReactionImageProvider,
): Function<NotificationUiModel, NotificationCellUiModel> {

    private val reactionCharacterMask: String by lazy {
        val builder = StringBuilder()
        ReactionType.valuesMap.values.forEach { reaction ->
            builder.append(reaction.characterRepresentation)
        }
        builder.append(REACTION_SYMBOLS_MASK)
        builder.toString()
    }

    override fun apply(notification: NotificationUiModel): NotificationCellUiModel {
        return NotificationCellUiModel(
            id = notification.id,
            data = notification,
            config = NotificationCellConfig(
                notificationTitle = getTitle(notification),
                sendDate = getTime(notification),
                userPicUiModel = getUserPic(notification),
                multiUserPicUiModel = getMultiUserPic(notification),
                postChipSrc = getPostChip(notification),
                notificationSubtitleText = getSubtitleText(notification),
                notificationBodyText = getDescriptionText(notification),
                contentText = getContentText(notification),
                contentIcnRes = getContentIcon(notification),
                actionButtonText = setActionButtonText(notification),
                imageConfig = getImageConfig(notification),
                viewed = notification.isRead
            )
        )
    }

    private fun getDescriptionText(notification: NotificationUiModel): CharSequence? {
        val meta = notification.meta
        val source = when (notification.type) {
            GIFT_RECEIVED_NOTIFICATION,
            GROUP_COMMENT_YOUR,
            GROUP_COMMENT,
            GROUP_COMMENT_REPLY,
            POST_COMMENT_REPLY,
            POST_COMMENT,
            POST_COMMENT_YOUR,
            GALLERY_REACTION,
            COMMENT_REACTION,
            POST_REACTION,
            MOMENT_REACTION,
            MOMENT_COMMENT,
            MOMENT_COMMENT_REPLY,
            MOMENT_MENTION_COMMENT,
            MOMENT_COMMENT_REACTION -> meta.comment

            EVENT_CALL_UNAVAILABLE,
            EVENT_START_SOON -> meta.eventTitle

            EVENT_PARTICIPANT -> meta.title
            SUBSCRIBERS_POST_CREATE,
            SUBSCRIBERS_AVATAR_POST_CREATE -> {
                if (meta.hasEventOnMap.isTrue()) {
                    meta.eventTitle.takeIf { !it.isNullOrEmpty() }
                } else {
                    meta.postText.takeIf { !it.isNullOrEmpty() }
                }
            }

            MENTION_POST -> meta.postText

            else -> meta.eventTitle ?: meta.comment ?: meta.postText
        }

        val parsed = tagsMapper.mapData(
            source = source ?: return null,
            tags = notification.meta.tags.orEmpty() +
                notification.meta.commentTags.orEmpty() +
                notification.meta.postTags.orEmpty()
        )
        return charToImageMapper.mapChars(
            imageProvider = obsceneWordImageProvider,
            source = parsed,
            mask = OBSCENE_WORDS_MASK,
            lineHeight = DEFAULT_CONTENT_LINE_HEIGHT.dp
        )
    }

    private fun getContentText(notification: NotificationUiModel): String? {
        val media = notification.meta.media
        val postMusicData = if (media?.artist != null) "${media.artist} - ${media.track}" else null
        return when(notification.type){
            SUBSCRIBERS_POST_CREATE -> postMusicData
            POST_COMMENT ->  notification.meta.text
            else -> null
        }
    }

    private fun getContentIcon(notification: NotificationUiModel): Int? {
        val media = notification.meta.media
        return if (notification.type == SUBSCRIBERS_POST_CREATE
            && media?.artist != null) R.drawable.ic_legacy_music_s else null
    }

    private fun getImageConfig(notification: NotificationUiModel): NotificationCellImageConfig? {
        val postAsset = notification.meta.postAsset
        val postThumbnail = postAsset?.metadata?.preview ?: postAsset?.url
        val momentAsset = notification.meta.momentAsset

        return when(notification.type){
            EVENT_START_SOON,
            EVENT_CALL_UNAVAILABLE,
            EVENT_PARTICIPANT -> NotificationCellImageConfig(
                url = notification.meta.eventImageUrl ?: String.empty(),
                isShowPlay = isVideoPathUrl(notification.meta.eventImageUrl)
            )
            SUBSCRIBERS_POST_CREATE,
            SUBSCRIBERS_AVATAR_POST_CREATE -> NotificationCellImageConfig(
                url = postThumbnail ?: String.empty(),
                isShowPlay = isVideoPathUrl(postAsset?.url)
            )
            GIFT_RECEIVED_NOTIFICATION -> NotificationCellImageConfig(
                url = notification.meta.image ?: String.empty(),
                isShowPlay = isVideoPathUrl(notification.meta.image)
            )
            MOMENT_COMMENT_REPLY,
            POST_COMMENT,
            MOMENT,
            POST_REACTION,
            GALLERY_REACTION,
            MOMENT_REACTION,
            MOMENT_COMMENT -> NotificationCellImageConfig(
                url = postThumbnail
                    ?: momentAsset?.preview
                    ?: momentAsset?.url
                    ?: String.empty(),
                isShowPlay = isVideoPathUrl(postAsset?.url) || isVideoPathUrl(momentAsset?.url)
            )
            else -> null
        }
    }

    private fun isVideoPathUrl(url: String?) =
        DOT + url?.substringAfterLast(DOT, String.empty()) == MEDIA_EXT_MP4

    private fun setActionButtonText(notification: NotificationUiModel): String? {
        return when (notification.type) {
            FRIEND_CONFIRM -> context.getString(R.string.start_chat)
            NOTIFY_PEOPLE -> context.getString(R.string.watch_profile)
            GIFT_RECEIVED_NOTIFICATION -> context.getString(R.string.go_to_gift)
            BIRTHDAY -> if (notification.isGroup.not()) context.getString(R.string.congratulation_txt) else null
            CREATE_ANIMATED_AVATAR -> context.getString(R.string.create_avatar_action)
            else -> null
        }
    }

    private fun getTitle(notification: NotificationUiModel): String {
        return if (notification.isGroup) {
            getTitleForGroupNotification(notification)
        } else {
            getTitleForSingleNotification(notification)
        }
    }

    private fun getTitleForGroupNotification(notification: NotificationUiModel): String {
        return if (notification.type == BIRTHDAY) {
            context.getString(R.string.celebrates_birthdays)
        } else {
            generateGroupedTitleText(
                users = notification.users,
                withTrail = true,
                context = context,
            )
        }
    }

    private fun getTitleForSingleNotification(notification: NotificationUiModel): String {
        return if (notification.type == BIRTHDAY) {
           context.getString(R.string.happy_bitrhday)
        } else {
            notification.users.firstOrNull()?.name ?: String.empty()
        }
    }

    private fun generateGroupedTitleText(
        users: List<User>,
        withTrail: Boolean,
        context: Context?
    ): String {
        val builder = StringBuffer()
        if (users.size == 1) {
            builder.append(users[0].name)
        } else if (users.size == 2) {
            builder.append(users[0].name)
            builder.append(
                context?.getString(R.string.group_title_text_separator_for_two)
                    ?: context?.getString(R.string.general_and)
            )
            builder.append(users[1].name)
        } else if (users.size > 2) {
            val minUsersCount = min(users.size, MAX_VISIBLE_USERS_IN_TITLE)
            for (i in 0 until minUsersCount) {
                builder.append(users[i].name)
                if (i == minUsersCount - 1) builder.append(NotificationViewHolder.SPACE) else builder.append(
                    NotificationViewHolder.SEPARATOR
                )
            }
            if (withTrail && users.size > MAX_VISIBLE_USERS_IN_TITLE) {
                builder.append(context?.getString(R.string.and_others))
            }
        }
        return builder.toString()
    }

    private fun getTime(notification: NotificationUiModel): String = timeAgoNotification(context, notification.date.time)

    private fun getUserPic(notification: NotificationUiModel): UserpicUiModel? {
        return if (notification.users.size == 1) {
            val user = notification.users.firstOrNull()
            UserpicUiModel(
                size = UserpicSizeEnum.Size40,
                userAvatarUrl = user?.avatarSmall,
                storiesState = storiesState(user),
                hat = true
            )
        } else {
            null
        }
    }

    private fun getMultiUserPic(notification: NotificationUiModel): MultiUserpicUiModel? {
        return if (notification.users.size > 1) {
            MultiUserpicUiModel(
                avatarUrls = notification.users.map { it.avatarSmall }
            )
        } else {
            null
        }
    }

    private fun storiesState(user: User?): UserpicStoriesStateEnum {
        val isEnabledFeatureMoments = featureToggles.momentsFeatureToggle.isEnabled
        return when {
            isEnabledFeatureMoments.not() -> UserpicStoriesStateEnum.NO_STORIES
            user?.hasNewMoments == true -> UserpicStoriesStateEnum.NEW
            user?.hasMoments == true -> UserpicStoriesStateEnum.VIEWED
            else -> UserpicStoriesStateEnum.NO_STORIES
        }
    }

    private fun getSubtitleText(item: NotificationUiModel): CharSequence {
        val subtitle = if (item.isGroup.not()) {
            getSubtitleTextForSingleNotification(item)
        } else {
            getSubtitleTextForGroupNotification(item)
        }
        val parsed = tagsMapper.mapData(
            source = subtitle,
            tags = item.meta.tags.orEmpty() +
                item.meta.commentTags.orEmpty() +
                item.meta.postTags.orEmpty()
        )
        return charToImageMapper.mapChars(
            imageProvider = reactionImageProvider,
            source = parsed,
            mask = reactionCharacterMask,
            lineHeight = DEFAULT_CONTENT_LINE_HEIGHT.dp
        )
    }

    private fun getSubtitleTextForSingleNotification(item: NotificationUiModel): Spannable = with(context) {
        return@with when (item.type) {
            MOMENT_COMMENT_REPLY ->
                string(R.string.meera_notification_moment_comment_reply).toSpannableStringBuilderFromHtml()
            MOMENT_COMMENT_REACTION -> {
                string(
                    R.string.meera_notification_moment_comment_reaction,
                    item.meta.reaction?.characterRepresentation,
                    String.empty()
                ).toSpannableStringBuilderFromHtml()
            }
            MOMENT_MENTION_COMMENT ->
                string(R.string.meera_mention_comment_moment_comment_notification,).toSpannableStringBuilderFromHtml()
            MOMENT_COMMENT -> string(R.string.meera_notification_moment_comment).toSpannableStringBuilderFromHtml()
            MOMENT -> string(R.string.meera_notification_moment).toSpannableStringBuilderFromHtml()
            MOMENT_REACTION -> {
                string(R.string.meera_notification_moment_reaction, item.meta.reaction?.characterRepresentation)
                    .toSpannableStringBuilderFromHtml()
            }
            POST_REACTION -> {
                val strResId = if (item.meta.hasEventOnMap.isTrue()) {
                    R.string.notification_event_post_reaction
                } else {
                    R.string.notification_post_reaction
                }
                string(strResId, item.meta.reaction?.characterRepresentation)
                    .makeTextForPost(item = item, media = item.meta.media)
            }
            GALLERY_REACTION -> {
                string(R.string.meera_notification_gallery_reaction, item.meta.reaction?.characterRepresentation)
                    .makeTextForPost(item = item, media = item.meta.media)
            }
            COMMENT_REACTION ->
                string(R.string.notification_post_comment_reaction, item.meta.reaction?.characterRepresentation)
                    .makeTextForPost(item = item, media = item.meta.media)
            SUBSCRIBERS_POST_CREATE -> {
                val strResId = if (item.meta.hasEventOnMap.isTrue()) {
                    R.string.meera_notification_subscriber_event_post_create
                } else {
                    R.string.meera_notification_subscriber_post_create
                }
                string(strResId).makeTextForPost(item = item, allowMetaText = false)
            }
            SUBSCRIBERS_AVATAR_POST_CREATE ->
                string(R.string.meera_notification_avatar_post_create).makeTextForPost(item = item, allowMetaText = false)
            POST_COMMENT_YOUR -> {
                val strResId = if (item.meta.hasEventOnMap.isTrue()) {
                    R.string.meera_notification_event_post_comment_your
                } else {
                    R.string.meera_notification_post_comment_your
                }
                string(strResId).makeTextForPost(item = item, media = item.meta.media)
            }

            POST_COMMENT -> {
                val strResId = if (item.meta.hasEventOnMap.isTrue()) {
                    R.string.meera_notification_event_post_comment
                } else {
                    R.string.meera_notification_post_comment
                }
                string(strResId).makeTextForPost(item = item, media = item.meta.media)
            }
            POST_COMMENT_REPLY -> {
                val commentReply = item.meta.replyComment ?: String.empty()
                string(R.string.meera_notification_post_comment_reply, commentReply).toSpannableStringBuilderFromHtml()
            }
            GROUP_COMMENT_YOUR ->
                string(R.string.meera_notification_group_comment_your).toSpannableStringBuilderFromHtml()

            GROUP_COMMENT ->
                string(R.string.meera_notification_group_comment).toSpannableStringBuilderFromHtml()

            GROUP_COMMENT_REPLY ->
                string(R.string.meera_notification_group_comment_reply).toSpannableStringBuilderFromHtml()

            FRIEND_REQUEST ->
                string(R.string.meera_notification_friend_request).toSpannableStringBuilderFromHtml()

            FRIEND_CONFIRM ->
                string(R.string.meera_notification_friend_received).toSpannableStringBuilderFromHtml()

            GIFT_RECEIVED_NOTIFICATION ->
                string(R.string.meera_notification_gift_received).toSpannableStringBuilderFromHtml()

            ADD_TO_GROUP_CHAT -> string(
                    R.string.meera_notification_add_group_chat,
                    item.meta.title.orEmpty()
                ).toSpannableStringBuilderFromHtml()

            ADD_TO_PRIVATE_BY_MODERATOR_NSFW ->
                string(R.string.meera_moderation_nsfw).toSpannableStringBuilderFromHtml()

            PUSH_GROUP_REQUEST -> string(
                R.string.meera_notification_group_request,
                item.meta.groupName.orEmpty()
            ).toSpannableStringBuilderFromHtml()

            EVENT_PARTICIPANT ->
                string(R.string.meera_notification_new_participant).toSpannableStringBuilderFromHtml()

            EVENT_CALL_UNAVAILABLE ->
                string(R.string.meera_notification_call_unavailable).toSpannableStringBuilderFromHtml()

            MentionNotificationType.MENTION_POST.value ->
                if (item.count > 1) string(R.string.mention_post_notifications).makeTextForPosts(item)
                else string(R.string.meera_mention_post_notification).makeTextForPost(item, isNotMention = false)

            MentionNotificationType.MENTION_EVENT_POST.value ->
                if (item.count > 1) string(R.string.mention_event_post_notifications).makeTextForPosts(item)
                else string(R.string.meera_mention_event_post_notification).makeTextForPost(item, isNotMention = false)

            MentionNotificationType.MENTION_GROUP_CHAT.value ->
                string(R.string.meera_mention_group_chat_notification).toSpannableStringBuilderFromHtml()

            MentionNotificationType.MENTION_COMMENT.value ->
                string(R.string.meera_mention_comment_post_comment_notification).makeTextForPostComment(item, item.meta.media)

            MentionNotificationType.MOMENT_MENTION_COMMENT.value ->
                string(R.string.meera_mention_comment_moment_comment_notification, item.meta.comment).toSpannableStringBuilderFromHtml()

            MentionNotificationType.MENTION_COMMENT_GROUP.value ->
                string(R.string.meera_mention_comment_group_post_notification).makeTextForPostComment(item, item.meta.media)

            MentionNotificationType.MENTION_COMMENT_YOUR.value ->
                string(R.string.meera_mention_comment_your_post_comment_notification).makeTextForPostComment(item, item.meta.media)

            MentionNotificationType.MENTION_COMMENT_GROUP_YOUR.value ->
                string(R.string.meera_mention_comment_your_group_post_notification).makeTextForPostComment(item, item.meta.media)

            COMMUNITY_NEW_POST ->
                string(R.string.meera_community_new_post_published).makeTextForPost(item, isNotMention = false)

            EVENT_START_SOON ->
                string(R.string.meera_map_event_start_soon).toSpannableStringBuilderFromHtml()

            NOTIFY_PEOPLE -> string(R.string.meera_notification_recommended_friend).toSpannableStringBuilderFromHtml()
            BIRTHDAY -> {
                val name = item.users.firstOrNull()?.name.orEmpty()
                string(R.string.meera_notification_birthday, name).toSpannableStringBuilderFromHtml()
            }

            else -> string(R.string.general_unknown).toSpannableStringBuilderFromHtml()
        }
    }

    private fun getSubtitleTextForGroupNotification(item: NotificationUiModel): Spannable = with(context) {
        return@with when (item.type) {
            MOMENT_COMMENT_REPLY -> getGroupedMomentCommandReplyComment(item).toSpannableStringBuilderFromHtml()
            MOMENT_COMMENT_REACTION -> {
                string(
                    R.string.meera_mention_comment_reaction_notification,
                    item.meta.comment.orEmpty()
                ).toSpannableStringBuilderFromHtml()
            }
            MOMENT_MENTION_COMMENT -> {
                string(R.string.meera_notification_moment_comment_grouped).toSpannableStringBuilderFromHtml()
            }
            MOMENT_COMMENT -> {
                getGroupedMomentCommentText(item).makeTextForMoment(item)
            }
            MOMENT -> {
                pluralString(R.plurals.meera_notification_moment_grouped,item.count).makeTextForMoment(item)
            }
            MOMENT_REACTION -> {
                string(R.string.notification_moment_reaction_grouped).toSpannable()
            }
            POST_REACTION -> {
                val strResId = if (item.meta.hasEventOnMap.isTrue()) {
                    R.string.notification_event_post_reaction_grouped
                } else {
                    R.string.notification_post_reaction_grouped
                }
                string(strResId).makeTextForPost(item)
            }
            GALLERY_REACTION ->
                string(R.string.meera_notification_gallery_reaction_grouped).makeTextForPost(item)
            COMMENT_REACTION ->
                string(R.string.notification_post_comment_reaction_grouped).makeTextForPost(item)
            SUBSCRIBERS_AVATAR_POST_CREATE ->
                string(R.string.meera_notification_avatar_post_create).makeTextForPost(item)
            SUBSCRIBERS_POST_CREATE -> {
                val pluralResId = if (item.meta.hasEventOnMap.isTrue()) {
                    R.plurals.notification_plural_event_posts
                } else {
                    R.plurals.notification_plural_posts
                }
                pluralString(idRes = pluralResId, quantity = item.count).toSpannableStringBuilderFromHtml()
            }

            COMMUNITY_NEW_POST -> pluralString(
                idRes = R.plurals.community_new_many_post_published,
                quantity = item.count
            ).toSpannableStringBuilderFromHtml()

            EVENT_CALL_UNAVAILABLE -> pluralString(
                idRes = R.plurals.notification_plural_event_calls,
                quantity = item.count
            ).toSpannableStringBuilderFromHtml()

            POST_COMMENT_YOUR ->
                getGroupedCommentYourText(item).makeTextForPost(item)

            POST_COMMENT ->
                getGroupedPostCommentText(item).makeTextForPost(item)

            POST_COMMENT_REPLY ->
                getGroupedPostCommandReplyComment(item).toSpannableStringBuilderFromHtml()

            GROUP_COMMENT_YOUR ->
                getGroupedCommentPostYoursText(item).toSpannableStringBuilderFromHtml()

            GROUP_COMMENT ->
                string(R.string.meera_notification_grouped_group_comment).toSpannableStringBuilderFromHtml()

            GROUP_COMMENT_REPLY ->
                getGroupedCommentReplyText(item).toSpannableStringBuilderFromHtml()

            FRIEND_REQUEST ->
                string(R.string.meera_notification_group_friend_request).toSpannableStringBuilderFromHtml()

            FRIEND_CONFIRM ->
                string(R.string.notification_grouped_friend_received).toSpannableStringBuilderFromHtml()

            GIFT_RECEIVED_NOTIFICATION ->
                string(R.string.meera_notification_group_gift_received).toSpannableStringBuilderFromHtml()

            ADD_TO_GROUP_CHAT ->
                string(R.string.meera_notification_group_add_group_chat).toSpannableStringBuilderFromHtml()

            PUSH_GROUP_REQUEST ->
                string(R.string.meera_notification_grouped__group_request).toSpannableStringBuilderFromHtml()

            ADD_TO_PRIVATE_BY_MODERATOR_NSFW ->
                string(R.string.meera_moderation_nsfw).toSpannableStringBuilderFromHtml()

            EVENT_PARTICIPANT ->
                string(R.string.meera_notification_group_new_participant).toSpannableStringBuilderFromHtml()

            MentionNotificationType.MENTION_POST.value ->
                if (item.count > 1) string(R.string.mention_post_notifications).makeTextForPosts(item)
                else string(R.string.meera_mention_post_notification).makeTextForPost(item, false)

            MentionNotificationType.MENTION_EVENT_POST.value ->
                if (item.count > 1) string(R.string.mention_event_post_notifications).makeTextForPosts(item)
                else string(R.string.meera_mention_event_post_notification).makeTextForPost(item, false)

            MentionNotificationType.MENTION_GROUP_CHAT.value ->
                getGroupedMentionYouInGroupChat(item).makeTextForPostComment(item)

            MentionNotificationType.MENTION_COMMENT.value ->
                getGroupedMentionYouInComment(item).makeTextForPostComment(item)

            MentionNotificationType.MOMENT_MENTION_COMMENT.value ->
                getGroupedMentionYouInComment(item).toSpannableStringBuilderFromHtml()

            MentionNotificationType.MENTION_COMMENT_YOUR.value ->
                getGroupedMentionYouInYourComment(item).makeTextForPostComment(item)

            MentionNotificationType.MENTION_COMMENT_GROUP.value ->
                getGroupedMentionYouInYourGroupComment(item).makeTextForPostComment(item)

            MentionNotificationType.MENTION_COMMENT_GROUP_YOUR.value ->
                getGroupedMentionYouInGroupComment(item).makeTextForPostComment(item)

            BIRTHDAY -> {
                getGroupNotificationUsersSubtitle(item).toSpannable()
            }

            else -> string(R.string.general_unknown).toSpannableStringBuilderFromHtml()
        }
    }

    private fun getGroupNotificationUsersSubtitle(item: NotificationUiModel): CharSequence = with(context) {
        val builder = SpannableStringBuilder()
        val highlightColor = ContextCompat.getColor(context, R.color.uiKitColorForegroundPrimary)
        val users = item.users
        when (users.size) {
            TWO_USERS -> {
                builder.color(highlightColor) { append(users[0].name) }
                builder.append(SPACE_SYMBOL)
                builder.append(text(R.string.meera_notification_and))
                builder.append(SPACE_SYMBOL)
                builder.color(highlightColor) { append(users[1].name) }
            }

            MAX_COUNT_USERS -> {
                builder.color(highlightColor) {
                    append(users.joinToString(separator = COMMA_SEPARATOR) { it.name })
                }
            }

            else -> {
                builder.color(highlightColor) {
                    append(users.take(MAX_COUNT_USERS).joinToString(separator = COMMA_SEPARATOR) { it.name })
                }
                builder.append(SPACE_SYMBOL)
                builder.append(text(R.string.meera_notification_and))
                builder.append(SPACE_SYMBOL)
                builder.color(highlightColor) { append(text(R.string.meera_notification_other)) }
            }
        }
        builder.append(SPACE_SYMBOL)
        builder.append(text(R.string.meera_notification_wait_for))
        builder.append(SPACE_SYMBOL)
        builder.color(ContextCompat.getColor(context, R.color.uiKitColorForegroundLink)) {
            append(text(R.string.meera_notification_congrats))
        }
        return builder
    }

    private fun String.toSpannableStringBuilderFromHtml(): SpannableStringBuilder {
        val s = Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
        return SpannableStringBuilder(s)
    }

    private fun String.makeTextForPost(
        item: NotificationUiModel,
        isNotMention: Boolean = true,
        allowMetaText: Boolean = true,
        media: Media? = null
    ): Spannable =
        if (!allowMetaText || itemHasNoPostfixText(item = item, media = media))
            this.toSpannableStringBuilderFromHtml().append(":")
        else
            this.toSpannableStringBuilderFromHtml()
                .apply {
                    append(": ")
                    if (isNotMention) append(getItemNoMediaPostfixText(item))
                    if (item.meta.hasEventOnMap.isNotTrue() && media?.artist != null && media.track != null) {
                        append(MUSIC_SIGN)
                        append(getTrackString(media.artist, media.track))
                    }
                }

    private fun String.makeTextForPosts(item: NotificationUiModel): Spannable =
        String.format(this, item.count).toSpannable()

    private fun String.makeTextForPostComment(item: NotificationUiModel, media: Media? = null): Spannable =
        if (itemHasNoPostfixText(item = item, media = media))
            toSpannableStringBuilderFromHtml().append(".")
        else
            toSpannableStringBuilderFromHtml()
                .apply {
                    append(": ")
                    append(getItemNoMediaPostfixText(item))
                    if (item.meta.hasEventOnMap.isNotTrue() && media?.artist != null && media.track != null) {
                        append(" ♫ ")
                        append(getTrackString(media.artist, media.track))
                    }
                }

    private fun getGroupedMomentCommandReplyComment(item: NotificationUiModel): String = with(context) {
        return@with if (item.isSingleAuthor()) {
            string(R.string.meera_notification_group_moment_comment_reply_by_single_user)
        } else {
            string(R.string.meera_notification_group_moment_comment_reply)
        }
    }

    private fun getGroupedMomentCommentText(item: NotificationUiModel): String = with(context) {
        return@with if (item.isSingleAuthor()) {
            pluralString(R.plurals.notification_plural_moment_comment, item.count)
        } else {
            pluralString(R.plurals.notification_plural_moment_comment_users, item.count)
        }
    }

    private fun getGroupedCommentYourText(item: NotificationUiModel): String = with(context) {
        return@with if (item.isSingleAuthor()) {
            pluralString(R.plurals.notification_plural_your_post_comment, item.count)
        } else {
            pluralString(R.plurals.notification_plural_your_post_comment_users, item.count)
        }
    }

    private fun getGroupedPostCommentText(item: NotificationUiModel): String = with(context) {
        return@with if (item.isSingleAuthor()) {
            pluralString(R.plurals.notification_plural_post_comment, item.count)
        } else {
            val pluralResId = if (item.meta.hasEventOnMap.isTrue()) {
                R.plurals.notification_plural_event_post_comment_users
            } else {
                R.plurals.notification_plural_post_comment_users
            }
            pluralString(pluralResId, item.count)
        }
    }

    private fun getGroupedMentionYouInGroupChat(item: NotificationUiModel): String = with(context) {
        return@with if (item.isSingleAuthor()) {
            pluralString(R.plurals.meera_mention_group_chat_notification_plural, item.count)
        } else {
            pluralString(R.plurals.meera_mention_group_chat_notification_users_plural, item.count)
        }
    }

    private fun getGroupedMentionYouInYourComment(item: NotificationUiModel): String = with(context) {
        return@with if (item.isSingleAuthor())
            pluralString(R.plurals.meera_many_mention_you_by_single_user_in_your_post, item.count)
        else
            pluralString(R.plurals.meera_mention_comment_users_plural, item.count)
    }

    private fun getGroupedPostCommandReplyComment(item: NotificationUiModel): String = with(context) {
        return@with if (item.isSingleAuthor()) {
            string(R.string.meera_notification_group_post_comment_reply_by_single_user)
        } else {
            string(R.string.meera_notification_group_post_comment_reply)
        }
    }

    private fun getGroupedMentionYouInComment(item: NotificationUiModel): String = with(context) {
        return@with if (item.isSingleAuthor()) {
            pluralString(R.plurals.meera_mention_comment_plural, item.count)
        } else {
            pluralString(R.plurals.meera_mention_comment_users_plural, item.count)
        }
    }

    private fun getGroupedMentionYouInYourGroupComment(item: NotificationUiModel): String = with(context) {
        return@with if (item.isSingleAuthor()) {
            pluralString(R.plurals.meera_mention_comment_group_your_plural, item.count)
        } else {
            pluralString(R.plurals.meera_mention_comment_group_your_users_plural, item.count)
        }
    }

    private fun getGroupedMentionYouInGroupComment(item: NotificationUiModel): String =
        context.pluralString(R.plurals.meera_mention_comment_group_plural, item.count)

    private fun getGroupedCommentPostYoursText(item: NotificationUiModel): String = with(context) {
        return@with if (item.isSingleAuthor()) {
            pluralString(R.plurals.notification_plural_post_your_comment_by_single_user, item.count)
        } else {
            pluralString(R.plurals.notification_plural_post_your_comment, item.count)
        }
    }

    private fun getGroupedCommentReplyText(item: NotificationUiModel): String = with(context) {
        return@with if (item.isSingleAuthor()) {
            string(R.string.meera_notification_grouped_comment_by_single_user_reply)
        } else {
            string(R.string.meera_notification_grouped_group_comment_reply)
        }
    }

    private fun NotificationUiModel.isSingleAuthor(): Boolean = users.size == 1


    private fun itemHasNoPostfixText(item: NotificationUiModel, media: Media? = null): Boolean {
        val noEventTitle = item.meta.hasEventOnMap.isTrue() && item.meta.eventTitle.isNullOrEmpty()
        val noPostContent = item.meta.hasEventOnMap.isNotTrue() && item.meta.postText.isNullOrEmpty() && media == null
        return noEventTitle || noPostContent
    }

    private fun getItemNoMediaPostfixText(item: NotificationUiModel): String =
        if (item.meta.hasEventOnMap.isTrue()) {
            item.meta.eventTitle.orEmpty()
        } else {
            item.meta.postText.orEmpty()
        }

    private fun getTrackString(artist: String, track: String): String {
        return "$artist — $track"
    }

    private fun String.makeTextForMoment(item: NotificationUiModel): Spannable =
        String.format(this, item.count).toSpannableStringBuilderFromHtml()

    private fun getPostChip(notification: NotificationUiModel): Int? {
        val type = notification.type
        val meta = notification.meta
        return when (type) {
            SUBSCRIBERS_POST_CREATE -> {
                if (meta.hasEventOnMap.isTrue()) {
                    R.drawable.ic_outlined_calendar_s
                } else {
                    R.drawable.ic_outlined_post_s
                }
            }
            NOTIFY_PEOPLE,
            SUBSCRIBERS_AVATAR_POST_CREATE -> R.drawable.ic_outlined_user_s
            POST_COMMENT_YOUR,
            GROUP_COMMENT_YOUR -> R.drawable.ic_outlined_message_s
            POST_COMMENT -> R.drawable.ic_outlined_post_s
            MENTION_POST,
            MENTION_MAP_EVENT,
            MENTION_COMMENT,
            ADD_TO_GROUP_CHAT,
            POST_COMMENT_REPLY,
            GROUP_COMMENT,
            GROUP_COMMENT_REPLY -> R.drawable.ic_outlined_dog_s
            FRIEND_REQUEST,
            FRIEND_CONFIRM -> R.drawable.ic_outlined_group_s
            COMMUNITY_NEW_POST -> R.drawable.ic_outlined_group_s
            PUSH_GROUP_REQUEST -> R.drawable.ic_outlined_group_s
            GIFT_RECEIVED_NOTIFICATION -> R.drawable.ic_outlined_gift_s
            BIRTHDAY -> R.drawable.ic_outlined_gift_s
            EVENT_CALL_UNAVAILABLE -> R.drawable.ic_outlined_phone_s
            EVENT_PARTICIPANT,
            EVENT_START_SOON -> R.drawable.ic_outlined_calendar_s
            else -> null
        }
    }

}
