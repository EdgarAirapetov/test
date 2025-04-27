package com.numplates.nomera3.presentation.view.ui.mediaViewer.viewer.view

import com.meera.core.dialogs.unlimiteditem.MeeraConfirmDialogUnlimitedNumberItemsAction

sealed class MeeraMediaViewerMenuAction : MeeraConfirmDialogUnlimitedNumberItemsAction {
    data object Share : MeeraMediaViewerMenuAction()
    data object Save : MeeraMediaViewerMenuAction()
    data object AddFavorite : MeeraMediaViewerMenuAction()
    data object RemoveFavorite : MeeraMediaViewerMenuAction()
    data object Copy : MeeraMediaViewerMenuAction()
}
