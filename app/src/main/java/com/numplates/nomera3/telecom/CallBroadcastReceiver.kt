package com.numplates.nomera3.telecom

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import timber.log.Timber

open class CallBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val BROADCAST_INTENT = "com.numplates.nomera3.android.action.broadcast"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Timber.e("BROADCAST getAction: ${intent?.action}")
    }
}