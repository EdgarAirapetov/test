package com.numplates.nomera3.presentation.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.meera.core.extensions.empty
import com.meera.core.network.websocket.WebSocketMainChannel
import com.meera.core.preferences.AppSettings
import com.meera.core.preferences.toJson
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.App
import com.numplates.nomera3.data.dbmodel.UsersWrapper
import com.numplates.nomera3.domain.interactornew.CallUserSettingsUseCase
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.presentation.view.adapter.ListUsersAdapter
import com.numplates.nomera3.presentation.view.fragments.CallListFriendsFragment.Companion.LIST_PAGE_LIMIT
import com.numplates.nomera3.presentation.view.fragments.CallListFriendsFragment.Companion.USER_LIST_TYPE_BLACKLIST
import com.numplates.nomera3.presentation.view.fragments.CallListFriendsFragment.Companion.USER_LIST_TYPE_WHITELIST
import com.numplates.nomera3.presentation.viewmodel.viewevents.ChatGroupViewEvent
import com.numplates.nomera3.presentation.viewmodel.viewevents.Event
import com.numplates.nomera3.presentation.viewmodel.viewevents.Status
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class CallListUsersViewModel : BaseViewModel(), ListUsersAdapter.OnUserActionListener {

    val disposables = CompositeDisposable()

    @Inject
    lateinit var appSettings: AppSettings

    @Inject
    lateinit var userSettingsUseCase: CallUserSettingsUseCase

    @Inject
    lateinit var webSocketMain: WebSocketMainChannel

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var amplitudeHelper: AnalyticsInteractor

    @Inject
    lateinit var getUserUidUseCase: GetUserUidUseCase


    lateinit var adapter: ListUsersAdapter

    val liveUsers = MutableLiveData<List<UserSimple>>()
    val liveProgress = MutableLiveData<Boolean>()
    val liveViewEvents = MutableLiveData<ChatGroupViewEvent>()

    var userListType: Int? = null

    var isLastPage = false
    var isLoading = false

    private var oldSearchQueryName: String = String.empty()


    fun init(userListType: Int?) {
        App.component.inject(this)
        adapter = ListUsersAdapter(mutableListOf(), userListType, this)
        this.userListType = userListType
        // Important! Default init adapter data
        liveUsers.value = mutableListOf()
    }

    private var countBlacklist = 0
    private var countWhitelist = 0

    fun observeUserCounters() {
        viewModelScope.launch {
            runCatching {
                appSettings.callUserCounter.asFlow().collect {
                    val counters = gson.fromJson(it, AppSettings.UserCallCounters::class.java)
                    countBlacklist = counters?.counterBlacklist!!
                    countWhitelist = counters.counterWhitelist
                }
            }.onFailure { Timber.e(it) }
        }
    }

    private fun incrementBlacklistCounters() {
        viewModelScope.launch {
            val count = countBlacklist + 1
            appSettings.callUserCounter.set(AppSettings.UserCallCounters(count, countWhitelist).toJson())
        }
    }

    private fun incrementWhitelistCounters() {
        viewModelScope.launch {
            val count = countWhitelist + 1
            appSettings.callUserCounter.set(AppSettings.UserCallCounters(countBlacklist, count).toJson())
        }
    }

    private fun decrementBlacklistCounters() {
        viewModelScope.launch {
            val count = countBlacklist - 1
            appSettings.callUserCounter.set(AppSettings.UserCallCounters(count, countWhitelist).toJson())
        }
    }

    private fun decrementWhitelistCounters() {
        viewModelScope.launch {
            val count = countWhitelist - 1
            appSettings.callUserCounter.set(AppSettings.UserCallCounters(countBlacklist, count).toJson())
        }
    }


    fun clearList() {
        adapter.clearList()
    }


    fun searchUsersBlackList(name: String, limit: Int, offset: Int, isShowProgress: Boolean = true) {
        requestWithCallback({
            userSettingsUseCase.searchCallUsersBlackList(name, limit, offset)
        }, { event ->
            handleSearchUserResults(name, event, isShowProgress)
        })
    }


    fun searchUsersWhiteList(name: String, limit: Int, offset: Int, isShowProgress: Boolean = true) {
        requestWithCallback({
            userSettingsUseCase.searchCallUsersWhiteList(name, limit, offset)
        }, { event ->
            handleSearchUserResults(name, event, isShowProgress)
        })
    }


    private fun handleSearchUserResults(name: String, event: Event<UsersWrapper<UserSimple>?>, isShowProgress: Boolean) {
        isLoading = true
        when (event.status) {
            Status.LOADING -> if (isShowProgress) { liveProgress.postValue(true) }
            Status.SUCCESS -> {
                liveProgress.postValue(false)

                event.data?.users?.let { users ->
                    // Every original request (if only text changed) adapter should be clear
                    if (name != oldSearchQueryName) {
                        adapter.clearList()
                    }
                    oldSearchQueryName = name

                    isLoading = false
                    isLastPage = users.isEmpty()
                    liveUsers.postValue(users)
                }
            }
            Status.ERROR -> {
                liveProgress.postValue(false)
                liveViewEvents.postValue(ChatGroupViewEvent.ErrorLoadFriendList)
            }
        }
    }


    override fun onUserItemSelect(user: UserSimple) {
        liveViewEvents.value = ChatGroupViewEvent.OnUserAvatarClicked(user)
    }


    override fun onUserChecked(user: UserSimple, position: Int, isChecked: Boolean) {
        Timber.e("USER Checked: ${user.name}  Position:$position isChecked: $isChecked")
        val uId = user.userId
        when (userListType) {
            USER_LIST_TYPE_BLACKLIST -> {
                if (isChecked) {
                    addUserToBlackList(uId, {
                        incrementBlacklistCounters()
                    }, {
                        showErrorSaveSetting()
                        adapter.disableCheckbox(position)
                    })
                } else {
                    deleteUserFromBlackList(uId, {
                        decrementBlacklistCounters()
                        adapter.deleteItem(position)
                    }, {
                        showErrorSaveSetting()
                        adapter.enableCheckbox(position)
                    })
                }
            }
            USER_LIST_TYPE_WHITELIST -> {
                if (isChecked) {
                    addUserToWhiteList(uId, {
                        incrementWhitelistCounters()
                    }, {
                        showErrorSaveSetting()
                        adapter.disableCheckbox(position)
                    })
                } else {
                    deleteUserFromWhiteList(uId, {
                        decrementWhitelistCounters()
                        adapter.deleteItem(position)
                    }, {
                        showErrorSaveSetting()
                        adapter.enableCheckbox(position)
                    })
                }
            }
        }
    }


    fun getUsersBlackList(offset: Int = 0, isShowProgress: Boolean = true) {
        requestWithCallback({
            userSettingsUseCase.getCallUsersBlackList(LIST_PAGE_LIMIT, offset)
        }, { event ->
            handleGetUsersRequest(event, isShowProgress)
        })
    }


    fun getUsersWhitelist(offset: Int = 0, isShowProgress: Boolean = true) {
        requestWithCallback({
            userSettingsUseCase.getCallUsersWhiteList(LIST_PAGE_LIMIT, offset)
        }, { event ->
            handleGetUsersRequest(event, isShowProgress)
        })
    }


    private fun handleGetUsersRequest(event: Event<UsersWrapper<UserSimple>?>, isShowProgress: Boolean) {
        isLoading = true
        when (event.status) {
            Status.LOADING -> if (isShowProgress) { liveProgress.postValue(true) }
            Status.SUCCESS -> {
                event.data?.users?.let { users ->
                    isLoading = false
                    isLastPage = users.isEmpty()
                    liveProgress.postValue(false)
                    liveUsers.postValue(users)
                }
            }
            Status.ERROR -> {
                liveProgress.postValue(false)
                liveViewEvents.postValue(ChatGroupViewEvent.ErrorLoadFriendList)
            }
        }
    }


    private fun addUserToBlackList(userId: Long, success: () -> Unit, error: () -> Unit) {
        requestWithCallback({
            userSettingsUseCase.addCallUserBlackList(mutableListOf(userId))
        }, { event ->
            when (event.status) {
                Status.SUCCESS -> { success.invoke();  Timber.e("Success add user to BLACKLIST") }
                Status.ERROR -> { error.invoke(); Timber.e("Error add user BLACKLIST") }
                else -> {}
            }
        })
    }


    private fun addUserToWhiteList(userId: Long, success: () -> Unit, error: () -> Unit) {
        requestWithCallback({
            userSettingsUseCase.addCallUserWhiteList(mutableListOf(userId))
        }, { event ->
            when (event.status) {
                Status.SUCCESS -> { success.invoke(); Timber.e("Success add user to WHITELIST") }
                Status.ERROR -> { error.invoke(); Timber.e("Error add user to WHITELIST") }
                else -> {}
            }
        })
    }


    private fun deleteUserFromBlackList(userId: Long, success: () -> Unit, error: () -> Unit) {
        requestWithCallback({
            userSettingsUseCase.deleteCallUserBlackList(mutableListOf(userId))
        }, { event ->
            when (event.status) {
                Status.SUCCESS -> {
                    success.invoke(); Timber.e("Success delete user from BLACKLIST")
                }
                Status.ERROR -> {
                    error.invoke(); Timber.e("Error delete user from BLACKLIST")
                }
                else -> {}
            }
        })
    }


    private fun deleteUserFromWhiteList(userId: Long, success: () -> Unit, error: () -> Unit) {
        requestWithCallback({
            userSettingsUseCase.deleteCallUserWhiteList(mutableListOf(userId))
        }, { event ->
            when (event.status) {
                Status.SUCCESS -> { success.invoke(); Timber.e("Success delete user from WHITELIST") }
                Status.ERROR -> { error.invoke(); Timber.e("Error delete user from WHITELIST") }
                else -> {}
            }
        })
    }


    private fun showErrorSaveSetting() {
        liveViewEvents.postValue(ChatGroupViewEvent.ErrorSaveSetting)
    }


    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }


}
