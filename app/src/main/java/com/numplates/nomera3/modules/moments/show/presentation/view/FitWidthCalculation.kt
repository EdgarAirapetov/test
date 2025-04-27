package com.numplates.nomera3.modules.moments.show.presentation.view

import com.meera.core.extensions.dp
import com.meera.core.extensions.getScreenHeight

private val statusBarHeight = 24.dp
private val actionBarHeight = 48.dp
private val uiInterfaceHeight = statusBarHeight + actionBarHeight

interface FitWidthCalculation {

    fun computeContentTypePositionType(positionType: (ActionBarPositionType) -> Unit)

    fun isActionBarUnderContent(scaledHeight: Int?): Boolean {
        if (scaledHeight == null) return false
        val neededHeight = getScreenHeight() - uiInterfaceHeight
        return neededHeight - scaledHeight > 36.dp
    }

    fun isActionBarOnContent(scaledHeight: Int?): Boolean {
        if (scaledHeight == null) return false
        val neededHeight = getScreenHeight() - uiInterfaceHeight
        return neededHeight - scaledHeight < 12.dp && neededHeight - scaledHeight > 0
    }
}

enum class ActionBarPositionType{
    UNDER_CONTENT, ON_CONTENT, BOTTOM_PINNED
}
