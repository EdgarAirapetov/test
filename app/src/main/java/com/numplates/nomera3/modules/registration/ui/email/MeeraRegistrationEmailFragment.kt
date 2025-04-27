package com.numplates.nomera3.modules.registration.ui.email

import android.os.Bundle
import android.text.InputType
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.addClickableText
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.showKeyboard
import com.meera.core.extensions.vibrate
import com.meera.core.extensions.writeToTechSupport
import com.meera.core.utils.KeyboardHeightProvider
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.dp
import com.meera.uikit.widgets.snackbar.AvatarUiState
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.numplates.nomera3.NOOMEERA_USER_AGREEMENT_URL
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentRegistrationEmailBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyHelpPressedWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyInputType
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseFragment
import com.numplates.nomera3.modules.registration.AuthType
import com.numplates.nomera3.modules.registration.ui.RegistrationContainerFragment
import com.numplates.nomera3.modules.registration.ui.RegistrationNavigationViewModel
import com.numplates.nomera3.modules.registration.ui.phoneemail.DELAY_KEYBOARD
import com.numplates.nomera3.modules.registration.ui.phoneemail.MeeraUserBlockedByAdminDialog
import com.numplates.nomera3.presentation.view.utils.NSupport

private const val TAG_USER_BLOCKED_DIALOG = "USER_BLOCKED_DIALOG"
private const val MARGIN_BUTTON = 16

class MeeraRegistrationEmailFragment : MeeraBaseFragment(R.layout.meera_fragment_registration_email) {

    private val binding by viewBinding(MeeraFragmentRegistrationEmailBinding::bind)

    private var errorSnack: UiKitSnackBar? = null

    private var keyboardHeightProvider: KeyboardHeightProvider? = null

    private val viewModel by viewModels<RegistrationEmailViewModel>()
    private val navigationViewModel by viewModels<RegistrationNavigationViewModel>(
        ownerProducer = { requireParentFragment() }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        keyboardHeightProvider = KeyboardHeightProvider(binding.root)
        observe()
        initView()
    }

    override fun onResume() {
        super.onResume()
        doDelayed(RegistrationContainerFragment.SHOW_KEYBOARD_DELAY) {
            setEmail()
            viewModel.checkContinueAvailability()
        }
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
                    sendTo = event.sendTo
                )

                is RegistrationEmailViewEvent.Error -> handleError(event)
                is RegistrationEmailViewEvent.ServerChanged -> requireContext().vibrate()
                else -> {}
            }
        }
    }

    override fun onStart() {
        super.onStart()
        keyboardHeightProvider?.start()
        keyboardHeightProvider?.observer = { keyboardHeight ->
            val rootHeight = binding.root.height
            val buttonsHeight = binding.vgRegEmailButtons.height
            val buttonsBottomMargin = (rootHeight - buttonsHeight) / 2
            val neededMargin = (keyboardHeight - buttonsBottomMargin) * 2 + MARGIN_BUTTON.dp
            if (neededMargin > 0) {
                binding.vgRegEmailButtons.setMargins(bottom = neededMargin)
            } else {
                binding.vgRegEmailButtons.setMargins(bottom = 0)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        keyboardHeightProvider?.release()
    }

    private fun showErrorSnack(message: String?) {
        errorSnack = UiKitSnackBar.make(
            requireView(), SnackBarParams(
                SnackBarContainerUiState(
                    messageText = message,
                    avatarUiState = AvatarUiState.ErrorIconState
                ),
                duration = BaseTransientBottomBar.LENGTH_LONG
            )
        )
        errorSnack?.show()
    }

    private fun initView() {
        initEmailEditor()
        initClicks()
    }

    private fun initEmailEditor() {
        binding.ukiRegistrationEmail.etInput.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        binding.ukiRegistrationEmail.doAfterSearchTextChanged { text ->
            viewModel.setEmail(text)
        }
        binding.ukiRegistrationEmail.etInput.requestFocus()
        binding.ukiRegistrationEmail.etInput.showKeyboard()
    }

    private fun setEmail() {
        viewModel.authType = AuthType.Email
        viewModel.checkContinueAvailability()
    }

    private fun initClicks() {
        binding.btnContinue.setThrottledClickListener {
            viewModel.continueClicked()
        }
        binding.nvRegistrationPhoneEmail.backButtonClickListener = {
            context?.hideKeyboard(requireView())
            doDelayed(DELAY_KEYBOARD.toLong()) {
                navigationViewModel.goBack()
            }
            errorSnack?.dismiss()
        }
        binding.btnHelpRegistration.setThrottledClickListener {
            viewModel.helpClicked(AmplitudePropertyHelpPressedWhere.REGISTRATION)
            requireActivity().writeToTechSupport()
        }
        binding.tvRulesDescription.text = requireContext().getString(R.string.continue_if_agree).addClickableText(
            color = ContextCompat.getColor(requireContext(), R.color.uiKitColorForegroundLink),
            requireContext().getString(R.string.continue_if_agree_link_key)
        ) { NSupport.openLink(requireActivity(), NOOMEERA_USER_AGREEMENT_URL) }
        binding.tvRulesDescription.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun goToNextStep(
        authType: AuthType, sendTo: String
    ) {
        viewModel.clearAll()
        viewModel.saveLastSmsCodeTime()
        navigationViewModel.authCodeIsSent(
            authType, sendTo, null,
            AmplitudePropertyInputType.EMAIL.property, null, null
        )
        navigationViewModel.authCodeIsSent(authType, sendTo, null, null, null, null)
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
                setContinueButtonAvailability(false)
            }

            else -> {}
        }
    }

    private fun showError(message: String?) {
        if (message.isNullOrEmpty()) return
        UiKitSnackBar.make(
            requireView(), SnackBarParams(
                SnackBarContainerUiState(
                    messageText = message,
                    avatarUiState = AvatarUiState.ErrorIconState
                )
            )
        ).show()
    }

    private fun showUserIsBlockedDialog(
        reason: String,
        expiredAt: Long
    ) {
        MeeraUserBlockedByAdminDialog.newInstance(
            blockReason = reason,
            blockDate = expiredAt
        ).show(childFragmentManager, TAG_USER_BLOCKED_DIALOG)
    }

    private fun handleProgress(inProgress: Boolean) {
        setContinueButtonAvailability(!inProgress)
    }

    private fun setContinueButtonAvailability(isAvailable: Boolean) {
        binding.btnContinue.isEnabled = isAvailable
    }

}
