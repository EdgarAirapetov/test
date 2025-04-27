package com.numplates.nomera3.modules.notifications.ui.mapper

import com.numplates.nomera3.modules.notifications.ui.entity.User
import io.reactivex.functions.Function

class UserMapper : Function<com.numplates.nomera3.modules.notifications.domain.entity.User, User> {

    override fun apply(t: com.numplates.nomera3.modules.notifications.domain.entity.User): User =
        User(
            userId = t.userId,
            accountType = t.accountType,
            name = t.name,
            gender = t.gender ?: 0,
            avatarBig = t.avatarBig ?: "",
            avatarSmall = t.avatarSmall ?: "",
            accountColor = t.accountColor,
            birthday = t.birthday,
            hasMoments = t.hasMoments,
            hasNewMoments = t.hasNewMoments
        )

}
