package com.numplates.nomera3.modules.notifications.ui.itemdecorator

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.dpToPx
import com.meera.core.extensions.drawable
import com.numplates.nomera3.R

class NotificationDividerItemDecorator(
        private val divider: Drawable
) : RecyclerView.ItemDecoration() {

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val left = parent.paddingLeft + dpToPx(16)
        val right = parent.width - parent.paddingRight
        val lastIndex = parent.adapter?.itemCount?.minus(1)

        parent.children.forEachIndexed { index, view ->
            val viewHolder = parent.getChildViewHolder(view)
            val nextViewHolder = parent.findViewHolderForAdapterPosition(viewHolder.absoluteAdapterPosition + 1)
            if (viewHolder is NotDecoratable) return@forEachIndexed
            if (nextViewHolder is NotDecoratable) return@forEachIndexed
            if (index == lastIndex) return@forEachIndexed

            val params = view.layoutParams as RecyclerView.LayoutParams
            val top = view.bottom + params.bottomMargin
            val bottom = top + divider.intrinsicHeight

            divider.bounds = Rect(left, top, right, bottom)
            divider.draw(c)
        }
    }

    /** Marker-interface to be used on ViewHolders we do not want to be decorated by NotificationDividerItemDecorator */
    interface NotDecoratable
}

fun Context.makeDefNotificationDivider(): NotificationDividerItemDecorator {
    val divider = this.drawable(R.drawable.notification_divider)
            ?: throw IllegalArgumentException("Couldn't get resource of notification divider")

    return NotificationDividerItemDecorator(divider)
}

class MeeraNotificationDividerItemDecorator(private val divider: Drawable): RecyclerView.ItemDecoration() {
    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight
        val lastIndex = parent.adapter?.itemCount?.minus(1)

        parent.children.forEachIndexed { index, view ->
            val viewHolder = parent.getChildViewHolder(view)
            val nextViewHolder = parent.findViewHolderForAdapterPosition(viewHolder.absoluteAdapterPosition + 1)
            if (viewHolder is NotDecoratable) return@forEachIndexed
            if (nextViewHolder is NotDecoratable) return@forEachIndexed
            if (index == lastIndex) return@forEachIndexed

            val params = view.layoutParams as RecyclerView.LayoutParams
            val top = view.bottom + params.bottomMargin
            val bottom = top + divider.intrinsicHeight

            divider.bounds = Rect(left, top, right, bottom)
            divider.draw(c)
        }
    }

    /** Marker-interface to be used on ViewHolders we do not want to be decorated by NotificationDividerItemDecorator */
    interface NotDecoratable
}

fun Context.makeMeeraDefNotificationDivider(): MeeraNotificationDividerItemDecorator {
    val divider = this.drawable(R.drawable.bg_divider_medium)
        ?: throw IllegalArgumentException("Couldn't get resource of notification divider")
    return MeeraNotificationDividerItemDecorator(divider)
}
