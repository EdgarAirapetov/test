package com.numplates.nomera3.modules.redesign.fragments.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.meera.core.extensions.combineWith
import com.numplates.nomera3.domain.interactornew.NotificationCounterUseCase
import com.numplates.nomera3.modules.chat.requests.domain.usecase.ChatRequestInfoUseCase
import com.numplates.nomera3.modules.chatrooms.domain.usecase.ObserveUnreadMessageCountUseCase
import com.numplates.nomera3.modules.notifications.service.SyncNotificationService
import com.numplates.nomera3.modules.peoples.domain.usecase.GetRelatedUsersAndCacheUseCase
import com.numplates.nomera3.modules.peoples.domain.usecase.GetTopUsersAndCacheUseCase
import com.numplates.nomera3.modules.redesign.deeplink.DeeplinkController
import com.numplates.nomera3.modules.redesign.fragments.main.actions.MeeraMainContainerActions
import com.numplates.nomera3.modules.redesign.fragments.main.state.BottomNavState
import com.numplates.nomera3.presentation.viewmodel.MainFragmentViewModel.Companion.PEOPLE_OFFSET_LIMIT
import com.numplates.nomera3.presentation.viewmodel.MainFragmentViewModel.Companion.PEOPLE_PAGE_LIMIT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class MeeraMainContainerViewModel @Inject constructor(
    private val notificationCounterUseCase: NotificationCounterUseCase,
    private val syncService: SyncNotificationService,
    private val unreadMessagesUseCase: ObserveUnreadMessageCountUseCase,
    private val chatRequestInfoUseCase: ChatRequestInfoUseCase,
    private val getRelatedUsersAndCacheUseCase: GetRelatedUsersAndCacheUseCase,
    private val getTopUsersAndCacheUseCase: GetTopUsersAndCacheUseCase,
    val deeplinkController: DeeplinkController
) : ViewModel() {

    val totalChatUnreadCounter: LiveData<Int?> = getTotalUnreadCounter()

    val notificationCounterFlow: StateFlow<Int>
        get() = _notificationCounterFlow
    private val _notificationCounterFlow = MutableStateFlow(0)


    private val _bottomNavState = MutableLiveData(BottomNavState())
    val bottomNavState: LiveData<BottomNavState> = _bottomNavState

    fun handleAction(action: MeeraMainContainerActions) {
        when (action) {
            is MeeraMainContainerActions.InitNotificationCounter -> initNotificationCounter()
            is MeeraMainContainerActions.PreloadPeopleContent -> preloadPeopleContent()
        }
    }

    private fun preloadPeopleContent() {
        saveRelatedUsers()
        saveTopUsers()
    }

    private fun saveRelatedUsers() {
        viewModelScope.launch {
            runCatching {
                getRelatedUsersAndCacheUseCase.invoke(
                    limit = PEOPLE_PAGE_LIMIT,
                    offset = PEOPLE_OFFSET_LIMIT
                )
            }.onFailure { t ->
                Timber.e(t)
            }
        }
    }

    private fun saveTopUsers() {
        viewModelScope.launch {
            runCatching {
                getTopUsersAndCacheUseCase.invoke(
                    limit = PEOPLE_PAGE_LIMIT,
                    offset = PEOPLE_OFFSET_LIMIT
                )
            }.onFailure {
                Timber.e(it)
            }
        }
    }

    private fun initNotificationCounter() {
        getCurrentNotificationCounter()
        observeChangeNotificationCounter()
    }

    private fun getCurrentNotificationCounter() {
        viewModelScope.launch {
            runCatching {
                val response = notificationCounterUseCase.getCounter()
                val currentNotificationCount = response.data.count
                if (currentNotificationCount != null) {
                    _notificationCounterFlow.emit(currentNotificationCount)
                }
            }
        }
    }

    private fun observeChangeNotificationCounter() {
        syncService.unreadNotificationCountFlowObservable()
            .onEach { getCurrentNotificationCounter() }
            .flowOn(Dispatchers.IO)
            .launchIn(viewModelScope)
    }

    /**
     * Счётчик считает количество непрочитанных сообщений + запросы на переписку
     * в которых есть хотя бы одно непрочитанное сообщение
     */
    private fun getTotalUnreadCounter(): LiveData<Int?> {
        val messageCounter = unreadMessagesUseCase.invoke()
        val requestCounter = chatRequestInfoUseCase.invoke().map { it?.unreadMessageCount }
        val totalCounter = messageCounter.combineWith(requestCounter) { msg, request ->
            return@combineWith  if (msg != null && request != null) {
                msg + request
            } else {
                null
            }
        }
        return totalCounter
    }

}
