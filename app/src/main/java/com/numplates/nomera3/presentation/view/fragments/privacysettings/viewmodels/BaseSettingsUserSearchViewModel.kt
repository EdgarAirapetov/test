package com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels

import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.meera.core.extensions.empty
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.data.dbmodel.UsersWrapper
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments.ARG_CHANGE_LIST_USER_KEY
import com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments.ARG_CHANGE_LIST_USER_REQUEST_KEY
import com.numplates.nomera3.presentation.view.fragments.privacysettings.basefragments.BaseSettingsUserSearchFragment.Companion.BASE_LIST_USERS_PAGE_SIZE
import com.numplates.nomera3.presentation.viewmodel.BaseViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.Event
import com.numplates.nomera3.presentation.viewmodel.viewevents.ListUsersSearchViewEvent
import com.numplates.nomera3.presentation.viewmodel.viewevents.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Base view model business logic search and show found users
 */
abstract class BaseSettingsUserSearchViewModel : BaseViewModel() {

    val liveSearchUsers = MutableLiveData<List<UserSimple>>()

    val liveViewEvent = MutableLiveData<ListUsersSearchViewEvent>()

    val liveProgress = MutableLiveData(false)

    var isLastPage = false
    var isLoading = false
    private var isConfirmed = false

    abstract suspend fun getNonSearchUsersModeRequest(text: String, limit: Int, offset: Int): ResponseWrapper<UsersWrapper<UserSimple>>

    abstract suspend fun getSearchUsersModeRequest(text: String, limit: Int, offset: Int): ResponseWrapper<UsersWrapper<UserSimple>>

    abstract suspend fun addUsersRequest(userIds: List<Long>)

    abstract suspend fun deleteUsersRequest(userIds: List<Long>)

    /**
     * Show all exclusion users
     */
    fun getExcludedUsers(limit: Int, offset: Int) {
        requestWithCallback({
            getNonSearchUsersModeRequest(String.empty(), limit, offset)
        }, { event ->
            val isPaginationRequest = offset > BASE_LIST_USERS_PAGE_SIZE
            handleRequestGetUsers(event, isPaginationRequest)
        })
    }

    /**
     * Search user request
     */
    fun searchUsers(name: String, limit: Int, offset: Int) {
        requestWithCallback({
            getSearchUsersModeRequest(name, limit, offset)
        }, { event ->
            val isPaginationRequest = offset >= BASE_LIST_USERS_PAGE_SIZE
            handleRequestSearchUsers(event, isPaginationRequest = isPaginationRequest)
        })
    }

    fun sendNotificationClosingFragment(fragmentManager: FragmentManager){
        fragmentManager.setFragmentResult(
            ARG_CHANGE_LIST_USER_REQUEST_KEY, bundleOf(
                ARG_CHANGE_LIST_USER_KEY to isConfirmed)
        )
    }

    fun changeStateConfirmButtonClick(isConfirmed: Boolean){
        this.isConfirmed = isConfirmed
    }

    /**
     * Add users to exclusion list
     */
    private fun addUsers(userIds: List<Long>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                addUsersRequest(userIds)
                liveViewEvent.postValue(ListUsersSearchViewEvent.OnAddUsersDone(checkedCount = userIds.size))
            } catch (e: Exception) {
                liveViewEvent.postValue(ListUsersSearchViewEvent.OnErrorAddUsers)
                Timber.e(e)
            }
        }
    }

    /**
     * Show Already chosen users
     */
    private fun handleRequestGetUsers(event: Event<UsersWrapper<UserSimple>?>, isPaginationRequest: Boolean = false) {
        isLoading = true
        when (event.status) {
            Status.LOADING -> if (!isPaginationRequest) {
                liveProgress.postValue(true)
            }
            Status.SUCCESS -> {
                liveProgress.postValue(false)
                event.data?.users?.let { users ->
                    // TODO: Отключил - прочекать всех полученных юзеров потому что раньше в searchMode выводились ранее добавленные юзеры
                    /*users.forEach { user ->
                        user.isChecked = true
                    }*/

                    isLoading = false
                    isLastPage = users.isEmpty()
                    liveSearchUsers.postValue(users)
                }
            }
            Status.ERROR -> {
                liveProgress.postValue(false)
                liveViewEvent.postValue(ListUsersSearchViewEvent.OnErrorLoadUsers)
            }
        }
    }


    /**
     * Show user search results
     */
    private fun handleRequestSearchUsers(event: Event<UsersWrapper<UserSimple>?>, isPaginationRequest: Boolean = false) {
        isLoading = true
        when (event.status) {
            Status.LOADING -> if (!isPaginationRequest) {
                liveProgress.postValue(true)
            }
            Status.SUCCESS -> {
                liveProgress.postValue(false)
                event.data?.users?.let { users ->
                    if (!isPaginationRequest) {
                        // do nothing
                    }

                    isLoading = false
                    isLastPage = users.isEmpty()
                    liveSearchUsers.postValue(users)
                }
            }
            Status.ERROR -> {
                liveProgress.postValue(false)
                liveViewEvent.postValue(ListUsersSearchViewEvent.OnErrorLoadUsers)
            }
        }
    }


    fun saveResultCheckedUsersNetwork(users: List<UserSimple>) {
        val checkedIds = mutableListOf<Long>()
        val uncheckedIds = mutableListOf<Long>()
        users.forEach { user ->
            if (user.isChecked) {
                checkedIds.add(user.userId)
            } else
                uncheckedIds.add(user.userId)
        }

        // Добавляем только прочеканных юзеров
        if (checkedIds.size > 0) {
            addUsers(checkedIds)
        }
    }

}
