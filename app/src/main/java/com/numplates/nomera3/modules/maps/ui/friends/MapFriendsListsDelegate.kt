package com.numplates.nomera3.modules.maps.ui.friends

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.meera.core.extensions.toBoolean
import com.numplates.nomera3.modules.chat.domain.usecases.CacheCompanionUserForChatInitUseCase
import com.numplates.nomera3.modules.maps.domain.events.model.GetMapFriendsParamsModel
import com.numplates.nomera3.modules.maps.domain.events.usecase.GetMapFriendsUseCase
import com.numplates.nomera3.modules.maps.domain.usecase.GetMapSettingsUseCase
import com.numplates.nomera3.modules.maps.domain.usecase.SetMapSettingsUseCase
import com.numplates.nomera3.modules.maps.ui.friends.mapper.MapFriendUiMapper
import com.numplates.nomera3.modules.maps.ui.friends.model.EnableFriendsDialogConfirmAction
import com.numplates.nomera3.modules.maps.ui.friends.model.MapFriendsListUiAction
import com.numplates.nomera3.modules.maps.ui.friends.model.MapFriendsListUiEffect
import com.numplates.nomera3.modules.maps.ui.friends.model.MapFriendsListUiModel
import com.numplates.nomera3.modules.maps.ui.model.MapUiEffect
import com.numplates.nomera3.modules.userprofile.domain.maper.toChatInitUserProfile
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserSimpleModel
import com.numplates.nomera3.modules.userprofile.domain.usecase.GetProfileUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

private const val SEARCH_DEBOUNCE = 200L

class MapFriendsListsDelegate @Inject constructor(
    private val uiMapper: MapFriendUiMapper,
    private val getMapFriendsUseCase: GetMapFriendsUseCase,
    private val getProfileUserCase: GetProfileUseCase,
    private val cacheCompanionUserUseCase: CacheCompanionUserForChatInitUseCase,
    private val getMapSettingsUseCase: GetMapSettingsUseCase,
    private val setMapSettingsUseCase: SetMapSettingsUseCase,
) {

    private var initJob: Job? = null
    private val eventParticipantItemsFlow = MutableStateFlow<List<UserSimpleModel>>(emptyList())

    private var config: MapFriendsListsDelegateConfigUiModel? = null
    private var lastUserIdClicked: Long? = null
    private var lastPosition: Int? = null

    private val pagingDataFlow = MutableStateFlow(
        PagingDataUiModel(
            isLoadingNextPage = false,
            isLastPage = false,
            search = null,
            true
        )
    )

    private val _uiEffectsFlow = MutableSharedFlow<MapFriendsListUiEffect>()
    val uiEffectsFlow = _uiEffectsFlow.asSharedFlow()

    private val searchInput = MutableSharedFlow<String>()

    private var searchJob: Job? = null

    val uiModelStateFlow = combine(
        eventParticipantItemsFlow,
        pagingDataFlow
    ) { users, pagingData ->
        uiMapper.mapFriendsListItems(
            participantUsers = users,
            pagingData = pagingData,
        )
    }

    fun initialize(config: MapFriendsListsDelegateConfigUiModel) {
        this.config = config
    }

    val liveUiModel: LiveData<MapFriendsListUiModel> = uiModelStateFlow.asLiveData()

    fun init() {
        config?.scope?.launch {
            searchInput.debounce(SEARCH_DEBOUNCE).collect { search ->
                getNextPage(true, search)
            }
        }
        config?.mapBottomSheetDialogIsOpenFlow?.value = true
        config?.scope?.launch {
            doGetNextPage(true)
        }
    }

    fun handleUiAction(uiAction: MapFriendsListUiAction) {
        when (uiAction) {
            is MapFriendsListUiAction.ParticipantClicked -> {
                onFriendClicked(uiAction)
            }

            MapFriendsListUiAction.LoadNextPageRequested -> getNextPage()
            MapFriendsListUiAction.Close -> handleListClosed()
            is MapFriendsListUiAction.SendMessageClicked -> onSendMessageClicked(uiAction)
            is MapFriendsListUiAction.SearchFriends -> {
                config?.scope?.launch {
                    searchInput.emit(uiAction.search)
                }
            }

            is MapFriendsListUiAction.MapFriendListItemSelected -> {
                uiAction.item?.let {
                    sendUiEffect(MapFriendsListUiEffect.MapFriendListItemSelected(it))
                }
            }

            MapFriendsListUiAction.OpenFriendList -> {
                config?.scope?.launch {
                    init()
                    config?.mapBottomSheetDialogIsOpenFlow?.value = true

                    val mapSettings = getMapSettingsUseCase.invoke()
                    if (mapSettings.showFriends.not()) {
                        config?.uiEffectsFlow?.emit(
                            MapUiEffect.ShowEnableFriendsLayerDialog(EnableFriendsDialogConfirmAction.ON_FRIENDS_LAYER)
                        )
                        return@launch
                    } else {
                        config?.uiEffectsFlow?.emit(MapUiEffect.ShowFriendsListStub)
                        config?.uiEffectsFlow?.emit(MapUiEffect.HideMapControls)
                    }
                }
            }

            MapFriendsListUiAction.OpenPeople -> {
                emitUiEffect(MapUiEffect.OpenFriends)
            }

            MapFriendsListUiAction.EnableFriendLayer -> {
                enableFriendLayer()
            }

            MapFriendsListUiAction.HideWidget -> {
                emitUiEffect(MapUiEffect.HideWidget)
            }

            MapFriendsListUiAction.ShowWidget -> {
                emitUiEffect(MapUiEffect.ShowWidget)
            }

            MapFriendsListUiAction.UpdateSelectedUser -> {
                config?.scope?.launch {
                    runCatching {

                        if (lastUserIdClicked == null || lastPosition == null) return@launch

                        val user = runCatching {
                            getProfileUserCase.invoke(lastUserIdClicked!!)
                        }.getOrNull()
                        val currentList = eventParticipantItemsFlow.value.toMutableList()
                        var result: List<UserSimpleModel> = emptyList()

                        if (user?.blacklistedByMe.toBoolean()) {

                            lastPosition?.let {
                                result = currentList.apply {
                                    removeAt(it)
                                }
                            }
                            eventParticipantItemsFlow.value = result
                        } else {
                            val mappedUser = user?.let { uiMapper.mapUserUiModel(it) }
                            val position = currentList.indexOfFirst { it.userId == mappedUser?.userId }
                            // TODO: https://nomera.atlassian.net/browse/BR-30681
                            currentList[position] = mappedUser!!
                            pagingDataFlow.value =
                                pagingDataFlow.value.copy(updatePosition = false)
                            eventParticipantItemsFlow.value = currentList
                        }
                    }
                }
            }
        }
    }

    private fun emitUiEffect(uiEffect: MapUiEffect) {
        config?.scope?.launch {
            config?.uiEffectsFlow?.emit(uiEffect)
        }
    }

    private fun enableFriendLayer() {
        config?.scope?.launch {
            val mapSettings = getMapSettingsUseCase.invoke()
            setMapSettingsUseCase.invoke(mapSettings.copy(showFriends = true))
            //delay for hiding of nav bar
            delay(500)
            handleUiAction(MapFriendsListUiAction.OpenFriendList)
        }
    }

    private fun handleListClosed() {
        val config = config ?: return
        initJob?.cancel()
        initJob = null
        config.scope.launch {
            pagingDataFlow.value = PagingDataUiModel(
                isLoadingNextPage = false,
                isLastPage = false,
                search = "",
                firstStart = true,
                updatePosition = true
            )
            config.mapBottomSheetDialogIsOpenFlow.value = false
            config.uiEffectsFlow.emit(MapUiEffect.ShowMapControls)
            config.uiEffectsFlow.emit(MapUiEffect.FocusMapItem(null))
            config.uiEffectsFlow.emit(MapUiEffect.ResetGlobalMap)
            eventParticipantItemsFlow.value = emptyList()
        }
    }

    private fun onSendMessageClicked(uiAction: MapFriendsListUiAction.SendMessageClicked) {
        lastUserIdClicked = uiAction.itemUiModel.userId
        lastPosition = uiAction.position
        config?.scope?.launch {
            val supportUserId = uiAction.itemUiModel.userId
            val supportUser = runCatching {
                getProfileUserCase.invoke(supportUserId)
            }.getOrNull()?.toChatInitUserProfile()
            cacheCompanionUserUseCase.invoke(supportUser)
            sendUiEffect(
                MapFriendsListUiEffect.SendMessage(
                    uiMapper.mapUserUiModel(uiAction.itemUiModel),
                    uiAction.position
                )
            )
        }
    }

    private fun onFriendClicked(uiAction: MapFriendsListUiAction.ParticipantClicked) {
        lastUserIdClicked = uiAction.itemUiModel.userId
        lastPosition = uiAction.position
        if (uiAction.itemUiModel.moments?.hasMoments == true && uiAction.isAvatarClicked) {
            sendUiEffect(
                MapFriendsListUiEffect.OpenMoments(
                    uiMapper.mapUserSnippetModel(uiAction.itemUiModel),
                    uiAction.view,
                    uiAction.position
                )
            )
            config?.scope?.launch(Dispatchers.IO) {
                val currentList = eventParticipantItemsFlow.value
                val currentItem = currentList.find { it.userId == uiAction.itemUiModel.userId }
                val resultItem = currentItem?.copy(
                    moments = currentItem.moments?.copy(hasNewMoments = 0)
                )

                if (resultItem != null) {
                    val result = currentList.toMutableList().apply {
                        set(uiAction.position, resultItem)
                    }
                    eventParticipantItemsFlow.value = result
                }
            }
        } else {
            sendUiEffect(
                MapFriendsListUiEffect.OpenUserProfile(
                    uiMapper.mapUserUiModel(uiAction.itemUiModel),
                    uiAction.position,
                    uiMapper.mapUserSnippetModel(uiAction.itemUiModel),
                )
            )
        }
    }

    private fun sendUiEffect(uiEffect: MapFriendsListUiEffect) {
        launchCatching {
            _uiEffectsFlow.emit(uiEffect)
        }
    }

    private fun getNextPage(shouldReset: Boolean = false, search: String? = null) {
        searchJob?.cancel()
        searchJob = config?.scope?.launch {
            doGetNextPage(shouldReset = shouldReset, search = search)
        }
    }

    private fun launchCatching(finally: suspend () -> Unit = {}, block: suspend () -> Unit) {
        config?.scope?.launch {
            try {
                block.invoke()
            } catch (e: Exception) {
                Timber.e(e)
            } finally {
                finally.invoke()
            }
        }
    }

    private suspend fun doGetNextPage(shouldReset: Boolean = false, search: String? = null) {
        try {
            pagingDataFlow.value =
                pagingDataFlow.value.copy(
                    isLoadingNextPage = true,
                    firstStart = false,
                    search = search,
                    updatePosition = true
                )
            val offset = if (shouldReset) 0 else eventParticipantItemsFlow.value.size
            val params = GetMapFriendsParamsModel(
                offset = offset,
                limit = PAGE_SIZE,
                search = search
            )
            val pageItems = getMapFriendsUseCase.invoke(params)
            if (pageItems.size < PAGE_SIZE) {
                pagingDataFlow.value = pagingDataFlow.value.copy(isLastPage = true)
            }
            eventParticipantItemsFlow.value = if (shouldReset) {
                pageItems
            } else {
                eventParticipantItemsFlow.value.plus(pageItems)
            }
            sendUiEffect(MapFriendsListUiEffect.ListState(eventParticipantItemsFlow.value.isEmpty()))
        } catch (e: Exception) {
            Timber.e(e)
        } finally {
            pagingDataFlow.value = pagingDataFlow.value.copy(isLoadingNextPage = false)
        }
    }

    data class PagingDataUiModel(
        val isLoadingNextPage: Boolean,
        val isLastPage: Boolean,
        val search: String?,
        val firstStart: Boolean?,
        val updatePosition: Boolean = false
    )

    companion object {
        private const val PAGE_SIZE = 30
    }
}
