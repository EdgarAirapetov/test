package com.numplates.nomera3.modules.posts.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ViewVideoDurationBinding

class VideoDurationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private val binding: ViewVideoDurationBinding = LayoutInflater.from(context)
        .inflate(R.layout.view_video_duration, this, false)
        .apply(::addView)
        .let(ViewVideoDurationBinding::bind)

    fun setDurationText(duration: String) {
        binding.tvVideoDurationLabel.text = duration
    }

    fun setSoundOff() = binding.ivVideoDurationIcon.setImageResource(R.drawable.ic_meera_sound_off)

    fun setSoundOn() = binding.ivVideoDurationIcon.setImageResource(R.drawable.ic_meera_sound_on)
}
