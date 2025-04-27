package com.numplates.nomera3.modules.registration.ui.country.fragment

import android.app.Dialog
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.widgets.dp
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.core.INetworkValues
import com.numplates.nomera3.databinding.MeeraFragmentRegistrationCountryListBinding
import com.numplates.nomera3.modules.registration.domain.model.RegistrationCountryModel
import com.numplates.nomera3.modules.registration.domain.model.RegistrationCountryState
import com.numplates.nomera3.modules.registration.ui.country.adapter.MeeraRegistrationCountryAdapter
import com.numplates.nomera3.modules.registration.ui.country.viewmodel.MeeraRegistrationCountryViewModel
import timber.log.Timber

//const val KEY_COUNTRY = "COUNTRY"
private const val KEY_FROM_SCREEN_TYPE = "KEY_FROM_SCREEN_TYPE"
private const val BOTTOM_SHEET_MARGIN_TOP = 65

class MeeraRegistrationCountriesFragment : BottomSheetDialogFragment(R.layout.meera_fragment_registration_country_list) {

    private val binding by viewBinding(MeeraFragmentRegistrationCountryListBinding::bind)

    private val viewModel by viewModels<MeeraRegistrationCountryViewModel> { App.component.getViewModelFactory() }

    private val adapter by lazy {
        MeeraRegistrationCountryAdapter(this::itemClicked)
    }

    private val transportAvailableCountriesArray by lazy { arrayOf(
        INetworkValues.ROAD_TO_RUSSIA,
        INetworkValues.ROAD_TO_UKRAINE,
        INetworkValues.ROAD_TO_BELORUS,
        INetworkValues.ROAD_TO_GEORGIA,
        INetworkValues.ROAD_TO_KAZAKHSTAN,
        INetworkValues.ROAD_TO_ARMENIA
    ) }

    private val fromScreenType: RegistrationCountryFromScreenType by lazy {
        RegistrationCountryFromScreenType.valueOf(
            arguments?.getString(KEY_FROM_SCREEN_TYPE) ?: RegistrationCountryFromScreenType.Registration.name
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        observeState()
        viewModel.loadCountries(fromScreenType)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            val parentLayout =
                bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            parentLayout?.let {
                val behaviour = BottomSheetBehavior.from(it)
                setupFullHeight(it)
                behaviour.skipCollapsed = true
                behaviour.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        return dialog
    }

    private fun setupFullHeight(bottomSheet: View) {
        val layoutParams = bottomSheet.layoutParams as CoordinatorLayout.LayoutParams
        layoutParams.height = Resources.getSystem().displayMetrics.heightPixels - BOTTOM_SHEET_MARGIN_TOP.dp
        bottomSheet.layoutParams = layoutParams
    }


    private fun initViews() {
        binding.apply {
            tvWriteToUs.isVisible = fromScreenType == RegistrationCountryFromScreenType.Registration
            tvCountryNotFound.isVisible = fromScreenType == RegistrationCountryFromScreenType.Registration
            tvWriteToUs.setThrottledClickListener { composeEmail() }
            rvAvailableCountryList.layoutManager = LinearLayoutManager(context)
            rvAvailableCountryList.adapter = adapter
            isRegistrationCountry.doAfterSearchTextChanged {
                viewModel.searchCountries(it)
            }
            isRegistrationCountry.forceBtnCloseVisibility(false)
        }
    }

    private fun observeState() {
        viewModel.stateLiveData.observe(viewLifecycleOwner) { state ->
            when (state) {
                is RegistrationCountryState.RegistrationCountryList -> {
                    val filteredCountries = if (fromScreenType == RegistrationCountryFromScreenType.Transport) {
                        state.countries.filter { it.id in transportAvailableCountriesArray }
                    } else {
                        state.countries
                    }
                    adapter.items = filteredCountries
                }
            }
        }
    }

    private fun itemClicked(country: RegistrationCountryModel) {
        setFragmentResult(KEY_COUNTRY, bundleOf(KEY_COUNTRY to country))
        dismiss()
    }

    private fun composeEmail() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(com.meera.core.R.string.auth_write_to_support_email)))
            putExtra(Intent.EXTRA_TEXT, getString(R.string.please_notify_when_noomeera_come_to))
        }
        runCatching {
            startActivity(intent)
        }.onFailure {
            Timber.e(it)
        }
    }

    companion object {
        fun newInstance(type: RegistrationCountryFromScreenType): MeeraRegistrationCountriesFragment {
            return MeeraRegistrationCountriesFragment().apply {
                arguments = bundleOf(KEY_FROM_SCREEN_TYPE to type.name)
            }
        }
    }
}
