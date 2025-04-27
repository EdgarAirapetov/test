package com.meera.core.di

import android.content.Context
import com.amplitude.api.AmplitudeClient
import com.google.gson.Gson
import com.meera.core.di.modules.APPLICATION_COROUTINE_SCOPE
import com.meera.core.di.modules.AnalyticsModule
import com.meera.core.di.modules.CoreUtilsModule
import com.meera.core.di.modules.DOWNLOAD_RETROFIT
import com.meera.core.di.modules.DbModule
import com.meera.core.di.modules.FILE_STORAGE_API_RETROFIT
import com.meera.core.di.modules.GIPHY_API
import com.meera.core.di.modules.HIWAY_API_RETROFIT
import com.meera.core.di.modules.NULLABLE_API_RETROFIT
import com.meera.core.di.modules.OLD_API_RETROFIT
import com.meera.core.di.modules.RetrofitModule
import com.meera.core.di.modules.UPLOAD_RETROFIT
import com.meera.core.di.modules.UPLOAD_RETROFIT_STORAGE
import com.meera.core.network.utils.BaseUrlManager
import com.meera.core.network.utils.LocaleManagerImpl
import com.meera.core.network.websocket.WebSocketMainChannel
import com.meera.core.permission.ReadContactsPermissionProvider
import com.meera.core.preferences.AppSettings
import com.meera.core.preferences.PrefManager
import com.meera.core.preferences.datastore.PreferenceDataStore
import com.meera.core.utils.contacts.UserContactsProvider
import com.meera.db.DataStore
import com.meera.db.dao.DraftsDao
import com.meera.db.dao.MediakeyboardFavoritesDao
import com.meera.db.dao.PeopleApprovedUsersDao
import com.meera.db.dao.PeopleRelatedUsersDao
import com.meera.db.dao.RegistrationCountriesDao
import dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.CoroutineScope
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        CoreUtilsModule::class,
        RetrofitModule::class,
        DbModule::class,
        AnalyticsModule::class
    ]
)
interface CoreComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): CoreComponent
    }

    fun getGson(): Gson

    fun getPreferenceDataStore(): PreferenceDataStore

    fun getAppSettings(): AppSettings

    fun getBaseUrlManager(): BaseUrlManager

    fun getPrefManager(): PrefManager

    fun getLocaleManager(): LocaleManagerImpl

    fun getOkHttpClient(): OkHttpClient

    fun getRetrofit(): Retrofit

    @Named(HIWAY_API_RETROFIT)
    fun getRetrofitApiHiWay(): Retrofit

    @Named(UPLOAD_RETROFIT)
    fun getUploadRetrofit(): Retrofit

    @Named(DOWNLOAD_RETROFIT)
    fun getDownloadRetrofit(): Retrofit

    @Named(UPLOAD_RETROFIT_STORAGE)
    fun getUploadStorageRetrofit(): Retrofit

    @Named(GIPHY_API)
    fun getRetrofitGiphyApi(): Retrofit

    @Named(OLD_API_RETROFIT)
    fun getRetrofitOldApi(): Retrofit

    @Named(FILE_STORAGE_API_RETROFIT)
    fun getRetrofitFileStorage(): Retrofit

    @Named(NULLABLE_API_RETROFIT)
    fun getRetrofitNullable(): Retrofit

    fun getWebSocket(): WebSocketMainChannel

    fun getDataStore(): DataStore

    fun getAmplitude(): AmplitudeClient

    fun getRegistrationCountriesDao(): RegistrationCountriesDao

    fun getMediakeyboardFavoritesDao(): MediakeyboardFavoritesDao

    fun getDraftsDao(): DraftsDao

    fun getPeopleApprovedUsersDao(): PeopleApprovedUsersDao

    fun getPeopleRelatedUsersDao(): PeopleRelatedUsersDao

    @Named(APPLICATION_COROUTINE_SCOPE)
    fun getApplicationCoroutineScope(): CoroutineScope

    fun getUserContactsProvider(): UserContactsProvider

    fun getReadContactsPermissionProvider(): ReadContactsPermissionProvider
}
