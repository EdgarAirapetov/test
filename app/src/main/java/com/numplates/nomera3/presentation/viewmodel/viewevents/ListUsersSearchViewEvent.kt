package com.numplates.nomera3.presentation.viewmodel.viewevents

sealed class ListUsersSearchViewEvent {

    class OnAddUsersDone(
            var checkedCount: Int = 0,
            var uncheckedCount: Int = 0
    ) : ListUsersSearchViewEvent()

    class OnSuccessRemoveUser(val position: Int) : ListUsersSearchViewEvent()

    object OnFailureRemoveUser : ListUsersSearchViewEvent()

    object OnErrorLoadUsers : ListUsersSearchViewEvent()

    object OnErrorAddUsers : ListUsersSearchViewEvent()

    object OnSuccessRemoveAllUsers : ListUsersSearchViewEvent()

    object OnErrorRemoveAllUsers : ListUsersSearchViewEvent()

    class OnUserCounter (val count: Long?): ListUsersSearchViewEvent()

}