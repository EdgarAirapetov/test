package com.numplates.nomera3.modules.feed.domain.usecase

import com.numplates.nomera3.modules.feed.data.repository.RoadSuggestsRepository
import com.numplates.nomera3.modules.userprofile.domain.model.ProfileSuggestionModel
import javax.inject.Inject

class GetRoadSuggestsUseCase @Inject constructor(
    private val roadSuggestsRepository: RoadSuggestsRepository
) {

    suspend fun invoke(): List<ProfileSuggestionModel> {
        return roadSuggestsRepository.getSuggestions()
    }

}
