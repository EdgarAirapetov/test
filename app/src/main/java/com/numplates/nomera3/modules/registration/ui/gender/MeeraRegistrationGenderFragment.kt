package com.numplates.nomera3.modules.registration.ui.gender

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.click
import com.meera.core.extensions.dp
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.utils.KeyboardHeightProvider
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentRegistrationGenderBinding
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseFragment
import com.numplates.nomera3.modules.registration.data.RegistrationUserData
import com.numplates.nomera3.modules.registration.ui.RegistrationContainerFragment
import com.numplates.nomera3.modules.registration.ui.RegistrationNavigationViewModel

private const val MARGIN_BUTTON = 16

class MeeraRegistrationGenderFragment : MeeraBaseFragment(R.layout.meera_fragment_registration_gender) {

    private val binding by viewBinding(MeeraFragmentRegistrationGenderBinding::bind)

    private val viewModel by viewModels<RegistrationGenderViewModel>()

    private var keyboardHeightProvider: KeyboardHeightProvider? = null
    private val navigationViewModel by viewModels<RegistrationNavigationViewModel>(
        ownerProducer = { requireParentFragment() }
    )

    private val argCountryNumber by lazy { requireArguments().getString(RegistrationContainerFragment.ARG_COUNTRY_NUMBER) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        observeViewModel()
        viewModel.initUserData()
    }

    override fun onStart() {
        super.onStart()
        keyboardHeightProvider?.release()
        keyboardHeightProvider = KeyboardHeightProvider(binding.root)
        keyboardHeightProvider?.observer = { height ->
            binding.btnContinue.setMargins(bottom = height + MARGIN_BUTTON.dp, end = MARGIN_BUTTON.dp)
        }
    }

    override fun onStop() {
        super.onStop()
        keyboardHeightProvider?.release()
    }

    override fun onResume() {
        super.onResume()
        context?.hideKeyboard(requireView())
    }

    private fun observeViewModel() {
        viewModel.liveData.observe(viewLifecycleOwner) { event ->
            when (event) {
                is RegistrationGenderViewEvent.Gender -> {
                    when (event.gender) {
                        RegistrationUserData.GENDER_MALE -> setMaleSelected()
                        RegistrationUserData.GENDER_FEMALE -> setFemaleSelected()
                        else -> setContinueButtonEnable(false)
                    }
                }
                is RegistrationGenderViewEvent.SetContinueButtonAvailable -> {
                    setContinueButtonEnable(event.isEnabled)
                }
                is RegistrationGenderViewEvent.GoToNextStep -> {
                    viewModel.clearAll()
                    navigationViewModel.registrationGenderNext(argCountryNumber)
                }
                else -> {}
            }
        }
    }

    private fun initView() {
        binding.btnBack.setThrottledClickListener {
            navigationViewModel.goBack()
        }
        binding.btnContinue.click {
            viewModel.continueClicked()
        }
        binding.vgFemale.click { setFemaleSelected() }
        binding.vgMale.click { setMaleSelected() }
    }

    private fun setFemaleSelected() {
        binding.rbFemale.checked = true
        binding.rbMale.checked = false
        viewModel.setGender(RegistrationUserData.GENDER_FEMALE)
    }

    private fun setMaleSelected() {
        binding.rbFemale.checked = false
        binding.rbMale.checked = true
        viewModel.setGender(RegistrationUserData.GENDER_MALE)
    }

    private fun setContinueButtonEnable(isEnabled: Boolean) {
        binding.btnContinue.isEnabled = isEnabled
    }
}
