package com.numplates.nomera3.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.meera.core.extensions.fromJson
import com.meera.core.network.websocket.WebSocketMainChannel
import com.numplates.nomera3.App
import com.numplates.nomera3.data.network.Vehicle
import com.numplates.nomera3.data.network.market.ResponseWizard
import com.numplates.nomera3.data.newmessenger.response.ResponseWrapperWebSock
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.presentation.view.utils.eventbus.busevents.BusEvents
import com.numplates.nomera3.presentation.viewmodel.viewevents.EditVehicleMarketEvents
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class EditVehicleMarketViewModel(app: Application) : AndroidViewModel(app) {

    private val disposables = CompositeDisposable()
    private var vehicle: Vehicle? = null
    private var userID: Long = 0

    @Inject
    lateinit var webSocketMainChannel: WebSocketMainChannel

    @Inject
    lateinit var getUserUidUseCase: GetUserUidUseCase

    @Inject
    lateinit var gson: Gson

    val liveImages = MutableLiveData<MutableList<String>>()
    val liveSetProgress = MutableLiveData<Int>()
    val liveWizard = MutableLiveData<ResponseWizard>()
    val liveEvent = MutableLiveData<EditVehicleMarketEvents>()

    fun init(vehicle: Vehicle?, userId: Long) {
        this.userID = userId
        this.vehicle = vehicle
        App.component.inject(this)
        requestVehicleInfo()
        observeEventBus()
    }

    fun getUserUid() = getUserUidUseCase.invoke()

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }

    private fun observeEventBus() {
        val d  = App.bus.toObservable().subscribe({ obj ->
            if (obj is BusEvents.UploadProgress) {
                Timber.d("UploadService eventBus total:${obj.total} progress:${obj.progress}")
            }
        }, {
            Timber.d("UploadService error")
        })
        disposables.add(d)
    }

    private fun requestVehicleInfo() {
        vehicle?.let {
            val payload = mutableMapOf(
                    "user_id" to userID,
                    "vehicle_id" to it.vehicleId!!
            )
            val d = webSocketMainChannel.pushGetVehicleInfo(payload)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ message ->
                        val response = gson.fromJson<ResponseWrapperWebSock<ResponseWizard>>(message.payload)
                        response?.let {
                            liveWizard.value = response.response
                        }?:kotlin.run {
                            liveEvent.value = EditVehicleMarketEvents.OnError
                        }
                    },{ throwable->
                        Timber.e(throwable)
                        liveEvent.value = EditVehicleMarketEvents.OnError
                    })
            disposables.add(d)
        }
    }

}
