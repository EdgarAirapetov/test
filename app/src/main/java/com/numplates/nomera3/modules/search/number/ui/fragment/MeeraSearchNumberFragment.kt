package com.numplates.nomera3.modules.search.number.ui.fragment

import android.os.Bundle
import android.text.TextWatcher
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.clearText
import com.meera.core.extensions.empty
import com.meera.core.extensions.setMargins
import com.meera.core.utils.KeyboardHeightProvider
import com.meera.db.models.userprofile.VehicleType
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.state.ErrorSnakeState
import com.meera.uikit.snackbar.state.SnackBarParams
import com.noomeera.nmrmediatools.extensions.hideKeyboard
import com.noomeera.nmrmediatools.utils.setThrottledClickListener
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentSearchNumberBinding
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.modules.redesign.util.NavigationManager
import com.numplates.nomera3.modules.search.ui.fragment.MeeraSearchMainFragment
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.getCountryFlag
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.numbersearch.NumberSearchParameters
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.numbersearch.NumberSearchViewEvent
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.numbersearch.NumberSearchViewModel
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.roadfilter.getCountryFilterItem
import com.numplates.nomera3.presentation.view.widgets.numberplateview.getNumberPlateEnum
import com.numplates.nomera3.presentation.view.widgets.numberplateview.maskformatter.MeeraMaskFormatter
import java.util.Locale

class MeeraSearchNumberFragment : MeeraBaseDialogFragment(
    R.layout.meera_fragment_search_number,
    ScreenBehaviourState.BottomScreensWrapContent
) {

    private val binding by viewBinding(MeeraFragmentSearchNumberBinding::bind)

    override val containerId: Int
        get() = R.id.fragment_second_container_view

    private val viewModel by viewModels<NumberSearchViewModel> { App.component.getViewModelFactory() }
    private var maskTextWatcher: TextWatcher? = null
    private var buttonsTextWatcher: TextWatcher? = null
    private var keyboardHeightProvider: KeyboardHeightProvider? = null

    private val searchMainFragment: MeeraSearchMainFragment?
        get() = NavigationManager.getManager().topNavHost.childFragmentManager.fragments.lastOrNull() as? MeeraSearchMainFragment?
    private val searchNumberListener: SearchNumberListener?
        get() = searchMainFragment?.childFragmentManager?.fragments?.firstOrNull { it is SearchNumberListener } as? SearchNumberListener?

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initKeyboardHeightProvider()
        initView()
    }

    override fun onStateChanged(newState: Int) {
        if (newState == BottomSheetBehavior.STATE_HIDDEN) {
            activity?.onBackPressedDispatcher?.onBackPressed()
            keyboardHeightProvider?.release()
            hideKeyboard()
        }
    }

    private fun initKeyboardHeightProvider() {
        keyboardHeightProvider = KeyboardHeightProvider(binding.root, true)
        keyboardHeightProvider?.start()
        keyboardHeightProvider?.observer = { height ->
            binding.root.setMargins(bottom = height)
        }
    }

    private fun initView() {
        binding.root.setThrottledClickListener {}
        setupObservables()
        addVehicleTypeClickListeners()
        addCountryClickListener()
        initReset()
        initShowButton()
        setupNumberView()
        updateCountryFilterContainer()
        binding.btnClose.setThrottledClickListener { activity?.onBackPressedDispatcher?.onBackPressed() }
    }

    private fun setupObservables() {
        viewModel.liveData.observe(viewLifecycleOwner) { event ->
            when (event) {
                is NumberSearchViewEvent.Reset -> {
                    setupNumberView()
                    updateCountryFilterContainer()
                }
            }
        }
    }

    private fun updateCountryFilterContainer() {
        setResetButtonState()
        setShowButtonState()
    }

    override fun onResume() {
        super.onResume()
        searchNumberListener?.onSearchNumberOpened()
    }

    override fun onHidden() {
        super.onHidden()
        searchNumberListener?.onSearchNumberClosed()
    }

    private fun setupNumberView() {
        binding.ukciSearchNumber.etInput.clearText()
        binding.ukciSearchNumber.etInput.isAllCaps = true
        binding.ukciSearchNumber.etInputMask.clearText()
        val countryFlag = getCountryFlag(getCountryFilterItem(viewModel.countryId))
        Glide.with(requireContext()).load(countryFlag).circleCrop().into(binding.ukciSearchNumber.ivIcon)

        val existNumPlateEnum = getNumberPlateEnum(viewModel.vehicleTypeId, viewModel.countryId) ?: return
        binding.ukciSearchNumber.etInput.apply {

            val mask = StringBuilder().apply {
                existNumPlateEnum.prefixPattern?.let { append(it) }
                append(existNumPlateEnum.numPattern)
                existNumPlateEnum.suffixPattern?.let { append(it) }
                if (!existNumPlateEnum.regionPattern.isNullOrEmpty()) {
                    append(" ")
                    append(existNumPlateEnum.regionPattern)
                }
            }.toString().uppercase()
            if (hint != null && hint.toString() == mask) return
            removeTextChangedListener(maskTextWatcher)
            removeTextChangedListener(buttonsTextWatcher)
            hint = mask
            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    setSelection(text.length)
                }
            }

            maskTextWatcher = MeeraMaskFormatter(
                mask = mask.lowercase(),
                maskedField = this,
                dynamicMask = binding.ukciSearchNumber.etInputMask,
                pattern = existNumPlateEnum.regexPattern
            )
            addTextChangedListener(maskTextWatcher)
            buttonsTextWatcher = addTextChangedListener {
                setResetButtonState()
                setShowButtonState()
            }
            setText(String.empty())
        }
        setResetButtonState()
        setShowButtonState()
    }

    private fun addVehicleTypeClickListeners() {
        binding.apply {
            rgSearchNumberType.addOnRadioButtonChosenListener {
                when (it.id) {
                    R.id.rcv_type_car -> {
                        viewModel.saveVehicleTypeId(VehicleType.TYPE_ID_CAR)
                    }

                    R.id.rcv_type_moto -> {
                        viewModel.saveVehicleTypeId(VehicleType.TYPE_ID_MOTO)
                    }
                }
                setResetButtonState()
                setShowButtonState()
            }
        }
    }

    private fun addCountryClickListener() {
        parentFragmentManager.setFragmentResultListener(KEY_NUMBER_SEARCH_COUNTRY, viewLifecycleOwner) { _, result ->
            if (result.containsKey(KEY_NUMBER_SEARCH_COUNTRY)) {
                val countryId = result.getLong(KEY_NUMBER_SEARCH_COUNTRY)
                viewModel.saveSelectedCountry(getCountryFilterItem(countryId))
            }
        }
        binding.ukciSearchNumber.setLeftIconClickListener {
            selectCountry()
        }
    }

    private fun selectCountry() {
        findNavController().navigate(R.id.action_meeraSearchNumberFragment_to_meeraSearchNumberCountriesFragment)
    }

    private fun initReset() {
        binding.apply {
            btnSearchNumberReset.setThrottledClickListener {
                ukciSearchNumber.etInput.clearText()
                rcvTypeCar.performClick()
                viewModel.setDefaults()
            }
        }
    }

    private fun initShowButton() {
        binding.btnSearchNumberShow.setThrottledClickListener {
            val number = binding.ukciSearchNumber.etInput.text.toString().replace(" ", "")
            if (number.isEmpty()) {
                UiKitSnackBar.make(
                    requireView(),
                    SnackBarParams(
                        errorSnakeState = ErrorSnakeState(getText(R.string.error_number_is_empty))
                    )
                ).show()
            } else {
                val parameters = NumberSearchParameters(
                    number = number,
                    vehicleTypeId = viewModel.vehicleTypeId,
                    countryId = viewModel.countryId,
                    countryName = viewModel.selectedCountry.name.lowercase(Locale.getDefault())
                )
                viewModel.saveSearchParameters(parameters)
                activity?.onBackPressedDispatcher?.onBackPressed()
            }
        }
    }

    private fun setResetButtonState() {
        binding.btnSearchNumberReset.isEnabled = isNumberInputEmpty()
    }

    private fun setShowButtonState() {
        binding.btnSearchNumberShow.isEnabled = isNumberInputEmpty()
    }

    private fun isNumberInputEmpty(): Boolean {
        return binding.ukciSearchNumber.etInput.text.length >= MIN_VALID_NUM
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    companion object {
        const val MIN_VALID_NUM = 1
    }
}


interface SearchNumberListener {
    fun onSearchNumberOpened()
    fun onSearchNumberClosed()
}
