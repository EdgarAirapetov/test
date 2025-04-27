package com.numplates.nomera3.modules.onboarding

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.viewModels
import com.meera.core.extensions.click
import com.meera.core.extensions.dp
import androidx.lifecycle.lifecycleScope
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentBottomDialogOnboardingBinding
import com.numplates.nomera3.databinding.ViewOnboardingStepBinding
import com.numplates.nomera3.modules.appDialogs.ui.DialogNavigator
import com.numplates.nomera3.modules.auth.util.AuthStatusObserver
import com.numplates.nomera3.modules.registration.AuthType
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.extensions.addOnPageChangeListener
import com.meera.core.extensions.getScreenHeight
import com.meera.core.extensions.gone
import com.meera.core.extensions.setPaddingBottom
import com.meera.core.extensions.setBackgroundTint
import com.meera.core.extensions.setTextStyle
import com.meera.core.extensions.visible
import com.numplates.nomera3.App
import com.numplates.nomera3.modules.newroads.ui.entity.MainRoadMode

private const val BOTTOM_SHEET_HEIGHT = 642

class OnboardingFragment : BaseFragmentNew<FragmentBottomDialogOnboardingBinding>(),
    OnBoardingViewControllerButtonsListener {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentBottomDialogOnboardingBinding
        get() = FragmentBottomDialogOnboardingBinding::inflate

    private var dialogNavigator: DialogNavigator? = null

    private val onboardingViewModel by viewModels<OnboardingViewModel>(
        ownerProducer = { this },
        factoryProducer = { App.component.getViewModelFactory() }
    )

    private var contentAdapter: ContentAdapter? = null
    private var steps = listOf<OnboardingStep>()
    private var currentStep = 0
    private val isThereOnlyOneStep: Boolean
        get() = steps.size == 1
    private var previousPagePosition = -1

    /**
     * Флаги для аналитики
     * */
    private var action = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        observe()
        addKeyboardListener()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        contentAdapter = null
    }

    override fun onCloseBtnClicked() {
        if (steps.isEmpty() || currentStep >= steps.size) return
        onboardingViewModel.onCloseBtnClicked(steps[currentStep])
        if (isThereOnlyOneStep) {
            (parentFragment as? OnBoardingFinishListener)?.onBoardingFinished()
        }
    }

    override fun onContinueBtnClicked() = onboardingViewModel.onContinueClicked()

    override fun onDownSwiped() {
        if (steps.isEmpty() || currentStep >= steps.size) return
        onboardingViewModel.onDownSwiped(steps[currentStep])
        if (isThereOnlyOneStep) {
            (parentFragment as? OnBoardingFinishListener)?.onBoardingFinished()
        }
    }

    private fun initView() {
        dialogNavigator = DialogNavigator(requireActivity() as Act)
        binding?.btnAction?.click { nextStep() }
        onboardingViewModel.doAction(OnboardingViewAction.Setup)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun observe() {
        onboardingViewModel.stateLiveData.observe(viewLifecycleOwner) { event ->
            when (event) {
                is OnboardingViewState.Setup -> {
                    steps = event.totalSteps
                    setSlidesIndicator(0)
                    setActionButton(event.currentStep)
                    removeFragments()
                    contentAdapter = ContentAdapter()
                    binding?.vpContent?.adapter = null
                    binding?.vpContent?.adapter = contentAdapter
                    binding?.vpContent?.setOnTouchListener { _, event ->
                        action = event.action
                        return@setOnTouchListener false
                    }
                    binding?.vpContent?.addOnPageChangeListener(
                        onPageSelected = { step ->
                            setSlidesIndicator(step)
                            setActionButton(steps[step])
                            stopPreviousFragmentLottieAnim()
                            startPagerFragmentLottieAnim(step)
                            onboardingViewModel.setCurrentStep(steps[step])
                            previousPagePosition = step
                            onboardingViewModel.onSwiped(
                                step, if (steps.size > 1) {
                                    steps[step - 1]
                                } else {
                                    steps[step]
                                }
                            )
                        }
                    )
                    checkLastScreen()
                }
                else -> {}
            }
        }
        initAuthStatusObserver()
        observeViewEvent()
    }

    private fun observeViewEvent() {
        lifecycleScope.launchWhenStarted {
            onboardingViewModel.onBoardingViewEvent.collect { typeEvent ->
                handleViewEvent(typeEvent)
            }
        }
    }

    private fun handleViewEvent(event: OnboardingViewEvent) {
        when (event) {
            is OnboardingViewEvent.SendCodeSuccess -> {
                act?.getAuthenticationNavigator()
                    ?.openCodeInput(
                        authType = AuthType.Phone,
                        sendTo = event.sendTo,
                        countryCode = event.currentCountry?.code,
                        countryName = event.currentCountry?.name,
                        countryMask = event.currentCountry?.mask,
                        smsTimeout = event.timeout
                    )
            }
            is OnboardingViewEvent.AuthFinished -> {
                (parentFragment as OnBoardingFinishListener).onBoardingFinished()
            }
            is OnboardingViewEvent.StartRegistration -> {
                act?.getAuthenticationNavigator()?.navigateToPhone()
            }
            else -> {}
        }
    }

    private fun removeFragments() {
        for (fragment in childFragmentManager.fragments) {
            childFragmentManager.beginTransaction().remove(fragment).commitNowAllowingStateLoss()
        }
    }

    private fun initAuthStatusObserver() {
        object : AuthStatusObserver(act, this) {
            override fun onAuthState() {
                val isNeedToShowWelcomeScreen =
                    arguments?.getBoolean(KEY_NEED_SHOW_LAST_STEP) ?: false
                onboardingViewModel.registrationCompleted(isNeedToShowWelcomeScreen)
            }
            override fun onNotAuthState() {
                onboardingViewModel.closeOnboardingIfShowed()
            }
        }
    }

    private fun setSlidesIndicator(position: Int) {
        binding?.llSteps?.removeAllViews()
        if (steps.size > 1) {
            steps.forEachIndexed { index, _ ->
                val view =
                    ViewOnboardingStepBinding.inflate(LayoutInflater.from(requireContext())).root
                if (index == position) {
                    view.isSelected = true
                    currentStep = index
                }
                binding?.llSteps?.addView(view)
            }
        } else {
            currentStep = 0
        }
    }

    private fun setActionButton(step: OnboardingStep) {
        when (step) {
            OnboardingStep.STEP_JOIN -> {
                binding?.btnAction?.text = getString(R.string.onboarding_join_later)
                binding?.btnAction?.visible()
                binding?.llSteps?.visible()
                setActionButtonWhite()
            }
            OnboardingStep.STEP_EVENTS -> {
                binding?.btnAction?.text = getString(R.string.onboarding_else)
                binding?.btnAction?.visible()
                binding?.llSteps?.visible()
                setActionButtonPurple()
            }
            OnboardingStep.STEP_PEOPLE_NEAR -> {
                binding?.btnAction?.text = getString(R.string.onboarding_next)
                binding?.btnAction?.visible()
                binding?.llSteps?.visible()
                setActionButtonPurple()
            }
            OnboardingStep.STEP_CONNECT -> {
                binding?.btnAction?.text = getString(R.string.onboarding_good)
                binding?.btnAction?.visible()
                binding?.llSteps?.visible()
                setActionButtonPurple()
            }
            OnboardingStep.STEP_WELCOME -> {
                binding?.btnAction?.text = getString(R.string.onboarding_thank_you)
                binding?.btnAction?.visible()
                binding?.llSteps?.visible()
                setActionButtonPurple()
            }
            OnboardingStep.STEP_JOIN_PHONE -> {
                binding?.btnAction?.gone()
                binding?.llSteps?.gone()
                setActionButtonPurple()
            }
        }
    }

    private fun startPagerFragmentLottieAnim(pagePosition: Int) {
        val currentPageFragment = getPagerFragmentByPosition(pagePosition)
        currentPageFragment?.let { fragment ->
            if (fragment is OnBoardingAnimationListener) {
                fragment.showLottieAnimation()
            }
        }
    }

    private fun stopPreviousFragmentLottieAnim() {
        if (previousPagePosition == -1) return
        getPagerFragmentByPosition(previousPagePosition)?.let { fragment ->
            if (fragment is OnBoardingAnimationListener) {
                fragment.stopLottieAnimation()
            }
        }
    }

    private fun getPagerFragmentByPosition(position: Int): Fragment? {
        return childFragmentManager.findFragmentByTag(
            ("android:switcher:${R.id.vpContent}:$position")
        )
    }

    private fun addKeyboardListener() {
        var isKeyboardVisible = false
        ViewCompat.setOnApplyWindowInsetsListener(requireView()) { v, insets ->
            val isNewKeyboardVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            if (isKeyboardVisible == isNewKeyboardVisible) return@setOnApplyWindowInsetsListener insets
            isKeyboardVisible = isNewKeyboardVisible
            if (isNewKeyboardVisible) {
                val pad =
                    insets.getInsets(WindowInsetsCompat.Type.ime()).bottom - (getScreenHeight() - BOTTOM_SHEET_HEIGHT.dp)
                binding?.root?.setPaddingBottom(pad)
                binding?.svContent?.fullScroll(View.FOCUS_DOWN)
            } else {
                binding?.root?.setPaddingBottom(0)
            }
            return@setOnApplyWindowInsetsListener insets
        }
    }

    private fun setActionButtonWhite() {
        binding?.btnAction?.background = null
        binding?.btnAction?.setTextStyle(R.style.BlackRegular16)
    }

    private fun setActionButtonPurple() {
        binding?.btnAction?.background = ContextCompat.getDrawable(
            requireContext(),
            R.drawable.background_white_rounded_6dp
        )
        binding?.btnAction?.setBackgroundTint(R.color.ui_purple)
        binding?.btnAction?.setTextStyle(R.style.WhiteSemiBold16)
    }

    private fun nextStep() {
        if (steps.isEmpty()) return
        if (currentStep > steps.size) return
        onboardingViewModel.onNextStepClicked(steps[currentStep])
        currentStep++
        if (currentStep == steps.size) {
            (parentFragment as OnBoardingFinishListener).onBoardingFinished()
        } else {
            binding?.vpContent?.currentItem = currentStep
        }
    }

    private fun checkLastScreen() {
        setSlidesIndicator(steps.indexOf(onboardingViewModel.getLastStep()))
        setActionButton(onboardingViewModel.getLastStep())
        binding?.vpContent?.currentItem = steps.indexOf(onboardingViewModel.getLastStep())
        val finishListener = (parentFragment as? OnBoardingFinishListener) ?: return
        if (steps[currentStep] == OnboardingStep.STEP_WELCOME && finishListener.getRoadMode() != MainRoadMode.MAP) {
            finishListener.onRegistrationFinished()
        }
    }

    private inner class ContentAdapter : FragmentPagerAdapter(
        childFragmentManager,
        BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
    ) {

        override fun getItemId(position: Int): Long {
            return steps[position].ordinal.toLong()
        }

        override fun getCount(): Int {
            return steps.size
        }

        override fun getItem(position: Int): Fragment {
            return OnboardingContentFragment.newInstance(
                steps[position],
                arguments?.getBoolean(KEY_NEED_SHOW_LAST_STEP) ?: false
            )
        }
    }

    companion object {
        private const val KEY_NEED_SHOW_LAST_STEP = "need show last step"
        fun getInstance(isNeedToShowLastStep: Boolean) = OnboardingFragment().apply {
            arguments = Bundle().apply {
                putBoolean(KEY_NEED_SHOW_LAST_STEP, isNeedToShowLastStep)
            }
        }
    }
}
