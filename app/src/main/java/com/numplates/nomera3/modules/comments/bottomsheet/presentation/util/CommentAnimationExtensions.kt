package com.numplates.nomera3.modules.comments.bottomsheet.presentation.util

import android.view.View

const val COMMENT_BOTTOM_SHEET_SHOW_ANIMATION_DURATION = 150L
const val COMMENT_BOTTOM_SHEET_HIDE_ANIMATION_DURATION = 100L

internal fun View.animateCommentPanelTranslationY(newTranslation: Float, duration: Long = COMMENT_BOTTOM_SHEET_SHOW_ANIMATION_DURATION) {
    kotlin.runCatching {
        animate().cancel()
        animate()
            .translationY(newTranslation)
            .setDuration(duration)
            .start()
    }
}
