package com.numplates.nomera3.presentation.view.fragments.vehiclebrandmodelselect

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.data.network.ApiHiWayKt
import com.numplates.nomera3.presentation.view.fragments.vehiclebrandmodelselect.MeeraVehicleBrandModelSelectFragment.Companion.ARG_LIST_TYPE_BRANDS
import com.numplates.nomera3.presentation.view.fragments.vehiclebrandmodelselect.MeeraVehicleBrandModelSelectFragment.Companion.ARG_LIST_TYPE_MODELS
import kotlinx.coroutines.launch
import javax.inject.Inject

class MeeraVehicleBrandModelSelectViewModel @Inject constructor(private val apiHiWayKt: ApiHiWayKt) : ViewModel() {


    private val _vehicleBrandModelSelectUiState = MutableLiveData<VehicleBrandModelSelectUiState>()
    val vehicleBrandModelSelectUiState: LiveData<VehicleBrandModelSelectUiState> = _vehicleBrandModelSelectUiState

    private var listType: String = ""
    private var brandId: Int? = null

    fun init(listType: String, brandId: Int?) {
        this.listType = listType
        this.brandId = brandId
        getData()
    }

    fun getData(query: String? = null) {
        when (listType) {
            ARG_LIST_TYPE_BRANDS -> getBrands(query)
            ARG_LIST_TYPE_MODELS -> getModels(query)
        }

    }

    private fun getModels(query: String?) {
        viewModelScope.launch {
            runCatching {
                val response = apiHiWayKt.carModelsList(makeId = brandId, query = query)
                val list = response.data?.models ?: emptyList()

                list.mapIndexed { index, model ->
                    VehicleBrandModelItem(
                        id = model.modelId, name = model.name ?: "", image = null, isLast = index == list.size - 1
                    )
                }
            }.onSuccess {
                _vehicleBrandModelSelectUiState.postValue(VehicleBrandModelSelectUiState.Success(it))
            }.onFailure {

            }
        }
    }

    private fun getBrands(query: String?) {
        viewModelScope.launch {
            runCatching {
                val response = apiHiWayKt.carBrandsList(query = query)
                val list = response.data?.makes ?: emptyList()

                list.mapIndexed { index, model ->
                    VehicleBrandModelItem(
                        id = model.makeId,
                        name = model.name ?: "",
                        image = model.imageUrl,
                        isLast = index == list.size - 1
                    )
                }
            }.onSuccess {
                _vehicleBrandModelSelectUiState.postValue(VehicleBrandModelSelectUiState.Success(it))
            }.onFailure {

            }
        }
    }


}
