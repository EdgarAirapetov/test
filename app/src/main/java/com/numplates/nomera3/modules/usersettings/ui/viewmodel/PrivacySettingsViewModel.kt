package com.numplates.nomera3.modules.usersettings.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import com.meera.application_api.analytic.model.AmplitudeProperty
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.toInt
import com.numplates.nomera3.App
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.domain.interactornew.ObserveSyncContactsUseCase
import com.numplates.nomera3.domain.interactornew.SetSyncContactsPrivacyUseCase
import com.numplates.nomera3.domain.interactornew.StartSyncContactsUseCase
import com.numplates.nomera3.domain.interactornew.StopSyncContactsUseCase
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.baseCore.domain.repository.AmplitudeShakeAnalyticRepository
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePrivacyAboutMeType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePrivacyGarageType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePrivacyGiftsType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePrivacyMapType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePrivacyPersonalRoadType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereMapPrivacy
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudePrivacyPostWithNewAvatarChangeValuesPublishSettings
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeProfile
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeSelfFeedVisibilityChangeWhereProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.screenshot.AmplitudeScreenshotAnalytics
import com.numplates.nomera3.modules.baseCore.helper.amplitude.screenshot.AmplitudeScreenshotPositionProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.shake.AmplitudeShakePositionProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.sync_contacts.SyncContactsAnalytic
import com.numplates.nomera3.modules.baseCore.helper.amplitude.sync_contacts.SyncContactsConst.SYNC_COUNT
import com.numplates.nomera3.modules.baseCore.helper.amplitude.sync_contacts.SyncContactsSuccessActionTypeProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.sync_contacts.SyncContactsToggleProperty
import com.numplates.nomera3.modules.bump.domain.usecase.SetNeedToRegisterShakeUseCase
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.maps.ui.entity.MapVisibilitySettingsOrigin
import com.numplates.nomera3.modules.maps.ui.entity.toAmplitudePropertyWhereMapPrivacy
import com.numplates.nomera3.modules.screenshot.domain.usecase.SetShareScreenshotEnabledUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.GetSettingsFlowUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.GetSettingsUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.RestoreDefaultSettingsUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.SetSettingsUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.SettingsParams.CommonSettingsParams
import com.numplates.nomera3.modules.usersettings.ui.mapper.PrivacySettingUiMapper
import com.numplates.nomera3.modules.usersettings.ui.models.PrivacySettingUiModel
import com.numplates.nomera3.presentation.model.enums.SettingsKeyEnum
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum.ALL
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum.FRIENDS
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum.NOBODY
import com.numplates.nomera3.presentation.model.enums.toAmplitudePropertySettingVisibility
import com.numplates.nomera3.presentation.utils.networkconn.NetworkStatusProvider
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.MeeraPrivacySettingsData
import com.numplates.nomera3.presentation.view.fragments.privacysettings.adapters.PrivacySettingsAdapter.Companion.SETTING_ITEM_TYPE_COMMUNICATION
import com.numplates.nomera3.presentation.viewmodel.BaseViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.PrivacySettingsViewEvent
import com.numplates.nomera3.presentation.viewmodel.viewevents.SingleLiveEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class PrivacyNewViewModel : BaseViewModel() {

    @Inject
    lateinit var getSettingsUseCase: GetSettingsUseCase

    @Inject
    lateinit var getSettingsFlowUseCase: GetSettingsFlowUseCase

    @Inject
    lateinit var setSettingsUseCase: SetSettingsUseCase

    @Inject
    lateinit var restoreDefaultSettingsUseCase: RestoreDefaultSettingsUseCase

    @Inject
    lateinit var networkStatusProvider: NetworkStatusProvider

    @Inject
    lateinit var amplitudeHelper: AnalyticsInteractor

    @Inject
    lateinit var amplitudeProfile: AmplitudeProfile

    @Inject
    lateinit var privacySettingUiMapper: PrivacySettingUiMapper

    @Inject
    lateinit var setShakeEnabledUseCase: SetNeedToRegisterShakeUseCase

    @Inject
    lateinit var setShareScreenshotEnabledUseCase: SetShareScreenshotEnabledUseCase

    @Inject
    lateinit var featureToggleContainer: FeatureTogglesContainer

    @Inject
    lateinit var amplitudeShakeAnalytic: AmplitudeShakeAnalyticRepository

    @Inject
    lateinit var getUserUidUseCase: GetUserUidUseCase

    @Inject
    lateinit var startSyncContactsUseCase: StartSyncContactsUseCase

    @Inject
    lateinit var stopSyncContactsUseCase: StopSyncContactsUseCase

    @Inject
    lateinit var observeSyncContactsUseCase: ObserveSyncContactsUseCase

    @Inject
    lateinit var syncContactsAnalytic: SyncContactsAnalytic

    @Inject
    lateinit var setSyncContactsPrivacyUseCase: SetSyncContactsPrivacyUseCase

    @Inject
    lateinit var featureTogglesContainer: FeatureTogglesContainer

    @Inject
    lateinit var screenshotAnalytics: AmplitudeScreenshotAnalytics

    private val _liveViewEvents = MutableLiveData<PrivacySettingsViewEvent>()
    val liveViewEvents = _liveViewEvents as LiveData<PrivacySettingsViewEvent>

    private val _liveProgress = MutableLiveData(false)
    val liveProgress = _liveProgress as LiveData<Boolean>

    private val _eventLiveData = SingleLiveEvent<PrivacyScreenEvent>()
    val eventLiveData = _eventLiveData as LiveData<PrivacyScreenEvent>

    private val _liveSettings = MutableLiveData<List<PrivacySettingUiModel>>()
    val liveSettings = _liveSettings as LiveData<List<PrivacySettingUiModel>>

    private val settingsSwitchingJobs = mutableMapOf<String, Job>()

    init {
        App.component.inject(this)
        observeSyncContactsWork()
    }

    fun isHiddenAgeAndGender() = featureToggleContainer.hiddenAgeAndSexFeatureToggle.isEnabled

    fun subscribeToPrivacySettings() {
        Timber.e("Subscribe to settings changes")
        getSettingsFlowUseCase.invoke()
            .flowOn(Dispatchers.IO)
            .catch {
                Timber.e("Error load privacy settings")
                _liveViewEvents.postValue(PrivacySettingsViewEvent.OnLoadSettingsError)
            }
            .onEach { result ->
                Timber.d("Updated settings were pushed from storage")
                result.getOrNull()?.let { settings ->
                    _liveSettings.postValue(settings.mapNotNull(privacySettingUiMapper::mapModelToUi))
                } ?: run {
                    _liveViewEvents.postValue(PrivacySettingsViewEvent.OnLoadSettingsError)
                }
            }
            .distinctUntilChanged()
            .launchIn(viewModelScope)
    }

    fun requestSettings() {
        Timber.e("Request settings from SERVER")
        viewModelScope.launch {
            runCatching {
                val settings = getSettingsUseCase.invoke()
                _liveSettings.postValue(settings.mapNotNull(privacySettingUiMapper::mapModelToUi))
            }.onFailure {
                Timber.e("Error load privacy settings")
                _liveViewEvents.postValue(PrivacySettingsViewEvent.OnLoadSettingsError)
            }
        }
    }

    fun checkNetworkAndRestoreSettings() {
        if (!networkStatusProvider.isInternetConnected()) {
            _eventLiveData.value = PrivacyScreenEvent.InternetConnectionError
        } else {
            _eventLiveData.value = PrivacyScreenEvent.StartUndoTimer
        }
    }

    fun restoreDefaultSettings() {
        viewModelScope.launch {
            try {
                restoreDefaultSettingsUseCase.invoke()
                _eventLiveData.value = PrivacyScreenEvent.SettingsRestored
            } catch (e: Exception) {
                _eventLiveData.value = PrivacyScreenEvent.InternetConnectionError
                Timber.e(e)
            }
        }
    }

    fun setSetting(key: String, value: Int, shouldUpdate: Boolean = false, needPushAmplitude: Boolean = true) {
        var job = settingsSwitchingJobs[key]
        if (job?.isActive == true) job.cancel()
        Timber.e("SEND setting to server: key:$key  value:$value")
        if (needPushAmplitude) {
            amplitudeHelper.logPrivacySettings(getPrivacySettingProperty(key, value))
        }
        job = setSettingsUseCase.invoke(CommonSettingsParams(key, value))
        handleSuccessSetSetting(
            key = key,
            enabled = value.toBoolean()
        )
        if (key == SettingsKeyEnum.SHOW_PERSONAL_ROAD.key) {
            logSelfFeedVisibilityChange(SettingsUserTypeEnum.fromKey(value))
        }
        job.invokeOnCompletion { error ->
            when {
                error == null && shouldUpdate -> requestSettings()
                error != null -> {
                    Timber.e("Failure: ${error};")
                    handleSetSettingHttpError(
                        key = key,
                        value = value
                    )
                }
            }
        }
        settingsSwitchingJobs[key] = job
    }

    fun handleSyncContactSwitchAction(
        key: String,
        enabled: Boolean
    ) {
        val togglePosition = SyncContactsToggleProperty.valueOf(enabled)
        syncContactsAnalytic.logSyncContactsToggleChanged(
            positionProperty = togglePosition,
            userId = getUserUidUseCase.invoke()
        )
        if (enabled.not()) {
            _eventLiveData.value = PrivacyScreenEvent.ShowConfirmCancelSyncContactsDialogUiEffect
        } else {
            _eventLiveData.value = PrivacyScreenEvent.ShowContactsSyncPermissionUiEffect(
                key = key,
                enabled = enabled.toInt()
            )
        }
    }

    fun mapPermissionsClicked() =
        amplitudeHelper.logMapPrivacySettingsClicked(AmplitudePropertyWhereMapPrivacy.SETTINGS)

    fun logSettings(
        typeEnum: SettingsUserTypeEnum,
        origin: MapVisibilitySettingsOrigin
    ) {
        val where = origin.toAmplitudePropertyWhereMapPrivacy()
        val visibility = typeEnum.toAmplitudePropertySettingVisibility()
        amplitudeHelper.logMapPrivacySettingsSetup(
            where = where,
            visibility = visibility
        )
    }

    fun changePermissionSyncContactsSwitch(
        currentList: List<MeeraPrivacySettingsData>,
        enabled: Boolean
    ) {
        val model = currentList.find { model ->
            model.itemViewType() == SETTING_ITEM_TYPE_COMMUNICATION
        } as? MeeraPrivacySettingsData.MeeraPrivacySettingsCommunicationModel ?: return
        val syncModel = model.settings?.find { it.key == SettingsKeyEnum.ALLOW_CONTACT_SYNC.key }
            ?.copy(value = enabled.toInt()) ?: return
        val index = model.settings.indexOf(syncModel)
        val rootIndex = currentList.indexOf(model)
        if (index == -1 || rootIndex == -1) return
        val settings = model.settings.toMutableList()
        settings[index] = syncModel
        val result = model.copy(
            settings = settings
        )
        if (enabled.not()) {
            stopSyncContactsUseCase.invoke()
        }
        _eventLiveData.value = PrivacyScreenEvent.UpdateSetting(
            model = result,
            position = rootIndex
        )
    }

    fun handleShakeEnabledUiAction(
        key: String,
        enabled: Boolean
    ) {
        logShakeSwitchChanged(enabled)
        setShakeEnabledBySetting(enabled)
        setSetting(
            key = key,
            value = enabled.toInt(),
            needPushAmplitude = false
        )
    }

    fun handleShareScreenshotSwitchAction(
        key: String,
        enabled: Boolean
    ) {
        setShareScreenshotEnabledBySettings(enabled)
        screenshotAnalytics.setUserPropertiesShareScreenshotChanged(enabled)
        screenshotAnalytics.logScreenshotShareTogglePress(
            positionProperty = if (enabled) AmplitudeScreenshotPositionProperty.ON else AmplitudeScreenshotPositionProperty.OFF,
            fromId = getUserUidUseCase.invoke()
        )
        setSetting(
            key = key,
            value = enabled.toInt(),
            needPushAmplitude = false
        )
    }

    fun logPrivacyPostWithNewAvatarChange(publishSettings: AmplitudePrivacyPostWithNewAvatarChangeValuesPublishSettings) {
        amplitudeProfile.privacyPostWithNewAvatarChangeSettings(publishSettings)
    }

    fun logSuccessDialogClosedByButton(
        isCloseByButton: Boolean, syncCount: Int
    ) {
        val actionType = if (isCloseByButton) SyncContactsSuccessActionTypeProperty.GREAT else
            SyncContactsSuccessActionTypeProperty.CLOSE
        syncContactsAnalytic.logContactsSyncFinished(
            actionTypeProperty = actionType,
            userId = getUserUidUseCase.invoke(),
            syncCount = syncCount
        )
    }

    fun isShakeToggleEnabled() = featureToggleContainer.shakeFeatureToggle.isEnabled

    private fun logShakeSwitchChanged(enabled: Boolean) {
        val property = if (enabled) AmplitudeShakePositionProperty.ON else AmplitudeShakePositionProperty.OFF
        amplitudeShakeAnalytic.logShakeSwitchChanged(
            shakePositionProperty = property,
            userId = getUserUidUseCase.invoke()
        )
    }

    private fun setShakeEnabledBySetting(isEnabled: Boolean) = viewModelScope.launch {
        setShakeEnabledUseCase.invoke(isEnabled)
    }

    private fun setShareScreenshotEnabledBySettings(isEnabled: Boolean) = viewModelScope.launch {
        setShareScreenshotEnabledUseCase.invoke(isEnabled)
    }

    fun closedProfile(): Boolean {
        return liveSettings.value?.firstOrNull { it.key == SettingsKeyEnum.CLOSED_PROFILE.key }?.value?.toBoolean()
            ?: false
    }

    private fun logSelfFeedVisibilityChange(typeEnum: SettingsUserTypeEnum) {
        amplitudeProfile.logSelfFeedVisibilityChange(
            where = AmplitudeSelfFeedVisibilityChangeWhereProperty.SETTINGS,
            visibility = typeEnum.toAmplitudePropertySettingVisibility(),
            userId = getUserUidUseCase.invoke()
        )
    }

    private fun getPrivacySettingProperty(key: String, value: Int): AmplitudeProperty {
        return when (key) {
            SettingsKeyEnum.SHOW_PERSONAL_ROAD.key -> {
                when (value) {
                    FRIENDS.key -> AmplitudePrivacyPersonalRoadType.FRIENDS
                    ALL.key -> AmplitudePrivacyPersonalRoadType.ALL
                    NOBODY.key -> AmplitudePrivacyPersonalRoadType.NOBODY
                    else -> AmplitudePrivacyMapType.ALL
                }
            }

            SettingsKeyEnum.SHOW_ABOUT_ME.key -> {
                when (value) {
                    FRIENDS.key -> AmplitudePrivacyAboutMeType.FRIENDS
                    ALL.key -> AmplitudePrivacyAboutMeType.ALL
                    NOBODY.key -> AmplitudePrivacyAboutMeType.NOBODY
                    else -> AmplitudePrivacyMapType.ALL
                }
            }

            SettingsKeyEnum.SHOW_GARAGE.key -> {
                when (value) {
                    FRIENDS.key -> AmplitudePrivacyGarageType.FRIENDS
                    ALL.key -> AmplitudePrivacyGarageType.ALL
                    NOBODY.key -> AmplitudePrivacyGarageType.NOBODY
                    else -> AmplitudePrivacyMapType.ALL

                }
            }

            SettingsKeyEnum.SHOW_GIFTS.key -> {
                when (value) {
                    FRIENDS.key -> AmplitudePrivacyGiftsType.FRIENDS
                    ALL.key -> AmplitudePrivacyGiftsType.ALL
                    NOBODY.key -> AmplitudePrivacyGiftsType.NOBODY
                    else -> AmplitudePrivacyMapType.ALL

                }
            }

            else -> {
                when (value) {
                    FRIENDS.key -> AmplitudePrivacyMapType.FRIENDS
                    ALL.key -> AmplitudePrivacyMapType.ALL
                    NOBODY.key -> AmplitudePrivacyMapType.NOBODY
                    else -> AmplitudePrivacyMapType.ALL
                }
            }
        }
    }

    private fun handleSetSettingHttpError(
        key: String,
        value: Int
    ) {
        when (key) {
            SettingsKeyEnum.ALLOW_SHAKE_GESTURE.key -> {
                setShakeEnabledBySetting(value.toBoolean().not())
            }
        }
    }

    private fun handleSuccessSetSetting(
        key: String,
        enabled: Boolean
    ) {
        viewModelScope.launch {
            when (key) {
                SettingsKeyEnum.ALLOW_CONTACT_SYNC.key -> {
                    if (enabled) {
                        viewModelScope.launch {
                            startSyncContactsUseCase.invoke()
                        }
                    }
                    setSyncContactsPrivacyUseCase.invoke(enabled)
                }
            }
        }
    }

    fun handlePermissionReadContactsDenied(
        currentList: List<MeeraPrivacySettingsData>,
        deniedAndNoRationaleNeededAfterRequest: Boolean
    ) {
        changePermissionSyncContactsSwitch(
            currentList = currentList,
            enabled = false
        )
        if (deniedAndNoRationaleNeededAfterRequest) {
            _eventLiveData.value = PrivacyScreenEvent.ShowSyncContactsDialogPermissionDenied
        }
    }

    private fun observeSyncContactsWork() {
        observeSyncContactsUseCase.invoke()
            .catch { e ->
                Timber.e(e)
            }
            .onEach { work ->
                val isSuccessFinished = work?.state == WorkInfo.State.SUCCEEDED
                if (isSuccessFinished) {
                    val syncCount = work?.outputData?.getInt(SYNC_COUNT, 0) ?: 0
                    _eventLiveData.postValue(PrivacyScreenEvent.ShowSyncContactsSuccessUiEffect(syncCount))
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Settings screen events.
     */
    sealed class PrivacyScreenEvent {
        object SettingsRestored : PrivacyScreenEvent()
        object InternetConnectionError : PrivacyScreenEvent()
        object StartUndoTimer : PrivacyScreenEvent()
        data class ShowContactsSyncPermissionUiEffect(
            val key: String,
            val enabled: Int
        ) : PrivacyScreenEvent()

        object ShowConfirmCancelSyncContactsDialogUiEffect : PrivacyScreenEvent()
        data class UpdateSetting(
            val model: MeeraPrivacySettingsData,
            val position: Int
        ) : PrivacyScreenEvent()

        object ShowSyncContactsDialogPermissionDenied : PrivacyScreenEvent()
        data class ShowSyncContactsSuccessUiEffect(val syncCount: Int) : PrivacyScreenEvent()
    }
}
