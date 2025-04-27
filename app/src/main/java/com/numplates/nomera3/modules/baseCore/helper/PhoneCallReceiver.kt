package com.numplates.nomera3.modules.baseCore.helper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager

interface CallState {
    fun onStartIncomingCall() {}

    fun onEndCall() {}
}

/**
 * В данный момент данный класс не используется. Но в будущем поставят задачу
 * на остановку записи аудио сообщения при входящем звонке. Данный класс применяется
 * в [com.numplates.nomera3.modules.chat.helpers.VoiceMessageRecordDelegate]
 *
 * fragment.context?.registerReceiver(
 *   phoneCallReceiver,
 *   IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
 * )
 *
 * fragment.context?.unregisterReceiver(phoneCallReceiver)
 */
class PhoneCallReceiver(private val result: CallState): BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val extra = intent?.getStringExtra(TelephonyManager.EXTRA_STATE)
        if (extra == TelephonyManager.EXTRA_STATE_OFFHOOK) {
            result.onStartIncomingCall()
        } else if (extra == TelephonyManager.EXTRA_STATE) {
            result.onEndCall()
        }
    }

}