package com.numplates.nomera3.presentation.view.fragments.vehicleedit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meera.core.dialogs.unlimiteditem.MeeraConfirmDialogUnlimitedNumberItemsData
import com.numplates.nomera3.data.network.Vehicle
import com.numplates.nomera3.data.network.core.ResponseError
import com.numplates.nomera3.modules.registration.domain.GetCountriesUseCase
import com.numplates.nomera3.presentation.view.fragments.vehicleedit.usecase.AddVehicleUseCase
import com.numplates.nomera3.presentation.view.fragments.vehicleedit.usecase.MeeraVehicleEditState
import com.numplates.nomera3.presentation.view.fragments.vehicleedit.usecase.UpdateVehicleUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class MeeraVehicleEditViewModel @Inject constructor(
    private val getCountriesUseCase: GetCountriesUseCase,
    private val addVehicleUseCase: AddVehicleUseCase,
    private val updateVehicleUseCase: UpdateVehicleUseCase,
    private val countryModelMapper: CountryModelMapper,
) : ViewModel() {

    private val _state: MutableStateFlow<MeeraVehicleEditState?> = MutableStateFlow(null)
    val state: Flow<MeeraVehicleEditState> by lazy { _state.filterNotNull() }

    private var countries = emptyList<MeeraConfirmDialogUnlimitedNumberItemsData>()

    private suspend fun emitState(newState: MeeraVehicleEditState) {
        _state.emit(newState)
    }

    fun loadCountries(isAuto: Boolean) {
        viewModelScope.launch {
            runCatching {
                getCountriesUseCase.invoke()
            }.onSuccess {
                countries =
                    countryModelMapper.mapRegistrationCountryModelListToMeeraConfirmDialogUnlimitedNumberItemsDataList(
                        items = it, isAuto = isAuto
                    )
            }.onFailure { t ->
                Timber.e(t)
            }
        }
    }


    fun updateVehicle(vehicle: Vehicle, image: String?) {
        viewModelScope.launch {
            emitState(MeeraVehicleEditState.OnLoading)
            runCatching {
                updateVehicleUseCase.invoke(vehicle, image)
            }.onSuccess {
                if (it.err != null) {
                    emitError(it.err)
                } else {
                    emitState(MeeraVehicleEditState.OnUpdateVehicleSuccess)
                }
            }.onFailure {
                emitState(MeeraVehicleEditState.MessageError(it.localizedMessage ?: it.message ?: it.toString()))
            }
        }
    }

    fun addVehicle(vehicle: Vehicle, image: String?) {
        viewModelScope.launch {
            emitState(MeeraVehicleEditState.OnLoading)
            runCatching {
                addVehicleUseCase.invoke(vehicle, image)
            }.onSuccess {
                if (it.err != null) {
                    emitError(it.err)
                } else {
                    emitState(MeeraVehicleEditState.OnAddVehicleSuccess)
                }
            }.onFailure {
                emitState(MeeraVehicleEditState.MessageError(it.localizedMessage ?: it.message ?: it.toString()))
            }
        }
    }

    private suspend fun emitError(responseError: ResponseError) {
        when (responseError.code) {
            NUM_PLATE_ERROR_CODE -> emitState(MeeraVehicleEditState.NumPlateError(responseError.message))
            else -> emitState(MeeraVehicleEditState.MessageError(responseError.message))
        }
    }

    fun countriesList(): List<MeeraConfirmDialogUnlimitedNumberItemsData> {
        return countries
    }

    companion object {
        const val NUM_PLATE_ERROR_CODE = 69
    }

}
