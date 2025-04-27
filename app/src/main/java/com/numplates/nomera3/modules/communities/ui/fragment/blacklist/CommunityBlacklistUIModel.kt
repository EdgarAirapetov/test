package com.numplates.nomera3.modules.communities.ui.fragment.blacklist

sealed class CommunityBlacklistUIModel {
    data class BlacklistHeaderUIModel(
        var listLength: Int
    ) : CommunityBlacklistUIModel()

    data class BlacklistedMemberUIModel(
        val memberId: Long,
        val memberPhotoUrl: String,
        val memberName: String,
        val uniqueName: String
    ) : CommunityBlacklistUIModel()

    object BlacklistClearButtonUIModel : CommunityBlacklistUIModel()
}
