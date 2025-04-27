package com.numplates.nomera3.presentation.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.numplates.nomera3.App
import com.numplates.nomera3.data.newmessenger.response.ResponseWrapperWebSock
import com.meera.core.network.websocket.WebSocketMainChannel
import com.numplates.nomera3.presentation.model.adaptermodel.VehicleTypeModel
import com.numplates.nomera3.presentation.model.webresponse.ResponseVehicleTypes
import com.meera.core.extensions.fromJson
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class AllVehicleTypesViewModel: ViewModel() {

    @Inject
    lateinit var webSocketMainChannel: WebSocketMainChannel

    @Inject
    lateinit var gson: Gson

    val liveTypes = MutableLiveData<MutableList<VehicleTypeModel>>()

    val disposables = CompositeDisposable()

    fun init() {
        App.component.inject(this)
        requestVehicles()
    }

    private fun requestVehicles() {
        val d = webSocketMainChannel.pushGetVehiclesTypes(hashMapOf())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({message ->
                    val response = gson.fromJson<ResponseWrapperWebSock<ResponseVehicleTypes>>(message.payload)
                    val res = mutableListOf<VehicleTypeModel>()
                    response.response?.let {responseVehicleTypes ->
                        responseVehicleTypes.types.forEach { type->
                            res.add(VehicleTypeModel(type))
                        }
                        liveTypes.value = res
                    }?:kotlin.run {

                    }
                },{
                    Timber.e(it)
                })
        disposables.add(d)
    }

    override fun onCleared() {
        disposables.clear()
    }
}
