package com.numplates.nomera3.presentation.view.fragments.entity

/**
 * Состояние для фрагмента
 * [com.numplates.nomera3.presentation.view.fragments.privacysettings.FriendsFollowersPrivacyFragment]
 * @param isButtonEnabled - Устанавливается состояние кнопки. будет false, если юзер не выбрал пункт
 * приватности
 * @param buttonText - Устанавливается текст для кнопки в зависимости от текущий page во ViewPager
 */
data class FriendsFollowersPrivacyUiState(
    var isButtonEnabled: Boolean,
    var buttonText: String
)