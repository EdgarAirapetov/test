package com.numplates.nomera3.presentation.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.meera.core.extensions.empty
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.toBooleanOrNull
import com.meera.core.network.websocket.WebSocketMainChannel
import com.meera.core.permission.ReadContactsPermissionProvider
import com.meera.core.preferences.AppSettings
import com.meera.db.DataStore
import com.numplates.nomera3.ADMIN_SUPPORT_ID_NAME
import com.numplates.nomera3.App
import com.numplates.nomera3.NEED_SHOW_DIALOG
import com.numplates.nomera3.SHOW_CALL_POPUP
import com.numplates.nomera3.SHOW_FRIENDS_SUBSCRIBERS_POPUP
import com.numplates.nomera3.TRUE_VALUE
import com.numplates.nomera3.data.network.GetToken
import com.numplates.nomera3.domain.interactornew.AuthOldTokenUseCase
import com.numplates.nomera3.domain.interactornew.CheckMainFilterRecommendedUseCase
import com.numplates.nomera3.domain.interactornew.GetFriendsSubscribersPopupPrivacyUseCase
import com.numplates.nomera3.domain.interactornew.IsUserAuthorizedUseCase
import com.numplates.nomera3.domain.interactornew.SetAdminSupportIdUseCase
import com.numplates.nomera3.domain.interactornew.SetUserDateOfBirthUseCase
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.appInfo.data.entity.AppLinks
import com.numplates.nomera3.modules.appInfo.data.entity.CurrentInfo
import com.numplates.nomera3.modules.appInfo.data.entity.Settings
import com.numplates.nomera3.modules.appInfo.data.entity.UpdateResponse
import com.numplates.nomera3.modules.appInfo.domain.usecase.GetAppInfoAsyncUseCase
import com.numplates.nomera3.modules.appInfo.domain.usecase.GetUpdateInfoUseCase
import com.numplates.nomera3.modules.appInfo.ui.mapper.toDialogEntity
import com.numplates.nomera3.modules.auth.AuthStatus
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyHowWasOpened
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyRoadType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.FilterState
import com.numplates.nomera3.modules.baseCore.helper.amplitude.MapFilters
import com.numplates.nomera3.modules.baseCore.helper.amplitude.UserPrivacy
import com.numplates.nomera3.modules.baseCore.helper.amplitude.createAnonUser
import com.numplates.nomera3.modules.baseCore.helper.amplitude.people.AmplitudePeopleAnalytics
import com.numplates.nomera3.modules.baseCore.helper.amplitude.people.AmplitudePeopleWhereProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.people.AmplitudePeopleWhich
import com.numplates.nomera3.modules.baseCore.helper.amplitude.toAnalyticsUser
import com.numplates.nomera3.modules.bump.domain.usecase.TryToRegisterShakeEventUseCase
import com.numplates.nomera3.modules.maps.domain.usecase.GetMapSettingsUseCase
import com.numplates.nomera3.modules.newroads.fragments.BaseRoadsFragment
import com.numplates.nomera3.modules.notifications.service.SyncNotificationService
import com.numplates.nomera3.modules.peoples.domain.usecase.GetRelatedUsersAndCacheUseCase
import com.numplates.nomera3.modules.peoples.domain.usecase.GetTopUsersAndCacheUseCase
import com.numplates.nomera3.modules.registration.domain.GetCountriesUseCase
import com.numplates.nomera3.modules.screenshot.domain.usecase.GetShareScreenshotEnabledUseCase
import com.numplates.nomera3.modules.userprofile.domain.usecase.GetOwnLocalProfileUseCase
import com.numplates.nomera3.modules.usersettings.domain.usecase.GetSettingsUseCase
import com.numplates.nomera3.modules.usersettings.ui.mapper.PrivacySettingUiMapper
import com.numplates.nomera3.presentation.utils.networkconn.NetworkStatusProvider
import com.numplates.nomera3.presentation.view.utils.kotlinextensions.getHardwareId
import com.numplates.nomera3.presentation.viewmodel.viewevents.MainFragmentEvents
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class MainFragmentViewModel : BaseViewModel() {

    private val disposables = CompositeDisposable()

    @Inject
    lateinit var appSettings: AppSettings

    @Inject
    lateinit var oldTokenUseCase: AuthOldTokenUseCase

    @Inject
    lateinit var webSocketMainChannel: WebSocketMainChannel

    @Inject
    lateinit var getAppInfoUseCase: GetAppInfoAsyncUseCase

    @Inject
    lateinit var getUpdateInfoUseCase: GetUpdateInfoUseCase

    @Inject
    lateinit var dataStore: DataStore

    @Inject
    lateinit var tracker: AnalyticsInteractor

    @Inject
    lateinit var getSettingsUseCase: GetSettingsUseCase

    @Inject
    lateinit var getMapSettingsUseCase: GetMapSettingsUseCase

    @Inject
    lateinit var setUserDateOfBirthUseCase: SetUserDateOfBirthUseCase

    @Inject
    lateinit var networkStatusProvider: NetworkStatusProvider

    @Inject
    lateinit var getFriendsSubscribersPopupPrivacyUseCase: GetFriendsSubscribersPopupPrivacyUseCase

    @Inject
    lateinit var isUserAuthorizedUseCase: IsUserAuthorizedUseCase

    @Inject
    lateinit var privacySettingUiMapper: PrivacySettingUiMapper

    @Inject
    lateinit var setAdminSupportIdUseCase: SetAdminSupportIdUseCase

    @Inject
    lateinit var amplitudePeopleAnalytics: AmplitudePeopleAnalytics

    @Inject
    lateinit var getRelatedUsersAndCacheUseCase: GetRelatedUsersAndCacheUseCase

    @Inject
    lateinit var getTopUsersAndCacheUseCase: GetTopUsersAndCacheUseCase

    @Inject
    lateinit var tryToRegisterShakeEventUseCase: TryToRegisterShakeEventUseCase

    @Inject
    lateinit var getOwnLocalProfileUseCase: GetOwnLocalProfileUseCase

    @Inject
    lateinit var checkMainFilterRecommendedUseCase: CheckMainFilterRecommendedUseCase

    @Inject
    lateinit var getCountriesUseCase: GetCountriesUseCase

    @Inject
    lateinit var syncNotificationService: SyncNotificationService

    @Inject
    lateinit var readContactsPermissionProvider: ReadContactsPermissionProvider

    @Inject
    lateinit var getShareScreenshotEnabledUseCase: GetShareScreenshotEnabledUseCase

    val liveEvents = MutableLiveData<MainFragmentEvents>()

    private val DEFAULT_DELAY = 1000L

    init {
        App.component.inject(this)
    }

    val mapSettings = getMapSettingsUseCase.invoke()

    fun writeFirstLogin() = appSettings.writeFirstLogin(false)

    fun setIsUserRegistered(isRegistered: Boolean) {
        appSettings.userRegistered = (isRegistered)
    }

    fun setUserRegistered(isRegistered: Boolean) {
        appSettings.isNewUserRegistered = isRegistered
    }

    fun setIsHolidayShowNeeded(isHolidayShowNeeded: Boolean) {
        appSettings.isHolidayShowNeeded = isHolidayShowNeeded
    }

    fun setWriteShownUpdatedDialog(isShown: Boolean) = appSettings.writeShownUpdatedDialog(isShown)

    fun setAppVersionName(verName: String) = appSettings.writeAppVerName(verName)

    fun getAppVersion() = appSettings.readAppVerName()

    fun getIsShownUpdateDialog() = appSettings.readShownUpdatedDalog()

    /**
     * Migrate from old to new token (with refresh and expired) if refresh token is EMPTY
     */
    fun migrateOldToken() {
        val currentToken = appSettings.readAccessToken()
        val refreshToken = appSettings.userRefreshToken
        Timber.d("REFRESH-Token: $refreshToken Current TOKEN: $currentToken")
        if (refreshToken == String.empty()) {
            Timber.d("This OLD token")
            currentToken.let {
                oldTokenUseCase.migrateFromOldToken(currentToken)?.let { migrate ->
                    migrate.subscribeOn(Schedulers.io())
                        .subscribe({ response ->
                            Timber.d("OLD Token Response: $response")
                            if (response.code() == 200) {
                                val token = response.body()
                                token?.let {
                                    Timber.d("Success SAVE New token (REFRESH)")
                                    writeNewTokensAfterMigrate(it)
                                }
                            } else {
                                Timber.e("Error response migrate OLD token: -code- ${response.code()}")
                            }
                        }, { error -> Timber.e("ERROR: migrate OLD token: $error") })
                }
            }
        } else {
            Timber.d("This ALREADY New token (Refresh)")
        }
    }

    fun tryToRegisterShakeEvent() {
        viewModelScope.launch {
            tryToRegisterShakeEventUseCase.invoke(true)
        }
    }

    private fun writeNewTokensAfterMigrate(token: GetToken) {
        appSettings.writeAccessToken(token.accessToken)
        appSettings.writeTokenExpiresIn(token.expiresIn.toLong())
        appSettings.writeRefreshToken(token.refreshToken)
    }

    fun savePreferencesBirthdayFlag(dateOfBirth: Long?) {
        setUserDateOfBirthUseCase.invoke(dateOfBirth = dateOfBirth?.toInt() ?: 0)
    }

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }

    fun savePeopleContent() {
        saveRelatedUsers()
        saveTopUsers()
    }

    fun isUserAuthorized() = isUserAuthorizedUseCase.invoke()

    /**
     * Requesting main information about app [updateNotes, appVersion, dialog constants, admin id]
     * */
    fun requestAppInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                getAppInfoUseCase.resetCache()
                val settings = getAppInfoUseCase.executeAsync().await()
                handleAppSettingsResult(settings)
            } catch (exception: Exception) {
                Timber.e(exception)
                liveEvents.postValue(MainFragmentEvents.AppSettingsRequestFinished)
                handleAppUpdate()
            }
        }
    }

    private fun handleAppSettingsResult(settings: Settings) {
        viewModelScope.launch(Dispatchers.Main) {
            settings.currentApp?.let { currentInfo -> checkUpdateScreen(currentInfo) }
            handleCallPopUp(settings)
            handleFriendsSubscribersPopup(settings)
            saveAdminSupportIdPref(settings)
            saveAppLinks(settings.links)
            handleBirthdayToken(settings.showBirthdayCongratulation)
            liveEvents.postValue(MainFragmentEvents.AppSettingsRequestFinished)
            handleAppUpdate()
            loadCountries()
        }
    }

    private fun loadCountries() {
        viewModelScope.launch {
            runCatching {
                getCountriesUseCase.invoke()
            }.onFailure { t -> Timber.e(t) }
        }
    }

    private suspend fun handleBirthdayToken(showBirthdayDialog: Int?) {
        showBirthdayDialog?.let { isShow ->
            Timber.d("birthday dialog shown: $isShow")
            appSettings.isNeedShowBirthdayDialog.set(
                isShow == NEED_SHOW_DIALOG
            )
        }
    }

    private fun handleCallPopUp(settings: Settings?) {
        settings?.appInfo?.forEach { appModel ->
            if (appModel.name == SHOW_CALL_POPUP) {
                when (appModel.value
                    ?: "false") {  //not used bool because on backend value might be a supported admin id as string =(
                    "false" -> {
                        appSettings.writeIsWorthToShow(false)
                    }

                    "true" -> {
                        appSettings.writeIsWorthToShow(true)
                    }
                }
                return@forEach
            }
        }
    }

    private suspend fun handleFriendsSubscribersPopup(settings: Settings) {
        settings.appInfo.forEach { model ->
            if (model.name == SHOW_FRIENDS_SUBSCRIBERS_POPUP) {
                getFriendsSubscribersPopupPrivacyUseCase.invoke().set(
                    model.value == TRUE_VALUE
                )
            }
            return@forEach
        }
    }

    private fun saveAdminSupportIdPref(settings: Settings) {
        settings.appInfo
            .find { it.name == ADMIN_SUPPORT_ID_NAME }
            ?.value
            ?.toLongOrNull()
            ?.let(setAdminSupportIdUseCase::invoke)
    }

    private fun handleAppUpdate() {
        val response = getUpdateInfoUseCase.execute(DefParams())
        //если есть настройки то показываем
        handleUpdateAppResponse(response)
    }

    private fun handleUpdateAppResponse(response: UpdateResponse) {
        when (response) {
            UpdateResponse.UpdateError -> liveEvents.postValue(MainFragmentEvents.RegisterInternetObserver)
            UpdateResponse.UpdateSuccessNoUpdate -> Timber.d("No updates available")
            is UpdateResponse.UpdateSuccessShowUpdate ->
                liveEvents.postValue(MainFragmentEvents.ForceUpdateEvent(response.updateInfo.toDialogEntity(response.version)))
        }
    }

    fun onInternetConnectivityChanged() {
        if (networkStatusProvider.isInternetConnected()) {
            viewModelScope.launch(Dispatchers.Main) {
                delay(DEFAULT_DELAY)
                handleUpdateAppResponse(getUpdateInfoUseCase.execute(DefParams()))
            }
        }
    }

    /**
     * Handle app notes and app version
     * */
    private fun checkUpdateScreen(currentInfo: CurrentInfo) {
        currentInfo.version?.let { _ ->
            liveEvents.value = MainFragmentEvents.UpdateScreenEvent(
                currentInfo.notes, currentInfo.version
            )
        }
    }

    private fun saveAppLinks(links: AppLinks?) {
        appSettings.saveAppLinks(links)
    }

    fun openChat() = tracker.logBottomBarChatClicked()

    fun openProfile() = tracker.logBottomBarProfileClicked()

    fun logPeopleTabBarOpened(
        which: AmplitudePeopleWhich,
        where: AmplitudePeopleWhereProperty
    ) {
        amplitudePeopleAnalytics.setPeopleSelected(
            where = where,
            which = which
        )
    }

    fun openRoad(currentTab: BaseRoadsFragment.RoadTypeEnum?, isLaunchAutomatically: Boolean) {
        val roadType = when (currentTab) {
            BaseRoadsFragment.RoadTypeEnum.MAIN_ROAD -> AmplitudePropertyRoadType.MAIN_FEED
            BaseRoadsFragment.RoadTypeEnum.CUSTOM_ROAD -> AmplitudePropertyRoadType.SELF_FEED
            BaseRoadsFragment.RoadTypeEnum.SUBSCRIPTIONS_ROAD -> AmplitudePropertyRoadType.FOLLOW_FEED
            null -> null
        }
        roadType?.let {
            tracker.logBottomBarRoad(
                roadType = it,
                recFeed = if (roadType == AmplitudePropertyRoadType.MAIN_FEED) {
                    checkMainFilterRecommendedUseCase.invoke()
                } else {
                    false
                },
                roadHowWasOpened = if (isLaunchAutomatically) {
                    AmplitudePropertyHowWasOpened.AUTOMATICALLY
                } else {
                    AmplitudePropertyHowWasOpened.MANUALLY
                }
            )
        }
    }

    private val lastAuth: AuthStatus? = null
    fun logUserAnalytics(authStatus: AuthStatus, isGeoEnabled: Boolean, pushPermitted: Boolean) {
        if (lastAuth is AuthStatus.Authorized && authStatus is AuthStatus.Authorized) return
        if (lastAuth is AuthStatus.None && authStatus is AuthStatus.None) return

        viewModelScope.launch(Dispatchers.IO) {
            val isAnon = authStatus !is AuthStatus.Authorized
            val deviceId = App.get()?.let { getHardwareId(it) }
            val hasContactsPermission = readContactsPermissionProvider.hasContactsPermission()
            val isShareScreenshotEnabled = getShareScreenshotEnabledUseCase.invoke()
            if (!isAnon) {
                val profile = getOwnLocalProfileUseCase.invoke()
                val pushEnabled = appSettings.isNotificationEnabled
                val userPrivacy = getUserPrivacyForAnalytics()
                val mapFilters = getMapFiltersForAnalytics()
                val formatter = SimpleDateFormat(DATE_PATTERN, Locale.getDefault())
                val userOnlineDate = formatter.format(Calendar.getInstance().time)
                val isRecSystemAvailable = appSettings.isRecSystemAvailable
                val autoRecSystemChanged = appSettings.isAutoRecSystemChangedSingle.toBooleanOrNull()
                val isRecommendedSystemSelected = checkMainFilterRecommendedUseCase.invoke()
                profile?.toAnalyticsUser(

                    isAnon = isAnon,
                    deviceId = deviceId,
                    geoEnabled = isGeoEnabled,
                    userPrivacy = userPrivacy,
                    mapFilters = mapFilters,
                    pushPermitted = pushPermitted,
                    pushEnabled = pushEnabled,
                    userOnlineDate = userOnlineDate,
                    formatter = formatter,
                    hasContactsPermission = hasContactsPermission,
                    recSystemAvailable = isRecSystemAvailable,
                    autoRecSystemChanged = autoRecSystemChanged,
                    isRecommendedSystemSelected = isRecommendedSystemSelected,
                    isShareScreenshotEnabled = isShareScreenshotEnabled
                )?.let(tracker::setUser)
            } else {
                val user = createAnonUser(
                    deviceId = deviceId,
                    contactsPermission = hasContactsPermission
                )
                user.geoEnabled = isGeoEnabled
                tracker.setUser(user)
            }
        }

    }

    private suspend fun getUserPrivacyForAnalytics(): UserPrivacy? {
        return try {
            val privacySettings = getSettingsUseCase.invoke().mapNotNull(privacySettingUiMapper::mapModelToUi)
            val privacyAboutMe = mapPrivacySettings(privacySettings.find { it.key == PRIVACY_SHOW_ABOUTME }?.value)
            val privacyGarage = mapPrivacySettings(privacySettings.find { it.key == PRIVACY_SHOW_GARAGE }?.value)
            val privacyGifts = mapPrivacySettings(privacySettings.find { it.key == PRIVACY_SHOW_GIFTS }?.value)
            val privacySelfRoad =
                mapPrivacySettings(privacySettings.find { it.key == PRIVACY_SHOW_PERSONAL_ROAD }?.value)
            val privacyMap = mapPrivacySettings(privacySettings.find { it.key == PRIVACY_SHOW_MAP }?.value)
            val privacyShake =
                getMappedSwitchPrivacySettings(privacySettings.find { it.key == ALLOW_SHAKE_GESTURE }?.value)
            val privacySyncContacts =
                getMappedSwitchPrivacySettings(privacySettings.find { it.key == ALLOW_CONTACTS_SYNC }?.value)
            UserPrivacy(
                privacyAboutMe = privacyAboutMe,
                privacyGarage = privacyGarage,
                privacyGifts = privacyGifts,
                privacySelfRoad = privacySelfRoad,
                privacyMap = privacyMap,
                shakeSign = privacyShake,
                privacySyncContacts = privacySyncContacts
            )
        } catch (e: Throwable) {
            Timber.e(e)
            return null
        }

    }

    private fun getMappedSwitchPrivacySettings(value: Int?): String =
        FilterState.valueOf(value.toBoolean()).value

    private fun getMapFiltersForAnalytics(): MapFilters {
        val mapSettings = getMapSettingsUseCase.invoke()
        return MapFilters(
            mapFilterShowFriendsOnly = FilterState.valueOf(mapSettings.showFriendsOnly),
            mapFilterShowMen = FilterState.valueOf(mapSettings.showMen),
            mapFilterShowWomen = FilterState.valueOf(mapSettings.showWomen),
            mapShowPeople = mapSettings.showPeople,
            mapShowEvents = mapSettings.showEvents,
            mapShowFriends = mapSettings.showFriends
        )
    }

    private fun mapPrivacySettings(value: Int?): String {
        return when (value) {
            VALUE_ALL -> ALL
            VALUE_FRIENDS -> FRIENDS
            else -> NOBODY
        }
    }

    private fun saveRelatedUsers() {
        viewModelScope.launch {
            runCatching {
                getRelatedUsersAndCacheUseCase.invoke(
                    limit = PEOPLE_PAGE_LIMIT,
                    offset = PEOPLE_OFFSET_LIMIT
                )
            }.onFailure { t ->
                Timber.e(t)
            }
        }
    }

    private fun saveTopUsers() {
        viewModelScope.launch {
            runCatching {
                getTopUsersAndCacheUseCase.invoke(
                    limit = PEOPLE_PAGE_LIMIT,
                    offset = PEOPLE_OFFSET_LIMIT
                )
            }.onFailure {
                Timber.e(it)
            }
        }
    }

    companion object {
        const val PRIVACY_SHOW_ABOUTME = "showAboutMe"
        const val PRIVACY_SHOW_GARAGE = "showVehicles"
        const val PRIVACY_SHOW_GIFTS = "showGifts"
        const val PRIVACY_SHOW_PERSONAL_ROAD = "showPersonalRoad"
        const val PRIVACY_SHOW_MAP = "showOnMap"
        const val ALLOW_SHAKE_GESTURE = "allowShakeGesture"
        const val ALLOW_CONTACTS_SYNC = "allowContactSync"
        const val VALUE_ALL = 1
        const val VALUE_FRIENDS = 2
        const val NOBODY = "nobody"
        const val FRIENDS = "friends"
        const val ALL = "all"
        const val DATE_PATTERN = "dd.MM.yyyy"
        const val PEOPLE_PAGE_LIMIT = 20
        const val PEOPLE_OFFSET_LIMIT = 0
    }
}
