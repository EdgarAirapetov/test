package com.numplates.nomera3.modules.userprofile.ui.meerafriends.subscribtions

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.meera.core.preferences.AppSettings
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.R
import com.numplates.nomera3.domain.interactornew.AddUserToFriendUseCaseNew
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.domain.interactornew.SubscribeUserUseCaseNew
import com.numplates.nomera3.domain.interactornew.SubscriptionUseCase
import com.numplates.nomera3.modules.baseCore.helper.amplitude.people.AmplitudePeopleAnalytics
import com.numplates.nomera3.modules.baseCore.helper.amplitude.people.AmplitudePeopleWhereProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.people.AmplitudePeopleWhich
import com.numplates.nomera3.modules.feed.domain.usecase.ReactiveUpdateSubscribeUserUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.UpdateSubscriptionUserParams
import com.numplates.nomera3.modules.user.domain.usecase.PushFriendStatusChangedUseCase
import com.numplates.nomera3.presentation.view.fragments.entity.UserSubscriptionViewEvent
import com.numplates.nomera3.presentation.view.utils.apphints.TooltipDuration
import com.numplates.nomera3.presentation.viewmodel.BaseViewModel
import com.numplates.nomera3.presentation.viewmodel.viewevents.SubscriptionViewEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class MeeraSubscriptionViewModel @Inject constructor(
    private val appSettings: AppSettings,
    private val subscriptions: SubscriptionUseCase,
    private val reactiveUpdateSubscribeUserUseCase: ReactiveUpdateSubscribeUserUseCase,
    private val getUserUidUseCase: GetUserUidUseCase,
    private val amplitudePeopleAnalytics: AmplitudePeopleAnalytics,
    private val subscribeUserUseCase: SubscribeUserUseCaseNew,
    private val addUserToFriendUseCase: AddUserToFriendUseCaseNew,
    private val pushFriendStatusChangedUseCase: PushFriendStatusChangedUseCase,
) : BaseViewModel() {

    val liveSubscriptions = MutableLiveData<List<UserSimple?>>()
    val liveViewEvent = MutableLiveData<SubscriptionViewEvent>()

    private val _viewEvent = MutableSharedFlow<UserSubscriptionViewEvent>()
    val viewEvent: SharedFlow<UserSubscriptionViewEvent> = _viewEvent

    private var isLastSubscription = false
    private var isLoadingSubscription = false

    private var isLastSubscriptionSearch = false
    private var isLoadingSubscriptionSearch = false

    fun getUserUid() = getUserUidUseCase.invoke()

    /**
     * Запрос на удаление подписки
     * */
    fun deleteFromSubscription(userId: Long?) {
        if (userId == null) return
        requestCallback({
            subscriptions.deleteFromSubscriptions(mutableListOf(userId))
        }, {
            onSuccess {
                updateUserSubscription(userId)
                liveViewEvent.value = SubscriptionViewEvent.SuccessDeleteFromSubscription(userId)
            }
            onProgress {}
            onError { _, _ -> }
        })
    }

    /**
     * Запрос на получение списка подписок
     * */
    fun requestSubscriptions(userId: Long?, limit: Int = 50, offset: Int = 0) {
        if (userId == null) return
        isLoadingSubscription = true
        requestCallback({
            subscriptions.getUserSubscriptions(userId, limit, offset)
        }, {
            onSuccess { response ->
                isLoadingSubscription = false
                response.subscriptions?.let { subscriptionsList ->
                    isLastSubscription = subscriptionsList.isEmpty()
                    val totalList = mutableListOf<UserSimple?>()
                    if (offset != 0)    liveSubscriptions.value?.let { totalList.addAll(it) }
                    totalList.addAll(subscriptionsList)
                    liveSubscriptions.value = totalList
                } ?: kotlin.run {
                    isLastSubscription = true
                    liveViewEvent.postValue(SubscriptionViewEvent.ErrorWhileRequestingSubscriptions)
                }
            }
            onProgress { }
            onError { _, _ ->
                isLoadingSubscription = false
                Timber.e("Error requesting subscriptions")
                liveViewEvent.postValue(SubscriptionViewEvent.ErrorWhileRequestingSubscriptions)
            }
        })
    }

    fun logSubscriptionsPeopleSelected() {
        amplitudePeopleAnalytics.setPeopleSelected(
            where = AmplitudePeopleWhereProperty.ICON_FOLLOW,
            which = AmplitudePeopleWhich.PEOPLE
        )
    }

    fun isNeedToShowReferralTooltip(): Boolean {
        val showTimes = appSettings.isCreateSubscribersReferralToolTipShownTimes ?: 0
        val isNewSession = appSettings.isShownTooltipSession(AppSettings.KEY_IS_CREATE_SUBSCRIBERS_REFERRAL_TOOLTIP_WAS_SHOWN_TIMES) == true
        return showTimes < TooltipDuration.DEFAULT_TIMES && isNewSession
    }

    fun referralToolTipShowed() {
        val shownTimes = appSettings.isCreateSubscribersReferralToolTipShownTimes
        if (shownTimes > TooltipDuration.DEFAULT_TIMES) return
        appSettings.isCreateSubscribersReferralToolTipShownTimes = shownTimes + 1
      appSettings.markTooltipAsShownSession(AppSettings.KEY_IS_CREATE_SUBSCRIBERS_REFERRAL_TOOLTIP_WAS_SHOWN_TIMES)
    }

    /**
     * Поиск по подпискам
     * */
    fun subscriptionsSearch(userId: Long?, limit: Int = 50, offset: Int = 0, text: String) {
        if (userId == null) return
        isLoadingSubscriptionSearch = true
        requestCallback({
            subscriptions.subscriptionsSearch(userId, limit, offset, text)
        }, {
            onSuccess { response ->
                isLoadingSubscriptionSearch = false
                response.subscriptions?.let { subscriptionsList ->
                    isLastSubscriptionSearch = subscriptionsList.isEmpty()
                    val totalList = mutableListOf<UserSimple?>()
                    if (offset != 0)    liveSubscriptions.value?.let { totalList.addAll(it) }
                    totalList.addAll(subscriptionsList)
                    liveSubscriptions.value = totalList
                } ?: kotlin.run {
                    isLastSubscriptionSearch = true
                    liveViewEvent.postValue(SubscriptionViewEvent.ErrorWhileRequestingSubscriptions)
                }
            }
            onProgress {}
            onError { _, _ ->
                isLoadingSubscriptionSearch = false
                liveViewEvent.postValue(SubscriptionViewEvent.ErrorWhileSearchSubscriptions)
            }
        })
    }

    // Методы для пагинации
    fun onLastSubscription() = isLastSubscription
    fun onLoadingSubscription() = isLoadingSubscription

    fun onLastSubscriptionSearch() = isLastSubscriptionSearch
    fun onLoadingSubscriptionSearch() = isLoadingSubscriptionSearch

    private fun updateUserSubscription(userId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            reactiveUpdateSubscribeUserUseCase.execute(
                params = UpdateSubscriptionUserParams(
                    userId = userId,
                    isSubscribed = false,
                    needToHideFollowButton = false,
                    isBlocked = false
                ),
                success = {},
                fail = {}
            )
        }
    }

    private fun emitViewEvent(typeEvent: UserSubscriptionViewEvent) {
        viewModelScope.launch {
            _viewEvent.emit(typeEvent)
        }
    }
    fun subscribeUser(userId: Long) {
        viewModelScope.launch {
            runCatching { subscribeUserUseCase.invoke(userId) }
                .onSuccess {
                    emitViewEvent(
                        UserSubscriptionViewEvent.ShowSuccessSnackBar(
                            R.string.subscribed_on_user_notif_on
                        )
                    )
                    pushFriendStatusChangedUseCase.invoke(
                        userId = userId,
                        isSubscribe = true
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
            runCatching { addUserToFriendUseCase.invoke(userId) }
                .onSuccess {
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
