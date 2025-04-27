package com.numplates.nomera3.presentation.view.fragments.entity

import com.numplates.nomera3.presentation.model.adaptermodel.FriendsFollowersUiModel

sealed class UserFriendsFollowersUiState {

    /**
     * Состояние, когда запрос успешно прошел или отрабатывает пагинация
     */
    data class SuccessGetList(
        val friendsList: List<FriendsFollowersUiModel>,
        val isShowProgress: Boolean,
        val isRefreshing: Boolean
    ) : UserFriendsFollowersUiState()

    /**
     * Состояние, когда у юзера список Друзей/Подписчиков/Подписок пуст
     * @param isSearch - Производится ли сейчас поиск
     */
    class ListEmpty constructor(var isSearch: Boolean) : UserFriendsFollowersUiState()
}
