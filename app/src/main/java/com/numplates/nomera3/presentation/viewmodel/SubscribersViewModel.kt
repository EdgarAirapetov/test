package com.numplates.nomera3.presentation.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.meera.core.extensions.toInt
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.domain.interactornew.SubscribersUseCase
import com.numplates.nomera3.domain.interactornew.SubscriptionUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.ReactiveUpdateSubscribeUserUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.UpdateSubscriptionUserParams
import com.numplates.nomera3.modules.user.domain.usecase.BlockStatusUseCase
import com.numplates.nomera3.modules.user.domain.usecase.DefBlockParams
import com.numplates.nomera3.presentation.viewmodel.viewevents.SubscriptionViewEvent
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class SubscribersViewModel @Inject constructor(
    private val subscribers: SubscribersUseCase,
    private val subscriptions: SubscriptionUseCase,
    private val reactiveUpdateSubscribeUserUseCase: ReactiveUpdateSubscribeUserUseCase,
    private val blockUser: BlockStatusUseCase,
    private val getUserUidUseCase: GetUserUidUseCase,
) : BaseViewModel() {


    private val disposables = CompositeDisposable()

    val liveSubscribers = MutableLiveData<List<UserSimple?>>()
    val liveViewEvent = MutableLiveData<SubscriptionViewEvent>()

    //Pagination flags
    private var isLastSubscriber = false
    private var isLoadingSubscriber = false

    private var isLastSubscriberSearch = false
    private var isLoadingSubscriberSearch = false


    fun getUserUid() = getUserUidUseCase.invoke()

    fun requestSubscribers(userId: Long?, limit: Int, offset: Int) {
        if (userId == null) return
        isLoadingSubscriber = true
        requestCallback({
            subscribers.getUserSubscribers(userId, limit, offset)
        }, {
            onSuccess { response ->
                isLoadingSubscriber = false
                response.subscriptions?.let { subscriptionsList ->
                    isLastSubscriber = subscriptionsList.isEmpty()
                    val totalList = mutableListOf<UserSimple?>()
                    if (offset != 0) liveSubscribers.value?.let { totalList.addAll(it) }
                    totalList.addAll(subscriptionsList)
                    liveSubscribers.value = totalList
                    liveViewEvent.postValue(SubscriptionViewEvent.SuccessLoadFromSubscription)
                } ?: kotlin.run {
                    liveViewEvent.postValue(SubscriptionViewEvent.ErrorWhileRequestingSubscriptions)
                }
            }

            onProgress {
                liveViewEvent.postValue(SubscriptionViewEvent.LoadFromSubscription)
            }

            onError { _, _ ->
                isLoadingSubscriber = false
                Timber.e("Error while loading Subscribers")
                liveViewEvent.postValue(SubscriptionViewEvent.ErrorWhileRequestingSubscriptions)
            }
        })
    }

    fun deleteFromSubscribers(userId: Long?) {
        if (userId == null) return
        viewModelScope.launch {
            runCatching {
                subscribers.deleteUserFromSubscribers(listOf(userId))
            }.onSuccess {
                liveViewEvent.value = SubscriptionViewEvent.SuccessDeleteFromSubscription(userId)
            }.onFailure {
                Timber.e("Error requesting to delete subscriber")
                liveViewEvent.postValue(SubscriptionViewEvent.ErrorDeleteFromSubscription)
            }
        }
    }

    fun deleteFromSubscription(userId: Long?) {
        if (userId == null) return
        viewModelScope.launch {
            runCatching {
                subscriptions.deleteFromSubscriptions(mutableListOf(userId))
            }.onSuccess {
                updateUserSubscription(userId, false)
            }.onFailure {
                Timber.e(it)
            }
        }
    }

    suspend fun addSubscription(userId: Long?) {
        if (userId == null) return
        runCatching {
            subscriptions.addSubscription(mutableListOf(userId))
        }.onSuccess {
            updateUserSubscription(userId, true)
        }
    }

    private fun updateUserSubscription(userId: Long, isSubscribed: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            reactiveUpdateSubscribeUserUseCase.execute(
                params = UpdateSubscriptionUserParams(
                    userId = userId, isSubscribed = isSubscribed, needToHideFollowButton = false, isBlocked = false
                ), success = {}, fail = {})
        }

        val totalList = mutableListOf<UserSimple?>()
        liveSubscribers.value?.let { totalList.addAll(it) }
        liveSubscribers.value = totalList.map {
            if (it?.userId == userId) {
                it.copy(settingsFlags = it.settingsFlags?.copy(subscription_on = isSubscribed.toInt()))
            } else {
                it
            }
        }
    }

    fun deleteAndBlock(myId: Long?, userToBlockId: Long?) {
        if (myId == null || userToBlockId == null) return
        val params = DefBlockParams(
            userId = myId, remoteUserId = userToBlockId, isBlocked = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                blockUser.invoke(
                    params = params
                )
            }.onSuccess {
                liveViewEvent.postValue(SubscriptionViewEvent.SuccessDeleteFromSubscription(userToBlockId))
            }.onFailure {
                liveViewEvent.postValue(SubscriptionViewEvent.ErrorDeleteFromSubscription)
            }
        }
    }


    /**
     * Поиск по подпискам
     * */
    fun subscribersSearch(userId: Long?, limit: Int = 50, offset: Int = 0, text: String) {
        if (userId != null) {
            isLoadingSubscriberSearch = true
            requestCallback({
                subscribers.subscriberSearch(userId, limit, offset, text)
            }, {
                onSuccess { response ->
                    isLoadingSubscriberSearch = false
                    response.subscriptions?.let { subscriptionsList ->
                        isLastSubscriberSearch = subscriptionsList.isEmpty()
                        val totalList = mutableListOf<UserSimple?>()
                        if (offset != 0) liveSubscribers.value?.let { totalList.addAll(it) }
                        totalList.addAll(subscriptionsList)
                        liveSubscribers.value = totalList
                        liveViewEvent.postValue(SubscriptionViewEvent.SuccessLoadFromSubscription)
                    } ?: kotlin.run {
                        isLastSubscriberSearch = true
                        liveViewEvent.postValue(SubscriptionViewEvent.ErrorWhileRequestingSubscriptions)
                    }

                }
                onProgress {
                    liveViewEvent.postValue(SubscriptionViewEvent.LoadFromSubscription)
                }
                onError { _, _ ->
                    isLoadingSubscriberSearch = false
                    liveViewEvent.postValue(SubscriptionViewEvent.ErrorWhileSearchSubscriptions)
                }
            })
        }
    }

    // Методы для пагинации
    fun onLastSubscriber() = isLastSubscriber
    fun onLoadingSubscriber() = isLoadingSubscriber

    fun onLastSubscriberSearch() = isLastSubscriberSearch
    fun onLoadingSubscriberSearch() = isLoadingSubscriberSearch


    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }
}
