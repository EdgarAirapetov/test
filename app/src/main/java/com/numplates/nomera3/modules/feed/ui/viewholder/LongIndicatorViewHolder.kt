package com.numplates.nomera3.modules.feed.ui.viewholder

import android.view.View

interface LongIndicatorViewHolder {
    fun getRootItemView(): View?

    fun getLongMediaContainer(): View?

    fun startDelayedShow()

    fun stopDelayedShow()

    fun showExpandMediaIndicator()

    fun hideLongIndicator()

    fun getContentBarHeight(): Long
}
