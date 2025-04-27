package com.meera.core.utils

import android.text.Layout
import android.text.Selection
import android.view.MotionEvent
import android.text.style.ClickableSpan
import android.text.Spannable
import android.widget.TextView
import android.text.method.ArrowKeyMovementMethod

/**
 * ArrowKeyMovementMethod does support selection of text but not the clicking of links.
 * LinkMovementMethod does support clicking of links but not the selection of text.
 * This class adds the link clicking to the ArrowKeyMovementMethod.
 * We basically take the LinkMovementMethod onTouchEvent code and remove the line
 * Selection.removeSelection(buffer);
 * which deselects all text when no link was found.
 */
object EnhancedMovementMethod : ArrowKeyMovementMethod() {

    override fun onTouchEvent(
        widget: TextView,
        buffer: Spannable,
        event: MotionEvent
    ): Boolean {
        val action = event.action
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
            var x = event.x.toInt()
            var y = event.y.toInt()
            x -= widget.totalPaddingLeft
            y -= widget.totalPaddingTop
            x += widget.scrollX
            y += widget.scrollY
            val layout: Layout = widget.layout
            val line: Int = layout.getLineForVertical(y)
            val off: Int = layout.getOffsetForHorizontal(line, x.toFloat())
            val link = buffer.getSpans(
                off,
                off - 1,
                ClickableSpan::class.java
            )
            if (link.isNotEmpty()) {
                when (action) {
                    MotionEvent.ACTION_UP -> {
                        link[0].onClick(widget)
                    }
                    MotionEvent.ACTION_DOWN -> {
                        Selection.setSelection(
                            buffer,
                            buffer.getSpanStart(link[0]),
                            buffer.getSpanEnd(link[0]))
                    }
                }
                return true
            } else {
                //Selection.removeSelection(buffer)
            }
        }
        return super.onTouchEvent(widget, buffer, event)
    }
}