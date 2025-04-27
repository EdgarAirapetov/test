package com.numplates.nomera3.presentation.view.fragments.privacysettings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.helper.amplitude.domain.mapvisibilitysettings.model.MapVisibilitySettingsListType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.domain.mapvisibilitysettings.usecase.MapVisibilityBlacklistChangeCountParams
import com.numplates.nomera3.modules.baseCore.helper.amplitude.domain.mapvisibilitysettings.usecase.MapVisibilityBlacklistInitParams
import com.numplates.nomera3.modules.baseCore.helper.amplitude.domain.mapvisibilitysettings.usecase.MapVisibilityBlacklistLogDataParams
import com.numplates.nomera3.modules.maps.ui.entity.MapVisibilitySettingsOrigin
import com.numplates.nomera3.presentation.view.callback.IOnBackPressed
import com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments.BaseSettingsUserListFragment
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.MapSettingsBlacklistViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.ListUsersSearchViewEvent
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


class MapSettingsBlacklistFragment : BaseSettingsUserListFragment(), IOnBackPressed {

    private val viewModel by viewModels<MapSettingsBlacklistViewModel> { App.component.getViewModelFactory() }

    override fun screenConfiguration() = BaseSettingsUserListConfiguration(
            getString(R.string.settings_never_let),
            true,
            getString(R.string.settings_privacy_add_user) + "..."
    )

    override fun transitToAddUsersFragment() {
        add(MapSettingsAddBlackListFragment(), Act.LIGHT_STATUSBAR)
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


    /** Setup logging logic in observers before parent's logic to handle count change before exit **/
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val origin = arguments?.getSerializable(MapVisibilitySettingsOrigin.ARG) as? MapVisibilitySettingsOrigin
        viewModel.mapVisibilityAnalyticsSettingsInitUseCase.execute(
            MapVisibilityBlacklistInitParams(origin, MapVisibilitySettingsListType.BLACKLIST)
        )

        // Observe users
        viewModel.liveUsers.observe(viewLifecycleOwner, Observer { users ->
            addUsersToAdapter(users)
        })
        // Observe counter
        viewModel.liveUsersCounter.observe(viewLifecycleOwner, Observer { count ->
            viewModel.mapVisibilitySettingsAnalyticsChangeCountUseCase.execute(
                MapVisibilityBlacklistChangeCountParams(setCount = count.toInt())
            )
            changeExclusionUsersCount(count)
        })
        // Observe progress
        viewModel.liveProgress.observe(viewLifecycleOwner, Observer { isShowProgress ->
            loadProgressVisibility(isShowProgress)
        })
        // Observe view events
        viewModel.liveViewEvent
            .flowWithLifecycle(lifecycle)
            .onEach { event ->
                when (event) {
                    ListUsersSearchViewEvent.OnSuccessRemoveAllUsers -> {
                        viewModel.mapVisibilitySettingsAnalyticsLogDataUseCase.execute(
                            MapVisibilityBlacklistLogDataParams(true)
                        )
                    }
                    is ListUsersSearchViewEvent.OnSuccessRemoveUser -> {
                        viewModel.mapVisibilitySettingsAnalyticsChangeCountUseCase.execute(
                            MapVisibilityBlacklistChangeCountParams(removeCount = 1)
                        )
                    }
                    else -> Unit
                }
                handleEvents(event)
            }
            .launchIn(lifecycleScope)
    }

    override fun delayUpdateOnReturnTransition(): Boolean = false

    override fun onBackPressed(): Boolean {
        viewModel.mapVisibilitySettingsAnalyticsLogDataUseCase.execute(
            MapVisibilityBlacklistLogDataParams(false)
        )
        return false
    }
}
