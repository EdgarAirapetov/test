package com.numplates.nomera3.data.newmessenger

import com.meera.db.models.chatmembers.UserEntity

fun FriendEntity.toUserEntity(): UserEntity {
    return UserEntity(
        userId = id,
        name = name,
        avatarBig = avatarBig,
        avatarSmall = avatarSmall,
        birthday = birthday,
        city = city,
        color = color,
        gender = gender,
        status = status,
        type = type,
        uniqueName = uniqueName,
        phone = null,
        email = null,
        topContentMaker = topContentMaker
    )
}
