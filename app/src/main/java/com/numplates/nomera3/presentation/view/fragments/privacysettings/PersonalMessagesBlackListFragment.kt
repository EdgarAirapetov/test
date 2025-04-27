package com.numplates.nomera3.presentation.view.fragments.privacysettings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments.BaseSettingsUserListFragment
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.PersonalMessagesBlackListViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class PersonalMessagesBlackListFragment : BaseSettingsUserListFragment() {

    private val viewModel by viewModels<PersonalMessagesBlackListViewModel> { App.component.getViewModelFactory() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.liveUsers.observe(viewLifecycleOwner) { users ->
            addUsersToAdapter(users)
        }

        viewModel.liveUsersCounter.observe(viewLifecycleOwner) { count ->
            changeExclusionUsersCount(count)
        }

        viewModel.liveProgress.observe(viewLifecycleOwner) { isShowProgress ->
            loadProgressVisibility(isShowProgress)
        }

        viewModel.liveViewEvent
            .flowWithLifecycle(lifecycle)
            .onEach { handleEvents(it) }
            .launchIn(lifecycleScope)
    }

    override fun screenConfiguration() = BaseSettingsUserListConfiguration(
        getString(R.string.settings_never_let),
            true,
            getString(R.string.settings_privacy_add_user) + "..."
    )

    override fun transitToAddUsersFragment() {
        add(PersonalMessageAddBlackListFragment(), Act.LIGHT_STATUSBAR)
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

    override fun lastPage() = viewModel.isLastPage

    override fun loading() = viewModel.isLoading

}
