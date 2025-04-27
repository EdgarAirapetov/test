package com.numplates.nomera3.modules.maps.domain.repository

import com.numplates.nomera3.modules.maps.domain.events.model.GetMapFriendsParamsModel
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserSimpleModel

interface MapFriendsRepository {

    suspend fun getFriends(params: GetMapFriendsParamsModel): List<UserSimpleModel>
}
