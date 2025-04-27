package com.numplates.nomera3.modules.registration.ui.location

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.meera.db.models.userprofile.UserProfileNew
import com.numplates.nomera3.App
import com.numplates.nomera3.data.network.City
import com.numplates.nomera3.data.network.Country
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyRegistrationLocationAutocomplete
import com.numplates.nomera3.modules.maps.domain.usecase.ObserveUserLocationUseCase
import com.numplates.nomera3.modules.maps.domain.usecase.ReadLastLocationFromStorageUseCase
import com.numplates.nomera3.modules.registration.data.RegistrationUserData
import com.numplates.nomera3.modules.registration.domain.GetCitiesParams
import com.numplates.nomera3.modules.registration.domain.GetCitiesUseCase
import com.numplates.nomera3.modules.registration.domain.GetCountriesUseCaseNew
import com.numplates.nomera3.modules.registration.domain.UploadUserDataUseCase
import com.numplates.nomera3.modules.registration.domain.UserDataUseCase
import com.numplates.nomera3.modules.registration.ui.RegistrationBaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class RegistrationLocationViewModel : RegistrationBaseViewModel() {

    val liveData = MutableLiveData<RegistrationLocationViewState>()

    @Inject
    lateinit var getCountriesUseCase: GetCountriesUseCaseNew

    @Inject
    lateinit var getCitiesUseCase: GetCitiesUseCase

    @Inject
    lateinit var userUseCase: UserDataUseCase

    @Inject
    lateinit var uploadUserDataUseCase: UploadUserDataUseCase

    @Inject
    lateinit var readLastLocationFromStorageUseCase: ReadLastLocationFromStorageUseCase

    @Inject
    lateinit var observeUserLocationUseCase: ObserveUserLocationUseCase

    @Inject
    lateinit var amplitudeHelper: AnalyticsInteractor

    private val countries = mutableListOf<Country>()
    private val cities = mutableListOf<City>()

    private val _locationViewEvent = MutableSharedFlow<RegistrationCityViewEvent>()
    val locationViewEvent: SharedFlow<RegistrationCityViewEvent> = _locationViewEvent

    init {
        App.getRegistrationComponent().inject(this)
    }

    fun getUserLocationFlow() = observeUserLocationUseCase.invoke()
    fun readLastLocation() = readLastLocationFromStorageUseCase.invoke()

    fun showCountries() {
        val country = countries.find { it.countryId == userUseCase.userData?.country?.countryId }
        if (countries.isNotEmpty()) {
            pushUiState(RegistrationLocationViewState.ShowCountriesSelector(country))
            emitViewEvent(RegistrationCityViewEvent.ShowCountriesDialogEvent)
        } else {
            getCountries(needToShow = true)
        }
    }

    fun setCountry(country: Country) {
        if (userUseCase.userData?.country == country) return
        userUseCase.userData?.city = null
        userUseCase.userData?.country = country
        pushUiState(
            RegistrationLocationViewState.Address(
                countryName = country.name,
                cityName = null
            )
        )
    }

    fun showCities() {
        if (userUseCase.userData?.country == null) return
        getCities(
            countryId = userUseCase.userData?.country?.countryId?.toLong(),
            query = null,
            showCitiesSelector = true
        )
    }

    fun setCity(city: City) {
        userUseCase.userData?.city = city
        pushUiState(
            RegistrationLocationViewState.Address(
                countryName = city.countryName,
                cityName = city.title_
            )
        )
    }

    fun setCountryCityFromLocation(countryCity: Pair<String?, String?>) {
        userUseCase.userData?.country = countries.find { it.name == countryCity.first } ?: return
        getCities(
            countryId = userUseCase.userData?.country?.countryId?.toLong(),
            query = countryCity.second,
            fromLocation = true
        )
    }

    fun getCountriesList(): List<Country> {
        return countries
    }

    fun getCitiesList(): List<City> {
        return cities
    }

    fun getCountries(needToShow: Boolean = false) {
        progress(true)
        viewModelScope.launch(Dispatchers.IO) {
            getCountriesUseCase.execute(DefParams(),
                success = {
                    countries.clear()
                    countries.addAll(it)
                    when {
                        needToShow -> {
                            val country = countries.find { c ->
                                c.countryId == userUseCase.userData?.country?.countryId
                            }
                            pushUiState(RegistrationLocationViewState.ShowCountriesSelector(country))
                        }
                        userUseCase.userData?.isLocationExist() != true ->
                            pushUiState(RegistrationLocationViewState.CheckLocation)
                    }
                    progress(false)
                },
                fail = {
                    progress(false)
                    Timber.e(it)
                }
            )
        }
    }

    private fun getCities(
        countryId: Long?,
        query: String?,
        fromLocation: Boolean = false,
        showCitiesSelector: Boolean = false
    ) {
        progress(true)
        viewModelScope.launch(Dispatchers.IO) {
            getCitiesUseCase.execute(GetCitiesParams(countryId, query),
                success = {
                    cities.clear()
                    cities.addAll(it)
                    userUseCase.userData?.locationAutocomplete = fromLocation
                    when {
                        cities.isEmpty() -> Unit
                        fromLocation -> {
                            userUseCase.userData?.city = cities.first()
                            pushUiState(
                                RegistrationLocationViewState.Address(
                                    countryName = cities.first().countryName,
                                    cityName = cities.first().title_
                                )
                            )
                        }
                        showCitiesSelector -> {
                            val city = cities.find { c ->
                                c.cityId == userUseCase.userData?.city?.cityId
                            }
                            pushUiState(RegistrationLocationViewState.ShowCitiesSelector(city))
                            emitViewEvent(RegistrationCityViewEvent.ShowCitiesDialogEvent)
                        }
                    }
                    progress(false)
                },
                fail = {
                    progress(false)
                    Timber.e(it)
                }
            )
        }
    }

    fun continueClicked() = uploadUserData(
        RegistrationUserData(
            country = userUseCase.userData?.country,
            city = userUseCase.userData?.city
        )
    )

    fun clearAll() {
        pushUiState(RegistrationLocationViewState.None)
    }

    private fun emitViewEvent(typeEvent: RegistrationCityViewEvent) {
        viewModelScope.launch {
            _locationViewEvent.emit(typeEvent)
        }
    }

    private fun pushUiState(state: RegistrationLocationViewState) {
        liveData.postValue(state)
    }

    override val userDataUseCase: UserDataUseCase
        get() = userUseCase

    override val uploadUserUseCase: UploadUserDataUseCase
        get() = uploadUserDataUseCase

    override fun userDataInitialized() {
        pushUiState(
            RegistrationLocationViewState.Address(
                userUseCase.userData?.country?.name,
                userUseCase.userData?.city?.title_
            )
        )
        getCountries()
    }

    override fun uploadSuccess(userProfile: UserProfileNew) {
        val autocomplete = if (userUseCase.userData?.locationAutocomplete == true) {
            AmplitudePropertyRegistrationLocationAutocomplete.TRUE
        } else {
            AmplitudePropertyRegistrationLocationAutocomplete.FALSE
        }
        amplitudeHelper.logRegistrationLocationSelected(autocomplete)
        emitViewEvent(RegistrationCityViewEvent.GoToNextStep)
    }
}
