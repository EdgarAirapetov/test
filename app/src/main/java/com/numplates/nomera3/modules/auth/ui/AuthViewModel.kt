package com.numplates.nomera3.modules.auth.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.meera.core.extensions.doDelayed
import com.meera.core.preferences.AppSettings
import com.numplates.nomera3.App
import com.numplates.nomera3.domain.interactornew.SetKeyboardHeightUseCase
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.appInfo.domain.usecase.GetAppInfoAsyncUseCase
import com.numplates.nomera3.modules.auth.AuthStatus
import com.numplates.nomera3.modules.auth.AuthUser
import com.numplates.nomera3.modules.auth.domain.AuthInitUseCase
import com.numplates.nomera3.modules.auth.domain.AuthLogoutUseCase
import com.numplates.nomera3.modules.auth.domain.AuthUserStateObserverUseCase
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyRegistrationStep
import com.numplates.nomera3.modules.registration.AuthType
import com.numplates.nomera3.modules.registration.ui.AuthFinishListener
import com.numplates.nomera3.modules.userprofile.domain.usecase.ObserveLocalOwnUserProfileModelUseCase
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class AuthViewModel : ViewModel() {

    private var completeCallback: ((Boolean) -> Unit)? = null
    private var authStatusInitCallback: (() -> Unit)? = null

    private var isAuthorized = false
    private var currentAuthStatus: AuthStatus = AuthStatus.Unspecified
        set(value) {
            if (field == AuthStatus.Unspecified && field != value) {
                field = value
                authStatusInitCallback?.invoke()
                authStatusInitCallback = null
            } else {
                field = value
            }

        }

    private var authStatusDisposable: Disposable? = null

    @Inject
    lateinit var authFinishListener: AuthFinishListener

    @Inject
    lateinit var authStateUseCase: AuthUserStateObserverUseCase

    @Inject
    lateinit var getAppInfoUseCase: GetAppInfoAsyncUseCase

    @Inject
    lateinit var authInitUseCase: AuthInitUseCase

    @Inject
    lateinit var authLogoutUseCase: AuthLogoutUseCase

    @Inject
    lateinit var amplitudeHelper: AnalyticsInteractor

    @Inject
    lateinit var observeLocalOwnUserProfileUseCase: ObserveLocalOwnUserProfileModelUseCase

    @Inject
    lateinit var appSettings: AppSettings

    @Inject
    lateinit var setKeyboardHeightUseCase: SetKeyboardHeightUseCase

    val authStatus = MutableLiveData<AuthStatus>()
    private val _authViewEvent = MutableSharedFlow<AuthViewEvent>()
    val authViewEvent: SharedFlow<AuthViewEvent> = _authViewEvent

    private val authStream: ReplaySubject<AuthUser>

    var navigationState = MutableLiveData<AuthNavigationState>(AuthNavigationState.None)
        private set

    var navigationEvent = PublishSubject.create<AuthNavigationEvent>()
        private set

    var rollbackIndex = 0
        private set

    var registrationStep = AmplitudePropertyRegistrationStep.REG_1
    private var maxMeasurementsKeyboardHeight = 0

    init {
        App.component.inject(this)
        authStream = authStateUseCase.getObserver()

        authStatusDisposable = authStream
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ onAuthChanged(it) }, { Timber.e(it) })
    }

    fun isWorthToShow() = appSettings.readIsWorthToShow()

    fun onAuthStatusInitialized(action: () -> Unit) {
        if (currentAuthStatus != AuthStatus.Unspecified) {
            action.invoke()
        } else {
            authStatusInitCallback = action
        }
    }

    fun needAuth(rollbackIndex: Int, completeCallback: ((Boolean) -> Unit)?): Boolean {
        if (navigationState.value != AuthNavigationState.None) {
            return false
        }

        this.rollbackIndex = rollbackIndex

        if (currentAuthStatus is AuthStatus.Authorized) {
            completeCallback?.invoke(false)
            return false
        }

        resetCallBacks()
        this.completeCallback = completeCallback
        emitAuthNeed()

        return true
    }

    fun isAuthorized(): Boolean {
        return isAuthorized
    }

    fun resetRegContainerInstance() =
        emitViewEvent(AuthViewEvent.ResetRegContainerInstance)

    /**
     * Пользователь нажал на крестик на первом экране авторизации и
     * завершает процедуру авторизации
     */
    fun backNavigatePhoneScreenByCloseButton() {
        amplitudeHelper.logRegistrationClose(registrationStep)
    }

    /**
     * Пользователь нажал "назад" на экране авторизации и
     * завершает процедуру авторизации
     */
    fun backNavigatePhoneScreenByBack() {
        publishEvent(AuthNavigationEvent.BackNavigatePhoneScreenByBack)
    }

    /**
     * Пользователь нажал "назад" на экране персональной информации и
     * завершает процедуру авторизации
     */
    fun backNavigatePersonalScreenByBack() {
        finishOnCancel()
    }

    fun socketLogout() {
        finishOnCancel()
    }

    /**
     * Пользователь нажал "назад" на экране восстановления и
     * завершает процедуру авторизации
     */
    fun backNavigateRecoveryScreenByBack() {
        restartAuth()
    }

    /**
     * Система навигации закрыла экраны авторизации и готова
     * к вызову коллбэка
     */
    fun onNavigationReady() {
        publishEvent(AuthNavigationEvent.Complete)
    }

    /**
     * Пользователь успешно прошёл все экраны авторизации и
     * запрашивается возврат на предыдущие экраны и вызов коллбэка
     */
    fun onAuthSuccess() {
        emitRollOut()
    }

    /**
     * Переход на экран SMS
     */
    fun navigateToSms(phoneNumber: String, login: String) {
        publishState(AuthNavigationState.Sms(phoneNumber, login))
    }

    /**
     * Перейти на экран ввода телефона/email
     */
    fun navigateToPhone() {
        publishState(AuthNavigationState.Phone)
    }

    /**
     * Перейти на экран ввода кода из смс
     * */
    fun navigateToConfirmCode(
        authType: AuthType,
        sendTo: String,
        countryCode: String?,
        countryName: String?,
        countryMask: String?,
        smsTimeout: Long?
    ) {
        publishState(
            AuthNavigationState.CodeConfirmation(
                authType,
                sendTo,
                countryCode,
                countryName,
                countryMask,
                smsTimeout
            )
        )
    }

    /**
     * Пользователь зарегистрировался и переводится на экран заполнения персональной информацией
     */
    fun navigateToPersonalInfo(countryName: String?) {
        publishState(AuthNavigationState.UserPersonalInfo(countryName))
    }

    /**
     * Последнее событие отрабатывающее при отмене авторизации
     */
    fun finishOnCancel() {
        publishState(AuthNavigationState.None)
        resetCallBacks()
        App.clearRegistrationComponent()
    }

    fun restartAuth() {
        finishOnCancel()
        doDelayed(DELAY_SHOWING_AUTH_MILLIS) {
            emitAuthNeed()
        }
    }

    fun recheckAuthStatusWithProfile() {
        viewModelScope.launch {
            authInitUseCase.init()
        }
    }

    /**
     * Последнее событие отрабатывающее при успешной авторизации
     */
    fun finishOnComplete() {
        publishState(AuthNavigationState.None)
        completeCallback?.invoke(true)
        resetCallBacks()
        App.clearRegistrationComponent()
    }


    fun logout(): String {
        val logoutToken = appSettings.readAccessToken()
        viewModelScope.launch(Dispatchers.IO) {
            authLogoutUseCase.logout()
        }

        return logoutToken
    }

    fun onAuthFinished() {
        viewModelScope.launch {
            authFinishListener.setAuthStatusChanged()
        }
    }

    fun onRegistrationFinished() {
        viewModelScope.launch {
            authFinishListener.setRegistrationStatusChanged()
        }
    }

    fun saveKeyboardHeight(height: Int) {
        if (height > 0 && height > maxMeasurementsKeyboardHeight) {
            setKeyboardHeightUseCase.invoke(height)
            maxMeasurementsKeyboardHeight = height
        }

    }

    private fun onAuthChanged(domainAuthUser: AuthUser) {
        val newStatus = domainAuthUser.getReadyAuthStatus()
        isAuthorized = domainAuthUser.authStatus is AuthStatus.Authorized
        currentAuthStatus = newStatus
        appSettings.isRegistrationCompleted = domainAuthUser.isRegistrationCompleted

        publishAuthStatus(currentAuthStatus)

        if (currentAuthStatus is AuthStatus.Authorized && isAuthProcessing()) {
            emitRollOut()
        }
    }

    private fun publishAuthStatus(status: AuthStatus) {
        if (status != authStatus.value) {
            authStatus.value = status
        }
    }

    private fun isAuthProcessing(): Boolean {
        return navigationState.value is AuthNavigationState.Phone
            || navigationState.value is AuthNavigationState.Sms
            || navigationState.value is AuthNavigationState.UserPersonalInfo
            || navigationState.value is AuthNavigationState.CodeConfirmation
    }

    private fun publishEvent(event: AuthNavigationEvent) {
        navigationEvent.onNext(event)
    }

    private fun publishState(state: AuthNavigationState) {
        navigationState.value = state
    }

    private fun emitAuthNeed() {
        publishEvent(AuthNavigationEvent.StartAuthProcess)
        navigateToPhone()
    }

    private fun emitRollOut() {
        val isFirstLogin = appSettings.firstLogin
        publishEvent(AuthNavigationEvent.RollOutToScreenBeforeAuth(isFirstLogin))
    }

    private fun emitViewEvent(typeEvent: AuthViewEvent) {
        viewModelScope.launch {
            _authViewEvent.emit(typeEvent)
        }
    }

    private fun resetCallBacks() {
        completeCallback = null
    }

    fun getOwnUserId() = appSettings.readUID()

    fun getUserProfileLive() = observeLocalOwnUserProfileUseCase.invoke().asLiveData()

    override fun onCleared() {
        super.onCleared()
        authStatusDisposable?.dispose()
    }

    companion object {
        private const val DELAY_SHOWING_AUTH_MILLIS = 100L
    }
}
