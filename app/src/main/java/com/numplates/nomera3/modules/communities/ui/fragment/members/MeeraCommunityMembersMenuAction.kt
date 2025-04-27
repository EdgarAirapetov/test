package com.numplates.nomera3.modules.communities.ui.fragment.members

import com.meera.core.dialogs.unlimiteditem.MeeraConfirmDialogUnlimitedNumberItemsAction

sealed class MeeraCommunityMembersMenuAction : MeeraConfirmDialogUnlimitedNumberItemsAction {
    data object Accept : MeeraCommunityMembersMenuAction()
    data object AcceptMakeAdmin : MeeraCommunityMembersMenuAction()
    data object Decline : MeeraCommunityMembersMenuAction()
    data object DeclineBlock : MeeraCommunityMembersMenuAction()
}
