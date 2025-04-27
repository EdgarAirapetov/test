package com.numplates.nomera3.modules.userprofile.domain.model

data class UserAvatarsModel(
    val avatars: List<AvatarModel>,
    val count: Int,
    val moreItems: Boolean
)
