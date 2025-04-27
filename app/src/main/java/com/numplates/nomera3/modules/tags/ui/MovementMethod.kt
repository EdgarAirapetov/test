package com.numplates.nomera3.modules.tags.ui

import android.text.Selection
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.MotionEvent
import android.widget.TextView

// если буду баги см. старый метод https://git.nomera.com/nomera/NUMAD/-/snippets/6
object MovementMethod : LinkMovementMethod() {

    override fun onTouchEvent(widget: TextView?, buffer: Spannable?, event: MotionEvent?): Boolean {
        if (widget == null || event == null || buffer == null) return super.onTouchEvent(widget, buffer, event)

        val action = event.action

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_DOWN) {
            val x = event.x.toInt()
            var y = event.y.toInt()
            y -= widget.totalPaddingTop
            y += widget.scrollY
            val layout = widget.layout
            val line = layout.getLineForVertical(y)
            val lineLeft = layout.getLineLeft(line)
            val lineRight = layout.getLineRight(line)

            y = event.y.toInt()

            val firstLineTop = layout.getLineTop(0) + widget.totalPaddingTop
            val lastLineBottom = layout.getLineBottom(widget.lineCount - 1) + widget.totalPaddingTop
            if (x > lineRight || (x >= 0 && x < lineLeft) || y < firstLineTop || y > lastLineBottom) {
                return true
            }

            val off: Int = layout.getOffsetForHorizontal(line, x.toFloat())
            val link = buffer.getSpans(off, off, ClickableSpan::class.java)
            if (link.isNotEmpty()) {
                if (action == MotionEvent.ACTION_DOWN) {
                    Selection.setSelection(
                        buffer,
                        buffer.getSpanStart(link[0]),
                        buffer.getSpanEnd(link[0])
                    )
                }
            } else {
                Selection.removeSelection(buffer)
            }
        }

        return super.onTouchEvent(widget, buffer, event)
    }
}
