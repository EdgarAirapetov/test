package com.numplates.nomera3.modules.registration.ui.email

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import com.meera.core.extensions.addClickableText
import com.meera.core.extensions.click
import com.meera.core.extensions.clickAnimateScaleUp
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.gone
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.showKeyboard
import com.meera.core.extensions.vibrate
import com.meera.core.extensions.visible
import com.meera.core.extensions.writeToTechSupport
import com.meera.core.utils.KeyboardHeightProvider
import com.meera.core.utils.NSnackbar
import com.numplates.nomera3.NOOMEERA_USER_AGREEMENT_URL
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentRegistrationEmailBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyHelpPressedWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyInputType
import com.numplates.nomera3.modules.registration.AuthType
import com.numplates.nomera3.modules.registration.ui.RegistrationContainerFragment
import com.numplates.nomera3.modules.registration.ui.RegistrationNavigationViewModel
import com.numplates.nomera3.modules.registration.ui.phoneemail.DELAY_KEYBOARD
import com.numplates.nomera3.modules.registration.ui.phoneemail.HeaderDialogType
import com.numplates.nomera3.modules.user.ui.dialog.BlockUserByAdminDialogFragment
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.view.utils.NSupport
import com.numplates.nomera3.presentation.view.utils.NToast
import io.reactivex.disposables.CompositeDisposable

private const val ERROR_SNACK_MARGIN = 12
private const val SNACKBAR_BOTTOM_MAX_MEDIA_COUNT_MARGIN_DP = 12

class RegistrationEmailFragment :
    BaseFragmentNew<FragmentRegistrationEmailBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) ->
    FragmentRegistrationEmailBinding
        get() = FragmentRegistrationEmailBinding::inflate

    private var errorSnack: NSnackbar? = null

    private var keyboardHeightProvider: KeyboardHeightProvider? = null

    private var isKeyboardOpen = false

    private val viewModel by viewModels<RegistrationEmailViewModel>()
    private val navigationViewModel by viewModels<RegistrationNavigationViewModel>(
        ownerProducer = { requireParentFragment() }
    )

    private val disposable = CompositeDisposable()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observe()
        initKeyboardListener()
        initView()
    }

    override fun onResume() {
        super.onResume()
        doDelayed(RegistrationContainerFragment.SHOW_KEYBOARD_DELAY) {
            setEmail()
            viewModel.checkContinueAvailability()
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
                is RegistrationEmailViewEvent.SendCodeSuccess -> goToNextStep(
                    authType = event.authType,
                    sendTo = event.sendTo,
                    countryCode = null,
                    countryName = null,
                    countryMask = null
                )

                is RegistrationEmailViewEvent.Error -> handleError(event)
                is RegistrationEmailViewEvent.ServerChanged -> requireContext().vibrate()
                else -> {}
            }
        }
    }

    private fun initKeyboardListener() {
        keyboardHeightProvider?.release()
        binding?.root?.let { root ->
            keyboardHeightProvider = KeyboardHeightProvider(root)
            keyboardHeightProvider?.observer = { height ->
                isKeyboardOpen = height > 0
            }
        }
    }

    private fun showErrorSnack(message: String?) {
        errorSnack = NSnackbar.with(view)
            .typeError()
            .inView(view)
            .text(message)
            .marginBottom(
                if (isKeyboardOpen) {
                    SNACKBAR_BOTTOM_MAX_MEDIA_COUNT_MARGIN_DP
                } else {
                    ERROR_SNACK_MARGIN
                }
            )
            .durationLong()
            .show()
    }

    private fun initView() {
        initEmailEditor()
        initClicks()
        binding?.etInputEmail?.transformationMethod = HideReturnsTransformationMethod.getInstance()
    }

    private fun initEmailEditor() {
        binding?.etInputEmail?.debounceInput(AuthType.Email)
    }

    private fun EditText.debounceInput(authType: AuthType) {
        disposable.clear()
        doAfterTextChanged {
            val text = when (authType) {
                else -> binding?.etInputEmail?.text?.trim().toString()
            }
            when (authType) {
                else -> viewModel.setEmail(text)
            }
        }
    }

    private fun setEmail() {
        viewModel.authType = AuthType.Email
        binding?.etInputEmail?.visible()
        binding?.etInputEmail?.requestFocus()
        binding?.etInputEmail.showKeyboard()
        viewModel.checkContinueAvailability()
    }

    private fun initClicks() {
        binding?.tvContinueButton?.click {
            viewModel.continueClicked()
        }
        binding?.btnBack?.click {
            it.clickAnimateScaleUp()
            context?.hideKeyboard(requireView())
            doDelayed(DELAY_KEYBOARD.toLong()) {
                navigationViewModel.goBack()
            }
            errorSnack?.dismiss()
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

    private fun goToNextStep(
        authType: AuthType, sendTo: String, countryCode: String?, countryName: String?, countryMask: String?
    ) {
        viewModel.clearAll()
        viewModel.saveLastSmsCodeTime()
        navigationViewModel.authCodeIsSent(
            authType, sendTo, countryCode,
            AmplitudePropertyInputType.EMAIL.property, countryMask,null
        )
        navigationViewModel.authCodeIsSent(authType, sendTo, countryCode, countryName, countryMask, null)
    }

    private fun handleError(error: RegistrationEmailViewEvent.Error) {
        when (error) {
            is RegistrationEmailViewEvent.Error.EmailEmpty ->
                showError(getString(R.string.error_error_is_empty))

            is RegistrationEmailViewEvent.Error.EmailIncorrect ->
                showError(getString(R.string.auth_incorrect_email))

            is RegistrationEmailViewEvent.Error.UserBlocked ->
                showUserIsBlockedDialog(
                    reason = error.reason ?: "",
                    expiredAt = error.blockExpired ?: 0L
                )

            is RegistrationEmailViewEvent.Error.UserNotFound -> {
                context?.hideKeyboard(requireView())
                showErrorSnack(error.reason)
            }

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
            show(this@RegistrationEmailFragment.childFragmentManager, tag)
        }
    }

    private fun handleProgress(inProgress: Boolean) {
//        binding?.etInputEmail?.isEnabled = !inProgress
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
            binding?.tvContinueButton?.alpha = ALPHA_ENABLED
        } else {
            binding?.tvContinueButton?.alpha = ALPHA_DISABLED
        }
    }

    companion object {
        val RULES_TEXT_HIGHLIGHT_RANGE = 25..76
        private const val ALPHA_DISABLED = .6F
        private const val ALPHA_ENABLED = 1F
    }
}
