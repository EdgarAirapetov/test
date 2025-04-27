package com.numplates.nomera3.modules.registration.ui.phoneemail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.meera.core.network.utils.BaseUrlManager
import com.numplates.nomera3.App
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.auth.data.SendCodeErrors
import com.numplates.nomera3.modules.baseCore.domain.delegate.SendEmailCodeDelegate
import com.numplates.nomera3.modules.baseCore.domain.delegate.SendPhoneCodeDelegate
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyHelpPressedWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyInputType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.auth.AmplitudeAuthAnalytic
import com.numplates.nomera3.modules.baseCore.helper.amplitude.auth.AmplitudeCodeRepeatRequestProperty
import com.numplates.nomera3.modules.registration.AuthType
import com.numplates.nomera3.modules.registration.data.RegistrationCountryCodeMapper
import com.numplates.nomera3.modules.registration.domain.GetSignupCountriesUseCase
import com.numplates.nomera3.modules.registration.domain.LoadSignupCountriesUseCase
import com.numplates.nomera3.modules.registration.domain.SetLastSmsCodeTimeUseCase
import com.numplates.nomera3.modules.registration.domain.model.RegistrationCountryModel
import com.numplates.nomera3.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

private const val DEFAULT_COUNTRY_CODE_LENGTH = 2

class RegistrationPhoneEmailViewModel : BaseViewModel() {

    val liveData = MutableLiveData<RegistrationPhoneEmailViewEvent>()
    val progressLiveData = MutableLiveData<Boolean>()
    val continueButtonAvailabilityLiveData = MutableLiveData(false)

    @Inject
    lateinit var tracker: AnalyticsInteractor

    @Inject
    lateinit var baseUrlManager: BaseUrlManager

    @Inject
    lateinit var sendEmailCodeDelegate: SendEmailCodeDelegate

    @Inject
    lateinit var sendPhoneCodeDelegate: SendPhoneCodeDelegate

    @Inject
    lateinit var getSignupCountriesUseCase: GetSignupCountriesUseCase

    @Inject
    lateinit var loadSignupCountriesUseCase: LoadSignupCountriesUseCase

    @Inject
    lateinit var registrationCountryCodeMapper: RegistrationCountryCodeMapper

    @Inject
    lateinit var setLastSmsCodeTimeUseCase: SetLastSmsCodeTimeUseCase

    @Inject
    lateinit var amplitudeAuthAnalytic : AmplitudeAuthAnalytic

    var authType: AuthType = AuthType.Phone
    var countryName: String? = null
    private var isResendCode = false

    init {
        App.component.inject(this)
        loadCountries()
    }

    fun saveLastSmsCodeTime() = setLastSmsCodeTimeUseCase.invoke()

    fun setPhone(phone: String) {
        sendPhoneCodeDelegate.setPhone(phone)
        checkPhone()
    }

    fun setEmail(email: String) {
        sendEmailCodeDelegate.setEmail(email)
        checkEmail()
    }

    fun checkContinueAvailability() {
       when (authType) {
           is AuthType.Phone -> checkPhone()
           else -> checkEmail()
       }
    }

    fun continueClicked() {
        when (authType) {
            is AuthType.Phone -> sendPhone()
            is AuthType.Email -> sendEmail()
        }
    }

    fun resendCode(authType: AuthType, sendTo: String) {
        isResendCode = true
        when (authType) {
            is AuthType.Phone -> {
                setPhone(sendTo)
                sendPhone()
                amplitudeAuthAnalytic.codeRepeatRequest(
                    regType = AmplitudeCodeRepeatRequestProperty.NUMBER.property,
                    countryNumber = registrationCountryCodeMapper.translateCountryNameRuToEn(countryName ?: ""),
                    number = sendTo,
                    email = "0"
                )
            }

            is AuthType.Email -> {
                setEmail(sendTo)
                sendEmail()
                amplitudeAuthAnalytic.codeRepeatRequest(
                    regType = AmplitudeCodeRepeatRequestProperty.EMAIL.property,
                    countryNumber = AmplitudeCodeRepeatRequestProperty.EMAIL.property,
                    number = "0",
                    email = sendTo
                )
            }
        }
    }

    fun helpClicked(where: AmplitudePropertyHelpPressedWhere) {
        tracker.logRegistrationHelpPressed(where)
    }

    fun clearAll(showKeyboard:Boolean = true) {
        val country = sendPhoneCodeDelegate.getCountry()
        val countryCodeLength = country?.code?.length ?: DEFAULT_COUNTRY_CODE_LENGTH
        val phone = sendPhoneCodeDelegate.getPhone()?.drop(countryCodeLength)
        event(
            RegistrationPhoneEmailViewEvent.None(
                authType = authType,
                phone = phone,
                country = country,
                showKeyboard=showKeyboard,
            )
        )
    }

    fun newCountryChosen(chosenCountry: RegistrationCountryModel) {
        sendPhoneCodeDelegate.currentCountry = chosenCountry
        event(RegistrationPhoneEmailViewEvent.CountryDetected(chosenCountry))
    }

    private fun loadCountries() {
        viewModelScope.launch {
            runCatching {
                getSignupCountriesUseCase.invoke()
                    .collectLatest {
                        handleCountries(it)
                    }
            }.onFailure {
                Timber.e(it)
                reloadCountries()
            }
        }
    }

    private fun handleCountries(countries: List<RegistrationCountryModel>) {
        if (countries.isEmpty()) reloadCountries()
        val currentCode = registrationCountryCodeMapper.getCountryCode()
        sendPhoneCodeDelegate.currentCountry = if (currentCode != null) {
            val currentCountryName = registrationCountryCodeMapper.getCountryNameByCode(currentCode)
            if (currentCountryName != null) {
                countries.firstOrNull { country ->
                    country.name == currentCountryName
                }
            } else {
                countries.firstOrNull { it.name == RegistrationCountryCodeMapper.DEFAULT_COUNTRY_NAME }
                    ?: countries.firstOrNull()
            }
        } else {
            countries.firstOrNull { it.name == RegistrationCountryCodeMapper.DEFAULT_COUNTRY_NAME }
                ?: countries.firstOrNull()
        } ?: countries.firstOrNull()
        sendPhoneCodeDelegate.currentCountry?.let { event(RegistrationPhoneEmailViewEvent.CountryDetected(it)) }
    }

    private fun reloadCountries() {
        viewModelScope.launch {
            runCatching { loadSignupCountriesUseCase.invoke() }
                .onFailure { Timber.d(it) }
        }
    }

    private fun checkPhone() {
        setPhoneValidationState()
    }

    private fun checkEmail() {
        val isValidationSuccess = isEmailTextValid()
        continueEnable(isValidationSuccess)
    }

    private fun setPhoneValidationState() {
        when {
            !isPhoneTextValid() -> continueEnable(false)
            else -> continueEnable(true)
        }
    }

    private fun isEmailTextValid() =
        sendEmailCodeDelegate.validateEmail()

    private fun isPhoneTextValid() =
        sendPhoneCodeDelegate.validatePhone()

    private fun sendPhone() {
        progressLiveData.postValue(true)
        viewModelScope.launch {
            sendPhoneCodeDelegate.sendPhoneCode(
                success = { isSuccess, timeout, blockTime, phone ->
                    logAuthEvent(AmplitudePropertyInputType.NUMBER)
                    handleSuccessSendCodeResult(isSuccess, timeout, blockTime, phone)
                },
                fail = { e ->
                    handleFailSendCodeResult(e)
                }
            )
        }
    }

    private fun sendEmail() {
        progressLiveData.postValue(true)
        viewModelScope.launch {
            sendEmailCodeDelegate.sendEmailCode(
                success = { isSuccess, email ->
                    logAuthEvent(AmplitudePropertyInputType.EMAIL)
                    handleSuccessSendCodeResult(isSuccess, null, null, email)
                },
                fail = ::handleFailSendCodeResult
            )
        }
    }

    private fun handleSuccessSendCodeResult(isSuccess: Boolean, smsTimeout: Long?, blockTime: Long?, sendTo: String) {
        progressLiveData.postValue(false)
        if (isSuccess) {
            event(
                RegistrationPhoneEmailViewEvent.SendCodeSuccess(
                    sendTo = sendTo,
                    authType = authType,
                    timeout = smsTimeout,
                    blockTime = blockTime,
                    countryCode = sendPhoneCodeDelegate.currentCountry?.code,
                    countryName = sendPhoneCodeDelegate.currentCountry?.name,
                    countryMask = sendPhoneCodeDelegate.currentCountry?.mask
                )
            )
        }
        else event(RegistrationPhoneEmailViewEvent.Error.SendCodeFailed)
    }

    private fun handleFailSendCodeResult(error: SendCodeErrors) {
        progressLiveData.postValue(false)
        when (error) {
            is SendCodeErrors.UserIsBlockedWithoutHideContent ->
                event(RegistrationPhoneEmailViewEvent.Error.UserBlocked(
                        error.reason, error.blockExpired))
            is SendCodeErrors.UserIsBlockedWithHideContent ->
                event(RegistrationPhoneEmailViewEvent.Error.UserBlocked(
                        error.reason, null))
            else -> event(RegistrationPhoneEmailViewEvent.Error.SendCodeFailed)
        }
    }

    private fun event(event: RegistrationPhoneEmailViewEvent) {
        liveData.postValue(event)
    }

    private fun continueEnable(enabled: Boolean) {
        continueButtonAvailabilityLiveData.postValue(enabled)
    }

    private fun logAuthEvent(property: AmplitudePropertyInputType) {
        if (isResendCode) return
        tracker.logRegistration(
            inputType = property,
            country = sendPhoneCodeDelegate.currentCountry?.name,
            number = sendPhoneCodeDelegate.getPhone(),
            email = sendEmailCodeDelegate.getEmail()
        )
    }

    fun setCountryNumber(countryName: String?) {
        this.countryName = countryName
    }
}
