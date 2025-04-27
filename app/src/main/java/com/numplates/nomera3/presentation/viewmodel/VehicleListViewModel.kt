package com.numplates.nomera3.presentation.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.App
import com.numplates.nomera3.data.network.Vehicles
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.domain.interactornew.DeleteVehicleUseCase
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.domain.interactornew.VehicleListUseCase
import com.numplates.nomera3.modules.vehicle.VehicleUpdateSubjectParams
import com.numplates.nomera3.modules.vehicle.VehicleUpdateSubjectUseCase
import com.numplates.nomera3.presentation.model.adaptermodel.VehicleModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.VehicleListEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class VehicleListViewModel: ViewModel() {

    @Inject
    lateinit var vehicleListUseCase: VehicleListUseCase
    @Inject
    lateinit var vehicleUpdateUseCase: VehicleUpdateSubjectUseCase

    @Inject
    lateinit var getUserUidUseCase: GetUserUidUseCase

    @Inject
    lateinit var deleteVehicleUseCase: DeleteVehicleUseCase

    var liveVehicles = MutableLiveData<List<VehicleModel>>()
    var liveEvent = MutableLiveData<VehicleListEvent>()

    private val disposables = CompositeDisposable()
    private var userId: Long = -1

    init {
        App.component.inject(this)
    }

    fun init(userID: Long) {
        this.userId = userID
        requestListVehicles()
        subscribeToVehicleUpdate()
    }

    fun getUserUid() = getUserUidUseCase.invoke()


    fun handleHideVehicle(vehicleId: String) {
        updateVehicleLocal(vehicleId = vehicleId, hidden = true)
    }

    fun handleShowVehicle(vehicleId: String) {
        updateVehicleLocal(vehicleId = vehicleId, hidden = false)
    }

    private fun updateVehicleLocal(vehicleId: String, hidden: Boolean) {
        liveEvent.value = VehicleListEvent.VehicleClearAdapter
        val list = liveVehicles.value ?: return
        val updatedList = list.map {
            if (it.vehicle?.vehicleId.toString() == vehicleId) it.copy(hidden = hidden)
            else it
        }
        liveVehicles.value = updatedList
    }

    fun handleDeleteVehicle(vehicleId: String) {
        viewModelScope.launch {
            runCatching {
                deleteVehicleUseCase.invoke(vehicleId)
            }.onSuccess {
                requestListVehicles()
            }.onFailure {
                Timber.e(it)
            }
        }
    }

    private fun subscribeToVehicleUpdate() {
        vehicleUpdateUseCase
            .execute(VehicleUpdateSubjectParams)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { vehicle ->
                if (vehicle != null) {
                    liveEvent.value = VehicleListEvent.VehicleUpdate(VehicleModel(vehicle))
                }
            }
            .addTo(disposables)
    }

    private fun requestListVehicles() {
        val d = vehicleListUseCase.getVehicleListUseCase(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    handleVehicle(it)
                },{
                    Timber.e(it)
                    liveEvent.value = VehicleListEvent.VehicleError
                })
        disposables.add(d)
    }

    private fun handleVehicle(vehicles: ResponseWrapper<Vehicles>) {
        liveEvent.value = VehicleListEvent.VehicleClearAdapter
        val res = vehicles.data.vehicles?.map { VehicleModel(it) } ?: emptyList()
        liveVehicles.value = res
    }

    override fun onCleared() {
        disposables.clear()
    }

    fun refreshData() {
        requestListVehicles()
    }
}
