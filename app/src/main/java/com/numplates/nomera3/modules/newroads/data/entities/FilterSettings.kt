package com.numplates.nomera3.modules.newroads.data.entities

import com.meera.core.preferences.AppSettings
import com.numplates.nomera3.data.network.City
import com.numplates.nomera3.modules.registration.domain.model.RegistrationCountryModel
import timber.log.Timber
import java.io.Serializable

class FilterSettings(
    private val prefKey: String,
    private val settings: AppSettings
) : Serializable {

    var data = Data()
    var dataCopy = Data()

    // TODO: https://nomera.atlassian.net/browse/BR-22660
    init {
        try {
            load()
        } catch (e: IllegalStateException){
            Timber.e("FilterSettings $e")
            settings.clearFilterSettings(prefKey)
            load()
        } catch (e: Exception){
            Timber.e("FilterSettings $e")
            settings.clearFilterSettings(prefKey)
        }
    }

    fun commit() {
        data = dataCopy.clone()
        settings.writeFilterSettings(prefKey, data)
    }

    fun load() {
        data = settings.readFilterSettings(prefKey, Data::class.java) ?: Data()
        dataCopy = data.clone()
    }

    fun isCountryFilterEmpty(): Boolean {
        return dataCopy.filterCountries.isEmpty()
    }

    fun setCountrySelected(country: RegistrationCountryModel, selected: Boolean) {
        if (selected) {
            addCountry(country)
        } else {
            removeCountry(country)
        }
    }

    fun addCountry(country: RegistrationCountryModel) {
        val alreadyHasCountry = dataCopy.filterCountries.any { it.id == country.id }
        if (alreadyHasCountry) {
            return
        }

        dataCopy.filterCountries.add(country)
    }

    fun removeCountry(country: RegistrationCountryModel) {
        dataCopy.filterCountries.removeAll { it.id == country.id }
    }

    fun readSelectedCountryCount(): Int {
        return dataCopy.filterCountries.size
    }

    fun hasCountryFilter(countryId: Long?): Boolean {
        return dataCopy.filterCountries.any { it.id == countryId?.toInt() }
    }

    fun isFilterDefaultState(): Boolean {
        val selectedCitiesCount = dataCopy.filterCities.size
        return selectedCitiesCount == 0 && dataCopy.allCountries && !dataCopy.hasCity
    }

    fun getFilterChangesCount(): Int {
        val cityCount = data.filterCities.size
        val countryCount = readSelectedCountryCount()

        return if (cityCount == 0) {
            if (data.allCountries) {
                0
            } else {
                countryCount
            }
        } else {
            cityCount + if (countryCount == 1) 1 else 0
        }
    }

    fun isRecommended(): Boolean {
        return dataCopy.isRecommended ?: false
    }

    data class Data(
        var hasCity: Boolean = false,
        var allCountries: Boolean = true,
        var countrySelected: Boolean = false,
        var filterCountries: ArrayList<RegistrationCountryModel> = arrayListOf(),
        var filterCities: ArrayList<City> = arrayListOf(),
        var showSort: Boolean = false,
        var isRecommended: Boolean? = null,
        var isRecommendedByDefault: Boolean = false
    ) {
        fun clone(): Data {
            return copy().apply {
                filterCities = filterCities.clone() as ArrayList<City>
                filterCountries = filterCountries.clone() as ArrayList<RegistrationCountryModel>
            }
        }
    }
}
