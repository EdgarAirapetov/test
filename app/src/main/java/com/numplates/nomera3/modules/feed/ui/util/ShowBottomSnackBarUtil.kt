package com.numplates.nomera3.modules.feed.ui.util

import android.content.Context
import android.view.View
import com.meera.core.extensions.dp
import com.meera.core.utils.NSnackbar
import com.numplates.nomera3.R

private val DEFAULT_BOTTOM_MARGIN = 32.dp
private const val DEFAULT_BOTTOM_TOAST_DURATION_MS = 1000

class ShowBottomSnackBarUtil(
    private var context: Context,
    private val view: View?
) {
    private var snackBar: NSnackbar? = null

    fun show(
        message: String?,
        isError: Boolean,
        bottomMargin: Int = DEFAULT_BOTTOM_MARGIN
    ) {
        if (message == null) {
            return
        }

        snackBar = NSnackbar.with(view)
            .apply {
                if (isError) {
                    durationLong()
                    typeError()
                } else {
                    duration(DEFAULT_BOTTOM_TOAST_DURATION_MS)
                    typeSuccess()
                }
            }
            .marginBottom(bottomMargin)
            .text(message)
            .show()
    }

    fun showMediaDownloadError(retryCallback: () -> Unit) {
        snackBar = NSnackbar.with(view)
            .typeError()
            .marginBottom(DEFAULT_BOTTOM_MARGIN)
            .text(context.getString(R.string.media_download_error_title))
            .description(context.getString(R.string.media_download_error_description))
            .button(context.getString(R.string.media_download_error_button_retry))
            .dismissManualListener(retryCallback)
            .durationLong()
            .show()
    }
}
