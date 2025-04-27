package com.numplates.nomera3.presentation.view.fragments.entity

import androidx.annotation.StringRes

sealed class UserFriendActionViewEvent {

    class ShowSuccessSnackBar(
        @StringRes var messageRes: Int
    ) : UserFriendActionViewEvent()

    class ShowErrorSnackBar(
        @StringRes var errorMessageRes: Int
    ) : UserFriendActionViewEvent()
}
