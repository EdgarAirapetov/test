package com.numplates.nomera3.presentation.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.domain.interactornew.AuthAuthenticateUseCase
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.auth.data.AuthenticationErrors
import com.numplates.nomera3.modules.auth.domain.AuthAuthenticateEmailParams
import com.numplates.nomera3.modules.auth.domain.AuthAuthenticateEmailUseCase
import com.numplates.nomera3.modules.auth.domain.AuthAuthenticatePhoneParams
import com.numplates.nomera3.modules.auth.domain.AuthAuthenticatePhoneUseCase
import com.numplates.nomera3.modules.userprofile.domain.usecase.ObserveLocalOwnUserProfileModelUseCase
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class AuthSmsViewModel : BaseViewModel() {

    @Inject
    lateinit var authEmailUseCase: AuthAuthenticateEmailUseCase

    @Inject
    lateinit var authPhoneUseCase: AuthAuthenticatePhoneUseCase

    @Inject
    lateinit var authenticateUseCase: AuthAuthenticateUseCase

    @Inject
    lateinit var observeLocalOwnUserProfileUseCase: ObserveLocalOwnUserProfileModelUseCase

    @Inject
    lateinit var tracker: AnalyticsInteractor

    val liveLoginProgress = MutableLiveData<Boolean>()

    val liveLoginSuccess = MutableLiveData<Boolean>()

    val disposables = CompositeDisposable()

    //analytics params
    //когда был отправлен запрос на смс или мейл (запрос на код)
    private var startTime: Long? = null
    //сколько раз был введен не верный код
    private var incorrectCount: Int = 0
    //сколько раз мы повторно запрашивали код
    private var requestCodeTimes: Int = 0
    private val DEFAULT_TIME = "00.00.00"

    /**
     * Pair(stringRes, List args)
     */
    var liveErrorMessage = MutableLiveData<Pair<Int, List<String>>>()

    init {
        App.component.inject(this)
    }

    fun getUserProfileLive() = observeLocalOwnUserProfileUseCase.invoke().asLiveData()

    fun authenticateEmail(email: String, code: String) {
        // Timber.d("AUTH-EMAIL viewModel: Email->$email Code->$code")
        liveLoginProgress.value = true
        viewModelScope.launch(Dispatchers.IO) {
            authEmailUseCase.execute(
                params = AuthAuthenticateEmailParams(email, code),
                { isSuccess ->
                    tracker.setIsFromSms(true)
                    liveLoginProgress.postValue(false)
                    if (isSuccess) {
                        logCodeEnter()
                        liveLoginSuccess.postValue(true)
                    }
                },
                { authErrors ->
                    liveLoginProgress.postValue(false)
                    when (authErrors) {
                        is AuthenticationErrors.NetworkAuthenticationError ->
                            showErrorMessage(authErrors.messageRes)
                        is AuthenticationErrors.AuthenticateErrorExt -> {
                            showErrorMessage(
                                authErrors.messageRes,
                                authErrors.field,
                                authErrors.reason
                            )
                            incorrectCount ++
                        }
                        else -> showErrorMessage(R.string.auth_code_failed_to_send)
                    }
                }
            )
        }
    }

    fun authenticatePhone(phone: String, code: String) {
        // Timber.d("AUTH-EMAIL viewModel: Phone->$phone Code->$code")
        liveLoginProgress.value = true
        viewModelScope.launch(Dispatchers.IO) {
            authPhoneUseCase.execute(
                params = AuthAuthenticatePhoneParams(phone, code),
                { isSuccess ->
                    tracker.setIsFromSms(true)
                    liveLoginProgress.postValue(false)
                    if (isSuccess) {
                        logCodeEnter()
                        liveLoginSuccess.postValue(true)
                    }
                },
                { authErrors ->
                    liveLoginProgress.postValue(false)
                    when (authErrors) {
                        is AuthenticationErrors.NetworkAuthenticationError ->
                            showErrorMessage(authErrors.messageRes)
                        is AuthenticationErrors.AuthenticateErrorExt ->
                            showErrorMessage(
                                authErrors.messageRes,
                                authErrors.field,
                                authErrors.reason
                            )
                        else -> showErrorMessage(R.string.auth_code_failed_to_send)
                    }
                }
            )
        }
    }

    private fun showErrorMessage(@StringRes stringRes: Int, vararg vars: String) {
        val params = mutableListOf<String>()
        vars.forEach { params.add(it) }
        val message = Pair(stringRes, params)
        liveErrorMessage.postValue(message)
    }

    fun init() {
        if (startTime == null) startTime = System.currentTimeMillis()
    }

    fun incSendCodeTimes() {
        startTime = System.currentTimeMillis()
        requestCodeTimes ++
    }

    private fun logCodeEnter() {
        val timeSpend = startTime?.let {
            val timeLong = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - it)
            val hours = timeLong / 3600
            val minutes = (timeLong % 3600) / 60
            val seconds = timeLong % 60
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } ?: kotlin.run {
            DEFAULT_TIME
        }
        tracker.logCodeEnter(timeSpend, incorrectCount, requestCodeTimes)
    }
}
