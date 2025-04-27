package com.numplates.nomera3.modules.userprofile.data.mapper

import com.meera.core.extensions.toBoolean
import com.numplates.nomera3.modules.feed.domain.mapper.toPostUIEntity
import com.numplates.nomera3.modules.userprofile.data.entity.AvatarDto
import com.numplates.nomera3.modules.userprofile.data.entity.UserAvatarsDto
import com.numplates.nomera3.modules.userprofile.domain.model.AvatarModel
import com.numplates.nomera3.modules.userprofile.domain.model.UserAvatarsModel
import javax.inject.Inject

class UserAvatarsMapper @Inject constructor() {

    fun userAvatarsDtoToUserAvatarsModel(userAvatarsDto: UserAvatarsDto): UserAvatarsModel {
        val avatarModels = userAvatarsDto.avatars.map { avatarDtoToAvatarModel(it) }
        return UserAvatarsModel(
            avatars = avatarModels,
            userAvatarsDto.count,
            moreItems = userAvatarsDto.moreItems.toBoolean()
        )
    }

    fun avatarDtoToAvatarModel(avatarDto: AvatarDto) = AvatarModel(
        animation = avatarDto.animation,
        big = avatarDto.big,
        id = avatarDto.id,
        main = avatarDto.main.toBoolean(),
        post = avatarDto.post?.toPostUIEntity(),
        postId = avatarDto.postId,
        small = avatarDto.small,
        userId = avatarDto.userId,
        isAdult = avatarDto.isAdult == 1
    )
}
