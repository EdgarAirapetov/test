package com.numplates.nomera3.modules.contentsharing.ui.infrastructure

import android.app.Activity
import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.meera.core.keyboard.getRootView
import com.meera.core.utils.NSnackbar
import com.meera.core.utils.checkAppRedesigned
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.state.DismissListeners
import com.meera.uikit.snackbar.state.ErrorSnakeState
import com.meera.uikit.snackbar.state.SnackBarParams
import com.numplates.nomera3.R

private const val SNACKBAR_BOTTOM_MARGIN_DP = 50

class SharingAlertsDelegate(val context: Context) {

    fun showWentWrongSnackbar(activity: Activity) = with(context) {
        checkAppRedesigned(
            isRedesigned = {
                UiKitSnackBar.makeError(
                    view = activity.getRootView(),
                    params = SnackBarParams(
                        errorSnakeState = ErrorSnakeState(
                            messageText = context.getText(R.string.sharing_went_wrong)
                        )
                    )
                ).show()
            },
            isNotRedesigned = {
                NSnackbar.with(activity)
                    .typeError()
                    .text(getString(R.string.sharing_went_wrong))
                    .marginBottom(SNACKBAR_BOTTOM_MARGIN_DP)
                    .durationLong()
                    .show()
            }
        )
    }

    fun showNetworkErrorAlert(activity: Activity, positiveClick: () -> Unit) = with(context) {
        checkAppRedesigned(
            isRedesigned = {
                UiKitSnackBar.makeError(
                    view = activity.getRootView(),
                    params = SnackBarParams(
                        errorSnakeState = ErrorSnakeState(
                            messageText = context.getText(R.string.sharing_network_error_title)
                        ),
                        dismissListeners = DismissListeners(
                            dismissListener = { positiveClick.invoke() }
                        )
                    )
                ).show()
            },
            isNotRedesigned = {
                MaterialAlertDialogBuilder(this)
                    .setCancelable(false)
                    .setTitle(getString(R.string.sharing_network_error_title))
                    .setMessage(getString(R.string.sharing_network_error_message))
                    .setPositiveButton(getString(R.string.ok)) { _, _ -> positiveClick.invoke() }
                    .show()
            }
        )
    }

    fun showMediaErrorAlert(
        activity: Activity,
        mediaName: String,
        positiveClick: () -> Unit
    ) = with(context) {
        checkAppRedesigned(
            isRedesigned = {
                UiKitSnackBar.makeError(
                    view = activity.getRootView(),
                    params = SnackBarParams(
                        errorSnakeState = ErrorSnakeState(
                            messageText = context.getText(R.string.sharing_went_wrong)
                        ),
                        dismissListeners = DismissListeners(
                            dismissListener = { positiveClick.invoke() }
                        )
                    )
                ).show()
            },
            isNotRedesigned = {
                MaterialAlertDialogBuilder(this, R.style.MultilineDialogTheme)
                    .setCancelable(false)
                    .setTitle(getString(R.string.sharing_media_error_content, mediaName))
                    .setPositiveButton(getString(R.string.ok)) { _, _ -> positiveClick.invoke() }
                    .show()
            }
        )
    }

}
