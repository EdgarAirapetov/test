package com.numplates.nomera3.modules.search.ui.entity.event

sealed class GroupSearchViewEvent : SearchBaseViewEvent() {
    data class SelectGroup(val groupId: Int) : GroupSearchViewEvent()
}