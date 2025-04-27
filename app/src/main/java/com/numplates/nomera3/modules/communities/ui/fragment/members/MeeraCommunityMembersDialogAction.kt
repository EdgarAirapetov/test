package com.numplates.nomera3.modules.communities.ui.fragment.members

sealed class MeeraCommunityMembersDialogAction {
    object FirstMenuItemClick: MeeraCommunityMembersDialogAction()
    object SecondMenuItemClick: MeeraCommunityMembersDialogAction()
    object ThirdMenuItemClick: MeeraCommunityMembersDialogAction()
}
