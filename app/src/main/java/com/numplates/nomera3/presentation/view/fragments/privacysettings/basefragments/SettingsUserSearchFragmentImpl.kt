package com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.numplates.nomera3.Act
import com.numplates.nomera3.R
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.presentation.router.Arg
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.view.fragments.UserInfoFragment
import com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels.BaseSettingsUserSearchViewModel
import com.numplates.nomera3.presentation.view.utils.NToast
import com.meera.core.extensions.visible
import com.meera.core.extensions.gone
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.presentation.viewmodel.viewevents.ListUsersSearchViewEvent
import timber.log.Timber

/**
 * Implement Base user search in settings
 */
abstract class SettingsUserSearchFragmentImpl : BaseSettingsUserSearchFragment() {

    private lateinit var viewModel: BaseSettingsUserSearchViewModel

    abstract fun getViewModel(): BaseSettingsUserSearchViewModel


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = getViewModel()
        initLiveObservables()

    }

    override fun screenConfiguration() = BaseSettingsUserSearchConfiguration(
            "SETTINGS"
    )

    override fun onUsersSelectedDone() {
        viewModel.saveResultCheckedUsersNetwork(listAdapter.getAllItems())
    }

    override fun searchUsers(text: String, offset: Int) {
        viewModel.searchUsers(text, BASE_LIST_USERS_PAGE_SIZE, offset)
    }

    override fun showLoadedUsers(offset: Int) {
        viewModel.getExcludedUsers(BASE_LIST_USERS_PAGE_SIZE, offset)
    }

    override fun lastPage(): Boolean = viewModel.isLastPage

    override fun loading(): Boolean = viewModel.isLoading


    override fun userAvatarClick(user: UserSimple) {
        //toast("User avatar Click!")
        add(
            UserInfoFragment(), Act.LIGHT_STATUSBAR,
            Arg(IArgContainer.ARG_USER_ID, user.userId),
            Arg(IArgContainer.ARG_TRANSIT_FROM, AmplitudePropertyWhere.SEARCH.property)
        )

    }

    override fun userChecked(user: UserSimple, isChecked: Boolean) {
        Timber.e("User check ID:${user.userId} Name:${user.name} (isChecked:$isChecked)")
        changeColorDoneButton(true)

        // nothing do
        /*if (isChecked) {
            viewModel.addToCheck(user)
        } else {
            viewModel.removeFromCheck(user)
        }*/
    }


    override fun searchModeObservable(isSearchMode: Boolean) {
        /** STUB - Not using */
        if (!isSearchMode) {
            // Show empty list placeholder id config enable
            if (config.isShowEmptyResultPlaceholder) {
                binding?.noSearchResultPlaceholder?.root?.visible()
            }
        }
    }


    private fun initLiveObservables() {
        // Show users from network
        viewModel.liveSearchUsers.observe(viewLifecycleOwner, Observer { users ->
            updateDataSet(users.toMutableList())
            handleEmptyListViewHolder(getAdapterItemCount())
            // Timber.e("Searched users:${users.size}: ITEMCount:${getAdapterItemCount()}")
        })

        // View events
        viewModel.liveViewEvent.observe(viewLifecycleOwner, Observer { event ->
            handleViewEvents(event)
        })

        // Loading progress
        viewModel.liveProgress.observe(viewLifecycleOwner, Observer { isShowProgress ->
            if (isShowProgress) {
                binding?.pbLoading?.visible()
            } else
                binding?.pbLoading?.gone()
        })
    }


    /**
     * Show / Hide - empty list placeholder id config enable
     */
    private fun handleEmptyListViewHolder(itemSize: Int){
        if (config.isShowEmptyResultPlaceholder) {
            if (itemSize != 0) {
                binding?.noSearchResultPlaceholder?.root?.gone()
            } else {
                binding?.noSearchResultPlaceholder?.root?.visible()
            }
        }
    }


    private fun handleViewEvents(event: ListUsersSearchViewEvent) {
        when (event) {
            is ListUsersSearchViewEvent.OnAddUsersDone -> act.onBackPressed()
            is ListUsersSearchViewEvent.OnErrorLoadUsers, ListUsersSearchViewEvent.OnErrorAddUsers -> {
                NToast.with(act)
                        .typeError()
                        .text(getString(R.string.error_try_later))
                        .show()
            }
            else -> Unit
        }
    }

}
