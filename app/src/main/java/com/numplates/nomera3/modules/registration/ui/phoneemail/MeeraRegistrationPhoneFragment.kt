package com.numplates.nomera3.modules.registration.ui.phoneemail

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.Gravity
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.snackbar.BaseTransientBottomBar.ANIMATION_MODE_FADE
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.addClickableText
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.empty
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.showKeyboard
import com.meera.core.extensions.vibrate
import com.meera.core.extensions.writeToTechSupport
import com.meera.core.utils.KeyboardHeightProvider
import com.meera.core.utils.getInDurationFromSeconds
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.state.PaddingState
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.dp
import com.meera.uikit.widgets.snackbar.AvatarUiState
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.numplates.nomera3.NOOMEERA_USER_AGREEMENT_URL
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentRegistrationPhoneBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyHelpPressedWhere
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseFragment
import com.numplates.nomera3.modules.registration.AuthType
import com.numplates.nomera3.modules.registration.domain.model.RegistrationCountryModel
import com.numplates.nomera3.modules.registration.ui.RegistrationContainerFragment
import com.numplates.nomera3.modules.registration.ui.RegistrationNavigationViewModel
import com.numplates.nomera3.modules.registration.ui.code.DEFAULT_MASK_CHAR
import com.numplates.nomera3.modules.registration.ui.code.SERVER_MASK_CHAR
import com.numplates.nomera3.modules.registration.ui.country.fragment.KEY_COUNTRY
import com.numplates.nomera3.modules.registration.ui.country.fragment.MeeraRegistrationCountriesFragment
import com.numplates.nomera3.modules.registration.ui.country.fragment.RegistrationCountryFromScreenType
import com.numplates.nomera3.presentation.view.utils.NSupport
import java.util.concurrent.TimeUnit

private const val TAG_USER_BLOCKED_DIALOG = "USER_BLOCKED_DIALOG"
private const val PADDING_SNACK = 72
private const val SNACKBAR_DURATION = 4L

class MeeraRegistrationPhoneFragment :
    MeeraBaseFragment(R.layout.meera_fragment_registration_phone) {

    private val binding by viewBinding(MeeraFragmentRegistrationPhoneBinding::bind)
    private val act by lazy { activity as MeeraAct }
    private var snackbarBlockedAlert: UiKitSnackBar? = null
    private var keyboardHeightProvider: KeyboardHeightProvider? = null

    private val viewModel by viewModels<RegistrationPhoneEmailViewModel>()
    private val navigationViewModel by viewModels<RegistrationNavigationViewModel>(
        ownerProducer = { requireParentFragment() }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initKeyboardHeightProvider()
        observe()
        initView()
    }

    override fun onResume() {
        super.onResume()
        doDelayed(RegistrationContainerFragment.SHOW_KEYBOARD_DELAY) {
            setPhone()
            viewModel.checkContinueAvailability()
        }
        setFragmentResultListener(KEY_COUNTRY) { _, bundle ->
            val chosenCountry =
                bundle.getParcelable<RegistrationCountryModel>(KEY_COUNTRY) ?: return@setFragmentResultListener
            viewModel.newCountryChosen(chosenCountry)
        }
    }

    override fun onStart() {
        super.onStart()
        keyboardHeightProvider?.start()
        keyboardHeightProvider?.observer = { keyboardHeight ->
            val rootHeight = binding.root.height
            val buttonsHeight = binding.vgRegButtons.height
            val buttonsBottomMargin = (rootHeight - buttonsHeight) / 2
            val neededMargin = (keyboardHeight - buttonsBottomMargin) * 2
            if (neededMargin > 0) {
                binding.vgRegButtons.setMargins(bottom = neededMargin)
            } else {
                binding.vgRegButtons.setMargins(bottom = 0)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        keyboardHeightProvider?.release()
    }

    private fun initKeyboardHeightProvider() {
        keyboardHeightProvider = KeyboardHeightProvider(binding.root)
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
                is RegistrationPhoneEmailViewEvent.CountryDetected -> applyNewCountry(event.country)
                is RegistrationPhoneEmailViewEvent.None -> {
                    if (event.authType == AuthType.Phone) {
                        event.country?.let { country ->
                            applyNewCountry(country)
                        }
                        event.phone?.let {
                            binding.ipRegistrationPhone.setText(it)
                        }
                    }
                }

                else -> Unit
            }
        }
    }

    private fun applyNewCountry(country: RegistrationCountryModel) {
        binding.apply {
            Glide.with(ipRegistrationPhone)
                .asBitmap()
                .load(country.flag)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        ipRegistrationPhone.setFlag(resource)
                    }

                    override fun onLoadCleared(placeholder: Drawable?) = Unit
                })
            ipRegistrationPhone.setCode(country.code ?: String.empty())
            ipRegistrationPhone.setMask(country.mask?.replace(SERVER_MASK_CHAR, DEFAULT_MASK_CHAR) ?: String.empty())
            doDelayed(DELAY_KEYBOARD.toLong()) {
                ipRegistrationPhone.showKeyboard()
            }
        }
    }

    private fun initView() {
        initPhoneEditor()
        initClicks()
    }

    private fun initPhoneEditor() {
        binding.ipRegistrationPhone.doAfterSearchTextChanged {
            val phoneText = binding.ipRegistrationPhone.countryCode + binding.ipRegistrationPhone.rawText
            viewModel.setPhone(phoneText)
        }
        binding.ipRegistrationPhone.setOnFlagClickListenerListener { navigateToCountries() }
        binding.ipRegistrationPhone.setHint(getString(R.string.phone_hint))
        binding.ipRegistrationPhone.etInput.showKeyboard()
    }

    private fun navigateToCountries() {
        val fragment = MeeraRegistrationCountriesFragment.newInstance(
            RegistrationCountryFromScreenType.Registration
        )
        fragment.show(parentFragmentManager, KEY_COUNTRY)
    }

    private fun setPhone() {
        viewModel.authType = AuthType.Phone
        viewModel.checkContinueAvailability()
    }

    private fun initClicks() {
        binding.btnContinue.setThrottledClickListener { viewModel.continueClicked() }
        binding.btnCloseRegistration.setThrottledClickListener {
            context?.hideKeyboard(requireView())
            doDelayed(DELAY_KEYBOARD.toLong()) {
                navigationViewModel.goBack()
            }
        }
        binding.btnEmail.setThrottledClickListener {
            snackbarBlockedAlert?.dismiss()
            context?.hideKeyboard(requireView())
            doDelayed(DELAY_KEYBOARD.toLong()) {
                navigationViewModel.registrationEmailNext()
            }
        }
        binding.btnHelpRegistration.setThrottledClickListener {
            viewModel.helpClicked(AmplitudePropertyHelpPressedWhere.REGISTRATION)
            act.writeToTechSupport()
        }
        binding.tvRulesDescription.text = requireContext().getString(R.string.continue_if_agree).addClickableText(
            color = ContextCompat.getColor(requireContext(), R.color.uiKitColorForegroundLink),
            requireContext().getString(R.string.continue_if_agree_link_key)
        ) { NSupport.openLink(act, NOOMEERA_USER_AGREEMENT_URL) }
        binding.tvRulesDescription.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun blockedAlert(blockTime: Long) {
        viewModel.clearAll(false)
        val timeString = getInDurationFromSeconds(requireContext(), blockTime.toInt())
        snackbarBlockedAlert = UiKitSnackBar.make(
            requireView(),
            SnackBarParams(
                SnackBarContainerUiState(
                    messageText = requireContext().getString(R.string.auth_you_blocked_for_time, timeString),
                    avatarUiState = AvatarUiState.ErrorIconState
                ),
                paddingState = PaddingState(top = context.getStatusBarHeight() + PADDING_SNACK.dp),
                duration = TimeUnit.SECONDS.toMillis(SNACKBAR_DURATION).toInt()
            )
        ).apply {
            animationMode = ANIMATION_MODE_FADE
            val params = view.layoutParams as CoordinatorLayout.LayoutParams
            params.gravity = Gravity.TOP
            view.layoutParams = params
        }
        snackbarBlockedAlert?.show()
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
        UiKitSnackBar.make(
            requireView(), SnackBarParams(
                SnackBarContainerUiState(
                    messageText = message,
                    avatarUiState = AvatarUiState.ErrorIconState
                )
            )
        )
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
        binding.ipRegistrationPhone.isEnabled = !inProgress
        setContinueButtonAvailability(!inProgress)
    }

    private fun setContinueButtonAvailability(isAvailable: Boolean) {
        binding.btnContinue.isEnabled = isAvailable
    }

}
