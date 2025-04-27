package com.numplates.nomera3.modules.notifications.data.mapper

import com.meera.core.extensions.toBoolean
import com.meera.db.models.notifications.AvatarMetaEntity
import com.meera.db.models.notifications.UserEntity
import com.numplates.nomera3.modules.notifications.data.entity.UserEntityResponse
import io.reactivex.functions.Function

class UserMapper : Function<UserEntityResponse, UserEntity> {

    override fun apply(t: UserEntityResponse): UserEntity =
            UserEntity(
                    userId = t.userId,
                    accountType = t.accountType,
                    name = t.name,
                    gender = t.gender,
                    avatar = AvatarMetaEntity(
                            big = t.avatar?.big ?: "", small = t.avatar?.small ?: ""
                    ),
                    accountColor = t.accountColor,
                    birthday = t.birthday,
                    hasMoments = t.moments?.hasMoments?.toBoolean() ?: false,
                    hasNewMoments = t.moments?.hasNewMoments?.toBoolean() ?: false
            )

}
