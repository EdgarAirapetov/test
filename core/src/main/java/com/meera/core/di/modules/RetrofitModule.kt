package com.meera.core.di.modules

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.google.gson.GsonBuilder
import com.meera.core.BuildConfig
import com.meera.core.common.FILE_STORAGE_BASE_URL
import com.meera.core.common.GIPHY_BASE_URL
import com.meera.core.common.PLATFORM_NAME
import com.meera.core.devtool.applyDevToolInterceptor
import com.meera.core.extensions.empty
import com.meera.core.extensions.getHardwareId
import com.meera.core.extensions.getTimeZone
import com.meera.core.network.MeraNetworkException
import com.meera.core.network.utils.BaseUrlManager
import com.meera.core.network.utils.BaseUrlProvider
import com.meera.core.preferences.AppSettings
import dagger.Module
import dagger.Provides
import okhttp3.ConnectionPool
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.Proxy
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

const val UPLOAD_RETROFIT = "UPLOAD_RETROFIT"
const val DOWNLOAD_RETROFIT = "DOWNLOAD_RETROFIT"
const val UPLOAD_RETROFIT_STORAGE = "UPLOAD_RETROFIT_STORAGE"
const val OLD_API_RETROFIT = "OLD_API_RETROFIT"
const val HIWAY_API_RETROFIT = "HIWAY_API_RETROFIT"
const val NULLABLE_API_RETROFIT = "NULLABLE_API_RETROFIT"
const val FILE_STORAGE_API_RETROFIT = "FILE_STORAGE_API_RETROFIT"
const val GIPHY_API = "GIPHY_API"

const val HEADER_AUTHORIZATION = "authorization"
const val HEADER_TOKEN = "token"
const val TOKEN_BEARER = "Bearer"
const val HEADER_X_API_KEY = "X-API-Key"
const val HEADER_N_DEVICE = "n-device"
const val HEADER_N_BUILD = "n-build"
const val HEADER_N_OS = "n-os"
const val HEADER_N_DEVICE_ID = "n-device-id"
const val HEADER_N_TIMEZONE = "n-timezone"
const val HEADER_N_LOCALE = "n-locale"
const val URL_PARAM_LOCALE = "locale"

const val LOCALE_RU = "ru"
const val LOCALE_RU_FULL = "ru_ru"
const val LOCALE_EN = "en"
const val LOCALE_EN_FULL = "en_us"

private const val MAX_LENGTH_CONTENT = 250000L


@Module
class RetrofitModule {

    private val connectionTimeout = 10000
    private val connectionUploadTimeout = 900 * 1000
    private val connectionDownloadTimeout = 900 * 1000

    @Singleton
    @Provides
    fun okHttp(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(connectionTimeout.toLong(), TimeUnit.MILLISECONDS)
            .writeTimeout(connectionTimeout.toLong(), TimeUnit.MILLISECONDS)
            .readTimeout(connectionTimeout.toLong(), TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(true)
            .connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
            .build()
    }

    @Singleton
    @Provides
    fun retrofit(
        context: Context,
        appSettings: AppSettings,
        baseUrlManager: BaseUrlManager,
        chuckyInterceptor: ChuckerInterceptor
    ): Retrofit {

        val builder = Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseUrlManager.provideBaseUrl())

        val client = OkHttpClient.Builder()
            .connectTimeout(connectionTimeout.toLong(), TimeUnit.MILLISECONDS)
            .addInterceptor(BearerHeaderInterceptor(appSettings))
            .addInterceptor(HeaderAppInfoInterceptor(context, getHardwareId(context), appSettings.locale))
            .addInterceptor(BaseUrlInterceptor(baseUrlManager))
            .writeTimeout(connectionTimeout.toLong(), TimeUnit.MILLISECONDS)
            .readTimeout(connectionTimeout.toLong(), TimeUnit.MILLISECONDS)
            .proxy(Proxy.NO_PROXY)
            .retryOnConnectionFailure(true)
            .connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
        client.applyDevToolInterceptor()
        addLoggingInterceptor(client, HttpLoggingInterceptor.Level.BODY, chuckyInterceptor)
        return builder.client(client.build()).build()
    }
    @Named("NULLABLE_API_RETROFIT")
    @Provides
    fun retrofitNullable(
        context: Context,
        appSettings: AppSettings,
        baseUrlManager: BaseUrlManager,
        chuckyInterceptor: ChuckerInterceptor
    ): Retrofit {

        val builder = Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().serializeNulls().create()))
            .baseUrl(baseUrlManager.provideBaseUrl())

        val client = OkHttpClient.Builder()
            .connectTimeout(connectionTimeout.toLong(), TimeUnit.MILLISECONDS)
            .addInterceptor(BearerHeaderInterceptor(appSettings))
            .addInterceptor(HeaderAppInfoInterceptor(context, getHardwareId(context), appSettings.locale))
            .addInterceptor(BaseUrlInterceptor(baseUrlManager))
            .writeTimeout(connectionTimeout.toLong(), TimeUnit.MILLISECONDS)
            .readTimeout(connectionTimeout.toLong(), TimeUnit.MILLISECONDS)
            .proxy(Proxy.NO_PROXY)
            .retryOnConnectionFailure(true)
            .connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
        client.applyDevToolInterceptor()
        addLoggingInterceptor(client, HttpLoggingInterceptor.Level.BODY, chuckyInterceptor)
        return builder.client(client.build()).build()
    }

    @Singleton
    @Provides
    fun chuckyInterceptor(
        context: Context,
        appSettings: AppSettings
    ) =
        ChuckerInterceptor.Builder(context)
            .collector(ChuckerCollector(context).apply {
                showNotification = appSettings.isChuckerEnabled
            })
            .maxContentLength(MAX_LENGTH_CONTENT)
            .redactHeaders(emptySet())
            .alwaysReadResponseBody(false)
            .build()

    /**
     * !!! Уровень логирования обязательно NONE, BASIC или HEADER,
     * но ни в коем случае не BODY т.к. при загрузке больших файлов
     * проискодит OOM Exception в интерцепторе логирования
     */
    @Singleton
    @Provides
    @Named(UPLOAD_RETROFIT)
    fun retrofitUpload(
        context: Context,
        appSettings: AppSettings,
        baseUrlManager: BaseUrlManager,
        chuckyInterceptor: ChuckerInterceptor
    ): Retrofit {

        val builder = Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseUrlManager.provideBaseUrl())

        val client = OkHttpClient.Builder()
            .connectTimeout(connectionUploadTimeout.toLong(), TimeUnit.MILLISECONDS)
            .addInterceptor(BearerHeaderInterceptor(appSettings))
            .addInterceptor(HeaderAppInfoInterceptor(context, getHardwareId(context), appSettings.locale))
            .addInterceptor(BaseUrlInterceptor(baseUrlManager))
            .writeTimeout(connectionUploadTimeout.toLong(), TimeUnit.MILLISECONDS)
            .readTimeout(connectionUploadTimeout.toLong(), TimeUnit.MILLISECONDS)
            .proxy(Proxy.NO_PROXY)
            .retryOnConnectionFailure(true)
            .connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
        client.applyDevToolInterceptor()
        addLoggingInterceptor(client, HttpLoggingInterceptor.Level.BODY, chuckyInterceptor)
        return builder.client(client.build()).build()
    }

    /**
     * Storage for upload to server
     * */
    @Singleton
    @Provides
    @Named(UPLOAD_RETROFIT_STORAGE)
    fun retrofitUploadStorage(
        context: Context,
        appSettings: AppSettings,
        baseUrlManager: BaseUrlManager,
        chuckyInterceptor: ChuckerInterceptor
    ): Retrofit {

        val builder = Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseUrlManager.provideBaseUrlUploadStorage())

        val client = OkHttpClient.Builder()
            .connectTimeout(connectionUploadTimeout.toLong(), TimeUnit.MILLISECONDS)
            .addInterceptor(BearerHeaderInterceptor(appSettings))
            .addInterceptor(HeaderAppInfoInterceptor(context, getHardwareId(context), appSettings.locale))
//            .addInterceptor(BaseUrlInterceptor(baseUrlManager))
            .writeTimeout(connectionUploadTimeout.toLong(), TimeUnit.MILLISECONDS)
            .readTimeout(connectionUploadTimeout.toLong(), TimeUnit.MILLISECONDS)
            .proxy(Proxy.NO_PROXY)
            .retryOnConnectionFailure(true)
            .connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
        client.applyDevToolInterceptor()
        addLoggingInterceptor(client, HttpLoggingInterceptor.Level.BASIC, chuckyInterceptor)
        return builder.client(client.build()).build()
    }

    @Singleton
    @Provides
    @Named(DOWNLOAD_RETROFIT)
    fun retrofitDownload(
        context: Context,
        appSettings: AppSettings,
        baseUrlManager: BaseUrlManager,
        chuckyInterceptor: ChuckerInterceptor
    ): Retrofit {

        val builder = Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseUrlManager.provideBaseUrl())

        val client = OkHttpClient.Builder()
            .connectTimeout(connectionDownloadTimeout.toLong(), TimeUnit.MILLISECONDS)
            .addInterceptor(BearerHeaderInterceptor(appSettings))
            .addInterceptor(HeaderAppInfoInterceptor(context, getHardwareId(context), appSettings.locale))
            .addInterceptor(BaseUrlInterceptor(baseUrlManager))
            .writeTimeout(connectionDownloadTimeout.toLong(), TimeUnit.MILLISECONDS)
            .readTimeout(connectionDownloadTimeout.toLong(), TimeUnit.MILLISECONDS)
            .proxy(Proxy.NO_PROXY)
            .retryOnConnectionFailure(true)
            .connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
        client.applyDevToolInterceptor()
        addLoggingInterceptor(client, HttpLoggingInterceptor.Level.BODY, chuckyInterceptor)
        return builder.client(client.build()).build()
    }

    @Singleton
    @Provides
    @Named(GIPHY_API)
    fun retrofitGiphy(
        chuckyInterceptor: ChuckerInterceptor
    ): Retrofit {
        val builder = Retrofit.Builder()
            .baseUrl(GIPHY_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
        val client = OkHttpClient.Builder()
        client.applyDevToolInterceptor()
        addLoggingInterceptor(client, HttpLoggingInterceptor.Level.BODY, chuckyInterceptor)
        return builder.client(client.build()).build()
    }

    @Singleton
    @Provides
    @Named(OLD_API_RETROFIT)
    fun retrofitApiOld(
        context: Context,
        appSettings: AppSettings,
        baseUrlManager: BaseUrlManager,
        chuckyInterceptor: ChuckerInterceptor
    ): Retrofit {
        val builder = Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseUrlManager.provideBaseUrl())

        val client = OkHttpClient.Builder()
            .connectTimeout(connectionTimeout.toLong(), TimeUnit.MILLISECONDS)
            .addInterceptor(TokenInterceptor(appSettings))
            .addInterceptor(LocaleInterceptor(appSettings))
            .addInterceptor(BasicAuthInterceptor(appSettings))
            .addInterceptor(
                HeaderAppInfoInterceptor(
                    context,
                    getHardwareId(context),
                    appSettings.locale
                )
            )
            .addInterceptor(BaseUrlInterceptor(baseUrlManager))
            .writeTimeout(connectionTimeout.toLong(), TimeUnit.MILLISECONDS)
            .readTimeout(connectionTimeout.toLong(), TimeUnit.MILLISECONDS)
            .proxy(Proxy.NO_PROXY)
            .retryOnConnectionFailure(true)
            .connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
        client.applyDevToolInterceptor()
        addLoggingInterceptor(client, HttpLoggingInterceptor.Level.BODY, chuckyInterceptor)
        return builder.client(client.build()).build()
    }

    @Singleton
    @Provides
    @Named(HIWAY_API_RETROFIT)
    fun retrofitApiHiWay(
        context: Context,
        appSettings: AppSettings,
        baseUrlManager: BaseUrlManager,
        chuckyInterceptor: ChuckerInterceptor
    ): Retrofit {
        val builder = Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(baseUrlManager.provideBaseUrl())

        val client = OkHttpClient.Builder()
            .connectTimeout(connectionTimeout.toLong(), TimeUnit.MILLISECONDS)
            .addInterceptor(XApiKeyInterceptor(appSettings))

            .addInterceptor(
                HeaderAppInfoInterceptor(
                    context,
                    getHardwareId(context),
                    appSettings.locale
                )
            )
            .addInterceptor(BaseUrlInterceptor(baseUrlManager))
            .writeTimeout(connectionTimeout.toLong(), TimeUnit.MILLISECONDS)
            .readTimeout(connectionTimeout.toLong(), TimeUnit.MILLISECONDS)
            .proxy(Proxy.NO_PROXY)
            .retryOnConnectionFailure(true)
            .connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
        client.applyDevToolInterceptor()
        addLoggingInterceptor(client, HttpLoggingInterceptor.Level.BODY, chuckyInterceptor)
        return builder.client(client.build()).build()
    }

    /**
     * Storage for download from server
     * */
    @Singleton
    @Provides
    @Named(FILE_STORAGE_API_RETROFIT)
    fun retrofitApiFileStorage(
        context: Context,
        chuckyInterceptor: ChuckerInterceptor,
        appSettings: AppSettings
    ): Retrofit {
        val builder = Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(FILE_STORAGE_BASE_URL)

        val client = OkHttpClient.Builder()
            .connectTimeout(connectionTimeout.toLong(), TimeUnit.MILLISECONDS)
            .addInterceptor(
                HeaderAppInfoInterceptor(
                    context,
                    getHardwareId(context),
                    appSettings.locale
                )
            )
            .writeTimeout(connectionTimeout.toLong(), TimeUnit.MILLISECONDS)
            .readTimeout(connectionTimeout.toLong(), TimeUnit.MILLISECONDS)
            .retryOnConnectionFailure(true)
            .connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
        client.applyDevToolInterceptor()
        addLoggingInterceptor(client, HttpLoggingInterceptor.Level.BODY, chuckyInterceptor)
        return builder.client(client.build()).build()
    }

    fun addLoggingInterceptor(
        builder: OkHttpClient.Builder,
        level: HttpLoggingInterceptor.Level,
        chuckyInterceptor: ChuckerInterceptor
    ) {
        if (BuildConfig.DEBUG) {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = level
            builder
                .addInterceptor(interceptor)
                .addInterceptor(chuckyInterceptor)
        }
    }

}


internal class BearerHeaderInterceptor(
    private val appSettings: AppSettings
) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = appSettings.readAccessToken()
        val request = if (token.isNotEmpty()) {
            val originalRequest = chain.request()
            val authenticatedRequest = originalRequest
                .newBuilder()
                .header(HEADER_AUTHORIZATION, "$TOKEN_BEARER $token")
                .build()
            authenticatedRequest
        } else {
            chain.request()
        }
        return try  {
            chain.proceed(request)
        } catch (e: Exception) {
            throw MeraNetworkException("e = $e original message: ${e.message} endpoint: ${request.url} ")
        }
    }

}

internal class HeaderAppInfoInterceptor(
    private val context: Context,
    private val deviceId: String,
    private val currentLocale: String
) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val device = Build.MANUFACTURER + " - " + Build.MODEL
        val appVersion = getAppVersion(context)
        val appInfoRequest = request.newBuilder()
            .header(HEADER_N_DEVICE, device)
            .header(HEADER_N_BUILD, appVersion)
            .header(HEADER_N_OS, PLATFORM_NAME)
            .header(HEADER_N_DEVICE_ID, deviceId)
            .header(HEADER_N_TIMEZONE, getTimeZone().toString())
            .header(HEADER_N_LOCALE, currentLocale)
            .build()
        return chain.proceed(appInfoRequest)
    }

    private fun getAppVersion(context: Context): String {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            String.empty()
        }
    }

}

internal class TokenInterceptor(
    private val appSettings: AppSettings
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = appSettings.readAccessToken()
        return if (!token.isNullOrEmpty()) {
            val originalRequest = chain.request()
            val originalUrl = originalRequest.url
            val newUrlBuilder: HttpUrl.Builder = originalUrl.newBuilder()
            newUrlBuilder.addQueryParameter(HEADER_TOKEN, token)
            val url: HttpUrl = newUrlBuilder.build()
            val tokenRequest = originalRequest.newBuilder().url(url).build()
            chain.proceed(tokenRequest)
        } else {
            chain.proceed(chain.request())
        }
    }
}

internal class LocaleInterceptor(
    private val appSettings: AppSettings
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val locale = getLocale()
        return if (locale.isNotEmpty()) {
            val originalRequest = chain.request()
            val originalUrl = originalRequest.url
            val newUrlBuilder: HttpUrl.Builder = originalUrl.newBuilder()
            newUrlBuilder.addQueryParameter(URL_PARAM_LOCALE, locale)
            val url: HttpUrl = newUrlBuilder.build()
            val tokenRequest = originalRequest.newBuilder().url(url).build()
            chain.proceed(tokenRequest)
        } else {
            chain.proceed(chain.request())
        }
    }

    private fun getLocale(): String {
        val temp: String = appSettings.locale ?: LOCALE_RU
        return if (temp.contains(LOCALE_EN)) {
            LOCALE_EN_FULL
        } else {
            LOCALE_RU_FULL
        }
    }
}

internal class BasicAuthInterceptor(
    private val appSettings: AppSettings
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = appSettings.readAccessToken()
        return if (token.isNotEmpty()) {
            val request = chain.request()
            val authenticatedRequest = request.newBuilder()
                .header(HEADER_TOKEN, token).build()
            chain.proceed(authenticatedRequest)
        } else {
            chain.proceed(chain.request())
        }
    }
}

internal class BaseUrlInterceptor(
    private val provider: BaseUrlProvider
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val url = provider.provideBaseUrl().toHttpUrlOrNull()
        return if (url != null) {
            val newUrl = chain.request().url.newBuilder()
                .scheme(url.scheme)
                .host(url.host)
                .port(url.port)
                .build()
            chain.proceed(chain.request().newBuilder().url(newUrl).build())
        } else {
            chain.proceed(chain.request())
        }
    }
}

internal class XApiKeyInterceptor(
    private val appSettings: AppSettings
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = appSettings.readAccessToken()
        return if (token.isNotEmpty()) {
            val originalRequest = chain.request()
            val authenticatedRequest = originalRequest
                .newBuilder()
                .header(HEADER_X_API_KEY, token)
                .build()
            chain.proceed(authenticatedRequest)
        } else {
            chain.proceed(chain.request())
        }
    }
}

