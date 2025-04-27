package com.numplates.nomera3.modules.userprofile.ui.model

import com.meera.core.utils.TopAuthorApprovedUserModel
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum

data class ProfileToolbarModelUIModel(
    val topContentParams: TopAuthorApprovedUserModel,
    val isMe: Boolean,
    val profileDeleted: Boolean,
    val blacklistedMe: Boolean,
    val accountType: AccountTypeEnum,
    val name: String?,
)
