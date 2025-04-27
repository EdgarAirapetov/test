package com.numplates.nomera3.presentation.view.utils.apphints

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.widget.PopupWindow
import com.numplates.nomera3.R

object TooltipPopupWindowCreator {
    @SuppressLint("ClickableViewAccessibility")
    fun createTooltip(context: Context?, tooltip: Tooltip): PopupWindow? {
        if (context != null) {
            val tooltipView = TooltipContentViewCreator
                    .createTooltipView(context, tooltip)
                    ?: return null

            val popupWindow = PopupWindow(context)
            popupWindow.contentView = tooltipView
            popupWindow.isOutsideTouchable = true
            popupWindow.animationStyle = R.style.popup_window_animation
            popupWindow.setBackgroundDrawable(null)
            popupWindow.setTouchInterceptor { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    popupWindow.dismiss()
                    return@setTouchInterceptor true
                }

                return@setTouchInterceptor false
            }

            return popupWindow
        } else {
            return null
        }
    }
}

