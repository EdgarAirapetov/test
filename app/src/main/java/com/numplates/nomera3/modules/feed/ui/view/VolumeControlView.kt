package com.numplates.nomera3.modules.feed.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.core.view.setPadding
import com.meera.core.extensions.expandTouchArea
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FeedVolumeControlButtonBinding

class VolumeControlView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val volumeControl: ImageButton = FeedVolumeControlButtonBinding
        .inflate(LayoutInflater.from(context), this, true)
        .root

    init {
        if (attrs == null) {
            val offset = context.resources.getDimensionPixelSize(R.dimen.video_volume_button_padding)
            setPadding(offset)
            volumeControl.expandTouchArea(offset)
        } else {
            val averagePadding = (paddingBottom + paddingTop + paddingStart + paddingEnd) / 4
            volumeControl.expandTouchArea(averagePadding)
        }
    }

    /**
     * Forward default setter to [ImageButton] since inner [volumeControl] should visually handle touches
     */
    override fun setOnClickListener(listener: OnClickListener?) {
        volumeControl.setOnClickListener(listener)
    }

    fun setSoundOn() {
        volumeControl.setImageResource(R.drawable.ic_feed_sound_control_on)
    }

    fun setSoundOff() {
        volumeControl.setImageResource(R.drawable.ic_feed_sound_control_off)
    }

}
