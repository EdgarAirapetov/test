package com.numplates.nomera3.presentation.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.App
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.data.network.Country
import com.numplates.nomera3.data.network.VehicleType
import com.numplates.nomera3.data.network.VehicleTypes
import com.numplates.nomera3.domain.interactornew.GetCountriesUseCase
import com.numplates.nomera3.domain.interactornew.VehicleTypesUseCase
import com.numplates.nomera3.modules.search.domain.usecase.SearchByNumberUseCase
import com.numplates.nomera3.modules.search.domain.usecase.SearchByNumberUseCaseParams
import com.numplates.nomera3.presentation.viewmodel.viewevents.FindByNumberViewEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Нужно заменить на NumberSearchViewModel
 */
class FindByNumberViewModel : BaseViewModel() {

    @Inject
    lateinit var getVehicleTypesUseCase: VehicleTypesUseCase

    @Inject
    lateinit var getCountriesUseCase: GetCountriesUseCase

    @Inject
    lateinit var searchByNumberUseCase: SearchByNumberUseCase

    val liveListVehicles = MutableLiveData<VehicleTypes>()

    val liveListCountries = MutableLiveData<List<Country?>>()

    val liveViewEvents = MutableLiveData<FindByNumberViewEvent>()

    val liveSearchedUsers = MutableLiveData<List<UserSimple>>()

    val disposables = CompositeDisposable()

    var vehicleTypeId: Int = 1
    var countryId: Long = 3159

    init {
        App.component.inject(this)
    }

    fun getListVehicleTypes() {
        disposables.add(
            getVehicleTypesUseCase.getVehicleTypes()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    val res = mutableListOf<VehicleType>()
                    response.data?.let {
                        it.vehicleTypes?.forEach { vehicleType ->
                            if (vehicleType?.hasNumber == 1) {
                                res.add(vehicleType)
                            }
                        }
                        liveListVehicles.value = VehicleTypes(res)
                    } ?: kotlin.run {
                        liveViewEvents.value = FindByNumberViewEvent.FailureGetVehicleTypes
                    }
                }, {
                    Timber.e(it)
                    liveViewEvents.value = FindByNumberViewEvent.FailureGetVehicleTypes
                })
        )
    }


    fun getListCountries() {
        getCountriesUseCase.getCountries()?.let { countries ->
            disposables.add(
                countries
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ response ->
                        response.data.countries?.let { countries ->
                            liveListCountries.value = countries
                        }
                            ?: kotlin.run {
                                liveViewEvents.value = FindByNumberViewEvent.FailureGetCountries
                            }
                    }, {
                        Timber.e(it)
                        liveViewEvents.value = FindByNumberViewEvent.FailureGetCountries
                    })

            )
        }
    }


    fun searchByNumberRequest(number: String) {
        viewModelScope.launch(Dispatchers.IO) {
            searchByNumberUseCase.execute(
                params = SearchByNumberUseCaseParams(number, countryId.toInt(), vehicleTypeId),
                success = {
                    if (it.isNotEmpty()) {
                        liveSearchedUsers.postValue(it)
                    } else {
                        liveViewEvents.postValue(FindByNumberViewEvent.NotFoundSearchUser)
                    }
                },
                fail = {
                    Timber.e(it)
                    liveViewEvents.postValue(FindByNumberViewEvent.FailureSearchUser)
                })
        }
    }


    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }
}
