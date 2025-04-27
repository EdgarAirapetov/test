package com.numplates.nomera3.presentation.view.fragments

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.findNavController
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.observeOnceButSkipNull
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.utils.showCommonError
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.LayoutRecoverProfileMeeraBinding
import com.numplates.nomera3.modules.appDialogs.ui.MeeraDialogNavigator
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseFragment
import com.numplates.nomera3.modules.registration.ui.EXTRA_COUNTRY_NAME
import com.numplates.nomera3.modules.registration.ui.RegistrationNavigationViewModel
import com.numplates.nomera3.modules.registration.ui.code.RegistrationCodeViewModel
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserProfileModel
import com.numplates.nomera3.presentation.viewmodel.ProfileDeleteRecoveryViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.UserProfileViewEvent
import timber.log.Timber

private const val MINUTE_IN_MILLIS = 60000L
private const val ONE_SEC_IN_MILLIS = 1000
private const val ONE_MIN_IN_SEC = 60
private const val FORTY_NINE = 49
private const val FIFTY_NINE = 59
const val RECOVERY_SERVICE_REQUEST_KEY = "RECOVERY_SERVICE_REQUEST_KEY"

class MeeraRecoveryProfileFragment : MeeraBaseFragment(
    layout = R.layout.layout_recover_profile_meera
) {

    private val viewModel by viewModels<ProfileDeleteRecoveryViewModel>()
    private val navigationViewModel by viewModels<RegistrationNavigationViewModel>(
        ownerProducer = { requireParentFragment() }
    )
    private val verifyCodeViewModel by viewModels<RegistrationCodeViewModel>()
    private val binding by viewBinding(LayoutRecoverProfileMeeraBinding::bind)
    private val act: MeeraAct by lazy {
        requireActivity() as MeeraAct
    }
    private var countryName: String? = null
    private var timer: CountDownTimer? = null
    private var isCanceledTimer: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        countryName = arguments?.getString(EXTRA_COUNTRY_NAME)
        initListeners()
        initObservers()
    }

    private fun initListeners() {
        binding?.buttonCancel?.setThrottledClickListener {
            navigationViewModel.finishRegistration()
        }
        binding?.buttonRecoverAccount?.setThrottledClickListener {
            viewModel.recoverProfile()
        }
    }

    private fun initObservers() {
        viewModel.liveViewEvent.observe(viewLifecycleOwner) { event ->
            profileRecovery(event)
        }

        observeUserProfileLive()
    }

    private fun observeUserProfileLive() {
        viewModel.getUserProfileLive().asLiveData().observe(
            viewLifecycleOwner
        ) { userProfileModel ->
            startTimer(
                userProfileModel.deletedAt ?: (System.currentTimeMillis() +
                    (ONE_SEC_IN_MILLIS * ONE_MIN_IN_SEC * ONE_MIN_IN_SEC * FORTY_NINE) +
                    ONE_SEC_IN_MILLIS * ONE_MIN_IN_SEC * FIFTY_NINE)
            )

            if (userProfileModel.profileVerified == 0) {
                binding?.ivVerified?.visibility = View.VISIBLE
            }

            loadUserInfo(userProfileModel)
        }
    }

    private fun openRottenProfileFragment() {
        navigationViewModel.registrationRottenProfileNext()
    }

    private fun startTimer(millsUntilDelete: Long) {
        timer = CountDownTimerLocal(millsUntilDelete).start()
    }


    override fun onDestroy() {
        super.onDestroy()
        isCanceledTimer = true
        timer = null
    }

    private fun profileRecovery(event: UserProfileViewEvent) {
        when (event) {
            is UserProfileViewEvent.ProfileRecoveryError -> {
                showCommonError(getText(R.string.error_while_restoring_profile), requireView())
            }

            is UserProfileViewEvent.ProfileRecoverySuccess -> {
                profileRecovery()
            }

            else -> {
                Timber.i("Required event did not arrive")
            }
        }
    }

    private fun profileRecovery() {
        if (act.getMeeraAuthenticationNavigator().isAuthScreenOpen()) {
            viewModel.getUserProfileLive().asLiveData().observeOnceButSkipNull(viewLifecycleOwner) {
                authenticationSuccess()
            }
        } else {
            handleRecoverySuccess()
        }
    }


    private fun authenticationSuccess() {
        verifyCodeViewModel.subscribePush()
        verifyCodeViewModel.saveLastSmsCodeTime()
        act.connectSocket()
        verifyCodeViewModel.getUserProfileLive()
            .observeOnceButSkipNull(viewLifecycleOwner) { profile ->
                handleProfile(profile.isProfileFilled)
                if (profile.isProfileFilled) {
                    act.getHolidayInfo(true)
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
            act.supportFragmentManager.setFragmentResult(RECOVERY_SERVICE_REQUEST_KEY, bundleOf())
            findNavController().popBackStack()
        } else {
            verifyCodeViewModel.setIsNeedShowHoliday(false)
            act.getMeeraAuthenticationNavigator().navigateToPersonalInfo(countryName)
        }
    }

    private fun loadUserInfo(userProfileModel: UserProfileModel) {
        binding?.let { layoutDeleteFriend ->
            layoutDeleteFriend.picIcon.setConfig(
                UserpicUiModel(
                    userAvatarUrl = userProfileModel.avatarBig,
                )
            )
            layoutDeleteFriend.userName.text = userProfileModel.name
            layoutDeleteFriend.userId.text =
                resources.getString(R.string.uniquename_prefix) + userProfileModel.uniquename
        }
    }

    private fun handleRecoverySuccess() {
        exitScreen()
    }

    private fun exitScreen() {
        findNavController().popBackStack()
    }

    inner class CountDownTimerLocal(val millsUntilDelete: Long) : CountDownTimer(millsUntilDelete, MINUTE_IN_MILLIS) {
        override fun onTick(millisUntilFinished: Long) {
            if (!isCanceledTimer) {
                binding.meeraNomeraTimerView.setTime(millsUntilDelete)
            } else {
                cancel()
            }
        }

        override fun onFinish() {
            if (millsUntilDelete - System.currentTimeMillis() >= 0) {
                openRottenProfileFragment()
            }
        }
    }
}
