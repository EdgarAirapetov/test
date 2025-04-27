package com.numplates.nomera3.modules.usersettings.data.repository

import com.meera.core.di.scopes.AppScope
import com.meera.core.extensions.toBoolean
import com.meera.core.preferences.AppSettings
import com.meera.db.DataStore
import com.meera.db.dao.PrivacySettingsDao
import com.meera.db.models.usersettings.PrivacySettingDto
import com.meera.db.models.usersettings.PrivacySettingsResponseDto
import com.numplates.nomera3.data.network.ApiMain
import com.numplates.nomera3.modules.baseCore.helper.amplitude.screenshot.AmplitudeScreenshotAnalytics
import com.numplates.nomera3.modules.usersettings.data.mapper.PrivacySettingDataMapper
import com.numplates.nomera3.modules.usersettings.data.mapper.SettingsEnumMapper
import com.numplates.nomera3.modules.usersettings.domain.models.PrivacySettingModel
import com.numplates.nomera3.modules.usersettings.domain.repository.PrivacyUserSettingsRepository
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AppScope
class PrivacyUserSettingsRepositoryImpl @Inject constructor(
    dataStore: DataStore,
    private val apiMain: ApiMain,
    private val dataMapper: PrivacySettingDataMapper,
    private val settingsEnumMapper: SettingsEnumMapper,
    private val appSettings: AppSettings,
    private val screenshotAnalytics: AmplitudeScreenshotAnalytics
) : PrivacyUserSettingsRepository {

    private val settingsDao: PrivacySettingsDao = dataStore.privacySettingsDao()
    private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)

    override fun getUserPrivacySettingsFlow(): Flow<Result<List<PrivacySettingModel>>> {
        return flow {
            emit(Result.success(settingsDao.getAll()))
            val result = try {
                apiMain.getPrivacySettings()
            } catch (e: Exception) {
                emit(Result.failure(e))
                return@flow
            }
            if (result.data != null) {
                val dbItems = result.data.settings.map(dataMapper::mapDtoToDb)
                settingsDao.insertAll(dbItems)
                emitAll(settingsDao.getAllFlow().map { Result.success(it) })
            } else {
                emit(Result.failure(IllegalArgumentException("data is null")))
            }
        }.shareIn(
            scope = coroutineScope,
            started = SharingStarted.Lazily,
            replay = 1,
        ).map { result ->
            result.map { list ->
                list.map(dataMapper::mapDbToModel)
            }
        }
    }

    override suspend fun getUserPrivacySettings(): List<PrivacySettingModel> = withContext(Dispatchers.IO) {
        val result = apiMain.getPrivacySettings()
        val settings = if (result.data != null) {
            val dbItems = result.data.settings.map(dataMapper::mapDtoToDb)
            settingsDao.insertAll(dbItems)
            result.data.settings.map(dataMapper::mapDtoToModel)
        } else {
            error(result.err ?: UnknownError())
        }
        findShakeFlagAndCacheValue(settings)
        findSyncContactsFlagAndCache(settings)
        findShareScreenshotFlagAndCache(settings)
        return@withContext settings
    }

    override suspend fun getLocalUserPrivacySettings(): List<PrivacySettingModel> = withContext(Dispatchers.IO) {
        return@withContext settingsDao.getAll().map(dataMapper::mapDbToModel)
    }

    override suspend fun getUserSettingByKey(key: String): PrivacySettingModel = withContext(Dispatchers.IO) {
        return@withContext settingsDao.getByKey(key).let(dataMapper::mapDbToModel)
    }

    override fun setUserPersonalPrivacySetting(key: String, value: Int) =
        coroutineScope.launch(CoroutineExceptionHandler { _,_ -> } + Dispatchers.IO) {
            val dtoItem = PrivacySettingDto(key, value)
            val requestBody = PrivacySettingsResponseDto(listOf(dtoItem))
            apiMain.setPrivacySetting(requestBody)
            val dbItem = dataMapper.mapDtoToDb(dtoItem)
            settingsDao.updateValue(dbItem.key, dbItem.value)
        }

    override suspend fun restoreSettingsToDefault() {
        apiMain.restoreDefaultSettings()
        settingsDao.updateValuesTransaction(getDefaultSettings())
    }

    /**
     * Settings should be prepared locally according to the doc:
     * https://nomera.atlassian.net/wiki/spaces/NOM/pages/2938241035/.+3#Feature-%D0%BB%D0%B8%D1%81%D1%82
     */
    private fun getDefaultSettings(): Map<String, Int?> {
        return SettingsKeyEnum.values().associate { settingKey ->
            settingKey.key to settingsEnumMapper.mapKeyToType(settingKey)?.key
        }
    }

    private fun findShakeFlagAndCacheValue(settings: List<PrivacySettingModel>) {
        settings.find { it.key == SettingsKeyEnum.ALLOW_SHAKE_GESTURE.key }
            ?.value.toBoolean()
            .apply { appSettings.isNeedToRegisterShakeEvent = this }
    }

    private suspend fun findSyncContactsFlagAndCache(settings: List<PrivacySettingModel>) {
        settings.find { it.key == SettingsKeyEnum.ALLOW_CONTACT_SYNC.key }
            ?.value.toBoolean()
            .apply { appSettings.allowSyncContacts.set(this) }
    }

    private fun findShareScreenshotFlagAndCache(settings: List<PrivacySettingModel>) {
        settings.find { it.key == SettingsKeyEnum.ALLOW_SCREENSHOT_SHARING.key }
            ?.value?.toBoolean()
            ?.let {
                appSettings.allowShareScreenshot = it
                screenshotAnalytics.setUserPropertiesShareScreenshotChanged(it)
            }
    }
}
