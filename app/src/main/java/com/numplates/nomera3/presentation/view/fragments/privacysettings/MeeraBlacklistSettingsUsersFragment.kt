package com.numplates.nomera3.presentation.view.fragments.privacysettings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.meera.core.extensions.safeNavigate
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.usersettings.ui.viewmodel.PrivacyNewViewModel
import com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments.MeeraBaseSettingsUserListConfiguration
import com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments.MeeraBaseSettingsUserListFragment
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.BlacklistSettingsViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Show already added users in blacklist
 */
class MeeraBlacklistSettingsUsersFragment : MeeraBaseSettingsUserListFragment() {

    private val viewModel by viewModels<BlacklistSettingsViewModel> { App.component.getViewModelFactory() }
    private val privacyViewModel by viewModels<PrivacyNewViewModel>()

    override fun screenConfiguration() = MeeraBaseSettingsUserListConfiguration(
        screenTitle = getString(R.string.meera_settings_privacy_blocked),
        isShowAddUserItem = true,
        addUserItemTitle = getString(R.string.settings_privacy_blacklist),
        isShowDeleteAllItem = true,
        dialogListTitleRes = R.string.settings_privacy_dialog_unblock_all_title,
        dialogListSubtitleRes = R.string.meera_settings_privacy_dialog_unblock_all_subtitle,
        dialogItemTitleRes = R.string.meera_settings_privacy_dialog_unblock_all_confirm_title,
        dialogItemSubtitleRes = R.string.meera_settings_privacy_dialog_unblock_all_confirm_subtitle,
        confirmationButtonTextRes = R.string.general_unblock
    )

    override fun transitToAddUsersFragment() {
        findNavController().safeNavigate(R.id.action_meeraBlacklistSettingsUsersFragment_to_meeraBlacklistSettingsAddUsersFragment)
    }

    override fun removeUserFromList(userIds: List<Long>, adapterPosition: Int) {
        viewModel.deleteUser(userIds, adapterPosition)
    }

    override fun removeAllUsersFromList() {
        viewModel.deleteAllUsers()
    }

    override fun showUsersRequest(limit: Int, offset: Int) {
        viewModel.getUsers(limit, offset)
    }

    override fun lastPage(): Boolean = viewModel.isLastPage

    override fun loading(): Boolean = viewModel.isLoading

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.liveUsers.observe(viewLifecycleOwner) { users ->
            addUsersToAdapter(users)
        }
        viewModel.liveUsersCounter.observe(viewLifecycleOwner){ count ->
            privacyViewModel.requestSettings()
        }
        viewModel.liveProgress.observe(viewLifecycleOwner) { isShowProgress ->
            loadProgressVisibility(isShowProgress)
        }
        viewModel.liveViewEvent
            .flowWithLifecycle(lifecycle)
            .onEach { handleEvents(it) }
            .launchIn(lifecycleScope)
    }
}
