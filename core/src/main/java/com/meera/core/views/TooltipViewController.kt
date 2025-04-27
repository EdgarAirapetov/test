package com.meera.core.views

import android.view.View
import androidx.annotation.StringRes
import com.meera.core.extensions.dp
import com.meera.core.utils.NSnackbar
import com.meera.core.utils.showCommonSuccessMessage

class TooltipViewController {

    private var infoTooltip: NSnackbar? = null
    private var view: View? = null

    fun init(view: View) {
        this.view = view
    }

    fun clear() {
        infoTooltip = null
        view = null
    }

    fun showSuccessTooltip(
        @StringRes text: Int,
        height: Int = 8
    ) {
        infoTooltip = NSnackbar.with(view)
            .typeSuccess()
            .marginBottom(height.dp)
            .text(view?.context?.getString(text))
            .durationLong()
            .show()
    }

    fun showSuccessTooltipMeera(
        @StringRes text: Int,
    ) {
        view?.let { showCommonSuccessMessage(it.context.getText(text), it) }
    }
}
