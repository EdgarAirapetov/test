package com.numplates.nomera3.modules.onboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meera.core.preferences.AppSettings
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.appDialogs.ui.DialogDismissListener
import com.numplates.nomera3.modules.appDialogs.ui.DismissDialogType
import com.numplates.nomera3.modules.auth.data.SendCodeErrors
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.baseCore.domain.delegate.SendPhoneCodeDelegate
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyHelpPressedWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyInputType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.onboarding.AmplitudeOnBoarding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.onboarding.AmplitudeOnBoardingEventName
import com.numplates.nomera3.modules.onboarding.domain.GetOnBoardingLastStepUseCase
import com.numplates.nomera3.modules.onboarding.domain.GetOnboardingTypeUseCase
import com.numplates.nomera3.modules.onboarding.domain.GetOnboardingWelcomeShowedUseCase
import com.numplates.nomera3.modules.onboarding.domain.OnBoardingStepDefParams
import com.numplates.nomera3.modules.onboarding.domain.SetOnBoardingLastStep
import com.numplates.nomera3.modules.onboarding.domain.SetOnboardingTypeParams
import com.numplates.nomera3.modules.onboarding.domain.SetOnboardingTypeUseCase
import com.numplates.nomera3.modules.registration.data.RegistrationCountryCodeMapper
import com.numplates.nomera3.modules.registration.domain.GetSignupCountriesUseCase
import com.numplates.nomera3.modules.registration.domain.LoadSignupCountriesUseCase
import com.numplates.nomera3.modules.registration.domain.model.RegistrationCountryModel
import com.numplates.nomera3.modules.registration.ui.AuthFinishListener
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class OnboardingViewModel @Inject constructor(
    private val getOnboardingTypeUseCase: GetOnboardingTypeUseCase,
    private val setOnboardingTypeUseCase: SetOnboardingTypeUseCase,
    private val getLastStepUseCase: GetOnBoardingLastStepUseCase,
    private val setLastStep: SetOnBoardingLastStep,
    private val analyticsInteractor: AnalyticsInteractor,
    private val amplitudeOnBoarding: AmplitudeOnBoarding,
    private val dismissDialogDismissListener: DialogDismissListener,
    private val authFinishListener: AuthFinishListener,
    private val appSettings: AppSettings,
    private val sendPhoneCodeDelegate: SendPhoneCodeDelegate,
    private val getSignupCountriesUseCase: GetSignupCountriesUseCase,
    private val loadSignupCountriesUseCase: LoadSignupCountriesUseCase,
    private val registrationCountryCodeMapper: RegistrationCountryCodeMapper,
    private val getOnboardingWelcomeShowedUseCase: GetOnboardingWelcomeShowedUseCase
) : ViewModel() {

    private val _stateLiveData = MutableLiveData<OnboardingViewState>()
    val stateLiveData: LiveData<OnboardingViewState> = _stateLiveData

    private val _isRegistrationCompleted = MutableLiveData<Boolean>()
    val isRegistrationCompleted: LiveData<Boolean> = _isRegistrationCompleted

    private var lastStep: OnboardingStep? = null
    private val steps = mutableListOf<OnboardingStep>()
    private var step: OnboardingStep = OnboardingStep.STEP_JOIN
    private var type = OnboardingType.INITIAL

    private val _onBoardingViewEvent = MutableSharedFlow<OnboardingViewEvent>()
    val onBoardingViewEvent: SharedFlow<OnboardingViewEvent> = _onBoardingViewEvent

    /**
     * For analytics
     * */
    private var closeClicked = false
    private var wasNotOnBoardingCollapsed = true

    private var swipedPosition = 0

    init {
        viewModelScope.launch {
            getOnboardingTypeUseCase.execute(
                params = DefParams(),
                success = { type = it },
                { Timber.e(it) }
            )
            getLastStepUseCase.execute(
                DefParams(),
                success = {
                    step = it
                    lastStep = it
                },
                fail = { Timber.e(it) }
            )
        }
        initSteps()
        initListeners()
        loadCountries()
    }

    fun setPhone(phone: String) {
        sendPhoneCodeDelegate.setPhone(phone)
        val isPhoneValid = sendPhoneCodeDelegate.validatePhone()
        emitViewState(OnboardingViewState.SetContinueButtonAvailable(isPhoneValid))
    }

    fun helpClicked(where: AmplitudePropertyHelpPressedWhere) {
        analyticsInteractor.logRegistrationHelpPressed(where)
    }

    fun setCurrentStep(step: OnboardingStep) {
        viewModelScope.launch {
            setLastStep.execute(OnBoardingStepDefParams(step))
        }
    }

    fun newCountryChosen(chosenCountry: RegistrationCountryModel) {
        sendPhoneCodeDelegate.currentCountry = chosenCountry
        emitViewState(OnboardingViewState.CurrentCountry(chosenCountry))
    }

    fun doAction(action: OnboardingViewAction) {
        when (action) {
            is OnboardingViewAction.Setup -> emitViewState(OnboardingViewState.Setup(step, steps))
            is OnboardingViewAction.StartRegistration -> {
                amplitudeOnBoarding.onEnterClicked(
                    eventName = AmplitudeOnBoardingEventName.JOIN,
                    afterContinue = wasNotOnBoardingCollapsed
                )
                emitViewEvent(OnboardingViewEvent.StartRegistration)
            }
        }
    }

    fun registrationCompleted(isNeedToShowWelcomeScreen: Boolean) {
        viewModelScope.launch {
            getLastStepUseCase.execute(
                DefParams(),
                success = { lastStep = it },
                fail = { Timber.e(it) }
            )

            type = if (lastStep == OnboardingStep.STEP_JOIN_PHONE || isNeedToShowWelcomeScreen) {
                OnboardingType.REGISTER_ON_END
            } else {
                OnboardingType.REGISTER_ON_START
            }

            step = OnboardingStep.STEP_WELCOME
            lastStep = OnboardingStep.STEP_WELCOME

            setLastStep.execute(OnBoardingStepDefParams(step))
            setOnboardingTypeUseCase.execute(SetOnboardingTypeParams(type), {}, {})
            emitViewState(OnboardingViewState.Setup(step, steps))
            initSteps()
        }
    }

    fun getLastStep() = lastStep ?: OnboardingStep.STEP_JOIN

    fun onCloseBtnClicked(currentStep: OnboardingStep) {
        closeClicked = true
        amplitudeOnBoarding.onCloseBtnClicked(
            getAmplitudeEventName(currentStep),
            wasNotOnBoardingCollapsed
        )
        wasNotOnBoardingCollapsed = false
    }

    fun onContinueClicked() {
        amplitudeOnBoarding.onContinueClicked()
    }

    fun closeOnboardingIfShowed() {
        if (getOnboardingWelcomeShowedUseCase.invoke()) {
            onBoardingFinished()
        }
    }

    fun onDownSwiped(currentStep: OnboardingStep) {
        amplitudeOnBoarding.onDownSwiped(
            getAmplitudeEventName(currentStep),
            wasNotOnBoardingCollapsed
        )
        wasNotOnBoardingCollapsed = false
    }

    fun onNextStepClicked(currentStep: OnboardingStep) {
        when (currentStep) {
            OnboardingStep.STEP_JOIN -> amplitudeOnBoarding.onAfterClicked(wasNotOnBoardingCollapsed)
            else -> amplitudeOnBoarding.onNextClicked(
                getAmplitudeEventName(currentStep),
                wasNotOnBoardingCollapsed
            )
        }
    }

    fun onEnterClicked() {
        amplitudeOnBoarding.onEnterClicked(
            AmplitudeOnBoardingEventName.JOIN_PHONE,
            wasNotOnBoardingCollapsed
        )
        viewModelScope.launch {
            sendPhoneCodeDelegate.sendPhoneCode(
                success = { success, timeout, blockTime, phone ->
                    analyticsInteractor.logRegistration(
                        inputType = AmplitudePropertyInputType.NUMBER,
                        country = sendPhoneCodeDelegate.currentCountry?.name,
                        number = phone,
                        email = null
                    )
                    handleSuccessSendCodeResult(success, timeout, phone)
                },
                fail = ::handleFailSendCodeResult
            )
        }
    }

    fun onSwiped(position: Int, onboardingStep: OnboardingStep) {
        if (position >= swipedPosition) {
            amplitudeOnBoarding.onSwiped(
                getAmplitudeEventName(onboardingStep),
                wasNotOnBoardingCollapsed
            )
        }
        swipedPosition = position
    }

    fun welcomeStepShowed() = appSettings.writeOnBoardingWelcomeShowed()

    private fun initListeners() {
        viewModelScope.launch {
            dismissDialogDismissListener.sharedFlow.collect { type ->
                if (type == DismissDialogType.CALL_ENABLE) {
                    setRegistrationFinished()
                }
            }
        }

        authFinishListener.observeAuthFinishListener()
            .onEach {
                onBoardingFinished()
            }
            .launchIn(viewModelScope)

        authFinishListener.observeRegistrationFinishListener()
            .onEach {
                setRegistrationFinished()
            }
            .launchIn(viewModelScope)
    }

    private fun initSteps() {
        steps.clear()
        when (type) {
            OnboardingType.REGISTER_ON_START -> {
                steps.add(OnboardingStep.STEP_WELCOME)
                steps.add(OnboardingStep.STEP_EVENTS)
                steps.add(OnboardingStep.STEP_PEOPLE_NEAR)
                steps.add(OnboardingStep.STEP_CONNECT)
            }
            OnboardingType.REGISTER_ON_END -> {
                steps.add(OnboardingStep.STEP_WELCOME)
            }
            OnboardingType.INITIAL -> {
                steps.add(OnboardingStep.STEP_JOIN)
                steps.add(OnboardingStep.STEP_EVENTS)
                steps.add(OnboardingStep.STEP_PEOPLE_NEAR)
                steps.add(OnboardingStep.STEP_CONNECT)
                steps.add(OnboardingStep.STEP_JOIN_PHONE)
            }
        }
        step = steps[0]
    }

    private fun handleSuccessSendCodeResult(isSuccess: Boolean, timeout:Long?, sendTo: String) {
        if (isSuccess) emitViewEvent(
            OnboardingViewEvent.SendCodeSuccess(
                sendTo,
                timeout,
                sendPhoneCodeDelegate.currentCountry
            )
        )
    }

    private fun handleFailSendCodeResult(error: SendCodeErrors) {
        when (error) {
            is SendCodeErrors.UserIsBlockedWithoutHideContent ->
                emitViewEvent(
                    OnboardingViewEvent.Error.UserBlocked(
                    error.reason, error.blockExpired))
            is SendCodeErrors.UserIsBlockedWithHideContent ->
                emitViewEvent(
                    OnboardingViewEvent.Error.UserBlocked(
                        error.reason, null
                    )
                )
            else -> {}
        }
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
                countries.first { country ->
                    country.name == currentCountryName
                }
            } else {
                countries.firstOrNull { it.name == RegistrationCountryCodeMapper.DEFAULT_COUNTRY_NAME }
                    ?: countries.firstOrNull()
            }
        } else {
            countries.firstOrNull { it.name == RegistrationCountryCodeMapper.DEFAULT_COUNTRY_NAME }
                ?: countries.firstOrNull()
        }
        sendPhoneCodeDelegate.currentCountry?.let { emitViewState(OnboardingViewState.CurrentCountry(it)) }
    }

    private fun reloadCountries() {
        viewModelScope.launch {
            runCatching { loadSignupCountriesUseCase.invoke() }
                .onFailure { Timber.d(it) }
        }
    }

    private fun emitViewState(state: OnboardingViewState) = _stateLiveData.postValue(state)
    private fun onBoardingFinished() {
        emitViewEvent(OnboardingViewEvent.AuthFinished)
        _isRegistrationCompleted.postValue(true)
    }

    private fun setRegistrationFinished() {
        _isRegistrationCompleted.postValue(true)
    }

    private fun getAmplitudeEventName(step: OnboardingStep): AmplitudeOnBoardingEventName {
        return when (step) {
            OnboardingStep.STEP_JOIN -> AmplitudeOnBoardingEventName.JOIN
            OnboardingStep.STEP_EVENTS -> AmplitudeOnBoardingEventName.FEED
            OnboardingStep.STEP_PEOPLE_NEAR -> AmplitudeOnBoardingEventName.MAP
            OnboardingStep.STEP_CONNECT -> AmplitudeOnBoardingEventName.COMMUNICATION
            OnboardingStep.STEP_JOIN_PHONE -> AmplitudeOnBoardingEventName.JOIN_PHONE
            OnboardingStep.STEP_WELCOME -> AmplitudeOnBoardingEventName.WELCOME
        }
    }

    private fun emitViewEvent(typeEvent: OnboardingViewEvent) {
        viewModelScope.launch {
            _onBoardingViewEvent.emit(typeEvent)
        }
    }
}
