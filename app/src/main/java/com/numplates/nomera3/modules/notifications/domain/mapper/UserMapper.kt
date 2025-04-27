package com.numplates.nomera3.modules.notifications.domain.mapper

import com.meera.db.models.notifications.UserEntity
import com.numplates.nomera3.modules.notifications.domain.entity.User
import io.reactivex.functions.Function

class UserMapper : Function<UserEntity, User> {

    override fun apply(t: UserEntity): User =
        User(
            userId = t.userId,
            accountType = t.accountType,
            name = t.name,
            gender = t.gender,
            avatarBig = t.avatar.big,
            avatarSmall = t.avatar.small,
            accountColor = t.accountColor,
            birthday = t.birthday,
            hasMoments = t.hasMoments,
            hasNewMoments = t.hasNewMoments
        )

}
