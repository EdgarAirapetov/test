package com.numplates.nomera3.modules.tags.domain.mappers

import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.modules.tags.ui.entity.UITagEntity

fun UserSimple.toUITagEntity(): UITagEntity {
    return UITagEntity(
            id = this.userId,
            image = this.avatarSmall,
            uniqueName = this.uniqueName,
            userName = this.name,
            isMale = this.gender,
            isVerified = this.profileVerified
    )
}
