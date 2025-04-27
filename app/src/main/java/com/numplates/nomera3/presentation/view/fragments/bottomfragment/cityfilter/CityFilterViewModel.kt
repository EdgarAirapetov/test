package com.numplates.nomera3.presentation.view.fragments.bottomfragment.cityfilter

import com.numplates.nomera3.App
import com.numplates.nomera3.data.network.City
import com.numplates.nomera3.domain.interactornew.CitySuggestionUseCase
import com.numplates.nomera3.modules.newroads.data.entities.FilterSettingsProvider
import com.numplates.nomera3.presentation.viewmodel.BaseViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.SingleLiveEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CityFilterViewModel(filterSettingsType: FilterSettingsProvider.FilterType) : BaseViewModel() {

    @Inject
    lateinit var citySuggestionUseCase: CitySuggestionUseCase

    @Inject
    lateinit var filterSettingsProvider: FilterSettingsProvider

    init {
        App.component.inject(this)
    }

    val filterSettings = filterSettingsProvider.get(filterSettingsType)

    var foundCities = SingleLiveEvent<MutableList<FoundCityModel>>()

    var showResultList = SingleLiveEvent<Boolean>()
    var showLimitAlert = SingleLiveEvent<Unit?>()
    var showApplyButton = SingleLiveEvent<Boolean>()
    var showNoResultPlaceholder = SingleLiveEvent<Boolean>()

    private var resultCityList = mutableListOf<FoundCityModel>()
    private var selectedCityList = mutableListOf<FoundCityModel>()
    private var previouslySavedCityList = mutableListOf<FoundCityModel>()

    private var searchCityDisposable: Disposable? = null

    fun initializeFilter(isGetSavedResult: Boolean) {
        if (isGetSavedResult) {
            val savedSelectedCitiesFromStorage = getSavedSelectedCitiesFromStorage()
            previouslySavedCityList = savedSelectedCitiesFromStorage.toMutableList()
            selectedCityList = savedSelectedCitiesFromStorage.toMutableList()
        }

        showResultList.value = true
        showApplyButton.value = false
        showNoResultPlaceholder.value = false

        foundCities.value = selectedCityList
    }

    fun resetFilter() {
        showResultList.value = true
        showNoResultPlaceholder.value = false
        showApplyButton.value = previouslySavedCityList != selectedCityList

        foundCities.value = selectedCityList
    }

    fun saveSelectedCitiesStorage() {
        selectedCityList.map { city: FoundCityModel ->
            City(
                cityId = city.cityId,
                name = city.cityName,
                countryId = city.countryId,
                title_ = city.title,
                countryName = city.countryName
            )
        }.let { mappedSelectedCities: List<City> ->
            filterSettings.dataCopy.filterCities = ArrayList(mappedSelectedCities)
        }
    }

    fun getSelectedCities(): List<City> {
        return selectedCityList.map { city: FoundCityModel ->
            City(
                cityId = city.cityId,
                name = city.cityName,
                countryId = city.countryId,
                title_ = city.title,
                countryName = city.countryName
            )
        }
    }

    private fun getSavedSelectedCitiesFromStorage(): List<FoundCityModel> {
        val returnValue = filterSettings.dataCopy.filterCities.let { citiesFromStorage: ArrayList<City> ->
            convertFilterFoundCities(citiesFromStorage).map {
                it.isSelected = true
                it
            }
        }

        filterSettings.commit()

        return returnValue
    }

    // todo should be refactor as suspend fun with runWithDispatcherIO (...)
    fun findCities(query: String? = null) {
        if (query != null && query.isNotEmpty() && query.isNotBlank()) {
            searchCityDisposable?.takeIf { !it.isDisposed }?.dispose()
            searchCityDisposable = citySuggestionUseCase.searchCity(query.trim())?.let { response ->
                response.debounce(400, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    .map { convertFilterFoundCities(it.data) }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { newFoundCities: MutableList<FoundCityModel> ->
                            showNoResultPlaceholder.value = newFoundCities.isEmpty()
                            showResultList.value = newFoundCities.isNotEmpty()

                            resultCityList.clear()
                            resultCityList.addAll(newFoundCities)

                            updateCityListAfterSearch()
                        },
                        { e: Throwable? ->
                            e?.printStackTrace()
                        }
                    )
            }
        }
    }

    fun onCityClicked(city: FoundCityModel) {
        if (selectedCityList.contains(city)) {
            selectedCityList.remove(city)
        } else {
            if (selectedCityList.size < 20) {
                selectedCityList.add(city.copy(isSelected = true))
            } else {
                showLimitAlert.call()
            }
        }

        showApplyButton.value = previouslySavedCityList != selectedCityList

        updateCityListAfterSearch()
    }


    private fun updateCityListAfterSearch() {
        foundCities.value = addSelectedCitiesToStartOfList(removeDuplicates())
    }

    private fun addSelectedCitiesToStartOfList(newResultCityList: MutableList<FoundCityModel>): MutableList<FoundCityModel> {
        newResultCityList.addAll(selectedCityList)
        newResultCityList.sortByDescending {
            it.isSelected
        }
        return newResultCityList
    }

    private fun removeDuplicates(): MutableList<FoundCityModel> {
        val selectedCityIds = selectedCityList.map { it.cityId }
        val duplicatedCities = resultCityList.filter { selectedCityIds.contains(it.cityId) }
        val newResultCityList = resultCityList.toMutableList()

        newResultCityList.removeAll(duplicatedCities)
        return newResultCityList
    }

    private fun convertFilterFoundCities(foundCities: MutableList<City>?): MutableList<FoundCityModel> {
        return foundCities?.map {
            FoundCityModel(
                cityId = it.cityId,
                cityName = it.name ?: "",
                countryId = it.countryId,
                countryName = it.countryName ?: "",
                title = it.title_ ?: "",
                isSelected = false
            )
        }?.toMutableList() ?: mutableListOf()
    }

    override fun onCleared() {
        super.onCleared()
        searchCityDisposable?.takeIf { !it.isDisposed }?.dispose()
    }
}
