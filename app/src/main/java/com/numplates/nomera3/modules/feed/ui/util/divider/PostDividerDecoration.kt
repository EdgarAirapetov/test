package com.numplates.nomera3.modules.feed.ui.util.divider

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.R
import com.meera.core.extensions.dp

private val DIVIDER_HEIGHT = 8.dp

class PostDividerDecoration(
    context: Context,
    @DrawableRes
    regularPostDividerDrawableId: Int,
    @DrawableRes
    blackPostDividerDrawableId: Int
) : RecyclerView.ItemDecoration() {

    private val blackPostDivider = ContextCompat.getDrawable(context, blackPostDividerDrawableId)
    private val regularPostDivider = ContextCompat.getDrawable(context, regularPostDividerDrawableId)

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        drawVertical(c, parent)
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val currentItem = parent.getChildViewHolder(view)
        val childPosition = parent.getChildAdapterPosition(view)
        val itemCount = parent.adapter?.itemCount ?: 0

        if (itemCount == 0) {
            outRect.set(0, 0, 0, DIVIDER_HEIGHT)
            return
        }

        if (currentItem is IDividedPost && childPosition != itemCount - 1) {
            outRect.set(0, 0, 0, DIVIDER_HEIGHT)
        } else {
            outRect.set(0, 0, 0, 0)
        }
    }


    private fun drawVertical(c: Canvas, parent: RecyclerView) {
        val childCount = parent.childCount

        for (viewPosition in 0 until childCount - 1) {
            val child = parent.getChildAt(viewPosition)
            val nextPosition = viewPosition + 1
            val nextChild = if (nextPosition < childCount) {
                parent.getChildAt(nextPosition)
            } else {
                null
            }

            validateVerticalDividerAndGetDrawable(child, nextChild, parent)?.let { dividerDrawable ->
                dividerDrawable.setBounds(child.left, child.bottom, child.right, child.bottom + DIVIDER_HEIGHT)
                dividerDrawable.draw(c)
            }
        }
    }

    private fun validateVerticalDividerAndGetDrawable(child: View, nextChild: View?, parent: RecyclerView): Drawable? {
        val currentItem = parent.getChildViewHolder(child)
        val nextItem = if (nextChild != null) {
            parent.getChildViewHolder(nextChild)
        } else {
            null
        }

        val currentIsBlack = (currentItem as? IDividedPost)?.isVip() ?: return null
        val nextIsBlack = (nextItem as? IDividedPost)?.isVip() ?: return null

        val isBothPostBlack = currentIsBlack && nextIsBlack
        val isBothPostWhite = currentIsBlack.not() && nextIsBlack.not()

        return when {
            isBothPostBlack -> {
                blackPostDivider
            }
            isBothPostWhite -> {
                regularPostDivider
            }
            else -> {
                regularPostDivider
            }
        }
    }

    companion object {
        fun build(context: Context): PostDividerDecoration {
            return PostDividerDecoration(
                context,
                R.drawable.drawable_post_divider_decoration_light,
                R.drawable.drawable_post_divider_decoration_dark
            )
        }
    }
}
