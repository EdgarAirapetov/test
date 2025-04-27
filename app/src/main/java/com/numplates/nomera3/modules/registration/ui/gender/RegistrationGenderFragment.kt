package com.numplates.nomera3.modules.registration.ui.gender

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.viewModels
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentRegistrationGenderBinding
import com.numplates.nomera3.modules.registration.data.RegistrationUserData
import com.numplates.nomera3.modules.registration.ui.RegistrationNavigationViewModel
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.meera.core.extensions.click
import com.meera.core.extensions.clickAnimateScaleUp
import com.meera.core.extensions.hideKeyboard
import com.numplates.nomera3.modules.registration.ui.RegistrationContainerFragment

class RegistrationGenderFragment : BaseFragmentNew<FragmentRegistrationGenderBinding>() {


    private val viewModel by viewModels<RegistrationGenderViewModel>()

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
                    binding?.switchHideGender?.isChecked = event.hideGender
                    binding?.gHiding?.isGone = event.hiddenAgeAndGender
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
        viewModel.progressLiveData.observe(viewLifecycleOwner) {
            handleProgress(it)
        }
    }

    private fun initView() {
        binding?.ivBackIcon?.click {
            it.clickAnimateScaleUp()
            navigationViewModel.goBack()
        }
        binding?.cvNextButton?.click {
            viewModel.continueClicked()
        }
        binding?.genderFemale?.text?.text = getString(R.string.gender_selector_female)
        binding?.genderFemale?.root?.click { setFemaleSelected() }
        binding?.genderMale?.text?.text = getString(R.string.gender_selector_male)
        binding?.genderMale?.root?.click { setMaleSelected() }
        binding?.tvStep?.text = getString(R.string.registration_step_count, STEP)
        binding?.switchHideGender?.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setHideGender(isChecked)
        }
    }

    private fun setFemaleSelected() {
        binding?.genderFemale?.icon?.setImageResource(R.drawable.checkbox_white_checked)
        binding?.genderMale?.icon?.setImageResource(R.drawable.checkbox_white_unchecked)
        viewModel.setGender(RegistrationUserData.GENDER_FEMALE)
    }

    private fun setMaleSelected() {
        binding?.genderFemale?.icon?.setImageResource(R.drawable.checkbox_white_unchecked)
        binding?.genderMale?.icon?.setImageResource(R.drawable.checkbox_white_checked)
        viewModel.setGender(RegistrationUserData.GENDER_MALE)
    }

    private fun handleProgress(inProgress: Boolean) {
        binding?.cvNextButton?.showProgress(inProgress)
    }

    private fun setContinueButtonEnable(isEnabled: Boolean) {
        binding?.cvNextButton?.isEnabled = isEnabled
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentRegistrationGenderBinding
        get() = FragmentRegistrationGenderBinding::inflate

    companion object {
        const val STEP = "3"
    }
}
