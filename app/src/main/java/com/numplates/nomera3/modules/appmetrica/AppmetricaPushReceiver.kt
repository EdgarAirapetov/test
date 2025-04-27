package com.numplates.nomera3.modules.appmetrica

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.yandex.metrica.push.YandexMetricaPush
import timber.log.Timber


class AppmetricaPushReceiver: BroadcastReceiver() {

    // initial push sdk integration, just log the received data for now
    override fun onReceive(context: Context?, intent: Intent?) {
        val payload = intent?.getStringExtra(YandexMetricaPush.EXTRA_PAYLOAD)
        Timber.tag("AppMetrica PUSH SDK").d(payload.toString())
    }
}
