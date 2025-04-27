package com.numplates.nomera3.modules.chat.helpers.voicemessage

import android.media.MediaPlayer
import com.numplates.nomera3.presentation.audio.VoiceMessageView

private const val VISUALIZE_BAR_DELAY_MS = 100L

class VoiceMessageVisualizerRunnable(
    private val player: MediaPlayer,
    private val voiceView: VoiceMessageView
) : Runnable {

    override fun run() {
        try {
            voiceView.visualizer.progress = player.currentPosition

            if (player.duration != voiceView.localDuration) {
                voiceView.apply {
                    setPlayButton()
                    visualizer.drawBar(voiceView.visualizer.max)
                    isPlaying = false
                    voiceHandler.removeCallbacksAndMessages(null)
                }
            } else
                voiceView.voiceHandler.postDelayed(this, VISUALIZE_BAR_DELAY_MS)

        } catch (e: Exception) {
            e.printStackTrace()
            voiceView.voiceHandler.removeCallbacksAndMessages(null)
        }
    }

}
