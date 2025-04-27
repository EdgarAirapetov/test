package com.numplates.nomera3.modules.moments.show.presentation.dialog

import android.content.Context
import android.content.DialogInterface
import androidx.fragment.app.FragmentManager
import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.view.ui.BottomSheetDialogEventsListener
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraMenuBottomSheet
import timber.log.Timber

const val WATCHER_DIALOG_TAG = "view_moment_watcher_dialog"

class ViewMomentWatcherDialog(
    val activityContext: Context?
) : MeeraMenuBottomSheet(activityContext),
    BottomSheetDialogEventsListener {

    var hideUserMoments: (() -> Unit)? = null
    var showUserMoments: (() -> Unit)? = null
    var complainOnMoment: (() -> Unit)? = null
    var onPressCopyMomentSettings: () -> Unit = {}
    var onPressShareMomentSettings: () -> Unit = {}

    private var onDismissListener: (() -> Unit)? = null

    fun createWatcherDialog(allowHideMoments: Boolean) {
        Timber.d("DiffMomentAdapter | creating dialog, allowHideMoments=$allowHideMoments")
        addItem(
            title = R.string.general_share,
            icon = R.drawable.ic_share_purple_new,
        ) { onPressShareMomentSettings.invoke() }
        addItem(
            title = R.string.copy_link,
            icon = R.drawable.ic_chat_copy_message,
        ) { onPressCopyMomentSettings.invoke() }
        if (allowHideMoments) {
            addItem(
                R.string.moments_bottom_menu_hide_user,
                R.drawable.ic_eye_off_all_menu_item_red
            ) { hideUserMoments?.invoke() }
        } else {
            addItem(
                R.string.moments_bottom_menu_show_user,
                R.drawable.ic_eye_purple
            ) { showUserMoments?.invoke() }
        }

        addItem(
            R.string.complain_about_moment,
            R.drawable.ic_report_round
        ) { complainOnMoment?.invoke() }
    }

    fun setOnDismissListener(listener: () -> Unit) {
        onDismissListener = listener
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissListener?.invoke()
    }

    companion object {

        fun ViewMomentWatcherDialog.showDialog(fragmentManager: FragmentManager) {
            showWithTag(fragmentManager, WATCHER_DIALOG_TAG)
        }
    }
}
