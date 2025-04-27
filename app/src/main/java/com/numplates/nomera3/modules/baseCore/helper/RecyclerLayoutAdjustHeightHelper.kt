package com.numplates.nomera3.modules.baseCore.helper

import android.view.View
import androidx.recyclerview.widget.RecyclerView



class RecyclerLayoutAdjustHeightHelper(private val recycler: RecyclerView): View.OnLayoutChangeListener {

    private var isFirstAdjustTop = true

    init {
        recycler.addOnLayoutChangeListener(this)
    }

    override fun onLayoutChange(
        v: View?,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        oldLeft: Int,
        oldTop: Int,
        oldRight: Int,
        oldBottom: Int
    ) {

        if (bottom < oldBottom) {
            if (isFirstAdjustTop) {
                isFirstAdjustTop = false
                return
            }
            recycler.smoothScrollBy(0, oldBottom - bottom)
        }
    }
}