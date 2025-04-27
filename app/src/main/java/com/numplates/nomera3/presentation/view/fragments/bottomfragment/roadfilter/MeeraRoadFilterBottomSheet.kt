package com.numplates.nomera3.presentation.view.fragments.bottomfragment.roadfilter

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import androidx.core.view.contains
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.meera.core.dialogs.MeeraConfirmDialogBuilder
import com.meera.core.extensions.dp
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogParams
import com.meera.uikit.widgets.buttons.ButtonType
import com.meera.uikit.widgets.chips.UiKitCheckableChipView
import com.meera.uikit.widgets.chips.UiKitChipView
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.City
import com.numplates.nomera3.databinding.MeeraDialogFiltersBinding
import com.numplates.nomera3.modules.newroads.data.entities.FilterSettingsProvider
import com.numplates.nomera3.modules.registration.domain.model.RegistrationCountryModel
import com.numplates.nomera3.presentation.utils.viewModelsFactory
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.createMeeraCountryFilterItemView

class MeeraRoadFilterBottomSheet : UiKitBottomSheetDialog<MeeraDialogFiltersBinding>() {

    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraDialogFiltersBinding
        get() = MeeraDialogFiltersBinding::inflate

    private val viewModel: RoadFilterViewModel by viewModelsFactory {
        val filterType = arguments.getFilterSettingsType()
        RoadFilterViewModel(filterType)
    }

    private fun Bundle?.getFilterSettingsType(): FilterSettingsProvider.FilterType {
        return (this?.getSerializable(FilterSettingsProvider.FilterType.BUNDLE_KEY) as? FilterSettingsProvider.FilterType)
            ?: FilterSettingsProvider.FilterType.Main
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        subscribeOnDataSource()
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

    fun onCitySearchComplete() {
        viewModel.initializeFilter(false)
    }

    private fun subscribeOnDataSource() {

        viewModel.selectedCities.observe(viewLifecycleOwner) { cities: MutableList<City> ->
            refreshCityFilter(cities)
        }

        viewModel.selectedCountries.observe(viewLifecycleOwner) {
            updateCountryFilterContainer(it)
        }

        viewModel.removeAllSelectedCities.observe(viewLifecycleOwner) {
            contentBinding?.vgSelectedCities?.removeAllViews()
        }

        viewModel.isResetButtonEnable.observe(viewLifecycleOwner) { isDefaultState: Boolean ->
            contentBinding?.btnReset?.isEnabled = !isDefaultState
        }

        viewModel.isCountriesSelectable.observe(viewLifecycleOwner) {
            contentBinding?.vgCountries?.children?.forEach { countryChip ->
                countryChip.isClickable = it
            }
        }

        viewModel.showResetSelectedCitiesDialog.observe(
            viewLifecycleOwner
        ) { country: Pair<RegistrationCountryModel, Boolean> ->
            showResetCitiesDialog(country)
        }
    }

    private fun showResetCitiesDialog(it: Pair<RegistrationCountryModel, Boolean>) {
        MeeraConfirmDialogBuilder()
            .setHeader(R.string.reset_cities_dialog_header)
            .setDescription(R.string.reset_cities_dialog_descripiton)
            .setTopBtnText(R.string.change)
            .setTopBtnType(ButtonType.FILLED)
            .setBottomBtnText(R.string.cancel)
            .setTopClickListener {
                viewModel.removeAllSelectedCities()
                viewModel.updateSelectedCountries(it.first, it.second)
                viewModel.getSelectedCountriesFromStorage()
            }
            .show(childFragmentManager)
    }

    private fun refreshCityFilter(cities: MutableList<City>) {
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
                createCityChipView(city.title_ ?: "").apply {
                    this.tag = city
                    this.setThrottledClickListener {
                        contentBinding?.vgSelectedCities?.contains(this)?.let { isContains ->
                            if (isContains) {
                                viewModel.removeCityFromStorage(this.tag as City)
                                viewModel.initializeFilter(false)
                            }
                        }
                    }
                }
            }.forEach { chip ->
                contentBinding?.vgSelectedCities?.addView(chip)
            }
        }
    }

    private fun initViews() {
        contentBinding?.apply {
            vgSelectedCities.setMargins(top = 16.dp, bottom = 20.dp)
            btnReset.setThrottledClickListener { onResetButtonClicked() }
            isFiltersSearch.isFocusable = false
            vInputClickable.setThrottledClickListener { getCallback()?.onCountrySearchClicked() }
            btnFiltersApply.setThrottledClickListener {
                viewModel.saveSelections(false)
                getCallback()?.onDismiss()
                dismiss()
            }
        }
        fillCountryFilterByItems()
    }

    private fun addCountryFilterClickListeners() {
        contentBinding?.vgCountries?.children?.filterIsInstance<UiKitCheckableChipView>()?.forEach { filterItemView ->
            filterItemView.setThrottledClickListener {
                viewModel.updateSelectedCountries(filterItemView.tag as RegistrationCountryModel, filterItemView.checked)
                viewModel.getSelectedCountriesFromStorage()
            }
        }
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

    private fun fillCountryFilterByItems() {
        viewModel.liveCountries.observe(viewLifecycleOwner) { list ->
            contentBinding?.vgCountries?.removeAllViews()
            list.forEach { item ->
                contentBinding?.vgCountries?.addView(requireContext().createMeeraCountryFilterItemView(item))
            }
            addCountryFilterClickListeners()
            viewModel.selectedCountries.value?.let { updateCountryFilterContainer(it) }
            contentBinding?.vgCountries?.requestLayout()
        }
    }

    private fun onResetButtonClicked() {
        viewModel.resetRoadFilter()
        viewModel.updateResetButton()
    }

    private fun getCallback(): RoadFilterCallback? {
        return (parentFragment as? RoadFilterCallback.CallbackOwner)?.roadFilterCallback
    }

    companion object {
        const val TAG = "RoadFilterBottomSheetNew"
        fun newInstance(filterType: FilterSettingsProvider.FilterType): MeeraRoadFilterBottomSheet {
            return MeeraRoadFilterBottomSheet().apply {
                val bundle = Bundle()
                bundle.putSerializable(FilterSettingsProvider.FilterType.BUNDLE_KEY, filterType)
                arguments = bundle
            }
        }
    }
}
