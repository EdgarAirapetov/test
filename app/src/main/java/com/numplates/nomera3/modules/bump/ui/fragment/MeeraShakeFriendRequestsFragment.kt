package com.numplates.nomera3.modules.bump.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.safeNavigate
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.core.extensions.visibleAppearAnimate
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.snackbar.AvatarUiState
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraFragmentShakeFriendRequestsBinding
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.bump.ui.animateNextUser
import com.numplates.nomera3.modules.bump.ui.animateSkipUser
import com.numplates.nomera3.modules.bump.ui.entity.ShakeFriendRequestsUiEffect
import com.numplates.nomera3.modules.bump.ui.entity.ShakeRequestsContentActions
import com.numplates.nomera3.modules.bump.ui.entity.UserFriendShakeStatus
import com.numplates.nomera3.modules.bump.ui.viewmodel.ShakeFriendRequestsViewModel
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.presentation.model.MutualUsersUiModel
import com.numplates.nomera3.presentation.router.IArgContainer
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

private const val VIEW_GROUP_MARGIN = 16

class MeeraShakeFriendRequestsFragment : MeeraBaseDialogFragment(
    layout = R.layout.meera_fragment_shake_friend_requests,
    behaviourConfigState = ScreenBehaviourState.FullScreenMoment
) {

    override val containerId: Int
        get() = R.id.fragment_first_container_view

    private val binding by viewBinding(MeeraFragmentShakeFriendRequestsBinding::bind)

    private val viewModel by viewModels<ShakeFriendRequestsViewModel> {
        App.component.getViewModelFactory()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
        initView()
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val topInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            binding?.vgFriendRequests?.setMargins(
                top = topInsets.top + VIEW_GROUP_MARGIN.dp,
                end = VIEW_GROUP_MARGIN.dp,
                start = VIEW_GROUP_MARGIN.dp,
                bottom = VIEW_GROUP_MARGIN.dp
            )
            return@setOnApplyWindowInsetsListener insets
        }
        closeShakeDialog()
    }

    private fun initView() {
        initListeners()
    }

    private fun initObservers() {
        observeState()
        observeEffect()
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

    private fun popBackStack() = requireActivity().onBackPressedDispatcher.onBackPressed()

    private fun navigateToUserFragment(userId: Long) {
        findNavController().safeNavigate(
            R.id.action_meeraShakeFriendRequestsFragment_to_userInfoFragment,
            bundleOf(
                IArgContainer.ARG_USER_ID to userId,
                IArgContainer.ARG_TRANSIT_FROM to AmplitudePropertyWhere.SHAKE.property
            )
        )
    }

    private fun showSuccessToast(@StringRes messageRes: Int) {
        UiKitSnackBar.make(
            requireView(),
            SnackBarParams(
                SnackBarContainerUiState(
                    messageText = requireContext().getText(messageRes),
                    avatarUiState = AvatarUiState.SuccessIconState
                )
            )
        ).show()
    }

    private fun showErrorToast(@StringRes messageRes: Int) {
        UiKitSnackBar.make(
            requireView(),
            SnackBarParams(
                SnackBarContainerUiState(
                    messageText = requireContext().getText(messageRes),
                    avatarUiState = AvatarUiState.ErrorIconState
                )
            )
        ).show()
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
        binding?.upiShakeFriendRequest?.setConfig(
            UserpicUiModel(
                userAvatarUrl = avatarUrl
            )
        )
    }

    private fun initListeners() {
        binding?.btnShakeFriendsDeclineRequest?.setThrottledClickListener {
            viewModel.setContentAction(ShakeRequestsContentActions.OnFriendDeclineFriendRequestClicked)
        }
        binding?.btnShakeFriendsFriendAction?.setThrottledClickListener {
            viewModel.setContentAction(ShakeRequestsContentActions.OnFriendActionButtonClicked)
        }
        binding?.btnShakeFriendsClose?.setThrottledClickListener {
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
        (requireActivity() as MeeraAct).hideShakeDialog()
    }

    private fun setFriendButtonActionText(@StringRes buttonTextRes: Int) {
        binding?.btnShakeFriendsFriendAction?.text = getString(buttonTextRes)
    }

    private fun setButtonsVisibilityByState(friendState: UserFriendShakeStatus) {
        when (friendState) {
            UserFriendShakeStatus.USER_SHAKE_ALREADY_FRIENDS -> {
                binding?.btnShakeFriendsFriendAction?.gone()
                binding?.btnShakeFriendsDeclineRequest?.gone()
                binding?.btnShakeFriendsClose?.visible()
            }

            else -> {
                binding?.btnShakeFriendsFriendAction?.visible()
                binding?.btnShakeFriendsDeclineRequest?.visible()
                binding?.btnShakeFriendsClose?.gone()
            }
        }
    }

    private fun animateNextUser() {
        binding?.vgFriendRequests?.animateNextUser {
            viewModel.setContentAction(ShakeRequestsContentActions.TryToRequestNextUserAction)
        }
    }

    private fun animateSkipUser() {
        binding?.vgFriendRequests?.animateSkipUser {
            viewModel.setContentAction(ShakeRequestsContentActions.TryToRequestNextUserAction)
        }
    }

    private fun animateVisibleAppearViews() {
        binding?.upiShakeFriendRequest?.visibleAppearAnimate()
        binding?.tvShakeFriendRequestsName?.visibleAppearAnimate()
    }

    private fun animateVisibleAppearViewsWithButtons() {
        binding?.upiShakeFriendRequest?.visibleAppearAnimate()
        binding?.tvShakeFriendRequestsName?.visibleAppearAnimate()
        binding?.btnShakeFriendsFriendAction?.visibleAppearAnimate()
        binding?.btnShakeFriendsDeclineRequest?.visibleAppearAnimate()
    }

    private fun setDotsIndicatorState(
        dotsCount: Int,
        selectedPosition: Int,
        needToShowDotIndicator: Boolean
    ) {
        if (!needToShowDotIndicator) {
            binding?.stlFriendRequests?.gone()
            return
        }
        binding?.stlFriendRequests?.visible()
        binding?.stlFriendRequests?.tabCount = dotsCount
        binding?.stlFriendRequests?.setSelectedTabIndex(selectedPosition)
    }
}
