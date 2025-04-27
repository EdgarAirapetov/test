package com.numplates.nomera3.modules.userprofile.ui

import android.content.Context
import com.meera.core.extensions.dp

object ProfileUiUtils {

    private const val SUBSCRIBE_AREA_HEIGHT_DP = 90

    fun getSnippetHeight(context: Context?): Int {
        return if (context != null) {
            context.resources.displayMetrics.widthPixels + SUBSCRIBE_AREA_HEIGHT_DP.dp
        } else {
            0
        }
    }
}
