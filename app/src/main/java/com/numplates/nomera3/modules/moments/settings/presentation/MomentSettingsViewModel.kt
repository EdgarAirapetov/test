package com.numplates.nomera3.modules.moments.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meera.core.extensions.toBoolean
import com.numplates.nomera3.modules.moments.settings.data.MomentSettingsEvent
import com.numplates.nomera3.modules.moments.settings.domain.SwitchSaveToGalleryUseCase
import com.numplates.nomera3.modules.usersettings.domain.models.PrivacySettingModel
import com.numplates.nomera3.modules.usersettings.domain.usecase.GetSettingsFlowUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.GetSettingsUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.SetSettingsUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.SettingsParams
import com.numplates.nomera3.modules.usersettings.ui.mapper.PrivacySettingUiMapper
import com.numplates.nomera3.modules.usersettings.ui.models.PrivacySettingUiModel
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class MomentSettingsViewModel @Inject constructor(
    private val setSettingsUseCase: SetSettingsUseCase,
    private val getSettingsFlowUseCase: GetSettingsFlowUseCase,
    private val switchSaveToGalleryUseCase: SwitchSaveToGalleryUseCase,
    private val privacySettingsUiMapper: PrivacySettingUiMapper,
    private val saveToGalleryMapper: SaveToGalleryMapper,
    private val getSettingsUseCase: GetSettingsUseCase
) : ViewModel() {

    private val _settingsState: MutableSharedFlow<List<PrivacySettingUiModel>> = MutableSharedFlow()
    val settingsState: SharedFlow<List<PrivacySettingUiModel>> = _settingsState.asSharedFlow()

    private val _eventStream: MutableSharedFlow<MomentSettingsEvent> = MutableSharedFlow()
    val eventStream: SharedFlow<MomentSettingsEvent> = _eventStream.asSharedFlow()

    private var cachedSettings: List<PrivacySettingModel> = emptyList()

    private val settingsSwitchingJobs = mutableMapOf<String, Job>()

    init {
        subscribeToPrivacySettings()
    }

    fun isProfileClosed(): Boolean {
        return cachedSettings.firstOrNull { it.key == SettingsKeyEnum.CLOSED_PROFILE.key }?.value?.toBoolean() ?: false
    }

    fun subscribeToPrivacySettings() {
        Timber.e("Subscribe to settings changes")
        getSettingsFlowUseCase.invoke()
            .flowOn(Dispatchers.IO)
            .catch {
                Timber.e("Error load privacy settings")
                _eventStream.emit(MomentSettingsEvent.Error)
            }
            .onEach { result ->
                result.getOrNull()?.let { settings ->
                    Timber.d("Updated settings were pulled: $settings")
                    updateSettings(settings)
                } ?: run {
                    _eventStream.emit(MomentSettingsEvent.Error)
                }
            }
            .distinctUntilChanged()
            .launchIn(viewModelScope)
    }

    private suspend fun updateSettings(settings: List<PrivacySettingModel>) {
        cachedSettings = settings
        val data = cachedSettings
            .mapNotNull(privacySettingsUiMapper::mapModelToUi)
            .map(saveToGalleryMapper::mapSaveToGallerySetting)
        _settingsState.emit(data)
    }

    fun refreshUserSettingsFromCache() {
        viewModelScope.launch {
            updateSettings(cachedSettings)
        }
    }

    fun refreshUserSettingsFromNetwork() {
        requestSettings()
    }

    fun toggleSaveToGallery() {
        viewModelScope.launch {
            runCatching {
                switchSaveToGalleryUseCase.invoke()
            }.onFailure {
                _eventStream.emit(MomentSettingsEvent.Error)
            }
        }
    }

    fun setSetting(settingEnum: SettingsKeyEnum, value: SettingsUserTypeEnum, shouldUpdate: Boolean = false) {
        var job = settingsSwitchingJobs[settingEnum.key]
        if (job?.isActive == true) job.cancel()

        val requestBody =
            SettingsParams.PrivacySettingsParams(settingEnum, value)

        job = setSettingsUseCase.invoke(requestBody)
        job.invokeOnCompletion { error ->
            when {
                error == null && shouldUpdate -> requestSettings()
                else -> Unit
            }
        }
        settingsSwitchingJobs[settingEnum.key] = job
    }

    private fun requestSettings() {
        viewModelScope.launch {
            runCatching {
                val settings = getSettingsUseCase.invoke()
                updateSettings(settings)
            }
        }
    }
}
