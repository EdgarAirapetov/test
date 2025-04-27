package com.numplates.nomera3.modules.peoples.ui.entity

import com.numplates.nomera3.modules.peoples.ui.content.entity.PeoplesContentUiEntity

sealed class PeopleUiStates {

    abstract val contentList: List<PeoplesContentUiEntity>
    abstract val isRefreshing: Boolean
    abstract val showProgressBar: Boolean

    /**
     * Состояние, когда должен отображаться контент с shimmer
     */
    data class LoadingState(
        override val contentList: List<PeoplesContentUiEntity>,
        override val isRefreshing: Boolean = false,
        override val showProgressBar: Boolean = false
    ) : PeopleUiStates()

    /**
     * Состояние, когда отображается основной контент
     */
    data class PeoplesContentUiState(
        override val contentList: List<PeoplesContentUiEntity>,
        override val isRefreshing: Boolean = false,
        override val showProgressBar: Boolean = false
    ) : PeopleUiStates()
}
