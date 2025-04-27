package com.numplates.nomera3.modules.newroads

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import com.meera.core.preferences.AppSettings
import com.meera.core.preferences.AppSettings.Companion.KEY_IS_ADD_NEW_POST_TOOLTIP_WAS_SHOWN_TIMES
import com.meera.core.preferences.AppSettings.Companion.KEY_IS_OPEN_ROAD_FILTER_WAS_SHOWN_TIMES
import com.meera.db.DataStore
import com.numplates.nomera3.App
import com.numplates.nomera3.BuildConfig
import com.numplates.nomera3.domain.interactornew.CheckMainFilterRecommendedUseCase
import com.numplates.nomera3.domain.interactornew.GetFriendsSubscribersPopupPrivacyUseCase
import com.numplates.nomera3.domain.interactornew.GetSubscribersPrivacyDialogShownRxUseCase
import com.numplates.nomera3.domain.interactornew.GetUserBirthdayDialogShownFlowUseCase
import com.numplates.nomera3.domain.interactornew.GetUserBirthdayDialogShownUseCase
import com.numplates.nomera3.domain.interactornew.ReadOnboardingUseCase
import com.numplates.nomera3.domain.interactornew.UpdateBirthdayShownUseCase
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.appDialogs.ui.DialogDismissListener
import com.numplates.nomera3.modules.appDialogs.ui.DismissDialogType
import com.numplates.nomera3.modules.baseCore.helper.HolidayInfoHelper
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereOpenMap
import com.numplates.nomera3.modules.baseCore.helper.amplitude.rec_system.AmplitudePropertyRecSystemChangeMethod
import com.numplates.nomera3.modules.baseCore.helper.amplitude.rec_system.AmplitudePropertyRecSystemType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.rec_system.AmplitudeRecSystemAnalytics
import com.numplates.nomera3.modules.baseCore.helper.amplitude.search.AmplitudeMainSearchAnalytics
import com.numplates.nomera3.modules.baseCore.helper.amplitude.search.toRoadSearchAmplitudeProperty
import com.numplates.nomera3.modules.bump.domain.usecase.TryToRegisterShakeEventUseCase
import com.numplates.nomera3.modules.maps.domain.usecase.GetMapSettingsUseCase
import com.numplates.nomera3.modules.newroads.data.PostsRepository
import com.numplates.nomera3.modules.newroads.data.entities.FilterSettings
import com.numplates.nomera3.modules.newroads.data.entities.SubscriptionNewPostEntity
import com.numplates.nomera3.modules.newroads.ui.entity.MainRoadMode
import com.numplates.nomera3.modules.newroads.util.FilterSettingsMapper
import com.numplates.nomera3.modules.registration.ui.AuthFinishListener
import com.numplates.nomera3.modules.tracker.FireBaseAnalytics
import com.numplates.nomera3.modules.user.ui.utils.UserBirthdayUtils
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserProfileModel
import com.numplates.nomera3.modules.userprofile.domain.usecase.GetOwnLocalProfileUseCase
import com.numplates.nomera3.modules.userprofile.domain.usecase.ObserveLocalOwnUserProfileModelUseCase
import com.numplates.nomera3.presentation.view.utils.apphints.TooltipDuration
import com.numplates.nomera3.presentation.viewmodel.BaseViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class MainPostRoadsViewModel : BaseViewModel() {

    @Inject
    lateinit var settings: AppSettings

    @Inject
    lateinit var amplitudeHelper: AnalyticsInteractor

    @Inject
    lateinit var fbAnalytic: FireBaseAnalytics

    @Inject
    lateinit var postsRepository: PostsRepository

    @Inject
    lateinit var getMapSettingsUseCase: GetMapSettingsUseCase

    @Inject
    lateinit var holidayInfoHelper: HolidayInfoHelper

    @Inject
    lateinit var authFinishListener: AuthFinishListener

    @Inject
    lateinit var getUserProfileUseCase: GetOwnLocalProfileUseCase

    @Inject
    lateinit var updateBirthdayShownUseCase: UpdateBirthdayShownUseCase

    @Inject
    lateinit var dialogDismissListener: DialogDismissListener

    @Inject
    lateinit var userBirthdayUtils: UserBirthdayUtils

    @Inject
    lateinit var getUserBirthdayDialogShownUseCase: GetUserBirthdayDialogShownUseCase

    @Inject
    lateinit var getFriendsSubscribersPopupPrivacyUseCase: GetFriendsSubscribersPopupPrivacyUseCase

    @Inject
    lateinit var getSubscribersPrivacyDialogShownRxUseCase: GetSubscribersPrivacyDialogShownRxUseCase

    @Inject
    lateinit var getUserBirthdayDialogShownFlowUseCase: GetUserBirthdayDialogShownFlowUseCase

    @Inject
    lateinit var amplitudeMainSearchAnalytics: AmplitudeMainSearchAnalytics

    @Inject
    lateinit var amplitudeRecSystemAnalytics: AmplitudeRecSystemAnalytics

    @Inject
    lateinit var readOnboardingUseCase: ReadOnboardingUseCase

    @Inject
    lateinit var tryToRegisterShakeEventUseCase: TryToRegisterShakeEventUseCase

    @Inject
    lateinit var filterSettingsMapper: FilterSettingsMapper

    @Inject
    lateinit var localProfileFlowUseCase: ObserveLocalOwnUserProfileModelUseCase

    @Inject
    lateinit var checkMainFilterRecommendedUseCase: CheckMainFilterRecommendedUseCase

    @Inject
    lateinit var dataStore: DataStore


    private val hasNewSubscriptionPost = MutableLiveData<SubscriptionNewPostEntity>()
    private val hasNewSubscriptionMoment = MutableLiveData<SubscriptionNewPostEntity>()

    val hasNewSubscriptionPostOrMoment = hasNewSubscriptionPost.asFlow()

    private val _mainPostRoadsEvents = MutableSharedFlow<MainPostRoadsEvent>()
    val mainPostRoadsEvents: SharedFlow<MainPostRoadsEvent> = _mainPostRoadsEvents

    private val _liveRoadMode = MutableLiveData(MainRoadMode.POSTS)
    val liveRoadMode: LiveData<MainRoadMode> = _liveRoadMode

    val disposables = CompositeDisposable()

    // isUserRegistered флаг нужен чтобы не показывать
    // подсказки раньше времени, в процессе регистрации
    val isUserRegistered: Boolean
        get() {
            return settings.isNewUserRegistered
        }

    // вместо live event для подсказок использовано свойство
    // isOpenRoadFilterTooltipWasShown , чтобы не перегружать
    // код модели и фрагмента лишней логикой. Если бы тут уже
    // были live events, то вполне можно было добавить к ним,
    // но из-за двух подсказок добавлять live event - сложно.
    var isOpenRoadFilterTooltipWasShown: Boolean
        get() {
            return settings.isOpenRoadFilterTooltipWasShown
        }
        set(value) {
            settings.isOpenRoadFilterTooltipWasShown = value
        }

    var isAddNewPostTooltipWasShown: Boolean
        get() {
            return settings.isAddNewPostTooltipWasShown
        }
        set(value) {
            settings.isAddNewPostTooltipWasShown = value
        }

    private var isOnBoardingCloseClicked = false
    private var isOnBoardingSwipedDown = false

    init {
        App.component.inject(this)
        initHasSubscriptionNewPost()
        initHasSubscriptionNewMoment()
        subscribeRegistrationListener()
        subscribeAuthListener()
        initDialogDismissListener()
    }

    val mapSettings = getMapSettingsUseCase.invoke()

    override fun onCleared() = disposables.clear()

    fun isRoadFilterEnabled() = settings.readMyGroupsFilter()

    fun getAccountType() = settings.readAccountType()

    fun getAccountColor() = settings.readAccountColor()

    fun isDarkMapStyle() = settings.readDarkMapStyle()

    fun setRoadMode(newMode: MainRoadMode) {
        if (newMode != _liveRoadMode.value) {
            _liveRoadMode.postValue(newMode)
        }
    }

    fun getUserProfileFlow() = localProfileFlowUseCase.invoke()

    /**
     * Подписка на индикатор новых постов для экрана "Подписки"
     */
    private fun initHasSubscriptionNewPost() {
        postsRepository.getSubscriptionsNewPostListenerUseCase.execute()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { subscriptionNewPostsEntity ->
                    hasNewSubscriptionPost.value = subscriptionNewPostsEntity
                },
                { error ->
                    Timber.e("ERROR: $error")
                }
            ).addTo(disposables)
    }

    private fun initHasSubscriptionNewMoment() {
        postsRepository.getSubscriptionsNewMomentListenerUseCase.execute()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { subscriptionNewPostsEntity ->
                    hasNewSubscriptionMoment.value = subscriptionNewPostsEntity
                },
                { error ->
                    Timber.e("ERROR: $error")
                }
            ).addTo(disposables)
    }

    fun clearMomentIndicator() {
        hasNewSubscriptionMoment.postValue(SubscriptionNewPostEntity(false))
    }

    fun isOpenRoadFilterTooltipWasShownTimes() =
        settings.isOpenRoadFilterTooltipWasShownTimes < TooltipDuration.OPEN_ROAD_FILTER_TIMES
            && settings.isShownTooltipSession(KEY_IS_OPEN_ROAD_FILTER_WAS_SHOWN_TIMES)

    fun incOpenRoadFilterTooltipWasShown() {
        val shownTimes = settings.isOpenRoadFilterTooltipWasShownTimes
        if (shownTimes > TooltipDuration.OPEN_ROAD_FILTER_TIMES) return
        settings.isOpenRoadFilterTooltipWasShownTimes = shownTimes + 1
        settings.markTooltipAsShownSession(KEY_IS_OPEN_ROAD_FILTER_WAS_SHOWN_TIMES)
    }

    fun isAddNewPostTooltipWasShownTimes() =
        settings.isAddNewPostTooltipWasShownTimes < TooltipDuration.DEFAULT_TIMES
            && settings.isShownTooltipSession(KEY_IS_ADD_NEW_POST_TOOLTIP_WAS_SHOWN_TIMES)

    fun incAddNewPostTooltipWasShown() {
        val shownTimes = settings.isAddNewPostTooltipWasShownTimes
        if (shownTimes > TooltipDuration.DEFAULT_TIMES) return
        settings.isAddNewPostTooltipWasShownTimes = shownTimes + 1
        settings.markTooltipAsShownSession(KEY_IS_ADD_NEW_POST_TOOLTIP_WAS_SHOWN_TIMES)
    }

    fun isNeedToShowOnBoarding() = settings.readNeedOnBoarding()

    /**
     * Just listens click on close btn. When clicked two times, onboarding should close
     * */
    fun onBoardingCloseClicked() {
        if (isOnBoardingCloseClicked) {
            onBoardingFinished()
        } else {
            isOnBoardingCloseClicked = true
        }
    }

    /**
     * Just listens swipes down. When clicked two times, onboarding should close
     * */
    fun onDownSwiped() {
        if (isOnBoardingSwipedDown) {
            onBoardingFinished()
        } else {
            isOnBoardingSwipedDown = true
        }
    }

    /**
     * Watched all onboarding and than click last btn or close btn
     * */
    fun onBoardingFinished() {
        settings.writeNeedOnBoarding(false)
        viewModelScope.launch { _mainPostRoadsEvents.emit(MainPostRoadsEvent.CloseOnBoarding) }
    }

    /**
     * Closed onboardiing
     * */
    suspend fun onBoardingClosed() {
        checkIsNeedBirthdayRequest()
    }

    fun onBoardingCollapsed() {
        viewModelScope.launch { _mainPostRoadsEvents.emit(MainPostRoadsEvent.OnBoardingCollapsed) }
    }

    fun logOpenMainFeed() {
        amplitudeHelper.logOpenMainFeed(recFeed = checkMainFilterRecommendedUseCase.invoke())
    }

    fun logOpenMap(whereOpenMap: AmplitudePropertyWhereOpenMap) {
        amplitudeHelper.logOpenMap(whereOpenMap)
    }

    fun updateBirthdayDialogShown() {
        viewModelScope.launch {
            try {
                updateBirthdayShownUseCase.invoke()
                settings.isNeedShowBirthdayDialog.set(false)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    fun logScreenForFragment(screenName: String) = fbAnalytic.logScreenForFragment(screenName)

    fun logOpenFollowFeed(hasPosts: Boolean) {
        amplitudeHelper.logOpenFollowFeed(hasPosts)
    }

    fun logOpenMainSearch(roadPosition: Int?) {
        val roadType = roadPosition?.toRoadSearchAmplitudeProperty() ?: return
        amplitudeMainSearchAnalytics.logOpenMainSearch(roadType)
    }

    suspend fun checkIsNeedBirthdayRequest() {
        val isNeedShowBirthdayDialog = getUserBirthdayDialogShownUseCase.invoke().get() ?: false
        if (isNeedShowBirthdayDialog) {
            getDateOfBirthState()
        }
    }

    /**
     * Получаем состояние Дня Рождения юзера
     */
    fun getDateOfBirthState() {
        Timber.d("Get birthday state!")
        viewModelScope.launch {
            try {
                val birthdayState = getUserProfileUseCase.invoke()
                handleBirthdayState(birthdayState)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    fun setNeedToRegisterShakeEvent(needToRegister: Boolean) {
        viewModelScope.launch {
            tryToRegisterShakeEventUseCase.invoke(needToRegister)
        }
    }

    fun logFilterMainRoad(filterSettings: FilterSettings) {
        val selectedCountriesForAnalytics =
            filterSettingsMapper.mapToAnalyticCountryData(filterSettings)
        val selectedCitiesForAnalytic =
            filterSettingsMapper.mapToAnalyticCityData(filterSettings)
        amplitudeHelper.logFilterMainRoad(
            selectedCountriesForAnalytics,
            selectedCitiesForAnalytic,
            checkMainFilterRecommendedUseCase.invoke()
        )
    }

    fun logFilterMainRoadRecChange(filterSettings: FilterSettings) {
        viewModelScope.launch(Dispatchers.IO) {
            amplitudeRecSystemAnalytics.logRecSystemChanged(
                how = AmplitudePropertyRecSystemChangeMethod.MANUALLY,
                type = when (filterSettings.data.isRecommended) {
                    true -> AmplitudePropertyRecSystemType.REC
                    else -> AmplitudePropertyRecSystemType.CHRON
                },
                userId = dataStore.userProfileDao().getUserProfile()?.userId ?: 0L
            )

            amplitudeRecSystemAnalytics.setUserPropertiesRecSystemChanged(filterSettings.data.isRecommended ?: false)
        }
    }

    private fun handleBirthdayState(userProfileModel: UserProfileModel?) {
        userProfileModel?.let { userProfile ->
            when {
                userBirthdayUtils.isBirthdayToday(userProfile.birthday) -> {
                    if (userBirthdayUtils.isDateAfter(DEFAULT_BIRTHDAY_AFTER_TIME)) {
                        emitFlowEvent(
                            MainPostRoadsEvent.ShowBirthdayDialog(isBirthdayToday = true)
                        )
                    }
                }

                userBirthdayUtils.isBirthdayYesterday(userProfile.birthday) -> {
                    if (userBirthdayUtils.isDateAfter(DEFAULT_BIRTHDAY_AFTER_TIME)) {
                        emitFlowEvent(
                            MainPostRoadsEvent.ShowBirthdayDialog(isBirthdayToday = false)
                        )
                    }
                }
            }
        }
    }

    private fun subscribeRegistrationListener() {
        viewModelScope.launch {
            authFinishListener.observeRegistrationFinishListener()
                .collect {
                    if (!settings.readNeedOnBoarding() && !settings.readOnBoardingWelcomeShowed()) {
                        _mainPostRoadsEvents.emit(MainPostRoadsEvent.ShowOnBoardingWelcome)
                        settings.writeOnBoardingWelcomeShowed()
                    } else {
                        _mainPostRoadsEvents.emit(MainPostRoadsEvent.CheckHolidays)
                    }
                }
        }
    }

    private fun subscribeAuthListener() {
        viewModelScope.launch {
            authFinishListener.observeAuthFinishListener()
                .collect {
                    observeBirthdayDialogNeedToShow()
                    observePrivacyDialogNeedToShow()
                }
        }
    }

    private fun observeBirthdayDialogNeedToShow() {
        viewModelScope.launch {
            runCatching {
                getUserBirthdayDialogShownFlowUseCase.invoke()
                    .collect { isNeedShowBirthdayDialog ->
                        if (isNeedShowBirthdayDialog == true) {
                            handleDialogQueue()
                        }
                    }
            }.onFailure {
                Timber.e(it)
            }
        }
    }

    private fun observePrivacyDialogNeedToShow() {
        viewModelScope.launch {
            runCatching {
                getSubscribersPrivacyDialogShownRxUseCase.invoke().asFlow().collect { isNeedShowPrivacyDialog ->
                    if (isNeedShowPrivacyDialog == true) {
                        handleDialogQueue()
                    }
                }
            }.onFailure {
                Timber.e(it)
            }
        }
    }

    /**
     * Необходимо показать (если нужно) диалог выбора настроек приватности "Подписки/Подписчики"
     * (он имееет наивысший приоритет)
     */
    private suspend fun handleDialogQueue() {
        val isNeedShowBirthdayDialog =
            getUserBirthdayDialogShownUseCase.invoke().get() ?: false
        val isNeedShowPrivacyDialog =
            getFriendsSubscribersPopupPrivacyUseCase.invoke().get() ?: false
        when {
            isNeedShowPrivacyDialog -> {
                emitFlowEvent(MainPostRoadsEvent.ShowSubscribersPrivacyDialog)
            }

            isNeedShowBirthdayDialog -> {
                getDateOfBirthState()
            }
        }
    }

    private fun isHolidayIntroduced(): Boolean {
        return settings.isHolidayIntroduced &&
            settings.holidayIntroducedVersion == BuildConfig.VERSION_NAME
    }

    private fun emitFlowEvent(event: MainPostRoadsEvent) {
        viewModelScope.launch {
            _mainPostRoadsEvents.emit(event)
        }
    }

    private fun initDialogDismissListener() {
        viewModelScope.launch {
            dialogDismissListener.sharedFlow.collect { type ->
                when (type) {
                    DismissDialogType.HOLIDAY,
                    DismissDialogType.FRIENDS_FOLLOWERS_PRIVACY -> {
                        checkIsNeedBirthdayRequest()
                    }

                    else -> {}
                }
            }
        }
    }

    companion object {
        private const val DEFAULT_BIRTHDAY_AFTER_TIME = "06:00"
    }
}
