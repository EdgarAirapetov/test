package com.numplates.nomera3.modules.appInfo.data.repository

import com.numplates.nomera3.modules.appInfo.data.entity.Settings
import com.numplates.nomera3.modules.appInfo.data.entity.UpdateResponse

interface AppInfoRepository {

    suspend fun getSettings(): Settings

    fun requestUpdateAppSettings(): UpdateResponse

    suspend fun requestUpdateAppSettingsNetwork(
        success: (UpdateResponse) -> Unit,
        fail: (Exception) -> Unit
    )

    fun resetCache()

    fun getFbForceUpdateAppVersion(): String

    fun setFbForceUpdateAppVersion(version: String)
}
