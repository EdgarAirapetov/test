package com.numplates.nomera3.modules.userprofile.ui.meerafriends.subscribers

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.domain.interactornew.AddUserToFriendUseCaseNew
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.domain.interactornew.SubscribeUserUseCaseNew
import com.numplates.nomera3.domain.interactornew.SubscribersUseCase
import com.numplates.nomera3.modules.user.domain.usecase.BlockStatusUseCase
import com.numplates.nomera3.modules.user.domain.usecase.DefBlockParams
import com.numplates.nomera3.modules.user.domain.usecase.PushFriendStatusChangedUseCase
import com.numplates.nomera3.presentation.view.fragments.entity.UserSubscriptionViewEvent
import com.numplates.nomera3.presentation.viewmodel.BaseViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.SubscriptionViewEvent
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class MeeraSubscribersViewModel @Inject constructor(
    private val subscribers: SubscribersUseCase,
    private val blockUser: BlockStatusUseCase,
    private val getUserUidUseCase: GetUserUidUseCase,
    private val subscribeUserUseCase: SubscribeUserUseCaseNew,
    private val addUserToFriendUseCase: AddUserToFriendUseCaseNew,
    private val pushFriendStatusChangedUseCase: PushFriendStatusChangedUseCase,
) : BaseViewModel() {
    private val disposables = CompositeDisposable()
    val liveSubscribers = MutableLiveData<List<UserSimple?>>()
    val liveViewEvent = MutableLiveData<SubscriptionViewEvent>()

    private val _viewEvent = MutableSharedFlow<UserSubscriptionViewEvent>()
    val viewEvent: SharedFlow<UserSubscriptionViewEvent> = _viewEvent


    //Pagination flags
    private var isLastSubscriber = false
    private var isLoadingSubscriber = false

    private var isLastSubscriberSearch = false
    private var isLoadingSubscriberSearch = false

    init {
        App.component.inject(this)
    }

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
                } ?: kotlin.run {
                    isLastSubscriber = true
                    liveViewEvent.postValue(SubscriptionViewEvent.ErrorWhileRequestingSubscriptions)
                }
            }

            onProgress { }

            onError { _, _ ->
                isLoadingSubscriber = false
                Timber.e("Error while loading Subscribers")
                liveViewEvent.postValue(SubscriptionViewEvent.ErrorWhileRequestingSubscriptions)
            }
        })
    }

    fun deleteFromSubscribers(userId: Long?) {
        if (userId == null) return
        requestCallback({
            subscribers.deleteUserFromSubscribers(listOf(userId))
        }, {
            onSuccess {
                liveViewEvent.value = SubscriptionViewEvent.SuccessDeleteFromSubscription(userId)
            }
            onProgress { }
            onError { _, _ ->
                Timber.e("Error requesting to delete subscriber")
                liveViewEvent.postValue(SubscriptionViewEvent.ErrorDeleteFromSubscription)
            }
        })
    }

    fun deleteAndBlock(myId: Long?, userToBlockId: Long?) {
        if (myId == null || userToBlockId == null) return
        val params = DefBlockParams(
            userId = myId, remoteUserId = userToBlockId, isBlocked = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            {
                liveViewEvent.postValue(
                    SubscriptionViewEvent.SuccessDeleteFromSubscription(userToBlockId)
                )
            }
            {
                liveViewEvent.postValue(SubscriptionViewEvent.ErrorDeleteFromSubscription)
            }
            blockUser.invoke(
                params = params
            )
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
                    } ?: kotlin.run {
                        isLastSubscriberSearch = true
                        liveViewEvent.postValue(SubscriptionViewEvent.ErrorWhileRequestingSubscriptions)
                    }

                }
                onProgress { }
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
