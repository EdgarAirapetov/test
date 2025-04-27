package com.numplates.nomera3.modules.moments.show.presentation.dialog

import android.content.Context
import android.content.DialogInterface
import androidx.fragment.app.FragmentManager
import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.view.ui.BottomSheetDialogEventsListener
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraMenuBottomSheet

private const val AUTHOR_DIALOG_TAG = "view_moment_author_dialog"

class ViewMomentAuthorDialog(
    val activityContext: Context?
) : MeeraMenuBottomSheet(activityContext),
    BottomSheetDialogEventsListener {

    var onPressDownloadContent: () -> Unit = {}
    var onPressDeleteMoment: () -> Unit = {}
    var onPressMomentSettings: () -> Unit = {}
    var onPressCopyMomentSettings: () -> Unit = {}
    var onPressShareMomentSettings: () -> Unit = {}

    private var onDismissListener: (() -> Unit)? = null

        //TODO Закомментил пункт меню о настройках комментариев по задаче https://nomera.atlassian.net/browse/BR-27412,
        // так как его в ближайшее время собираются вернуть
    fun createAuthorDialog(onShowAllowCommentsDialog: () -> Unit = {}) {
        addItem(
            title = R.string.general_share,
            icon = R.drawable.ic_share_purple_new,
        ) { onPressShareMomentSettings.invoke() }
        addItem(
            title = R.string.copy_link,
            icon = R.drawable.ic_chat_copy_message,
        ) { onPressCopyMomentSettings.invoke() }
//        addItem(
//            title = R.string.moment_author_menu_settings,
//            icon = R.drawable.ic_moment_settings,
//        ) { onPressMomentSettings.invoke() }
        addItem(
            title = R.string.moment_author_menu_download,
            icon = R.drawable.ic_moment_download
        ) { onPressDownloadContent.invoke() }
        addItemIsArrow(
            title = R.string.moment_author_menu_allow_comments,
            icon = R.drawable.ic_moment_allow_comments
        ) { onShowAllowCommentsDialog.invoke() }
        addItem(
            title = R.string.moment_author_menu_delete,
            icon = R.drawable.ic_moment_delete,
        ) { onPressDeleteMoment.invoke() }
    }

    fun setOnDismissListener(listener: () -> Unit) {
        onDismissListener = listener
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.invoke()
    }

    companion object {

        fun ViewMomentAuthorDialog.showDialog(fragmentManager: FragmentManager) {
            showWithTag(fragmentManager, AUTHOR_DIALOG_TAG)
        }
    }
}
