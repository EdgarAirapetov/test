package com.numplates.nomera3.modules.userprofile.ui.fragment

import com.meera.core.dialogs.unlimiteditem.MeeraConfirmDialogUnlimitedNumberItemsAction

sealed class MeeraUnsubscribeMenuAction : MeeraConfirmDialogUnlimitedNumberItemsAction {
    data object Unsubscribe : MeeraUnsubscribeMenuAction()
    data object Subscribe : MeeraUnsubscribeMenuAction()
    data object RemoveFriend : MeeraUnsubscribeMenuAction()
    data object RemoveUnsubscribeFriend : MeeraUnsubscribeMenuAction()
    data object DisableNotification : MeeraUnsubscribeMenuAction()
    data object EnableNotification : MeeraUnsubscribeMenuAction()
    data object AddFriend : MeeraUnsubscribeMenuAction()
    data object AcceptRequest : MeeraUnsubscribeMenuAction()
    data object RejectRequest : MeeraUnsubscribeMenuAction()
    data object RejectRequestUnsubscribe : MeeraUnsubscribeMenuAction()
}
