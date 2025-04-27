package com.numplates.nomera3.presentation.view.fragments.bottomfragment.numbersearch

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.numplates.nomera3.R
import com.meera.db.models.userprofile.VehicleCountry
import com.meera.db.models.userprofile.VehicleEntity
import com.meera.db.models.userprofile.VehicleType
import com.numplates.nomera3.databinding.BottomSheetSearchNumberBinding
import com.numplates.nomera3.presentation.router.BaseBottomSheetDialogFragment
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.roadfilter.CountryFilterItem
import com.numplates.nomera3.presentation.view.utils.NToast
import com.meera.core.extensions.click
import com.meera.core.extensions.empty
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.createCountryFilterItemView
import com.numplates.nomera3.presentation.view.widgets.numberplateview.NumberPlateEditView
import timber.log.Timber
import java.util.*


class NumberSearchBottomSheetFragment(
    private val callback: NumberSearchCallback
) :
    BaseBottomSheetDialogFragment<BottomSheetSearchNumberBinding>() {

    private val viewModel by viewModels<NumberSearchViewModel>()
    private var isNumberPrefixEmpty = true
    private var isNumberSuffixEmpty = true
    private var isNumberRegionEmpty = true
    private var isNumberEmpty = true

    private val countryFilterItems = listOf(
        CountryFilterItem.RUSSIA,
        CountryFilterItem.ARMENIA,
        CountryFilterItem.BELARUS,
        CountryFilterItem.GEORGIA,
        CountryFilterItem.KAZAKHSTAN,
        CountryFilterItem.UKRAINE
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    override fun onDestroy() {
        super.onDestroy()
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED)
    }

    private fun initView() {
        setupObservables()
        addVehicleTypeClickListeners()
        fillCountryFilterByItems()
        addCountryFilterClickListeners()
        initReset()
        initShowButton()
        setupNumberView()
        updateCountryFilterContainer()
    }

    private fun setupObservables() {
        viewModel.liveData.observe(viewLifecycleOwner, { event ->
            when (event) {
                is NumberSearchViewEvent.Reset -> {
                    setupNumberView()
                    updateCountryFilterContainer()
                }
            }
        })
    }

    private fun updateCountryFilterContainer() {
        binding?.countryFilter
            ?.children
            ?.forEach { filterItemView: View ->
                val roadCountryFilterItem = filterItemView.tag as CountryFilterItem
                filterItemView.isSelected = viewModel.selectedCountry == roadCountryFilterItem
            }
        setResetButtonState()
        setShowButtonState()
    }

    private fun setupNumberView() {
        Timber.d(
            "Setup NumberView: vehicle: ${viewModel.vehicleTypeId}" +
                    " country: ${viewModel.countryId}"
        )

        val vehicle = VehicleEntity(
            "",
            VehicleType(viewModel.vehicleTypeId),
            VehicleCountry(viewModel.countryId)
        )
        binding?.numberEditor?.let {
            NumberPlateEditView.Builder(it)
                .setVehicleNewGray(vehicle)
                .build()
        }
        binding?.numberEditor?.etNum?.setFocusAndChangeListener(NumberPlateEditView.NUMBER)
        binding?.numberEditor?.etPrefix?.setFocusAndChangeListener(NumberPlateEditView.PREFIX)
        binding?.numberEditor?.etSuffix?.setFocusAndChangeListener(NumberPlateEditView.SUFFIX)
        binding?.numberEditor?.etRegion?.setFocusAndChangeListener(NumberPlateEditView.REGION)
        binding?.numberEditor?.etPrefix?.setText(String.empty())
        binding?.numberEditor?.etSuffix?.setText(String.empty())
        binding?.numberEditor?.etNum?.setText(String.empty())
        binding?.numberEditor?.etRegion?.setText(String.empty())
        setResetButtonState()
        setShowButtonState()
    }

    private fun EditText.setFocusAndChangeListener(field: Int) {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val isEmpty = s?.isEmpty() == true
                when (field) {
                    NumberPlateEditView.PREFIX -> isNumberPrefixEmpty = isEmpty
                    NumberPlateEditView.SUFFIX -> isNumberSuffixEmpty = isEmpty
                    NumberPlateEditView.REGION -> isNumberRegionEmpty = isEmpty
                    NumberPlateEditView.NUMBER -> isNumberEmpty = isEmpty
                }
                setResetButtonState()
                setShowButtonState()
            }

            override fun afterTextChanged(s: Editable?) = Unit

        })
    }

    private fun addVehicleTypeClickListeners() {
        binding?.apply {
            btnVehicleCar.click {
                setVehicleTypeButton((it as TextView), vehicleTypeButtonContainer)
                viewModel.saveVehicleTypeId(VehicleType.TYPE_ID_CAR)
            }
            btnVehicleMoto.click {
                setVehicleTypeButton((it as TextView), vehicleTypeButtonContainer)
                viewModel.saveVehicleTypeId(VehicleType.TYPE_ID_MOTO)
            }
        }
    }

    private fun setVehicleTypeButton(currentButton: TextView, container: LinearLayout) {
        container.children.forEach { view ->
            view.setBackgroundResource(R.drawable.road_filter_country_item_bg)
            (view as TextView).setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.ui_text_gray
                )
            )
        }

        currentButton.setBackgroundResource(R.drawable.gradient_purple_button_ripple)
        currentButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.ui_white))
        setResetButtonState()
        setShowButtonState()
    }

    private fun fillCountryFilterByItems() {
        countryFilterItems.forEach { filterCountryItem: CountryFilterItem ->
            binding?.countryFilter?.addView(
                requireContext().createCountryFilterItemView(
                    filterCountryItem
                )
            )
        }
    }

    private fun addCountryFilterClickListeners() {
        binding?.countryFilter?.children?.forEach { filterItemView: View ->
            filterItemView.setOnClickListener {
                viewModel.saveSelectedCountry(it.tag as CountryFilterItem)
            }
        }
    }

    private fun initReset() {
        binding?.resetFilter?.click {
            binding?.apply {
                numberEditor.etPrefix?.setText(String.empty())
                numberEditor.etSuffix?.setText(String.empty())
                numberEditor.etNum?.setText(String.empty())
                numberEditor.etRegion?.setText(String.empty())
                setVehicleTypeButton(btnVehicleCar, vehicleTypeButtonContainer)
            }
            viewModel.setDefaults()
        }
    }

    private fun initShowButton() {
        binding?.tvBtnApplyFilter?.click {
            val number = binding?.numberEditor?.getFullNumberString()
            if (number.isNullOrEmpty()) {
                NToast.with(view)
                    .text(getString(R.string.error_number_is_empty))
                    .typeAlert()
                    .show()
            } else {
                val parameters = NumberSearchParameters(
                    number = number,
                    vehicleTypeId = viewModel.vehicleTypeId,
                    countryId = viewModel.countryId,
                    countryName = viewModel.selectedCountry.name.lowercase(Locale.getDefault())
                )
                callback.searchParameters(parameters)
                dismiss()
            }
        }
    }

    private fun setResetButtonState() {
        val color = if (!isNumberInputEmpty()) R.color.inactive_reset_road_filter
        else R.color.active_reset_road_filter

        binding?.resetFilter?.isEnabled = isNumberInputEmpty()
        binding?.resetFilter?.setTextColor(ContextCompat.getColor(requireContext(), color))
    }

    private fun setShowButtonState() {
        binding?.tvBtnApplyFilter?.alpha =
            if (!isNumberInputEmpty()) DISABLE_BUTTON_ALPHA
            else ENABLE_BUTTON_ALPHA
        binding?.tvBtnApplyFilter?.isEnabled = isNumberInputEmpty()
    }

    private fun isNumberInputEmpty(): Boolean {
        return binding?.numberEditor?.getFullNumberString()?.length ?: 0 >= MIN_VALID_NUM
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> BottomSheetSearchNumberBinding
        get() = BottomSheetSearchNumberBinding::inflate

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        // This work around is needed, to scroll up bottomsheet when keyboard appears
        if (dialog is BottomSheetDialog) {
            dialog.behavior.skipCollapsed = true
            dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
        return dialog

    }

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    companion object {
        const val DEFAULT_COUNTRY_ID = 3159L
        private const val DISABLE_BUTTON_ALPHA = 0.3f
        private const val ENABLE_BUTTON_ALPHA = 1.0f
        const val MIN_VALID_NUM = 1
    }
}
