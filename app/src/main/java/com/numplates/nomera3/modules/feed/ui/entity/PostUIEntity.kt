package com.numplates.nomera3.modules.feed.ui.entity

import android.os.Parcelable
import com.meera.core.extensions.empty
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.toBoolean
import com.meera.core.preferences.AppSettings
import com.meera.db.models.message.ParsedUniquename
import com.meera.referrals.ui.model.ReferralDataUIModel
import com.numplates.nomera3.MEDIA_IMAGE
import com.numplates.nomera3.MEDIA_VIDEO
import com.numplates.nomera3.data.network.MediaPositioningDto
import com.numplates.nomera3.data.network.Vehicle
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.baseCore.PostPrivacy
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyContentType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyPostType
import com.numplates.nomera3.modules.feed.ui.adapter.FeedType
import com.numplates.nomera3.modules.feed.ui.data.LoadingPostVideoInfoUIModel
import com.numplates.nomera3.modules.maps.ui.events.model.EventUiModel
import com.numplates.nomera3.modules.moments.show.data.entity.MomentInfoCarouselUiModel
import com.numplates.nomera3.modules.moments.show.domain.UserMomentsStateUpdateModel
import com.numplates.nomera3.modules.moments.user.domain.model.UserMomentsSimpleModel
import com.numplates.nomera3.modules.newroads.data.entities.PostSourceType
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity
import com.numplates.nomera3.modules.userprofile.ui.entity.ProfileSuggestionUiModels
import com.numplates.nomera3.modules.volume.domain.model.VolumeState
import kotlinx.parcelize.Parcelize
import kotlin.math.abs
import kotlin.math.max

private const val MAX_ASPECT_FOR_LONG_PICTURE: Double = 0.5625
private const val MAX_ASPECT_FOR_WIDE_PICTURE: Double = 3.1875

@Parcelize
data class PostUIEntity(
    val postId: Long = 0,

    val city: CityPost? = null,

    val country: CountryPost? = null,

    val user: UserPost? = null,

    val repostCount: Int = 0,

    val commentCount: Int = 0,

    val postText: String = String.empty(),

    val tagSpan: ParsedUniquename? = null,

    val mainVehicle: Vehicle? = null,

    val date: Long? = null,

    val editedAt: Long? = null,

    val privacy: PostPrivacy? = PostPrivacy.PUBLIC,

    val groupName: String? = null,

    val groupAvatar: String? = null,

    val groupId: Long? = null,

    @Deprecated(message = "Для получения корректного значения необходимо использовать метод getSingleSmallImage()")
    val postSmallImage: String? = null,

    val postSmallUrl: String? = null,

    @Deprecated(message = "Для получения корректного значения необходимо использовать метод getImageUrl()")
    val postImage: String? = null,

    @Deprecated(message = "Для получения корректного значения необходимо использовать метод getSingleAspect()")
    val aspect: Double = 0.0,

    val isAdultContent: Boolean? = false,

    val parentPost: PostUIEntity? = null,

    @Deprecated(message = "Для получения корректного значения необходимо использовать метод getSingleVideoPreview()")
    val videoPreview: String? = null,

    @Deprecated(message = "Для получения корректного значения необходимо использовать метод getVideoUrl()")
    val video: String? = null,

    @Deprecated(message = "Для получения корректного значения необходимо использовать метод getSingleVideoDuration()")
    val videoDuration: Int? = null,

    val deleted: Int? = null,

    val media: UiMedia? = null,

    val assets: List<MediaAssetEntity>? = null,
    // подписаны ли мы на пост
    val isPostSubscribed: Boolean = false,

    var feedType: FeedType = FeedType.IMAGE_POST,

    var featureData: FeatureData? = null,

    val reactions: List<ReactionEntity>? = emptyList(),

    val isPrivateGroupPost: Boolean = false,

    val isAllowedToComment: Boolean = true,

    val sourceType: String? = String.empty(),

    val type: PostTypeEnum = PostTypeEnum.IMAGE,

    val commentAvailability: String? = String.empty(),

    val moments: MomentInfoCarouselUiModel? = null,

    val needToShowFollowButton: Boolean = false,

    val momentsBlockAvatar: String? = String.empty(),

    val loadingInfo: LoadingPostVideoInfoUIModel = LoadingPostVideoInfoUIModel(),

    val postUpdatingLoadingInfo: LoadingPostVideoInfoUIModel = LoadingPostVideoInfoUIModel(),

    val event: EventUiModel? = null,

    val isNotExpandedSnippetState: Boolean = false,

    var backgroundUrl: String? = null,

    var backgroundId: Int? = null,

    var fontColor: String? = null,

    var fontSize: Int? = null,

    @Deprecated(message = "Для получения корректного значения необходимо использовать метод getSingleMediaPositioning()")
    var mediaPositioning: MediaPositioning? = null,

    val isNewSubsPost: Boolean = false,

    val isPostHidden: Boolean = false,

    val selectedMediaPosition: Int = 0,

    val volumeState: VolumeState = VolumeState.OFF,

    var openedFromRoad: Boolean = false

    ) : Parcelable {

    fun isParentPostDeleted(): Boolean {
        return parentPost?.deleted == 1
    }

    fun updateModel(payload: UIPostUpdate) = copy(
        repostCount = payload.repostCount ?: repostCount,
        commentCount = payload.commentCount ?: commentCount,
        reactions = (payload as? UIPostUpdate.UpdateReaction)?.reactionUpdate?.reactionList ?:
        (payload as? UIPostUpdate.MeeraUpdateReaction)?.reactionUpdate?.reactionList ?:
        reactions,
        moments = (payload as? UIPostUpdate.UpdateMoments)?.moments ?: moments,
        loadingInfo = (payload as? UIPostUpdate.UpdateLoadingState)?.loadingInfo ?: loadingInfo,
        postUpdatingLoadingInfo = (payload as? UIPostUpdate.UpdateUpdatingState)?.loadingInfo ?: postUpdatingLoadingInfo,
        event = (payload as? UIPostUpdate.UpdateEventPostParticipationState)?.postUIEntity?.event ?: event,
        user = user?.copy(
            moments = user.moments?.copy(
                hasMoments = (payload as? UIPostUpdate.UpdateUserMomentsState)?.hasMoments ?: user.moments.hasMoments,
                hasNewMoments = (payload as? UIPostUpdate.UpdateUserMomentsState)?.hasNewMoments ?: user.moments.hasNewMoments
            )
        ),
        selectedMediaPosition = (payload as? UIPostUpdate.UpdateSelectedMediaPosition)?.selectedMediaPosition ?: selectedMediaPosition,
        volumeState = (payload as? UIPostUpdate.UpdateVolumeState)?.volumeState ?: volumeState,
        tagSpan = (payload as? UIPostUpdate.UpdateTagSpan)?.post?.tagSpan ?: tagSpan,
    )

    fun updateModel(payload: UserMomentsStateUpdateModel) = copy(
        user = user?.copy(
            moments = user.moments?.copy(
                hasMoments = payload.hasMoments,
                hasNewMoments = payload.hasNewMoments
            )
        )
    )

    fun hasAssets() = this.assets != null

    fun isNeedToShowExpandView(): Boolean {
        return this.getSingleAspect() <= MAX_ASPECT_FOR_LONG_PICTURE
            || this.getSingleAspect() >= MAX_ASPECT_FOR_WIDE_PICTURE
    }

    fun containsMedia(): Boolean {
        return this.getImageUrl().isNullOrEmpty().not()
            || this.getVideoUrl() != null
            || this.media?.trackId.isNullOrEmpty().not()
            || (this.assets?.isNotEmpty().isTrue())
    }

    fun containsAssets(): Boolean {
        return this.getImageUrl().isNullOrEmpty().not()
            || this.getVideoUrl() != null
    }

    /**
     * If [PostUIEntity.video] is not null it means we have a GIF or a VIDEO.
     * By checking [PostUIEntity.sourceType] we can understand which exactly content we have.
     */
    fun hasPostVideo(): Boolean {
        return !getVideoUrl().isNullOrBlank() && sourceType != PostSourceType.GIF.key
    }

    /**
     * The logic is the same as [PostUIEntity.hasPostVideo] but GIF type check is used.
     */
    fun hasPostGif(): Boolean {
        return !getVideoUrl().isNullOrBlank() && sourceType == PostSourceType.GIF.key
    }

    /**
     * Detect user's post by [AppSettings].
     */
    fun isMyPost(ownUserId: Long?): Boolean {
        return user?.userId == ownUserId
    }

    /**
     * Detect subscription to post's user.
     */
    fun isSubscribedToPostUser(): Boolean {
        return user?.subscriptionOn?.toBoolean() == true
    }

    fun isCommunityPost(): Boolean = groupName != null

    fun isVipPost(): Boolean = false||false //TODO ROAD_FIX

    fun isAdultContent(): Boolean = isAdultContent == true
        && (!videoPreview().isNullOrEmpty() || !getImageUrl().isNullOrEmpty())

    private fun videoPreview() = if (getSingleVideoPreview().isNullOrEmpty()) getVideoUrl() else getSingleVideoPreview()

    fun isEmptyPost(): Boolean = postId == 0L

    fun isWhiteFont(): Boolean = fontColor == "FFFFFF"
    fun isTextWithBackgroundPost(): Boolean = backgroundUrl.isNullOrEmpty().not()

    fun isMeBlocked() = user?.blackListedMe == true

    fun hasComments() = commentCount > 0

    fun hasImage() = !getImageUrl().isNullOrEmpty()

    fun propertyContentType(): AmplitudePropertyContentType {
        val postType = if (parentPost != null) {
            AmplitudePropertyPostType.REPOST
        } else {
            AmplitudePropertyPostType.POST
        }

        return if (postType == AmplitudePropertyPostType.POST) {
            var contentCount = 0
            val hasText = postText.isNotEmpty()
            val hasImage = !getImageUrl().isNullOrEmpty()
            val hasVideo = !getVideoUrl().isNullOrEmpty()
            val hasMusic = media != null
            if (hasText) contentCount++
            if (hasImage) contentCount++
            if (hasVideo) contentCount++
            if (hasMusic) contentCount++
            if (contentCount > 1) AmplitudePropertyContentType.MULTIPLE
            else AmplitudePropertyContentType.SINGLE
        } else {
            AmplitudePropertyContentType.NONE
        }
    }

    fun isEvent(): Boolean = event != null

    fun getUserId(): Long? = user?.userId

    fun isUserHasMoments(): Boolean = user?.moments?.hasMoments == true

    fun isEditable() = type != PostTypeEnum.AVATAR_VISIBLE
        && type != PostTypeEnum.AVATAR_HIDDEN
        && parentPost == null

    fun isPostVideoAvailable(): Boolean {
        val isPostDeleted = deleted?.toBoolean() ?: false
        val hasVideo = !getVideoUrl().isNullOrBlank()

        return !isPostHidden && !isPostDeleted && hasVideo
    }

    fun isMultimediaPostUnavailable(): Boolean {
        val isPostDeleted = deleted?.toBoolean() ?: false
        val hasAssets = !assets.isNullOrEmpty()

        return !isPostDeleted && !isPostHidden && hasAssets
    }

    fun getImageUrl(): String? {
        return if (assets.isNullOrEmpty()) {
           postImage
        } else {
            assets[0].image
        }
    }

    fun getVideoUrl(): String? {
        return if (assets.isNullOrEmpty()) {
            video
        } else {
            assets[0].video
        }
    }

    fun getSingleMediaPositioning(): MediaPositioning? {
        return if (assets.isNullOrEmpty()) {
            mediaPositioning
        } else {
            assets[0].mediaPositioning
        }
    }

    fun getSingleAspect(): Double {
        return if (assets.isNullOrEmpty()) {
            aspect
        } else {
            assets[0].aspect.toDouble()
        }
    }

    fun getSingleVideoDuration(): Int? {
        return if (assets.isNullOrEmpty()) {
            videoDuration
        } else {
            assets[0].duration
        }
    }

    fun getSingleVideoPreview(): String? {
        return if (assets.isNullOrEmpty()) {
            videoPreview
        } else {
            assets[0].videoPreview
        }
    }

    fun getSingleSmallImage(): String? {
        return if (assets.isNullOrEmpty()) {
            postSmallImage
        } else {
            assets[0].smallImage
        }
    }

    fun getMediaPosition(): Int {
        val mediaCount = assets?.size ?: 0
        return if (selectedMediaPosition > mediaCount - 1) {
            0
        } else {
            selectedMediaPosition
        }
    }

    fun getSingleAsset(): MediaAssetEntity? {
        return this.assets?.firstOrNull()
    }

    fun getSingleAssetUrl(): String? {
        val assets = this.assets
        if (assets.isNullOrEmpty()) return null
        val asset = assets[0]
        return when {
            !asset.image.isNullOrEmpty() -> asset.image
            !asset.video.isNullOrEmpty() -> asset.video
            !asset.videoPreview.isNullOrEmpty() -> asset.videoPreview
            else -> null
        }
    }

    fun getAvailableAsset(): MediaAssetEntity? {
        val assets = this.assets
        if (!assets.isNullOrEmpty()) {
            return assets.first()
        }
        return mapSingleMediaPostAssets().firstOrNull()
    }

    fun mapSingleMediaPostAssets(): List<MediaAssetEntity> {
        val assetsList = arrayListOf<MediaAssetEntity>()

        when (this.type) {
            PostTypeEnum.IMAGE -> {
                if (postImage != null) {
                    assetsList.add(
                        MediaAssetEntity.makeAsset(
                            type = MEDIA_IMAGE,
                            image = postImage,
                            smallUrl = postSmallUrl,
                            smallImage = postSmallImage,
                            aspect = aspect.toFloat(),
                            mediaPositioning = mediaPositioning
                        )
                    )
                }
            }

            PostTypeEnum.VIDEO -> {
                if (video != null) {
                    assetsList.add(
                        MediaAssetEntity.makeAsset(
                            type = MEDIA_VIDEO,
                            video = video,
                            smallUrl = postSmallUrl,
                            videoPreview = videoPreview,
                            aspect = aspect.toFloat(),
                            mediaPositioning = mediaPositioning
                        )
                    )
                }
            }

            PostTypeEnum.GIF -> {
                if (video != null) {
                    assetsList.add(
                        MediaAssetEntity.makeAsset(
                            type = MEDIA_VIDEO,
                            gifPreview = videoPreview,
                            video = video,
                            smallUrl = postSmallUrl,
                            aspect = aspect.toFloat(),
                            videoPreview = videoPreview,
                            mediaPositioning = mediaPositioning
                        )
                    )
                }
            }

            else -> Unit
        }

        return assetsList
    }
}

@Parcelize
data class CityPost(
    val id: Long?,

    val name: String?
) : Parcelable

@Parcelize
data class CountryPost(
    val id: Long?,

    val name: String?
) : Parcelable

@Parcelize
data class UserPost(
    var userId: Long = -1,

    var name: String? = String.empty(),

    var birthday: Long? = null, // если запретили показ возраста то null

    var avatarSmall: String? = String.empty(),

    var gender: Int? = -1,

    var accountType: AccountTypeEnum = AccountTypeEnum.ACCOUNT_TYPE_REGULAR,

    var accountColor: Int? = 0,

    var city: CityPost? = null,

    var country: CountryPost? = null,

    // статус подписки на пользователя
    var subscriptionOn: Int? = 0,

    //скрыли ли мы дорогу юзера
    var hideRoadPosts: Int? = 0,

    var approved: Int = 0,

    val topContentMaker: Int = 0,

    val isSystemAdministrator: Boolean = false,

    val blackListedMe: Boolean = false,

    val blackListedByMe: Boolean = false,

    val subscribedToMe: Int? = 0,

    val friendStatus: Int? = 0,

    val moments: UserMomentsSimpleModel? = null

) : Parcelable

@Parcelize
data class UiMedia(
    val album: String? = "",
    val albumUrl: String? = "",
    val artist: String? = "",
    val artistUrl: String? = "",
    val recognized: Boolean? = false,
    val track: String? = "",
    val trackId: String? = "",
    val trackPreviewUrl: String = "",
    val trackUrl: String? = "",
) : Parcelable

@Parcelize
data class FeatureData(
    val id: Long,
    val text: String? = "",
    val button: String? = "",
    val deepLink: String? = "",
    val hideable: Boolean = false,
    val tagSpan: ParsedUniquename? = null,
    val aspect: Double? = null,
    val videoDuration: Int? = null,
    val video: String? = null,
    val videoPreview: String? = null,
    val image: String? = null,
    val smallImage: String? = null,
    val positions: List<Int>,
    val isClosable: Boolean = false,
    val dismissButton: String? = "",
    val referralInfo: ReferralDataUIModel? = null,
    var suggestions: List<ProfileSuggestionUiModels>? = null
) : Parcelable

@Parcelize
data class MediaPositioning(
    val x: Double = 0.0,
    val y: Double = 0.0
) : Parcelable

@Parcelize
data class MediaAssetEntity(
    var id: String?,
    var type: String?,
    var aspect: Float,
    var base64Preview: String?,
    var image: String?,
    var smallImage: String?,
    var smallUrl: String?,
    var mediaPositioning: MediaPositioning?,
    var video: String?,
    var videoPreview: String?,
    var duration: Int?,
    var gifPreview: String?,
    var isAvailable: Boolean = true
) : Parcelable {
    fun isMediaOverflowsBy25Percent(frameWidth: Int, frameHeight: Int): Boolean {
        val a = frameWidth / this.aspect
        val b = this.aspect * frameHeight

        val isOverflows = max((abs(a - frameHeight) / frameHeight), (abs(b - frameWidth) / frameWidth)) > 0.25

        return isOverflows
    }

    companion object {
        fun makeAsset(
            id: String? = null,
            type: String? = null,
            aspect: Float,
            base64Preview: String? = null,
            image: String? = null,
            smallImage: String? = null,
            smallUrl: String? = null,
            mediaPositioning: MediaPositioning? = null,
            video: String? = null,
            videoPreview: String? = null,
            duration: Int? = null,
            gifPreview: String? = null,
            isAvailable: Boolean = true
        ) =
            MediaAssetEntity(
                id = id,
                aspect = aspect,
                type = type,
                base64Preview = base64Preview,
                image = image,
                smallImage = smallImage,
                smallUrl = smallUrl,
                mediaPositioning = mediaPositioning,
                video = video,
                videoPreview = videoPreview,
                duration = duration,
                gifPreview = gifPreview,
                isAvailable = isAvailable
            )
    }
}

fun MediaPositioningDto?.toMediaPositioning(): MediaPositioning {
    return MediaPositioning(
        x = this?.x ?: 0.0,
        y = this?.y ?: 0.0
    )
}
