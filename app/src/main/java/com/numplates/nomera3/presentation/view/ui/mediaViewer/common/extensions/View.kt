package com.numplates.nomera3.presentation.view.ui.mediaViewer.common.extensions

import android.animation.Animator
import android.graphics.Rect
import android.view.View
import com.meera.core.extensions.setListener

internal val View?.localVisibleRect: Rect
    get() = Rect().also { this?.getLocalVisibleRect(it) }

internal val View?.globalVisibleRect: Rect
    get() = Rect().also { this?.getGlobalVisibleRect(it) }

internal val View?.hitRect: Rect
    get() = Rect().also { this?.getHitRect(it) }

internal val View?.isRectVisible: Boolean
    get() = this != null && globalVisibleRect != localVisibleRect

internal val View?.isVisible: Boolean
    get() = this != null && visibility == View.VISIBLE


internal inline fun <T : View> T.postApply(crossinline block: T.() -> Unit) {
    post { apply(block) }
}


internal fun View.animateAlpha(
    from: Float?,
    to: Float?,
    duration: Long,
    onAnimationEnd: ((Animator?) -> Unit)? = null
) {
    alpha = from ?: 0f
    clearAnimation()
    animate()
        .alpha(to ?: 0f)
        .setDuration(duration)
        .setListener(onAnimationEnd = onAnimationEnd)
        .start()
}


