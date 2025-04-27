package com.numplates.nomera3.presentation.viewmodel

import com.meera.core.preferences.AppSettings
import com.meera.db.models.userprofile.City
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.App
import com.numplates.nomera3.data.network.ListFriendsResponse
import com.numplates.nomera3.data.network.UserSearchByNameModel
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.domain.interactornew.GetFriendsUseCase
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.domain.interactornew.RemoveUserUseCase
import com.numplates.nomera3.domain.interactornew.SearchUserUseCase
import com.numplates.nomera3.domain.interactornew.SubscriptionUseCase
import com.numplates.nomera3.presentation.viewmodel.viewevents.SingleLiveEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class OutgoingFriendshipRequestListViewModel : BaseViewModel() {

    companion object {
        private const val GET_FRIENDS_LIMIT = 24
    }

    val requests = SingleLiveEvent<MutableList<UserSimple>>()
    val foundRequests = SingleLiveEvent<MutableList<UserSimple>>()

    val showRequestsView = SingleLiveEvent<Boolean>()
    val showSearchResultView = SingleLiveEvent<Boolean>()
    val showRequestsPlaceholderView = SingleLiveEvent<Boolean>()
    val showSearchResultPlaceholderView = SingleLiveEvent<Boolean>()

    val showFriendshipCancellationResultView = SingleLiveEvent<Pair<Boolean, UserSimple>>()
    val showOutgoingFriendshipRequestSubscribedCancellationDialog = SingleLiveEvent<UserSimple>()
    val showOutgoingFriendshipRequestUnSubscribedCancellationDialog = SingleLiveEvent<UserSimple>()

    private val foundRequestsBuffer = mutableListOf<UserSimple>()
    private val requestsBuffer = mutableListOf<UserSimple>()
    private val disposables = CompositeDisposable()

    private var lastSearchQuery = ""
    private var requestsEnd = false
    private var requestsLoading = true
    private var foundRequestsEnd = false
    private var foundRequestsLoading = false
    private var searchDisposable: Disposable? = null

    @Inject
    lateinit var removeUserUseCase: RemoveUserUseCase

    @Inject
    lateinit var subscriptionUseCase: SubscriptionUseCase

    @Inject
    lateinit var appSettings: AppSettings

    @Inject
    lateinit var searchUserUseCase: SearchUserUseCase

    @Inject
    lateinit var friendUseCase: GetFriendsUseCase

    @Inject
    lateinit var getUserUidUseCase: GetUserUidUseCase

    init {
        App.component.inject(this)
    }

    fun setRequestsVisible(isVisible: Boolean) {
        showRequestsView.value = isVisible
    }

    fun setRequestsPlaceholderVisible(isVisible: Boolean) {
        showRequestsPlaceholderView.value = isVisible
    }

    fun setSearchResultVisible(isVisible: Boolean) {
        showSearchResultView.value = isVisible
    }

    fun setSearchResultPlaceholderVisible(isVisible: Boolean) {
        showSearchResultPlaceholderView.value = isVisible
    }

    fun resetParams() {
        requestsBuffer.clear()
    }

    fun getOutgoingFriendRequestList(offset: Int) {
        appSettings.readUID().let { nonNullUserId ->
            runWithDispatcherIORaw(
                    coroutine = {
                        friendUseCase.getOutgoingFriends(nonNullUserId, GET_FRIENDS_LIMIT, offset)
                    },
                    onStart = {
                        requestsLoading = true
                    },
                    onSuccess = { response: ResponseWrapper<ListFriendsResponse> ->
                        val newOutgoingRequests: MutableList<UserSimple> = response
                            .data
                                ?.friends
                                ?.filterNotNull()
                                ?.toMutableList()
                                ?: mutableListOf()

                        // флаг для пагинатора - больше нет результатов
                        requestsEnd = newOutgoingRequests.isEmpty()

                        // флаг для пагинатора - конец загрузки
                        requestsLoading = false

                        // буффер для использования внутри вью модели
                        requestsBuffer.addAll(newOutgoingRequests)

                        // отправляем во вью на отображение
                        requests.value = newOutgoingRequests

                        // показать список исходящих запросов
                        showRequestsView.value = requestsBuffer.isNotEmpty()

                        // показать плейсхолдер для списка исходящих запросов
                        showRequestsPlaceholderView.value = requestsBuffer.isEmpty()

                        // показать список найденных исходящих запросов
                        showSearchResultView.value = false

                        // показать плейсхолдер для списка найденных исходящих запросов
                        showSearchResultView.value = false
                    },
                    onError = {
                        requestsLoading = false
                        Timber.e(it)
                    }
            )
        }
    }

    fun clearLastQuery() {
        lastSearchQuery = ""
    }

    fun getLastQuery(): String = lastSearchQuery

    fun isRequestsEnd(): Boolean = requestsEnd

    fun isRequestsLoading(): Boolean = requestsLoading

    fun isFoundRequestsEnd(): Boolean = foundRequestsEnd

    fun isFoundRequestsLoading(): Boolean = foundRequestsLoading

    fun search(queryString: String, offset: Int) {
            searchDisposable?.dispose()

            if (queryString != lastSearchQuery) {
                foundRequestsBuffer.clear()
            }

            lastSearchQuery = queryString

            searchDisposable = searchUserUseCase
                    .searchOutgoingRequestedFriends(queryString, offset = offset)
                    .subscribeOn(Schedulers.io())
                    .doOnSubscribe { foundRequestsLoading = true }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ response ->
                        foundRequestsLoading = false
                        response?.data?.let { newFoundRequests: List<UserSearchByNameModel> ->
                            val mappedNewFoundRequests = newFoundRequests
                                    .mapNotNull { account: UserSearchByNameModel ->
                                        UserSimple(
                                            account.userId.toLong(),
                                            account.avatar,
                                            account.name,
                                            account.uniqueName,
                                            account.accountColor,
                                            account.accountType,
                                            account.birthday,
                                                City(null, account.cityName),
                                            account.approved
                                        )
                                    }
                                    .toMutableList()

                            foundRequests.value = mappedNewFoundRequests
                            foundRequestsBuffer.addAll(mappedNewFoundRequests)

                            showSearchResultPlaceholderView.value = foundRequestsBuffer.isEmpty()
                            foundRequestsEnd = mappedNewFoundRequests.size == 0
                        } ?: kotlin.run {
                            foundRequestsEnd = true
                        }
                    }, {
                        foundRequestsEnd = true
                        foundRequestsLoading = false
                    })
    }

    fun openCancelOutGoingRequestDialog(userSimple: UserSimple) {
        val isSubscribedOn: Boolean = userSimple
            .settingsFlags
                ?.subscription_on
                ?.let { it == 1 }
                ?: false

        if (isSubscribedOn) {
            openCancelDialogForSubscribedUser(userSimple)
        } else {
            openCancelDialogForUnsubscribedUser(userSimple)
        }
    }

    fun cancelOutgoingFriendshipRequest(userSimple: UserSimple?) {
        userSimple?.userId?.let {
            removeUserUseCase
                    .removeUserAndSaveSubscription(it)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        showFriendshipCancellationResultView.value = Pair(it?.data
                                ?: false, userSimple)
                    }, {
                        Timber.e(it)
                    }).let { disposable ->
                        disposables.add(disposable)
                    }
        }
    }

    fun cancelOutgoingFriendshipRequestAndUnsubscribe(userSimple: UserSimple?) {
        userSimple?.userId?.let {
            removeUserUseCase
                    .removeUser(it)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        showFriendshipCancellationResultView.value = Pair(it?.data
                                ?: false, userSimple)
                    }, {
                        Timber.e(it)
                    }).let { disposable ->
                        disposables.add(disposable)
                    }
        }
    }

    private fun openCancelDialogForSubscribedUser(userSimple: UserSimple) {
        showOutgoingFriendshipRequestSubscribedCancellationDialog.value = userSimple
    }

    private fun openCancelDialogForUnsubscribedUser(userSimple: UserSimple) {
        showOutgoingFriendshipRequestUnSubscribedCancellationDialog.value = userSimple
    }

    fun resetSearch() {
        clearLastQuery()
        showSearchResultPlaceholderView.value = false
        showSearchResultView.value = true
        foundRequestsBuffer.clear()

        foundRequests.value = mutableListOf()
    }

    override fun onCleared() {
        super.onCleared()

        disposables.dispose()
        searchDisposable?.dispose()
    }
}
