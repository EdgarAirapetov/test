package com.numplates.nomera3.modules.feedviewcontent.presentation.dialog

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentManager
import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.view.ui.bottomMenu.MeeraMenuBottomSheet

const val WATCHER_DIALOG_TAG = "view_content_watcher_dialog"

class ViewContentWatcherDialog(
    val activityContext: Context?
) : MeeraMenuBottomSheet(activityContext) {

    private var listener: ContentBottomSheetDialogListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment is ContentBottomSheetDialogListener) {
            listener = parentFragment as? ContentBottomSheetDialogListener
        }
    }

    override fun onDetach() {
        listener = null
        super.onDetach()
    }

    fun createWatcherDialog(isEventPost: Boolean, isPostSubscribed: Boolean) {
        addItem(
            title = R.string.save_to_device,
            icon = R.drawable.ic_download_new
        ) { listener?.onClickDownloadContent() }
        addItem(
            title = getSubscriptionText(isEventPost = isEventPost, isPostSubscribed = isPostSubscribed),
            icon = getSubscriptionIcon(isPostSubscribed)
        ) {
            if (isPostSubscribed) listener?.onClickUnsubscribePost() else listener?.onClickSubscribePost()
        }
        val complainTitleResId = if (isEventPost) R.string.complain_about_event_post else R.string.complain_about_post
        addItem(
            title = complainTitleResId,
            icon = R.drawable.ic_report_profile
        ) { listener?.onClickComplainOnPost() }
    }

    @DrawableRes
    private fun getSubscriptionIcon(isPostSubscribed: Boolean): Int {
        return if (isPostSubscribed) {
            R.drawable.ic_unsubscribe_post_menu_purple
        } else {
            R.drawable.ic_subscribe_post_menu_purple
        }
    }

    @StringRes
    private fun getSubscriptionText(isEventPost: Boolean, isPostSubscribed: Boolean): Int {
        return if (isPostSubscribed) {
            if (isEventPost) R.string.unsubscribe_event_post_txt else R.string.unsubscribe_post_txt
        } else {
            if (isEventPost) R.string.subscribe_event_post_txt else R.string.subscribe_post_txt
        }
    }

    companion object {

        fun ViewContentWatcherDialog.showDialog(fragmentManager: FragmentManager) {
            showWithTag(fragmentManager, WATCHER_DIALOG_TAG)
        }
    }
}

