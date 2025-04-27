package com.numplates.nomera3.presentation.view.fragments.bottomfragment.roadfilter

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.contains
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import com.google.android.flexbox.FlexboxLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.meera.core.extensions.dp
import com.meera.core.extensions.dpToPx
import com.meera.core.extensions.gone
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setPaddingBottom
import com.meera.core.extensions.setVisible
import com.meera.core.extensions.updatePadding
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.City
import com.numplates.nomera3.databinding.BottomSheetRoadFilterDialogFragmentNewBinding
import com.numplates.nomera3.modules.newroads.data.entities.FilterSettingsProvider
import com.numplates.nomera3.modules.registration.domain.model.RegistrationCountryModel
import com.numplates.nomera3.presentation.router.BaseBottomSheetDialogFragment
import com.numplates.nomera3.presentation.utils.viewModelsFactory
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.createCountryFilterItemView
import com.numplates.nomera3.presentation.view.fragments.dialogs.ConfirmDialogBuilder

class RoadFilterBottomSheetNew : BaseBottomSheetDialogFragment<BottomSheetRoadFilterDialogFragmentNewBinding>() {

    private var rootContainer: View? = null
    private var resetDialogButton: TextView? = null
    private var sortContainer: View? = null
    private var sortRecommendedButton: View? = null
    private var sortNewButton: View? = null
    private var filterParamsContainer: NestedScrollView? = null
    private var headerDivider: View? = null
    private var bottomDivider: View? = null
    private var citySearchContainer: View? = null
    private var countryFilter: ViewGroup? = null
    private var selectedCitiesContainer: ChipGroup? = null

    private val viewModel: RoadFilterViewModel by viewModelsFactory {
        val filterType = arguments.getFilterSettingsType()
        RoadFilterViewModel(filterType)
    }

    private fun Bundle?.getFilterSettingsType(): FilterSettingsProvider.FilterType {
        return (this?.getSerializable(FilterSettingsProvider.FilterType.BUNDLE_KEY) as? FilterSettingsProvider.FilterType)
            ?: FilterSettingsProvider.FilterType.Main
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> BottomSheetRoadFilterDialogFragmentNewBinding
        get() = BottomSheetRoadFilterDialogFragmentNewBinding::inflate


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView()
        initViews()
        initSortViews()
        subscribeOnDataSource()
        viewModel.initializeFilter()
    }

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    fun onCitySearchComplete() {
        viewModel.initializeFilter()
    }

    private fun setupView() {
        (dialog as? BottomSheetDialog)?.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            ?.let {
                BottomSheetBehavior.from(it).apply {
                    state = BottomSheetBehavior.STATE_EXPANDED
                    skipCollapsed = true
                }
            }
    }

    private fun subscribeOnDataSource() {
        viewModel.sortState.observe(viewLifecycleOwner, Observer { sortState ->
            val needShowSort = sortState.first
            val isRecommended = sortState.second

            setSortVisible(needShowSort)

            if (needShowSort) {
                selectSort(isRecommended)
            }
        })

        viewModel.selectedCities.observe(viewLifecycleOwner) { cities: MutableList<City> ->
            refreshCityFilter(cities)
        }

        viewModel.selectedCountries.observe(viewLifecycleOwner) {
            updateCountryFilterContainer(it)
        }

        viewModel.removeAllSelectedCities.observe(viewLifecycleOwner, Observer {
            selectedCitiesContainer?.removeAllViews()
        })

        viewModel.isResetButtonEnable.observe(viewLifecycleOwner, Observer { isDefaultState: Boolean ->
            val color = if (!isDefaultState) {
                R.color.active_reset_road_filter
            } else {
                R.color.inactive_reset_road_filter
            }

            resetDialogButton?.isEnabled = !isDefaultState
            resetDialogButton?.setTextColor(ContextCompat.getColor(requireContext(), color))
        })

        viewModel.isCountriesSelectable.observe(viewLifecycleOwner, Observer {
            countryFilter?.children?.forEach { countryChip: View ->
                countryChip.isClickable = it
            }
        })

        viewModel.showResetSelectedCitiesDialog.observe(
            viewLifecycleOwner
        ) { country: Pair<RegistrationCountryModel, Boolean> ->
            // country - страна которую пользователь собирается выбрать
            showResetCitiesDialog(country)
        }
    }

    private fun showResetCitiesDialog(it: Pair<RegistrationCountryModel, Boolean>) {
        ConfirmDialogBuilder()
            .setHeader(getString(R.string.reset_cities_dialog_header))
            .setDescription(getString(R.string.reset_cities_dialog_descripiton))
            .setLeftBtnText(getString(R.string.reset_cities_dialog_cancel))
            .setRightBtnText(getString(R.string.reset_cities_dialog_change))
            .setRightClickListener {
                viewModel.removeAllSelectedCities()
                viewModel.updateSelectedCountries(it.first, it.second)
                viewModel.getSelectedCountriesFromStorage()
            }
            .show(childFragmentManager)
    }

    private fun refreshCityFilter(cities: MutableList<City>) {
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
                    this.setOnCloseIconClickListener {
                        if (selectedCitiesContainer?.contains(this) == true) {
                            viewModel.removeCityFromStorage(it.tag as City)
                            viewModel.initializeFilter()
                        }
                    }
                }
            }.forEach { chip: Chip ->
                selectedCitiesContainer?.addView(chip)
            }
        }
    }

    private fun initViews() {
        rootContainer = binding?.roadFilterRootContainer

        resetDialogButton = binding?.resetFilter
        resetDialogButton?.setOnClickListener {
            onResetButtonClicked()
        }

        citySearchContainer = binding?.citySearchContainer
        citySearchContainer?.setOnClickListener {
            getCallback()?.onCountrySearchClicked()
        }

        countryFilter = binding?.countryFilter
        fillCountryFilterByItems()

        selectedCitiesContainer = binding?.selectedCitiesContainer

        filterParamsContainer = binding?.roadFilterParamsContainer
        filterParamsContainer?.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, _, _, _ ->
            val canScroll = filterParamsContainer?.canScrollVertically(-DIVIDER_VISIBILITY_SCROLL_Y) ?: false
            headerDivider?.setVisible(canScroll)
        })

        headerDivider = binding?.roadFilterHeaderDivider
        bottomDivider = binding?.roadFilterBottomDivider
    }

    private fun addCountryFilterClickListeners() {
        countryFilter?.children?.forEach { filterItemView: View ->
            filterItemView.setOnClickListener {
                viewModel.updateSelectedCountries(it.tag as RegistrationCountryModel, it.isSelected)
                viewModel.getSelectedCountriesFromStorage()
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

    private fun initSortViews() {
        sortContainer = binding?.sortFilterContainer

        sortRecommendedButton = binding?.btnSortRecommended
        sortRecommendedButton?.setOnClickListener {
            selectSort(isRecommended = true)

            viewModel.resetRoadFilter(toRecommended = true)
            viewModel.initializeFilter()
        }

        sortNewButton = binding?.btnSortNew
        sortNewButton?.setOnClickListener {
            selectSort(isRecommended = false)

            viewModel.setSortState(false)
            viewModel.updateResetButton()
        }
    }

    private fun selectSort(isRecommended: Boolean) {
        val targetSortView = if (isRecommended) sortRecommendedButton else sortNewButton
        val isSelected = targetSortView?.isSelected ?: false
        if (isSelected.not()) {
            sortRecommendedButton?.isSelected = isRecommended
            sortNewButton?.isSelected = !isRecommended
            filterParamsContainer?.setVisible(!isRecommended)
            if (isRecommended) headerDivider?.gone()
            sortContainer?.updatePadding(paddingTop = if (isRecommended) 0 else dpToPx(SORT_CONTAINER_PADDING_TOP))
        }
    }

    private fun setSortVisible(isVisible: Boolean) {
        sortContainer?.setVisible(isVisible)
        bottomDivider?.setVisible(isVisible)
        filterParamsContainer?.setPaddingBottom(if (isVisible) PARAMS_CONTAINER_PADDING_BOTTOM.dp else 0)
        rootContainer?.setMargins(bottom = if (isVisible) ROOT_CONTAINER_MARGIN_BOTTOM.dp else 0)
    }

    private fun fillCountryFilterByItems() {
        viewModel.liveCountries.observe(viewLifecycleOwner) { list ->
            countryFilter?.removeAllViews()
            list.forEach { item ->
                countryFilter?.addView(requireContext().createCountryFilterItemView(item))
            }
            addCountryFilterClickListeners()
            viewModel.selectedCountries.value?.let { updateCountryFilterContainer(it) }
        }
    }

    private fun createCountryFilterItemView(filterCountryItem: CountryFilterItem): View {
        fun getStringById(id: Int): String {
            return resources.getString(id)
        }

        val filterItemView = LayoutInflater
            .from(requireContext())
            .inflate(R.layout.bottom_sheet_road_filter_country_item, null)
            .apply {
                tag = filterCountryItem
                layoutParams = FlexboxLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    32.dp
                )
                setMargins(0, 11.dp, 6.dp, 0)
            }

        when (filterCountryItem) {
            CountryFilterItem.ALL -> getStringById(R.string.road_post_all_countries)
            CountryFilterItem.RUSSIA -> getStringById(R.string.general_russia)
            CountryFilterItem.KAZAKHSTAN -> getStringById(R.string.general_kazakhstan)
            CountryFilterItem.BELARUS -> getStringById(R.string.general_belarus_filter)
            CountryFilterItem.ARMENIA -> getStringById(R.string.general_armenia)
            CountryFilterItem.GEORGIA -> getStringById(R.string.general_georgia)
            CountryFilterItem.UKRAINE -> getStringById(R.string.general_ukraine)
            CountryFilterItem.AZERBAIJAN -> getStringById(R.string.general_azerbaijan)
            CountryFilterItem.KYRGYZSTAN -> getStringById(R.string.general_kyrgyzstan)
            CountryFilterItem.MOLDOVA -> getStringById(R.string.general_moldova)
            CountryFilterItem.TAJIKISTAN -> getStringById(R.string.general_tajikistan)
            CountryFilterItem.TURKMENISTAN -> getStringById(R.string.general_turkmenistan)
            CountryFilterItem.UZBEKISTAN -> getStringById(R.string.general_uzbekistan)
            CountryFilterItem.TURKEY -> getStringById(R.string.general_turkey)
            CountryFilterItem.OAE -> getStringById(R.string.general_oae)
            CountryFilterItem.THAILAND -> getStringById(R.string.general_thailand)
        }.let { countryName: String ->
            filterItemView as TextView
            filterItemView.text = countryName
        }

        return filterItemView
    }

    private fun onResetButtonClicked() {
        viewModel.resetRoadFilter()
        viewModel.updateResetButton()
    }

    private fun getCallback(): RoadFilterCallback? {
        return (parentFragment as? RoadFilterCallback.CallbackOwner)?.roadFilterCallback
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        viewModel.saveSelections(isRecommended = sortRecommendedButton?.isSelected ?: false)
        getCallback()?.onDismiss()
    }

    companion object {
        const val TAG = "RoadFilterBottomSheetNew"
        const val DIVIDER_VISIBILITY_SCROLL_Y = 20
        const val SORT_CONTAINER_PADDING_TOP = 20
        const val PARAMS_CONTAINER_PADDING_BOTTOM = 20
        const val ROOT_CONTAINER_MARGIN_BOTTOM = 130

        fun newInstance(filterType: FilterSettingsProvider.FilterType): RoadFilterBottomSheetNew {
            return RoadFilterBottomSheetNew().apply {
                val bundle = Bundle()
                bundle.putSerializable(FilterSettingsProvider.FilterType.BUNDLE_KEY, filterType)
                arguments = bundle
            }
        }
    }
}
