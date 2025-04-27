package com.numplates.nomera3.modules.registration.ui.birthday

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import com.meera.core.extensions.click
import com.meera.core.extensions.gone
import com.meera.core.extensions.showKeyboard
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentRegistrationBirthdayBinding
import com.numplates.nomera3.modules.registration.ui.RegistrationContainerFragment.Companion.SHOW_KEYBOARD_DELAY
import com.numplates.nomera3.modules.registration.ui.RegistrationNavigationViewModel
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.meera.core.extensions.clickAnimateScaleUp
import com.meera.core.extensions.doDelayed
import com.numplates.nomera3.modules.registration.ui.RegistrationContainerFragment


class RegistrationBirthdayFragment: BaseFragmentNew<FragmentRegistrationBirthdayBinding>() {

    private val viewModel by viewModels<RegistrationBirthdayViewModel>()

    private val navigationViewModel by viewModels<RegistrationNavigationViewModel>(
        ownerProducer = { requireParentFragment() }
    )
    private val argCountryNumber by lazy { requireArguments().getString(RegistrationContainerFragment.ARG_COUNTRY_NUMBER) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        observeViewModel()
        viewModel.initUserData()
        binding?.gHiding?.isGone = viewModel.isHiddenAge()
    }

    override fun onResume() {
        super.onResume()
        doDelayed(SHOW_KEYBOARD_DELAY) {
            binding?.etBirthday?.requestFocus()
            binding?.etBirthday?.showKeyboard()
        }
    }

    private fun initView() {
        binding?.ivBackIcon?.click {
            it.clickAnimateScaleUp()
            navigationViewModel.goBack()
        }
        binding?.tvStep?.text = getString(R.string.registration_step_count, STEP)
        binding?.cvNextButton?.click {
            viewModel.continueClicked()
        }
        binding?.switchHideAge?.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setHideAge(isChecked)
        }
        setContinueButtonAvailability(false)
    }

    private fun observeViewModel() {
        viewModel.eventLiveData.observe(viewLifecycleOwner) { event ->
            when (event) {
                is RegistrationBirthdayViewEvent.BirthdayData -> {
                    initBirthday(event.birthday, event.hideAge)
                }
                is RegistrationBirthdayViewEvent.GoToNextStep -> {
                    viewModel.clearAll()
                    navigationViewModel.registrationBirthdayNext(argCountryNumber)
                }
                else -> {}
            }
        }
        viewModel.progressLiveData.observe(viewLifecycleOwner) {
            binding?.cvNextButton?.showProgress(it)
        }
    }

    private fun handleBirthdayInput(birthday: String?) {
        viewModel.setBirthday(birthday)
        if (birthday.isNullOrEmpty() || birthday.length < BIRTHDAY_INPUT_FORMAT.length) {
            setContinueButtonAvailability(false)
            return
        }
        when (birthday.validateBirthday()) {
            is BirthdayValidationResult.BirthdayIncorrect ->
                showBirthdayDescription(getString(R.string.birthdate_incorrect))
            is BirthdayValidationResult.TooYoung ->
                showBirthdayDescription(getString(R.string.age_should_not_be_less_than_17))
            is BirthdayValidationResult.TooOld ->
                showBirthdayDescription(getString(R.string.age_should_not_be_larger_than_89))
            is BirthdayValidationResult.BirthdayCorrect ->
                setContinueButtonAvailability(true)
        }
    }

    private fun initBirthday(birthday: String?, hideAge: Boolean) {
        binding?.etBirthday?.apply {
            addTextChangedListener( onTextChanged = { chars, _, _, _ ->
                binding?.tvBirthdayDescription?.gone()
                handleBirthdayInput(rawText)
            })
            setText(birthday)
            setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    setSelection(text?.length ?: 0)
                }
            }
            setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_UP -> showKeyboard()
                    MotionEvent.ACTION_DOWN -> setSelection(text?.length ?: 0)
                }
                performClick()
                return@setOnTouchListener true
            }
        }
        binding?.switchHideAge?.isChecked = hideAge
    }

    private fun showBirthdayDescription(message: String?) {
        binding?.cvNextButton?.isEnabled = false
        binding?.tvBirthdayDescription?.text = message
        binding?.tvBirthdayDescription?.visible()
    }

    private fun setContinueButtonAvailability(enabled: Boolean) {
        binding?.cvNextButton?.isEnabled = enabled
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentRegistrationBirthdayBinding
        get() = FragmentRegistrationBirthdayBinding::inflate

    companion object {
        const val STEP = "2"
    }
}
