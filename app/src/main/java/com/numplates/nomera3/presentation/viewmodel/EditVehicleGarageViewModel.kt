package com.numplates.nomera3.presentation.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.meera.core.network.websocket.WebSocketMainChannel
import com.numplates.nomera3.App
import com.numplates.nomera3.presentation.viewmodel.viewevents.EditVehicleGarageViewEvent
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class EditVehicleGarageViewModel: ViewModel() {

    var liveViewEvents = MutableLiveData<EditVehicleGarageViewEvent>()
    val disposables = CompositeDisposable()

    @Inject
    lateinit var webSocketMainChannel: WebSocketMainChannel

    fun init() {
        App.component.inject(this)
        requestVehicleInfo()
    }

    private fun requestVehicleInfo() = Unit
    fun deleteVehicle() = Unit
    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}
