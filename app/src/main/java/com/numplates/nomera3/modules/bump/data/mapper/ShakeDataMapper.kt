package com.numplates.nomera3.modules.bump.data.mapper

import com.numplates.nomera3.data.network.UpdateFriendshipDtoModel
import com.numplates.nomera3.domain.model.UpdateFriendshipModel
import com.numplates.nomera3.modules.bump.data.entity.ShakeDataEvent
import com.numplates.nomera3.modules.bump.data.entity.ShakeMutualUserDto
import com.numplates.nomera3.modules.bump.data.entity.UserShakeDtoModel
import com.numplates.nomera3.modules.bump.domain.entity.ShakeEvent
import com.numplates.nomera3.modules.bump.domain.entity.ShakeMutualUserModel
import com.numplates.nomera3.modules.bump.domain.entity.ShakeMutualUsersModel
import com.numplates.nomera3.modules.bump.domain.entity.UserShakeModel
import javax.inject.Inject

class ShakeDataMapper @Inject constructor() {

    fun map(users: List<UserShakeDtoModel>): List<UserShakeModel> = users.map { dtoEntity ->
        UserShakeModel(
            userId = dtoEntity.userId,
            name = dtoEntity.name.orEmpty(),
            uniqueName = dtoEntity.uniqueName.orEmpty(),
            birthday = dtoEntity.birthday ?: 0,
            avatarSmall = dtoEntity.avatarSmall.orEmpty(),
            gender = dtoEntity.gender ?: 0,
            accountType = dtoEntity.accountType ?: 0,
            accountColor = dtoEntity.accountColor ?: 0,
            approved = dtoEntity.approved ?: 0,
            topContentMaker = dtoEntity.topContentMaker ?: 0,
            complete = dtoEntity.complete ?: 0,
            cityId = dtoEntity.city?.id ?: 0,
            city = dtoEntity.city?.name.orEmpty(),
            countryId = dtoEntity.country?.id ?: 0,
            country = dtoEntity.country?.name.orEmpty(),
            isFriends = dtoEntity.isFriends ?: 0,
            mutualUserModel = if (dtoEntity.hasMutualUsers) {
                ShakeMutualUsersModel(
                    mutualUsers = dtoEntity.mutualUsers?.users?.map(::mapShakeMutualUser) ?: mutableListOf(),
                    moreCount = dtoEntity.mutualUsers?.moreCount ?: 0
                )
            } else null
        )
    }

    private fun mapShakeMutualUser(model: ShakeMutualUserDto): ShakeMutualUserModel {
        return ShakeMutualUserModel(
            userId = model.userId,
            name = model.name,
            avatarLink = model.avatar?.small
        )
    }

    fun mapShakeDataEvent(event: ShakeDataEvent): ShakeEvent = when (event) {
        is ShakeDataEvent.TryToRegisterShakeEvent -> ShakeEvent.TryToRegisterShakeEvent(event.isNeedToRegister)
        is ShakeDataEvent.ForceToRegisterShakeEvent -> ShakeEvent.ForceToRegisterShakeEvent(event.isNeedToRegister)
        is ShakeDataEvent.ShakeUserNotFoundEvent -> ShakeEvent.ShakeUsersNotFound
    }

    fun mapUpdateFriendship(dtoModel: UpdateFriendshipDtoModel): UpdateFriendshipModel {
        return UpdateFriendshipModel(
            userId = dtoModel.friendId,
            action = dtoModel.action
        )
    }
}
