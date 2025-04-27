package com.numplates.nomera3.modules.viewvideo.presentation.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import com.google.android.exoplayer2.C
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ViewVideoSmallSeekBarBinding
import com.numplates.nomera3.modules.feed.ui.getScreenWidth

class ViewVideoSmallSeekBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private val binding = LayoutInflater.from(context)
        .inflate(R.layout.view_video_small_seek_bar, this, false)
        .apply(::addView)
        .let(ViewVideoSmallSeekBarBinding::bind)

    private var currentDuration: Long = C.TIME_UNSET
    private var currentPosition: Long = C.TIME_UNSET

    fun setDuration(duration: Long) {
        currentDuration = duration
    }

    fun setPosition(position: Long) {
        currentPosition = position
        renderCurrentPosition(position)
    }

    private fun renderCurrentPosition(position: Long) {
        if (currentDuration == 0L) return
        val totalWidth = getScreenWidth() - paddingStart - paddingEnd - marginStart - marginEnd
        val lp = binding.sivViewSmallVideoSeekBar.layoutParams
        lp.width = ((totalWidth * position) / currentDuration).toInt()
        binding.sivViewSmallVideoSeekBar.layoutParams = lp
    }
}
