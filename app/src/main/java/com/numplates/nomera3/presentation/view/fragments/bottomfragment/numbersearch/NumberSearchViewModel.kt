package com.numplates.nomera3.presentation.view.fragments.bottomfragment.numbersearch

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.meera.db.models.userprofile.VehicleType
import com.numplates.nomera3.modules.search.domain.usecase.SetNumberSearchParamsUseCase
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.roadfilter.CountryFilterItem
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.roadfilter.getCountryFilterItem
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.roadfilter.getCountryId
import javax.inject.Inject

class NumberSearchViewModel @Inject constructor(
    private val setNumberSearchParamsUseCase: SetNumberSearchParamsUseCase
) : ViewModel() {

    val liveData = MutableLiveData<NumberSearchViewEvent>()

    var defaultVehicleTypeId = VehicleType.TYPE_ID_CAR
    var vehicleTypeId: Int = defaultVehicleTypeId
    var defaultCountryId: Long = NumberSearchBottomSheetFragment.DEFAULT_COUNTRY_ID
    var countryId: Long = defaultCountryId
    var selectedCountry: CountryFilterItem = CountryFilterItem.RUSSIA

    fun saveVehicleTypeId(typeId: Int) {
        vehicleTypeId = typeId
        liveData.postValue(NumberSearchViewEvent.Reset)
    }

    fun saveSelectedCountry(countryItem: CountryFilterItem) {
        selectedCountry = countryItem
        selectedCountry.getCountryId()?.let { countryId = it }
        liveData.postValue(NumberSearchViewEvent.Reset)
    }

    fun setDefaults() {
        vehicleTypeId = defaultVehicleTypeId
        countryId = defaultCountryId
        selectedCountry = getCountryFilterItem(defaultCountryId)
        liveData.postValue(NumberSearchViewEvent.Reset)
    }

    fun saveSearchParameters(parameters: NumberSearchParameters) {
        setNumberSearchParamsUseCase.invoke(parameters)
    }
}
