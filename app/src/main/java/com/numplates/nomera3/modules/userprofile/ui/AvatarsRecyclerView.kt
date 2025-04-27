package com.numplates.nomera3.modules.userprofile.ui

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView

class AvatarsRecyclerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : RecyclerView(context, attrs) {

    var scrollEnabled = true
    override fun canScrollHorizontally(direction: Int): Boolean {
        if (scrollEnabled.not()) return false
        return super.canScrollHorizontally(direction)
    }
}
