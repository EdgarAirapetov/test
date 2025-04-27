package com.numplates.nomera3.telecom

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import com.meera.core.extensions.apiAtLeast26

class RingtoneAudioFocusManager(private val appContext: Context) {

    private val audioManager: AudioManager by lazy(mode = LazyThreadSafetyMode.NONE) {
        appContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    private var currentAudioFocusRequest: AudioFocusRequest? = null


    fun requestAudioFocus() {
        if (!apiAtLeast26()) return
        val audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(getCallRingtoneAudioAttributes())
            .build()
        val response = audioManager.requestAudioFocus(audioFocusRequest)
        if (response == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            currentAudioFocusRequest = audioFocusRequest
        }
    }

    fun releaseAudioFocus() {
        currentAudioFocusRequest?.let {
            if (apiAtLeast26()) {
                audioManager.abandonAudioFocusRequest(it)
            }
        }
    }

    fun getCallRingtoneAudioAttributes(): AudioAttributes = AudioAttributes.Builder()
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
        .build()
}
