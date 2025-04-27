package com.numplates.nomera3.modules.moments.show.presentation.view.progress

import android.content.Context
import android.util.AttributeSet

/**
 * Progress View for moment cards
 *
 * Current bar and progress of the current bar is set with [setCurrentProgress]
 */
class ExactProgressMomentsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : BaseMomentsProgressView(context, attrs, defStyleAttr) {

    fun setCurrentProgress(currentBar: Int, currentProgress: Float) {
        this.currentBar = currentBar
        this.currentBarProgress = currentProgress
        invalidate()
    }
}
