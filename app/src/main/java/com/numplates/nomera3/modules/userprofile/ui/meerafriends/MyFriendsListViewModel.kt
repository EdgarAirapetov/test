package com.numplates.nomera3.modules.userprofile.ui.meerafriends

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meera.core.network.websocket.WebSocketMainChannel
import com.meera.core.preferences.AppSettings
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.domain.interactornew.AddUserToFriendUseCaseNew
import com.numplates.nomera3.domain.interactornew.BlockUserUseCase
import com.numplates.nomera3.domain.interactornew.GetFriendsListUseCase
import com.numplates.nomera3.domain.interactornew.GetFriendsUseCase
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.domain.interactornew.RemoveUserUseCase
import com.numplates.nomera3.domain.interactornew.SearchUserUseCase
import com.numplates.nomera3.domain.interactornew.SubscribeUserUseCaseNew
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.baseCore.helper.amplitude.people.AmplitudePeopleAnalytics
import com.numplates.nomera3.modules.baseCore.helper.amplitude.people.AmplitudePeopleWhereProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.people.AmplitudePeopleWhich
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeFriendRequest
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeFriendRequestPropertyWhere
import com.numplates.nomera3.modules.tracker.ITrackerActions
import com.numplates.nomera3.modules.user.domain.usecase.PushFriendStatusChangedUseCase
import com.numplates.nomera3.presentation.view.adapter.newfriends.FriendModel
import com.numplates.nomera3.presentation.view.fragments.FriendsHostOpenedType
import com.numplates.nomera3.presentation.view.fragments.entity.UserSubscriptionViewEvent
import com.numplates.nomera3.presentation.viewmodel.viewevents.FriendsListViewEvents
import com.numplates.nomera3.presentation.viewmodel.viewevents.SingleLiveEvent
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

const val FRIEND_REQUEST_PAGE_LIMIT = 20

class MyFriendsListViewModel @Inject constructor(
    private val appSettings: AppSettings,
    private val getUserUidUseCase: GetUserUidUseCase,
    private val subscribeUserUseCase: SubscribeUserUseCaseNew,
    private val addUserToFriendUseCase: AddUserToFriendUseCaseNew,
    private val pushFriendStatusChangedUseCase: PushFriendStatusChangedUseCase,
    private val friendUseCase: GetFriendsListUseCase,
    private val getFriendsV2: GetFriendsUseCase,
    private val blockUserUseCase: BlockUserUseCase,
    private val webSocketMainChannel: WebSocketMainChannel,
    private val removeUserUseCase: RemoveUserUseCase,
    private val searchFriend: SearchUserUseCase,
    private val myTracker: ITrackerActions,
    private val amplitudeHelper: AnalyticsInteractor,
    private val amplitudeHelperNew: AmplitudeFriendRequest,
    private val amplitudePeopleAnalytics: AmplitudePeopleAnalytics
) : ViewModel() {

    private var mode: Int? = null
    private var userID: Long? = null
    private var isSearch = false
    private var searchDisposable: Disposable? = null

    private var friendsList = mutableListOf<FriendModel>()

    val liveAllFriends = MutableLiveData<List<FriendModel>>()
    val liveIncomingFriends = MutableLiveData<List<FriendModel>>()
    val liveBlackList = MutableLiveData<MutableList<FriendModel>>()
    val liveOutcomingFriends = MutableLiveData<MutableList<FriendModel>>()

    val liveRemoveItem = MutableLiveData<FriendModel>()
    val liveRemoveFriend = MutableLiveData<FriendModel>()

    val liveSearch = MutableLiveData<MutableList<FriendModel>>()
    val liveDeleteFriend = MutableLiveData<FriendModel>()
    val liveNewFriend = MutableLiveData<FriendModel>()
    val liveEvent = MutableLiveData<FriendsListViewEvents>()

    val showCancelOutcomeFriendshipRequestDialog = SingleLiveEvent<FriendModel>()
    val showCancelOutcomeFriendshipRequestUnSubscribeDialog = SingleLiveEvent<FriendModel>()

    private val disposables = CompositeDisposable()

    private val _viewEvent = MutableSharedFlow<UserSubscriptionViewEvent>()
    val viewEvent: SharedFlow<UserSubscriptionViewEvent> = _viewEvent


    fun init(userID: Long?, mode: Int) {
        App.component.inject(this)
        this.mode = mode
        this.userID = userID
        userID?.let {
            friendUseCase.setParams(userID, 1)
        }

        requestData()
    }

    fun getUserUid() = getUserUidUseCase.invoke()

    private fun requestData() {
        when (mode) {
            GetFriendsListUseCase.INCOMING -> {
                requestFriendIncomingList()
            }

            GetFriendsListUseCase.FRIENDS -> {
                requestAllFriends()
            }

            GetFriendsListUseCase.BLACKLIST -> {
                requestBlackList()
            }

            GetFriendsListUseCase.OUTCOMING -> {
                requestOutComing()
            }
        }
    }

    fun logMyFriendsPeopleSelected() {
        amplitudePeopleAnalytics.setPeopleSelected(
            where = AmplitudePeopleWhereProperty.FIND_FRIEND_BUTTON, which = AmplitudePeopleWhich.PEOPLE
        )
    }

    fun search(queryString: String) {
        Timber.d("запрос:  $queryString Length-string: ${queryString.length}")
        if (queryString.isNotEmpty()) {
            val res = mutableListOf<FriendModel>()
            for (friend in friendsList) {
                val pattern = queryString.toRegex()
                val found = pattern.findAll(friend.userModel.name.toLowerCase())
                var foundUniqueName = 0
                friend.userModel.uniqueName?.toLowerCase()?.let {
                    val uniqueNamePattern = queryString.replace("@", "").toRegex()
                    foundUniqueName = uniqueNamePattern.findAll(it).count()
                }
                Timber.d(friend.userModel.name)

                if (found.count() > 0 || foundUniqueName > 0) {
                    res.add(friend)
                }

            }
            liveSearch.value = res
        } else {
            // Clear list and show no results placeholder
            liveSearch.value = mutableListOf()
        }
    }

    fun searchFriendNetwork(queryString: String, limit: Int = 1000, offset: Int = 0) {
        Timber.d("запрос:  $queryString Length-string: ${queryString.length}")
        searchDisposable?.dispose()
        searchDisposable = searchFriend.searchFriendUserSimple(queryString, limit, offset)?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())?.subscribe({
                val res = mutableListOf<FriendModel>()
                it?.data?.forEach { user ->
                    user?.let { notNullUser ->
                        res.add(FriendModel(notNullUser, GetFriendsListUseCase.FRIENDS))
                    }
                }
                liveSearch.value = res
            }, {
                Timber.e(it)
                liveSearch.value = mutableListOf()
            })
    }

    fun removeFriendSaveSubscriptionById(userId: Long) {
        val friend = friendsList.firstOrNull { it.userModel.userId == userId }
        removeFriendSaveSubscription(friend)
    }

    fun removeFriendSaveSubscription(friend: FriendModel?) {
        friend?.userModel?.userId?.let { id ->
            removeUserUseCase.removeUserAndSaveSubscription(id).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe({ response: ResponseWrapper<Boolean>? ->
                    val isRemoved = response?.data ?: false
                    amplitudeHelper.logDelFriend(appSettings.readUID(), id)

                    if (isRemoved) {
                        friendsList.remove(friend)
                        liveAllFriends.value = friendsList
                        liveDeleteFriend.value = friend
                        liveEvent.value = FriendsListViewEvents.OnFriendRejected
                    }
                }, {
                    liveEvent.value = FriendsListViewEvents.OnErrorRemoveFriend
                    it.printStackTrace()
                }).let { disposable ->
                    disposables.add(disposable)
                }
        }
    }

    fun removeFriend(friend: FriendModel?) {
        friend?.userModel?.userId?.let { id ->
            disposables.add(
                removeUserUseCase.removeUser(id).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ response: ResponseWrapper<Boolean>? ->
                        val isRemoved = response?.data ?: false
                        amplitudeHelper.logDelFriend(appSettings.readUID(), id)
                        if (isRemoved) {
                            friendsList.remove(friend)
                            liveAllFriends.value = friendsList
                            liveDeleteFriend.value = friend
                            liveEvent.value = FriendsListViewEvents.OnFriendRejected
                        } else {

                        }
                    }, {
                        liveEvent.value = FriendsListViewEvents.OnErrorRemoveFriend
                        it.printStackTrace()
                    })
            )
        }
    }

    fun removeFriendById(userId: Long) {
        val friend = friendsList.firstOrNull { it.userModel.userId == userId }
        removeFriend(friend)
    }

    private fun deleteFriendSocket(
        friend: FriendModel, isRejected: Boolean = false, openedType: FriendsHostOpenedType
    ) {
        val payload = hashMapOf<String, Any>(
            "id" to friend.userModel.userId
        )
        val d = webSocketMainChannel.pushRemoveFriends(payload).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe({
                amplitudeHelper.logDelFriend(appSettings.readUID(), friend.userModel.userId)
                liveDeleteFriend.value = friend
                if (isRejected) {
                    onPushFriendRequest(
                        fromId = appSettings.readUID(),
                        toId = friend.userModel.userId,
                        isRejected = true,
                        openedType = openedType
                    )
                    liveEvent.value = FriendsListViewEvents.OnFriendRejected
                }
            }, {
                Timber.e(it)
                liveEvent.value = FriendsListViewEvents.OnErrorRemoveFriend
            })
        disposables.add(d)
    }

    private fun confirmFriendSocket(
        friend: FriendModel, message: String = "", openedType: FriendsHostOpenedType
    ) {
        myTracker.trackFriendAccept()

        val payload = hashMapOf(
            "friend_id" to friend.userModel.userId, "message" to message
        )

        val d = webSocketMainChannel.pushAddFriends(payload).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe({
                onPushFriendRequest(
                    fromId = appSettings.readUID(),
                    toId = friend.userModel.userId,
                    isRejected = false,
                    openedType = openedType
                )
                liveNewFriend.value = friend
            }, {
                Timber.e(it)
                liveEvent.value = FriendsListViewEvents.OnErrorAddFriend
            })
        disposables.add(d)
    }


    fun requestAllFriends(limit: Int = FRIEND_REQUEST_PAGE_LIMIT, offset: Int = 0) {
        Timber.d("FRIENDS_LOG requestAllFriends")
        val existUserId = userID ?: return
        allFriendsLoadingJob?.cancel()
        allFriendsLoadingJob = viewModelScope.launch {
            try {
                val response = getFriendsV2.getConfirmedFriends(userId = existUserId, limit = limit, offset = offset)
                val result = response.data.friends?.map { user ->
                    FriendModel(friendsEntity = user, type = GetFriendsListUseCase.FRIENDS)
                } ?: emptyList()

                if (offset == 0) friendsList.clear()
                friendsList.addAll(result)

                isLastFriend = result.isEmpty()
                liveAllFriends.postValue(friendsList)
            } catch (e: Exception) {
                isLastFriend = false
                Timber.e(e.message)
            }
        }
    }


    private fun requestBlackList() {
        Timber.d("RequestingBlackList")
        friendUseCase.getFriendsBlockedList()?.let { flowable ->
            val d = flowable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
                it.data?.let { data ->
                    val res = mutableListOf<FriendModel>()
                    data.forEach { userModel ->
                        res.add(FriendModel(userModel, GetFriendsListUseCase.BLACKLIST))
                    }
                    liveBlackList.value = res
                    friendsList = res
                }
            }, {
                Timber.e(it)
                liveEvent.value = FriendsListViewEvents.OnErrorAction
            })
            disposables.add(d)
        }
    }

    private fun requestOutComing(page: Int = 0) {
        Timber.d("requestOutComing")
        friendUseCase.getFriendOutcomingList(page)?.let { flowable ->
            val d = flowable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
                it.data?.let { data ->
                    val res = mutableListOf<FriendModel>()
                    data.forEach { userModel ->
                        res.add(FriendModel(userModel, GetFriendsListUseCase.OUTCOMING))
                    }
                    liveOutcomingFriends.value = res

                    if (page == 0) friendsList.clear()
                    friendsList.addAll(res)
                }
            }, {
                Timber.e(it)
                liveEvent.value = FriendsListViewEvents.OnErrorAction
            })
            disposables.add(d)
        }
    }

    fun requestFriendIncomingList(limit: Int = 1000, offset: Int = 0) {
        Timber.d("requestFriendIncomingList")
        val existUserId = userID ?: return
        incomingLoadingJob?.cancel()
        incomingLoadingJob = viewModelScope.launch {
            try {
                val response = getFriendsV2.getIncomingFriends(userId = existUserId, limit = limit, offset = offset)
                val result = response.data.friends?.map { user ->
                    FriendModel(friendsEntity = user, type = GetFriendsListUseCase.INCOMING)
                } ?: emptyList()
                isLastIncoming = result.isEmpty()
                liveEvent.postValue(if (result.isEmpty()) FriendsListViewEvents.NoIncomigRequests else FriendsListViewEvents.HasIncomingRequests)
                liveIncomingFriends.postValue(result)

                friendsList.addAll(result)
            } catch (e: Exception) {
                liveIncomingFriends.postValue(emptyList())
                Timber.e(e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
        searchDisposable?.dispose()
    }

    fun onActionClicked(friend: FriendModel, openedType: FriendsHostOpenedType) {
        when (friend.type) {

            GetFriendsListUseCase.FRIENDS -> {
                if (isSearch) {
                    deleteFriendSocket(friend = friend, openedType = openedType)
                } else {
                    deleteFriendSocket(friend = friend, openedType = openedType)
                }
            }

            GetFriendsListUseCase.INCOMING -> {
                confirmFriendSocket(friend = friend, openedType = openedType)
            }

            GetFriendsListUseCase.BLACKLIST -> {
                blockUser(friend)
            }

            GetFriendsListUseCase.OUTCOMING -> {
                // не используется
            }
        }
    }

    // todo проверить поведение pushRemoveFriends, т.к. данный метод удаляет из друзей и отписывает
    fun cancelOutcomeFriendshipRequest(friend: FriendModel) {
        val payload = hashMapOf<String, Any>(
            "id" to friend.userModel.userId
        )
        val d = webSocketMainChannel.pushRemoveFriends(payload).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe({
                liveRemoveItem.value = friend
            }, {
                Timber.e(it)
                liveEvent.value = FriendsListViewEvents.OnErrorAction
            })
        disposables.add(d)
    }

    // todo проверить поведение pushRemoveFriends, т.к. данный метод удаляет из друзей и отписывает
    fun cancelOutcomeFriendshipRequestUnSubscribe(friend: FriendModel) {
        val payload = hashMapOf<String, Any>(
            "id" to friend.userModel.userId
        )
        val d = webSocketMainChannel.pushRemoveFriends(payload).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread()).subscribe({
                liveRemoveItem.value = friend
            }, {
                Timber.e(it)
                liveEvent.value = FriendsListViewEvents.OnErrorAction
            })
        disposables.add(d)
    }


    private fun blockUser(friend: FriendModel, isBlocked: Boolean = false) {
        userID?.let { uID ->
            blockUserUseCase.blockUser(uID, friend.userModel.userId, isBlocked)?.let { single ->
                val d = single.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ response ->
                        Timber.d("Response block user")
                        if (!isBlocked) {
                            when {
                                response.data != null -> {
                                    liveRemoveItem.value = friend
                                }

                                response.err != null -> liveEvent.value = FriendsListViewEvents.OnErrorAction

                                else -> liveEvent.value = FriendsListViewEvents.OnErrorAction
                            }
                        } else {
                            when {
                                response.data != null -> {
                                    liveRemoveFriend.value = friend
                                }

                                response.err != null -> liveEvent.value = FriendsListViewEvents.OnErrorAction

                                else -> liveEvent.value = FriendsListViewEvents.OnErrorAction
                            }
                        }
                    }, {
                        Timber.e(it)
                        liveEvent.value = FriendsListViewEvents.OnErrorAction
                    })
                disposables.add(d)
            }
        }
    }

    fun blackListUser(friend: FriendModel) {
        blockUser(friend, true)
    }

    fun onRefresh() {
        userID?.let {
            friendUseCase.setParams(it, 1)
            requestData()
        }
    }

    fun rejectFriend(friend: FriendModel, openedType: FriendsHostOpenedType) {
        deleteFriendSocket(friend = friend, isRejected = true, openedType = openedType)
    }

    fun onStartSearch() {
        isSearch = true
    }

    fun onStopSearch() {
        isSearch = false
    }


    private fun onPushFriendRequest(
        fromId: Long, toId: Long, isRejected: Boolean, openedType: FriendsHostOpenedType
    ) {
        if (isRejected) {
            amplitudeHelperNew.onRequestDenied(
                fromId = fromId, toId = toId, where = getAmplitudeAction(openedType)
            )

        } else {
            amplitudeHelperNew.onRequestAccepted(
                fromId = fromId, toId = toId, where = getAmplitudeAction(openedType)
            )
        }
    }

    private fun getAmplitudeAction(openedType: FriendsHostOpenedType): AmplitudeFriendRequestPropertyWhere {
        return when (openedType) {
            FriendsHostOpenedType.FRIENDS_HOST_OPENED_FROM_PROFILE -> AmplitudeFriendRequestPropertyWhere.INCOMING_APPLICATIONS

            FriendsHostOpenedType.FRIENDS_HOST_OPENED_FROM_NOTIFICATIONS -> AmplitudeFriendRequestPropertyWhere.NOTIFICATIONS

            else -> AmplitudeFriendRequestPropertyWhere.OTHER
        }
    }

    //Pagination
    private var isLastFriend = false
    private var isLastIncoming = false

    private var allFriendsLoadingJob: Job? = null
    private var incomingLoadingJob: Job? = null

    fun isLastFriend() = isLastFriend
    fun isLastIncoming() = isLastIncoming


    fun isLoadingFriend() = allFriendsLoadingJob?.isActive == true
    fun isLoadingIncoming() = incomingLoadingJob?.isActive == true

    private fun emitViewEvent(typeEvent: UserSubscriptionViewEvent) {
        viewModelScope.launch {
            _viewEvent.emit(typeEvent)
        }
    }

    fun subscribeUser(userId: Long) {
        viewModelScope.launch {
            runCatching { subscribeUserUseCase.invoke(userId) }.onSuccess {
                emitViewEvent(
                    UserSubscriptionViewEvent.ShowSuccessSnackBar(
                        R.string.subscribed_on_user_notif_on
                    )
                )
                pushFriendStatusChangedUseCase.invoke(
                    userId = userId, isSubscribe = true
                )
            }.onFailure { t ->
                emitViewEvent(
                    UserSubscriptionViewEvent.ShowErrorSnackBar(
                        R.string.error_try_later
                    )
                )
                Timber.e(t)
            }
        }

    }

    fun addToFriend(userId: Long) {
        viewModelScope.launch {
            runCatching { addUserToFriendUseCase.invoke(userId) }.onSuccess {
                val messageRes = R.string.enabled_new_post_notif
                emitViewEvent(UserSubscriptionViewEvent.ShowSuccessSnackBar(messageRes))
                pushFriendStatusChangedUseCase.invoke(userId)
            }.onFailure { e ->
                emitViewEvent(UserSubscriptionViewEvent.ShowErrorSnackBar(R.string.error_try_later))
                Timber.e(e)
            }
        }
    }
}
