package com.numplates.nomera3.modules.chat.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.meera.core.extensions.gone
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.databinding.MeeraChatToolbarSubscribeButtonBinding

class MeeraChatToolbarSubscribeButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
): FrameLayout(context, attrs, defStyleAttr) {

    var itemClickListener: (() -> Unit)? = {}
    var itemCloseListener: (() -> Unit)? = {}

    private val binding = MeeraChatToolbarSubscribeButtonBinding
        .inflate(LayoutInflater.from(context), this, true)

    init {
        initViews()
    }

    private fun initViews() {
        binding.root.setThrottledClickListener {
            this.gone()
            itemClickListener?.invoke()
        }
        binding.ivSubscribeButtonClose.setThrottledClickListener {
            this.gone()
            itemCloseListener?.invoke()
        }
    }

}
