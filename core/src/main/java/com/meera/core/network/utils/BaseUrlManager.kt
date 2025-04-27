package com.meera.core.network.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.meera.core.BuildConfig
import com.meera.core.extensions.empty
import com.meera.core.extensions.toast
import com.meera.core.preferences.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val DEV = "Dev"
private const val STAGE = "Stage"
private const val PROD = "Prod"

private const val KEY_WORD_SERVER = "server = "

class BaseUrlManager(
    private val context: Context,
    private val appSettings: AppSettings?
) : BaseUrlProvider {

    private var baseUrl: String
    private var baseUrlSocket: String
    private var baseUrlUploadStorage: String

    init {
            baseUrl = when {
                !BuildConfig.DEBUG -> BaseUrlEnum.PROD_SERVER.link
                !appSettings?.readLastBaseUrl().isNullOrEmpty() ->
                    appSettings?.readLastBaseUrl().orDevServerLink()

                else -> BaseUrlEnum.DEV_SERVER.link
            }
            baseUrlSocket = when {
                !BuildConfig.DEBUG -> BaseUrlSocketEnum.PROD_SERVER.link
                !appSettings?.readLastBaseUrlSocket().isNullOrEmpty() ->
                    appSettings?.readLastBaseUrlSocket().orDevServerLink()

                else -> BaseUrlSocketEnum.DEV_SERVER.link
            }
            baseUrlUploadStorage = when (baseUrl) {
                BaseUrlEnum.PROD_SERVER.link -> BaseUrlUploadStorageEnum.PROD_SERVER.link
                BaseUrlEnum.STAGE_SERVER.link -> BaseUrlUploadStorageEnum.STAGE_SERVER.link
                else -> BaseUrlUploadStorageEnum.DEV_SERVER.link
            }
    }

    private fun String?.orDevServerLink() = this ?: BaseUrlSocketEnum.DEV_SERVER.link

    private fun showToastCurrentServer() {
        val toastText = when (baseUrl) {
            BaseUrlEnum.DEV_SERVER.link -> "$KEY_WORD_SERVER${BaseUrlEnum.DEV_SERVER.name}"
            BaseUrlEnum.STAGE_SERVER.link -> "$KEY_WORD_SERVER${BaseUrlEnum.STAGE_SERVER.name}"
            BaseUrlEnum.PROD_SERVER.link -> "$KEY_WORD_SERVER${BaseUrlEnum.PROD_SERVER.name}"
            else -> String.empty()
        }

        Handler(Looper.getMainLooper()).post { context.toast(toastText) }
    }

    suspend fun changeServer(url: String) {
        if (!BuildConfig.DEBUG) return
        when (url) {
            STAGE -> {
                baseUrl = BaseUrlEnum.STAGE_SERVER.link
                baseUrlSocket = BaseUrlSocketEnum.STAGE_SERVER.link
                baseUrlUploadStorage = BaseUrlUploadStorageEnum.STAGE_SERVER.link
            }

            PROD -> {
                baseUrl = BaseUrlEnum.PROD_SERVER.link
                baseUrlSocket = BaseUrlSocketEnum.PROD_SERVER.link
                baseUrlUploadStorage = BaseUrlUploadStorageEnum.PROD_SERVER.link
            }

            DEV -> {
                baseUrl = BaseUrlEnum.DEV_SERVER.link
                baseUrlSocket = BaseUrlSocketEnum.DEV_SERVER.link
                baseUrlUploadStorage = BaseUrlUploadStorageEnum.PROD_SERVER.link
            }
        }

        withContext(Dispatchers.IO) {
            appSettings?.writeLastBaseUrl(baseUrl)
            appSettings?.writeLastBaseUrlSocket(baseUrlSocket)
            appSettings?.writeLastBeagleUrl(url)
        }

        showToastCurrentServer()
    }

    fun provideServerName(): String {
        return when (baseUrl) {
            BaseUrlEnum.STAGE_SERVER.link -> BaseUrlEnum.STAGE_SERVER.name
            BaseUrlEnum.PROD_SERVER.link -> BaseUrlEnum.PROD_SERVER.name
            else -> BaseUrlEnum.DEV_SERVER.name
        }
    }

    override fun provideBaseUrl() = baseUrl

    override fun provideBaseUrlSocket() = baseUrlSocket

    override fun provideBaseUrlUploadStorage() = baseUrlUploadStorage

}
