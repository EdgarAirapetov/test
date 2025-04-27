package com.numplates.nomera3.modules.registration.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.hideKeyboard
import com.meera.core.extensions.observeOnceButSkipNull
import com.meera.core.utils.KeyboardHeightProvider
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentRegistrationContainerBinding
import com.numplates.nomera3.modules.auth.ui.AuthViewModel
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyInputType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyRegistrationStep
import com.numplates.nomera3.modules.registration.AuthType
import com.numplates.nomera3.modules.registration.ui.avatar.RegistrationAvatarFragment
import com.numplates.nomera3.modules.registration.ui.birthday.RegistrationBirthdayFragment
import com.numplates.nomera3.modules.registration.ui.code.RegistrationCodeFragment
import com.numplates.nomera3.modules.registration.ui.email.RegistrationEmailFragment
import com.numplates.nomera3.modules.registration.ui.gender.RegistrationGenderFragment
import com.numplates.nomera3.modules.registration.ui.location.RegistrationLocationFragment
import com.numplates.nomera3.modules.registration.ui.name.RegistrationNameFragment
import com.numplates.nomera3.modules.registration.ui.phoneemail.RegistrationPhoneEmailFragment
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.callback.IOnBackPressed
import timber.log.Timber

class RegistrationContainerFragment : BaseFragmentNew<FragmentRegistrationContainerBinding>(),
    IArgContainer,
    IOnBackPressed {

    private var isClosing = false

    /**
     * requireParentFragment() не подходит, потому что этот фрагмент приавязан к активити
     */
    private val authViewModel by viewModels<AuthViewModel>(
        ownerProducer = { requireActivity() }
    )

    private val navigationViewModel by viewModels<RegistrationNavigationViewModel>(
        ownerProducer = { this }
    )

    private var authType:AuthType? =null
    private var countryNumber:String? =null

    private var keyboardHeightProvider: KeyboardHeightProvider? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initKeyboardListener()
        initStatusBar()
        observeNavigationViewModel()
    }

    override fun onStart() {
        super.onStart()
        isClosing = false
    }

    override fun onBackPressed(): Boolean {
        return if (isBackStackContainsMoreThanOne() && !isClosing) {
            childFragmentManager.popBackStack()
            true
        } else {
            closeRegistration()
            false
        }
    }

    fun startRegistration(countryName: String?) {
        navigationViewModel.isBackFromNameAvailable = isBackStackContainsMoreThanOne()
        childFragmentManager.popBackStack()
        showFragment(RegistrationNameFragment().apply { arguments = bundleOf(ARG_COUNTRY_NUMBER to countryName) })
        authViewModel.registrationStep = AmplitudePropertyRegistrationStep.REG_3
    }

    fun openPhoneMailInput() {
        showFragment(RegistrationPhoneEmailFragment())
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
        showFragment(RegistrationCodeFragment.newInstance(authType, sendTo, countryCode, countryName, countryMask, smsTimeout))
        authViewModel.registrationStep = AmplitudePropertyRegistrationStep.REG_2
    }

    private fun isBackStackContainsMoreThanOne(): Boolean {
        return childFragmentManager.backStackEntryCount > 1
    }

    private fun initStatusBar() {
        val layoutParamsStatusBar =
            binding?.statusBarRegistration?.layoutParams as ViewGroup.LayoutParams
        layoutParamsStatusBar.height = context.getStatusBarHeight()
        binding?.statusBarRegistration?.layoutParams = layoutParamsStatusBar
    }

    private fun observeNavigationViewModel() {
        navigationViewModel.eventLiveData.observe(viewLifecycleOwner) { event ->
            when (event) {
                is RegistrationNavigationEvent.GoBack -> onBackPressed()
                is RegistrationNavigationEvent.ShowAuthCode -> {
                    countryNumber = event.countryName
                    authType = event.authType
                    showFragment(
                        RegistrationCodeFragment.newInstance(
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
                    showFragment(RegistrationNameFragment())
                    authViewModel.registrationStep = AmplitudePropertyRegistrationStep.REG_3
                }
                is RegistrationNavigationEvent.ShowRegistrationBirthday -> {
                    showFragment(RegistrationBirthdayFragment().apply { arguments = bundleOf(ARG_COUNTRY_NUMBER to event.countryName)})
                    authViewModel.registrationStep = AmplitudePropertyRegistrationStep.REG_4
                }
                is RegistrationNavigationEvent.ShowRegistrationGender -> {
                    showFragment(RegistrationGenderFragment().apply { arguments = bundleOf(ARG_COUNTRY_NUMBER to event.countryName)})
                    authViewModel.registrationStep = AmplitudePropertyRegistrationStep.REG_5
                }
                is RegistrationNavigationEvent.ShowRegistrationLocation -> {
                    showFragment(RegistrationLocationFragment().apply { arguments = bundleOf(ARG_COUNTRY_NUMBER to event.countryName)})
                    authViewModel.registrationStep = AmplitudePropertyRegistrationStep.REG_6
                }
                is RegistrationNavigationEvent.ShowRegistrationAvatar -> {
                    val authType =
                        if (authType == AuthType.Phone) AmplitudePropertyInputType.NUMBER.property else AmplitudePropertyInputType.EMAIL.property

                    showFragment(RegistrationAvatarFragment().apply {
                        arguments = bundleOf(
                            ARG_AUTH_TYPE to authType, ARG_COUNTRY_NUMBER to (event.countryName?:countryNumber)
                        )
                    })
                    authViewModel.registrationStep = AmplitudePropertyRegistrationStep.REG_7
                }
                is RegistrationNavigationEvent.RegistrationEmail -> {
                    showFragment(RegistrationEmailFragment())
                }
                is RegistrationNavigationEvent.FinishRegistration -> {
                    authViewModel.getUserProfileLive()
                        .observeOnceButSkipNull(viewLifecycleOwner) {
                            if (authViewModel.isWorthToShow()) {
                                authViewModel.onRegistrationFinished()
                            } else {
                                authViewModel.onAuthFinished()
                            }
                            act.getAuthenticationNavigator().completeOnPersonalScreen()
                        }
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
            keyboardHeightProvider?.observer = { height -> authViewModel.saveKeyboardHeight(height) }
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

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentRegistrationContainerBinding
        get() = FragmentRegistrationContainerBinding::inflate

    companion object {
        const val TAG = "RegistrationContainerFragment"
        const val SHOW_KEYBOARD_DELAY = 500L
        const val ARG_AUTH_TYPE = "ARG_AUTH_TYPE"
        const val ARG_COUNTRY_NUMBER = "ARG_COUNTRY_NUMBER"
    }
}
