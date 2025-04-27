package com.numplates.nomera3.modules.peoples.ui.entity

sealed class PeoplesCommunitiesContainerState {

    /**
     * Состояние, когда в контейнер будет добавлен
     * [com.numplates.nomera3.modules.peoples.ui.fragments.PeoplesFragment]
     */
    object PeoplesState : PeoplesCommunitiesContainerState()

    /**
     * Состояние, когда в контейнер будет добавлен
     * [com.numplates.nomera3.modules.communities.ui.fragment.list.CommunitiesListsContainerFragment]
     */
    object CommunitiesState : PeoplesCommunitiesContainerState()
}
