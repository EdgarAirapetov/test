package com.numplates.nomera3.modules.feed.ui

import androidx.annotation.StringRes
import com.numplates.nomera3.modules.feed.ui.entity.MediaAssetEntity
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.feed.ui.viewmodel.RoadTypesEnum
import com.numplates.nomera3.modules.reactionStatistics.ui.ReactionsEntityType
import com.numplates.nomera3.modules.uploadpost.ui.entity.NotAvailableReasonUiEntity

sealed class FeedViewEvent {

    object FailChangeLikeStatus : FeedViewEvent()
    object LikeChangeVibration : FeedViewEvent()

    class TotalPostCount(
        var postCount: Long
    ) : FeedViewEvent()

    class UpdatePostById(
        val postId: Long
    ) : FeedViewEvent()

    class OnSuccessDeletePost(val postId: Long) : FeedViewEvent()
    class OnSuccessHideUserRoad(
        val userId: Long,
        @StringRes val messageResId: Int
    ) : FeedViewEvent()

    class ShowCommonError(@StringRes val messageResId: Int) : FeedViewEvent()
    class ShowErrorAndHideProgress(@StringRes val messageResId: Int) : FeedViewEvent()
    class ShowCommonErrorString(val message: String) : FeedViewEvent()
    class ShowCommonSuccess(@StringRes val messageResId: Int) : FeedViewEvent()

    class OnFirstPageLoaded(
        val posts: List<PostUIEntity>,
        val features: List<PostUIEntity> = emptyList()
    ) : FeedViewEvent()

    data class OnMomentsFirstLoaded(val roadTypesEnum: RoadTypesEnum?) : FeedViewEvent()

    object ScrollMomentsToStart : FeedViewEvent()
    class OnSuccessHideFeature(val featureId: Long) : FeedViewEvent()
    class OpenDeepLink(val deepLink: String) : FeedViewEvent()

    class OnShowLoader(val show: Boolean) : FeedViewEvent()

    class ClearPostByUserId(val userId: Long, val needToShowSuccessMessage: Boolean) : FeedViewEvent()

    class PlaceHolderEvent(val hasPosts: Boolean) : FeedViewEvent()

    object EmptyFeed : FeedViewEvent()

    class CopyLinkEvent(val link: String) : FeedViewEvent()

    class UpdateEventPost(val post: PostUIEntity) : FeedViewEvent()

    data class ShowEventSharingSuggestion(val post: PostUIEntity) : FeedViewEvent()

    object RequestContactsPermission : FeedViewEvent()

    object ShowSyncDialogPermissionDenied : FeedViewEvent()

    object ShowContactsHasBeenSyncDialog : FeedViewEvent()

    data class ShowGetVipDialog(val vipUntilDate: Long, @StringRes val descriptionTextId: Int) : FeedViewEvent()

    data class OnSuccessGetVip(val vipUntilDate: Long) : FeedViewEvent()

    object OnFailGetVip : FeedViewEvent()

    class PostEditAvailableEvent(
        val post: PostUIEntity,
        val isAvailable: Boolean,
        val currentMedia: MediaAssetEntity?
    ) : FeedViewEvent()

    class OpenEditPostEvent(val post: PostUIEntity) : FeedViewEvent()

    class ShowAvailabilityError(val reason: NotAvailableReasonUiEntity) : FeedViewEvent()

    data class ShowReactionStatisticsEvent(
        val post: PostUIEntity,
        val entityType: ReactionsEntityType
    ) : FeedViewEvent()

    object LoadInitialPosts: FeedViewEvent()
}
