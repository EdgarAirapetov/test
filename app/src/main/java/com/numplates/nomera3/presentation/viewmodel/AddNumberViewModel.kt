package com.numplates.nomera3.presentation.viewmodel

import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.meera.core.extensions.fromJson
import com.meera.core.preferences.AppSettings
import com.numplates.nomera3.App
import com.numplates.nomera3.data.network.Countries
import com.numplates.nomera3.data.newmessenger.response.ErrorMessage
import com.numplates.nomera3.data.newmessenger.response.ResponseWrapperWebSock
import com.meera.db.DataStore
import com.meera.core.network.websocket.WebSocketMainChannel
import com.meera.core.network.websocket.WebSocketResponseException
import com.numplates.nomera3.domain.interactornew.GetCountriesUseCase
import com.numplates.nomera3.presentation.viewmodel.exception.Failure
import com.numplates.nomera3.presentation.viewmodel.viewevents.AddNumberViewEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import timber.log.Timber

class AddNumberViewModel : BaseViewModel() {

    @Inject
    lateinit var webSocketMainChannel: WebSocketMainChannel

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var dataStore: DataStore

    @Inject
    lateinit var countriesUseCase: GetCountriesUseCase

    val countriesLiveData by lazy { MutableLiveData<Countries>() }
    val liveViewEvents = MutableLiveData<AddNumberViewEvent>()

    val liveTimeTick = MutableLiveData<Long>()

    private var phoneNumber: String = ""
    private val disposables = CompositeDisposable()
    private lateinit var appSettings: AppSettings
    private var timer: CountDownTimer? = null
    private val timeStep = 1000L
    private val timeTotal = 1000L * 120L


    init {
        App.component.inject(this)
    }

    fun getCountries() {
        disposables.add(
                countriesUseCase
                        .getCountries()!!
                        .subscribeOn(Schedulers.io())
                        .subscribe({
                            if (it.data != null)
                                countriesLiveData.postValue(it.data)
                            else if (it.err != null)
                                failure.postValue(it.err.userMessage?.let { it1 -> Failure.ServerError(it1) })
                        }, {
                            Timber.e(it)
                        })
        )
    }


    override fun onCleared() {
        disposables.clear()
        stopTimer()
    }

    fun sendVerifyCode(phoneNumber: String) {

        var resNumber: String = phoneNumber.replace(" ","")
        resNumber = resNumber.replace("-","")
        resNumber = resNumber.replace("+","")
        this.phoneNumber = resNumber
        initTimer()

        val payload = hashMapOf<String, Any>(
                "phone" to resNumber
        )

        val d = webSocketMainChannel.pushContactSendCode(payload)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    liveViewEvents.value = AddNumberViewEvent.VerifyCodeSend
                },{
                    Timber.e(it)
                    val err = it as WebSocketResponseException
                    Timber.d("ERR: ${err.getPayload()["status"]}")
                    val msg = gson.fromJson<ResponseWrapperWebSock<ErrorMessage>>(
                            gson.toJson(err.getPayload()))
                    Timber.d(msg.response?.message)
                    liveViewEvents.value = AddNumberViewEvent.FailedToSendVerifyCode(msg.response?.message ?: "")
                })

        disposables.add(d)
    }

    private fun initTimer(time: Long = timeTotal) {
        appSettings.writeLastSmsCodeTime(System.currentTimeMillis())
        timer = object: CountDownTimer(time, timeStep){

            override fun onFinish() {
                liveViewEvents.value = AddNumberViewEvent.TimerFinished
            }

            override fun onTick(millisUntilFinished: Long) {
                liveTimeTick.value = millisUntilFinished
            }

        }.start()
    }

    fun sendConfirmCode(code: String) {
        val payload = hashMapOf<String, Any>(
                "phone" to phoneNumber,
                "code" to code.toInt()
        )
        val d = webSocketMainChannel.pushContactVerify(payload)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    liveViewEvents.value = AddNumberViewEvent.VerifySuccess
                    appSettings.writeUserPhoneNumber(phoneNumber)
                },{
                    Timber.e(it)
                    liveViewEvents.value = AddNumberViewEvent.VerifyFailure
                })
        disposables.add(d)
    }

    fun init(appSettings: AppSettings) {
        this.appSettings = appSettings
    }

    fun stopTimer() {
        timer?.cancel()
        timer?.onFinish()
    }
}
