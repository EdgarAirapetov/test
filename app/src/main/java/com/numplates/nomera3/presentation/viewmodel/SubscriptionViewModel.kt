package com.numplates.nomera3.presentation.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.meera.core.extensions.toInt
import com.meera.core.preferences.AppSettings
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.domain.interactornew.SubscriptionUseCase
import com.numplates.nomera3.modules.baseCore.helper.amplitude.people.AmplitudePeopleAnalytics
import com.numplates.nomera3.modules.baseCore.helper.amplitude.people.AmplitudePeopleWhereProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.people.AmplitudePeopleWhich
import com.numplates.nomera3.modules.feed.domain.usecase.ReactiveUpdateSubscribeUserUseCase
import com.numplates.nomera3.modules.feed.domain.usecase.UpdateSubscriptionUserParams
import com.numplates.nomera3.presentation.view.utils.apphints.TooltipDuration
import com.numplates.nomera3.presentation.viewmodel.viewevents.SubscriptionViewEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class SubscriptionViewModel @Inject constructor(
    private val appSettings: AppSettings,
    private val subscriptions: SubscriptionUseCase,
    private val reactiveUpdateSubscribeUserUseCase: ReactiveUpdateSubscribeUserUseCase,
    private val getUserUidUseCase: GetUserUidUseCase,
    private val amplitudePeopleAnalytics: AmplitudePeopleAnalytics,
) : BaseViewModel() {

    val liveSubscriptions = MutableLiveData<List<UserSimple?>>()
    val liveViewEvent = MutableLiveData<SubscriptionViewEvent>()

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
                updateUserSubscription(userId, false)
            }
            onProgress {}
            onError { _, _ -> }
        })
    }

    /**
     * Запрос на подписку на юзера
     * */
    suspend fun addSubscription(userId: Long?) {
        if (userId == null) return
        runCatching {
            subscriptions.addSubscription(mutableListOf(userId))
        }
            .onSuccess {
                updateUserSubscription(userId, true)
            }
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
                    isLastSubscription = subscriptionsList.size < REQUEST_LIMIT
                    val totalList = mutableListOf<UserSimple?>()
                    if (offset != 0) liveSubscriptions.value?.let { totalList.addAll(it) }
                    totalList.addAll(subscriptionsList)
                    liveSubscriptions.value = totalList
                    liveViewEvent.postValue(SubscriptionViewEvent.SuccessLoadFromSubscription)
                } ?: kotlin.run {
                    isLastSubscription = true
                    liveViewEvent.postValue(SubscriptionViewEvent.ErrorWhileRequestingSubscriptions)
                }
            }
            onProgress {
                liveViewEvent.postValue(SubscriptionViewEvent.LoadFromSubscription)
            }
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
        val isNewSession =
            appSettings.isShownTooltipSession(AppSettings.KEY_IS_CREATE_SUBSCRIBERS_REFERRAL_TOOLTIP_WAS_SHOWN_TIMES) == true
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
                    if (offset != 0) liveSubscriptions.value?.let { totalList.addAll(it) }
                    totalList.addAll(subscriptionsList)
                    liveSubscriptions.value = totalList
                    liveViewEvent.postValue(SubscriptionViewEvent.SuccessLoadFromSubscription)
                } ?: kotlin.run {
                    isLastSubscriptionSearch = true
                    liveViewEvent.postValue(SubscriptionViewEvent.ErrorWhileRequestingSubscriptions)
                }
            }
            onProgress {
                liveViewEvent.postValue(SubscriptionViewEvent.LoadFromSubscription)
            }
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

    private fun updateUserSubscription(userId: Long, isSubscribed: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            reactiveUpdateSubscribeUserUseCase.execute(
                params = UpdateSubscriptionUserParams(
                    userId = userId,
                    isSubscribed = isSubscribed,
                    needToHideFollowButton = false,
                    isBlocked = false
                ),
                success = {},
                fail = {}
            )
        }

        val totalList = mutableListOf<UserSimple?>()
        liveSubscriptions.value?.let { totalList.addAll(it) }
        liveSubscriptions.value = totalList.map {
            if(it?.userId==userId) {
                it.copy(settingsFlags = it.settingsFlags?.copy(subscription_on = isSubscribed.toInt()))
            } else {
                it
            }
        }
    }

    companion object {
        const val REQUEST_LIMIT = 50

    }
}
