package com.numplates.nomera3.modules.feed.ui

import android.content.Context
import android.content.res.Resources
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Layout manager which load 1 extra page sized content by default.
 *
 * @property extraSpace extra size in pixels
 */
class ExtraLinearLayoutManager @JvmOverloads constructor(
    context: Context,
    orientation: Int = VERTICAL,
    reverseLayout: Boolean = false,
) : LinearLayoutManager(context, orientation, reverseLayout) {

    private var extraSpace = -1

    fun setExtraSpace(extraSpace: Int) {
        this.extraSpace = extraSpace
    }

    @Deprecated("Deprecated in Java")
    override fun getExtraLayoutSpace(state: RecyclerView.State): Int {
        return if (extraSpace > 0) extraSpace else DEFAULT_EXTRA_LAYOUT_SPACE
    }

    companion object {
        private val DEFAULT_EXTRA_LAYOUT_SPACE = getScreenHeight()
    }
}

fun getScreenWidth(): Int =
    Resources.getSystem().displayMetrics.widthPixels

fun getScreenHeight(): Int =
    Resources.getSystem().displayMetrics.heightPixels
