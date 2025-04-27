package com.numplates.nomera3.modules.contentsharing.ui.rooms

import androidx.lifecycle.viewModelScope
import com.meera.core.base.viewmodel.BaseViewModel
import com.numplates.nomera3.domain.interactornew.IsUserAuthorizedUseCase
import com.numplates.nomera3.modules.baseCore.ResourceManager
import com.numplates.nomera3.modules.contentsharing.domain.usecase.GetShareItemsUseCase
import com.numplates.nomera3.modules.contentsharing.ui.infrastructure.SharingDataCache
import com.numplates.nomera3.modules.share.ui.entity.UIShareItem
import com.numplates.nomera3.modules.share.ui.entity.toUIShareItems
import com.numplates.nomera3.presentation.utils.networkconn.NetworkStatusProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject

private const val CODE_UNAUTHORIZED = 401

class SharingRoomsViewModel @Inject constructor(
    private val getShareItemsUseCase: GetShareItemsUseCase,
    private val resourceManager: ResourceManager,
    private val isUserAuthorizedUseCase: IsUserAuthorizedUseCase,
    private val networkStatusProvider: NetworkStatusProvider,
    private val contentChecker: ContentChecker,
) : BaseViewModel<SharingRoomsState, SharingRoomsEffect, SharingRoomsAction>() {

    private val sharingDataCache: SharingDataCache = SharingDataCache
    private val stateValue: SharingRoomsState
        get() = _state.value ?: SharingRoomsState()

    private val _shareItems: MutableStateFlow<List<UIShareItem>> = MutableStateFlow(sharingDataCache.getUsers())
    val shareItems by lazy { _shareItems.filterNotNull() }

    @Suppress("REDUNDANT_ELSE_IN_WHEN")
    override fun handleUIAction(action: SharingRoomsAction) {
        viewModelScope.launch {
            when (action) {
                is SharingRoomsAction.LoadAvailableChatsToShare -> {
                    loadChatsOrRedirectToAuth(null)
                }

                is SharingRoomsAction.QueryShareItems -> {
                    loadChatsOrRedirectToAuth(action.query)
                }

                is SharingRoomsAction.ChangeSelectedState -> {
                    changeCheckedState(action.item, action.isChecked)
                }

                is SharingRoomsAction.SendContentToChats -> {
                    sendContentToChats()
                }

                is SharingRoomsAction.ShareContentToChats -> {
                    shareContentToChats(action.content)
                }

                else -> error("Unsupported state. Make sure some of ${SharingRoomsAction::class.simpleName} used.")
            }
        }
    }

    fun canBeCheckedMoreItems(): Boolean {
        return sharingDataCache.getUsers().count { it.isChecked } < MAX_SELECTED_ALLOWED
    }

    private suspend fun loadChatsOrRedirectToAuth(query: String?) {
        if (isUserAuthorizedUseCase.invoke()) {
            loadShareItemsData(query)
        } else {
            navigateToUnauthorizedScreen()
        }
    }

    private suspend fun sendContentToChats() {
        if (!networkStatusProvider.isInternetConnected()) {
            emitEffect(SharingRoomsEffect.SendNetworkError)
        } else if (contentChecker.hasIncorrectVideo()) {
            emitEffect(SharingRoomsEffect.SendVideoDurationError)
        } else {
            emitEffect(SharingRoomsEffect.SendContentToChats)
        }
    }

    private suspend fun changeCheckedState(item: UIShareItem, isChecked: Boolean) {
        val updatedItems = _shareItems.value.map { shareItem ->
            if (shareItem.id == item.id) shareItem.copy(isChecked = isChecked) else shareItem
        }
        sharingDataCache.cacheUsers(updatedItems)
        _shareItems.emit(updatedItems)
    }

    private suspend fun navigateToUnauthorizedScreen() {
        sharingDataCache.cacheUsers(emptyList())
        _state.emit(stateValue.copy(isRedirecting = true))
    }

    private suspend fun shareContentToChats(content: String?) {
        sharingDataCache.messageComment = content
        _effect.emit(SharingRoomsEffect.ShareContentToChats)
    }

    private suspend fun loadShareItemsData(query: String?) {
        runCatching {
            emitState(stateValue.copy(isLoading = true, query = query))
            getShareItemsUseCase.invoke(query, null, null)
        }
            .onSuccess { items ->
                emitState(stateValue.copy(isLoading = false))
                emitEffect(SharingRoomsEffect.ScheduleScrollListToTop)
                if (items.isEmpty()) {
                    _shareItems.emit(emptyList())
                } else {
                    val loadedItems = items.toUIShareItems(resourceManager).associateBy({ it.id }, { it })
                    val checkedItems =
                        sharingDataCache.getUsers().filter { it.isChecked }.associateBy({ it.id }, { it })
                    val updatedList = mutableMapOf<String, UIShareItem>()
                    updatedList.putAll(loadedItems)
                    updatedList.putAll(checkedItems)
                    _shareItems.emit(updatedList.values.toList())
                }
            }
            .onFailure { error ->
                Timber.e(error)
                emitState(
                    stateValue.copy(isRedirecting = (error as? HttpException)?.code() == CODE_UNAUTHORIZED)
                )
            }
    }
}
