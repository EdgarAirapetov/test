package com.numplates.nomera3.modules.moments.show.presentation.dialog

import android.content.Context
import androidx.fragment.app.FragmentManager
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.moments.show.domain.CommentsAvailabilityType
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraMenuBottomSheet

private const val ALLOW_COMMENTS_TAG = "view_moment_author_dialog_allow_comments"

class ViewMomentAllowCommentsDialog(
    activityContext: Context?
) : MeeraMenuBottomSheet(activityContext) {

    var onSelectAllowType: (CommentsAvailabilityType) -> Unit = {}
    var onBackClick: () -> Unit = {}

    fun createAllowCommentsDialog(commentsAvailabilityType: CommentsAvailabilityType) {
        addTitleWithBack(title = R.string.moment_author_menu_allow_comments, click = { onBackClick.invoke() })
        addRadioItem(
            title = R.string.moment_author_menu_allow_comments_all,
            tag = CommentsAvailabilityType.ALL.name,
            isSelected = commentsAvailabilityType == CommentsAvailabilityType.ALL,
            dismissAfterClick = false
        ) { onSelectAllowType.invoke(CommentsAvailabilityType.ALL) }
        addRadioItem(
            title = R.string.moment_author_menu_allow_comments_friends,
            tag = CommentsAvailabilityType.FRIENDS.name,
            isSelected = commentsAvailabilityType == CommentsAvailabilityType.FRIENDS,
            dismissAfterClick = false
        ) { onSelectAllowType.invoke(CommentsAvailabilityType.FRIENDS) }
        addRadioItem(
            title = R.string.moment_author_menu_allow_comments_nobody,
            tag = CommentsAvailabilityType.NOBODY.name,
            isSelected = commentsAvailabilityType == CommentsAvailabilityType.NOBODY,
            dismissAfterClick = false
        ) { onSelectAllowType.invoke(CommentsAvailabilityType.NOBODY) }
    }

    fun setCommentsAvailabilityType(commentsAvailabilityType: CommentsAvailabilityType) {
        setRadioButtonChecked(commentsAvailabilityType.name)
    }

    companion object {

        fun ViewMomentAllowCommentsDialog.showDialog(fragmentManager: FragmentManager) {
            showWithTag(fragmentManager, ALLOW_COMMENTS_TAG)
        }
    }
}
