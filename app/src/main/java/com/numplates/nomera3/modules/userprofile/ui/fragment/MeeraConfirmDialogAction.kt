package com.numplates.nomera3.modules.userprofile.ui.fragment

import com.meera.core.dialogs.unlimiteditem.MeeraConfirmDialogUnlimitedNumberItemsAction

sealed class MeeraConfirmDialogAction : MeeraConfirmDialogUnlimitedNumberItemsAction {
    data object ShareProfile : MeeraConfirmDialogAction()
    data class CopyLink(val profileLink: String, val uniquename: String) : MeeraConfirmDialogAction()
    data object MessageBlock : MeeraConfirmDialogAction()
    data object CallBlock : MeeraConfirmDialogAction()
    data object HidePostRoad : MeeraConfirmDialogAction()
    data object ReportProfile : MeeraConfirmDialogAction()
    data object UserBlock : MeeraConfirmDialogAction()
    data object Settings : MeeraConfirmDialogAction()
    data object CreateMoment : MeeraConfirmDialogAction()
    data object AddAvatar : MeeraConfirmDialogAction()
    data object CreateAvatar : MeeraConfirmDialogAction()
}
