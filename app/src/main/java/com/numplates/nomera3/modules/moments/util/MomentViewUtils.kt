package com.numplates.nomera3.modules.moments.util

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.meera.core.extensions.getScreenWidth
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.meera.uikit.widgets.dp
import com.numplates.nomera3.modules.feed.ui.adapter.ContentActionBar
import com.numplates.nomera3.modules.feed.ui.adapter.MeeraContentActionBar
import com.numplates.nomera3.modules.moments.show.presentation.view.ActionBarPositionType

private const val SMALL_SCREEN = 320

private const val BOTTOM_MARGIN = 16

fun isSmallScreen() = getScreenWidth() <= SMALL_SCREEN

fun getMomentActionBarDefaultParams(): ContentActionBar.Params = ContentActionBar.Params(
    isEnabled = false,
    reactions = emptyList(),
    userAccountType = null,
    commentCount = 0,
    repostCount = 0,
    isMoment = true,
    commentsIsHide = true
)

fun getMeeraMomentActionBarDefaultParams(): MeeraContentActionBar.Params = MeeraContentActionBar.Params(
    isEnabled = false,
    reactions = emptyList(),
    userAccountType = null,
    commentCount = 0,
    repostCount = 0,
    isMoment = true,
    commentsIsHide = true
)

fun View.setActionBarPosition(
    positionType: ActionBarPositionType,
    gradient: View?,
    actionBar: ContentActionBar?
) {
    val viewLayoutParams = layoutParams as? ConstraintLayout.LayoutParams
    val actionBarParams = actionBar?.layoutParams as? ConstraintLayout.LayoutParams
    val gradientParams = gradient?.layoutParams as? ConstraintLayout.LayoutParams
    when (positionType) {
        ActionBarPositionType.UNDER_CONTENT -> {
            actionBarParams?.topToBottom = id
            actionBarParams?.bottomToBottom = ConstraintLayout.LayoutParams.UNSET
            viewLayoutParams?.height = ConstraintLayout.LayoutParams.WRAP_CONTENT
            gradient?.gone()
        }

        ActionBarPositionType.ON_CONTENT -> {
            actionBarParams?.topToBottom = ConstraintLayout.LayoutParams.UNSET
            actionBarParams?.bottomToBottom = id
            gradientParams?.bottomToBottom = id
            viewLayoutParams?.height = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
            gradient?.visible()
        }

        else -> {
            actionBarParams?.topToBottom = ConstraintLayout.LayoutParams.UNSET
            actionBarParams?.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            actionBarParams?.bottomMargin = BOTTOM_MARGIN.dp
            viewLayoutParams?.height = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
            gradient?.visible()
        }
    }
    layoutParams = viewLayoutParams
    actionBar?.requestLayout()
}

fun View.setActionBarPositionMeera(
    positionType: ActionBarPositionType,
    gradient: View?,
    actionBar: MeeraContentActionBar?
) {
    val viewLayoutParams = layoutParams as? ConstraintLayout.LayoutParams
    val actionBarParams = actionBar?.layoutParams as? ConstraintLayout.LayoutParams
    val gradientParams = gradient?.layoutParams as? ConstraintLayout.LayoutParams
    when (positionType) {
        ActionBarPositionType.UNDER_CONTENT -> {
            actionBarParams?.topToBottom = id
            actionBarParams?.bottomToBottom = ConstraintLayout.LayoutParams.UNSET
            viewLayoutParams?.height = ConstraintLayout.LayoutParams.WRAP_CONTENT
            gradient?.gone()
        }

        ActionBarPositionType.ON_CONTENT -> {
            actionBarParams?.topToBottom = ConstraintLayout.LayoutParams.UNSET
            actionBarParams?.bottomToBottom = id
            gradientParams?.bottomToBottom = id
            viewLayoutParams?.height = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
            gradient?.visible()
        }

        else -> {
            actionBarParams?.topToBottom = ConstraintLayout.LayoutParams.UNSET
            actionBarParams?.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
            actionBarParams?.bottomMargin = BOTTOM_MARGIN.dp
            viewLayoutParams?.height = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
            gradient?.visible()
        }
    }
    layoutParams = viewLayoutParams
    actionBar?.requestLayout()
}
