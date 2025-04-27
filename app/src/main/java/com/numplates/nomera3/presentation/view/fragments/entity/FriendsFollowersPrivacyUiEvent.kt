package com.numplates.nomera3.presentation.view.fragments.entity

sealed class FriendsFollowersPrivacyUiEvent {

    /**
     * Будет отображать одноразовый эвент, когда возникла ошибка выбора пункта конфиденциальности
     */
    object ErrorSelectPrivacyUi : FriendsFollowersPrivacyUiEvent()
}
