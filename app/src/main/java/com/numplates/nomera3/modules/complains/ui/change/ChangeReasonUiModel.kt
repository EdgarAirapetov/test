package com.numplates.nomera3.modules.complains.ui.change

import com.numplates.nomera3.modules.complains.ui.model.UserComplainUiModel

data class ChangeReasonUiModel(
    val complainUiModel: UserComplainUiModel,
    val isChecked: Boolean = false,
)
