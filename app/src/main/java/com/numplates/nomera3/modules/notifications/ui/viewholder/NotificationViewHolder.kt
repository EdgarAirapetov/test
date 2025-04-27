package com.numplates.nomera3.modules.notifications.ui.viewholder

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.text.toSpannable
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.meera.core.adapters.ImagePreviewCarouselAdapter
import com.meera.core.extensions.click
import com.meera.core.extensions.color
import com.meera.core.extensions.dp
import com.meera.core.extensions.dpToPx
import com.meera.core.extensions.empty
import com.meera.core.extensions.gone
import com.meera.core.extensions.invisible
import com.meera.core.extensions.isEllipsized
import com.meera.core.extensions.isNotTrue
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.loadGlideCenterCrop
import com.meera.core.extensions.loadGlideCircleWithPlaceHolder
import com.meera.core.extensions.pluralString
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setVisible
import com.meera.core.extensions.string
import com.meera.core.extensions.visible
import com.meera.core.utils.convertUnixDate
import com.meera.core.utils.getDurationSeconds
import com.meera.db.models.PostAsset
import com.numplates.nomera3.R
import com.numplates.nomera3.data.fcm.IPushInfo
import com.numplates.nomera3.modules.notifications.ui.adapter.IDeleting
import com.numplates.nomera3.modules.notifications.ui.entity.Media
import com.numplates.nomera3.modules.notifications.ui.entity.MentionNotificationType
import com.numplates.nomera3.modules.notifications.ui.entity.NotificationUiModel
import com.numplates.nomera3.modules.notifications.ui.entity.User
import com.numplates.nomera3.modules.notifications.ui.entity.model.NotificationTransitActions
import com.numplates.nomera3.modules.notifications.ui.entity.model.NotificationTransitActions.OnCommunityNotificationIconClicked
import com.numplates.nomera3.presentation.utils.parseUniquename
import com.numplates.nomera3.presentation.utils.spanTagsTextInPosts
import com.numplates.nomera3.presentation.view.ui.OverlapDecoration
import com.numplates.nomera3.presentation.view.ui.TextViewWithImages
import com.numplates.nomera3.presentation.view.widgets.VipView
import com.numplates.nomera3.presentation.view.widgets.VipView.Companion.TYPE_DEFAULT
import com.numplates.nomera3.presentation.view.widgets.VipView.Companion.TYPE_NO_HAT
import kotlin.math.min


val communityNotificationList = listOf(IPushInfo.COMMUNITY_NEW_POST)

const val MARGIN_WITH_RIGHT_IMAGE = 76
const val MARGIN_WITHOUT_RIGHT_IMAGE = 16
const val MARGIN_SUBTITLE = 8
const val MUSIC_SIGN = " ♫ "
private const val MAX_VISIBLE_USERS_IN_TITLE = 3

class NotificationViewHolder(
    parent: ViewGroup
) : BaseViewHolder(parent, R.layout.item_notification_base) {

    private val context = itemView.context

    private val notificationContainer: FrameLayout = itemView.findViewById(R.id.notification_container)

    private val avatarContainer: FrameLayout = itemView.findViewById(R.id.notification_avatar_container)
    private val communityNotificationIconImageView: ImageView? = itemView.findViewById(R.id.communityNotificationIcon)
    private val ivUserAvatar: VipView = itemView.findViewById(R.id.notification_user_avatar)
    private val ivTypeIcon: ImageView = itemView.findViewById(R.id.notification_type_icon)
    private val tvTitle: TextView = itemView.findViewById(R.id.tv_notification_title)
    private val tvTitleAndOthers: TextView = itemView.findViewById(R.id.tv_title_and_others)
    private val tvTime: TextView = itemView.findViewById(R.id.tv_notification_time)
    private val tvSubtitle: TextView = itemView.findViewById(R.id.tv_notification_subtitle)
    private val tvSubtitleBlack: TextView = itemView.findViewById(R.id.tv_subtitle_black)
    private val tvDescription: TextViewWithImages = itemView.findViewById(R.id.tv_notification_description)
    private val ivImageRight: ImageView = itemView.findViewById(R.id.iv_notification_image_right)
    private val imageContainer: LinearLayout = itemView.findViewById(R.id.notification_image_container)
    private val imageRecycler: RecyclerView = itemView.findViewById(R.id.notification_image_recycler)
    private val tvBtnAction: TextView = itemView.findViewById(R.id.tv_btn_notification_action)
    private val deleteContainer: FrameLayout = itemView.findViewById(R.id.fl_delete_container)
    private val videoView: ConstraintLayout = itemView.findViewById(R.id.cl_video_notif)
    private val timeVideo: TextView = itemView.findViewById(R.id.tv_video_time)
    private val rightImageContainer: CardView = itemView.findViewById(R.id.cv_right_image_container)
    private val tvMusic = itemView.findViewById<TextView>(R.id.tv_music_info)

    private var doubleAvatarView: View =
        LayoutInflater.from(itemView.context).inflate(R.layout.grouped_avatar_item, null)
    private val avaOne = doubleAvatarView.findViewById<VipView>(R.id.double_avatar_one)
    private val avaTwo = doubleAvatarView.findViewById<VipView>(R.id.double_avatar_two)
    private val ivTypeIconDouble = doubleAvatarView.findViewById<ImageView>(R.id.notification_type_icon_double)

    private var imagePreviewAdapter: ImagePreviewCarouselAdapter
    private var itemClickListener: View.OnClickListener? = null

    init {
        doubleAvatarView.layoutParams = FrameLayout.LayoutParams(dpToPx(52), dpToPx(55))
        imagePreviewAdapter = ImagePreviewCarouselAdapter()

        // Init image carousel
        imageRecycler.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(itemView.context, RecyclerView.HORIZONTAL, false)
            adapter = imagePreviewAdapter
            addItemDecoration(OverlapDecoration(dpToPx(9)))
        }
    }

    fun makeDeleteContainerVisible() {
        deleteContainer.visible()
    }

    fun bindTimeAgo(timeAgo: String) {
        tvTime.text = timeAgo
    }

    fun bindTo(
        notificationListener: (NotificationTransitActions, isGroupedNotifications: Boolean) -> Unit,
        data: NotificationUiModel,
        deletingManager: IDeleting,
        isMomentsEnabled: Boolean
    ) {
        itemClickListener = null
        ivUserAvatar.setOnClickListener(null)

        if (deletingManager.isDeleting(data.id)) {
            deleteContainer.visible()
        } else {
            deleteContainer.gone()
        }
        hideMusicContainer()

        tvBtnAction.gone()
        tvBtnAction.text = itemView.context.string(R.string.go_to_chat)
        ivUserAvatar.type = TYPE_DEFAULT

        itemClickListener = View.OnClickListener { view ->
            if (data.isGroup) {
                handleGroupedItemClick(
                    item = data,
                    onClickNotification = notificationListener
                )
            } else {
                handleSingleItemClick(
                    item = data,
                    onClickNotification = notificationListener,
                    momentScreenOpenAnimationView = ivImageRight
                )
            }
        }

        itemView.setOnClickListener(itemClickListener)

        avatarContainer.removeView(doubleAvatarView)

        if (!data.isRead) {
            notificationContainer.setBackgroundColor(itemView.context.color(R.color.color_white_f4f))
            tvBtnAction.setBackgroundResource(R.drawable.background_start_buisness_btn_unchecked)
        } else {
            notificationContainer.setBackgroundColor(Color.WHITE)
            tvBtnAction.setBackgroundResource(R.drawable.background_start_buisness_btn_checked)
        }
        tvBtnAction.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)

        tvTime.text = data.timeAgo
        ivTypeIconDouble.loadGlide(getTypeIcon(data))
        ivTypeIcon.loadGlide(getTypeIcon(data))

        if (isCommunityNotification(data.type)) {
            setCommunityNotificationIcon(data.meta.communityAvatar)
            setCommunityNotificationIconClickListener(data.meta.communityId) { selectedCommunityId ->
                notificationListener.invoke(
                    OnCommunityNotificationIconClicked(selectedCommunityId),
                    false
                )
            }
        } else {
            communityNotificationIconImageView?.gone()
            if (data.isGroup) {
                setGroupedNotificationAvatar(data) {
                    setSingleNotificationAvatar(data, isMomentsEnabled, notificationListener)
                }
                getGroupNotificationTitle(data, itemView.context)
            } else {
                if (data.type != IPushInfo.SYSTEM_NOTIFICATION
                    && data.type != IPushInfo.ADD_TO_PRIVATE_BY_MODERATOR_NSFW
                ) {
                    setSingleNotificationAvatar(data, isMomentsEnabled, notificationListener)
                    tvTitleAndOthers.gone()
                    tvTitle.text = data.users.firstOrNull()?.name
                    handleAvatarClick()
                }
            }
        }

        val mentionType: MentionNotificationType? = MentionNotificationType.make(data.type)

        if (mentionType != null) {
            when (mentionType) {
                MentionNotificationType.MENTION_POST,
                MentionNotificationType.MENTION_EVENT_POST,
                MentionNotificationType.COMMUNITY_POST_CREATE ->
                    setTypeMentionPost(data, mentionType)

                MentionNotificationType.MENTION_GROUP_CHAT ->
                    setTypeMentionChat(data)

                MentionNotificationType.MENTION_COMMENT ->
                    setTypeMentionComment(data)

                MentionNotificationType.MENTION_COMMENT_YOUR ->
                    setTypeMentionComment(data)

                MentionNotificationType.MENTION_COMMENT_GROUP ->
                    setTypeMentionComment(data)

                MentionNotificationType.MENTION_COMMENT_GROUP_YOUR ->
                    setTypeMentionComment(data)
                MentionNotificationType.MOMENT_MENTION_COMMENT ->
                    setTypeMentionComment(data)
            }
        } else {
            when (data.type) {
                IPushInfo.MOMENT_COMMENT_REPLY -> setMomentTypeCommentContent(data)
                IPushInfo.MOMENT_COMMENT_REACTION -> setCommentReaction(data)
                IPushInfo.MOMENT -> setMoment(data)
                IPushInfo.MOMENT_COMMENT -> setMomentComment(data)
                IPushInfo.MOMENT_REACTION -> setMomentReaction(data)
                IPushInfo.POST_REACTION -> setPostReaction(data)
                IPushInfo.COMMENT_REACTION -> setCommentReaction(data)
                IPushInfo.GALLERY_REACTION -> setGalleryReaction(data)
                IPushInfo.SUBSCRIBERS_AVATAR_POST_CREATE -> setTypePostCreateContent(data)
                IPushInfo.SUBSCRIBERS_POST_CREATE -> setTypePostCreateContent(data)
                IPushInfo.POST_COMMENT_YOUR -> setTypeCommentContent(data)
                IPushInfo.POST_COMMENT -> setTypeCommentContent(data)
                IPushInfo.POST_COMMENT_REPLY -> setTypeCommentContent(data)
                IPushInfo.GROUP_COMMENT_YOUR -> setTypeCommentContent(data)
                IPushInfo.GROUP_COMMENT -> setTypeCommentContent(data)
                IPushInfo.GROUP_COMMENT_REPLY -> setTypeCommentContent(data)
                IPushInfo.FRIEND_REQUEST -> setSubtitleText(data)
                IPushInfo.FRIEND_CONFIRM -> {
                    setSubtitleText(data)
                    setActionButton(itemView.context.string(R.string.start_chat))
                }
                IPushInfo.NOTIFY_PEOPLE -> setupNotifyPeopleNotification(data)
                IPushInfo.GIFT_RECEIVED_NOTIFICATION -> setTypeGiftReceivedContent(data)
                IPushInfo.ADD_TO_GROUP_CHAT -> setSubtitleText(data)
                IPushInfo.ADD_TO_PRIVATE_BY_MODERATOR_NSFW -> setSubtitleModerationNSFW(data)
                IPushInfo.SYSTEM_NOTIFICATION -> setSystemContent(data)
                IPushInfo.CREATE_ANIMATED_AVATAR -> setSystemContent(data)
                IPushInfo.PUSH_GROUP_REQUEST -> setSubtitleText(data)
                IPushInfo.BIRTHDAY -> setBirthdayContent(data)
                IPushInfo.COMMUNITY_NEW_POST -> bindAsNewPostInCommunity(data)
                IPushInfo.USER_SOFT_BLOCKED -> setUserSoftBlocked(data)
                IPushInfo.EVENT_START_SOON -> setEventStartSoon(data)
                IPushInfo.EVENT_CALL_UNAVAILABLE -> setCallUnavailable(data)
                IPushInfo.EVENT_PARTICIPANT -> setEventParticipant(data)
            }
        }

        handleActionButtonClicks(data, notificationListener)
    }

    private fun setupNotifyPeopleNotification(data: NotificationUiModel) {
        tvTitle.text = itemView.context.getString(R.string.notifications_know_friend_question, data.users[0].name)
        tvSubtitle.text = itemView.context.getString(R.string.notifications_recommended_friend)
        tvSubtitle.visible()
        tvDescription.gone()
        ivImageRight.gone()
        videoView.gone()
        setActionButton(itemView.context.string(R.string.watch_profile))
    }

    private fun handleActionButtonClicks(
        item: NotificationUiModel,
        onClickNotification: (NotificationTransitActions, isGroupedNotifications: Boolean) -> Unit
    ) {
        tvBtnAction.click {
            when (item.type) {
                IPushInfo.FRIEND_CONFIRM -> {
                    val action = NotificationTransitActions.OnTransitToChatScreen(item.users[0])
                    onClickNotification.invoke(action, false)
                }
                IPushInfo.GIFT_RECEIVED_NOTIFICATION -> {
                    val action = NotificationTransitActions.OnTransitToGiftScreen
                    onClickNotification.invoke(action, false)
                }
                IPushInfo.BIRTHDAY -> {
                    showToChoseGiftForFriends(item, onClickNotification)
                }
                IPushInfo.NOTIFY_PEOPLE -> {
                    val action = NotificationTransitActions.OnTransitToUserProfileScreen(item.users[0])
                        .apply {
                            notifId = item.id
                            isGroup = item.isGroup
                        }
                    onClickNotification.invoke(action, false)
                }
            }
        }
    }

    private fun setActionButton(text: String) {
        tvBtnAction.visible()
        tvBtnAction.text = text
    }

    private fun hideMusicContainer() {
        tvMusic?.gone()
    }

    private fun setEventStartSoon(item: NotificationUiModel) {
        setSubtitleText(item)
        tvDescription.isVisible = item.meta.eventTitle.isNullOrEmpty().not()
        spanTagsTextInPosts(
            context = itemView.context,
            tvText = tvDescription,
            post = parseUniquename(item.meta.eventTitle),
            movementMethod = null
        )
        if (!item.meta.eventImageUrl.isNullOrEmpty()) {
            ivImageRight.visible()
            Glide.with(itemView.context)
                .load(item.meta.eventImageUrl)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(ivImageRight)
            tvDescription.setMargins(end = dpToPx(MARGIN_WITH_RIGHT_IMAGE))
            tvSubtitle.setMargins(end = MARGIN_WITH_RIGHT_IMAGE.dp)
        } else {
            ivImageRight.gone()
            tvDescription.setMargins(end = dpToPx(MARGIN_WITHOUT_RIGHT_IMAGE))
            tvSubtitle.setMargins(end = 8.dp)
        }
    }

    private fun setCallUnavailable(item: NotificationUiModel) {
        setSubtitleText(item)
        val eventTitle = item.meta.eventTitle
        if (eventTitle.isNullOrEmpty().not()) {
            tvDescription.isVisible = true
            spanTagsTextInPosts(
                context = itemView.context,
                tvText = tvDescription,
                post = parseUniquename(eventTitle),
                movementMethod = null
            )
        }
        if (!item.meta.eventImageUrl.isNullOrEmpty()) {
            ivImageRight.visible()
            Glide.with(itemView.context)
                .load(item.meta.eventImageUrl)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(ivImageRight)

            tvDescription.setMargins(end = dpToPx(MARGIN_WITH_RIGHT_IMAGE))
            tvSubtitle.setMargins(end = MARGIN_WITH_RIGHT_IMAGE.dp)
        } else {
            ivImageRight.gone()
            tvDescription.setMargins(end = dpToPx(MARGIN_WITHOUT_RIGHT_IMAGE))
            tvSubtitle.setMargins(end = MARGIN_SUBTITLE.dp)
        }
    }

    private fun setEventParticipant(item: NotificationUiModel) {
        setSubtitleText(item)
        tvDescription.isVisible = item.meta.title.isNullOrEmpty().not()
        spanTagsTextInPosts(
            context = itemView.context,
            tvText = tvDescription,
            post = parseUniquename(item.meta.title),
            movementMethod = null
        )
        if (!item.meta.eventImageUrl.isNullOrEmpty()) {
            ivImageRight.visible()
            ivImageRight.loadGlideCenterCrop(item.meta.eventImageUrl)
            tvDescription.setMargins(end = dpToPx(MARGIN_WITH_RIGHT_IMAGE))
            tvSubtitle.setMargins(end = MARGIN_WITH_RIGHT_IMAGE.dp)
        } else {
            ivImageRight.gone()
            tvDescription.setMargins(end = dpToPx(MARGIN_WITHOUT_RIGHT_IMAGE))
            tvSubtitle.setMargins(end = 8.dp)
        }
    }

    private fun setCommunityNotificationIconClickListener(
        communityId: Int?,
        callback: (Int) -> Unit
    ) {
        if (communityId != null) {
            communityNotificationIconImageView?.click {
                callback.invoke(communityId)
            }
        }
    }

    private fun setCommunityNotificationIcon(communityIconUrl: String?) {
        ivUserAvatar.invisible()
        avatarContainer.removeView(doubleAvatarView)

        ivTypeIcon.visible()
        communityNotificationIconImageView?.visible()
        communityNotificationIconImageView?.loadGlideCircleWithPlaceHolder(
            path = communityIconUrl,
            placeholderResId = R.drawable.community_cover_image_placeholder_new
        )
    }

    private fun isCommunityNotification(notificationType: String?): Boolean {
        return communityNotificationList.find { it == notificationType } != null
    }

    private fun bindAsNewPostInCommunity(data: NotificationUiModel) {
        clearExtraContent()
        tvSubtitle.text = getSubtitleText(data)
    }

    private fun setTypeMentionPost(
        data: NotificationUiModel,
        type: MentionNotificationType,
    ) {
        val postData = data.meta.postAsset ?: PostAsset.buildEmpty()

        val postThumbnail = postData.metadata?.preview ?: postData.url
        val duration = postData.metadata?.duration

        if (type == MentionNotificationType.MENTION_POST || type == MentionNotificationType.MENTION_EVENT_POST) {
            tvTitle.text = data.users.firstOrNull()?.name
        } else {
            tvTitle.text = data.meta.communityName.orEmpty()
        }

        setSubtitleText(data)

        imageContainer.gone()
        if (data.isGroup) {
            return
        }

        if (!data.meta.postText.isNullOrEmpty()) {
            tvDescription.visible()
            spanTagsTextInPosts(
                context = itemView.context,
                tvText = tvDescription,
                post = parseUniquename(data.meta.tagSpan?.text),
                movementMethod = null
            )
        } else {
            tvDescription.gone()
        }

        rightImageContainer.setMargins(top = 30.dp)

        if (!postThumbnail.isNullOrEmpty()) {
            // Image right
            ivImageRight.visible()
            Glide.with(itemView.context)
                .load(postThumbnail)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(ivImageRight)

            tvDescription.setMargins(end = dpToPx(MARGIN_WITH_RIGHT_IMAGE))
            tvSubtitle.setMargins(end = MARGIN_WITH_RIGHT_IMAGE.dp)
        } else {
            ivImageRight.gone()
            tvDescription.setMargins(end = dpToPx(MARGIN_WITHOUT_RIGHT_IMAGE))
            tvSubtitle.setMargins(end = 8.dp)
        }

        if (duration != null) {
            videoView.visible()
            timeVideo.text = getDurationSeconds(duration)
        } else {
            videoView.gone()
        }
    }

    private fun setTypeMentionChat(data: NotificationUiModel) {
        setSubtitleText(data)

        imageContainer.gone()

        if (data.isGroup) {
            return
        }

        if (!data.meta.tagSpan?.text.isNullOrEmpty()) {
            tvDescription.visible()
            spanTagsTextInPosts(
                context = itemView.context,
                tvText = tvDescription,
                post = parseUniquename(data.meta.tagSpan?.text),
                movementMethod = null
            )
        } else {
            tvDescription.gone()
        }

        ivImageRight.gone()
        tvDescription.setMargins(end = dpToPx(MARGIN_WITHOUT_RIGHT_IMAGE))
    }

    private fun setTypeMentionComment(data: NotificationUiModel) {
        val postData = data.meta.postAsset ?: PostAsset.buildEmpty()

        val postThumbnail = getThumbnail(data)
        val duration = postData.metadata?.duration

        setSubtitleText(data)

        imageContainer.gone()
        if (data.isGroup) {
            return
        }

        if (!data.meta.postText.isNullOrEmpty() || !data.meta.comment.isNullOrEmpty()) {
            tvDescription.visible()
            spanTagsTextInPosts(
                context = itemView.context,
                tvText = tvDescription,
                post = parseUniquename(data.meta.tagSpan?.text),
                movementMethod = null
            )
        } else {
            tvDescription.gone()
        }

        rightImageContainer.setMargins(top = 30.dp)

        if (!postThumbnail.isNullOrEmpty()) {
            // Image right
            rightImageContainer.visible()
            ivImageRight.visible()
            Glide.with(itemView.context)
                .load(postThumbnail)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(ivImageRight)

            tvDescription.setMargins(end = dpToPx(MARGIN_WITH_RIGHT_IMAGE))
            tvSubtitle.setMargins(end = MARGIN_WITH_RIGHT_IMAGE.dp)

        } else {
            rightImageContainer.gone()
            ivImageRight.gone()
            tvDescription.setMargins(end = dpToPx(MARGIN_WITHOUT_RIGHT_IMAGE))
            tvSubtitle.setMargins(end = 8.dp)
        }

        if (duration != null) {
            videoView.visible()
            timeVideo.text = getDurationSeconds(duration)
        } else {
            videoView.gone()
        }
    }

    private fun setSingleNotificationAvatar(
        item: NotificationUiModel,
        isMomentsEnabled: Boolean,
        notificationListener: (NotificationTransitActions, isGroupedNotifications: Boolean) -> Unit
    ) {
        ivUserAvatar.visible()
        ivTypeIcon.visible()
        avatarContainer.removeView(doubleAvatarView)

        item.users.firstOrNull()?.let { user ->
            ivUserAvatar.setUp(
                itemView.context,
                user.avatarSmall,
                user.accountType,
                user.accountColor,
                hasMoments = user.hasMoments ?: false,
                hasNewMoments = user.hasNewMoments ?: false
            )

            ivUserAvatar.click {
                if (item.meta.isAnonym.not()) {
                    val action = if (user.hasMoments == true && isMomentsEnabled) {
                        NotificationTransitActions
                            .OnTransitToMomentScreen(
                                userId = user.userId.toLong(),
                                momentScreenOpenAnimationView = ivUserAvatar,
                                hasNewMoments = user.hasNewMoments
                            )
                    } else {
                        NotificationTransitActions.OnUserAvatarClicked(user, ivUserAvatar)
                    }

                    notificationListener(action, false)
                }
            }
        }
    }

    private fun setGroupedNotificationAvatar(
        item: NotificationUiModel,
        showSingleAvatar: () -> Unit
    ) {
        val users = item.users
        if (users.size > 1) {
            ivUserAvatar.gone()
            ivTypeIcon.gone()

            ivTypeIconDouble.loadGlide(getTypeIcon(item))
            avatarContainer.addView(doubleAvatarView)

            item.users.firstOrNull()?.let {
                avaOne.setUp(
                    itemView.context,
                    it.avatarSmall,
                    it.accountType,
                    it.accountColor,
                    true
                )
            }

            val secondUser = item.users[1]
            avaTwo.setUp(
                itemView.context,
                secondUser.avatarSmall,
                secondUser.accountType,
                secondUser.accountColor,
                true
            )
        } else {
            showSingleAvatar.invoke()
        }
    }

    private fun handleSingleItemClick(
        item: NotificationUiModel,
        onClickNotification: (NotificationTransitActions, isGroupedNotifications: Boolean) -> Unit,
        momentScreenOpenAnimationView: View
    ) {
        when (item.type) {
            IPushInfo.MOMENT,
            IPushInfo.MOMENT_REACTION,
            IPushInfo.MOMENT_COMMENT,
            IPushInfo.MOMENT_MENTION_COMMENT,
            IPushInfo.MOMENT_COMMENT_REACTION,
            IPushInfo.MOMENT_COMMENT_REPLY,
            MentionNotificationType.MOMENT_MENTION_COMMENT.value,
            -> {
                val userId = item.meta.momentAuthorId ?: return
                val momentId = item.meta.momentId
                val hasNewMoments = item.users.firstOrNull()?.hasNewMoments
                val transitPushInfo =
                    if (item.type == IPushInfo.MOMENT_REACTION) item.type else null
                val action = NotificationTransitActions
                    .OnTransitToMomentScreen(
                        userId = userId,
                        momentId = momentId,
                        pushInfo = transitPushInfo,
                        momentScreenOpenAnimationView = momentScreenOpenAnimationView,
                        latestReactionType = item.meta.reaction,
                        commentId = item.meta.commentId,
                        hasNewMoments = hasNewMoments
                    )
                    .apply {
                        notifId = item.id
                        isGroup = item.isGroup
                    }

                onClickNotification.invoke(action, false)
            }
            MentionNotificationType.MENTION_POST.value,
            MentionNotificationType.MENTION_EVENT_POST.value,
            IPushInfo.COMMUNITY_NEW_POST,
            IPushInfo.POST_REACTION,
            IPushInfo.SUBSCRIBERS_POST_CREATE -> {
                val transitPushInfo = if (item.type == IPushInfo.POST_REACTION) item.type else null
                val action = NotificationTransitActions
                    .OnTransitToPostScreen(item.meta.postId, transitPushInfo, item.meta.reaction)
                    .apply {
                        notifId = item.id
                        isGroup = item.isGroup
                    }

                onClickNotification.invoke(action, false)
            }

            MentionNotificationType.MENTION_COMMENT.value,
            MentionNotificationType.MENTION_COMMENT_YOUR.value,
            MentionNotificationType.MENTION_COMMENT_GROUP.value,
            MentionNotificationType.MENTION_COMMENT_GROUP_YOUR.value,
            IPushInfo.POST_COMMENT_YOUR,
            IPushInfo.POST_COMMENT,
            IPushInfo.POST_COMMENT_REPLY,
            IPushInfo.GROUP_COMMENT_YOUR,
            IPushInfo.GROUP_COMMENT,
            IPushInfo.ADD_TO_PRIVATE_BY_MODERATOR_NSFW,
            IPushInfo.GROUP_COMMENT_REPLY -> {
                val action =
                    NotificationTransitActions
                        .OnTransitToCommentPostScreen(item.meta.postId, item.meta.commentId)
                        .apply {
                            notifId = item.id
                            isGroup = item.isGroup
                        }

                onClickNotification.invoke(action, false)
            }
            IPushInfo.COMMENT_REACTION -> {
                val action =
                    NotificationTransitActions
                        .OnTransitToCommentPostScreenWithReactions(
                            postId = item.meta.postId,
                            commentId = item.meta.commentId,
                            latestReactionType = item.meta.reaction
                        )
                        .apply {
                            notifId = item.id
                            isGroup = item.isGroup
                        }

                onClickNotification.invoke(action, false)
            }
            IPushInfo.GALLERY_REACTION -> {
                val action = NotificationTransitActions
                    .OnTransitToProfileViewScreen(item.meta.postId)
                    .apply {
                        notifId = item.id
                        isGroup = item.isGroup
                    }
                onClickNotification.invoke(action, false)
                                          }
            IPushInfo.SUBSCRIBERS_AVATAR_POST_CREATE -> {
                val action = NotificationTransitActions
                    .OnTransitToPostScreen(item.meta.postId)
                    .apply {
                        notifId = item.id
                        isGroup = item.isGroup
                    }
                onClickNotification.invoke(action, false)
            }

            IPushInfo.FRIEND_REQUEST -> {
                val action = NotificationTransitActions
                    .OnTransitToIncomingFriendRequestScreen
                    .apply {
                        notifId = item.id
                        isGroup = item.isGroup
                    }

                onClickNotification.invoke(action, false)
            }

            IPushInfo.FRIEND_CONFIRM,
            IPushInfo.NOTIFY_PEOPLE -> {
                val action = NotificationTransitActions
                    .OnTransitToUserProfileScreen(item.users[0])
                    .apply {
                        notifId = item.id
                        isGroup = item.isGroup
                    }
                onClickNotification.invoke(action, false)
            }

            IPushInfo.GIFT_RECEIVED_NOTIFICATION -> {
                val action = NotificationTransitActions.OnTransitToGiftScreen
                    .apply {
                        notifId = item.id
                        item.isGroup
                    }

                onClickNotification(action, false)
            }

            MentionNotificationType.MENTION_GROUP_CHAT.value,
            IPushInfo.ADD_TO_GROUP_CHAT -> {
                val action = NotificationTransitActions
                    .OnTransitToGroupChat(item.meta.roomId)
                    .apply {
                        notifId = item.id
                        isGroup = item.isGroup
                    }

                onClickNotification.invoke(action, false)
            }

            IPushInfo.SYSTEM_NOTIFICATION -> {
                val action = NotificationTransitActions
                    .OnSystemNotification(item.meta.link)
                    .apply {
                        notifId = item.id
                        isGroup = item.isGroup
                    }

                onClickNotification.invoke(action, false)
            }

            IPushInfo.PUSH_GROUP_REQUEST -> {
                val action = NotificationTransitActions
                    .OnTransitToPrivateGroupRequest(item.meta.groupId)
                    .apply {
                        notifId = item.id
                        isGroup = item.isGroup
                    }

                onClickNotification.invoke(action, false)
            }
            IPushInfo.EVENT_CALL_UNAVAILABLE -> {
                val action = NotificationTransitActions
                    .OnTransitToUserProfileScreen(item.users[0])
                    .apply {
                        notifId = item.id
                        isGroup = item.isGroup
                    }
                onClickNotification.invoke(action, false)
            }
            IPushInfo.CREATE_ANIMATED_AVATAR -> {
                val action = NotificationTransitActions.OnCreateAvatarClicked()
                action.notifId = item.id
                onClickNotification.invoke(action, false)
            }
            IPushInfo.BIRTHDAY -> {
                showToChoseGiftForFriends(item, onClickNotification)
            }
            IPushInfo.USER_SOFT_BLOCKED -> {
                val blockedTo = item.meta.userBlockedTo ?: 0L
                val action = NotificationTransitActions.OnUserSoftBlockClicked(
                    isBlocked = blockedTo > System.currentTimeMillis() / 1000,
                    blockReason = item.meta.userBlocReason ?: String.empty(),
                    blockedTo = item.meta.userBlockedTo ?: 0
                )
                onClickNotification.invoke(action, false)
            }
            IPushInfo.EVENT_START_SOON -> {
                if (item.meta.postId != null) {
                    val action = NotificationTransitActions
                        .OnTransitToEventView(item.meta.postId)
                        .apply {
                            notifId = item.id
                            isGroup = item.isGroup
                        }
                    onClickNotification.invoke(action, false)
                }
            }
            IPushInfo.EVENT_PARTICIPANT -> {
                if (item.meta.postId != null) {
                    val action = NotificationTransitActions
                        .OnTransitToPostScreen(item.meta.postId)
                        .apply {
                            notifId = item.id
                            isGroup = item.isGroup
                        }
                    onClickNotification.invoke(action, false)
                }
            }
        }
    }

    private fun showToChoseGiftForFriends(
        item: NotificationUiModel,
        onClickNotification: (NotificationTransitActions, isGroupedNotifications: Boolean) -> Unit
    ) {
        val user = item.users.firstOrNull()
        val action = NotificationTransitActions
            .OnBirthdayFriendClicked(user?.userId?.toLong(), user?.name, user?.birthday ?: 0)
            .apply {
                notifId = item.id
                isGroup = item.isGroup
            }
        onClickNotification.invoke(action, false)
    }

    // Обработка нажатия по всему item если уведомление сгрупированно
    private fun handleGroupedItemClick(
        item: NotificationUiModel,
        onClickNotification: (NotificationTransitActions, isGroupedNotifications: Boolean) -> Unit
    ) {
        val action = NotificationTransitActions
            .OnTransitGroupNotificationsScreen(item.id, item.type, item.isRead).apply {
                notifId = item.id
                isGroup = true
            }

        onClickNotification.invoke(action, true)
    }

    private fun handleAvatarClick() {
        avatarContainer.click {}
    }

    private fun setTypeGiftReceivedContent(item: NotificationUiModel) {
        if (item.meta.isAnonym && item.users.isEmpty()) {
            clearExtraContent()
            tvTitle.setText(R.string.gift_for_you)
            tvSubtitle.setText(R.string.from_anonym_user)

            ivUserAvatar.setUp(
                itemView.context,
                R.drawable.ic_anonym_notification,
                accountType = 0,
                frameColor = 0
            )

            ivUserAvatar.visible()
        } else {
            setSubtitleText(item)

            val user = item.users.find { it.userId.toLong() == item.meta.fromUserId }
            tvTitle.text = user?.name

            ivUserAvatar.setUp(
                itemView.context,
                user?.avatarSmall,
                accountType = user?.accountType,
                frameColor = 0
            )

            ivUserAvatar.visible()
        }

        tvBtnAction.visible()
        tvBtnAction.text = itemView.context.string(R.string.go_to_gift)

        // Gift comment if exists
        item.meta.comment?.let { text ->
            if (text.isEmpty()) {
                tvDescription.gone()
            } else {
                tvDescription.visible()
                tvDescription.setMargins(end = dpToPx(MARGIN_WITH_RIGHT_IMAGE))
                spanTagsTextInPosts(
                    context = itemView.context,
                    tvText = tvDescription,
                    post = parseUniquename(text),
                    movementMethod = null
                )
            }
        } ?: kotlin.run {
            tvDescription.gone()
        }

        // Image right
        ivImageRight.visible()
        videoView.gone()

        Glide.with(itemView.context)
            .load(item.meta.image)
            .centerCrop()
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(ivImageRight)
    }

    private fun setTypePostCreateContent(item: NotificationUiModel) {
        val postData = item.meta.postAsset ?: PostAsset.buildEmpty()

        val postThumbnail = postData.metadata?.preview ?: postData.url
        val duration = postData.metadata?.duration

        // Если есть текст в посте -> изображение справа, если нет, то внизу
        setSubtitleText(item)

        imageContainer.gone()
        if (item.isGroup) {
            return
        }

        if (item.meta.hasEventOnMap.isTrue()) {
            if (item.meta.eventTitle.isNullOrEmpty().not()) {
                tvDescription.visible()
                spanTagsTextInPosts(
                    context = itemView.context,
                    tvText = tvDescription,
                    post = parseUniquename(item.meta.eventTitle),
                    movementMethod = null
                )
            } else {
                tvDescription.gone()
            }
        } else {
            if (!item.meta.postText.isNullOrEmpty()) {
                tvDescription.visible()
                spanTagsTextInPosts(
                    context = itemView.context,
                    tvText = tvDescription,
                    post = parseUniquename(item.meta.postText),
                    movementMethod = null
                )
            } else {
                tvDescription.gone()
            }
        }

        rightImageContainer.setMargins(top = 30.dp)
        if (!postThumbnail.isNullOrEmpty()) {
            // Image right
            rightImageContainer.visible()
            ivImageRight.visible()
            Glide.with(itemView.context)
                .load(postThumbnail)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(ivImageRight)

            tvDescription.setMargins(end = dpToPx(MARGIN_WITH_RIGHT_IMAGE))
            tvSubtitle.setMargins(end = MARGIN_WITH_RIGHT_IMAGE.dp)
        } else {
            rightImageContainer.gone()
            ivImageRight.gone()
            tvDescription.setMargins(end = dpToPx(MARGIN_WITHOUT_RIGHT_IMAGE))
            tvSubtitle.setMargins(end = 8.dp)
        }
        if (duration != null) {
            videoView.visible()
            timeVideo.text = getDurationSeconds(duration)
        } else {
            videoView.gone()
        }
        showMusicContainer(item, postThumbnail)
    }

    private fun showMusicContainer(item: NotificationUiModel, postThumbnail: String?) {
        val track = item.meta.media?.track
        val artist = item.meta.media?.artist
        if (track != null && artist != null) {
            tvMusic?.visible()
            tvMusic?.text = getTrackString(artist, track)
        } else {
            hideMusicContainer()
        }

        if (!postThumbnail.isNullOrEmpty()) {
            tvMusic.setMargins(end = dpToPx(MARGIN_WITH_RIGHT_IMAGE))
        } else {
            tvMusic.setMargins(end = dpToPx(MARGIN_WITHOUT_RIGHT_IMAGE))
        }
    }

    private fun getTrackString(artist: String, track: String): String {
        return "$artist — $track"
    }

    private fun setBirthdayContent(item: NotificationUiModel) {
        val ctx = itemView.context
        val users = item.users
        val titleRes = when {
            users.size > 1 -> R.string.celebrates_birthdays
            else -> R.string.happy_bitrhday
        }
        tvTitle.setText(titleRes)
        val subtitleText = when (users.size) {
            1 -> String.format(
                ctx.getString(R.string.birthday_waiting_your_wishes),
                users[0].name,
            ).toSpannableStringBuilderFromHtml()
            2 -> String.format(
                ctx.getString(R.string.birthday_waiting_your_wishes_2),
                users[0].name,
                users[1].name,
            ).toSpannableStringBuilderFromHtml()
            3 -> String.format(
                ctx.getString(R.string.birthday_waiting_your_wishes_3),
                users[0].name,
                users[1].name,
                users[2].name,
            ).toSpannableStringBuilderFromHtml()
            in 4..Int.MAX_VALUE -> String.format(
                ctx.getString(R.string.birthday_waiting_your_wishes_n),
                users[0].name,
                users[1].name,
                users.size.toString(),
            ).toSpannableStringBuilderFromHtml()
            else -> String.empty()
        }
        tvSubtitle.text = subtitleText
        if (users.size == 1) {
            val gift = ContextCompat.getDrawable(ctx, R.drawable.ic_gift_purple_notification)
            tvBtnAction.setCompoundDrawablesWithIntrinsicBounds(gift, null, null, null)
            tvBtnAction.compoundDrawablePadding = 8.dp
            tvBtnAction.text = ctx?.getString(R.string.select_gift)
            tvBtnAction.visible()
        }
        tvDescription.gone()
        rightImageContainer.gone()
    }

    private fun getGroupNotificationTitle(item: NotificationUiModel, context: Context?) {
        tvTitle.text = generateGroupedTitleText(
            users = item.users,
            withTrail = true,
            context = context,
        )
        tvTitle.post {
            if (tvTitle.isEllipsized()) {
                tvTitle.setMargins(end = dpToPx(4))
                tvTitle.text = generateGroupedTitleText(
                    item.users,
                    withTrail = true,
                    context,
                )
                item?.users?.size?.takeIf { it >= MAX_VISIBLE_USERS_IN_TITLE }?.run { tvTitleAndOthers?.visible() }
            } else {
                tvTitle.setMargins(end = dpToPx(MARGIN_WITHOUT_RIGHT_IMAGE))
                tvTitleAndOthers.gone()
            }
        }
    }

    // withTrail: Boolean -- with or not "and others" in string trail
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
            builder.append(context?.getString(R.string.group_title_text_separator_for_two) ?: " и ")
            builder.append(users[1].name)
        } else if (users.size > 2) {
            val minUsersCount = min(users.size, MAX_VISIBLE_USERS_IN_TITLE)
            // add all items with separator except last item
            for (i in 0 until minUsersCount) {
                builder.append(users[i].name)
                if (i == minUsersCount - 1) builder.append(SPACE) else builder.append(SEPARATOR)
            }
            if (withTrail && users.size > MAX_VISIBLE_USERS_IN_TITLE) {
                builder.append(context?.getString(R.string.and_others))
            }
        }
        return builder.toString()
    }

    private fun setMomentTypeCommentContent(item: NotificationUiModel) {
        setSubtitleText(item)
        setDescription(item)
        setThumbnailImage(item)
    }

    private fun setTypeCommentContent(item: NotificationUiModel) {
        setSubtitleText(item)

        if (item.isGroup) {
            tvDescription.gone()
        } else {
            tvDescription.visible()
            spanTagsTextInPosts(
                context = itemView.context,
                tvText = tvDescription,
                post = parseUniquename(item.meta.comment),
                movementMethod = null
            )
        }
        when (item.type) {
            IPushInfo.POST_COMMENT_YOUR,
            IPushInfo.POST_COMMENT_REPLY,
            IPushInfo.POST_COMMENT -> {
                setThumbnailImage(item)
            }
        }
    }

    private fun checkActionForSystemContent(type: String) {

        when (type) {
            IPushInfo.CREATE_ANIMATED_AVATAR -> {
                tvSubtitle.maxLines = 10
                tvBtnAction.visible()
                tvBtnAction.setText(R.string.create_avatar_action)
            }
        }

    }

    private fun setSystemContent(item: NotificationUiModel) {
        clearExtraContent()
        val subtitle = item.meta.text ?: ""
        checkActionForSystemContent(item.type)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tvSubtitle.text = Html.fromHtml(subtitle, Html.FROM_HTML_MODE_LEGACY)
        } else {
            tvSubtitle.text = Html.fromHtml(subtitle)
        }

        ivTypeIcon.gone()
        tvTitle.text = item.meta.title ?: ""
        itemClickListener?.let { if (item.meta.isAnonym.not()) ivUserAvatar.setOnClickListener(it) }
        ivUserAvatar.type = TYPE_NO_HAT
        ivUserAvatar.hideHolidayHat()
        ivUserAvatar.setUp(
            itemView.context,
            item.meta.avatar ?: R.drawable.system_notification_img_new,
            accountType = 0,
            frameColor = 0
        )

        ivUserAvatar.visible()
        if (item.meta.image.isNullOrEmpty()) return

        tvSubtitle.setMargins(end = dpToPx(MARGIN_WITH_RIGHT_IMAGE))
        ivImageRight.visible()
        Glide.with(itemView.context)
            .load(item.meta.image)
            .centerCrop()
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(ivImageRight)
    }

    private fun setUserSoftBlocked(item: NotificationUiModel) {
        clearExtraContent()
        ivTypeIcon.gone()

        ivUserAvatar.type = TYPE_NO_HAT
        ivUserAvatar.hideHolidayHat()
        ivUserAvatar.setUp(
            itemView.context,
            R.drawable.system_notification_img_new,
            accountType = null,
            frameColor = null
        )
        ivUserAvatar.visible()

        tvTitle.text = context.getString(R.string.general_noomeera)

        tvSubtitleBlack.visible()
        tvSubtitleBlack.text = context.getString(
            R.string.post_main_road_forbidden_until,
            convertUnixDate(item.meta.userBlockedTo)
        )

        val subtitle = context.getString(
            R.string.user_complain_reason_dialog_message,
            item.meta.userBlocReason
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tvSubtitle.text = Html.fromHtml(subtitle, Html.FROM_HTML_MODE_LEGACY)
        } else {
            tvSubtitle.text = Html.fromHtml(subtitle)
        }
        tvSubtitle.setMargins(end = dpToPx(MARGIN_WITH_RIGHT_IMAGE))

        itemClickListener?.let { if (item.meta.isAnonym.not()) ivUserAvatar.setOnClickListener(it) }
    }

    private fun setMoment(item: NotificationUiModel) {
        setSubtitleText(item)
        if (item.isGroup.not()) {
            setThumbnailImage(item)
        }
    }

    private fun setPostReaction(item: NotificationUiModel) {
        setSubtitleText(item)

        if (item.isGroup) {
            tvDescription.gone()
        } else {
            tvDescription.visible()
            spanTagsTextInPosts(
                context = itemView.context,
                tvText = tvDescription,
                post = parseUniquename(item.meta.comment),
                movementMethod = null
            )
        }

        setThumbnailImage(item)
    }

    private fun setGalleryReaction(item: NotificationUiModel) {
        setSubtitleText(item)

        if (item.isGroup) {
            tvDescription.gone()
        } else {
             tvDescription.visible()
            spanTagsTextInPosts(
                context = itemView.context,
                tvText = tvDescription,
                post = parseUniquename(item.meta.comment),
                movementMethod = null
            )
         }

         setThumbnailImage(item)
     }

    private fun setMomentReaction(item: NotificationUiModel) {
        setSubtitleText(item)
        setDescription(item)
        setThumbnailImage(item)
    }

    private fun setDescription(item: NotificationUiModel) {
        tvDescription.setVisible(item.isGroup.not())
        spanTagsTextInPosts(
            context = itemView.context,
            tvText = tvDescription,
            post = parseUniquename(item.meta.comment),
            movementMethod = null
        )
    }

    private fun setMomentComment(item: NotificationUiModel) {
        setSubtitleText(item)
        setDescription(item)
        setThumbnailImage(item)
    }

    private fun getThumbnail(item: NotificationUiModel): String? {
        val postAsset = item.meta.postAsset
        val momentAsset = item.meta.momentAsset
        return postAsset?.metadata?.preview ?: postAsset?.url ?: momentAsset?.preview ?: momentAsset?.url
    }

    private fun setThumbnailImage(item: NotificationUiModel) {
        val postData = item.meta.postAsset
        val thumbnail = getThumbnail(item)

        val duration = postData?.metadata?.duration
        if (thumbnail.isNullOrEmpty()) return

        imageContainer.gone()
        tvDescription.setMargins(end = dpToPx(MARGIN_WITH_RIGHT_IMAGE))
        tvSubtitle.setMargins(end = dpToPx(MARGIN_WITH_RIGHT_IMAGE))
        tvTitleAndOthers.post {
            if (tvTitleAndOthers.visibility == View.VISIBLE) {
                rightImageContainer.setMargins(top = 60.dp)
            } else {
                rightImageContainer.setMargins(top = 30.dp)
            }
        }

        if (!thumbnail.isNullOrEmpty()) {
            // Image right
            ivImageRight.visible()
            rightImageContainer.visible()
            Glide.with(itemView.context)
                .load(thumbnail)
                .centerCrop()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(ivImageRight)
        } else {
            ivImageRight.gone()
            rightImageContainer.gone()
        }
        if (duration != null) {
            videoView.visible()
            timeVideo.text = getDurationSeconds(duration)
        } else {
            videoView.gone()
        }
    }

    private fun setCommentReaction(item: NotificationUiModel) {
        setSubtitleText(item)

        if (item.isGroup) {
            tvDescription.gone()
        } else {
            tvDescription.visible()
            spanTagsTextInPosts(
                context = itemView.context,
                tvText = tvDescription,
                post = parseUniquename(item.meta.comment),
                movementMethod = null
            )
        }
    }

    private fun setSubtitleText(item: NotificationUiModel) {
        clearExtraContent()
        tvSubtitle.text = getSubtitleText(item)
    }

    private fun setSubtitleModerationNSFW(item: NotificationUiModel) {
        clearExtraContent()

        tvTitle.text = tvTitle.context?.getString(R.string.moved_to_private_road) ?: ""
        ivUserAvatar.type = TYPE_NO_HAT
        ivUserAvatar.hideHolidayHat()
        ivUserAvatar.setUp(
            itemView.context,
            R.drawable.system_notification_img_new,
            accountType = 0,
            frameColor = 0
        )

        itemClickListener?.let { if (item.meta.isAnonym.not()) ivUserAvatar.setOnClickListener(it) }

        ivUserAvatar.visible()
        ivTypeIcon.gone()
        tvSubtitle.setMargins(end = 8.dp)
        tvTitleAndOthers.gone()
        tvSubtitle.text = getSubtitleText(item)
    }

    private fun String.toSpannableStringBuilderFromHtml(): SpannableStringBuilder {
        val s = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(this)
        }

        return SpannableStringBuilder(s)
    }

    private fun String.makeTextForMoment(item: NotificationUiModel): Spannable =
        String.format(this, item.count).toSpannableStringBuilderFromHtml()

    private fun String.makeTextForPost(
        item: NotificationUiModel,
        isNotMention: Boolean = true,
        allowMetaText: Boolean = true,
        media: Media? = null
    ): Spannable =
        if (!allowMetaText || itemHasNoPostfixText(item = item, media = media))
            this.toSpannableStringBuilderFromHtml()
                .append(".")
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
            toSpannableStringBuilderFromHtml()
                .append(".")
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

    // Clear extras
    private fun clearExtraContent() {
        tvDescription.gone()
        tvBtnAction.gone()
        ivImageRight.gone()
        imageContainer.gone()
        imageRecycler.gone()
        tvSubtitle.setMargins(end = 8.dp)
        tvSubtitleBlack.apply {
            text = ""
            gone()
        }
    }

    private fun getSubtitleText(item: NotificationUiModel): Spannable = with(itemView.context) {
        return if (!item.isGroup) {
            when (item.type) {
                IPushInfo.MOMENT_COMMENT_REPLY -> {
                    string(R.string.notification_moment_comment_reply)
                        .toSpannableStringBuilderFromHtml()
                }
                IPushInfo.MOMENT_COMMENT_REACTION -> {
                    string(
                        R.string.notification_moment_comment_reaction,
                        item.meta.reaction?.characterRepresentation,
                        ""
                    ).toSpannableStringBuilderFromHtml()
                }
                IPushInfo.MOMENT_MENTION_COMMENT -> {
                    string(
                        R.string.mention_comment_moment_comment_notification,
                    ).toSpannableStringBuilderFromHtml()
                }
                IPushInfo.MOMENT_COMMENT -> {
                    string(R.string.notification_moment_comment).toSpannableStringBuilderFromHtml()
                }
                IPushInfo.MOMENT -> {
                    string(R.string.notification_moment).toSpannableStringBuilderFromHtml()
                }
                IPushInfo.MOMENT_REACTION -> {
                    string(
                        R.string.notification_moment_reaction,
                        item.meta.reaction?.characterRepresentation
                    ).toSpannableStringBuilderFromHtml()
                }
                IPushInfo.POST_REACTION -> {
                    val strResId = if (item.meta.hasEventOnMap.isTrue()) {
                        R.string.notification_event_post_reaction
                    } else {
                        R.string.notification_post_reaction
                    }
                    string(strResId, item.meta.reaction?.characterRepresentation)
                        .makeTextForPost(
                            item = item,
                            media = item.meta.media
                        )
                }
                IPushInfo.GALLERY_REACTION -> {
                    string(R.string.notification_gallery_reaction, item.meta.reaction?.characterRepresentation)
                        .makeTextForPost(
                            item = item,
                            media = item.meta.media
                        )
                }
                IPushInfo.COMMENT_REACTION ->
                    string(R.string.notification_post_comment_reaction, item.meta.reaction?.characterRepresentation)
                        .makeTextForPost(
                            item = item,
                            media = item.meta.media
                        )

                IPushInfo.SUBSCRIBERS_POST_CREATE -> {
                    val strResId = if (item.meta.hasEventOnMap.isTrue()) {
                        R.string.notification_subscriber_event_post_create
                    } else {
                        R.string.notification_subscriber_post_create
                    }
                    string(strResId)
                        .makeTextForPost(
                            item = item,
                            allowMetaText = false
                        )
                }

                IPushInfo.SUBSCRIBERS_AVATAR_POST_CREATE ->
                    string(R.string.notification_avatar_post_create)
                        .makeTextForPost(
                            item = item,
                            allowMetaText = false
                        )

                IPushInfo.POST_COMMENT_YOUR -> {
                    val strResId = if (item.meta.hasEventOnMap.isTrue()) {
                        R.string.notification_event_post_comment_your
                    } else {
                        R.string.notification_post_comment_your
                    }
                    string(strResId)
                        .makeTextForPost(
                            item = item,
                            media = item.meta.media
                        )
                }

                IPushInfo.POST_COMMENT -> {
                    val strResId = if (item.meta.hasEventOnMap.isTrue()) {
                        R.string.notification_event_post_comment
                    } else {
                        R.string.notification_post_comment
                    }
                    string(strResId)
                        .makeTextForPost(
                            item = item,
                            media = item.meta.media
                        )
                }

                IPushInfo.POST_COMMENT_REPLY ->
                    string(R.string.notification_post_comment_reply)
                        .toSpannableStringBuilderFromHtml()

                IPushInfo.GROUP_COMMENT_YOUR ->
                    string(R.string.notification_group_comment_your)
                        .toSpannableStringBuilderFromHtml()

                IPushInfo.GROUP_COMMENT ->
                    string(R.string.notification_group_comment)
                        .toSpannableStringBuilderFromHtml()

                IPushInfo.GROUP_COMMENT_REPLY ->
                    string(R.string.notification_group_comment_reply)
                        .toSpannableStringBuilderFromHtml()

                IPushInfo.FRIEND_REQUEST ->
                    string(R.string.notification_friend_request)
                        .toSpannableStringBuilderFromHtml()

                IPushInfo.FRIEND_CONFIRM ->
                    string(R.string.notification_friend_received)
                        .toSpannableStringBuilderFromHtml()

                IPushInfo.GIFT_RECEIVED_NOTIFICATION ->
                    string(R.string.notification_gift_received)
                        .toSpannableStringBuilderFromHtml()

                IPushInfo.ADD_TO_GROUP_CHAT ->
                    string(R.string.notification_add_group_chat)
                        .toSpannableStringBuilderFromHtml()

                IPushInfo.ADD_TO_PRIVATE_BY_MODERATOR_NSFW ->
                    string(R.string.moderation_nsfw).toSpannableStringBuilderFromHtml()

                IPushInfo.PUSH_GROUP_REQUEST ->
                    string(R.string.notification_group_request)
                        .toSpannableStringBuilderFromHtml()
                IPushInfo.EVENT_PARTICIPANT ->
                    string(R.string.notification_new_participant)
                        .toSpannableStringBuilderFromHtml()

                IPushInfo.EVENT_CALL_UNAVAILABLE ->
                    string(R.string.notification_call_unavailable)
                        .toSpannableStringBuilderFromHtml()

//                IPushInfo.BIRTHDAY ->
//                    string(R.string.notification_group_request)
//                            .toSpannableStringBuilderFromHtml()

                MentionNotificationType.MENTION_POST.value ->
                    if (item.count > 1) string(R.string.mention_post_notifications)
                        .makeTextForPosts(item)
                    else string(R.string.mention_post_notification)
                        .makeTextForPost(item, false)

                MentionNotificationType.MENTION_EVENT_POST.value ->
                    if (item.count > 1) string(R.string.mention_event_post_notifications)
                        .makeTextForPosts(item)
                    else string(R.string.mention_event_post_notification)
                        .makeTextForPost(item, false)

                MentionNotificationType.MENTION_GROUP_CHAT.value ->
                    string(R.string.mention_group_chat_notification)
                        .toSpannableStringBuilderFromHtml()

                MentionNotificationType.MENTION_COMMENT.value ->
                    string(R.string.mention_comment_post_comment_notification)
                        .makeTextForPostComment(item, item.meta.media)

                MentionNotificationType.MOMENT_MENTION_COMMENT.value ->
                    string(R.string.mention_comment_moment_comment_notification, item.meta.comment)
                        .toSpannableStringBuilderFromHtml()

                MentionNotificationType.MENTION_COMMENT_GROUP.value ->
                    string(R.string.mention_comment_group_post_notification)
                        .makeTextForPostComment(item, item.meta.media)

                MentionNotificationType.MENTION_COMMENT_YOUR.value ->
                    string(R.string.mention_comment_your_post_comment_notification)
                        .makeTextForPostComment(item, item.meta.media)

                MentionNotificationType.MENTION_COMMENT_GROUP_YOUR.value ->
                    string(R.string.mention_comment_your_group_post_notification)
                        .makeTextForPostComment(item, item.meta.media)

                IPushInfo.COMMUNITY_NEW_POST ->
                    string(R.string.community_new_post_published)
                        .makeTextForPost(item, false)

                IPushInfo.EVENT_START_SOON ->
                    string(R.string.map_event_start_soon)
                        .toSpannableStringBuilderFromHtml()

                else -> string(R.string.general_unknown).toSpannableStringBuilderFromHtml()
            }
        } else {
            // когда сгруппированное уведомление
            when (item.type) {
                IPushInfo.MOMENT_COMMENT_REPLY -> {
                    getGroupedMomentCommandReplyComment(item)
                        .toSpannableStringBuilderFromHtml()
                }
                IPushInfo.MOMENT_COMMENT_REACTION -> {
                    string(
                        R.string.mention_comment_reaction_notification,
                        item.meta.comment.orEmpty()
                    ).toSpannableStringBuilderFromHtml()
                }
                IPushInfo.MOMENT_MENTION_COMMENT -> {
                    string(R.string.notification_moment_comment_grouped).toSpannableStringBuilderFromHtml()
                }
                IPushInfo.MOMENT_COMMENT -> {
                    getGroupedMomentCommentText(item).makeTextForMoment(item)
                }
                IPushInfo.MOMENT -> {
                    pluralString(R.plurals.notification_moment_grouped,item.count).makeTextForMoment(item)
                }
                IPushInfo.MOMENT_REACTION -> {
                    string(R.string.notification_moment_reaction_grouped).toSpannable()
                }
                IPushInfo.POST_REACTION -> {
                    val strResId = if (item.meta.hasEventOnMap.isTrue()) {
                        R.string.notification_event_post_reaction_grouped
                    } else {
                        R.string.notification_post_reaction_grouped
                    }
                    string(strResId).makeTextForPost(item)
                }
                IPushInfo.GALLERY_REACTION ->
                    string(R.string.notification_gallery_reaction_grouped).makeTextForPost(item)
                IPushInfo.COMMENT_REACTION ->
                    string(R.string.notification_post_comment_reaction_grouped).makeTextForPost(item)
                IPushInfo.SUBSCRIBERS_AVATAR_POST_CREATE ->
                    string(R.string.notification_avatar_post_create).makeTextForPost(item)
                IPushInfo.SUBSCRIBERS_POST_CREATE -> {
                    val pluralResId = if (item.meta.hasEventOnMap.isTrue()) {
                        R.plurals.notification_plural_event_posts
                    } else {
                        R.plurals.notification_plural_posts
                    }
                    pluralString(idRes = pluralResId, quantity = item.count)
                        .toSpannableStringBuilderFromHtml()
                }

                IPushInfo.COMMUNITY_NEW_POST -> pluralString(
                    idRes = R.plurals.community_new_many_post_published,
                    quantity = item.count
                ).toSpannableStringBuilderFromHtml()

                IPushInfo.EVENT_CALL_UNAVAILABLE -> pluralString(
                    idRes = R.plurals.notification_plural_event_calls,
                    quantity = item.count
                ).toSpannableStringBuilderFromHtml()

                IPushInfo.POST_COMMENT_YOUR ->
                    getGroupedCommentYourText(item).makeTextForPost(item)

                IPushInfo.POST_COMMENT ->
                    getGroupedPostCommentText(item).makeTextForPost(item)

                IPushInfo.POST_COMMENT_REPLY ->
                    getGroupedPostCommandReplyComment(item)
                        .toSpannableStringBuilderFromHtml()

                IPushInfo.GROUP_COMMENT_YOUR ->
                    getGroupedCommentPostYoursText(item)
                        .toSpannableStringBuilderFromHtml()

                IPushInfo.GROUP_COMMENT ->
                    string(R.string.notification_grouped_group_comment)
                        .toSpannableStringBuilderFromHtml()

                IPushInfo.GROUP_COMMENT_REPLY ->
                    getGroupedCommentReplyText(item).toSpannableStringBuilderFromHtml()

                IPushInfo.FRIEND_REQUEST ->
                    string(R.string.notification_group_friend_request)
                        .toSpannableStringBuilderFromHtml()

                IPushInfo.FRIEND_CONFIRM ->
                    string(R.string.notification_grouped_friend_received)
                        .toSpannableStringBuilderFromHtml()

                IPushInfo.GIFT_RECEIVED_NOTIFICATION ->
                    string(R.string.notification_group_gift_received)
                        .toSpannableStringBuilderFromHtml()

                IPushInfo.ADD_TO_GROUP_CHAT ->
                    string(R.string.notification_group_add_group_chat)
                        .toSpannableStringBuilderFromHtml()

                IPushInfo.PUSH_GROUP_REQUEST ->
                    string(R.string.notification_grouped__group_request)
                        .toSpannableStringBuilderFromHtml()

                IPushInfo.ADD_TO_PRIVATE_BY_MODERATOR_NSFW ->
                    string(R.string.moderation_nsfw).toSpannableStringBuilderFromHtml()

                IPushInfo.EVENT_PARTICIPANT ->
                    string(R.string.notification_group_new_participant)
                        .toSpannableStringBuilderFromHtml()

                MentionNotificationType.MENTION_POST.value ->
                    if (item.count > 1) string(R.string.mention_post_notifications)
                        .makeTextForPosts(item)
                    else string(R.string.mention_post_notification)
                        .makeTextForPost(item, false)

                MentionNotificationType.MENTION_EVENT_POST.value ->
                    if (item.count > 1) string(R.string.mention_event_post_notifications)
                        .makeTextForPosts(item)
                    else string(R.string.mention_event_post_notification)
                        .makeTextForPost(item, false)

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

                else -> string(R.string.general_unknown).toSpannableStringBuilderFromHtml()
            }
        }
    }

    private fun Context.getGroupedMentionYouInGroupChat(item: NotificationUiModel): String =
        if (item.isSingleAuthor()) {
            pluralString(R.plurals.mention_group_chat_notification_plural, item.count)
        } else {
            pluralString(R.plurals.mention_group_chat_notification_users_plural, item.count)
        }

    private fun Context.getGroupedMentionYouInYourComment(item: NotificationUiModel): String =
        if (item.isSingleAuthor())
            pluralString(R.plurals.many_mention_you_by_single_user_in_your_post, item.count)
        else
            pluralString(R.plurals.mention_comment_users_plural, item.count)

    private fun Context.getGroupedMomentCommandReplyComment(item: NotificationUiModel): String =
        if (item.isSingleAuthor()) {
            string(R.string.notification_group_moment_comment_reply_by_single_user)
        } else {
            string(R.string.notification_group_moment_comment_reply)
        }

    private fun Context.getGroupedPostCommandReplyComment(item: NotificationUiModel): String =
        if (item.isSingleAuthor()) {
            string(R.string.notification_group_post_comment_reply_by_single_user)
        } else {
            string(R.string.notification_group_post_comment_reply)
        }

    private fun Context.getGroupedMentionYouInComment(item: NotificationUiModel): String =
        if (item.isSingleAuthor()) {
            pluralString(R.plurals.mention_comment_plural, item.count)
        } else {
            pluralString(R.plurals.mention_comment_users_plural, item.count)
        }

    private fun Context.getGroupedMentionYouInYourGroupComment(item: NotificationUiModel): String =
        if (item.isSingleAuthor()) {
            pluralString(R.plurals.mention_comment_group_your_plural, item.count)
        } else {
            pluralString(R.plurals.mention_comment_group_your_users_plural, item.count)
        }

    private fun Context.getGroupedMentionYouInGroupComment(item: NotificationUiModel): String =
        pluralString(R.plurals.mention_comment_group_plural, item.count)

    private fun Context.getGroupedCommentYourText(item: NotificationUiModel): String =
        if (item.isSingleAuthor()) {
            pluralString(R.plurals.notification_plural_your_post_comment, item.count)
        } else {
            pluralString(R.plurals.notification_plural_your_post_comment_users, item.count)
        }

    private fun Context.getGroupedMomentCommentText(item: NotificationUiModel): String =
        if (item.isSingleAuthor()) {
            pluralString(R.plurals.notification_plural_moment_comment, item.count)
        } else {
            pluralString(R.plurals.notification_plural_moment_comment_users, item.count)
        }


    private fun Context.getGroupedPostCommentText(item: NotificationUiModel): String =
        if (item.isSingleAuthor()) {
            pluralString(R.plurals.notification_plural_post_comment, item.count)
        } else {
            val pluralResId = if (item.meta.hasEventOnMap.isTrue()) {
                R.plurals.notification_plural_event_post_comment_users
            } else {
                R.plurals.notification_plural_post_comment_users
            }
            pluralString(pluralResId, item.count)
        }

    private fun Context.getGroupedCommentReplyText(item: NotificationUiModel): String =
        if (item.isSingleAuthor()) {
            string(R.string.notification_grouped_comment_by_single_user_reply)
        } else {
            string(R.string.notification_grouped_group_comment_reply)
        }

    private fun Context.getGroupedCommentPostYoursText(item: NotificationUiModel): String =
        if (item.isSingleAuthor()) {
            pluralString(R.plurals.notification_plural_post_your_comment_by_single_user, item.count)
        } else {
            pluralString(R.plurals.notification_plural_post_your_comment, item.count)
        }

    private fun NotificationUiModel.isSingleAuthor(): Boolean {
        return users.size == 1
    }

    private fun getTypeIcon(notification: NotificationUiModel): Int? {
        val type = notification.type
        val meta = notification.meta
        return when (type) {
            IPushInfo.SUBSCRIBERS_POST_CREATE -> {
                if (meta.hasEventOnMap.isTrue()) {
                    R.drawable.ic_notification_type_event
                } else {
                    R.drawable.ic_notification_type_post
                }
            }
            IPushInfo.NOTIFY_PEOPLE,
            IPushInfo.SUBSCRIBERS_AVATAR_POST_CREATE -> R.drawable.ic_notification_type_user
            IPushInfo.POST_COMMENT_YOUR,
            IPushInfo.GROUP_COMMENT_YOUR -> R.drawable.ic_notification_type_comment
            IPushInfo.POST_COMMENT -> R.drawable.ic_notification_type_post
            IPushInfo.MENTION_POST,
            IPushInfo.MENTION_MAP_EVENT,
            IPushInfo.MENTION_COMMENT,
            IPushInfo.ADD_TO_GROUP_CHAT,
            IPushInfo.POST_COMMENT_REPLY,
            IPushInfo.GROUP_COMMENT,
            IPushInfo.GROUP_COMMENT_REPLY -> R.drawable.ic_notification_type_mentions
            IPushInfo.FRIEND_REQUEST,
            IPushInfo.FRIEND_CONFIRM -> R.drawable.ic_notification_type_community
            IPushInfo.COMMUNITY_NEW_POST -> R.drawable.ic_notification_type_community
            IPushInfo.PUSH_GROUP_REQUEST -> R.drawable.ic_notification_type_community
            IPushInfo.GIFT_RECEIVED_NOTIFICATION -> R.drawable.ic_notification_type_gift
            IPushInfo.BIRTHDAY -> R.drawable.ic_notification_type_birthday
            IPushInfo.EVENT_CALL_UNAVAILABLE -> R.drawable.ic_notification_type_call
            IPushInfo.EVENT_PARTICIPANT,
            IPushInfo.EVENT_START_SOON -> R.drawable.ic_notification_type_event
            else -> null
        }
    }

    companion object {
        const val SEPARATOR = ", "
        const val SPACE = " "
    }
}
