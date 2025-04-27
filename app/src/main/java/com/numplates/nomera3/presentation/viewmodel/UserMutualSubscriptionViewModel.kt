package com.numplates.nomera3.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.domain.interactornew.GetUserMutualUseCase
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.domain.util.UserSubscriptionsUiMapper
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mutual_friends.AmplitudeHowSelectedMutualFriendsProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mutual_friends.AmplitudeMutualFriendsAnalytic
import com.numplates.nomera3.modules.baseCore.helper.amplitude.mutual_friends.AmplitudeSelectedMutualFriendsTabProperty
import com.numplates.nomera3.modules.user.domain.effect.UserSettingsEffect
import com.numplates.nomera3.modules.user.domain.usecase.GetUserSettingsStateChangedUseCase
import com.numplates.nomera3.presentation.model.adaptermodel.FriendsFollowersUiModel
import com.numplates.nomera3.presentation.view.fragments.entity.UserFriendsFollowersUiState
import com.numplates.nomera3.presentation.view.fragments.entity.UserSubscriptionViewEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class UserMutualSubscriptionViewModel @Inject constructor(
    private val getUserMutualUseCase: GetUserMutualUseCase,
    private val getUserUidUseCase: GetUserUidUseCase,
    private val getUserSettingsStateChangedUseCase: GetUserSettingsStateChangedUseCase,
    private val amplitudeMutualFriendsAnalytic: AmplitudeMutualFriendsAnalytic
) : ViewModel() {

    private val _sameSubscriptionState = MutableLiveData<UserFriendsFollowersUiState>()
    val sameSubscriptionState: LiveData<UserFriendsFollowersUiState> = _sameSubscriptionState
    private val _userMutualSubscriptionViewEvent = MutableSharedFlow<UserSubscriptionViewEvent>()
    val userMutualSubscriptionViewEvent: SharedFlow<UserSubscriptionViewEvent> =
        _userMutualSubscriptionViewEvent

    private val userSearchState = UserSearchState()
    private val mapper = UserSubscriptionsUiMapper()

    /**
     * Данный arrayList будет хранить весь имеющийся список
     * Устанавливаются данные в 2 случаях:
     * 1.Отрабатывает пагинация
     * 2.Самый первый запрос
     */
    private var currentList = mutableListOf<FriendsFollowersUiModel>()

    // Pagination
    var isLoading = false
        private set

    var isLastPage = false
        private set

    init {
        observeFriendStatusChanged()
    }

    /**
     * В методе мы получаем список общих подписок
     * @param userId - id юзера
     * @param isRefreshing - Происходит ли запрос через SwipeRefreshLayout
     * @param querySearch - Поисковая строка
     * @param clickedItem - Модель, если юзер кликнул на состояние иконки
     */
    fun getUserMutualSubscription(
        userId: Long,
        isRefreshing: Boolean = false,
        querySearch: String? = null,
        clickedItem: FriendsFollowersUiModel? = null,
        offset: Int = DEFAULT_OFFSET,
    ) {
        isLoading = true
        if (isRefreshing) pushUiStateList(isRefreshing = true)
        userSearchState.setSearchInput(querySearch)
        viewModelScope.launch {
            runCatching {
                getUserMutualUseCase.invoke(
                    userId = userId,
                    offset = offset,
                    querySearch = querySearch,
                    limit = DEFAULT_PAGE_LIMIT
                )
            }.onSuccess { data ->
                isLoading = false
                handleListResponse(
                    ownUserId = getUserUidUseCase.invoke(),
                    mutualUsers = data?.mutualUserList,
                    offset = offset,
                    clickedItem = clickedItem
                )
                isLastPage = data?.mutualUserList?.isEmpty() == true
            }.onFailure { t ->
                isLastPage = false
                isLoading = false
                handleErrorResponse(t)
            }
        }
    }

    fun isSearch() = userSearchState.isSearch

    fun getSearchInput() = userSearchState.searchInputText

    fun clearSearchList() = userSearchState.resetSearchList()

    /**
     * Заменяем поисковый arrayList на текущий
     */
    fun replaceSearchListToCurrentList() {
        handleUiState()
    }

    fun searchOpenStateChanged(isSearchOpen: Boolean) {
        if (isSearchOpen) userSearchState.searchOpen() else userSearchState.resetSearch()
    }

    fun logMutualFriendsAmplitude() {
        amplitudeMutualFriendsAnalytic.logMutualFriendsTabSelected(
            friendTabSelected = AmplitudeSelectedMutualFriendsTabProperty.MUTUAL_FOLLOWS,
            typeSelected = AmplitudeHowSelectedMutualFriendsProperty.TABS_INSIDE_BLOCK
        )
    }

    private fun pushUiStateList(isRefreshing: Boolean = false) {
        val newList = mutableListOf<FriendsFollowersUiModel>()
        if (userSearchState.isSearch) newList.addAll(userSearchState.searchList) else newList.addAll(
            currentList
        )
        _sameSubscriptionState.postValue(
            UserFriendsFollowersUiState.SuccessGetList(
                isShowProgress = false,
                friendsList = newList,
                isRefreshing = isRefreshing
            )
        )
    }

    private fun handleListResponse(
        ownUserId: Long,
        mutualUsers: List<UserSimple?>?,
        offset: Int,
        clickedItem: FriendsFollowersUiModel?
    ) {
        mutualUsers?.let { mutualUsersListNotNull ->
            val newList = mapper.mapFromUserSimpleListToUiModel(
                responseList = mutualUsersListNotNull,
                myUserId = ownUserId
            )
            if (offset == DEFAULT_OFFSET) {
                createNewList(newList, clickedItem)
            } else {
                addAllList(newList, clickedItem)
            }
            handleUiState()
        }
    }

    private fun handleUiState() {
        when {
            currentList.isEmpty() || (userSearchState.isSearch
                && userSearchState.searchList.isEmpty()) -> pushPlaceholderState()
            else -> pushUiStateList(false)
        }
    }

    private fun pushPlaceholderState() {
        _sameSubscriptionState.postValue(
            UserFriendsFollowersUiState.ListEmpty(userSearchState.isSearch)
        )
    }

    private fun findItemByIdFromCurrentList(
        userId: Long
    ): FriendsFollowersUiModel? {
        return currentList.find { item ->
            userId == item.userSimple?.userId
        }
    }

    /**
     * В данном методе мы изменяем index основого списка currentList
     * Кейс в том, что если происходит поиск и мы изменяем состояние иконки, то будет обновлен только
     * searchList. Основной список тоже будет обновлен.
     */
    private fun checkIsNeedReplaceIndex(
        newList: List<FriendsFollowersUiModel>,
        clickedItem: FriendsFollowersUiModel?
    ) {
        clickedItem?.let { item ->
            val index = currentList.indexOf(item)
            newList.find { currentModel ->
                currentModel.userSimple?.userId == clickedItem.userSimple?.userId
            }.let { uiModel ->
                currentList[index] = uiModel ?: return
            }
        }
    }

    private fun handleErrorResponse(t: Throwable) {
        Timber.e(t)
        pushUiStateList(isRefreshing = false)
    }

    /**
     * Определяем, что идет ли сейчас поиск.
     * Если данные пришли вне поиска, то устанавливаем значения в currentList
     */
    private fun createNewList(
        newList: List<FriendsFollowersUiModel>,
        clickedItem: FriendsFollowersUiModel?
    ) {
        if (userSearchState.isSearch) {
            userSearchState.searchList = newList.toMutableList()
            checkIsNeedReplaceIndex(newList, clickedItem)
        } else currentList = newList.toMutableList()
    }

    /**
     * Устанавливаются значения, если отрабатывает пагинация
     */
    private fun addAllList(
        newList: List<FriendsFollowersUiModel>,
        clickedItem: FriendsFollowersUiModel?
    ) {
        if (userSearchState.isSearch) {
            userSearchState.searchList.addAll(newList)
            checkIsNeedReplaceIndex(newList, clickedItem)
        } else currentList.addAll(newList)
    }

    /**
     * Тут мы будем слушать изменения, когда юзер меняет состояние друга:
     * 1.Подписывается
     * 2.Отписывается
     * 3.Добавляет в друзья
     * 4.Удаляет из друзей
     */
    private fun observeFriendStatusChanged() {
        getUserSettingsStateChangedUseCase.invoke()
            .onEach(::handleFriendStatusChanged)
            .launchIn(viewModelScope)
    }

    private fun handleFriendStatusChanged(event: UserSettingsEffect) {
        when (event) {
            is UserSettingsEffect.UserFriendStatusChanged -> {
                val itemSelected = findItemByIdFromCurrentList(event.userId)
                itemSelected?.let { selectedItemNotNull ->
                    emitViewEvent(UserSubscriptionViewEvent.RefreshUserList(selectedItemNotNull))
                }
            }
            is UserSettingsEffect.UserBlockStatusChanged -> {
                Timber.d("Is user blocked me: ${event.isUserBlocked}")
                val itemSelected = findItemByIdFromCurrentList(event.userId)
                itemSelected?.let { selectedItemNotNull ->
                    emitViewEvent(UserSubscriptionViewEvent.RefreshUserList(selectedItemNotNull))
                }
            }
            else -> Unit
        }
    }

    private fun emitViewEvent(typeEvent: UserSubscriptionViewEvent) {
        viewModelScope.launch {
            _userMutualSubscriptionViewEvent.emit(typeEvent)
        }
    }

    /**
     * Данный класс будет хранить состояние поиска
     */
    private inner class UserSearchState {
        var isSearch = false
        var searchInputText: String? = null

        /**
         * Данный список хранит весь поисковый массив. Устанавливаются значения в случае, когда идет поиск
         * И очищается, когда юзер нажал кнопку "Назад"
         */
        var searchList = mutableListOf<FriendsFollowersUiModel>()

        fun resetSearch() {
            isSearch = false
            searchInputText = null
        }

        fun searchOpen() {
            isSearch = true
        }

        fun setSearchInput(searchInput: String?) {
            searchInputText = searchInput
        }

        fun resetSearchList() = searchList.clear()
    }

    companion object {
        private const val DEFAULT_PAGE_LIMIT = 100
        private const val DEFAULT_OFFSET = 0
    }
}
