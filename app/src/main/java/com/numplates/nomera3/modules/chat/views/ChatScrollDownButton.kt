package com.numplates.nomera3.modules.chat.views

import android.content.Context
import android.icu.text.CompactDecimalFormat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.meera.core.extensions.invisible
import com.meera.core.extensions.visible
import com.numplates.nomera3.databinding.ChatScrollDownButtonBinding
import java.util.Locale


class ChatScrollDownButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ChatScrollDownButtonBinding.inflate(LayoutInflater.from(context), this)

    fun setCount(count: Int) = with(binding.tvUnreadCount) {
        if (count > 0) {
            setCount(formatCount(count))
            visible()
        } else {
            invisible()
        }
    }

    fun setClickListener(clickListener: OnClickListener) = with(binding.btnScrollDown) {
        setOnClickListener(clickListener)
    }

    private fun formatCount(count: Int): String {
        return CompactDecimalFormat.getInstance(
            Locale.US,
            CompactDecimalFormat.CompactStyle.SHORT
        ).format(count).replace(".", ",").lowercase()
    }
}
