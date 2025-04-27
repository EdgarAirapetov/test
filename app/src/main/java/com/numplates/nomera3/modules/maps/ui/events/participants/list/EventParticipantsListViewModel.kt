package com.numplates.nomera3.modules.maps.ui.events.participants.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.modules.maps.domain.analytics.MapEventsAnalyticsInteractor
import com.numplates.nomera3.modules.maps.domain.analytics.model.MapEventIdParamsAnalyticsModel
import com.numplates.nomera3.modules.maps.domain.events.model.GetEventParticipantsParamsModel
import com.numplates.nomera3.modules.maps.domain.events.usecase.GetEventParticipantsUseCase
import com.numplates.nomera3.modules.maps.domain.events.usecase.GetEventUseCase
import com.numplates.nomera3.modules.maps.domain.events.usecase.LeaveEventUseCase
import com.numplates.nomera3.modules.maps.domain.events.usecase.RemoveEventParticipantUseCase
import com.numplates.nomera3.modules.maps.domain.usecase.GetEventPostUseCase
import com.numplates.nomera3.modules.maps.ui.events.participants.list.mapper.EventParticipantListUiMapper
import com.numplates.nomera3.modules.maps.ui.events.participants.list.model.EventParticipantsListItemUiModel
import com.numplates.nomera3.modules.maps.ui.events.participants.list.model.EventParticipantsListUiAction
import com.numplates.nomera3.modules.maps.ui.events.participants.list.model.EventParticipantsListUiEffect
import com.numplates.nomera3.modules.maps.ui.events.participants.list.model.EventParticipantsListUiModel
import com.numplates.nomera3.modules.maps.ui.events.participants.list.model.EventParticipantsParamsUiModel
import com.numplates.nomera3.modules.maps.ui.events.participants.list.model.ParticipantRemoveOption
import com.numplates.nomera3.modules.search.data.states.UserState
import com.numplates.nomera3.modules.user.domain.usecase.AddUserToFriendObserverParams
import com.numplates.nomera3.modules.user.domain.usecase.UserStateObserverUseCase
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserSimpleModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class EventParticipantsListViewModel @Inject constructor(
    private val getEventParticipantsUseCase: GetEventParticipantsUseCase,
    private val getUserUidUseCase: GetUserUidUseCase,
    private val getEventPostUseCase: GetEventPostUseCase,
    private val leaveEventUseCase: LeaveEventUseCase,
    private val removeEventParticipantUseCase: RemoveEventParticipantUseCase,
    private val getEventUseCase: GetEventUseCase,
    private val uiMapper: EventParticipantListUiMapper,
    private val userStateObserverUseCase: UserStateObserverUseCase,
    private val mapEventsAnalyticsInteractor: MapEventsAnalyticsInteractor
) : ViewModel() {
    private val compositeDisposable = CompositeDisposable()
    private var params: EventParticipantsParamsUiModel? = null
    private val eventParticipantItemsFlow = MutableStateFlow<List<UserSimpleModel>>(emptyList())
    private val eventParticipantsCountFlow = MutableStateFlow(0)
    private val idsDataFlow = MutableStateFlow<IdsDataUiModel?>(null)
    private val isRefreshingFlow = MutableStateFlow(false)
    private val pagingDataFlow = MutableStateFlow(
        PagingDataUiModel(
            isLoadingNextPage = false,
            isLastPage = false
        )
    )
    private val uiModelStateFlow = combine(
        eventParticipantItemsFlow,
        eventParticipantsCountFlow,
        idsDataFlow,
        isRefreshingFlow,
        pagingDataFlow
    ) { users, count, idsData, isRefreshing, pagingData ->
        uiMapper.mapUiModel(
            participantUsers = users,
            participantsCount = count,
            hostUserId = idsData?.hostUserId,
            myUserId = idsData?.myUserId,
            isRefreshing = isRefreshing,
            isLoadingNextPage = pagingData.isLoadingNextPage,
            isLastPage = pagingData.isLastPage
        )
    }
    val liveUiModel: LiveData<EventParticipantsListUiModel> = uiModelStateFlow.asLiveData()

    private val _uiEffectsFlow = MutableSharedFlow<EventParticipantsListUiEffect>()
    val uiEffectsFlow = _uiEffectsFlow.asSharedFlow()

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

    fun handleUiAction(uiAction: EventParticipantsListUiAction) {
        when (uiAction) {
            is EventParticipantsListUiAction.ViewInitialized -> onViewInitialized(uiAction.params)
            is EventParticipantsListUiAction.ParticipantOptionsClicked -> showParticipantOptionsMenu(uiAction.item)
            is EventParticipantsListUiAction.ParticipantClicked -> onParticipantClicked(uiAction)
            EventParticipantsListUiAction.RefreshRequested -> refreshParticipantsList()
            EventParticipantsListUiAction.LoadNextPageRequested -> getNextPage()
            EventParticipantsListUiAction.LeaveEventClicked -> leaveEvent()
            is EventParticipantsListUiAction.RemoveParticipantClicked -> removeParticipant(uiAction.userId)
        }
    }

    private fun onViewInitialized(params: EventParticipantsParamsUiModel) {
        this.params = params
        observeUserBlockStatusChanges()
        launchCatching {
            setInitialValues(params)
            refreshParticipantsList()
        }
    }

    private fun observeUserBlockStatusChanges() {
        userStateObserverUseCase.execute(AddUserToFriendObserverParams())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::processUserState, Timber::e)
            .addTo(compositeDisposable)
    }

    private fun processUserState(userState: UserState) {
        if (userState is UserState.BlockStatusUserChanged && userState.isBlocked) {
            eventParticipantItemsFlow.value = eventParticipantItemsFlow.value.filter { it.userId != userState.userId }
            refreshParticipantsCount()
            updateEventPost()
        }
    }

    private fun updateEventPost() {
        val postId = params?.postId ?: return
        launchCatching {
           getEventPostUseCase.invoke(postId)
        }
    }

    private suspend fun setInitialValues(params: EventParticipantsParamsUiModel) {
        eventParticipantsCountFlow.value = params.participantsCount
        val hostUserId = getEventPostUseCase.invoke(params.postId, true).user?.userId
            ?: throw RuntimeException("Post ${params.postId} has no author userId")
        val myUserId = getUserUidUseCase.invoke()
        idsDataFlow.value = IdsDataUiModel(hostUserId = hostUserId, myUserId = myUserId)
    }

    private fun onParticipantClicked(uiAction: EventParticipantsListUiAction.ParticipantClicked) {
        sendUiEffect(EventParticipantsListUiEffect.OpenUserProfile(uiAction.userId))
    }

    fun refreshParticipantsList() {
        if (isRefreshingFlow.value) return
        launchCatching(
            finally = { isRefreshingFlow.value = false }
        ) {
            isRefreshingFlow.value = true
            doGetNextPage(true)
            doRefreshParticipantsCount()
        }
    }

    private fun refreshParticipantsCount() {
        launchCatching {
            doRefreshParticipantsCount()
        }
    }

    private suspend fun doRefreshParticipantsCount() {
        val postId = params?.postId ?: return
        val participant = getEventUseCase.invoke(postId).participation
        val participantsCount = participant.participantsCount
        eventParticipantsCountFlow.emit(participantsCount)
    }

    private fun showParticipantOptionsMenu(itemUiModel: EventParticipantsListItemUiModel) {
        launchCatching {
            val ids = idsDataFlow.value ?: return@launchCatching
            val hostIsMe = ids.myUserId == ids.hostUserId
            val removeOption = when {
                itemUiModel.isHost -> ParticipantRemoveOption.RemoveNotAvailable
                hostIsMe -> ParticipantRemoveOption.CanRemove
                itemUiModel.isMe -> ParticipantRemoveOption.CanLeave
                else -> ParticipantRemoveOption.RemoveNotAvailable
            }
            val uiEffect = EventParticipantsListUiEffect.ShowParticipantMenu(
                userId = itemUiModel.userId,
                removeOption = removeOption
            )
            _uiEffectsFlow.emit(uiEffect)
        }
    }

    private fun leaveEvent() {
        val eventId = params?.eventId ?: return
        launchCatching {
            leaveEventUseCase.invoke(eventId)
            logMapEventMemberDeleteYouself()
            refreshParticipantsList()
        }
    }

    private fun removeParticipant(userId: Long) {
        val eventId = params?.eventId ?: return
        launchCatching {
            removeEventParticipantUseCase.invoke(eventId = eventId, userId = userId)
            logMapEventMemberDelete()
            eventParticipantItemsFlow.value = eventParticipantItemsFlow.value.filter { it.userId != userId }
            doRefreshParticipantsCount()
        }
    }

    private fun getNextPage(shouldReset: Boolean = false) {
        viewModelScope.launch {
            doGetNextPage(shouldReset = shouldReset)
        }
    }

    private suspend fun doGetNextPage(shouldReset: Boolean = false) {
        val eventId = params?.eventId ?: return
        if (pagingDataFlow.value.isLoadingNextPage) return
        try {
            pagingDataFlow.value = pagingDataFlow.value.copy(isLoadingNextPage = true)
            val offset = if (shouldReset) 0 else eventParticipantItemsFlow.value.size
            val params = GetEventParticipantsParamsModel(
                eventId = eventId,
                offset = offset,
                limit = PAGE_SIZE
            )
            val pageItems = getEventParticipantsUseCase.invoke(params)
            if (pageItems.size < PAGE_SIZE) {
                pagingDataFlow.value = pagingDataFlow.value.copy(isLastPage = true)
            }
            eventParticipantItemsFlow.value = if (shouldReset) {
                pageItems
            } else {
                eventParticipantItemsFlow.value.plus(pageItems)
            }
        } catch (e: Exception) {
            Timber.e(e)
        } finally {
            pagingDataFlow.value = pagingDataFlow.value.copy(isLoadingNextPage = false)
        }
    }

    private fun sendUiEffect(uiEffect: EventParticipantsListUiEffect) {
        launchCatching {
            _uiEffectsFlow.emit(uiEffect)
        }
    }

    private fun launchCatching(finally: suspend () -> Unit = {}, block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block.invoke()
            } catch (e: Exception) {
                Timber.e(e)
            } finally {
                finally.invoke()
            }
        }
    }

    private fun logMapEventMemberDelete() {
        val eventId = params?.eventId ?: return
        val authorId = idsDataFlow.value?.hostUserId ?: return
        mapEventsAnalyticsInteractor.logMapEventMemberDelete(MapEventIdParamsAnalyticsModel(
            eventId = eventId,
            authorId = authorId
        ))
    }

    private fun logMapEventMemberDeleteYouself() {
        val eventId = params?.eventId ?: return
        val authorId = idsDataFlow.value?.hostUserId ?: return
        mapEventsAnalyticsInteractor.logMapEventMemberDeleteYouself(MapEventIdParamsAnalyticsModel(
            eventId = eventId,
            authorId = authorId
        ))
    }

    private data class IdsDataUiModel(
        val hostUserId: Long,
        val myUserId: Long
    )

    private data class PagingDataUiModel(
        val isLoadingNextPage: Boolean,
        val isLastPage: Boolean
    )

    companion object {
        private const val PAGE_SIZE = 30
    }
}
