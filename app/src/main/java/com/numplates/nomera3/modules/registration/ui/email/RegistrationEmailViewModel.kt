package com.numplates.nomera3.modules.registration.ui.email

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.meera.core.network.utils.BaseUrlManager
import com.numplates.nomera3.App
import com.numplates.nomera3.domain.interactornew.GetKeyboardHeightUseCase
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.auth.data.SendCodeErrors
import com.numplates.nomera3.modules.baseCore.domain.delegate.SendEmailCodeDelegate
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyHelpPressedWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyInputType
import com.numplates.nomera3.modules.registration.AuthType
import com.numplates.nomera3.modules.registration.data.RegistrationCountryCodeMapper
import com.numplates.nomera3.modules.registration.domain.GetSignupCountriesUseCase
import com.numplates.nomera3.modules.registration.domain.LoadSignupCountriesUseCase
import com.numplates.nomera3.modules.registration.domain.SetLastSmsCodeTimeUseCase
import com.numplates.nomera3.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class RegistrationEmailViewModel : BaseViewModel() {

    val liveData = MutableLiveData<RegistrationEmailViewEvent>()
    val progressLiveData = MutableLiveData<Boolean>()
    val continueButtonAvailabilityLiveData = MutableLiveData(false)

    @Inject
    lateinit var tracker: AnalyticsInteractor

    @Inject
    lateinit var baseUrlManager: BaseUrlManager

    @Inject
    lateinit var sendEmailCodeDelegate: SendEmailCodeDelegate

    @Inject
    lateinit var getSignupCountriesUseCase: GetSignupCountriesUseCase

    @Inject
    lateinit var loadSignupCountriesUseCase: LoadSignupCountriesUseCase

    @Inject
    lateinit var getKeyboardHeightUseCase: GetKeyboardHeightUseCase

    @Inject
    lateinit var registrationCountryCodeMapper: RegistrationCountryCodeMapper

    @Inject
    lateinit var setLastSmsCodeTimeUseCase: SetLastSmsCodeTimeUseCase

    var authType: AuthType = AuthType.Phone

    private var isResendCode = false

    init {
        App.component.inject(this)
    }

    fun saveLastSmsCodeTime() = setLastSmsCodeTimeUseCase.invoke()

    fun setEmail(email: String) {
        sendEmailCodeDelegate.setEmail(email)
        checkEmail()
    }

    fun checkContinueAvailability() {
       when (authType) {
           else -> checkEmail()
       }
    }

    fun getKbHeight() = getKeyboardHeightUseCase.invoke()

    fun continueClicked() {
        when (authType) {
            is AuthType.Email -> sendEmail()
            else -> {}
        }
    }

    fun resendCode(authType: AuthType, sendTo: String) {
        isResendCode = true
        when (authType) {
            is AuthType.Email -> {
                setEmail(sendTo)
                sendEmail()
            }
            else -> {}
        }
    }

    fun helpClicked(where: AmplitudePropertyHelpPressedWhere) {
        tracker.logRegistrationHelpPressed(where)
    }

    fun clearAll() {
        event(RegistrationEmailViewEvent.None)
    }

    private fun checkEmail() {
        val isValidationSuccess = isEmailTextValid()
        continueEnable(isValidationSuccess)
    }

    private fun isEmailTextValid() =
        sendEmailCodeDelegate.validateEmail()

    private fun sendEmail() {
        progressLiveData.postValue(true)
        viewModelScope.launch {
            sendEmailCodeDelegate.sendEmailCode(
                success = { isSuccess, email ->
                    logAuthEvent(AmplitudePropertyInputType.EMAIL)
                    handleSuccessSendCodeResult(isSuccess, email)
                },
                fail = ::handleFailSendCodeResult
            )
        }
    }

    private fun handleSuccessSendCodeResult(isSuccess: Boolean, sendTo: String) {
        progressLiveData.postValue(false)
        if (isSuccess) {
            event(
                RegistrationEmailViewEvent.SendCodeSuccess(
                    sendTo = sendTo,
                    authType = authType,
                )
            )
        }
        else event(RegistrationEmailViewEvent.Error.SendCodeFailed)
    }

    private fun handleFailSendCodeResult(error: SendCodeErrors) {
        progressLiveData.postValue(false)
        when (error) {
            is SendCodeErrors.UserIsBlockedWithoutHideContent ->
                event(
                    RegistrationEmailViewEvent.Error.UserBlocked(
                        error.reason, error.blockExpired
                    )
                )
            is SendCodeErrors.UserIsBlockedWithHideContent ->
                event(
                    RegistrationEmailViewEvent.Error.UserBlocked(
                        error.reason, null
                    )
                )
            is SendCodeErrors.UserNotFound ->
                event(
                    RegistrationEmailViewEvent.Error.UserNotFound(
                        error.reason
                    )
                )
            else -> event(RegistrationEmailViewEvent.Error.SendCodeFailed)
        }
    }

    private fun event(event: RegistrationEmailViewEvent) {
        liveData.postValue(event)
    }

    private fun continueEnable(enabled: Boolean) {
        continueButtonAvailabilityLiveData.postValue(enabled)
    }

    private fun logAuthEvent(property: AmplitudePropertyInputType) {
        if (isResendCode) return
        tracker.logRegistration(inputType = property, email = sendEmailCodeDelegate.getEmail())
    }
}
