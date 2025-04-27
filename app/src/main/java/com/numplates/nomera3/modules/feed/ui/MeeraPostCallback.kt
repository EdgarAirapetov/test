package com.numplates.nomera3.modules.feed.ui

import android.graphics.Point
import android.view.View
import android.widget.TextView
import com.meera.referrals.ui.model.ReferralDataUIModel
import com.numplates.nomera3.modules.feed.ui.adapter.MeeraContentActionBar
import com.numplates.nomera3.modules.feed.ui.entity.MediaAssetEntity
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentGroupUiModel
import com.numplates.nomera3.modules.moments.show.presentation.data.MomentItemUiModel
import com.numplates.nomera3.modules.rateus.data.RateUsAnalyticsRating
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity
import com.numplates.nomera3.modules.reaction.ui.custom.FlyingReaction
import com.numplates.nomera3.modules.tags.data.entity.SpanDataClickType
import com.numplates.nomera3.modules.tags.data.entity.TagOrigin

interface MeeraPostCallback {

    fun onFindPeoplesClicked() {}

    fun onShowMoreSuggestionsClicked() {}

    fun onUnsubscribeSuggestedUserClicked(
        userId: Long,
        isApprovedUser: Boolean,
        topContentMaker: Boolean
    ) {}

    fun onSubscribeSuggestedUserClicked(
        userId: Long,
        isApprovedUser: Boolean,
        topContentMaker: Boolean
    ) {}

    fun onAddFriendSuggestedUserClicked(
        userId: Long,
        isApprovedUser: Boolean,
        topContentMaker: Boolean
    ) {}

    fun onRemoveFriendSuggestedUserClicked(userId: Long) {}

    fun onHideSuggestedUserClicked(userId: Long) {}

    fun onSuggestedUserClicked(
        isTopContentMaker: Boolean,
        isApproved: Boolean,
        hasMutualFriends: Boolean,
        isSubscribed: Boolean,
        toUserId: Long
    ) {}

    fun onActivateVipClicked(data: ReferralDataUIModel) {}

    fun onReferralClicked() {}

    fun onSyncContactsClicked() {}

    fun onRepostClicked(post: PostUIEntity) {}

    fun onCommentClicked(post: PostUIEntity, adapterPosition: Int) {}

    fun onAvatarClicked(post: PostUIEntity, adapterPosition: Int) {}

    fun onPostClicked(post: PostUIEntity, adapterPosition: Int) {}

    fun onBackFromEventClicked() = Unit

    fun onReactionLongClicked(
        post: PostUIEntity,
        showPoint: Point,
        reactionTip: TextView,
        viewsToHide: List<View>,
        reactionHolderViewId: MeeraContentActionBar.ReactionHolderViewId
    ) {
    }

    fun onReactionBottomSheetShow(post: PostUIEntity, adapterPosition: Int) {}

    fun onReactionRegularClicked(
        post: PostUIEntity,
        adapterPosition: Int,
        reactionHolderViewId: MeeraContentActionBar.ReactionHolderViewId,
        forceDefault: Boolean = false
    ) {
    }

    fun onReactionClickToShowScreenAnimation(
        reactionEntity: ReactionEntity,
        anchorViewLocation: Pair<Int, Int>
    ) = Unit

    fun onFlyingAnimationInitialized(flyingReaction: FlyingReaction) {}

    fun onPictureClicked(post: PostUIEntity) {}

    fun onVideoClicked(post: PostUIEntity, adapterPosition: Int) {}

    fun onMediaClicked(post: PostUIEntity, mediaAsset: MediaAssetEntity, adapterPosition: Int) {}

    fun onCommunityClicked(communityId: Long, adapterPosition: Int) {}

    fun onDotsMenuClicked(post: PostUIEntity, adapterPosition: Int, currentMedia: MediaAssetEntity?) {}
    fun onPostCloseClicked() {}

    fun onTagClicked(
        clickType: SpanDataClickType,
        adapterPosition: Int,
        tagOrigin: TagOrigin,
        post: PostUIEntity? = null
    ) {}

    fun onRateUsClicked(rating: Int, comment: String, adapterPosition: Int) {}

    fun onRateUsProcessAnalytic(rateUsAnalyticsRating: RateUsAnalyticsRating) {}

    fun onRateUsGoToGoogleMarketClicked() {}

    fun onHideRateUsPostClicked(adapterPosition: Int) {}

    fun onHolidayWordClicked() {}

    fun onCreatePostClicked(withImage: Boolean) {}

    fun onFeatureClicked(
        featureId: Long,
        haveAction: Boolean,
        dismiss: Boolean,
        deepLink: String? = null,
        featureText: String? = null,
    ) {}

    fun onAddMomentClicked() {}

    fun onShowMomentsClicked(startGroupId: Long = -1, view: View? = null, isViewed: Boolean) {}

    fun onShowUserMomentsClicked(
        userId: Long,
        fromView: View? = null,
        hasNewMoments: Boolean?
    ) {}

    fun onMomentClicked(moment: MomentItemUiModel) {}

    fun onMomentProfileClicked(momentGroup: MomentGroupUiModel) {}

    fun onMomentGroupClicked(momentGroup: MomentGroupUiModel, view: View?, isViewed:Boolean) {}

    fun onMomentGroupLongClicked(momentGroup: MomentGroupUiModel) {}

    fun onUpdateMomentsClicked() {}

    fun onShowMoreRepostClicked(post: PostUIEntity, adapterPosition: Int) {}

    fun onShowMoreTextClicked(post: PostUIEntity, adapterPosition: Int, isOpenPostDetail: Boolean = false) {}

    fun onHideMoreTextClicked(post: PostUIEntity, adapterPosition: Int, isOpenPostDetail: Boolean = true) {}

    fun onPressMoreText(postId: Long, isOpenPostDetail: Boolean) {}

    fun onPressRepostHeader(post: PostUIEntity, adapterPosition: Int) {}

    fun onFollowUserClicked(post: PostUIEntity, adapterPosition: Int) {}

    fun onStopLoadingClicked(post: PostUIEntity) {}

    fun onPostSnippetExpandedStateRequested(post: PostUIEntity) {}

    fun onJoinEventClicked(post: PostUIEntity, isRepost: Boolean = false, adapterPosition: Int = -1) {}
    fun onLeaveEventClicked(post: PostUIEntity, isRepost: Boolean = false, adapterPosition: Int = -1) {}
    fun onNavigateToEventClicked(post: PostUIEntity) {}
    fun onShowEventOnMapClicked(post: PostUIEntity, isRepost: Boolean = false, adapterPosition: Int = -1) {}
    fun onShowEventParticipantsClicked(post: PostUIEntity) {}

    fun onJoinAnimationFinished(post: PostUIEntity, adapterPosition: Int) {}

    fun onStartPlayingVideoRequested() {}

    fun forceStartPlayingVideoRequested() {}

    fun onStopPlayingVideoRequested() {}

    fun onMediaExpandCheckRequested() {}

    fun onMultimediaPostSwiped(postId: Long, selectedMediaPosition: Int) {}

    fun onClickRepostEvent(postId: Long) {}

    /* Необходим для кейса сниппетов на карте, когда родитель изменяет свою ширину,
    и для перерисовки айтемов списка требуется всегда использовать актуальную ширину родителя */
    fun getParentWidth(): Int? = null
}
