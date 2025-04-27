package com.numplates.nomera3.modules.registration.ui.code

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.App
import com.numplates.nomera3.modules.auth.data.AuthenticationErrors
import com.numplates.nomera3.modules.auth.domain.AuthAuthenticateEmailParams
import com.numplates.nomera3.modules.auth.domain.AuthAuthenticateEmailUseCase
import com.numplates.nomera3.modules.auth.domain.AuthAuthenticatePhoneParams
import com.numplates.nomera3.modules.auth.domain.AuthAuthenticatePhoneUseCase
import com.numplates.nomera3.modules.registration.AuthType
import com.numplates.nomera3.modules.registration.ui.AuthFinishListener
import com.meera.core.preferences.AppSettings
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.registration.ui.FirebasePushSubscriberDelegate
import com.numplates.nomera3.modules.userprofile.domain.usecase.ObserveLocalOwnUserProfileModelUseCase
import com.numplates.nomera3.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RegistrationCodeViewModel : BaseViewModel() {

    val liveData = MutableLiveData<RegistrationCodeViewState>()
    val progressLiveData = MutableLiveData<Boolean>()

    private val _regCodeViewEvent = MutableSharedFlow<RegistrationCodeViewEvent>()
    val regCodeViewEvent: SharedFlow<RegistrationCodeViewEvent> = _regCodeViewEvent

    @Inject
    lateinit var authEmailUseCase: AuthAuthenticateEmailUseCase

    @Inject
    lateinit var authPhoneUseCase: AuthAuthenticatePhoneUseCase

    @Inject
    lateinit var tracker: AnalyticsInteractor

    @Inject
    lateinit var appSettings: AppSettings

    @Inject
    lateinit var authFinishListener: AuthFinishListener

    @Inject
    lateinit var observeLocalOwnUserProfileUseCase: ObserveLocalOwnUserProfileModelUseCase

    @Inject
    lateinit var firebasePushSubscriberDelegate: FirebasePushSubscriberDelegate

    //analytics params
    //когда был отправлен запрос на смс или мейл (запрос на код)
    private var startTime: Long? = null
    //сколько раз был введен не верный код
    private var incorrectCount: Int = 0
    //сколько раз мы повторно запрашивали код
    private var requestCodeTimes: Int = 0
    private val DEFAULT_TIME = "00.00.00"

    init {
        App.component.inject(this)
    }

    fun getUserProfileLive() = observeLocalOwnUserProfileUseCase.invoke().asLiveData()

    fun start() {
        if (startTime == null) startTime = System.currentTimeMillis()
    }

    fun saveLastSmsCodeTime() = appSettings.writeLastSmsCodeTime(System.currentTimeMillis() - 120L * 1000L)

    fun incSendCodeTimes() {
        startTime = System.currentTimeMillis()
        requestCodeTimes ++
    }

    fun setIsNeedShowHoliday(isNeed: Boolean) {
        appSettings.isHolidayShowNeeded = isNeed
    }

    fun setCode(authType: AuthType, sendTo: String?, code: String?, countryName: String?) {
        if (code?.length != CODE_LENGTH || sendTo == null) return
        progressLiveData.postValue(true)
        when (authType) {
            is AuthType.Phone -> authenticatePhone(sendTo, code, countryName)
            is AuthType.Email -> authenticateEmail(sendTo, code)
        }
    }

    fun authenticatePhone(sendTo: String, code: String, countryName: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            authPhoneUseCase.execute(
                params = AuthAuthenticatePhoneParams(phone = sendTo, code = code),
                success = { handleAuthenticationSuccess(it, country = countryName, number = sendTo, email = null) },
                fail = ::handleAuthenticationError
            )
        }
    }

    fun authenticateEmail(sendTo: String, code: String) {
        viewModelScope.launch(Dispatchers.IO) {
            authEmailUseCase.execute(
                    params = AuthAuthenticateEmailParams(email = sendTo, code = code),
                    success = { handleAuthenticationSuccess(it, country = null,number = null, email = sendTo) },
                    fail = ::handleAuthenticationError
            )
        }
    }

    fun subscribePush() = firebasePushSubscriberDelegate.subscribePush()

    fun logLoginFinished() {
        tracker.logLoginFinished()
    }

    fun isWorthToShowCallEnableFragment() = appSettings.readIsWorthToShow()

    fun onAuthFinished() {
        viewModelScope.launch {
            authFinishListener.setAuthStatusChanged()
        }
    }

    private fun handleAuthenticationSuccess(isSuccess: Boolean, country: String?, number: String?, email: String?) {
        progressLiveData.postValue(false)
        if (isSuccess) {
            logCodeEnter(country, number, email)
            setUiState(RegistrationCodeViewState.ClearInputTextViewState)
            emitViewEvent(RegistrationCodeViewEvent.AuthenticationSuccess)
        } else {
            setUiState(RegistrationCodeViewState.AuthenticationFailed())
        }
    }

    private fun handleAuthenticationError(error: AuthenticationErrors) {
        progressLiveData.postValue(false)
        when (error) {
            is AuthenticationErrors.AuthenticateErrorExt,
            is AuthenticationErrors.AuthenticateError -> {
                incorrectCount++
                setUiState(RegistrationCodeViewState.Error.IncorrectCode)
            }
            else -> setUiState(RegistrationCodeViewState.Error.NetworkError)
        }
    }

    private fun logCodeEnter(
        country: String?, number: String?, email: String?
    ) {
        val timeSpend = startTime?.let {
            val timeLong = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - it)
            val hours = timeLong / 3600
            val minutes = (timeLong % 3600) / 60
            val seconds = timeLong % 60
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } ?: kotlin.run {
            DEFAULT_TIME
        }
        tracker.logCodeEnter(
            inputTime = timeSpend,
            incorrectCount = incorrectCount,
            requestCount = requestCodeTimes,
            country = country,
            number = number,
            email = email
        )
    }

    private fun setUiState(event: RegistrationCodeViewState) {
        liveData.postValue(event)
    }

    private fun emitViewEvent(event: RegistrationCodeViewEvent) {
        viewModelScope.launch {
            _regCodeViewEvent.emit(event)
        }
    }

    companion object {
        const val CODE_LENGTH = 6
    }
}
