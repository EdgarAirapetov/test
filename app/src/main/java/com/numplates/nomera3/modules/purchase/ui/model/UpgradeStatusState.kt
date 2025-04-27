package com.numplates.nomera3.modules.purchase.ui.model

import com.numplates.nomera3.presentation.view.widgets.UserInfoUIEntity


data class UpgradeStatusUIState(
    val name: String,
    val accountColor: Int?,
    val accountType: Int?,
    val accountTypeExpiration: Long?,
    val avatar: String,
    val birthday: Long?,
    val hideBirthday: Int?,
    val cityName: String?,
    val vehicles: List<UserInfoUIEntity>
)
