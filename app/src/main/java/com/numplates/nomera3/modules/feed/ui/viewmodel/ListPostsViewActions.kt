package com.numplates.nomera3.modules.feed.ui.viewmodel

import com.numplates.nomera3.R
import com.numplates.nomera3.modules.feed.data.entity.PostMediaViewInfo
import com.numplates.nomera3.modules.feed.ui.entity.MediaAssetEntity
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.rateus.data.RateUsAnalyticsRating
import com.numplates.nomera3.modules.volume.domain.model.VolumeState

sealed class FeedViewActions {

    object RetryLastPostsRequest : FeedViewActions()

    class GetUserPosts(val startPostId: Long, val userId: Long, val selectedPostId: Long?) : FeedViewActions()

    class GetGroupPosts(val startPostId: Long, val groupId: Int) : FeedViewActions()

    class GetHashtagPosts(val startPostId: Long, val hashtag: String?) : FeedViewActions()

    class GetAllPosts(val startPostId: Long) : FeedViewActions()

    class GetSubscriptionPosts(val startPostId: Long) : FeedViewActions()

    class SubscribeToPost(val postId: Long?, val titles: PostSubscribeTitle) : FeedViewActions()

    class UnsubscribeFromPost(val postId: Long?, val titles: PostSubscribeTitle) : FeedViewActions()

    object ResetAppInfoCache : FeedViewActions()

    class SubscribeToUser(
        val postId: Long?,
        val userId: Long?,
        val needToHideFollowButton: Boolean,
        val fromFollowButton: Boolean,
        val isApproved: Boolean,
        val topContentMaker: Boolean
    ) : FeedViewActions()

    class UnsubscribeFromUser(
        val postId: Long?,
        val userId: Long?,
        val fromFollowButton: Boolean,
        val isApproved: Boolean,
        val topContentMaker: Boolean
    ) : FeedViewActions()

    class HideUserRoads(val userId: Long?) : FeedViewActions()

    class DeletePost(val post: PostUIEntity, val adapterPosition: Int) : FeedViewActions()

    class ComplainToPost(val postId: Long?) : FeedViewActions()

    class ComplainToUser(val userId: Long?) : FeedViewActions()

    data class FeatureClick(
        val featureId: Long,
        val dismiss: Boolean,
        val deepLink: String?
    ) : FeedViewActions()

    class UnsubscribeFromUserAndClear(val postId: Long?, val userId: Long) : FeedViewActions()

    class OnShowMoreText(val post: PostUIEntity) : FeedViewActions()

    class UpdateMainRoad(val startPostId: Long) : FeedViewActions()

    class CopyPostLink(val postId: Long?) : FeedViewActions()

    class RateUs(val rating: Int, val comment: String) : FeedViewActions()
    class RateUsAnalytic(val rateUsAnalyticsRating: RateUsAnalyticsRating) : FeedViewActions()

    object HideRateUsPost : FeedViewActions()

    class CheckUpdateAvailability(val post: PostUIEntity, val currentMedia: MediaAssetEntity?) : FeedViewActions()

    class EditPost(val post: PostUIEntity) : FeedViewActions()

    class SaveLastPostMediaViewInfo(val lastPostMediaViewInfo: PostMediaViewInfo?) : FeedViewActions()

    class UpdatePostSelectedMediaPosition(val postId: Long, val selectedMediaPosition: Int) : FeedViewActions()

    class UpdateVolumeState(val volumeState: VolumeState) : FeedViewActions()

    object CheckIfInitialOpen: FeedViewActions()
}

sealed interface PostSubscribeTitle {

    class NotificationString(
        val subscribeString: Int? = R.string.turn_on_notifications,
        val unsubscribeString: Int? = R.string.turn_off_notifications
    ) : PostSubscribeTitle

    class SubscribeString(
        val subscribeString: Int? = R.string.subscribe_post,
        val unsubscribeString: Int? = R.string.unsubscribe_post
    ) : PostSubscribeTitle
}


