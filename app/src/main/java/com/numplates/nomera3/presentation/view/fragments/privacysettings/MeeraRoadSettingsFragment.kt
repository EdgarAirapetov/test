package com.numplates.nomera3.presentation.view.fragments.privacysettings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.usersettings.ui.viewmodel.PrivacyNewViewModel
import com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments.MeeraBaseSettingsUserListConfiguration
import com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments.MeeraBaseSettingsUserListFragment
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.RoadSettingsViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


class MeeraRoadSettingsFragment : MeeraBaseSettingsUserListFragment() {

    private val viewModel by viewModels<RoadSettingsViewModel> { App.component.getViewModelFactory() }
    private val privacyViewModel by viewModels<PrivacyNewViewModel>()
    override fun screenConfiguration() = MeeraBaseSettingsUserListConfiguration (
        screenTitle = getString(R.string.meera_settings_privacy_hide_posts),
        isShowAddUserItem = false,
        addUserItemTitle = getString(R.string.settings_privacy_list_user_new_post_not_show),
        dialogListTitleRes = R.string.settings_privacy_list_user_delete_title,
        dialogListSubtitleRes = R.string.settings_privacy_list_user_delete_subtitle,
        dialogItemTitleRes = R.string.settings_privacy_list_user_delete_all_title,
        dialogItemSubtitleRes = R.string.settings_privacy_list_user_delete_all_subtitle,
        confirmationButtonTextRes = R.string.road_delete
    )

    override fun transitToAddUsersFragment() {
        /** STUB  do nothing */
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

        viewModel.liveUsers.observe(viewLifecycleOwner, Observer { users ->
            addUsersToAdapter(users)
        })
        viewModel.liveUsersCounter.observe(viewLifecycleOwner){ _ ->
            privacyViewModel.requestSettings()
        }
        viewModel.liveProgress.observe(viewLifecycleOwner, Observer { isShowProgress ->
            loadProgressVisibility(isShowProgress)
        })
        viewModel.liveViewEvent
            .flowWithLifecycle(lifecycle)
            .onEach { handleEvents(it) }
            .launchIn(lifecycleScope)
    }
}
