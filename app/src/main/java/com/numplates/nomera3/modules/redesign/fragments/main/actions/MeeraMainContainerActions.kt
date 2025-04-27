package com.numplates.nomera3.modules.redesign.fragments.main.actions

sealed interface MeeraMainContainerActions {
    data object InitNotificationCounter: MeeraMainContainerActions
    data object PreloadPeopleContent : MeeraMainContainerActions
}
