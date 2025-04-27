package com.numplates.nomera3.presentation.view.fragments.notificationsettings.message

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments.BaseSettingsUserListFragment
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


class MessageNotificationsUsersFragment : BaseSettingsUserListFragment() {

    private val viewModel by viewModels<MessageNotificationsUsersViewModel> { App.component.getViewModelFactory() }


    override fun screenConfiguration() = BaseSettingsUserListConfiguration(
            getString(R.string.settings_exclude),
            true,
            getString(R.string.settings_add_exclusion)
    )


    override fun transitToAddUsersFragment() {
        add(MessageNotificationsAddUsersFragment(), Act.LIGHT_STATUSBAR)
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
        // Observe users
        viewModel.liveUsers.observe(viewLifecycleOwner, Observer { users ->
            addUsersToAdapter(users)
        })
        // Observe counter
        viewModel.liveUsersCounter.observe(viewLifecycleOwner, Observer { count ->
            changeExclusionUsersCount(count)
        })
        // Observe progress
        viewModel.liveProgress.observe(viewLifecycleOwner, Observer { isShowProgress ->
            loadProgressVisibility(isShowProgress)
        })
        // Observe view events
        viewModel.liveViewEvent
            .flowWithLifecycle(lifecycle)
            .onEach { handleEvents(it) }
            .launchIn(lifecycleScope)
    }
}
