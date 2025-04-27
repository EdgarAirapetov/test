package com.numplates.nomera3.modules.userprofile.ui.model


import com.numplates.nomera3.modules.userprofile.ui.fragment.UserInfoRecyclerData

data class MeeraUserInfoStateUiModel(
    val profile: UserProfileUIModel,
    val profileUIList: List<UserInfoRecyclerData>,
    val scrollToTop: Boolean = false
)
