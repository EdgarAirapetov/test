package com.numplates.nomera3.modules.moments.settings.notshow.presentation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.upload.util.safeCollect
import com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments.BaseSettingsUserListFragment
import kotlinx.coroutines.launch

class MomentSettingsNotShowFragment : BaseSettingsUserListFragment() {

    private val viewModel by viewModels<MomentSettingsNotShowViewModel> { App.component.getViewModelFactory() }

    override fun screenConfiguration() = BaseSettingsUserListConfiguration(
        screenTitle = getString(R.string.moments_settings_not_show_title),
        isShowAddUserItem = true,
        addUserItemTitle = getString(R.string.moments_settings_not_show_add_user_label),
        isShowDeleteAllItem = true
    )

    override fun transitToAddUsersFragment() {
        add(MomentSettingsNotShowAddUserFragment(), Act.LIGHT_STATUSBAR)
    }

    override fun removeUserFromList(userIds: List<Long>, adapterPosition: Int) {
        viewModel.deleteUser(userIds, adapterPosition)
    }

    override fun removeAllUsersFromList(userIds: List<Long>) {
        showDeleteAllConfirmationDialog {
            viewModel.deleteAllUsers()
        }
    }

    override fun showUsersRequest(limit: Int, offset: Int) {
        viewModel.getUsers(limit, offset)
    }

    override fun lastPage(): Boolean = viewModel.isLastPage

    override fun loading(): Boolean = viewModel.isLoading

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeUsers()
        observeCounter()
        observeProgress()
        observeViewEvent()
    }

    private fun observeUsers() {
        viewModel.liveUsers.observe(viewLifecycleOwner) { users ->
            addUsersToAdapter(users)
        }
    }

    private fun observeCounter() {
        viewModel.liveUsersCounter.observe(viewLifecycleOwner) { count ->
            changeExclusionUsersCount(count)
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
