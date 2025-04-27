package com.numplates.nomera3.modules.registration.ui.location

import android.Manifest
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.meera.core.extensions.click
import com.meera.core.extensions.clickAnimateScaleUp
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.utils.LocationUtility
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentRegistrationLocationBinding
import com.numplates.nomera3.modules.registration.ui.RegistrationContainerFragment
import com.numplates.nomera3.modules.registration.ui.RegistrationNavigationViewModel
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.view.utils.PermissionManager
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import timber.log.Timber
import java.util.Locale

class RegistrationLocationFragment : BaseFragmentNew<FragmentRegistrationLocationBinding>() {

    private val disposable = CompositeDisposable()
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

    override fun onResume() {
        super.onResume()
        context?.hideKeyboard(requireView())
    }

    override fun onStop() {
        super.onStop()
        disposable.clear()
    }

    private fun initView() {
        binding?.ivBackIcon?.click {
            it.clickAnimateScaleUp()
            navigationViewModel.goBack()
        }
        binding?.cvNextButton?.click {
            viewModel.continueClicked()
        }
        binding?.tvCountry?.setThrottledClickListener { viewModel.showCountries() }
        binding?.llCityContainer?.setThrottledClickListener { viewModel.showCities() }
        binding?.llCityContainer?.alpha = ALPHA_UNSELECTED
        binding?.tvStep?.text = getString(R.string.registration_step_count, STEP)
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
        binding?.tvCountry?.text =
            if (countryName.isNullOrEmpty()) getString(R.string.country)
            else countryName
    }

    private fun setCityName(cityName: String?) {
        if (cityName.isNullOrEmpty()) {
            binding?.tvCity?.text = getString(R.string.city)
            binding?.llCityContainer?.alpha = ALPHA_UNSELECTED
            binding?.tvCity?.requestLayout()
            setContinueButtonAvailability(false)
        } else {
            binding?.tvCity?.text = cityName.replace("-", "-" + System.lineSeparator())
            binding?.llCityContainer?.alpha = ALPHA_SELECTED
            setContinueButtonAvailability(true)
        }
    }

    private fun isLocationFilled(): Boolean {
        return binding?.tvCity?.text != getString(R.string.city)
    }

    private fun setContinueButtonAvailability(enabled: Boolean) {
        binding?.cvNextButton?.isEnabled = enabled
    }

    private fun checkLocationPermission() {
        if (LocationUtility.checkPermissionLocation(act)) {
            getCountryAndCityFromLocation()
        } else {
            act.requestRuntimePermission(
                PermissionManager.PERMISSION_LOCATION_CODE, arrayOf(
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
        CountriesBottomSheetFragment()
            .show(this@RegistrationLocationFragment.childFragmentManager, tag)
    }

    private fun showCitiesSelector() {
        CitiesBottomSheetFragment()
            .show(this@RegistrationLocationFragment.childFragmentManager, tag)
    }

    private fun handleProgress(inProgress: Boolean) {
        binding?.tvCountry?.isEnabled = !inProgress
        binding?.tvCity?.isEnabled = !inProgress
        binding?.cvNextButton?.showProgress(inProgress)
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentRegistrationLocationBinding
        get() = FragmentRegistrationLocationBinding::inflate

    companion object {
        const val STEP = "4"
        private const val ALPHA_SELECTED = 1F
        private const val ALPHA_UNSELECTED = .6F
    }
}
