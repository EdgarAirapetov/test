package com.numplates.nomera3.modules.registration.ui.country.fragment

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.meera.core.extensions.dp
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.core.INetworkValues
import com.numplates.nomera3.databinding.FragmentRegistrationCountryListBinding
import com.numplates.nomera3.modules.registration.domain.model.RegistrationCountryModel
import com.numplates.nomera3.modules.registration.domain.model.RegistrationCountryState
import com.numplates.nomera3.modules.registration.ui.country.adapter.RegistrationCountryAdapter
import com.numplates.nomera3.modules.registration.ui.country.viewmodel.RegistrationCountryViewModel
import com.numplates.nomera3.presentation.router.BaseBottomSheetDialogFragment
import com.numplates.nomera3.presentation.view.fragments.HorizontalLineDivider
import timber.log.Timber

const val KEY_COUNTRY = "COUNTRY"
private const val KEY_FROM_SCREEN_TYPE = "KEY_FROM_SCREEN_TYPE"
private const val TYPE_ANY = "*/*"

class RegistrationCountryFragment : BaseBottomSheetDialogFragment<FragmentRegistrationCountryListBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentRegistrationCountryListBinding
        get() = FragmentRegistrationCountryListBinding::inflate

    private val viewModel by viewModels<RegistrationCountryViewModel> { App.component.getViewModelFactory() }

    private var bottomSheet: FrameLayout? = null

    private val adapter by lazy {
        RegistrationCountryAdapter(fromScreenType, this::itemClicked)
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

    override fun getTheme(): Int = R.style.BottomSheetDialogTheme

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        dialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            bottomSheet =
                bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?
            bottomSheet?.let {
                val bottomSheetBehavior = BottomSheetBehavior.from(it)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                bottomSheetBehavior.skipCollapsed = true
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        observeState()
        viewModel.loadCountries(fromScreenType)
    }

    private fun initViews() {
        binding?.apply {
            tvWriteToUs.isVisible = fromScreenType == RegistrationCountryFromScreenType.Registration
            tvCountryNotFound.isVisible = fromScreenType == RegistrationCountryFromScreenType.Registration
            tvWriteToUs.setThrottledClickListener { composeEmail() }
            rvAvailableCountryList.layoutManager = LinearLayoutManager(context)
            rvAvailableCountryList.adapter = adapter
            rvAvailableCountryList.addItemDecoration(HorizontalLineDivider(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.drawable_friend_list_divider_decoration
                ) ?: return@apply,
                54.dp
            ))
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
                    binding?.rvAvailableCountryList?.post {
                        bottomSheet?.let {
                            val bottomSheetBehavior = BottomSheetBehavior.from(it)
                            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                            bottomSheetBehavior.skipCollapsed = true
                        }
                    }
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
            type = TYPE_ANY
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("help@noomeera.com"))
            putExtra(Intent.EXTRA_TEXT, getString(R.string.please_notify_when_noomeera_come_to))
        }
        runCatching {
            startActivity(intent)
        }.onFailure {
            Timber.e(it)
        }
    }

    companion object {
        fun newInstance(type: RegistrationCountryFromScreenType): RegistrationCountryFragment {
            return RegistrationCountryFragment().apply {
                arguments = bundleOf(KEY_FROM_SCREEN_TYPE to type.name)
            }
        }
    }
}
