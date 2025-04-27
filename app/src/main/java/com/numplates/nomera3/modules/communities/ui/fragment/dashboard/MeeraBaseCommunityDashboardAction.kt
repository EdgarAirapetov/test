package com.numplates.nomera3.modules.communities.ui.fragment.dashboard

sealed class MeeraBaseCommunityDashboardAction {
    class SettingsGroup(
        val clickableState: (isClickable: Boolean) -> Unit,
        val progressBarState: (isVisible: Boolean) -> Unit
    ) : MeeraBaseCommunityDashboardAction()

    class Users(
        val clickableState: (isClickable: Boolean) -> Unit,
        val progressBarState: (isVisible: Boolean) -> Unit
    ) : MeeraBaseCommunityDashboardAction()

    class BlackList(
        val clickableState: (isClickable: Boolean) -> Unit,
        val progressBarState: (isVisible: Boolean) -> Unit
    ) : MeeraBaseCommunityDashboardAction()

    class DeleteGroup(
        val clickableState: (isClickable: Boolean) -> Unit,
        val progressBarState: (isVisible: Boolean) -> Unit
    ) : MeeraBaseCommunityDashboardAction()
}
