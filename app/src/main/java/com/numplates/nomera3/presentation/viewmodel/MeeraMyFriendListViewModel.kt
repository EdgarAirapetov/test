package com.numplates.nomera3.presentation.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meera.core.network.websocket.WebSocketMainChannel
import com.meera.core.preferences.AppSettings
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.domain.interactornew.BlockUserUseCase
import com.numplates.nomera3.domain.interactornew.GetFriendsListUseCase
import com.numplates.nomera3.domain.interactornew.GetFriendsUseCase
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.domain.interactornew.RemoveUserUseCase
import com.numplates.nomera3.domain.interactornew.SearchFriendsUseCase
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.baseCore.helper.amplitude.people.AmplitudePeopleAnalytics
import com.numplates.nomera3.modules.baseCore.helper.amplitude.people.AmplitudePeopleWhereProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.people.AmplitudePeopleWhich
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeFriendRequest
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeFriendRequestPropertyWhere
import com.numplates.nomera3.modules.tracker.ITrackerActions
import com.numplates.nomera3.presentation.view.adapter.newfriends.FriendModel
import com.numplates.nomera3.presentation.view.fragments.FriendsHostOpenedType
import com.numplates.nomera3.presentation.viewmodel.viewevents.FriendsListViewEvents
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


class MeeraMyFriendListViewModel @Inject constructor(
    private val getFriendsV2: GetFriendsUseCase,
    private val blockUserUseCase: BlockUserUseCase,
    private val webSocketMainChannel: WebSocketMainChannel,
    private val removeUserUseCase: RemoveUserUseCase,
    private val myTracker: ITrackerActions,
    private val amplitudeHelper: AnalyticsInteractor,
    private val appSettings: AppSettings,
    private val amplitudeHelperNew: AmplitudeFriendRequest,
    private val getUserUidUseCase: GetUserUidUseCase,
    private val amplitudePeopleAnalytics: AmplitudePeopleAnalytics,
    private val searchFriendsUseCase: SearchFriendsUseCase

) : ViewModel() {

    private var userID: Long? = null
    var isSearch = false

    private var friendsList = listOf<FriendModel>()

    val liveAllFriends = MutableLiveData<List<FriendModel>>()
    val liveRemoveItem = MutableLiveData<FriendModel>()
    val liveRemoveFriend = MutableLiveData<FriendModel>()
    val liveDeleteFriend = MutableLiveData<FriendModel>()
    val liveNewFriend = MutableLiveData<FriendModel>()
    val liveEvent = MutableLiveData<FriendsListViewEvents>()

    var searchQuery: String? = null

    private val disposables = CompositeDisposable()

    fun init(userID: Long?) {
        this.userID = userID
        requestData()
    }

    fun getUserUid() = getUserUidUseCase.invoke()

    private fun requestData() {
        if (isSearch) searchQuery?.let { searchFriendNetwork(queryString = it) }
        else requestAllFriends()
    }

    fun loadMoreData(offset: Int) {
        if (isSearch) searchQuery?.let { searchFriendNetwork(queryString = it, offset = offset) }
        else requestAllFriends(offset = offset)
    }

    fun logMyFriendsPeopleSelected() {
        amplitudePeopleAnalytics.setPeopleSelected(
            where = AmplitudePeopleWhereProperty.FIND_FRIEND_BUTTON, which = AmplitudePeopleWhich.PEOPLE
        )
    }


    fun searchFriendNetwork(queryString: String, limit: Int = FRIEND_REQUEST_PAGE_LIMIT, offset: Int = 0) {
        if (offset == 0) isLastFriend = false
        searchQuery = queryString
        Timber.d("запрос:  $queryString Length-string: ${queryString.length}")
        allFriendsLoadingJob?.cancel()
        allFriendsLoadingJob = viewModelScope.launch {
            runCatching {
                searchFriendsUseCase.invoke(query = queryString, limit = limit, offset = offset)

            }.onSuccess {
                val result = mutableListOf<FriendModel>()
                it.data?.forEach { user ->
                    user?.let { notNullUser ->
                        result.add(FriendModel(notNullUser, GetFriendsListUseCase.FRIENDS))
                    }
                }
                isLastFriend = result.size < FRIEND_REQUEST_PAGE_LIMIT
                friendsList = if (offset == 0) result else friendsList.plus(result)
                liveAllFriends.postValue(friendsList)
            }.onFailure {
                Timber.e(it)
                isLastFriend = false
            }
        }
    }


    fun removeFriendSaveSubscription(friend: FriendModel?) {
        friend?.userModel?.userId?.let { id ->
            removeUserUseCase.removeUserAndSaveSubscription(id).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe({ response: ResponseWrapper<Boolean>? ->
                    val isRemoved = response?.data ?: false
                    amplitudeHelper.logDelFriend(appSettings.readUID(), id)
                    if (isRemoved) {
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
                            liveDeleteFriend.value = friend
                            liveEvent.value = FriendsListViewEvents.OnFriendRejected
                        }
                    }, {
                        liveEvent.value = FriendsListViewEvents.OnErrorRemoveFriend
                        it.printStackTrace()
                    })
            )
        }
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
        if (offset == 0) isLastFriend = false
        val existUserId = userID ?: return
        allFriendsLoadingJob?.cancel()
        allFriendsLoadingJob = viewModelScope.launch {
            try {
                val response = getFriendsV2.getConfirmedFriends(userId = existUserId, limit = limit, offset = offset)
                val result = response.data.friends?.map { user ->
                    FriendModel(friendsEntity = user, type = GetFriendsListUseCase.FRIENDS)
                } ?: emptyList()

                isLastFriend = result.size < FRIEND_REQUEST_PAGE_LIMIT
                friendsList = if (offset == 0) result else friendsList.plus(result)
                liveAllFriends.postValue(friendsList)
            } catch (e: Exception) {
                isLastFriend = false
                Timber.e(e)
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        disposables.clear()
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

            GetFriendsListUseCase.OUTCOMING -> Unit
        }
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


    fun onRefresh() {

        requestData()
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
    private var allFriendsLoadingJob: Job? = null

    fun isLastFriend() = isLastFriend
    fun isLoadingFriend() = allFriendsLoadingJob?.isActive == true
}
