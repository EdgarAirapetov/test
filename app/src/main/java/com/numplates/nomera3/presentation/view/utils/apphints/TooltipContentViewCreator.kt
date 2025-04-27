package com.numplates.nomera3.presentation.view.utils.apphints

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.numplates.nomera3.R

object TooltipContentViewCreator {

    fun createTooltipView(context: Context, tooltip: Tooltip): View? {
        return LayoutInflater.from(context).inflate(R.layout.new_tooltip_layout, null, false)?.apply {
            val txt = this.findViewById<TextView>(R.id.tooltip_text)
            txt?.text = context.getString(tooltip.textResId)
            this.findViewById<ImageView>(R.id.tooltip_top_pointer)?.visibility =
                    if (tooltip.pointerAlignment == TooltipPointerAlignment.Top && tooltip.pointerAlignment != TooltipPointerAlignment.None) {
                        View.VISIBLE
                    } else View.GONE

            this.findViewById<ImageView>(R.id.tooltip_bottom_pointer)?.visibility =
                    if (tooltip.pointerAlignment == TooltipPointerAlignment.Bottom && tooltip.pointerAlignment != TooltipPointerAlignment.None) {
                        View.VISIBLE
                    } else View.GONE

            val drawableStart = if (tooltip.imageDrawableStart == 0) null
            else ContextCompat.getDrawable(txt.context, tooltip.imageDrawableStart)
            txt?.setCompoundDrawablesWithIntrinsicBounds(
                    drawableStart, null, null, null
            )
        }
    }
}
