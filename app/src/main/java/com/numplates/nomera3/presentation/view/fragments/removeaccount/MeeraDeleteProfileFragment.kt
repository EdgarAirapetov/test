package com.numplates.nomera3.presentation.view.fragments.removeaccount

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.meera.core.base.viewbinding.viewBinding
import com.meera.core.dialogs.MeeraConfirmDialogBuilder
import com.meera.core.extensions.setHtmlText
import com.meera.uikit.snackbar.UiKitSnackBar
import com.meera.uikit.snackbar.state.SnackBarParams
import com.meera.uikit.widgets.snackbar.AvatarUiState
import com.meera.uikit.widgets.snackbar.SnackBarContainerUiState
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.LayoutDeleteFriendMeeraBinding
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseDialogFragment
import com.numplates.nomera3.modules.redesign.fragments.base.ScreenBehaviourState
import com.numplates.nomera3.modules.redesign.util.NavigationManager
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserProfileModel
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_REMOVE_ACCOUNT_REASON_ID
import com.numplates.nomera3.presentation.viewmodel.ProfileDeleteRecoveryViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.UserProfileViewEvent
import kotlinx.coroutines.launch
import timber.log.Timber

class MeeraDeleteProfileFragment : MeeraBaseDialogFragment(R.layout.layout_delete_friend_meera, ScreenBehaviourState.Full) {

    override val containerId: Int
        get() = R.id.fragment_first_container_view

    private val binding by viewBinding(LayoutDeleteFriendMeeraBinding::bind)

    private val viewModel by viewModels<ProfileDeleteRecoveryViewModel>()
    private var reasonId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reasonId = arguments?.getInt(ARG_REMOVE_ACCOUNT_REASON_ID)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initClickListeners()
        initObservers()
    }

    private fun initView() {
        binding.tvDeleteDesc1.setHtmlText(getString(R.string.delete_profile_desc_1))
        binding.tvDeleteDesc2.setHtmlText(getString(R.string.delete_profile_desc_2))
    }

    private fun initObservers() {
        viewModel.liveViewEvent.observe(viewLifecycleOwner) { event ->
            when (event) {
                is UserProfileViewEvent.ProfileDeleteSuccess -> handleSuccessProfileDeleted()
                is UserProfileViewEvent.ProfileDeleteError -> showCommonError(R.string.error_while_deleting_profile)

                else -> {
                    Timber.i("Required event did not arrive")
                }
            }
        }

        viewModel.getUserProfileLive().asLiveData().observe(
            viewLifecycleOwner
        ) { userProfileModel ->
            loadUserInfo(userProfileModel)
        }
    }

    private fun showCommonError(errorTextRes: Int) {
        UiKitSnackBar.make(
            view = requireView(),
            params = SnackBarParams(
                snackBarViewState = SnackBarContainerUiState(
                    messageText = getText(errorTextRes),
                    avatarUiState = AvatarUiState.ErrorIconState
                ),
                duration = BaseTransientBottomBar.LENGTH_SHORT,
                dismissOnClick = true,
            )

        ).show()
    }

    private fun loadUserInfo(userProfileModel: UserProfileModel) {
        binding.let { layoutDeleteFriend ->
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

    private fun handleSuccessProfileDeleted() {
        (requireActivity() as? MeeraAct)?.logOutWithDelegate {
            lifecycleScope.launch {
                NavigationManager.getManager().logOutDoPassAndSetState()
            }
        }
    }

    private fun initClickListeners() {
        binding.buttonDelete.setOnClickListener {
            showConfirmDeleteDialog()
        }
        binding.buttonCancel.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun showConfirmDeleteDialog() {
        MeeraConfirmDialogBuilder()
            .setHeader(R.string.meera_account_remove)
            .setDescription(R.string.all_data_will_be_deleted)
            .setTopBtnText(R.string.general_delete)
            .setBottomBtnText(R.string.map_events_creation_cancel_positive)
            .setTopClickListener { viewModel.deleteProfile(reasonId) }
            .show(childFragmentManager)
    }
}
