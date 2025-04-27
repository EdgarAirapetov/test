package com.numplates.nomera3.modules.search.number.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentSearchNumberCountriesBinding
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.modules.search.number.ui.adapter.MeeraSearchNumberCountryAdapter
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.roadfilter.CountryFilterItem
import com.numplates.nomera3.presentation.view.fragments.bottomfragment.roadfilter.getCountryId

const val KEY_NUMBER_SEARCH_COUNTRY = "KEY_NUMBER_SEARCH_COUNTRY"

class MeeraSearchNumberCountriesFragment : MeeraBaseDialogFragment(
    R.layout.meera_fragment_search_number_countries,
    behaviourConfigState = ScreenBehaviourState.BottomScreensWrapContent
) {

    private val binding by viewBinding(MeeraFragmentSearchNumberCountriesBinding::bind)
    private val adapter by lazy { MeeraSearchNumberCountryAdapter(::handleCountryClicked) }

    override val containerId: Int
        get() = R.id.fragment_second_container_view

    private val countryFilterItems = listOf(
        CountryFilterItem.RUSSIA,
        CountryFilterItem.ARMENIA,
        CountryFilterItem.BELARUS,
        CountryFilterItem.GEORGIA,
        CountryFilterItem.KAZAKHSTAN,
        CountryFilterItem.UKRAINE
    )

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        binding.apply {
            btnBack.setThrottledClickListener { findNavController().popBackStack() }
            rvSearchNumberCountries.layoutManager = LinearLayoutManager(context)
            rvSearchNumberCountries.adapter = adapter
        }
        adapter.submitList(countryFilterItems)
    }

    private fun handleCountryClicked(item: CountryFilterItem) {
        parentFragmentManager.setFragmentResult(KEY_NUMBER_SEARCH_COUNTRY, bundleOf(KEY_NUMBER_SEARCH_COUNTRY to item.getCountryId()))
        findNavController().popBackStack()
    }

}
