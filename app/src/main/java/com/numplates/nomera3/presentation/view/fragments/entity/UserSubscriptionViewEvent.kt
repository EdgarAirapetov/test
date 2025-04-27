package com.numplates.nomera3.presentation.view.fragments.entity

import androidx.annotation.StringRes
import com.numplates.nomera3.presentation.model.adaptermodel.FriendsFollowersUiModel

sealed class UserSubscriptionViewEvent {

    /**
     * Данный event обновляет список если меняется состояние иконки
     */
    class RefreshUserList(var clickedItem: FriendsFollowersUiModel) : UserSubscriptionViewEvent()

    class ShowSuccessSnackBar(
        @StringRes var messageRes: Int
    ) : UserSubscriptionViewEvent()

    class ShowErrorSnackBar(
        @StringRes var errorMessageRes: Int
    ) : UserSubscriptionViewEvent()
}
