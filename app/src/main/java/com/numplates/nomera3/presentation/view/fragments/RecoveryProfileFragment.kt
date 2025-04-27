package com.numplates.nomera3.presentation.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import com.meera.core.extensions.getStatusBarHeight
import com.meera.core.extensions.observeOnceButSkipNull
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.LayoutRecoverProfileBinding
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.callback.IOnBackPressed
import com.numplates.nomera3.presentation.view.navigator.NavigatorViewPager
import com.numplates.nomera3.presentation.view.utils.NToast
import com.numplates.nomera3.presentation.viewmodel.ProfileDeleteRecoveryViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.UserProfileViewEvent

class RecoveryProfileFragment : BaseFragmentNew<LayoutRecoverProfileBinding>(), IOnBackPressed {
    val viewModel by viewModels<ProfileDeleteRecoveryViewModel>()
    private var userId: Long? = null
    private var millsUntilDelete: Long? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userId = 0
        arguments?.let {
            millsUntilDelete = it.getLong(
                IArgContainer.ARG_TIME_MILLS,
                System.currentTimeMillis() + (1000 * 60 * 60 * 49) + 1000 * 60 * 59
            )
        }
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> LayoutRecoverProfileBinding
        get() = LayoutRecoverProfileBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.statusBarRecoveryProfile?.layoutParams?.height = context.getStatusBarHeight()
        act.navigatorViewPager.setAllowedSwipeDirection(NavigatorViewPager.SwipeDirection.NONE)
        initListeners()
        initObservers()
    }

    private fun initListeners() {
        binding?.ivBackRecoveryProfile?.setOnClickListener {
            onBackPressed()
        }
        binding?.tvDeleteProfileBtn?.setOnClickListener {
            binding?.tvDeleteProfileBtn?.isEnabled = false
            viewModel.recoverProfile()
        }
        millsUntilDelete?.let {
            binding?.nomeraTimerView?.setTime(it)
        }
    }

    private fun initObservers() {
        viewModel.liveViewEvent.observe(viewLifecycleOwner, Observer { event ->
            when (event) {
                is UserProfileViewEvent.ProfileRecoveryError -> {
                    binding?.tvDeleteProfileBtn?.isEnabled = true
                    handleRecoveryError()
                }
                is UserProfileViewEvent.ProfileRecoverySuccess -> {
                    if (act.getAuthenticationNavigator().isAuthScreenOpen()) {
                        viewModel.getUserProfileLive().asLiveData().observeOnceButSkipNull(viewLifecycleOwner) {
                            act.getAuthenticationNavigator().completeOnPersonalScreen()
                        }
                    } else {
                        binding?.tvDeleteProfileBtn?.isEnabled = true
                        handleRecoverySuccess()
                    }
                }
                else -> {}
            }
        })
    }

    private fun handleRecoveryError() {
        NToast.with(view)
            .text(getString(R.string.error_while_restoring_profile))
            .typeError()
            .show()
    }

    private fun handleRecoverySuccess() {
        exitScreen()
    }

    override fun onBackPressed(): Boolean {
        act.getAuthenticationNavigator().backNavigateRecoveryScreenByBack()
        act.logOutWithDelegate()
        return true
    }

    private fun exitScreen() {
        act.navigatorViewPager.setCurrentItem(act.navigatorViewPager.currentItem - 1, true)
    }

}
