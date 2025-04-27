package com.numplates.nomera3.modules.registration.ui.birthday

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.showKeyboard
import com.meera.core.extensions.visible
import com.meera.core.utils.KeyboardHeightProvider
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentRegistrationBirthdayBinding
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseFragment
import com.numplates.nomera3.modules.registration.ui.RegistrationContainerFragment
import com.numplates.nomera3.modules.registration.ui.RegistrationContainerFragment.Companion.SHOW_KEYBOARD_DELAY
import com.numplates.nomera3.modules.registration.ui.RegistrationNavigationViewModel

private const val MARGIN_BUTTON = 16

class MeeraRegistrationBirthdayFragment: MeeraBaseFragment(R.layout.meera_fragment_registration_birthday) {

    private val binding by viewBinding(MeeraFragmentRegistrationBirthdayBinding::bind)

    private val viewModel by viewModels<RegistrationBirthdayViewModel>()

    private val navigationViewModel by viewModels<RegistrationNavigationViewModel>(
        ownerProducer = { requireParentFragment() }
    )

    private var keyboardHeightProvider: KeyboardHeightProvider? = null
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
        doDelayed(SHOW_KEYBOARD_DELAY) {
            binding.etBirthday.requestFocus()
            binding.etBirthday.showKeyboard()
        }
    }

    private fun initView() {
        binding.btnBack.setThrottledClickListener {
            navigationViewModel.goBack()
        }
        binding.btnContinue.setThrottledClickListener {
            viewModel.continueClicked()
        }
        setContinueButtonAvailability(false)
    }

    private fun observeViewModel() {
        viewModel.eventLiveData.observe(viewLifecycleOwner) { event ->
            when (event) {
                is RegistrationBirthdayViewEvent.BirthdayData -> {
                    initBirthday(event.birthday)
                }
                is RegistrationBirthdayViewEvent.GoToNextStep -> {
                    viewModel.clearAll()
                    navigationViewModel.registrationBirthdayNext(argCountryNumber)
                }
                else -> {}
            }
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

    private fun initBirthday(birthday: String?) {
        binding.etBirthday.apply {
            addTextChangedListener( onTextChanged = { _, _, _, _ ->
                binding.vgErrorContainer.gone()
                handleBirthdayInput(rawText)
            })
            setText(birthday)
            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    setSelection(text?.length ?: 0)
                }
            }
            setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_UP -> showKeyboard()
                    MotionEvent.ACTION_DOWN -> setSelection(text?.length ?: 0)
                }
                performClick()
                return@setOnTouchListener true
            }
        }
    }

    private fun showBirthdayDescription(message: String?) {
        binding.btnContinue.isEnabled = false
        binding.tvErrorMessage.text = message
        binding.vgErrorContainer.visible()
    }

    private fun setContinueButtonAvailability(enabled: Boolean) {
        binding.btnContinue.isEnabled = enabled
    }

}
