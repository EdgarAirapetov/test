package com.numplates.nomera3.modules.feedviewcontent.presentation.fragment

import android.graphics.Point
import android.view.View
import android.widget.TextView
import com.numplates.nomera3.modules.feed.ui.adapter.ContentActionBar
import com.numplates.nomera3.modules.reaction.ui.custom.FlyingReaction

interface ViewContentReactionsListener {
    fun onReactionBottomSheetShow()

    fun onReactionLongClicked(
        showPoint: Point,
        reactionTip: TextView,
        viewsToHide: List<View>,
        reactionHolderViewId: ContentActionBar.ReactionHolderViewId
    )

    fun onReactionRegularClicked(reactionHolderViewId: ContentActionBar.ReactionHolderViewId)

    fun onFlyingAnimationInitialized(flyingReaction: FlyingReaction)
}
