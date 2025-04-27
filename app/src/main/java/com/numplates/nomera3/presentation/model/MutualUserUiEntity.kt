package com.numplates.nomera3.presentation.model

import com.numplates.nomera3.modules.baseCore.AccountTypeEnum

@Deprecated("Используйте MutualUsersUiModel")
data class MutualUserUiEntity(
    var userData: MutualUser?,
    var iconWidth: Float,
    var iconHeight: Float,
    var accountTypeEnum: AccountTypeEnum
)
