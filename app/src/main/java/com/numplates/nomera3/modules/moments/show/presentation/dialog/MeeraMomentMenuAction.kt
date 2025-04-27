package com.numplates.nomera3.modules.moments.show.presentation.dialog

import com.meera.core.dialogs.unlimiteditem.MeeraConfirmDialogUnlimitedNumberItemsAction

sealed class MeeraMomentMenuAction : MeeraConfirmDialogUnlimitedNumberItemsAction {
    data object Share : MeeraMomentMenuAction()
    data object CopyLink : MeeraMomentMenuAction()
    data object MomentDownload : MeeraMomentMenuAction()
    data object AllowComments : MeeraMomentMenuAction()
    data object Settings : MeeraMomentMenuAction()
    data object Delete : MeeraMomentMenuAction()
    data object ComplainMoment : MeeraMomentMenuAction()
    data object ShowMomentUser : MeeraMomentMenuAction()
    data object HideMomentUser : MeeraMomentMenuAction()
}
