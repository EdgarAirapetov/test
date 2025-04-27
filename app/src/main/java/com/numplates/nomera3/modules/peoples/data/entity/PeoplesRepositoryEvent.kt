package com.numplates.nomera3.modules.peoples.data.entity

sealed class PeoplesRepositoryEvent {
    object SelectCommunityViewEvent : PeoplesRepositoryEvent()
    object SelectPeopleViewEvent : PeoplesRepositoryEvent()
}
