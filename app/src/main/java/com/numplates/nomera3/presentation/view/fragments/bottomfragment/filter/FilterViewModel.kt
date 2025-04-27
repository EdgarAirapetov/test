package com.numplates.nomera3.presentation.view.fragments.bottomfragment.filter

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.data.network.City
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.modules.registration.domain.GetCountriesUseCase
import com.numplates.nomera3.modules.registration.domain.model.RegistrationCountryModel
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.filter.FilterBottomSheet.Companion.MAX_FILTER_AGE
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.filter.FilterBottomSheet.Companion.MIN_FILTER_AGE
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class FilterViewModel @Inject constructor(
    private val getCountriesUseCase: GetCountriesUseCase,
    private val featureTogglesContainer: FeatureTogglesContainer
) : ViewModel() {

    val liveEvent = MutableLiveData<FilterViewEvent>()

    private val _liveCountries = MutableLiveData<List<RegistrationCountryModel>>()
    val liveCountries = _liveCountries

    var filterResult = FilterResult()

    fun initializeFilter() {
        viewModelScope.launch {
            runCatching {
                val countries = getCountriesUseCase.invoke().toMutableList()
                countries.add(0, ALL_COUNTRY)
                _liveCountries.postValue(countries)
                event(FilterViewEvent.Initialize)
            }.onFailure { t -> Timber.e(t) }
        }
    }

    fun isHiddenAgeAndGender() = featureTogglesContainer.hiddenAgeAndSexFeatureToggle.isEnabled

    fun saveSelectedCountries(countryItem: RegistrationCountryModel, isSelected: Boolean) {
        // Показ диалога сброса всех стан
        val selectedCitiesCount = filterResult.cities.size
        if (selectedCitiesCount > 0) {
            event(FilterViewEvent.Country(Pair(countryItem, isSelected)))
            return
        }

        // Сохранение выбранных стран в хранилище
        if (countryItem.id == ALL_COUNTRY.id) {
            filterResult.countries.clear()
            filterResult.countries.add(ALL_COUNTRY)
        } else {
            countryItem.id?.let {
                filterResult.countries.remove(ALL_COUNTRY)
                filterResult.countries.add(countryItem)
            }
        }

        event(FilterViewEvent.Countries(filterResult.countries))
        event(FilterViewEvent.ResetButtonState(isFilterDefaultState()))
    }

    fun removeCountry(countryItem: RegistrationCountryModel) {
        filterResult.countries.remove(countryItem)
        if (filterResult.countries.isEmpty()) {
            filterResult.countries.add(ALL_COUNTRY)
        }
        event(FilterViewEvent.Countries(filterResult.countries))
        event(FilterViewEvent.ResetButtonState(isFilterDefaultState()))
    }

    fun showSelectedCities(cities: List<City>) {
        filterResult.countries = mutableSetOf(ALL_COUNTRY)
        event(FilterViewEvent.Countries(filterResult.countries))
        filterResult.cities.addAll(cities)
        event(FilterViewEvent.Cities(filterResult.cities))
        event(FilterViewEvent.ResetButtonState(isFilterDefaultState()))
    }

    fun removeCity(city: City) {
        filterResult.cities.remove(city)
        event(FilterViewEvent.Cities(filterResult.cities))
        event(FilterViewEvent.ResetButtonState(isFilterDefaultState()))
    }

    fun removeAllCitiesAndSetNewCountries(it: Pair<RegistrationCountryModel, Boolean>) {
        filterResult.cities.clear()
        event(FilterViewEvent.Cities(filterResult.cities))
        saveSelectedCountries(it.first, it.second)
        event(FilterViewEvent.ResetButtonState(isFilterDefaultState()))
    }

    fun isFilterDefaultState(): Boolean {
        val countriesSize = filterResult.countries.size
        val citiesSize = filterResult.cities.size
        val isAgeDefault = filterResult.age.start == MIN_FILTER_AGE &&
            filterResult.age.end == MAX_FILTER_AGE
        val isGenderDefault = filterResult.gender == FilterGender.ANY

        if ((countriesSize > 0 && !filterResult.countries.any { it.id == ALL_COUNTRY.id })
            || citiesSize > 0
            || !isAgeDefault
            || !isGenderDefault
        ) {
            // Кнопка "Сброс активна"
            return false
        }
        // Кнопка "Сброс" не активна
        return true
    }

    fun setAge(start: Int, end: Int) {
        filterResult.age.start = start
        filterResult.age.end = end
    }

    fun triggerResetFilterButton() {
        event(FilterViewEvent.ResetButtonState(isFilterDefaultState()))
    }

    fun clearFilter() {
        filterResult.setByDefault()
        event(FilterViewEvent.Countries(filterResult.countries))
        event(FilterViewEvent.Cities(filterResult.cities))
        event(FilterViewEvent.ResetButtonState(isFilterDefaultState()))
    }

    private fun event(event: FilterViewEvent) {
        liveEvent.value = event
    }
}
