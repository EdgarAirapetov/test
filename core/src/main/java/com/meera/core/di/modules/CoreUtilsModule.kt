package com.meera.core.di.modules

import android.content.Context
import android.content.SharedPreferences
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.google.gson.Gson
import com.meera.core.common.DATA_STORE_PREFERENCES_STORAGE_NAME
import com.meera.core.common.PREF_NAME
import com.meera.core.network.utils.BaseUrlManager
import com.meera.core.network.utils.LocaleManagerImpl
import com.meera.core.network.websocket.WebSocketMainChannel
import com.meera.core.permission.ReadContactsPermissionProvider
import com.meera.core.preferences.AppSettings
import com.meera.core.preferences.PrefManager
import com.meera.core.preferences.PrefManagerImpl
import com.meera.core.preferences.datastore.PreferenceDataStore
import com.meera.core.utils.HardwareIdUtil
import com.meera.core.utils.contacts.UserContactsProvider
import com.meera.core.utils.contacts.UserContactsProviderImpl
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Named
import javax.inject.Singleton

const val APPLICATION_COROUTINE_SCOPE = "APPLICATION_COROUTINE_SCOPE"

@Module
class CoreUtilsModule {

    @Singleton
    @Provides
    fun provideGson(): Gson = Gson()

    @Singleton
    @Provides
    fun providePreferenceDataStore(context: Context): PreferenceDataStore {
        return PreferenceDataStore(
            context = context,
            preferenceNameStorage = DATA_STORE_PREFERENCES_STORAGE_NAME
        )
    }

    @Singleton
    @Provides
    fun provideSettings(
        context: Context,
        gson: Gson,
        dataStore: PreferenceDataStore,
        prefManager: PrefManager
    ): AppSettings {
        return AppSettings(
            context = context,
            gson = gson,
            dataStore = dataStore,
            prefManager = prefManager
        )
    }

    @Singleton
    @Provides
    fun provideBaseUrlManager(context: Context, applicationSettings: AppSettings): BaseUrlManager {
        return BaseUrlManager(context, applicationSettings)
    }

    @Singleton
    @Provides
    fun provideSharedPreference(context: Context) : SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    @Singleton
    @Provides
    fun providePrefManager(context: Context, sharedPreferences: SharedPreferences) : PrefManager = PrefManagerImpl(sharedPreferences, context)

    @Singleton
    @Provides
    fun provideHardwareIdUtil(context: Context): HardwareIdUtil = HardwareIdUtil(context)

    /**
     * An external coroutine scope, nearly identical to [kotlinx.coroutines.GlobalScope].
     *
     * With this we can configure [kotlin.coroutines.CoroutineContext] for the whole scope.
     *
     * Currently, the only difference is the default dispatcher, coroutine name, global cancellability and injectability of the scope.
     */
    @Provides
    @Singleton
    @Named(APPLICATION_COROUTINE_SCOPE)
    fun provideApplicationCoroutineScope(): CoroutineScope {
        return CoroutineScope(Dispatchers.Main.immediate + SupervisorJob() + CoroutineName("ApplicationScope"))
    }

    @Provides
    @Singleton
    fun provideLocaleManager(context: Context): LocaleManagerImpl {
        return LocaleManagerImpl(
            context = context
        )
    }

    @Singleton
    @Provides
    fun provideWebSocket(
        context: Context,
        urlManager: BaseUrlManager,
        hardwareUtils: HardwareIdUtil,
        chuckerInterceptor: ChuckerInterceptor,
        appSettings: AppSettings
    ): WebSocketMainChannel {
        return WebSocketMainChannel(
            context = context,
            baseUrlManager = urlManager,
            hardwareUtil = hardwareUtils,
            chuckerInterceptor = chuckerInterceptor,
            appSettings = appSettings
        )
    }

    @Singleton
    @Provides
    fun provideUserContactsProvider(context: Context): UserContactsProvider {
        return UserContactsProviderImpl(context)
    }

    @Singleton
    @Provides
    fun provideContactsPermissionProvider(context: Context): ReadContactsPermissionProvider {
        return ReadContactsPermissionProvider(context)
    }
}
