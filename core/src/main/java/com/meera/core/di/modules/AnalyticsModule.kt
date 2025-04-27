package com.meera.core.di.modules

import android.app.Application
import android.content.Context
import com.amplitude.api.Amplitude
import com.amplitude.api.AmplitudeClient
import com.meera.core.common.AMPLITUDE_API_KEY
import com.meera.core.common.APP_METRICA_API_KEY
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
import com.yandex.metrica.push.YandexMetricaPush
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AnalyticsModule {

    @Singleton
    @Provides
    fun provideAmplitudeAnalytics(context: Context): AmplitudeClient {
        val application = context as Application
        return Amplitude.getInstance()
            .initialize(context, AMPLITUDE_API_KEY)
            .enableForegroundTracking(context).apply {
                setFlushEventsOnClose(false)
                trackSessionEvents(true)
                enableLogging(true)
            }.apply { initAppMetrics(application) }
    }

    private fun initAppMetrics(application: Application) {
        val config = YandexMetricaConfig
            .newConfigBuilder(APP_METRICA_API_KEY)
            .withCrashReporting(true)
            .withLocationTracking(true)
            .withSessionTimeout(300) // 5 минут
            .build()
        YandexMetrica.activate(application, config)
        YandexMetrica.enableActivityAutoTracking(application)
        YandexMetricaPush.init(application)
    }

}
