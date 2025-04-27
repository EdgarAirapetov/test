package com.numplates.nomera3.modules.chat.ui.mapper

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.withTranslation
import androidx.core.text.inSpans
import com.google.gson.Gson
import com.meera.core.extensions.dp
import com.meera.core.extensions.fromJson
import com.meera.core.extensions.toBoolean
import com.meera.uikit.widgets.adult.UiKitAdultOverlayConfig
import com.meera.uikit.widgets.chat.repost.UiKitRepostConfig
import com.meera.uikit.widgets.dpToPx
import com.meera.uikit.widgets.musicplayer.MusicPlayWidgetConfig
import com.meera.uikit.widgets.userpic.UserpicSizeEnum
import com.meera.uikit.widgets.userpic.UserpicStoriesStateEnum
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.OBSCENE_WORDS_MASK
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.MediaAssetDto
import com.numplates.nomera3.data.network.Post
import com.numplates.nomera3.modules.chat.ui.model.MessageConfigWrapperUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageUiModel
import com.numplates.nomera3.modules.common.CharToImageMapper
import com.numplates.nomera3.modules.common.ObsceneWordImageProvider
import com.numplates.nomera3.modules.maps.domain.events.model.EventType
import com.numplates.nomera3.modules.maps.ui.events.mapper.EventsCommonUiMapper
import com.numplates.nomera3.modules.newroads.data.PostsRepository
import com.numplates.nomera3.presentation.view.utils.NTime
import timber.log.Timber
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

private const val AT_LEAST_MEDIA = 2
private const val ICON_SPACING = 4
private const val SPACE_SYMBOL = " "
private const val BULLET_SYMBOL = "\u2022"
private const val REPOST_POST_CONTENT_LINE_HEIGHT = 22
private const val REPOST_EVENT_INFO_LINE_HEIGHT = 18

class RepostMessageConfigMapper @Inject constructor(
    private val context: Context,
    private val gson: Gson,
    private val postsRepository: PostsRepository,
    private val charToImageMapper: CharToImageMapper,
    private val commonUiMapper: EventsCommonUiMapper,
    private val obsceneWordImageProvider: ObsceneWordImageProvider,
    replyMessageMapper: ReplyMessageMapper,
) : BaseConfigMapper(context, replyMessageMapper) {

    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    override fun getConfig(message: MessageUiModel, isGroupChat: Boolean): MessageConfigWrapperUiModel {
        val postMap = message.attachments?.attachments?.first()?.repost ?: emptyMap()
        val post = gson.fromJson<Post?>(postMap)
        Timber.d("gson.fromJson<Post?>(postMap): $post")
        return MessageConfigWrapperUiModel.Repost(
            UiKitRepostConfig(
                isMe = message.isMy,
                removedContentText = getPostDeletedStatus(post),
                postAuthor = post?.user?.name,
                postSubtitle = getTimeAgo(post),
                postUserPick = UserpicUiModel(
                    size = UserpicSizeEnum.Size40,
                    userAvatarUrl = post?.user?.avatarSmall,
                    userName = post?.user?.name,
                    storiesState = UserpicStoriesStateEnum.NO_STORIES
                ),
                innerPostAuthor = getRepostInnerPostAuthorTitle(post),
                innerPostSubtitle = getRepostInnerPostSubtitle(post),
                innerPostImageRes = getRepostInnerPostDrawableRes(post),
                isInnerPostAuthorVerified = isRepostInnerPostAuthorVerified(post),
                isInnerPostAuthorTopContent = isRepostInnerPostTopContent(post),
                isPostAuthorVerified = isRepostAuthorVerified(post),
                isPostAuthorTopContent = isRepostTopContent(post),
                repostEventInfo = getRepostEventInfo(post),
                repostPostDescription = getRepostEventDescription(post),
                repostPostContent = getRepostPostContent(post),
                messageContent = getMessageContent(message),
                isMultipleMedia = isMultiplePostMedia(post),
                postMediaContent = getPostImage(post),
                adultOverlayConfig = getAdultContentConfig(post),
                postMusicConfig = getMusicConfig(post),
                replyConfig = getMessageReplyConfig(message),
                forwardConfig = getMessageForwardConfig(message),
                headerConfig = getMessageHeaderNameConfig(message, isGroupChat),
                statusConfig = getMessageStatusConfig(message),
            )
        )
    }

    private fun getRepostInnerPostAuthorTitle(post: Post?): CharSequence? {
        return when {
            post?.deleted.toBoolean() -> null
            else -> post?.parentPost?.user?.name
        }
    }

    @DrawableRes
    private fun getRepostInnerPostDrawableRes(post: Post?): Int? {
        return when {
            post?.deleted.toBoolean() -> null
            else -> R.drawable.ic_outlined_post_m
        }
    }

    private fun isRepostInnerPostAuthorVerified(post: Post?): Boolean {
        return post?.parentPost?.user?.approved.toBoolean()
    }

    private fun isRepostInnerPostTopContent(post: Post?): Boolean {
        return if (!isRepostInnerPostAuthorVerified(post)) post?.parentPost?.user?.topContentMaker.toBoolean() else false
    }

    private fun getPostDeletedStatus(post: Post?): CharSequence? {
        return if (post?.deleted.toBoolean()) {
            context.getString(R.string.post_deleted)
        } else {
            null
        }
    }

    private fun getRepostEventDescription(post: Post?): CharSequence? {
        return when {
            post?.deleted.toBoolean() -> null
            else -> post?.event?.title
        }
    }

    private fun getAdultContentConfig(post: Post?): UiKitAdultOverlayConfig? {
        return when {
            post?.deleted.toBoolean() || post?.parentPost != null -> null
            post?.isAdultContent == true
                && getPostImage(post) != null
                && !postsRepository.isMarkedAsNonSensitivePost(post.id) -> UiKitAdultOverlayConfig(
                title = context.getString(R.string.sensetive_content_title),
                subtitle = context.getString(R.string.sensetive_content_description),
                buttonTitle = context.getString(R.string.general_show)
            )

            else -> null
        }
    }

    private fun isRepostAuthorVerified(post: Post?): Boolean {
        return post?.user?.approved.toBoolean()
    }

    private fun isRepostTopContent(post: Post?): Boolean {
        return if (!isRepostAuthorVerified(post)) post?.user?.topContentMaker.toBoolean() else false
    }

    private fun getRepostEventInfo(post: Post?): CharSequence? {
        if (post?.deleted.toBoolean() || post?.parentPost != null) {
            return null
        }
        val eventDto = post?.event ?: return null
        val eventZonedDateTime = Instant.parse(eventDto.startTime).atZone(ZoneId.of(eventDto.address.timeZone))
        val time = eventZonedDateTime.toLocalTime()
        val date = eventZonedDateTime.toLocalDate()
        val timeString = timeFormatter.format(time)
        val monthTitle = commonUiMapper.mapMonthShort(date)
        val dateString = "${date.dayOfMonth} $monthTitle"
        val eventType = EventType.fromValue(eventDto.eventType)
        val builder = SpannableStringBuilder()
        val drawable =
            ResourcesCompat.getDrawable(
                context.resources,
                commonUiMapper.mapEventTypeImgSmallResId(eventType),
                null
            )
        if (drawable != null) {
            val drawableWidth = drawable.intrinsicWidth.toFloat()
            val drawableHeight = drawable.intrinsicHeight.toFloat()
            val aspectRatio = drawableWidth / drawableHeight
            drawable.setBounds(
                0,
                0,
                (REPOST_EVENT_INFO_LINE_HEIGHT.dp * aspectRatio).toInt(),
                REPOST_EVENT_INFO_LINE_HEIGHT.dp
            )
            builder.append("#", SPACE_SYMBOL)
            builder.setSpan(
                ImageSpan(drawable), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        builder.append(
            context.getString(commonUiMapper.mapEventTypeTitleResId(eventType)),
            SPACE_SYMBOL,
            BULLET_SYMBOL,
            SPACE_SYMBOL,
            dateString,
            SPACE_SYMBOL,
            BULLET_SYMBOL,
            SPACE_SYMBOL,
            timeString
        )
        return builder
    }

    private fun getRepostPostContent(post: Post?): CharSequence? {
        return if (post?.deleted.toBoolean()) {
            null
        } else {
            post?.tagSpan?.text?.takeIf { it.isNotBlank() }
                ?.let { text ->
                    charToImageMapper.mapChars(
                        imageProvider = obsceneWordImageProvider,
                        source = text,
                        mask = OBSCENE_WORDS_MASK,
                        lineHeight = REPOST_POST_CONTENT_LINE_HEIGHT.dp
                    )
                }
        }
    }

    private fun getMessageContent(message: MessageUiModel): CharSequence? {
        return message.content.rawText
    }

    private fun isMultiplePostMedia(post: Post?): Boolean {
        return post?.assets.orEmpty().size >= AT_LEAST_MEDIA
    }

    private fun getPostImage(post: Post?): String? {
        return if (post?.deleted.toBoolean() || post?.parentPost != null) {
            null
        } else {
            when {
                post?.asset?.metadata?.preview != null -> post.asset?.metadata?.preview
                post?.asset?.url != null -> post.asset?.url
                post?.assets?.firstOrNull() != null -> getUrlFromMedia(requireNotNull(post.assets?.first()))
                else -> null
            }
        }
    }

    private fun getUrlFromMedia(media: MediaAssetDto): String? {
        return when {
            !media.metadata?.preview.isNullOrEmpty() -> media.metadata?.preview
            !media.metadata?.smallUrl.isNullOrEmpty() -> media.metadata?.smallUrl
            !media.image.isNullOrEmpty() -> media.image
            !media.videoPreview.isNullOrEmpty() -> media.videoPreview
            else -> media.video
        }
    }

    private fun getTimeAgo(post: Post?): CharSequence {
        return SpannableString(NTime.timeAgo(post?.createdAt?.div(1000) ?: 0))
    }

    private fun getMusicConfig(post: Post?): MusicPlayWidgetConfig? {
        if (post?.deleted.toBoolean() || post?.parentPost != null) {
            return null
        } else {
            val media = post?.mediaEntity
            return if (media == null || media.artist == null || media.track == null || media.trackPreviewUrl == null) {
                null
            } else {
                MusicPlayWidgetConfig(
                    artistName = media.artist,
                    artistSong = media.track,
                    songUrl = media.albumUrl
                )
            }
        }
    }

    private fun getRepostInnerPostSubtitle(post: Post?): CharSequence? {
        if (post?.deleted.toBoolean() || post?.parentPost == null) {
            return null
        } else {
            val builder = SpannableStringBuilder()
            val imageSpan = object : ImageSpan(context, R.drawable.ic_outlined_repost_s) {
                override fun draw(
                    canvas: Canvas,
                    text: CharSequence?,
                    start: Int,
                    end: Int,
                    x: Float,
                    top: Int,
                    y: Int,
                    bottom: Int,
                    paint: Paint
                ) {
                    val icon = drawable
                    canvas.withTranslation(x, dpToPx(ICON_SPACING).toFloat()) {
                        icon.setTint(context.getColor(R.color.uiKitColorForegroundSecondary))
                        icon.draw(this)
                    }
                }
            }
            return builder.inSpans(imageSpan) { append(SPACE_SYMBOL) }
                .append(SPACE_SYMBOL)
                .append(context.getString(R.string.general_repost))
        }
    }
}

