package com.numplates.nomera3.modules.moments.show.presentation.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.meera.core.extensions.applyRoundedOutline
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.moments.util.isSmallScreen

class PlayerViewContainer @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAtr: Int = 0
) : FrameLayout(context, attributeSet, defStyleAtr) {

    private val cornerRadius = context.resources.getDimension(R.dimen.corner_radius_large)

    override fun onAttachedToWindow() {
        if (!isSmallScreen()) applyRoundedOutline(cornerRadius)
        super.onAttachedToWindow()
    }
}
