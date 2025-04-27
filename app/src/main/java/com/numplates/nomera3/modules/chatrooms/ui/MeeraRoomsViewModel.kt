package com.numplates.nomera3.modules.chatrooms.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.meera.core.extensions.empty
import com.meera.core.extensions.toBoolean
import com.meera.core.network.websocket.WebSocketMainChannel
import com.meera.core.network.websocket.WebSocketResponseException
import com.meera.db.DataStore
import com.meera.db.models.DraftUiModel
import com.meera.uikit.widgets.roomcell.UiKitRoomCellConfig
import com.numplates.nomera3.domain.interactornew.CheckSwipeDownToShowChatSearchTooltipRequiredUseCase
import com.numplates.nomera3.domain.interactornew.GetRoomDataUseCase
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.modules.chat.data.DialogApproved
import com.numplates.nomera3.modules.chat.domain.usecases.ChangeMuteStateUseCase
import com.numplates.nomera3.modules.chat.drafts.domain.GetAllDraftsUseCase
import com.numplates.nomera3.modules.chat.drafts.ui.DraftsUiMapper
import com.numplates.nomera3.modules.chat.requests.domain.usecase.RemoveRoomByIdUseCase
import com.numplates.nomera3.modules.chatrooms.data.entity.RoomsSettingsModel
import com.numplates.nomera3.modules.chatrooms.domain.interactors.RoomsInteractor
import com.numplates.nomera3.modules.chatrooms.domain.usecase.GetRoomsResponseUseCase
import com.numplates.nomera3.modules.chatrooms.domain.usecase.GetRoomsSettingsUseCase
import com.numplates.nomera3.modules.chatrooms.domain.usecase.MarkRoomDeletedUseCase
import com.numplates.nomera3.modules.chatrooms.ui.mapper.RoomsSettingsMapper
import com.numplates.nomera3.modules.chatrooms.ui.mapper.RoomsUiMapper
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum
import com.numplates.nomera3.presentation.view.adapter.newchat.MessageSettingsItemType
import com.numplates.nomera3.presentation.view.adapter.newchat.MessagesSettings
import com.numplates.nomera3.presentation.viewmodel.viewevents.ChatRoomsViewEvent
import com.numplates.nomera3.presentation.viewmodel.viewevents.SettingsPrivateMessagesState
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Collections
import javax.inject.Inject

class MeeraRoomsViewModel @Inject constructor(
    private val dataStore: DataStore,
    private val getRoomsResponseUseCase: GetRoomsResponseUseCase,
    private val getRoomsSettingsUseCase: GetRoomsSettingsUseCase,
    private val mainChannel: WebSocketMainChannel,
    private val getRoomDataUseCase: GetRoomDataUseCase,
    private val getAllDraftsUseCase: GetAllDraftsUseCase,
    private val draftsMapper: DraftsUiMapper,
    private val removeRoomByIdUseCase: RemoveRoomByIdUseCase,
    private val markRoomDeletedUseCase: MarkRoomDeletedUseCase,
    private val getUserUidUseCase: GetUserUidUseCase,
    private val roomsInteractor: RoomsInteractor,
    private val roomsSettingsMapper: RoomsSettingsMapper,
    private val roomsUiMapper: RoomsUiMapper,
    private val featureTogglesContainer: FeatureTogglesContainer,
    private val changeMuteStateUseCase: ChangeMuteStateUseCase,
    private val checkSwipeDownToShowChatSearchTooltipRequiredUseCase: CheckSwipeDownToShowChatSearchTooltipRequiredUseCase
) : ViewModel() {

    companion object {
        const val ROOM_DIALOGS_PAGE_SIZE = 30
        private const val SOCKET_ERROR_ROOM_NOT_FOUND =
            "{response={error=room not found}, status=error}"
    }

    var roomsPagingList: LiveData<PagedList<UiKitRoomCellConfig>> = MutableLiveData()

    private val _liveRoomsViewEvent = MutableSharedFlow<ChatRoomsViewEvent>()
    val liveRoomsViewEvent = _liveRoomsViewEvent.asSharedFlow()

    val userSettings = MediatorLiveData<List<MessagesSettings>>()

    private val disposables = CompositeDisposable()
    private var queryLiveData: MutableLiveData<String> = MutableLiveData<String>("")

    private var isEnabledChatRequest: Boolean = true
    private var topTs: Long? = null
    private var cachedUserSettings: List<MessagesSettings> = emptyList()
    private val _settingsInfoLiveData: LiveData<List<MessagesSettings?>> = MutableLiveData(emptyList())
    private val drafts = Collections.synchronizedList<DraftUiModel>(mutableListOf())

    init {
        observeReloadDialogs()
        getMessageSettings()
    }

    fun getUserUid() = getUserUidUseCase.invoke()

    /**
     * When exit a room we should unsubscribe from receiving events (online, typing e.t.c)
     */
    fun unsubscribeRoom() {
        // Timber.e("Unsubscribe ROOM")
        viewModelScope.launch(Dispatchers.IO) {
            mainChannel.pushRoomUnSubscribe(hashMapOf())
            // No server response
        }
    }

    fun getDrafts(reloadRooms: Boolean = false) {
        viewModelScope.launch {
            drafts.clear()
            drafts += runCatching {
                getAllDraftsUseCase.invoke().map(draftsMapper::mapDomainToUiModel)
            }.getOrDefault(emptyList())
            if (reloadRooms) roomsInteractor.getRooms()
        }
    }

    fun checkSwipeDownToShowChatSearchTooltip() {
        val isChatSearchEnabled = featureTogglesContainer.chatSearchFeatureToggle.isEnabled
        val isTooltipRequired = checkSwipeDownToShowChatSearchTooltipRequiredUseCase.invoke()
        if (isTooltipRequired && isChatSearchEnabled) {
            viewModelScope.launch {
                _liveRoomsViewEvent.emit(ChatRoomsViewEvent.ShowSwipeToShowChatSearchEvent)
            }
        }
    }

    fun getSearchQuery() = queryLiveData.value.orEmpty()

    fun initPaging(isClearSearch: Boolean) {
        if (isClearSearch) clearSearchQuery()

        val pagedListConfig = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(ROOM_DIALOGS_PAGE_SIZE)
            .setInitialLoadSizeHint(ROOM_DIALOGS_PAGE_SIZE)
            .build()

        roomsPagingList = queryLiveData.switchMap { input ->
            val dataSource = if (input.startsWith("@")) {
                dataStore.dialogDao().getDialogsByUniqueName(
                    search = input.substring(input.indexOf("@").inc()),
                    approveStatus = DialogApproved.ALLOW.key,
                )
            } else {
                dataStore.dialogDao().getDialogsBySearch(
                    search = input,
                    approveStatus = DialogApproved.ALLOW.key,
                )
            }.map { dialog ->
                drafts.find { draft -> draft.roomId == dialog.roomId }
                    ?.let { draft -> dialog.copy(draft = draft) }
                    ?: dialog
            }.map(roomsUiMapper::mapUiRoom)

            LivePagedListBuilder(dataSource, pagedListConfig).build()
        }
    }

    fun search(newQuery: String) {
        queryLiveData.value = newQuery
        pushUserSettingsToLiveData()
    }

    private fun observeReloadDialogs() {
        disposables.add(
            mainChannel.observeReloadDialogs()
                .subscribeOn(Schedulers.io())
                .subscribe({ response ->
                    Timber.d("Observe RELOAD Room [RoomsViewModel]")
                    viewModelScope.launch { roomsInteractor.getRooms() }
                }, { Timber.e(it) })
        )
    }

    fun getMessageSettings() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val lastUpdatedAtDB = dataStore.dialogDao().getRoomsMaxUpdatedAt() ?: 0L
                getRoomsResponseUseCase.invoke(
                    updatedAt = lastUpdatedAtDB,
                    topTs = topTs,
                    limit = 0                    // #optional (default=300)
                )
            }.onSuccess { roomsResponse ->
                try {
                    val settings = roomsSettingsMapper.mapRoomsSettings(roomsResponse)
                    handleRoomsSettings(settings)
                    isEnabledChatRequest = roomsResponse?.chatRequest.toBoolean()
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }.onFailure { error ->
                Timber.e("ERROR Response REST GetRooms:$error")
                val roomsSettings = getRoomsSettingsUseCase.invoke()
                handleRoomsSettings(roomsSettings)
                isEnabledChatRequest = roomsSettings?.chatRequest.toBoolean()
            }
        }
    }

    fun changeMuteState(roomConfig: UiKitRoomCellConfig) {
        viewModelScope.launch {
            changeMuteStateUseCase.invoke(roomConfig.id, !roomConfig.isMuted)
        }
    }

    private suspend fun handleRoomsSettings(settings: RoomsSettingsModel?) {
        val state = getPrivateMessagesSettingState(settings)
        withContext(Dispatchers.Main) {
            when (state.settingsType) {
                SettingsUserTypeEnum.NOBODY -> showChatLimitItem(state)
                SettingsUserTypeEnum.ALL,
                SettingsUserTypeEnum.FRIENDS -> showChatRequestItem()
            }
        }
    }

    private fun showChatLimitItem(state: SettingsPrivateMessagesState) {
        userSettings.removeSource(_settingsInfoLiveData)
        userSettings.addSource(_settingsInfoLiveData) { settings ->
            val items = mutableListOf<MessagesSettings?>()
            items.add(
                MessagesSettings(
                    itemType = MessageSettingsItemType.SETTINGS,
                    settingState = state
                )
            )
            cachedUserSettings = items.filterNotNull()
            pushUserSettingsToLiveData()
        }
    }

    private fun showChatRequestItem() {
        userSettings.removeSource(_settingsInfoLiveData)
        userSettings.addSource(_settingsInfoLiveData) { settings ->
            settings.firstOrNull()?.chatRequestData?.isMuteCounter = isEnabledChatRequest.not()
            cachedUserSettings = settings.filterNotNull()
            pushUserSettingsToLiveData()
        }
    }

    private fun pushUserSettingsToLiveData() {
        userSettings.value = if (queryLiveData.value.isNullOrEmpty()) cachedUserSettings else emptyList()
    }

    /**
     * Method for get room updatedAt before transit to chat fragment
     */
    fun triggerGoToChat(roomId: Long) {
        viewModelScope.launch {
            val roomData = runCatching {
                getRoomDataUseCase.invoke(roomId)
            }.getOrNull()
            if (roomData != null) {
                val event = ChatRoomsViewEvent.OnNavigateToChatEvent(roomData)
                _liveRoomsViewEvent.emit(event)
            }
        }
    }

    fun markRoomAsDeleted(roomId: Long, isDeleted: Boolean) {
        viewModelScope.launch {
            runCatching {
                markRoomDeletedUseCase.invoke(roomId, isDeleted = isDeleted)
            }.onFailure { Timber.e("Error when mark room as deleted UID:$roomId") }
        }
    }

    fun removeRoom(roomId: Long, isBoth: Boolean) {
        viewModelScope.launch {
            runCatching {
                removeRoomByIdUseCase.invoke(roomId, isBoth)
                getDrafts()
            }.onFailure {
                Timber.e(it)
            }
        }
    }

    fun removeRoomGroupDialog(roomId: Long) {
        val payload = hashMapOf(
            "room_id" to roomId,
        )
        disposables.add(
            mainChannel.pushDeleteRoom(payload)
                .flatMap { Observable.fromCallable { deleteAllRoomDataFromDb(roomId) } }
                .subscribeOn(Schedulers.io())
                .subscribe({
                    getDrafts()
                    Timber.d("GROUP Room (Dialog) successfully deleted")
                },
                    { error -> deleteRoomWhenSocketException(error, roomId) }
                )
        )
    }

    private fun deleteAllRoomDataFromDb(roomId: Long) {
        dataStore.dialogDao().updateUnreadMessageCount(roomId, 0)
        dataStore.dialogDao().deleteById(roomId)
        dataStore.messageDao().deleteMessagesByRoomId(roomId)
    }

    private fun deleteRoomWhenSocketException(error: Throwable, roomId: Long) {
        try {
            val err = error as WebSocketResponseException
            if (err.getPayload().toString() == SOCKET_ERROR_ROOM_NOT_FOUND) {
                deleteAllRoomDataFromDb(roomId)
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
        Timber.e("SOCKET ERROR: delete room: $error")
    }

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }

    private fun getPrivateMessagesSettingState(settings: RoomsSettingsModel?): SettingsPrivateMessagesState {
        return SettingsPrivateMessagesState(
            whiteListCount = settings?.countWhiteList ?: 0,
            blackListCount = settings?.countBlackList ?: 0,
            settingsType = when (settings?.whoCanChat) {
                0 -> SettingsUserTypeEnum.NOBODY
                1 -> SettingsUserTypeEnum.ALL
                else -> SettingsUserTypeEnum.FRIENDS
            }
        )
    }

    private fun clearSearchQuery() {
        queryLiveData.value = String.empty()
    }
}
