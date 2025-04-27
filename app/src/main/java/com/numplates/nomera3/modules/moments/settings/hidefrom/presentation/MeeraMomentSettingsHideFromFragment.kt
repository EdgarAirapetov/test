package com.numplates.nomera3.modules.moments.settings.hidefrom.presentation

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.meera.core.extensions.empty
import com.meera.core.extensions.safeNavigate
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.moments.settings.presentation.MOMENT_SETTINGS_REFRESH_CACHE
import com.numplates.nomera3.modules.moments.settings.presentation.REFRESH_RESULT_EMPTY_KEY
import com.numplates.nomera3.modules.upload.util.safeCollect
import com.numplates.nomera3.modules.usersettings.ui.viewmodel.PrivacyNewViewModel
import com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments.MeeraBaseSettingsUserListConfiguration
import com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments.MeeraBaseSettingsUserListFragment
import kotlinx.coroutines.launch

class MeeraMomentSettingsHideFromFragment : MeeraBaseSettingsUserListFragment() {

    private val viewModel by viewModels<MomentSettingsHideFromViewModel> { App.component.getViewModelFactory() }
    private val privacyViewModel by viewModels<PrivacyNewViewModel>()

    override fun screenConfiguration() = MeeraBaseSettingsUserListConfiguration(
        screenTitle = getString(R.string.moment_settings_hide_from_title),
        isShowAddUserItem = true,
        addUserItemTitle = getString(R.string.meera_moments_settings_not_show_add_user_label),
        isShowDeleteAllItem = true,
        dialogListTitleRes = R.string.settings_privacy_list_user_delete_all_title,
        dialogListSubtitleRes = R.string.settings_privacy_list_user_delete_all_subtitle,
        dialogItemTitleRes = R.string.settings_privacy_list_user_delete_title,
        dialogItemSubtitleRes = R.string.settings_privacy_list_user_delete_subtitle,
        confirmationButtonTextRes = R.string.general_delete

    )

    override fun transitToAddUsersFragment() {
        findNavController().safeNavigate(R.id.action_meeraMomentSettingsHideFromFragment_to_meeraMomentSettingsHideFromAddUserFragment)
    }

    override fun onDestroy() {
        super.onDestroy()
        setFragmentResult(
            MOMENT_SETTINGS_REFRESH_CACHE,
            bundleOf(REFRESH_RESULT_EMPTY_KEY to String.empty())
        )
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

        observeUsers()
        observeProgress()
        observeViewEvent()
    }

    private fun observeUsers() {
        viewModel.liveUsers.observe(viewLifecycleOwner) { users ->
            addUsersToAdapter(users)
        }
        viewModel.liveUsersCounter.observe(viewLifecycleOwner){ _ ->
            privacyViewModel.requestSettings()
        }
    }

    private fun observeProgress() {
        viewModel.liveProgress.observe(viewLifecycleOwner) { isShowProgress ->
            loadProgressVisibility(isShowProgress)
        }
    }

    private fun observeViewEvent() {
        lifecycleScope.launch {
            viewModel.liveViewEvent.safeCollect { event ->
                handleEvents(event)
            }
        }
    }
}
