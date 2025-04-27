package com.numplates.nomera3.modules.auth.ui

import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.lifecycleScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.meera.core.extensions.doDelayed
import com.numplates.nomera3.Act
import com.numplates.nomera3.modules.auth.AuthStatus
import com.numplates.nomera3.modules.registration.AuthType
import com.numplates.nomera3.modules.registration.ui.RegistrationContainerFragment
import com.numplates.nomera3.presentation.view.navigator.NavigatorPageTransformerHorizontal
import com.numplates.nomera3.presentation.view.navigator.NavigatorPageTransformerVertical
import com.numplates.nomera3.presentation.view.navigator.NavigatorViewPager
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

internal const val VIEW_PAGER_ANIMATION_SET_DELAY = 1000L

// TODO: Удалить после редизайна https://nomera.atlassian.net/browse/BR-30859
class AuthNavigator(
    private val rootActivity: Act,
    private val navigatorViewPager: NavigatorViewPager,
) {
    private val authViewModel by rootActivity.viewModels<AuthViewModel>()

    private var disposable: Disposable? = null
    private var viewPageAnimationDisposable: Disposable? = null

    private var registrationContainerFragment: RegistrationContainerFragment? = null

    private val lifecycleObserver = object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        fun onStart() {
            subscribeRx()
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun onStop() {
            disposeRx()
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy() {
            rootActivity.lifecycle.removeObserver(this)
        }
    }

    init {
        authViewModel.navigationState.observe(rootActivity, ::doState)
        rootActivity.lifecycle.addObserver(lifecycleObserver)

        setHorizontalScreenAnimation()
        observeViewEvent()
    }

    fun isAuthorized(): Boolean {
        return authViewModel.authStatus.value is AuthStatus.Authorized
    }

    fun isAuthScreenOpen(): Boolean {
        return authViewModel.navigationState.value != AuthNavigationState.None
    }

    fun onAuthStatusInitialized(action: () -> Unit) {
        authViewModel.onAuthStatusInitialized(action)
    }

    /**
     * Покинули первый экран через закрытие крестиком
     */
    fun backNavigatePhoneScreenByCloseButton() = authViewModel.backNavigatePhoneScreenByCloseButton()

    /**
     * Покинули первый экран авторизации через "назад"
     */
    fun backNavigatePhoneScreenByBack() = authViewModel.backNavigatePhoneScreenByBack()

    fun backNavigatePersonalScreenByBack() = authViewModel.backNavigatePersonalScreenByBack()
    fun backNavigateRecoveryScreenByBack() = authViewModel.backNavigateRecoveryScreenByBack()

    fun completeOnSmsScreen() = authViewModel.recheckAuthStatusWithProfile()
    fun completeOnPersonalScreen() = authViewModel.recheckAuthStatusWithProfile()
    fun completeOnRecoveryScreen() = authViewModel.recheckAuthStatusWithProfile()

    /**
     * Перейти на экран SMS
     */
    fun navigateToSms(phoneNumber: String, login: String) =
        authViewModel.navigateToSms(phoneNumber, login)

    /**
     * Перейти на экран ввода email/телефона
     */
    fun navigateToPhone() =
        authViewModel.navigateToPhone()

    /**
     * Перейти на экран подтверждения кода из смс
     * */
    fun openCodeInput(
        authType: AuthType,
        sendTo: String,
        countryCode: String?,
        countryName: String?,
        countryMask: String?,
        smsTimeout: Long?
    ) {
        authViewModel.navigateToConfirmCode(authType, sendTo, countryCode, countryName, countryMask, smsTimeout)
    }

    fun navigateToPersonalInfo(countryName: String? = null) =
        authViewModel.navigateToPersonalInfo(countryName)

    fun needAuth(complete: ((Boolean) -> Unit)?): Boolean {
        return authViewModel.needAuth(getCurrentIndex(), complete)
    }

    private fun getCurrentIndex(): Int {
        return rootActivity.getFragmentsCount()
    }

    private fun subscribeRx() {
        disposable = authViewModel
            .navigationEvent
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { event ->
                    doEvent(event)
                },
                { throwable ->
                    FirebaseCrashlytics.getInstance().recordException(throwable)
                }
            )
    }

    private fun doEvent(event: AuthNavigationEvent) {
        when (event) {
            is AuthNavigationEvent.StartAuthProcess -> {
                setVerticalScreenAnimation()
            }

            is AuthNavigationEvent.RollOutToScreenBeforeAuth -> {
                handleRollOutToScreenBeforeAuth()
            }

            is AuthNavigationEvent.BackNavigatePhoneScreenByCloseButton -> {
                authViewModel.finishOnCancel()
                rootActivity.onBackPressed()

                setHorizontalScreenAnimationAfterDelay()
            }

            is AuthNavigationEvent.BackNavigatePhoneScreenByBack -> {
                authViewModel.finishOnCancel()
                setHorizontalScreenAnimationAfterDelay()
            }

            is AuthNavigationEvent.Complete -> {
                authViewModel.finishOnComplete()

                setHorizontalScreenAnimationAfterDelay()
            }
        }
    }


    /**
     * тут мы закрываем экран с навигацией
     * */
    private fun handleRollOutToScreenBeforeAuth() {
        rootActivity.returnToTargetFragment(authViewModel.rollbackIndex - 1, false) {
            authViewModel.onNavigationReady()
        }
    }

    private fun doState(state: AuthNavigationState) {
        when (state) {
            is AuthNavigationState.None -> {
                if (registrationContainerFragment != null) registrationContainerFragment = null
            }

            is AuthNavigationState.Phone -> {
                val fragment = getRegistrationContainerFragmentAddIfNot()
                rootActivity.doDelayed(30) { fragment.openPhoneMailInput() }
            }

            is AuthNavigationState.UserPersonalInfo -> {
                getRegistrationContainerFragmentAddIfNot().startRegistration(state.countryName)
            }

            is AuthNavigationState.CodeConfirmation -> {
                getRegistrationContainerFragmentAddIfNot().openCodeInput(
                    state.authType,
                    state.sendTo,
                    state.countryCode,
                    state.countryName,
                    state.countryMask,
                    state.smsTimeout
                )
            }

            else -> Unit
        }
    }

    private fun getRegistrationContainerFragmentAddIfNot(): RegistrationContainerFragment {
        return registrationContainerFragment ?: RegistrationContainerFragment().apply {
            rootActivity.addFragment(this, Act.LIGHT_STATUSBAR)
            registrationContainerFragment = this
        }
    }

    private fun setHorizontalScreenAnimationAfterDelay() {
        viewPageAnimationDisposable?.dispose()
        viewPageAnimationDisposable = Observable.timer(VIEW_PAGER_ANIMATION_SET_DELAY, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                setHorizontalScreenAnimation()
            }
    }

    private fun setHorizontalScreenAnimation() {
        navigatorViewPager.setCurrentPageTransformer(
            false,
            NavigatorPageTransformerHorizontal()
        )
    }

    private fun setVerticalScreenAnimation() {
        viewPageAnimationDisposable?.dispose()
        navigatorViewPager.setCurrentPageTransformer(
            false,
            NavigatorPageTransformerVertical()
        )
    }

    private fun observeViewEvent() {
        rootActivity.lifecycleScope.launchWhenStarted {
            authViewModel.authViewEvent.collect { event ->
                handleViewEvent(event)
            }
        }
    }

    private fun handleViewEvent(event: AuthViewEvent) {
        when (event) {
            AuthViewEvent.ResetRegContainerInstance -> {
                registrationContainerFragment?.let {
                    registrationContainerFragment = null
                }
            }
        }
    }

    private fun disposeRx() {
        disposable?.dispose()
    }
}
