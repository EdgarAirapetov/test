package com.numplates.nomera3.modules.userprofile.ui.mapper

import com.numplates.nomera3.modules.userprofile.domain.model.ProfileSuggestionModel
import com.numplates.nomera3.modules.userprofile.ui.entity.ProfileSuggestionUiModels
import javax.inject.Inject

class UserSuggestionsUiMapper @Inject constructor() {

    fun mapSuggestionToUiModel(
        suggestions: List<ProfileSuggestionModel>,
        allowSyncContacts: Boolean
    ): List<ProfileSuggestionUiModels> {
        val listResult = mutableListOf<ProfileSuggestionUiModels>()
        val suggestionMapped = suggestions.map { model ->
            ProfileSuggestionUiModels.ProfileSuggestionUiModel(
                userId = model.userId,
                avatarLink = model.avatarLink,
                name = model.name,
                uniqueName = model.uniqueName,
                cityName = model.cityName,
                isApproved = model.isApproved,
                isTopContentMaker = model.isTopContentMaker,
                accountType = model.accountType,
                mutualFriendsCount = model.mutualFriendsCount,
                gender = model.gender
            )
        }
        listResult.addAll(suggestionMapped)
        if (allowSyncContacts.not()) {
            listResult.add(ProfileSuggestionUiModels.SuggestionSyncContactUiModel())
        }
        return listResult
    }
}
