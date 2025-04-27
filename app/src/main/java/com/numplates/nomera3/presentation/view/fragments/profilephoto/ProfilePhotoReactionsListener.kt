package com.numplates.nomera3.presentation.view.fragments.profilephoto

import android.graphics.Point
import android.view.View
import android.widget.TextView
import com.numplates.nomera3.modules.feed.ui.adapter.ContentActionBar
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.reaction.ui.custom.FlyingReaction

interface ProfilePhotoReactionsListener {
    fun onReactionBottomSheetShow(post: PostUIEntity) = Unit

    fun onReactionLongClicked(
        post: PostUIEntity,
        showPoint: Point,
        reactionTip: TextView,
        viewsToHide: List<View>,
        reactionHolderViewId: ContentActionBar.ReactionHolderViewId
    ) = Unit

    fun onReactionRegularClicked(post: PostUIEntity, reactionHolderViewId: ContentActionBar.ReactionHolderViewId) = Unit

    fun onFlyingAnimationInitialized(flyingReaction: FlyingReaction)
}
