package com.numplates.nomera3.modules.feed.data.repository

import com.numplates.nomera3.modules.feed.data.api.RoadApi
import com.numplates.nomera3.modules.userprofile.data.mapper.ProfileSuggestionMapper
import com.numplates.nomera3.modules.userprofile.domain.model.ProfileSuggestionModel
import javax.inject.Inject

class RoadSuggestsRepositoryImpl @Inject constructor(
    private val api: RoadApi,
    private val profileSuggestionMapper: ProfileSuggestionMapper
) : RoadSuggestsRepository {

    override suspend fun getSuggestions(): List<ProfileSuggestionModel> {
        val users = api.getRoadSuggestions().data.users
        return users.map(profileSuggestionMapper::mapUserSimpleDtoToModel)
    }
}
