package com.numplates.nomera3.presentation.view.fragments.bottomfragment.roadfilter

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.App
import com.numplates.nomera3.data.network.City
import com.numplates.nomera3.modules.newroads.data.entities.FilterSettingsProvider
import com.numplates.nomera3.modules.registration.domain.GetCountriesUseCase
import com.numplates.nomera3.modules.registration.domain.model.RegistrationCountryModel
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.filter.ALL_COUNTRY
import com.numplates.nomera3.presentation.viewmodel.BaseViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.SingleLiveEvent
import kotlinx.coroutines.launch
import javax.inject.Inject

class RoadFilterViewModel(filterSettingsType: FilterSettingsProvider.FilterType) : BaseViewModel() {


    var selectedCountries = SingleLiveEvent<MutableSet<RegistrationCountryModel>>()
    var selectedCities = SingleLiveEvent<ArrayList<City>>()
    var sortState = SingleLiveEvent<Pair<Boolean, Boolean>>()

    var removeAllSelectedCities = SingleLiveEvent<Boolean>()
    var isResetButtonEnable = SingleLiveEvent<Boolean>()
    var isCountriesSelectable = SingleLiveEvent<Boolean>()
    var showResetSelectedCitiesDialog = SingleLiveEvent<Pair<RegistrationCountryModel, Boolean>>()

    private val _liveCountries = MutableLiveData<List<RegistrationCountryModel>>()
    val liveCountries = _liveCountries

    @Inject
    lateinit var filterSettingsProvider: FilterSettingsProvider

    @Inject
    lateinit var getCountriesUseCase: GetCountriesUseCase

    init {
        App.component.inject(this)
    }

    val filterSettings = filterSettingsProvider.get(filterSettingsType)

    fun initializeFilter(reload: Boolean = true) {
        if (reload) {
            filterSettings.load()
        }
        getSortStateFromStorage()
        // получаем список выбранных городов
        viewModelScope.launch {
            val countries = getCountries() ?: return@launch
            getSelectedCitiesFromStorage()
//            _liveCountries.postValue(countries)
            _liveCountries.value = countries
            // если выбран хотя бы один город, выделяем кнопку все страны
            val selectedCitiesCount = selectedCities.value?.size ?: 0
            if (selectedCitiesCount > 0) {
                filterSettings.dataCopy.hasCity = true
                filterSettings.dataCopy.allCountries = true

                _liveCountries.value?.forEach {
                    filterSettings.addCountry(it)
                }
            } else {
                filterSettings.dataCopy.hasCity = false
            }


            // получаем список выбранных стран
            // после получения списка городов,
            // т.к. наличие городов блокирует
            // выбор страны
            getSelectedCountriesFromStorage()

            updateResetButton()
        }
    }

    fun updateResetButton() {
        isResetButtonEnable.value = isRoadFilterDefaultState()
    }

    private suspend fun getCountries(): List<RegistrationCountryModel>? {
        val currentCountries = _liveCountries.value
        return if (currentCountries.isNullOrEmpty()) {
            val fetchedCountries = mutableListOf(ALL_COUNTRY)
            runCatching {
                fetchedCountries.addAll(getCountriesUseCase.invoke())
            }.onFailure {
                return null
            }
            fetchedCountries
        } else {
            currentCountries
        }
    }

    // сбросить все настройки в shared preference
    fun resetRoadFilter(toRecommended: Boolean = false) {
        // все страны выбраны
        filterSettings.dataCopy.allCountries = true

        _liveCountries.value?.forEach {
            filterSettings.addCountry(it)
        }

        // нет выбранных городов
        filterSettings.dataCopy.filterCities = arrayListOf()

        filterSettings.dataCopy.hasCity = false

        if (toRecommended) {
            filterSettings.dataCopy.isRecommended = true
        } else {
            if (filterSettings.dataCopy.isRecommended != null) {
                filterSettings.dataCopy.isRecommended = filterSettings.dataCopy.isRecommendedByDefault
            }
        }

        getSelectedCitiesFromStorage()
        getSelectedCountriesFromStorage()
    }

    fun setSortState(isRecommended: Boolean) {
        filterSettings.dataCopy.isRecommended = isRecommended
    }

    fun getSelectedCountriesFromStorage() {
        selectedCountries.value = readSelectedCountriesFromStorage()
    }

    private fun readSelectedCountriesFromStorage(): MutableSet<RegistrationCountryModel> {
        return if (filterSettings.dataCopy.allCountries) {
            mutableSetOf(ALL_COUNTRY)
        } else {
            val selectedCountriesFromStorage = mutableSetOf<RegistrationCountryModel>()

            filterSettings.dataCopy.filterCountries.forEach { countryFilterItem ->
                selectedCountriesFromStorage.add(countryFilterItem)
            }

            selectedCountriesFromStorage
        }
    }

    fun removeCityFromStorage(city: City) {
        val cities = selectedCities.value ?: arrayListOf()
        cities.remove(city)

        filterSettings.dataCopy.filterCities = cities
    }

    fun saveSelections(isRecommended: Boolean) {
        if (isRecommended) {
            // все страны выбраны
            filterSettings.dataCopy.allCountries = true

            // нет выбранных городов
            filterSettings.dataCopy.filterCities = arrayListOf()

            filterSettings.dataCopy.hasCity = false
        } else {
            filterSettings.dataCopy.filterCities = ArrayList(selectedCities.value ?: arrayListOf())
        }

        if (filterSettings.dataCopy.showSort) {
            filterSettings.dataCopy.isRecommended = isRecommended
        }

        filterSettings.commit()
    }

    fun updateSelectedCountries(roadFilterCountryItem: RegistrationCountryModel, isSelected: Boolean) {
        val selectedCitiesCount = selectedCities.value?.size ?: 0
        if (selectedCitiesCount > 0) {
            showResetSelectedCitiesDialog.value = Pair(roadFilterCountryItem, isSelected)
            return
        }

        if (roadFilterCountryItem.id == ALL_COUNTRY.id) {
            filterSettings.dataCopy.allCountries = true

            _liveCountries.value?.forEach { supportedCountry ->
                filterSettings.addCountry(supportedCountry)
            }
        } else {
            if (filterSettings.dataCopy.allCountries) {
                filterSettings.dataCopy.allCountries = false

                _liveCountries.value?.forEach {
                    filterSettings.removeCountry(it)
                }
            }

            filterSettings.setCountrySelected(roadFilterCountryItem, !isSelected)
        }

        if (isCountryStorageNotEmpty()) {

        } else {
            // если нет выбранных стран
            // активируем кнопку все страны
            filterSettings.dataCopy.allCountries = true
            _liveCountries.value?.forEach {
                filterSettings.setCountrySelected(it, true)
            }
        }


        isResetButtonEnable.value = isRoadFilterDefaultState()
    }

    private fun getSortStateFromStorage() {
        sortState.value = Pair(
            filterSettings.dataCopy.showSort,
            filterSettings.dataCopy.isRecommended ?: false
        )
    }

    private fun isRoadFilterDefaultState(): Boolean {
        val selectedCitiesCount = selectedCities.value?.size ?: 0
        return selectedCitiesCount == 0
            && allCountriesSelectedInStorage()
            && filterSettings.dataCopy.allCountries
            && !filterSettings.dataCopy.hasCity
    }

    private fun getSelectedCitiesFromStorage() {
        selectedCities.value = filterSettings.dataCopy.filterCities
    }

    private fun isCountryStorageNotEmpty(): Boolean {
        return filterSettings.isCountryFilterEmpty().not()
    }

    private fun allCountriesSelectedInStorage(): Boolean {
        return filterSettings.readSelectedCountryCount() == 0
            || filterSettings.readSelectedCountryCount() == _liveCountries.value?.size
    }

    fun removeAllSelectedCities() {
        filterSettings.dataCopy.hasCity = false
        filterSettings.dataCopy.filterCities = arrayListOf()

        getSelectedCitiesFromStorage()
    }
}
