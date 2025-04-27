package com.numplates.nomera3.modules.maps.data.repository

import com.meera.core.di.scopes.AppScope
import com.numplates.nomera3.data.network.ApiMain
import com.numplates.nomera3.modules.maps.data.mapper.MapEventsDataMapper
import com.numplates.nomera3.modules.maps.domain.events.model.GetMapFriendsParamsModel
import com.numplates.nomera3.modules.maps.domain.repository.MapFriendsRepository
import com.numplates.nomera3.modules.userprofile.domain.model.usermain.UserSimpleModel
import javax.inject.Inject

@AppScope
class MapFriendsRepositoryImpl @Inject constructor(
    private val apiMain: ApiMain,
    private val dataMapper: MapEventsDataMapper
) : MapFriendsRepository {

    override suspend fun getFriends(params: GetMapFriendsParamsModel): List<UserSimpleModel> =
        dataMapper.mapParticipants(
            apiMain.getMapFriends(
                offset = params.offset,
                limit = params.limit,
                search = params.search
            ).data
        )
}
