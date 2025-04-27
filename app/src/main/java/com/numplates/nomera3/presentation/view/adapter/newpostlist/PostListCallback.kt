package com.numplates.nomera3.presentation.view.adapter.newpostlist

import android.graphics.Point
import android.view.View
import android.widget.TextView
import com.numplates.nomera3.modules.feed.ui.adapter.ContentActionBar

/*
* Callback for post used in:
* - PagePostListAdapterNew (adapter for main road screen only (PostListFragmentNew))
* - PostListAdapter (adapter for user's post screen and screen of group feed)
* - PostFragmentV2 (detailed post view)
*
* Used as supertype in
* - PostListFragmentNew (main road screen)
* - UserPostListFragment (user's post screen, can be opened from profile)
* - GroupRoadFragmentNew (group feed)
* */

interface PostListCallback {

    fun onPostItemClick(
        adapterPosition: Int,
        isRepostContainerClicked: Boolean = false,
        isRepostAllowed: Boolean = true
    ) {
    }

    fun onPostItemPictureClick(adapterPosition: Int) {}

    fun onPostItemPictureLongClick(adapterPosition: Int) {}

    fun onPostItemDotsMenuClick(adapterPosition: Int) {}

    fun onPostItemProfileClick(adapterPosition: Int) {}

    fun onPostItemUniqnameUserClick(userId: Long?) {}

    fun onPostHashtagClick(hashtag: String?) {}

    fun openAdsLink(link: String) {}

    fun onRateClicked(rating: Float, comment: String) {}

    fun onMarketClicked(itemPosition: Int) {}

    fun onCancelClicked(itemPosition: Int) {}

    fun onUnsubscribe(position: Int?) {}

    fun onSubscribe(position: Int?) {}

    fun onRepostClicked(position: Int?) {}

    fun onParentPostAuthorNameClicked(position: Int?) {}

    fun onShowMoreRepostClicked(position: Int?) {}

    fun onAddPostClicked() {}

    fun onAddImagePostClicked() {}

    fun onOpenPostClicked(position: Int) {}

    fun onCommunityViewClicked(position: Int, groupId: Long?) {}

    fun onPressMoreText(postId: Long, isOpenPostDetail: Boolean) {}

    fun actionOnFeatureAnnouncement(featureId: Long, actionDismiss: Boolean, deepLink: String?) {}

    fun onHolidayWordClicked() {}

    fun onReactionBottomSheetShow(postId: Long) {}

    fun onReactionRegularClicked(postId: Long, reactionHolderViewId: ContentActionBar.ReactionHolderViewId) {}

    fun onReactionLongClicked(
        postId: Long,
        showPoint: Point,
        reactionTip: TextView,
        viewsToHide: List<View>,
        reactionHolderViewId: ContentActionBar.ReactionHolderViewId
    ) {
    }

    fun onMessage(message: String) {}
}
