package com.numplates.nomera3.modules.feed.ui.mapper

import com.meera.core.extensions.isTrue
import com.meera.core.extensions.toBoolean
import com.numplates.nomera3.MEDIA_IMAGE
import com.numplates.nomera3.MEDIA_VIDEO
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.baseCore.PostPrivacy
import com.numplates.nomera3.modules.baseCore.createAccountTypeEnum
import com.numplates.nomera3.modules.feed.data.entity.CityEntityResponse
import com.numplates.nomera3.modules.feed.data.entity.CountryEntityResponse
import com.numplates.nomera3.modules.feed.data.entity.FeatureEntityResponse
import com.numplates.nomera3.modules.feed.data.entity.FeedUpdateEvent
import com.numplates.nomera3.modules.feed.data.entity.PostEntityResponse
import com.numplates.nomera3.modules.feed.data.entity.PostsEntityResponse
import com.numplates.nomera3.modules.feed.data.entity.UserEntityResponse
import com.numplates.nomera3.modules.feed.domain.mapper.toMediaAssetEntity
import com.numplates.nomera3.modules.feed.domain.mapper.toUiMedia
import com.numplates.nomera3.modules.feed.domain.model.PostModelEntity
import com.numplates.nomera3.modules.feed.domain.model.PostsModelEntity
import com.numplates.nomera3.modules.feed.ui.adapter.FeedType
import com.numplates.nomera3.modules.feed.ui.adapter.MediaLoadingState
import com.numplates.nomera3.modules.feed.ui.data.LoadingPostVideoInfoUIModel
import com.numplates.nomera3.modules.feed.ui.entity.CityPost
import com.numplates.nomera3.modules.feed.ui.entity.CountryPost
import com.numplates.nomera3.modules.feed.ui.entity.FeatureData
import com.numplates.nomera3.modules.feed.ui.entity.PostTypeEnum
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.entity.UIPostUpdate
import com.numplates.nomera3.modules.feed.ui.entity.UserPost
import com.numplates.nomera3.modules.maps.ui.events.mapper.MapEventsUiMapper
import com.numplates.nomera3.modules.moments.show.domain.UserMomentsStateUpdateModel
import com.numplates.nomera3.modules.moments.user.data.mapper.UserMomentsMapper
import com.numplates.nomera3.modules.reaction.data.MeeraReactionUpdate
import com.numplates.nomera3.modules.reaction.data.ReactionUpdate
import com.numplates.nomera3.modules.upload.mapper.MediaPositioningMapper
import com.numplates.nomera3.presentation.utils.parseUniquename
import com.numplates.nomera3.presentation.view.utils.TextProcessorUtil


fun ReactionUpdate.toUIPostUpdate(): UIPostUpdate =
    UIPostUpdate.UpdateReaction(reactionSource.id, this)

fun MeeraReactionUpdate.toUIPostUpdate(): UIPostUpdate =
    UIPostUpdate.MeeraUpdateReaction(reactionSource.id, this)

fun PostsEntityResponse.toUiEntity(textProcessorUtil: TextProcessorUtil, editPostId: Long? = null): List<PostUIEntity> =
    posts?.map { it.toUiEntity(textProcessorUtil, editPostId = editPostId) } ?: listOf()

fun PostsModelEntity.toUiEntity(textProcessorUtil: TextProcessorUtil): List<PostUIEntity> =
    posts?.map { it.toUiEntity(textProcessorUtil) } ?: listOf()

fun PostsModelEntity.toFeatureUiEntity(textProcessorUtil: TextProcessorUtil): List<PostUIEntity> =
    features?.map { it.toUiEntity(textProcessorUtil) } ?: listOf()

fun FeedUpdateEvent.FeedUpdatePayload.toUIPostUpdate(): UIPostUpdate =
    UIPostUpdate(
        postId,
        repostCount,
        commentCount,
        reactions,
    )

fun FeedUpdateEvent.FeedUpdateMoments.toUIPostUpdate() =
    UIPostUpdate.UpdateMoments(momentHolderId, roadType, asPayload, scrollToGroupId, scrollToStart, moments, momentsBlockAvatar)

fun UserMomentsStateUpdateModel.toUIPostUpdate() =
    UIPostUpdate.UpdateUserMomentsState(userId = userId, hasMoments = hasMoments, hasNewMoments = hasNewMoments)

fun UIPostUpdate.toFeedUpdatePayload(): FeedUpdateEvent =
    FeedUpdateEvent.FeedUpdatePayload(
        postId,
        repostCount,
        commentCount,
        reactions,
    )

fun PostUIEntity.toFeedUpdatePayload(): FeedUpdateEvent.FeedUpdatePayload =
    FeedUpdateEvent.FeedUpdatePayload(
        postId,
        repostCount,
        commentCount,
        reactions,
        moments
    )

fun PostEntityResponse.toFeedUpdatePayload(): FeedUpdateEvent.FeedUpdatePayload =
    FeedUpdateEvent.FeedUpdatePayload(
        id,
        repostsCount,
    )

fun PostModelEntity.toUiEntity(
    textProcessorUtil: TextProcessorUtil,
    isInSnippet: Boolean = false,
    isNotExpandedSnippetState: Boolean = false
): PostUIEntity =
    PostUIEntity(
        postId = id,
        city = city?.toCityUiEntity(),
        country = country?.toCountryUiEntity(),
        user = user?.toUserUiEntity(),
        repostCount = repostsCount,
        commentCount = commentsCount,
        postText = text,
        //TODO реализовать маппинг и создать ui модель
        mainVehicle = mainVehicle,
        date = date,
        editedAt = editedAt,
        privacy = privacy?.toPrivacy() ?: PostPrivacy.PUBLIC,
        groupName = groupName,
        groupAvatar = groupAvatar,
        groupId = groupId,
        postSmallImage = smallImage,
        postSmallUrl = smallUrl,
        postImage = image,
        aspect = aspect,
        isAdultContent = isAdultContent,
        parentPost = parentPost?.toUiEntity(textProcessorUtil),
        videoPreview = videoPreview,
        video = video,
        videoDuration = videoDurationInSeconds,
        deleted = deleted,
        media = media?.toUiMedia(),
        tagSpan = textProcessorUtil.calculateTextLineCount(
            tagSpan = parseUniquename(text, tags),
            isMedia = this.containsMedia(),
            isInSnippet = isInSnippet
        ),
        feedType = feedType(),
        reactions = reactions,
        isPrivateGroupPost = groupType == 0,
        sourceType = sourceType,
        type = PostTypeEnum.valueOf(type),
        isAllowedToComment = isAllowedToComment,
        isPostSubscribed = itsSubscribed.toBoolean(),
        commentAvailability = commentAvailability,
        event = event?.let(MapEventsUiMapper::mapEventUiModel),
        isNotExpandedSnippetState = isNotExpandedSnippetState,
        backgroundUrl = backgroundUrl,
        backgroundId = backgroundId,
        fontColor = fontColor,
        fontSize = fontSize,
        mediaPositioning = mediaPositioning?.let(MediaPositioningMapper::mapMediaPositioning),
        isNewSubsPost = isNewSubsPost.toBoolean(),
        postUpdatingLoadingInfo = loadProgressByEditingPostId(postInEditProcess),
        isPostHidden = isPostHidden ?: false,
        assets = assets?.map { it.toMediaAssetEntity() }
    )

fun PostEntityResponse.toUiEntity(
    textProcessorUtil: TextProcessorUtil,
    isInSnippet: Boolean = false,
    isNotExpandedSnippetState: Boolean = false,
    editPostId : Long? = null
): PostUIEntity =
    PostUIEntity(
        postId = id,
        city = city?.toCityUiEntity(),
        country = country?.toCountryUiEntity(),
        user = user?.toUserUiEntity(),
        repostCount = repostsCount,
        commentCount = commentsCount,
        postText = text,
        //TODO реализовать маппинг и создать ui модель
        mainVehicle = mainVehicle,
        date = date,
        editedAt = editedAt,
        privacy = privacy?.toPrivacy() ?: PostPrivacy.PUBLIC,
        groupName = groupName,
        groupAvatar = groupAvatar,
        groupId = groupId,
        postSmallImage = smallImage,
        postSmallUrl = smallUrl,
        postImage = image,
        aspect = aspect,
        isAdultContent = isAdultContent,
        parentPost = parentPost?.toUiEntity(textProcessorUtil),
        videoPreview = videoPreview,
        video = video,
        videoDuration = videoDurationInSeconds,
        deleted = deleted,
        media = media?.toUiMedia(),
        tagSpan = textProcessorUtil.calculateTextLineCount(
            tagSpan = parseUniquename(text, tags),
            isMedia = this.containsMedia(),
            isInSnippet = isInSnippet
        ),
        feedType = feedType(),
        reactions = reactions,
        isPrivateGroupPost = groupType == 0,
        sourceType = sourceType,
        type = PostTypeEnum.valueOf(type),
        isAllowedToComment = isAllowedToComment,
        isPostSubscribed = itsSubscribed.toBoolean(),
        commentAvailability = commentAvailability,
        event = event?.let(MapEventsUiMapper::mapEventUiModel),
        isNotExpandedSnippetState = isNotExpandedSnippetState,
        backgroundUrl = backgroundUrl,
        backgroundId = backgroundId,
        fontColor = fontColor,
        fontSize = fontSize,
        mediaPositioning = mediaPositioning?.let(MediaPositioningMapper::mapMediaPositioning),
        isNewSubsPost = isNewSubsPost.toBoolean(),
        postUpdatingLoadingInfo = loadProgressByEditingPostId(editPostId),
        isPostHidden = (postHidden ?: 0).toBoolean(),
        assets = assets?.map { it.toMediaAssetEntity() }
    )

private fun PostEntityResponse.loadProgressByEditingPostId(editPostId: Long?): LoadingPostVideoInfoUIModel {
    return if (this.id == editPostId) {
        LoadingPostVideoInfoUIModel(
            loadingState = MediaLoadingState.LOADING_NO_CANCEL_BUTTON
        )
    } else {
        LoadingPostVideoInfoUIModel()
    }
}

private fun PostModelEntity.loadProgressByEditingPostId(postInEditProcess: Boolean): LoadingPostVideoInfoUIModel {
    return if (postInEditProcess) {
        LoadingPostVideoInfoUIModel(
            loadingState = MediaLoadingState.LOADING_NO_CANCEL_BUTTON,
            loadingTime = System.currentTimeMillis()
        )
    } else {
        LoadingPostVideoInfoUIModel()
    }
}

private fun CityEntityResponse.toCityUiEntity(): CityPost =
    CityPost(
        id = id,
        name = name
    )

private fun CountryEntityResponse.toCountryUiEntity() =
    CountryPost(
        id = id,
        name = name
    )

private fun UserEntityResponse.toUserUiEntity() =
    UserPost(
        userId = userId,
        name = name,
        birthday = birthday,
        avatarSmall = avatar,
        gender = gender,
        accountColor = accountColor,
        accountType = createAccountTypeEnum(accountType),
        approved = approved,
        topContentMaker = topContentMaker,
        isSystemAdministrator = isSystemAdministrator,
        subscriptionOn = settingsFlags.subscriptionOnUser,
        blackListedByMe = blacklistedByMe?.isTrue() ?: false,
        blackListedMe = blacklistedMe?.isTrue() ?: false,
        subscribedToMe = settingsFlags.subscribedToMe,
        friendStatus = settingsFlags.friendStatus,
        moments = moments?.let { UserMomentsMapper.mapUserMomentsSimpleModel(it) }
    )


private fun String.toPrivacy(): PostPrivacy = PostPrivacy.from(this)

private fun PostEntityResponse.feedType(): FeedType {
    val isVip = createAccountTypeEnum(user?.accountType) == AccountTypeEnum.ACCOUNT_TYPE_VIP

    val isVideoPost = !video.isNullOrEmpty() || (!assets.isNullOrEmpty() && assets[0].type == MEDIA_VIDEO)
    val parentPostVideo = if (parentPost != null && !parentPost.assets.isNullOrEmpty()) {
        parentPost.assets[0].video
    } else {
        parentPost?.video
    }

    return when {
        parentPost != null && !parentPostVideo.isNullOrEmpty() && isVip -> FeedType.VIDEO_REPOST_VIP
        parentPost != null && !parentPostVideo.isNullOrEmpty() -> FeedType.VIDEO_REPOST
        parentPost != null && isVip -> FeedType.REPOST_VIP
        parentPost != null -> FeedType.REPOST
        !assets.isNullOrEmpty() && assets.size > 1 -> FeedType.MULTIMEDIA_POST
        isVideoPost && isVip -> FeedType.VIDEO_POST_VIP
        isVideoPost -> FeedType.VIDEO_POST
        isVip -> FeedType.IMAGE_POST_VIP
        else -> FeedType.IMAGE_POST
    }
}

private fun PostModelEntity.feedType(): FeedType {
    val isVip = createAccountTypeEnum(user?.accountType) == AccountTypeEnum.ACCOUNT_TYPE_VIP

    val repostAssets = parentPost?.assets
    val hasVideoInRepost = if (!parentPost?.video.isNullOrEmpty()) {
        true
    } else if (!repostAssets.isNullOrEmpty()) {
        repostAssets[0].type == MEDIA_VIDEO
    } else {
        false
    }
    val isVideoPost = !video.isNullOrEmpty() || (!assets.isNullOrEmpty() && assets[0].type == MEDIA_VIDEO)

    return when {
        parentPost != null && hasVideoInRepost && isVip -> FeedType.VIDEO_REPOST_VIP
        parentPost != null && hasVideoInRepost -> FeedType.VIDEO_REPOST
        parentPost != null && isVip -> FeedType.REPOST_VIP
        parentPost != null -> FeedType.REPOST
        !assets.isNullOrEmpty() && assets.size > 1 -> FeedType.MULTIMEDIA_POST
        isVideoPost && isVip -> FeedType.VIDEO_POST_VIP
        isVideoPost -> FeedType.VIDEO_POST
        isVip -> FeedType.IMAGE_POST_VIP
        else -> FeedType.IMAGE_POST
    }
}

private const val MULTIPLIER_FEATURE_POST = -1

private fun FeatureEntityResponse.toUiEntity(
    textProcessorUtil: TextProcessorUtil,
    isSnippet: Boolean = false
): PostUIEntity =
    PostUIEntity(
        postId = id * MULTIPLIER_FEATURE_POST,
        featureData = FeatureData(
            id = id,
            text = text,
            button = button,
            deepLink = deepLink,
            hideable = hideable,
            isClosable = isClosable == 1,
            tagSpan = textProcessorUtil.calculateTextLineCount(
                tagSpan = parseUniquename(text, tags),
                isMedia = this.containsMedia(),
                isInSnippet = isSnippet
            ),
            aspect = aspect,
            videoDuration = videoDurationInSeconds,
            video = video,
            videoPreview = videoPreview,
            image = image,
            smallImage = image,
            positions = positions,
            dismissButton = dismissButton
        ),
        feedType = FeedType.ANNOUNCEMENT
    )


fun PostEntityResponse.containsMedia(): Boolean {
    val hasImage = !image.isNullOrEmpty() || (!assets.isNullOrEmpty() && assets[0].type == MEDIA_IMAGE)
    val hasVideo = !video.isNullOrEmpty() || (!assets.isNullOrEmpty() && assets[0].type == MEDIA_VIDEO)
    return hasImage
        || hasVideo
        || this.media?.track_id.isNullOrEmpty().not()
}

fun PostModelEntity.containsMedia(): Boolean {
    val hasImage = !image.isNullOrEmpty() || (!assets.isNullOrEmpty() && assets[0].type == MEDIA_IMAGE)
    val hasVideo = !video.isNullOrEmpty() || (!assets.isNullOrEmpty() && assets[0].type == MEDIA_VIDEO)
    return hasImage
        || hasVideo
        || this.media?.track_id.isNullOrEmpty().not()
}

fun FeatureEntityResponse.containsMedia(): Boolean {
    return this.image.isNullOrEmpty().not()
        || this.video != null
}
