package com.numplates.nomera3.modules.registration.ui

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.observeOnceButSkipNull
import com.meera.core.utils.KeyboardHeightProvider
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentRegistrationContainerBinding
import com.numplates.nomera3.modules.auth.ui.AuthViewModel
import com.numplates.nomera3.modules.avatar.MeeraContainerAvatarBaseFragment
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyInputType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyRegistrationStep
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.modules.redesign.fragments.main.SUBSCRIPTION_ROAD_REQUEST_KEY
import com.numplates.nomera3.modules.registration.AuthType
import com.numplates.nomera3.modules.registration.ui.avatar.MeeraRegistrationAvatarFragment
import com.numplates.nomera3.modules.registration.ui.birthday.MeeraRegistrationBirthdayFragment
import com.numplates.nomera3.modules.registration.ui.code.MeeraRegistrationCodeFragment
import com.numplates.nomera3.modules.registration.ui.email.MeeraRegistrationEmailFragment
import com.numplates.nomera3.modules.registration.ui.gender.MeeraRegistrationGenderFragment
import com.numplates.nomera3.modules.registration.ui.location.MeeraRegistrationLocationFragment
import com.numplates.nomera3.modules.registration.ui.name.MeeraRegistrationNameFragment
import com.numplates.nomera3.modules.registration.ui.phoneemail.MeeraRegistrationPhoneFragment
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_AVATAR_STATE
import com.numplates.nomera3.presentation.view.fragments.MeeraRecoveryProfileFragment
import com.numplates.nomera3.presentation.view.fragments.MeeraRottenProfileFragment
import timber.log.Timber

const val EXTRA_COUNTRY_NAME = "EXTRA_COUNTRY_NAME"

class MeeraRegistrationContainerFragment :
    MeeraBaseDialogFragment(
        layout = R.layout.meera_fragment_registration_container,
        behaviourConfigState = ScreenBehaviourState.Authorizes
    ), IArgContainer {

    private val binding by viewBinding(MeeraFragmentRegistrationContainerBinding::bind)
    private val act by lazy { activity as MeeraAct }

    private var isClosing = false
    private var isFirstStart = true

    /**
     * requireParentFragment() не подходит, потому что этот фрагмент приавязан к активити
     */
    private val authViewModel by viewModels<AuthViewModel>(
        ownerProducer = { requireActivity() }
    )

    private val navigationViewModel by viewModels<RegistrationNavigationViewModel>(
        ownerProducer = { this }
    )

    override val containerId: Int
        get() = R.id.fragment_first_container_view

    private var authType: AuthType? = null
    private var countryNumber: String? = null

    private var keyboardHeightProvider: KeyboardHeightProvider? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initKeyboardListener()
        observeNavigationViewModel()
        initStatusBar()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (isBackStackContainsMoreThanOne() && !isClosing) {
                childFragmentManager.popBackStack()
            } else {
                findNavController().popBackStack()
                closeRegistration()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        isClosing = false
        if (isFirstStart) {
            isFirstStart = false
            act.getMeeraAuthenticationNavigator().setMeeraRegContainerFragment(this)
            authViewModel.navigateToPhone()
        }
    }

    fun startRegistration(countryName: String?) {
        navigationViewModel.isBackFromNameAvailable = isBackStackContainsMoreThanOne()
        childFragmentManager.popBackStack()
        showFragment(MeeraRegistrationNameFragment().apply { arguments = bundleOf(ARG_COUNTRY_NUMBER to countryName) })
        authViewModel.registrationStep = AmplitudePropertyRegistrationStep.REG_3
    }

    fun openPhoneMailInput() {
        showFragment(MeeraRegistrationPhoneFragment())
        authViewModel.registrationStep = AmplitudePropertyRegistrationStep.REG_1
    }

    fun openCodeInput(
        authType: AuthType,
        sendTo: String,
        countryCode: String?,
        countryName: String?,
        countryMask: String?,
        smsTimeout: Long?
    ) {
        showFragment(
            MeeraRegistrationCodeFragment.newInstance(
                authType,
                sendTo,
                countryCode,
                countryName,
                countryMask,
                smsTimeout
            )
        )
        authViewModel.registrationStep = AmplitudePropertyRegistrationStep.REG_2
    }

    private fun isBackStackContainsMoreThanOne(): Boolean {
        return childFragmentManager.backStackEntryCount > 1
    }

    private fun observeNavigationViewModel() {
        navigationViewModel.eventLiveData.observe(viewLifecycleOwner) { event ->
            when (event) {
                is RegistrationNavigationEvent.GoBack -> requireActivity().onBackPressedDispatcher.onBackPressed()
                is RegistrationNavigationEvent.ShowAuthCode -> {
                    countryNumber = event.countryName
                    authType = event.authType
                    showFragment(
                        MeeraRegistrationCodeFragment.newInstance(
                            event.authType,
                            event.sendTo,
                            event.countryCode,
                            event.countryName,
                            event.countryMask,
                            event.smsTimeout
                        )
                    )
                    authViewModel.registrationStep = AmplitudePropertyRegistrationStep.REG_2
                }

                is RegistrationNavigationEvent.ShowRegistrationName -> {
                    showFragment(MeeraRegistrationNameFragment())
                    authViewModel.registrationStep = AmplitudePropertyRegistrationStep.REG_3
                }

                is RegistrationNavigationEvent.ShowRegistrationBirthday -> {
                    showFragment(MeeraRegistrationBirthdayFragment().apply {
                        arguments = bundleOf(ARG_COUNTRY_NUMBER to event.countryName)
                    })
                    authViewModel.registrationStep = AmplitudePropertyRegistrationStep.REG_4
                }

                is RegistrationNavigationEvent.ShowRegistrationGender -> {
                    showFragment(MeeraRegistrationGenderFragment().apply {
                        arguments = bundleOf(ARG_COUNTRY_NUMBER to event.countryName)
                    })
                    authViewModel.registrationStep = AmplitudePropertyRegistrationStep.REG_5
                }

                is RegistrationNavigationEvent.ShowRegistrationLocation -> {
                    showFragment(MeeraRegistrationLocationFragment().apply {
                        arguments = bundleOf(ARG_COUNTRY_NUMBER to event.countryName)
                    })
                    authViewModel.registrationStep = AmplitudePropertyRegistrationStep.REG_6
                }

                is RegistrationNavigationEvent.ShowRegistrationAvatar -> {
                    val authType =
                        if (authType == AuthType.Phone) AmplitudePropertyInputType.NUMBER.property else AmplitudePropertyInputType.EMAIL.property

                    showFragment(MeeraRegistrationAvatarFragment().apply {
                        arguments = bundleOf(
                            ARG_AUTH_TYPE to authType, ARG_COUNTRY_NUMBER to (event.countryName ?: countryNumber)
                        )
                    })
                    authViewModel.registrationStep = AmplitudePropertyRegistrationStep.REG_7
                }

                is RegistrationNavigationEvent.RegistrationEmail -> {
                    showFragment(MeeraRegistrationEmailFragment())
                }

                is RegistrationNavigationEvent.FinishRegistration -> {
                    act.supportFragmentManager.setFragmentResult(SUBSCRIPTION_ROAD_REQUEST_KEY, bundleOf())

                    authViewModel.getUserProfileLive()
                        .observeOnceButSkipNull(viewLifecycleOwner) {
                            if (authViewModel.isWorthToShow()) {
                                authViewModel.onRegistrationFinished()
                            } else {
                                authViewModel.onAuthFinished()
                            }
                            act.getMeeraAuthenticationNavigator().completeOnPersonalScreen()
                            findNavController().popBackStack()
                        }
                }

                is RegistrationNavigationEvent.CreateAvatar -> {
                    showFragment(MeeraContainerAvatarBaseFragment().apply {
                        arguments = bundleOf(ARG_AVATAR_STATE to event.avatarState)
                    })
                }

                is RegistrationNavigationEvent.ShowRegistrationDeleteProfileFragment -> {
                    showFragment(MeeraRecoveryProfileFragment()).apply {
                        arguments = bundleOf(EXTRA_COUNTRY_NAME to event.countryName)
                    }
                }

                is RegistrationNavigationEvent.ShowRegistrationRottenProfileFragment -> {
                    showFragment(MeeraRottenProfileFragment())
                }

                is RegistrationNavigationEvent.ShowRegistrationPhoneFragment -> {
                    showFragment(MeeraRegistrationPhoneFragment())
                }

                else -> {}
            }
        }
    }

    private fun showFragment(fragment: Fragment) {
        runCatching {
            childFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.slide_in_left,
                    R.anim.fade_out,
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
                )
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit()
        }.onFailure {
            Timber.e(it)
        }
    }

    private fun closeRegistration() {
        if (!isClosing) {
            context?.hideKeyboard(requireView())
            navigationViewModel.clearNavigationState()
            authViewModel.backNavigatePhoneScreenByCloseButton()
            logoutIfNeeded()
            isClosing = true
        }
    }

    private fun logoutIfNeeded() {
        act.logOutWithDelegate(true)
    }

    private fun initKeyboardListener() {
        keyboardHeightProvider?.release()
        binding?.root?.let { root ->
            keyboardHeightProvider = KeyboardHeightProvider(root)
            keyboardHeightProvider?.observer = { height ->
                authViewModel.saveKeyboardHeight(height)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        keyboardHeightProvider?.release()
    }

    override fun onDestroy() {
        super.onDestroy()
        authViewModel.resetRegContainerInstance()
    }

    private fun initStatusBar() {
        val layoutParamsStatusBar =
            binding?.statusBarRegistration?.layoutParams as ViewGroup.LayoutParams
        layoutParamsStatusBar.height = context.getStatusBarHeight()
        binding?.statusBarRegistration?.layoutParams = layoutParamsStatusBar
    }

    companion object {
        const val TAG = "RegistrationContainerFragment"
        const val SHOW_KEYBOARD_DELAY = 300L
        const val ARG_AUTH_TYPE = "ARG_AUTH_TYPE"
        const val ARG_COUNTRY_NUMBER = "ARG_COUNTRY_NUMBER"
    }
}
