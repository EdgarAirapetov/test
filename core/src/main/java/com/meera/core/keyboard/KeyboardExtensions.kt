package com.meera.core.keyboard

import android.app.Activity
import android.graphics.Rect
import android.view.View
import com.meera.core.utils.convertDpToPx

fun Activity.getRootView(): View {
    return findViewById(android.R.id.content)
}

fun Activity.isKeyboardOpen(): Boolean {
    val visibleBounds = Rect()
    this.getRootView().getWindowVisibleDisplayFrame(visibleBounds)
    val heightDiff = getRootView().height - visibleBounds.height()
    val marginOfError = Math.round(this.convertDpToPx(100F))
    return heightDiff > marginOfError
}

fun Activity.isKeyboardClosed(): Boolean {
    return !this.isKeyboardOpen()
}
