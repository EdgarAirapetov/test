package com.numplates.nomera3.presentation.view.fragments.bottomfragment.filter

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.contains
import androidx.core.view.isGone
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.meera.core.extensions.click
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.visible
import com.meera.core.views.rangeslider.NRangeBar
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.City
import com.numplates.nomera3.databinding.BottomSheetRoadFilterDialogFragmentNewBinding
import com.numplates.nomera3.modules.registration.domain.model.RegistrationCountryModel
import com.numplates.nomera3.presentation.router.BaseBottomSheetDialogFragment
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.createCountryFilterItemView
import com.numplates.nomera3.presentation.view.fragments.dialogs.ConfirmDialogBuilder


/**
 * Фильтр быбора страны, города, пола, возраста. Не привязан к данным
 * дороги, не сохраняет результат выбора а только отдаёт
 * результат выбора фильтра.
 */
class FilterBottomSheet(
    private val filterType: FilterType = FilterType.ROAD,
    private val callback: FilterCallback
) : BaseBottomSheetDialogFragment<BottomSheetRoadFilterDialogFragmentNewBinding>() {

    private val viewModel by viewModels<FilterViewModel> { App.component.getViewModelFactory() }

    private var resetDialogButton: TextView? = null
    private var citySearchContainer: View? = null
    private var countryFilter: ViewGroup? = null
    private var selectedCitiesContainer: ChipGroup? = null
    private var filterResult = FilterResult()

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> BottomSheetRoadFilterDialogFragmentNewBinding
        get() = BottomSheetRoadFilterDialogFragmentNewBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initClickListeners()
        initObservables()
        initGenderFilter()
        viewModel.filterResult = filterResult
        viewModel.initializeFilter()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val onCreateDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        return onCreateDialog.apply {
            behavior.peekHeight = resources.displayMetrics?.heightPixels ?: 0
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    fun setFilterResult(result: FilterResult) {
       filterResult = result.copy()
    }

    private fun initViews() {
        binding?.selectedCitiesContainer?.setMargins(top = 0, bottom = 20.dp)

        resetDialogButton = binding?.resetFilter
        resetDialogButton?.click { clearFilter() }

        citySearchContainer = binding?.citySearchContainer
        citySearchContainer?.click { callback.onSelectCityClick() }

        countryFilter = binding?.countryFilter
        selectedCitiesContainer = binding?.selectedCitiesContainer

        fillCountryFilterByItems()

        when (filterType) {
            FilterType.ROAD -> binding?.filterUserContainer?.gone()
            FilterType.PEOPLE_SEARCH -> binding?.filterUserContainer?.visible()
        }

        val hideAgeAndGender = viewModel.isHiddenAgeAndGender()
        binding?.vGenderDivider?.isGone = hideAgeAndGender
        binding?.genderFilterLabel?.isGone = hideAgeAndGender
        binding?.genderButtonContainer?.isGone = hideAgeAndGender
        binding?.vAgeDivider?.isGone = hideAgeAndGender
        binding?.ageFilterLabel?.isGone = hideAgeAndGender
        binding?.llAge?.isGone = hideAgeAndGender


        binding?.countryFilterDescription?.gone()
        binding?.cityFilterDescription?.gone()
    }

    private fun fillCountryFilterByItems() {
        viewModel.liveCountries.observe(viewLifecycleOwner) { list ->
            list.forEach { item ->
                countryFilter?.addView(requireContext().createCountryFilterItemView(item))
            }
            addCountryFilterClickListeners()
            updateCountryFilterContainer(viewModel.filterResult.countries)
        }
    }

    private fun initClickListeners() {
        addGenderFilterClickListeners()
        addFilterAgeRangeListener()
        binding?.llBtnApplyFilter?.click {
            val result =
                if (viewModel.isFilterDefaultState()) null
                else viewModel.filterResult
            callback.onFilterResult(result)
            dismiss()
        }
    }

    private fun addCountryFilterClickListeners() {
        countryFilter?.children?.forEach { filterItemView: View ->
            filterItemView.click {
                if (it.isSelected) {
                    viewModel.removeCountry(it.tag as RegistrationCountryModel)
                } else {
                    viewModel.saveSelectedCountries(it.tag as RegistrationCountryModel, it.isSelected)
                }
            }
        }
    }

    private fun addGenderFilterClickListeners() {
        binding?.apply {
            val buttonContainer = genderButtonContainer
            btnGenderAny.click {
                viewModel.filterResult.gender = FilterGender.ANY
                setGenderItemSelection((it as TextView), buttonContainer)
            }
            btnGenderMale.click {
                viewModel.filterResult.gender = FilterGender.MALE
                setGenderItemSelection((it as TextView), buttonContainer)
            }
            btnGenderFemale.click {
                viewModel.filterResult.gender = FilterGender.FEMALE
                setGenderItemSelection((it as TextView), buttonContainer)
            }
        }
    }

    private fun setGenderItemSelection(
        textView: TextView,
        container: LinearLayout
    ) {
        setGenderButton(textView, container)
        viewModel.triggerResetFilterButton()
    }

    private fun addFilterAgeRangeListener() {
        binding?.rangeBar?.setOnRangeBarChangeListener(
            object : NRangeBar.OnRangeBarChangeListener {

                override fun onRangeChangeListener(
                    rangeBar: NRangeBar?,
                    leftPinIndex: Int,
                    rightPinIndex: Int,
                    leftPinValue: String?,
                    rightPinValue: String?
                ) {
                    val minAge = leftPinValue?.toInt() ?: MIN_FILTER_AGE
                    val maxAge = rightPinValue?.toInt() ?: MAX_FILTER_AGE

                    viewModel.filterResult.age = FilterAgeRange(minAge, maxAge)
                    viewModel.setAge(minAge, maxAge)
                    viewModel.triggerResetFilterButton()
                }

                override fun onTouchStarted(rangeBar: NRangeBar?) {
                    /** STUB */
                }

                override fun onTouchEnded(rangeBar: NRangeBar?) {
                    /** STUB */
                }
            })
    }

    private fun setAge() {
        binding?.rangeBar?.setRangePinsByValue(
            viewModel.filterResult.age.start.toFloat(),
            viewModel.filterResult.age.end.toFloat()
        )
    }

    private fun setGenderButton(currentButton: TextView, container: LinearLayout) {
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
    }

    private fun initObservables() {
        viewModel.liveEvent.observe(viewLifecycleOwner) {
            when (it) {
                is FilterViewEvent.Initialize -> initialize()
                is FilterViewEvent.Countries -> updateCountryFilterContainer(it.countries)
                is FilterViewEvent.Country -> showResetCitiesDialog(it.country)
                is FilterViewEvent.Cities -> refreshCityFilter(it.cities)
                is FilterViewEvent.ResetButtonState -> setResetButtonState(it.enabled)
            }
        }
    }

    private fun setResetButtonState(isDefaultState: Boolean) {
        val color = if (!isDefaultState) {
            R.color.active_reset_road_filter
        } else {
            R.color.inactive_reset_road_filter
        }

        resetDialogButton?.isEnabled = !isDefaultState
        resetDialogButton?.setTextColor(ContextCompat.getColor(requireContext(), color))
    }

    private fun showResetCitiesDialog(it: Pair<RegistrationCountryModel, Boolean>) {
        ConfirmDialogBuilder()
            .setHeader(getString(R.string.reset_cities_dialog_header))
            .setDescription(getString(R.string.reset_cities_dialog_descripiton))
            .setLeftBtnText(getString(R.string.reset_cities_dialog_cancel))
            .setRightBtnText(getString(R.string.reset_cities_dialog_change))
            .setRightClickListener {
                viewModel.removeAllCitiesAndSetNewCountries(it)
            }
            .show(childFragmentManager)
    }

    private fun initialize() {
        initGenderFilter()
        refreshCityFilter(viewModel.filterResult.cities)
        setResetButtonState(!viewModel.isFilterDefaultState())
        setAge()
    }

    private fun initGenderFilter() {
        val filterItem = when (viewModel.filterResult.gender) {
            FilterGender.MALE -> binding?.btnGenderMale
            FilterGender.FEMALE -> binding?.btnGenderFemale
            else -> binding?.btnGenderAny
        }
        binding?.apply {
            filterItem?.let {
                setGenderItemSelection(it, genderButtonContainer)
            }
        }
    }

    private fun updateCountryFilterContainer(it: MutableSet<RegistrationCountryModel>) {
        countryFilter
            ?.children
            ?.forEach { filterItemView: View ->
                val roadCountryFilterItem = filterItemView.tag as RegistrationCountryModel
                filterItemView.isSelected = it.contains(roadCountryFilterItem)
            }
    }

    fun onCitySearchComplete(cities: List<City>) {
        viewModel.showSelectedCities(cities)
    }

    private fun refreshCityFilter(cities: Set<City>) {
        viewModel.filterResult.cities.addAll(cities)

        fun createCityChipView(name: String): Chip {
            val view = LayoutInflater
                .from(requireContext())
                .inflate(R.layout.bottom_sheet_road_filter_chip, null, false)

            view as Chip
            view.text = name
            return view
        }

        selectedCitiesContainer?.removeAllViews()
        if (cities.isNotEmpty()) {
            cities.map { city: City ->
                createCityChipView(city.title_ ?: "").apply {
                    this.tag = city
                    this.setOnCloseIconClickListener { view ->
                        selectedCitiesContainer?.contains(this)?.let { isContains ->
                            if (isContains) viewModel.removeCity(view.tag as City)
                        }
                    }
                }
            }.forEach { chip: Chip ->
                selectedCitiesContainer?.addView(chip)
            }
            selectedCitiesContainer?.setMargins(top = 8.dp)
        } else {
            selectedCitiesContainer?.setMargins(top = 0)
        }
    }

    fun clearFilter() {
        viewModel.clearFilter()
        binding?.apply {
            // Set gender button by default
            val buttonContainer = genderButtonContainer
            setGenderButton(btnGenderAny, buttonContainer)
            // Set age range by default
            rangeBar.setRangePinsByValue(
                MIN_FILTER_AGE.toFloat(),
                MAX_FILTER_AGE.toFloat()
            )
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        callback.onDismiss()
    }

    companion object {
        enum class FilterType { ROAD, PEOPLE_SEARCH }

        const val MIN_FILTER_AGE = 18
        const val MAX_FILTER_AGE = 80
    }

}
