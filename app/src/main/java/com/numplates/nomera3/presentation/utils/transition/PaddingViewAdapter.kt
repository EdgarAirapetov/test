package com.numplates.nomera3.presentation.utils.transition

import android.graphics.drawable.Drawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.TransitionDrawable
import android.os.Build
import android.view.View
import com.bumptech.glide.request.transition.Transition

@Deprecated("Unused")
class PaddingViewAdapter(
    private val realAdapter: Transition.ViewAdapter,
    private val targetWidth: Int,
    private val targetHeight: Int
) : Transition.ViewAdapter {

    override fun getView(): View = realAdapter.view

    override fun getCurrentDrawable(): Drawable? {
        var drawable = realAdapter.currentDrawable
        if (drawable != null) {
            val padX = Math.max(0, targetWidth - drawable.intrinsicWidth) / 2
            val padY = Math.max(0, targetHeight - drawable.intrinsicHeight) / 2
            if (padX > 0 || padY > 0) {
                drawable = InsetDrawable(drawable, padX, padY, padX, padY)
            }
        }
        return drawable
    }

    override fun setDrawable(drawable: Drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && drawable is TransitionDrawable) {
            // For some reason padding is taken into account differently on M than before in LayerDrawable
            // PaddingMode was introduced in 21 and gravity in 23, I think NO_GRAVITY default may play
            // a role in this, but didn't have time to dig deeper than this.
            drawable.paddingMode = TransitionDrawable.PADDING_MODE_STACK
        }
        realAdapter.setDrawable(drawable)
    }
}
