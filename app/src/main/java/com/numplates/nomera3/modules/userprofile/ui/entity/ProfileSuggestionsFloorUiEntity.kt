package com.numplates.nomera3.modules.userprofile.ui.entity

import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.userprofile.ui.adapter.UserProfileAdapterType

data class ProfileSuggestionsFloorUiEntity(
    val userType: AccountTypeEnum,
    var suggestions: List<ProfileSuggestionUiModels>
) : UserUIEntity {
    override val type: UserProfileAdapterType
        get() = UserProfileAdapterType.PROFILE_SUGGESTIONS
}
