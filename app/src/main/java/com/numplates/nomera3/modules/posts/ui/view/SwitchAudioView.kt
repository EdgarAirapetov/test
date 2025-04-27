package com.numplates.nomera3.modules.posts.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.meera.uikit.widgets.gone
import com.meera.uikit.widgets.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ViewSwitchAudioBinding
import com.numplates.nomera3.modules.volume.domain.model.VolumeState

class SwitchAudioView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private val binding: ViewSwitchAudioBinding = LayoutInflater.from(context)
        .inflate(R.layout.view_switch_audio, this, false)
        .apply(::addView)
        .let(ViewSwitchAudioBinding::bind)

    fun setSoundState(volumeState: VolumeState) {
        when (volumeState) {
            VolumeState.ON -> {
                binding.controlViewIconSoundOn.visible()
                binding.controlViewIconSoundOff.gone()
            }
            VolumeState.OFF -> {
                binding.controlViewIconSoundOn.gone()
                binding.controlViewIconSoundOff.visible()
            }
        }
    }
}
