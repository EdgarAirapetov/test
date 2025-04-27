package com.numplates.nomera3.presentation.view.adapter.newfriends

import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.data.network.UserInfoModelRestOld
import com.numplates.nomera3.data.network.UserModel
import com.numplates.nomera3.data.network.UserSearchByNameModel
import com.numplates.nomera3.data.newmessenger.FriendEntity

data class FriendModel(
        var userModel: UserModel,
        var type: Int,
        var needSeparator: Boolean = true,
        var userSimple: UserSimple? = null
) {

    constructor(old: UserInfoModelRestOld, type: Int) : this(
            UserModel(old.userId.toLong(),
                    old.name,
                    old.birthday,
                    old.avatar,
                    old.accountType,
                    old.accountColor,
                    old.gender),
            type
    )

    constructor(friendsEntity:
                FriendEntity, type: Int) : this(
            UserModel(friendsEntity.id,
                    friendsEntity.name ?: "",
                    friendsEntity.birthday,
                    friendsEntity.avatarBig,
                    friendsEntity.type,
                    friendsEntity.color,
                    friendsEntity.gender ?: 0,
                    city = friendsEntity.city ?: ""),
            type
    )

    constructor(model: UserSearchByNameModel, type: Int) : this(
            UserModel(model.userId.toLong(),
                    model.name,
                    model.birthday,
                    model.avatar,
                    model.accountType,
                    model.accountColor,
                    model.gender,
                    city = model.cityName,
                    approved = model.approved),
            type
    )

    constructor(friendsEntity: UserSimple, type: Int) : this(
            UserModel(friendsEntity.userId,
                    friendsEntity.name ?: "",
                    friendsEntity.birthday,
                    friendsEntity.avatarSmall,
                    friendsEntity.accountType ?: 0,
                    friendsEntity.accountColor,
                    friendsEntity.gender ?: 0,
                    city = friendsEntity.city?.name ?: "",
                    settingsFlags = friendsEntity.settingsFlags,
                    approved = friendsEntity.approved),
            type,
            userSimple = friendsEntity
    )
}
