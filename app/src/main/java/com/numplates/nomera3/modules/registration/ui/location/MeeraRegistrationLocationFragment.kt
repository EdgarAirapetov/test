package com.numplates.nomera3.modules.registration.ui.location

import android.Manifest
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.dp
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.utils.KeyboardHeightProvider
import com.meera.core.utils.LocationUtility
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentRegistrationLocationBinding
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseFragment
import com.numplates.nomera3.modules.registration.ui.RegistrationContainerFragment
import com.numplates.nomera3.modules.registration.ui.RegistrationNavigationViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import timber.log.Timber
import java.util.Locale

private const val TAG_COUNTRIES = "COUNTRIES"
private const val TAG_CITIES = "CITIES"

private const val MARGIN_BUTTON = 16

class MeeraRegistrationLocationFragment : MeeraBaseFragment(R.layout.meera_fragment_registration_location) {

    private val binding by viewBinding(MeeraFragmentRegistrationLocationBinding::bind)

    private var keyboardHeightProvider: KeyboardHeightProvider? = null
    private val act by lazy { activity as MeeraAct }
    private val viewModel by viewModels<RegistrationLocationViewModel>()
    private val navigationViewModel by viewModels<RegistrationNavigationViewModel>(
        ownerProducer = { requireParentFragment() }
    )

    private val argCountryNumber by lazy { requireArguments().getString(RegistrationContainerFragment.ARG_COUNTRY_NUMBER) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
        observeViewEvent()
        initView()
        viewModel.initUserData()
    }

    override fun onStart() {
        super.onStart()
        keyboardHeightProvider?.release()
        keyboardHeightProvider = KeyboardHeightProvider(binding.root)
        keyboardHeightProvider?.observer = { height ->
            binding.btnContinue.setMargins(bottom = height + MARGIN_BUTTON.dp, end = MARGIN_BUTTON.dp)
        }
    }

    override fun onStop() {
        super.onStop()
        keyboardHeightProvider?.release()
    }

    override fun onResume() {
        super.onResume()
        context?.hideKeyboard(requireView())
    }

    private fun initView() {
        binding.btnBack.setThrottledClickListener {
            navigationViewModel.goBack()
        }
        binding.btnContinue.setThrottledClickListener {
            viewModel.continueClicked()
        }
        binding.tvCountry.setThrottledClickListener { viewModel.showCountries() }
        binding.tvCity.setThrottledClickListener { viewModel.showCities() }
        setContinueButtonAvailability(false)
    }

    private fun observeViewModel() {
        viewModel.liveData.observe(viewLifecycleOwner) { state ->
            when (state) {
                is RegistrationLocationViewState.CheckLocation -> checkLocationPermission()
                is RegistrationLocationViewState.ShowCountriesSelector -> {
                    setCountryName(state.selectedCountry?.name)
                }

                is RegistrationLocationViewState.ShowCitiesSelector -> {
                    setCityName(state.selectedCity?.title_)
                }

                is RegistrationLocationViewState.Address -> {
                    setCountryName(state.countryName)
                    setCityName(state.cityName)
                }

                else -> {}
            }
        }
        viewModel.progressLiveData.observe(viewLifecycleOwner) { inProgress ->
            handleProgress(inProgress)
            setContinueButtonAvailability(isLocationFilled())
        }
    }

    private fun observeViewEvent() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.locationViewEvent.collect { event ->
                handleEvent(event)
            }
        }
    }

    private fun handleEvent(event: RegistrationCityViewEvent) {
        when (event) {
            RegistrationCityViewEvent.ShowCitiesDialogEvent -> showCitiesSelector()
            RegistrationCityViewEvent.ShowCountriesDialogEvent -> showCountriesSelector()
            RegistrationCityViewEvent.GoToNextStep -> {
                viewModel.clearAll()
                navigationViewModel.registrationLocationNext(argCountryNumber)
            }
        }
    }

    private fun setCountryName(countryName: String?) {
        binding.tvCountry.text =
            if (countryName.isNullOrEmpty()) getString(R.string.meera_choose_country)
            else countryName
    }

    private fun setCityName(cityName: String?) {
        if (cityName.isNullOrEmpty()) {
            binding.tvCity.text = getString(R.string.meera_choose_city)
            setContinueButtonAvailability(false)
        } else {
            binding.tvCity.text = cityName.replace("-", "-" + System.lineSeparator())
            setContinueButtonAvailability(true)
        }
    }

    private fun isLocationFilled(): Boolean {
        return binding.tvCity.text != getString(R.string.meera_choose_city)
    }

    private fun setContinueButtonAvailability(enabled: Boolean) {
        binding.btnContinue.isEnabled = enabled
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}

    private fun checkLocationPermission() {
        if (LocationUtility.checkPermissionLocation(act)) {
            getCountryAndCityFromLocation()
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
            subscribeToLocation()
        }
    }

    private fun getCountryAndCityFromLocation() {
        val location = viewModel.readLastLocation() ?: return
        getAddressFromLocation(
            latitude = location.lat,
            longitude = location.lon
        )
    }

    private fun subscribeToLocation() {
        viewModel.getUserLocationFlow()
            .take(1)
            .onEach { getAddressFromLocation(it.lat, it.lon) }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun getAddressFromLocation(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val addresses: List<Address>?
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val countryCity =
                    Pair(addresses.first()?.countryName, addresses.first()?.locality)
                viewModel.setCountryCityFromLocation(countryCity)
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun showCountriesSelector() {
        MeeraCountriesBottomSheetFragment()
            .show(childFragmentManager, TAG_COUNTRIES)
    }

    private fun showCitiesSelector() {
        MeeraCitiesBottomSheetFragment()
            .show(childFragmentManager, TAG_CITIES)
    }

    private fun handleProgress(inProgress: Boolean) {
        binding.tvCountry.isEnabled = !inProgress
        binding.tvCity.isEnabled = !inProgress
    }
}
