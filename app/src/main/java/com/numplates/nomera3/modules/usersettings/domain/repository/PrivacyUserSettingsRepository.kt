package com.numplates.nomera3.modules.usersettings.domain.repository

import com.numplates.nomera3.modules.usersettings.domain.models.PrivacySettingModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow

interface PrivacyUserSettingsRepository {

    /**
     * Request the latest setting from server, cache it and return as a [Flow].
     * Any local changes will trigger flow updates.
     */
    fun getUserPrivacySettingsFlow(): Flow<Result<List<PrivacySettingModel>>>

    /**
     * Request the latest setting from server, cache it and return as a [List].
     */
    suspend fun getUserPrivacySettings(): List<PrivacySettingModel>

    /**
     * Get the latest user privacy settings from local storage.
     */
    suspend fun getLocalUserPrivacySettings(): List<PrivacySettingModel>

    /**
     * Set get single setting by key
     */
    suspend fun getUserSettingByKey(key: String): PrivacySettingModel

    /**
     * Set user settings on server side and cache locally.
     * NOTE: this function only updates value by key. black and white lists stay unchanged.
     */
    fun setUserPersonalPrivacySetting(key: String, value: Int) : Job

    /**
     * Restore privacy user settings to default on server and locally if there is no server error.
     */
    suspend fun restoreSettingsToDefault()
}
