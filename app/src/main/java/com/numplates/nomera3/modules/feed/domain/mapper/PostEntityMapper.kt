package com.numplates.nomera3.modules.feed.domain.mapper

import com.meera.core.extensions.empty
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.toInt
import com.meera.db.models.userprofile.City
import com.meera.db.models.userprofile.Country
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.data.network.MediaAssetDto
import com.numplates.nomera3.data.network.Post
import com.numplates.nomera3.modules.baseCore.PostPrivacy
import com.numplates.nomera3.modules.baseCore.createAccountTypeEnum
import com.numplates.nomera3.modules.feed.data.entity.PostEntityResponse
import com.numplates.nomera3.modules.feed.data.entity.PostsEntityResponse
import com.numplates.nomera3.modules.feed.domain.model.PostModelEntity
import com.numplates.nomera3.modules.feed.domain.model.PostsModelEntity
import com.numplates.nomera3.modules.feed.ui.entity.CityPost
import com.numplates.nomera3.modules.feed.ui.entity.CountryPost
import com.numplates.nomera3.modules.feed.ui.entity.MediaAssetEntity
import com.numplates.nomera3.modules.feed.ui.entity.PostTypeEnum
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.entity.UiMedia
import com.numplates.nomera3.modules.feed.ui.entity.UserPost
import com.numplates.nomera3.modules.feed.ui.entity.toMediaPositioning
import com.numplates.nomera3.modules.newroads.data.entities.MediaEntity


fun PostEntityResponse.toDataPostModel(currentEditingPostId: Long? = null): PostModelEntity {
    return PostModelEntity(
        id = id,
        aspect = aspect,
        commentsCount = commentsCount,
        date = date,
        editedAt = editedAt,
        deleted = deleted,
        groupId = groupId,
        info = info,
        itsSubscribed = itsSubscribed,
        mainVehicle = mainVehicle,
        repostsCount = repostsCount,
        text = text,
        image = image,
        smallImage = smallImage,
        smallUrl = smallUrl,
        title = title,
        user = user,
        parentPost = parentPost?.toDataPostModel(),
        asset = asset,
        createdAt = createdAt,
        itemType = itemType,
        isAllowedToComment = isAllowedToComment,
        isAdultContent = isAdultContent,
        refreshItem = refreshItem,
        parentPostId = parentPostId,
        privacy = privacy,
        groupName = groupName,
        groupAvatar = groupAvatar,
        tags = tags,
        country = country,
        city = city,
        videoDurationInSeconds = videoDurationInSeconds,
        video = video,
        videoPreview = videoPreview,
        media = media,
        reactions = reactions,
        groupType = groupType,
        sourceType = sourceType,
        type = type,
        commentAvailability = commentAvailability,
        event = event,
        backgroundUrl = backgroundUrl,
        backgroundId = backgroundId,
        fontColor = fontColor,
        fontSize = fontSize,
        mediaPositioning = mediaPositioning,
        isNewSubsPost = isNewSubsPost,
        postInEditProcess = id == currentEditingPostId,
        isPostHidden = postHidden?.toBoolean(),
        assets = assets
    )
}

fun PostsEntityResponse.toDataPostsModel(currentEditingPostId: Long? = null): PostsModelEntity {
    return PostsModelEntity(
        posts = this.posts?.map { it.toDataPostModel(currentEditingPostId) } ?: listOf(),
        features = this.features,
        ads = this.ads,
        hashtag = this.hashtag
    )
}

fun PostUIEntity.toPost(): Post {
    val post = Post()
    post.id = postId
    post.city = if (city == null) null else City(city.id, city.name)
    post.country = if (country == null) null else Country(country.id, country.name)
    post.user = user?.toUserSimple()
    post.repostsCount = repostCount
    post.commentsCount = commentCount
    post.text = postText
    post.tagSpan = tagSpan
    post.mainVehicle = mainVehicle
    post.date = date ?: 0
    post.privacy = privacy?.status
    post.groupName = groupName
    post.groupAvatar = groupAvatar
    post.groupId = groupId ?: -1
    post.smallImage = getSingleSmallImage()
    post.smallUrl = postSmallUrl
    post.image = getImageUrl()
    post.aspect = getSingleAspect()
    post.isAdultContent = isAdultContent
    post.parentPost = null
    post.videoPreview = getSingleVideoPreview()
    post.video = getVideoUrl()
    post.videoDurationInSeconds = getSingleVideoDuration()
    post.deleted = deleted ?: 0
    post.mediaEntity = media?.toMediaEntity()
    post.commentAvailability = commentAvailability
    if (parentPost != null) {
        post.parentPost = parentPost.toPost()
    }
    post.postHidden = isPostHidden.toInt()
    return post
}

fun UserPost.toUserSimple(): UserSimple {
    return UserSimple(
        userId = userId,
        avatar = avatarSmall ?: "",
        name = name ?: "",
        uniqueName = null,
        accountColor = accountColor,
        accountType = accountType.value,
        birthday = birthday,
        city = City(city?.id, city?.name),
        approved = approved
    )
}

fun Post.toPostUIEntity(): PostUIEntity {
    val uiPost = PostUIEntity(
        postId = id,
        city = if (city == null) null else CityPost(city?.id, city?.name),
        country = if (country == null) null else CountryPost(country?.id, country?.name),
        user = user?.toPostUser(),
        repostCount = repostsCount,
        commentCount = commentsCount,
        postText = text ?: String.empty(),
        tagSpan = tagSpan,
        mainVehicle = mainVehicle,
        date = date,
        editedAt = editedAt,
        privacy = when (privacy) {
            PostPrivacy.PRIVATE.status -> PostPrivacy.PRIVATE
            else -> PostPrivacy.PUBLIC
        },
        groupName = groupName,
        groupAvatar = groupAvatar,
        groupId = groupId,
        postSmallImage = smallImage,
        postSmallUrl = smallUrl,
        postImage = image,
        aspect = aspect,
        isAdultContent = isAdultContent,
        videoPreview = videoPreview,
        video = video,
        videoDuration = videoDurationInSeconds,
        deleted = deleted,
        media = mediaEntity?.toUiMedia(),
        parentPost = parentPost?.toPostUIEntity(),
        reactions = reactions,
        commentAvailability = commentAvailability,
        type = PostTypeEnum.valueOf(type),
        isPostHidden = (postHidden ?: 0).toBoolean(),
        assets = assets?.map { it.toMediaAssetEntity() }
    )
    return uiPost
}

fun MediaAssetDto.toMediaAssetEntity(): MediaAssetEntity{
    return MediaAssetEntity(
        id = id,
        type = type,
        aspect = aspect ?: 1.0f,
        base64Preview = base64Preview,
        image = image,
        smallImage = smallImage,
        smallUrl = smallUrl,
        mediaPositioning = mediaPositioning?.toMediaPositioning(),
        video = video,
        videoPreview = videoPreview,
        duration = duration,
        gifPreview = gifPreview
    )
}

fun UserSimple.toPostUser(): UserPost {
    val cityId = city?.id ?: 0L
    val cityName = city?.name ?: ""
    val countryId = country?.id ?: 0L
    val countryName = country?.name ?: ""

    val postUser = UserPost()
    postUser.userId = userId
    postUser.accountColor = accountColor
    postUser.accountType = createAccountTypeEnum(accountType)
    postUser.avatarSmall = avatarSmall
    postUser.birthday = birthday
    postUser.city = if (city == null) null else CityPost(cityId, cityName)
    postUser.country = if (country == null) null else CountryPost(countryId, countryName)
    postUser.gender = gender
    postUser.hideRoadPosts = settingsFlags?.hideRoadPosts
    postUser.name = name
    postUser.subscriptionOn = settingsFlags?.subscription_on
    postUser.approved = approved
    return postUser
}

fun UiMedia.toMediaEntity(): MediaEntity = MediaEntity(
    album = album,
    albumUrl = albumUrl,
    artist = artist,
    artistUrl = artistUrl,
    recognized = recognized,
    track = track,
    track_id = trackId,
    trackPreviewUrl = trackPreviewUrl,
    trackUrl = trackUrl
)

fun MediaEntity.toUiMedia(): UiMedia = UiMedia(
    album = album,
    albumUrl = albumUrl,
    artist = artist,
    artistUrl = artistUrl,
    recognized = recognized,
    track = track,
    trackId = track_id,
    trackPreviewUrl = trackPreviewUrl ?: "",
    trackUrl = trackUrl
)
