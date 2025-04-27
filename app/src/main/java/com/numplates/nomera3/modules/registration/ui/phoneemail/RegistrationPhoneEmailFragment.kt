package com.numplates.nomera3.modules.registration.ui.phoneemail

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.meera.core.extensions.addClickableText
import com.meera.core.extensions.checkCorrectSelection
import com.meera.core.extensions.click
import com.meera.core.extensions.clickAnimateScaleUp
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.showKeyboard
import com.meera.core.extensions.vibrate
import com.meera.core.extensions.visible
import com.meera.core.extensions.writeToTechSupport
import com.meera.core.utils.NSnackbar
import com.meera.core.utils.getInDurationFromSeconds
import com.numplates.nomera3.NOOMEERA_USER_AGREEMENT_URL
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentRegistrationPhoneEmailBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyHelpPressedWhere
import com.numplates.nomera3.modules.registration.AuthType
import com.numplates.nomera3.modules.registration.domain.model.RegistrationCountryModel
import com.numplates.nomera3.modules.registration.ui.RegistrationContainerFragment
import com.numplates.nomera3.modules.registration.ui.RegistrationNavigationViewModel
import com.numplates.nomera3.modules.registration.ui.code.DEFAULT_MASK_CHAR
import com.numplates.nomera3.modules.registration.ui.code.SERVER_MASK_CHAR
import com.numplates.nomera3.modules.registration.ui.country.fragment.KEY_COUNTRY
import com.numplates.nomera3.modules.registration.ui.country.fragment.RegistrationCountryFragment
import com.numplates.nomera3.modules.registration.ui.country.fragment.RegistrationCountryFromScreenType
import com.numplates.nomera3.modules.user.ui.dialog.BlockUserByAdminDialogFragment
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.view.utils.NSupport
import com.numplates.nomera3.presentation.view.utils.NToast
import io.reactivex.disposables.CompositeDisposable

const val DELAY_KEYBOARD = 250

class RegistrationPhoneEmailFragment :
    BaseFragmentNew<FragmentRegistrationPhoneEmailBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) ->
    FragmentRegistrationPhoneEmailBinding
        get() = FragmentRegistrationPhoneEmailBinding::inflate

    private val viewModel by viewModels<RegistrationPhoneEmailViewModel>()
    private val navigationViewModel by viewModels<RegistrationNavigationViewModel>(
        ownerProducer = { requireParentFragment() }
    )

    private val disposable = CompositeDisposable()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observe()
        initView()
    }

    override fun onResume() {
        super.onResume()
        doDelayed(RegistrationContainerFragment.SHOW_KEYBOARD_DELAY) {
            if (viewModel.authType == AuthType.Phone) setPhone()
            else setEmail()
            viewModel.checkContinueAvailability()
        }
        setFragmentResultListener(KEY_COUNTRY) { _, bundle ->
            val chosenCountry =
                bundle.getParcelable<RegistrationCountryModel>(KEY_COUNTRY) ?: return@setFragmentResultListener
            viewModel.newCountryChosen(chosenCountry)
        }
    }

    override fun onStop() {
        super.onStop()
        disposable.clear()
    }

    private fun observe() {
        viewModel.progressLiveData.observe(viewLifecycleOwner) { handleProgress(it) }
        viewModel.continueButtonAvailabilityLiveData.observe(viewLifecycleOwner) {
            setContinueButtonAvailability(it)
        }
        viewModel.liveData.observe(viewLifecycleOwner) { event ->
            when (event) {
                is RegistrationPhoneEmailViewEvent.SendCodeSuccess -> {
                    if (event.blockTime == null) {
                        snackbarBlockedAlert?.dismiss()
                        goToNextStep(
                            authType = event.authType,
                            sendTo = event.sendTo,
                            countryCode = event.countryCode,
                            countryName = event.countryName,
                            countryMask = event.countryMask,
                            smsTimeout = event.timeout
                        )
                    } else {
                        blockedAlert(event.blockTime)
                    }
                }

                is RegistrationPhoneEmailViewEvent.Error -> handleError(event)
                is RegistrationPhoneEmailViewEvent.ServerChanged -> requireContext().vibrate()
                is RegistrationPhoneEmailViewEvent.CountryDetected -> {
                    val country = event.country
                    binding?.apply {
                        ivCountryIcon?.loadGlide(country.flag)
                        tvCountryCode.text = country.code
                        etInputPhone.mask = country.mask?.replace(SERVER_MASK_CHAR, DEFAULT_MASK_CHAR)
                        doDelayed(DELAY_KEYBOARD.toLong()) {
                            binding?.etInputPhone?.showKeyboard()
                        }
                    }
                }

                is RegistrationPhoneEmailViewEvent.None -> {
                    if (event.authType == AuthType.Phone) {
                        event.country?.let { country ->
                            binding?.apply {
                                ivCountryIcon?.loadGlide(country.flag)
                                tvCountryCode.text = country.code
                                etInputPhone.mask = country.mask?.replace(SERVER_MASK_CHAR, DEFAULT_MASK_CHAR)
                                if (event.showKeyboard) {
                                    doDelayed(DELAY_KEYBOARD.toLong()) {
                                        binding?.etInputPhone?.showKeyboard()
                                    }
                                }
                            }
                        }
                        event.phone?.let {
                            binding?.etInputPhone?.setText(it)
                        }
                    }
                }

                else -> {}
            }
        }
    }

    private fun initView() {
        initPhoneEditor()
        initClicks()
    }

    private fun initPhoneEditor() {
        binding?.etInputPhone?.debounceInput(AuthType.Phone)
        binding?.vgCountryFlag?.setThrottledClickListener {
            navigateToCountries()
        }
        binding?.ivArrowDown?.setThrottledClickListener {
            navigateToCountries()
        }
    }

    private fun navigateToCountries() {
        val fragment = RegistrationCountryFragment.newInstance(
            RegistrationCountryFromScreenType.Registration
        )
        fragment.show(parentFragmentManager, KEY_COUNTRY)
    }

    private fun EditText.debounceInput(authType: AuthType) {
        disposable.clear()
        doAfterTextChanged {
            val text = when (authType) {
                is AuthType.Phone -> binding?.tvCountryCode?.text.toString() + binding?.etInputPhone?.rawText?.trim()
                else -> binding?.etInputEmail?.text?.trim().toString()
            }
            when (authType) {
                is AuthType.Phone -> viewModel.setPhone(text)
                else -> viewModel.setEmail(text)
            }
        }
    }

    private fun setPhone() {
        viewModel.authType = AuthType.Phone
        binding?.etInputEmail?.gone()
        binding?.etInputPhone?.visible()
        binding?.vgCountryFlag?.visible()
        binding?.tvCountryCode?.visible()
        binding?.ivArrowDown?.visible()
        binding?.etInputPhone?.requestFocus()
        binding?.etInputPhone?.showKeyboard()
        viewModel.checkContinueAvailability()
    }

    private fun setEmail() {
        viewModel.authType = AuthType.Email
        binding?.etInputPhone?.gone()
        binding?.etInputEmail?.visible()
        binding?.vgCountryFlag?.gone()
        binding?.tvCountryCode?.gone()
        binding?.ivArrowDown?.gone()
        binding?.etInputEmail?.requestFocus()
        binding?.etInputEmail.showKeyboard()
        viewModel.checkContinueAvailability()
    }

    private fun initClicks() {
        binding?.tvContinueButton?.click {
            viewModel.continueClicked()
        }
        binding?.ivCloseIcon?.click {
            it.clickAnimateScaleUp()
            context?.hideKeyboard(requireView())
            doDelayed(DELAY_KEYBOARD.toLong()) {
                navigationViewModel.goBack()
            }
        }
        binding?.tvEmail?.setThrottledClickListener {
            snackbarBlockedAlert?.dismiss()
            binding?.tvEmail?.clickAnimateScaleUp()
            context?.hideKeyboard(requireView())
            doDelayed(DELAY_KEYBOARD.toLong()) {
                navigationViewModel.registrationEmailNext()
            }
        }
        binding?.tvHelp?.click {
            viewModel.helpClicked(AmplitudePropertyHelpPressedWhere.REGISTRATION)
            act?.writeToTechSupport()
        }
        binding?.tvRulesDescription?.text = requireContext().getString(R.string.continue_if_agree).addClickableText(
            color = ContextCompat.getColor(requireContext(), R.color.white),
            requireContext().getString(R.string.continue_if_agree_link_key)
        ) { NSupport.openLink(act, NOOMEERA_USER_AGREEMENT_URL) }
        binding?.tvRulesDescription?.movementMethod = LinkMovementMethod.getInstance()
    }

    private var snackbarBlockedAlert: NSnackbar? = null
    private fun blockedAlert(blockTime: Long) {
        viewModel.clearAll(false)
        val timeString = getInDurationFromSeconds(requireContext(), blockTime.toInt())
        snackbarBlockedAlert = NSnackbar.with(view)
            .inView(view)
            .marginBottom(BLOCK_ALERT_MARGIN_BOTTOM)
            .text(requireContext().getString(R.string.auth_you_blocked_for_time, timeString))
            .typeError()
            .show()
    }

    private fun goToNextStep(
        authType: AuthType,
        sendTo: String,
        countryCode: String?,
        countryName: String?,
        countryMask: String?,
        smsTimeout: Long?
    ) {
        viewModel.clearAll()
        viewModel.saveLastSmsCodeTime()
        navigationViewModel.authCodeIsSent(authType, sendTo, countryCode, countryName, countryMask, smsTimeout)
    }

    private fun handleError(error: RegistrationPhoneEmailViewEvent.Error) {
        when (error) {
            is RegistrationPhoneEmailViewEvent.Error.PhoneEmpty ->
                showError(getString(R.string.error_number_is_empty))

            is RegistrationPhoneEmailViewEvent.Error.PhoneIncorrect ->
                showError(getString(R.string.auth_incorrect_phone))

            is RegistrationPhoneEmailViewEvent.Error.EmailEmpty ->
                showError(getString(R.string.error_error_is_empty))

            is RegistrationPhoneEmailViewEvent.Error.EmailIncorrect ->
                showError(getString(R.string.auth_incorrect_email))

            is RegistrationPhoneEmailViewEvent.Error.UserBlocked ->
                showUserIsBlockedDialog(
                    reason = error.reason ?: "",
                    expiredAt = error.blockExpired ?: 0L
                )

            else -> {}
        }
    }

    private fun showError(message: String?) {
        if (message.isNullOrEmpty()) return
        NToast.with(requireView())
            .typeError()
            .text(message)
            .show()
    }

    private fun showUserIsBlockedDialog(
        reason: String,
        expiredAt: Long
    ) {
        BlockUserByAdminDialogFragment().apply {
            blockReason = reason
            blockDateUnixtimeSec = expiredAt
            closeDialogClickListener = { dialog?.dismiss() }
            writeSupportClickListener = {
                viewModel.helpClicked(AmplitudePropertyHelpPressedWhere.REGISTRATION)
                act?.writeToTechSupport()
            }
            headerDialogType = HeaderDialogType.BlockedProfileType
            show(this@RegistrationPhoneEmailFragment.childFragmentManager, tag)
        }
    }

    private fun handleProgress(inProgress: Boolean) {
        binding?.etInputPhone?.isEnabled = !inProgress
        if (inProgress) {
            binding?.progressBar?.visible()
            binding?.tvContinueButton?.text = null
        } else {
            binding?.progressBar?.gone()
            binding?.tvContinueButton?.text = getString(R.string.general_continue)
        }
        setContinueButtonAvailability(!inProgress)
    }

    private fun setContinueButtonAvailability(isAvailable: Boolean) {
        binding?.tvContinueButton?.isEnabled = isAvailable
        if (isAvailable) {
            binding?.etInputPhone?.checkCorrectSelection()
            binding?.tvContinueButton?.alpha = ALPHA_ENABLED
        } else {
            binding?.tvContinueButton?.alpha = ALPHA_DISABLED
        }
    }

    companion object {
        private const val ALPHA_DISABLED = .6F
        private const val ALPHA_ENABLED = 1F
        private val BLOCK_ALERT_MARGIN_BOTTOM = 8.dp
    }
}
