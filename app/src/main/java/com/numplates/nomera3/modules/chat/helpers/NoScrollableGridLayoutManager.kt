package com.numplates.nomera3.modules.chat.helpers

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager

class NoScrollableGridLayoutManager(val context: Context, spanCnt: Int) :
        GridLayoutManager(context, spanCnt, HORIZONTAL, false) {

    override fun canScrollVertically(): Boolean {
        return false
    }

    override fun canScrollHorizontally(): Boolean {
        return false
    }
}