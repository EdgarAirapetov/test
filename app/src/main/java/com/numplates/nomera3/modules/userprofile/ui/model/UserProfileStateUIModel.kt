package com.numplates.nomera3.modules.userprofile.ui.model

import com.meera.core.base.viewmodel.State
import com.numplates.nomera3.modules.userprofile.ui.entity.UserUIEntity

data class UserProfileStateUIModel(
    val profile: UserProfileUIModel,
    val profileUIList: List<UserUIEntity>
): State
