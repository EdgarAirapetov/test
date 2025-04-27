package com.numplates.nomera3.presentation.view.fragments.meerasettings

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.dialogs.MeeraConfirmDialogBuilder
import com.meera.core.extensions.safeNavigate
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.toBoolean
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.snackbar.AvatarUiState
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraProfileSettingsFragmentBinding
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitData
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitType
import com.numplates.nomera3.modules.profilesettings.model.ProfileSettingsEffect
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.modules.redesign.util.NavigationManager
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserProfileModel
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.viewmodel.ProfileSettingsViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MeeraProfileSettingsFragment :
    MeeraBaseDialogFragment(R.layout.meera_profile_settings_fragment, ScreenBehaviourState.Full) {

    override val containerId: Int
        get() = R.id.fragment_first_container_view

    private val binding by viewBinding(MeeraProfileSettingsFragmentBinding::bind)

    private val profileSettingsViewModel by viewModels<ProfileSettingsViewModel> {
        App.component.getViewModelFactory()
    }
    private var profileSettingsAdapter: MeeraProfileSettingsAdapter? = null
    private var profileSettingsRecyclerView: RecyclerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.component.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        initRecyclerView()
        setupLiveObservers()
        binding.logOutProfileSettings.setThrottledClickListener {
            showLogOutProfileDialog()
        }
        binding.vProfileSettingsUserHeader.setRightElementContainerClickable(false)
        binding.vProfileSettingsUserHeader.setThrottledClickListener {
            profileSettingsViewModel.logProfileEditTap()
            findNavController().safeNavigate(
                R.id.action_meeraProfileSettingsFragment_to_meeraUserPersonalInfoFragment,
                bundleOf(IArgContainer.ARG_CALLED_FROM_PROFILE to true)
            )
        }
        binding.userSettingsNavView.backButtonClickListener = { findNavController().popBackStack() }
        (view.parent as? View)?.doOnPreDraw { startPostponedEnterTransition() }
    }

    private fun setupLiveObservers() {
        lifecycleScope.launch {
            profileSettingsViewModel.getOwnProfileFlow().collect(::initUserPersonalInfo)
        }

        profileSettingsViewModel.profileSettingsEffectFlow
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach(::handleProfileSettingsEffect)
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun handleProfileSettingsEffect(effect: ProfileSettingsEffect) {
        when (effect) {
            is ProfileSettingsEffect.SupportUserIdFound -> {
                findNavController().safeNavigate(
                    R.id.action_meeraProfileSettingsFragment_to_meeraChatFragment,
                    bundle = bundleOf(
                        IArgContainer.ARG_CHAT_INIT_DATA to ChatInitData(
                            initType = ChatInitType.FROM_PROFILE,
                            userId = effect.userId
                        )
                    )
                )
            }
            is ProfileSettingsEffect.AboutMeeraUserIdFound -> {
                findNavController().safeNavigate(
                    resId = R.id.action_meeraProfileSettingsFragment_to_meeraAboutFragment,
                    bundle = Bundle().apply {
                        putLong(IArgContainer.ARG_USER_ID, effect.userId)
                    }
                )
            }
        }
    }

    private fun initUserPersonalInfo(user: UserProfileModel) {
        val prefix = getString(R.string.uniquename_prefix)
        binding.vProfileSettingsUserHeader.apply {
            cellCityText = false
            if (user.approved.toBoolean()) {
                cellTitleVerified = user.approved.toBoolean()
            } else {
                cellTitleInterestingAuthor = user.topContentMaker.toBoolean()
            }
            setTitleValue(user.name ?: "")
            setDescriptionValue("""$prefix${user.uniquename}""")
            setLeftUserPicConfig(UserpicUiModel(userAvatarUrl = user.avatarSmall))
        }
    }

    private fun initRecyclerView() {
        profileSettingsAdapter = MeeraProfileSettingsAdapter(
            items = MeeraProfileSettingsItemType.entries,
            callback = this::initProfileSettingsAction
        )
        profileSettingsRecyclerView = binding.settingsRecyclerView
        profileSettingsRecyclerView?.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        profileSettingsRecyclerView?.adapter = profileSettingsAdapter
    }

    private fun initProfileSettingsAction(action: MeeraProfileSettingsAction) {
        when (action) {
            MeeraProfileSettingsAction.MeeraAboutMeeraAction -> profileSettingsViewModel.aboutMeeraClicked()

            MeeraProfileSettingsAction.MeeraPrivacySecurityAction ->
                findNavController().safeNavigate(R.id.action_meeraProfileSettingsFragment_to_meeraPrivacyFragment)

            MeeraProfileSettingsAction.MeeraPushNotificationAction ->
                findNavController()
                    .safeNavigate(R.id.action_meeraProfileSettingsFragment_to_meeraPushNotificationSettingsFragment)

            MeeraProfileSettingsAction.MeeraRateAppAction -> rateApp()
            MeeraProfileSettingsAction.MeeraRestorePurchases -> showPurchasesRestoredSnackBar()
            MeeraProfileSettingsAction.MeeraSupportAction -> profileSettingsViewModel.supportClicked()
        }
    }

    private fun showPurchasesRestoredSnackBar() {
        UiKitSnackBar.make(
            view = requireView(),
            params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = getText(R.string.purchases_successfully_restored),
                    avatarUiState = AvatarUiState.SuccessIconState
                ),
                duration = BaseTransientBottomBar.LENGTH_SHORT,
                dismissOnClick = true,
            )
        ).show()
    }

    private fun rateApp() {
        MeeraRateUsFlowController().startRateUsFlow(childFragmentManager)
    }

    private fun showLogOutProfileDialog() {
        MeeraConfirmDialogBuilder()
            .setHeader(R.string.want_to_get_out)
            .setTopBtnText(R.string.profile_logout)
            .setBottomBtnText(R.string.cancel)
            .setTopClickListener {
                (requireActivity() as? MeeraAct)?.logOutWithDelegate {
                    lifecycleScope.launch {
                        NavigationManager.getManager().logOutDoPassAndSetState()
                    }
                }
            }
            .show(childFragmentManager)
    }
}
