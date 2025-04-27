package com.numplates.nomera3.modules.moments.show.presentation.view

import android.content.Context
import android.util.AttributeSet
import androidx.core.view.isGone
import com.google.android.exoplayer2.ui.PlayerView
import com.meera.core.extensions.applyRoundedOutline
import com.meera.core.extensions.onMeasured
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.moments.util.isSmallScreen

class FitWidthPlayerView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAtr: Int = 0
) : PlayerView(context, attributeSet, defStyleAtr), FitWidthCalculation {

    private val cornerRadius = context.resources.getDimension(R.dimen.corner_radius_large)

    override fun onAttachedToWindow() {
        if (!isSmallScreen()) applyRoundedOutline(cornerRadius)
        super.onAttachedToWindow()
    }

    override fun computeContentTypePositionType(positionType: (ActionBarPositionType) -> Unit) {
        onMeasured {
            if (isGone) return@onMeasured
            val scaledHeight = height
            positionType.invoke(
                when {
                    isActionBarUnderContent(scaledHeight) -> ActionBarPositionType.UNDER_CONTENT
                    isActionBarOnContent(scaledHeight) -> ActionBarPositionType.ON_CONTENT
                    else -> ActionBarPositionType.BOTTOM_PINNED
                }
            )
        }
    }
}
