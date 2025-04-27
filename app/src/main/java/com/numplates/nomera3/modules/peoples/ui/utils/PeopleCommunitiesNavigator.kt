package com.numplates.nomera3.modules.peoples.ui.utils

/**
 * Данный Navigator служит для того, чтобы переключаться между фрагментами:
 * [com.numplates.nomera3.modules.peoples.ui.fragments.PeoplesFragment]
 * [com.numplates.nomera3.modules.communities.ui.fragment.list.CommunitiesListsContainerFragment]
 */
interface PeopleCommunitiesNavigator {
    /**
     * Метод добавит фрагмент с людьми [com.numplates.nomera3.modules.peoples.ui.fragments.PeoplesFragment]
     * в контейнер [com.numplates.nomera3.modules.peoples.ui.fragments.PeoplesCommunitiesContainerFragment]
     */
    fun selectPeople()

    /**
     * Метод добавит фрагмент с группами
     * [com.numplates.nomera3.modules.communities.ui.fragment.list.CommunitiesListsContainerFragment]
     * в контейнер [com.numplates.nomera3.modules.peoples.ui.fragments.PeoplesCommunitiesContainerFragment]
     */
    fun selectCommunities()

    /**
     * @return Добавлен ли фрагмент с группами в контейнер
     */
    fun isCommunitySelected(): Boolean
}
