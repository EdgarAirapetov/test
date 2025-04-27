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
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.OnlineSettingsWhitelistViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MeeraOnlineSettingsWhitelistFragment : MeeraBaseSettingsUserListFragment() {

    private val viewModel by viewModels<OnlineSettingsWhitelistViewModel> { App.component.getViewModelFactory() }
    private val privacyViewModel by viewModels<PrivacyNewViewModel>()

    override fun screenConfiguration() = MeeraBaseSettingsUserListConfiguration(
        screenTitle = getString(R.string.notification_settings_sources),
        isShowAddUserItem = true,
        addUserItemTitle = getString(R.string.meera_notification_settings_sources_subtitle),
        removeListMenuIcon = R.drawable.remove_user_menu_item,
        dialogListTitleRes = R.string.settings_privacy_list_user_delete_all_title,
        dialogListSubtitleRes = R.string.settings_privacy_list_user_delete_all_subtitle,
        dialogItemTitleRes = R.string.settings_privacy_list_user_delete_title,
        dialogItemSubtitleRes = R.string.settings_privacy_list_user_delete_subtitle,
        confirmationButtonTextRes = R.string.delete
    )

    override fun transitToAddUsersFragment() {
        findNavController().safeNavigate(R.id.action_meeraOnlineSettingsWhitelistFragment_to_meeraOnlineSettingsAddWhitelistFragment)
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
        viewModel.liveUsers.observe(viewLifecycleOwner){ users ->
            addUsersToAdapter(users)
        }
        viewModel.liveUsersCounter.observe(viewLifecycleOwner){ count ->
            privacyViewModel.requestSettings()
        }
        viewModel.liveProgress.observe(viewLifecycleOwner){ isShowProgress ->
            loadProgressVisibility(isShowProgress)
        }
        viewModel.liveViewEvent
            .flowWithLifecycle(lifecycle)
            .onEach { handleEvents(it) }
            .launchIn(lifecycleScope)
    }
}
