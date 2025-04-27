package com.numplates.nomera3.presentation.view.fragments.privacysettings.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.data.dbmodel.UserWrapperWithCounter
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.usersettings.domain.usecase.GetSettingsUseCase
import com.numplates.nomera3.presentation.viewmodel.BaseViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.Event
import com.numplates.nomera3.presentation.viewmodel.viewevents.ListUsersSearchViewEvent
import com.numplates.nomera3.presentation.viewmodel.viewevents.Status
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class BaseSettingsUserListViewModel(private val getSettingsUseCase: GetSettingsUseCase) : BaseViewModel() {

    private val _liveUsers = MutableLiveData<List<UserSimple>>()
    val liveUsers = _liveUsers as LiveData<List<UserSimple>>

    private val _liveProgress = MutableLiveData(false)
    val liveProgress = _liveProgress as LiveData<Boolean>

    private val _liveViewEvent = MutableSharedFlow<ListUsersSearchViewEvent>()
    val liveViewEvent = _liveViewEvent as Flow<ListUsersSearchViewEvent>

    private val _liveUsersCounter = MutableLiveData<Long>()
    val liveUsersCounter = _liveUsersCounter as LiveData<Long>

    var isLastPage = false
    var isLoading = false

    abstract suspend fun getListUsersRequest(
        limit: Int,
        offset: Int
    ): ResponseWrapper<UserWrapperWithCounter<UserSimple>>

    abstract suspend fun addUsersRequest(userIds: List<Long>)

    abstract suspend fun deleteUsersRequest(userIds: List<Long>)


    fun getUsers(limit: Int, offset: Int) {
        requestWithCallback({
            getListUsersRequest(limit, offset)
        }, { event ->
            handleListUsersResponse(event)
        })
    }

    private fun handleListUsersResponse(event: Event<UserWrapperWithCounter<UserSimple>>) {
        isLoading = true
        when (event.status) {
            Status.LOADING -> {
                _liveProgress.postValue(true)
            }
            Status.SUCCESS -> {
                _liveProgress.postValue(false)
                event.data?.users?.let { users ->
                    isLoading = false
                    isLastPage = users.isEmpty()

                    _liveUsers.postValue(users)
                }

                // Handle count
                event.data?.count?.let { count ->
                    _liveUsersCounter.value = count
                }
            }
            Status.ERROR -> {
                _liveProgress.postValue(false)
                viewModelScope.launch {
                    _liveViewEvent.emit(ListUsersSearchViewEvent.OnErrorLoadUsers)
                }
            }
        }
    }

    fun addUsers(userIds: List<Long>) {
        viewModelScope.launch {
            runCatching {
                addUsersRequest(userIds)
            }.onFailure {
                Timber.e("FAIL DELETE Users")
            }
        }
    }


    fun deleteUser(userIds: List<Long>, adapterPosition: Int) {
        viewModelScope.launch {
            _liveProgress.postValue(true)
            runCatching {
                deleteUsersRequest(userIds)
                getSettingsUseCase.invoke()
            }.onSuccess {
                viewModelScope.launch {
                    _liveViewEvent.emit(ListUsersSearchViewEvent.OnSuccessRemoveUser(adapterPosition))
                }
            }.onFailure {
                _liveProgress.postValue(false)
                viewModelScope.launch {
                    _liveViewEvent.emit(ListUsersSearchViewEvent.OnFailureRemoveUser)
                }
            }
        }
    }

    /**
     * Для удаления отправлем список UID, но в данном случае
     * для удаления всех юзеров можно передать пустой список.
     * Это сделано, потому-что юзеров может быть много (пагинация)
     */
    fun deleteAllUsers() {
        viewModelScope.launch {
            _liveProgress.postValue(true)
            runCatching {
                deleteUsersRequest(mutableListOf())
                getSettingsUseCase.invoke()
            }.onSuccess {
                viewModelScope.launch {
                    _liveViewEvent.emit(ListUsersSearchViewEvent.OnSuccessRemoveAllUsers)
                }
            }.onFailure {
                _liveProgress.postValue(false)
                viewModelScope.launch {
                    _liveViewEvent.emit(ListUsersSearchViewEvent.OnErrorRemoveAllUsers)
                }
            }
        }
    }
}
