package com.numplates.nomera3.modules.newroads.fragments

sealed class MeeraPostDotsMenuAction {
    object EditPost: MeeraPostDotsMenuAction()
    object SaveToDevice: MeeraPostDotsMenuAction()
    object SubscribeToPost: MeeraPostDotsMenuAction()
    object SubscribeToProfile: MeeraPostDotsMenuAction()
    object SharePost: MeeraPostDotsMenuAction()
    object CopyLink: MeeraPostDotsMenuAction()
    object HideAllProfilePost: MeeraPostDotsMenuAction()
    object ComplainPost: MeeraPostDotsMenuAction()
    object DeletePost: MeeraPostDotsMenuAction()
}
