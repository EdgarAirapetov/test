package com.numplates.nomera3.modules.feed.data.repository

import com.numplates.nomera3.modules.userprofile.domain.model.ProfileSuggestionModel

interface RoadSuggestsRepository {

    suspend fun getSuggestions(): List<ProfileSuggestionModel>

}
