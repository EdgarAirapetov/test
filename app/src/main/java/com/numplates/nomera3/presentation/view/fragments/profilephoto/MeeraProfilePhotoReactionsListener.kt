package com.numplates.nomera3.presentation.view.fragments.profilephoto

import android.graphics.Point
import android.view.View
import android.widget.TextView
import com.numplates.nomera3.modules.feed.ui.adapter.MeeraContentActionBar
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity
import com.numplates.nomera3.modules.reaction.ui.custom.FlyingReaction

interface MeeraProfilePhotoReactionsListener {
    fun onReactionBottomSheetShow(post: PostUIEntity) = Unit

    fun onReactionLongClicked(
        post: PostUIEntity,
        showPoint: Point,
        reactionTip: TextView,
        viewsToHide: List<View>,
        reactionHolderViewId: MeeraContentActionBar.ReactionHolderViewId
    ) = Unit

    fun onReactionRegularClicked(post: PostUIEntity, reactionHolderViewId: MeeraContentActionBar.ReactionHolderViewId) = Unit

    fun onFlyingAnimationInitialized(flyingReaction: FlyingReaction)

    fun onReactionClickToShowScreenAnimation(
        reactionEntity: ReactionEntity,
        anchorViewLocation: Pair<Int, Int>
    ) = Unit
}
