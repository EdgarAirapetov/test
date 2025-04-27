package com.numplates.nomera3.modules.bump.ui.entity

sealed class ShakeRequestsContentActions {

    object OnFriendActionButtonClicked: ShakeRequestsContentActions()

    object OnFriendDeclineFriendRequestClicked : ShakeRequestsContentActions()

    object OnCloseShakeUserClicked : ShakeRequestsContentActions()

    object TryToRequestNextUserAction : ShakeRequestsContentActions()
}
