package com.numplates.nomera3.modules.bump.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.meera.core.extensions.gone
import com.meera.core.extensions.loadGlideCircleWithPlaceHolder
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.core.extensions.visibleAppearAnimate
import com.meera.core.utils.NToast
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentShakeFriendRequestsBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.bump.ui.animateNextUser
import com.numplates.nomera3.modules.bump.ui.animateSkipUser
import com.numplates.nomera3.modules.bump.ui.entity.ShakeFriendRequestsUiEffect
import com.numplates.nomera3.modules.bump.ui.entity.ShakeRequestsContentActions
import com.numplates.nomera3.modules.bump.ui.entity.UserFriendShakeStatus
import com.numplates.nomera3.modules.bump.ui.viewmodel.ShakeFriendRequestsViewModel
import com.numplates.nomera3.presentation.model.MutualUsersUiModel
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.BaseFragmentNew
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.fragments.UserInfoFragment
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ShakeFriendRequestsFragment : BaseFragmentNew<FragmentShakeFriendRequestsBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentShakeFriendRequestsBinding
        get() = FragmentShakeFriendRequestsBinding::inflate

    private val viewModel by viewModels<ShakeFriendRequestsViewModel> {
        App.component.getViewModelFactory()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
        initView()
    }

    override fun onStartFragment() {
        super.onStartFragment()
        closeShakeDialog()
        initStatusNavigationBarColored()
    }

    private fun initView() {
        initListeners()
    }

    private fun initObservers() {
        observeState()
        observeEffect()
    }

    private fun initStatusNavigationBarColored() {
        act.setStatusNavigationBarColor(
            navigationBarColor = R.color.colorPrimaryDark,
        )
    }

    private fun observeEffect() {
        viewModel.shakeFriendRequestsUiFlow
            .flowWithLifecycle(lifecycle)
            .onEach(::handleFriendRequestsEffect)
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun observeState() {
        viewModel.shakeFriendRequestsUiState.observe(viewLifecycleOwner) { state ->
            loadAvatar(state.shakeUser.avatarSmall)
            setShakeLabel(state.shakeUser.labelText)
            setMutualUsers(state.shakeUser.mutualUsers)
            handleUiStateByFriendStatus(state.shakeUser.userFriendShakeStatus)
            setButtonsVisibilityByState(state.shakeUser.userFriendShakeStatus)
            setDotsIndicatorState(
                dotsCount = state.dotsCount,
                selectedPosition = state.selectedPosition,
                needToShowDotIndicator = state.isNeedToShowDotsIndicator
            )
        }
    }

    private fun handleFriendRequestsEffect(effect: ShakeFriendRequestsUiEffect) {
        when (effect) {
            is ShakeFriendRequestsUiEffect.ShowSuccessToast -> {
                showSuccessToast(effect.messageRes)
            }
            is ShakeFriendRequestsUiEffect.CloseShakeFriendRequests -> {
                popBackStack()
            }
            is ShakeFriendRequestsUiEffect.ShowErrorToast -> {
                showErrorToast(effect.errorMessageRes)
            }
            is ShakeFriendRequestsUiEffect.NavigateToUserFragment -> {
                navigateToUserFragment(effect.userId)
            }
            is ShakeFriendRequestsUiEffect.AnimateNextUserUiEffect -> {
                animateNextUser()
            }
            is ShakeFriendRequestsUiEffect.AnimateSkipUserUiEffect -> {
                animateSkipUser()
            }
            is ShakeFriendRequestsUiEffect.AnimateVisibleAppearWithButtonsEffect -> {
                animateVisibleAppearViewsWithButtons()
            }
            is ShakeFriendRequestsUiEffect.AnimateVisibleAppearWithoutButtonsEffect -> {
                animateVisibleAppearViews()
            }
        }
    }

    private fun popBackStack() = requireActivity().onBackPressed()

    private fun navigateToUserFragment(userId: Long) {
        replace(
            getCurrentNavigatorPosition(),
            UserInfoFragment(),
            Act.LIGHT_STATUSBAR,
            Arg(IArgContainer.ARG_USER_ID, userId),
            Arg(IArgContainer.ARG_TRANSIT_FROM, AmplitudePropertyWhere.SHAKE.property)
        )
    }

    private fun showSuccessToast(@StringRes messageRes: Int) {
        NToast.with(activity)
            .typeSuccess()
            .text(getString(messageRes))
            .show()
    }

    private fun showErrorToast(@StringRes messageRes: Int) {
        NToast.with(activity)
            .typeError()
            .text(getString(messageRes))
            .show()
    }

    private fun setMutualUsers(mutualUsers: MutualUsersUiModel?) {
        mutualUsers?.let { users ->
            binding?.vgShakeFriendsMutualFriends?.visible()
            binding?.vgShakeFriendsMutualFriends?.setMutualUsers(users)
        } ?: run {
            binding?.vgShakeFriendsMutualFriends?.gone()
        }
    }

    private fun setShakeLabel(label: String) {
        binding?.tvShakeFriendRequestsName?.text = label
    }

    private fun loadAvatar(avatarUrl: String) {
        binding?.ivShakeFriendRequestsAvatar?.loadGlideCircleWithPlaceHolder(
            path = avatarUrl,
            placeholderResId = R.drawable.fill_8_round
        )
    }

    private fun initListeners() {
        binding?.tvShakeFriendsDeclineRequest?.setThrottledClickListener {
            viewModel.setContentAction(ShakeRequestsContentActions.OnFriendDeclineFriendRequestClicked)
        }
        binding?.btnShakeFriendsFriendAction?.setThrottledClickListener {
            viewModel.setContentAction(ShakeRequestsContentActions.OnFriendActionButtonClicked)
        }
        binding?.tvShakeFriendsClose?.setThrottledClickListener {
            viewModel.setContentAction(ShakeRequestsContentActions.OnCloseShakeUserClicked)
        }
    }

    private fun handleUiStateByFriendStatus(status: UserFriendShakeStatus) {
        when (status) {
            UserFriendShakeStatus.USER_SHAKE_FRIEND_REQUESTED_BY_USER -> {
                setFriendButtonActionText(R.string.accept_request)
            }
            else -> {
                setFriendButtonActionText(R.string.add_to_friends)
            }
        }
    }

    private fun closeShakeDialog() {
        act.hideShakeDialog()
    }

    private fun setFriendButtonActionText(@StringRes buttonTextRes: Int) {
        binding?.btnShakeFriendsFriendAction?.text = getString(buttonTextRes)
    }

    private fun getCurrentNavigatorPosition(): Int = try {
        act.navigatorViewPager.currentItem
    } catch (e: UninitializedPropertyAccessException) {
        e.printStackTrace()
        -1
    }

    private fun setButtonsVisibilityByState(friendState: UserFriendShakeStatus) {
        when (friendState) {
            UserFriendShakeStatus.USER_SHAKE_ALREADY_FRIENDS -> {
                binding?.btnShakeFriendsFriendAction?.gone()
                binding?.tvShakeFriendsDeclineRequest?.gone()
                binding?.tvShakeFriendsClose?.visible()
            }
            else -> {
                binding?.btnShakeFriendsFriendAction?.visible()
                binding?.tvShakeFriendsDeclineRequest?.visible()
                binding?.tvShakeFriendsClose?.gone()
            }
        }
    }

    private fun animateNextUser() {
        binding?.vgFriendRequestsConstrain?.animateNextUser {
            viewModel.setContentAction(ShakeRequestsContentActions.TryToRequestNextUserAction)
        }
    }

    private fun animateSkipUser() {
        binding?.vgFriendRequestsConstrain?.animateSkipUser {
            viewModel.setContentAction(ShakeRequestsContentActions.TryToRequestNextUserAction)
        }
    }

    private fun animateVisibleAppearViews() {
        binding?.cvShakeFriendRequestsAvatar?.visibleAppearAnimate()
        binding?.tvShakeFriendRequestsName?.visibleAppearAnimate()
    }

    private fun animateVisibleAppearViewsWithButtons() {
        binding?.cvShakeFriendRequestsAvatar?.visibleAppearAnimate()
        binding?.tvShakeFriendRequestsName?.visibleAppearAnimate()
        binding?.btnShakeFriendsFriendAction?.visibleAppearAnimate()
        binding?.tvShakeFriendsDeclineRequest?.visibleAppearAnimate()
    }

    private fun setDotsIndicatorState(
        dotsCount: Int,
        selectedPosition: Int,
        needToShowDotIndicator: Boolean
    ) {
        if (!needToShowDotIndicator) {
            binding?.piFriendRequests?.gone()
            return
        }
        binding?.piFriendRequests?.visible()
        binding?.piFriendRequests?.setDotCount(dotsCount)
        binding?.piFriendRequests?.setCurrentPosition(selectedPosition)
    }
}
