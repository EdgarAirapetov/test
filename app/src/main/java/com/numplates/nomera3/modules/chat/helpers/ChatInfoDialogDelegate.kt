package com.numplates.nomera3.modules.chat.helpers

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.meera.core.dialogs.MeeraConfirmDialogBuilder
import com.meera.core.extensions.dp
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.visible
import com.meera.core.utils.NSnackbar
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.DialogAddToFavoritesSuccessBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SNACKBAR_BOTTOM_MARGIN_DP = 24
private const val TIME_TO_UNDO_ACTION_SEC = 6
private const val FWD_MESSAGE_SNACKBAR_MARGIN_BOTTOM = 70
private const val MEDIA_COPIED_SNACKBAR_MARGIN_BOTTOM = 70
private const val ADDED_TO_FAVORITES_TOAST_DURATION = 2000L

/**
 * Содержит методы показа диалогов, тостов, snackbar и т д
 */
class ChatInfoDialogDelegate(
    private val context: Context,
    private val view: View
) {

    fun blockReportResultSnackbar(onDismiss: () -> Unit): NSnackbar {
        return NSnackbar.with(view)
            .inView(view)
            .marginBottom(SNACKBAR_BOTTOM_MARGIN_DP)
            .text(context.getString(R.string.complaints_user_blocked_report_send))
            .description(context.getString(R.string.touch_to_delete))
            .durationIndefinite()
            .button(context.getString(R.string.general_cancel))
            .dismissManualListener { onDismiss() }
            .timer(TIME_TO_UNDO_ACTION_SEC)
            .show()
    }

    fun blockUserResultSnackbar(onDismiss: () -> Unit): NSnackbar {
        return NSnackbar.with(view)
            .inView(view)
            .marginBottom(SNACKBAR_BOTTOM_MARGIN_DP)
            .text(context.getString(R.string.user_complain_user_blocked))
            .description(context.getString(R.string.touch_to_delete))
            .durationIndefinite()
            .button(context.getString(R.string.general_cancel))
            .dismissManualListener { onDismiss() }
            .timer(TIME_TO_UNDO_ACTION_SEC)
            .show()
    }

    fun reportActionResultSnackbar(): NSnackbar {
        return NSnackbar.with(view)
            .inView(view)
            .typeSuccess()
            .marginBottom(SNACKBAR_BOTTOM_MARGIN_DP)
            .text(context.getString(R.string.chat_complaint_send_success))
            .durationLong()
            .show()
    }

    fun successForwardChatMessageSnackbar(view: View, chatName: String): NSnackbar {
        val text = context.getString(R.string.chat_fwd_message_success, chatName)
        return NSnackbar.with(view)
            .typeSuccess()
            .marginBottom(FWD_MESSAGE_SNACKBAR_MARGIN_BOTTOM)
            .text(text)
            .show()
    }

    fun showTextCopiedTooltip(messageView: View?, scope: CoroutineScope) {
        if (messageView != null) {
            val messageViewScreenLocation = IntArray(2)
            messageView.getLocationInWindow(messageViewScreenLocation)

            val textCopiedView = LayoutInflater
                .from(context)
                .inflate(R.layout.text_copied_popup, null, false)

            val textCopiedPopup: PopupWindow? = PopupWindow(context)
            textCopiedPopup?.contentView = textCopiedView
            textCopiedPopup?.setBackgroundDrawable(null)
            textCopiedPopup?.isOutsideTouchable = true
            textCopiedPopup?.animationStyle = R.style.popup_window_animation
            textCopiedPopup?.showAtLocation(
                this.view.rootView,
                Gravity.TOP or Gravity.START,
                messageViewScreenLocation[0],
                messageViewScreenLocation[1] - 40.dp // 40.dp - высота textCopiedView
            )

            scope.launch {
                delay(2000)
                textCopiedPopup?.dismiss()
            }
        }
    }

    fun showMediaCopiedSnackbar(): NSnackbar {
        val text = context.getString(R.string.image_is_copied)
        return NSnackbar.with(view)
            .typeSuccess()
            .setIcon(R.drawable.ic_chat_copy_media)
            .marginBottom(MEDIA_COPIED_SNACKBAR_MARGIN_BOTTOM)
            .text(text)
            .show()
    }

    fun showTextCopiedSnackbar(): NSnackbar {
        return NSnackbar.with(view)
            .typeSuccess()
            .setIcon(R.drawable.ic_outlined_check_circle_m)
            .iconTintColor(R.color.uiKitColorAccentSuccess)
            .marginBottom(MEDIA_COPIED_SNACKBAR_MARGIN_BOTTOM)
            .text(context.getString(R.string.general_text_copied))
            .show()
    }

    fun showAddToFavoritesToast(
        fragment: Fragment,
        mediaUrl: String?,
        marginBottom: Int,
        scope: CoroutineScope
    ) {
        val toast = DialogAddToFavoritesSuccessBinding.inflate(fragment.layoutInflater)
        toast.ustAddToFavorites.setupAddToFavorites(mediaUrl)
        toast.ustAddToFavorites.visible()
        toast.ustAddToFavorites.setMargins(bottom = marginBottom)
        val dialog = AlertDialog.Builder(this@ChatInfoDialogDelegate.context)
            .setView(toast.root)
            .create()
        val window = dialog.window
        val params = window?.attributes
        params?.gravity = Gravity.BOTTOM
        params?.flags = params?.flags?.and(WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv())
        window?.attributes = params
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        scope.launch {
            dialog.show()
            delay(ADDED_TO_FAVORITES_TOAST_DURATION)
            dialog.dismiss()
        }
    }

    fun showAbortEditMessageDialog(fragmentManager: FragmentManager, onAllowClick: () -> Unit) {
        MeeraConfirmDialogBuilder()
            .setHeader(R.string.chat_edit_message_abort_title)
            .setDescription(R.string.chat_edit_message_abort_message)
            .setTopBtnText(R.string.chat_edit_message_abort_allow)
            .setBottomBtnText(R.string.chat_edit_message_abort_deny)
            .setCancelable(true)
            .setTopClickListener { onAllowClick.invoke() }
            .show(fragmentManager)
    }
}
