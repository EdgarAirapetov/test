package com.numplates.nomera3.modules.peoples.ui.content.decorator

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.dp

class BloggerMediaContentOffSetsDecorator(
    private val mLeft: Int = 0,
    private val mTop: Int = 0,
    private val mRight: Int = 0,
    private val mBottom: Int = 0
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        with(outRect) {
            left = mLeft.dp
            top = mTop.dp
            right = mRight.dp
            bottom = mBottom.dp
        }
    }
}
