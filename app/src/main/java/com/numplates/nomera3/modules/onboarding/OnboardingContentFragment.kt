package com.numplates.nomera3.modules.onboarding

import android.animation.Animator
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.meera.core.extensions.addClickableText
import com.meera.core.extensions.checkCorrectSelection
import com.meera.core.extensions.click
import com.meera.core.extensions.getColorCompat
import com.meera.core.extensions.gone
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.setBackgroundTint
import com.meera.core.extensions.setTextStyle
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.core.extensions.writeToTechSupport
import com.numplates.nomera3.App
import com.numplates.nomera3.NOOMEERA_USER_AGREEMENT_URL
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentOnboardingContentBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyHelpPressedWhere
import com.numplates.nomera3.modules.registration.domain.model.RegistrationCountryModel
import com.numplates.nomera3.modules.registration.ui.code.DEFAULT_MASK_CHAR
import com.numplates.nomera3.modules.registration.ui.code.SERVER_MASK_CHAR
import com.numplates.nomera3.modules.registration.ui.country.fragment.KEY_COUNTRY
import com.numplates.nomera3.modules.registration.ui.country.fragment.RegistrationCountryFragment
import com.numplates.nomera3.modules.registration.ui.country.fragment.RegistrationCountryFromScreenType
import com.numplates.nomera3.modules.registration.ui.phoneemail.HeaderDialogType
import com.numplates.nomera3.modules.user.ui.dialog.BlockUserByAdminDialogFragment
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.view.utils.NSupport
import io.reactivex.disposables.CompositeDisposable


class OnboardingContentFragment : BaseFragmentNew<FragmentOnboardingContentBinding>(),
    OnBoardingAnimationListener {

    private var isAnimationCalled = false
    private var onBoardingStep: OnboardingStep? = null
    private var isAnimationFromPagerScroll = false

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentOnboardingContentBinding
        get() = FragmentOnboardingContentBinding::inflate

    private val onboardingViewModel by viewModels<OnboardingViewModel>(
        ownerProducer = { requireParentFragment() },
        factoryProducer = { App.component.getViewModelFactory() }
    )

    private val disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(KEY_COUNTRY) { _, bundle ->
            val chosenCountry =
                bundle.getParcelable<RegistrationCountryModel>(KEY_COUNTRY) ?: return@setFragmentResultListener
            onboardingViewModel.newCountryChosen(chosenCountry)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onBoardingStep = arguments?.getSerializable(EXTRA_STEP) as? OnboardingStep
        initView(onBoardingStep)
        observe()
    }

    override fun onStart() {
        super.onStart()
        if (arguments?.getBoolean(KEY_SHOW_ONLY_WELCOME_STEP, false) == true){
            act?.showFireworkAnimation {  }
        }
    }
    override fun onResume() {
        super.onResume()
        if (onBoardingStep == OnboardingStep.STEP_WELCOME || isAnimationFromPagerScroll) return
        handleStartOrStopAnim()
    }
    override fun onPause() {
        super.onPause()
        context?.hideKeyboard(requireView())
        handleStartOrStopAnim()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposable.clear()
    }

    override fun showLottieAnimation() {
        isAnimationFromPagerScroll = true
        startLottieAnimation()
    }

    override fun stopLottieAnimation() {
        isAnimationCalled = true
        isAnimationFromPagerScroll = true
        handleStartOrStopAnim()
    }

    private fun startLottieAnimation() {
        isAnimationCalled = true
        binding?.lavContent?.playAnimation()
        val isShowOnlyWelcomeStep = arguments?.getBoolean(
            KEY_SHOW_ONLY_WELCOME_STEP,
            false
        ) ?: false
        if (!isShowOnlyWelcomeStep && onBoardingStep == OnboardingStep.STEP_WELCOME) {
            act?.showFireworkAnimation()
        }
    }

    private fun handleStartOrStopAnim() {
        if (isAnimationCalled) {
            binding?.lavContent?.progress = 0f
            binding?.lavContent?.pauseAnimation()
            return
        }
        startLottieAnimation()
    }

    private fun observe() {
        onboardingViewModel.isRegistrationCompleted.observe(viewLifecycleOwner) {
            if (onBoardingStep == OnboardingStep.STEP_WELCOME) handleStartOrStopAnim()
        }
        onboardingViewModel.stateLiveData.observe(viewLifecycleOwner) { event ->
            when (event) {
                is OnboardingViewState.SetContinueButtonAvailable -> {
                    setLoginButtonEnabled(event.available)
                }
                is OnboardingViewState.CurrentCountry -> {
                    val country = event.country
                    binding?.apply {
                        ivCountryIcon.loadGlide(country.flag)
                        tvCountryCode.text = country.code
                        etInputPhone.mask = country.mask?.replace(SERVER_MASK_CHAR, DEFAULT_MASK_CHAR)
                    }
                }
                else -> Unit
            }
        }
        observeEvents()
    }

    /**
     * A problem which this method solve is paste full phone from cash.
     * */
    private fun checkEditTextSelection() = binding?.etInputPhone?.checkCorrectSelection()

    private fun setLoginButtonEnabled(isEnabled: Boolean) {
        binding?.btnLoginRegister?.isEnabled = isEnabled
        if (isEnabled) {
            setLoginButtonPurple()
            checkEditTextSelection()
        } else setLoginButtonTale()
    }

    private fun setLoginButtonPurple() {
        binding?.btnLoginRegister?.setBackgroundTint(R.color.ui_purple)
        binding?.btnLoginRegister?.setTextStyle(R.style.WhiteSemiBold16)
    }

    private fun setLoginButtonTale() {
        binding?.btnLoginRegister?.setBackgroundTint(R.color.tale_white)
        binding?.btnLoginRegister?.setTextStyle(R.style.PurpleRegular16)
    }

    private fun initView(step: OnboardingStep?) {
        when (step) {
            OnboardingStep.STEP_JOIN -> initStepJoinStart()
            OnboardingStep.STEP_EVENTS -> initStepEvents()
            OnboardingStep.STEP_CONNECT -> initStepConnect()
            OnboardingStep.STEP_PEOPLE_NEAR -> initStepPeopleNear()
            OnboardingStep.STEP_JOIN_PHONE -> initStepPhone()
            OnboardingStep.STEP_WELCOME -> initStepWelcome()
            else -> return
        }
        initLottieAnimationListener()
    }

    private fun initLottieAnimationListener() {
        binding?.lavContent?.addAnimatorListener(object: Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) = Unit
            override fun onAnimationEnd(animation: Animator) {
                isAnimationFromPagerScroll = false
            }
            override fun onAnimationCancel(animation: Animator) = Unit
            override fun onAnimationRepeat(animation: Animator) = Unit
        })
    }

    private fun initStepJoinStart() {
        binding?.tvTitle?.text = getString(R.string.onboarding_join_title)
        binding?.tvDescription?.text = getString(R.string.onboarding_join_description)
        setLoginButtonEnabled(true)
        binding?.btnOpenRegistration?.visible()
        binding?.btnLoginRegister?.gone()
        binding?.btnOpenRegistration?.text = getString(R.string.onboarding_registration)
        binding?.btnOpenRegistration?.click {
            onboardingViewModel.doAction(OnboardingViewAction.StartRegistration)
        }
        binding?.lavContent?.setAnimation(OnBoardingIllustrations.JOIN.value)
    }

    private fun initStepEvents() {
        binding?.tvTitle?.text = getString(R.string.onboarding_events_title)
        binding?.tvDescription?.text = getString(R.string.onboarding_events_description)

        binding?.lavContent?.setAnimation(OnBoardingIllustrations.FEED.value)
    }

    private fun initStepPeopleNear() {
        binding?.tvTitle?.text = getString(R.string.onboarding_people_near_title)
        binding?.tvDescription?.text = getString(R.string.onboarding_people_near_description)

        binding?.lavContent?.setAnimation(OnBoardingIllustrations.MAP.value)
    }

    private fun initStepConnect() {
        binding?.tvTitle?.text = getString(R.string.onboarding_connect_title)
        binding?.tvDescription?.text = getString(R.string.onboarding_discuss_description)

        binding?.lavContent?.setAnimation(OnBoardingIllustrations.CONVERSATION.value)
    }

    private fun initStepPhone() {
        binding?.vgCountryFlag?.setThrottledClickListener {
            navigateToCountries()
        }
        binding?.ivArrowDown?.setThrottledClickListener {
            navigateToCountries()
        }
        binding?.tvTitle?.text = getString(R.string.onboarding_join_title)
        binding?.tvDescription?.text = getString(R.string.onboarding_phone_description)
        binding?.vgPhoneCard?.visible()
        binding?.etInputPhone?.debounceInput()
        setLoginButtonEnabled(false)
        binding?.btnOpenRegistration?.gone()
        binding?.btnLoginRegister?.text = getString(R.string.btn_continue)
        binding?.btnLoginRegister?.visible()
        binding?.btnLoginRegister?.click {
            context?.hideKeyboard(requireView())
            onboardingViewModel.onEnterClicked()
        }
        binding?.tvOnBoardingRulesDescription?.visible()
        binding?.tvOnBoardingRulesDescription?.movementMethod = LinkMovementMethod.getInstance()
        binding?.tvOnBoardingRulesDescription?.text = requireContext().getString(R.string.continue_if_agree).addClickableText(
                color = requireContext().getColorCompat(R.color.ui_purple),
                requireContext().getString(R.string.continue_if_agree_link_key)
        ) { NSupport.openLink(act, NOOMEERA_USER_AGREEMENT_URL) }
        binding?.lavContent?.setAnimation(OnBoardingIllustrations.JOIN.value)
    }

    private fun navigateToCountries() {
        val fragment = RegistrationCountryFragment.newInstance(
            RegistrationCountryFromScreenType.Registration
        )
        fragment.show(parentFragmentManager, KEY_COUNTRY)
    }

    private fun initStepWelcome() {
        binding?.tvTitle?.text = getString(R.string.onboarding_welcome_title)
        binding?.tvDescription?.text = getString(R.string.onboarding_welcome_description)
        binding?.btnOpenRegistration?.gone()
        binding?.lavContent?.setAnimation(OnBoardingIllustrations.WELCOME.value)
        onboardingViewModel.welcomeStepShowed()
    }

    private fun EditText.debounceInput() {
        disposable.clear()
        doAfterTextChanged {
            val text = binding?.tvCountryCode?.text.toString() + binding?.etInputPhone?.rawText?.trim()
            onboardingViewModel.setPhone(text.trim())
        }
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
                onboardingViewModel.helpClicked(AmplitudePropertyHelpPressedWhere.REGISTRATION)
                act?.writeToTechSupport()
            }
            headerDialogType = HeaderDialogType.BlockedProfileType
            show(this@OnboardingContentFragment.childFragmentManager, tag)
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            onboardingViewModel.onBoardingViewEvent.collect { typeEvent ->
                handleViewEvent(typeEvent)
            }
        }
    }

    private fun handleViewEvent(typeEvent: OnboardingViewEvent) {
        when (typeEvent) {
            is OnboardingViewEvent.Error.UserBlocked -> {
                showUserIsBlockedDialog(
                    reason = typeEvent.reason ?: REASON_EMPTY,
                    expiredAt = typeEvent.blockExpired ?: BLOCK_EXPIRED_DEFAULT
                )
            }
            else -> {}
        }
    }

    companion object {
        private const val REASON_EMPTY = ""
        private const val BLOCK_EXPIRED_DEFAULT = 0L

        private const val EXTRA_STEP = "extra step"
        /**
         * Флаг передается для случаев если онбординг не был пройден до конца и был закрыт.
         * В таком случае есть необходимость показать экран приветствия после регистрации нового пользователя
         * */
        private const val KEY_SHOW_ONLY_WELCOME_STEP = "show only welcome step"

        fun newInstance(step: OnboardingStep, isNeedToShowWelcomeStep: Boolean) = OnboardingContentFragment().apply {
            arguments = Bundle().apply {
                putSerializable(EXTRA_STEP, step)
                putBoolean(KEY_SHOW_ONLY_WELCOME_STEP, isNeedToShowWelcomeStep)
            }
        }

    }
}
