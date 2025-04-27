package com.numplates.nomera3.presentation.viewmodel.viewevents

sealed class GroupUsersListEvents {

    object OnErrorLoadigUsers: GroupUsersListEvents()
    object OnClearAdapter: GroupUsersListEvents()

    object FailedToDeleteUser: GroupUsersListEvents()
    object FailedToBlockUser: GroupUsersListEvents()
    object SuccessToBlockUser: GroupUsersListEvents()

}