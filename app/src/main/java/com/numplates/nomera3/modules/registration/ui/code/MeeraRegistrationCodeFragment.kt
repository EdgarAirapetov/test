package com.numplates.nomera3.modules.registration.ui.code

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.clearText
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.observeOnceButSkipNull
import com.meera.core.extensions.register
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.showKeyboard
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.visible
import com.meera.core.extensions.writeToTechSupport
import com.meera.core.utils.getInDurationFromSeconds
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.input.BottomTextState
import com.meera.uikit.widgets.snackbar.AvatarUiState
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.numplates.nomera3.BuildConfig
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentRegistrationCodeBinding
import com.numplates.nomera3.modules.appDialogs.ui.MeeraDialogNavigator
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyHelpPressedWhere
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseFragment
import com.numplates.nomera3.modules.redesign.fragments.main.SUBSCRIPTION_ROAD_REQUEST_KEY
import com.numplates.nomera3.modules.registration.AuthType
import com.numplates.nomera3.modules.registration.ui.RegistrationNavigationViewModel
import com.numplates.nomera3.modules.registration.ui.phoneemail.RegistrationPhoneEmailViewEvent
import com.numplates.nomera3.modules.registration.ui.phoneemail.RegistrationPhoneEmailViewModel
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

private const val INDEX_NOT_FOUND = -1
private const val SHAKE_DURATION = 1200L
private const val TEXT_SIZE_ANIMATION_DURATION = 400L
private const val MIN_CODE_TEXT_SIZE = 16F

class MeeraRegistrationCodeFragment : MeeraBaseFragment(R.layout.meera_fragment_registration_code) {

    private val binding by viewBinding(MeeraFragmentRegistrationCodeBinding::bind)

    private val verifyCodeViewModel by viewModels<RegistrationCodeViewModel>()
    private val sendCodeViewModel by viewModels<RegistrationPhoneEmailViewModel>()
    private val timerViewModel by viewModels<TimerViewModel>()
    private val navigationViewModel by viewModels<RegistrationNavigationViewModel>(
        ownerProducer = { requireParentFragment() }
    )

    private val smsRetrieverBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (SmsRetriever.SMS_RETRIEVED_ACTION == intent?.action) {
                val extras = intent.extras ?: return
                val status = extras.get(SmsRetriever.EXTRA_STATUS) as? Status? ?: return

                if (status.statusCode == CommonStatusCodes.SUCCESS) {
                    val message = extras.getString(SmsRetriever.EXTRA_SMS_MESSAGE) ?: return
                    val codeFirstIndex = message.indexOfFirst { it.isDigit() }
                    if (codeFirstIndex != INDEX_NOT_FOUND) {
                        val code = message.subSequence(codeFirstIndex, codeFirstIndex + 6)
                        binding.ukiRegistrationCode.etInput.setText(code)
                    }
                }
            }
        }
    }

    private val act by lazy { activity as MeeraAct }
    private var authType: AuthType = AuthType.Phone
    private var sendTo: String? = null
    private var countryCode: String? = null
    private var countryName: String? = null
    private var countryMask: String? = null
    private var argSmsTimeout: Long? = null
    private var isVerifying = false
    private var isCodeIncorrect = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        authType = arguments?.getParcelable(EXTRA_AUTH_TYPE) ?: AuthType.Phone
        sendTo = arguments?.getString(EXTRA_SEND_TO)
        countryCode = arguments?.getString(EXTRA_COUNTRY_CODE)
        countryName = arguments?.getString(EXTRA_COUNTRY_NAME)
        countryMask = arguments?.getString(EXTRA_COUNTRY_MASK)
        argSmsTimeout = arguments?.getLong(EXTRA_SMS_TIMEOUT, 0L)
        if (argSmsTimeout == 0L) {
            argSmsTimeout = null
        } else {
            argSmsTimeout = argSmsTimeout?.let { TimeUnit.SECONDS.toMillis(it) }
        }

        observe()
        observeViewEvent()
        initView()
        initCodeEditor()
        verifyCodeViewModel.start()
        sendCodeViewModel.setCountryNumber(countryName)
    }

    override fun onResume() {
        super.onResume()
        doDelayed(SHOW_KEYBOARD_DELAY) {
            binding.ukiRegistrationCode.etInput.requestFocus()
            binding.ukiRegistrationCode.etInput.showKeyboard()
        }
        smsRetrieverBroadcastReceiver.register(
            context = requireActivity(),
            filter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION),
            permission = SmsRetriever.SEND_PERMISSION
        )
        val client = SmsRetriever.getClient(requireActivity())
        client.startSmsRetriever()
    }

    override fun onPause() {
        super.onPause()
        act.unregisterReceiver(smsRetrieverBroadcastReceiver)
    }

    private fun observe() {
        verifyCodeViewModel.progressLiveData.observe(viewLifecycleOwner) { showProgress(it) }
        verifyCodeViewModel.liveData.observe(viewLifecycleOwner) { event ->
            when (event) {
                is RegistrationCodeViewState.Error.IncorrectCode -> showIncorrectCode(true)
                is RegistrationCodeViewState.Error.NetworkError -> showCodeNotReceived()
                is RegistrationCodeViewState.AuthenticationFailed -> binding.btnResendCode.visible()
                is RegistrationCodeViewState.ClearInputTextViewState -> binding.ukiRegistrationCode.etInput.clearText()
            }
        }
        sendCodeViewModel.progressLiveData.observe(viewLifecycleOwner) { showProgress(it) }
        sendCodeViewModel.liveData.observe(viewLifecycleOwner) { event ->
            when (event) {
                is RegistrationPhoneEmailViewEvent.Error.SendCodeFailed -> showCodeNotReceived()
                is RegistrationPhoneEmailViewEvent.SendCodeSuccess -> {
                    verifyCodeViewModel.incSendCodeTimes()
                    binding.ukiRegistrationCode.etInput.clearText()
                    val time = (event.timeout ?: event.blockTime)?.let { TimeUnit.SECONDS.toMillis(it) }
                    timerViewModel.startTimer(time)
                }

                else -> Unit
            }
        }
        timerViewModel.liveData.observe(viewLifecycleOwner) { event ->
            when (event) {
                is TimerViewEvent.Time -> showCountDownTimer(event.time)
                is TimerViewEvent.TimerFinished -> showDidNotGetCodeMessage()
            }
        }
        timerViewModel.startTimer(argSmsTimeout)
    }

    private fun initView() {
        binding.ukiRegistrationCode.etInput.filters = arrayOf(InputFilter.LengthFilter(6))
        binding.ukiRegistrationCode.etInput.gravity = Gravity.CENTER
        when (authType) {
            is AuthType.Email -> {
                binding.tvSendToCountryCode.doAfterTextChanged {
                    binding.tvSendToCountryCode.post {
                        if (binding.tvSendToCountryCode.lineCount > 1) {
                            binding.tvSendToCountryCode.textSize = MIN_CODE_TEXT_SIZE
                        }
                    }
                }
                binding.tvCodeSentDescription.text = getString(R.string.code_sent_to_email)
                binding.tvSendToCountryCode.text = sendTo
                binding.etCodeSendTo.gone()
            }

            is AuthType.Phone -> {
                binding.tvCodeSentDescription.text = getString(R.string.code_sent_to_number)
                binding.tvSendToCountryCode.text = countryCode ?: DEFAULT_COUNTRY_CODE
                binding.etCodeSendTo.mask =
                    countryMask?.replace(SERVER_MASK_CHAR, DEFAULT_MASK_CHAR) ?: DEFAULT_COUNTRY_MASK
                binding.etCodeSendTo.setText(sendTo?.removePrefix(countryCode ?: DEFAULT_COUNTRY_CODE))
                binding.etCodeSendTo.visible()
            }
        }
        binding.tvSendToCountryCode.visible()
        binding.tvCodeSentDescription.visible()
        binding.nvRegistrationCode.backButtonClickListener = {
            navigationViewModel.goBack()
        }
        binding.btnResendCode.setThrottledClickListener {
            val address = sendTo
            shakeTextView(binding.tvSendToCountryCode)
            shakeTextView(binding.etCodeSendTo)
            if (address != null) sendCodeViewModel.resendCode(authType, address)
        }
        binding.btnHelpRegistration.setThrottledClickListener {
            sendCodeViewModel.helpClicked(AmplitudePropertyHelpPressedWhere.CODE_ENTER)
            act.writeToTechSupport()
        }
    }

    private fun initCodeEditor() {
        binding.ukiRegistrationCode.etInput.inputType = InputType.TYPE_CLASS_NUMBER
        binding.ukiRegistrationCode.doAfterSearchTextChanged { text ->
            showIncorrectCode(false)
            isVerifying = if (
                text.isNotEmpty() &&
                text.length == RegistrationCodeViewModel.CODE_LENGTH &&
                !isVerifying
            ) {
                verifyCodeViewModel.setCode(authType, sendTo, text, countryName)
                true
            } else {
                false
            }
        }
        binding.ukiRegistrationCode.etInput.setThrottledClickListener {
            binding.ukiRegistrationCode.etInput.clearText()
            binding.tvResendCodeTime.gone()
        }
        if (BuildConfig.DEBUG && sendTo?.endsWith(PREFIX_MAIL) == true) {
            doDelayed(DEFAULT_CODE_INSERT_DELAY) {
                binding.ukiRegistrationCode.etInput.setText(CORRECT_CODE)
            }
        }
    }

    private fun showCountDownTimer(time: Long) {
        val timeStr = getInDurationFromSeconds(requireContext(), time.toInt())
        binding.tvResendCodeTime.text = getString(R.string.meera_auth_you_can_resend_after, timeStr)
        binding.tvResendCodeTime.visible()
        binding.btnResendCode.gone()
    }

    private fun shakeTextView(view: TextView?) {
        animateSize(view)
        Handler(Looper.getMainLooper()).postDelayed({
            ObjectAnimator
                .ofFloat(view, "translationX", 0f, 20f, -20f, 20f, -20f, 0f)
                .setDuration(SHAKE_DURATION)
                .start()
        }, TEXT_SIZE_ANIMATION_DURATION)
    }

    private fun animateSize(view: TextView?) {
        val startSize = 22f // Size in pixels
        val endSize = 26f

        if (view?.textSize != endSize.dp) {
            val animator = ValueAnimator.ofFloat(startSize, endSize)
            animator.duration = TEXT_SIZE_ANIMATION_DURATION
            animator.addUpdateListener { valueAnimator ->
                val animatedValue = valueAnimator.animatedValue as? Float
                if (animatedValue != null) {
                    view?.textSize = animatedValue
                    val typeface = ResourcesCompat.getFont(requireContext(), R.font.nmrmedia_source_sans_pro_semibold)
                    view?.typeface = typeface
                }
            }
            animator.start()
        }
    }

    private fun clearResendTimer() {
        timerViewModel.clearTimer()
    }

    private fun showCodeNotReceived() {
        clearResendTimer()
        showDidNotGetCodeMessage()
    }

    private fun showDidNotGetCodeMessage() {
        binding.tvResendCodeTime.gone()
        binding.btnResendCode.visible()
    }

    private fun showIncorrectCode(isIncorrect: Boolean) {
        isCodeIncorrect = isIncorrect
        binding.ukiRegistrationCode.setBottomTextState(
            if (isIncorrect) BottomTextState.Error(
                errorText = getString(R.string.incorrect_code),
                centerError = true
            ) else BottomTextState.Empty
        )
    }

    private fun showProgress(inProgress: Boolean) {
        isVerifying = inProgress
        binding.ukiRegistrationCode.etInput.isEnabled = !inProgress
    }

    private fun showError(message: String?) {
        message?.let {
            UiKitSnackBar.make(
                requireView(), SnackBarParams(
                    SnackBarContainerUiState(
                        messageText = message,
                        avatarUiState = AvatarUiState.ErrorIconState
                    )
                )
            ).show()
        }
    }

    private fun authenticationSuccess() {
        act.supportFragmentManager.setFragmentResult(SUBSCRIPTION_ROAD_REQUEST_KEY, bundleOf())

        verifyCodeViewModel.subscribePush()
        verifyCodeViewModel.saveLastSmsCodeTime()
        act.connectSocket()
        verifyCodeViewModel.getUserProfileLive()
            .observeOnceButSkipNull(viewLifecycleOwner) { profile ->
                if (profile.profileDeleted.toBoolean()) {
                    verifyCodeViewModel.logLoginFinished()
                    if (verifyCodeViewModel.isWorthToShowCallEnableFragment()) {
                        MeeraDialogNavigator(act).showCallsEnableDialog { act.getHolidayInfo(true) }
                    } else {
                        verifyCodeViewModel.onAuthFinished()
                    }
                    act.getMeeraAuthenticationNavigator().completeOnSmsScreen()
                    navigationViewModel.registrationDeleteProfileNext(countryName)
                } else {
                    handleProfile(profile.isProfileFilled)
                    if (profile.isProfileFilled) {
                        act.getHolidayInfo(true)
                    }
                }
            }
    }

    private fun handleProfile(isProfileFilled: Boolean) {
        if (isProfileFilled) {
            verifyCodeViewModel.logLoginFinished()
            if (verifyCodeViewModel.isWorthToShowCallEnableFragment()) {
                MeeraDialogNavigator(act).showCallsEnableDialog { act.getHolidayInfo() }
            } else {
                verifyCodeViewModel.onAuthFinished()
            }
            act.getMeeraAuthenticationNavigator().completeOnSmsScreen()
            findNavController().popBackStack()
        } else {
            verifyCodeViewModel.setIsNeedShowHoliday(false)
            act.getMeeraAuthenticationNavigator().navigateToPersonalInfo(countryName)
        }
    }

    private fun observeViewEvent() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    verifyCodeViewModel.regCodeViewEvent.collect(::handleEvent)
                }
            }
        }
    }

    private fun handleEvent(typeEvent: RegistrationCodeViewEvent) {
        when (typeEvent) {
            RegistrationCodeViewEvent.AuthenticationSuccess -> authenticationSuccess()
            is RegistrationCodeViewEvent.ShowErrorSnackEvent -> showError(typeEvent.message)
        }
    }

    companion object {
        private const val DEFAULT_CODE_INSERT_DELAY = 100L
        private const val PREFIX_MAIL = "@testmail.test"
        private const val CORRECT_CODE = "111111"
        private const val SHOW_KEYBOARD_DELAY = 300L
        private const val EXTRA_AUTH_TYPE = "EXTRA_AUTH_TYPE"
        private const val EXTRA_SEND_TO = "EXTRA_SEND_TO"
        private const val EXTRA_COUNTRY_CODE = "EXTRA_COUNTRY_CODE"
        private const val EXTRA_COUNTRY_NAME = "EXTRA_COUNTRY_NAME"
        private const val EXTRA_COUNTRY_MASK = "EXTRA_COUNTRY_MASK"
        private const val EXTRA_SMS_TIMEOUT = "EXTRA_SMS_TIMEOUT"

        private const val DEFAULT_COUNTRY_CODE = "+7"
        private const val DEFAULT_COUNTRY_MASK = "### ###-##-##"

        fun newInstance(
            authType: AuthType,
            sendTo: String,
            countryCode: String?,
            countryName: String?,
            countryMask: String?,
            smsTimeout: Long?
        ) = MeeraRegistrationCodeFragment().apply {
            arguments = Bundle().apply {
                putParcelable(EXTRA_AUTH_TYPE, authType)
                putString(EXTRA_SEND_TO, sendTo)
                putString(EXTRA_COUNTRY_CODE, countryCode)
                putString(EXTRA_COUNTRY_NAME, countryName)
                putString(EXTRA_COUNTRY_MASK, countryMask)
                smsTimeout?.let { putLong(EXTRA_SMS_TIMEOUT, smsTimeout) }
            }
        }
    }
}
