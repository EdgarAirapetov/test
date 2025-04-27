package com.numplates.nomera3.modules.chat.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraChatToolbarRequestMenuBinding

class MeeraChatToolbarRequestMenu @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    var clickButtonListener: ((clickType: MeeraChatToolbarRequestClickType) -> Unit)? = {}

    private val binding = MeeraChatToolbarRequestMenuBinding.inflate(LayoutInflater.from(context), this)

    init {
        orientation = VERTICAL
        initViews()
    }

    private fun initViews() {
        binding.root.setBackgroundColor(ContextCompat.getColor(context, R.color.uiKitColorBackgroundPrimary))
        binding.apply {
            btnAllowChat.setThrottledClickListener {
                clickButtonListener?.invoke(MeeraChatToolbarRequestClickType.ALLOW)
            }
            btnForbidChat.setThrottledClickListener {
                clickButtonListener?.invoke(MeeraChatToolbarRequestClickType.FORBID)
            }
        }
    }

}

enum class MeeraChatToolbarRequestClickType {
    ALLOW, FORBID
}
