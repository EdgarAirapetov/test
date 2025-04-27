package com.numplates.nomera3.modules.userprofile.domain.usecase

import com.numplates.nomera3.modules.userprofile.data.repository.ProfileRepository
import com.numplates.nomera3.modules.userprofile.domain.model.ProfileSuggestionModel
import javax.inject.Inject

class GetProfileSuggestionsUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {

    suspend fun invoke(userId: Long): List<ProfileSuggestionModel> {
        return profileRepository.getProfileSuggestions(userId)
    }

}
