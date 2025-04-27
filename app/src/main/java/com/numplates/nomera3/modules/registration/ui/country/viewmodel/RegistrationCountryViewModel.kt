package com.numplates.nomera3.modules.registration.ui.country.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.modules.registration.domain.GetCountriesUseCase
import com.numplates.nomera3.modules.registration.domain.GetSignupCountriesUseCase
import com.numplates.nomera3.modules.registration.domain.LoadSignupCountriesUseCase
import com.numplates.nomera3.modules.registration.domain.model.RegistrationCountryState
import com.numplates.nomera3.modules.registration.ui.country.fragment.RegistrationCountryFromScreenType
import com.numplates.nomera3.presentation.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class RegistrationCountryViewModel @Inject constructor(
    private val loadSignupCountriesUseCase: LoadSignupCountriesUseCase,
    private val getSignupCountriesUseCase: GetSignupCountriesUseCase,
    private val getCountriesUseCase: GetCountriesUseCase
) : BaseViewModel() {

    private val _stateLiveData: MutableLiveData<RegistrationCountryState> = MutableLiveData()
    val stateLiveData: LiveData<RegistrationCountryState> = _stateLiveData

    fun loadCountries(fromScreenType: RegistrationCountryFromScreenType) {
        viewModelScope.launch {
            when (fromScreenType) {
                RegistrationCountryFromScreenType.Registration -> {
                    runCatching {
                        getSignupCountriesUseCase.invoke()
                            .collectLatest { countries ->
                                if (countries.isEmpty()) reloadCountries()
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
