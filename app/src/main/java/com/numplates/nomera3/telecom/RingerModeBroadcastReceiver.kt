package com.numplates.nomera3.telecom

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager

class RingerModeBroadcastReceiver(private val listener: OnRingerModeChangedListener) : BroadcastReceiver() {
    companion object {
        fun createIntentFilter() = IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        listener.onRingerModeChanged(audioManager?.ringerMode)
    }

    interface OnRingerModeChangedListener {
        fun onRingerModeChanged(mode: Int?)
    }
}
