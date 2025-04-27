package com.numplates.nomera3.modules.appInfo.data.repository

import com.meera.core.di.scopes.AppScope
import com.meera.core.extensions.empty
import com.meera.core.preferences.PrefManager
import com.numplates.nomera3.modules.appInfo.data.api.AppInfoApi
import com.numplates.nomera3.modules.appInfo.data.entity.Settings
import com.numplates.nomera3.modules.appInfo.data.entity.UpdateResponse
import timber.log.Timber
import javax.inject.Inject

const val DEFAULT_ERROR_MESSAGE = "Failed to load app info"
private const val APP_VERSION_FOR_FB_FORCE_UPDATE = "app version for fb force update"

@AppScope
class AppInfoRepositoryImpl @Inject constructor(
    private val prefManager: PrefManager,
    private val appInfoApi: AppInfoApi,
    ) : AppInfoRepository {

    private var setting: Settings? = null
    private var isSuccessfullyAppSettingsRequest = false

    override suspend fun getSettings(): Settings {
        try {
            return if (setting != null) {
                setting!!
            } else {
                doRequest()
            }
        } catch (exception: Exception) {
            isSuccessfullyAppSettingsRequest = false

            throw exception
        }
    }

    /**
     * @return Возвращает UpdateRecommendations если они имеются, иначе null
     * */
    override fun requestUpdateAppSettings() = getUpdateResponse()

    override suspend fun requestUpdateAppSettingsNetwork(
        success: (UpdateResponse) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            val res = appInfoApi.getApplicationInfo()
            res.data?.let {
                setting = it
                isSuccessfullyAppSettingsRequest = true
                success(getUpdateResponse())
            } ?: kotlin.run {
                fail(error("$DEFAULT_ERROR_MESSAGE data = null"))
            }
        } catch (e: Exception) {
            isSuccessfullyAppSettingsRequest = false
            Timber.e(DEFAULT_ERROR_MESSAGE)
            fail(e)
        }
    }

    override fun resetCache() {
        setting = null
        isSuccessfullyAppSettingsRequest = false
    }

    override fun getFbForceUpdateAppVersion(): String {
        return prefManager.getString(APP_VERSION_FOR_FB_FORCE_UPDATE, String.empty()).orEmpty()
    }

    override fun setFbForceUpdateAppVersion(version: String) {
        prefManager.putValue(APP_VERSION_FOR_FB_FORCE_UPDATE, version)
    }

    private suspend fun doRequest(): Settings {
        val response = appInfoApi.getApplicationInfo().data

        if (response != null) {
            setting = response
            isSuccessfullyAppSettingsRequest = true
        } else {
            error("$DEFAULT_ERROR_MESSAGE data = null")
        }

        return response
    }

    private fun getUpdateResponse(): UpdateResponse {
        return if (!isSuccessfullyAppSettingsRequest)
            UpdateResponse.UpdateError
        else if (setting?.updateRecommendations != null)
            UpdateResponse.UpdateSuccessShowUpdate(
                setting?.updateRecommendations!!,
                setting?.currentApp?.version
            )
        else UpdateResponse.UpdateSuccessNoUpdate
    }
}
