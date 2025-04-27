package com.numplates.nomera3.modules.registration.ui.country.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.modules.registration.domain.GetCountriesUseCase
import com.numplates.nomera3.modules.registration.domain.GetSignupCountriesUseCase
import com.numplates.nomera3.modules.registration.domain.LoadSignupCountriesUseCase
import com.numplates.nomera3.modules.registration.domain.model.RegistrationCountryModel
import com.numplates.nomera3.modules.registration.domain.model.RegistrationCountryState
import com.numplates.nomera3.modules.registration.ui.country.fragment.RegistrationCountryFromScreenType
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class MeeraRegistrationCountryViewModel @Inject constructor(
    private val loadSignupCountriesUseCase: LoadSignupCountriesUseCase,
    private val getSignupCountriesUseCase: GetSignupCountriesUseCase,
    private val getCountriesUseCase: GetCountriesUseCase
) : ViewModel() {

    private val _stateLiveData: MutableLiveData<RegistrationCountryState> = MutableLiveData()
    val stateLiveData: LiveData<RegistrationCountryState> = _stateLiveData

    private val loadedCountries = mutableListOf<RegistrationCountryModel>()

    fun loadCountries(fromScreenType: RegistrationCountryFromScreenType) {
        viewModelScope.launch {
            when (fromScreenType) {
                RegistrationCountryFromScreenType.Registration -> {
                    runCatching {
                        getSignupCountriesUseCase.invoke()
                            .collectLatest { countries ->
                                if (countries.isEmpty()) reloadCountries()
                                loadedCountries.clear()
                                loadedCountries.addAll(countries)
                                _stateLiveData.postValue(RegistrationCountryState.RegistrationCountryList(countries))
                            }
                    }.onFailure { t ->
                        Timber.e(t)
                        reloadCountries()
                    }
                }
                RegistrationCountryFromScreenType.Transport,
                RegistrationCountryFromScreenType.Profile -> {
                    runCatching {
                        val countries = getCountriesUseCase.invoke()
                        _stateLiveData.postValue(RegistrationCountryState.RegistrationCountryList(countries))
                    }.onFailure { t ->
                        Timber.e(t)
                    }
                }
            }
        }
    }

    fun searchCountries(query: String) {
        if (query.isBlank()) {
            _stateLiveData.postValue(RegistrationCountryState.RegistrationCountryList(loadedCountries))
            return
        }
        val filteredCountries = loadedCountries.filter {
            val doesNameContainQuery = it.name.contains(query, true)
            val doesCodeContainQuery = it.code?.contains(query, true) == true
            doesCodeContainQuery || doesNameContainQuery
        }
        _stateLiveData.postValue(RegistrationCountryState.RegistrationCountryList(filteredCountries))
    }

    private fun reloadCountries() {
        viewModelScope.launch {
            runCatching {
                loadSignupCountriesUseCase.invoke()
            }.onFailure {
                Timber.e(it)
            }
        }
    }

}
