package com.numplates.nomera3.modules.newroads

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.App
import com.numplates.nomera3.modules.feed.ui.fragment.SubscriptionRoadViewEvent
import com.numplates.nomera3.modules.newroads.data.PostsRepository
import com.numplates.nomera3.modules.newroads.data.entities.SubscriptionNewPostEntity
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

const val DELAY_REFRESH = 1000L

class SubscriptionRoadViewModel : ViewModel() {

    val hasNewSubscriptionPost = MutableLiveData<SubscriptionNewPostEntity>()

    private val disposables = CompositeDisposable()

    private val eventStreamSubject = PublishSubject.create<SubscriptionRoadViewEvent>()

    @Inject
    lateinit var postRepository: PostsRepository

    init {
        App.component.inject(this)

        initHasSubscriptionNewPost()
    }

    fun getEventStream(): Observable<SubscriptionRoadViewEvent> {
        return eventStreamSubject
    }

    fun refreshRoad(expandAppBarLayout: Boolean) {
        publishEvent(SubscriptionRoadViewEvent.NeedRefresh(expandAppBarLayout))
        viewModelScope.launch {
            delay(DELAY_REFRESH)
            markNewPostsAsRead()
        }
    }

    fun markNewPostsAsRead() {
        viewModelScope.launch {
            postRepository.markAllSubscriptionPostViewed()
        }
    }

    /**
     * Пометить, что Подписочные посты были запрошены в рамках сессии
     * (посты в подписочной дороге автоматически запрашиваем только один раз за сессию)
     */
    fun setSubscriptionPostsWereRequestedWithinSession() {
        postRepository.setSubscriptionPostsWereRequestedWithinSession(true)
    }

    /**
     * Были ли посты в подписочной дороги запрошены во время сессии
     * (посты в подписочной дороге автоматически запрашиваем только один раз за сессию)
     */
    fun getPostsWereRequestedWithinSession(): Boolean {
        return postRepository.getSubscriptionPostsWereRequestedWithinSession()
    }

    /**
     * Сбросить флаг – Были ли посты в подписочной дороги запрошены во время сессии
     */
    fun resetPostsWereRequestedWithinSession() {
        postRepository.setSubscriptionPostsWereRequestedWithinSession(false)
    }

    /**
     * Подписка на индикатор "Есть ли новые посты" для экрана "Подписки"
     */
    fun initHasSubscriptionNewPost() {
        postRepository.getNewSubscriptionPostsObservable()
            .map { newPostEntity ->
                doNotShowRefreshButtonIfFirstTimeOpen(newPostEntity)
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { subscriptionNewPostsEntity ->
                    hasNewSubscriptionPost.value = subscriptionNewPostsEntity
                },
                { error ->
                    Timber.e("ERROR: $error")
                }
            ).addTo(disposables)
    }

    private fun doNotShowRefreshButtonIfFirstTimeOpen(newPostEntity: SubscriptionNewPostEntity):
        SubscriptionNewPostEntity {
        return if (postRepository.getSubscriptionPostsWereRequestedWithinSession()) {
            newPostEntity
        } else {
            SubscriptionNewPostEntity(false)
        }
    }

    private fun publishEvent(event: SubscriptionRoadViewEvent) {
        eventStreamSubject.onNext(event)
    }

    override fun onCleared() {
        super.onCleared()

        disposables.clear()
    }

}
