package com.numplates.nomera3.modules.maps.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.core.view.marginStart
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ViewMapPointInfoScrollingTextBinding
import com.numplates.nomera3.modules.maps.ui.widget.model.MapPointInfoScrollingTextUiModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


@SuppressLint("ClickableViewAccessibility")
class MapPointInfoScrollingTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var isUserControlled = false
    private val binding = LayoutInflater.from(context)
        .inflate(R.layout.view_map_point_info_scrolling_text, this, true)
        .let(ViewMapPointInfoScrollingTextBinding::bind)

    init {
        binding.hsvMapPointInfoScrollingText.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> isUserControlled = true
                MotionEvent.ACTION_UP -> isUserControlled = false
            }
            false
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        findViewTreeLifecycleOwner()
            ?.lifecycleScope
            ?.launch {
                while (isActive) {
                    delay(SCROLL_UPDATE_PERIOD_MS)
                    if (isUserControlled.not()) {
                        binding.hsvMapPointInfoScrollingText.smoothScrollBy(SCROLL_UPDATE_DELTA_PX, 0)
                    }
                    val scrollLimit = binding.tvMapPointInfoScrollingText2.x - binding.tvMapPointInfoScrollingText2.marginStart
                    if (binding.hsvMapPointInfoScrollingText.scrollX >= scrollLimit) {
                        binding.hsvMapPointInfoScrollingText.scrollTo(0, 0)
                    }
                }
            }
    }

    fun setUiModel(uiModel: MapPointInfoScrollingTextUiModel) {
        listOf(binding.tvMapPointInfoScrollingText1, binding.tvMapPointInfoScrollingText2).forEach {
            it.textSize = uiModel.textSizeSp
            it.text = uiModel.text
        }
    }

    companion object {
        private const val SCROLL_UPDATE_PERIOD_MS = 20L
        private const val SCROLL_UPDATE_DELTA_PX = 3
    }
}
