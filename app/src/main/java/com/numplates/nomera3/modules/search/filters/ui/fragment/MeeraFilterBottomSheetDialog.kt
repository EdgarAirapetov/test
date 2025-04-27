package com.numplates.nomera3.modules.search.filters.ui.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.core.view.contains
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.meera.core.dialogs.MeeraConfirmDialogBuilder
import com.meera.core.extensions.dp
import com.meera.core.extensions.empty
import com.meera.core.extensions.gone
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogParams
import com.meera.uikit.widgets.buttons.ButtonType
import com.meera.uikit.widgets.chips.UiKitCheckableChipView
import com.meera.uikit.widgets.chips.UiKitChipView
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.City
import com.numplates.nomera3.databinding.MeeraDialogFiltersBinding
import com.numplates.nomera3.modules.registration.domain.model.RegistrationCountryModel
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.createMeeraCountryFilterItemView
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.filter.FilterResult
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.filter.FilterViewEvent
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.filter.FilterViewModel


interface MeeraFilterCallback {
    fun onSelectCityClick()
    fun onFilterResult(result: FilterResult?) {}
}

class MeeraFilterBottomSheetDialog : UiKitBottomSheetDialog<MeeraDialogFiltersBinding>() {

    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraDialogFiltersBinding
        get() = MeeraDialogFiltersBinding::inflate

    private val viewModel by viewModels<FilterViewModel> { App.component.getViewModelFactory() }

    private var filterResult = FilterResult()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initClickListeners()
        initObservables()
        viewModel.filterResult = filterResult
        viewModel.initializeFilter()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val onCreateDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        return onCreateDialog.apply {
            behavior.peekHeight = resources.displayMetrics.heightPixels
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
    }

    override fun createDialogState(): UiKitBottomSheetDialogParams {
        return UiKitBottomSheetDialogParams(
            labelText = getString(R.string.map_filters_title),
            dialogStyle = R.style.BottomSheetDialogTheme
        )
    }

    fun setFilterResult(result: FilterResult) {
        filterResult = result.copy()
    }

    fun onCitySearchComplete(cities: List<City>) {
        viewModel.showSelectedCities(cities)
    }

    private fun initViews() {
        contentBinding?.apply {
            tvCountryDescription.gone()
            tvCityDescription.gone()
            vgSelectedCities.setMargins(top = 0, bottom = 20.dp)
            btnReset.setThrottledClickListener { clearFilter() }
            isFiltersSearch.isFocusable = false
            vInputClickable.setThrottledClickListener { (parentFragment as? MeeraFilterCallback?)?.onSelectCityClick() }
        }
        fillCountryFilterByItems()
    }

    private fun fillCountryFilterByItems() {
        viewModel.liveCountries.observe(viewLifecycleOwner) { list ->
            list.forEach { item ->
                contentBinding?.vgCountries?.addView(requireContext().createMeeraCountryFilterItemView(item))
            }
            addCountryFilterClickListeners()
            updateCountryFilterContainer(viewModel.filterResult.countries)
            contentBinding?.vgCountries?.requestLayout()
        }
    }

    private fun initClickListeners() {
        contentBinding?.btnFiltersApply?.setThrottledClickListener {
            val result = if (viewModel.isFilterDefaultState()) null else viewModel.filterResult
            (parentFragment as? MeeraFilterCallback)?.onFilterResult(result)
            dismiss()
        }
    }

    private fun addCountryFilterClickListeners() {
        contentBinding?.vgCountries?.children?.filterIsInstance<UiKitCheckableChipView>()?.forEach { filterItemView ->
            filterItemView.setThrottledClickListener {
                if (filterItemView.checked) {
                    viewModel.removeCountry(filterItemView.tag as RegistrationCountryModel)
                } else {
                    viewModel.saveSelectedCountries(
                        filterItemView.tag as RegistrationCountryModel,
                        filterItemView.checked
                    )
                }
            }
        }
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
        contentBinding?.btnReset?.isEnabled = !isDefaultState
    }

    private fun showResetCitiesDialog(it: Pair<RegistrationCountryModel, Boolean>) {
        MeeraConfirmDialogBuilder()
            .setHeader(R.string.reset_cities_dialog_header)
            .setDescription(R.string.reset_cities_dialog_descripiton)
            .setTopBtnText(R.string.change)
            .setTopBtnType(ButtonType.FILLED)
            .setBottomBtnText(R.string.cancel)
            .setTopClickListener {
                viewModel.removeAllCitiesAndSetNewCountries(it)
            }
            .show(childFragmentManager)
    }

    private fun initialize() {
        refreshCityFilter(viewModel.filterResult.cities)
        setResetButtonState(viewModel.isFilterDefaultState())
    }

    private fun updateCountryFilterContainer(it: MutableSet<RegistrationCountryModel>) {
        contentBinding?.vgCountries
            ?.children
            ?.filterIsInstance<UiKitCheckableChipView>()
            ?.forEach { filterItemView ->
                val roadCountryFilterItem = filterItemView.tag as RegistrationCountryModel
                filterItemView.checked = it.contains(roadCountryFilterItem)
            }
    }

    private fun refreshCityFilter(cities: Set<City>) {
        viewModel.filterResult.cities.addAll(cities)

        fun createCityChipView(name: String): UiKitChipView {
            val view = LayoutInflater
                .from(requireContext())
                .inflate(R.layout.meera_item_filter_city_chip, null, false)

            view as UiKitChipView
            view.text = name
            return view
        }

        contentBinding?.vgSelectedCities?.removeAllViews()
        if (cities.isNotEmpty()) {
            cities.map { city: City ->
                createCityChipView(city.title_ ?: String.empty()).apply {
                    this.tag = city
                    this.setThrottledClickListener {
                        contentBinding?.vgSelectedCities?.contains(this)?.let { isContains ->
                            if (isContains) viewModel.removeCity(this.tag as City)
                        }
                    }
                }
            }.forEach { chip ->
                contentBinding?.vgSelectedCities?.addView(chip)
            }
            contentBinding?.vgSelectedCities?.setMargins(top = 8.dp)
        } else {
            contentBinding?.vgSelectedCities?.setMargins(top = 0)
        }
    }

    private fun clearFilter() {
        viewModel.clearFilter()
    }

}
