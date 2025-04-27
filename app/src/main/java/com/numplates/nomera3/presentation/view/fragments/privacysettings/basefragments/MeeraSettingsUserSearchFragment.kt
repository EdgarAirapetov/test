package com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.meera.core.extensions.gone
import com.meera.core.extensions.safeNavigate
import com.meera.core.extensions.visible
import com.meera.core.utils.showCommonError
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.BaseSettingsUserSearchViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.ListUsersSearchViewEvent


const val DEFAULT_SETTINGS_CONFIGURE = "SETTINGS"

/**
 * Implement Base user search in settings
 */
abstract class MeeraSettingsUserSearchFragment : MeeraBaseSettingsUserSearchFragment() {

    private var viewModel: BaseSettingsUserSearchViewModel? = null

    abstract fun getViewModel(): BaseSettingsUserSearchViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = getViewModel()
        initLiveObservables()
    }

    override fun screenConfiguration() = BaseSettingsUserSearchConfiguration(
        DEFAULT_SETTINGS_CONFIGURE
    )

    override fun onUsersSelectedDone(users: List<UserSimple>) {
        viewModel?.saveResultCheckedUsersNetwork(users)
    }

    override fun searchUsers(text: String, offset: Int) {
        viewModel?.searchUsers(text, BASE_LIST_USERS_PAGE_SIZE, offset)
    }

    override fun showLoadedUsers(offset: Int) {
        viewModel?.getExcludedUsers(BASE_LIST_USERS_PAGE_SIZE, offset)
    }

    override fun lastPage(): Boolean = viewModel?.isLastPage ?: false

    override fun loading(): Boolean = viewModel?.isLoading ?: false

    override fun confirmButtonClickResult(isConfirmed: Boolean) {
        viewModel?.changeStateConfirmButtonClick(isConfirmed)
    }

    override fun sendNotificationClosedFragment() {
        viewModel?.sendNotificationClosingFragment(parentFragmentManager)
    }

    override fun userAvatarClick(user: UserSimple) {
        findNavController().safeNavigate(
            resId = R.id.action_meeraBaseSettingsUserSearchFragment_to_userInfoFragment,
            bundle = bundleOf(
                IArgContainer.ARG_USER_ID to user.userId,
                IArgContainer.ARG_TRANSIT_FROM to AmplitudePropertyWhere.SEARCH.property
            )
        )
    }

    override fun searchModeObservable(isSearchMode: Boolean) {
            if (config?.isShowEmptyResultPlaceholder == true && listAdapter.currentList.size == 0) {
                binding.tvDescriptionEmptyState.text = getString(R.string.meera_settings_empty_state)
                binding.vMomentsSettingsEmptyStateGroup.visible()
            }
    }

    private fun initLiveObservables() {
        viewModel?.liveSearchUsers?.observe(viewLifecycleOwner) { users ->
            updateDataSet(users.toMutableList())
            shimmerVisibility(false)
            handleEmptyListViewHolder(users.size)
        }

        viewModel?.liveViewEvent?.observe(viewLifecycleOwner) { event ->
            handleViewEvents(event)
        }
    }

    /**
     * Show / Hide - empty list placeholder id config enable
     */
    private fun handleEmptyListViewHolder(itemSize: Int) {
        if (config?.isShowEmptyResultPlaceholder == true) {
            if (itemSize != 0 || listAdapter.currentList.size > 0) {
                binding.vMomentsSettingsEmptyStateGroup.gone()
            } else {
                binding.tvDescriptionEmptyState.text = getString(R.string.meera_settings_empty_state)
                binding.vMomentsSettingsEmptyStateGroup.visible()
            }
        }
    }

    private fun handleViewEvents(event: ListUsersSearchViewEvent) {
        when (event) {
            is ListUsersSearchViewEvent.OnAddUsersDone -> findNavController().popBackStack()
            is ListUsersSearchViewEvent.OnErrorLoadUsers, ListUsersSearchViewEvent.OnErrorAddUsers -> {
                showCommonError(getText(R.string.error_try_later), requireView())
            }

            else -> Unit
        }
    }
}
